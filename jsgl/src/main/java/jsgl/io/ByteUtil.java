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

/**
 * Routines for working with bytes and byte arrays
 *
 * @author Justin Stoecker
 */
public class ByteUtil
{
	/** Retrieves four bytes from an array and converts it to a float */
	public static float getFloatFromBuf(byte[] buf, int offset)
	{
		byte[] bytes = new byte[4];
		System.arraycopy(buf, offset, bytes, 0, 4);
		return bytesToFloat(bytes);
	}

	/** Retrieves numFloats floats from a byte array starting at offset */
	public static float[] getFloatsFromBuf(byte[] buf, int offset, int numFloats)
	{
		float[] floats = new float[numFloats];
		for (int i = 0; i < numFloats; i++)
			floats[i] = getFloatFromBuf(buf, offset + 4 * i);
		return floats;
	}

	/** Converts four bytes to a float where the highest order byte is last */
	public static float bytesToFloat(byte[] bytes)
	{
		return Float.intBitsToFloat(
				(bytes[0] & 255) | (bytes[1] & 255) << 8 | (bytes[2] & 255) << 16 | (bytes[3] & 255) << 24);
	}

	/** Retrieves the unsigned value of a byte */
	public static int uValue(byte b)
	{
		return b < 0 ? b + 256 : b;
	}
}
