/*
 * The code of this class is licensed under the MIT license:
 *
 * Copyright (c) 2022 Overrun Organization
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package jsgl.math;

/**
 * <pre>{@code float  stb_perlin_noise3( float x,
 *                           float y,
 *                           float z,
 *                           int   x_wrap=0,
 *                           int   y_wrap=0,
 *                           int   z_wrap=0)}</pre>
 * <p>
 * This function computes a random value at the coordinate (x,y,z).<br>
 * Adjacent random values are continuous but the noise fluctuates
 * its randomness with period 1, i.e. takes on wholly unrelated values
 * at integer points. Specifically, this implements Ken Perlin's
 * revised noise function from 2002.
 * <p>
 * The "wrap" parameters can be used to create wraparound noise that
 * wraps at powers of two. The numbers <b>MUST</b> be powers of two. Specify
 * 0 to mean "don't care". (The noise always wraps every 256 due
 * details of the implementation, even if you ask for larger or no
 * wrapping.)
 * <p>
 * <pre>{@code float  stb_perlin_noise3_seed( float x,
 *                                float y,
 *                                float z,
 *                                int   x_wrap=0,
 *                                int   y_wrap=0,
 *                                int   z_wrap=0,
 *                                int   seed)}</pre>
 * <p>
 * As above, but 'seed' selects from multiple different variations of the
 * noise function. The current implementation only uses the bottom 8 bits
 * of 'seed', but possibly in the future more bits will be used.
 * <p>
 * <p>
 * Fractal Noise:
 * <p>
 * Three common fractal noise functions are included, which produce
 * a wide variety of nice effects depending on the parameters
 * provided. Note that each function will call stb_perlin_noise3
 * 'octaves' times, so this parameter will affect runtime.
 *
 * <pre>{@code float stb_perlin_ridge_noise3(float x, float y, float z,
 *                               float lacunarity, float gain, float offset, int octaves)
 *
 * float stb_perlin_fbm_noise3(float x, float y, float z,
 *                             float lacunarity, float gain, int octaves)
 *
 * float stb_perlin_turbulence_noise3(float x, float y, float z,
 *                                    float lacunarity, float gain, int octaves)}</pre>
 * <p>
 * Typical values to start playing with:
 * <ul>
 * <li>octaves    =   6     -- number of "octaves" of noise3() to sum</li>
 * <li>lacunarity = ~ 2.0   -- spacing between successive octaves (use exactly 2.0 for wrapping output)</li>
 * <li>gain       =   0.5   -- relative weighting applied to each successive octave</li>
 * <li>offset     =   1.0?  -- used to invert the ridges, may need to be larger, not sure</li>
 * </ul>
 *
 * @author squid233
 * @since 0.1.0
 */
public final class PerlinNoise
{
	/**
	 * @author squid233
	 * @since 0.1.0
	 */
	private static final class Vector3b
	{
		public final byte x, y, z;

		public Vector3b(int x, int y, int z)
		{
			this.x = (byte) x;
			this.y = (byte) y;
			this.z = (byte) z;
		}
	}

	private static final byte[] RANDTAB = {
			23,
			125,
			-95,
			52,
			103,
			117,
			70,
			37,
			-9,
			101,
			-53,
			-87,
			124,
			126,
			44,
			123,
			-104,
			-18,
			-111,
			45,
			-85,
			114,
			-3,
			10,
			-64,
			-120,
			4,
			-99,
			-7,
			30,
			35,
			72,
			-81,
			63,
			77,
			90,
			-75,
			16,
			96,
			111,
			-123,
			104,
			75,
			-94,
			93,
			56,
			66,
			-16,
			8,
			50,
			84,
			-27,
			49,
			-46,
			-83,
			-17,
			-115,
			1,
			87,
			18,
			2,
			-58,
			-113,
			57,
			-31,
			-96,
			58,
			-39,
			-88,
			-50,
			-11,
			-52,
			-57,
			6,
			73,
			60,
			20,
			-26,
			-45,
			-23,
			94,
			-56,
			88,
			9,
			74,
			-101,
			33,
			15,
			-37,
			-126,
			-30,
			-54,
			83,
			-20,
			42,
			-84,
			-91,
			-38,
			55,
			-34,
			46,
			107,
			98,
			-102,
			109,
			67,
			-60,
			-78,
			127,
			-98,
			13,
			-13,
			65,
			79,
			-90,
			-8,
			25,
			-32,
			115,
			80,
			68,
			51,
			-72,
			-128,
			-24,
			-48,
			-105,
			122,
			26,
			-44,
			105,
			43,
			-77,
			-43,
			-21,
			-108,
			-110,
			89,
			14,
			-61,
			28,
			78,
			112,
			76,
			-6,
			47,
			24,
			-5,
			-116,
			108,
			-70,
			-66,
			-28,
			-86,
			-73,
			-117,
			39,
			-68,
			-12,
			-10,
			-124,
			48,
			119,
			-112,
			-76,
			-118,
			-122,
			-63,
			82,
			-74,
			120,
			121,
			86,
			-36,
			-47,
			3,
			91,
			-15,
			-107,
			85,
			-51,
			-106,
			113,
			-40,
			31,
			100,
			41,
			-92,
			-79,
			-42,
			-103,
			-25,
			38,
			71,
			-71,
			-82,
			97,
			-55,
			29,
			95,
			7,
			92,
			54,
			-2,
			-65,
			118,
			34,
			-35,
			-125,
			11,
			-93,
			99,
			-22,
			81,
			-29,
			-109,
			-100,
			-80,
			17,
			-114,
			69,
			12,
			110,
			62,
			27,
			-1,
			0,
			-62,
			59,
			116,
			-14,
			-4,
			19,
			21,
			-69,
			53,
			-49,
			-127,
			64,
			-121,
			61,
			40,
			-89,
			-19,
			102,
			-33,
			106,
			-97,
			-59,
			-67,
			-41,
			-119,
			36,
			32,
			22,
			5,

			// and a second copy, so we don't need an extra mask or static initializer
			23,
			125,
			-95,
			52,
			103,
			117,
			70,
			37,
			-9,
			101,
			-53,
			-87,
			124,
			126,
			44,
			123,
			-104,
			-18,
			-111,
			45,
			-85,
			114,
			-3,
			10,
			-64,
			-120,
			4,
			-99,
			-7,
			30,
			35,
			72,
			-81,
			63,
			77,
			90,
			-75,
			16,
			96,
			111,
			-123,
			104,
			75,
			-94,
			93,
			56,
			66,
			-16,
			8,
			50,
			84,
			-27,
			49,
			-46,
			-83,
			-17,
			-115,
			1,
			87,
			18,
			2,
			-58,
			-113,
			57,
			-31,
			-96,
			58,
			-39,
			-88,
			-50,
			-11,
			-52,
			-57,
			6,
			73,
			60,
			20,
			-26,
			-45,
			-23,
			94,
			-56,
			88,
			9,
			74,
			-101,
			33,
			15,
			-37,
			-126,
			-30,
			-54,
			83,
			-20,
			42,
			-84,
			-91,
			-38,
			55,
			-34,
			46,
			107,
			98,
			-102,
			109,
			67,
			-60,
			-78,
			127,
			-98,
			13,
			-13,
			65,
			79,
			-90,
			-8,
			25,
			-32,
			115,
			80,
			68,
			51,
			-72,
			-128,
			-24,
			-48,
			-105,
			122,
			26,
			-44,
			105,
			43,
			-77,
			-43,
			-21,
			-108,
			-110,
			89,
			14,
			-61,
			28,
			78,
			112,
			76,
			-6,
			47,
			24,
			-5,
			-116,
			108,
			-70,
			-66,
			-28,
			-86,
			-73,
			-117,
			39,
			-68,
			-12,
			-10,
			-124,
			48,
			119,
			-112,
			-76,
			-118,
			-122,
			-63,
			82,
			-74,
			120,
			121,
			86,
			-36,
			-47,
			3,
			91,
			-15,
			-107,
			85,
			-51,
			-106,
			113,
			-40,
			31,
			100,
			41,
			-92,
			-79,
			-42,
			-103,
			-25,
			38,
			71,
			-71,
			-82,
			97,
			-55,
			29,
			95,
			7,
			92,
			54,
			-2,
			-65,
			118,
			34,
			-35,
			-125,
			11,
			-93,
			99,
			-22,
			81,
			-29,
			-109,
			-100,
			-80,
			17,
			-114,
			69,
			12,
			110,
			62,
			27,
			-1,
			0,
			-62,
			59,
			116,
			-14,
			-4,
			19,
			21,
			-69,
			53,
			-49,
			-127,
			64,
			-121,
			61,
			40,
			-89,
			-19,
			102,
			-33,
			106,
			-97,
			-59,
			-67,
			-41,
			-119,
			36,
			32,
			22,
			5,
	};

	// perlin's gradient has 12 cases so some get used 1/16th of the time
	// and some 2/16ths. We reduce bias by changing those fractions
	// to 5/64ths and 6/64ths

	private static final byte[] RANDTAB_GRAD_IDX = {
			7,
			9,
			5,
			0,
			11,
			1,
			6,
			9,
			3,
			9,
			11,
			1,
			8,
			10,
			4,
			7,
			8,
			6,
			1,
			5,
			3,
			10,
			9,
			10,
			0,
			8,
			4,
			1,
			5,
			2,
			7,
			8,
			7,
			11,
			9,
			10,
			1,
			0,
			4,
			7,
			5,
			0,
			11,
			6,
			1,
			4,
			2,
			8,
			8,
			10,
			4,
			9,
			9,
			2,
			5,
			7,
			9,
			1,
			7,
			2,
			2,
			6,
			11,
			5,
			5,
			4,
			6,
			9,
			0,
			1,
			1,
			0,
			7,
			6,
			9,
			8,
			4,
			10,
			3,
			1,
			2,
			8,
			8,
			9,
			10,
			11,
			5,
			11,
			11,
			2,
			6,
			10,
			3,
			4,
			2,
			4,
			9,
			10,
			3,
			2,
			6,
			3,
			6,
			10,
			5,
			3,
			4,
			10,
			11,
			2,
			9,
			11,
			1,
			11,
			10,
			4,
			9,
			4,
			11,
			0,
			4,
			11,
			4,
			0,
			0,
			0,
			7,
			6,
			10,
			4,
			1,
			3,
			11,
			5,
			3,
			4,
			2,
			9,
			1,
			3,
			0,
			1,
			8,
			0,
			6,
			7,
			8,
			7,
			0,
			4,
			6,
			10,
			8,
			2,
			3,
			11,
			11,
			8,
			0,
			2,
			4,
			8,
			3,
			0,
			0,
			10,
			6,
			1,
			2,
			2,
			4,
			5,
			6,
			0,
			1,
			3,
			11,
			9,
			5,
			5,
			9,
			6,
			9,
			8,
			3,
			8,
			1,
			8,
			9,
			6,
			9,
			11,
			10,
			7,
			5,
			6,
			5,
			9,
			1,
			3,
			7,
			0,
			2,
			10,
			11,
			2,
			6,
			1,
			3,
			11,
			7,
			7,
			2,
			1,
			7,
			3,
			0,
			8,
			1,
			1,
			5,
			0,
			6,
			10,
			11,
			11,
			0,
			2,
			7,
			0,
			10,
			8,
			3,
			5,
			7,
			1,
			11,
			1,
			0,
			7,
			9,
			0,
			11,
			5,
			10,
			3,
			2,
			3,
			5,
			9,
			7,
			9,
			8,
			4,
			6,
			5,

			// and a second copy, so we don't need an extra mask or static initializer
			7,
			9,
			5,
			0,
			11,
			1,
			6,
			9,
			3,
			9,
			11,
			1,
			8,
			10,
			4,
			7,
			8,
			6,
			1,
			5,
			3,
			10,
			9,
			10,
			0,
			8,
			4,
			1,
			5,
			2,
			7,
			8,
			7,
			11,
			9,
			10,
			1,
			0,
			4,
			7,
			5,
			0,
			11,
			6,
			1,
			4,
			2,
			8,
			8,
			10,
			4,
			9,
			9,
			2,
			5,
			7,
			9,
			1,
			7,
			2,
			2,
			6,
			11,
			5,
			5,
			4,
			6,
			9,
			0,
			1,
			1,
			0,
			7,
			6,
			9,
			8,
			4,
			10,
			3,
			1,
			2,
			8,
			8,
			9,
			10,
			11,
			5,
			11,
			11,
			2,
			6,
			10,
			3,
			4,
			2,
			4,
			9,
			10,
			3,
			2,
			6,
			3,
			6,
			10,
			5,
			3,
			4,
			10,
			11,
			2,
			9,
			11,
			1,
			11,
			10,
			4,
			9,
			4,
			11,
			0,
			4,
			11,
			4,
			0,
			0,
			0,
			7,
			6,
			10,
			4,
			1,
			3,
			11,
			5,
			3,
			4,
			2,
			9,
			1,
			3,
			0,
			1,
			8,
			0,
			6,
			7,
			8,
			7,
			0,
			4,
			6,
			10,
			8,
			2,
			3,
			11,
			11,
			8,
			0,
			2,
			4,
			8,
			3,
			0,
			0,
			10,
			6,
			1,
			2,
			2,
			4,
			5,
			6,
			0,
			1,
			3,
			11,
			9,
			5,
			5,
			9,
			6,
			9,
			8,
			3,
			8,
			1,
			8,
			9,
			6,
			9,
			11,
			10,
			7,
			5,
			6,
			5,
			9,
			1,
			3,
			7,
			0,
			2,
			10,
			11,
			2,
			6,
			1,
			3,
			11,
			7,
			7,
			2,
			1,
			7,
			3,
			0,
			8,
			1,
			1,
			5,
			0,
			6,
			10,
			11,
			11,
			0,
			2,
			7,
			0,
			10,
			8,
			3,
			5,
			7,
			1,
			11,
			1,
			0,
			7,
			9,
			0,
			11,
			5,
			10,
			3,
			2,
			3,
			5,
			9,
			7,
			9,
			8,
			4,
			6,
			5,
	};
	private static final Vector3b[] basis = {
			new Vector3b(1, 1, 0),
			new Vector3b(-1, 1, 0),
			new Vector3b(1, -1, 0),
			new Vector3b(-1, -1, 0),
			new Vector3b(1, 0, 1),
			new Vector3b(-1, 0, 1),
			new Vector3b(1, 0, -1),
			new Vector3b(-1, 0, -1),
			new Vector3b(0, 1, 1),
			new Vector3b(0, -1, 1),
			new Vector3b(0, 1, -1),
			new Vector3b(0, -1, -1),
	};

	private static float lerp(float a, float b, float t)
	{
		return a + (b - a) * t;
	}

	private static int fastfloor(float a)
	{
		int ai = (int) a;
		return (a < ai) ? ai - 1 : ai;
	}

	// different grad function from Perlin's, but easy to modify to match reference
	private static float grad(int grad_idx, float x, float y, float z)
	{
		Vector3b grad = basis[grad_idx];
		return grad.x * x + grad.y * y + grad.z * z;
	}

	private static float noise3internal(float x, float y, float z, int x_wrap, int y_wrap, int z_wrap, byte seed)
	{
		float u, v, w;
		float n000, n001, n010, n011, n100, n101, n110, n111;
		float n00, n01, n10, n11;
		float n0, n1;

		int x_mask = (x_wrap - 1) & 255;
		int y_mask = (y_wrap - 1) & 255;
		int z_mask = (z_wrap - 1) & 255;
		int px = fastfloor(x);
		int py = fastfloor(y);
		int pz = fastfloor(z);
		int x0 = px & x_mask, x1 = (px + 1) & x_mask;
		int y0 = py & y_mask, y1 = (py + 1) & y_mask;
		int z0 = pz & z_mask, z1 = (pz + 1) & z_mask;
		int r0, r1, r00, r01, r10, r11;

		x -= px;
		u = (((x * 6 - 15) * x + 10) * x * x * x);
		y -= py;
		v = (((y * 6 - 15) * y + 10) * y * y * y);
		z -= pz;
		w = (((z * 6 - 15) * z + 10) * z * z * z);

		final int seed_i = seed & 0xff;
		r0 = RANDTAB[x0 + seed_i] & 0xff;
		r1 = RANDTAB[x1 + seed_i] & 0xff;

		r00 = RANDTAB[r0 + y0] & 0xff;
		r01 = RANDTAB[r0 + y1] & 0xff;
		r10 = RANDTAB[r1 + y0] & 0xff;
		r11 = RANDTAB[r1 + y1] & 0xff;

		n000 = grad(RANDTAB_GRAD_IDX[r00 + z0], x, y, z);
		n001 = grad(RANDTAB_GRAD_IDX[r00 + z1], x, y, z - 1);
		n010 = grad(RANDTAB_GRAD_IDX[r01 + z0], x, y - 1, z);
		n011 = grad(RANDTAB_GRAD_IDX[r01 + z1], x, y - 1, z - 1);
		n100 = grad(RANDTAB_GRAD_IDX[r10 + z0], x - 1, y, z);
		n101 = grad(RANDTAB_GRAD_IDX[r10 + z1], x - 1, y, z - 1);
		n110 = grad(RANDTAB_GRAD_IDX[r11 + z0], x - 1, y - 1, z);
		n111 = grad(RANDTAB_GRAD_IDX[r11 + z1], x - 1, y - 1, z - 1);

		n00 = lerp(n000, n001, w);
		n01 = lerp(n010, n011, w);
		n10 = lerp(n100, n101, w);
		n11 = lerp(n110, n111, w);

		n0 = lerp(n00, n01, v);
		n1 = lerp(n10, n11, v);

		return lerp(n0, n1, u);
	}

	public static float noise3(float x, float y, float z, int x_wrap, int y_wrap, int z_wrap)
	{
		return noise3internal(x, y, z, x_wrap, y_wrap, z_wrap, (byte) 0);
	}

	public static float noise3seed(float x, float y, float z, int x_wrap, int y_wrap, int z_wrap, int seed)
	{
		return noise3internal(x, y, z, x_wrap, y_wrap, z_wrap, (byte) seed);
	}

	public static float ridgeNoise3(float x, float y, float z, float lacunarity, float gain, float offset, int octaves)
	{
		float frequency = 1.0f;
		float prev = 1.0f;
		float amplitude = 0.5f;
		float sum = 0.0f;

		for (int i = 0; i < octaves; i++) {
			float r = noise3internal(x * frequency, y * frequency, z * frequency, 0, 0, 0, (byte) i);
			r = offset - Math.abs(r);
			r = r * r;
			sum += r * amplitude * prev;
			prev = r;
			frequency *= lacunarity;
			amplitude *= gain;
		}
		return sum;
	}

	public static float fbmNoise3(float x, float y, float z, float lacunarity, float gain, int octaves)
	{
		float frequency = 1.0f;
		float amplitude = 1.0f;
		float sum = 0.0f;

		for (int i = 0; i < octaves; i++) {
			sum += noise3internal(x * frequency, y * frequency, z * frequency, 0, 0, 0, (byte) i) * amplitude;
			frequency *= lacunarity;
			amplitude *= gain;
		}
		return sum;
	}

	public static float turbulenceNoise3(float x, float y, float z, float lacunarity, float gain, int octaves)
	{
		int i;
		float frequency = 1.0f;
		float amplitude = 1.0f;
		float sum = 0.0f;

		for (i = 0; i < octaves; i++) {
			float r = noise3internal(x * frequency, y * frequency, z * frequency, 0, 0, 0, (byte) i) * amplitude;
			sum += Math.abs(r);
			frequency *= lacunarity;
			amplitude *= gain;
		}
		return sum;
	}

	public static float noise3wrapNonpow2(float x, float y, float z, int x_wrap, int y_wrap, int z_wrap, byte seed)
	{
		float u, v, w;
		float n000, n001, n010, n011, n100, n101, n110, n111;
		float n00, n01, n10, n11;
		float n0, n1;

		int px = fastfloor(x);
		int py = fastfloor(y);
		int pz = fastfloor(z);
		int x_wrap2 = ((x_wrap != 0) ? x_wrap : 256);
		int y_wrap2 = ((y_wrap != 0) ? y_wrap : 256);
		int z_wrap2 = ((z_wrap != 0) ? z_wrap : 256);
		int x0 = px % x_wrap2, x1;
		int y0 = py % y_wrap2, y1;
		int z0 = pz % z_wrap2, z1;
		int r0, r1, r00, r01, r10, r11;

		if (x0 < 0)
			x0 += x_wrap2;
		if (y0 < 0)
			y0 += y_wrap2;
		if (z0 < 0)
			z0 += z_wrap2;
		x1 = (x0 + 1) % x_wrap2;
		y1 = (y0 + 1) % y_wrap2;
		z1 = (z0 + 1) % z_wrap2;

		x -= px;
		u = (((x * 6 - 15) * x + 10) * x * x * x);
		y -= py;
		v = (((y * 6 - 15) * y + 10) * y * y * y);
		z -= pz;
		w = (((z * 6 - 15) * z + 10) * z * z * z);

		final int seed_i = seed & 0xff;
		r0 = RANDTAB[x0] & 0xff;
		r0 = RANDTAB[r0 + seed_i] & 0xff;
		r1 = RANDTAB[x1] & 0xff;
		r1 = RANDTAB[r1 + seed_i] & 0xff;

		r00 = RANDTAB[r0 + y0] & 0xff;
		r01 = RANDTAB[r0 + y1] & 0xff;
		r10 = RANDTAB[r1 + y0] & 0xff;
		r11 = RANDTAB[r1 + y1] & 0xff;

		n000 = grad(RANDTAB_GRAD_IDX[r00 + z0], x, y, z);
		n001 = grad(RANDTAB_GRAD_IDX[r00 + z1], x, y, z - 1);
		n010 = grad(RANDTAB_GRAD_IDX[r01 + z0], x, y - 1, z);
		n011 = grad(RANDTAB_GRAD_IDX[r01 + z1], x, y - 1, z - 1);
		n100 = grad(RANDTAB_GRAD_IDX[r10 + z0], x - 1, y, z);
		n101 = grad(RANDTAB_GRAD_IDX[r10 + z1], x - 1, y, z - 1);
		n110 = grad(RANDTAB_GRAD_IDX[r11 + z0], x - 1, y - 1, z);
		n111 = grad(RANDTAB_GRAD_IDX[r11 + z1], x - 1, y - 1, z - 1);

		n00 = lerp(n000, n001, w);
		n01 = lerp(n010, n011, w);
		n10 = lerp(n100, n101, w);
		n11 = lerp(n110, n111, w);

		n0 = lerp(n00, n01, v);
		n1 = lerp(n10, n11, v);

		return lerp(n0, n1, u);
	}
}
