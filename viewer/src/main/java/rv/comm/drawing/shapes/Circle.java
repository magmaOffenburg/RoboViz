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

package rv.comm.drawing.shapes;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import java.nio.ByteBuffer;
import rv.comm.drawing.commands.Command;

/**
 * Circle that lies on the surface of the field
 *
 * @author Justin Stoecker
 */
public class Circle extends Shape
{
	private final double[][] pts = new double[20][];
	private final float thickness;

	public Circle(String set, float[] pos, float[] color, float radius, float thickness)
	{
		super(set, color);
		double angleInc = Math.PI * 2.0 / pts.length;

		// these are all 3D coordinates, not SimSpark coordinates
		for (int i = 0; i < pts.length; i++) {
			pts[i] = new double[3];
			pts[i][0] = Math.cos(angleInc * i) * radius + pos[0];
			pts[i][1] = pts[i][1];
			pts[i][2] = Math.sin(angleInc * i) * radius + pos[2];
		}
		this.thickness = thickness;
	}

	@Override
	public void draw(GL2 gl)
	{
		gl.glColor3fv(color, 0);
		gl.glLineWidth(thickness);
		gl.glBegin(GL.GL_LINE_LOOP);
		for (double[] pt : pts)
			gl.glVertex3d(pt[0], pt[1], pt[2]);
		gl.glEnd();
	}

	public static Circle parse(ByteBuffer buf)
	{
		float[] posXY = Command.readCoords(buf, 2);
		float radius = Command.readFloat(buf);
		float thickness = Command.readFloat(buf);
		float[] color = Command.readRGB(buf);
		String set = Command.getString(buf);

		return new Circle(set, posXY, color, radius, thickness);
	}
}
