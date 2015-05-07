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

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventObject;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.swing.SwingUtilities;
import js.jogl.GLInfo;
import js.jogl.prog.GLProgramSwing;
import js.jogl.view.Viewport;
import rv.comm.NetworkManager;
import rv.comm.drawing.Drawings;
import rv.comm.rcssserver.LogPlayer;
import rv.comm.rcssserver.scenegraph.SceneGraph;
import rv.content.ContentManager;
import rv.ui.UserInterface;
import rv.world.WorldModel;
import com.jogamp.opengl.util.awt.Screenshot;

/**
 * Program entry point / main class. Creates a window and delegates OpenGL rendering the Renderer
 * object.
 * 
 * @author Justin Stoecker
 */
public class Viewer extends GLProgramSwing implements GLEventListener {

    public enum Mode {
        LOGFILE, LIVE,
    }

    /** Event object for when the main RoboVis window is resized */
    public class WindowResizeEvent extends EventObject {

        private Viewport       window;
        private GLAutoDrawable drawable;

        public Viewport getWindow() {
            return window;
        }

        public GLAutoDrawable getDrawable() {
            return drawable;
        }

        public WindowResizeEvent(Object src, Viewport window) {
            super(src);
            this.window = window;
            this.drawable = getCanvas();
        }
    }

    /** Event listener interface when the main RoboVis window is resized */
    public interface WindowResizeListener {
        public void windowResized(WindowResizeEvent event);
    }

    // list of modules that should be included in main update loop
    private List<IUpdatable>           updatables            = new ArrayList<>();

    private List<WindowResizeListener> windowResizeListeners = new ArrayList<>();

    private WorldModel                 world;
    private UserInterface              ui;
    private NetworkManager             netManager;
    private ContentManager             contentManager;
    private Drawings                   drawings;
    private Renderer                   renderer;
    private LogPlayer                  logPlayer;
    boolean                            init                  = false;
    private boolean                    fullscreen            = false;
    private GLInfo                     glInfo;
    private Configuration              config;
    private String                     ssName                = null;
    private String                     logFileName;
    private Mode                       mode                  = Mode.LIVE;

    public LogPlayer getLogPlayer() {
        return logPlayer;
    }

    public Mode getMode() {
        return mode;
    }

    public GLInfo getGLInfo() {
        return glInfo;
    }

    public Configuration getConfig() {
        return config;
    }

    public Viewport getScreen() {
        return screen;
    }

    public WorldModel getWorldModel() {
        return world;
    }

    public UserInterface getUI() {
        return ui;
    }

    public NetworkManager getNetManager() {
        return netManager;
    }

    public Drawings getDrawings() {
        return drawings;
    }

    public void addWindowResizeListener(WindowResizeListener l) {
        windowResizeListeners.add(l);
    }

    public void removeWindowResizeListener(WindowResizeListener l) {
        windowResizeListeners.remove(l);
    }

    public void shutdown() {
        getFrame().dispose();
        System.exit(0);
    }

    public Renderer getRenderer() {
        return renderer;
    }

    /** Creates a new RoboVis viewer */
    public Viewer(Configuration config, GLCapabilities caps, String[] args) {
        super("RoboViz", config.graphics.frameWidth, config.graphics.frameHeight, caps);

        this.config = config;

        // check command-line args
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("--logfile") && i < args.length - 1)
                logFileName = args[i + 1];
        }

        getFrame().setIconImage(Globals.getIcon());
    }

    public void takeScreenShot() {
        String s = Calendar.getInstance().getTime().toString();
        s = s.replaceAll("\\s+", "_");
        ssName = String.format(Locale.US, "screenshots/%s_%s.png", "roboviz", s).replace(':', '_');
    }

    private void takeScreenshot(String fileName) {
        BufferedImage ss = Screenshot.readToBufferedImage(0, 0, screen.w, screen.h, false);
        File ssFile = new File(fileName);
        File ssDir = new File("screenshots");
        try {
            if (!ssDir.exists())
                ssDir.mkdir();
            ImageIO.write(ss, "png", ssFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Screenshot taken: " + ssFile.getAbsolutePath());
    }

    /** Enter or exit full-screen exclusive mode depending on current mode */
    public void toggleFullScreen() {
        fullscreen = !fullscreen;
        getCurrentScreen().setFullScreenWindow(fullscreen ? getFrame() : null);
    }

    private GraphicsDevice getCurrentScreen() {
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getScreenDevices();
        Point location = getFrame().getLocation();
        for (GraphicsDevice device : devices) {
            Rectangle bounds = device.getDefaultConfiguration().getBounds();
            if (bounds.contains(location)) {
                return device;
            }
        }
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    }

    public void exitError(String msg) {
        System.err.println(msg);
        animator.stop();
        getFrame().dispose();
        System.exit(1);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        if (logFileName != null)
            mode = Mode.LOGFILE;

        GL2 gl = drawable.getGL().getGL2();

        if (!init) {
            // print OpenGL renderer info
            glInfo = new GLInfo(drawable.getGL());
            glInfo.print();
        }

        // initialize / load content
        contentManager = new ContentManager(config.teamColors);
        if (!contentManager.init(drawable, glInfo)) {
            exitError("Problems loading resource files!");
        }

        SceneGraph oldSceneGraph = null;
        if (init)
            oldSceneGraph = world.getSceneGraph();
        world = new WorldModel();
        world.init(drawable.getGL(), contentManager, config, mode);
        drawings = new Drawings();

        if (mode == Mode.LIVE) {
            netManager = new NetworkManager();
            netManager.init(this, config);

            netManager.getServer().addChangeListener(world.getGameState());
        } else {
            File log = new File(logFileName);
            if (log.exists()) {
                if (!init)
                    logPlayer = new LogPlayer(new File(logFileName), world);
                else
                    logPlayer.setWorldModel(world);
            } else {
                System.err.println("Could not find log file " + logFileName);
                System.exit(0);
            }
        }
        ui = new UserInterface(this);
        ui.init();
        renderer = new Renderer(this);
        renderer.init(drawable, contentManager, glInfo);

        if (init && oldSceneGraph != null)
            world.setSceneGraph(oldSceneGraph);
        world.addSceneGraphListener(contentManager);

        gl.glClearColor(0, 0, 0, 1);
        init = true;
    }

    public void update(GL glGeneric) {
        if (!init)
            return;

        GL2 gl = glGeneric.getGL2();
        contentManager.update(gl);
        ui.update(gl, elapsedMS);
        world.update(gl, elapsedMS, ui);
        drawings.update();

        // update any plug-ins that request updates
        for (IUpdatable module : updatables)
            module.update(gl, world, ui, elapsedMS);
    }

    public static void main(String[] args) {

        final Configuration config = Configuration.loadFromFile();

        GLProfile glp = GLProfile.get(GLProfile.GL2);
        final GLCapabilities caps = new GLCapabilities(glp);
        caps.setStereo(config.graphics.useStereo);
        if (config.graphics.useFsaa) {
            caps.setSampleBuffers(true);
            caps.setNumSamples(config.graphics.fsaaSamples);
        }

        final String[] arguments = args;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Viewer(config, caps, arguments);
            }
        });
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        if (netManager != null)
            netManager.shutdown();
        if (world != null)
            world.dispose(gl);
        if (renderer != null)
            renderer.dispose(gl);
        if (contentManager != null)
            contentManager.dispose(gl);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        super.reshape(drawable, x, y, w, h);

        WindowResizeEvent event = new WindowResizeEvent(this, screen);
        for (WindowResizeListener l : windowResizeListeners)
            l.windowResized(event);
    }

    @Override
    public void render(GL gl) {
        if (!init)
            return;

        if (ssName != null) {
            takeScreenshot(ssName);
            ssName = null;
        }

        renderer.render(drawable, config.graphics);
    }
}
