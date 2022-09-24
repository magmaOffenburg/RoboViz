package rv.ui.screens;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import jsgl.jogl.view.Camera3D;
import jsgl.jogl.view.Viewport;
import jsgl.math.BoundingBox;
import jsgl.math.vector.Vec3f;
import org.magmaoffenburg.roboviz.configuration.Config.Graphics;
import org.magmaoffenburg.roboviz.configuration.Config.OverlayVisibility;
import org.magmaoffenburg.roboviz.gui.MainWindow;
import org.magmaoffenburg.roboviz.rendering.CameraController;
import org.magmaoffenburg.roboviz.rendering.Renderer;
import rv.comm.drawing.BufferedSet;
import rv.comm.drawing.annotations.AgentAnnotation;
import rv.comm.drawing.annotations.Annotation;
import rv.comm.rcssserver.GameState;
import rv.ui.view.RobotVantageBase;
import rv.ui.view.RobotVantageFirstPerson;
import rv.ui.view.RobotVantageThirdPerson;
import rv.ui.view.TargetTrackerCamera;
import rv.util.WindowResizeEvent;
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

	public enum RobotVantageType
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

	public ViewerScreenBase()
	{
		gameStateOverlay = new GameStateOverlay();
		overlays.add(gameStateOverlay);
		fieldOverlay = new Field2DOverlay(Renderer.world);
		overlays.add(fieldOverlay);
		foulListOverlay = new FoulListOverlay();
		overlays.add(foulListOverlay);

		overlayTextRenderer = new BorderTextRenderer(new Font("Arial", Font.PLAIN, 48), true, false);
		Font font = new Font("Arial", Font.BOLD, 16);
		tr = new BorderTextRenderer(font, true, false);

		Graphics config = Graphics.INSTANCE;
		firstPersonFOV = config.getFirstPersonFOV();
		thirdPersonFOV = config.getThirdPersonFOV();

		Renderer.world.getGameState().addListener(this);
		Renderer.world.addSelectionChangeListener(this);
		// viewer.addWindowResizeListener(this); // TODO

		loadOverlayVisibilities();
	}

	protected void loadOverlayVisibilities()
	{
		fieldOverlay.setVisible(OverlayVisibility.INSTANCE.getFieldOverlay());
		foulListOverlay.setVisible(OverlayVisibility.INSTANCE.getFoulOverlay());
		setShowNumPlayers(OverlayVisibility.INSTANCE.getNumberOfPlayers());
		if (OverlayVisibility.INSTANCE.getPlayerIDs())
			agentOverheadType = AgentOverheadType.IDS;
	}

	private void renderAnnotations()
	{
		List<BufferedSet<Annotation>> sets = Renderer.drawings.getAnnotationSets();
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
		tr.beginRendering(Renderer.instance.getScreen().w, Renderer.instance.getScreen().h);
		if (Renderer.drawings.isVisible())
			// Render annotations before other things so that screen overlays may be later rendered
			// on top of the annotations
			renderAnnotations();
		if (agentOverheadType != AgentOverheadType.NONE) {
			renderAgentOverheads(Renderer.world.getLeftTeam());
			renderAgentOverheads(Renderer.world.getRightTeam());
		}
		// draw number of agents on each team
		if (showNumPlayers) {
			Team lt = Renderer.world.getLeftTeam();
			Color outlineColor = new Color(0, 0, 0, 0.5f);
			tr.drawWithOutline(formatNumTeamPlayers(lt), 10, 10, Color.white, outlineColor);

			Team rt = Renderer.world.getRightTeam();
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
		return String.format("%s : %d", team.getDisplayName(), team.getAgents().size());
	}

	private void renderAgentOverheads(Team team)
	{
		ISelectable selected = Renderer.world.getSelectedObject();

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
		Camera3D camera = CameraController.vantage;
		Vec3f screenPos = camera.project(pos3D, Renderer.instance.getScreen());
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
			CameraController.cameraController.attachToCanvas(canvas);
		} else {
			canvas.removeKeyListener(this);
			canvas.removeMouseListener(this);
			canvas.removeMouseMotionListener(this);
			canvas.removeMouseWheelListener(this);
			CameraController.cameraController.detachFromCanvas(canvas);
		}

		for (Screen overlay : overlays)
			overlay.setEnabled(canvas, enabled);
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
	}

	private Agent getTrackedPlayer()
	{
		Agent result = null;

		List<Agent> leftTeam = Renderer.world.getLeftTeam().getAgents();
		if (leftTeam.size() > 0)
			result = leftTeam.get(0);
		List<Agent> rightTeam = Renderer.world.getRightTeam().getAgents();
		if (rightTeam.size() > 0)
			result = rightTeam.get(0);

		ISelectable selection = Renderer.world.getSelectedObject();
		if (selection instanceof Agent)
			result = (Agent) selection;

		// players usually beam right after they connect, avoid camera jumps
		if (result != null && result.getAge() < 50) {
			return null;
		}
		return result;
	}

	public void togglePlayerTracker()
	{
		switchTrackerCamera(getTrackedPlayer(), TrackerCameraType.PLAYER);
	}

	public void toggleBallTracker()
	{
		switchTrackerCamera(Renderer.world.getBall(), TrackerCameraType.BALL);
	}

	public void toggleOverheadType()
	{
		nextAgentOverheadType();
		if (agentOverheadType == AgentOverheadType.ANNOTATIONS && !teamHasAnnotations(Renderer.world.getLeftTeam()) &&
				!teamHasAnnotations(Renderer.world.getRightTeam()))
			nextAgentOverheadType();
	}

	public void toggleShowServerSpeed()
	{
		gameStateOverlay.toggleShowServerSpeed();
	}

	private void switchTrackerCamera(ISelectable target, TrackerCameraType type)
	{
		TargetTrackerCamera camera = CameraController.trackerCamera;
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

	public void setShowNumPlayers(boolean showNumPlayers)
	{
		this.showNumPlayers = showNumPlayers;
		fieldOverlay.setyPos(showNumPlayers ? 35 : 10);
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
		default:
			break;
		}
	}

	private void nextAgentOverheadType()
	{
		AgentOverheadType[] vals = AgentOverheadType.values();
		agentOverheadType = vals[(agentOverheadType.ordinal() + 1) % vals.length];
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

		CameraController.objectPicker.updatePickRay(Renderer.instance.getScreen(), e.getX(), e.getY());
		if (e.isAltDown()) {
			altClick(e);
		} else {
			boolean handled = false;
			ISelectable selectedObject = Renderer.world.getSelectedObject();
			if (selectedObject != null) {
				handled = selectedObjectClick(selectedObject, e);
			}

			if (!handled) {
				ISelectable newSelection = CameraController.objectPicker.pickObject();
				Renderer.world.setSelectedObject(newSelection);
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
		TargetTrackerCamera camera = CameraController.trackerCamera;
		if (trackerCameraType == TrackerCameraType.PLAYER) {
			ISelectable target = camera.getTarget();
			boolean agentExists = Renderer.world.getLeftTeam().getAgents().contains(target) ||
								  Renderer.world.getRightTeam().getAgents().contains(target);
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

		textOverlays.add(new TextOverlay(String.format("Goal %s!", teamName), Renderer.world, 4000));
	}

	public void setRobotVantage(RobotVantageType type)
	{
		boolean differentType = robotVantageType != type;
		Agent oldAgent = null;

		if (robotVantage != null) {
			oldAgent = robotVantage.getAgent();
			robotVantage.detach();
			robotVantage = null;
			CameraController.vantage = CameraController.fpCamera;
			CameraController.cameraController.attachToCanvas(MainWindow.glCanvas);
			robotVantageType = RobotVantageType.NONE;
		}

		if (type == RobotVantageType.NONE) {
			return;
		}

		ISelectable selected = Renderer.world.getSelectedObject();
		if (!(selected instanceof Agent)) {
			return;
		}

		Agent agent = (Agent) Renderer.world.getSelectedObject();
		if (differentType || oldAgent != agent) {
			if (type == RobotVantageType.FIRST_PERSON)
				robotVantage = new RobotVantageFirstPerson(agent, firstPersonFOV);
			else
				robotVantage = new RobotVantageThirdPerson(agent, thirdPersonFOV);
			CameraController.vantage = robotVantage;
			CameraController.cameraController.detachFromCanvas(MainWindow.glCanvas);
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
			CameraController.trackerCamera.setTarget(newSelection);
		}
	}

	@Override
	public void windowResized(WindowResizeEvent event)
	{
		for (Screen overlay : overlays)
			overlay.windowResized(event);
	}

	public Field2DOverlay getFieldOverlay()
	{
		return fieldOverlay;
	}

	public FoulListOverlay getFoulListOverlay()
	{
		return foulListOverlay;
	}

	public List<TextOverlay> getTextOverlays()
	{
		return textOverlays;
	}

	public boolean isShowNumPlayers()
	{
		return showNumPlayers;
	}

	public AgentOverheadType getAgentOverheadType()
	{
		return agentOverheadType;
	}
}
