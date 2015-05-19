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
import rv.comm.rcssserver.LogPlayer;

public class LogfileModeScreen extends ViewerScreenBase {

    private final LogPlayer      player;
    private final PlayerControls playDialog;

    public LogfileModeScreen(Viewer viewer) {
        super(viewer);
        this.player = viewer.getLogPlayer();
        playDialog = PlayerControls.getInstance(player);
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
    protected void addTextOverlay(TextOverlay textOverlay) {
        super.addTextOverlay(textOverlay);
        textOverlay.setTimeScale(viewer.getLogPlayer().getPlayBackSpeed());
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
}
