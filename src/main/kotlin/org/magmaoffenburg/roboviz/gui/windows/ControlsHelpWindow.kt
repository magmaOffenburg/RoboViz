package org.magmaoffenburg.roboviz.gui.windows

import java.awt.Dimension
import javax.imageio.ImageIO
import javax.swing.JEditorPane
import javax.swing.JFrame
import javax.swing.JScrollPane

/**
 * TODO key listener for close on esc
 */
object ControlsHelpWindow : JFrame() {

    init {
        initializeWindow()
        createTextArea()
    }

    private fun initializeWindow() {
        title = "Help"
        size = Dimension(600, 800)
        iconImage = ImageIO.read(ControlsHelpWindow::class.java.getResource("/images/icon.png"))
        minimumSize = Dimension(400, 500)
    }

    private fun createTextArea() {
        val textArea = JEditorPane()
        textArea.contentType = "text/html"
        textArea.isEditable = false
        textArea.isFocusable = false
        textArea.text = loadText()
        textArea.caretPosition = 0

        add(JScrollPane(textArea))
    }

    private fun loadText(): String {
        return ControlsHelpWindow::class.java.getResource("/help/controls.html").readText()
    }

    fun showWindow(): ControlsHelpWindow = apply {
        if (!isVisible) {
            isVisible = true
        } else {
            toFront()
        }
    }

}