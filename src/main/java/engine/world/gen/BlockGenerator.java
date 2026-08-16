package engine.world.gen;

import engine.block.BlockRegistry;
import engine.block.BlockType;
import game.Settings;

public class BlockGenerator {

    public static final int OCTAVES = 4;

    // Offsets keep the three noise fields independent while still deriving from the
    // world seed, so two different parameters never produce identical maps.
    public static final int CONTINENTALNESS_SEED_OFFSET = 0;
    public static final int EROSION_SEED_OFFSET = 6969;
    public static final int PEAKS_AND_VALLEYS_SEED_OFFSET = 69420;
    public static final int DIRT_DEPTH_SEED_OFFSET = -6969;

    private static PerlinNoise continentalnessNoise;
    private static PerlinNoise erosionNoise;
    private static PerlinNoise peaksAndValleysNoise;
    private static PerlinNoise dirtDepthNoise;
    private static int noiseSeed;
    private static boolean noiseInit = false;

    // The surface height only depends on x and z, but getBlockAt is called once per
    // y in a column, so the last column's result is cached instead of recomputed.
    private static int cachedColumnX;
    private static int cachedColumnZ;
    private static int cachedColumnSeed;
    private static int cachedSurfaceY;
    private static int cachedDirtDepth;
    private static boolean columnCacheValid = false;

    // Initializes the Perlin noise generators if not already initialized, or if the
    // seed has changed. Doing this avoids recreating them every time getBlockAt is
    // called, which is computationally expensive.
    private static void initNoise(int seed) {
        if (noiseInit && noiseSeed == seed) {
            return;
        }
        continentalnessNoise = new PerlinNoise(OCTAVES, seed + CONTINENTALNESS_SEED_OFFSET);
        erosionNoise = new PerlinNoise(OCTAVES, seed + EROSION_SEED_OFFSET);
        peaksAndValleysNoise = new PerlinNoise(OCTAVES, seed + PEAKS_AND_VALLEYS_SEED_OFFSET);
        dirtDepthNoise = new PerlinNoise(OCTAVES, seed + DIRT_DEPTH_SEED_OFFSET);
        noiseSeed = seed;
        noiseInit = true;
        columnCacheValid = false;
    }

    // Clamps a raw noise sample onto the [-1, 1] domain that the spline points in
    // Settings are defined over. Perlin noise only lands roughly in that range, so
    // the clamp keeps the odd overshoot from running off the end of a spline.
    public static float toSplineInput(double noiseValue) {
        return (float) Math.min(Math.max(noiseValue, -1.0), 1.0);
    }

    // Same sample remapped onto [0, 1], for the values that are used as a plain
    // fraction rather than fed through a spline.
    public static float toUnitRange(double noiseValue) {
        return (toSplineInput(noiseValue) + 1.0f) * 0.5f;
    }

    // Linearly interpolates the spline defined by `points` (sorted by x) at `x`.
    // Values outside the spline's x range clamp to the first/last point.
    public static float evaluateSpline(float[][] points, float x) {
        int last = points.length - 1;
        if (x <= points[0][0]) {
            return points[0][1];
        }
        if (x >= points[last][0]) {
            return points[last][1];
        }

        for (int i = 0; i < last; i++) {
            float x0 = points[i][0];
            float x1 = points[i + 1][0];
            if (x >= x0 && x <= x1) {
                float span = x1 - x0;
                if (span <= 0.0f) { // two points share an x, the later one wins
                    return points[i + 1][1];
                }
                float t = (x - x0) / span;
                return points[i][1] + t * (points[i + 1][1] - points[i][1]);
            }
        }
        return points[last][1];
    }

    // Samples one terrain parameter and turns it into the height that parameter
    // alone would like the terrain to have.
    public static float splineHeight(PerlinNoise noise, double scale, float[][] points, int x, int z) {
        return evaluateSpline(points, toSplineInput(noise.sample2d(x * scale, z * scale)));
    }

    // Blends the three terrain parameters into a single surface height. Each spline
    // maps its parameter to an absolute height, and the weights decide how much say
    // each one has: continentalness sets the broad land/ocean shape, erosion
    // flattens or roughens it, and peaks and valleys adds the local relief. The
    // weights are normalized against their own sum, so only their ratio matters.
    // Split out from computeSurfaceY so tools can preview other weights without
    // editing Settings.
    public static int blendSurfaceY(float continentalness, float erosion, float peaksAndValleys,
            float continentalnessWeight, float erosionWeight, float peaksAndValleysWeight) {
        float totalWeight = continentalnessWeight + erosionWeight + peaksAndValleysWeight;
        if (totalWeight <= 0.0f) { // misconfigured weights, fall back to a flat world
            return Settings.SEA_LEVEL;
        }

        float height = (continentalness * continentalnessWeight
                + erosion * erosionWeight
                + peaksAndValleys * peaksAndValleysWeight) / totalWeight;

        int surfaceY = Math.round(height);
        return Math.min(Math.max(surfaceY, 0), Settings.CHUNK_HEIGHT - 1);
    }

    private static int computeSurfaceY(int x, int z) {
        float continentalness = splineHeight(continentalnessNoise, Settings.CONTINENTALNESS_NOISE_SCALE,
                Settings.CONTINENTALNESS_SPLINE_POINTS, x, z);
        float erosion = splineHeight(erosionNoise, Settings.EROSION_NOISE_SCALE,
                Settings.EROSION_SPLINE_POINTS, x, z);
        float peaksAndValleys = splineHeight(peaksAndValleysNoise, Settings.PEAKS_AND_VALLEYS_NOISE_SCALE,
                Settings.PEAKS_AND_VALLEYS_SPLINE_POINTS, x, z);

        return blendSurfaceY(continentalness, erosion, peaksAndValleys,
                Settings.CONTINENTALNESS_WEIGHT, Settings.EROSION_WEIGHT, Settings.PEAKS_AND_VALLEYS_WEIGHT);
    }

    // Fills the column cache for (x, z) if it does not already hold that column.
    private static void prepareColumn(int x, int z, int seed) {
        initNoise(seed);
        if (columnCacheValid && cachedColumnX == x && cachedColumnZ == z && cachedColumnSeed == seed) {
            return;
        }

        cachedSurfaceY = computeSurfaceY(x, z);

        double dirtScale = Settings.DIRT_DEPTH_NOISE_SCALE;
        float dirtNoise = toUnitRange(dirtDepthNoise.sample2d(x * dirtScale, z * dirtScale));
        cachedDirtDepth = Settings.MIN_DIRT_DEPTH
                + Math.round(dirtNoise * (Settings.MAX_DIRT_DEPTH - Settings.MIN_DIRT_DEPTH));

        cachedColumnX = x;
        cachedColumnZ = z;
        cachedColumnSeed = seed;
        columnCacheValid = true;
    }

    public static BlockType getBlockAt(int x, int y, int z, int seed) {

        prepareColumn(x, z, seed);
        int surfaceY = cachedSurfaceY;
        int dirtDepth = cachedDirtDepth;

        if (y < surfaceY - dirtDepth) {
            return BlockRegistry.get("stone");
        } else if (y < surfaceY) {
            return BlockRegistry.get("dirt_block");
        } else if (y == surfaceY) {
            return BlockRegistry.get("grass_block");
        } else {
            return null;
        }
    }

}
