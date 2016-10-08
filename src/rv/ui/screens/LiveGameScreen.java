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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.AbstractAction;
import js.math.vector.Vec3f;
import rv.Configuration;
import rv.Viewer;
import rv.comm.rcssserver.GameState;
import rv.comm.rcssserver.ServerComm;
import rv.comm.rcssserver.ServerSpeedBenchmarker;
import rv.ui.menus.Menu;
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
        viewer.getNetManager().getServer().addChangeListener(this);
        viewer.getNetManager().getServer().addChangeListener(ssb);
        gameStateOverlay.addServerSpeedBenchmarker(ssb);
        playmodeOverlay = new PlaymodeOverlay(viewer, this);
        overlays.add(playmodeOverlay);
        connectionOverlay = new InfoOverlay().setMessage(getConnectionMessage());
        overlays.add(connectionOverlay);
        createServerMenu(viewer.getFrame().getServerMenu());
    }

    @Override
    protected void loadOverlayVisibilities(Configuration.OverlayVisibility config) {
        super.loadOverlayVisibilities(config);
        gameStateOverlay.setShowServerSpeed(config.serverSpeed);
    }

    @Override
    public void createViewMenu(Menu menu) {
        super.createViewMenu(menu);

        menu.addItem("Toggle Server Speed", "M", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleShowServerSpeed();
            }
        });

        menu.addItem("Playmode Overlay", "O", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openPlaymodeOverlay();
            }
        });
    }

    private void createServerMenu(Menu menu) {
        if (!viewer.getConfig().networking.autoConnect)
            menu.addItem("Connect", "C", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    connect();
                }
            });

        menu.addItem("Kill Server", "shift X", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getServer().killServer();
            }
        });

        menu.addItem("Kick Off Left", "K", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kickOff(true);
            }
        });
        menu.addItem("Kick Off Right", "J", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kickOff(false);
            }
        });

        menu.addItem("Free Kick Left", "L", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                freeKick(true);
            }
        });
        menu.addItem("Free Kick Right", "R", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                freeKick(false);
            }
        });

        menu.addItem("Direct Free Kick Left", "shift L", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                directFreeKick(true);
            }
        });
        menu.addItem("Direct Free Kick Right", "shift R", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                directFreeKick(false);
            }
        });

        menu.addItem("Reset Time", "shift T", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getServer().resetTime();
            }
        });

        menu.addItem("Request Full State Update", "U", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getServer().requestFullState();
            }
        });

        menu.addItem("Drop Ball", "B", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dropBall();
            }
        });
    }

    private ServerComm getServer() {
        return viewer.getNetManager().getServer();
    }

    private void kickOff(boolean left) {
        resetTimeIfExpired();
        getServer().kickOff(left);
    }

    private void directFreeKick(boolean left) {
        resetTimeIfExpired();
        getServer().directFreeKick(left);
    }

    private void freeKick(boolean left) {
        resetTimeIfExpired();
        getServer().freeKick(left);
    }

    private void connect() {
        if (!getServer().isConnected())
            getServer().connect();
    }

    private void dropBall() {
        resetTimeIfExpired();
        getServer().dropBall();
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
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);

        switch (e.getKeyCode()) {
        case KeyEvent.VK_X:
            if (e.isShiftDown())
                getServer().killServer();
            break;
        case KeyEvent.VK_K:
            kickOff(true);
            break;
        case KeyEvent.VK_J:
            kickOff(false);
            break;
        case KeyEvent.VK_O:
            if (viewer.getWorldModel().getGameState() != null
                    && viewer.getWorldModel().getGameState().getPlayModes() != null) {
                openPlaymodeOverlay();
            }
            break;
        case KeyEvent.VK_C:
            connect();
            break;
        case KeyEvent.VK_L:
            if (e.isShiftDown())
                directFreeKick(true);
            else
                freeKick(true);
            break;
        case KeyEvent.VK_R:
            if (e.isShiftDown())
                directFreeKick(false);
            else
                freeKick(false);
            break;
        case KeyEvent.VK_T:
            if (e.isShiftDown())
                getServer().resetTime();
            break;
        case KeyEvent.VK_U:
            getServer().requestFullState();
            break;
        case KeyEvent.VK_B:
            dropBall();
            break;
        case KeyEvent.VK_M:
            toggleShowServerSpeed();
            break;
        }
    }

    private void openPlaymodeOverlay() {
        setEnabled((GLCanvas) viewer.getCanvas(), false);
        playmodeOverlay.setVisible(true);
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
        if (pos == null)
            return;
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

    private void pushBallTowardPosition(Vec3f pos, boolean fAir) {
        if (pos == null)
            return;

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
            vel = new Vec3f(xDiff * GROUND_XY_POWER_FACTOR, yDiff * GROUND_XY_POWER_FACTOR, 0.0f);
        }
        viewer.getNetManager().getServer().moveBall(ballPos, vel);

    }

    @Override
    public void connectionChanged(ServerComm server) {
        connectionOverlay.setMessage(getConnectionMessage());
        connectionOverlay.setVisible(!server.isConnected());
        if (server.isConnected()) {
            viewer.getWorldModel().setSelectedObject(viewer.getWorldModel().getBall());
        } else {
            playmodeOverlay.setVisible(false);
            prevScoreL = -1;
            prevScoreR = -1;
        }
    }
}
