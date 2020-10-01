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
import jsgl.jogl.light.Material;
import jsgl.math.geom.GeodesicSphere;
import rv.comm.drawing.commands.Command;

public class Sphere extends Shape
{
	/** Size of a parsed draw circle command packet */
	public static final int CMD_SIZE = 31;

	private final float[] position;
	private final GeodesicSphere model;

	public Sphere(String set, float[] position, float[] color, float radius)
	{
		super(set, color);
		this.position = position;
		Material mat = new Material();
		this.color = color;
		mat.setDiffAmbient(color[0], color[1], color[2], 1);
		model = new GeodesicSphere(radius, 1);
	}

	@Override
	public void draw(GL2 gl)
	{
		gl.glPushMatrix();
		gl.glColor3fv(color, 0);
		material.apply(gl, GL2.GL_FRONT_AND_BACK);
		gl.glTranslatef(position[0], position[1], position[2]);
		model.render(gl);
		gl.glPopMatrix();
	}

	public static Sphere parse(ByteBuffer buf)
	{
		float[] pos = Command.readCoords(buf, 3);
		float radius = Command.readFloat(buf);
		float[] color = Command.readRGB(buf);
		String set = Command.getString(buf);

		return new Sphere(set, pos, color, radius);
	}
}
