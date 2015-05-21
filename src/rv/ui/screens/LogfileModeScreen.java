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
import javax.media.opengl.awt.GLCanvas;
import rv.Viewer;
import rv.comm.rcssserver.GameState;
import rv.comm.rcssserver.LogAnalyzerThread.Goal;
import rv.comm.rcssserver.LogPlayer;

public class LogfileModeScreen extends ViewerScreenBase {

    private final LogPlayer      player;
    private final PlayerControls playDialog;
    private final InfoOverlay    openFileOverlay;

    public LogfileModeScreen(Viewer viewer) {
        super(viewer);
        this.player = viewer.getLogPlayer();
        playDialog = PlayerControls.getInstance(player);
        openFileOverlay = new InfoOverlay("Please open a logfile.");
        overlays.add(openFileOverlay);
        player.addListener(new LogPlayer.StateChangeListener() {
            @Override
            public void playerStateChanged(boolean playing) {
                openFileOverlay.setVisible(!player.isValid());
            }

            @Override
            public void logfileChanged() {
                prevScoreL = -1;
                prevScoreR = -1;
            }
        });
    }

    @Override
    public void setEnabled(GLCanvas canvas, boolean enabled) {
        super.setEnabled(canvas, enabled);
        if (enabled) {
            playDialog.showFrame(viewer.getFrame());
        } else {
            playDialog.hideFrame(viewer.getFrame());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        switch (e.getKeyCode()) {
        case KeyEvent.VK_P:
            if (player.isPlaying())
                player.pause();
            else
                player.resume();
            break;
        case KeyEvent.VK_R:
            player.rewind();
            break;
        case KeyEvent.VK_X:
            player.decreasePlayBackSpeed();
            break;
        case KeyEvent.VK_C:
            player.increasePlayBackSpeed();
            break;
        case KeyEvent.VK_COMMA:
            if (!player.isPlaying())
                player.stepBackward();
            break;
        case KeyEvent.VK_PERIOD:
            if (!player.isPlaying())
                player.stepForward();
            break;
        case KeyEvent.VK_G:
            player.stepBackwardGoal();
            break;
        case KeyEvent.VK_H:
            player.stepForwardGoal();
            break;
        }
    }

    @Override
    public void gsPlayStateChanged(GameState gs) {
        int frame = player.getFrame();
        if (player.getAnalyzedFrames() > frame) {
            for (Goal goal : player.getGoals()) {
                if (goal.frame == frame) {
                    addTeamScoredOverlay(gs, goal.scoringTeam);
                }
            }
        } else {
            super.gsPlayStateChanged(gs);
        }
    }
}
