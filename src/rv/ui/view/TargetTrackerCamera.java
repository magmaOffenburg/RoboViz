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

    private boolean        enabled       = false;
    private final FPCamera camera;
    private GameState      gs;
    private ISelectable    target;
    private double         playbackSpeed = 1;

    public void toggleEnabled() {
        enabled = !enabled;
    }

    public void setPlaybackSpeed(double playbackSpeed) {
        this.playbackSpeed = playbackSpeed;
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

        float halfLength = gs.getFieldLength() / 2;
        float halfWidth = gs.getFieldWidth() / 2;

        float xFactor = fuzzyValue(targetPos.x, -halfLength, halfLength);
        float xOffset = 4 * weight(xFactor);

        float zFactor = fuzzyValue(targetPos.z, -halfWidth, halfWidth);
        float zOffset = -8 + 3 * weight(zFactor);

        targetPos.add(Vec3f.unitX().times(xOffset));
        targetPos.add(Vec3f.unitY().times(4));
        targetPos.add(Vec3f.unitZ().times(zOffset));

        Vec3f newPos = Vec3f.lerp(targetPos, cameraPos, (float) (1 - (0.02f * playbackSpeed)));

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

    /** maps t values from 0...1 to -1...1 using a quadratic function */
    private float weight(float t) {
        float result = (float) -(Math.sqrt(1 - Math.pow(2 * t - 1, 2)) - 1);
        if (t < 0.5)
            result *= -1;
        return result;
    }
}
