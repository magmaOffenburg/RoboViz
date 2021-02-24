package rv.util;

import com.jogamp.opengl.GLAutoDrawable;
import java.util.EventObject;
import jsgl.jogl.view.Viewport;
import org.magmaoffenburg.roboviz.gui.MainWindow;

public class WindowResizeEvent extends EventObject
{
	private final Viewport window;
	private final GLAutoDrawable drawable;

	public Viewport getWindow()
	{
		return window;
	}

	public GLAutoDrawable getDrawable()
	{
		return drawable;
	}

	public WindowResizeEvent(Object src, Viewport window)
	{
		super(src);
		this.window = window;
		this.drawable = MainWindow.glCanvas;
	}
}
