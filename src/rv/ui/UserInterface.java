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

package rv.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilitiesImmutable;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import js.jogl.view.FPCamera;
import js.math.vector.Vec2f;
import js.math.vector.Vec3f;
import rv.Viewer;
import rv.ui.screens.LiveGameScreen;
import rv.ui.screens.LogfileModeScreen;
import rv.ui.screens.Screen;
import rv.ui.view.CamTargetTracker;
import rv.ui.view.CameraController;
import rv.ui.view.SimsparkController;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * User interface controls
 * 
 * @author Justin Stoecker
 */
public class UserInterface implements KeyListener {
    private Viewer            viewer;
    private FPCamera          camera;
    private DrawingListPanel  poolPanel;
    private CameraController  cameraControl;

    private SceneObjectPicker picker;
    private Screen            overlay;
    private KeyListener[]     tempListeners;
    private Screen            activeScreen;
    private CamTargetTracker  ballTracker;

    public CamTargetTracker getBallTracker() {
        return ballTracker;
    }

    public CameraController getCameraControl() {
        return cameraControl;
    }

    public SceneObjectPicker getObjectPicker() {
        return picker;
    }

    public void showSettings() {
    }

    public Screen getActiveScreen() {
        return activeScreen;
    }

    public void setActiveScreen(Screen activeScreen) {
        GLCanvas canvas = (GLCanvas) viewer.getCanvas();
        if (this.activeScreen != null)
            this.activeScreen.setEnabled(canvas, false);

        this.activeScreen = activeScreen;
        this.activeScreen.setEnabled(canvas, true);
        canvas.addKeyListener(this);
    }

    public DrawingListPanel getShapeSetPanel() {
        return poolPanel;
    }

    public FPCamera getCamera() {
        return camera;
    }

    public UserInterface(Viewer viewer) {
        setLookFeel();
        this.viewer = viewer;

        GLAutoDrawable drawable = viewer.getCanvas();

        camera = initCamera(drawable.getChosenGLCapabilities());
        picker = new SceneObjectPicker(viewer.getWorldModel(), camera);
        poolPanel = new DrawingListPanel(viewer.getDrawings());
    }

    public void init() {
        if (viewer.getMode() == Viewer.Mode.LIVE)
            setActiveScreen(new LiveGameScreen(viewer));
        else
            setActiveScreen(new LogfileModeScreen(viewer));

        ballTracker = new CamTargetTracker(viewer.getWorldModel().getBall(), camera);
        ballTracker.setTetherDist(7);
        ballTracker.setMinTetherDist(4);
    }

    private FPCamera initCamera(GLCapabilitiesImmutable glcaps) {
        Vec3f pos = new Vec3f(0, 7, -10); // 3D camera position
        Vec2f rot = new Vec2f(-40, 180); // camera x,y rotations
        float fov = 45; // field of view (degrees)
        float near = 0.1f; // near clip plane distance
        float far = 200; // far clip plane distance

        camera = new FPCamera(pos, rot, fov, near, far);

        if (glcaps.getStereo()) {
            camera.setFocalLength(8);
            camera.setEyeSeparation(3 / 20.0f);
        }

        SimsparkController ctrl = new SimsparkController(this);
        cameraControl = ctrl;
        viewer.getWorldModel().getGameState().addListener(ctrl);

        return camera;
    }

    public void update(GL2 gl, double elapsedMS) {
        cameraControl.update(elapsedMS);
        camera.update(elapsedMS);

        ballTracker.update();
    }

    public void render(GL2 gl, GLU glu, GLUT glut) {

        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL.GL_BLEND);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(0, viewer.getScreen().w, 0, viewer.getScreen().h);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        activeScreen.render(gl, glu, glut, viewer.getScreen());

        gl.glDisable(GL.GL_BLEND);
    }

    private void setLookFeel() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_F12:
            viewer.takeScreenShot();
            break;
        case KeyEvent.VK_HOME:
            ballTracker.changeMinTetherDist(-0.5f);
            break;
        case KeyEvent.VK_END:
            ballTracker.changeMinTetherDist(0.5f);
            ballTracker.update();
            break;
        default:
            break;
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }
}
