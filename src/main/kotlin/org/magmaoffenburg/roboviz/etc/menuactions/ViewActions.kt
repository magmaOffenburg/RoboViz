package org.magmaoffenburg.roboviz.etc.menuactions

import org.magmaoffenburg.roboviz.gui.panels.ControlsHelpPanel
import org.magmaoffenburg.roboviz.gui.MainWindow
import org.magmaoffenburg.roboviz.rendering.Renderer
import rv.ui.DrawingListPanel
import rv.ui.screens.TextOverlay

class ViewActions {

    fun openHelp() {
        ControlsHelpPanel().isVisible = true
    }

    fun openDrawingsPanel() {
        DrawingListPanel(Renderer.drawings, "").showFrame(MainWindow.instance) // TODO drawingFilter
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

}