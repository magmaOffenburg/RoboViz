package rv.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class SwingUtil
{
	public static GraphicsDevice getCurrentScreen(Window window)
	{
		return getCurrentScreen(window.getLocation(), window.getSize());
	}

	public static GraphicsDevice getCurrentScreen(Point location, Dimension size)
	{
		GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

		GraphicsDevice bestMatch = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		float bestPercentage = 0;
		for (GraphicsDevice device : devices) {
			Rectangle bounds = device.getDefaultConfiguration().getBounds();
			float percentage = getPercentageOnScreen(location, size, bounds);

			if (percentage > bestPercentage) {
				bestMatch = device;
				bestPercentage = percentage;
			}
		}
		return bestMatch;
	}

	private static float getPercentageOnScreen(Point location, Dimension size, Rectangle screen)
	{
		Rectangle frameBounds = new Rectangle(location, size);
		Rectangle2D intersection = frameBounds.createIntersection(screen);
		int frameArea = size.width * size.height;
		int intersectionArea = (int) (intersection.getWidth() * intersection.getHeight());
		float percentage = (float) intersectionArea / frameArea;
		return percentage < 0 ? 0 : percentage;
	}

	public static Point getCurrentScreenLocation(Window window)
	{
		GraphicsDevice currentScreen = getCurrentScreen(window);
		if (currentScreen == GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()) {
			return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getLocation();
		}
		return currentScreen.getDefaultConfiguration().getBounds().getLocation();
	}

	public static void centerOnScreenAtLocation(Window window, Point desiredLocation)
	{
		GraphicsDevice currentScreen = getCurrentScreen(desiredLocation, window.getSize());
		Rectangle2D screenBounds = currentScreen.getDefaultConfiguration().getBounds();
		window.setLocation((int) screenBounds.getCenterX() - (window.getWidth() / 2),
				(int) screenBounds.getCenterY() - (window.getHeight() / 2));
	}

	/**
	 * A version of setLocationRelativeTo() that takes the maximum window bounds into account
	 * (taskbar).
	 */
	public static void setLocationRelativeTo(Window window, Component component)
	{
		window.setLocationRelativeTo(component);
		Point screenLocation = getCurrentScreenLocation(window);
		if (window.getX() < screenLocation.x)
			window.setLocation(screenLocation.x, window.getY());
		if (window.getY() < screenLocation.y)
			window.setLocation(window.getX(), screenLocation.y);
	}

	public static Color toColor(float[] color)
	{
		if (color.length == 4)
			return new Color(color[0], color[1], color[2], color[3]);
		else
			return new Color(color[0], color[1], color[2], 1);
	}

	public static BufferedImage imageIconToBufferedImage(ImageIcon icon)
	{
		BufferedImage bufferedImage =
				new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = bufferedImage.createGraphics();
		icon.paintIcon(null, graphics, 0, 0);
		graphics.dispose();
		return bufferedImage;
	}

	public static void setPreferredWidth(Component component, int width)
	{
		component.setPreferredSize(new Dimension(width, component.getPreferredSize().height));
	}
}
