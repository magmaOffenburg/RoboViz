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
import java.awt.geom.Rectangle2D;

/**
 * A 2D overlay that displays text on the screen
 * 
 * @author justin
 */
public class TextOverlay {

    private final String  text;
    private int           fadeDuration = 750;
    private int           duration;
    private int           x, y;
    private float         a            = 1;
    private long          elapsed      = 0;
    private long          lastTime     = 0;
    private boolean       done         = false;
    private final float[] color;

    public void setFadeDuration(int fadeDuration) {
        this.fadeDuration = fadeDuration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isExpired() {
        return done;
    }

    public TextOverlay(String text, int duration, float[] color) {
        this.duration = duration;
        this.text = text;
        this.color = color;
    }

    private void update() {
        long curTime = System.currentTimeMillis();
        if (lastTime != 0)
            elapsed += curTime - lastTime;
        lastTime = curTime;

        if (fadeDuration > 0 && elapsed > duration)
            a = color[3] * Math.max(1 - (float) (elapsed - duration) / fadeDuration, 0);

        if (a == 0)
            done = true;
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
}
