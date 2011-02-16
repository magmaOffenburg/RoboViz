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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import js.jogl.view.FPCamera;
import js.jogl.view.Viewport;
import js.math.vector.Vec2f;
import js.math.vector.Vec3f;
import rv.Viewer;
import rv.comm.rcssserver.GameState;
import rv.comm.rcssserver.GameState.GameStateChangeListener;
import rv.ui.CameraSetting;

import com.jogamp.opengl.util.gl2.GLUT;

public class LogfileModeScreen implements Screen, KeyListener, MouseListener,
        MouseMotionListener, GameStateChangeListener {

    private GameStateOverlay gsOverlay;
    private LogPlayerOverlay lpOverlay;
    private Viewer           viewer;
    private CameraSetting[]  cameras;

    public LogfileModeScreen(Viewer viewer) {
        this.viewer = viewer;
        lpOverlay = new LogPlayerOverlay(viewer.getLogPlayer());
        gsOverlay = new GameStateOverlay(viewer);
    }

    @Override
    public void setEnabled(GLCanvas canvas, boolean enabled) {
        if (enabled) {
            canvas.addKeyListener(this);
            canvas.addMouseListener(this);
            canvas.addMouseMotionListener(this);
            viewer.getUI().getCameraControl().attachToCanvas(canvas);
        } else {
            canvas.removeKeyListener(this);
            canvas.removeMouseListener(this);
            canvas.removeMouseMotionListener(this);
            viewer.getUI().getCameraControl().detachFromCanvas(canvas);
        }

        gsOverlay.setEnabled(canvas, enabled);
        lpOverlay.setEnabled(canvas, enabled);

        viewer.getWorldModel().getGameState().addListener(this);
    }

    @Override
    public void render(GL2 gl, GLU glu, GLUT glut, Viewport vp) {
        gsOverlay.render(gl, glu, glut, vp);
        lpOverlay.render(gl, glu, glut, vp);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_ESCAPE:
            viewer.shutdown();
            break;
        case KeyEvent.VK_Q:
            viewer.shutdown();
            break;
        case KeyEvent.VK_F1:
            viewer.toggleFullScreen();
            break;
        case KeyEvent.VK_SPACE:
            viewer.getUI().getBallTracker().toggleEnabled();
            break;
        }
    }

    private void initCameras(GameState gs) {
        float fl = gs.getFieldLength();
        float fw = gs.getFieldWidth();

        cameras = new CameraSetting[] {
                new CameraSetting(new Vec3f(fl * 0.8f, fl * 0.4f, 0),
                        new Vec2f(-35, 90)),
                new CameraSetting(new Vec3f(fl * 0.8f, fl * 0.4f, -fw),
                        new Vec2f(-30, 180 - 50)),
                new CameraSetting(new Vec3f(0, fl * 0.4f, -fw), new Vec2f(-40,
                        180 + 35.8f)),
                new CameraSetting(new Vec3f(0, fl * 0.6f, -fw * 1.1f),
                        new Vec2f(-45, 180)),
                new CameraSetting(new Vec3f(0, fl * 0.4f, -fw), new Vec2f(-40,
                        180 - 35.8f)),
                new CameraSetting(new Vec3f(-fl * 0.8f, fl * 0.4f, -fw),
                        new Vec2f(-30, 180 + 50)),
                new CameraSetting(new Vec3f(-fl * 0.8f, fl * 0.4f, 0),
                        new Vec2f(-35, 180 + 90)), };
    }

    private void setCamera(int i) {
        if (i >= cameras.length || i < 0)
            return;

        FPCamera camera = viewer.getUI().getCamera();
        camera.setPosition(cameras[i].getPosition().clone());
        camera.setRotation(cameras[i].getRotation().clone());
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void gsMeasuresAndRulesChanged(GameState gs) {
        initCameras(gs);
    }

    @Override
    public void gsPlayStateChanged(GameState gs) {
        // TODO Auto-generated method stub

    }

    @Override
    public void gsTimeChanged(GameState gs) {
        // TODO Auto-generated method stub

    }
}
