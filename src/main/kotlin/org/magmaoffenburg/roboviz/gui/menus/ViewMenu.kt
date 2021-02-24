package org.magmaoffenburg.roboviz.gui.menus

import org.magmaoffenburg.roboviz.etc.menuactions.ViewActions
import org.magmaoffenburg.roboviz.rendering.Renderer
import java.awt.event.KeyEvent

class ViewMenu : MenuBase() {

    val actions = ViewActions()

    init {
        initializeMenu()
    }

    private fun initializeMenu() {
        text = "View"

        addItem("Screenshot", KeyEvent.VK_F12) { Renderer.screenshotOnRender = true}
        this.addSeparator()

        addItem("Drawings", KeyEvent.VK_Y) { actions.openDrawingsPanel() }
        addItem("Toggle Full Screen", KeyEvent.VK_F11) { actions.toggleFullScreen() }
        addItem("Toggle Agent Overhead Type", KeyEvent.VK_I) { actions.toggleOverheadType() }
        addItem("Toggle Player Numbers", KeyEvent.VK_N) { actions.togglePlayerNumbers() }
        addItem("Toggle Field Overlay", KeyEvent.VK_F) { actions.toggleFieldOverlay() }
        addItem("Toggle Drawings", KeyEvent.VK_T) { actions.toggleDrawings() }
        addItem("Toggle Fouls", KeyEvent.VK_Q) { actions.toggleFouls() }
    }

}