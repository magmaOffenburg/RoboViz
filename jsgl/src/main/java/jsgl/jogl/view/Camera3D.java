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

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import jsgl.math.Ray;
import jsgl.math.vector.Matrix;
import jsgl.math.vector.Vec3f;
import jsgl.math.vector.Vec4f;

/**
 * A camera used for three-dimensional perspective viewing. Sets up the OpenGL
 * modelView and projection matrices before rendering. Also Contains methods for
 * generating left and right eye views for stereoscopic rendering.
 *
 * @author Justin Stoecker
 */
public abstract class Camera3D
{
	// basic parameters
	protected float fovY;
	protected float far;
	protected float near;
	protected Vec3f position;
	protected Vec3f up;
	protected Vec3f forward;
	protected Vec3f right;
	protected Matrix viewMatrix;
	protected Matrix viewInverseMatrix;

	// stereo parameters
	protected float focalLength = 2.0f;
	protected float fovX = 45.0f;
	protected float eyeSep = 0.1f;

	/**
	 * Returns the vertical field of view of the perspective viewing frustum
	 */
	public float getFOVY()
	{
		return fovY;
	}

	/**
	 * Returns the distance from the camera's position to the near clip plane of
	 * the viewing frustum
	 */
	public float getNear()
	{
		return near;
	}

	/**
	 * Returns the distance from the camera's position to the far clip plane of
	 * the viewing frustum
	 */
	public float getFar()
	{
		return far;
	}

	/**
	 * Returns the current 3D position of the camera in world space
	 */
	public Vec3f getPosition()
	{
		return position;
	}

	/**
	 * Returns the vector indicating the camera's forward direction
	 */
	public Vec3f getForward()
	{
		return forward;
	}

	/**
	 * Returns the vector indicating the camera's right direction
	 */
	public Vec3f getRight()
	{
		return right;
	}

	/**
	 * Returns the vector indicating the camera's up direction
	 */
	public Vec3f getUp()
	{
		return up;
	}

	/**
	 * Returns the camera's transformations wrapped in a view matrix
	 */
	public Matrix getView()
	{
		return viewMatrix;
	}

	/**
	 * Returns the camera's transformation matrix inverse
	 */
	public Matrix getViewInverse()
	{
		return viewInverseMatrix;
	}

	/**
	 * Returns the focal length used by stereo rendering. This is the distance
	 * between the camera's position and the plane where objects are in focus
	 * (zero parallax).
	 */
	public float getFocalLength()
	{
		return focalLength;
	}

	/**
	 * Returns the camera's aperture, or horizontal field of view (degrees).
	 * This is only used for calculating the left / right view frustums used in
	 * stereo rendering.
	 */
	public float getFOVX()
	{
		return fovX;
	}

	/**
	 * Returns the distance between left and right "eyes" used to create
	 * separate images for stereo rendering.
	 */
	public float getEyeSeparation()
	{
		return eyeSep;
	}

	/**
	 * Sets the focal length used in stereo rendering.
	 */
	public void setFocalLength(float fo)
	{
		this.focalLength = fo;
	}

	/**
	 * Sets the distance between left and right "eyes" used for stereo
	 * rendering.
	 */
	public void setEyeSeparation(float es)
	{
		this.eyeSep = es;
		updateView();
	}

	protected Camera3D(Vec3f pos, float near, float far)
	{
		this.position = pos;
		this.far = far;
		this.near = near;
	}

	/**
	 * Updates the camera matrices and local axes to reflect changes in the
	 * camera's position or rotations.
	 */
	protected abstract void updateView();

	/**
	 * Adds the camera's event listeners to a GLCanvas so the user's input
	 * affects the camera's movements and orientation
	 */
	public abstract void addListeners(GLCanvas canvas);

	public abstract void removeListeners(GLCanvas canvas);

	/**
	 * Applies the camera matrices to the OpenGL modelView and projection
	 * matrices.
	 */
	public void apply(GL2 gl, GLU glu, Viewport vp)
	{
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(fovY, vp.getAspect(), near, far);

		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glLoadMatrixd(viewMatrix.wrap());
	}

	/** Applies right eye projection/modelview matrices */
	public void applyRight(GL2 gl, GLU glu, Viewport screen)
	{
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();
		screen.apply(gl);

		double wd2 = near * Math.tan(Math.toRadians(fovX) / 2);
		double top = wd2;
		double bottom = -wd2;
		double left = -screen.getAspect() * wd2 - 0.5 * eyeSep * near / focalLength;
		double right = screen.getAspect() * wd2 - 0.5 * eyeSep * near / focalLength;
		gl.glFrustum(left, right, bottom, top, near, far);

		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glLoadIdentity();
		Vec3f eyeR = position.plus(this.right.times(eyeSep / 2));
		Vec3f t = eyeR.plus(forward);
		glu.gluLookAt(eyeR.x, eyeR.y, eyeR.z, t.x, t.y, t.z, up.x, up.y, up.z);
	}

	/** Applies left eye projection/modelview matrices */
	public void applyLeft(GL2 gl, GLU glu, Viewport screen)
	{
		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();
		screen.apply(gl);

		double wd2 = near * Math.tan(Math.toRadians(fovX) / 2);
		double top = wd2;
		double bottom = -wd2;
		double left = -screen.getAspect() * wd2 + 0.5 * eyeSep * near / focalLength;
		double right = screen.getAspect() * wd2 + 0.5 * eyeSep * near / focalLength;
		gl.glFrustum(left, right, bottom, top, near, far);

		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		gl.glLoadIdentity();
		Vec3f eyeL = position.minus(this.right.times(eyeSep / 2));
		Vec3f t = eyeL.plus(forward);
		glu.gluLookAt(eyeL.x, eyeL.y, eyeL.z, t.x, t.y, t.z, up.x, up.y, up.z);
	}

	public Ray unproject(Viewport screen, int mouseX, int mouseY)
	{
		return unproject(viewInverseMatrix, screen, near, mouseX, mouseY, fovY);
	}

	public static Ray unproject(Matrix viewInv, Viewport vp, float nearZ, int mouseX, int mouseY, float fovY)
	{
		double fov = Math.toRadians(fovY);

		float windowY = (vp.h - mouseY) - vp.h / 2.0f;
		float normY = windowY / (vp.h / 2.0f);
		float windowX = mouseX - vp.w / 2.0f;
		float normX = windowX / (vp.w / 2.0f);

		float nearH = (float) Math.tan(fov / 2) * nearZ;

		float y = nearH * normY;
		float x = nearH * vp.aspect * normX;

		// eye space
		Vec4f rayPos = new Vec4f(0, 0, 0, 1);
		Vec4f rayVec = new Vec4f(x, y, -nearZ, 0);

		// object space
		Vec4f rayPosObj = viewInv.transform(rayPos);
		Vec4f rayVecObj = viewInv.transform(rayVec);

		// truncate to 3 components
		Vec3f pos = new Vec3f(rayPosObj.x, rayPosObj.y, rayPosObj.z);
		Vec3f dir = new Vec3f(rayVecObj.x, rayVecObj.y, rayVecObj.z);
		return new Ray(pos, dir);
	}

	/**
	 * Projects 3D coordinates to screen space
	 */
	public Vec3f project(Vec3f pos, Viewport vp)
	{
		Matrix projection = Matrix.createPerspective(fovY, vp.aspect, near, far);
		Matrix viewProjection = projection.times(viewMatrix);

		// clip space coordinates
		Vec4f coords = viewProjection.transform(new Vec4f(pos, 1));

		// normalized device coordinates
		coords.div(coords.w);

		// window coordinates
		return vp.transform(coords, near, far);
	}
}
