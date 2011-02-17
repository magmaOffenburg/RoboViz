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
import java.awt.event.WindowAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import js.jogl.GLInfo;
import js.jogl.view.Viewport;
import rv.comm.NetworkManager;
import rv.comm.drawing.Drawings;
import rv.comm.rcssserver.LogPlayer;
import rv.content.ContentManager;
import rv.ui.UserInterface;
import rv.world.WorldModel;

import com.jogamp.opengl.util.FPSAnimator;

/**
 * Program entry point / main class. Creates a window and delegates OpenGL
 * rendering the Renderer object.
 * 
 * @author Justin Stoecker
 */
public class Viewer implements GLEventListener {

    // necessary for JOGL/Swing interaction
    static {
        GLProfile.initSingleton(true);
    }

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
            this.drawable = Viewer.this.canvas;
        }
    }

    /** Event listener interface when the main RoboVis window is resized */
    public interface WindowResizeListener {
        public void windowResized(WindowResizeEvent event);
    }

    // list of modules that should be included in main update loop
    private List<IUpdatable>           updatables            = new ArrayList<IUpdatable>();

    private List<WindowResizeListener> windowResizeListeners = new ArrayList<WindowResizeListener>();

    private Viewport                   screen;
    private WorldModel                 world;
    private UserInterface              ui;
    private NetworkManager             netManager;
    private ContentManager             contentManager;
    private Drawings                   drawings;
    private Renderer                   renderer;
    private LogPlayer                  logPlayer;
    boolean                            init                  = false;
    private boolean                    fullscreen            = false;
    private JFrame                     frame;
    private final FPSAnimator          animator;
    private GLInfo                     glInfo;
    private Configuration              config;
    private GLCanvas                   canvas;

    private double                     elapsedMS             = 0;
    private long                       lastNanoTime          = 0;
    private double                     fpsTimer              = 0;
    private double                     fps                   = 0;

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

    public GLAutoDrawable getDrawable() {
        return canvas;
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
        frame.dispose();
        System.exit(0);
    }

    public Renderer getRenderer() {
        return renderer;
    }

    /** Creates a new RoboVis viewer */
    public Viewer(Configuration config, String[] args) {
        this.config = config;

        // check command-line args
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("--logfile") && i < args.length - 1)
                logFileName = args[i + 1];
        }

        // configure OpenGL context
        GLProfile glp = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(glp);
        caps.setStereo(config.getGraphics().useStereo());
        if (config.getGraphics().useFSAA()) {
            caps.setSampleBuffers(true);
            caps.setNumSamples(config.getGraphics().getFSAASamples());
        }

        // create window
        int w = config.getGraphics().getFrameWidth();
        int h = config.getGraphics().getFrameHeight();
        screen = new Viewport(0, 0, w, h);
        canvas = new GLCanvas(caps);
        canvas.addGLEventListener(this);
        frame = new JFrame("RoboViz");

        frame.setSize(w, h);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.add(canvas);

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                frame.dispose();
                System.exit(0);
            }
        });

        // start render loop
        int fps = config.getGraphics().getTargetFPS();
        animator = new FPSAnimator(canvas, fps);

        animator.start();
    }

    /** Enter or exit full-screen exclusive mode depending on current mode */
    public void toggleFullScreen() {
        fullscreen = !fullscreen;
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
        gd.setFullScreenWindow(fullscreen ? frame : null);
    }

    public void exitError(String msg) {
        System.err.println(msg);
        animator.stop();
        frame.dispose();
        System.exit(1);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        if (logFileName != null)
            mode = Mode.LOGFILE;

        GL2 gl = drawable.getGL().getGL2();

        // print OpenGL renderer info
        glInfo = new GLInfo(drawable.getGL());
        glInfo.print();
        
        // initialize / load content
        contentManager = new ContentManager();
        if (!contentManager.init(drawable, glInfo, config)) {
            exitError("Problems loading resource files!");
        }

        world = new WorldModel();
        world.init(drawable.getGL(), contentManager, config.getGraphics(), mode);
        drawings = new Drawings();

        if (mode == Mode.LIVE) {
            netManager = new NetworkManager();
            netManager.init(this, config);
        } else {
            File log = new File(logFileName);
            if (log.exists())
                logPlayer = new LogPlayer(new File(logFileName), world);
            else {
                System.err.println("Could not find log file!");
                shutdown();
            }
        }
        ui = new UserInterface(this);
        ui.init();
        renderer = new Renderer(this);
        renderer.init(drawable, contentManager, glInfo);

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

        Configuration config = Configuration.loadFromFile();
        new Viewer(config, args);
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
    public void display(GLAutoDrawable drawable) {
        if (!init)
            return;

        long nanoTime = System.nanoTime();
        if (lastNanoTime > 0)
            elapsedMS = (nanoTime - lastNanoTime) / 10e5;
        lastNanoTime = nanoTime;

        fpsTimer += elapsedMS;
        if (fpsTimer >= 1000.0) {
            fps = 1000.0 / elapsedMS;
            fpsTimer -= 1000.0;
        }

        update(drawable.getGL());
        renderer.render(drawable, config.getGraphics());
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        screen = new Viewport(x, y, w, h);

        WindowResizeEvent event = new WindowResizeEvent(this, screen);
        for (WindowResizeListener l : windowResizeListeners)
            l.windowResized(event);
    }
}
