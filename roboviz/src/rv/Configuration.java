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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Configuration parameters for RoboVis startup
 * 
 * @author Justin Stoecker
 */
public class Configuration {

    private static final String CONFIG_FILE_PATH = "resources/config.txt";

    public class Graphics {

        private boolean useBloom         = false;
        private boolean usePhong         = false;
        private boolean useShadows       = false;
        private boolean softShadow       = false;
        private boolean useStereo        = false;
        private boolean vsync            = true;
        private boolean fsaa             = false;
        private int     fsaaSamples      = 4;
        private int     targetFPS        = 60;
        private int     frameWidth       = 800;
        private int     frameHeight      = 600;
        private int     shadowResolution = 1024;

        public int getTargetFPS() {
            return targetFPS;
        }

        public int getFSAASamples() {
            return fsaaSamples;
        }

        public void setTargetFPS(int targetFPS) {
            this.targetFPS = targetFPS;
        }

        public void setUseBloom(boolean useBloom) {
            this.useBloom = useBloom;
        }

        public void setUsePhong(boolean usePhong) {
            this.usePhong = usePhong;
        }

        public void setUseShadows(boolean useShadows) {
            this.useShadows = useShadows;
        }

        public void setUseStereo(boolean useStereo) {
            this.useStereo = useStereo;
        }

        public boolean useSoftShadows() {
            return softShadow;
        }

        public boolean useBloom() {
            return useBloom;
        }

        public boolean usePhong() {
            return usePhong;
        }

        public boolean useStereo() {
            return useStereo;
        }

        public boolean useShadows() {
            return useShadows;
        }

        public boolean useFSAA() {
            return fsaa;
        }

        public int getFrameHeight() {
            return frameHeight;
        }

        public int getShadowResolution() {
            return shadowResolution;
        }

        public void setFrameHeight(int frameHeight) {
            this.frameHeight = frameHeight;
        }

        public int getFrameWidth() {
            return frameWidth;
        }

        public void setFrameWidth(int frameWidth) {
            this.frameWidth = frameWidth;
        }

        public boolean isVsync() {
            return vsync;
        }

        private void read(BufferedReader in) throws IOException {
            in.readLine();
            useBloom = Boolean.parseBoolean(getVal(in.readLine()));
            usePhong = Boolean.parseBoolean(getVal(in.readLine()));
            useShadows = Boolean.parseBoolean(getVal(in.readLine()));
            softShadow = Boolean.parseBoolean(getVal(in.readLine()));
            shadowResolution = Integer.parseInt(getVal(in.readLine()));
            useStereo = Boolean.parseBoolean(getVal(in.readLine()));
            vsync = Boolean.parseBoolean(getVal(in.readLine()));
            fsaa = Boolean.parseBoolean(getVal(in.readLine()));
            fsaaSamples = Integer.parseInt(getVal(in.readLine()));
            targetFPS = Integer.parseInt(getVal(in.readLine()));
            frameWidth = Integer.parseInt(getVal(in.readLine()));
            frameHeight = Integer.parseInt(getVal(in.readLine()));
            in.readLine();
        }

        private void write(BufferedWriter out) throws IOException {
            out.write("Graphics Settings:\n");
            out.write(String.format("%-20s : %b\n", "Bloom", useBloom));
            out.write(String.format("%-20s : %b\n", "Phong", usePhong));
            out.write(String.format("%-20s : %b\n", "Shadows", useShadows));
            out.write(String.format("%-20s : %b\n", "Soft Shadows", softShadow));
            out.write(String.format("%-20s : %b\n", "Shadow Resolution",
                    shadowResolution));
            out.write(String.format("%-20s : %b\n", "Stereo 3D", useStereo));
            out.write(String.format("%-20s : %b\n", "V-Sync", vsync));
            out.write(String.format("%-20s : %b\n", "FSAA", fsaa));
            out.write(String
                    .format("%-20s : %b\n", "FSAA Samples", fsaaSamples));
            out.write(String.format("%-20s : %d\n", "Target FPS", targetFPS));
            out.write(String.format("%-20s : %d\n", "Frame Width", frameWidth));
            out.write(String
                    .format("%-20s : %d\n", "Frame Height", frameHeight));
            out.write("\n");
        }
    }

    public class Networking {
        private boolean autoConnect      = true;
        private String  serverHost       = "localhost";
        private int     serverPort       = 3200;
        private int     listenPort       = 32769;
        private int     autoConnectDelay = 1000;

        public int getListenPort() {
            return listenPort;
        }

        public String getServerHost() {
            return serverHost;
        }

        public int getServerPort() {
            return serverPort;
        }

        public boolean isAutoConnect() {
            return autoConnect;
        }

        public int getAutoConnectDelay() {
            return autoConnectDelay;
        }

        public void setAutoConnect(boolean autoConnect) {
            this.autoConnect = autoConnect;
        }

        public void setAutoConnectDelay(int autoConnectDelay) {
            this.autoConnectDelay = autoConnectDelay;
        }

        public void setListenPort(int listenPort) {
            this.listenPort = listenPort;
        }

        public void setServerHost(String serverHost) {
            this.serverHost = serverHost;
        }

        public void setServerPort(int serverPort) {
            this.serverPort = serverPort;
        }

        private void read(BufferedReader in) throws IOException {
            in.readLine();
            autoConnect = Boolean.parseBoolean(getVal(in.readLine()));
            autoConnectDelay = Integer.parseInt(getVal(in.readLine()));
            serverHost = getVal(in.readLine());
            serverPort = Integer.parseInt(getVal(in.readLine()));
            listenPort = Integer.parseInt(getVal(in.readLine()));
            in.readLine();
        }

        private void write(BufferedWriter out) throws IOException {
            out.write("Networking Settings:\n");
            out.write(String
                    .format("%-20s : %b\n", "Auto-Connect", autoConnect));
            out.write(String.format("%-20s : %d\n", "Auto-Connect Delay",
                    autoConnectDelay));
            out.write(String.format("%-20s : %s\n", "Server Host", serverHost));
            out.write(String.format("%-20s : %d\n", "Server Port", serverPort));
            out.write(String.format("%-20s : %d\n", "Drawing Port", listenPort));
            out.write("\n");
        }
    }

    public class General {
        private boolean recordLogs = false;

        public boolean isRecordLogs() {
            return recordLogs;
        }

        private void read(BufferedReader in) throws IOException {
            in.readLine();
            recordLogs = Boolean.parseBoolean(getVal(in.readLine()));
            in.readLine();
        }

        private void write(BufferedWriter out) throws IOException {
            out.write("General Settings:\n");
            out.write(String.format("%-20s : %b\n", "Record Logfiles",
                    recordLogs));
            out.write("\n");
        }
    }

    private Graphics   graphics   = new Graphics();
    private Networking networking = new Networking();
    private General    general    = new General();

    public Graphics getGraphics() {
        return graphics;
    }

    public Networking getNetworking() {
        return networking;
    }

    public General getGeneral() {
        return general;
    }

    private static String getVal(String line) {
        return line.substring(line.indexOf(":") + 1).trim();
    }

    public void write() {
        File configFile = new File(CONFIG_FILE_PATH);
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(configFile));
            graphics.write(out);
            networking.write(out);
            general.write(out);
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

    private Configuration read(File file) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            try {
                graphics.read(in);
                networking.read(in);
                general.read(in);
            } catch (IOException e) {
                System.err.println("Error reading values from config file");
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Could not find config file");
            e.printStackTrace();
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

    public static Configuration loadFromFile() {
        return loadFromFile(new File(CONFIG_FILE_PATH));
    }

    public static Configuration loadFromFile(File file) {
        return new Configuration().read(file);
    }
}