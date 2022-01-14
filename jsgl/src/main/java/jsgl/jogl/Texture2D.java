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
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.glu.GLU;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A texture object that can be applied to geometry using texture coordinates
 *
 * @author Justin Stoecker
 */
public class Texture2D implements GLDisposable
{
	private static final Logger LOGGER = LogManager.getLogger();

	private boolean disposed = false;
	private int id;
	private int w;
	private int h;

	public int getID()
	{
		return id;
	}

	public int getWidth()
	{
		return w;
	}

	public int getHeight()
	{
		return h;
	}

	private Texture2D(int id, int w, int h)
	{
		this.id = id;
		this.w = w;
		this.h = h;
	}

	/**
	 * Generates a texture object with a unique ID. This is equivalent to
	 * calling glGenTextures(1, &id) and storing the result in the returned
	 * object. The width and height values are stored for convenience when using
	 * the createImage method.
	 */
	public static Texture2D generate(GL gl)
	{
		IntBuffer temp = Buffers.newDirectIntBuffer(1);
		gl.glGenTextures(1, temp);
		int id = temp.get();

		return new Texture2D(id, 0, 0);
	}

	/**
	 * Creates a new 2D texture object using a buffer of pixel data of a
	 * specified format. Uses no mipmapping and linear min/mag filtering.
	 *
	 * @param gl
	 *            - GL object
	 * @param w
	 *            - width of the texture
	 * @param h
	 *            - height of the texture
	 * @param format
	 *            - format of the pixel data and texture
	 * @param pixelData
	 *            - bytes corresponding to pixel data
	 */
	public static Texture2D create(GL gl, int w, int h, int format, ByteBuffer pixelData)
	{
		IntBuffer idBuffer = Buffers.newDirectIntBuffer(1);
		gl.glGenTextures(1, idBuffer);
		int id = idBuffer.get();

		gl.glBindTexture(GL.GL_TEXTURE_2D, id);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);

		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, 256, 256, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, pixelData);
		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

		return new Texture2D(id, w, h);
	}

	@Override
	public void dispose(GL gl)
	{
		gl.glDeleteTextures(1, new int[] {id}, 0);
		disposed = true;
	}

	@Override
	public boolean isDisposed()
	{
		return disposed;
	}

	/**
	 * Sets a parameter of actively bound texture. Equivalent to using
	 * glTexParameteri(GL_TEXTURE_2D, pname, value).
	 *
	 * @param pname
	 *            - GL_TEXTURE_MIN_FILTER, GL_TEXTURE_MAG_FILTER,
	 *            GL_TEXTURE_MIN_LOD, GL_TEXTURE_MAX_LOD, GL_TEXTURE_BASE_LEVEL,
	 *            GL_TEXTURE_MAX_LEVEL, GL_TEXTURE_WRAP_S, GL_TEXTURE_WRAP_T,
	 *            GL_TEXTURE_WRAP_R, GL_TEXTURE_PRIORITY,
	 *            GL_TEXTURE_COMPARE_MODE, GL_TEXTURE_COMPARE_FUNC,
	 *            GL_DEPTH_TEXTURE_MODE, or GL_GENERATE_MIPMAP
	 * @param value
	 *            - depends on parameter
	 */
	public static void setParameter(GL gl, int pname, int value)
	{
		gl.glTexParameteri(GL.GL_TEXTURE_2D, pname, value);
	}

	/**
	 * Equivalent to glTexImage2D(GL_TEXTURE_2D, level, internalFormat, width,
	 * height, border, format, type, data).
	 */
	public void texImage(
			GL gl, int level, int internalFormat, int width, int height, int border, int format, int type, Buffer data)
	{
		this.w = width;
		this.h = height;

		gl.glTexImage2D(GL.GL_TEXTURE_2D, level, internalFormat, width, height, border, format, type, data);
	}

	/**
	 * Sets a parameter of actively bound texture. Equivalent to using
	 * glTexParameterf(GL_TEXTURE_2D, pname, value).
	 *
	 * @param pname
	 *            - GL_TEXTURE_MIN_FILTER, GL_TEXTURE_MAG_FILTER,
	 *            GL_TEXTURE_MIN_LOD, GL_TEXTURE_MAX_LOD, GL_TEXTURE_BASE_LEVEL,
	 *            GL_TEXTURE_MAX_LEVEL, GL_TEXTURE_WRAP_S, GL_TEXTURE_WRAP_T,
	 *            GL_TEXTURE_WRAP_R, GL_TEXTURE_PRIORITY,
	 *            GL_TEXTURE_COMPARE_MODE, GL_TEXTURE_COMPARE_FUNC,
	 *            GL_DEPTH_TEXTURE_MODE, or GL_GENERATE_MIPMAP
	 * @param value
	 *            - depends on parameter
	 */
	public static void setParameter(GL gl, int pname, float value)
	{
		gl.glTexParameterf(GL.GL_TEXTURE_2D, pname, value);
	}

	/**
	 * Binds the current texture object to OpenGL. Equivalent to calling
	 * glBindTexture(GL_TEXTURE_2D, id) using this object's ID.
	 */
	public void bind(GL gl)
	{
		gl.glBindTexture(GL.GL_TEXTURE_2D, id);
	}

	/**
	 * Unbinds any texture. Equivalent to calling glBindTexture(GL_TEXTURE_2D,
	 * 0).
	 */
	public static void unbind(GL gl)
	{
		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
	}

	public static ByteBuffer readPixels(BufferedImage img, boolean alpha) throws InterruptedException
	{
		int w = img.getWidth();
		int h = img.getHeight();

		// Create a buffer big enough to store pixel data separated into bytes.
		// There are 4 channels if alpha is present (RGBA); 3 otherwise (RGB)
		int channels = alpha ? 4 : 3;
		ByteBuffer data = Buffers.newDirectByteBuffer(w * h * channels);

		// Put pixel data into the buffer starting from bottom row going left
		// to right. Each pixel must be unpacked from int form into each channel
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int pixel = img.getRGB(x, h - y - 1);
				data.put((byte) ((pixel >> 16) & 0xFF));
				data.put((byte) ((pixel >> 8) & 0xFF));
				data.put((byte) ((pixel) &0xFF));
				if (alpha)
					data.put((byte) ((pixel >> 24) & 0xFF));
			}
		}

		data.rewind();
		return data;
	}

	public static Texture2D loadTexMipmaps(GL gl, GLU glu, BufferedImage img)
	{
		// read image into ByteBuffer for use in OpenGL texture
		int w = img.getWidth();
		int h = img.getHeight();
		boolean alpha = img.getColorModel().hasAlpha();
		ByteBuffer pixelData;
		try {
			pixelData = readPixels(img, alpha);
		} catch (InterruptedException e) {
			LOGGER.error("Error buffering pixel data", e);
			return null;
		}

		// generate texture id
		IntBuffer idBuffer = Buffers.newDirectIntBuffer(1);
		gl.glGenTextures(1, idBuffer);
		int id = idBuffer.get();

		// set default texture parameters
		gl.glBindTexture(GL.GL_TEXTURE_2D, id);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);

		// upload pixel data to texture object
		int format = alpha ? GL.GL_RGBA : GL.GL_RGB;
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL2ES1.GL_GENERATE_MIPMAP, GL.GL_TRUE);
		glu.gluBuild2DMipmaps(GL.GL_TEXTURE_2D, format, w, h, format, GL.GL_UNSIGNED_BYTE, pixelData);
		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

		return new Texture2D(id, w, h);
	}

	public static Texture2D loadTex(GL gl, BufferedImage img)
	{
		// read image into ByteBuffer for use in OpenGL texture
		int w = img.getWidth();
		int h = img.getHeight();
		boolean alpha = img.getColorModel().hasAlpha();

		// WritableRaster raster = img.getRaster();
		// System.out.printf("Raster: bands = %d, w = %d, h = %d\n",
		// raster.getNumBands(), raster.getWidth(), raster.getHeight());
		// ByteBuffer pixelData =
		// Buffers.newDirectByteBuffer(raster.getNumBands()
		// * raster.getWidth() * raster.getHeight());
		// for (int y = 0; y < raster.getHeight(); y++) {
		// for (int x = 0; x < raster.getWidth(); x++) {
		// raster.getpixel
		// }
		// }

		ByteBuffer pixelData;
		try {
			pixelData = readPixels(img, alpha);
		} catch (InterruptedException e) {
			LOGGER.error("Error buffering pixel data", e);
			return null;
		}

		// generate texture id
		IntBuffer idBuffer = Buffers.newDirectIntBuffer(1);
		gl.glGenTextures(1, idBuffer);
		int id = idBuffer.get();

		// set default texture parameters
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
		gl.glBindTexture(GL.GL_TEXTURE_2D, id);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);

		// upload pixel data to texture object
		int format = alpha ? GL.GL_RGBA : GL.GL_RGB;
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, format, w, h, 0, format, GL.GL_UNSIGNED_BYTE, pixelData);
		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

		return new Texture2D(id, w, h);
	}

	public static Texture2D loadTexMipmaps(GL gl, GLU glu, String name, ClassLoader loader)
	{
		BufferedImage img;
		try {
			img = ImageIO.read(loader.getResourceAsStream(name));
		} catch (IOException e) {
			LOGGER.error("Error loading image", e);
			return null;
		}
		return loadTexMipmaps(gl, glu, img);
	}

	public static Texture2D loadTex(GL gl, String name, ClassLoader loader)
	{
		BufferedImage img;
		try {
			img = ImageIO.read(loader.getResourceAsStream(name));
		} catch (IOException e) {
			LOGGER.error("Error loading image", e);
			return null;
		}
		return loadTex(gl, img);
	}

	/**
	 * Creates a 2D texture.
	 *
	 * @param w
	 *            - texture width
	 * @param h
	 *            - texture height
	 * @param internalFormat
	 *            - format for texels stored in texture
	 * @param format
	 *            - format for texel values uploaded to texture
	 * @param bytesPerPixel
	 *            - number of bytes per texel uploaded to texture
	 */
	public static Texture2D create(GL gl, int w, int h, int internalFormat, int format, int type, int bytesPerPixel)
	{
		// storage space for texture data - 4 bytes per pixel (RGBA)
		ByteBuffer data = Buffers.newDirectByteBuffer(w * h * bytesPerPixel);
		data.limit(data.capacity());

		IntBuffer idBuffer = Buffers.newDirectIntBuffer(1);
		gl.glGenTextures(1, idBuffer);
		int tex = idBuffer.get();

		gl.glBindTexture(GL.GL_TEXTURE_2D, tex);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);

		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, internalFormat, w, h, 0, format, type, data);

		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

		return new Texture2D(tex, w, h);
	}

	/** Creates a blank Texture2D object that can be used to store pixel info */
	public static Texture2D createTexture(GL gl, int w, int h, int internalFormat, int bytesPerPixel)
	{
		// storage space for texture data - 4 bytes per pixel (RGBA)
		ByteBuffer data = Buffers.newDirectByteBuffer(w * h * bytesPerPixel);
		data.limit(data.capacity());

		IntBuffer idBuffer = Buffers.newDirectIntBuffer(1);
		gl.glGenTextures(1, idBuffer);
		int tex = idBuffer.get();

		gl.glBindTexture(GL.GL_TEXTURE_2D, tex);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
		// gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_GENERATE_MIPMAP,
		// GL.GL_TRUE);

		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, internalFormat, w, h, 0, internalFormat, GL.GL_UNSIGNED_BYTE, data);

		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

		return new Texture2D(tex, w, h);
	}
}