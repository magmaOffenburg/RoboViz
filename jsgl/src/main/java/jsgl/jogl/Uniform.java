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

import com.jogamp.opengl.GL2;
import java.nio.FloatBuffer;
import jsgl.math.vector.Matrix;
import jsgl.math.vector.Vec2f;
import jsgl.math.vector.Vec3f;
import jsgl.math.vector.Vec4f;

/**
 * References a uniform variable used in a shader. Values wrapped in these
 * objects are stored locally in RAM; they must be copied to the shader program
 * when the program is active and the OpenGL context is current.
 * <p>
 * Creating a uniform variable requires the OpenGL context to be current and the
 * shader program to be active, as well.
 *
 * @author justin
 */
public abstract class Uniform
{
	protected final int location;
	protected final String name;

	public Uniform(GL2 gl, ShaderProgram prog, String name)
	{
		location = prog.getUniform(gl, name);
		this.name = name;
	}

	/**
	 * Updates the value in the actual shader program, requiring a current GL
	 * context and the program to be enabled
	 */
	public abstract void update(GL2 gl);

	/**
	 * Uniform variable type "mat4" (4x4 matrix)
	 */
	public static class Mat4 extends Uniform
	{
		private FloatBuffer matrix;

		public Mat4(GL2 gl, ShaderProgram prog, String name, Matrix m)
		{
			super(gl, prog, name);
			setValue(gl, m.wrapf());
		}

		public Mat4(GL2 gl, ShaderProgram prog, String name, FloatBuffer m)
		{
			super(gl, prog, name);
		}

		public void update(GL2 gl)
		{
			gl.glUniformMatrix4fv(location, 1, false, matrix);
		}

		/** Sets the value and updates it in the shader program */
		public void setValue(GL2 gl, FloatBuffer m)
		{
			this.matrix = m;
			update(gl);
		}

		/** Sets the locally stored value */
		public void setValue(FloatBuffer m)
		{
			this.matrix = m;
		}

		/** Sets the value and updates it in the shader program */
		public void setValue(GL2 gl, Matrix m)
		{
			this.matrix = m.wrapf();
			update(gl);
		}

		/** Sets the locally stored value */
		public void setValue(Matrix m)
		{
			this.matrix = m.wrapf();
		}
	}

	/**
	 * Uniform variable type "bool"
	 */
	public static class Bool extends Uniform
	{
		private boolean value = false;

		public Bool(GL2 gl, ShaderProgram prog, String name, boolean b)
		{
			super(gl, prog, name);
			setValue(gl, b);
		}

		@Override
		public void update(GL2 gl)
		{
			gl.glUniform1i(location, value ? 1 : 0);
		}

		/** Sets the value and updates it in the shader program */
		public void setValue(GL2 gl, boolean b)
		{
			value = b;
			update(gl);
		}

		/** Sets the locally stored value */
		public void setValue(boolean b)
		{
			value = b;
		}

		/** Returns the locally stored value */
		public boolean getValue()
		{
			return value;
		}
	}

	/**
	 * Uniform variable type "int"
	 */
	public static class Int extends Uniform
	{
		private int value = 0;

		public Int(GL2 gl, ShaderProgram prog, String name, int i)
		{
			super(gl, prog, name);
			setValue(gl, i);
		}

		@Override
		public void update(GL2 gl)
		{
			gl.glUniform1i(location, value);
		}

		/** Sets the value and updates it in the shader program */
		public void setValue(GL2 gl, int i)
		{
			value = i;
			update(gl);
		}

		/** Sets the locally stored value */
		public void setValue(int i)
		{
			value = i;
		}

		/** Returns the locally stored value */
		public int getValue()
		{
			return value;
		}
	}

	/**
	 * Uniform variable type "float"
	 */
	public static class Float extends Uniform
	{
		private float value = 0f;

		public Float(GL2 gl, ShaderProgram prog, String name, float f)
		{
			super(gl, prog, name);
			setValue(gl, f);
		}

		@Override
		public void update(GL2 gl)
		{
			gl.glUniform1f(location, value);
		}

		/** Sets the value and updates it in the shader program */
		public void setValue(GL2 gl, float f)
		{
			value = f;
			update(gl);
		}

		/** Sets the locally stored value */
		public void setValue(float f)
		{
			value = f;
		}

		/** Returns the locally stored value */
		public float getValue()
		{
			return value;
		}
	}

	/**
	 * Uniform variable type "vec2"
	 */
	public static class Vec2 extends Uniform
	{
		private final float[] value = new float[2];

		public Vec2(GL2 gl, ShaderProgram prog, String name, Vec2f value)
		{
			super(gl, prog, name);
			setValue(gl, value);
		}

		public Vec2(GL2 gl, ShaderProgram prog, String name, float[] value)
		{
			super(gl, prog, name);
			setValue(gl, value);
		}

		@Override
		public void update(GL2 gl)
		{
			gl.glUniform2fv(location, 1, value, 0);
		}

		/** Sets the value and updates it in the shader program */
		public void setValue(GL2 gl, Vec2f v)
		{
			System.arraycopy(v.getVals(), 0, value, 0, 2);
			update(gl);
		}

		/** Sets the value and updates it in the shader program */
		public void setValue(GL2 gl, float[] v)
		{
			System.arraycopy(v, 0, value, 0, 2);
			update(gl);
		}

		/** Sets the locally stored value */
		public void setValue(Vec2f v)
		{
			System.arraycopy(v.getVals(), 0, value, 0, 2);
		}

		/** Sets the locally stored value */
		public void setValue(float[] v)
		{
			System.arraycopy(v, 0, value, 0, 2);
		}

		/** Returns the locally stored value */
		public float[] getValue()
		{
			return value;
		}
	}

	/**
	 * Uniform variable type "vec3"
	 */
	public static class Vec3 extends Uniform
	{
		private final float[] value = new float[3];

		public Vec3(GL2 gl, ShaderProgram prog, String name, Vec3f value)
		{
			super(gl, prog, name);
			setValue(gl, value);
		}

		public Vec3(GL2 gl, ShaderProgram prog, String name, float[] value)
		{
			super(gl, prog, name);
			setValue(gl, value);
		}

		@Override
		public void update(GL2 gl)
		{
			gl.glUniform3fv(location, 1, value, 0);
		}

		/** Sets the value and updates it in the shader program */
		public void setValue(GL2 gl, Vec3f v)
		{
			System.arraycopy(v.getVals(), 0, value, 0, 3);
			update(gl);
		}

		/** Sets the value and updates it in the shader program */
		public void setValue(GL2 gl, float[] v)
		{
			System.arraycopy(v, 0, value, 0, 3);
			update(gl);
		}

		/** Sets the locally stored value */
		public void setValue(Vec3f v)
		{
			System.arraycopy(v.getVals(), 0, value, 0, 3);
		}

		/** Sets the locally stored value */
		public void setValue(float[] v)
		{
			System.arraycopy(v, 0, value, 0, 3);
		}

		/** Returns the locally stored value */
		public float[] getValue()
		{
			return value;
		}
	}

	/**
	 * Uniform variable type "vec4"
	 */
	public static class Vec4 extends Uniform
	{
		private final float[] value = new float[4];

		public Vec4(GL2 gl, ShaderProgram prog, String name, Vec4f value)
		{
			super(gl, prog, name);
			setValue(gl, value);
		}

		public Vec4(GL2 gl, ShaderProgram prog, String name, float[] value)
		{
			super(gl, prog, name);
			setValue(gl, value);
		}

		@Override
		public void update(GL2 gl)
		{
			gl.glUniform4fv(location, 1, value, 0);
		}

		/** Sets the value and updates it in the shader program */
		public void setValue(GL2 gl, Vec4f v)
		{
			System.arraycopy(v.getVals(), 0, value, 0, 4);
			update(gl);
		}

		/** Sets the value and updates it in the shader program */
		public void setValue(GL2 gl, float[] v)
		{
			System.arraycopy(v, 0, value, 0, 4);
			update(gl);
		}

		/** Sets the locally stored value */
		public void setValue(Vec4f v)
		{
			System.arraycopy(v.getVals(), 0, value, 0, 4);
		}

		/** Sets the locally stored value */
		public void setValue(float[] v)
		{
			System.arraycopy(v, 0, value, 0, 4);
		}

		/** Returns the locally stored value */
		public float[] getValue()
		{
			return value;
		}
	}
}
