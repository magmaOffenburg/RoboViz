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

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL3;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates a compiled GLSL shader from a source code. Supports both OpenGL 2.0+
 * shaders and the ARB extension versions.
 *
 * @author Justin Stoecker
 */
public class Shader implements GLDisposable
{
	private static final Logger LOGGER = LogManager.getLogger();

	private boolean disposed = false;
	private int id;
	private String fileName;
	private String[] srcLines;

	public int getID()
	{
		return id;
	}

	public String getFileName()
	{
		return fileName;
	}

	/**
	 * Returns an array of lines containing the source code of the shader
	 */
	public String[] getSourceLines()
	{
		return srcLines;
	}

	private Shader(int id, String file, String[] srcLines)
	{
		this.id = id;
		this.fileName = file;
		this.srcLines = srcLines;
	}

	/**
	 * Creates a compiled GLSL fragment shader from source. Requires GL version
	 * 2.0 or greater.
	 */
	public static Shader createFragmentShader(GL2 gl, String file, InputStream is)
	{
		return createShaderObject(gl, GL2ES2.GL_FRAGMENT_SHADER, file, is);
	}

	/**
	 * Creates a compiled GLSL vertex shader from source. Requires GL version 2.0
	 * or greater.
	 */
	public static Shader createVertexShader(GL2 gl, String file, InputStream is)
	{
		return createShaderObject(gl, GL2ES2.GL_VERTEX_SHADER, file, is);
	}

	private static Shader createShaderObject(GL2 gl, int type, String file, InputStream is)
	{
		int id = gl.glCreateShader(type);

		String[] src = copySourceToArray(file, is);
		IntBuffer lineLengths = getLineLengths(src);

		gl.glShaderSource(id, src.length, src, lineLengths);
		gl.glCompileShader(id);

		if (!checkCompileStatus(gl, id, file)) {
			gl.glDeleteShader(id);
			return null;
		}

		return new Shader(id, file, src);
	}

	private static String[] copySourceToArray(String fileName, InputStream is)
	{
		ArrayList<String> src = new ArrayList<>();
		BufferedReader in = new BufferedReader(new InputStreamReader(is));

		try {
			String currentLine;
			while ((currentLine = in.readLine()) != null)
				src.add(currentLine + "\n");
			in.close();
		} catch (IOException e) {
			LOGGER.error("Error creating shader ({}): could not copy source.\n{}", fileName, e.getMessage());
		}

		return src.toArray(new String[0]);
	}

	private static IntBuffer getLineLengths(String[] src)
	{
		IntBuffer buf = Buffers.newDirectIntBuffer(src.length);
		for (String s : src)
			buf.put(s.length());
		buf.rewind();
		return buf;
	}

	private static boolean checkCompileStatus(GL2 gl, int id, String file)
	{
		IntBuffer status = Buffers.newDirectIntBuffer(1);
		gl.glGetShaderiv(id, GL2ES2.GL_COMPILE_STATUS, status);

		if (status.get() == GL.GL_FALSE) {
			IntBuffer iLogLengthBuf = Buffers.newDirectIntBuffer(1);
			gl.glGetShaderiv(id, GL2ES2.GL_INFO_LOG_LENGTH, iLogLengthBuf);
			int infoLogLength = iLogLengthBuf.get();
			ByteBuffer infoLog = Buffers.newDirectByteBuffer(infoLogLength);
			gl.glGetShaderInfoLog(id, infoLogLength, null, infoLog);
			CharBuffer info = StandardCharsets.US_ASCII.decode(infoLog);

			LOGGER.error("Error compiling shader ({}):\n{}", file, info.toString());
			return false;
		}
		return true;
	}

	@Override
	public void dispose(GL gl)
	{
		if (gl instanceof GL2)
			gl.getGL2().glDeleteShader(id);
		else if (gl instanceof GL3)
			gl.getGL3().glDeleteShader(id);
		disposed = true;
	}

	@Override
	public boolean isDisposed()
	{
		return disposed;
	}
}
