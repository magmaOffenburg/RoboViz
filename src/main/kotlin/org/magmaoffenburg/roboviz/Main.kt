package org.magmaoffenburg.roboviz

import org.magmaoffenburg.roboviz.Main.Companion.config
import org.magmaoffenburg.roboviz.Main.Companion.mode
import org.magmaoffenburg.roboviz.configuration.Config
import org.magmaoffenburg.roboviz.configuration.Config.General
import org.magmaoffenburg.roboviz.etc.LookAndFeelController
import org.magmaoffenburg.roboviz.gui.MainWindow
import org.magmaoffenburg.roboviz.rendering.Renderer
import org.magmaoffenburg.roboviz.util.DataTypes.Mode
import java.awt.EventQueue

class Main {
    companion object {
        const val name = "RoboViz"
        const val version = "1.8.0"

        var mode = Mode.LIVE
        lateinit var config: Config // TODO maybe Config should be a Object
        lateinit var mainWindow: MainWindow
        lateinit var renderer: Renderer

        fun changeMode() {
            println("changing mode ...")
            mode = when(mode) {
                Mode.LIVE -> Mode.LOG
                Mode.LOG -> Mode.LIVE
            }

            // call onModeChange for Render and GUI
            mainWindow.onModeChange()
            renderer.onModeChange()
        }
    }
}

fun main(args: Array<String>) {
    setInitMode(args)

    // parse parameters and config
    config = Config(args) // TODO --logFile, --drawingFilter

    // set Look and Feel
    LookAndFeelController.setLookAndFeel(General.lookAndFeel)

    // create MainWindow
    EventQueue.invokeLater(::createAndShowGUI)
}

private fun createAndShowGUI() {
    Main.mainWindow = MainWindow()
    Main.mainWindow.isVisible = true

    Main.renderer = Renderer() // create the Renderer
}

private fun setInitMode(args: Array<String>) {
    if (args.contains("--logMode")) {
        mode = Mode.LOG
    }
}
