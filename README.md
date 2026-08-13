# MinecraftClone

A Minecraft clone built with **Java 25** and **LWJGL 3.3.6** (OpenGL 3.2 core profile). Uses a chunk-based voxel world system with face culling optimization and procedural generation using Perlin Noise.

## Build & Run

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

## Controls

| Key           | Control                         |
| ------------- | ------------------------------- |
| `Move Mouse`  | look around                     |
| `Left Click`  | break block                     |
| `Right Click` | place block                     |
| `Scroll`      | switch selected block in hotbar |
| `W`           | move forward                    |
| `A`           | move left                       |
| `S`           | move behind                     |
| `D`           | move right                      |
| `Space`       | move up                         |
| `Left Shift`  | move down                       |
| `F2`          | take screenshot                 |
| `F3`          | toggle debug GUI mode           |
| `F4`          | toggle wireframe mode           |
| `1` - `9`     | toggle selected hotbar slot     |
