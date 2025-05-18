package ui;

import capture.ScreenCaptureUtils;
import capture.ScreenCaptureData;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

public class ScreenCaptureOverlay extends JWindow {
    private Point startPoint;
    private Point endPoint;
    private Rectangle captureRect;

    public ScreenCaptureOverlay(JFrame parent, BiConsumer<ScreenCaptureData, Rectangle> onCapture) {
        super(parent);
        setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        setBackground(new Color(0, 0, 0, 32));
        setAlwaysOnTop(true);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
                endPoint = startPoint;
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                endPoint = e.getPoint();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                endPoint = e.getPoint();
                captureRect = getRectangle(startPoint, endPoint);
                setVisible(false);
                dispose();
                if (captureRect.width > 0 && captureRect.height > 0) {
                    ScreenCaptureData data = ScreenCaptureUtils.capture(captureRect);
                    if (onCapture != null && data != null) {
                        onCapture.accept(data, captureRect);
                    }
                }
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    private Rectangle getRectangle(Point p1, Point p2) {
        int x = Math.min(p1.x, p2.x);
        int y = Math.min(p1.y, p2.y);
        int w = Math.abs(p1.x - p2.x);
        int h = Math.abs(p1.y - p2.y);
        return new Rectangle(x, y, w, h);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (startPoint != null && endPoint != null) {
            Rectangle rect = getRectangle(startPoint, endPoint);
            g.setColor(new Color(0, 0, 255, 64));
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
            g.setColor(Color.BLUE);
            g.drawRect(rect.x, rect.y, rect.width, rect.height);
        }
    }

    public Rectangle getLastCaptureRect() {
        return captureRect;
    }
} 