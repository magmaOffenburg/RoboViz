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

package jsgl.math.geom;

import jsgl.math.BoundingBox;
import jsgl.math.vector.Matrix;
import jsgl.math.vector.Vec3f;
import jsgl.math.vector.Vec4f;

/**
 * Bounding frustum of a view that can be used for intersection tests
 *
 * @author justin
 */
public class BoundingFrustum
{
	public enum ContainmentType
	{
		Disjoint,
		Contains,
		Intersects,
	}

	private Matrix viewProjection;

	public BoundingFrustum(Matrix view, Matrix projection)
	{
		viewProjection = projection.times(view);
	}

	public BoundingFrustum(Matrix viewProjection)
	{
		this.viewProjection = viewProjection;
	}

	public boolean contains(Vec3f point)
	{
		return contains(new Vec4f(point, 1));
	}

	public boolean contains(Vec4f point)
	{
		// transform point to clip space
		Vec4f p = viewProjection.transform(point);

		// perform homogeneous division for normalize device coordinates
		p = p.over(p.w);

		// now check if point is inside cube
		if (p.x > 1 || p.x < -1 || p.y > 1 || p.y < -1 || p.z > 1 || p.z < -1)
			return false;
		return true;
	}

	public boolean intersects(BoundingBox box)
	{
		// TODO: check all cases

		// check if any corners of the box are inside the frustum
		Vec3f[] corners = box.getCorners();
		for (Vec3f corner : corners)
			if (contains(corner))
				return true;

		// check if an edge intersects

		// check if entire frustum is contained by box

		return false;
	}
}
