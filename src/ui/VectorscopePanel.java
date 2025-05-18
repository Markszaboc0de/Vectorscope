package ui;

import javax.swing.*;
import java.awt.*;
import capture.ScreenCaptureData;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.Timer;

public class VectorscopePanel extends JPanel {
    private ScreenCaptureData data;
    private Double referenceAngle = null;
    private Double referenceRadius = null;
    private boolean referenceIsLine = false;
    private double zoom = 1.0;
    private Integer clickedPixelR = null, clickedPixelG = null, clickedPixelB = null;
    private Float clickedPixelSat = null;
    private Double clickedPixelLum = null;

    public VectorscopePanel() {
        setPreferredSize(new Dimension(250, 210));
        setOpaque(true);
        setBackground(new Color(241, 241, 241)); // dark background

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    public void setScreenCaptureData(ScreenCaptureData data) {
        this.data = data;
        repaint();
    }

    public void setReferencePoint(Double angle, Double radius) {
        this.referenceAngle = angle;
        this.referenceRadius = radius;
        this.referenceIsLine = false;
        repaint();
    }

    public void setReferenceLine(Double angle, Double radius) {
        this.referenceAngle = angle;
        this.referenceRadius = radius;
        this.referenceIsLine = true;
        repaint();
    }

    public void autoSetReferenceLine() {
        if (data == null) return;
        int[][] rgb = data.getRgb();
        int wImg = data.getWidth();
        int hImg = data.getHeight();
        int bins = 360;
        int[] hueHist = new int[bins];
        double[] satSum = new double[bins];
        for (int y = 0; y < hImg; y++) {
            for (int x = 0; x < wImg; x++) {
                int pixel = rgb[y][x];
                int r = (pixel >> 16) & 0xff;
                int gC = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
                float[] hsv = Color.RGBtoHSB(r, gC, b, null);
                float hue = hsv[0]; // 0..1
                float sat = hsv[1]; // 0..1
                int bin = (int)(hue * bins) % bins;
                hueHist[bin]++;
                satSum[bin] += sat;
            }
        }
        int maxBin = 0;
        for (int i = 1; i < bins; i++) {
            if (hueHist[i] > hueHist[maxBin]) maxBin = i;
        }
        double angle = -Math.PI / 2 + ((double)maxBin / bins) * 2 * Math.PI;
        double avgSat = hueHist[maxBin] > 0 ? satSum[maxBin] / hueHist[maxBin] : 1.0;
        setReferenceLine(angle, avgSat);
    }

    public void clearReferenceLine() {
        referenceAngle = null;
        referenceRadius = null;
        referenceIsLine = false;
        repaint();
    }

    public boolean hasReferenceLine() {
        return referenceAngle != null && referenceRadius != null && referenceIsLine;
    }

    public Double getReferenceAngle() {
        return referenceAngle;
    }

    public Double getReferenceRadius() {
        return referenceRadius;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
        repaint();
    }

    private void handleClick(int mx, int my) {
        int w = getWidth();
        int h = getHeight();
        int radius = Math.min(w, h) / 2 - 20;
        int cx = w / 2;
        int cy = h / 2;
        int plotRadius = (int)(radius * zoom);
        double dx = mx - cx;
        double dy = my - cy;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > plotRadius || data == null) {
            clickedPixelR = clickedPixelG = clickedPixelB = null;
            clickedPixelSat = null;
            clickedPixelLum = null;
            repaint();
            return;
        }
        double angle = Math.atan2(dy, dx);
        double sat = dist / plotRadius;
        float hue = (float)(((angle + Math.PI/2) / (2 * Math.PI)) % 1.0);
        float bri = 1.0f;
        int rgb = Color.HSBtoRGB(hue, (float)sat, bri);
        int r = (rgb >> 16) & 0xff;
        int gC = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        double luminance = 0.2126 * r + 0.7152 * gC + 0.0722 * b;
        clickedPixelR = r;
        clickedPixelG = gC;
        clickedPixelB = b;
        clickedPixelSat = (float)sat;
        clickedPixelLum = luminance;
        repaint();
    }

    public String getClickedPixelInfo() {
        if (clickedPixelR == null) return null;
        return String.format("[Pixel] RGB: (%d, %d, %d)  Sat: %.2f  Lum: %.1f", clickedPixelR, clickedPixelG, clickedPixelB, clickedPixelSat, clickedPixelLum);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();
        int radius = Math.min(w, h) / 2 - 20;
        int cx = w / 2;
        int cy = h / 2;

        int plotRadius = (int)(radius * zoom); // for plotting points
        int circleRadius = (int) (radius * 1.1); // 10% bigger for the circle only

        // Draw circle (10% bigger)
        // g2.setColor(Color.DARK_GRAY);
        // g2.setStroke(new BasicStroke(2));
        // g2.drawOval(cx - circleRadius, cy - circleRadius, 2 * circleRadius, 2 * circleRadius);

        // Place R, G, B letters (G and B swapped)
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN};
        // Angles: R at -90°, B at +30°, G at +150
        for (int i = 0; i < 3; i++) {
            g2.setColor(colors[i]);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            // g2.drawString(letters[i], lx - 9, ly + 7);
        }

        // Draw skintone line (now at ~12° from horizontal, between R and G)
        double skintoneAngle = Math.toRadians(-90 + 12); // -90° is up (R), +12° is 12° counterclockwise from R
        int skintoneX = (int) (cx + circleRadius * Math.cos(skintoneAngle));
        int skintoneY = (int) (cy + circleRadius * Math.sin(skintoneAngle));
        g2.setColor(new Color(255, 230, 100, 180)); // subtle yellow
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(cx, cy, skintoneX, skintoneY);

        // Draw graticule: rings for 25%, 50%, 75%, 100% saturation
        g2.setColor(new Color(120, 120, 120, 80));
        g2.setStroke(new BasicStroke(1f));
        for (int i = 1; i <= 4; i++) {
            int rGrat = (int) (plotRadius * (i * 0.25));
            g2.drawOval(cx - rGrat, cy - rGrat, 2 * rGrat, 2 * rGrat);
        }

        // Draw angle reference lines every 30 degrees
        for (int deg = 0; deg < 360; deg += 30) {
            double angle = Math.toRadians(deg) - Math.PI / 2;
            int x1 = (int) (cx + plotRadius * Math.cos(angle));
            int y1 = (int) (cy + plotRadius * Math.sin(angle));
            int x0 = (int) (cx + (plotRadius - 10) * Math.cos(angle));
            int y0 = (int) (cy + (plotRadius - 10) * Math.sin(angle));
            g2.drawLine(x0, y0, x1, y1);
        }

        // Draw color targets for primaries and secondaries (BT.709)
        // Hue values: R=0, G=1/3, B=2/3, C=0.5, M=5/6, Y=1/6
        float[] hues = {0f, 1f/3f, 2f/3f, 0.5f, 5f/6f, 1f/6f};
        Color[] targetColors = {Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW};
        String[] targetLabels = {"R", "G", "B", "C", "M", "Y"};
        for (int i = 0; i < hues.length; i++) {
            double angle = -Math.PI / 2 + hues[i] * 2 * Math.PI;
            int tx = (int) (cx + plotRadius * Math.cos(angle));
            int ty = (int) (cy + plotRadius * Math.sin(angle));
            g2.setColor(targetColors[i]);
            g2.fillRect(tx - 5, ty - 5, 10, 10);
            g2.setColor(Color.BLACK);
            g2.drawRect(tx - 5, ty - 5, 10, 10);
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            g2.drawString(targetLabels[i], tx - 4, ty - 8);
        }

        // Plot captured pixel data if available
        if (data != null) {
            int[][] rgb = data.getRgb();
            int wImg = data.getWidth();
            int hImg = data.getHeight();
            for (int y = 0; y < hImg; y++) {
                for (int x = 0; x < wImg; x++) {
                    int pixel = rgb[y][x];
                    int r = (pixel >> 16) & 0xff;
                    int gC = (pixel >> 8) & 0xff;
                    int b = pixel & 0xff;
                    float[] hsv = Color.RGBtoHSB(r, gC, b, null);
                    float hue = hsv[0]; // 0..1
                    float sat = hsv[1]; // 0..1
                    // Use default mapping for correct color placement
                    double angle = -Math.PI / 2 + hue * 2 * Math.PI;
                    double pointRadius = sat * plotRadius;
                    int px = (int) (cx + pointRadius * Math.cos(angle));
                    int py = (int) (cy + pointRadius * Math.sin(angle));
                    g2.setColor(new Color(r, gC, b));
                    g2.fillRect(px, py, 2, 2);
                }
            }
        }

        // Draw reference point/line if set
        if (referenceAngle != null && referenceRadius != null) {
            int px = (int) (cx + referenceRadius * plotRadius * Math.cos(referenceAngle));
            int py = (int) (cy + referenceRadius * plotRadius * Math.sin(referenceAngle));
            if (referenceIsLine) {
                g2.setColor(Color.MAGENTA);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(cx, cy, px, py);
            } else {
                g2.setColor(Color.ORANGE);
                g2.fillOval(px - 3, py - 3, 6, 6);
            }
        }

        // (No info below scope; info will be shown by parent module)
    }
} 