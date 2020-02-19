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

public class Point extends Shape
{
	/** Size of a parsed draw circle command packet */
	public static final int CMD_SIZE = 31;

	private final float[] position;
	private final float size;

	public Point(String set, float[] position, float[] color, float size)
	{
		super(set, color);
		this.position = position;
		this.size = size;
	}

	@Override
	public void draw(GL2 gl)
	{
		gl.glPointSize(size);
		gl.glColor3fv(color, 0);
		gl.glBegin(GL.GL_POINTS);
		gl.glVertex3fv(position, 0);
		gl.glEnd();
	}

	public static Point parse(ByteBuffer buf)
	{
		float[] pos = Command.readCoords(buf, 3);
		float size = Command.readFloat(buf);
		float[] color = Command.readRGB(buf);
		String set = Command.getString(buf);

		return new Point(set, pos, color, size);
	}
}
