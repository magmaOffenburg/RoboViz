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

package rv.world.objects;

import com.jogamp.opengl.GL2;
import jsgl.math.vector.Vec3f;
import rv.content.Model;
import rv.world.ModelObject;

/**
 * Box that is centered on camera and provides background scenery
 *
 * @author Justin Stoecker
 */
public class SkyBox extends ModelObject
{
	private Vec3f position = new Vec3f(0);

	public void setPosition(Vec3f position)
	{
		this.position = position;
	}

	public SkyBox(Model model)
	{
		super(model);
	}

	@Override
	public void render(GL2 gl)
	{
		if (!model.isLoaded())
			return;
		// no need to write to depth buffer since everything should be drawn
		// over the skybox pixels
		gl.glDepthMask(false);

		// center the skybox on the camera
		gl.glPushMatrix();
		gl.glTranslatef(position.x, position.y, position.z);
		model.getMesh().render(gl, modelMatrix);
		gl.glPopMatrix();
		gl.glDepthMask(true);
	}
}
