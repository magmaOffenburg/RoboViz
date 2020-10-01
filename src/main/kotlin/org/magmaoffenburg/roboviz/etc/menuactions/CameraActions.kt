package org.magmaoffenburg.roboviz.etc.menuactions

import org.magmaoffenburg.roboviz.rendering.Renderer

class CameraActions {

    fun toggleBallTracker() {
        Renderer.activeScreen.toggleBallTracker()
    }

    fun togglePlayerTracker() {
        Renderer.activeScreen.togglePlayerTracker()
    }

    fun setRobotVantageFirst() {
        println("TODO setRobotVantageFirst")
    }

    fun setRobotVantageThird() {
        println("TODO setRobotVantageThird")
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