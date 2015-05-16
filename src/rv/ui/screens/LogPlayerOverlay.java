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
import java.awt.event.KeyListener;
import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import js.jogl.view.Viewport;
import rv.Viewer;
import rv.comm.rcssserver.LogPlayer;
import com.jogamp.opengl.util.gl2.GLUT;

public class LogPlayerOverlay extends ScreenBase implements KeyListener {

    private final LogPlayer      player;
    private final PlayerControls playDialog;
    private final Viewer         viewer;

    public LogPlayerOverlay(Viewer viewer) {
        this.viewer = viewer;
        this.player = viewer.getLogPlayer();
        playDialog = PlayerControls.getInstance(player);
    }

    @Override
    public void setEnabled(GLCanvas canvas, boolean enabled) {
        if (enabled) {
            canvas.addKeyListener(this);
            playDialog.showFrame(viewer.getFrame());
        } else {
            canvas.removeKeyListener(this);
            playDialog.hideFrame(viewer.getFrame());
        }
    }

    @Override
    public void render(GL2 gl, GLU glu, GLUT glut, Viewport vp) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_P:
            if (player.isPlaying())
                player.pause();
            else
                player.resume();
            break;
        case KeyEvent.VK_O:
            player.stop();
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
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {

    }

    @Override
    public void keyTyped(KeyEvent arg0) {

    }
}
