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
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import js.jogl.view.Viewport;
import rv.Viewer;
import rv.comm.rcssserver.GameState;
import rv.world.Team;
import rv.world.WorldModel;

import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

public class GameStateOverlay implements Screen {

    private Viewer       viewer;
    private TextRenderer textRenderer;

    public GameStateOverlay(Viewer viewer) {
        this.viewer = viewer;
        Font font = new Font("Arial", Font.PLAIN, 20);
        textRenderer = new TextRenderer(font, true, false);
    }

    public void setEnabled(GLCanvas canvas, boolean enabled) {
    }

    @Override
    public void render(GL2 gl, GLU glu, GLUT glut, Viewport vp) {
        gl.glColor4f(0.2f, 0.2f, 0.2f, 0.3f);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(0, viewer.getScreen().h - 10);
        gl.glVertex2f(viewer.getScreen().w, viewer.getScreen().h - 10);
        gl.glVertex2f(viewer.getScreen().w, viewer.getScreen().h - 70);
        gl.glVertex2f(0, viewer.getScreen().h - 70);
        gl.glEnd();

        WorldModel wm = viewer.getWorldModel();
        textRenderer.beginRendering(viewer.getScreen().w, viewer.getScreen().h);

        Team left = wm.getLeftTeam();
        float[] leftColor = left.getTeamMaterial().getDiffuse();

        String leftTeamText = String.format("(%d) %s", left.getScore(),
                left.getName());
        Rectangle2D bounds = textRenderer.getBounds(leftTeamText);
        textRenderer.setColor(0, 0, 0, 1);
        textRenderer.draw(leftTeamText, -1, viewer.getScreen().h - 41);
        textRenderer.setColor(leftColor[0], leftColor[1], leftColor[2], 1);
        textRenderer.draw(leftTeamText, 0, viewer.getScreen().h - 40);

        Team right = wm.getRightTeam();
        float[] rightColor = right.getTeamMaterial().getDiffuse();

        String rtText = String.format("%s (%d)", right.getName(),
                right.getScore());
        textRenderer.setColor(0, 0, 0, 1);
        textRenderer.draw(rtText, (int) (viewer.getScreen().w - textRenderer
                .getBounds(rtText).getWidth()) - 1, viewer.getScreen().h - 41);
        textRenderer.setColor(rightColor[0], rightColor[1], rightColor[2], 1);
        textRenderer.draw(rtText, (int) (viewer.getScreen().w - textRenderer
                .getBounds(rtText).getWidth()), viewer.getScreen().h - 40);

        GameState gs = wm.getGameState();
        textRenderer.setColor(1, 1, 1, 1);
        String timeText = String.format("Time: %.1f   Half: %d", gs.getTime(),
                gs.getHalf());
        textRenderer.setColor(0, 0, 0, 1);
        textRenderer.draw(timeText, (int) (viewer.getScreen().w - textRenderer
                .getBounds(timeText).getWidth()) / 2 - 1,
                viewer.getScreen().h - 31);
        textRenderer.setColor(1, 1, 1, 1);
        textRenderer
                .draw(timeText, (int) (viewer.getScreen().w - textRenderer
                        .getBounds(timeText).getWidth()) / 2, viewer
                        .getScreen().h - 30);

        String playmode = gs.getPlayMode();
        textRenderer.setColor(0, 0, 0, 1);
        textRenderer.draw(playmode, (int) (viewer.getScreen().w - textRenderer
                .getBounds(playmode).getWidth()) / 2 - 1,
                viewer.getScreen().h - 61);
        textRenderer.setColor(1, 1, 1, 1);
        textRenderer
                .draw(playmode, (int) (viewer.getScreen().w - textRenderer
                        .getBounds(playmode).getWidth()) / 2, viewer
                        .getScreen().h - 60);

        textRenderer.endRendering();
    }
}
