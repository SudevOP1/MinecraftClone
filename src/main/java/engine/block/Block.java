package engine.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import data_structures.Vector3s;
import engine.graph.Material;
import engine.graph.Mesh;
import engine.graph.Model;
import engine.graph.Texture;
import engine.scene.Entity;
import engine.scene.Scene;

public class Block {

    private Vector3s position;
    private Texture texture;
    private Entity entity;
    private String blockId;
    private final BlockType type;

    private static class Helpers {

        private static int blockCounter = 0;

        private static int[] buildIndices(boolean[] visibleFaces) {
            int visible = 0;
            for (boolean f : visibleFaces) {
                if (f) {
                    visible++;
                }
            }

            int[] result = new int[visible * 6];
            int w = 0;
            // Each face uses 4 consecutive vertices in POSITIONS: base = face*4
            for (int face = 0; face < BlockGeometry.FACE_COUNT; face++) {
                if (!visibleFaces[face]) {
                    continue;
                }
                int b = face * 4;
                // two triangles: (b, b+1, b+3) and (b+3, b+1, b+2)
                result[w++] = b;
                result[w++] = b + 1;
                result[w++] = b + 3;
                result[w++] = b + 3;
                result[w++] = b + 1;
                result[w++] = b + 2;
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
            Function<Vector3s, BlockType> getNeighborType) {

        this.position = new Vector3s(x, y, z);
        this.blockId = type.codename + "-" + Helpers.blockCounter++;
        this.type = type;

        // check which faces are visible if block is not transparent and render the face
        // if neighbor is transparent
        boolean[] visibleFaces = new boolean[BlockGeometry.FACE_COUNT];
        Vector3s neighbor = new Vector3s(x, y, z);
        for (int face = 0; face < BlockGeometry.FACE_COUNT; face++) {
            int o = face * 3;
            neighbor.set(
                    (short) (x + BlockGeometry.FACE_OFFSETS[o]),
                    (short) (y + BlockGeometry.FACE_OFFSETS[o + 1]),
                    (short) (z + BlockGeometry.FACE_OFFSETS[o + 2]));
            BlockType neighborType = getNeighborType.apply(neighbor);
            visibleFaces[face] = neighborType == null || neighborType.hasTransparency;
        }

        // load texture (now using the texture atlas)
        this.texture = scene.getTextureCache().createTexture("textures/texture_atlas.png");

        // create material
        Material material = new Material();
        material.setTexturePath("textures/texture_atlas.png");

        // Enable transparency for blocks that need it
        if (type.hasTransparency) {
            material.setTransparent(true);
        }

        List<Material> materialList = new ArrayList<>();
        materialList.add(material);

        // Build texture coordinates based on BlockType
        float[] textureCoords = BlockGeometry.getTexCoords(type);

        // create mesh with only visible faces
        int[] indices = Helpers.buildIndices(visibleFaces);
        Mesh mesh = new Mesh(BlockGeometry.POSITIONS, textureCoords, indices);
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

    public Vector3s getPosition() {
        return this.position;
    }

    public void setPosition(short x, short y, short z) {
        this.position.set(x, y, z);
        this.entity.setPosition(x, y, z);
        this.entity.updateModelMatrix();
    }

    public void setPosition(Vector3s position) {
        this.position.set(position.x, position.y, position.z);
        this.entity.setPosition(position.x, position.y, position.z);
        this.entity.updateModelMatrix();
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

    public BlockType getBlockType() {
        return this.type;
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
