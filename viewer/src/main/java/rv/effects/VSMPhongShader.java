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

package rv.effects;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import jsgl.jogl.ShaderProgram;
import jsgl.jogl.Texture2D;
import jsgl.jogl.Uniform;
import jsgl.math.vector.Matrix;

/**
 * Variance shadow mapping w/ Phong illumination shader. Wraps underlying ShaderProgram and gives
 * access to its uniform variables.
 *
 * @author justin
 */
public class VSMPhongShader
{
	// Used to transform normalized device coordinates in [-1,1] to [0,1]
	private static final Matrix BIAS_MATRIX = new Matrix(new double[] {
			0.5,
			0.0,
			0.0,
			0.0,
			0.0,
			0.5,
			0.0,
			0.0,
			0.0,
			0.0,
			0.5,
			0.0,
			0.5,
			0.5,
			0.5,
			1.0,
	});

	private final ShaderProgram prog;
	private Uniform.Mat4 modelMatrix;
	private Uniform.Mat4 lvpbMatrix;

	/** Uploads modelMatrix of geometry to be rendered */
	public void setModelMatrix(GL2 gl, Matrix m)
	{
		modelMatrix.setValue(gl, m);
	}

	/** Uploads viewProjection matrix used by light */
	public void setLightViewProjection(GL2 gl, Matrix m)
	{
		lvpbMatrix.setValue(gl, BIAS_MATRIX.times(m));
	}

	/** Uploads shadow map texture (assumes shader is enabled!) */
	public void setShadowMap(GL2 gl, Texture2D shadowMap)
	{
		if (shadowMap == null)
			return;
		gl.glActiveTexture(GL.GL_TEXTURE1);
		shadowMap.bind(gl);
		gl.glActiveTexture(GL.GL_TEXTURE0);
	}

	private VSMPhongShader(ShaderProgram prog)
	{
		this.prog = prog;
	}

	public static VSMPhongShader create(GL2 gl)
	{
		ShaderProgram prog = ShaderProgram.create(
				gl, "shaders/vsm_phong.vert", "shaders/vsm_phong.frag", VSMPhongShader.class.getClassLoader());

		if (prog == null)
			return null;

		VSMPhongShader shader = new VSMPhongShader(prog);

		Matrix i = Matrix.createIdentity();
		prog.enable(gl);
		shader.modelMatrix = new Uniform.Mat4(gl, prog, "modelMatrix", i);
		shader.lvpbMatrix = new Uniform.Mat4(gl, prog, "lightViewProjectionBias", i);
		gl.glUniform1i(prog.getUniform(gl, "diffuseTexture"), 0);
		gl.glUniform1i(prog.getUniform(gl, "shadowTexture"), 1);
		prog.disable(gl);

		return shader;
	}

	public void enable(GL2 gl)
	{
		prog.enable(gl);
	}

	public void disable(GL2 gl)
	{
		prog.disable(gl);
	}

	public void dispose(GL gl)
	{
		prog.dispose(gl);
	}
}
