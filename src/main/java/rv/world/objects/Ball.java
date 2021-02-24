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

package rv.world.objects;

import com.jogamp.opengl.GL2;
import jsgl.math.BoundingBox;
import jsgl.math.vector.Matrix;
import jsgl.math.vector.Vec3f;
import rv.comm.rcssserver.ISceneGraphItem;
import rv.comm.rcssserver.scenegraph.SceneGraph;
import rv.comm.rcssserver.scenegraph.StaticMeshNode;
import rv.content.ContentManager;
import rv.content.Model;
import rv.world.ISelectable;
import rv.world.WorldModel;

public class Ball implements ISelectable, ISceneGraphItem
{
	private BoundingBox bounds;
	private boolean selected = false;
	private StaticMeshNode node;
	private final ContentManager content;

	public Ball(ContentManager content)
	{
		this.content = content;
	}

	@Override
	public void sceneGraphChanged(SceneGraph sg)
	{
		node = sg.findStaticMeshNode("soccerball.obj");
	}

	@Override
	public void update(SceneGraph sg)
	{
		if (node == null)
			return;

		Model model = content.getModel(node.getName());
		if (!model.isLoaded()) {
			return;
		}

		Vec3f min = new Vec3f(Float.POSITIVE_INFINITY);
		Vec3f max = new Vec3f(Float.NEGATIVE_INFINITY);

		Vec3f[] corners = model.getMesh().getBounds().getCorners();
		Matrix modelMat = WorldModel.COORD_TFN.times(node.getWorldTransform());
		for (int j = 0; j < 8; j++) {
			Vec3f v = modelMat.transform(corners[j]);
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

	@Override
	public Vec3f getPosition()
	{
		return bounds == null ? null : bounds.getCenter();
	}

	@Override
	public BoundingBox getBoundingBox()
	{
		return bounds;
	}

	@Override
	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	@Override
	public boolean isSelected()
	{
		return selected;
	}

	@Override
	public void renderSelected(GL2 gl)
	{
		if (getPosition() != null) {
			ContentManager.renderSelection(gl, getPosition(), 0.15f, new float[] {1, 1, 1}, 1, false);
		}
	}
}
