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

import js.jogl.view.FPCamera;
import js.math.vector.Vec2f;
import js.math.vector.Vec3f;
import rv.comm.rcssserver.GameState;
import rv.world.ISelectable;

public class TargetTrackerCamera {

    private boolean        enabled = false;
    private final FPCamera camera;
    private GameState      gs;
    private ISelectable    target;

    public boolean isEnabled() {
        return enabled;
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

    public TargetTrackerCamera(ISelectable target, FPCamera camera, GameState gs) {
        this.target = target;
        this.camera = camera;
        this.gs = gs;
    }

    public void update() {
        if (!enabled)
            return;

        Vec3f targetPos = target.getPosition();
        Vec3f cameraPos = camera.getPosition();
        Vec3f newPos = Vec3f.lerp(cameraPos, targetPos, 0.02f);

        float halfLength = gs.getFieldLength() / 2;
        float halfWidth = gs.getFieldWidth() / 2;

        float xFactor = (fuzzyValue(targetPos.x, -halfLength, halfLength) - 0.5f) * 2;
        float xOffset = Math.signum(xFactor) * 0.08f * circIn(xFactor);

        float zFactor = (fuzzyValue(targetPos.z, -halfWidth, halfWidth) - 0.5f) * 2;
        float zOffset = -0.16f + (Math.signum(zFactor) * 0.06f * circIn(zFactor));

        newPos.add(Vec3f.unitX().times(xOffset));
        newPos.add(Vec3f.unitY().times(0.08f));
        newPos.add(Vec3f.unitZ().times(zOffset));

        camera.setPosition(newPos);
        camera.setRotation(new Vec2f(-30, 180));
    }

    private float fuzzyValue(float value, float lower, float upper) {
        if (value <= lower)
            return 1;
        if (value >= upper)
            return 0;
        return 1 - ((value - lower) / (upper - lower));
    }

    private float circIn(float t) {
        return (float) -(Math.sqrt(1 - t * t) - 1);
    }
}
