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
import jsgl.math.Maths;
import jsgl.math.vector.Matrix;
import jsgl.math.vector.Vec2f;
import jsgl.math.vector.Vec3f;

/**
 * A first-person camera that uses perspective projection. Can handle key/mouse
 * events to update position and rotation.
 *
 * @author Justin Stoecker
 */
public class FPCamera extends Camera3D implements KeyListener, MouseListener, MouseMotionListener
{
	protected Vec2f rotAngle;
	protected Matrix rotation;

	protected boolean moveF; // camera is moving
	// forward
	protected boolean moveB;		   // camera is moving back
	protected boolean moveL;		   // camera is moving left
	protected boolean moveR;		   // camera is moving right
	protected boolean rotate;		   // camera is rotating
	protected int translateSpeed = 10; // units moved per second
	protected float rotateSpeed = 0.25f;
	protected Vec2f lastMouse = new Vec2f(0);

	private int moveForwardKey = KeyEvent.VK_W;
	private int moveBackwardKey = KeyEvent.VK_S;
	private int moveLeftKey = KeyEvent.VK_A;
	private int moveRightKey = KeyEvent.VK_D;

	public Matrix getRotation()
	{
		return rotation;
	}

	public float getRotateSpeed()
	{
		return rotateSpeed;
	}

	public int getTranslatedSpeed()
	{
		return translateSpeed;
	}

	public int getMoveForwardKey()
	{
		return moveForwardKey;
	}

	public int getMoveBackwardKey()
	{
		return moveBackwardKey;
	}

	public int getMoveLeftKey()
	{
		return moveLeftKey;
	}

	public int getMoveRightKey()
	{
		return moveRightKey;
	}

	public Vec2f getRotAngle()
	{
		return rotAngle;
	}

	public void setPosition(Vec3f pos)
	{
		this.position = pos;
		updateView();
	}

	public void setRotation(Vec2f rot)
	{
		this.rotAngle = rot;
		updateView();
	}

	public void setRotateSpeed(float speed)
	{
		rotateSpeed = speed;
	}

	public void setTranslateSpeed(int speed)
	{
		translateSpeed = speed;
	}

	public void setMoveForwardKey(int keyCode)
	{
		moveForwardKey = keyCode;
	}

	public void setMoveBackwardKey(int keyCode)
	{
		moveBackwardKey = keyCode;
	}

	public void setMoveLeftKey(int keyCode)
	{
		moveLeftKey = keyCode;
	}

	public void setMoveRightKey(int keyCode)
	{
		moveRightKey = keyCode;
	}

	/**
	 * Creates a new first-person camera.
	 *
	 * @param pos
	 *            - initial position of the camera in world space
	 * @param rot
	 *            - initial rotation of the camera around X/Y axes in degrees
	 * @param fov
	 *            - field of view angle in degrees
	 * @param near
	 *            - near viewing plane distance from camera position
	 * @param far
	 *            - far viewing plane distance from camera position
	 */
	public FPCamera(Vec3f pos, Vec2f rot, float fov, float near, float far)
	{
		super(pos, near, far);
		this.fovY = fov;
		this.rotAngle = rot;
		updateView();
	}

	/**
	 * Moves the camera using a vector in relative to the camera's
	 * orientation.For example, moving (0,0,-1) will move the camera forward;
	 * using (1,0,0) will move it right.
	 */
	public void moveLocal(Vec3f dir)
	{
		Vec3f translation = rotation.transform(dir);
		position = position.plus(translation);
		updateView();
	}

	/**
	 * Moves the camera using a vector in object space. For example, moving a
	 * camera with position (1,0,5) by a vector (2,3,4) will result in a new
	 * position of (3,3,9).
	 */
	public void moveWorld(Vec3f dir)
	{
		position = position.plus(dir);
		updateView();
	}

	/**
	 * Moves the camera to a specified location in the world.
	 */
	public void moveTo(Vec3f pos)
	{
		position = pos;
		updateView();
	}

	/**
	 * Adds rotation around both X and Y axes. Useful for rotating the camera
	 * with a change in mouse position.
	 */
	public void rotate(Vec2f v)
	{
		rotAngle.x -= v.y;
		rotAngle.y -= v.x;
		updateView();
	}

	/**
	 * Centers an object at position 'target' in front of the camera
	 */
	public void focus(Vec3f target)
	{
		rotAngle = Maths.calcPitchYaw(position, target);
		updateView();
	}

	/**
	 * Internal method that updates the modelView matrix after a translation or
	 * rotation.
	 */
	@Override
	protected void updateView()
	{
		// camera view uses inverse operations for matrices
		Matrix tInv = Matrix.createTranslation(position.times(-1));
		Matrix rotXInv = Matrix.createRotationX(Math.toRadians(-rotAngle.x));
		Matrix rotYInv = Matrix.createRotationY(Math.toRadians(-rotAngle.y));
		viewMatrix = rotXInv.times(rotYInv).times(tInv);

		// transform the local axes of the camera
		Matrix trans = Matrix.createTranslation(position);
		Matrix rotX = Matrix.createRotationX(Math.toRadians(rotAngle.x));
		Matrix rotY = Matrix.createRotationY(Math.toRadians(rotAngle.y));
		rotation = rotY.times(rotX);
		viewInverseMatrix = trans.times(rotation);
		forward = rotation.transform(Vec3f.unitZ().times(-1));
		right = rotation.transform(Vec3f.unitX());
		up = rotation.transform(Vec3f.unitY());
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
			moveLocal(move);
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
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
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
	public void addListeners(GLCanvas canvas)
	{
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addKeyListener(this);
	}

	public void removeListeners(GLCanvas canvas)
	{
		canvas.removeMouseListener(this);
		canvas.removeMouseMotionListener(this);
		canvas.removeKeyListener(this);
	}

	@Override
	public String toString()
	{
		return String.format("Pos: %s, Rotation: %s", position, rotAngle);
	}
}
