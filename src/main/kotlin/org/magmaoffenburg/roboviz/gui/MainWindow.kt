package org.magmaoffenburg.roboviz.gui

import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.awt.GLCanvas
import org.magmaoffenburg.roboviz.Main
import org.magmaoffenburg.roboviz.configuration.Config.Graphics
import org.magmaoffenburg.roboviz.gui.config.ConfigWindow
import org.magmaoffenburg.roboviz.gui.menus.CameraMenu
import org.magmaoffenburg.roboviz.gui.menus.ConnectionMenu
import org.magmaoffenburg.roboviz.gui.menus.ServerMenu
import org.magmaoffenburg.roboviz.gui.menus.ViewMenu
import org.magmaoffenburg.roboviz.gui.panels.LogPlayerControlsPanel
import org.magmaoffenburg.roboviz.rendering.Renderer
import org.magmaoffenburg.roboviz.util.DataTypes
import rv.comm.rcssserver.ServerComm
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JMenuBar

/**
 * FIXME auto focus for canvas to allow key listener to work
 */
class MainWindow : JFrame(), ServerComm.ServerChangeListener {

    private val windowTitle = "RoboVizKt ${Main.version}"

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

        isVisible = true
        instance = this

        if (Main.mode == DataTypes.Mode.LOG) {
            initializeLogPlayerControls()
        }

        //val testWindow = ConfigWindow()
        //testWindow.isVisible = true
    }

    private fun initializeWindow() {
        title = windowTitle
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(Graphics.frameWidth, Graphics.frameHeight)
        jMenuBar = JMenuBar()
        iconImage = ImageIO.read(MainWindow::class.java.getResource("/images/icon.png"))

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                Graphics.frameWidth = width
                Graphics.frameHeight = height

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

        // Live Mode specific items
        view.addSeparator()
        view.addItem("Toggle Server Speed", KeyEvent.VK_M) { println("TODO toggleShowServerSpeed") }
        view.addItem("Playmode Overlay", KeyEvent.VK_O) { println("TODO openPlaymodeOverlay") }

        jMenuBar.add(connection)
        jMenuBar.add(server)
        jMenuBar.add(view)
        jMenuBar.add(camera)
    }

    private fun initializeLogMenu() {
        val view = ViewMenu()
        val camera = CameraMenu()

        jMenuBar.add(view)
        jMenuBar.add(camera)
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

        add(glCanvas, BorderLayout.CENTER)
    }

    private fun initializeLogPlayerControls() {
        logPlayerControls = LogPlayerControlsPanel()
        logPlayerControls!!.isVisible = true
    }

    // TODO investigate window title for logMode
    override fun connectionChanged(server: ServerComm?) {
        server?.let {
            title = if (it.isConnected) "${it.serverHost}:${it.serverPort} - $windowTitle" else windowTitle
        }
    }

    fun toggleFullscreen() {
        val device = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
        device.fullScreenWindow = if (isFullscreen) null else this
        isFullscreen = !isFullscreen
    }

}
