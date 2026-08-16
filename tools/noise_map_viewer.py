"""
Top down noise maps for the three terrain parameters, plus a larger map of the
surface height they blend into.

The three parameter maps are plain grayscale. The final map is grayscale too,
except that everything below sea level is drawn in blue, light in the shallows and
dark in the deeps.

The noise itself is never reimplemented here: tools.NoiseMapDumper samples it with
the real generator code (PerlinNoise + BlockGenerator.splineHeight) and this script
only drives it and draws the result. The class files therefore have to exist, so
compile the project first:

    mvn compile
    python tools/noise_map_viewer.py

Options:
    --seed N        override the seed (defaults to the one game/Main.java uses)
    --x-width N     starting width of the sampled area along x
    --z-width N     starting width of the sampled area along z

Every parameter is also a slider on the right hand side. The weights and the sea
level redraw instantly; the noise scales and the map widths have to resample, so
they apply when the mouse is released. Closing the window prints the tuned
constants, ready to paste back into Settings.java.

The defaults for all of them are read from Settings.java, so the view always starts
from what the game would currently generate.
"""

import argparse
import os
import re
import shutil
import struct
import subprocess
import sys
import tempfile

import numpy as np
import matplotlib.pyplot as plt
from matplotlib.colors import Normalize
from matplotlib.gridspec import GridSpec
from matplotlib.widgets import Slider

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SETTINGS_PATH = os.path.join(REPO_ROOT, "src", "main", "java", "game", "Settings.java")
MAIN_PATH = os.path.join(REPO_ROOT, "src", "main", "java", "game", "Main.java")
CLASSES_DIR = os.path.join(REPO_ROOT, "target", "classes")
DUMPER_CLASS = "tools.NoiseMapDumper"

DEFAULT_WIDTH = 1001  # x, z in [-500, 500]

WEIGHT_SLIDER_MAX = 2.0
SCALE_SLIDER_MIN = 0.00001
SCALE_SLIDER_MAX = 0.05
WIDTH_SLIDER_MIN = 51
WIDTH_SLIDER_MAX = 1001

# shallow water to deep water, as fractions of 255
SHALLOW_WATER = np.array([0.42, 0.75, 0.98])
DEEP_WATER = np.array([0.01, 0.09, 0.30])

PARAMETERS = [
    # (settings weight constant, settings scale constant, title)
    ("CONTINENTALNESS_WEIGHT", "CONTINENTALNESS_NOISE_SCALE", "Continentalness"),
    ("EROSION_WEIGHT", "EROSION_NOISE_SCALE", "Erosion"),
    ("PEAKS_AND_VALLEYS_WEIGHT", "PEAKS_AND_VALLEYS_NOISE_SCALE", "Peaks and Valleys"),
]


def read_java_constant(path, name, cast):
    """Pulls a single numeric constant out of a java source file."""
    with open(path, "r", encoding="utf-8") as f:
        source = f.read()
    match = re.search(name + r"\s*=\s*(-?[0-9.eE+-]+?)[fFdDlL]?\s*;", source)
    if match is None:
        raise RuntimeError("could not find " + name + " in " + path)
    return cast(match.group(1))


def read_seed():
    """The seed Main.java hands to the World constructor."""
    with open(MAIN_PATH, "r", encoding="utf-8") as f:
        source = f.read()
    match = re.search(r"int\s+seed\s*=\s*(-?[0-9]+)\s*;", source)
    if match is None:
        return 0
    return int(match.group(1))


def find_java():
    java_home = os.environ.get("JAVA_HOME")
    if java_home:
        candidate = os.path.join(java_home, "bin", "java")
        found = shutil.which(candidate) or shutil.which(candidate + ".exe")
        if found:
            return found
    found = shutil.which("java")
    if found is None:
        raise RuntimeError("java not found on PATH, and JAVA_HOME is not set")
    return found


def trim(value, decimals):
    """Formats a float without the trailing zeros, e.g. 0.5000 -> '0.5'."""
    text = ("%." + str(decimals) + "f") % value
    if "." in text:
        text = text.rstrip("0")
        if text.endswith("."):
            text += "0"
    return text


def format_settings(sea_level, scales, weights):
    """The tuned constants, written the way they are declared in Settings.java."""
    lines = ["    public static final int SEA_LEVEL = %d;" % sea_level]
    for (_, scale_name, _), value in zip(PARAMETERS, scales):
        lines.append(
            "    public static final double %s = %s;" % (scale_name, trim(value, 5))
        )
    for (weight_name, _, _), value in zip(PARAMETERS, weights):
        lines.append(
            "    public static final float %s = %sf;" % (weight_name, trim(value, 2))
        )
    return "\n".join(lines)


class NoiseMapViewer:

    def __init__(self, seed, x_width, z_width):
        self.seed = seed
        self.x_width = x_width
        self.z_width = z_width
        self.java = find_java()

        self.chunk_height = read_java_constant(SETTINGS_PATH, "CHUNK_HEIGHT", int)
        self.sea_level = read_java_constant(SETTINGS_PATH, "SEA_LEVEL", int)
        self.weights = [
            read_java_constant(SETTINGS_PATH, weight, float)
            for weight, _, _ in PARAMETERS
        ]
        self.scales = [
            read_java_constant(SETTINGS_PATH, scale, float)
            for _, scale, _ in PARAMETERS
        ]

        self.dirty = False  # a slider that needs a resample has been touched
        self.maps = self.run_dumper()

    # the sampled rectangle is always centered on the origin
    @property
    def min_x(self):
        return -(self.x_width // 2)

    @property
    def max_x(self):
        return self.min_x + self.x_width - 1

    @property
    def min_z(self):
        return -(self.z_width // 2)

    @property
    def max_z(self):
        return self.min_z + self.z_width - 1

    def extent(self):
        return [self.min_x, self.max_x, self.max_z, self.min_z]

    def run_dumper(self):
        """Runs the java dumper and reads its three parameter maps back."""
        if not os.path.isdir(CLASSES_DIR):
            raise RuntimeError(CLASSES_DIR + " does not exist, run `mvn compile` first")

        handle, path = tempfile.mkstemp(suffix=".bin")
        os.close(handle)
        try:
            command = [
                self.java,
                "-cp",
                CLASSES_DIR,
                DUMPER_CLASS,
                str(self.seed),
                str(self.min_x),
                str(self.max_x),
                str(self.min_z),
                str(self.max_z),
                "%.10g" % self.scales[0],
                "%.10g" % self.scales[1],
                "%.10g" % self.scales[2],
                path,
            ]
            result = subprocess.run(command, capture_output=True, text=True)
            if result.returncode != 0:
                raise RuntimeError("dumper failed:\n" + (result.stderr or ""))

            with open(path, "rb") as f:
                raw = f.read()
        finally:
            os.remove(path)

        size_x, size_z = struct.unpack(">ii", raw[:8])
        count = size_x * size_z
        values = np.frombuffer(raw, dtype=">f4", count=3 * count, offset=8)
        # the dumper writes [x][z], the same order it samples in
        return [
            values[i * count : (i + 1) * count].reshape(size_x, size_z)
            for i in range(3)
        ]

    def blended(self):
        """Mirrors BlockGenerator.blendSurfaceY over the whole map at once."""
        total = sum(self.weights)
        if total <= 0.0:  # matches the generator's fallback for bad weights
            return np.full(self.maps[0].shape, float(self.sea_level))
        height = sum(m * w for m, w in zip(self.maps, self.weights)) / total
        return np.clip(np.round(height), 0, self.chunk_height - 1)

    def surface_rgb(self, surface):
        """Grayscale by height, but anything below sea level shades blue by depth."""
        gray = np.clip(surface / float(self.chunk_height), 0.0, 1.0)
        rgb = np.repeat(gray[:, :, None], 3, axis=2)

        if self.sea_level > 0:
            underwater = surface < self.sea_level
            # 0 right at the shoreline, 1 at the bottom of the world
            depth = np.clip(
                (self.sea_level - surface) / float(self.sea_level), 0.0, 1.0
            )
            water = SHALLOW_WATER + (DEEP_WATER - SHALLOW_WATER) * depth[:, :, None]
            rgb = np.where(underwater[:, :, None], water, rgb)

        return rgb

    # drawing

    def title_for(self, index):
        data = self.maps[index]
        return "%s  (%.0f - %.0f)" % (PARAMETERS[index][2], data.min(), data.max())

    def surface_title(self, surface):
        underwater = float(np.count_nonzero(surface < self.sea_level))
        return "Final surface height  (%.0f - %.0f)      %.1f%% below sea level" % (
            surface.min(),
            surface.max(),
            100.0 * underwater / surface.size,
        )

    def redraw_surface(self):
        surface = self.blended()
        self.surface_image.set_data(self.surface_rgb(surface).transpose(1, 0, 2))
        self.surface_image.set_extent(self.extent())
        self.surface_axes.set_title(self.surface_title(surface), fontsize=11)
        self.figure.canvas.draw_idle()

    def redraw_all(self):
        for i in range(len(PARAMETERS)):
            # the arrays are [x][z], imshow wants [row][col], so z becomes the rows
            self.images[i].set_data(self.maps[i].T)
            self.images[i].set_extent(self.extent())
            self.axes[i].set_title(self.title_for(i), fontsize=9)
        self.redraw_surface()

    # slider handling

    def on_weight_changed(self, index, value):
        self.weights[index] = value
        self.redraw_surface()

    def on_sea_level_changed(self, value):
        self.sea_level = int(value)
        self.redraw_surface()

    def mark_dirty(self, _value):
        # resampling shells out to java, far too slow to do on every step of a
        # drag, so it waits for the mouse to be released
        self.dirty = True

    def on_release(self, _event):
        if not self.dirty:
            return
        self.dirty = False

        for i in range(len(PARAMETERS)):
            self.scales[i] = self.scale_sliders[i].val
        self.x_width = int(self.x_width_slider.val)
        self.z_width = int(self.z_width_slider.val)

        try:
            self.maps = self.run_dumper()
        except RuntimeError as error:
            print(error, file=sys.stderr)
            return
        self.redraw_all()

    def add_slider(self, row, name, low, high, value, on_changed, valfmt, valstep=None):
        """Stacks one labelled slider into the control column on the right."""
        axes = self.figure.add_axes([0.80, 0.90 - row * 0.088, 0.165, 0.022])
        axes.set_title(name, fontsize=9, loc="left", pad=4)
        slider = Slider(
            axes, "", low, high, valinit=value, valfmt=valfmt, valstep=valstep
        )
        slider.label.set_visible(False)
        slider.valtext.set_fontsize(9)
        slider.on_changed(on_changed)
        self.sliders.append(slider)
        return slider

    def build_controls(self):
        self.sliders = []
        self.weight_sliders = []
        self.scale_sliders = []

        row = 0
        for i, (_, _, title) in enumerate(PARAMETERS):
            self.weight_sliders.append(
                self.add_slider(
                    row,
                    title + " weight",
                    0.0,
                    WEIGHT_SLIDER_MAX,
                    self.weights[i],
                    lambda value, index=i: self.on_weight_changed(index, value),
                    "%.2f",
                )
            )
            row += 1

        for i, (_, _, title) in enumerate(PARAMETERS):
            self.scale_sliders.append(
                self.add_slider(
                    row,
                    title + " scale",
                    SCALE_SLIDER_MIN,
                    SCALE_SLIDER_MAX,
                    self.scales[i],
                    self.mark_dirty,
                    "%.5f",
                )
            )
            row += 1

        self.sea_level_slider = self.add_slider(
            row,
            "Sea level",
            0,
            self.chunk_height - 1,
            self.sea_level,
            self.on_sea_level_changed,
            "%d",
            valstep=1,
        )
        row += 1

        self.x_width_slider = self.add_slider(
            row,
            "xWidth (blocks)",
            WIDTH_SLIDER_MIN,
            WIDTH_SLIDER_MAX,
            self.x_width,
            self.mark_dirty,
            "%d",
            valstep=10,
        )
        row += 1

        self.z_width_slider = self.add_slider(
            row,
            "zWidth (blocks)",
            WIDTH_SLIDER_MIN,
            WIDTH_SLIDER_MAX,
            self.z_width,
            self.mark_dirty,
            "%d",
            valstep=10,
        )

        self.figure.canvas.mpl_connect("button_release_event", self.on_release)

    def show(self):
        self.figure = plt.figure(figsize=(15, 9))
        self.figure.canvas.manager.set_window_title("Noise Map Viewer")
        self.figure.suptitle(
            "seed %d      height 0 - %d" % (self.seed, self.chunk_height)
        )

        # the three parameters stack down the left, the blended result gets the
        # rest of the room
        grid = GridSpec(
            3,
            2,
            figure=self.figure,
            width_ratios=[1.0, 2.4],
            left=0.05,
            right=0.70,
            bottom=0.06,
            top=0.90,
            wspace=0.22,
            hspace=0.38,
        )

        self.axes = []
        self.images = []
        norm = Normalize(vmin=0, vmax=self.chunk_height)
        for i in range(len(PARAMETERS)):
            axes = self.figure.add_subplot(grid[i, 0])
            image = axes.imshow(
                self.maps[i].T,
                origin="upper",
                cmap="gray",
                norm=norm,
                extent=self.extent(),
                interpolation="nearest",
            )
            axes.set_title(self.title_for(i), fontsize=9)
            axes.tick_params(labelsize=8)
            self.axes.append(axes)
            self.images.append(image)

        surface = self.blended()
        self.surface_axes = self.figure.add_subplot(grid[:, 1])
        self.surface_image = self.surface_axes.imshow(
            self.surface_rgb(surface).transpose(1, 0, 2),
            origin="upper",
            extent=self.extent(),
            interpolation="nearest",
        )
        self.surface_axes.set_title(self.surface_title(surface), fontsize=11)
        self.surface_axes.set_xlabel("x")
        self.surface_axes.set_ylabel("z")

        # the grayscale bar only describes the three parameter maps, the final one
        # is an rgb image
        color_axes = self.figure.add_axes([0.725, 0.06, 0.010, 0.84])
        self.figure.colorbar(self.images[0], cax=color_axes, label="height")

        self.build_controls()
        plt.show()
        return self.final_values()

    def final_values(self):
        """Reads the sliders back after the window is gone, so nothing is missed if
        a drag ended off the canvas and never fired a release event."""
        return {
            "sea_level": int(self.sea_level_slider.val),
            "scales": [slider.val for slider in self.scale_sliders],
            "weights": [slider.val for slider in self.weight_sliders],
            "x_width": int(self.x_width_slider.val),
            "z_width": int(self.z_width_slider.val),
        }


def main():
    parser = argparse.ArgumentParser(
        description="preview the world generation noise maps"
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=None,
        help="seed to sample with, defaults to the one in Main.java",
    )
    parser.add_argument(
        "--x-width",
        type=int,
        default=DEFAULT_WIDTH,
        help="starting width of the sampled area along x",
    )
    parser.add_argument(
        "--z-width",
        type=int,
        default=DEFAULT_WIDTH,
        help="starting width of the sampled area along z",
    )
    args = parser.parse_args()

    seed = args.seed if args.seed is not None else read_seed()

    try:
        result = NoiseMapViewer(seed, args.x_width, args.z_width).show()
    except RuntimeError as error:
        print(error, file=sys.stderr)
        return 1

    print("\n// paste into src/main/java/game/Settings.java")
    print(
        "// previewed over x %d wide, z %d wide, seed %d\n"
        % (result["x_width"], result["z_width"], seed)
    )
    print(format_settings(result["sea_level"], result["scales"], result["weights"]))
    return 0


if __name__ == "__main__":
    sys.exit(main())
