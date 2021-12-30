package org.magmaoffenburg.roboviz.gui.windows.config

import org.magmaoffenburg.roboviz.Main
import org.magmaoffenburg.roboviz.util.SwingUtils
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.Point
import javax.imageio.ImageIO
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTabbedPane

object ConfigWindow : JFrame() {

    init {
        initializeWindow()
        initializePane()
        this.pack()
    }

    private fun initializeWindow() {
        title = "RoboViz Configuration"
        iconImage = ImageIO.read(ConfigWindow::class.java.getResource("/images/icon.png"))
        isResizable = false
    }

    // add tab and bottom pane
    private fun initializePane() {
        val tabbedPane = JTabbedPane().apply {
            addTab("General", GeneralPanel())
            addTab("Graphics", GraphicsPanel())
            addTab("Window", WindowPanel())
            addTab("Server", ServerPanel())
        }

        val cancelButton = JButton("Cancel")
        cancelButton.addActionListener {
            this.isVisible = false
            this.dispose()
        }
        val saveButton = JButton("Save and Close")
        saveButton.addActionListener {
            Main.config.write() // save to the config file
            Main.config.configChanged()

            this.isVisible = false
            this.dispose()

            // TODO make optional, add option to restart
//            JOptionPane.showMessageDialog(
//                    MainWindow.instance,
//                    "For some changes to apply, you need to restart RoboViz."
//            )
        }

        val bottomPanel = JPanel(GridLayout(1, 2)).apply {
            add(cancelButton)
            add(saveButton)
        }

        this.rootPane.defaultButton = saveButton
        this.add(JPanel(BorderLayout()).apply {
            preferredSize = Dimension(350, 444)
            add(tabbedPane, BorderLayout.CENTER)
            add(bottomPanel, BorderLayout.PAGE_END)
        })
    }

    /**
     * if the windows is visible already, call toFront(), else set visible to true
     */
    fun showWindow(): ConfigWindow = apply {
        if (!isVisible) {
            location = SwingUtils.centerWindowOnScreen(this@ConfigWindow, Point(0,0))
            isVisible = true
        } else {
            toFront()
        }
    }

}