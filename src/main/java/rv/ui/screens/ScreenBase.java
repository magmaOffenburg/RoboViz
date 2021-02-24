package rv.ui.screens;

import com.jogamp.opengl.awt.GLCanvas;
import rv.util.WindowResizeEvent;

public abstract class ScreenBase implements Screen
{
	protected boolean visible = true;

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	@Override
	public void setEnabled(GLCanvas canvas, boolean enabled)
	{
	}

	@Override
	public void windowResized(WindowResizeEvent event)
	{
	}

	@Override
	public void stop()
	{
	}
}
