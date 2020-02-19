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

package rv.world;

import com.jogamp.opengl.GL2;
import jsgl.math.BoundingBox;
import jsgl.math.vector.Matrix;
import jsgl.math.vector.Vec3f;
import rv.content.Model;

/**
 * An abstract class that represents an object that contains a mesh loaded by the content manager
 * that may be transformed by its model matrix.
 *
 * @author Justin Stoecker
 */
public class ModelObject
{
	protected final Model model;
	protected Matrix modelMatrix = Matrix.createIdentity();
	protected BoundingBox bounds;

	public ModelObject(Model model)
	{
		this.model = model;
	}

	/**
	 * Gets a an axis-aligned box that surrounds all mesh vertices. The box returned is in world
	 * space.
	 */
	public BoundingBox getBoundingBox()
	{
		return bounds;
	}

	public Model getModel()
	{
		return model;
	}

	public Matrix getModelMatrix()
	{
		return modelMatrix;
	}

	/**
	 * Sets the transformation matrix for the mesh. Also updates the bounding box for the object.
	 */
	public void setModelMatrix(Matrix m)
	{
		this.modelMatrix = m;
		calcBounds();
	}

	/**
	 * Calculate bounding box of object in world space.
	 */
	private void calcBounds()
	{
		if (model.getMesh() == null)
			return;

		Vec3f min = new Vec3f(Float.POSITIVE_INFINITY);
		Vec3f max = new Vec3f(Float.NEGATIVE_INFINITY);

		// instead of checking every vertex in the mesh, the 8 corners of the
		// mesh's bounding box (in object space) are transformed by the object's
		// model matrix
		Vec3f[] corners = model.getMesh().getBounds().getCorners();
		for (int i = 0; i < 8; i++) {
			Vec3f v = modelMatrix.transform(corners[i]);
			if (v.x < min.x)
				min.x = v.x;
			if (v.y < min.y)
				min.y = v.y;
			if (v.z < min.z)
				min.z = v.z;
			if (v.x > max.x)
				max.x = v.x;
			if (v.y > max.y)
				max.y = v.y;
			if (v.z > max.z)
				max.z = v.z;
		}
		bounds = new BoundingBox(min, max);
	}

	public void render(GL2 gl)
	{
		if (model.isLoaded())
			model.getMesh().render(gl, modelMatrix);
	}
}
