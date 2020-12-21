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

package rv.ui.view;

import com.jogamp.opengl.awt.GLCanvas;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import jsgl.jogl.view.FPCamera;
import jsgl.math.vector.Vec2f;
import jsgl.math.vector.Vec3f;
import org.magmaoffenburg.roboviz.rendering.CameraController;
import rv.comm.rcssserver.GameState;
import rv.comm.rcssserver.GameState.GameStateChangeListener;
import rv.ui.CameraSetting;

/**
 * SimSpark style camera controller
 *
 * @author justin
 */
public class SimsparkController implements ICameraController, GameStateChangeListener, FocusListener
{
	protected boolean rotate;
	protected boolean moveF; // camera is moving
	protected boolean moveB; // camera is moving back
	protected boolean moveL; // camera is moving left
	protected boolean moveR; // camera is moving right
	protected boolean moveU;
	protected boolean moveD;
	protected Vec2f lastMouse = new Vec2f(0);

	float dL, dR, dF, dB, dU, dD = 0;
	float dMax = 1;
	float dChange = 0.08f;

	private CameraSetting[] cameras;

	public SimsparkController()
	{
		CameraController.fpCamera.setTranslateSpeed(6);
	}

	public void update(double elapsedMS)
	{
		// local move is vector in camera's local coordinate system
		Vec3f tLocal = new Vec3f(0);

		// world move is vector in world space
		Vec3f tWorld = new Vec3f(0);

		FPCamera cam = CameraController.fpCamera;

		dR = moveR ? dMax : Math.max(dR - dChange, 0);
		dL = moveL ? dMax : Math.max(dL - dChange, 0);
		dF = moveF ? dMax : Math.max(dF - dChange, 0);
		dB = moveB ? dMax : Math.max(dB - dChange, 0);
		dU = moveU ? dMax / 2 : Math.max(dU - dChange, 0);
		dD = moveD ? dMax / 2 : Math.max(dD - dChange, 0);

		if (dR > 0)
			tLocal.add(Vec3f.unitX().times(dR));
		if (dL > 0)
			tLocal.add(Vec3f.unitX().times(-dL));
		if (dF > 0) {
			Vec3f v = cam.getRotation().transform(Vec3f.unitZ().times(-1));
			v.y = 0;
			tWorld.add(v.normalize().times(dF));
		}
		if (dB > 0) {
			Vec3f v = cam.getRotation().transform(Vec3f.unitZ());
			v.y = 0;
			tWorld.add(v.normalize().times(dB));
		}
		if (dU > 0)
			tWorld.add(Vec3f.unitY().times(dU));
		if (dD > 0)
			tWorld.add(Vec3f.unitY().times(-dD));

		// Adjust camera speed dependent on its height
		float speed = Math.abs(cam.getPosition().y / 6.0f);
		if (speed < 0.25f)
			speed = 0.25f;
		else if (speed > 4.0f)
			speed = 4.0f;
		speed *= CameraController.fpCamera.getTranslatedSpeed();

		float scale = (float) (elapsedMS / 1000.0f) * speed;

		tLocal.mul(scale);
		tWorld.mul(scale);

		if (tLocal.lengthSquared() > 0)
			cam.moveLocal(tLocal);
		if (tWorld.lengthSquared() > 0)
			cam.moveWorld(tWorld);
	}

	private void setCamera(int i)
	{
		if (i >= cameras.length || i < 0)
			return;

		FPCamera camera = CameraController.fpCamera;
		camera.setPosition(cameras[i].getPosition().clone());
		camera.setRotation(cameras[i].getRotation().clone());
	}

	/** Initialize saved camera positions */
	private void initCameras(GameState gs)
	{
		float fl = gs.getFieldLength();
		float fw = gs.getFieldWidth();

		double fov = Math.toRadians(CameraController.fpCamera.getFOVY());
		float aerialHeight = (float) (0.5 * fw / Math.tan(fov * 0.5) * 1.1);

		cameras = new CameraSetting[] {new CameraSetting(new Vec3f(fl * 0.8f, fl * 0.4f, 0), new Vec2f(-35, 90)),
				new CameraSetting(new Vec3f(fl * 0.8f, fl * 0.4f, -fw), new Vec2f(-30, 180 - 50)),
				new CameraSetting(new Vec3f(0, fl * 0.4f, -fw), new Vec2f(-40, 180 + 35.8f)),
				new CameraSetting(new Vec3f(0, fl * 0.6f, -fw * 1.1f), new Vec2f(-45, 180)),
				new CameraSetting(new Vec3f(0, fl * 0.4f, -fw), new Vec2f(-40, 180 - 35.8f)),
				new CameraSetting(new Vec3f(-fl * 0.8f, fl * 0.4f, -fw), new Vec2f(-30, 180 + 50)),
				new CameraSetting(new Vec3f(-fl * 0.8f, fl * 0.4f, 0), new Vec2f(-35, 180 + 90)),
				new CameraSetting(new Vec3f(0, aerialHeight, 0), new Vec2f(-90, 180))};
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			rotate = true;
			break;
		case MouseEvent.BUTTON3:
			if (e.isShiftDown())
				moveD = true;
			else
				moveU = true;
			break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			rotate = false;
			break;
		case MouseEvent.BUTTON3:
			moveD = false;
			moveU = false;
			break;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (rotate) {
			Vec2f mouseMove = new Vec2f(e.getX(), e.getY()).minus(lastMouse);
			CameraController.fpCamera.rotate(mouseMove.times(CameraController.fpCamera.getRotateSpeed()));
		}
		lastMouse = new Vec2f(e.getX(), e.getY());
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		lastMouse = new Vec2f(e.getX(), e.getY());
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		int key = e.getKeyCode();

		// key is a number 1 through 9
		int keyChar = e.getKeyChar();
		if (keyChar > 48 && keyChar < 58 && cameras != null)
			setCamera(keyChar - 49);

		switch (key) {
		case KeyEvent.VK_W:
		case KeyEvent.VK_UP:
			moveF = true;
			break;
		case KeyEvent.VK_A:
		case KeyEvent.VK_LEFT:
			moveL = true;
			break;
		case KeyEvent.VK_S:
		case KeyEvent.VK_DOWN:
			moveB = true;
			break;
		case KeyEvent.VK_D:
		case KeyEvent.VK_RIGHT:
			moveR = true;
			break;
		case KeyEvent.VK_PAGE_DOWN:
			moveD = true;
			break;
		case KeyEvent.VK_PAGE_UP:
			moveU = true;
			break;
		case KeyEvent.VK_SHIFT:
			dChange = 1;
			dMax = 0.5f;
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		int key = e.getKeyCode();

		switch (key) {
		case KeyEvent.VK_W:
		case KeyEvent.VK_UP:
			moveF = false;
			break;
		case KeyEvent.VK_A:
		case KeyEvent.VK_LEFT:
			moveL = false;
			break;
		case KeyEvent.VK_S:
		case KeyEvent.VK_DOWN:
			moveB = false;
			break;
		case KeyEvent.VK_D:
		case KeyEvent.VK_RIGHT:
			moveR = false;
			break;
		case KeyEvent.VK_PAGE_DOWN:
			moveD = false;
			break;
		case KeyEvent.VK_PAGE_UP:
			moveU = false;
			break;
		case KeyEvent.VK_SHIFT:
			dChange = 0.08f;
			dMax = 1;
			break;
		}
	}

	@Override
	public void attachToCanvas(GLCanvas canvas)
	{
		canvas.addKeyListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseWheelListener(this);
		canvas.addFocusListener(this);
	}

	@Override
	public void detachFromCanvas(GLCanvas canvas)
	{
		canvas.removeKeyListener(this);
		canvas.removeMouseMotionListener(this);
		canvas.removeMouseListener(this);
		canvas.removeMouseWheelListener(this);
		canvas.removeFocusListener(this);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		float factor = e.isShiftDown() ? 0.2f : 1;
		if (e.getWheelRotation() < 0) {
			factor *= -1;
		}
		CameraController.fpCamera.moveLocal(Vec3f.unitZ().times(factor));
	}

	public void gsMeasuresAndRulesChanged(GameState gs)
	{
		initCameras(gs);
	}

	public void gsPlayStateChanged(GameState gs)
	{
	}

	public void gsTimeChanged(GameState gs)
	{
	}

	@Override
	public void focusGained(FocusEvent e)
	{
	}

	@Override
	public void focusLost(FocusEvent e)
	{
		rotate = false;
		moveF = false;
		moveB = false;
		moveL = false;
		moveR = false;
		moveU = false;
		moveD = false;
		dChange = 0.08f;
		dMax = 1;
	}
}
