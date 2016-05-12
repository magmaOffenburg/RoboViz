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
import javax.media.opengl.awt.GLCanvas;
import js.math.vector.Vec3f;
import rv.Configuration;
import rv.Viewer;
import rv.comm.rcssserver.GameState;
import rv.comm.rcssserver.ServerComm;
import rv.comm.rcssserver.ServerSpeedBenchmarker;
import rv.world.ISelectable;
import rv.world.Team;
import rv.world.WorldModel;
import rv.world.objects.Agent;
import rv.world.objects.Ball;

public class LiveGameScreen extends ViewerScreenBase implements ServerComm.ServerChangeListener {

    private final PlaymodeOverlay playmodeOverlay;
    private final InfoOverlay     connectionOverlay;

    public LiveGameScreen(Viewer viewer) {
        super(viewer);
        ServerSpeedBenchmarker ssb = new ServerSpeedBenchmarker();
        viewer.getWorldModel().getGameState().addListener(ssb);
        gameStateOverlay.addServerSpeedBenchmarker(ssb);
        playmodeOverlay = new PlaymodeOverlay(viewer, this);
        overlays.add(playmodeOverlay);
        connectionOverlay = new InfoOverlay().setMessage(getConnectionMessage());
        overlays.add(connectionOverlay);
    }

    private String getConnectionMessage() {
        Configuration.Networking config = viewer.getConfig().networking;
        String server = config.getServerHost() + ":" + config.getServerPort();
        GameState gameState = viewer.getWorldModel().getGameState();
        // in competitions, the server is restarted for the second half
        // display a viewer-friendly message in that case to let them know why the game has
        // "stopped"
        if (gameState.isInitialized()
                && Math.abs(gameState.getTime() - gameState.getHalfTime()) < 0.1)
            return "Waiting for second half...";
        else if (config.autoConnect)
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
        case KeyEvent.VK_O:
            if (viewer.getWorldModel().getGameState() != null
                    && viewer.getWorldModel().getGameState().getPlayModes() != null) {
                setEnabled((GLCanvas) viewer.getCanvas(), false);
                playmodeOverlay.setVisible(true);
            }
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
        case KeyEvent.VK_M:
            toggleShowServerSpeed();
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

    @Override
    protected void altClick(MouseEvent e) {
        Vec3f fieldPos = viewer.getUI().getObjectPicker().pickField();
        if (e.isControlDown()) {
            pushBallTowardPosition(fieldPos, false);
        } else if (e.isShiftDown()) {
            pushBallTowardPosition(fieldPos, true);
        }
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

    private void pushBallTowardPosition(Vec3f pos, boolean fAir) {
        if (pos != null) {
            Vec3f targetPos = WorldModel.COORD_TFN.transform(pos);
            Vec3f ballPos = WorldModel.COORD_TFN
                    .transform(viewer.getWorldModel().getBall().getPosition());
            ballPos.z = viewer.getWorldModel().getGameState().getBallRadius();
            Vec3f vel;
            float xDiff = targetPos.x - ballPos.x;
            float yDiff = targetPos.y - ballPos.y;
            float xyDist = (float) Math.sqrt(xDiff * xDiff + yDiff * yDiff);
            if (fAir) {
                final float AIR_XY_POWER_FACTOR = (float) Math
                        .sqrt(9.81 * xyDist * (.82 + .022 * xyDist)); // with no drag =
                                                                      // (float)Math.sqrt(9.81*xyDist/2);
                final float Z_POWER = AIR_XY_POWER_FACTOR;
                vel = new Vec3f((float) Math.cos(Math.atan2(yDiff, xDiff)) * AIR_XY_POWER_FACTOR,
                        (float) Math.sin(Math.atan2(yDiff, xDiff)) * AIR_XY_POWER_FACTOR, Z_POWER);
            } else {
                final float GROUND_XY_POWER_FACTOR = 1.475f;
                vel = new Vec3f(xDiff * GROUND_XY_POWER_FACTOR, yDiff * GROUND_XY_POWER_FACTOR,
                        0.0f);
            }
            viewer.getNetManager().getServer().moveBall(ballPos, vel);
        }
    }

    @Override
    public void connectionChanged(ServerComm server) {
        connectionOverlay.setMessage(getConnectionMessage());
        connectionOverlay.setVisible(!server.isConnected());
        if (!server.isConnected()) {
            prevScoreL = -1;
            prevScoreR = -1;
        }
    }
}
