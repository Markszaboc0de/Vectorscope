package capture;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenCaptureUtils {
    public static ScreenCaptureData capture(Rectangle rect) {
        try {
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(rect);
            return new ScreenCaptureData(image);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
} 