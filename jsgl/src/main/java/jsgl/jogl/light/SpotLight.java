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
import jsgl.math.Maths;
import jsgl.math.vector.Vec3f;

/**
 * A point light that emits light restricted within a cone.
 *
 * @author Justin Stoecker
 */
public class SpotLight extends PointLight
{
	private static final int MIN_CUTOFF = 0;
	private static final int MAX_CUTOFF = 90;
	private static final int MIN_EXPONENT = 0;
	private static final int MAX_EXPONENT = 128;

	protected float[] spotDirection = new float[] {0, 0, -1};
	protected float spotCutoff = 45;
	protected float spotExponent = 0;

	public Vec3f getSpotDirection()
	{
		return new Vec3f(spotDirection);
	}

	public float getSpotCutoff()
	{
		return spotCutoff;
	}

	public float getSpotExponent()
	{
		return spotExponent;
	}

	/** Sets the light's spot direction */
	public void setSpotDirection(Vec3f dir)
	{
		spotDirection[0] = dir.x;
		spotDirection[1] = dir.y;
		spotDirection[2] = dir.z;
	}

	/** Sets the light's spot direction */
	public void setSpotDirection(float x, float y, float z)
	{
		spotDirection[0] = x;
		spotDirection[1] = y;
		spotDirection[2] = z;
	}

	/**
	 * Sets the light's spot cutoff angle in degrees. Values should be in the
	 * range [0,90].
	 */
	public void setSpotCutoff(float c)
	{
		spotCutoff = Maths.clamp(c, MIN_CUTOFF, MAX_CUTOFF);
	}

	/**
	 * Sets the light's spot exponent, which determines the intensity
	 * distribution of the light. Values should be in the range [0,128].
	 */
	public void setSpotExponent(float e)
	{
		spotExponent = Maths.clamp(e, MIN_EXPONENT, MAX_EXPONENT);
	}

	/** Creates a new spotlight. */
	public SpotLight(Vec3f pos, Vec3f dir)
	{
		super(pos);
		setSpotDirection(dir);
	}

	/** Creates a new spotlight. */
	public SpotLight(Vec3f pos, Vec3f dir, float cutoff)
	{
		this(pos, dir);
		this.spotCutoff = cutoff;
	}

	/** Creates a new spotlight. */
	public SpotLight(Vec3f pos, Vec3f dir, float cutoff, float exponent)
	{
		this(pos, dir, cutoff);
		this.spotExponent = exponent;
	}

	@Override
	public void applyTo(GL2 gl, int glLight)
	{
		super.applyTo(gl, glLight);
		gl.glLightfv(glLight, GLLightingFunc.GL_SPOT_DIRECTION, spotDirection, 0);
		gl.glLightf(glLight, GLLightingFunc.GL_SPOT_CUTOFF, spotCutoff);
		gl.glLightf(glLight, GLLightingFunc.GL_SPOT_EXPONENT, spotExponent);
	}
}
