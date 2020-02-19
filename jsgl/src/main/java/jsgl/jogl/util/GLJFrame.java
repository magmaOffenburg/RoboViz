/*
 *  Copyright 2011 Justin Stoecker
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jsgl.jogl.util;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import jsgl.jogl.view.Viewport;

/**
 * An abstract class that creates a Swing frame with an OpenGL canvas that can
 * be be used for drawing. Maintains an animation loop and keeps track of
 * elapsed time / estimated frames per second.
 *
 * @author Justin Stoecker
 */
public abstract class GLJFrame implements GLEventListener
{
	// utility classes
	public final GLUT glut = new GLUT();
	public final GLU glu = new GLU();

	private static final int TARGET_FPS = 60;
	protected final GLCapabilities glCaps;
	private final FPSAnimator animator;
	protected JFrame frame;
	protected GLCanvas canvas;
	protected Viewport screen;
	private double elapsedMS = 0;
	private long lastNanoTime = 0;
	private double fpsTimer = 0;
	private double fpsCheckTimeMS = 1000;
	private double fps = 0;

	public GLCanvas getCanvas()
	{
		return canvas;
	}

	public JFrame getFrame()
	{
		return frame;
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

	public GLJFrame(String name, int w, int h)
	{
		this(name, w, h, new GLCapabilities(GLProfile.get(GLProfile.GL2)));
	}

	/**
	 * Creates a new GLFrame object, displays it, and starts its animation loop
	 *
	 * @param name
	 *            - title given to the frame
	 * @param w
	 *            - initial width of the frame
	 * @param h
	 *            - initial height of the frame
	 */
	public GLJFrame(String name, int w, int h, GLCapabilities caps)
	{
		this.glCaps = caps;

		screen = new Viewport(0, 0, w, h);

		canvas = new GLCanvas(glCaps);
		canvas.addGLEventListener(this);

		frame = new JFrame(name);
		frame.setLayout(new BorderLayout());
		frame.add(canvas, BorderLayout.CENTER);
		frame.setSize(w, h);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowevent)
			{
				frame.dispose();
				System.exit(0);
			}
		});

		frame.setVisible(true);
		animator = new FPSAnimator(canvas, TARGET_FPS);
		animator.start();
		canvas.requestFocus();
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

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
	{
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		screen = new Viewport(0, 0, width, height);
	}
}
