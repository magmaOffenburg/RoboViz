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
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;

/**
 * An arrangement of lights that can be applied to a scene
 *
 * @author Justin Stoecker
 */
public class LightModel
{
	private float[] globalAmbient = new float[] {0.2f, 0.2f, 0.2f, 1};
	private Light[] lights = new Light[8];
	private int numLights = 0;

	public void setGlobalAmbient(float r, float g, float b, float a)
	{
		globalAmbient = new float[] {r, g, b, a};
	}

	public void addLight(Light light)
	{
		if (numLights < lights.length)
			lights[numLights++] = light;
	}

	public void removeLight(int i)
	{
		for (int j = i; j < lights.length - 1; j++)
			lights[j] = lights[j + 1];
		numLights--;
	}

	public void removeLight(Light light)
	{
		for (int i = 0; i < lights.length; i++)
			if (lights[i] == light)
				removeLight(i);
	}

	public Light getLight(int i)
	{
		return lights[i];
	}

	public void apply(GL2 gl)
	{
		gl.glLightModelfv(GL2ES1.GL_LIGHT_MODEL_AMBIENT, globalAmbient, 0);
		for (int i = 0; i < lights.length; i++) {
			if (lights[i] != null) {
				gl.glEnable(GLLightingFunc.GL_LIGHT0 + i);
				lights[i].applyTo(gl, GLLightingFunc.GL_LIGHT0 + i);
			}
		}
	}
}
