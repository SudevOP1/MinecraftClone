# MinecraftClone (Java + LWJGL 3)

## Build & Run

Compile:

```bash
mvn clean install
```

Run:

```bash
mvn exec:java
```

## Controls

| Key          | Control               |
| ------------ | --------------------- |
| `Move Mouse` | look around           |
| `W`          | move forward          |
| `A`          | move left             |
| `S`          | move behind           |
| `D`          | move right            |
| `Space`      | move up               |
| `Left Shift` | move down             |
| `F2`         | take screenshot       |
| `F3`         | toggle wireframe mode |

## Notes for Developers

### Coordinate System

The engine uses a right-handed 3D coordinate system with the following axis orientation:
| Axis | Direction |
| ---- | ----------------- |
| `+X` | Rightwards |
| `+Y` | Upwards |
| `+Z` | Out of the screen (towards user) |

### Texture Atlas System

The game uses a **texture atlas** system where all block textures are combined into a single image file (`texture_map.png`). This improves performance by reducing texture switching during rendering.

### Texture Map (`texture_map.png`)

- **Location**: `src/main/resources/models/texture_map.png`
- **Dimensions**: 4096×2048 pixels (configured in `BlocksData.json`)
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

### Block Configuration (`BlocksData.json`)

#### Location: `src/main/resources/BlocksData.json`

#### Format:

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

### Face Culling Optimization

The engine automatically culls (hides) faces that are adjacent to other blocks, improving performance. Only visible faces are rendered.
