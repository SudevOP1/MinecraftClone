package engine.block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Shared cube geometry + atlas UV maths for blocks.
// Face order everywhere in this class is: front, top, right, left, bottom, back
// (matches BlockType.getTextureIndices()).
public final class BlockGeometry {

    public static final int FACE_COUNT = 6;

    // 24 unique vertices (4 per face) so each face carries its own texture
    // coordinates without sharing vertices between faces.
    public static final float[] POSITIONS = new float[] {
            // Front face (bottom-left, top-left, top-right, bottom-right)
            0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f,
            // Top face
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f,
            // Right face
            1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            // Left face
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            // Bottom face
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f,
            // Back face
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            1.0f, 0.0f, 0.0f
    };

    // Neighbor offset per face, flattened as (dx, dy, dz) triples.
    public static final int[] FACE_OFFSETS = new int[] {
            0, 0, 1, // front
            0, 1, 0, // top
            1, 0, 0, // right
            -1, 0, 0, // left
            0, -1, 0, // bottom
            0, 0, -1 // back
    };

    // Fake directional light: fixed brightness per face direction, baked at mesh
    // build time. No propagation, no light updates - a constant lookup, so it
    // costs nothing beyond one extra float per vertex.
    public static final float[] FACE_BRIGHTNESS = new float[] {
            0.8f, // front
            1.0f, // top
            0.6f, // right
            0.6f, // left
            0.4f, // bottom
            0.8f, // back
    };

    // Per-vertex brightness matching POSITIONS layout (4 verts/face), for
    // callers that build a full 24-vertex cube (e.g. single held/dropped blocks).
    public static final float[] LIGHT = new float[FACE_COUNT * 4];

    static {
        for (int face = 0; face < FACE_COUNT; face++) {
            float brightness = FACE_BRIGHTNESS[face];
            for (int v = 0; v < 4; v++) {
                LIGHT[face * 4 + v] = brightness;
            }
        }
    }

    // Texture coords are identical for every instance of a BlockType, so build
    // them once per type instead of once per block.
    private static final Map<BlockType, float[]> TEX_COORD_CACHE = new ConcurrentHashMap<>();

    private BlockGeometry() {
    }

    // Returns 48 floats: 8 (4 vertices x u,v) per face, in face order.
    public static float[] getTexCoords(BlockType type) {
        return TEX_COORD_CACHE.computeIfAbsent(type, BlockGeometry::buildTexCoords);
    }

    private static float[] buildTexCoords(BlockType type) {
        int[] textures = type.getTextureIndices();
        int[] rotations = type.getTextureRotations();

        float[] result = new float[FACE_COUNT * 8];
        for (int face = 0; face < FACE_COUNT; face++) {
            writeFaceTexCoords(result, face * 8, textures[face], rotations[face]);
        }
        return result;
    }

    // Writes the 8 UV floats for one atlas tile into dst at dstOffset.
    private static void writeFaceTexCoords(float[] dst, int dstOffset, int textureIndex, int rotation) {
        int atlasColumns = BlockRegistry.getAtlasColumns();
        int atlasRows = BlockRegistry.getAtlasRows();

        // Convert 1-indexed to 0-indexed
        int index = textureIndex - 1;

        int col = index % atlasColumns;
        int row = index / atlasColumns;

        float u0 = (float) col / atlasColumns;
        float v0 = (float) row / atlasRows;
        float u1 = (float) (col + 1) / atlasColumns;
        float v1 = (float) (row + 1) / atlasRows;

        // Apply rotation (1=0deg, 2=90deg, 3=180deg, 4=270deg)
        // Base corner order: bottom-left, top-left, top-right, bottom-right
        switch (rotation) {
            case 2: // 90 clockwise
                write(dst, dstOffset, u0, v0, u1, v0, u1, v1, u0, v1);
                break;
            case 3: // 180
                write(dst, dstOffset, u1, v0, u1, v1, u0, v1, u0, v0);
                break;
            case 4: // 270 clockwise
                write(dst, dstOffset, u1, v1, u0, v1, u0, v0, u1, v0);
                break;
            default: // 1 == 0 deg, and anything unexpected
                write(dst, dstOffset, u0, v1, u0, v0, u1, v0, u1, v1);
                break;
        }
    }

    private static void write(float[] dst, int o,
            float u0, float v0, float u1, float v1,
            float u2, float v2, float u3, float v3) {
        dst[o] = u0;
        dst[o + 1] = v0;
        dst[o + 2] = u1;
        dst[o + 3] = v1;
        dst[o + 4] = u2;
        dst[o + 5] = v2;
        dst[o + 6] = u3;
        dst[o + 7] = v3;
    }

}
