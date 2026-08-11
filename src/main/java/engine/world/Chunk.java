package engine.world;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import engine.block.BlockType;
import engine.graph.Material;
import engine.graph.Mesh;
import engine.graph.Model;
import engine.scene.Entity;
import engine.scene.Scene;
import engine.world.gen.BlockGenerator;
import game.Settings;

public class Chunk {

    private static final int WIDTH = Settings.CHUNK_WIDTH;
    private static final int HEIGHT = Settings.CHUNK_HEIGHT;
    private static final int VOLUME = WIDTH * HEIGHT * WIDTH;
    private static final String ATLAS_PATH = "textures/texture_atlas.png";

    public final short x; // chunk's x-coord (in chunks, not blocks)
    public final short z; // chunk's z-coord (in chunks, not blocks)
    public final int seed;

    // Flat array indexed by index(). Null entry == air. A dense array beats a
    // HashMap<Vector3s, BlockType> here: no hashing, no boxed keys, no per-block
    // allocation, and the mesher walks it in memory order.
    private final BlockType[] blocks = new BlockType[VOLUME];
    private boolean dataGenerated = false;

    // Render side: the whole chunk is a single Model (one merged mesh for opaque
    // blocks, one for cutout blocks) and a single Entity placed at the chunk
    // origin, so the chunk costs 1-2 draw calls instead of one per block.
    private Entity entity;

    public Chunk(short x, short z, int seed) {
        this.x = x;
        this.z = z;
        this.seed = seed;
    }

    public Chunk(int x, int z, int seed) {
        this((short) x, (short) z, seed);
    }

    // Index layout keeps y contiguous, so the mesher's innermost loop walks
    // straight down the array.
    private static int index(int localX, int y, int localZ) {
        return (localX * WIDTH + localZ) * HEIGHT + y;
    }

    public static boolean inBounds(int localX, int y, int localZ) {
        return localX >= 0 && localX < WIDTH
                && y >= 0 && y < HEIGHT
                && localZ >= 0 && localZ < WIDTH;
    }

    // No bounds check - callers inside the world package guarantee the range.
    BlockType getBlockUnchecked(int localX, int y, int localZ) {
        return this.blocks[index(localX, y, localZ)];
    }

    public BlockType getBlockType(int localX, int y, int localZ) {
        if (!inBounds(localX, y, localZ)) {
            return null;
        }
        return this.blocks[index(localX, y, localZ)];
    }

    public void setBlockType(int localX, int y, int localZ, BlockType blockType) {
        if (!inBounds(localX, y, localZ)) {
            return;
        }
        this.blocks[index(localX, y, localZ)] = blockType;
    }

    // Topmost non-air block, centermost column first. If center column is all
    // air, spirals outward ring by ring within the chunk for the nearest
    // column that has one.
    public Vector3f getSpawnableBlockCoords() {
        int centerX = WIDTH / 2;
        int centerZ = WIDTH / 2;

        int localX = centerX;
        int localZ = centerZ;
        int topY = findTopmostY(centerX, centerZ);

        if (topY < 0) {
            outer: for (int radius = 1; radius < WIDTH; radius++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        // only ring cells, not ones already checked at smaller radius
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                            continue;
                        }
                        int cx = centerX + dx;
                        int cz = centerZ + dz;
                        if (cx < 0 || cx >= WIDTH || cz < 0 || cz >= WIDTH) {
                            continue;
                        }
                        int y = findTopmostY(cx, cz);
                        if (y >= 0) {
                            localX = cx;
                            localZ = cz;
                            topY = y;
                            break outer;
                        }
                    }
                }
            }
        }

        // whole chunk is air - stand on top of it, still centered
        if (topY < 0) {
            topY = 0;
        }

        float worldX = this.x * WIDTH + localX + 0.5f;
        float worldZ = this.z * WIDTH + localZ + 0.5f;
        return new Vector3f(worldX, topY + Settings.PLAYER_HEIGHT + 1, worldZ);
    }

    // -1 if column has no non-air block
    private int findTopmostY(int localX, int localZ) {
        for (int y = HEIGHT - 1; y >= 0; y--) {
            if (this.blocks[index(localX, y, localZ)] != null) {
                return y;
            }
        }
        return -1;
    }

    public boolean isDataGenerated() {
        return this.dataGenerated;
    }

    public boolean hasModel() {
        return this.entity != null;
    }

    // Fills the block array from the terrain generator. No OpenGL, no scene
    // access - safe to call before the chunk is ever rendered.
    public void generateData(int seed) {
        if (this.dataGenerated) {
            return;
        }

        int worldXOffset = this.x * WIDTH;
        int worldZOffset = this.z * WIDTH;

        for (int localX = 0; localX < WIDTH; localX++) {
            int worldX = worldXOffset + localX;
            for (int localZ = 0; localZ < WIDTH; localZ++) {
                int worldZ = worldZOffset + localZ;
                int base = (localX * WIDTH + localZ) * HEIGHT;
                for (int y = 0; y < HEIGHT; y++) {
                    this.blocks[base + y] = BlockGenerator.getBlockAt(worldX, y, worldZ, seed);
                }
            }
        }

        this.dataGenerated = true;
    }

    // (Re)builds this chunk's merged mesh and registers it with the scene.
    // Neighbors are used for border face culling; pass null for ungenerated ones.
    public void buildModel(Scene scene, Chunk negX, Chunk posX, Chunk negZ, Chunk posZ) {
        this.removeModel(scene);

        ChunkMesher.Result result = ChunkMesher.build(this, negX, posX, negZ, posZ);
        if (result.isEmpty()) {
            return;
        }

        scene.getTextureCache().createTexture(ATLAS_PATH);

        List<Material> materialList = new ArrayList<>(2);
        if (!result.solid.isEmpty()) {
            materialList.add(buildMaterial(result.solid, false));
        }
        if (!result.cutout.isEmpty()) {
            materialList.add(buildMaterial(result.cutout, true));
        }

        Model model = new Model(this.getModelId(), materialList);
        scene.addModel(model);

        this.entity = new Entity(this.getEntityId(), model.getId());
        this.entity.setPosition(this.x * WIDTH, 0, this.z * WIDTH);
        this.entity.updateModelMatrix();
        scene.addEntity(this.entity);
    }

    private static Material buildMaterial(ChunkMesher.Buffer buffer, boolean alphaCutout) {
        Material material = new Material();
        material.setTexturePath(ATLAS_PATH);
        material.setAlphaCutout(alphaCutout);
        material.getMeshList().add(new Mesh(
                buffer.positions, buffer.positionsLength,
                buffer.texCoords, buffer.texCoordsLength,
                buffer.light, buffer.lightLength,
                buffer.indices, buffer.indicesLength));
        return material;
    }

    // Drops the chunk's GPU buffers. Block data is kept so the chunk can be
    // re-meshed later without regenerating (and without losing player edits).
    public void removeModel(Scene scene) {
        if (this.entity != null) {
            scene.removeEntity(this.entity);
            this.entity = null;
        }
        scene.removeModel(this.getModelId());
    }

    public String getModelId() {
        return "chunk-" + this.x + "-" + this.z + "-model";
    }

    public String getEntityId() {
        return "chunk-" + this.x + "-" + this.z + "-entity";
    }

}
