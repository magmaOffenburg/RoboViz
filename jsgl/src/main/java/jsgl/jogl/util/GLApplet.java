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
import com.jogamp.opengl.util.Animator;
import javax.swing.JApplet;
import jsgl.jogl.view.Viewport;

/**
 * An abstract class that creates a Swing applet with an OpenGL canvas that can
 * be be used for drawing. Maintains an animation loop and keeps track of
 * elapsed time / estimated frames per second.
 *
 * @author Justin Stoecker
 */
public abstract class GLApplet extends JApplet implements GLEventListener
{
	private static final long serialVersionUID = 1L;

	private GLCapabilities glCaps;
	private Animator animator;

	protected GLU glu;
	protected GLCanvas canvas;
	protected Viewport screen;

	private boolean closing = false;
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

	public void startClosing()
	{
		closing = true;
	}

	public GLApplet(String name, int w, int h)
	{
		screen = new Viewport(0, 0, w, h);
		setName(name);

		glu = new GLU();
		glCaps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
		setGLCapabilities(glCaps);

		canvas = new GLCanvas(glCaps);
		canvas.addGLEventListener(this);
		add("Center", canvas);
		animator = new Animator(canvas);
		canvas.requestFocus();
	}

	@Override
	public void start()
	{
		animator.start();
		setSize(screen.getW(), screen.getH());
	}

	@Override
	public void stop()
	{
		closing = true;
	}

	/**
	 * Called repeatedly by the GLAutoDrawable. This method calls the update and
	 * render methods in order. This method also keeps tracks of the time elapsed
	 * since it was last called.
	 */
	@Override
	public final void display(GLAutoDrawable drawable)
	{
		GL gl = drawable.getGL();
		if (closing) {
			disposeResources(gl);
			animator.stop();
			System.exit(0);
		}

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
	 * Called before the OpenGL canvas is created, this allows certain special
	 * configurations to be set for the application: multisampling, stereo, etc.
	 */
	public abstract void setGLCapabilities(GLCapabilities caps);

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
	 * This method is called just before the JOGLApp closes. This is a good place
	 * to put any resource cleanup code that should be called before the
	 * application quits.
	 */
	public abstract void disposeResources(GL gl);

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
	{
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		screen = new Viewport(0, 0, w, h);
	}
}
