/*
 *  Copyright 2011 Justin Stoecker
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

package jsgl.jogl.view;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import jsgl.math.Maths;
import jsgl.math.vector.Vec2f;
import jsgl.math.vector.Vec3f;

/**
 * A first-person camera with the addition of a thrust component to create a jet
 * pack type of behavior.
 *
 * @author Justin Stoecker
 */
public class JetCamera extends FPCamera
{
	private Vec3f thrust = new Vec3f(0);
	private float thrustSpeed = 0.005f;
	private float maxThrust = 0.05f;
	private float thrustIncrement = 0.0001f;

	public JetCamera(Vec3f pos, Vec2f rot, float fov, float near, float far)
	{
		super(pos, rot, fov, near, far);
	}

	@Override
	public void moveLocal(Vec3f dir)
	{
		Vec3f t = rotation.transform(dir);
		thrust = thrust.plus(t.times(thrustSpeed));
		updateView();
	}

	@Override
	public void moveWorld(Vec3f dir)
	{
		thrust = thrust.plus(dir.times(thrustSpeed));
		updateView();
	}

	private void updateThrust(float elapsedMS)
	{
		for (int i = 0; i < 3; i++) {
			float ti = thrust.get(i);
			if (ti < 0)
				thrust.set(i, Maths.clamp(ti + thrustIncrement, -maxThrust, 0));
			else if (ti > 0)
				thrust.set(i, Maths.clamp(ti - thrustIncrement, 0, maxThrust));
		}
		position = position.plus(thrust);
		updateView();
	}

	public void apply(GL2 gl, GLU glu, Viewport vp, float elapsedMS)
	{
		updateThrust(elapsedMS);
		super.apply(gl, glu, vp);
	}
}
