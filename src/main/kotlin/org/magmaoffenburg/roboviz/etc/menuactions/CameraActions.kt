package org.magmaoffenburg.roboviz.etc.menuactions

import org.magmaoffenburg.roboviz.rendering.Renderer
import rv.ui.screens.ViewerScreenBase
import rv.world.Team
import rv.world.objects.Agent


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
        val agent: Agent = Renderer.world.selectedObject as? Agent ?: return
        if (agent.team.agents.size <= 1) return

        var nextAgent: Agent?
        var nextID: Int = agent.id
        do {
            nextID += direction
            if (nextID > Team.MAX_AGENTS) nextID = 0 else if (nextID < 0) nextID = Team.MAX_AGENTS
            nextAgent = agent.team.getAgentByID(nextID) // if id is not found -> null
        } while (nextAgent == null && nextID != agent.id)

        if (nextAgent != null) Renderer.world.selectedObject = nextAgent
    }

}