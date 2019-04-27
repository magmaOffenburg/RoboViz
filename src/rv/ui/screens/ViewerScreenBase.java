package rv.ui.screens;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.AbstractAction;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import com.jogamp.opengl.util.gl2.GLUT;
import js.jogl.view.Camera3D;
import js.jogl.view.Viewport;
import js.math.BoundingBox;
import js.math.vector.Vec3f;
import rv.Configuration;
import rv.Viewer;
import rv.comm.drawing.BufferedSet;
import rv.comm.drawing.annotations.AgentAnnotation;
import rv.comm.drawing.annotations.Annotation;
import rv.comm.rcssserver.GameState;
import rv.ui.menus.Menu;
import rv.ui.view.RobotVantageBase;
import rv.ui.view.RobotVantageFirstPerson;
import rv.ui.view.RobotVantageThirdPerson;
import rv.ui.view.TargetTrackerCamera;
import rv.util.swing.SwingUtil;
import rv.world.ISelectable;
import rv.world.Team;
import rv.world.WorldModel;
import rv.world.objects.Agent;

public abstract class ViewerScreenBase
		extends ScreenBase implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener,
									  GameState.GameStateChangeListener, WorldModel.SelectionChangeListener
{
	enum AgentOverheadType
	{
		NONE,
		ANNOTATIONS,
		IDS
	}

	enum RobotVantageType
	{
		NONE,
		FIRST_PERSON,
		THIRD_PERSON
	}

	enum TrackerCameraType
	{
		NONE,
		BALL,
		PLAYER
	}

	protected final Viewer viewer;

	protected final GameStateOverlay gameStateOverlay;
	private final Field2DOverlay fieldOverlay;
	private final FoulListOverlay foulListOverlay;
	protected final List<Screen> overlays = new ArrayList<>();

	protected final BorderTextRenderer overlayTextRenderer;
	private final List<TextOverlay> textOverlays = new ArrayList<>();

	private RobotVantageBase robotVantage = null;
	private RobotVantageType robotVantageType = RobotVantageType.NONE;
	private int firstPersonFOV;
	private int thirdPersonFOV;

	private AgentOverheadType agentOverheadType = AgentOverheadType.ANNOTATIONS;
	protected final BorderTextRenderer tr;

	private TrackerCameraType trackerCameraType = TrackerCameraType.NONE;

	protected int prevScoreL = -1;
	protected int prevScoreR = -1;
	private boolean showNumPlayers = false;

	public ViewerScreenBase(Viewer viewer)
	{
		this.viewer = viewer;
		gameStateOverlay = new GameStateOverlay(viewer);
		overlays.add(gameStateOverlay);
		fieldOverlay = new Field2DOverlay(viewer.getWorldModel());
		overlays.add(fieldOverlay);
		foulListOverlay = new FoulListOverlay(viewer);
		overlays.add(foulListOverlay);

		overlayTextRenderer = new BorderTextRenderer(new Font("Arial", Font.PLAIN, 48), true, false);
		Font font = new Font("Arial", Font.BOLD, 16);
		tr = new BorderTextRenderer(font, true, false);

		Configuration.Graphics config = viewer.getConfig().graphics;
		firstPersonFOV = config.firstPersonFOV;
		thirdPersonFOV = config.thirdPersonFOV;

		viewer.getWorldModel().getGameState().addListener(this);
		viewer.getWorldModel().addSelectionChangeListener(this);
		viewer.addWindowResizeListener(this);

		loadOverlayVisibilities(viewer.getConfig().overlayVisibility);
		createCameraMenu(viewer.getFrame().getMenu().getCameraMenu());
	}

	@Override
	public void createViewMenu(Menu menu)
	{
		menu.addItem("Help", "F1", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				openHelp();
			}
		});

		menu.addItem("Drawings", "Y", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				openDrawingsPanel();
			}
		});

		menu.addItem("Toggle Full Screen", "F11", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				viewer.toggleFullScreen();
			}
		});

		menu.addItem("Toggle Agent Overhead Type", "I", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				toggleOverheadType();
			}
		});

		menu.addItem("Toggle Player Numbers", "N", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				togglePlayerNumbers();
			}
		});

		menu.addItem("Toggle Field Overlay", "F", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				toggleFieldOverlay();
			}
		});

		menu.addItem("Toggle Drawings", "T", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				toggleDrawings();
			}
		});

		menu.addItem("Toggle Fouls", "Q", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				toggleFouls();
			}
		});
	}

	private void createCameraMenu(Menu menu)
	{
		menu.addItem("Track Ball", KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				toggleBallTracker();
			}
		});

		menu.addItem(
				"Track Player", KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.SHIFT_MASK), new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e)
					{
						togglePlayerTracker();
					}
				});

		menu.addItem("First Person Vantage", "V", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setRobotVantage(RobotVantageType.FIRST_PERSON);
			}
		});
		menu.addItem("Third Person Vantage", "E", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setRobotVantage(RobotVantageType.THIRD_PERSON);
			}
		});

		menu.add(new JSeparator());

		menu.addItem("Select Ball", "0", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				selectBall();
			}
		});

		menu.addItem("Remove Selection", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				removeSelection();
			}
		});

		menu.addItem("Select Previous Player", KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK),
				new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e)
					{
						cyclePlayers(-1);
					}
				});
		menu.addItem("Select Next Player", KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				cyclePlayers(1);
			}
		});
	}

	protected void loadOverlayVisibilities(Configuration.OverlayVisibility config)
	{
		fieldOverlay.setVisible(config.fieldOverlay);
		foulListOverlay.setVisible(config.foulOverlay);
		setShowNumPlayers(config.numberOfPlayers);
		if (config.playerIDs)
			agentOverheadType = AgentOverheadType.IDS;
	}

	private void renderAnnotations()
	{
		List<BufferedSet<Annotation>> sets = viewer.getDrawings().getAnnotationSets();
		if (sets.size() <= 0)
			return;

		for (BufferedSet<Annotation> set : sets) {
			if (!set.isVisible())
				continue;

			ArrayList<Annotation> annotations = set.getFrontSet();
			for (Annotation a : annotations)
				if (a != null)
					renderBillboardText(a.getText(), new Vec3f(a.getPos()), a.getColor());
		}
	}

	@Override
	public void render(GL2 gl, GLU glu, GLUT glut, Viewport vp)
	{
		// text overlays
		tr.beginRendering(viewer.getScreen().w, viewer.getScreen().h);
		if (viewer.getDrawings().isVisible())
			// Render annotations before other things so that screen overlays may be later rendered
			// on top of the annotations
			renderAnnotations();
		if (agentOverheadType != AgentOverheadType.NONE) {
			renderAgentOverheads(viewer.getWorldModel().getLeftTeam());
			renderAgentOverheads(viewer.getWorldModel().getRightTeam());
		}
		// draw number of agents on each team
		if (showNumPlayers) {
			Team lt = viewer.getWorldModel().getLeftTeam();
			Color outlineColor = new Color(0, 0, 0, 0.5f);
			tr.drawWithOutline(formatNumTeamPlayers(lt), 10, 10, Color.white, outlineColor);

			Team rt = viewer.getWorldModel().getRightTeam();
			String s = formatNumTeamPlayers(rt);
			tr.drawWithOutline(s, (int) (vp.w - tr.getBounds(s).getWidth() - 10), 10, Color.white, outlineColor);
		}
		tr.endRendering();

		vp.apply(gl);
		if (textOverlays.size() > 0)
			renderTextOverlays(vp.w, vp.h);

		// Render screen overlays last so that they may cover text and won't have other text
		// accidentally drawn over them
		for (Screen overlay : overlays)
			if (overlay.isVisible())
				overlay.render(gl, glu, glut, vp);
	}

	private String formatNumTeamPlayers(Team team)
	{
		return String.format("%s : %d", team.getName(), team.getAgents().size());
	}

	private void renderAgentOverheads(Team team)
	{
		ISelectable selected = viewer.getWorldModel().getSelectedObject();

		for (int i = 0; i < team.getAgents().size(); i++) {
			Agent a = team.getAgents().get(i);
			BoundingBox b = a.getBoundingBox();
			if (b == null)
				continue;
			Vec3f p = b.getCenter();
			p.y += 0.6f;
			String text = "" + a.getID();

			AgentAnnotation aa = a.getAnnotation();
			if (aa != null && agentOverheadType == AgentOverheadType.ANNOTATIONS) {
				renderBillboardText(aa.getText(), p, aa.getColor());
			} else if (agentOverheadType == AgentOverheadType.IDS) {
				float[] color;
				if (selected != null && selected == a) {
					color = new float[] {1, 1, 1, 1};
				} else
					color = team.getColorMaterial().getDiffuse();
				renderBillboardText(text, p, color);
			}
		}
	}

	protected void renderBillboardText(String text, Vec3f pos3D, float[] color)
	{
		Camera3D camera = viewer.getRenderer().getVantage();
		Vec3f screenPos = camera.project(pos3D, viewer.getScreen());
		int x = (int) (screenPos.x - tr.getBounds(text).getWidth() / 2);
		int y = (int) screenPos.y;

		if (screenPos.z > 1)
			return;

		tr.drawWithShadow(text, x, y, SwingUtil.toColor(color), Color.black);
	}

	private void renderTextOverlays(int w, int h)
	{
		overlayTextRenderer.beginRendering(w, h);
		for (int i = 0; i < textOverlays.size(); i++) {
			TextOverlay overlay = textOverlays.get(i);
			if (overlay.isExpired()) {
				textOverlays.remove(i);
				i--;
			} else {
				overlay.render(overlayTextRenderer, w, h);
			}
		}
		overlayTextRenderer.endRendering();
	}

	@Override
	public void setEnabled(GLCanvas canvas, boolean enabled)
	{
		if (enabled) {
			canvas.addKeyListener(this);
			canvas.addMouseListener(this);
			canvas.addMouseMotionListener(this);
			canvas.addMouseWheelListener(this);
			viewer.getUI().getCameraControl().attachToCanvas(canvas);
		} else {
			canvas.removeKeyListener(this);
			canvas.removeMouseListener(this);
			canvas.removeMouseMotionListener(this);
			canvas.removeMouseWheelListener(this);
			viewer.getUI().getCameraControl().detachFromCanvas(canvas);
		}

		for (Screen overlay : overlays)
			overlay.setEnabled(canvas, enabled);
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		int keyCode = e.getKeyCode();
		if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F11 && e.isControlDown())
			selectPlayer(keyCode - KeyEvent.VK_F1 + 1, !e.isShiftDown());

		switch (keyCode) {
		case KeyEvent.VK_SPACE:
			if (e.isShiftDown())
				togglePlayerTracker();
			else
				toggleBallTracker();
			break;
		case KeyEvent.VK_F11:
			if (!e.isControlDown())
				viewer.toggleFullScreen();
			break;
		case KeyEvent.VK_F:
			if (e.isControlDown())
				viewer.toggleFullScreen();
			else
				toggleFieldOverlay();
			break;
		case KeyEvent.VK_ENTER:
			if (e.isAltDown())
				viewer.toggleFullScreen();
			break;
		case KeyEvent.VK_F1:
			if (!e.isControlDown())
				openHelp();
			break;
		case KeyEvent.VK_0:
		case KeyEvent.VK_NUMPAD0:
			selectBall();
			break;
		case KeyEvent.VK_ESCAPE:
			removeSelection();
			break;
		case KeyEvent.VK_V:
			setRobotVantage(RobotVantageType.FIRST_PERSON);
			break;
		case KeyEvent.VK_E:
			setRobotVantage(RobotVantageType.THIRD_PERSON);
			break;
		case KeyEvent.VK_I:
			toggleOverheadType();
			break;
		case KeyEvent.VK_N:
			togglePlayerNumbers();
			break;
		case KeyEvent.VK_W:
		case KeyEvent.VK_UP:
			changeFOV(-1);
			break;
		case KeyEvent.VK_S:
		case KeyEvent.VK_DOWN:
			changeFOV(1);
			break;
		case KeyEvent.VK_TAB:
			cyclePlayers(e.isShiftDown() ? -1 : 1);
			break;
		case KeyEvent.VK_T:
			if (!e.isShiftDown())
				toggleDrawings();
			break;
		case KeyEvent.VK_Y:
			openDrawingsPanel();
			break;
		case KeyEvent.VK_Q:
			toggleFouls();
			break;
		}
	}

	private Agent getTrackedPlayer()
	{
		Agent result = null;

		List<Agent> leftTeam = viewer.getWorldModel().getLeftTeam().getAgents();
		if (leftTeam.size() > 0)
			result = leftTeam.get(0);
		List<Agent> rightTeam = viewer.getWorldModel().getRightTeam().getAgents();
		if (rightTeam.size() > 0)
			result = rightTeam.get(0);

		ISelectable selection = viewer.getWorldModel().getSelectedObject();
		if (selection instanceof Agent)
			result = (Agent) selection;

		// players usually beam right after they connect, avoid camera jumps
		if (result != null && result.getAge() < 50) {
			return null;
		}
		return result;
	}

	private void togglePlayerTracker()
	{
		switchTrackerCamera(getTrackedPlayer(), TrackerCameraType.PLAYER);
	}

	private void toggleBallTracker()
	{
		switchTrackerCamera(viewer.getWorldModel().getBall(), TrackerCameraType.BALL);
	}

	private void switchTrackerCamera(ISelectable target, TrackerCameraType type)
	{
		TargetTrackerCamera camera = viewer.getUI().getTrackerCamera();
		if (camera.isEnabled() && trackerCameraType == type) {
			type = TrackerCameraType.NONE;
		}

		if (type == TrackerCameraType.NONE) {
			camera.setEnabled(false);
			camera.setTarget(null);
			trackerCameraType = TrackerCameraType.NONE;
		} else {
			camera.setEnabled(true);
			camera.setTarget(target);
			trackerCameraType = type;
		}
	}

	private void selectBall()
	{
		viewer.getWorldModel().setSelectedObject(viewer.getWorldModel().getBall());
	}

	private void removeSelection()
	{
		viewer.getWorldModel().setSelectedObject(null);
	}

	private void openHelp()
	{
		viewer.getUI().getShortcutHelpPanel().showFrame(viewer.getFrame());
	}

	private void openDrawingsPanel()
	{
		viewer.getUI().getShapeSetPanel().showFrame(viewer.getFrame());
	}

	private void toggleOverheadType()
	{
		nextAgentOverheadType();
		if (agentOverheadType == AgentOverheadType.ANNOTATIONS &&
				!teamHasAnnotations(viewer.getWorldModel().getLeftTeam()) &&
				!teamHasAnnotations(viewer.getWorldModel().getRightTeam()))
			nextAgentOverheadType();
	}

	private void togglePlayerNumbers()
	{
		setShowNumPlayers(!showNumPlayers);
	}

	private void toggleFieldOverlay()
	{
		fieldOverlay.setVisible(!fieldOverlay.isVisible());
	}

	private void toggleDrawings()
	{
		viewer.getDrawings().toggle();
	}

	private void toggleFouls()
	{
		foulListOverlay.setVisible(!foulListOverlay.isVisible());
	}

	private void setShowNumPlayers(boolean showNumPlayers)
	{
		this.showNumPlayers = showNumPlayers;
		fieldOverlay.setyPos(showNumPlayers ? 35 : 10);
	}

	private void selectPlayer(int playerID, boolean leftTeam)
	{
		WorldModel worldModel = viewer.getWorldModel();
		Team team = leftTeam ? worldModel.getLeftTeam() : worldModel.getRightTeam();
		Agent agent = team.getAgentByID(playerID);
		if (agent != null)
			viewer.getWorldModel().setSelectedObject(agent);
	}

	private void cyclePlayers(int direction)
	{
		WorldModel worldModel = viewer.getWorldModel();
		ISelectable selection = worldModel.getSelectedObject();
		if (!(selection instanceof Agent))
			return;

		Agent agent = (Agent) selection;
		if (agent.getTeam().getAgents().size() <= 1)
			return;

		Agent nextAgent = null;
		int nextID = agent.getID();
		do {
			nextID += direction;
			if (nextID > Team.MAX_AGENTS)
				nextID = 0;
			else if (nextID < 0)
				nextID = Team.MAX_AGENTS;

			nextAgent = agent.getTeam().getAgentByID(nextID);
		} while (nextAgent == null && nextID != agent.getID());

		if (nextAgent != null)
			worldModel.setSelectedObject(nextAgent);
	}

	private void nextAgentOverheadType()
	{
		AgentOverheadType[] vals = AgentOverheadType.values();
		agentOverheadType = vals[(agentOverheadType.ordinal() + 1) % vals.length];
	}

	private void changeFOV(int amount)
	{
		if (robotVantage == null)
			return;

		switch (robotVantageType) {
		case FIRST_PERSON:
			firstPersonFOV += amount;
			robotVantage.setFOV(firstPersonFOV);
			break;
		case THIRD_PERSON:
			thirdPersonFOV += amount;
			robotVantage.setFOV(thirdPersonFOV);
			break;
		}
	}

	private boolean teamHasAnnotations(Team team)
	{
		for (Agent agent : team.getAgents()) {
			if (agent.getAnnotation() != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if (robotVantage != null || e.getButton() != MouseEvent.BUTTON1)
			return;

		viewer.getUI().getObjectPicker().updatePickRay(viewer.getScreen(), e.getX(), e.getY());
		if (e.isAltDown()) {
			altClick(e);
		} else {
			boolean handled = false;
			ISelectable selectedObject = viewer.getWorldModel().getSelectedObject();
			if (selectedObject != null) {
				handled = selectedObjectClick(selectedObject, e);
			}

			if (!handled) {
				ISelectable newSelection = viewer.getUI().getObjectPicker().pickObject();
				viewer.getWorldModel().setSelectedObject(newSelection);
			}
		}
	}

	protected boolean selectedObjectClick(ISelectable object, MouseEvent e)
	{
		return false;
	}

	protected void altClick(MouseEvent e)
	{
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		changeFOV(e.getWheelRotation() < 0 ? -1 : 1);
	}

	@Override
	public void gsTimeChanged(GameState gs)
	{
		TargetTrackerCamera camera = viewer.getUI().getTrackerCamera();
		if (trackerCameraType == TrackerCameraType.PLAYER) {
			ISelectable target = camera.getTarget();
			boolean agentExists = viewer.getWorldModel().getLeftTeam().getAgents().contains(target) ||
								  viewer.getWorldModel().getRightTeam().getAgents().contains(target);
			if (target == null || !agentExists) {
				target = getTrackedPlayer();
			}
			camera.setTarget(target);
		}
	}

	@Override
	public void gsMeasuresAndRulesChanged(GameState gs)
	{
	}

	@Override
	public void gsPlayStateChanged(GameState gs)
	{
		if (prevScoreL != -1 && prevScoreR != -1) {
			if (gs.getScoreLeft() != prevScoreL)
				addTeamScoredOverlay(gs, Team.LEFT);
			if (gs.getScoreRight() != prevScoreR)
				addTeamScoredOverlay(gs, Team.RIGHT);
		}

		prevScoreL = gs.getScoreLeft();
		prevScoreR = gs.getScoreRight();
	}

	protected void addTeamScoredOverlay(GameState gs, int team)
	{
		String teamName;
		if (team == Team.LEFT && gs.getTeamLeft() != null) {
			teamName = gs.getTeamLeft();
		} else if (team == Team.RIGHT && gs.getTeamRight() != null) {
			teamName = gs.getTeamRight();
		} else {
			return;
		}

		textOverlays.add(new TextOverlay(
				String.format("Goal %s!", teamName), viewer.getWorldModel(), 4000, new float[] {1, 1, 1, 1}));
	}

	private void setRobotVantage(RobotVantageType type)
	{
		boolean differentType = robotVantageType != type;
		Agent oldAgent = null;

		if (robotVantage != null) {
			oldAgent = robotVantage.getAgent();
			robotVantage.detach();
			robotVantage = null;
			viewer.getRenderer().setVantage(viewer.getUI().getCamera());
			viewer.getUI().getCameraControl().attachToCanvas((GLCanvas) viewer.getCanvas());
			robotVantageType = RobotVantageType.NONE;
		}

		if (type == RobotVantageType.NONE) {
			return;
		}

		ISelectable selected = viewer.getWorldModel().getSelectedObject();
		if (!(selected instanceof Agent)) {
			return;
		}

		Agent agent = (Agent) viewer.getWorldModel().getSelectedObject();
		if (differentType || oldAgent != agent) {
			if (type == RobotVantageType.FIRST_PERSON)
				robotVantage = new RobotVantageFirstPerson(agent, firstPersonFOV);
			else
				robotVantage = new RobotVantageThirdPerson(agent, thirdPersonFOV);
			viewer.getRenderer().setVantage(robotVantage);
			viewer.getUI().getCameraControl().detachFromCanvas((GLCanvas) viewer.getCanvas());
			robotVantageType = type;
		}
	}

	@Override
	public void selectionChanged(ISelectable newSelection)
	{
		if (robotVantage != null) {
			if (newSelection instanceof Agent) {
				setRobotVantage(robotVantageType);
			} else {
				setRobotVantage(RobotVantageType.NONE);
			}
		}

		if (trackerCameraType == TrackerCameraType.PLAYER && newSelection instanceof Agent) {
			viewer.getUI().getTrackerCamera().setTarget(newSelection);
		}
	}

	protected void toggleShowServerSpeed()
	{
		gameStateOverlay.toggleShowServerSpeed();
	}

	@Override
	public void windowResized(Viewer.WindowResizeEvent event)
	{
		for (Screen overlay : overlays)
			overlay.windowResized(event);
	}
}
