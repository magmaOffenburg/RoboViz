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

/**
 * Wrapper class for working with integer-packed pixels
 *
 * @author Justin Stoecker
 */
public class Pixel
{
	public int r, g, b, a;

	public Pixel(int r, int g, int b, int a)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	/**
	 * Construct pixel from an integer whose bits are distributed such that each
	 * channel gets 8 bits. The order must be alpha, red, green, and blue
	 */
	public Pixel(int intARGB)
	{
		a = (intARGB >> 24) & 255;
		r = (intARGB >> 16) & 255;
		g = (intARGB >> 8) & 255;
		b = (intARGB) &255;
	}

	/**
	 * Calculates the luminance of the pixel
	 *
	 * @return luminance in range 0.0 to 1.0
	 */
	public double lum()
	{
		return 0.299 * r + 0.587 * g + 0.114 * b;
	}

	/**
	 * Packs alpha, red, green, and blue color channels into a single 32-bit
	 * integer
	 */
	public int packValue()
	{
		return pack(r, g, b, a);
	}

	/**
	 * Creates a copy of the current pixel
	 */
	@Override
	public Pixel clone()
	{
		return new Pixel(r, g, b, a);
	}

	public static int[] unpack(int argb)
	{
		return new int[] {(argb >> 24) & 255, (argb >> 16) & 255, (argb >> 8) & 255, (argb) &255};
	}

	public static int pack(int r, int g, int b, int a)
	{
		return (r << 16) | (g << 8) | (b << 0) | (a << 24);
	}
}