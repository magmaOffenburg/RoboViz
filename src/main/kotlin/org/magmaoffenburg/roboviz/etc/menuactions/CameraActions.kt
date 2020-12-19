package org.magmaoffenburg.roboviz.etc.menuactions

import org.magmaoffenburg.roboviz.rendering.Renderer
import rv.ui.screens.ViewerScreenBase

class CameraActions {

    fun toggleBallTracker() {
        Renderer.activeScreen.toggleBallTracker()
    }

    fun togglePlayerTracker() {
        Renderer.activeScreen.togglePlayerTracker()
    }

    fun setRobotVantageFirst() {
        Renderer.activeScreen.setRobotVantage(ViewerScreenBase.RobotVantageType.FIRST_PERSON)
    }

    fun setRobotVantageThird() {
        Renderer.activeScreen.setRobotVantage(ViewerScreenBase.RobotVantageType.THIRD_PERSON)
    }

    fun selectBall() {
        Renderer.world.selectedObject = Renderer.world.ball
    }
    fun removeSelection() {
        Renderer.world.selectedObject = null
    }

    fun cyclePlayers(direction: Int) {
        println("TODO cyclePlayers $direction")
    }

}