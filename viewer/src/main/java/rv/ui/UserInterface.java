/*
 *  Copyright 2011 RoboViz
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

package rv.ui;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilitiesImmutable;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import jsgl.jogl.view.FPCamera;
import jsgl.math.vector.Vec2f;
import jsgl.math.vector.Vec3f;
import rv.Viewer;
import rv.ui.screens.LiveGameScreen;
import rv.ui.screens.LogfileModeScreen;
import rv.ui.screens.Screen;
import rv.ui.screens.ViewerScreenBase;
import rv.ui.view.CameraController;
import rv.ui.view.SimsparkController;
import rv.ui.view.TargetTrackerCamera;

/**
 * User interface controls
 *
 * @author Justin Stoecker
 */
public class UserInterface implements KeyListener
{
	private final Viewer viewer;
	private FPCamera camera;
	private final DrawingListPanel poolPanel;
	private final ControlsHelpPanel helpPanel;
	private CameraController cameraControl;

	private final SceneObjectPicker picker;
	private Screen overlay;
	private KeyListener[] tempListeners;
	private Screen activeScreen;
	private TargetTrackerCamera trackerCamera;
	
	
	private ViewerScreenBase activeViewerScreen;

	public TargetTrackerCamera getTrackerCamera()
	{
		return trackerCamera;
	}

	public CameraController getCameraControl()
	{
		return cameraControl;
	}

	public SceneObjectPicker getObjectPicker()
	{
		return picker;
	}

	private void setActiveScreen(Screen activeScreen)
	{
		GLCanvas canvas = (GLCanvas) viewer.getCanvas();
		if (this.activeScreen != null)
			this.activeScreen.setEnabled(canvas, false);

		this.activeScreen = activeScreen;
		this.activeScreen.setEnabled(canvas, true);
		this.activeScreen.createViewMenu(viewer.getFrame().getMenu().getViewMenu());
		canvas.addKeyListener(this);
	}

	public DrawingListPanel getShapeSetPanel()
	{
		return poolPanel;
	}

	public ControlsHelpPanel getShortcutHelpPanel()
	{
		return helpPanel;
	}

	public FPCamera getCamera()
	{
		return camera;
	}

	public UserInterface(Viewer viewer, String drawingFilter)
	{
		this.viewer = viewer;

		GLAutoDrawable drawable = viewer.getCanvas();

		camera = initCamera(drawable.getChosenGLCapabilities());
		picker = new SceneObjectPicker(viewer.getWorldModel(), camera);
		poolPanel = new DrawingListPanel(viewer.getDrawings(), drawingFilter);
		helpPanel = new ControlsHelpPanel();
	}

	public void init()
	{
		if (viewer.getMode() == Viewer.Mode.LIVE)
			setActiveScreen(new LiveGameScreen(viewer));
		else
			setActiveScreen(new LogfileModeScreen(viewer));

		trackerCamera = new TargetTrackerCamera(camera, viewer.getWorldModel().getGameState());
	}
	
	public void stopActiveScreen() {
		if (activeScreen != null) activeScreen.stop();
	}

	private FPCamera initCamera(GLCapabilitiesImmutable glcaps)
	{
		Vec3f pos = new Vec3f(0, 7, -10); // 3D camera position
		Vec2f rot = new Vec2f(-40, 180);  // camera x,y rotations
		float fov = 45;					  // field of view (degrees)
		float near = 0.1f;				  // near clip plane distance
		float far = 200;				  // far clip plane distance

		camera = new FPCamera(pos, rot, fov, near, far);

		if (glcaps.getStereo()) {
			camera.setFocalLength(8);
			camera.setEyeSeparation(3 / 20.0f);
		}

		SimsparkController ctrl = new SimsparkController(this);
		cameraControl = ctrl;
		viewer.getWorldModel().getGameState().addListener(ctrl);

		return camera;
	}

	public void update(GL2 gl, double elapsedMS)
	{
		cameraControl.update(elapsedMS);
		camera.update(elapsedMS);

		trackerCamera.update(viewer.getScreen());
	}

	public void render(GL2 gl, GLU glu, GLUT glut)
	{
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_BLEND);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluOrtho2D(0, viewer.getScreen().w, 0, viewer.getScreen().h);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

		activeScreen.render(gl, glu, glut, viewer.getScreen());

		gl.glDisable(GL.GL_BLEND);
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		switch (e.getKeyCode()) {
		case KeyEvent.VK_F12:
			viewer.setTakeScreenshotOnNextRender();
			break;
		default:
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
	}
}
