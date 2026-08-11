# MinecraftClone (Java + LWJGL 3)

A chunk-based voxel world. Each chunk builds one merged mesh (instead of one draw call per block), so a full render-distance world stays under ~100 draw calls per frame. Faces are shaded with fixed per-direction brightness (top brightest, bottom darkest) - cheap, baked-in lighting with no runtime light propagation.

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
