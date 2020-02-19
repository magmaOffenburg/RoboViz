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

import com.jogamp.opengl.GL2;
import java.nio.ByteBuffer;
import jsgl.io.ByteUtil;
import rv.comm.drawing.commands.Command;

public class Polygon extends Shape
{
	private final float[][] v;

	public Polygon(String set, float[] color, float[][] verts)
	{
		super(set, color);
		this.v = verts;
	}

	@Override
	public void draw(GL2 gl)
	{
		gl.glColor4fv(color, 0);
		gl.glBegin(GL2.GL_POLYGON);
		for (float[] aV : v)
			gl.glVertex3fv(aV, 0);
		gl.glEnd();
	}

	public static Polygon parse(ByteBuffer buf)
	{
		int numVerts = ByteUtil.uValue(buf.get());
		float[][] v = new float[numVerts][];

		float[] color = Command.readRGBA(buf);
		for (int i = 0; i < numVerts; i++)
			v[i] = Command.readCoords(buf, 3);

		String set = Command.getString(buf);

		return new Polygon(set, color, v);
	}
}
