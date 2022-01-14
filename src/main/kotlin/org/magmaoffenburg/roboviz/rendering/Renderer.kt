package org.magmaoffenburg.roboviz.rendering

import com.jogamp.newt.event.KeyListener
import com.jogamp.newt.event.MouseListener
import com.jogamp.newt.event.awt.AWTKeyAdapter
import com.jogamp.newt.event.awt.AWTMouseAdapter
import com.jogamp.opengl.GL
import com.jogamp.opengl.GL2
import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLProfile
import com.jogamp.opengl.fixedfunc.GLLightingFunc
import com.jogamp.opengl.glu.GLU
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil
import com.jogamp.opengl.util.gl2.GLUT
import jsgl.jogl.FrameBufferObject
import jsgl.jogl.GLInfo
import jsgl.jogl.Texture2D
import jsgl.jogl.prog.GLProgram
import jsgl.jogl.view.Viewport
import org.apache.logging.log4j.kotlin.logger
import org.magmaoffenburg.roboviz.Main
import org.magmaoffenburg.roboviz.configuration.Config.*
import org.magmaoffenburg.roboviz.gui.MainWindow
import org.magmaoffenburg.roboviz.util.DataTypes
import rv.comm.NetworkManager
import rv.comm.drawing.Drawings
import rv.comm.rcssserver.LogPlayer
import rv.content.ContentManager
import rv.effects.EffectManager
import rv.ui.screens.LiveGameScreen
import rv.ui.screens.LogfileModeScreen
import rv.ui.screens.ViewerScreenBase
import rv.util.WindowResizeEvent
import rv.world.WorldModel
import rv.world.rendering.BasicSceneRenderer
import rv.world.rendering.PhongWorldRenderer
import rv.world.rendering.SceneRenderer
import rv.world.rendering.VSMPhongWorldRenderer
import java.io.File
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO

class Renderer : GLProgram(MainWindow.instance.width, MainWindow.instance.height) {

    private val logger = logger()

    private lateinit var glInfo: GLInfo
    private var isInitialized = false

    // Renderer.java
    private var numSamples = -1
    private var sceneFBO: FrameBufferObject? = null
    private var msSceneFBO: FrameBufferObject? = null
    private var sceneRenderer: SceneRenderer? = null

    companion object {
        lateinit var instance: Renderer
        var renderSettingsChanged = false
        var screenshotOnRender = false

        lateinit var contentManager: ContentManager
        lateinit var world: WorldModel
        lateinit var drawings: Drawings
        lateinit var effectManager: EffectManager
        lateinit var cameraController: CameraController
        lateinit var netManager: NetworkManager
        lateinit var logPlayer: LogPlayer
        lateinit var activeScreen: ViewerScreenBase

        // Renderer.java
        val glu = GLU()
        val glut = GLUT()

        // Helpers
        fun activeScreenIsInitialized(): Boolean {
            return this::activeScreen.isInitialized
        }

        fun netManagerIsInitialized(): Boolean {
            return this::netManager.isInitialized
        }
    }

    init {
        attachDrawableAndStart(MainWindow.glCanvas)
        instance = this
    }

    /**
     * FIXME on linux(x11) this is called when switching displays
     */
    override fun init(drawable: GLAutoDrawable?) {
        if (drawable == null) return // make sure drawable != null

        if (!isInitialized) {
            GLInfo(drawable.gl).print()
        }

        val gl = drawable.gl
        val oldSceneGraph =  if (isInitialized) world.sceneGraph else null

        glInfo = GLInfo(gl)

        contentManager = ContentManager(TeamColors)
        if (!contentManager.init(drawable, glInfo)) {
            logger.error { "Problems loading resource files!" }
        }

        world = WorldModel()
        world.init(gl, contentManager, Main.mode)

        drawings = Drawings()

        // initialize all camera stuff
        cameraController = CameraController(drawable)

        if (Main.mode == DataTypes.Mode.LIVE) {
            netManager = NetworkManager()
            netManager.init()
            netManager.server.addChangeListener(world.gameState)
            netManager.server.addChangeListener(MainWindow.instance)
        } else {
            if (!isInitialized) {
                initLogPlayer()
            } else {
                logPlayer.setWorldModel(world)
            }

        }

        if (activeScreenIsInitialized()) {
            activeScreen.setEnabled(MainWindow.glCanvas, false)
        }
        activeScreen = if (Main.mode == DataTypes.Mode.LIVE) LiveGameScreen() else LogfileModeScreen()
        activeScreen.setEnabled(MainWindow.glCanvas, true)

        gl?.let { initEffects(gl) }

        if (isInitialized && oldSceneGraph != null) {
            world.sceneGraph = oldSceneGraph
        }
        world.addSceneGraphListener(contentManager)

        gl?.gL2?.glClearColor(0F, 0F, 0F, 1F)
        isInitialized = true

        logger.info { "Initialization successful" }
    }

    /**
     * always shutdown before disposing anything
     */
    override fun dispose(drawable: GLAutoDrawable?) {
        if (netManagerIsInitialized())
            netManager.shutdown()

        world.dispose(drawable?.gl)
        effectManager.dispose(drawable?.gl)
        contentManager.dispose(drawable?.gl)
        sceneFBO?.dispose(drawable?.gl)
        msSceneFBO?.dispose(drawable?.gl)
        sceneRenderer?.dispose(drawable?.gl)

        // set the sceneRenderer to null, since we may need a new one
        sceneRenderer = null // TODO this is workaround for the fixme on linux(x11)
    }

    override fun update(gl: GL?) {
        if (!isInitialized) return

        val gl2 = gl?.gL2
        contentManager.update(gl2)
        cameraController.update(elapsedMS)
        world.update(gl2, elapsedMS)
        drawings.update()
    }

    override fun render(gl: GL?) {
        if (!isInitialized) return

        // screenshot
        if (screenshotOnRender) takeScreenshot()

        // Renderer.render()
        synchronized(world) {
            val gl2 = drawable.gl.gL2

            if (renderSettingsChanged) {
                updateRenderingSettings()
            }

            if (Graphics.useShadows) {
                effectManager.shadowRenderer.render(gl2, world, drawings)
            }

            if (Graphics.useStereo) {
                CameraController.vantage.applyLeft(gl2, glu, screen)
                gl2?.glDrawBuffer(GL2.GL_BACK_LEFT)
                gl2?.glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)
                drawScene(gl2)

                CameraController.vantage.applyRight(gl2, glu, screen)
                gl2.glDrawBuffer(GL2.GL_BACK_RIGHT)
                gl2.glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)
                drawScene(gl2)
            } else {
                CameraController.vantage.apply(gl2, glu, screen)
                gl2.glDrawBuffer(GL.GL_BACK)
                gl2.glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)
                drawScene(gl2)
            }

            // UserInterface.render()
            gl2.glDisable(GL.GL_DEPTH_TEST)
            gl2.glEnable(GL.GL_BLEND)
            gl2.glDisable(GL2.GL_LIGHTING)
            gl2.glMatrixMode(GL2.GL_PROJECTION)
            gl2.glLoadIdentity()
            glu.gluOrtho2D(0.0, screen.w.toDouble(), 0.0, screen.h.toDouble())
            gl2.glMatrixMode(GL2.GL_MODELVIEW)
            gl2.glLoadIdentity()

            activeScreen.render(gl2, glu, glut, screen)

            gl2.glDisable(GL.GL_BLEND)
        }

    }

    override fun reshape(drawable: GLAutoDrawable?, x: Int, y: Int, width: Int, height: Int) {
        super.reshape(drawable, x, y, width, height)

        if (Graphics.useBloom || Graphics.useShadows) {
            sceneFBO?.dispose(drawable!!.gl)
            msSceneFBO?.dispose(drawable!!.gl)
            genFBO(drawable!!.gl.gL2, screen)
        }

        val event = WindowResizeEvent(this, screen)
        activeScreen.windowResized(event)
        effectManager.bloom?.windowResized(event)
    }

    override fun addKeyListener(l: KeyListener?) {
        AWTKeyAdapter(l, MainWindow.glCanvas).addTo(MainWindow.glCanvas)
    }

    override fun addMouseListener(l: MouseListener?) {
        AWTMouseAdapter(l, MainWindow.glCanvas).addTo(MainWindow.glCanvas)
    }

    private fun initLogPlayer() {
        val helperFile = File(General.logReplayFile)
        logPlayer = LogPlayer(helperFile, world)
        Thread.sleep(10) // there needs to be a short delay, or else the logMode wont happen FIXME
        logPlayer.addListener(MainWindow.logPlayerControls)
        logPlayer.addListener(MainWindow.instance)
        MainWindow.instance.logfileChanged()
    }

    /**
     * This is the former Renderer.init()
     * Initialize effects (FSAA, Bloom, VSync)
     */
    private fun initEffects(gl: GL) {
        val supportAAFBO = glInfo.extSupported("GL_EXT_framebuffer_multisample") && glInfo.extSupported("GL_EXT_framebuffer_blit")
        if (Graphics.useFsaa && !supportAAFBO) {
            logger.warn { "No support for FSAA while bloom enabled" }
        }

        val useFSAA = Graphics.useFsaa && ((supportAAFBO && Graphics.useBloom) || !Graphics.useBloom)
        if (useFSAA) {
            drawable.gl.glEnable(GL.GL_MULTISAMPLE)
        } else {
            drawable.gl.glDisable(GL.GL_MULTISAMPLE)
        }

        drawable?.gl?.swapInterval = if (Graphics.useVsync) 1 else 0 // vsync

        effectManager = EffectManager()
        effectManager.init(gl.gL2, getScreen(), Graphics, contentManager)

        if (Graphics.useBloom) {
            numSamples = if (useFSAA) Graphics.fsaaSamples else -1
            genFBO(gl.gL2, screen) // if we do post-processing we'll need an FBO for the scene
        }

        selectRenderer(gl.gL2, contentManager)
        //vantage = CameraController.camera
    }

    private fun selectRenderer(gl: GL2, cm: ContentManager) {
        while (sceneRenderer == null) {
            sceneRenderer = when {
                Graphics.useShadows -> VSMPhongWorldRenderer(effectManager)
                Graphics.usePhong -> PhongWorldRenderer()
                else -> BasicSceneRenderer()
            }

            if (!sceneRenderer!!.init(gl, Graphics, cm)) {
                logger.error { "Could not initialize $sceneRenderer" }
                sceneRenderer = null
            }
        }
    }

    private fun genFBO(gl: GL2, vp: Viewport) {
        if (numSamples > 0) {
            msSceneFBO = FrameBufferObject.create(gl, vp.w, vp.h, GL.GL_RGBA, numSamples)
            sceneFBO = FrameBufferObject.createNoDepth(gl, vp.w, vp.h, GL.GL_RGB8)
        } else {
            sceneFBO = FrameBufferObject.create(gl, vp.w, vp.h, GL.GL_RGB)
        }
    }

    private fun drawScene(gl: GL2) {
        if (Graphics.useBloom) {
            if (msSceneFBO != null) {
                msSceneFBO!!.bind(gl)
                msSceneFBO!!.clear(gl)
                sceneFBO!!.setViewport(gl)
                sceneRenderer?.render(gl, world, drawings)

                val w = sceneFBO!!.getColorTexture(0).width
                val h = sceneFBO!!.getColorTexture(0).height
                gl.glBindFramebuffer(GL2.GL_READ_FRAMEBUFFER, msSceneFBO!!.id)
                gl.glBindFramebuffer(GL2.GL_DRAW_FRAMEBUFFER, sceneFBO!!.id)
                gl.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL.GL_COLOR_BUFFER_BIT, GL.GL_NEAREST)

                msSceneFBO!!.unbind(gl)
            } else {
                sceneFBO?.bind(gl)
                sceneFBO?.clear(gl)
                sceneFBO?.setViewport(gl)
                sceneRenderer?.render(gl, world, drawings)
                sceneFBO?.unbind(gl)
            }
            // post processing
            gl.glDisable(GLLightingFunc.GL_LIGHTING)
            gl.glEnable(GL.GL_TEXTURE_2D)
            val output: Texture2D = effectManager.bloom.process(gl, sceneFBO!!.getColorTexture(0))
            gl.glColor4f(1f, 1f, 1f, 1f)

            // render result to window
            screen.apply(gl)
            output.bind(gl)
            EffectManager.renderScreenQuad(gl)
            Texture2D.unbind(gl)
        } else {
            screen.apply(gl)
            sceneRenderer?.render(gl, world, drawings)
        }
    }

    // TODO Info: GLReadBufferUtil.readPixels: pre-exisiting GL error 0x501
    private fun takeScreenshot() {
        val date = Calendar.getInstance().time.toString().replace("[\\s:]+".toRegex(), "_")

        val glp = GLProfile.getDefault()
        val bufferUtil = AWTGLReadBufferUtil(glp, false)
        val screenshot = bufferUtil.readPixelsToBufferedImage(drawable.gl, true)

        val file = File("screenshots/roboviz_$date.png")
        val dir = File("screenshots")
        try {
            if (!dir.exists()) dir.mkdir()
            ImageIO.write(screenshot, "png", file)
        } catch (e: IOException) {
            logger.error("Unable to write screenshot", e)
        }

        screenshotOnRender = false

        logger.info { "Screenshot taken: ${file.absolutePath}" }
    }

    private fun updateRenderingSettings() {
        // dispose EffectManager, SceneRenderer and Buffers
        effectManager.dispose(drawable.gl)
        sceneFBO?.dispose(drawable!!.gl)
        msSceneFBO?.dispose(drawable!!.gl)
        sceneRenderer?.dispose(drawable?.gl)
        sceneFBO = null // needs to be null -> drawScene()
        msSceneFBO = null // needs to be null -> drawScene()
        sceneRenderer = null // needs to be null -> selectRenderer()

        // create new EffectManager and SceneRenderer
        initEffects(drawable.gl)

        // bloom can be enabled at runtime level
        if (Graphics.useBloom && effectManager.bloom == null) {
            effectManager.initBloom(drawable.gl.gL2, getScreen(), Graphics, contentManager)
        }

        renderSettingsChanged = false
    }

    fun onModeChange() {
        // shut down old stuff
        activeScreen.setEnabled(MainWindow.glCanvas, false)
        activeScreen.stop()

        if (netManagerIsInitialized()) {
            netManager.shutdown()
        }

        // dispose and recreate world, else we still have old agents
        world.dispose(drawable?.gl)
        world = WorldModel()
        world.init(drawable?.gl, contentManager, Main.mode)

        // after recreating the world, we need to re-initialize the target tracker camera and the picker in CameraController
        cameraController.initTargetTrackerCamera()
        cameraController.initPicker()

        when (Main.mode) {
            DataTypes.Mode.LIVE -> {
                netManager = NetworkManager()
                netManager.init()
                netManager.server.addChangeListener(world.gameState)
                netManager.server.addChangeListener(MainWindow.instance)
            }
            DataTypes.Mode.LOG -> {
                initLogPlayer()
            }
        }

        activeScreen = if (Main.mode == DataTypes.Mode.LIVE) LiveGameScreen() else LogfileModeScreen()
        activeScreen.setEnabled(MainWindow.glCanvas, true)
    }

}