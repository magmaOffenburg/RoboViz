package org.magmaoffenburg.roboviz.gui.menus

import java.awt.event.KeyEvent
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.KeyStroke

open class MenuBase: JMenu() {

    fun addItem(text: String, keyCode: Int, func: () -> Unit) {
        val item = JMenuItem(text)
        item.accelerator = KeyStroke.getKeyStroke(KeyEvent.getKeyText(keyCode))
        item.addActionListener { func() }

        add(item)
    }

    fun addItem(text: String, keyCode: Int, modifiers: Int, func: () -> Unit) {
        val item = JMenuItem(text)
        item.accelerator = KeyStroke.getKeyStroke(keyCode, modifiers)
        item.addActionListener { func() }

        add(item)
    }

}