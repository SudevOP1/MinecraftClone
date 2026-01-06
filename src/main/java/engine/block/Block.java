package engine.block;

import java.util.*;
import java.util.function.Function;

import engine.graph.*;
import engine.scene.*;
import data_structures.Vector3s;

public class Block {

    private Vector3s position;
    private Texture texture;
    private Entity entity;
    private String blockId;

    private static class Helpers {

        private static int blockCounter = 0;

        // Use 24 unique vertices (4 per face) so each face can have its own
        // texture coordinates without sharing vertices between faces.
        // Face order: front, top, right, left, bottom, back (matches BlockType textures)
        private static final float[] POSITIONS = new float[] {
            // Front face (bottom-left, top-left, top-right, bottom-right)
            -0.5f, -0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,

            // Top face (bottom-left, top-left, top-right, bottom-right)
            -0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,

            // Right face (bottom-left, top-left, top-right, bottom-right)
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, -0.5f,

            // Left face (bottom-left, top-left, top-right, bottom-right)
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            -0.5f, 0.5f, 0.5f,
            -0.5f, -0.5f, 0.5f,

            // Bottom face (bottom-left, top-left, top-right, bottom-right)
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,

            // Back face (bottom-left, top-left, top-right, bottom-right)
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f,
            0.5f, -0.5f, -0.5f
        };

        // Calculate texture coordinates for a given texture index in the atlas
        private static float[] getTexCoordsForTexture(int textureIndex, int rotation) {
            int atlasColumns = BlockRegistry.getAtlasColumns();
            int atlasRows = BlockRegistry.getAtlasRows();

            // Convert 1-indexed to 0-indexed
            int index = textureIndex - 1;

            // Calculate position in atlas
            int col = index % atlasColumns;
            int row = index / atlasColumns;

            // Calculate UV coordinates
            float u0 = (float) col / atlasColumns;
            float v0 = (float) row / atlasRows;
            float u1 = (float) (col + 1) / atlasColumns;
            float v1 = (float) (row + 1) / atlasRows;

            // Apply rotation (1=0°, 2=90°, 3=180°, 4=270°)
            // Base coords: bottom-left, top-left, top-right, bottom-right
            float[][] rotatedCoords = new float[4][2];

            switch (rotation) {
                case 1: // 0°
                    rotatedCoords[0] = new float[] { u0, v1 }; // bottom-left
                    rotatedCoords[1] = new float[] { u0, v0 }; // top-left
                    rotatedCoords[2] = new float[] { u1, v0 }; // top-right
                    rotatedCoords[3] = new float[] { u1, v1 }; // bottom-right
                    break;
                case 2: // 90° clockwise
                    rotatedCoords[0] = new float[] { u0, v0 }; // top-left -> bottom-left
                    rotatedCoords[1] = new float[] { u1, v0 }; // top-right -> top-left
                    rotatedCoords[2] = new float[] { u1, v1 }; // bottom-right -> top-right
                    rotatedCoords[3] = new float[] { u0, v1 }; // bottom-left -> bottom-right
                    break;
                case 3: // 180°
                    rotatedCoords[0] = new float[] { u1, v0 }; // top-right -> bottom-left
                    rotatedCoords[1] = new float[] { u1, v1 }; // bottom-right -> top-left
                    rotatedCoords[2] = new float[] { u0, v1 }; // bottom-left -> top-right
                    rotatedCoords[3] = new float[] { u0, v0 }; // top-left -> bottom-right
                    break;
                case 4: // 270° clockwise
                    rotatedCoords[0] = new float[] { u1, v1 }; // bottom-right -> bottom-left
                    rotatedCoords[1] = new float[] { u0, v1 }; // bottom-left -> top-left
                    rotatedCoords[2] = new float[] { u0, v0 }; // top-left -> top-right
                    rotatedCoords[3] = new float[] { u1, v0 }; // top-right -> bottom-right
                    break;
                default:
                    rotatedCoords[0] = new float[] { u0, v1 };
                    rotatedCoords[1] = new float[] { u0, v0 };
                    rotatedCoords[2] = new float[] { u1, v0 };
                    rotatedCoords[3] = new float[] { u1, v1 };
                    break;
            }

            return new float[] {
                    rotatedCoords[0][0], rotatedCoords[0][1],
                    rotatedCoords[1][0], rotatedCoords[1][1],
                    rotatedCoords[2][0], rotatedCoords[2][1],
                    rotatedCoords[3][0], rotatedCoords[3][1]
            };
        }

        private static float[] buildTextureCoords(BlockType type) {
            int[] textures = type.getTextureIndices();
            int[] rotations = type.getTextureRotations();

            List<Float> texCoords = new ArrayList<>();
            // Append texture coords per-face in the same order as POSITIONS.
            // BlockType.getTextureIndices() returns [front, top, right, left, bottom, back]
            for (int face = 0; face < 6; face++) {
                float[] coords = getTexCoordsForTexture(textures[face], rotations[face]);
                for (float c : coords) {
                    texCoords.add(c);
                }
            }

            float[] result = new float[texCoords.size()];
            for (int i = 0; i < texCoords.size(); i++) {
                result[i] = texCoords.get(i);
            }
            return result;
        }

        private static int[] buildIndices(boolean[] visibleFaces) {
            List<Integer> indices = new ArrayList<>();
            // Each face uses 4 consecutive vertices in POSITIONS: base = face*4
            for (int face = 0; face < 6; face++) {
                if (!visibleFaces[face]) continue;
                int b = face * 4;
                // two triangles: (b, b+1, b+3) and (b+3, b+1, b+2)
                indices.add(b);
                indices.add(b + 1);
                indices.add(b + 3);
                indices.add(b + 3);
                indices.add(b + 1);
                indices.add(b + 2);
            }
            int[] result = new int[indices.size()];
            for (int i = 0; i < indices.size(); i++) {
                result[i] = indices.get(i);
            }
            return result;
        }
    }

    public Block(
            Scene scene,
            BlockType type,
            short x,
            short y,
            short z,
            Function<Vector3s, Boolean> hasNeighbor) {

        this.position = new Vector3s(x, y, z);
        this.blockId = type.codename + "-" + Helpers.blockCounter++;

        // Check which faces are visible (no neighbor blocking them)
        boolean[] visibleFaces = new boolean[6];
        visibleFaces[0] = !hasNeighbor.apply(new Vector3s(x, y, (short) (z + 1))); // front
        visibleFaces[1] = !hasNeighbor.apply(new Vector3s(x, (short) (y + 1), z)); // top
        visibleFaces[2] = !hasNeighbor.apply(new Vector3s((short) (x + 1), y, z)); // right
        visibleFaces[3] = !hasNeighbor.apply(new Vector3s((short) (x - 1), y, z)); // left
        visibleFaces[4] = !hasNeighbor.apply(new Vector3s(x, (short) (y - 1), z)); // bottom
        visibleFaces[5] = !hasNeighbor.apply(new Vector3s(x, y, (short) (z - 1))); // back

        // load texture (now using the texture atlas)
        this.texture = scene.getTextureCache().createTexture("texture_map.png");

        // create material
        Material material = new Material();
        material.setTexturePath("texture_map.png");
        List<Material> materialList = new ArrayList<>();
        materialList.add(material);

        // Build texture coordinates based on BlockType
        float[] textureCoords = Helpers.buildTextureCoords(type);

        // create mesh with only visible faces
        int[] indices = Helpers.buildIndices(visibleFaces);
        Mesh mesh = new Mesh(Helpers.POSITIONS, textureCoords, indices);
        material.getMeshList().add(mesh);

        // create model
        Model blockModel = new Model(this.blockId + "-model", materialList);
        scene.addModel(blockModel);

        // create entity
        this.entity = new Entity(blockId + "-entity", blockModel.getId());
        this.entity.setPosition(x, y, z);
        this.entity.updateModelMatrix();
        scene.addEntity(this.entity);
    }

    Vector3s getPosition() {
        return this.position;
    }

    void setPosition(short x, short y, short z) {
        this.position.set(x, y, z);
        this.entity.setPosition(x, y, z);
        this.entity.updateModelMatrix();
    }

    void setPosition(Vector3s position) {
        this.position.set(position.x, position.y, position.z);
    }

    public Entity getEntity() {
        return this.entity;
    }

    public Texture getTexture() {
        return this.texture;
    }

    public String getBlockId() {
        return this.blockId;
    }

    public void setScale(float scale) {
        this.entity.setScale(scale);
        this.entity.updateModelMatrix();
    }

    public void setRotation(float x, float y, float z, float angle) {
        this.entity.setRotation(x, y, z, angle);
        this.entity.updateModelMatrix();
    }

}
