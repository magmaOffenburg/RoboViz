package org.magmaoffenburg.roboviz.gui.menus

import org.magmaoffenburg.roboviz.gui.windows.config.ConfigWindow
import org.magmaoffenburg.roboviz.gui.windows.ControlsHelpWindow
import java.awt.event.KeyEvent

class HelpMenu : MenuBase() {

    init {
        initializeMenu()
    }

    private fun initializeMenu() {
        text = "Help"

        addItem("Help", KeyEvent.VK_F1) { openHelp() }
        addItem("Configuration", KeyEvent.VK_F2) { openConfiguration() }
    }

    private fun openHelp() {
        ControlsHelpWindow.showWindow()
    }

    private fun openConfiguration() {
        ConfigWindow.showWindow()
    }
}