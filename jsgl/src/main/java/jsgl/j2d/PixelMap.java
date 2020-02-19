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

package jsgl.j2d;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

public class PixelMap
{
	private int[] pixels; // pixels in packed form (ARGB each take 1 byte)
	private int width;	  // width of image in pixels
	private int height;	  // height of image in pixels

	public PixelMap(int width, int height)
	{
		pixels = new int[width * height];
		this.width = width;
		this.height = height;
	}

	public PixelMap(Pixel[][] pixels)
	{
		this.pixels = new int[pixels.length * pixels[0].length];
		this.width = pixels.length;
		this.height = pixels[0].length;

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				this.pixels[x + y * width] = pixels[x][y].packValue();
	}

	public PixelMap(Image img) throws InterruptedException
	{
		width = img.getWidth(null);
		height = img.getHeight(null);
		pixels = new int[width * height];

		PixelGrabber pg = new PixelGrabber(img, 0, 0, width, height, pixels, 0, width);
		pg.grabPixels();
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public Pixel get(int x, int y)
	{
		return new Pixel(pixels[x + y * width]);
	}

	/**
	 * Returns a 2D subsection of the pixel map
	 *
	 * @param x
	 *           - top-left x coordinate of area
	 * @param y
	 *           - top-left y coordinate of area
	 * @param w
	 *           - width of area in pixels
	 * @param h
	 *           - height of area in pixels
	 * @return a 2D array of pixels
	 */
	public Pixel[][] getRegion(int x, int y, int w, int h)
	{
		Pixel[][] pixels = new Pixel[w][h];
		for (int py = 0; py < h; py++)
			for (int px = 0; px < w; px++)
				pixels[px][py] = get(x + px, y + py);

		return pixels;
	}

	public Pixel[][] getRegion(Rectangle region)
	{
		Pixel[][] pixels = new Pixel[region.width][region.height];
		for (int y = 0; y < region.height; y++)
			for (int x = 0; x < region.width; x++)
				pixels[x][y] = get(region.x + x, region.y + y);

		return pixels;
	}

	public void set(int x, int y, Pixel p)
	{
		pixels[x + y * width] = p.packValue();
	}

	public Image toImage()
	{
		return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(width, height, pixels, 0, width));
	}
}
