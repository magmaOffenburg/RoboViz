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

package jsgl.jogl.util.shader;

import com.jogamp.opengl.GL2;
import javax.swing.JPanel;
import jsgl.jogl.Uniform;

public abstract class ShaderWidget extends JPanel
{
	public String uniformName;
	protected Uniform uVariable;
	protected boolean valueChanged = false;

	public String getUniformName()
	{
		return uniformName;
	}

	public void setUniformRef(Uniform u)
	{
		this.uVariable = u;
	}

	/**
	 * Retrieves value from widget and applies it to the uniform variable it
	 * references if a change has occurred
	 */
	public void updateShaderValue(GL2 gl)
	{
		if (valueChanged)
			uVariable.update(gl);
		valueChanged = false;
	}
}
