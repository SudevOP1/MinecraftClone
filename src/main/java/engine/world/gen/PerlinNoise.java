package engine.world.gen;

import java.util.Random;

public class PerlinNoise {
    final int octaves;
    final int seed;

    private final int[] perm = new int[512];

    public PerlinNoise(int octaves, int seed) {
        this.octaves = octaves;
        this.seed = seed;

        int[] p = new int[256];
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }

        Random rand = new Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }

        for (int i = 0; i < 512; i++) {
            this.perm[i] = p[i & 255];
        }
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y) {
        int h = hash & 7;
        double u = h < 4 ? x : y;
        double v = h < 4 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    // Single-octave Perlin noise, roughly in [-1, 1].
    private double noise2d(double x, double y) {
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;

        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);

        double u = fade(xf);
        double v = fade(yf);

        int aa = this.perm[this.perm[xi] + yi];
        int ab = this.perm[this.perm[xi] + yi + 1];
        int ba = this.perm[this.perm[xi + 1] + yi];
        int bb = this.perm[this.perm[xi + 1] + yi + 1];

        double x1 = lerp(u, grad(aa, xf, yf), grad(ba, xf - 1, yf));
        double x2 = lerp(u, grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1));

        return lerp(v, x1, x2);
    }

    // Fractal Brownian motion over `octaves` lay1, 1].
    public double sample2d(double x, double y) {
        double total = 0;
        double frequency = 1;
        double amplitude = 1;
        double maxAmplitude = 0;

        for (int i = 0; i < this.octaves; i++) {
            total += noise2d(x * frequency, y * frequency) * amplitude;
            maxAmplitude += amplitude;
            amplitude *= 0.5;
            frequency *= 2;
        }

        return total / maxAmplitude;
    }
}
