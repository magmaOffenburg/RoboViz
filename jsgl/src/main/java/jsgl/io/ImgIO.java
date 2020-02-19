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

package jsgl.io;

import com.jogamp.common.nio.Buffers;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.nio.ByteBuffer;

public class ImgIO
{
	/** Reads pixels from an RGB or RGBA image into a byte buffer */
	public static ByteBuffer readPixels(BufferedImage img) throws InterruptedException
	{
		int w = img.getWidth();
		int h = img.getHeight();
		boolean alpha = img.getColorModel().hasAlpha();

		// Grabs pixels from image and store them into pixel array. Each pixel
		// is packed into an int with 1 byte per channel.
		int[] pixels = new int[w * h];
		PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
		pg.grabPixels();

		// Create a buffer big enough to store pixel data separated into bytes.
		int channels = img.getColorModel().getNumComponents();
		ByteBuffer data = Buffers.newDirectByteBuffer(pixels.length * channels);

		// Put pixel data into the buffer starting from bottom row going left
		// to right. Each pixel must be unpacked from int form into each channel
		for (int y = h - 1; y >= 0; y--) {
			for (int x = 0; x < w; x++) {
				int pixel = pixels[y * w + x];
				if (channels > 2)
					data.put((byte) ((pixel >> 16) & 0xFF));
				if (channels > 1)
					data.put((byte) ((pixel >> 8) & 0xFF));
				data.put((byte) ((pixel >> 0) & 0xFF));
				if (alpha)
					data.put((byte) ((pixel >> 24) & 0xFF));
			}
		}

		data.flip();
		return data;
	}
}
