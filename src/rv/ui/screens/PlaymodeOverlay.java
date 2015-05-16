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
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import js.jogl.view.Viewport;
import rv.Viewer;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

public class PlaymodeOverlay implements Screen, KeyListener {

    private static final String  DEFAULT_FILTER_TEXT = "Type anything to filter...";
    private static final int     MAX_FILTER_LENGTH   = 30;
    private static final String  CARET               = "|";
    private static final int     CARET_INTERVAL      = 30;

    final Viewer                 viewer;
    int                          index               = 0;
    String[]                     modes;
    List<String>                 filteredModes       = new ArrayList<>();
    String                       filterText;
    String                       caret               = CARET;
    int                          caretTimer          = 0;
    boolean                      justCreated         = true;
    final TextRenderer           tr;
    private final LiveGameScreen masterScreen;

    public PlaymodeOverlay(Viewer viewer, LiveGameScreen masterScreen) {
        this.viewer = viewer;
        this.masterScreen = masterScreen;
        if (viewer.getWorldModel().getGameState() != null)
            this.modes = viewer.getWorldModel().getGameState().getPlayModes();
        tr = new TextRenderer(new Font("Calibri", Font.BOLD, 18), true, true);
        filterText = DEFAULT_FILTER_TEXT;
        resetFilter(null);

        // temporarily disable active screen
        masterScreen.setEnabled((GLCanvas) viewer.getCanvas(), false);
    }

    @Override
    public void render(GL2 gl, GLU glu, GLUT glut, Viewport vp) {

        int h = (int) (tr.getBounds(modes[0]).getHeight()) + 3;
        int y = (vp.h - (modes.length + 2) * h) / 2;

        gl.glColor4f(0, 0, 0, 0.5f);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(0, y);
        gl.glVertex2f(vp.w, y);
        gl.glVertex2f(vp.w, y + h * (modes.length + 3));
        gl.glVertex2f(0, y + h * (modes.length + 3));
        gl.glEnd();
        gl.glColor4f(1, 1, 1, 1);

        tr.beginRendering(vp.w, vp.h);

        tr.setColor(0.6f, 0.6f, 0.6f, 1.0f);
        drawText(filterText + getCaret(), tr.getBounds(filterText), vp, 0, h, y);

        for (int i = 0; i < filteredModes.size(); i++) {

            if (i == index)
                tr.setColor(Color.white);
            else
                tr.setColor(0.6f, 0.6f, 0.6f, 1.0f);

            String mode = filteredModes.get(i);
            drawText(mode, tr.getBounds(mode), vp, i + 2, h, y);
        }
        tr.endRendering();
    }

    private void drawText(String text, Rectangle2D bounds, Viewport vp, int index, int h, int y) {
        int x = (int) ((vp.w - bounds.getWidth()) / 2);
        tr.draw(text, x, vp.h - (y + index * h));
    }

    private String getCaret() {
        if (filterText.equals(DEFAULT_FILTER_TEXT))
            return "";

        caretTimer++;
        if (caretTimer >= CARET_INTERVAL) {
            caretTimer = 0;
            if (caret.equals(CARET))
                caret = "";
            else
                caret = CARET;
        }
        return caret;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_DOWN:
            index = wrap(index + 1, 0, modes.length - 1);
            break;
        case KeyEvent.VK_UP:
            index = wrap(index - 1, 0, modes.length - 1);
            break;
        case KeyEvent.VK_ENTER:
            // changing the play mode doesn't have any effect if the game has ended
            float gameTime = viewer.getWorldModel().getGameState().getHalfTime() * 2;
            if (viewer.getWorldModel().getGameState().getTime() >= gameTime)
                viewer.getNetManager().getServer().resetTime();
            viewer.getNetManager().getServer().setPlayMode(modes[index]);
            close();
            break;
        case KeyEvent.VK_ESCAPE:
            close();
            break;
        case KeyEvent.VK_BACK_SPACE:
            filterTextChanged();
            int endIndex = Math.max(0, filterText.length() - 1);
            filterText = filterText.substring(0, endIndex);
            resetFilter(filterText);
            break;
        }
    }

    private int wrap(int value, int min, int max) {
        if (value < min)
            return max;
        else if (value > max)
            return min;
        return value;
    }

    private void close() {
        setEnabled((GLCanvas) viewer.getCanvas(), false);
        masterScreen.setEnabled((GLCanvas) viewer.getCanvas(), true);
        masterScreen.removeOverlay(this);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {
        // prevent overlay hotkey from showing up
        if (justCreated) {
            justCreated = false;
            return;
        }

        filterTextChanged();

        char c = e.getKeyChar();
        if (Pattern.matches("[A-Za-z_]", Character.toString(c))
                && filterText.length() < MAX_FILTER_LENGTH) {
            filterText += c;
            resetFilter(filterText);
        }
    }

    private void filterTextChanged() {
        if (filterText.equals(DEFAULT_FILTER_TEXT))
            filterText = "";
    }

    private void resetFilter(String filter) {
        filteredModes.clear();
        for (String mode : modes) {
            if (filter == null || mode.contains(filter)) {
                filteredModes.add(mode);
            }
        }
        index = 0;
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
