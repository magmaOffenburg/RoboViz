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

package rv.comm.drawing.commands;

import java.nio.ByteBuffer;
import java.util.Locale;
import jsgl.io.ByteUtil;
import jsgl.math.vector.Vec3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rv.world.Team;
import rv.world.WorldModel;
import rv.world.objects.Agent;

/**
 * Client -> RoboViz command
 *
 * @author Justin Stoecker
 */
public abstract class Command
{
	private static final Logger LOGGER = LogManager.getLogger();

	// packet command IDs
	public static final int DRAW_OPTION = 0;
	public static final int DRAW_SHAPE = 1;
	public static final int DRAW_ANNOTATION = 2;
	public static final int CONTROL = 3;

	/** Performs the command's function */
	public abstract void execute();

	/**
	 * Reads bytes from buffer until 0 is reached, then returns the string of all previous bytes.
	 */
	public static String getString(ByteBuffer buf)
	{
		StringBuilder sb = new StringBuilder();
		char c;
		while ((c = (char) ByteUtil.uValue(buf.get())) != 0)
			sb.append(c);
		return sb.toString();
	}

	/**
	 * Extracts the index of the team the command is intended for.
	 *
	 * @param b
	 *            - byte that represents both the agent and the team
	 * @return the index of the team in the debugger's team array
	 */
	protected static int parseTeam(byte b)
	{
		return ByteUtil.uValue(b) / 128;
	}

	/**
	 * Extracts the id of the agent the command is intended for.
	 *
	 * @param b
	 *            - byte that represents both the agent and the team
	 * @return the id of the agent
	 */
	protected static int parseAgent(byte b)
	{
		int ub = ByteUtil.uValue(b);
		return ub / 128 == 0 ? ub : ub - 128;
	}

	/**
	 * Retrieves the agent the command is intended for.
	 *
	 * @param world
	 *            - world model containing teams and their agents
	 * @param teamAgent
	 *            - byte that represents both the agent and the team
	 * @return intended agent of the team
	 */
	protected static Agent getAgent(WorldModel world, byte teamAgent)
	{
		int uTeamAgent = ByteUtil.uValue(teamAgent);
		int teamID = uTeamAgent / 128;
		int agentID = (teamID == 0 ? uTeamAgent : uTeamAgent - 128) + 1;

		Team team = teamID == Team.LEFT ? world.getLeftTeam() : world.getRightTeam();

		return team.getAgentByID(agentID);
	}

	/**
	 * Extracts a string from a buffer.
	 *
	 * @param data
	 *            - packet's data
	 * @param start
	 *            - starting index of the string
	 * @return string from buffer
	 */
	protected static String getString(byte[] data, int start)
	{
		// find end of string
		int end = start;
		while (data[end] != 0) {
			end++;
		}

		// check if there is no string
		if (end == start)
			return null;

		// copy bytes and return
		byte[] stringBytes = new byte[end - start];
		System.arraycopy(data, start, stringBytes, 0, stringBytes.length);
		return new String(stringBytes);
	}

	/** Converts a sequence of bytes into a command */
	public static Command parse(ByteBuffer buf)
	{
		int type = ByteUtil.uValue(buf.get());
		switch (type) {
		case Command.DRAW_OPTION:
			return new DrawOption(buf);
		case Command.DRAW_SHAPE:
			return new DrawShape(buf);
		case Command.DRAW_ANNOTATION:
			return new DrawAnnotation(buf);
		case Command.CONTROL:
			return new Control(buf);
		default:
			return null;
		}
	}

	/** Retrieves n floats from a buffer and returns them in an array */
	public static float[] readFloats(ByteBuffer buf, int n)
	{
		float[] floats = new float[n];
		for (int i = 0; i < floats.length; i++)
			floats[i] = readFloat(buf);
		return floats;
	}

	/** Writes a float formatted in 6 ASCII characters to a buffer */
	public static void writeFloat(ByteBuffer buf, float value)
	{
		buf.put(String.format(Locale.US, "%6f", value).substring(0, 6).getBytes());
	}

	/** Reads a float formatted in 6 ASCII characters to a buffer */
	public static Float readFloat(ByteBuffer buf)
	{
		byte[] chars = new byte[6];
		buf.get(chars);
		Float result = null;
		String message = new String(chars);
		try {
			result = Float.parseFloat(message);
		} catch (NumberFormatException e) {
			LOGGER.debug("Could not parse command, float '" + message + "' contains invalid characters.");
		}
		return result;
	}

	/**
	 * Retrieves RGB colors as floats in [0,1] from 3 sequential bytes of RGB in a ByteBuffer
	 */
	public static float[] readRGB(ByteBuffer buf)
	{
		return new float[] {
				ByteUtil.uValue(buf.get()) / 255.0f,
				ByteUtil.uValue(buf.get()) / 255.0f,
				ByteUtil.uValue(buf.get()) / 255.0f,
		};
	}

	/**
	 * Retrieves RGBA colors as floats in [0,1] from 4 sequential bytes in a ByteBuffer
	 */
	public static float[] readRGBA(ByteBuffer buf)
	{
		return new float[] {
				ByteUtil.uValue(buf.get()) / 255.0f,
				ByteUtil.uValue(buf.get()) / 255.0f,
				ByteUtil.uValue(buf.get()) / 255.0f,
				ByteUtil.uValue(buf.get()) / 255.0f,
		};
	}

	/**
	 * Reads a series of floats from a buffer and converts them from SimSpark coordinates to RoboViz
	 * coordinates
	 */
	public static float[] readCoords(ByteBuffer buf, int n)
	{
		float[] vals = readFloats(buf, n);
		Vec3f v = n == 2 ? new Vec3f(vals[0], vals[1], 0) : new Vec3f(vals);
		return WorldModel.COORD_TFN.transform(v).getVals();
	}

	public static Agent readAgent(ByteBuffer buf, WorldModel world)
	{
		int agentTeam = ByteUtil.uValue(buf.get());
		Team team = (agentTeam / 128 == Team.LEFT) ? world.getLeftTeam() : world.getRightTeam();
		Agent agent;
		int agentID = agentTeam % 128 + 1;
		agent = team.getAgentByID(agentID);
		return agent;
	}
}
