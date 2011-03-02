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
import js.jogl.view.FPCamera;
import js.jogl.view.OrbitCamera;
import js.jogl.view.Viewport;
import js.math.BoundingBox;
import js.math.vector.Vec2f;
import js.math.vector.Vec3f;
import rv.Viewer;
import rv.comm.drawing.BufferedSet;
import rv.comm.drawing.annotations.AgentAnnotation;
import rv.comm.drawing.annotations.Annotation;
import rv.comm.rcssserver.GameState;
import rv.comm.rcssserver.ServerComm;
import rv.comm.rcssserver.GameState.GameStateChangeListener;
import rv.comm.rcssserver.ServerComm.ServerChangeListener;
import rv.comm.rcssserver.scenegraph.SceneGraph;
import rv.comm.rcssserver.scenegraph.SceneGraph.SceneGraphListener;
import rv.ui.CameraSetting;
import rv.ui.view.RobotVantage;
import rv.world.ISelectable;
import rv.world.Team;
import rv.world.WorldModel;
import rv.world.objects.Agent;
import rv.world.objects.Ball;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

public class LiveGameScreen implements Screen, KeyListener, MouseListener,
        MouseMotionListener, ServerChangeListener, SceneGraphListener,
        GameStateChangeListener {
    
    enum AgentOverheadType { NONE, ANNOTATIONS, IDS }

    private Viewer            viewer;
    private GameStateOverlay  gsOverlay;
    private ConnectionOverlay connectionOverlay;
    private Field2DOverlay    fieldOverlay;
    private List<Screen>      overlays       = new ArrayList<Screen>();
    private List<TextOverlay> textOverlays   = new ArrayList<TextOverlay>();
    private boolean           moveObjectMode = false;
    private AgentOverheadType agentOverheadType = AgentOverheadType.ANNOTATIONS;
    private TextRenderer      tr;
    private TextRenderer      overlayTextRenderer;
    private RobotVantage      robotVantage   = null;
    private int               prevScoreL     = -1;
    private int               prevScoreR     = -1;
    
    private boolean shift = false;

    public void removeOverlay(Screen overlay) {
        overlays.remove(overlay);
    }

    public LiveGameScreen(Viewer viewer) {
        this.viewer = viewer;
        gsOverlay = new GameStateOverlay(viewer);
        connectionOverlay = new ConnectionOverlay();

        Font font = new Font("Arial", Font.BOLD, 16);
        tr = new TextRenderer(font, true, false);
        overlayTextRenderer = new TextRenderer(new Font("Arial", Font.PLAIN, 48),
                true, false);

        viewer.getNetManager().getServer().addChangeListener(this);
        viewer.getWorldModel().addSceneGraphListener(this);

        fieldOverlay = new Field2DOverlay(viewer.getWorldModel());
        overlays.add(fieldOverlay);

        viewer.getWorldModel().getGameState().addListener(this);
    }

    @Override
    public void setEnabled(GLCanvas canvas, boolean enabled) {
        if (enabled) {
            canvas.addKeyListener(this);
            canvas.addMouseListener(this);
            canvas.addMouseMotionListener(this);
            viewer.getUI().getCameraControl().attachToCanvas(canvas);
        } else {
            canvas.removeKeyListener(this);
            canvas.removeMouseListener(this);
            canvas.removeMouseMotionListener(this);
            viewer.getUI().getCameraControl().detachFromCanvas(canvas);
        }
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
        if (viewer.getDrawings().isVisible())
            renderAnnotations();
        tr.endRendering();

        if (!viewer.getNetManager().getServer().isConnected())
            connectionOverlay.render(gl, glu, glut, vp);
        else
            gsOverlay.render(gl, glu, glut, vp);

        for (Screen overlay : overlays)
            overlay.render(gl, glu, glut, vp);

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

        switch (e.getKeyCode()) {
        case KeyEvent.VK_X:
            if (shift)
                viewer.getNetManager().getServer().killServer();
        case KeyEvent.VK_K:
                viewer.getNetManager().getServer().kickOff(true);
            break;
        case KeyEvent.VK_ESCAPE:
            viewer.shutdown();
            break;
        case KeyEvent.VK_P:
            viewer.getUI().getShapeSetPanel().showFrame();
            break;
        case KeyEvent.VK_O:
            if (viewer.getWorldModel().getGameState() != null
                    && viewer.getWorldModel().getGameState().getPlayModes() != null) {
                PlaymodeOverlay pmo = new PlaymodeOverlay(viewer, this);
                overlays.add(pmo);
                pmo.setEnabled((GLCanvas) viewer.getDrawable(), true);
            }
            break;
        case KeyEvent.VK_B:
            viewer.getNetManager().getServer().dropBall();
            break;
        case KeyEvent.VK_F1:
            viewer.toggleFullScreen();
            break;
        case KeyEvent.VK_T:
            viewer.getDrawings().toggle();
            break;
        case KeyEvent.VK_CONTROL:
            moveObjectMode = true;
            break;
        case KeyEvent.VK_I:
            AgentOverheadType[] vals = AgentOverheadType.values();
            agentOverheadType = vals[(agentOverheadType.ordinal()+1)%vals.length];
            break;
        case KeyEvent.VK_F:
            fieldOverlay.setVisible(!fieldOverlay.isVisible());
            break;
        case KeyEvent.VK_V:
            setRobotVantage();
            break;
        case KeyEvent.VK_C:
            if (!viewer.getNetManager().getServer().isConnected()) {
                viewer.getNetManager().getServer().connect();
            }
            break;
        case KeyEvent.VK_Q:
            viewer.shutdown();
            break;
        case KeyEvent.VK_L:
            viewer.getNetManager().getServer().freeKick(true);
            break;
        case KeyEvent.VK_R:
            viewer.getNetManager().getServer().freeKick(false);
            break;
        case KeyEvent.VK_SPACE:
            viewer.getUI().getBallTracker().toggleEnabled();
            break;
        case KeyEvent.VK_SHIFT:
            shift = true;
            break;
        }
    }


    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_CONTROL:
            moveObjectMode = false;
            break;
        case KeyEvent.VK_SHIFT:
            shift = false;
            break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        boolean connected = viewer.getNetManager().getServer().isConnected();
        if (robotVantage == null && connected
                && e.getButton() == MouseEvent.BUTTON1) {
            viewer.getUI().getObjectPicker()
                    .updatePickRay(viewer.getScreen(), e.getX(), e.getY());

            if (moveObjectMode
                    && viewer.getWorldModel().getSelectedObject() != null) {
                Vec3f fieldPos = viewer.getUI().getObjectPicker().pickField();
                moveSelection(fieldPos);
            } else {
                ISelectable newSelection = viewer.getUI().getObjectPicker()
                        .pickObject();
                changeSelection(newSelection);
            }
        }
    }

    private void changeSelection(ISelectable newSelection) {
        if (newSelection != null) {
            if (viewer.getWorldModel().getSelectedObject() != null)
                viewer.getWorldModel().getSelectedObject().setSelected(false);
            viewer.getWorldModel().setSelectedObject(newSelection);
            viewer.getWorldModel().getSelectedObject().setSelected(true);
        } else {
            if (viewer.getWorldModel().getSelectedObject() != null)
                viewer.getWorldModel().getSelectedObject().setSelected(false);
            viewer.getWorldModel().setSelectedObject(null);
        }
    }

    private void moveSelection(Vec3f pos) {
        if (pos != null) {
            ISelectable selected = viewer.getWorldModel().getSelectedObject();
            pos.y = selected.getBoundingBox().getCenter().y + 0.1f;
            Vec3f serverPos = WorldModel.COORD_TFN.transform(pos);

            if (selected instanceof Ball)
                viewer.getNetManager().getServer().moveBall(serverPos);
            else if (selected instanceof Agent) {
                Agent a = (Agent) selected;
                boolean leftTeam = a.getTeam().getID() == Team.LEFT;
                viewer.getNetManager().getServer()
                        .moveAgent(serverPos, leftTeam, a.getID());
            }
        }
    }

    private void setRobotVantage() {
        if (robotVantage != null) {
            robotVantage.detach();
            robotVantage = null;
            viewer.getRenderer().setVantage(viewer.getUI().getCamera());
            viewer.getUI().getCameraControl()
                    .attachToCanvas((GLCanvas) viewer.getDrawable());
        } else if (viewer.getWorldModel().getSelectedObject() != null
                && viewer.getWorldModel().getSelectedObject() instanceof Agent) {
            Agent a = (Agent) viewer.getWorldModel().getSelectedObject();
            robotVantage = new RobotVantage(a, 0.1f, 300);
            viewer.getRenderer().setVantage(robotVantage);
            viewer.getUI().getCameraControl()
                    .detachFromCanvas((GLCanvas) viewer.getDrawable());
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent arg0) {
    }

    @Override
    public void mouseMoved(MouseEvent arg0) {
    }

    @Override
    public void connectionChanged(ServerComm server) {
        changeSelection(null);
    }

    @Override
    public void newSceneGraph(SceneGraph sg) {
        changeSelection(null);
    }

    @Override
    public void updatedSceneGraph(SceneGraph sg) {
    }

    @Override
    public void gsMeasuresAndRulesChanged(GameState gs) {
    }

    @Override
    public void gsPlayStateChanged(GameState gs) {
        if (prevScoreL != -1 && prevScoreR != -1) {
            if (gs.getScoreLeft() > prevScoreL)
                textOverlays.add(new TextOverlay(String.format("Goal %s!",
                        gs.getTeamLeft()), 4000, new float[]{1,1,1,1}));
            if (gs.getScoreRight() > prevScoreR)
                textOverlays.add(new TextOverlay(String.format("Goal %s!",
                        gs.getTeamRight()), 4000, new float[]{1,1,1,1}));
        }
  
        prevScoreL = gs.getScoreLeft();
        prevScoreR = gs.getScoreRight();
    }

    @Override
    public void gsTimeChanged(GameState gs) {

    }
}
