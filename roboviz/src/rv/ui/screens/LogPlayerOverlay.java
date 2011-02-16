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
import rv.comm.rcssserver.LogPlayer;

import com.jogamp.opengl.util.gl2.GLUT;

public class LogPlayerOverlay implements Screen, KeyListener {

    private float     barPad    = 20;
    private float     barHeight = 10;
    private LogPlayer player;

    public LogPlayerOverlay(LogPlayer player) {
        this.player = player;
    }

    @Override
    public void setEnabled(GLCanvas canvas, boolean enabled) {
        if (enabled)
            canvas.addKeyListener(this);
        else
            canvas.removeKeyListener(this);
    }

    @Override
    public void render(GL2 gl, GLU glu, GLUT glut, Viewport vp) {
        // player bar
        gl.glColor4f(0.5f, 0.5f, 0.5f, 0.8f);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(barPad, barPad);
        gl.glVertex2f(vp.w - barPad, barPad);
        gl.glVertex2f(vp.w - barPad, barPad + barHeight);
        gl.glVertex2f(barPad, barPad + barHeight);
        gl.glEnd();

        // player position knob
        float percent = (float) player.getFrame() / player.getNumFrames();
        float knobX = percent * (vp.w - 2 * barPad) + barPad;
        gl.glColor4f(1, 1, 1, 1);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(knobX - 1, barPad - 2);
        gl.glVertex2f(knobX + 1, barPad - 2);
        gl.glVertex2f(knobX + 1, barPad + 2 + barHeight);
        gl.glVertex2f(knobX - 1, barPad + 2 + barHeight);
        gl.glEnd();
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
        case KeyEvent.VK_Z:
            player.addDelay(-25);
            break;
        case KeyEvent.VK_X:
            player.addDelay(25);
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