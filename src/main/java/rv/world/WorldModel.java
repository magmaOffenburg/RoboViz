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

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import java.awt.Color;
import java.util.ArrayList;
import jsgl.jogl.light.DirLight;
import jsgl.jogl.light.LightModel;
import jsgl.math.vector.Matrix;
import jsgl.math.vector.Vec3f;
import org.magmaoffenburg.roboviz.configuration.Config;
import org.magmaoffenburg.roboviz.configuration.Config.TeamColors;
import org.magmaoffenburg.roboviz.rendering.CameraController;
import org.magmaoffenburg.roboviz.util.DataTypes;
import rv.comm.rcssserver.GameState;
import rv.comm.rcssserver.ISceneGraphItem;
import rv.comm.rcssserver.scenegraph.SceneGraph;
import rv.comm.rcssserver.scenegraph.SceneGraph.SceneGraphListener;
import rv.content.ContentManager;
import rv.world.objects.Agent;
import rv.world.objects.Ball;
import rv.world.objects.Field;
import rv.world.objects.SkyBox;

/**
 * Contains, updates, and renders world state data
 *
 * @author Justin Stoecker
 */
public class WorldModel
{
	public interface SelectionChangeListener
	{
		void selectionChanged(ISelectable newSelection);
	}

	/** Transforms SimSpark coordinates to RoboViz coordinates (and reverse) */
	public static final Matrix COORD_TFN = new Matrix(new double[] {-1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1});

	private final GameState gameState = new GameState();
	private SceneGraph sceneGraph = null;
	private ContentManager cm;

	private final ArrayList<ISceneGraphItem> sgItems = new ArrayList<>();

	private Field field;
	private Ball ball;
	private Team leftTeam;
	private Team rightTeam;
	private LightModel lighting;
	private SkyBox skyBox;

	private ISelectable selectedObject;
	private float ballCircleTimeLeft;
	private float ballCircleTime;

	private final ArrayList<SceneGraphListener> sgListeners = new ArrayList<>();
	private final ArrayList<SelectionChangeListener> selListeners = new ArrayList<>();

	public void addSceneGraphListener(SceneGraphListener sgl)
	{
		sgListeners.add(sgl);
	}

	public void removeSceneGraphListener(SceneGraphListener sgl)
	{
		sgListeners.remove(sgl);
	}

	public void addSelectionChangeListener(SelectionChangeListener sl)
	{
		selListeners.add(sl);
	}

	public void removeSelectionChangeListener(SelectionChangeListener sl)
	{
		selListeners.remove(sl);
	}

	public ISelectable getSelectedObject()
	{
		return selectedObject;
	}

	public Ball getBall()
	{
		return ball;
	}

	public void setSelectedObject(ISelectable newSelection)
	{
		if (newSelection == selectedObject)
			return;

		if (selectedObject != null)
			selectedObject.setSelected(false);

		selectedObject = newSelection;

		if (selectedObject != null)
			selectedObject.setSelected(true);

		for (SelectionChangeListener listener : selListeners)
			listener.selectionChanged(selectedObject);
	}

	public GameState getGameState()
	{
		return gameState;
	}

	public SceneGraph getSceneGraph()
	{
		return sceneGraph;
	}

	public synchronized void setSceneGraph(SceneGraph sceneGraph)
	{
		this.sceneGraph = sceneGraph;

		if (sceneGraph != null) {
			for (SceneGraphListener sgl : sgListeners)
				sgl.newSceneGraph(sceneGraph);

			for (ISceneGraphItem sgi : sgItems)
				sgi.sceneGraphChanged(sceneGraph);
		}

		if (selectedObject instanceof Agent) {
			Agent agent = (Agent) selectedObject;
			Agent newSelection;
			if (agent.getTeam().getID() == Team.LEFT) {
				newSelection = getLeftTeam().getAgentByID(agent.getID());
			} else {
				newSelection = getRightTeam().getAgentByID(agent.getID());
			}
			setSelectedObject(newSelection);
		}
	}

	public LightModel getLighting()
	{
		return lighting;
	}

	public SkyBox getSkyBox()
	{
		return skyBox;
	}

	public Field getField()
	{
		return field;
	}

	public ArrayList<ISceneGraphItem> getSceneGraphItems()
	{
		return sgItems;
	}

	public Team getLeftTeam()
	{
		return leftTeam;
	}

	public Team getRightTeam()
	{
		return rightTeam;
	}

	public void init(GL glObj, ContentManager cm, DataTypes.Mode mode)
	{
		this.cm = cm;
		GL2 gl = glObj.getGL2();

		field = new Field(cm.getModel("models/newfield.obj"), cm);
		gameState.addListener(field);
		gameState.addListener(cm);

		initTeams();

		ball = new Ball(cm);
		sgItems.add(ball);

		skyBox = new SkyBox(cm.getModel("models/skybox.obj"));

		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		gl.getGL2().glShadeModel(GLLightingFunc.GL_SMOOTH);
		lighting = new LightModel();
		lighting.setGlobalAmbient(0.25f, 0.25f, 0.25f, 1);
		DirLight d1 = new DirLight(new Vec3f(-8, 7, -6).normalize());
		d1.setDiffuse(1, 1, 1, 1);
		lighting.addLight(d1);
	}

	private void initTeams()
	{
		if (leftTeam != null) {
			gameState.removeListener(leftTeam);
			sgItems.remove(leftTeam);
		}
		if (rightTeam != null) {
			gameState.removeListener(rightTeam);
			sgItems.remove(rightTeam);
		}

		TeamColors teamColors = TeamColors.INSTANCE;
		leftTeam = new Team(
				teamColors.getByTeamNames().getOrDefault(Config.defaultLeftTeamName, teamColors.getDefaultLeft()),
				Team.LEFT, cm, teamColors);
		gameState.addListener(leftTeam);
		rightTeam = new Team(
				teamColors.getByTeamNames().getOrDefault(Config.defaultRightTeamName, teamColors.getDefaultRight()),
				Team.RIGHT, cm, teamColors);
		gameState.addListener(rightTeam);

		sgItems.add(leftTeam);
		sgItems.add(rightTeam);
	}

	public synchronized void update(GL gl, double elapsedMS)
	{
		// Allow scene graph items to update their states prior to rendering.
		// This is done in the update loop rather than the scene graph update
		// method because the scene graph might update much more frequently than
		// rendering occurs.
		if (sceneGraph != null) {
			for (ISceneGraphItem sgi : sgItems)
				sgi.update(sceneGraph);
		}

		skyBox.setPosition(CameraController.fpCamera.getPosition());

		ballCircleTimeLeft -= elapsedMS / 1000.0;
	}

	public void renderBallCircle(GL2 gl)
	{
		if (gameState.hasPlayModeJustChanged()) {
			// just switched
			switch (gameState.getPlayMode()) {
			case GameState.PASS_LEFT:
			case GameState.PASS_RIGHT:
				ballCircleTime = ballCircleTimeLeft = gameState.getPassModeDuration();
				break;
			case GameState.KICK_IN_LEFT:
			case GameState.KICK_IN_RIGHT:
			case GameState.CORNER_KICK_LEFT:
			case GameState.CORNER_KICK_RIGHT:
			case GameState.FREE_KICK_LEFT:
			case GameState.FREE_KICK_RIGHT:
				ballCircleTime = ballCircleTimeLeft = 15;
				break;
			}
		}

		Color color = null;
		switch (gameState.getPlayMode()) {
		case GameState.PASS_LEFT:
		case GameState.KICK_IN_LEFT:
		case GameState.CORNER_KICK_LEFT:
		case GameState.FREE_KICK_LEFT:
			color = leftTeam.getColor();
			break;
		case GameState.PASS_RIGHT:
		case GameState.KICK_IN_RIGHT:
		case GameState.CORNER_KICK_RIGHT:
		case GameState.FREE_KICK_RIGHT:
			color = rightTeam.getColor();
			break;
		}

		float radius = 0;
		switch (gameState.getPlayMode()) {
		case GameState.PASS_LEFT:
		case GameState.PASS_RIGHT:
			radius = gameState.getPassModeMinOppBallDist();
			break;
		case GameState.KICK_IN_LEFT:
		case GameState.KICK_IN_RIGHT:
		case GameState.CORNER_KICK_LEFT:
		case GameState.CORNER_KICK_RIGHT:
		case GameState.FREE_KICK_LEFT:
		case GameState.FREE_KICK_RIGHT:
			radius = 2.2f;
			break;
		}

		Vec3f ballPos = ball.getPosition();
		if (ballPos != null && color != null) {
			float[] colorComponents = new float[3];
			color.getRGBColorComponents(colorComponents);
			float alpha = Math.max(0.1f, ballCircleTimeLeft / ballCircleTime);
			ContentManager.renderSelection(gl, ballPos, radius, colorComponents, alpha, true);
		}
	}

	public void dispose(GL gl)
	{
		if (field != null)
			field.dispose(gl);
	}

	public synchronized void reset()
	{
		gameState.reset();
		if (cm != null)
			initTeams();
		setSceneGraph(null);
	}
}
