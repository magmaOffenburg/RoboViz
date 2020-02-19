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

import jsgl.math.vector.Matrix;
import jsgl.math.vector.Vec3f;
import rv.world.objects.Agent;

public class RobotVantageThirdPerson extends RobotVantageBase
{
	private static final int CAMERA_AVERAGE = 20;
	private Vec3f[] avgPos;
	private Vec3f[] avgForward;
	private int ct = 0;
	private Vec3f lastAvgF = new Vec3f(0);

	public RobotVantageThirdPerson(Agent agent, int fovDegrees)
	{
		super(agent, fovDegrees);
		avgPos = new Vec3f[CAMERA_AVERAGE];
		avgForward = new Vec3f[CAMERA_AVERAGE];
		updateView();
	}

	public void detach()
	{
		agent.removeChangeListener(this);
	}

	@Override
	protected void updateView()
	{
		Matrix m = agent.getHeadTransform();
		Vec3f torsoDirection = agent.getTorsoDirection();
		if (m == null || torsoDirection == null) {
			return;
		}
		forward = agent.getTorsoDirection().normalize();

		// for side view
		// forward = new Vec3f(-forward.z, -forward.y, -forward.x);

		Vec3f head = agent.getHeadCenter();

		// Only update if robot is not fallen. Height might need to be tuned
		// for shorter robot stances.
		boolean updateForward = head.y > .45 || ct <= avgPos.length;
		head.y = 1;

		avgPos[ct % avgPos.length] = head;
		if (updateForward) {
			avgForward[ct % avgPos.length] = forward;
		}
		Vec3f avg = new Vec3f(0);
		Vec3f avgF = lastAvgF;
		if (updateForward) {
			avgF = new Vec3f(0);
		}
		for (int i = 0; i < Math.min(ct + 1, avgPos.length); ++i) {
			avg = avg.plus(avgPos[i]);
			if (updateForward) {
				avgF = avgF.plus(avgForward[i]);
			}
		}
		avg = avg.times(1.0f / Math.min(ct + 1, avgPos.length));
		if (updateForward) {
			avgF = avgF.times(1.0f / Math.min(ct + 1, avgPos.length));
			lastAvgF = avgF;
		}
		++ct;

		up = m.transform(new Vec3f(0, 1, 0));
		right = forward.cross(up).normalize();

		Vec3f camPos = avg.minus(avgF.times(2));
		camPos.y = 2;

		viewMatrix = Matrix.createLookAt(camPos.x, camPos.y, camPos.z, avg.x, avg.y, avg.z, 0, 1, 0);
	}
}
