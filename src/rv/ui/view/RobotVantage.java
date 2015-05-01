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

import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import js.jogl.view.Camera3D;
import js.jogl.view.Viewport;
import js.math.vector.Matrix;
import js.math.vector.Vec3f;
import rv.world.objects.Agent;

public class RobotVantage extends Camera3D implements Agent.ChangeListener {

    private static final float FOV_DEGREES = 120.0f;

    private Agent              agent;

    public RobotVantage(Agent agent, float near, float far) {
        super(agent.getHeadCenter(), near, far);
        this.agent = agent;
        agent.addChangeListener(this);
        fovY = FOV_DEGREES;
        updateView();
    }

    public void detach() {
        agent.removeChangeListener(this);
    }

    @Override
    protected void updateView() {
        Matrix m = agent.getHeadTransform();
        Vec3f c = agent.getHeadCenter();
        forward = agent.getHeadDirection();
        up = m.transform(new Vec3f(0, 1, 0));
        right = forward.cross(up).normalize();
        viewMatrix = Matrix.createLookAt(c.x, c.y, c.z, c.x + forward.x, c.y + forward.y, c.z
                + forward.z, 0, 1, 0);
    }

    @Override
    public void apply(GL2 gl, GLU glu, Viewport vp) {
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(fovY, 1, near, far);

        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glLoadMatrixd(viewMatrix.wrap());
    }

    @Override
    public void applyLeft(GL2 gl, GLU glu, Viewport screen) {
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();
        screen.apply(gl);

        double wd2 = near * Math.tan(Math.toRadians(fovX) / 2);
        double top = wd2;
        double bottom = -wd2;
        double left = -screen.getAspect() * wd2 + 0.5 * eyeSep * near / focalLength;
        double right = screen.getAspect() * wd2 + 0.5 * eyeSep * near / focalLength;
        gl.glFrustum(left, right, bottom, top, near, far);

        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();
        Vec3f eyeL = position.minus(this.right.times(eyeSep / 2));
        Vec3f t = eyeL.plus(forward);
        glu.gluLookAt(eyeL.x, eyeL.y, eyeL.z, t.x, t.y, t.z, up.x, up.y, up.z);

        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glLoadMatrixd(viewMatrix.wrap());
    }

    @Override
    public void applyRight(GL2 gl, GLU glu, Viewport screen) {
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();
        screen.apply(gl);

        double wd2 = near * Math.tan(Math.toRadians(fovX) / 2);
        double top = wd2;
        double bottom = -wd2;
        double left = -screen.getAspect() * wd2 - 0.5 * eyeSep * near / focalLength;
        double right = screen.getAspect() * wd2 - 0.5 * eyeSep * near / focalLength;
        gl.glFrustum(left, right, bottom, top, near, far);

        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();
        Vec3f eyeR = position.plus(this.right.times(eyeSep / 2));
        Vec3f t = eyeR.plus(forward);

        Vec3f c = agent.getHeadCenter().minus(this.right.times(eyeSep / 2));
        Matrix viewL = Matrix.createLookAt(c.x, c.y, c.z, c.x + forward.x, c.y + forward.y, c.z
                + forward.z, 0, 1, 0);
        gl.glLoadIdentity();
        gl.glLoadMatrixd(viewMatrix.wrap());
    }

    @Override
    public void addListeners(GLCanvas canvas) {
    }

    @Override
    public void removeListeners(GLCanvas canvas) {
    }

    public void transformChanged(Matrix t) {
        updateView();
    }
}
