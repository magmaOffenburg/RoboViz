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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
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
	private ArrayList<Pair<String, String>> configList = new ArrayList<>();

	private static String getConfigFilePath()
	{
		String userConfig = System.getProperty("user.home") + "/.roboviz/" + CONFIG_FILE_NAME;
		if (new File(userConfig).exists())
			return userConfig;
		return CONFIG_FILE_NAME;
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
			frameX = getInt("Frame X");
			frameY = getInt("Frame Y");
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
			equal &= this.useFsaa == other.useFsaa;
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

		@Override
		public Graphics clone()
		{
			Graphics clone = new Graphics();
			clone.useBloom = this.useBloom;
			clone.usePhong = this.usePhong;
			clone.useShadows = this.useShadows;
			clone.useSoftShadows = this.useSoftShadows;
			clone.useStereo = this.useStereo;
			clone.useVsync = this.useVsync;
			clone.useFsaa = this.useFsaa;
			clone.centerFrame = this.centerFrame;
			clone.isMaximized = this.isMaximized;
			clone.saveFrameState = this.saveFrameState;
			clone.fsaaSamples = this.fsaaSamples;
			clone.targetFPS = this.targetFPS;
			clone.firstPersonFOV = this.firstPersonFOV;
			clone.thirdPersonFOV = this.thirdPersonFOV;
			clone.frameWidth = this.frameWidth;
			clone.frameHeight = this.frameHeight;
			clone.frameX = this.frameX;
			clone.frameY = this.frameY;
			clone.shadowResolution = this.shadowResolution;

			return clone;
		}
	}

	public class OverlayVisibility
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

		@Override
		public OverlayVisibility clone()
		{
			OverlayVisibility clone = new OverlayVisibility();
			clone.serverSpeed = this.serverSpeed;
			clone.foulOverlay = this.foulOverlay;
			clone.fieldOverlay = this.fieldOverlay;
			clone.numberOfPlayers = this.numberOfPlayers;
			clone.playerIDs = this.playerIDs;

			return clone;
		}
	}

	public class Networking
	{
		public boolean autoConnect = true;
		public List<Pair<String, Integer>> servers;
		public String defaultServerHost = "localhost";
		public int defaultServerPort = 3200;
		public int listenPort = 32769;
		public int autoConnectDelay = 1000;

		private String overriddenServerHost = null;
		private Integer overriddenServerPort = null;

		private void read()
		{
			autoConnect = getBool("Auto-Connect");
			autoConnectDelay = getInt("Auto-Connect Delay");
			defaultServerHost = getString("Default Server Host");
			defaultServerPort = getInt("Default Server Port");
			listenPort = getInt("Drawing Port");

			servers = new LinkedList<>();
			List<String> serverValues = getAllValues("Server");
			for (String serverValue : serverValues) {
				Pair<String, String> decodedServer = decodeValueValuePair(serverValue);
				servers.add(new Pair<>(decodedServer.getFirst(), Integer.decode(decodedServer.getSecond())));
			}
			if (servers.size() == 0) {
				servers.add(new Pair<>(defaultServerHost, defaultServerPort));
			} else {
				// Remove duplicates (if existing)
				servers = servers.stream().distinct().collect(Collectors.toList());
			}
		}

		private void write(List<String> lines)
		{
			writeVal(lines, "Auto-Connect", autoConnect);
			writeVal(lines, "Auto-Connect Delay", autoConnectDelay);
			writeVal(lines, "Default Server Host", defaultServerHost);
			writeVal(lines, "Default Server Port", defaultServerPort);
			writeVal(lines, "Drawing Port", listenPort);

			List<String> encodedServers =
					servers.stream()
							.distinct()
							.map(server -> encodeValueValuePair(server.getFirst(), server.getSecond().toString()))
							.collect(Collectors.toList());
			writeValList(lines, "Server", encodedServers);
		}

		public boolean equals(Networking other)
		{
			boolean equal = true;

			equal &= this.autoConnect == other.autoConnect;
			equal &= this.defaultServerHost.equals(other.defaultServerHost);
			equal &= this.defaultServerPort == other.defaultServerPort;
			equal &= this.listenPort == other.listenPort;
			equal &= this.autoConnectDelay == other.autoConnectDelay;

			equal &= this.servers.size() == other.servers.size();
			for (Pair<String, Integer> server : this.servers) {
				equal &= other.servers.contains(server);
			}

			return equal;
		}

		@Override
		public Networking clone()
		{
			Networking clone = new Networking();
			clone.autoConnect = this.autoConnect;
			clone.defaultServerHost = this.defaultServerHost;
			clone.defaultServerPort = this.defaultServerPort;
			clone.listenPort = this.listenPort;
			clone.autoConnectDelay = this.autoConnectDelay;

			clone.servers = new LinkedList<>();
			for (Pair<String, Integer> server : this.servers) {
				clone.servers.add(new Pair<>(server.getFirst(), server.getSecond()));
			}

			return clone;
		}

		public void overrideServer(String serverHost, Integer serverPort)
		{
			if (serverHost != null) {
				this.overriddenServerHost = serverHost;
			} else {
				this.overriddenServerHost = defaultServerHost;
			}

			if (serverPort != null) {
				this.overriddenServerPort = serverPort;
			} else {
				this.overriddenServerPort = defaultServerPort;
			}
		}

		public String getOverriddenServerHost()
		{
			return overriddenServerHost;
		}

		public Integer getOverriddenServerPort()
		{
			return overriddenServerPort;
		}
	}

	public class General
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

		@Override
		public General clone()
		{
			General clone = new General();
			clone.recordLogs = this.recordLogs;
			clone.logfileDirectory = this.logfileDirectory;
			clone.lookAndFeel = this.lookAndFeel;

			return clone;
		}
	}

	public class TeamColors
	{
		public final HashMap<String, Color> colorByTeamName = new HashMap<>();
		public final Color defaultLeftColor = new Color(0x2626ff);
		public final Color defaultRightColor = new Color(0xff2626);

		private void read()
		{
			List<String> teamColors = getAllValues("Team Color");
			for (String teamColor : teamColors) {
				Pair<String, String> decodedTeamColor = decodeValueValuePair(teamColor);
				colorByTeamName.put(
						decodedTeamColor.getFirst(), new Color(Integer.decode(decodedTeamColor.getSecond())));
			}

			// Add default colors (if not present)
			if (!colorByTeamName.containsKey("<Left>")) {
				colorByTeamName.put("<Left>", defaultLeftColor);
			}
			if (!colorByTeamName.containsKey("<Right>")) {
				colorByTeamName.put("<Right>", defaultRightColor);
			}
		}

		private void write(List<String> lines)
		{
			List<String> encodedTeamNames =
					colorByTeamName.entrySet()
							.stream()
							.map(teamColor
									-> encodeValueValuePair(teamColor.getKey(),
											String.format("0x%06x", teamColor.getValue().getRGB() & 0xFFFFFF)))
							.collect(Collectors.toList());

			writeValList(lines, "Team Color", encodedTeamNames);
		}

		public boolean equals(TeamColors other)
		{
			boolean equal = true;

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

		@Override
		public TeamColors clone()
		{
			TeamColors clone = new TeamColors();

			for (Entry<String, Color> entry : this.colorByTeamName.entrySet()) {
				clone.colorByTeamName.put(entry.getKey(), entry.getValue());
			}

			return clone;
		}
	}

	// Original configuration read from config file
	private Graphics originalGraphics = new Graphics();
	private OverlayVisibility originalOverlayVisibility = new OverlayVisibility();
	private Networking originalNetworking = new Networking();
	private General originalGeneral = new General();
	private TeamColors originalTeamColors = new TeamColors();

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

	private static Pair<String, String> parseLine(String line)
	{
		try {
			String key = line.substring(0, line.indexOf(":")).trim();
			String val = line.substring(line.indexOf(":") + 1).trim();
			return new Pair<>(key, val);
		} catch (IndexOutOfBoundsException e) {
			// Line doesn't contain a colon
			System.err.println("\"" + line + "\" is not a valid key-value pair.");
			return null;
		}
	}

	private String getValue(String key)
	{
		Pair<String, String> valuePair = configList.stream().filter(it -> it.getFirst().equals(key)).findFirst().get();

		if (valuePair != null) {
			return valuePair.getSecond();
		} else {
			return "";
		}
	}

	private List<String> getAllValues(String key)
	{
		return configList.stream()
				.filter(it -> it.getFirst().equals(key))
				.map(it -> it.getSecond())
				.collect(Collectors.toList());
	}

	private boolean getBool(String key)
	{
		return Boolean.parseBoolean(getValue(key));
	}

	private int getInt(String key)
	{
		return Integer.parseInt(getValue(key));
	}

	private String getString(String key)
	{
		return getValue(key);
	}

	private static Pair<String, String> decodeValueValuePair(String pair)
	{
		String val1 = "";
		String val2 = "";
		try {
			val1 = pair.substring(0, pair.indexOf(",")).trim();
			val2 = pair.substring(pair.indexOf(",") + 1).trim();
		} catch (IndexOutOfBoundsException e) {
			// Line doesn't contain a comma
			System.err.println("\"" + pair + "\" is not a valid csv-value pair.");
			return null;
		}

		return new Pair<>(val1, val2);
	}

	private static String encodeValueValuePair(String val1, String val2)
	{
		return val1 + "," + val2;
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

	private static void writeValList(List<String> configLines, String name, List<String> newStrings)
	{
		// Escape special regex characters
		name = name.replace("\\", "\\\\");
		name = name.replace(".", "\\.");
		name = name.replace("*", "\\*");

		// Replace existing lines
		int i = 0;
		for (String newString : newStrings) {
			String line = formatProperty('s', name, newString);
			boolean replaced = false;
			while (!replaced) {
				if (i < configLines.size()) {
					// Line existing
					if (configLines.get(i).matches("^" + name + " *:.*$")) {
						configLines.set(i, line);
						replaced = true;
					}
				} else {
					// No more matching lines existing
					configLines.add(line);
					replaced = true;
				}
				i++;
			}
		}

		// Delete leftover old values
		while (i < configLines.size()) {
			if (configLines.get(i).matches("^" + name + " *:.*$")) {
				configLines.remove(i);
			} else {
				i++;
			}
		}
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

	/**
	 * Sorts the lines which will be written to the config file.
	 * This prevents parameters apprearing more than once (e.g. "Server") from being spread over the whole document.
	 */
	private static void sortLines(List<String> lines)
	{
		int sameParameters;
		for (int i = 0; i < lines.size(); i += sameParameters + 1) {
			sameParameters = 0;

			String iLine = lines.get(i);
			if (!iLine.matches("^[^#\\t\\n\\r].*:.*")) {
				// Not a key-value line
				continue;
			}

			Pair<String, String> iParsedLine = parseLine(iLine);

			for (int j = i + 1; j < lines.size(); j++) {
				String jLine = lines.get(j);
				if (!jLine.matches("^[^#\\t\\n\\r].*:.*")) {
					// Not a key-value line
					continue;
				}

				Pair<String, String> jParsedLine = parseLine(jLine);

				if (jParsedLine.getFirst().equals(iParsedLine.getFirst())) {
					// Move line up to the first one of its kind
					sameParameters++;
					Collections.rotate(lines.subList(i + sameParameters, j + 1), 1);
				}
			}
		}
	}

	private void setCurrentStateAsOriginal()
	{
		originalGraphics = graphics.clone();
		originalOverlayVisibility = overlayVisibility.clone();
		originalNetworking = networking.clone();
		originalGeneral = general.clone();
		originalTeamColors = teamColors.clone();
	}

	private void readLines(File file, Consumer<? super String> action)
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
		sortLines(lines);

		// Write modified configuration file back
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(configFile));
			out.write(join(lines, getNewline()));
			setCurrentStateAsOriginal();
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
				Pair<String, String> parsedLine = parseLine(line);
				if (parsedLine != null) {
					configList.add(parsedLine);
				}
			}
		});

		// read the parsed data
		try {
			graphics.read();
			overlayVisibility.read();
			networking.read();
			general.read();
			teamColors.read();

			setCurrentStateAsOriginal();
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
