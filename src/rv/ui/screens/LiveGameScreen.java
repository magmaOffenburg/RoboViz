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

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import js.jogl.view.Viewport;
import js.math.vector.Vec3f;
import rv.Configuration;
import rv.Viewer;
import rv.comm.drawing.BufferedSet;
import rv.comm.drawing.annotations.Annotation;
import rv.comm.rcssserver.ServerComm;
import rv.world.ISelectable;
import rv.world.Team;
import rv.world.WorldModel;
import rv.world.objects.Agent;
import rv.world.objects.Ball;
import com.jogamp.opengl.util.gl2.GLUT;

public class LiveGameScreen extends ViewerScreenBase implements ServerComm.ServerChangeListener {
    private final PlaymodeOverlay playmodeOverlay;
    private final InfoOverlay     connectionOverlay;

    public LiveGameScreen(Viewer viewer) {
        super(viewer);
        playmodeOverlay = new PlaymodeOverlay(viewer, this);
        overlays.add(playmodeOverlay);
        connectionOverlay = new InfoOverlay(getConnectionMessage());
        overlays.add(connectionOverlay);
    }

    private String getConnectionMessage() {
        Configuration.Networking config = viewer.getConfig().networking;
        String server = config.getServerHost() + ":" + config.getServerPort();
        if (config.autoConnect)
            return "Trying to connect to " + server + "...";
        else
            return "Press C to connect to " + server + ".";
    }

    @Override
    public void setEnabled(GLCanvas canvas, boolean enabled) {
        super.setEnabled(canvas, enabled);
        if (enabled) {
            viewer.getNetManager().getServer().addChangeListener(this);
        } else {
            viewer.getNetManager().getServer().removeChangeListener(this);
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
        super.render(gl, glu, glut, vp);

        tr.beginRendering(viewer.getScreen().w, viewer.getScreen().h);
        if (viewer.getDrawings().isVisible())
            renderAnnotations();
        tr.endRendering();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);

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
                setEnabled((GLCanvas) viewer.getCanvas(), false);
                playmodeOverlay.setVisible(true);
            }
            break;
        case KeyEvent.VK_T:
            viewer.getDrawings().toggle();
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
        case KeyEvent.VK_U:
            viewer.getNetManager().getServer().requestFullState();
            break;
        case KeyEvent.VK_B:
            resetTimeIfExpired();
            viewer.getNetManager().getServer().dropBall();
            break;
        }
    }

    private void resetTimeIfExpired() {
        // changing the play mode doesn't have any effect if the game has ended
        float gameTime = viewer.getWorldModel().getGameState().getHalfTime() * 2;
        if (viewer.getWorldModel().getGameState().getTime() >= gameTime)
            viewer.getNetManager().getServer().resetTime();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (viewer.getNetManager().getServer().isConnected()) {
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

    @Override
    public void connectionChanged(ServerComm server) {
        connectionOverlay.setVisible(!server.isConnected());
    }
}
