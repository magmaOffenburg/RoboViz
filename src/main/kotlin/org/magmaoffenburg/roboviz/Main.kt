package org.magmaoffenburg.roboviz

import org.magmaoffenburg.roboviz.Main.Companion.config
import org.magmaoffenburg.roboviz.Main.Companion.mode
import org.magmaoffenburg.roboviz.configuration.Config
import org.magmaoffenburg.roboviz.configuration.Config.General
import org.magmaoffenburg.roboviz.etc.LookAndFeelController
import org.magmaoffenburg.roboviz.gui.MainWindow
import org.magmaoffenburg.roboviz.rendering.Renderer
import org.magmaoffenburg.roboviz.util.DataTypes
import java.awt.EventQueue

class Main {
    companion object {
        const val name = "RoboViz"
        const val version = "1.8.0"

        var mode = DataTypes.Mode.LIVE
        lateinit var config: Config // TODO maybe Config should be a Object
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
    val mainWindow = MainWindow()
    mainWindow.isVisible = true

    Renderer() // create the Renderer
}

private fun setInitMode(args: Array<String>) {
    if (args.contains("--logMode")) {
        mode = DataTypes.Mode.LOG
    }
}
