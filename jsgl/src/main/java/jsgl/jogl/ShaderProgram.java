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
import com.jogamp.opengl.GL2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A compiled and linked set of vertex and fragment shaders that can be enabled
 * while rendering to replace the default OpenGL vertex and fragment processing
 *
 * @author Justin Stoecker
 */
public class ShaderProgram implements GLDisposable
{
	private static final Logger LOGGER = LogManager.getLogger();

	private boolean disposed;
	private int id;
	private Shader vShader;
	private Shader fShader;

	public int getID()
	{
		return id;
	}

	public Shader getFragmentShader()
	{
		return fShader;
	}

	public Shader getVertexShader()
	{
		return vShader;
	}

	private ShaderProgram(int id, Shader v, Shader f)
	{
		this.id = id;
		this.vShader = v;
		this.fShader = f;
	}

	/**
	 * Creates a fully compiled and linked GLSL shader program. Requires GL
	 * version 2.0 or greater.
	 *
	 * @param gl
	 *           - OpenGL context
	 * @param v
	 *           - vertex shader source code file
	 * @param f
	 *           - fragment shader source code file
	 * @param loader
	 *           - class resource loader
	 * @return Shader program reference if successful; otherwise, returns null
	 */
	public static ShaderProgram create(GL2 gl, String v, String f, ClassLoader loader)
	{
		Shader vShader = Shader.createVertexShader(gl, v, loader.getResourceAsStream(v));
		if (vShader == null)
			return null;
		Shader fShader = Shader.createFragmentShader(gl, f, loader.getResourceAsStream(f));
		if (fShader == null)
			return null;

		int id = gl.glCreateProgram();
		gl.glAttachShader(id, vShader.getID());
		gl.glAttachShader(id, fShader.getID());
		gl.glLinkProgram(id);
		gl.glValidateProgram(id);

		return new ShaderProgram(id, vShader, fShader);
	}

	/** Enables the shader program */
	public void enable(GL2 gl)
	{
		gl.glUseProgram(id);
	}

	/** Disables the shader program */
	public void disable(GL2 gl)
	{
		gl.glUseProgram(0);
	}

	/** Returns the location of a uniform variable */
	public int getUniform(GL2 gl, String name)
	{
		return gl.glGetUniformLocation(id, name);
	}

	/** Removes the shader program and its shaders from memory */
	@Override
	public void dispose(GL gl)
	{
		GL2 gl2 = gl.getGL2();
		gl2.glDetachShader(id, vShader.getID());
		gl2.glDetachShader(id, fShader.getID());
		gl2.glDeleteProgram(id);
		vShader.dispose(gl);
		fShader.dispose(gl);
		disposed = true;
	}

	@Override
	public boolean isDisposed()
	{
		return disposed;
	}

	@Override
	public void finalize()
	{
		if (!disposed)
			LOGGER.warn("ShaderProgram {} was not disposed!", id);
	}

	public static boolean isSupported(GLInfo info)
	{
		return info.getGLVersion() > 2.0;
	}
}
