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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.Timer;
import js.math.vector.Vec3f;
import rv.Configuration;
import rv.Viewer;
import rv.ui.DebugInfo;
import rv.world.WorldModel;

/**
 * Communication interface between RoboVis and SimSpark server
 * 
 * @see http://simspark.sourceforge.net/wiki/index.php/Network_Protocol
 * @author Justin Stoecker
 */
public class ServerComm {

    /**
     * Receives messages from rcssserver3d and hands them off to a message parser to handle the data
     * contained in each message
     */
    private class MessageReceiver extends Thread {

        private MessageParser parser = new MessageParser(world);

        private void writeToLogfile(String msg) {
            logfileOutput.write(msg);
            logfileOutput.write("\n");
        }

        @Override
        public void run() {
            try {
                String message = null;
                do {
                    message = readMessage();
                    if (message != null) {
                        try {
                            parser.parse(message);
                            if (logfileOutput != null)
                                writeToLogfile(message);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                } while (message != null);

                // If the thread gets to this point the server has stopped
                // sending messages by closing the connection
                DebugInfo.println(getClass(), "rcssserver3d closed TCP connection");
                disconnect();
                if (autoConnectTimer != null)
                    autoConnectTimer.start();
            } catch (IOException e) {
                disconnect();
                if (autoConnectTimer != null)
                    autoConnectTimer.start();
            }
        }

        private String readMessage() throws IOException {
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

    public interface ServerChangeListener {
        public void connectionChanged(ServerComm server);
    }

    private List<ServerChangeListener> changeListeners = new ArrayList<ServerChangeListener>();
    private Timer                      autoConnectTimer;
    private MessageReceiver            inThread;
    private Socket                     socket;
    private PrintWriter                out             = null;
    private WorldModel                 world;
    private DataInputStream            in;
    private boolean                    connected       = false;
    private String                     serverHost;
    private int                        serverPort;
    private PrintWriter                logfileOutput   = null;
    private boolean                    recordLogs      = false;

    private void setConnected(boolean connected) {
        this.connected = connected;
        for (ServerChangeListener l : changeListeners)
            l.connectionChanged(this);
    }

    public void addChangeListener(ServerChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ServerChangeListener l) {
        changeListeners.remove(l);
    }

    public boolean isConnected() {
        return connected;
    }

    public ServerComm(WorldModel world, Configuration config, Viewer.Mode viewerMode) {
        this.world = world;

        Configuration.Networking net = config.getNetworking();

        serverHost = net.getServerHost();
        serverPort = net.getServerPort();

        // automatically attempt connection with server while not connected
        if (net.isAutoConnect()) {
            autoConnectTimer = new Timer(net.getAutoConnectDelay(), new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (socket == null) {
                        connect(serverHost, serverPort);
                    }
                }
            });
            autoConnectTimer.setRepeats(true);
            autoConnectTimer.start();
        }

        recordLogs = viewerMode != Viewer.Mode.LOGFILE && config.getGeneral().isRecordLogs();
    }

    private void setupNewLogfile() {
        String s = Calendar.getInstance().getTime().toString();
        s = s.replaceAll("\\s+", "_");
        File logFile = new File(String.format("roboviz_log_%s.log", s));
        System.out.println("Recording to new logfile: " + logFile.getName());
        if (logFile != null) {
            try {
                logfileOutput = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeCurrentLogfile() {
        logfileOutput.close();
    }

    public void connect() {
        connect(serverHost, serverPort);
    }

    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new DataInputStream(socket.getInputStream());
            inThread = new MessageReceiver();
            inThread.start();
            DebugInfo.println(getClass(), "connected with rcssserver3d " + socket.getInetAddress()
                    + ":" + socket.getPort());
            if (autoConnectTimer != null)
                autoConnectTimer.stop();
            setConnected(true);
            if (recordLogs)
                setupNewLogfile();
        } catch (IOException e) {

        }
    }

    public void disconnect() {
        if (recordLogs)
            closeCurrentLogfile();

        setConnected(false);
        if (socket != null) {
            try {
                in.close();
            } catch (IOException e) {
                System.err.println("Error: closing input stream with server" + e.getMessage());
                e.printStackTrace();
            }
            out.close();

            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                System.err.println("Error: closing input stream with server" + e.getMessage());
            }

            socket = null;
        }
    }

    private char[] intToBytes(int i) {
        char[] buf = new char[4];
        buf[0] = (char) ((i >> 24) & 255);
        buf[1] = (char) ((i >> 16) & 255);
        buf[2] = (char) ((i >> 8) & 255);
        buf[3] = (char) (i & 255);
        return buf;
    }

    public void sendMessage(String msg) {
        if (out == null) {
            DebugInfo.println(getClass(), String.format("Cannot send message"
                    + " \"%s\" - not connected to server", msg));
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

    public void sendInit() {
        sendMessage("(init)");
    }

    public void kickOff(boolean left) {
        sendMessage(left ? "(kickOff Left)" : "(kickOff Right)");
    }

    public void dropBall() {
        sendMessage("(dropBall)");
    }

    public void moveBall(Vec3f pos) {
        sendMessage(String.format("(ball (pos %.2f %.2f %.2f))", pos.x, pos.y, pos.z));
    }

    public void setPlayMode(String mode) {
        sendMessage(String.format("(playMode %s)", mode));
    }

    public void freeKick(boolean left) {
        setPlayMode(left ? "free_kick_left" : "free_kick_right");
    }

    public void killServer() {
        sendMessage("(killsim)");
    }

    public void moveAgent(Vec3f pos, boolean leftTeam, int agentID) {
        // - (agent (team [Right,Left])(unum <n>)(pos <x y z>)):
        // Set the position and velocity of the given player on the field.
        // Example: (agent (team Left)(unum 1)(pos -52.0 0.0 0.3))
        String team = leftTeam ? "Left" : "Right";
        String m = String.format("(agent (team %s)(unum %d)(pos %.2f %.2f %.2f))", team, agentID,
                pos.x, pos.y, pos.z);
        sendMessage(m);
    }
}
