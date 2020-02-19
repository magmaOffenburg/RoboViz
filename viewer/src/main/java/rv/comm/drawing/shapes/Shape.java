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
import jsgl.jogl.light.Material;

public abstract class Shape
{
	protected final Material material;
	protected float[] color;
	protected final String set;

	public String getSetName()
	{
		return set;
	}

	public Shape(String set, float[] color)
	{
		this.set = set;
		this.color = color;

		material = new Material();
		material.setDiffAmbient(color[0], color[1], color[2], 1);
	}

	public abstract void draw(GL2 gl);
}
