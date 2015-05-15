package rv.ui.screens;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import js.jogl.view.Camera3D;
import js.jogl.view.Viewport;
import js.math.BoundingBox;
import js.math.vector.Vec3f;
import rv.Viewer;
import rv.comm.drawing.annotations.AgentAnnotation;
import rv.comm.rcssserver.GameState;
import rv.ui.view.RobotVantageBase;
import rv.ui.view.RobotVantageFirstPerson;
import rv.ui.view.RobotVantageThirdPerson;
import rv.world.ISelectable;
import rv.world.Team;
import rv.world.WorldModel;
import rv.world.objects.Agent;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

public abstract class ViewerScreenBase implements Screen, KeyListener, MouseListener,
        MouseMotionListener, GameState.GameStateChangeListener, WorldModel.SelectionChangeListener {

    enum AgentOverheadType {
        NONE, ANNOTATIONS, IDS
    }

    enum RobotVantageType {
        NONE, FIRST_PERSON, THIRD_PERSON
    }

    protected final Viewer          viewer;

    private final Field2DOverlay    fieldOverlay;
    protected final List<Screen>    overlays          = new ArrayList<>();

    protected final TextRenderer    overlayTextRenderer;
    private final List<TextOverlay> textOverlays      = new ArrayList<>();

    private RobotVantageBase        robotVantage      = null;
    private RobotVantageType        robotVantageType  = RobotVantageType.NONE;

    private AgentOverheadType       agentOverheadType = AgentOverheadType.ANNOTATIONS;
    protected final TextRenderer    tr;

    private int                     prevScoreL        = -1;
    private int                     prevScoreR        = -1;
    private boolean                 showNumPlayers    = false;

    public ViewerScreenBase(Viewer viewer) {
        this.viewer = viewer;
        overlays.add(new GameStateOverlay(viewer));
        fieldOverlay = new Field2DOverlay(viewer.getWorldModel());
        overlays.add(fieldOverlay);

        overlayTextRenderer = new TextRenderer(new Font("Arial", Font.PLAIN, 48), true, false);
        Font font = new Font("Arial", Font.BOLD, 16);
        tr = new TextRenderer(font, true, false);
    }

    @Override
    public void render(GL2 gl, GLU glu, GLUT glut, Viewport vp) {
        // text overlays
        tr.beginRendering(viewer.getScreen().w, viewer.getScreen().h);
        if (agentOverheadType != AgentOverheadType.NONE) {
            renderAgentOverheads(viewer.getWorldModel().getLeftTeam());
            renderAgentOverheads(viewer.getWorldModel().getRightTeam());
        }
        // draw number of agents on each team
        if (showNumPlayers) {
            Team lt = viewer.getWorldModel().getLeftTeam();
            renderOutlinedText(formatNumTeamPlayers(lt), 10, 10);

            Team rt = viewer.getWorldModel().getRightTeam();
            String s = formatNumTeamPlayers(rt);
            renderOutlinedText(s, (int) (vp.w - tr.getBounds(s).getWidth() - 10), 10);
        }
        tr.endRendering();

        for (Screen overlay : overlays)
            overlay.render(gl, glu, glut, vp);

        vp.apply(gl);
        if (textOverlays.size() > 0)
            renderTextOverlays(vp.w, vp.h);
    }

    private String formatNumTeamPlayers(Team team) {
        return String.format("%s : %d", team.getName(), team.getAgents().size());
    }

    private void renderOutlinedText(String s, int x, int y) {
        int delta = 1;
        tr.setColor(0, 0, 0, 0.5f);
        tr.draw(s, x - delta, y - delta);
        tr.draw(s, x - delta, y + delta);
        tr.draw(s, x + delta, y - delta);
        tr.draw(s, x + delta, y + delta);
        tr.draw(s, x + delta, y);
        tr.draw(s, x - delta, y);
        tr.draw(s, x, y + delta);
        tr.draw(s, x, y - delta);

        tr.setColor(Color.white);
        tr.draw(s, x, y);
    }

    private void renderAgentOverheads(Team team) {
        ISelectable selected = viewer.getWorldModel().getSelectedObject();

        for (int i = 0; i < team.getAgents().size(); i++) {
            Agent a = team.getAgents().get(i);
            BoundingBox b = a.getBoundingBox();
            if (b == null)
                continue;
            Vec3f p = b.getCenter();
            p.y = 1;
            String text = "" + a.getID();

            AgentAnnotation aa = a.getAnnotation();
            if (aa != null && agentOverheadType == AgentOverheadType.ANNOTATIONS) {
                renderBillboardText(aa.getText(), p, aa.getColor());
            } else if (agentOverheadType == AgentOverheadType.IDS) {
                float[] color;
                if (selected != null && selected == a) {
                    color = new float[] { 1, 1, 1, 1 };
                } else
                    color = team.getTeamMaterial().getDiffuse();
                renderBillboardText(text, p, color);
            }
        }
    }

    protected void renderBillboardText(String text, Vec3f pos3D, float[] color) {
        Camera3D camera = viewer.getUI().getCamera();
        Vec3f screenPos = camera.project(pos3D, viewer.getScreen());
        int x = (int) (screenPos.x - tr.getBounds(text).getWidth() / 2);
        int y = (int) screenPos.y;

        if (screenPos.z > 1)
            return;

        tr.setColor(0, 0, 0, 1);
        tr.draw(text, x - 1, y - 1);
        if (color.length == 4)
            tr.setColor(color[0], color[1], color[2], color[3]);
        else
            tr.setColor(color[0], color[1], color[2], 1);
        tr.draw(text, x, y);
    }

    private void renderTextOverlays(int w, int h) {
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
    public void setEnabled(GLCanvas canvas, boolean enabled) {
        if (enabled) {
            canvas.addKeyListener(this);
            canvas.addMouseListener(this);
            canvas.addMouseMotionListener(this);
            viewer.getUI().getCameraControl().attachToCanvas(canvas);
            viewer.getWorldModel().getGameState().addListener(this);
            viewer.getWorldModel().addSelectionChangeListener(this);
        } else {
            canvas.removeKeyListener(this);
            canvas.removeMouseListener(this);
            canvas.removeMouseMotionListener(this);
            viewer.getUI().getCameraControl().detachFromCanvas(canvas);
            viewer.getWorldModel().getGameState().removeListener(this);
            viewer.getWorldModel().removeSelectionChangeListener(this);
        }

        for (Screen overlay : overlays)
            overlay.setEnabled(canvas, enabled);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F11 && e.isControlDown())
            selectPlayer(keyCode - KeyEvent.VK_F1 + 1, !e.isAltDown());

        switch (keyCode) {
        case KeyEvent.VK_Q:
            viewer.shutdown();
            break;
        case KeyEvent.VK_SPACE:
            viewer.getUI().getBallTracker().toggleEnabled();
            break;
        case KeyEvent.VK_F11:
            if (!e.isControlDown())
                viewer.toggleFullScreen();
            break;
        case KeyEvent.VK_F:
            if (e.isControlDown()) {
                viewer.toggleFullScreen();
            } else {
                fieldOverlay.setVisible(!fieldOverlay.isVisible());
            }
            break;
        case KeyEvent.VK_ENTER:
            if (e.isAltDown()) {
                viewer.toggleFullScreen();
            }
            break;
        case KeyEvent.VK_F1:
            if (!e.isControlDown())
                viewer.getUI().getShortcutHelpPanel().showFrame(viewer.getFrame());
            break;
        case KeyEvent.VK_B:
            if (e.isControlDown()) {
                viewer.getWorldModel().setSelectedObject(viewer.getWorldModel().getBall());
            } else {
                bPressed();
            }
            break;
        case KeyEvent.VK_ESCAPE:
            viewer.getWorldModel().setSelectedObject(null);
            break;
        case KeyEvent.VK_V:
            setRobotVantage(RobotVantageType.FIRST_PERSON);
            break;
        case KeyEvent.VK_E:
            setRobotVantage(RobotVantageType.THIRD_PERSON);
            break;
        case KeyEvent.VK_I:
            AgentOverheadType[] vals = AgentOverheadType.values();
            agentOverheadType = vals[(agentOverheadType.ordinal() + 1) % vals.length];
            break;
        case KeyEvent.VK_N:
            showNumPlayers = !showNumPlayers;
            fieldOverlay.setyPos(showNumPlayers ? 35 : 10);
            break;
        }
    }

    protected void bPressed() {

    }

    private void selectPlayer(int playerID, boolean leftTeam) {
        WorldModel worldModel = viewer.getWorldModel();
        Team team = leftTeam ? worldModel.getLeftTeam() : worldModel.getRightTeam();
        Agent agent = team.getAgentByID(playerID);
        if (agent != null)
            viewer.getWorldModel().setSelectedObject(agent);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (robotVantage == null && e.getButton() == MouseEvent.BUTTON1) {
            viewer.getUI().getObjectPicker().updatePickRay(viewer.getScreen(), e.getX(), e.getY());

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

    protected boolean selectedObjectClick(ISelectable object, MouseEvent e) {
        return false;
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void gsTimeChanged(GameState gs) {

    }

    @Override
    public void gsMeasuresAndRulesChanged(GameState gs) {

    }

    @Override
    public void gsPlayStateChanged(GameState gs) {
        if (prevScoreL != -1 && prevScoreR != -1) {
            if (gs.getScoreLeft() > prevScoreL && gs.getTeamLeft() != null)
                addTeamScoredOverlay(gs.getTeamLeft());
            if (gs.getScoreRight() > prevScoreR && gs.getTeamRight() != null)
                addTeamScoredOverlay(gs.getTeamRight());
        }

        prevScoreL = gs.getScoreLeft();
        prevScoreR = gs.getScoreRight();
    }

    private void addTeamScoredOverlay(String teamName) {
        textOverlays.add(new TextOverlay(String.format("Goal %s!", teamName), 4000, new float[] {
                1, 1, 1, 1 }));
    }

    private void setRobotVantage(RobotVantageType type) {
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
                robotVantage = new RobotVantageFirstPerson(agent);
            else
                robotVantage = new RobotVantageThirdPerson(agent);
            viewer.getRenderer().setVantage(robotVantage);
            viewer.getUI().getCameraControl().detachFromCanvas((GLCanvas) viewer.getCanvas());
            robotVantageType = type;
        }
    }

    @Override
    public void selectionChanged(ISelectable newSelection) {
        if (robotVantage != null) {
            if (newSelection instanceof Agent) {
                setRobotVantage(robotVantageType);
            } else {
                setRobotVantage(RobotVantageType.NONE);
            }
        }
    }
}
