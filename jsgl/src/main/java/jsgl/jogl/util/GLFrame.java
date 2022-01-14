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

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import java.awt.*;
import java.awt.event.*;
import jsgl.jogl.view.Viewport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An abstract class that creates an AWT frame with an OpenGL canvas that can be
 * be used for drawing. Maintains an animation loop, and keeps track of elapsed
 * time and estimated frames per second.
 *
 * @author Justin Stoecker
 */
public abstract class GLFrame implements GLEventListener
{
	private static final Logger LOGGER = LogManager.getLogger();

	// utility classes
	public final GLUT glut = new GLUT();
	public final GLU glu = new GLU();

	// graphics device settings
	protected GraphicsDevice gDevice;
	protected GraphicsEnvironment gEnvironment;
	protected DisplayMode displayMode;
	protected GLCapabilities glCaps;
	protected GLProfile glProfile;

	private FPSAnimator animator;
	protected Frame frame;
	protected GLCanvas canvas;
	protected Viewport screen;

	private boolean fullScreen = false;
	private double elapsedMS = 0;
	private long lastNanoTime = 0;
	private double fpsTimer = 0;
	private double fpsCheckTimeMS = 1000;
	private double fps = 0;

	public double getElapsedTimeMS()
	{
		return elapsedMS;
	}

	public double getFPS()
	{
		return fps;
	}

	public boolean isFullScreen()
	{
		return fullScreen;
	}

	public Frame getFrame()
	{
		return frame;
	}

	public GLCanvas getCanvas()
	{
		return canvas;
	}

	public Viewport getScreen()
	{
		return screen;
	}

	public GLUT getGLUT()
	{
		return glut;
	}

	public GLU getGLU()
	{
		return glu;
	}

	public GLFrame(String name, int w, int h, GLCapabilities caps, int fps)
	{
		this.glCaps = caps;
		screen = new Viewport(0, 0, w, h);
		canvas = new GLCanvas(caps);

		gEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gDevice = gEnvironment.getDefaultScreenDevice();
		displayMode = gDevice.getDisplayMode();

		canvas.addGLEventListener(this);

		frame = new Frame(name);
		frame.add(canvas);
		frame.setSize(w, h);
		frame.setLocationRelativeTo(null);
		animator = new FPSAnimator(canvas, fps);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowevent)
			{
				frame.dispose();
				System.exit(0);
			}
		});

		frame.setVisible(true);

		animator.start();
		canvas.requestFocusInWindow();
	}

	public GLFrame(String name, int w, int h, GLCapabilities caps)
	{
		this(name, w, h, caps, 60);
	}

	public GLFrame(String name, int w, int h)
	{
		this(name, w, h, new GLCapabilities(GLProfile.get(GLProfile.GL2)));
	}

	public void enterFullScreen()
	{
		frame.setUndecorated(true);
		try {
			frame.setIgnoreRepaint(true);
			gDevice.setFullScreenWindow(frame);
			fullScreen = true;
		} catch (Exception e) {
			LOGGER.error("Error while attempting full screen", e);
			gDevice.setFullScreenWindow(null);
		}
	}

	public void exitFullScreen()
	{
		frame.setUndecorated(false);
		fullScreen = false;
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
