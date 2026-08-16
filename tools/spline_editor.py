"""
Interactive spline point editor for the world generation splines in Settings.java.

Usage:
    python tools/spline_editor.py

Controls:
    left click + drag on a point  -> move it
    left click on empty area      -> add a point there
    right click on a point        -> delete it
    close the window              -> print the Java arrays to stdout

The three graphs share the same axes ranges:
    x: -1.0 .. 1.0               (noise value)
    y: 0    .. CHUNK_HEIGHT      (terrain height)

Both the existing spline points and CHUNK_HEIGHT are read from Settings.java, so the
editor always starts from whatever is currently in the file.
"""

import os
import re
import sys

import matplotlib.pyplot as plt

SETTINGS_PATH = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "src", "main", "java", "game", "Settings.java",
)

# java field name -> title shown above the graph
SPLINES = [
    ("CONTINENTALNESS_SPLINE_POINTS", "Continentalness"),
    ("EROSION_SPLINE_POINTS", "Erosion"),
    ("PEAKS_AND_VALLEYS_SPLINE_POINTS", "Peaks and Valleys"),
]

MIN_X = -1.0  # the noise value domain the spline points are defined over
MAX_X = 1.0

PICK_RADIUS_PX = 10  # how close the cursor must be to a point to grab or delete it
MIN_POINTS = 2  # never let a spline drop below this many points


def read_settings(path):
    """Returns (chunk_height, {field_name: [[x, y], ...]}) parsed out of Settings.java."""
    with open(path, "r", encoding="utf-8") as f:
        source = f.read()

    height_match = re.search(r"CHUNK_HEIGHT\s*=\s*([0-9.]+)", source)
    if height_match is None:
        raise RuntimeError("could not find CHUNK_HEIGHT in " + path)
    chunk_height = float(height_match.group(1))

    splines = {}
    for field, _ in SPLINES:
        block = re.search(field + r"\s*=\s*\{(.*?)\}\s*;", source, re.DOTALL)
        if block is None:
            raise RuntimeError("could not find " + field + " in " + path)
        pairs = re.findall(
            r"\{\s*(-?[0-9.]+)f?\s*,\s*(-?[0-9.]+)f?\s*\}", block.group(1))
        if not pairs:
            raise RuntimeError("no spline points found in " + field)
        splines[field] = [[float(x), float(y)] for x, y in pairs]

    return chunk_height, splines


def format_number(value):
    """Formats a float the way it is written in Settings.java, e.g. 0.5 -> '0.5f'."""
    text = ("%.4f" % value).rstrip("0")
    if text.endswith("."):
        text += "0"
    return text + "f"


def format_java(field, points):
    lines = ["    public static final float[][] " + field + " = {"]
    for x, y in points:
        lines.append("            { %s, %s }," %
                     (format_number(x), format_number(y)))
    lines.append("    };")
    return "\n".join(lines)


class SplineEditor:

    def __init__(self, chunk_height, splines):
        self.chunk_height = chunk_height
        self.points = {field: list(splines[field]) for field, _ in SPLINES}
        self.dragging = None  # (field, index) while a point is being moved

        self.figure, axes_list = plt.subplots(
            1, len(SPLINES), figsize=(16, 6), squeeze=False)
        self.figure.canvas.manager.set_window_title("Spline Editor")

        self.axes = {}
        self.lines = {}
        for axes, (field, title) in zip(axes_list[0], SPLINES):
            axes.set_title(title)
            axes.set_xlim(MIN_X, MAX_X)
            axes.set_ylim(0.0, chunk_height)
            axes.set_xlabel("noise")
            axes.set_ylabel("height")
            axes.grid(True, alpha=0.3)
            line, = axes.plot([], [], "-o", color="tab:blue",
                              markersize=7, markerfacecolor="white")
            self.axes[field] = axes
            self.lines[field] = line
            self.redraw(field)

        self.figure.tight_layout()
        canvas = self.figure.canvas
        canvas.mpl_connect("button_press_event", self.on_press)
        canvas.mpl_connect("motion_notify_event", self.on_motion)
        canvas.mpl_connect("button_release_event", self.on_release)

    def field_of(self, axes):
        for field, field_axes in self.axes.items():
            if field_axes is axes:
                return field
        return None

    def redraw(self, field):
        self.points[field].sort(key=lambda point: point[0])
        points = self.points[field]
        self.lines[field].set_data([p[0] for p in points], [p[1]
                                   for p in points])
        self.figure.canvas.draw_idle()

    def nearest_point(self, field, event):
        """Returns the index of the point within PICK_RADIUS_PX of the cursor, else None."""
        axes = self.axes[field]
        best_index = None
        best_distance = PICK_RADIUS_PX
        for index, (x, y) in enumerate(self.points[field]):
            px, py = axes.transData.transform((x, y))
            distance = ((px - event.x) ** 2 + (py - event.y) ** 2) ** 0.5
            if distance <= best_distance:
                best_index = index
                best_distance = distance
        return best_index

    def clamp(self, x, y):
        return (min(max(x, MIN_X), MAX_X), min(max(y, 0.0), self.chunk_height))

    def on_press(self, event):
        if event.inaxes is None or event.xdata is None:
            return
        field = self.field_of(event.inaxes)
        if field is None:
            return
        index = self.nearest_point(field, event)

        if event.button == 1:
            if index is None:
                self.points[field].append(
                    list(self.clamp(event.xdata, event.ydata)))
                self.redraw(field)
            else:
                self.dragging = (field, index)

        elif event.button == 3 and index is not None:
            if len(self.points[field]) <= MIN_POINTS:
                return
            del self.points[field][index]
            self.redraw(field)

    def on_motion(self, event):
        if self.dragging is None or event.xdata is None:
            return
        field, index = self.dragging
        if event.inaxes is not self.axes[field]:
            return
        self.points[field][index] = list(self.clamp(event.xdata, event.ydata))
        # keep the index pointing at the dragged point after the sort
        moved = self.points[field][index]
        self.redraw(field)
        self.dragging = (field, self.points[field].index(moved))

    def on_release(self, event):
        self.dragging = None

    def run(self):
        plt.show()
        return {field: self.points[field] for field, _ in SPLINES}


def main():
    if not os.path.isfile(SETTINGS_PATH):
        print("Settings.java not found at " + SETTINGS_PATH, file=sys.stderr)
        return 1

    chunk_height, splines = read_settings(SETTINGS_PATH)
    result = SplineEditor(chunk_height, splines).run()

    print("\n// paste into src/main/java/game/Settings.java\n")
    for field, _ in SPLINES:
        print(format_java(field, result[field]))
    return 0


if __name__ == "__main__":
    sys.exit(main())
