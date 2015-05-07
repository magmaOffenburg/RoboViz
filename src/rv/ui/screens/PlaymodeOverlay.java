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
import java.awt.geom.Rectangle2D;
import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import js.jogl.view.Viewport;
import rv.Viewer;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

public class PlaymodeOverlay implements Screen, KeyListener {

    final Viewer                 viewer;
    int                          index = 0;
    String[]                     modes;
    final TextRenderer           tr;
    private final LiveGameScreen masterScreen;

    public PlaymodeOverlay(Viewer viewer, LiveGameScreen masterScreen) {
        this.viewer = viewer;
        this.masterScreen = masterScreen;
        if (viewer.getWorldModel().getGameState() != null)
            this.modes = viewer.getWorldModel().getGameState().getPlayModes();
        tr = new TextRenderer(new Font("Calibri", Font.BOLD, 18), true, true);

        // temporarily disable active screen
        masterScreen.setEnabled((GLCanvas) viewer.getCanvas(), false);
    }

    @Override
    public void render(GL2 gl, GLU glu, GLUT glut, Viewport vp) {

        int h = (int) (tr.getBounds(modes[0]).getHeight()) + 3;
        int y = (vp.h - modes.length * h) / 2;

        gl.glColor4f(0, 0, 0, 0.5f);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(0, y);
        gl.glVertex2f(vp.w, y);
        gl.glVertex2f(vp.w, y + h * (modes.length + 1));
        gl.glVertex2f(0, y + h * (modes.length + 1));
        gl.glEnd();
        gl.glColor4f(1, 1, 1, 1);

        tr.beginRendering(vp.w, vp.h);
        for (int i = 0; i < modes.length; i++) {

            if (i == index)
                tr.setColor(1, 1, 1, 1);
            else
                tr.setColor(0.6f, 0.6f, 0.6f, 1.0f);

            Rectangle2D bounds = tr.getBounds(modes[i]);
            int x = (int) ((vp.w - bounds.getWidth()) / 2);
            tr.draw(modes[i], x, vp.h - (y + i * h));
        }
        tr.endRendering();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_DOWN:
            index = Math.min(index + 1, modes.length - 1);
            break;
        case KeyEvent.VK_UP:
            index = Math.max(index - 1, 0);
            break;
        case KeyEvent.VK_ENTER:
            // changing the play mode doesn't have any effect if the game has ended
            float gameTime = viewer.getWorldModel().getGameState().getHalfTime() * 2;
            if (viewer.getWorldModel().getGameState().getTime() >= gameTime)
                viewer.getNetManager().getServer().resetTime();
            viewer.getNetManager().getServer().setPlayMode(modes[index]);
            setEnabled((GLCanvas) viewer.getCanvas(), false);
            masterScreen.setEnabled((GLCanvas) viewer.getCanvas(), true);
            masterScreen.removeOverlay(this);
            break;
        case KeyEvent.VK_ESCAPE:
            setEnabled((GLCanvas) viewer.getCanvas(), false);
            masterScreen.setEnabled((GLCanvas) viewer.getCanvas(), true);
            masterScreen.removeOverlay(this);
            break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void setEnabled(GLCanvas canvas, boolean enabled) {
        if (enabled) {
            canvas.addKeyListener(this);
        } else {
            canvas.removeKeyListener(this);
        }
    }
}
