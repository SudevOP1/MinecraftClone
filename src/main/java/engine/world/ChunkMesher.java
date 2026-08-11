package engine.world;

import engine.block.BlockGeometry;
import engine.block.BlockType;
import game.Settings;

// Builds one merged vertex buffer per chunk instead of one mesh per block.
// Pure CPU work - touches no OpenGL - so it can be moved off the render thread
// later without changing anything here.
final class ChunkMesher {

    private static final int WIDTH = Settings.CHUNK_WIDTH;
    private static final int HEIGHT = Settings.CHUNK_HEIGHT;

    private ChunkMesher() {
    }

    // Growable primitive buffers. Deliberately not List<Float>: boxing every
    // vertex is what made the old path allocate millions of objects per chunk.
    static final class Buffer {

        float[] positions = new float[4096];
        float[] texCoords = new float[2048];
        float[] light = new float[1024];
        int[] indices = new int[2048];

        int positionsLength;
        int texCoordsLength;
        int lightLength;
        int indicesLength;
        int vertexCount;

        boolean isEmpty() {
            return indicesLength == 0;
        }

        void addFace(int face, float ox, float oy, float oz, float[] faceTexCoords) {
            if (positionsLength + 12 > positions.length) {
                positions = grow(positions, positionsLength + 12);
            }
            int p = face * 12;
            for (int i = 0; i < 12; i += 3) {
                positions[positionsLength++] = BlockGeometry.POSITIONS[p + i] + ox;
                positions[positionsLength++] = BlockGeometry.POSITIONS[p + i + 1] + oy;
                positions[positionsLength++] = BlockGeometry.POSITIONS[p + i + 2] + oz;
            }

            if (texCoordsLength + 8 > texCoords.length) {
                texCoords = grow(texCoords, texCoordsLength + 8);
            }
            System.arraycopy(faceTexCoords, face * 8, texCoords, texCoordsLength, 8);
            texCoordsLength += 8;

            if (lightLength + 4 > light.length) {
                light = grow(light, lightLength + 4);
            }
            float brightness = BlockGeometry.FACE_BRIGHTNESS[face];
            light[lightLength++] = brightness;
            light[lightLength++] = brightness;
            light[lightLength++] = brightness;
            light[lightLength++] = brightness;

            if (indicesLength + 6 > indices.length) {
                indices = grow(indices, indicesLength + 6);
            }
            // two triangles: (b, b+1, b+3) and (b+3, b+1, b+2)
            int b = vertexCount;
            indices[indicesLength++] = b;
            indices[indicesLength++] = b + 1;
            indices[indicesLength++] = b + 3;
            indices[indicesLength++] = b + 3;
            indices[indicesLength++] = b + 1;
            indices[indicesLength++] = b + 2;
            vertexCount += 4;
        }

        private static float[] grow(float[] array, int minCapacity) {
            int capacity = array.length * 2;
            while (capacity < minCapacity) {
                capacity *= 2;
            }
            float[] grown = new float[capacity];
            System.arraycopy(array, 0, grown, 0, array.length);
            return grown;
        }

        private static int[] grow(int[] array, int minCapacity) {
            int capacity = array.length * 2;
            while (capacity < minCapacity) {
                capacity *= 2;
            }
            int[] grown = new int[capacity];
            System.arraycopy(array, 0, grown, 0, array.length);
            return grown;
        }
    }

    static final class Result {

        final Buffer solid = new Buffer();
        final Buffer cutout = new Buffer();

        boolean isEmpty() {
            return solid.isEmpty() && cutout.isEmpty();
        }
    }

    // Neighbor chunks are passed in so faces on chunk borders get culled against
    // real blocks instead of being drawn as walls. A null neighbor means "not
    // generated" and its side is treated as air.
    static Result build(Chunk chunk, Chunk negX, Chunk posX, Chunk negZ, Chunk posZ) {
        Result result = new Result();

        for (int lx = 0; lx < WIDTH; lx++) {
            for (int lz = 0; lz < WIDTH; lz++) {
                for (int y = 0; y < HEIGHT; y++) {

                    BlockType type = chunk.getBlockUnchecked(lx, y, lz);
                    if (type == null) {
                        continue;
                    }

                    Buffer target = type.hasTransparency ? result.cutout : result.solid;
                    float[] faceTexCoords = BlockGeometry.getTexCoords(type);

                    for (int face = 0; face < BlockGeometry.FACE_COUNT; face++) {
                        int o = face * 3;
                        BlockType neighbor = sample(chunk, negX, posX, negZ, posZ,
                                lx + BlockGeometry.FACE_OFFSETS[o],
                                y + BlockGeometry.FACE_OFFSETS[o + 1],
                                lz + BlockGeometry.FACE_OFFSETS[o + 2]);

                        // Faces between two transparent blocks stay visible on
                        // purpose (leaves need their inner faces).
                        if (neighbor == null || neighbor.hasTransparency) {
                            target.addFace(face, lx, y, lz, faceTexCoords);
                        }
                    }
                }
            }
        }

        return result;
    }

    // Only ever called with at most one axis out of range, since face offsets
    // move along a single axis.
    private static BlockType sample(Chunk chunk, Chunk negX, Chunk posX, Chunk negZ, Chunk posZ,
            int lx, int y, int lz) {

        if (y < 0 || y >= HEIGHT) {
            return null;
        }
        if (lx < 0) {
            return negX == null ? null : negX.getBlockUnchecked(WIDTH - 1, y, lz);
        }
        if (lx >= WIDTH) {
            return posX == null ? null : posX.getBlockUnchecked(0, y, lz);
        }
        if (lz < 0) {
            return negZ == null ? null : negZ.getBlockUnchecked(lx, y, WIDTH - 1);
        }
        if (lz >= WIDTH) {
            return posZ == null ? null : posZ.getBlockUnchecked(lx, y, 0);
        }
        return chunk.getBlockUnchecked(lx, y, lz);
    }

}
