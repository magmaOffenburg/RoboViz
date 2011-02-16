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

import java.util.ArrayList;

import js.jogl.view.FPCamera;
import js.math.vector.Vec3f;
import rv.world.ISelectable;

/**
 * Smooths out camera tracking of an object by taking previous positions into
 * consideration
 * 
 * @author justin
 * 
 */
public class CamTargetTracker {

    private boolean          enabled           = false;
    private FPCamera         camera;
    private ISelectable      target;
    private int              maxSaved          = 3;
    private ArrayList<Vec3f> saved             = new ArrayList<Vec3f>(maxSaved);
    private float            tetherDist        = -1;
    private float            tetherVelocity    = 0;
    private float            tetherChange      = 0.05f;
    private float            maxTetherVelocity = 2f;
    private float            slowTetherRange   = 4;
    private float            minTetherDist     = 2.5f;

    public boolean isEnabled() {
        return enabled;
    }

    public void setMinTetherDist(float minTetherDist) {
        this.minTetherDist = minTetherDist;
        slowTetherRange = (tetherDist - minTetherDist) / 2 + minTetherDist;
    }

    public void setTetherDist(float tetherDist) {
        this.tetherDist = tetherDist;
        slowTetherRange = (tetherDist - minTetherDist) / 2 + minTetherDist;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggleEnabled() {
        enabled = !enabled;
    }

    public ISelectable getTarget() {
        return target;
    }

    public void setTarget(ISelectable target) {
        this.target = target;
    }

    public CamTargetTracker(ISelectable target, FPCamera camera) {
        this.target = target;
        this.camera = camera;
    }

    private void savePosition(Vec3f pos) {
        if (pos == null)
            return;
        if (saved.size() > maxSaved)
            saved.remove(0);
        saved.add(pos.clone());
    }

    private Vec3f getCameraTarget() {
        if (saved.size() == 0)
            return target.getPosition();

        Vec3f camTarget = new Vec3f(0);
        for (Vec3f v : saved)
            camTarget.add(v);
        camTarget.div(saved.size());

        return camTarget;
    }

    private void applyTethering(Vec3f targetPos) {
        // keeps the camera smoothly following the target as it moves across
        // the field
        Vec3f v = targetPos.minus(camera.getPosition());
        v.y = 0;
        float d = v.length();
        if (d > minTetherDist) {
            float scale = 1.0f;
            if (d > slowTetherRange) {
                scale = 1.0f;
            } else {
                scale = (d - minTetherDist) / (slowTetherRange - minTetherDist);
            }
            tetherVelocity = Math.min(scale * (tetherVelocity + tetherChange),
                    maxTetherVelocity);
            camera.moveWorld(v.normalize().times(tetherVelocity));
        }
        tetherVelocity = Math.max(tetherVelocity - tetherChange, 0);
    }

    public void update() {
        if (!enabled)
            return;

        Vec3f targetPos = getCameraTarget();
        if (targetPos != null) {
            camera.focus(targetPos);
            if (tetherDist > 0)
                applyTethering(targetPos);
        }
        savePosition(target.getPosition());
    }
}
