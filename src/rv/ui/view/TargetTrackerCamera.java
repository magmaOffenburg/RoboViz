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

    private float angle;

    public void update() {
        angle += 0.1f;

        Vec2f rotatedPoint = rotate(new Vec2f(0, 0), new Vec2f(22f, 0f), angle);

        camera.setPosition(new Vec3f(rotatedPoint.y, 10f, rotatedPoint.x));
        camera.setRotation(new Vec2f(-35, angle));
    }

    private Vec2f rotate(Vec2f pivot, Vec2f point, float angle) {
        float radians = (float) Math.toRadians(angle);
        float sin = (float) Math.sin(radians);
        float cos = (float) Math.cos(radians);

        float dx = point.x - pivot.x;
        float dy = point.y - pivot.y;
        float x = cos * dx - sin * dy + pivot.x;
        float y = sin * dx + cos * dy + pivot.y;

        return new Vec2f(x, y);
    }
}
