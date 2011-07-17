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

package rv.effects;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import js.jogl.GLDisposable;
import js.jogl.light.DirLight;
import js.jogl.view.Viewport;
import js.math.vector.Vec3f;
import rv.Configuration;
import rv.Viewer;
import rv.content.ContentManager;
import rv.world.rendering.ShadowMapRenderer;
import rv.world.rendering.ShadowMapRenderer.LightShadowVolume;

/**
 * Applies post-processing effects to an input texture; merge this with RENDERER?
 * 
 * @author Justin Stoecker
 */
public class EffectManager implements GLDisposable {

    private boolean           disposed = false;
    private Bloom             bloom;
    private ShadowMapRenderer shadowRenderer;

    public Bloom getBloom() {
        return bloom;
    }

    public ShadowMapRenderer getShadowRenderer() {
        return shadowRenderer;
    }

    public void init(GL2 gl, Viewer viewer, Viewport screen, Configuration.Graphics config,
            ContentManager cm) {

        // configure sun
        Vec3f lightPos = new Vec3f(-8, 7, -6);
        Vec3f lightDir = lightPos.times(-1).normalize();
        DirLight light = new DirLight(lightDir);
        LightShadowVolume sun = new LightShadowVolume(light, lightPos, new Vec3f(0, 0, 0),
                Vec3f.unitY(), 24, 24, 30);

        if (config.useBloom()) {
            bloom = new Bloom();
            boolean success = bloom.init(gl, screen, cm, config);
            if (!success)
                bloom = null;
            else
                viewer.addWindowResizeListener(bloom);
        }

        if (config.useShadows()) {
            shadowRenderer = new ShadowMapRenderer(sun);
            if (!shadowRenderer.init(gl, config, cm))
                shadowRenderer = null;
        }
    }

    /**
     * Renders a screen-aligned quad with identity viewing / projection matrices
     */
    public static void renderScreenQuad(GL2 gl) {

        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(0, 0);
        gl.glVertex2f(-1, -1);
        gl.glTexCoord2f(1, 0);
        gl.glVertex2f(1, -1);
        gl.glTexCoord2f(1, 1);
        gl.glVertex2f(1, 1);
        gl.glTexCoord2f(0, 1);
        gl.glVertex2f(-1, 1);
        gl.glEnd();

        gl.glPopMatrix();
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glPopMatrix();
    }

    @Override
    public void dispose(GL gl) {
        if (bloom != null)
            bloom.dispose(gl);
        if (shadowRenderer != null)
            shadowRenderer.dispose(gl);

        disposed = true;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
