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

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.awt.AWTKeyAdapter;
import com.jogamp.newt.event.awt.AWTMouseAdapter;
import com.jogamp.opengl.util.awt.Screenshot;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import js.jogl.GLInfo;
import js.jogl.prog.GLProgram;
import js.jogl.view.Viewport;
import rv.comm.NetworkManager;
import rv.comm.drawing.Drawings;
import rv.comm.rcssserver.LogPlayer;
import rv.comm.rcssserver.ServerComm;
import rv.comm.rcssserver.scenegraph.SceneGraph;
import rv.content.ContentManager;
import rv.ui.UserInterface;
import rv.ui.menus.MenuBar;
import rv.util.commandline.Argument;
import rv.util.commandline.BooleanArgument;
import rv.util.commandline.IntegerArgument;
import rv.util.commandline.StringArgument;
import rv.util.swing.SwingUtil;
import rv.world.WorldModel;

/**
 * Program entry point / main class. Creates a window and delegates OpenGL rendering the Renderer
 * object.
 *
 * @author Justin Stoecker
 */
public class Viewer
		extends GLProgram implements GLEventListener, ServerComm.ServerChangeListener, LogPlayer.StateChangeListener
{
	private static final String VERSION = "1.6.1";

	public enum Mode {
		LOGFILE,
		LIVE,
	}

	/** Event object for when the main RoboViz window is resized */
	public class WindowResizeEvent extends EventObject
	{
		private final Viewport window;
		private final GLAutoDrawable drawable;

		public Viewport getWindow()
		{
			return window;
		}

		public GLAutoDrawable getDrawable()
		{
			return drawable;
		}

		public WindowResizeEvent(Object src, Viewport window)
		{
			super(src);
			this.window = window;
			this.drawable = getCanvas();
		}
	}

	/** Event listener interface when the main RoboViz window is resized */
	public interface WindowResizeListener {
		void windowResized(WindowResizeEvent event);
	}

	private final List<WindowResizeListener> windowResizeListeners = new ArrayList<>();

	private RVFrame frame;
	private boolean movedFrame;
	private GLCanvas canvas;
	private WorldModel world;
	private UserInterface ui;
	private NetworkManager netManager;
	private ContentManager contentManager;
	private Drawings drawings;
	private Renderer renderer;
	private LogPlayer logPlayer;
	boolean init = false;
	private boolean fullscreen = false;
	private GLInfo glInfo;
	private final Configuration config;
	private String ssName = null;
	private File logFile;
	private String drawingFilter;
	private Mode mode = Mode.LIVE;

	public LogPlayer getLogPlayer()
	{
		return logPlayer;
	}

	public Mode getMode()
	{
		return mode;
	}

	public GLInfo getGLInfo()
	{
		return glInfo;
	}

	public Configuration getConfig()
	{
		return config;
	}

	public Viewport getScreen()
	{
		return screen;
	}

	public WorldModel getWorldModel()
	{
		return world;
	}

	public UserInterface getUI()
	{
		return ui;
	}

	public NetworkManager getNetManager()
	{
		return netManager;
	}

	public Drawings getDrawings()
	{
		return drawings;
	}

	public RVFrame getFrame()
	{
		return frame;
	}

	public void addWindowResizeListener(WindowResizeListener l)
	{
		windowResizeListeners.add(l);
	}

	public void removeWindowResizeListener(WindowResizeListener l)
	{
		windowResizeListeners.remove(l);
	}

	public void shutdown()
	{
		if (config.graphics.saveFrameState)
			storeConfig();
		frame.dispose();
		System.exit(0);
	}

	public Renderer getRenderer()
	{
		return renderer;
	}

	public Viewer(Configuration config, GLCapabilities caps, String[] args)
	{
		super(config.graphics.frameWidth, config.graphics.frameHeight);
		this.config = config;

		parseArgs(args);
		initComponents(caps);

		System.out.println("RoboViz " + VERSION + "\n");
	}

	private void parseArgs(String[] args)
	{
		StringArgument logFileArgument = new StringArgument("logFile", null);
		BooleanArgument logModeArgument = new BooleanArgument("logMode");
		StringArgument serverHostArgument = new StringArgument("serverHost", null);
		IntegerArgument serverPortArgument = new IntegerArgument("serverPort", null, 1, 65535);
		StringArgument drawingFilterArgument = new StringArgument("drawingFilter", ".*");

		handleLogModeArgs(logFileArgument.parse(args), logModeArgument.parse(args));
		config.networking.overrideServerHost(serverHostArgument.parse(args));
		config.networking.overrideServerPort(serverPortArgument.parse(args));
		drawingFilter = drawingFilterArgument.parse(args);
		Argument.endParse(args);
	}

	private void handleLogModeArgs(String logFilePath, boolean logMode)
	{
		String error = null;

		if (logFilePath != null) {
			// handle linux home directory
			logFilePath = logFilePath.replaceFirst("^~", System.getProperty("user.home"));

			logFile = new File(logFilePath);
			mode = Mode.LOGFILE;
			if (!logFile.exists())
				error = "Could not find logfile '" + logFilePath + "'";
			else if (logFile.isDirectory())
				error = "The specified logfile '" + logFilePath + "' is a directory";
		}

		if (error != null) {
			System.err.println(error);
			logFile = null;
		}

		if (logMode)
			mode = Mode.LOGFILE;
	}

	private void initComponents(GLCapabilities caps)
	{
		Globals.setLookFeel();
		canvas = new GLCanvas(caps);
		canvas.setFocusTraversalKeysEnabled(false);

		frame = new RVFrame(getTitle(null));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
			{
				shutdown();
			}
		});
		frame.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e)
			{
				movedFrame = true;
			}
		});
		frame.setIconImage(Globals.getIcon());
		frame.setLayout(new BorderLayout());
		frame.add(canvas, BorderLayout.CENTER);
		restoreConfig();
		frame.setVisible(true);
		attachDrawableAndStart(canvas);
	}

	private void restoreConfig()
	{
		Configuration.Graphics graphics = config.graphics;
		int frameX = graphics.frameX;
		int frameY = graphics.frameY;
		boolean maximized = graphics.isMaximized;

		frame.setSize(graphics.frameWidth, graphics.frameHeight);

		if (graphics.centerFrame)
			frame.setLocationRelativeTo(null);
		else
			frame.setLocation(frameX, frameY);

		frame.setState(maximized ? Frame.MAXIMIZED_BOTH : Frame.NORMAL);
	}

	private void storeConfig()
	{
		Configuration config = Configuration.loadFromFile();

		Configuration.Graphics graphics = config.graphics;
		Point location = frame.getLocation();
		Dimension size = frame.getSize();
		int state = frame.getState();

		if (movedFrame) {
			graphics.frameX = location.x;
			graphics.frameY = location.y;
			graphics.centerFrame = false;
		}
		graphics.frameWidth = size.width;
		graphics.frameHeight = size.height;
		graphics.isMaximized = (state & Frame.MAXIMIZED_BOTH) > 0;

		config.write();
	}

	@Override
	public void init(GLAutoDrawable drawable)
	{
		GL2 gl = drawable.getGL().getGL2();

		if (!init) { // print OpenGL renderer info
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
		ui = new UserInterface(this, drawingFilter);

		if (mode == Mode.LIVE) {
			netManager = new NetworkManager();
			netManager.init(this, config);
			netManager.getServer().addChangeListener(world.getGameState());
			netManager.getServer().addChangeListener(this);
		} else {
			if (!init) {
				logPlayer = new LogPlayer(logFile, world, config, this);
				logPlayer.addListener(this);
				logfileChanged();
			} else
				logPlayer.setWorldModel(world);
		}

		ui.init();
		renderer = new Renderer(this);
		renderer.init(drawable, contentManager, glInfo);

		if (init && oldSceneGraph != null)
			world.setSceneGraph(oldSceneGraph);
		world.addSceneGraphListener(contentManager);

		gl.glClearColor(0, 0, 0, 1);
		init = true;
	}

	public void addKeyListener(KeyListener l)
	{
		(new AWTKeyAdapter(l)).addTo(canvas);
	}

	public void addMouseListener(MouseListener l)
	{
		(new AWTMouseAdapter(l)).addTo(canvas);
	}

	public void takeScreenShot()
	{
		String s = Calendar.getInstance().getTime().toString();
		s = s.replaceAll("[\\s:]+", "_");
		ssName = String.format(Locale.US, "screenshots/%s_%s.png", "roboviz", s);
	}

	private void takeScreenshot(String fileName)
	{
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
	public void toggleFullScreen()
	{
		fullscreen = !fullscreen;
		SwingUtil.getCurrentScreen(frame).setFullScreenWindow(fullscreen ? frame : null);
	}

	public void exitError(String msg)
	{
		System.err.println(msg);
		if (animator != null)
			animator.stop();
		if (frame != null)
			frame.dispose();
		System.exit(1);
	}

	public void update(GL glGeneric)
	{
		if (!init)
			return;

		GL2 gl = glGeneric.getGL2();
		contentManager.update(gl);
		ui.update(gl, elapsedMS);
		world.update(gl, elapsedMS, ui);
		drawings.update();
	}

	public static void main(String[] args)
	{
		final Configuration config = Configuration.loadFromFile();

		GLProfile glp = GLProfile.get(GLProfile.GL2);
		final GLCapabilities caps = new GLCapabilities(glp);
		caps.setStereo(config.graphics.useStereo);
		if (config.graphics.useFsaa) {
			caps.setSampleBuffers(true);
			caps.setNumSamples(config.graphics.fsaaSamples);
		}

		final String[] arguments = args;
		SwingUtilities.invokeLater(() -> new Viewer(config, caps, arguments));
	}

	@Override
	public void dispose(GLAutoDrawable drawable)
	{
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
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		super.reshape(drawable, x, y, w, h);

		WindowResizeEvent event = new WindowResizeEvent(this, screen);
		for (WindowResizeListener l : windowResizeListeners)
			l.windowResized(event);
	}

	@Override
	public void render(GL gl)
	{
		if (!init)
			return;

		if (ssName != null) {
			takeScreenshot(ssName);
			ssName = null;
		}

		renderer.render(drawable, config.graphics);
	}

	@Override
	public void connectionChanged(ServerComm server)
	{
		if (mode != Mode.LIVE)
			return;

		String host = server.isConnected() ? config.networking.getServerHost() : null;
		frame.setTitle(getTitle(host));
	}

	@Override
	public void playerStateChanged(boolean playing)
	{
		if (getUI().getTrackerCamera() != null)
			getUI().getTrackerCamera().setPlaybackSpeed(logPlayer.getPlayBackSpeed());
	}

	@Override
	public void logfileChanged()
	{
		frame.setTitle(getTitle(logPlayer.getFilePath()));
	}

	private String getTitle(String current)
	{
		String roboviz = "RoboViz " + VERSION;
		if (current == null)
			return roboviz;
		return current + " - " + roboviz;
	}

	public class RVFrame extends JFrame
	{
		private MenuBar menuBar;

		public RVFrame(String title) throws HeadlessException
		{
			super(title);
			menuBar = new MenuBar(Viewer.this);
			setJMenuBar(menuBar);
		}

		@Override
		public void list(PrintStream out, int indent)
		{
			// hack to suppress the output of java.awt.Window's
			// hardcoded debugging hotkey Ctrl+Shift+F1
		}

		public MenuBar getMenu()
		{
			return menuBar;
		}
	}
}
