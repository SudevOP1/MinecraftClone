# MinecraftClone

A Minecraft clone built with **Java 25** and **LWJGL 3.3.6** (OpenGL 3.2 core profile). Uses a chunk-based voxel world system with face culling optimization and procedural generation using Perlin Noise.

## ✨ Key Features

- Chunk-based voxel world system
- Block breaking and placing
- Hotbar to switch blocks
- Multiple Gamemodes (Creative, Survival, Spectator)
- Face culling optimization (~99.83% fewer faces to be rendered)
- Procedural generation using Perlin Noise, shaped by continentalness, erosion and peaks-and-valleys splines

## 🚀 Build & Run

### Dependencies:

- Java 17 or higher
- Maven 3.6 or higher

### Compile:

```bash
mvn clean install
```

### Run:

```bash
mvn exec:java
```

## 🛠️ Controls

| Key           | Control                                                             |
| ------------- | ------------------------------------------------------------------- |
| `Move Mouse`  | look around                                                         |
| `Left Click`  | break block                                                         |
| `Right Click` | place block                                                         |
| `Scroll`      | switch selected block in hotbar                                     |
| `W`           | move forward                                                        |
| `A`           | move left                                                           |
| `S`           | move behind                                                         |
| `D`           | move right                                                          |
| `Space`       | move up                                                             |
| `Left Shift`  | move down                                                           |
| `Left Ctrl`   | sprint                                                              |
| `F1` (hold)   | opens gamemode selector, move mouse to pick, release `F1` to switch |
| `F2`          | take screenshot                                                     |
| `F3`          | toggle debug GUI mode                                               |
| `F4`          | toggle wireframe mode                                               |
| `1` - `9`     | toggle selected hotbar slot                                         |

## 🌍 Terrain Modifiers

Python tools in `tools/` for tuning world generation. Both read their starting values straight out of `Settings.java` and print Java code when you close the window, nothing is written to the project automatically.

### Setup Virtual Environment

```bash
cd tools/
python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt
```

### Spline editor

```bash
python tools/spline_editor.py
```

Graphs the continentalness, erosion and peaks-and-valleys splines, with noise value (`-1.0` to `1.0`) on x and terrain height (`0` to `CHUNK_HEIGHT`) on y.

| Input                | Action              |
| -------------------- | ------------------- |
| `Left Click` + drag  | move a spline point |
| `Left Click` (empty) | add a point         |
| `Right Click`        | delete a point      |

### Noise map viewer

```bash
mvn compile
python tools/noise_map_viewer.py
```

Top-down maps of the three parameters (grayscale) plus a larger map of the surface height they blend into, where anything below sea level is drawn in blue by depth. Sliders on the right vary the weights, the noise scales, the sea level and the sampled area (`xWidth`/`zWidth`).

The noise is sampled by `tools.NoiseMapDumper`, which calls the real `PerlinNoise` and `BlockGenerator` code, so the preview cannot drift from what the game generates. That is why it needs `mvn compile` first. `--seed N` overrides the seed (default: the one in `Main.java`).
