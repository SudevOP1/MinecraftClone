package engine.world.gen;

import engine.block.BlockRegistry;
import engine.block.BlockType;

public class BlockGenerator {

    public static BlockType getBlockAt(int x, int y, int z) {
        int surfaceY = 20;
        return (y < surfaceY) ? BlockRegistry.get("stone") : null;
    }

}
