package engine.world.gen;

import engine.block.BlockRegistry;
import engine.block.BlockType;

public class BlockGenerator {

    private static final double NOISE_SCALE = 0.02;
    private static final int OCTAVES = 4;
    private static PerlinNoise noise;
    private static int noiseSeed;
    private static boolean noiseInit = false;

    // Initializes the Perlin noise generator if not already initialized, or if the
    // seed has changed. Doing this avoids recreating the noise generator every time
    // getBlockAt is called, which is computationally expensive.
    private static PerlinNoise getNoise(int seed) {
        if (!noiseInit || noiseSeed != seed) {
            noise = new PerlinNoise(OCTAVES, seed);
            noiseSeed = seed;
            noiseInit = true;
        }
        return noise;
    }

    public static BlockType getBlockAt(int x, int y, int z, int seed) {
        int surfaceY = 50 + (int) (getNoise(seed).sample2d(x * NOISE_SCALE, z * NOISE_SCALE) * 30);
        return (y < surfaceY) ? BlockRegistry.get("stone") : null;
    }

}
