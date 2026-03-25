package engine.world;

import java.util.HashMap;
import java.util.Map;

import data_structures.Vector3s;
import engine.block.Block;
import engine.block.BlockRegistry;
import engine.block.BlockType;
import engine.scene.Scene;
import engine.world.gen.StructureGenerator;
import game.Settings;

public class Chunk {

    public short x; // chunk's x-coord
    public short z; // chunk's z-coord
    int seed;

    public BlockType[][][] blocks;
    private Map<Vector3s, BlockType> blockTypes;
    private Map<Vector3s, Block> renderBlocks;

    public Chunk(short x, short z, int seed) {
        this.x = x;
        this.z = z;
        this.seed = seed;

        blocks = new BlockType[Settings.CHUNK_WIDTH][Settings.CHUNK_WIDTH][Settings.CHUNK_WIDTH];
        blockTypes = new HashMap<>();
        renderBlocks = new HashMap<>();
    }

    public Chunk(int x, int z, int seed) {
        this((short) x, (short) z, seed);
    }

    // Populates blockTypes with test data. This will be replaced with noise-based terrain generation later.
    public void generate(Scene scene) {

        // tree simulation
        Map<Vector3s, BlockType> treeBlocks = StructureGenerator.generateOakTree(Settings.CHUNK_WIDTH / 2, 1, Settings.CHUNK_WIDTH / 2);
        this.blockTypes.putAll(treeBlocks);
        for (int i = 0; i < Settings.CHUNK_WIDTH; i++) {
            for (int j = 0; j < Settings.CHUNK_WIDTH; j++) {
                this.blockTypes.put(new Vector3s(i, 0, j), BlockRegistry.get("grass_block"));
            }
        }

        this.generateBlocks(scene);
    }

    public Map<Vector3s, BlockType> getBlockTypes() {
        Map<Vector3s, BlockType> actualBlockTypes = new HashMap<>();
        int xOffset = this.x * Settings.CHUNK_WIDTH;
        int zOffset = this.z * Settings.CHUNK_WIDTH;

        for (Map.Entry<Vector3s, BlockType> entry : this.blockTypes.entrySet()) {
            Vector3s localPos = entry.getKey();
            actualBlockTypes.put(new Vector3s(localPos.x + xOffset, localPos.y, localPos.z + zOffset), entry.getValue());
        }

        return actualBlockTypes;
    }

    public BlockType getBlockType(short x, short y, short z) {
        BlockType type = this.blocks[x][y][z];
        if (type != null) {
            return type;
        }
        return this.blockTypes.get(new Vector3s(x, y, z));
    }

    // Creates Block render objects for this chunk, performing face culling.
    public void generateBlocks(Scene scene) {
        Map<Vector3s, BlockType> actualBlockTypes = this.getBlockTypes();

        for (Map.Entry<Vector3s, BlockType> entry : actualBlockTypes.entrySet()) {
            Vector3s pos = entry.getKey();
            BlockType type = entry.getValue();

            if (type == null) {
                continue;
            }

            Block block = new Block(
                    scene,
                    type,
                    pos.x,
                    pos.y,
                    pos.z,
                    p -> actualBlockTypes.get(p));

            // Store the render block using local coords
            int xOffset = this.x * Settings.CHUNK_WIDTH;
            int zOffset = this.z * Settings.CHUNK_WIDTH;
            Vector3s localPos = new Vector3s(pos.x - xOffset, pos.y, pos.z - zOffset);
            renderBlocks.put(localPos, block);
        }
    }

    // Regenerates a single block's render mesh with updated face culling. Call this on neighbors after a block is broken/placed.
    public void regenerateBlock(Scene scene, short localX, short localY, short localZ,
            java.util.function.Function<Vector3s, BlockType> getWorldBlock) {
        Vector3s localPos = new Vector3s(localX, localY, localZ);
        BlockType type = this.blockTypes.get(localPos);
        if (type == null) {
            return; // no block here, nothing to regenerate
        }

        // Remove old render block
        Block oldBlock = this.renderBlocks.remove(localPos);
        if (oldBlock != null) {
            scene.removeEntity(oldBlock.getEntity());
            scene.removeModel(oldBlock.getBlockId() + "-model");
        }

        // Create new render block with updated face culling
        int xOffset = this.x * Settings.CHUNK_WIDTH;
        int zOffset = this.z * Settings.CHUNK_WIDTH;
        short worldX = (short) (localX + xOffset);
        short worldZ = (short) (localZ + zOffset);

        Block newBlock = new Block(scene, type, worldX, localY, worldZ, getWorldBlock);
        this.renderBlocks.put(localPos, newBlock);
    }

    public void removeBlock(short x, short y, short z) {
        this.blockTypes.remove(new Vector3s(x, y, z));
    }

    public void removeBlock(Scene scene, short x, short y, short z) {
        this.blockTypes.remove(new Vector3s(x, y, z));
        Block block = this.renderBlocks.remove(new Vector3s(x, y, z));
        if (block != null) {
            scene.removeEntity(block.getEntity());
            scene.removeModel(block.getBlockId() + "-model");
        }
    }

    public void placeBlock(short x, short y, short z, BlockType blockType) {
        this.blockTypes.put(new Vector3s(x, y, z), blockType);
    }

}
