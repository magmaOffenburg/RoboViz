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
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;
import jsgl.jogl.view.Viewport;
import jsgl.math.*;
import jsgl.math.vector.Matrix;
import jsgl.math.vector.Vec3d;
import jsgl.math.vector.Vec3f;

/**
 * Contains methods for drawing primitive shapes. For performance reasons, many
 * of these methods should avoided. It is far more efficient to batch calls of
 * multiple primitives rather than processing them one at a time. The primary
 * use of these methods is minimizing code for testing / debug purposes.
 *
 * @author Justin
 */
public class Draw
{
	public static void arrow(GL2 gl, Vec3f base, Vec3f tip, Vec3f up, float headLength, float headWidth)
	{
		Vec3f v = tip.minus(base);

		Matrix rotX = Matrix.createRotationZ(Maths.vecAngle(v, Vec3f.unitY()));
		Matrix rotY = Matrix.createRotationY(Maths.vecAngle(v, Vec3f.unitX()));
		// Matrix rotZ = Matrix.createRotationZ(Maths.vecAngle(v, Vec3f.unitY()));
		Matrix rot = rotY.times(rotX);

		// local coordinate space
		Vec3f x = rot.transform(Vec3f.unitX());
		Vec3f y = rot.transform(Vec3f.unitY());

		// vertices
		Vec3f tipY = tip.minus(x.times(headLength));
		Vec3f offsetY = y.times(headWidth / 2.0f);

		Vec3f tipL = tipY.minus(offsetY);
		Vec3f tipR = tipY.plus(offsetY);

		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(base.x, base.y, base.z);
		gl.glVertex3f(tip.x, tip.y, tip.z);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glVertex3f(tip.x, tip.y, tip.z);
		gl.glVertex3f(tipL.x, tipL.y, tipL.z);
		gl.glVertex3f(tipR.x, tipR.y, tipR.z);
		gl.glEnd();
	}

	public static void ray(GL2 gl, Ray r)
	{
		Vec3f a = r.getPosition();
		Vec3f b = a.plus(r.getDirection().times(Float.MAX_VALUE));

		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(a.x, a.y, a.z);
		gl.glVertex3f(b.x, b.y, b.z);
		gl.glEnd();
		gl.glBegin(GL.GL_POINTS);
		gl.glVertex3f(a.x, a.y, a.z);
		gl.glEnd();
	}

	public static void polygon(GL2 gl, Polygon p)
	{
		gl.glBegin(GL2.GL_POLYGON);
		for (int i = 0; i < p.v.length; i++)
			gl.glVertex3fv(p.v[i].getVals(), 0);
		gl.glEnd();
	}

	public static void triangle(GL2 gl, Triangle t)
	{
		gl.glBegin(GL2.GL_POLYGON);
		for (int i = 0; i < 3; i++)
			gl.glVertex3fv(t.v[i].getVals(), 0);
		gl.glEnd();
	}

	public static void point(GL2 gl, Tuplef c)
	{
		gl.glBegin(GL.GL_POINTS);
		switch (c.getN()) {
		case 2:
			gl.glVertex2fv(c.getVals(), 0);
			break;
		case 3:
			gl.glVertex3fv(c.getVals(), 0);
			break;
		case 4:
			gl.glVertex4fv(c.getVals(), 0);
			break;
		}
		gl.glEnd();
	}

	public static void point(GL2 gl, Tupled c)
	{
		gl.glBegin(GL.GL_POINTS);
		switch (c.getN()) {
		case 2:
			gl.glVertex2dv(c.getVals(), 0);
			break;
		case 3:
			gl.glVertex3dv(c.getVals(), 0);
			break;
		case 4:
			gl.glVertex4dv(c.getVals(), 0);
			break;
		}
		gl.glEnd();
	}

	/** Draws a single line between start and end. */
	public static void line(GL2 gl, Vec3d start, Vec3d end)
	{
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(start.x, start.y, start.z);
		gl.glVertex3d(end.x, end.y, end.z);
		gl.glEnd();
	}

	public static void line(GL2 gl, Vec3f start, Vec3f end)
	{
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(start.x, start.y, start.z);
		gl.glVertex3d(end.x, end.y, end.z);
		gl.glEnd();
	}

	/** Draws the world X,Y,Z axes */
	public static void axes(GL2 gl, double length)
	{
		gl.glBegin(GL.GL_LINES);
		gl.glColor3f(0.5f, 0, 0);
		gl.glVertex3d(0, 0, 0);
		gl.glVertex3d(length, 0, 0);
		gl.glColor3f(0, 0.5f, 0);
		gl.glVertex3d(0, 0, 0);
		gl.glVertex3d(0, length, 0);
		gl.glColor3f(0, 0, 0.5f);
		gl.glVertex3d(0, 0, 0);
		gl.glVertex3d(0, 0, length);
		gl.glEnd();
	}

	/**
	 * Draws a square grid on the XZ plane
	 *
	 * @param size
	 *           - size of the side of the square grid
	 * @param spacing
	 *           - spacing units between grid lines
	 * @param color
	 *           - color of the grid
	 */
	public static void grid(GL2 gl, double size, double spacing)
	{
		gl.glBegin(GL.GL_LINES);
		double i = -size;
		while (i <= size) {
			gl.glVertex3d(i, 0, -size);
			gl.glVertex3d(i, 0, size);
			gl.glVertex3d(-size, 0, i);
			gl.glVertex3d(size, 0, i);
			i += spacing;
		};
		gl.glEnd();
	}

	/**
	 * Draws text on the screen
	 *
	 * @param tr
	 *           - text renderer object
	 * @param text
	 *           - text to be displayed
	 * @param x
	 *           - x coordinate on screen
	 * @param y
	 *           - y coordinate on screen
	 */
	public static void text(TextRenderer tr, Viewport vp, String text, int x, int y)
	{
		String[] lines = text.split("\n");

		tr.beginRendering(vp.getW(), vp.getH());
		// for (int i = 0; i < lines.length; i++)
		// tr.draw(lines[i], x - 1, y - tr.getFont().getSize() * i - 1);
		for (int i = 0; i < lines.length; i++)
			tr.draw(lines[i], x, y - tr.getFont().getSize() * i);
		tr.endRendering();
	}
}
