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

package jsgl.math;

import jsgl.math.vector.Vec3f;

/**
 * A 3D ray
 *
 * @author Justin Stoecker
 */
public class Ray
{
	private Vec3f p;
	private Vec3f d;

	public Vec3f getPosition()
	{
		return p;
	}

	public Vec3f getDirection()
	{
		return d;
	}

	public Ray(Vec3f position, Vec3f direction)
	{
		this.p = position;
		this.d = direction.normalize();
	}
}
