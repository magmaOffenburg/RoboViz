package jsgl.jogl.prog;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;

public abstract class GLProgramNEWT extends GLProgram
{
	private final GLWindow window;

	public GLWindow getWindow()
	{
		return window;
	}

	public GLProgramNEWT(String name, int w, int h, GLCapabilities caps)
	{
		super(w, h);
		GLProfile.initSingleton();
		window = GLWindow.create(caps);
		window.setSize(800, 600);
		window.setVisible(true);

		window.addWindowListener(new WindowAdapter() {
			public void windowDestroyNotify(WindowEvent arg0)
			{
				System.exit(0);
			};
		});

		attachDrawableAndStart(window);
	}

	@Override
	public void addKeyListener(KeyListener l)
	{
		window.addKeyListener(l);
	}

	@Override
	public void addMouseListener(MouseListener l)
	{
		window.addMouseListener(l);
	}
}
