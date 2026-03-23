package engine.world;

import java.util.HashMap;
import java.util.Map;

import data_structures.Vector3s;
import engine.block.BlockRegistry;
import engine.block.BlockType;
import engine.world.gen.StructureGenerator;
import game.Settings;

public class Chunk {

    public short x; // chunk's smallest x-coord
    public short z; // chunk's smallest z-coord
    int seed;

    public BlockType[][][] blocks;
    private Map<Vector3s, BlockType> blockTypes;

    public Chunk(short x, short z, int seed) {
        this.x = x;
        this.z = z;
        this.seed = seed;

        blocks = new BlockType[Settings.CHUNK_WIDTH][Settings.CHUNK_WIDTH][Settings.CHUNK_WIDTH];
        blockTypes = new HashMap<>();

        this.initBlocks();
    }

    public Chunk(int x, int z, int seed) {
        this((short) x, (short) z, seed);
    }

    // Populates blockTypes with test data. This will be replaced with noise-based
    // terrain generation later.
    private void initBlocks() {

        // // testing one block type
        // this.blockTypes.put(new Vector3s(5, 2, 5), BlockRegistry.get("oak_leaves"));
        // for (int x = 2; x < 12; x++) {
        // for (int z = 0; z < 10; z++) {
        // this.blockTypes.put(new Vector3s(x, 0, z), BlockRegistry.get("oak_leaves"));
        // }
        // }
        //
        //
        // // testing two block types
        // for (int x = 2; x < 12; x++) {
        // for (int z = 0; z < 10; z++) {
        // this.blockTypes.put(new Vector3s(-x, 0, z),
        // BlockRegistry.get("grass_block"));
        // this.blockTypes.put(new Vector3s(x, 0, z), BlockRegistry.get("cobblestone"));
        // }
        // }
        //
        //
        // // testing all block types
        // String[] blockNames = BlockRegistry.keySet().toArray(new String[0]);
        // for (int bx = 0; bx < blockNames.length; bx++) {
        // for (int bz = 0; bz < 5; bz++) {
        // BlockType blockType = BlockRegistry.get(blockNames[bx]);
        // this.blockTypes.put(new Vector3s(2 * bx, 0, bz), blockType);
        // }
        // }
        //
        //
        // tree simulation
        Map<Vector3s, BlockType> treeBlocks = StructureGenerator.generateOakTree(Settings.CHUNK_WIDTH / 2, 1, Settings.CHUNK_WIDTH / 2);
        this.blockTypes.putAll(treeBlocks);
        for (int i = 0; i < Settings.CHUNK_WIDTH; i++) {
            for (int j = 0; j < Settings.CHUNK_WIDTH; j++) {
                this.blockTypes.put(new Vector3s(i, 0, j), BlockRegistry.get("grass_block"));
            }
        }
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
        return this.blocks[x][y][z];
    }
}
