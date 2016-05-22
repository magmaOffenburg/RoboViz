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

import java.util.List;
import java.awt.Font;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import js.jogl.view.Viewport;
import rv.Viewer;
import js.math.vector.Vec3f;
import rv.comm.rcssserver.GameState;
import rv.comm.rcssserver.GameState.GameStateChangeListener;
import rv.world.WorldModel;

/**
 * Displays a running list of fouls. Based off initial implementation by Sander van Dijk.
 * 
 * @author patmac
 */
public class FoulListOverlay extends ScreenBase implements GameStateChangeListener {
    private static final int   FOUL_HEIGHT        = 20;
    private static final int   FOUL_WIDTH         = 180;
    private static final float FOUL_SHOW_TIME     = 10.0f;
    private static final float FOUL_FADE_TIME     = 2.0f;
    private static final int   TOP_SCREEN_OFFSET  = 17;
    private static final int   SIDE_SCREEN_OFFSET = 17;

    private final Viewer       viewer;

    public FoulListOverlay(Viewer viewer) {
        this.viewer = viewer;
        viewer.getWorldModel().getGameState().addListener(this);
    }

    void render(GL2 gl, GameState gs, int screenW, int screenH) {
        int y = screenH - TOP_SCREEN_OFFSET;
        int x = screenW - FOUL_WIDTH - SIDE_SCREEN_OFFSET;
        TextRenderer tr = new TextRenderer(new Font("Arial", Font.PLAIN, 16), true, false);

        gl.glBegin(GL2.GL_QUADS);
        gl.glColor4fv(new float[] { 0.0f, 0.0f, 0.0f, 1.0f / 3.0f }, 0);
        drawBox(gl, x, y - FOUL_HEIGHT, FOUL_WIDTH, FOUL_HEIGHT);

        gl.glEnd();

        tr.setColor(0.9f, 0.9f, 0.9f, 1.0f);
        tr.beginRendering(screenW, screenH);
        tr.draw("Fouls:", x + 3, y - FOUL_HEIGHT + 4);
        tr.endRendering();

        float[] lc = viewer.getWorldModel().getLeftTeam().getTeamMaterial().getDiffuse();
        float[] rc = viewer.getWorldModel().getRightTeam().getTeamMaterial().getDiffuse();

        List<GameState.Foul> fouls = gs.getFouls();
        float n = 1.0f;
        for (int i = 0; i < fouls.size(); ++i) {
            GameState.Foul f = fouls.get(i);
            float dt = (System.currentTimeMillis() - f.receivedTime) / 1000.0f;
            if (shouldDisplayFoul(f)) {
                float opacity = dt > FOUL_SHOW_TIME ? 1.0f - (dt - FOUL_SHOW_TIME) / FOUL_FADE_TIME
                        : 1.0f;
                drawFoul(gl, x, y - (int) (20 * n), FOUL_WIDTH, FOUL_HEIGHT, screenW, screenH, f,
                        opacity, f.team == 1 ? lc : rc);
                n += opacity;
            }
        }
    }

    @Override
    public void render(GL2 gl, GLU glu, GLUT glut, Viewport vp) {
        render(gl, viewer.getWorldModel().getGameState(), vp.w, vp.h);
    }

    void drawFoul(GL2 gl, int x, int y, int w, int h, int screenW, int screenH,
            GameState.Foul foul, float opacity, float[] teamColor) {
        float[] cardFillColor;
        TextRenderer tr = new TextRenderer(new Font("Arial", Font.PLAIN, 16), true, false);

        String foulText = "#" + Integer.toString(foul.unum) + ": " + foul.type + "!";

        switch (foul.type) {
        case CROWDING:
        case TOUCHING:
        case ILLEGALDEFENCE:
        case ILLEGALATTACK:
        case INCAPABLE:
        case KICKOFF:
            // Yellow
            cardFillColor = new float[] { 0.8f, 0.6f, 0.0f, 1.0f };
            break;

        case CHARGING:
        default:
            // Red
            cardFillColor = new float[] { 0.8f, 0.0f, 0.0f, 1.0f };
            break;
        }

        cardFillColor[3] = opacity;
        teamColor[3] = opacity / 3;

        gl.glBegin(GL2.GL_QUADS);
        gl.glColor4fv(teamColor, 0);
        gl.glVertex2fv(new float[] { x + 16, y }, 0);
        gl.glVertex2fv(new float[] { x + w, y }, 0);
        gl.glVertex2fv(new float[] { x + w, y - h }, 0);
        gl.glVertex2fv(new float[] { x + 16, y - h }, 0);

        float[][] v = { { x + 2, y - 1 }, { x + 12, y - 3 }, { x + 10, y - 19 }, { x, y - 17 } };
        gl.glColor4fv(cardFillColor, 0);
        for (int i = 0; i < v.length; ++i) {
            gl.glVertex2fv(v[i], 0);
        }
        gl.glEnd();

        tr.setColor(0.9f, 0.9f, 0.9f, opacity);
        tr.beginRendering(screenW, screenH);
        tr.draw(foulText, x + 20, y - h + 4);
        tr.endRendering();
    }

    void removeOldFouls(GameState gs) {
        // Remove fouls that are no longer to be displayed and out of date
        // so that they don't block other fouls from being added later.
        // This can be a bit trcky if we're moving backwards/forwards in
        // time in a log.
        List<GameState.Foul> fouls = gs.getFouls();
        for (int i = 0; i < fouls.size(); ++i) {
            GameState.Foul f = fouls.get(i);
            if (!shouldDisplayFoul(f)) {
                if (Math.abs(gs.getTime() - f.time) >= 1 || gs.getPlayMode().equals("GameOver")
                        || gs.getPlayMode().equals("BeforeKickOff")) {
                    fouls.remove(i);
                    i--;
                }
            }
        }
    }

    boolean shouldDisplayFoul(GameState.Foul f) {
        float dt = (System.currentTimeMillis() - f.receivedTime) / 1000.0f;
        return dt < FOUL_SHOW_TIME + FOUL_FADE_TIME;
    }

    static void drawBox(GL2 gl, float x, float y, float w, float h) {
        gl.glVertex2f(x, y);
        gl.glVertex2f(x + w, y);
        gl.glVertex2f(x + w, y + h);
        gl.glVertex2f(x, y + h);
    }

    @Override
    public void gsMeasuresAndRulesChanged(GameState gs) {
    }

    @Override
    public void gsPlayStateChanged(GameState gs) {
        removeOldFouls(gs);
    }

    @Override
    public void gsTimeChanged(GameState gs) {
        removeOldFouls(gs);
    }
}