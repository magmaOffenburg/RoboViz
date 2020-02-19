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
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.gl2.GLUT;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import jsgl.jogl.view.Viewport;

public abstract class AWTWindowProgram implements WindowProgram
{
	public final GLUT glut = new GLUT();
	public final GLU glu = new GLU();

	private Animator animator;
	protected Frame frame;
	protected GLCanvas canvas;
	protected Viewport screen;

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

	public Frame getFrame()
	{
		return frame;
	}

	public GLCanvas getCanvas()
	{
		return canvas;
	}

	public GLAutoDrawable getGLDrawable()
	{
		return canvas;
	}

	public GLU getGLU()
	{
		return glu;
	}

	public GLUT getGLUT()
	{
		return glut;
	}

	public Viewport getWindowDimensions()
	{
		return screen;
	}

	public AWTWindowProgram(String title, int w, int h, GLCapabilities caps)
	{
		canvas = new GLCanvas(caps);
		screen = new Viewport(0, 0, w, h);

		canvas.addGLEventListener(this);

		frame = new Frame(title);
		frame.add(canvas);
		frame.setSize(w, h);
		frame.setLocationRelativeTo(null);
		animator = new Animator(canvas);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				new Thread(() -> {
					animator.stop();
					System.exit(0);
				}).start();
			}
		});

		frame.setVisible(true);
		animator.start();
		canvas.requestFocusInWindow();
	}

	/**
	 * Called repeatedly by the GLAutoDrawable. This method calls the update and
	 * render methods in order. This method also keeps tracks of the time
	 * elapsed since it was last called.
	 */
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

		update(gl, elapsedMS);
		render(gl);
	}

	/**
	 * First method called when OpenGL context is current. This method is called
	 * only once by the GLAutoDrawable, and it should be used to setup necessary
	 * data structures, lighting, etc.
	 */
	public abstract void init(GLAutoDrawable drawable);

	/**
	 * First method called in the animation loop. This method is where the world
	 * state and input events should be handled.
	 */
	public abstract void update(GL gl, double elapsedMS);

	/**
	 * Second method called in the animation loop. This method is where all
	 * rendering code should be placed: setting materials and lights, drawing
	 * objects, etc.
	 */
	public abstract void render(GL gl);

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		screen = new Viewport(0, 0, width, height);
	}
}
