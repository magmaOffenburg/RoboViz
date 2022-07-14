package org.magmaoffenburg.roboviz.gui.windows

import org.magmaoffenburg.roboviz.etc.LookAndFeelController
import org.magmaoffenburg.roboviz.gui.MainWindow
import org.magmaoffenburg.roboviz.rendering.CameraController
import org.magmaoffenburg.roboviz.rendering.Renderer.Companion.logPlayer
import org.magmaoffenburg.roboviz.util.SwingUtils
import rv.comm.rcssserver.LogPlayer
import java.awt.*
import java.awt.image.FilteredImageSource
import javax.imageio.ImageIO
import javax.swing.*

class LogPlayerControlsPanel : JFrame(), LogPlayer.StateChangeListener {

    private lateinit var fileOpenBtn: JButton
    private lateinit var rewindBtn: JButton
    private lateinit var previousFrameBtn: JButton
    private lateinit var playPauseBtn: JButton
    private lateinit var nextFrameBtn: JButton
    private lateinit var previousGoalBtn: JButton
    private lateinit var nextGoalBtn: JButton

    private lateinit var playbackSpeedSpinner: JSpinner
    private lateinit var slider: JSlider
    private var ignoreSliderEvent = false

    private val fileOpenIcon = getIcon("/images/baseline_folder_open_black_24dp.png")
    private val rewindIcon = getIcon("/images/baseline_replay_black_24dp.png")
    private val previousFrameIcon = getIcon("/images/baseline_skip_previous_black_24dp.png")
    private val playIcon = getIcon("/images/baseline_play_arrow_black_24dp.png")
    private val pauseIcon = getIcon("/images/baseline_pause_black_24dp.png")
    private val nextFrameIcon = getIcon("/images/baseline_skip_next_black_24dp.png")
    private val previousGoalIcon = getIcon("/images/baseline_undo_black_24dp.png")
    private val nextGoalIcon = getIcon("/images/baseline_redo_black_24dp.png")

    init {
        initializeWindow()
        createControls()

        pack()
    }

    private fun initializeWindow() {
        title = "Log Player"
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        isResizable = false
        iconImage = ImageIO.read(MainWindow::class.java.getResource("/images/icon.png"))
    }

    private fun createControls() {
        fileOpenBtn = createBtn(fileOpenIcon, "Open logfile...") { fileOpen() }
        rewindBtn = createBtn(rewindIcon, "Rewind") { rewind() }
        previousFrameBtn = createBtn(previousFrameIcon, "Step back") { previousFrame() }
        playPauseBtn = createBtn(pauseIcon, "Pause") { togglePlayPause() }
        nextFrameBtn = createBtn(nextFrameIcon, "Step forward") { nextFrame() }
        previousGoalBtn = createBtn(previousGoalIcon, "Previous goal") { previousGoal() }
        nextGoalBtn = createBtn(nextGoalIcon, "Next goal") { nextGoal() }



        playbackSpeedSpinner = JSpinner(SpinnerNumberModel(1.0, -10.0, 10.0, 0.25)).apply {
            toolTipText = "Playback speed factor"
            preferredSize = Dimension(60, 30)
            addChangeListener { setPlaybackSpeed(this.value as Double) }
        }

        //slider = JSlider(0 ,logPlayer.numFrames, logPlayer.desiredFrame).apply {
        slider = JSlider(0 , 100, 1).apply {
            toolTipText = "Select Frame"
            preferredSize = Dimension(370, 20)
            addChangeListener {
                if (!ignoreSliderEvent) selectFrame(this.value)
            }
        }

        add(JPanel(FlowLayout(FlowLayout.LEADING, 5, 7)).apply {
            preferredSize = Dimension(380, 77)

            add(fileOpenBtn)
            add(rewindBtn)
            add(previousFrameBtn)
            add(playPauseBtn)
            add(nextFrameBtn)
            add(previousGoalBtn)
            add(nextGoalBtn)

            add(Box.createHorizontalStrut(20))

            add(playbackSpeedSpinner)
            add(slider)
        })
    }

    private fun fileOpen() {
        logPlayer.openFileDialog(this)
    }

    private fun rewind() {
        logPlayer.rewind()
    }

    private fun previousFrame() {
        if (logPlayer.isPlaying) logPlayer.stepBackward()
    }

    private fun togglePlayPause() {
        if (logPlayer.isPlaying) {
            logPlayer.pause()
            playPauseBtn.apply {
                setModeDependantIcon(playIcon)
                toolTipText = "Play"
            }
        } else {
            logPlayer.resume()
            playPauseBtn.apply {
                setModeDependantIcon(pauseIcon)
                toolTipText = "Pause"
            }
        }
    }

    private fun nextFrame() {
        if (logPlayer.isPlaying) logPlayer.stepBackward()
    }

    private fun previousGoal() {
        logPlayer.stepBackwardGoal()
    }

    private fun nextGoal() {
        logPlayer.stepForwardGoal()
    }

    private fun setPlaybackSpeed(playbackSpeed: Double) {
        logPlayer.playBackSpeed = playbackSpeed
    }

    private fun selectFrame(frameNr: Int) {
        if (slider.isEnabled) {
            logPlayer.desiredFrame = frameNr
        }
    }

    private fun updateButtons() {
        val isPlaying = logPlayer.isPlaying
        val isValid = logPlayer.isValid

        rewindBtn.isEnabled = isValid
        playPauseBtn.isEnabled = isValid

        previousFrameBtn.isEnabled = isValid && !isPlaying
        nextFrameBtn.isEnabled = isValid && !isPlaying

        previousGoalBtn.isEnabled = isValid && logPlayer.hasPreviousGoal()
        nextGoalBtn.isEnabled = isValid && logPlayer.hasNextGoal()

        playbackSpeedSpinner.isEnabled = isValid
        playbackSpeedSpinner.value = logPlayer.playBackSpeed
    }

    private fun updateSlider() {
        if (slider.maximum < logPlayer.numFrames) slider.maximum = logPlayer.numFrames

        ignoreSliderEvent = true
        if (logPlayer.logfileHasDrawCmds()) {
            SwingUtilities.invokeLater {
                slider.value = logPlayer.desiredFrame
                slider.isEnabled = logPlayer.isValid
                ignoreSliderEvent = false
            }
        } else {
            slider.value = logPlayer.desiredFrame
            slider.isEnabled = logPlayer.isValid
            ignoreSliderEvent = false
        }
    }

    private fun createBtn(icon: ImageIcon, toolTipText: String?, func: () -> Unit): JButton {
        return JButton().apply {
            preferredSize = Dimension(36, 36)
            setModeDependantIcon(icon)
            this.toolTipText = toolTipText
            addActionListener { func() }
        }
    }

    private fun JButton.setModeDependantIcon(i: ImageIcon) {
        if (LookAndFeelController.isDarkMode()) {
            this.icon = i.toDarkMode(true)
            this.disabledIcon = i.toDarkMode(false)
        } else {
            this.icon = i
        }
    }

    private fun ImageIcon.toDarkMode(enabled: Boolean) : ImageIcon {
        val filter = GrayFilter(true, if (enabled) 73 else 36)
        val prod = FilteredImageSource(this.image.source, filter)
        return ImageIcon(Toolkit.getDefaultToolkit().createImage(prod))
    }

    private fun getIcon(path: String): ImageIcon {
        return ImageIcon(LogPlayerControlsPanel::class.java.getResource(path))
    }

    /**
     * if the windows is visible already, call toFront(), else set visible to true
     */
    fun showWindow(): LogPlayerControlsPanel = apply {
        if (!isVisible) {
            location = SwingUtils.centerWindowOnScreen(this@LogPlayerControlsPanel, Point(0,0))
            isVisible = true
        } else {
            toFront()
        }
    }

    override fun playerStateChanged(playing: Boolean) {
        updateButtons()
        updateSlider()

        CameraController.trackerCamera.setPlaybackSpeed(logPlayer.playBackSpeed)
    }

    override fun logfileChanged() {
    }
}
