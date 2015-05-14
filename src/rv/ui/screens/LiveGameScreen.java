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

package rv.ui.screens;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
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
import rv.comm.drawing.BufferedSet;
import rv.comm.drawing.annotations.AgentAnnotation;
import rv.comm.drawing.annotations.Annotation;
import rv.comm.rcssserver.GameState;
import rv.ui.view.RobotVantageBase;
import rv.ui.view.RobotVantageFirstPerson;
import rv.ui.view.RobotVantageThirdPerson;
import rv.world.ISelectable;
import rv.world.Team;
import rv.world.WorldModel;
import rv.world.objects.Agent;
import rv.world.objects.Ball;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

public class LiveGameScreen extends ViewerScreenBase implements WorldModel.SelectionChangeListener {

    enum AgentOverheadType {
        NONE, ANNOTATIONS, IDS
    }

    enum RobotVantageType {
        NONE, FIRST_PERSON, THIRD_PERSON
    }

    private final ConnectionOverlay connectionOverlay;
    private final List<TextOverlay> textOverlays      = new ArrayList<>();
    private AgentOverheadType       agentOverheadType = AgentOverheadType.ANNOTATIONS;
    private final TextRenderer      tr;
    private final TextRenderer      overlayTextRenderer;
    private RobotVantageBase        robotVantage      = null;
    private RobotVantageType        robotVantageType  = RobotVantageType.NONE;
    private int                     prevScoreL        = -1;
    private int                     prevScoreR        = -1;

    boolean                         showNumPlayers    = false;

    public void removeOverlay(Screen overlay) {
        overlays.remove(overlay);
    }

    public LiveGameScreen(Viewer viewer) {
        super(viewer);
        connectionOverlay = new ConnectionOverlay();

        Font font = new Font("Arial", Font.BOLD, 16);
        tr = new TextRenderer(font, true, false);
        overlayTextRenderer = new TextRenderer(new Font("Arial", Font.PLAIN, 48), true, false);

        viewer.getWorldModel().getGameState().addListener(this);
        viewer.getWorldModel().addSelectionChangeListener(this);
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

    private void renderAnnotations() {
        List<BufferedSet<Annotation>> sets = viewer.getDrawings().getAnnotationSets();
        if (sets.size() > 0) {
            for (BufferedSet<Annotation> set : sets) {
                if (set.isVisible()) {
                    ArrayList<Annotation> annotations = set.getFrontSet();
                    for (Annotation a : annotations) {
                        if (a != null) {
                            renderBillboardText(a.getText(), new Vec3f(a.getPos()), a.getColor());
                        }
                    }
                }
            }
        }
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
            tr.setColor(Color.white);
            tr.draw(String.format("%s : %d", lt.getName(), lt.getAgents().size()), 10, 10);
            Team rt = viewer.getWorldModel().getRightTeam();
            String s = String.format("%s : %d", rt.getName(), rt.getAgents().size());
            tr.draw(s, (int) (vp.w - tr.getBounds(s).getWidth() - 10), 10);
        }
        if (viewer.getDrawings().isVisible())
            renderAnnotations();
        tr.endRendering();

        if (!viewer.getNetManager().getServer().isConnected())
            connectionOverlay.render(gl, glu, glut, vp);
        else
            gsOverlay.render(gl, glu, glut, vp);

        super.render(gl, glu, glut, vp);

        vp.apply(gl);
        if (textOverlays.size() > 0)
            renderTextOverlays(vp.w, vp.h);
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

    private void renderBillboardText(String text, Vec3f pos3D, float[] color) {
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

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);

        int keyCode = e.getKeyCode();
        if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F11 && e.isControlDown())
            togglePlayerSelection(keyCode - KeyEvent.VK_F1 + 1, !e.isAltDown());

        switch (e.getKeyCode()) {
        case KeyEvent.VK_X:
            if (e.isShiftDown())
                viewer.getNetManager().getServer().killServer();
            break;
        case KeyEvent.VK_K:
            resetTimeIfExpired();
            viewer.getNetManager().getServer().kickOff(true);
            break;
        case KeyEvent.VK_J:
            resetTimeIfExpired();
            viewer.getNetManager().getServer().kickOff(false);
            break;
        case KeyEvent.VK_P:
            viewer.getUI().getShapeSetPanel().showFrame(viewer.getFrame());
            break;
        case KeyEvent.VK_O:
            if (viewer.getWorldModel().getGameState() != null
                    && viewer.getWorldModel().getGameState().getPlayModes() != null) {
                PlaymodeOverlay pmo = new PlaymodeOverlay(viewer, this);
                overlays.add(pmo);
                pmo.setEnabled((GLCanvas) viewer.getCanvas(), true);
            }
            break;
        case KeyEvent.VK_T:
            viewer.getDrawings().toggle();
            break;
        case KeyEvent.VK_I:
            AgentOverheadType[] vals = AgentOverheadType.values();
            agentOverheadType = vals[(agentOverheadType.ordinal() + 1) % vals.length];
            break;
        case KeyEvent.VK_V:
            setRobotVantage(RobotVantageType.FIRST_PERSON);
            break;
        case KeyEvent.VK_E:
            setRobotVantage(RobotVantageType.THIRD_PERSON);
            break;
        case KeyEvent.VK_C:
            if (!viewer.getNetManager().getServer().isConnected()) {
                viewer.getNetManager().getServer().connect();
            }
            break;
        case KeyEvent.VK_L:
            resetTimeIfExpired();
            viewer.getNetManager().getServer().freeKick(true);
            break;
        case KeyEvent.VK_R:
            if (e.isShiftDown())
                viewer.getNetManager().getServer().resetTime();
            else {
                resetTimeIfExpired();
                viewer.getNetManager().getServer().freeKick(false);
            }
            break;
        case KeyEvent.VK_N:
            showNumPlayers = !showNumPlayers;
            break;
        case KeyEvent.VK_ESCAPE:
            viewer.getWorldModel().setSelectedObject(null);
            break;
        case KeyEvent.VK_U:
            viewer.getNetManager().getServer().requestFullState();
            break;
        }
    }

    @Override
    protected void bPressed() {
        viewer.getNetManager().getServer().dropBall();
    }

    private void togglePlayerSelection(int playerID, boolean leftTeam) {
        WorldModel worldModel = viewer.getWorldModel();
        Team team = leftTeam ? worldModel.getLeftTeam() : worldModel.getRightTeam();
        Agent agent = team.getAgentByID(playerID);
        if (agent != null)
            viewer.getWorldModel().setSelectedObject(agent);
    }

    private void resetTimeIfExpired() {
        // changing the play mode doesn't have any effect if the game has ended
        float gameTime = viewer.getWorldModel().getGameState().getHalfTime() * 2;
        if (viewer.getWorldModel().getGameState().getTime() >= gameTime)
            viewer.getNetManager().getServer().resetTime();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (robotVantage == null && viewer.getNetManager().getServer().isConnected()) {
            super.mouseClicked(e);
        }
    }

    @Override
    protected boolean selectedObjectClick(ISelectable object, MouseEvent e) {
        if (e.isControlDown()) {
            Vec3f fieldPos = viewer.getUI().getObjectPicker().pickField();
            moveSelection(fieldPos);
            return true;
        }
        return false;
    }

    private void moveSelection(Vec3f pos) {
        if (pos != null) {
            ISelectable selected = viewer.getWorldModel().getSelectedObject();
            pos.y = selected.getBoundingBox().getCenter().y + 0.1f;
            Vec3f serverPos = WorldModel.COORD_TFN.transform(pos);

            if (selected instanceof Ball) {
                serverPos.z = viewer.getWorldModel().getGameState().getBallRadius();
                viewer.getNetManager().getServer().moveBall(serverPos);
            } else if (selected instanceof Agent) {
                Agent a = (Agent) selected;
                boolean leftTeam = a.getTeam().getID() == Team.LEFT;
                viewer.getNetManager().getServer().moveAgent(serverPos, leftTeam, a.getID());
            }
        }
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
    public void gsPlayStateChanged(GameState gs) {
        if (prevScoreL != -1 && prevScoreR != -1) {
            if (gs.getScoreLeft() > prevScoreL && gs.getTeamLeft() != null)
                textOverlays.add(new TextOverlay(String.format("Goal %s!", gs.getTeamLeft()), 4000,
                        new float[] { 1, 1, 1, 1 }));
            if (gs.getScoreRight() > prevScoreR && gs.getTeamRight() != null)
                textOverlays.add(new TextOverlay(String.format("Goal %s!", gs.getTeamRight()),
                        4000, new float[] { 1, 1, 1, 1 }));
        }

        prevScoreL = gs.getScoreLeft();
        prevScoreR = gs.getScoreRight();
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
