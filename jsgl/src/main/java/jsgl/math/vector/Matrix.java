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

package jsgl.math.vector;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

/**
 * A 4x4 matrix stored in column-major order
 *
 * @author Justin Stoecker
 */
public class Matrix
{
	private double[] m = new double[16];

	/**
	 * Creates a 4x4 matrix with all elements set to the same value
	 *
	 * @param v
	 *            - the value for each element of the matrix
	 */
	public Matrix(double v)
	{
		for (int i = 0; i < 16; i++)
			m[i] = v;
	}

	/**
	 * Creates a 4x4 matrix
	 *
	 * @param a
	 *            - the 16 elements of the matrix in column-major form
	 */
	public Matrix(double[] a)
	{
		this.m = a;
	}

	/**
	 * Transforms a Vec3d using the current matrix
	 */
	public Vec3d transform(Vec3d v)
	{
		Vec4d r = transform(new Vec4d(v, 1));
		return new Vec3d(r.x, r.y, r.z);
	}

	/**
	 * Transforms a Vec3f using the current matrix. Treats the argument as a
	 * point with w = 1. If transforming a vector, use transform(Vec4f) and set
	 * w = 0.
	 */
	public Vec3f transform(Vec3f v)
	{
		Vec4f r = transform(new Vec4f(v, 1));
		return new Vec3f(r.x, r.y, r.z);
	}

	/**
	 * Transforms a Vec4d using the current matrix
	 */
	public Vec4d transform(Vec4d v)
	{
		double x = v.x * m[0] + v.y * m[4] + v.z * m[8] + v.w * m[12];
		double y = v.x * m[1] + v.y * m[5] + v.z * m[9] + v.w * m[13];
		double z = v.x * m[2] + v.y * m[6] + v.z * m[10] + v.w * m[14];
		double w = v.x * m[3] + v.y * m[7] + v.z * m[11] + v.w * m[15];
		return new Vec4d(x, y, z, w);
	}

	/**
	 * Transforms a Vec4f using the current matrix
	 */
	public Vec4f transform(Vec4f v)
	{
		float x = (float) (v.x * m[0] + v.y * m[4] + v.z * m[8] + v.w * m[12]);
		float y = (float) (v.x * m[1] + v.y * m[5] + v.z * m[9] + v.w * m[13]);
		float z = (float) (v.x * m[2] + v.y * m[6] + v.z * m[10] + v.w * m[14]);
		float w = (float) (v.x * m[3] + v.y * m[7] + v.z * m[11] + v.w * m[15]);
		return new Vec4f(x, y, z, w);
	}

	/**
	 * Multiplies the current matrix by another matrix
	 *
	 * @param that
	 *            - the second matrix in the multiplication
	 * @return the result of multiplying the two matrices
	 */
	public Matrix times(Matrix that)
	{
		Matrix mat = new Matrix(0);

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				double sum = 0;
				for (int k = 0; k < 4; k++) {
					sum += m[i + 4 * k] * that.m[k + 4 * j];
				}
				mat.m[i + 4 * j] = sum;
			}
		}

		return mat;
	}

	/**
	 * Creates an identity matrix
	 */
	public static Matrix createIdentity()
	{
		double[] a = new double[16];
		for (int i = 0; i < 4; i++)
			a[i + 4 * i] = 1;
		return new Matrix(a);
	}

	/**
	 * Creates a translation matrix
	 *
	 * @param t
	 *            - the translation vector (Tx, Ty, Tz)
	 * @return a translation matrix that translates by the vector t
	 */
	public static Matrix createTranslation(Vec3f t)
	{
		return new Matrix(new double[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, t.x, t.y, t.z, 1});
	}

	public static Matrix createTranslation(double x, double y, double z)
	{
		return new Matrix(new double[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, x, y, z, 1});
	}

	/**
	 * Creates a scale matrix
	 *
	 * @param s
	 *            - the scaling for x, y, and z components
	 * @return the scale matrix that scales by s
	 */
	public static Matrix createScale(Vec3f s)
	{
		return new Matrix(new double[] {s.x, 0, 0, 0, 0, s.y, 0, 0, 0, 0, s.z, 0, 0, 0, 0, 1});
	}

	/**
	 * Creates a rotation matrix using the x-axis
	 *
	 * @param radians
	 *            - the rotation angle in radians
	 * @return the rotation matrix
	 */
	public static Matrix createRotationX(double radians)
	{
		double c = Math.cos(radians);
		double s = Math.sin(radians);
		return new Matrix(new double[] {1, 0, 0, 0, 0, c, s, 0, 0, -s, c, 0, 0, 0, 0, 1});
	}

	/**
	 * Creates a rotation matrix using the y-axis
	 *
	 * @param radians
	 *            - the rotation angle in radians
	 * @return the rotation matrix
	 */
	public static Matrix createRotationY(double radians)
	{
		double c = Math.cos(radians);
		double s = Math.sin(radians);
		return new Matrix(new double[] {c, 0, -s, 0, 0, 1, 0, 0, s, 0, c, 0, 0, 0, 0, 1});
	}

	/**
	 * Creates a rotation matrix using the z-axis
	 *
	 * @param radians
	 *            - the rotation angle in radians
	 * @return the rotation matrix
	 */
	public static Matrix createRotationZ(double radians)
	{
		double c = Math.cos(radians);
		double s = Math.sin(radians);
		return new Matrix(new double[] {c, s, 0, 0, -s, c, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1});
	}

	/**
	 * Creates a rotation matrix that allows rotation about an arbitrary axis
	 *
	 * @param radians
	 *            - the rotation angle in radians
	 * @param u
	 *            - the axis to rotate about
	 * @return the rotation matrix
	 * @see http://inside.mines.edu/~gmurray/ArbitraryAxisRotation/
	 */
	public static Matrix createRotation(double radians, Vec3d u)
	{
		double c = Math.cos(radians);
		double s = Math.sin(radians);
		double u2 = u.x * u.x;
		double v2 = u.y * u.y;
		double w2 = u.z * u.z;
		double d = u2 + v2 + w2;
		double ic = 1 - c;
		double sqrtDs = Math.sqrt(d) * s;

		double[] a = {(u2 + (v2 + w2) * c) / d, (u.x * u.y * ic + u.z * sqrtDs) / d,
				(u.x * u.z * ic - u.y * sqrtDs) / d, 0, (u.x * u.y * ic - u.z * sqrtDs) / d, (v2 + (u2 + w2) * c) / d,
				(u.y * u.z * ic + u.x * sqrtDs) / d, 0, (u.x * u.z * ic + u.y * sqrtDs) / d,
				(u.y * u.z * ic - u.x * sqrtDs) / d, (w2 + (u2 + v2) * c) / d, 0,

				0, 0, 0, 1};

		return new Matrix(a);
	}

	/**
	 * Creates a rotation matrix using yaw (y-axis rotation), pitch (x-axis
	 * rotation), and roll (z-axis rotation)
	 */
	public static Matrix createYawPitchRoll(double yaw, double pitch, double roll)
	{
		double a = Math.cos(yaw);
		double b = Math.sin(yaw);
		double c = Math.cos(pitch);
		double d = Math.sin(pitch);
		double f = Math.cos(roll);
		double g = Math.sin(roll);

		double[] vals = {f * a + d * g * b, c * g, d * g * a - f * b, 0, -g * a + d * f * b, c * f, d * f * a + g * b,
				0, c * b, -d, c * a, 0, 0, 0, 0, 1};
		return new Matrix(vals);
	}

	/**
	 * Creates a perspective projection matrix that may be off-axis and
	 * asymmetric. Same as glFrustum(left,right,bottom,top,near,far). Parameters
	 * are values with respect to (0,0,near) in camera space.
	 *
	 * @param left
	 *            - distance from eye to left clipping plane at near depth
	 * @param right
	 *            - distance from eye to right clipping plane at near depth
	 * @param top
	 *            - distance from eye to top clipping plane at near depth
	 * @param bottom
	 *            - distance from eye to bottom clipping plane at near depth
	 * @param near
	 *            - distance from eye to near clipping plane
	 * @param far
	 *            - distance from eye to far clipping plane
	 */
	public static Matrix createPerspective(
			double left, double right, double bottom, double top, double near, double far)
	{
		double rightMinusLeft = right - left;
		double topMinusBottom = top - bottom;
		double farMinusNear = far - near;
		double twoNear = 2 * near;

		double A = (right + left) / rightMinusLeft;
		double B = (top + bottom) / topMinusBottom;
		double C = -(far + near) / farMinusNear;
		double D = -twoNear * far / farMinusNear;

		double[] m = {
				twoNear / rightMinusLeft,
				0,
				0,
				0,
				0,
				twoNear / topMinusBottom,
				0,
				0,
				A,
				B,
				C,
				-1,
				0,
				0,
				D,
				0,
		};

		return new Matrix(m);
	}

	/**
	 * Creates a perspective projection matrix.
	 *
	 * @param fovY
	 *            - vertical field of view
	 * @param aspect
	 *            - aspect ratio of frustum
	 * @param near
	 *            - distance from eye to near clipping plane
	 * @param far
	 *            - distance from eye to far clipping plane
	 */
	public static Matrix createPerspective(double fovY, double aspect, double near, double far)
	{
		double f = 1.0 / Math.tan(Math.toRadians(fovY) / 2);
		double nearMinusFar = near - far;

		double[] m = {f / aspect, 0, 0, 0, 0, f, 0, 0, 0, 0, (far + near) / nearMinusFar, -1, 0, 0,
				(2 * far * near) / nearMinusFar, 0};

		return new Matrix(m);
	}

	/**
	 * Creates an orthographic projection matrix.
	 *
	 * @param left
	 *            - distance from eye to left clipping plane at near depth
	 * @param right
	 *            - distance from eye to right clipping plane at near depth
	 * @param top
	 *            - distance from eye to top clipping plane at near depth
	 * @param bottom
	 *            - distance from eye to bottom clipping plane at near depth
	 * @param near
	 *            - distance from eye to near clipping plane
	 * @param far
	 *            - distance from eye to far clipping plane
	 */
	public static Matrix createOrtho(double left, double right, double bottom, double top, double near, double far)
	{
		double rightMinusLeft = right - left;
		double topMinusBottom = top - bottom;
		double farMinusNear = far - near;

		double tx = -(right + left) / rightMinusLeft;
		double ty = -(top + bottom) / topMinusBottom;
		double tz = -(far + near) / farMinusNear;

		double[] m = {2.0 / rightMinusLeft, 0, 0, 0, 0, 2.0 / topMinusBottom, 0, 0, 0, 0, -2.0 / farMinusNear, 0, tx,
				ty, tz, 1.0};

		return new Matrix(m);
	}

	/**
	 * Creates an orthographic projection matrix for use in 2D rendering. Same
	 * as createOrtho with near = -1 and far = 1.
	 *
	 * @param left
	 *            - distance from eye to left clipping plane at near depth
	 * @param right
	 *            - distance from eye to right clipping plane at near depth
	 * @param top
	 *            - distance from eye to top clipping plane at near depth
	 * @param bottom
	 *            - distance from eye to bottom clipping plane at near depth
	 */
	public static Matrix createOrtho2D(double left, double right, double bottom, double top)
	{
		return createOrtho(left, right, bottom, top, -1, 1);
	}

	/**
	 * Creates a viewing matrix derived from an eye point, a reference point
	 * indicating the center of the scene, and an UP vector.
	 */
	public static Matrix createLookAt(double eyeX, double eyeY, double eyeZ, double centerX, double centerY,
			double centerZ, double upX, double upY, double upZ)
	{
		Vec3d f = new Vec3d(centerX - eyeX, centerY - eyeY, centerZ - eyeZ);
		f = f.normalize();

		Vec3d up = new Vec3d(upX, upY, upZ);
		up = up.normalize();

		Vec3d s = f.cross(up).normalize();
		Vec3d u = s.cross(f).normalize();

		double[] m = {s.x, u.x, -f.x, 0.0, s.y, u.y, -f.y, 0.0, s.z, u.z, -f.z, 0.0, 0.0, 0.0, 0.0, 1.0};

		Matrix m1 = new Matrix(m);
		Matrix m2 = Matrix.createTranslation(-eyeX, -eyeY, -eyeZ);

		return m1.times(m2);
	}

	public static Matrix createRotation(double radians, Vec3f u)
	{
		return createRotation(radians, new Vec3d(u.x, u.y, u.z));
	}

	/**
	 * Retrieves the elements of the matrix wrapped in a double buffer
	 *
	 * @return the double buffer containing the elements of the matrix
	 */
	public DoubleBuffer wrap()
	{
		return DoubleBuffer.wrap(m);
	}

	/**
	 * Returns the values of the matrix in a float buffer
	 */
	public FloatBuffer wrapf()
	{
		FloatBuffer buf = FloatBuffer.allocate(m.length);
		for (int i = 0; i < buf.capacity(); i++)
			buf.put((float) m[i]);
		buf.rewind();
		return buf;
	}

	public float[] getRow(int row)
	{
		float[] v = new float[4];
		for (int i = 0; i < 4; i++)
			v[i] = (float) m[row + 4 * i];
		return v;
	}

	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < 4; i++)
			b.append(String.format("%5.1f %5.1f %5.1f %5.1f\n", m[i], m[i + 4], m[i + 8], m[i + 12]));
		return b.toString();
	}
}