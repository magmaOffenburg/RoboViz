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
import js.jogl.view.Viewport;
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

    public void update(Viewport screen) {
        if (!enabled || target.getPosition() == null)
            return;
        float scale = (float) (1 - (0.02f * playbackSpeed));
        Vec3f cameraTarget = offsetTargetPosition(target.getPosition());

        // Percentage of screen near edges where the scale is increased when
        // target is within this threshold percentage of the screen's edge
        float SCREEN_THRESH_PERC = 0.05f;

        // Factor to increase scale by
        float SCALE_FACTOR = 2.0f;

        // Get position of target relative to screen
        Vec3f screenPos = camera.project(target.getPosition(), screen);

        if (screenPos.x < screen.w * SCREEN_THRESH_PERC
                || screenPos.x > screen.w * (1 - SCREEN_THRESH_PERC)
                || screenPos.y < screen.h * SCREEN_THRESH_PERC
                || screenPos.y > screen.h * (1 - SCREEN_THRESH_PERC)) {
            // Outside of SCREEN_THRESH_PERC boundaries so increase scale by
            // SCALE_FACTOR
            scale = (float) (1 - (0.02f * playbackSpeed * SCALE_FACTOR));
        }

        camera.setPosition(Vec3f.lerp(cameraTarget, camera.getPosition(), scale));

        camera.setRotation(new Vec2f(-30, 180));

    }

    /**
     * Tries to keep the ball in the middle of the screen (unless we're near a field edge, then it
     * shifts the position a bit to fill as much of the screen with the field as possible)
     */
    private Vec3f offsetTargetPosition(Vec3f targetPos) {
        float halfLength = gs.getFieldLength() / 2;
        float halfWidth = gs.getFieldWidth() / 2;

        float xOffset = 4 * fuzzyValue(targetPos.x, -halfLength, halfLength);
        float zOffset = -8 + 3 * fuzzyValue(targetPos.z, -halfWidth, halfWidth);

        Vec3f offsetPos = targetPos.clone();
        offsetPos.add(Vec3f.unitX().times(xOffset));
        offsetPos.add(Vec3f.unitY().times(4));
        offsetPos.add(Vec3f.unitZ().times(zOffset));
        return offsetPos;
    }

    private float fuzzyValue(float value, float lower, float upper) {
        if (value <= lower)
            return 1;
        if (value >= upper)
            return 0;
        return weight(1 - ((value - lower) / (upper - lower)));
    }

    /** maps t values from 0...1 to -1...1 using a quadratic function */
    private float weight(float t) {
        float result = (float) -(Math.sqrt(1 - Math.pow(2 * t - 1, 2)) - 1);
        if (t < 0.5)
            result *= -1;
        return result;
    }
}
