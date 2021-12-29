package org.magmaoffenburg.roboviz.etc.menuactions

import org.magmaoffenburg.roboviz.configuration.Config
import org.magmaoffenburg.roboviz.gui.MainWindow
import org.magmaoffenburg.roboviz.rendering.Renderer
import rv.ui.DrawingListPanel
import rv.ui.screens.LiveGameScreen
import rv.ui.screens.TextOverlay

class ViewActions {

    fun openDrawingsPanel() {
        DrawingListPanel(Renderer.drawings, Config.General.drawingFilter).showFrame(MainWindow.instance)
    }

    fun toggleFullScreen() {
        MainWindow.instance.toggleFullscreen()
    }

    fun toggleOverheadType() {
        Renderer.activeScreen.toggleOverheadType()
    }

    fun togglePlayerNumbers() {
        Renderer.activeScreen.isShowNumPlayers = !Renderer.activeScreen.isShowNumPlayers
    }

    fun toggleFieldOverlay() {
        Renderer.activeScreen.fieldOverlay.isVisible = !Renderer.activeScreen.fieldOverlay.isVisible
    }

    fun toggleDrawings() {
        Renderer.drawings.toggle()
    }

    fun toggleFouls() {
        Renderer.activeScreen.foulListOverlay.isVisible = !Renderer.activeScreen.foulListOverlay.isVisible
        Renderer.activeScreen.textOverlays.add(TextOverlay(
                "Foul Overlay: ${ if (Renderer.activeScreen.foulListOverlay.isVisible) "Enabled" else "Disabled"}",
                Renderer.world,700
        ))
    }

    fun toggleShowServerSpeed() {
        Renderer.activeScreen.toggleShowServerSpeed()
    }

    fun openPlaymodeOverlay() {
        Renderer.world.gameState?.playModes?.let {
            if (Renderer.activeScreen is LiveGameScreen) {
                (Renderer.activeScreen as LiveGameScreen).openPlaymodeOverlay()
            }
        }
    }

}