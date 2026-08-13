# MinecraftClone - Project Instructions & Context

## Project Overview

A Minecraft clone built with **Java 25** and **LWJGL 3.3.6** (OpenGL 3.2 core profile). Uses a chunk-based voxel world system with face culling and per-chunk merged mesh generation.

## Quick Commands

```bash
# Build
mvn clean install

# Run
mvn exec:java
```

## Key Components & Technologies

| Component | Library                       |
| --------- | ----------------------------- |
| Graphics  | LWJGL 3.3.6 + OpenGL 3.2      |
| Windowing | GLFW                          |
| Math      | JOML 1.10.8                   |
| UI        | ImGui (imgui-java 1.86.11)    |
| Logging   | Tinylog 2.6.2 & Custom Logger |
| JSON      | Gson 2.11.0                   |

## Project Structure

```
src/main/java/
├── engine/
│   ├── block/          # Block, BlockType, BlockRegistry, BlockGeometry
│   ├── graph/          # Rendering (Mesh, Model, ShaderProgram, Texture, Render)
│   ├── scene/          # Camera, Entity, Projection, Scene
│   ├── ui/             # DebugUI, HotbarUI, UIManager
│   ├── world/          # World, Chunk, ChunkMesher (merged mesh build), player/, gen/
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
    ├── texture_atlas.png   # 4096x2048px, 8x4 grid, 512x512px tiles
    └── items_atlas.png     # 2048x512px, 8x2 grid, 256x256px icons
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

### Face Culling & Chunk Meshing

Each chunk builds **one merged mesh** (`ChunkMesher.java`) instead of one Model/Mesh/Entity per block — at render distance 3 (~250k blocks) this cuts draw calls from ~250k/frame to <=98/frame (2 draw calls per chunk: opaque + cutout). `ChunkMesher` walks the chunk's flat `BlockType[]` array and, per face, queries the neighbor (`Function<Vector3s, BlockType>`) to skip faces touching solid blocks. Transparent-vs-transparent faces (e.g. leaves next to leaves) are still kept — see [[feedback_no_transparent_face_culling]]. The one exception is a `glass_block`-`glass_block` face, which is culled (`ChunkMesher.isGlassPair`).

`BlockGeometry.java` caches each `BlockType`'s 6-face texture-atlas UVs on first use (computed once, not per block instance).

Transparent blocks (`hasTransparency`) render **alpha-cutout** (discard on low alpha in `scene.frag`), not alpha-blended — per-block back-to-front sorting isn't possible once faces are merged into one chunk-wide mesh.

Editing a block (break/place) rebuilds the owning chunk's mesh (and border-adjacent chunks if the edit sits on a chunk edge), not a single block's mesh.

### Lighting

Fake directional (per-face) lighting, not a real light-propagation system: `BlockGeometry.FACE_BRIGHTNESS` assigns a fixed brightness per face direction (top=1.0, front/back=0.8, right/left=0.6, bottom=0.4). `ChunkMesher` bakes that constant into a third per-vertex attribute (`light`, VAO location 2, 1 float/vertex) alongside positions and UVs when it builds a chunk's mesh - no runtime cost, no light updates on block change. `scene.vert` passes `light` through as `outLight`; `scene.frag` multiplies the sampled texture color by it (`fragColor = vec4(color.rgb * outLight, color.a)`). `Mesh.java` now takes a `light` array/length in both constructors, and single-block meshes (`Block.java`) use `BlockGeometry.LIGHT`, a precomputed 24-entry array matching `POSITIONS`.

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
int chunkX = Math.floorDiv(worldX, Settings.CHUNK_WIDTH);
int localX = Math.floorMod(worldX, Settings.CHUNK_WIDTH);
```

(integer `floorDiv`/`floorMod`, not double division — correct at negative coords, no round-trip through double)

**Chunk to World:**

```java
int worldX = chunk.x * Settings.CHUNK_WIDTH + localX;
```

### Important Classes

| Class           | Responsibility                                                                                               |
| --------------- | ------------------------------------------------------------------------------------------------------------ |
| `World`         | Game logic, input, chunk management                                                                          |
| `Chunk`         | Flat `BlockType[]` block storage                                                                             |
| `ChunkMesher`   | Builds merged opaque/cutout mesh for a chunk                                                                 |
| `Block`         | Single-block render entity, now only used for destroy-stage overlays (world blocks render via `ChunkMesher`) |
| `BlockGeometry` | Cached per-BlockType texture UVs                                                                             |
| `Scene`         | Entity/model registry, camera                                                                                |
| `Render`        | OpenGL rendering pipeline                                                                                    |
| `ShaderProgram` | GLSL shader management                                                                                       |
| `BlockRegistry` | JSON block definition loader                                                                                 |

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

Edit `Chunk.generateBlocks()` (terrain, calls `BlockGenerator.getBlockAt()` per world coord), `BlockGenerator.java` (terrain rules), or `StructureGenerator.generateOakTree()` (structures). After changing generation, mesh rebuild happens automatically via `ChunkMesher` — no separate mesh code to touch.

`BlockGenerator` uses `PerlinNoise` (`engine/world/gen/PerlinNoise.java`) for terrain height:

- `PerlinNoise` instance is cached statically per seed (`getNoise()`) — building a new `PerlinNoise` reshuffles a 256-entry permutation table, too costly to do per block.
- World `x`/`z` are integer block coords; `sample2d` needs fractional lattice input to vary, so callers multiply by `NOISE_SCALE` (0.01) first. Passing raw integer coords makes every octave land exactly on lattice points and `sample2d` returns 0 everywhere (flat world). Lower `NOISE_SCALE` = broader/smoother hills, higher = choppier terrain.

## Known Design Decisions

- Texture indices start at 1 (not 0) - matches atlas UI layout
- Short-based vectors for chunk/block coords (memory efficiency)
- Chunk block storage is a flat `BlockType[]` indexed by `(x*WIDTH+z)*HEIGHT+y`, not a map - cache-friendly, no per-block boxing/hashing
- One merged mesh per chunk (opaque + cutout), not one Model/Mesh/Entity per block - see `ChunkMesher.java`
- Immediate whole-chunk mesh rebuild on block change (breaks/places rebuild the owning chunk + border-adjacent chunks on edge edits)
- Transparent blocks (leaves, etc.) render alpha-cutout, not alpha-blended, since per-block sort isn't possible once meshes are merged
- Lighting is a static per-face brightness constant baked into the mesh at build time, not a real light-propagation/light-map system - see "Lighting" above
- No persistence yet - `World.save()` is a stub
