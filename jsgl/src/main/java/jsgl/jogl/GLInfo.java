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

package jsgl.jogl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;

/**
 * Checks the OpenGL / shading language versions and queries which extensions
 * are available.
 *
 * @author Justin Stoecker
 */
public class GLInfo
{
	private float glVersion;
	private float slVersion;
	private String basicInfo;
	private String extensions;

	public float getGLVersion()
	{
		return glVersion;
	}

	public float getSLVersion()
	{
		return slVersion;
	}

	public GLInfo(GL gl)
	{
		String vendor = gl.glGetString(GL.GL_VENDOR);
		String renderer = gl.glGetString(GL.GL_RENDERER);
		String gl_version = gl.glGetString(GL.GL_VERSION);
		String sl_version = gl.glGetString(GL2ES2.GL_SHADING_LANGUAGE_VERSION);

		StringBuilder b = new StringBuilder();
		b.append("*** OpenGL Support Info ***\n");
		b.append(String.format("-%-25s: %s\n", "GL Version", gl_version));
		b.append(String.format("-%-25s: %s\n", "SL Version", sl_version));
		b.append(String.format("-%-25s: %s\n", "Vendor", vendor));
		b.append(String.format("-%-25s: %s\n", "Renderer", renderer));
		basicInfo = b.toString();

		if (gl_version != null)
			glVersion = Float.parseFloat(gl_version.substring(0, 3));
		if (sl_version != null)
			slVersion = Float.parseFloat(sl_version.substring(0, 3));

		extensions = gl.glGetString(GL.GL_EXTENSIONS);
	}

	/**
	 * Checks if a particular extension is supported by the client's OpenGL
	 * implementation.
	 */
	public boolean extSupported(String ext)
	{
		return extensions == null ? false : extensions.contains(ext);
	}

	/**
	 * Prints out basic info about client's OpenGL implementation. This requires
	 * that the client info has been checked by calling GLInfo.check(GL gl).
	 */
	public void print()
	{
		System.out.println(basicInfo);
	}

	/**
	 * List all OpenGL extensions supported by the client
	 */
	public void printExtensions()
	{
		System.out.println(extensions);
	}
}
