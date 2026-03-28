# MinecraftClone (Java + LWJGL 3)

## Build & Run

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

## Notes for Developers

### Coordinate System

The engine uses a right-handed 3D coordinate system with the following axis orientation:

| Axis | Direction                        |
| ---- | -------------------------------- |
| `+X` | Rightwards                       |
| `+Y` | Upwards                          |
| `+Z` | Out of the screen (towards user) |

### Texture Atlas System

The game uses a **texture atlas** system where all block textures are combined into a single image file (`texture_atlas.png`). This improves performance by reducing texture switching during rendering.

#### Texture Map (`texture_atlas.png`)

- **Location**: `src/main/resources/textures/texture_atlas.png`
- **Dimensions**: 4096×2048 pixels (configured in `blocks_data.json`)
- **Grid Layout**: 8 columns × 4 rows = 32 texture slots
- **Individual Texture Size**: 512×512 pixels per slot

#### Texture Index Layout

Textures are numbered **starting from 1 (not 0)**, arranged left-to-right, top-to-bottom:

```
Row 1: [1]  [2]  [3]  [4]  [5]  [6]  [7]  [8]
Row 2: [9]  [10] [11] [12] [13] [14] [15] [16]
Row 3: [17] [18] [19] [20] [21] [22] [23] [24]
Row 4: [25] [26] [27] [28] [29] [30] [31] [32]
```

#### Block Configuration (`blocks_data.json`)

##### Location: `src/main/resources/blocks_data.json`

##### Format:

```json
{
  "blockLength": 512, // Size of each texture in pixels (512×512)
  "resolution": {
    "width": 4096, // Total atlas width in pixels
    "height": 2048 // Total atlas height in pixels
  },
  "blocks": {
    "block_codename": {
      "name": "Display Name",
      "texture1": 1,
      "texture2": 1,
      ...
      "textureRotation1": 1,
      "textureRotation2": 1,
      ...
      "hasTransparency": false // Whether block has transparent parts or not
      "isSolid": true // Whether block is solid or not
      "hardness": 1 // How hard the block is to break (0.0 - 1.0)
    }
  }
}
```

#### Fields:

Each block in the `blocks` object has the following properties:

| Field              | Type    | Description                               |
| ------------------ | ------- | ----------------------------------------- |
| `name`             | string  | Human-readable name (e.g., "Grass Block") |
| `texture1`         | integer | Texture index for top face (1-32)         |
| `texture2`         | integer | Texture index for bottom face (1-32)      |
| `texture3`         | integer | Texture index for front face (1-32)       |
| `texture4`         | integer | Texture index for back face (1-32)        |
| `texture5`         | integer | Texture index for left face (1-32)        |
| `texture6`         | integer | Texture index for right face (1-32)       |
| `textureRotation1` | integer | Rotation for top face (1-4)               |
| `textureRotation2` | integer | Rotation for bottom face (1-4)            |
| `textureRotation3` | integer | Rotation for front face (1-4)             |
| `textureRotation4` | integer | Rotation for back face (1-4)              |
| `textureRotation5` | integer | Rotation for left face (1-4)              |
| `textureRotation6` | integer | Rotation for right face (1-4)             |
| `hasTransparency`  | boolean | Whether the block has transparent parts   |

| Value | Rotation       |
| ----- | -------------- |
| `1`   | 0°             |
| `2`   | 90° clockwise  |
| `3`   | 180° clockwise |
| `4`   | 270° clockwise |

### Items Textures Atlas System

The game uses an **items textures atlas** system to manage item icons efficiently. Similar to the block textures atlas, this system combines all item icons into a single image file (`items_atlas.png`).

#### Items Atlas (`items_atlas.png`)

- **Location**: `src/main/resources/textures/items_atlas.png`
- **Dimensions**: 2048×512 pixels (configured in `items_data.json`)
- **Grid Layout**: 8 columns × 2 rows = 16 icon slots
- **Individual Icon Size**: 256×256 pixels per slot

#### Icon Index Layout

Icons are numbered **starting from 1**, arranged left-to-right, top-to-bottom:

```
Row 1: [1] [2]  [3]  [4]  [5]  [6]  [7]  [8]
Row 2: [9] [10] [11] [12] [13] [14] [15] [16]
```

#### Items Configuration (`items_data.json`)

##### Location: `src/main/resources/items_data.json`

##### Format:

```json
{
  "itemLength": 256, // Size of each icon in pixels (256×256)
  "resolution": {
    "width": 2048, // Total atlas width in pixels
    "height": 512 // Total atlas height in pixels
  },
  "icons": {
    "item_codename": {
      "name": "Display Name",
      "icon": 1, // Index in the atlas
      "stackSize": 64 // Maximum stack size for this item
    }
  }
}
```

### Face Culling Optimization

The engine automatically culls (hides) faces that are adjacent to other blocks, improving performance. Only visible faces are rendered. The engine needs to recalculate the mesh for a chunk when a block is added or removed from it.

### Target Block Calculation

The engine uses a DDA (Digital Differential Analyzer) voxel traversal algorithm to accurately determine the block the player is targeting within their reach. This allows for precise block breaking and placement.
