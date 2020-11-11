package org.magmaoffenburg.roboviz.gui.config

import org.magmaoffenburg.roboviz.Main
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import javax.imageio.ImageIO
import javax.swing.*

object ConfigWindow : JFrame() {

    init {
        initializeWindow()
        initializePane()
        this.pack()
    }

    private fun initializeWindow() {
        title = "RoboViz Configuration"
        size = Dimension(350, 470)
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
        val applyButton = JButton("Save and Close")
        applyButton.addActionListener {
            Main.config.write() // save to the config file
            println("write")
            this.isVisible = false
            this.dispose()
        }

        val bottomPanel = JPanel(GridLayout(1, 2)).apply {
            add(cancelButton)
            add(applyButton)
        }

        this.rootPane.defaultButton = applyButton
        this.layout = BorderLayout()
        this.add(tabbedPane, BorderLayout.CENTER)
        this.add(bottomPanel, BorderLayout.PAGE_END)
    }

    /**
     * if the windows is visible already, call toFront(), else set visible to true
     */
    fun showWindow(): ConfigWindow = apply {
        if (!isVisible) {
            isVisible = true
        } else {
            toFront()
        }
    }

}