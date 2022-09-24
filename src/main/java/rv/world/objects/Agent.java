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
import java.util.ArrayList;
import java.util.List;
import jsgl.math.BoundingBox;
import jsgl.math.vector.Matrix;
import jsgl.math.vector.Vec3f;
import rv.comm.drawing.annotations.AgentAnnotation;
import rv.comm.rcssserver.scenegraph.Node;
import rv.comm.rcssserver.scenegraph.SceneGraph;
import rv.comm.rcssserver.scenegraph.StaticMeshNode;
import rv.content.ContentManager;
import rv.content.Model;
import rv.world.ISelectable;
import rv.world.Team;
import rv.world.WorldModel;

/**
 * A single RoboCup agent. This object contains references to all the mesh parts for a specific
 * agent in the simulation. It maintains a bounding box for all the parts, collectively.
 *
 * @author Justin Stoecker
 */
public class Agent implements ISelectable
{
	public interface ChangeListener
	{
		void transformChanged(Matrix headTransform);
	}

	private final List<ChangeListener> listeners = new ArrayList<>();
	private final List<StaticMeshNode> meshNodes;
	private BoundingBox bounds;
	private final ContentManager content;
	private final Team team;
	private final int id;
	private boolean selected = false;
	private Matrix headTransform;

	private Vec3f headCenter;
	private Vec3f headDirection;

	private AgentAnnotation annotation = null;
	private Vec3f torsoDirection = null;

	private int age;

	public void setAnnotation(AgentAnnotation annotation)
	{
		this.annotation = annotation;
	}

	public AgentAnnotation getAnnotation()
	{
		return annotation;
	}

	public Matrix getHeadTransform()
	{
		return headTransform;
	}

	public Vec3f getHeadCenter()
	{
		return headCenter;
	}

	public Vec3f getHeadDirection()
	{
		return headDirection;
	}

	public Vec3f getTorsoDirection()
	{
		return torsoDirection;
	}

	/**
	 * Returns the agent's ID number with the first agent on a team having ID 1
	 */
	public int getID()
	{
		return id;
	}

	/**
	 * Returns a reference to the team the agent belongs to
	 */
	public Team getTeam()
	{
		return team;
	}

	public void addChangeListener(ChangeListener l)
	{
		listeners.add(l);
	}

	public void removeChangeListener(ChangeListener l)
	{
		listeners.remove(l);
	}

	public Agent(Team team, int id, Node rootNode, SceneGraph sg, ContentManager cm)
	{
		this.team = team;
		this.id = id;
		this.content = cm;

		meshNodes = sg.getAllMeshNodes(rootNode);

		for (StaticMeshNode node : meshNodes) {
			// Check for switching models to goalie jersey
			if ((node.getName().matches(".*naobody.*[.]obj$") || node.getName().matches(".*lupperarm.*[.]obj$") ||
						node.getName().matches(".*rupperarm.*[.]obj$")) &&
					!node.getName().endsWith("G.obj") && id == 1) {
				node.setName(node.getName().substring(0, node.getName().length() - 4) + "G.obj");
			}
		}
	}

	/**
	 * Grabs model matrices from scene graph and updates bounding box
	 */
	public void update(SceneGraph sg)
	{
		Vec3f min = new Vec3f(Float.POSITIVE_INFINITY);
		Vec3f max = new Vec3f(Float.NEGATIVE_INFINITY);

		for (StaticMeshNode node : meshNodes) {
			Model model = content.getModel(node.getName());
			if (model.isLoaded()) {
				Vec3f[] corners = model.getMesh().getBounds().getCorners();
				Matrix modelMat = WorldModel.COORD_TFN.times(node.getWorldTransform());

				// store head transformation for "robot perspective" camera mode
				if (node.getName().endsWith("head.obj")) {
					headTransform = modelMat;
					headCenter = headTransform.transform(new Vec3f(0));
					headDirection = headTransform.transform(new Vec3f(0, 0, 1)).minus(headCenter).normalize();
				} else if (node.getName().matches(".*body.*[.]obj$")) {
					// Store body direction for third person view
					Matrix bodyRot = modelMat;
					Vec3f bodyCenter = bodyRot.transform(new Vec3f(0));
					torsoDirection = bodyRot.transform(new Vec3f(0, 0, 1)).minus(bodyCenter).normalize();
				}

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
			}
		}

		bounds = new BoundingBox(min, max);
		for (ChangeListener l : listeners)
			l.transformChanged(headTransform);

		age++;
	}

	@Override
	public Vec3f getPosition()
	{
		return bounds == null ? null : bounds.getCenter();
	}

	public int getAge()
	{
		return age;
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
			ContentManager.renderSelection(gl, getPosition(), 0.25f, team.getColorMaterial().getDiffuse(), 1, false);
		}
	}

	/** Returns identifier for agent based on team and ID (ex. L.1 for left 1) */
	public String getShortName()
	{
		return String.format("%c.%d", (team.getID() == Team.LEFT) ? 'L' : 'R', id);
	}
}
