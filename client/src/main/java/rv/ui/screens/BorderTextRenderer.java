package rv.ui.screens;

import com.jogamp.opengl.util.awt.TextRenderer;
import java.awt.Color;
import java.awt.Font;

/** A text renderer with some border effects */
public class BorderTextRenderer extends TextRenderer
{
	public BorderTextRenderer(Font font, boolean b, boolean b1)
	{
		super(font, b, b1);
	}

	public void drawWithOutline(String s, int x, int y, Color color, Color outlineColor)
	{
		int delta = 1;
		setColor(outlineColor);
		draw(s, x - delta, y - delta);
		draw(s, x - delta, y + delta);
		draw(s, x + delta, y - delta);
		draw(s, x + delta, y + delta);
		draw(s, x + delta, y);
		draw(s, x - delta, y);
		draw(s, x, y + delta);
		draw(s, x, y - delta);

		setColor(color);
		draw(s, x, y);
	}

	public void drawWithShadow(String s, int x, int y, Color color, Color shadowColor)
	{
		int delta = 1;
		setColor(shadowColor);
		draw(s, x - 1, y - 1);

		setColor(color);
		draw(s, x, y);
	}
}
