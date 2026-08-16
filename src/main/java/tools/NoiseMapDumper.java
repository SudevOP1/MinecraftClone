package tools;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import engine.world.gen.BlockGenerator;
import engine.world.gen.PerlinNoise;
import game.Settings;

// Headless helper for tools/noise_map_viewer.py. Samples the three terrain
// parameters over a rectangle of world coordinates using the real generator code
// and writes the results to a file, so the viewer never has to reimplement the
// noise.
//
// Usage:
//   java -cp target/classes tools.NoiseMapDumper <seed> <minX> <maxX> <minZ> <maxZ>
//       <continentalnessScale> <erosionScale> <peaksAndValleysScale> <outputFile>
//
// Output is big endian: two ints holding the x and z side lengths, then three
// sizeX * sizeZ blocks of floats (continentalness, erosion, peaks and valleys),
// each the spline height for that parameter, indexed as [x][z].
public class NoiseMapDumper {

    private static void dump(DataOutputStream out, PerlinNoise noise, double scale, float[][] splinePoints,
            int minX, int sizeX, int minZ, int sizeZ) throws IOException {
        for (int ix = 0; ix < sizeX; ix++) {
            int worldX = minX + ix;
            for (int iz = 0; iz < sizeZ; iz++) {
                int worldZ = minZ + iz;
                out.writeFloat(BlockGenerator.splineHeight(noise, scale, splinePoints, worldX, worldZ));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 9) {
            System.err.println("usage: NoiseMapDumper <seed> <minX> <maxX> <minZ> <maxZ> <contScale>"
                    + " <eroScale> <pvScale> <outputFile>");
            System.exit(1);
        }

        int seed = Integer.parseInt(args[0]);
        int minX = Integer.parseInt(args[1]);
        int maxX = Integer.parseInt(args[2]);
        int minZ = Integer.parseInt(args[3]);
        int maxZ = Integer.parseInt(args[4]);
        double continentalnessScale = Double.parseDouble(args[5]);
        double erosionScale = Double.parseDouble(args[6]);
        double peaksAndValleysScale = Double.parseDouble(args[7]);
        String outputFile = args[8];

        int sizeX = maxX - minX + 1;
        int sizeZ = maxZ - minZ + 1;
        if (sizeX <= 0 || sizeZ <= 0) {
            System.err.println("maxX and maxZ must be >= minX and minZ");
            System.exit(1);
        }

        PerlinNoise continentalnessNoise = new PerlinNoise(BlockGenerator.OCTAVES,
                seed + BlockGenerator.CONTINENTALNESS_SEED_OFFSET);
        PerlinNoise erosionNoise = new PerlinNoise(BlockGenerator.OCTAVES,
                seed + BlockGenerator.EROSION_SEED_OFFSET);
        PerlinNoise peaksAndValleysNoise = new PerlinNoise(BlockGenerator.OCTAVES,
                seed + BlockGenerator.PEAKS_AND_VALLEYS_SEED_OFFSET);

        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(outputFile)))) {
            out.writeInt(sizeX);
            out.writeInt(sizeZ);
            dump(out, continentalnessNoise, continentalnessScale, Settings.CONTINENTALNESS_SPLINE_POINTS,
                    minX, sizeX, minZ, sizeZ);
            dump(out, erosionNoise, erosionScale, Settings.EROSION_SPLINE_POINTS,
                    minX, sizeX, minZ, sizeZ);
            dump(out, peaksAndValleysNoise, peaksAndValleysScale, Settings.PEAKS_AND_VALLEYS_SPLINE_POINTS,
                    minX, sizeX, minZ, sizeZ);
        }
    }

}
