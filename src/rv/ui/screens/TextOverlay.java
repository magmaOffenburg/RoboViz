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

import rv.comm.rcssserver.GameState;
import rv.world.WorldModel;
import java.awt.Color;
import java.awt.geom.Rectangle2D;

/**
 * A 2D overlay that displays text on the screen
 * 
 * @author justin
 */
public class TextOverlay implements GameState.GameStateChangeListener {

    private static final int FADE_DURATION = 750;

    private final String     text;
    private final WorldModel world;
    private int              duration;
    private int              x, y;
    private float            a             = 1;
    private float            startTime     = 0;
    private float            curTime       = 0;
    private boolean          done          = false;
    private final float[]    color;

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isExpired() {
        return done;
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            world.getGameState().addListener(this);
        } else {
            world.getGameState().removeListener(this);
        }
    }

    public TextOverlay(String text, WorldModel world, int duration, float[] color) {
        this.world = world;
        this.duration = duration;
        this.startTime = world.getGameState().getTime();
        curTime = startTime;
        this.text = text;
        this.color = color;
    }

    private void update() {
        int elapsedMS = Math.abs((int) ((curTime - startTime) * 1000));

        if (elapsedMS > duration)
            a = color[3] * Math.max(1 - (float) (elapsedMS - duration) / FADE_DURATION, 0);

        if (a == 0) {
            done = true;
            setEnabled(false);
        }
    }

    private void calcXY(BorderTextRenderer tr, int w, int h) {
        Rectangle2D bounds = tr.getBounds(text);
        x = (int) (w - bounds.getWidth()) / 2;
        y = (int) (h - bounds.getHeight()) / 2;
    }

    public void render(BorderTextRenderer tr, int w, int h) {
        if (duration > 0)
            update();
        calcXY(tr, w, h);
        Color textColor = new Color(color[0], color[1], color[2], a);
        Color shadowColor = new Color(0, 0, 0, a);
        tr.drawWithShadow(text, x, y, textColor, shadowColor);
    }

    @Override
    public void gsMeasuresAndRulesChanged(GameState gs) {
    }

    @Override
    public void gsPlayStateChanged(GameState gs) {
    }

    @Override
    public void gsTimeChanged(GameState gs) {
        curTime = gs.getTime();
    }
}
