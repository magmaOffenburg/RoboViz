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

import jsgl.math.vector.Vec3f;

/**
 * Stores directional light parameters
 *
 * @author Justin Stoecker
 */
public class DirLight extends Light
{
	/** Returns a copy of the light's direction */
	public Vec3f getDirection()
	{
		return new Vec3f(position);
	}

	/** Sets the light direction */
	public void setDirection(float x, float y, float z)
	{
		setPosition(x, y, z, 0);
	}

	/** Sets the light direction */
	public void setDirection(Vec3f direction)
	{
		setPosition(direction.x, direction.y, direction.z, 0);
	}

	/** Creates a new directional light */
	public DirLight(float dirX, float dirY, float dirZ)
	{
		setDirection(dirX, dirY, dirZ);
	}

	/** Creates a new directional light */
	public DirLight(Vec3f direction)
	{
		setDirection(direction);
	}
}
