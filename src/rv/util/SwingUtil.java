package rv.util;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import javax.swing.JFrame;

public class SwingUtil {
    public static GraphicsDevice getCurrentScreen(JFrame frame) {
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getScreenDevices();

        GraphicsDevice bestMatch = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
        float bestPercentage = 0;
        for (GraphicsDevice device : devices) {
            Rectangle bounds = device.getDefaultConfiguration().getBounds();
            float percentage = getPercentageOnScreen(frame, bounds);

            if (percentage > bestPercentage) {
                bestMatch = device;
                bestPercentage = percentage;
            }
        }
        return bestMatch;
    }

    private static float getPercentageOnScreen(JFrame frame, Rectangle screen) {
        Rectangle frameBounds = new Rectangle(frame.getLocation(), frame.getSize());
        Rectangle2D intersection = frameBounds.createIntersection(screen);
        int screenArea = screen.width * screen.height;
        int intersectionArea = (int) Math.abs(intersection.getWidth() * intersection.getHeight());
        return (float) intersectionArea / screenArea;
    }

    public static Point getCurrentScreenLocation(JFrame frame) {
        GraphicsDevice currentScreen = getCurrentScreen(frame);
        if (currentScreen == GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()) {
            return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds()
                    .getLocation();
        }
        return currentScreen.getDefaultConfiguration().getBounds().getLocation();
    }
}
