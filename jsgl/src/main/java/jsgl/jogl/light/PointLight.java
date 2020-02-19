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
import jsgl.math.vector.Vec3f;

/**
 * Stores point light parameters
 *
 * @author Justin Stoecker
 */
public class PointLight extends Light
{
	protected float constantAttenuation = 1;
	protected float linearAttenuation = 0;
	protected float quadraticAttenuation = 0;

	public float getConstantAttenuation()
	{
		return constantAttenuation;
	}

	public float getLinearAttenuation()
	{
		return linearAttenuation;
	}

	public float getQuadraticAttenuation()
	{
		return quadraticAttenuation;
	}

	public void setConstantAttenuation(float c)
	{
		constantAttenuation = c;
	}

	public void setLinearAttenuation(float c)
	{
		linearAttenuation = c;
	}

	public void setQuadraticAttenuation(float c)
	{
		quadraticAttenuation = c;
	}

	/** Creates a new point light */
	public PointLight(float posX, float posY, float posZ)
	{
		setPosition(posX, posY, posZ, 1);
	}

	/** Creates a new point light */
	public PointLight(Vec3f pos)
	{
		setPosition(pos.x, pos.y, pos.z, 1);
	}

	@Override
	public void applyTo(GL2 gl, int light)
	{
		super.applyTo(gl, light);
		gl.glLightf(light, GLLightingFunc.GL_CONSTANT_ATTENUATION, constantAttenuation);
		gl.glLightf(light, GLLightingFunc.GL_LINEAR_ATTENUATION, linearAttenuation);
		gl.glLightf(light, GLLightingFunc.GL_QUADRATIC_ATTENUATION, quadraticAttenuation);
	}
}
