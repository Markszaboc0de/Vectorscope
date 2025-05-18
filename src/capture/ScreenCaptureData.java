package capture;

import java.awt.image.BufferedImage;

public class ScreenCaptureData {
    private int width;
    private int height;
    private int[][] rgb;
    private double[][] luminosity;

    public ScreenCaptureData(BufferedImage image) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.rgb = new int[height][width];
        this.luminosity = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                rgb[y][x] = pixel;
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
                luminosity[y][x] = 0.2126 * r + 0.7152 * g + 0.0722 * b;
            }
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int[][] getRgb() { return rgb; }
    public double[][] getLuminosity() { return luminosity; }
} 