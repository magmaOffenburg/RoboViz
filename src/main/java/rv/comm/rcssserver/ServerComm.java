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

package rv.comm.rcssserver;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.Timer;
import jsgl.math.vector.Vec3f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magmaoffenburg.roboviz.configuration.Config.General;
import org.magmaoffenburg.roboviz.configuration.Config.Networking;
import org.magmaoffenburg.roboviz.util.DataTypes;
import rv.comm.drawing.DrawComm.DrawCommListener;
import rv.world.WorldModel;

/**
 * Communication interface between RoboViz and SimSpark server
 *
 * @see <a href="https://gitlab.com/robocup-sim/SimSpark/-/wikis/Network-Protocol">
 *     https://gitlab.com/robocup-sim/SimSpark/-/wikis/Network-Protocol</a>
 * @author Justin Stoecker
 */
public class ServerComm implements DrawCommListener
{
	/**
	 * Receives messages from rcssserver3d and hands them off to a message parser to handle the data
	 * contained in each message
	 */
	private class MessageReceiver extends Thread
	{
		private final MessageParser parser = new MessageParser(world);

		private final String host;

		private final int port;

		public MessageReceiver(String host, int port)
		{
			this.host = host;
			this.port = port;
		}

		@Override
		public void run()
		{
			// Receive messages in a separate thread and put them in a queue
			LinkedBlockingQueue<Optional<String>> messages = new LinkedBlockingQueue<>(3000);
			new Thread(() -> {
				try {
					socket = new Socket(host, port);
					out = new PrintWriter(socket.getOutputStream(), true);
					in = new DataInputStream(socket.getInputStream());
					setConnected(true);
					String message;
					do {
						message = readMessage();
						messages.put(Optional.ofNullable(message));
					} while (message != null);
				} catch (IOException | InterruptedException e) {
					// This is fine, just leave.
					// The surrounding thread will quit when receiving an
					// empty optional.
					try {
						messages.put(Optional.empty());
					} catch (InterruptedException ignored) {
					}
				}

				// If the thread gets to this point the server has stopped
				// sending messages by closing the connection
				// DebugInfo.println(getClass(), "rcssserver3d closed TCP connection");
				disconnect();
				if (autoConnectTimer != null)
					autoConnectTimer.start();
			}).start();

			if (recordLogs) {
				setupNewLogfile();
			}

			Optional<String> message = Optional.empty();
			long lastUpdateTimestamp = 0;
			long monitorStepMillis = Math.round(Networking.INSTANCE.getMonitorStep() * 1000.0);
			do {
				// Retrieve a message from the queue
				try {
					message = messages.take();
				} catch (InterruptedException e) {
					continue;
				}

				if (Networking.INSTANCE.getUseBuffer()) {
					// Wait until the time for one monitor frame elapsed.
					// This ensures that two messages are not applied immediately after each
					// other. That may be the case with a bad network connection.
					long elapsed = System.currentTimeMillis() - lastUpdateTimestamp;
					long timeLeft = monitorStepMillis - elapsed;
					if (timeLeft > 0) {
						try {
							Thread.sleep(timeLeft);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					lastUpdateTimestamp = System.currentTimeMillis();
				}

				// Process message
				message.ifPresent(msg -> {
					try {
						parser.parse(msg);
						if (logfileOutput != null)
							writeToLogfile(msg);
					} catch (ParseException e) {
						LOGGER.error("Unable to parse server message", e);
					}
				});
			} while (message.isPresent());
		}

		private String readMessage() throws IOException
		{
			// message is prefixed by its size in bytes
			int length = in.readInt();
			if (length <= 0)
				return null;

			// read from stream until all bytes in message are read
			byte[] buf = new byte[length];
			int bytesRead = 0;
			while (bytesRead < length)
				bytesRead += in.read(buf, bytesRead, length - bytesRead);

			return new String(buf);
		}
	}

	public interface ServerChangeListener
	{
		void connectionChanged(ServerComm server);
	}

	private static final Logger LOGGER = LogManager.getLogger();
	private final List<ServerChangeListener> changeListeners = new CopyOnWriteArrayList<>();
	private Timer autoConnectTimer;

	private Socket socket;
	private PrintWriter out = null;
	private final WorldModel world;
	private DataInputStream in;
	private boolean connected = false;
	private String serverHost;
	private int serverPort;
	private PrintWriter logfileOutput = null;
	private boolean recordLogs = false;
	private String logfileDirectory = null;
	private String drawCommands = "";

	private void setConnected(boolean connected)
	{
		this.connected = connected;
		for (ServerChangeListener l : changeListeners)
			l.connectionChanged(this);
	}

	public void addChangeListener(ServerChangeListener l)
	{
		changeListeners.add(l);
	}

	public void removeChangeListener(ServerChangeListener l)
	{
		changeListeners.remove(l);
	}

	public boolean isConnected()
	{
		return connected;
	}

	public WorldModel getWorldModel()
	{
		return world;
	}

	private void writeToLogfile(String msg)
	{
		synchronized (this) {
			logfileOutput.write(drawCommands);
			drawCommands = "";
		}
		logfileOutput.write(msg);
		logfileOutput.write("\n");
	}

	public ServerComm(WorldModel world, DataTypes.Mode viewerMode)
	{
		this.world = world;

		serverHost = Networking.INSTANCE.getCurrentHost();
		serverPort = Networking.INSTANCE.getCurrentPort();

		// automatically attempt connection with server while not connected
		if (Networking.INSTANCE.getAutoConnect()) {
			autoConnectTimer = new Timer(Networking.INSTANCE.getAutoConnectDelay(), e -> {
				if (socket == null) {
					connect(serverHost, serverPort);
				}
			});
			autoConnectTimer.setRepeats(true);
			autoConnectTimer.start();
		}

		recordLogs = viewerMode != DataTypes.Mode.LOG && General.INSTANCE.getRecordLogs();
		logfileDirectory = General.INSTANCE.getLogfileDirectory();
	}

	private void setupNewLogfile()
	{
		String logDirPath = "logfiles";
		if (logfileDirectory != null && !logfileDirectory.isEmpty()) {
			logDirPath = logfileDirectory;
		}

		File logDir = new File(logDirPath);
		if (!logDir.exists())
			logDir.mkdir();

		String s = Calendar.getInstance().getTime().toString();
		s = s.replaceAll("[\\s:]+", "_");
		File logFile = new File(logDirPath + String.format("/roboviz_log_%s.log", s));
		LOGGER.info("Recording to new logfile: " + logFile.getPath());
		try {
			logfileOutput = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
		} catch (IOException e) {
			LOGGER.error("Unable to create new logfile", e);
		}
	}

	private void closeCurrentLogfile()
	{
		if (logfileOutput != null)
			logfileOutput.close();
	}

	public void connect()
	{
		connect(serverHost, serverPort);
	}

	public void changeConnection(String host, int port)
	{
		serverHost = host;
		serverPort = port;
		disconnect();
		connect(host, port);
	}

	public void connect(String host, int port)
	{
		if (autoConnectTimer != null)
			autoConnectTimer.stop();
		new MessageReceiver(host, port).start();
	}

	public void disconnect()
	{
		if (autoConnectTimer != null)
			autoConnectTimer.stop();

		if (recordLogs)
			closeCurrentLogfile();

		setConnected(false);
		if (socket != null) {
			try {
				in.close();
			} catch (IOException e) {
				LOGGER.error("Closing input stream with server", e);
			}
			out.close();

			try {
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				LOGGER.error("Error: closing input stream with server", e);
			}

			socket = null;
		}
	}

	private char[] intToBytes(int i)
	{
		char[] buf = new char[4];
		buf[0] = (char) ((i >> 24) & 255);
		buf[1] = (char) ((i >> 16) & 255);
		buf[2] = (char) ((i >> 8) & 255);
		buf[3] = (char) (i & 255);
		return buf;
	}

	public void sendMessage(String msg)
	{
		if (out == null) {
			LOGGER.debug(String.format("Cannot send message \"%s\" - not connected to server", msg));
			return;
		}
		char[] buf = new char[4 + msg.length()];
		char[] msgSize = intToBytes(msg.length());
		System.arraycopy(msgSize, 0, buf, 0, 4);
		for (int i = 0; i < msg.length(); i++)
			buf[i + 4] = msg.charAt(i);

		out.write(buf);
		out.flush();
	}

	public void sendInit()
	{
		sendMessage("(init)");
	}

	public void kickOff(boolean left)
	{
		sendMessage(left ? "(kickOff Left)" : "(kickOff Right)");
	}

	public void dropBall()
	{
		sendMessage("(dropBall)");
	}

	public void moveBall(Vec3f pos)
	{
		moveBall(pos, new Vec3f(0, 0, 0));
	}

	public void moveBall(Vec3f pos, Vec3f vel)
	{
		sendMessage(String.format(Locale.US, "(ball (pos %.2f %.2f %.2f) (vel %.2f %.2f %.2f))", pos.x, pos.y, pos.z,
				vel.x, vel.y, vel.z));
	}

	public void setPlayMode(String mode)
	{
		sendMessage(String.format("(playMode %s)", mode));
	}

	public void freeKick(boolean left)
	{
		setPlayMode(left ? "free_kick_left" : "free_kick_right");
	}

	public void directFreeKick(boolean left)
	{
		setPlayMode(left ? "direct_free_kick_left" : "direct_free_kick_right");
	}

	public void killServer()
	{
		sendMessage("(killsim)");
	}

	public void moveAgent(Vec3f pos, boolean leftTeam, int agentID)
	{
		// - (agent (team [Right,Left])(unum <n>)(pos <x y z>)):
		// Set the position and velocity of the given player on the field.
		// Example: (agent (team Left)(unum 1)(pos -52.0 0.0 0.3))
		String team = leftTeam ? "Left" : "Right";
		String m = String.format(
				Locale.US, "(agent (team %s)(unum %d)(pos %.2f %.2f %.2f))", team, agentID, pos.x, pos.y, pos.z);
		sendMessage(m);
	}

	public void resetTime()
	{
		sendMessage("(time 0)");
	}

	public void requestFullState()
	{
		sendMessage("(reqfullstate)");
	}

	@Override
	public void drawCommandReceived(byte[] cmd)
	{
		if (logfileOutput != null) {
			synchronized (this) {
				drawCommands += Arrays.toString(cmd);
			}
		}
	}

	public String getServerHost()
	{
		return serverHost;
	}

	public int getServerPort()
	{
		return serverPort;
	}
}
