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
 * @author Justin Stoecker
 */
public class Line extends Shape
{
	private final float[] start;
	private final float[] end;
	private final float thickness;

	public Line(String set, float[] start, float[] end, float[] color, float thickness)
	{
		super(set, color);
		this.start = start;
		this.end = end;
		this.color = color;
		this.thickness = thickness;
	}

	@Override
	public void draw(GL2 gl)
	{
		gl.glColor3fv(color, 0);
		gl.glLineWidth(thickness);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3fv(start, 0);
		gl.glVertex3fv(end, 0);
		gl.glEnd();
	}

	public static Line parse(ByteBuffer buf)
	{
		float[] start = Command.readCoords(buf, 3);
		float[] end = Command.readCoords(buf, 3);
		float thickness = Command.readFloat(buf);
		float[] color = Command.readRGB(buf);
		String set = Command.getString(buf);

		return new Line(set, start, end, color, thickness);
	}
}
