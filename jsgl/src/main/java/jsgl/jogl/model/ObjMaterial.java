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

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.glu.GLU;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import jsgl.jogl.Texture2D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Material used for shading an OBJ model
 *
 * @author Justin Stoecker
 */
public class ObjMaterial extends MeshMaterial
{
	private static final Logger LOGGER = LogManager.getLogger();

	protected float alpha = 1.0f;
	protected int illum = 1;
	protected Texture2D texture = null;
	private InputStream textureSource = null;
	private boolean autoDisposeTexture = true;
	private boolean useMipMaps = false;

	public boolean isUseMipMaps()
	{
		return useMipMaps;
	}

	public void setUseMipMaps(boolean useMipMaps)
	{
		this.useMipMaps = useMipMaps;
	}

	public void setTexture(Texture2D tex, boolean autoDispose)
	{
		this.texture = tex;
		this.autoDisposeTexture = autoDispose;
	}

	public Texture2D getTexture()
	{
		return texture;
	}

	public InputStream getTextureSource()
	{
		return textureSource;
	}

	public ObjMaterial(String name)
	{
		this.name = name;
	}

	@Override
	public void init(GL2 gl)
	{
		if (textureSource == null)
			return;

		try {
			BufferedImage img = ImageIO.read(textureSource);
			if (img != null) {
				if (useMipMaps)
					texture = Texture2D.loadTexMipmaps(gl, new GLU(), img);
				else
					texture = Texture2D.loadTex(gl, img);
			}

			if (img.getColorModel().hasAlpha())
				containsTransparency = true;
		} catch (IOException e) {
			LOGGER.error("Error loading material texture", e);
		}
	}

	private static float[] parseValues(String line)
	{
		String[] parts = line.trim().split("\\s+");
		float[] values = new float[parts.length - 1];
		for (int i = 0; i < values.length; i++)
			values[i] = Float.parseFloat(parts[i + 1]);
		return values;
	}

	public void readAmbientColor(String line)
	{
		// Ka r g b
		System.arraycopy(parseValues(line), 0, ambient, 0, 3);
	}

	public void readDiffuseColor(String line)
	{
		// Kd r g b
		System.arraycopy(parseValues(line), 0, diffuse, 0, 3);
	}

	public void readSpecularColor(String line)
	{
		// Ks r g b
		System.arraycopy(parseValues(line), 0, specular, 0, 3);
	}

	public void readShininess(String line)
	{
		// Ns s
		shininess = (int) parseValues(line)[0];
	}

	public void readAlpha(String line)
	{
		// "d alpha" or "Tr alpha"
		alpha = parseValues(line)[0];
		ambient[3] = alpha;
		diffuse[3] = alpha;
		specular[3] = alpha;

		if (alpha < 1.0f)
			containsTransparency = true;
	}

	public void readIlluminationModel(String line)
	{
		// illum n
		illum = (int) parseValues(line)[0];
	}

	public void readTextureMap(InputStream texSrc)
	{
		// map_Ka filename
		textureSource = texSrc;
	}

	@Override
	public void apply(GL2 gl)
	{
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_AMBIENT, ambient, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_DIFFUSE, diffuse, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SPECULAR, specular, 0);
		gl.glMateriali(GL.GL_FRONT_AND_BACK, GLLightingFunc.GL_SHININESS, illum == 2 ? shininess : 0);

		gl.glBindTexture(GL.GL_TEXTURE_2D, texture == null ? 0 : texture.getID());
	}

	@Override
	public void dispose(GL gl)
	{
		if (texture != null && autoDisposeTexture)
			texture.dispose(gl);
		disposed = true;
	}

	@Override
	public boolean isDisposed()
	{
		return disposed;
	}
}
