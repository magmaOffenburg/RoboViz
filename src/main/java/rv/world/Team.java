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

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import jsgl.jogl.model.ObjMaterial;
import org.magmaoffenburg.roboviz.configuration.Config;
import org.magmaoffenburg.roboviz.configuration.Config.TeamColors;
import rv.comm.rcssserver.GameState;
import rv.comm.rcssserver.GameState.GameStateChangeListener;
import rv.comm.rcssserver.ISceneGraphItem;
import rv.comm.rcssserver.scenegraph.Node;
import rv.comm.rcssserver.scenegraph.SceneGraph;
import rv.comm.rcssserver.scenegraph.StaticMeshNode;
import rv.content.ContentManager;
import rv.util.jogl.MaterialUtil;
import rv.world.objects.Agent;

/**
 * A team of RoboCup agents
 *
 * @author Justin Stoecker
 */
public class Team implements ISceneGraphItem, GameStateChangeListener
{
	public static final int LEFT = 0;
	public static final int RIGHT = 1;

	public static final int MAX_AGENTS = 11;

	private final TeamColors config;
	private final ContentManager content;
	private final int id;
	private String name;
	private String otherTeamName;
	private final List<Agent> agents;
	private final ObjMaterial colorMaterial;
	private Color color;
	private int score;

	public ObjMaterial getColorMaterial()
	{
		return colorMaterial;
	}

	public Color getColor()
	{
		return color;
	}

	private void setColor(Color color)
	{
		this.color = color;
		MaterialUtil.setColor(colorMaterial, color);
	}

	public int getScore()
	{
		return score;
	}

	public List<Agent> getAgents()
	{
		return agents;
	}

	public String getDisplayName()
	{
		return (name != null) ? name : ((id == LEFT) ? Config.defaultLeftTeamName : Config.defaultRightTeamName);
	}

	/**
	 * Returns the team's ID number where left is Team.LEFT and right is Team.RIGHT
	 */
	public int getID()
	{
		return id;
	}

	public void setScore(int score)
	{
		this.score = score;
	}

	public Team(Color defaultColor, int id, ContentManager content, TeamColors config)
	{
		this.id = id;
		this.content = content;
		this.config = config;
		agents = new CopyOnWriteArrayList<>();

		colorMaterial = new ObjMaterial((id == LEFT) ? Config.defaultLeftTeamName : Config.defaultRightTeamName);
		setColor(defaultColor);
		colorMaterial.setShininess(96);
		colorMaterial.setSpecular(1, 1, 1, 1);
	}

	@Override
	public void sceneGraphChanged(SceneGraph sg)
	{
		// Add agents from scene graph to this team
		// TODO: hopefully we can have the scene graph store agent IDs directly
		// to avoid this computation and guessing
		for (int i = 1; i <= MAX_AGENTS; i++) {
			Agent existingAgent = getAgentByID(i);
			Node agentNode = findAgent(i, sg);
			if (agentNode != null) {
				Agent newAgent = new Agent(this, i, agentNode, sg, content);
				if (existingAgent != null) {
					newAgent.setAnnotation(existingAgent.getAnnotation());
					agents.remove(existingAgent);
				}
				agents.add(newAgent);
			} else if (existingAgent != null) {
				agents.remove(existingAgent);
			}
		}
	}

	@Override
	public void update(SceneGraph sg)
	{
		for (Agent agent : agents)
			agent.update(sg);
	}

	public Agent getAgentByID(int id)
	{
		for (Agent a : agents)
			if (a.getID() == id)
				return a;
		return null;
	}

	/**
	 * Check scene graph to see if an agent on this team with given agentID can be located. Returns
	 * the root node containing that agent's mesh nodes if it can be found; otherwise, returns null.
	 */
	private Node findAgent(int agentID, SceneGraph sg)
	{
		// the agents might be added in any order (two teams joining at once),
		// so the "root" node is found by finding the branch containing a mesh
		// with specific materials that only belong to this agent
		String matAgentID = "matNum" + agentID;
		String matTeamID = id == Team.LEFT ? "matLeft" : "matRight";
		String[] materials = {matAgentID, matTeamID};

		// see if a node can be found with these materials
		StaticMeshNode leaf = sg.findStaticMeshNode(sg.getRoot(), materials);
		if (leaf == null)
			return null;

		// root of the agent node is one level lower than root of entire scene
		// graph
		Node root = leaf;
		if (root.getParent() == null)
			return root;
		while (root.getParent().getParent() != null) {
			root = root.getParent();
		}
		return root;
	}

	@Override
	public void gsMeasuresAndRulesChanged(GameState gs)
	{
	}

	@Override
	public void gsPlayStateChanged(GameState gs)
	{
		// update team color, name & score
		if (id == LEFT) {
			this.name = gs.getTeamLeft();
			this.otherTeamName = gs.getTeamRight();
			setColor(config.getLeftColor(name, otherTeamName));
			score = gs.getScoreLeft();
		} else {
			this.name = gs.getTeamRight();
			this.otherTeamName = gs.getTeamLeft();
			setColor(config.getRightColor(otherTeamName, name));
			score = gs.getScoreRight();
		}
	}

	@Override
	public void gsTimeChanged(GameState gs)
	{
	}
}
