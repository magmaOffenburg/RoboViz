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

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUtessellatorCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Used by GLU for tessellating polygons
 *
 * @see http
 *      ://www.java-tips.org/other-api-tips/jogl/polygon-tessellation-in-jogl
 *      .html
 */
public class TessCallback implements GLUtessellatorCallback
{
	private static final Logger LOGGER = LogManager.getLogger();

	private GL2 gl;
	private GLU glu;

	public TessCallback(GL2 gl, GLU glu)
	{
		this.gl = gl;
		this.glu = glu;
	}

	@Override
	public void begin(int type)
	{
		gl.glBegin(type);
	}

	@Override
	public void end()
	{
		gl.glEnd();
	}

	@Override
	public void vertex(Object vertexData)
	{
		double[] pointer;
		if (vertexData instanceof double[]) {
			pointer = (double[]) vertexData;
			if (pointer.length == 6)
				gl.glColor3dv(pointer, 3);
			gl.glVertex3dv(pointer, 0);
		}
	}

	@Override
	public void error(int errnum)
	{
		String estring;

		estring = glu.gluErrorString(errnum);
		LOGGER.error("Tessellation Error: " + estring);
		System.exit(0);
	}

	@Override
	public void combine(double[] coords, Object[] data, float[] weight, Object[] outData)
	{
	}

	@Override
	public void combineData(double[] coords, Object[] data, float[] weight, Object[] outData, Object polygonData)
	{
	}

	@Override
	public void edgeFlag(boolean boundaryEdge)
	{
	}

	@Override
	public void edgeFlagData(boolean boundaryEdge, Object polygonData)
	{
	}

	@Override
	public void endData(Object polygonData)
	{
	}

	@Override
	public void errorData(int errnum, Object polygonData)
	{
	}

	@Override
	public void beginData(int type, Object polygonData)
	{
	}

	@Override
	public void vertexData(Object vertexData, Object polygonData)
	{
	}
}
