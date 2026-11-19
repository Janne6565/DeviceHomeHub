package com.janne.webserverraspberrypi.services.ambilight;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Turns an album cover into a few dominant colors and renders them as a gradient that runs
 * around the frame. HyperHDR maps that frame onto the LED layout, so this class never needs to
 * know where the LEDs actually are.
 */
public final class AlbumPalette {

    static final int SAMPLE_SIZE = 64;
    static final int CLUSTERS = 5;
    static final int ITERATIONS = 20;

    /** Clusters darker or greyer than this are background, borders or text, not "the" color. */
    static final float MIN_BRIGHTNESS = 0.15f;
    static final float MIN_SATURATION = 0.20f;

    /** LEDs wash colors out, so they get pushed a bit before they leave the hub. */
    static final float SATURATION_GAIN = 1.35f;
    static final float SATURATION_OFFSET = 0.10f;
    static final float BRIGHTNESS_GAIN = 1.20f;
    static final float MIN_OUTPUT_BRIGHTNESS = 0.55f;

    public static final int FRAME_WIDTH = 64;
    public static final int FRAME_HEIGHT = 36;

    private AlbumPalette() {
    }

    /** A palette entry: the boosted color and its share of the kept clusters (shares sum to 1). */
    public record Swatch(Color color, double weight) {
    }

    public static List<Swatch> extract(BufferedImage cover) {
        double[][] pixels = sample(cover);
        double[][] centroids = initialCentroids(pixels);
        int[] labels = new int[pixels.length];

        for (int iteration = 0; iteration < ITERATIONS; iteration++) {
            for (int i = 0; i < pixels.length; i++) {
                labels[i] = nearest(pixels[i], centroids);
            }
            double[][] sums = new double[CLUSTERS][3];
            int[] counts = new int[CLUSTERS];
            for (int i = 0; i < pixels.length; i++) {
                counts[labels[i]]++;
                for (int c = 0; c < 3; c++) {
                    sums[labels[i]][c] += pixels[i][c];
                }
            }
            for (int k = 0; k < CLUSTERS; k++) {
                if (counts[k] > 0) {
                    for (int c = 0; c < 3; c++) {
                        centroids[k][c] = sums[k][c] / counts[k];
                    }
                }
            }
        }

        int[] counts = new int[CLUSTERS];
        for (int label : labels) {
            counts[label]++;
        }

        List<Candidate> candidates = new ArrayList<>();
        for (int k = 0; k < CLUSTERS; k++) {
            if (counts[k] == 0) {
                continue;
            }
            float[] hsb = Color.RGBtoHSB(
                    (int) Math.round(centroids[k][0]), (int) Math.round(centroids[k][1]), (int) Math.round(centroids[k][2]), null);
            candidates.add(new Candidate(hsb, (double) counts[k] / pixels.length));
        }

        List<Candidate> kept = candidates.stream()
                .filter(c -> c.hsb[2] > MIN_BRIGHTNESS && c.hsb[1] > MIN_SATURATION)
                .sorted(Comparator.comparingDouble(Candidate::weight).reversed())
                .toList();
        if (kept.isEmpty()) {
            // Black-and-white covers: take the most colorful thing there is instead of going dark.
            kept = List.of(candidates.stream()
                    .max(Comparator.comparingDouble(c -> c.hsb[1] * c.hsb[2]))
                    .orElseThrow());
        }

        double total = kept.stream().mapToDouble(Candidate::weight).sum();
        return kept.stream()
                .map(c -> new Swatch(boost(c.hsb), c.weight / total))
                .toList();
    }

    /**
     * Renders the swatches around the frame, clockwise from the top centre: each swatch owns a
     * slice proportional to its weight and blends into its neighbours.
     */
    public static BufferedImage render(List<Swatch> palette) {
        BufferedImage frame = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_RGB);
        int n = palette.size();
        // Stop i is the centre of swatch i's slice; the dominant swatch is centred on the top.
        double[] stops = new double[n];
        for (int i = 1; i < n; i++) {
            stops[i] = stops[i - 1] + (palette.get(i - 1).weight() + palette.get(i).weight()) / 2;
        }

        for (int y = 0; y < FRAME_HEIGHT; y++) {
            for (int x = 0; x < FRAME_WIDTH; x++) {
                frame.setRGB(x, y, colorAt(palette, stops, position(x, y)).getRGB());
            }
        }
        return frame;
    }

    /** Position around the frame in [0, 1), 0 at the top centre, increasing clockwise. */
    static double position(int x, int y) {
        double dx = x - FRAME_WIDTH / 2.0 + 0.5;
        double dy = y - FRAME_HEIGHT / 2.0 + 0.5;
        double t = Math.atan2(dy, dx) / (2 * Math.PI) + 0.25;
        return t - Math.floor(t);
    }

    private static Color colorAt(List<Swatch> palette, double[] stops, double t) {
        int n = palette.size();
        if (n == 1) {
            return palette.get(0).color();
        }
        for (int i = 0; i < n; i++) {
            double from = stops[i];
            double to = i == n - 1 ? 1 : stops[i + 1];
            if (t >= from && t < to) {
                return blend(palette.get(i).color(), palette.get((i + 1) % n).color(), (t - from) / (to - from));
            }
        }
        return palette.get(0).color();
    }

    private static Color blend(Color a, Color b, double f) {
        return new Color(
                (int) Math.round(a.getRed() * (1 - f) + b.getRed() * f),
                (int) Math.round(a.getGreen() * (1 - f) + b.getGreen() * f),
                (int) Math.round(a.getBlue() * (1 - f) + b.getBlue() * f));
    }

    private static Color boost(float[] hsb) {
        float saturation = Math.min(1f, hsb[1] * SATURATION_GAIN + SATURATION_OFFSET);
        float brightness = Math.max(MIN_OUTPUT_BRIGHTNESS, Math.min(1f, hsb[2] * BRIGHTNESS_GAIN));
        return Color.getHSBColor(hsb[0], saturation, brightness);
    }

    private static double[][] sample(BufferedImage cover) {
        BufferedImage small = new BufferedImage(SAMPLE_SIZE, SAMPLE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = small.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(cover, 0, 0, SAMPLE_SIZE, SAMPLE_SIZE, null);
        g.dispose();

        double[][] pixels = new double[SAMPLE_SIZE * SAMPLE_SIZE][];
        for (int y = 0; y < SAMPLE_SIZE; y++) {
            for (int x = 0; x < SAMPLE_SIZE; x++) {
                int rgb = small.getRGB(x, y);
                pixels[y * SAMPLE_SIZE + x] = new double[]{(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF};
            }
        }
        return pixels;
    }

    /** Deterministic init: pixels at evenly spaced luminance quantiles, so the same cover always gives the same palette. */
    private static double[][] initialCentroids(double[][] pixels) {
        Integer[] order = new Integer[pixels.length];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        java.util.Arrays.sort(order, Comparator.comparingDouble(i -> luminance(pixels[i])));
        double[][] centroids = new double[CLUSTERS][];
        for (int k = 0; k < CLUSTERS; k++) {
            int index = (int) Math.round((double) k * (pixels.length - 1) / (CLUSTERS - 1));
            centroids[k] = pixels[order[index]].clone();
        }
        return centroids;
    }

    private static double luminance(double[] p) {
        return 0.299 * p[0] + 0.587 * p[1] + 0.114 * p[2];
    }

    private static int nearest(double[] p, double[][] centroids) {
        int best = 0;
        double bestDistance = Double.MAX_VALUE;
        for (int k = 0; k < centroids.length; k++) {
            double dr = p[0] - centroids[k][0];
            double dg = p[1] - centroids[k][1];
            double db = p[2] - centroids[k][2];
            double distance = dr * dr + dg * dg + db * db;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = k;
            }
        }
        return best;
    }

    private record Candidate(float[] hsb, double weight) {
    }
}
