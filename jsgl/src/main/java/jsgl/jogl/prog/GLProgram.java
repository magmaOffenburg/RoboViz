package jsgl.jogl.prog;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.FPSAnimator;
import jsgl.jogl.view.Viewport;

public abstract class GLProgram implements GLEventListener
{
	private static final int TARGET_FPS = 60;

	protected GLAutoDrawable drawable;
	protected FPSAnimator animator;
	protected Viewport screen;
	protected double elapsedMS = 0;
	protected long lastNanoTime = 0;
	protected double fpsTimer = 0;
	protected double fpsCheckTimeMS = 1000;
	protected double fps = 0;

	public GLAutoDrawable getCanvas()
	{
		return drawable;
	}

	public double getElapsedTimeMS()
	{
		return elapsedMS;
	}

	public double getFPS()
	{
		return fps;
	}

	public Viewport getScreen()
	{
		return screen;
	}

	public GLProgram(int w, int h)
	{
		screen = new Viewport(0, 0, w, h);
	}

	protected void attachDrawableAndStart(GLAutoDrawable drawable)
	{
		this.drawable = drawable;
		drawable.addGLEventListener(this);
		animator = new FPSAnimator(drawable, TARGET_FPS);
		animator.start();
	}

	/**
	 * Called repeatedly by the GLAutoDrawable. This method calls the update and
	 * render methods in order. This method also keeps tracks of the time
	 * elapsed since it was last called.
	 */
	@Override
	public final void display(GLAutoDrawable drawable)
	{
		GL gl = drawable.getGL();

		long nanoTime = System.nanoTime();
		if (lastNanoTime > 0)
			elapsedMS = (nanoTime - lastNanoTime) / 10e5;
		lastNanoTime = nanoTime;

		fpsTimer += elapsedMS;
		if (fpsTimer >= fpsCheckTimeMS) {
			fps = 1000.0 / elapsedMS;
			fpsTimer -= fpsCheckTimeMS;
		}

		update(gl);
		render(gl);
	}

	/**
	 * First method called when OpenGL context is current. This method is called
	 * only once by the GLAutoDrawable, and it should be used to setup necessary
	 * data structures, lighting, etc.
	 */
	@Override
	public abstract void init(GLAutoDrawable drawable);

	/**
	 * First method called in the animation loop. This method is where the world
	 * state and input events should be handled.
	 */
	public abstract void update(GL gl);

	/**
	 * Second method called in the animation loop. This method is where all
	 * rendering code should be placed: setting materials and lights, drawing
	 * objects, etc.
	 */
	public abstract void render(GL gl);

	/**
	 * This is a good place to put any resource cleanup code that should be
	 * called before the application quits.
	 */
	@Override
	public abstract void dispose(GLAutoDrawable drawable);

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		screen = new Viewport(0, 0, width, height);
	}

	public abstract void addKeyListener(KeyListener l);

	public abstract void addMouseListener(MouseListener l);
}
