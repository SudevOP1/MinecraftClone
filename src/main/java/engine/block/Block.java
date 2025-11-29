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

        private static final float[] POSITIONS = new float[] {
                // V0
                -0.5f, 0.5f, 0.5f,
                // V1
                -0.5f, -0.5f, 0.5f,
                // V2
                0.5f, -0.5f, 0.5f,
                // V3
                0.5f, 0.5f, 0.5f,
                // V4
                -0.5f, 0.5f, -0.5f,
                // V5
                0.5f, 0.5f, -0.5f,
                // V6
                -0.5f, -0.5f, -0.5f,
                // V7
                0.5f, -0.5f, -0.5f,

                // For text coords in top face
                // V8: V4 repeated
                -0.5f, 0.5f, -0.5f,
                // V9: V5 repeated
                0.5f, 0.5f, -0.5f,
                // V10: V0 repeated
                -0.5f, 0.5f, 0.5f,
                // V11: V3 repeated
                0.5f, 0.5f, 0.5f,

                // For text coords in right face
                // V12: V3 repeated
                0.5f, 0.5f, 0.5f,
                // V13: V2 repeated
                0.5f, -0.5f, 0.5f,

                // For text coords in left face
                // V14: V0 repeated
                -0.5f, 0.5f, 0.5f,
                // V15: V1 repeated
                -0.5f, -0.5f, 0.5f,

                // For text coords in bottom face
                // V16: V6 repeated
                -0.5f, -0.5f, -0.5f,
                // V17: V7 repeated
                0.5f, -0.5f, -0.5f,
                // V18: V1 repeated
                -0.5f, -0.5f, 0.5f,
                // V19: V2 repeated
                0.5f, -0.5f, 0.5f,
        };

        private static final float[] TEXT_COORDS = new float[] {
                0.0f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.5f,
                0.5f, 0.0f,

                0.0f, 0.0f,
                0.5f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.5f,

                // For text coords in top face
                0.0f, 0.5f,
                0.5f, 0.5f,
                0.0f, 1.0f,
                0.5f, 1.0f,

                // For text coords in right face
                0.0f, 0.0f,
                0.0f, 0.5f,

                // For text coords in left face
                0.5f, 0.0f,
                0.5f, 0.5f,

                // For text coords in bottom face
                0.5f, 0.0f,
                1.0f, 0.0f,
                0.5f, 0.5f,
                1.0f, 0.5f,
        };

        private static final int[][] FACE_INDICES = new int[][] {
                // Front face
                { 0, 1, 3, 3, 1, 2 },
                // Top Face
                { 8, 10, 11, 9, 8, 11 },
                // Right face
                { 12, 13, 7, 5, 12, 7 },
                // Left face
                { 14, 15, 6, 4, 14, 6 },
                // Bottom face
                { 16, 18, 19, 17, 16, 19 },
                // Back face
                { 4, 6, 7, 5, 4, 7 }
        };

        private static int[] buildIndices(boolean[] visibleFaces) {
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                if (visibleFaces[i]) {
                    for (int index : FACE_INDICES[i]) {
                        indices.add(index);
                    }
                }
            }
            int[] result = new int[indices.size()];
            for (int i = 0; i < indices.size(); i++) {
                result[i] = indices.get(i);
            }
            return result;
        }
    }

    public Block(Scene scene, String texturePath, short x, short y, short z, Function<Vector3s, Boolean> hasNeighbor) {

        this.position = new Vector3s(x, y, z);
        this.blockId = "block-" + Helpers.blockCounter++;

        // Check which faces are visible (no neighbor blocking them)
        boolean[] visibleFaces = new boolean[6];
        // Front (+Z)
        visibleFaces[0] = !hasNeighbor.apply(new Vector3s(x, y, (short) (z + 1)));
        // Top (+Y)
        visibleFaces[1] = !hasNeighbor.apply(new Vector3s(x, (short) (y + 1), z));
        // Right (+X)
        visibleFaces[2] = !hasNeighbor.apply(new Vector3s((short) (x + 1), y, z));
        // Left (-X)
        visibleFaces[3] = !hasNeighbor.apply(new Vector3s((short) (x - 1), y, z));
        // Bottom (-Y)
        visibleFaces[4] = !hasNeighbor.apply(new Vector3s(x, (short) (y - 1), z));
        // Back (-Z)
        visibleFaces[5] = !hasNeighbor.apply(new Vector3s(x, y, (short) (z - 1)));

        // load texture
        this.texture = scene.getTextureCache().createTexture(texturePath);

        // create material
        Material material = new Material();
        material.setTexturePath(texturePath);
        List<Material> materialList = new ArrayList<>();
        materialList.add(material);

        // create mesh with only visible faces
        int[] indices = Helpers.buildIndices(visibleFaces);
        Mesh mesh = new Mesh(Helpers.POSITIONS, Helpers.TEXT_COORDS, indices);
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
