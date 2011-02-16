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

package rv.ui.view;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.media.opengl.awt.GLCanvas;

import js.jogl.view.FPCamera;
import js.math.vector.Vec2f;
import js.math.vector.Vec3f;
import rv.ui.UserInterface;

/**
 * SimSpark style camera controller
 * 
 * @author justin
 */
public class SimsparkController implements CameraController {

    private UserInterface ui;

    protected boolean     rotate;
    protected boolean     moveF;                   // camera is moving
    protected boolean     moveB;                   // camera is moving back
    protected boolean     moveL;                   // camera is moving left
    protected boolean     moveR;                   // camera is moving right
    protected boolean     moveU;
    protected boolean     moveD;
    protected Vec2f       lastMouse = new Vec2f(0);

    float                 dL, dR, dF, dB, dU, dD = 0;
    float                 dMax      = 1;
    float                 dChange   = 0.08f;

    public SimsparkController(UserInterface ui) {
        this.ui = ui;
        ui.getCamera().setTranslateSpeed(4);
    }

    public void update(double elapsedMS) {

        // local move is vector in camera's local coordinate system
        Vec3f tLocal = new Vec3f(0);

        // world move is vector in world space
        Vec3f tWorld = new Vec3f(0);

        FPCamera cam = ui.getCamera();

        dR = moveR ? dMax : Math.max(dR - dChange, 0);
        dL = moveL ? dMax : Math.max(dL - dChange, 0);
        dF = moveF ? dMax : Math.max(dF - dChange, 0);
        dB = moveB ? dMax : Math.max(dB - dChange, 0);
        dU = moveU ? dMax / 2 : Math.max(dU - dChange, 0);
        dD = moveD ? dMax / 2 : Math.max(dD - dChange, 0);

        if (dR > 0)
            tLocal.add(Vec3f.unitX().times(dR));
        if (dL > 0)
            tLocal.add(Vec3f.unitX().times(-dL));
        if (dF > 0) {
            Vec3f v = cam.getRotation().transform(Vec3f.unitZ().times(-1));
            v.y = 0;
            tWorld.add(v.normalize().times(dF));
        }
        if (dB > 0) {
            Vec3f v = cam.getRotation().transform(Vec3f.unitZ());
            v.y = 0;
            tWorld.add(v.normalize().times(dB));
        }
        if (dU > 0)
            tLocal.add(Vec3f.unitY().times(dU));
        if (dD > 0)
            tLocal.add(Vec3f.unitY().times(-dD));

        float scale = (float) (elapsedMS / 1000.0f * ui.getCamera()
                .getTranslatedSpeed());

        tLocal.mul(scale);
        tWorld.mul(scale);

        if (tLocal.lengthSquared() > 0)
            cam.moveLocal(tLocal);
        if (tWorld.lengthSquared() > 0)
            cam.moveWorld(tWorld);

    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        switch (e.getButton()) {
        case MouseEvent.BUTTON1:
            rotate = true;
            break;
        case MouseEvent.BUTTON3:
            moveU = true;
            break;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        switch (e.getButton()) {
        case MouseEvent.BUTTON1:
            rotate = false;
            break;
        case MouseEvent.BUTTON3:
            moveU = false;
            break;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (rotate) {
            Vec2f mouseMove = new Vec2f(e.getX(), e.getY()).minus(lastMouse);
            ui.getCamera().rotate(
                    mouseMove.times(ui.getCamera().getRotateSpeed()));
        }
        lastMouse = new Vec2f(e.getX(), e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        lastMouse = new Vec2f(e.getX(), e.getY());
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        switch (key) {
        case KeyEvent.VK_W:
        case KeyEvent.VK_UP:
            moveF = true;
            break;
        case KeyEvent.VK_A:
        case KeyEvent.VK_LEFT:
            moveL = true;
            break;
        case KeyEvent.VK_S:
        case KeyEvent.VK_DOWN:
            moveB = true;
            break;
        case KeyEvent.VK_D:
        case KeyEvent.VK_RIGHT:
            moveR = true;
            break;
        case KeyEvent.VK_PAGE_DOWN:
            moveD = true;
            break;
        case KeyEvent.VK_PAGE_UP:
            moveU = true;
            break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        switch (key) {
        case KeyEvent.VK_W:
        case KeyEvent.VK_UP:
            moveF = false;
            break;
        case KeyEvent.VK_A:
        case KeyEvent.VK_LEFT:
            moveL = false;
            break;
        case KeyEvent.VK_S:
        case KeyEvent.VK_DOWN:
            moveB = false;
            break;
        case KeyEvent.VK_D:
        case KeyEvent.VK_RIGHT:
            moveR = false;
            break;
        case KeyEvent.VK_PAGE_DOWN:
            moveD = false;
            break;
        case KeyEvent.VK_PAGE_UP:
            moveU = false;
            break;
        }
    }

    @Override
    public void attachToCanvas(GLCanvas canvas) {
        canvas.addKeyListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addMouseListener(this);
        canvas.addMouseWheelListener(this);
    }

    @Override
    public void detachFromCanvas(GLCanvas canvas) {
        canvas.removeKeyListener(this);
        canvas.removeMouseMotionListener(this);
        canvas.removeMouseListener(this);
        canvas.removeMouseWheelListener(this);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() < 0) {
            ui.getCamera().moveLocal(Vec3f.unitZ().times(-1));
        } else {
            ui.getCamera().moveLocal(Vec3f.unitZ());
        }
    }
}
