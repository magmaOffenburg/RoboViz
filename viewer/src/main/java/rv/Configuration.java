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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import rv.util.Pair;

/**
 * Configuration parameters for RoboViz startup
 *
 * @author Justin Stoecker
 */
public class Configuration
{
	private static final String CONFIG_FILE_NAME = "config.txt";
	private static ArrayList<Pair<String, String>> configList = new ArrayList<>();

	private static String getConfigFilePath()
	{
		String userConfig = System.getProperty("user.home") + "/.roboviz/" + CONFIG_FILE_NAME;
		if (new File(userConfig).exists())
			return userConfig;
		return CONFIG_FILE_NAME;
	}

	public static class Graphics
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

		private void read() throws IOException
		{
			useBloom = getBool("Bloom");
			usePhong = getBool("Phong");
			useShadows = getBool("Shadows");
			useSoftShadows = getBool("Soft Shadows");
			shadowResolution = getInt("Shadow Resolution");
			useStereo = getBool("Stereo 3D");
			useVsync = getBool("V-Sync");
			useFsaa = getBool("FSAA");
			fsaaSamples = getInt("FSAA Samples");
			targetFPS = getInt("Target FPS");
			firstPersonFOV = getInt("First Person FOV");
			thirdPersonFOV = getInt("Third Person FOV");
			frameWidth = getInt("Frame Width");
			frameHeight = getInt("Frame Height");
			frameX = getInteger("Frame X");
			frameY = getInteger("Frame Y");
			centerFrame = getBool("Center Frame");
			isMaximized = getBool("Frame Maximized");
			saveFrameState = getBool("Save Frame State");
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

	public static class OverlayVisibility
	{
		public boolean serverSpeed = true;
		public boolean foulOverlay = true;
		public boolean fieldOverlay = false;
		public boolean numberOfPlayers = false;
		public boolean playerIDs = false;

		private void read() throws IOException
		{
			serverSpeed = getBool("Server Speed");
			foulOverlay = getBool("Foul Overlay");
			fieldOverlay = getBool("Field Overlay");
			numberOfPlayers = getBool("Number of Players");
			playerIDs = getBool("Player IDs");
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

	public static class Networking
	{
		public boolean autoConnect = true;
		public String serverHost = "localhost";
		public List<String> serverHosts;
		public int serverPort = 3200;
		public int listenPort = 32769;
		public int autoConnectDelay = 1000;

		public String overriddenServerHost = null;
		private Integer overriddenServerPort = null;

		private void read() throws IOException
		{
			autoConnect = getBool("Auto-Connect");
			autoConnectDelay = getInt("Auto-Connect Delay");
			serverHosts = new LinkedList<>(Arrays.asList(getStringList("Server Hosts")));
			serverHost = serverHosts.get(0);
			serverPort = getInt("Server Port");
			listenPort = getInt("Drawing Port");
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

	public static class General
	{
		public boolean recordLogs = false;
		public String logfileDirectory = null;
		public String lookAndFeel = "javax.swing.plaf.nimbus.NimbusLookAndFeel";

		private void read() throws IOException
		{
			recordLogs = getBool("Record Logfiles");
			logfileDirectory = getString("Logfile Directory");
			lookAndFeel = getString("Look and Feel");
		}

		private void write(BufferedWriter out) throws IOException
		{
			writeSection(out, "General Settings");
			writeVal(out, "Record Logfiles", recordLogs);
			writeVal(out, "Logfile Directory", logfileDirectory);
			writeVal(out, "Look and Feel", lookAndFeel);
			out.write(getNewline());
		}
	}

	public static class TeamColors
	{
		public final HashMap<String, Color> colorByTeamName = new HashMap<>();
		public Color defaultLeftColor = new Color(0x2626ff);
		public Color defaultRightColor = new Color(0xff2626);

		private void read() throws IOException
		{
			defaultRightColor = new Color(Integer.decode(getValue("<Right>")));
			defaultLeftColor = new Color(Integer.decode(getValue("<Left>")));

			colorByTeamName.put("<Right>", defaultRightColor);
			colorByTeamName.put("<Left>", defaultLeftColor);
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

	private boolean checkLine(String line)
	{
		return (line.length() > 0 && !line.startsWith("#") &&
				!(line.equals("Graphics Settings:") || line.equals("Overlay Default Visibility:") ||
						line.equals("Networking Settings:") || line.equals("General Settings:") ||
						line.equals("Team Colors:")));
	}

	private void parseLine(String line)
	{
		String key = line.substring(0, line.indexOf(":") - 1).trim();
		String val = line.substring(line.indexOf(":") + 1).trim();

		configList.add(new Pair<String, String>(key, val));
	}

	private static String getValue(String key)
	{
		Pair<String, String> valuePair = new Pair<>(null, null);
		valuePair = configList.stream().filter(it -> it.getFirst().equals(key)).findFirst().get();

		return valuePair.getSecond();
	}

	private static boolean getBool(String key) throws IOException
	{
		return Boolean.parseBoolean(getValue(key));
	}

	private static int getInt(String key) throws IOException
	{
		return Integer.parseInt(getValue(key));
	}

	private static Integer getInteger(String key) throws IOException
	{
		try {
			return Integer.parseInt(getValue(key));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static String getString(String key) throws IOException
	{
		return getValue(key);
	}

	private static String[] getStringList(String key) throws IOException
	{
		return getValue(key).split(", ?");
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
		// parse the configuration file
		try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()))) {
			stream.forEach(line -> {
				if (checkLine(line)) {
					parseLine(line);
				}
			});
		} catch (NoSuchFileException nf) {
			System.err.println("Could not find config file");
			nf.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		// read the parsed data
		try {
			graphics.read();
			overlayVisibility.read();
			networking.read();
			general.read();
			teamColors.read();
		} catch (Exception e) {
			System.err.println(
					"Error reading values from config file '" + file +
					"'. The configuration file might be corrupt or incompatible with this version of RoboViz, try resetting it.");
			System.exit(1);
		}

		return this;
	}

	public static Configuration loadFromFile()
	{
		return new Configuration().read(new File(getConfigFilePath()));
	}
}
