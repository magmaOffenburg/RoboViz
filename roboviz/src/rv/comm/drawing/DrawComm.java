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

package rv.comm.drawing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import js.io.ByteUtil;
import rv.Configuration;
import rv.Viewer;

/**
 * Communication interface between clients sending draw commands and RoboVis
 * 
 * @author Justin Stoecker
 */
public class DrawComm {

    // -------------------------------------------------------------------------

    /** Receives UDP packets */
    private class ReceiveThread extends Thread {
        private static final int BUFFER_SIZE = 512;
        private DatagramSocket   socket      = null;
        private volatile boolean running     = true;

        public ReceiveThread(int port) throws SocketException {
            socket = new DatagramSocket(port);
        }

        @Override
        public void run() {
            while (running) {
                try {
                    byte[] buf = new byte[BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    handle(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            socket.close();
        }
    }

    // -------------------------------------------------------------------------

    private boolean        showWarnings = true;
    private Viewer         viewer;
    private ReceiveThread  packetReceiver;
    private DatagramSocket outSocket;
    private InetAddress    sendAddress;
    private int            sendPort;

    /** Creates a new AgentComm */
    public DrawComm(Viewer viewer, int port) throws SocketException,
            UnknownHostException {
        this.viewer = viewer;
        packetReceiver = new ReceiveThread(port);
        packetReceiver.start();
        outSocket = new DatagramSocket();
        Configuration.Networking config = viewer.getConfig().getNetworking();
        sendAddress = InetAddress.getByName(config.getServerHost());
        sendPort = config.getServerPort();
    }

    /** Sends a UDP packet to all clients */
    public void sendPktToAgents(byte[] buf) throws IOException {
        DatagramPacket pkt = new DatagramPacket(buf, buf.length, sendAddress,
                sendPort);
        outSocket.send(pkt);
    }

    /**
     * Handle incoming UDP packet data
     * */
    public void handle(DatagramPacket packet) {
        byte[] pktData = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), packet.getOffset(), pktData, 0,
                pktData.length);
        ByteBuffer buf = ByteBuffer.wrap(pktData);

        while (buf.hasRemaining()) {
            Command cmd = null;
            try {
                cmd = Command.parse(buf, viewer);
            } catch (Exception e) {
                if (showWarnings) {
                    System.out.printf(
                            "Exception parsing command (start index %d)\n",
                            buf.position());
                    printPacket(packet);
                }
                return;
            }

            if (cmd == null) {
                if (showWarnings) {
                    System.out.printf(
                            "Exception parsing command (start index %d)\n",
                            buf.position());
                    printPacket(packet);
                }
                return;
            } else {
                cmd.execute();
            }
        }
    }

    /** Stops receiving UDP packets and closes connections */
    public void shutdown() {
        packetReceiver.running = false;
    }

    /** Prints packet contents for debug purposes */
    private static void printPacket(DatagramPacket pkt) {
        int length = pkt.getLength();
        int offset = pkt.getOffset();
        byte[] data = pkt.getData();

        System.out.printf("Packet Length: %d\n", length);
        System.out.printf("Packet Offset: %d\n", offset);
        System.out.println("Packet Data:");

        for (int i = 0; i < length; i++)
            System.out.printf("%3d|", i);
        System.out.println();

        for (int i = 0; i < length; i++)
            System.out.print("----");
        System.out.println();

        for (int i = 0; i < length; i++) {
            int d = ByteUtil.uValue(data[i + offset]);
            System.out.printf("%3d|", d);
        }
        System.out.println();
        System.out.println();
    }
}