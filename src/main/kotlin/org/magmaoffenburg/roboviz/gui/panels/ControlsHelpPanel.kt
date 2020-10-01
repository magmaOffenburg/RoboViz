package org.magmaoffenburg.roboviz.gui.panels

import java.awt.Dimension
import javax.swing.JEditorPane
import javax.swing.JFrame
import javax.swing.JScrollPane

/**
 * TODO key listener for close on esc
 */
class ControlsHelpPanel : JFrame() {

    init {
        initializeWindow()
        createTextArea()
    }

    private fun initializeWindow() {
        title = "Help"
        size = Dimension(600, 800)
        minimumSize = Dimension(400, 500)
    }

    private fun createTextArea() {
        val textArea = JEditorPane()
        textArea.contentType = "text/html"
        textArea.isEditable = false
        textArea.isFocusable = false
        textArea.text = loadText()

        add(JScrollPane(textArea))
    }

    private fun loadText(): String {
        return ControlsHelpPanel::class.java.getResource("/help/controls.html").readText()
    }

}