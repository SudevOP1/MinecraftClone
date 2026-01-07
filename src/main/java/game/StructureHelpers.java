package game;

import java.util.HashMap;
import java.util.Map;

import data_structures.Vector3s;
import engine.block.BlockRegistry;
import engine.block.BlockType;

public class StructureHelpers {

    public static Map<Vector3s, BlockType> getTreeBlocks(short x, short y, short z) {
        return getTreeBlocks((int) x, (int) y, (int) z);
    }

    public static Map<Vector3s, BlockType> getTreeBlocks(int x, int y, int z) {
        Map<Vector3s, BlockType> blocks = new HashMap<>();
        BlockType oakLog = BlockRegistry.get("oak_log");
        BlockType oakLeaves = BlockRegistry.get("oak_leaves");

        // wood
        for (int blockY = y; blockY <= y + 3; blockY++) {
            blocks.put(new Vector3s(x, blockY, z), oakLog);
        }

        // bottom 2 leaf layers
        for (int blockX = x - 2; blockX <= x + 2; blockX++) {
            for (int blockZ = z - 2; blockZ <= z + 2; blockZ++) {
                for (int blockY = y + 2; blockY <= y + 3; blockY++) {
                    if (!((blockX == x && blockZ == z)
                            || (blockX == x + 2 && blockZ == z + 2)
                            || (blockX == x + 2 && blockZ == z - 2)
                            || (blockX == x - 2 && blockZ == z + 2)
                            || (blockX == x - 2 && blockZ == z - 2))) {
                        blocks.put(new Vector3s(blockX, blockY, blockZ), oakLeaves);
                    }
                }
            }
        }

        // bottom 2 leaf layer corners (randomized)
        int[][] corners = {
                { -2, -2 }, { -2, 2 }, { 2, -2 }, { 2, 2 }
        };
        for (int[] corner : corners) {
            randomlyPut(blocks, x + corner[0], y + 2, z + corner[1], oakLeaves, 0.7);
            randomlyPut(blocks, x + corner[0], y + 3, z + corner[1], oakLeaves, 0.7);
        }

        // 3rd leaf layer
        for (int blockX = x - 1; blockX <= x + 1; blockX++) {
            for (int blockZ = z - 1; blockZ <= z + 1; blockZ++) {
                blocks.put(new Vector3s(blockX, y + 4, blockZ), oakLeaves);
            }
        }

        // 4th leaf layer (top)
        blocks.put(new Vector3s(x, y + 5, z), oakLeaves);
        blocks.put(new Vector3s(x - 1, y + 5, z), oakLeaves);
        blocks.put(new Vector3s(x + 1, y + 5, z), oakLeaves);
        blocks.put(new Vector3s(x, y + 5, z - 1), oakLeaves);
        blocks.put(new Vector3s(x, y + 5, z + 1), oakLeaves);

        return blocks;
    }

    private static void randomlyPut(
            Map<Vector3s, BlockType> blocks,
            int x, int y, int z,
            BlockType type,
            double probability) {

        if (Math.random() < probability) {
            blocks.put(new Vector3s(x, y, z), type);
        }
    }
}
