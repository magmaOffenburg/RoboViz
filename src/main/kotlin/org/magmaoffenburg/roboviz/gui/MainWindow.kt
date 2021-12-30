package org.magmaoffenburg.roboviz.gui

import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.awt.GLCanvas
import org.magmaoffenburg.roboviz.Main
import org.magmaoffenburg.roboviz.configuration.Config.Graphics
import org.magmaoffenburg.roboviz.gui.menus.*
import org.magmaoffenburg.roboviz.gui.windows.LogPlayerControlsPanel
import org.magmaoffenburg.roboviz.rendering.Renderer
import org.magmaoffenburg.roboviz.util.DataTypes
import org.magmaoffenburg.roboviz.util.SwingUtils
import rv.comm.rcssserver.LogPlayer
import rv.comm.rcssserver.ServerComm
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.SwingUtilities

/**
 * FIXME auto focus for canvas to allow key listener to work
 */
class MainWindow : JFrame(), ServerComm.ServerChangeListener, LogPlayer.StateChangeListener {

    private val windowTitle = "${Main.name} ${Main.version}"

    private var isFullscreen = false

    companion object {
        lateinit var instance: MainWindow
        lateinit var glCanvas: GLCanvas

        var logPlayerControls: LogPlayerControlsPanel? = null
    }

    init {
        initializeWindow()
        initializeMenu()
        initializeGLCanvas()

        pack()
        isVisible = true // set visible after everything is initialized
        instance = this

        if (Main.mode == DataTypes.Mode.LOG) {
            initializeLogPlayerControls()
        }
    }

    private fun initializeWindow() {
        title = windowTitle
        defaultCloseOperation = EXIT_ON_CLOSE
        jMenuBar = JMenuBar()
        iconImage = ImageIO.read(MainWindow::class.java.getResource("/images/icon.png"))

        // set the main window position
        location = if (Graphics.centerFrame) {
            SwingUtils.centerWindowOnScreen(this, Point(0,0))
        } else {
            Point(Graphics.frameX, Graphics.frameY)
        }
        toFront()

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                Graphics.frameWidth = glCanvas.width
                Graphics.frameHeight = glCanvas.height

                Main.config.write()
                Renderer.instance.dispose(glCanvas)
            }
        })
    }

    private fun initializeMenu() {
        jMenuBar.removeAll()

        when (Main.mode) {
            DataTypes.Mode.LIVE -> initializeLiveMenu()
            DataTypes.Mode.LOG -> initializeLogMenu()
        }
    }

    private fun initializeLiveMenu() {
        val connection = ConnectionMenu(this)
        val server = ServerMenu()
        val view = ViewMenu()
        val camera = CameraMenu()
        val help = HelpMenu()

        // Live Mode specific items
        view.addSeparator()
        view.addItem("Toggle Server Speed", KeyEvent.VK_M) { view.actions.toggleShowServerSpeed() }
        view.addItem("Playmode Overlay", KeyEvent.VK_O) { view.actions.openPlaymodeOverlay() }
        view.addItem("Log Mode", KeyEvent.VK_F4) { Main.changeMode() }

        // add connection menu to config change listeners
        Main.config.addConfigChangedListener(connection)

        jMenuBar.add(connection)
        jMenuBar.add(server)
        jMenuBar.add(view)
        jMenuBar.add(camera)
        jMenuBar.add(help)
    }

    private fun initializeLogMenu() {
        val view = ViewMenu()
        val camera = CameraMenu()
        val help = HelpMenu()

        view.addSeparator()
        view.addItem("Live Mode", KeyEvent.VK_F4) { Main.changeMode() }

        jMenuBar.add(view)
        jMenuBar.add(camera)
        jMenuBar.add(help)
    }

    private fun initializeGLCanvas() {
        val glProfile = GLProfile.get(GLProfile.GL2)
        val glCapabilities = GLCapabilities(glProfile)

        // set glCapabilities according to config
        glCapabilities.stereo = Graphics.useStereo
        if (Graphics.useFsaa) {
            glCapabilities.sampleBuffers = true
            glCapabilities.numSamples = Graphics.fsaaSamples
        }

        glCanvas = GLCanvas(glCapabilities)
        glCanvas.focusTraversalKeysEnabled = false

        glCanvas.preferredSize = Dimension(Graphics.frameWidth, Graphics.frameHeight)
        add(glCanvas, BorderLayout.CENTER)
    }

    private fun initializeLogPlayerControls() {
        logPlayerControls = LogPlayerControlsPanel()
        logPlayerControls!!.showWindow()
    }

    override fun connectionChanged(server: ServerComm?) {
        server?.let {
            val newTitle = if (it.isConnected) "${it.serverHost}:${it.serverPort} - $windowTitle" else windowTitle
            SwingUtilities.invokeLater { title = newTitle }
        }
    }

    fun toggleFullscreen() {
        val device = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
        device.fullScreenWindow = if (isFullscreen) null else this
        isFullscreen = !isFullscreen
    }

    fun onModeChange() {
        initializeMenu() // reinitialize the menu
        SwingUtilities.updateComponentTreeUI(this)

        when (Main.mode) {
            DataTypes.Mode.LOG -> initializeLogPlayerControls()
            DataTypes.Mode.LIVE -> logPlayerControls?.dispose()
        }
    }

    override fun playerStateChanged(playing: Boolean) = Unit

    override fun logfileChanged() {
        val newTitle = if (Renderer.logPlayer.filePath != null) "${Renderer.logPlayer.filePath} - $windowTitle" else windowTitle
        SwingUtilities.invokeLater { title = newTitle }
    }

}
