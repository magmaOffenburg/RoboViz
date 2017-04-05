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

package rv;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Configuration parameters for RoboViz startup
 *
 * @author Justin Stoecker
 */
public class Configuration
{
	private static final String CONFIG_FILE_NAME = "config.txt";

	private static String getConfigFilePath()
	{
		String userConfig = System.getProperty("user.home") + "/.roboviz/" + CONFIG_FILE_NAME;
		if (new File(userConfig).exists())
			return userConfig;
		return CONFIG_FILE_NAME;
	}

	public static String getNextLine(BufferedReader in) throws IOException
	{
		String result = in.readLine();
		while (result != null && result.startsWith("#")) {
			result = in.readLine();
		}
		return result;
	}

	public class Graphics
	{
		public boolean useBloom = false;
		public boolean usePhong = false;
		public boolean useShadows = false;
		public boolean useSoftShadows = false;
		public boolean useStereo = false;
		public boolean useVsync = true;
		public boolean useFsaa = false;
		public boolean centerFrame = true;
		public boolean isMaximized = false;
		public boolean saveFrameState = true;
		public int fsaaSamples = 4;
		public int targetFPS = 60;
		public int firstPersonFOV = 120;
		public int thirdPersonFOV = 80;
		public int frameWidth = 1024;
		public int frameHeight = 768;
		public int frameX = 0;
		public int frameY = 0;
		public int shadowResolution = 1024;

		private void read(BufferedReader in) throws IOException
		{
			getNextLine(in);
			useBloom = getNextBool(in);
			usePhong = getNextBool(in);
			useShadows = getNextBool(in);
			useSoftShadows = getNextBool(in);
			shadowResolution = getNextInt(in);
			useStereo = getNextBool(in);
			useVsync = getNextBool(in);
			useFsaa = getNextBool(in);
			fsaaSamples = getNextInt(in);
			targetFPS = getNextInt(in);
			firstPersonFOV = getNextInt(in);
			thirdPersonFOV = getNextInt(in);
			frameWidth = getNextInt(in);
			frameHeight = getNextInt(in);
			frameX = getNextInteger(in);
			frameY = getNextInteger(in);
			centerFrame = getNextBool(in);
			isMaximized = getNextBool(in);
			saveFrameState = getNextBool(in);
			getNextLine(in);
		}

		private void write(BufferedWriter out) throws IOException
		{
			writeSection(out, "Graphics Settings");
			writeVal(out, "Bloom", useBloom);
			writeVal(out, "Phong", usePhong);
			writeVal(out, "Shadows", useShadows);
			writeVal(out, "Soft Shadows", useSoftShadows);
			writeVal(out, "Shadow Resolution", shadowResolution);
			writeVal(out, "Stereo 3D", useStereo);
			writeVal(out, "V-Sync", useVsync);
			writeVal(out, "FSAA", useFsaa);
			writeVal(out, "FSAA Samples", fsaaSamples);
			writeVal(out, "Target FPS", targetFPS);
			writeVal(out, "First Person FOV", firstPersonFOV);
			writeVal(out, "Third Person FOV", thirdPersonFOV);
			writeVal(out, "Frame Width", frameWidth);
			writeVal(out, "Frame Height", frameHeight);
			writeVal(out, "Frame X", frameX);
			writeVal(out, "Frame Y", frameY);
			writeVal(out, "Center Frame", centerFrame);
			writeVal(out, "Frame Maximized", isMaximized);
			writeVal(out, "Save Frame State", saveFrameState);
			out.write(getNewline());
		}
	}

	public class OverlayVisibility
	{
		public boolean serverSpeed = true;
		public boolean foulOverlay = true;
		public boolean fieldOverlay = false;
		public boolean numberOfPlayers = false;
		public boolean playerIDs = false;

		private void read(BufferedReader in) throws IOException
		{
			getNextLine(in);
			serverSpeed = getNextBool(in);
			foulOverlay = getNextBool(in);
			fieldOverlay = getNextBool(in);
			numberOfPlayers = getNextBool(in);
			playerIDs = getNextBool(in);
			getNextLine(in);
		}

		private void write(BufferedWriter out) throws IOException
		{
			writeSection(out, "Overlay Default Visibility");
			writeVal(out, "Server Speed", serverSpeed);
			writeVal(out, "Foul Overlay", foulOverlay);
			writeVal(out, "Field Overlay", fieldOverlay);
			writeVal(out, "Number of Players", numberOfPlayers);
			writeVal(out, "Player IDs", playerIDs);
			out.write(getNewline());
		}
	}

	public class Networking
	{
		public boolean autoConnect = true;
		public String serverHost = "localhost";
		public List<String> serverHosts;
		public int serverPort = 3200;
		public int listenPort = 32769;
		public int autoConnectDelay = 1000;

		public String overriddenServerHost = null;
		private Integer overriddenServerPort = null;

		private void read(BufferedReader in) throws IOException
		{
			getNextLine(in);
			autoConnect = getNextBool(in);
			autoConnectDelay = getNextInt(in);
			serverHosts = new LinkedList<String>(Arrays.asList(getNextStringList(in)));
			serverHost = serverHosts.get(0);
			serverPort = getNextInt(in);
			listenPort = getNextInt(in);
			getNextLine(in);
		}

		private void write(BufferedWriter out) throws IOException
		{
			writeSection(out, "Networking Settings");
			writeVal(out, "Auto-Connect", autoConnect);
			writeVal(out, "Auto-Connect Delay", autoConnectDelay);
			writeVal(out, "Server Hosts", serverHosts);
			writeVal(out, "Server Port", serverPort);
			writeVal(out, "Drawing Port", listenPort);
			out.write(getNewline());
		}

		public void overrideServerHost(String serverHost)
		{
			this.overriddenServerHost = serverHost;
		}

		public void overrideServerPort(Integer serverPort)
		{
			this.overriddenServerPort = serverPort;
		}

		public String getServerHost()
		{
			return (overriddenServerHost == null) ? serverHost : overriddenServerHost;
		}

		public int getServerPort()
		{
			return (overriddenServerPort == null) ? serverPort : overriddenServerPort;
		}
	}

	public class General
	{
		public boolean recordLogs = false;
		public String logfileDirectory = null;

		private void read(BufferedReader in) throws IOException
		{
			getNextLine(in);
			recordLogs = getNextBool(in);
			logfileDirectory = getNextString(in);
			getNextLine(in);
		}

		private void write(BufferedWriter out) throws IOException
		{
			writeSection(out, "General Settings");
			writeVal(out, "Record Logfiles", recordLogs);
			writeVal(out, "Logfile Directory", logfileDirectory);
			out.write(getNewline());
		}
	}

	public class TeamColors
	{
		public final HashMap<String, Color> colorByTeamName = new HashMap<>();
		public Color defaultLeftColor = new Color(0x2626ff);
		public Color defaultRightColor = new Color(0xff2626);

		private void read(BufferedReader in) throws IOException
		{
			getNextLine(in);
			String line;
			while (true) {
				line = getNextLine(in);
				if (line == null || line.trim().length() == 0)
					break;
				String key = getKey(line);
				int val = Integer.decode(getVal(line));
				Color color = new Color(val);
				colorByTeamName.put(key, color);

				if ("<Left>".equals(key))
					defaultLeftColor = color;
				else if ("<Right>".equals(key))
					defaultRightColor = color;
			}
		}

		private void write(BufferedWriter out) throws IOException
		{
			writeSection(out, "Team Colors");
			for (String teamName : colorByTeamName.keySet()) {
				Color color = colorByTeamName.get(teamName);
				writeVal(out, teamName, String.format("0x%06x", color.getRGB() & 0xFFFFFF));
			}
			out.write(getNewline());
		}
	}

	public final Graphics graphics = new Graphics();
	public final OverlayVisibility overlayVisibility = new OverlayVisibility();
	public final Networking networking = new Networking();
	public final General general = new General();
	public final TeamColors teamColors = new TeamColors();

	private static String getKey(String line)
	{
		return line.substring(0, line.indexOf(":") - 1).trim();
	}

	private static String getVal(String line)
	{
		return line.substring(line.indexOf(":") + 1).trim();
	}

	private static int getNextInt(BufferedReader in) throws IOException
	{
		return Integer.parseInt(getNextString(in));
	}

	private static Integer getNextInteger(BufferedReader in) throws IOException
	{
		try {
			return Integer.parseInt(getNextString(in));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static boolean getNextBool(BufferedReader in) throws IOException
	{
		return Boolean.parseBoolean(getNextString(in));
	}

	private static String getNextString(BufferedReader in) throws IOException
	{
		return getVal(getNextLine(in));
	}

	private static String[] getNextStringList(BufferedReader in) throws IOException
	{
		String str = getVal(getNextLine(in));
		return str.split(", ");
	}

	private static void writeSection(Writer out, String name) throws IOException
	{
		out.write(name + ":" + getNewline());
	}

	private static void writeVal(Writer out, String name, int value) throws IOException
	{
		out.write(formatProperty('d', name, value));
	}

	private static void writeVal(Writer out, String name, boolean value) throws IOException
	{
		out.write(formatProperty('b', name, value));
	}

	private static void writeVal(Writer out, String name, String value) throws IOException
	{
		out.write(formatProperty('s', name, value));
	}

	private static void writeVal(Writer out, String name, List<String> values) throws IOException
	{
		out.write(formatProperty('s', name, join(values, ", ")));
	}

	private static String join(Collection<String> data, String separator)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String item : data) {
			if (!first || (first = false))
				sb.append(separator);
			sb.append(item);
		}
		return sb.toString();
	}

	private static String formatProperty(char type, String name, Object value)
	{
		return String.format("%-20s : %" + type + getNewline(), name, value);
	}

	private static String getNewline()
	{
		return System.getProperty("line.separator");
	}

	public void write()
	{
		File configFile = new File(getConfigFilePath());
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(configFile));
			graphics.write(out);
			overlayVisibility.write(out);
			networking.write(out);
			general.write(out);
			teamColors.write(out);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Configuration read(File file)
	{
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			try {
				graphics.read(in);
				overlayVisibility.read(in);
				networking.read(in);
				general.read(in);
				teamColors.read(in);
			} catch (Exception e) {
				System.err.println(
						"Error reading values from config file '" + file +
						"'. The configuration file might be corrupt or incompatible with this version of RoboViz, try resetting it.");
				System.exit(1);
			}
		} catch (FileNotFoundException e) {
			System.err.println("Could not find config file");
			System.exit(1);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return this;
	}

	public static Configuration loadFromFile()
	{
		return loadFromFile(new File(getConfigFilePath()));
	}

	public static Configuration loadFromFile(File file)
	{
		return new Configuration().read(file);
	}
}
