# MinecraftClone - Project Instructions & Context

## Project Overview

A Minecraft clone built with **Java 25** and **LWJGL 3.3.6** (OpenGL 3.2 core profile). Uses a chunk-based voxel world system with face culling optimization.

## Quick Commands

```bash
# Build
mvn clean install

# Run
mvn exec:java
```

## Tech Stack

| Component | Library                    |
| --------- | -------------------------- |
| Graphics  | LWJGL 3.3.6 + OpenGL 3.2   |
| Windowing | GLFW                       |
| Math      | JOML 1.10.8                |
| UI        | ImGui (imgui-java 1.86.11) |
| Logging   | Tinylog 2.6.2              |
| JSON      | Gson 2.11.0                |

## Project Structure

```
src/main/java/
├── engine/
│   ├── block/          # Block, BlockType, BlockRegistry
│   ├── graph/          # Rendering (Mesh, Model, ShaderProgram, Texture, Render)
│   ├── scene/          # Camera, Entity, Projection, Scene
│   ├── ui/             # DebugUI, HotbarUI, UIManager
│   ├── world/          # World, Chunk, player/, gen/
│   ├── Window.java     # GLFW window wrapper
│   ├── Engine.java     # Game loop (30 UPS, variable FPS)
│   └── IAppLogic.java  # Interface for game logic
├── game/
│   ├── Main.java       # Entry point
│   └── Settings.java   # All game constants
├── data_structures/
│   ├── Vector2s.java   # Short-based 2D vector (chunk coords)
│   └── Vector3s.java   # Short-based 3D vector (block coords)
└── utils/
    └── Debug.java      # Debug utilities

src/main/resources/
├── blocks_data.json    # Block definitions + texture indices
├── items_data.json     # Item definitions + icon indices
└── textures/
    ├── texture_atlas.png   # 4096x2048, 8x4 grid, 512px tiles
    └── items_atlas.png     # 2048x512, 8x2 grid, 256px icons
```

## Coordinate System

**Right-handed 3D:**

- **+X**: Right
- **+Y**: Up
- **+Z**: Toward viewer (out of screen)

**Chunk System:**

- Chunks are 16x128x16 (WIDTH x HEIGHT x DEPTH)
- World Y range: 0-127
- Render distance: 3 chunks (7x7 = 49 chunks total)
- Chunk coords use `Vector2s`, block coords use `Vector3s`

## Key Systems

### Texture Atlas

Blocks reference textures by index (1-32), not file paths:

```
Row 1: [1]  [2]  [3]  [4]  [5]  [6]  [7]  [8]
Row 2: [9]  [10] [11] [12] [13] [14] [15] [16]
Row 3: [17] [18] [19] [20] [21] [22] [23] [24]
Row 4: [25] [26] [27] [28] [29] [30] [31] [32]
```

See `blocks_data.json` for block-to-texture mappings. Texture rotation (1-4) allows 0°, 90°, 180°, 270° rotations per face.

### Face Culling

The `Block` constructor receives a `Function<Vector3s, BlockType>` to query neighbors. Faces adjacent to solid blocks are not rendered. When a block changes, `regenerateBlockAndNeighbors()` updates 7 blocks (center + 6 neighbors).

### DDA Ray Tracing

`World.calculateTargetBlock()` uses Digital Differential Analyzer voxel traversal for precise block selection (5 block reach). Returns both the hit block and the adjacent empty position for placement.

### Game Loop

`Engine.java` runs a fixed timestep update loop (30 UPS) with variable rendering:

- Updates: Fixed 33.3ms intervals
- Render: Variable, capped by `targetFps` or vsync
- Input: Polling via GLFW callbacks

## Controls

| Key         | Action           |
| ----------- | ---------------- |
| Mouse       | Look             |
| Left Click  | Break block      |
| Right Click | Place block      |
| Scroll      | Hotbar slot      |
| WASD        | Move horizontal  |
| Space       | Move up          |
| Shift       | Move down        |
| F2          | Screenshot       |
| F3          | Toggle debug UI  |
| F4          | Toggle wireframe |
| Esc         | Close window     |

## Game Modes

- **CREATIVE**: Instant block break, infinite blocks
- **SURVIVAL**: Timed breaking (hardness-based), consumes inventory

## Settings (game/Settings.java)

```java
WORLD_Y_LOWER_LIMIT = 0
WORLD_Y_UPPER_LIMIT = 128
CHUNK_WIDTH = 16
RENDER_DISTANCE = 3
MOUSE_SENSITIVITY = 0.1f
MOVEMENT_SPEED = 0.005f
MAX_BLOCK_REACH = 5.0f
BREAK_COOLDOWN_MS = 200
HOTBAR_CELL_COUNT = 9
INVENTORY_SIZE = 36  // 9 hotbar + 27 main
```

## Development Notes

### Adding a New Block

1. Add texture to `texture_atlas.png` (note the index)
2. Add entry in `blocks_data.json`:

```json
"my_block": {
  "name": "My Block",
  "texture1": 33, "texture2": 33, ...,
  "textureRotation1": 1, ...,
  "hasTransparency": false,
  "hardness": 1.0,
  "isSolid": true
}
```

3. BlockRegistry auto-loads from JSON

### Coordinate Conversion

**World to Chunk:**

```java
int chunkX = (int) Math.floor((double) worldX / Settings.CHUNK_WIDTH);
int localX = worldX - chunkX * Settings.CHUNK_WIDTH;
```

**Chunk to World:**

```java
int worldX = chunk.x * Settings.CHUNK_WIDTH + localX;
```

### Important Classes

| Class           | Responsibility                      |
| --------------- | ----------------------------------- |
| `World`         | Game logic, input, chunk management |
| `Chunk`         | Block storage, mesh generation      |
| `Block`         | Render entity for a single block    |
| `Scene`         | Entity/model registry, camera       |
| `Render`        | OpenGL rendering pipeline           |
| `ShaderProgram` | GLSL shader management              |
| `BlockRegistry` | JSON block definition loader        |

## Common Tasks

### Debug a rendering issue

1. Press F4 for wireframe mode
2. Press F3 for debug UI
3. Check `target/classes/shaders/` for shader errors

### Add a new key binding

1. Import GLFW key constant in `World.java`
2. Add `isKeyPressed()` check in `input()`
3. Consider adding to controls table in README

### Modify chunk generation

Edit `Chunk.generate()` or `StructureGenerator.generateOakTree()`

## Known Design Decisions

- Texture indices start at 1 (not 0) - matches atlas UI layout
- Short-based vectors for chunk/block coords (memory efficiency)
- Immediate mesh regeneration on block change (simple, works for small scale)
- No persistence yet - `World.save()` is a stub
