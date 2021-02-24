package org.magmaoffenburg.roboviz.gui.menus

import org.magmaoffenburg.roboviz.etc.menuactions.CameraActions
import java.awt.event.KeyEvent

class CameraMenu : MenuBase() {

    private val actions = CameraActions()

    init {
        initializeMenu()
    }

    private fun initializeMenu() {
        text = "Camera"

        addItem("Track Ball", KeyEvent.VK_SPACE, 0) { actions.toggleBallTracker() }
        addItem("Track Player", KeyEvent.VK_SPACE, KeyEvent.SHIFT_DOWN_MASK) { actions.togglePlayerTracker() }
        addItem("First Person Vantage", KeyEvent.VK_V) { actions.setRobotVantageFirst() }
        addItem("Third Person Vantage", KeyEvent.VK_E) { actions.setRobotVantageThird() }

        addSeparator()

        addItem("Select Ball", KeyEvent.VK_0) { actions.selectBall() }
        addItem("Remove Selection", KeyEvent.VK_ESCAPE, 0) { actions.removeSelection() }
        addItem("Select Previous Player", KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK) { actions.cyclePlayers(-1) }
        addItem("Select Next Player", KeyEvent.VK_TAB, 0) { actions.cyclePlayers(1) }
    }

}