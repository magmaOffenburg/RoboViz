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

package jsgl.jogl.model;

import com.jogamp.opengl.GL2;
import jsgl.jogl.GLDisposable;
import jsgl.jogl.light.Material;

/**
 * Material used by mesh parts.
 *
 * @author Justin Stoecker
 */
public abstract class MeshMaterial extends Material implements GLDisposable
{
	protected boolean disposed = false;
	protected String name;
	protected boolean containsTransparency = false;

	public String getName()
	{
		return name;
	}

	/**
	 * Override if material has content that must be initialized with an active
	 * OpenGL context
	 */
	public abstract void init(GL2 gl);
}
