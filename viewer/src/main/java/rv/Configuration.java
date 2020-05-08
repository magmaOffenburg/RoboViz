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
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
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

		private void read()
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

		private void write(List<String> lines)
		{
			writeVal(lines, "Bloom", useBloom);
			writeVal(lines, "Phong", usePhong);
			writeVal(lines, "Shadows", useShadows);
			writeVal(lines, "Soft Shadows", useSoftShadows);
			writeVal(lines, "Shadow Resolution", shadowResolution);
			writeVal(lines, "Stereo 3D", useStereo);
			writeVal(lines, "V-Sync", useVsync);
			writeVal(lines, "FSAA", useFsaa);
			writeVal(lines, "FSAA Samples", fsaaSamples);
			writeVal(lines, "Target FPS", targetFPS);
			writeVal(lines, "First Person FOV", firstPersonFOV);
			writeVal(lines, "Third Person FOV", thirdPersonFOV);
			writeVal(lines, "Frame Width", frameWidth);
			writeVal(lines, "Frame Height", frameHeight);
			writeVal(lines, "Frame X", frameX);
			writeVal(lines, "Frame Y", frameY);
			writeVal(lines, "Center Frame", centerFrame);
			writeVal(lines, "Frame Maximized", isMaximized);
			writeVal(lines, "Save Frame State", saveFrameState);
		}

		public boolean equals(Graphics other)
		{
			boolean equal = true;

			equal &= this.useBloom == other.useBloom;
			equal &= this.usePhong == other.usePhong;
			equal &= this.useShadows == other.useShadows;
			equal &= this.useSoftShadows == other.useSoftShadows;
			equal &= this.useStereo == other.useStereo;
			equal &= this.useVsync == other.useVsync;
			equal &= this.useFsaa == other.useShadows;
			equal &= this.centerFrame == other.centerFrame;
			equal &= this.isMaximized == other.isMaximized;
			equal &= this.saveFrameState == other.saveFrameState;
			equal &= this.fsaaSamples == other.fsaaSamples;
			equal &= this.targetFPS == other.targetFPS;
			equal &= this.firstPersonFOV == other.firstPersonFOV;
			equal &= this.thirdPersonFOV == other.thirdPersonFOV;
			equal &= this.frameWidth == other.frameWidth;
			equal &= this.frameHeight == other.frameHeight;
			equal &= this.frameX == other.frameX;
			equal &= this.frameY == other.frameY;
			equal &= this.shadowResolution == other.shadowResolution;

			return equal;
		}
	}

	public static class OverlayVisibility
	{
		public boolean serverSpeed = true;
		public boolean foulOverlay = true;
		public boolean fieldOverlay = false;
		public boolean numberOfPlayers = false;
		public boolean playerIDs = false;

		private void read()
		{
			serverSpeed = getBool("Server Speed");
			foulOverlay = getBool("Foul Overlay");
			fieldOverlay = getBool("Field Overlay");
			numberOfPlayers = getBool("Number of Players");
			playerIDs = getBool("Player IDs");
		}

		private void write(List<String> lines)
		{
			writeVal(lines, "Server Speed", serverSpeed);
			writeVal(lines, "Foul Overlay", foulOverlay);
			writeVal(lines, "Field Overlay", fieldOverlay);
			writeVal(lines, "Number of Players", numberOfPlayers);
			writeVal(lines, "Player IDs", playerIDs);
		}

		public boolean equals(OverlayVisibility other)
		{
			boolean equal = true;

			equal &= this.serverSpeed == other.serverSpeed;
			equal &= this.foulOverlay == other.foulOverlay;
			equal &= this.fieldOverlay == other.fieldOverlay;
			equal &= this.numberOfPlayers == other.numberOfPlayers;
			equal &= this.playerIDs == other.playerIDs;

			return equal;
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

		private void read()
		{
			autoConnect = getBool("Auto-Connect");
			autoConnectDelay = getInt("Auto-Connect Delay");
			serverHosts = new LinkedList<>(Arrays.asList(getStringList("Server Hosts")));
			serverHost = serverHosts.get(0);
			serverPort = getInt("Server Port");
			listenPort = getInt("Drawing Port");
		}

		private void write(List<String> lines)
		{
			writeVal(lines, "Auto-Connect", autoConnect);
			writeVal(lines, "Auto-Connect Delay", autoConnectDelay);
			writeVal(lines, "Server Hosts", serverHosts);
			writeVal(lines, "Server Port", serverPort);
			writeVal(lines, "Drawing Port", listenPort);
		}

		public boolean equals(Networking other)
		{
			boolean equal = true;

			equal &= this.autoConnect == other.autoConnect;
			equal &= this.serverHost.equals(other.serverHost);
			equal &= this.serverPort == other.serverPort;
			equal &= this.listenPort == other.listenPort;
			equal &= this.autoConnectDelay == other.autoConnectDelay;

			equal &= this.serverHosts.size() == other.serverHosts.size();
			for (String host : this.serverHosts) {
				equal &= other.serverHosts.contains(host);
			}

			return equal;
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

		private void read()
		{
			recordLogs = getBool("Record Logfiles");
			logfileDirectory = getString("Logfile Directory");
			lookAndFeel = getString("Look and Feel");
		}

		private void write(List<String> lines)
		{
			writeVal(lines, "Record Logfiles", recordLogs);
			writeVal(lines, "Logfile Directory", logfileDirectory);
			writeVal(lines, "Look and Feel", lookAndFeel);
		}

		public boolean equals(General other)
		{
			boolean equal = true;

			equal &= this.recordLogs == other.recordLogs;
			equal &= this.logfileDirectory.equals(other.logfileDirectory);
			equal &= this.lookAndFeel.equals(other.lookAndFeel);

			return equal;
		}
	}

	public static class TeamColors
	{
		public final HashMap<String, Color> colorByTeamName = new HashMap<>();
		public Color defaultLeftColor = new Color(0x2626ff);
		public Color defaultRightColor = new Color(0xff2626);

		private void read()
		{
			defaultRightColor = new Color(Integer.decode(getValue("<Right>")));
			defaultLeftColor = new Color(Integer.decode(getValue("<Left>")));

			colorByTeamName.put("<Right>", defaultRightColor);
			colorByTeamName.put("<Left>", defaultLeftColor);
		}

		private void write(List<String> lines)
		{
			for (String teamName : colorByTeamName.keySet()) {
				Color color = colorByTeamName.get(teamName);
				writeVal(lines, teamName, String.format("0x%06x", color.getRGB() & 0xFFFFFF));
			}
		}

		public boolean equals(TeamColors other)
		{
			boolean equal = true;

			equal &= this.defaultLeftColor.equals(other.defaultLeftColor);
			equal &= this.defaultRightColor.equals(other.defaultRightColor);

			equal &= this.colorByTeamName.size() == other.colorByTeamName.size();
			for (Entry<String, Color> entry : this.colorByTeamName.entrySet()) {
				String key = entry.getKey();
				if (entry.getValue() != null) {
					equal &= entry.getValue().equals(other.colorByTeamName.get(key));
				} else {
					equal &= other.colorByTeamName.get(key) == null;
				}
			}

			return equal;
		}
	}

	// Original configuration read from config file
	public final Graphics originalGraphics = new Graphics();
	public final OverlayVisibility originalOverlayVisibility = new OverlayVisibility();
	public final Networking originalNetworking = new Networking();
	public final General originalGeneral = new General();
	public final TeamColors originalTeamColors = new TeamColors();

	// Current configuration
	public final Graphics graphics = new Graphics();
	public final OverlayVisibility overlayVisibility = new OverlayVisibility();
	public final Networking networking = new Networking();
	public final General general = new General();
	public final TeamColors teamColors = new TeamColors();

	private boolean checkLine(String line)
	{
		return (line.trim().length() > 0 && !line.startsWith("#"));
	}

	private void parseLine(String line)
	{
		try {
			String key = line.substring(0, line.indexOf(":") - 1).trim();
			String val = line.substring(line.indexOf(":") + 1).trim();

			configList.add(new Pair<>(key, val));
		} catch (IndexOutOfBoundsException e) {
			// Line doesn't contain a colon
			System.err.println("\"" + line + "\" is not a valid key-value pair.");
		}
	}

	private static String getValue(String key)
	{
		Pair<String, String> valuePair = configList.stream().filter(it -> it.getFirst().equals(key)).findFirst().get();

		return valuePair.getSecond();
	}

	private static boolean getBool(String key)
	{
		return Boolean.parseBoolean(getValue(key));
	}

	private static int getInt(String key)
	{
		return Integer.parseInt(getValue(key));
	}

	private static Integer getInteger(String key)
	{
		try {
			return Integer.parseInt(getValue(key));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static String getString(String key)
	{
		return getValue(key);
	}

	private static String[] getStringList(String key)
	{
		return getValue(key).split(", ?");
	}

	private static void writeConfigLine(List<String> configLines, String name, String newString)
	{
		// Escape special regex characters
		name = name.replace("\\", "\\\\");
		name = name.replace(".", "\\.");
		name = name.replace("*", "\\*");

		// Replace existing line
		for (String line : configLines) {
			if (line.matches("^" + name + " *:.*$")) {
				configLines.set(configLines.indexOf(line), newString);
				return;
			}
		}

		// Name not existing in config file
		configLines.add(newString);
	}

	private static void writeVal(List<String> configLines, String name, int value)
	{
		writeConfigLine(configLines, name, formatProperty('d', name, value));
	}

	private static void writeVal(List<String> configLines, String name, boolean value)
	{
		writeConfigLine(configLines, name, formatProperty('b', name, value));
	}

	private static void writeVal(List<String> configLines, String name, String value)
	{
		writeConfigLine(configLines, name, formatProperty('s', name, value));
	}

	private static void writeVal(List<String> configLines, String name, List<String> values)
	{
		writeConfigLine(configLines, name, formatProperty('s', name, join(values, ", ")));
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
		return String.format("%-20s : %" + type, name, value);
	}

	private static String getNewline()
	{
		return System.getProperty("line.separator");
	}

	public boolean didChange()
	{
		return !(graphics.equals(originalGraphics) && overlayVisibility.equals(originalOverlayVisibility) &&
				 networking.equals(originalNetworking) && general.equals(originalGeneral) &&
				 teamColors.equals(originalTeamColors));
	}

	public void readLines(File file, Consumer<? super String> action)
	{
		try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()))) {
			stream.forEach(action);
		} catch (NoSuchFileException nf) {
			System.err.println("Could not find config file");
			nf.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void write()
	{
		if (!didChange()) {
			// Doesn't need to be saved if nothing changed
			return;
		}

		File configFile = new File(getConfigFilePath());

		// Read configuration including comments
		List<String> lines = new LinkedList<String>();
		readLines(configFile, line -> lines.add(line));

		graphics.write(lines);
		overlayVisibility.write(lines);
		networking.write(lines);
		general.write(lines);
		teamColors.write(lines);

		// Write modified configuration file back
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(configFile));
			out.write(join(lines, getNewline()));
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
		readLines(file, line -> {
			if (checkLine(line)) {
				parseLine(line);
			}
		});

		// read the parsed data
		try {
			graphics.read();
			overlayVisibility.read();
			networking.read();
			general.read();
			teamColors.read();

			// Read again to store the original configuration
			originalGraphics.read();
			originalOverlayVisibility.read();
			originalNetworking.read();
			originalGeneral.read();
			originalTeamColors.read();
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
