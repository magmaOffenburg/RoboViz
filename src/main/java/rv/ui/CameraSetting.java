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

package rv.ui;

import jsgl.math.vector.Vec2f;
import jsgl.math.vector.Vec3f;

public class CameraSetting
{
	private final Vec3f position;
	private final Vec2f rotation;

	public Vec3f getPosition()
	{
		return position;
	}

	public Vec2f getRotation()
	{
		return rotation;
	}

	public CameraSetting(Vec3f position, Vec2f rotation)
	{
		this.position = position;
		this.rotation = rotation;
	}
}
