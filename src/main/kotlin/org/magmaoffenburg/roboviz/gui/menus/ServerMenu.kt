package org.magmaoffenburg.roboviz.gui.menus

import org.magmaoffenburg.roboviz.etc.menuactions.ServerActions
import java.awt.event.KeyEvent

class ServerMenu : MenuBase() {

    private val actions = ServerActions()

    init {
        initializeMenu()
    }

    private fun initializeMenu() {
        text = "Server"

        addItem("Connect", KeyEvent.VK_C) { actions.connect() }
        addItem("Kill Server", KeyEvent.VK_X, KeyEvent.SHIFT_DOWN_MASK) { actions.killServer() }
        addItem("Kick Off Left", KeyEvent.VK_K) { actions.kickOff(true) }
        addItem("Kick Off Right", KeyEvent.VK_J) { actions.kickOff(false) }
        addItem("Free Kick Left", KeyEvent.VK_L) { actions.freeKick(true) }
        addItem("Free Kick Right", KeyEvent.VK_R) { actions.freeKick(false) }
        addItem("Direct Free Kick Left", KeyEvent.VK_L, KeyEvent.SHIFT_DOWN_MASK) { actions.directKick(true) }
        addItem("Direct Free Kick Right", KeyEvent.VK_R, KeyEvent.SHIFT_DOWN_MASK) { actions.directKick(false) }
        addItem("Reset Time", KeyEvent.VK_T, KeyEvent.SHIFT_DOWN_MASK){ actions.resetTime() }
        addItem("Request Full State Update", KeyEvent.VK_U){ actions.requestFullState() }
        addItem("Drop Ball", KeyEvent.VK_B){ actions.dropBall() }
    }

}