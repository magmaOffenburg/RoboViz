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

import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import js.jogl.view.Viewport;
import rv.Viewer;
import com.jogamp.opengl.util.gl2.GLUT;

public class LogfileModeScreen extends ViewerScreenBase {

    private final GameStateOverlay gsOverlay;
    private final LogPlayerOverlay lpOverlay;

    public LogfileModeScreen(Viewer viewer) {
        super(viewer);
        lpOverlay = new LogPlayerOverlay(viewer.getLogPlayer());
        gsOverlay = new GameStateOverlay(viewer);
    }

    @Override
    public void setEnabled(GLCanvas canvas, boolean enabled) {
        super.setEnabled(canvas, enabled);

        gsOverlay.setEnabled(canvas, enabled);
        lpOverlay.setEnabled(canvas, enabled);

        viewer.getWorldModel().getGameState().addListener(this);
    }

    @Override
    public void render(GL2 gl, GLU glu, GLUT glut, Viewport vp) {
        gsOverlay.render(gl, glu, glut, vp);
        lpOverlay.render(gl, glu, glut, vp);
    }
}
