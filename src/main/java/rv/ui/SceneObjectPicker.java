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

package rv.ui;

import java.util.ArrayList;
import java.util.List;
import jsgl.jogl.view.Camera3D;
import jsgl.jogl.view.Viewport;
import jsgl.math.Plane;
import jsgl.math.Ray;
import jsgl.math.vector.Vec3f;
import rv.world.ISelectable;
import rv.world.WorldModel;

/**
 * For picking selectable objects with a ray
 *
 * @author justin
 *
 */
public class SceneObjectPicker
{
	private final WorldModel world;
	private final Camera3D camera;
	private Ray pickRay;

	public SceneObjectPicker(WorldModel world, Camera3D camera)
	{
		this.world = world;
		this.camera = camera;
	}

	/** Updates ray used for picking from screen coordinates */
	public void updatePickRay(Viewport vp, int x, int y)
	{
		pickRay = camera.unproject(vp, x, y);
	}

	/** Picks position on field (plane Y=0) that picking ray intersects */
	public Vec3f pickField()
	{
		Plane p = new Plane(new Vec3f(0), Vec3f.unitY());
		return p.intersect(pickRay);
	}

	/**
	 * Selects object nearest to the camera whose bounding box intersects picking ray
	 */
	public ISelectable pickObject()
	{
		if (pickRay == null)
			return null;

		List<ISelectable> selectables = new ArrayList<>();
		selectables.addAll(world.getLeftTeam().getAgents());
		selectables.addAll(world.getRightTeam().getAgents());
		selectables.add(world.getBall());

		return pickClosest(selectables);
	}

	private ISelectable pickClosest(List<ISelectable> objects)
	{
		ISelectable closest = null;
		float minDepth = Float.POSITIVE_INFINITY;
		float d;
		for (ISelectable o : objects) {
			if (o.getBoundingBox() == null)
				continue;
			Vec3f[] x = o.getBoundingBox().intersect(pickRay);
			if (x != null && (d = getMinDepth(x, pickRay.getPosition())) < minDepth) {
				closest = o;
				minDepth = d;
			}
		}
		return closest;
	}

	private static float getMinDepth(Vec3f[] x, Vec3f origin)
	{
		float minDepth = Float.POSITIVE_INFINITY;
		for (Vec3f aX : x) {
			float d = aX.minus(origin).lengthSquared();
			minDepth = d < minDepth ? d : minDepth;
		}

		return minDepth;
	}
}
