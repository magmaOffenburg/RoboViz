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

import jsgl.jogl.view.FPCamera;
import jsgl.jogl.view.Viewport;
import jsgl.math.vector.Vec2f;
import jsgl.math.vector.Vec3f;
import rv.comm.rcssserver.GameState;
import rv.world.ISelectable;
import rv.world.objects.Agent;
import rv.world.objects.Ball;

public class TargetTrackerCamera
{
	private boolean enabled = false;
	private final FPCamera camera;
	private GameState gs;

	private ISelectable target;
	private double playbackSpeed = 1;
	private Vec3f lastScreenPos;

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public ISelectable getTarget()
	{
		return target;
	}

	public void setTarget(ISelectable target)
	{
		this.target = target;
	}

	public void setPlaybackSpeed(double playbackSpeed)
	{
		this.playbackSpeed = playbackSpeed;
	}

	public TargetTrackerCamera(FPCamera camera, GameState gs)
	{
		this.camera = camera;
		this.gs = gs;
		lastScreenPos = null;
	}

	public void update(Viewport screen)
	{
		if (!enabled || target == null || target.getPosition() == null)
			return;

		float scale = (float) (1 - (0.02f * playbackSpeed));
		if (target instanceof Agent) {
			scale = 0.95f;
		} else {
			scale = scaleWithBallSpeed(screen, scale);
		}

		Vec3f cameraTarget = offsetTargetPosition(target.getPosition());

		camera.setPosition(Vec3f.lerp(cameraTarget, camera.getPosition(), scale));
		camera.setRotation(new Vec2f(-30, 180));
	}

	private float scaleWithBallSpeed(Viewport screen, float scale)
	{
		// Get position of target relative to screen
		Vec3f screenPos = camera.project(target.getPosition(), screen);

		if (lastScreenPos == null) {
			lastScreenPos = screenPos;
		}

		// Maximum factor that velocity can increase scale by
		float VEL_SCALE_FACTOR_MAX = 12.0f;

		// Amount that screen velocity is multiplied by when determining scale
		float VEL_SCALE_FACTOR = 0.003f;

		double screenVel =
				Math.sqrt(Math.pow(lastScreenPos.x - screenPos.x, 2.0) + Math.pow(lastScreenPos.y - screenPos.y, 2.0));
		scale = (float) Math.max(
				Math.min(1 - screenVel * VEL_SCALE_FACTOR, scale), 1 - (0.02f * playbackSpeed * VEL_SCALE_FACTOR_MAX));
		lastScreenPos = screenPos;

		return scale;
	}

	/**
	 * Tries to keep the ball in the middle of the screen (unless we're near a field edge, then it
	 * shifts the position a bit to fill as much of the screen with the field as possible)
	 */
	private Vec3f offsetTargetPosition(Vec3f targetPos)
	{
		float halfLength = gs.getFieldLength() / 2;
		float halfWidth = gs.getFieldWidth() / 2;

		float zoom = target instanceof Ball ? 1 : 4;

		float xOffset = 4 * fuzzyValue(targetPos.x, -halfLength, halfLength);
		float baseZOffset = -8 / zoom;
		float zOffset = baseZOffset + 3 * fuzzyValue(targetPos.z, -halfWidth, halfWidth);

		Vec3f offsetPos = targetPos.clone();
		offsetPos.add(Vec3f.unitX().times(xOffset));
		offsetPos.add(Vec3f.unitY().times(4 / zoom));
		offsetPos.add(Vec3f.unitZ().times(zOffset));
		return offsetPos;
	}

	private float fuzzyValue(float value, float lower, float upper)
	{
		if (value <= lower)
			return 1;
		if (value >= upper)
			return 0;
		return weight(1 - ((value - lower) / (upper - lower)));
	}

	/** maps t values from 0...1 to -1...1 using a quadratic function */
	private float weight(float t)
	{
		float result = (float) -(Math.sqrt(1 - Math.pow(2 * t - 1, 2)) - 1);
		if (t < 0.5)
			result *= -1;
		return result;
	}
}
