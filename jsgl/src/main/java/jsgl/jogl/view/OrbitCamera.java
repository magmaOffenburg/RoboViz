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

package jsgl.jogl.view;

import com.jogamp.opengl.awt.GLCanvas;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import jsgl.math.vector.Matrix;
import jsgl.math.vector.Vec2f;
import jsgl.math.vector.Vec3f;

/**
 * 3D Camera that rotates around a fixed point in space
 *
 * @author Justin Stoecker
 */
public class OrbitCamera extends Camera3D implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener
{
	protected Vec3f target;
	protected float yaw;
	protected float pitch;
	protected float distance;
	protected Matrix rotation;

	protected boolean moveF; // camera is moving
	// forward
	protected boolean moveB;		   // camera is moving back
	protected boolean moveL;		   // camera is moving left
	protected boolean moveR;		   // camera is moving right
	protected boolean rotate;		   // camera is rotating
	protected int translateSpeed = 10; // units moved per second
	protected float rotateSpeed = 0.25f;
	protected float scrollSpeed = 0.5f;
	protected Vec2f lastMouse = new Vec2f(0);

	private int moveForwardKey = KeyEvent.VK_W;
	private int moveBackwardKey = KeyEvent.VK_S;
	private int moveLeftKey = KeyEvent.VK_A;
	private int moveRightKey = KeyEvent.VK_D;

	public Vec3f getTarget()
	{
		return target;
	}

	public void setTarget(Vec3f target)
	{
		this.target = target;
		updateView();
	}

	public void setTranslateSpeed(int translateSpeed)
	{
		this.translateSpeed = translateSpeed;
	}

	public void setScrollSpeed(float scrollSpeed)
	{
		this.scrollSpeed = scrollSpeed;
	}

	public OrbitCamera(Vec3f target, float distance, float yaw, float pitch, float fovY, float near, float far)
	{
		super(Vec3f.unitZ().times(distance).plus(target), near, far);
		this.target = target;
		this.yaw = yaw;
		this.pitch = pitch;
		this.distance = distance;
		this.fovY = fovY;
		updateView();
	}

	@Override
	protected void updateView()
	{
		Matrix rotY = Matrix.createRotationY(Math.toRadians(-yaw));
		Matrix rotX = Matrix.createRotationX(Math.toRadians(-pitch));
		Matrix rotXY = rotX.times(rotY);
		Matrix translate = Matrix.createTranslation(new Vec3f(0, 0, -distance));
		viewMatrix = translate.times(rotXY).times(Matrix.createTranslation(target.times(-1)));

		Matrix rotYi = Matrix.createRotationY(Math.toRadians(yaw));
		Matrix rotXi = Matrix.createRotationX(Math.toRadians(pitch));
		rotation = rotYi.times(rotXi);
		forward = rotation.transform(Vec3f.unitZ()).times(-1);
		right = rotation.transform(Vec3f.unitX());
		up = right.cross(forward);
		position = target.plus(forward.times(-distance));
	}

	/**
	 * Adds rotation around both X and Y axes. Useful for rotating the camera
	 * with a change in mouse position.
	 */
	public void rotate(Vec2f v)
	{
		yaw -= v.x;
		pitch -= v.y;
		updateView();
	}

	/**
	 * Updates the camera's position and rotation based on UI input. Should be
	 * put inside the update loop of a GL application. ElapsedMS is used to
	 * scale translation by framerate to keep movement consistent with varying
	 * framerate or performance.
	 */
	public void update(double elapsedMS)
	{
		Vec3f move = new Vec3f(0);
		if (moveF)
			move = move.plus(Vec3f.unitZ().times(-1));
		if (moveR)
			move = move.plus(Vec3f.unitX());
		if (moveL)
			move = move.plus(Vec3f.unitX().times(-1));
		if (moveB)
			move = move.plus(Vec3f.unitZ());
		move = move.times((float) (elapsedMS / 1000.0f * translateSpeed));

		if (move.lengthSquared() > 0)
			moveTargetLocal(move);
	}

	/**
	 * Moves the camera's target using a vector in relative to the camera's
	 * orientation.For example, moving (0,0,-1) will move the camera forward;
	 * using (1,0,0) will move it right.
	 */
	public void moveTargetLocal(Vec3f dir)
	{
		Vec3f translation = rotation.transform(dir);
		target = target.plus(translation);
		updateView();
	}

	/**
	 * Moves the camera's target using a vector in object space. For example,
	 * moving a camera with position (1,0,5) by a vector (2,3,4) will result in
	 * a new position of (3,3,9).
	 */
	public void moveWorld(Vec3f dir)
	{
		target = target.plus(dir);
		updateView();
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON3)
			rotate = true;
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON3)
			rotate = false;
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (rotate) {
			Vec2f mouseMove = new Vec2f(e.getX(), e.getY()).minus(lastMouse);
			rotate(mouseMove.times(rotateSpeed));
		}
		lastMouse = new Vec2f(e.getX(), e.getY());
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		lastMouse = new Vec2f(e.getX(), e.getY());
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		int key = e.getKeyCode();
		if (key == moveForwardKey)
			moveF = true;
		else if (key == moveBackwardKey)
			moveB = true;
		else if (key == moveLeftKey)
			moveL = true;
		else if (key == moveRightKey)
			moveR = true;
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		int key = e.getKeyCode();
		if (key == moveForwardKey)
			moveF = false;
		else if (key == moveBackwardKey)
			moveB = false;
		else if (key == moveLeftKey)
			moveL = false;
		else if (key == moveRightKey)
			moveR = false;
	}

	@Override
	public void keyTyped(KeyEvent arg0)
	{
	}

	@Override
	public void mouseClicked(MouseEvent arg0)
	{
	}

	@Override
	public void mouseEntered(MouseEvent arg0)
	{
	}

	@Override
	public void mouseExited(MouseEvent arg0)
	{
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		int notches = e.getWheelRotation();
		if (notches < 0) {
			distance -= scrollSpeed;
		} else {
			distance += scrollSpeed;
		}
		updateView();
	}

	@Override
	public void addListeners(GLCanvas canvas)
	{
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addKeyListener(this);
		canvas.addMouseWheelListener(this);
	}

	@Override
	public void removeListeners(GLCanvas canvas)
	{
		canvas.removeMouseListener(this);
		canvas.removeMouseMotionListener(this);
		canvas.removeKeyListener(this);
		canvas.removeMouseWheelListener(this);
	}
}
