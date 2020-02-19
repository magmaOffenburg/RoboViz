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

package jsgl.jogl.light;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;

/**
 * Data structure used to store information necessary for lighting. This class
 * does not correspond to an OpenGL fixed-function light; that is, modifying its
 * members will not have an effect on the current scene's lighting. The purpose
 * of this class is to consolidate lighting attributes in one structure. There
 * are also some utility methods to apply the stored values to a specified
 * OpenGL light.
 *
 * @author Justin Stoecker
 */
public abstract class Light
{
	protected float[] ambient = new float[] {0, 0, 0, 1};
	protected float[] diffuse = new float[] {1, 1, 1, 1};
	protected float[] specular = new float[] {1, 1, 1, 1};
	protected float[] position = new float[] {0, 0, 0, 0};

	/** Returns a clone of the light's ambient color values */
	public float[] getAmbient()
	{
		return ambient.clone();
	}

	/** Returns a clone of the light's diffuse color values */
	public float[] getDiffuse()
	{
		return diffuse.clone();
	}

	/** Returns a clone of the light's specular color values */
	public float[] getSpecular()
	{
		return specular.clone();
	}

	/** Returns a clone of the light's position values */
	public float[] getPosition()
	{
		return position.clone();
	}

	/** Sets the light's ambient color values */
	public void setAmbient(float r, float g, float b, float a)
	{
		ambient[0] = r;
		ambient[1] = g;
		ambient[2] = b;
		ambient[3] = a;
	}

	/** Sets the light's diffuse color values */
	public void setDiffuse(float r, float g, float b, float a)
	{
		diffuse[0] = r;
		diffuse[1] = g;
		diffuse[2] = b;
		diffuse[3] = a;
	}

	/** Sets the light's specular color values */
	public void setSpecular(float r, float g, float b, float a)
	{
		specular[0] = r;
		specular[1] = g;
		specular[2] = b;
		specular[3] = a;
	}

	/** Sets the light's position values */
	protected void setPosition(float x, float y, float z, float w)
	{
		position[0] = x;
		position[1] = y;
		position[2] = z;
		position[3] = w;
	}

	/**
	 * Applies this light's values to an OpenGL fixed-function light
	 *
	 * @param gl
	 *           - OpenGL context
	 * @param light
	 *           - id of the light (ex. GL_LIGHT0)
	 */
	public void applyTo(GL2 gl, int light)
	{
		gl.glLightfv(light, GLLightingFunc.GL_SPECULAR, specular, 0);
		gl.glLightfv(light, GLLightingFunc.GL_AMBIENT, ambient, 0);
		gl.glLightfv(light, GLLightingFunc.GL_DIFFUSE, diffuse, 0);
		gl.glLightfv(light, GLLightingFunc.GL_POSITION, position, 0);
	}
}
