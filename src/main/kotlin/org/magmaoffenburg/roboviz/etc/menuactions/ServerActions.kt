package org.magmaoffenburg.roboviz.etc.menuactions

import org.magmaoffenburg.roboviz.rendering.Renderer
import rv.comm.rcssserver.ServerComm

class ServerActions {

    fun connect() {
        if (!getServer().isConnected) getServer().connect()
    }

    fun killServer() {
        getServer().killServer()
    }

    fun kickOff(left: Boolean) {
        resetTimeIfExpired()
        getServer().kickOff(left)
    }

    fun freeKick(left: Boolean) {
        resetTimeIfExpired()
        getServer().freeKick(left)
    }

    fun directKick(left: Boolean) {
        resetTimeIfExpired()
        getServer().directFreeKick(left)
    }

    fun resetTime() {
        getServer().resetTime()
    }

    fun requestFullState() {
        getServer().requestFullState()
    }

    fun dropBall() {
        resetTimeIfExpired()
        getServer().dropBall()
    }

    private fun getServer(): ServerComm {
        return Renderer.netManager.server
    }

    private fun resetTimeIfExpired() {
        if (Renderer.world.gameState.halfTime >= Renderer.world.gameState.halfTime * 2) {
            getServer().resetTime()
        }
    }
}