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

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * Utility class for creating draw commands for RoboViz shapes
 *
 * @author Justin Stoecker
 */
public class RVDraw
{
	/** Writes a float formatted in 6 ASCII characters to a buffer */
	public static void writeFloatToBuffer(ByteBuffer buf, float value)
	{
		buf.put(String.format(Locale.US, "%6f", value).substring(0, 6).getBytes());
	}

	/** Writes RGB values of a Color object to a buffer */
	public static void writeColorToBuffer(ByteBuffer buf, Color color, boolean alpha)
	{
		buf.put((byte) color.getRed());
		buf.put((byte) color.getGreen());
		buf.put((byte) color.getBlue());
		if (alpha)
			buf.put((byte) color.getAlpha());
	}

	/** Writes a string to a buffer */
	public static void writeStringToBuffer(ByteBuffer buf, String s)
	{
		buf.put(s.getBytes());
		buf.put((byte) 0);
	}

	/** Reads a float formatted in 6 ASCII characters to a buffer */
	public static float readFloatFromBuffer(ByteBuffer buf)
	{
		byte[] chars = new byte[6];
		buf.get(chars);
		return Float.parseFloat(new String(chars));
	}

	/**
	 * Creates a buffer swap command
	 *
	 * @param group
	 *            - drawing group name (if null, swaps all buffers)
	 */
	public static byte[] newBufferSwap(String group)
	{
		int numBytes = 3 + ((group != null) ? group.length() : 0);
		ByteBuffer buf = ByteBuffer.allocate(numBytes);

		buf.put((byte) 0);
		buf.put((byte) 0);
		if (group != null)
			buf.put(group.getBytes());
		buf.put((byte) 0);

		return buf.array();
	}

	/**
	 * Creates a circle draw command
	 *
	 * @param center
	 *            - coordinates of circle (x,y)
	 * @param radius
	 *            - radius of circle
	 * @param thickness
	 *            - width of circle edge in pixels
	 * @param color
	 *            - color of circle
	 * @param group
	 *            - drawing group name
	 */
	public static byte[] newCircle(float[] center, float radius, float thickness, Color color, String group)
	{
		int numBytes = 30 + group.length();
		ByteBuffer buf = ByteBuffer.allocate(numBytes);

		buf.put((byte) 1);
		buf.put((byte) 0);
		writeFloatToBuffer(buf, center[0]);
		writeFloatToBuffer(buf, center[1]);
		writeFloatToBuffer(buf, radius);
		writeFloatToBuffer(buf, thickness);
		writeColorToBuffer(buf, color, false);
		writeStringToBuffer(buf, group);

		return buf.array();
	}

	/**
	 * Creates a line draw command
	 *
	 * @param a
	 *            - starting point
	 * @param b
	 *            - ending point
	 * @param thickness
	 *            - width of line in pixels
	 * @param color
	 *            - color of line
	 * @param group
	 *            - drawing group name
	 */
	public static byte[] newLine(float[] a, float[] b, float thickness, Color color, String group)
	{
		int numBytes = 48 + group.length();
		ByteBuffer buf = ByteBuffer.allocate(numBytes);

		buf.put((byte) 1);
		buf.put((byte) 1);
		writeFloatToBuffer(buf, a[0]);
		writeFloatToBuffer(buf, a[1]);
		writeFloatToBuffer(buf, a[2]);
		writeFloatToBuffer(buf, b[0]);
		writeFloatToBuffer(buf, b[1]);
		writeFloatToBuffer(buf, b[2]);
		writeFloatToBuffer(buf, thickness);
		writeColorToBuffer(buf, color, false);
		writeStringToBuffer(buf, group);

		return buf.array();
	}

	/**
	 * Creates a point draw command
	 *
	 * @param p
	 *            - coordinates of point (x,y,z)
	 * @param size
	 *            - size of point in pixels
	 * @param color
	 *            - color of point
	 * @param group
	 *            - drawing group name
	 */
	public static byte[] newPoint(float[] p, float size, Color color, String group)
	{
		int numBytes = 30 + group.length();
		ByteBuffer buf = ByteBuffer.allocate(numBytes);

		buf.put((byte) 1);
		buf.put((byte) 2);
		writeFloatToBuffer(buf, p[0]);
		writeFloatToBuffer(buf, p[1]);
		writeFloatToBuffer(buf, p[2]);
		writeFloatToBuffer(buf, size);
		writeColorToBuffer(buf, color, false);
		writeStringToBuffer(buf, group);

		return buf.array();
	}

	/**
	 * Creates a sphere draw command
	 *
	 * @param p
	 *            - coordinates of sphere center (x,y,z)
	 * @param radius
	 *            - radius of sphere
	 * @param color
	 *            - color of sphere
	 * @param group
	 *            - drawing group name
	 */
	public static byte[] newSphere(float[] p, float radius, Color color, String group)
	{
		int numBytes = 30 + group.length();
		ByteBuffer buf = ByteBuffer.allocate(numBytes);

		buf.put((byte) 1);
		buf.put((byte) 3);
		writeFloatToBuffer(buf, p[0]);
		writeFloatToBuffer(buf, p[1]);
		writeFloatToBuffer(buf, p[2]);
		writeFloatToBuffer(buf, radius);
		writeColorToBuffer(buf, color, false);
		writeStringToBuffer(buf, group);

		return buf.array();
	}

	public static byte[] newPolygon(float[][] v, Color color, String set)
	{
		int numBytes = 18 * v.length + 8 + set.length();

		ByteBuffer buf = ByteBuffer.allocate(numBytes);

		// 7 bytes
		buf.put((byte) 1);
		buf.put((byte) 4);
		buf.put((byte) v.length);
		writeColorToBuffer(buf, color, true);

		// 18 * v.length bytes
		for (int i = 0; i < v.length; i++) {
			writeFloatToBuffer(buf, v[i][0]);
			writeFloatToBuffer(buf, v[i][1]);
			writeFloatToBuffer(buf, v[i][2]);
		}

		// set.length + 1 bytes
		writeStringToBuffer(buf, set);

		return buf.array();
	}

	public static byte[] newAnnotation(String text, float[] pos, Color color, String set)
	{
		// header bytes                        = 2
		// pos  = 3 floats * 6 bytes per float = 18
		// color                               = 3
		// text                                = text.length + 1
		// set                                 = set.length + 1
		int numBytes = 25 + text.length() + set.length();
		ByteBuffer buf = ByteBuffer.allocate(numBytes);

		buf.put((byte) 2);
		buf.put((byte) 0);
		writeFloatToBuffer(buf, pos[0]);
		writeFloatToBuffer(buf, pos[1]);
		writeFloatToBuffer(buf, pos[2]);
		writeColorToBuffer(buf, color, false);
		writeStringToBuffer(buf, text);
		writeStringToBuffer(buf, set);

		return buf.array();
	}

	/**
	 * Adds an overhead annotation to a specified agent; if the text is null,
	 * this will clear the existing annotation from the agent.
	 */
	public static byte[] newAgentAnnotation(String text, boolean leftTeam, int agentNum, Color color)
	{
		int numBytes = (text == null) ? 3 : 7 + text.length();
		ByteBuffer buf = ByteBuffer.allocate(numBytes);

		buf.put((byte) 2);
		if (text == null) {
			buf.put((byte) 2);
			buf.put((byte) (leftTeam ? agentNum - 1 : agentNum + 127));
		} else {
			buf.put((byte) 1);
			buf.put((byte) (leftTeam ? agentNum - 1 : agentNum + 127));
			writeColorToBuffer(buf, color, false);
			writeStringToBuffer(buf, text);
		}

		return buf.array();
	}

	/**
	 * Selects the agent with the given team and uniform number.
	 */
	public static byte[] newSelectAgent(boolean leftTeam, int agentNum)
	{
		int numBytes = 3;
		ByteBuffer buf = ByteBuffer.allocate(numBytes);

		buf.put((byte) 3);
		buf.put((byte) 0);
		buf.put((byte) (leftTeam ? agentNum - 1 : agentNum + 127));

		return buf.array();
	}
}
