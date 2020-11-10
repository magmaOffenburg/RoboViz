package org.magmaoffenburg.roboviz.gui.config

import org.magmaoffenburg.roboviz.configuration.Config.*
import org.magmaoffenburg.roboviz.util.etc.ColorEditor
import org.magmaoffenburg.roboviz.util.etc.ColorRenderer
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.table.DefaultTableModel


class ConfigWindow : JFrame() {
    init {
        initializeWindow()
        initializePane()
        this.pack()
    }

    private fun initializeWindow() {
        title = "RoboViz Configuration"
        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(400, 600)
        iconImage = ImageIO.read(ConfigWindow::class.java.getResource("/images/icon.png"))
        isResizable = false
    }

    // add tab and bottom pane
    private fun initializePane() {
        val tabbedPane = JTabbedPane()
        tabbedPane.addTab("General", createGeneralPanel())
        tabbedPane.addTab("Graphics", createGraphicsPanel())
        tabbedPane.addTab("Window", createWindowPanel())
        tabbedPane.addTab("Server", createServerPanel())

        this.layout = BorderLayout()
        this.add(tabbedPane, BorderLayout.CENTER)
        this.add(createBottomPanel(), BorderLayout.PAGE_END)
    }

    private fun createBottomPanel(): JPanel {
        val cancelButton = JButton("Cancel")
        val applyButton = JButton("Apply and Close")

        cancelButton.addActionListener {
            this.isVisible = false
            this.dispose()
        }

        this.rootPane.defaultButton = applyButton

        return JPanel(GridLayout(1, 2)).apply {
            add(cancelButton)
            add(applyButton)
        }
    }

    private fun createGeneralPanel(): JPanel {
        val panel = JPanel()

        val layout = GroupLayout(panel).apply {
            autoCreateGaps = true
            autoCreateContainerGaps = true
        }
        panel.layout = layout

        val logfilesLabel = JLabel("Logfiles")
        val logfilesSeparator = JSeparator().apply {
            maximumSize = Dimension(0, logfilesLabel.preferredSize.height)
        }
        val recordLogsCb = JCheckBox("Record Logfiles", General.recordLogs)
        val logDirectoryLabel = JLabel("Logfiles Directory:")
        val logDirectoryTF = JTextField(General.logfileDirectory)
        val openDirectoryButton = JButton("...")

        val teamColorsLabel = JLabel("Team Colors")
        val teamColorsSeparator = JSeparator().apply {
            maximumSize = Dimension(0, teamColorsLabel.preferredSize.height)
        }

        val tableModel = DefaultTableModel().apply {
            addColumn("Team Name")
            addColumn("Colors")
        }

        TeamColors.byTeamNames.forEach{ (teamName, color) ->
            tableModel.addRow(arrayOf<Any>(teamName, color))
        }

        val teamColorTable = JTable(tableModel).apply {
            setDefaultRenderer(Color::class.java, ColorRenderer())
            setDefaultEditor(Color::class.java, ColorEditor())
            columnModel.getColumn(1).maxWidth = 30
        }
        val addButton = JButton("Add")
        val removeButton = JButton("Remove")

        layout.setHorizontalGroup(layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)

                // log files
                .addGroup(layout.createSequentialGroup()
                        .addComponent(logfilesLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(logfilesSeparator, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                )
                .addComponent(recordLogsCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addGroup(layout.createSequentialGroup()
                        .addComponent(logDirectoryLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(logDirectoryTF, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        .addComponent(openDirectoryButton, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                )

                // team colors
                .addGroup(layout.createSequentialGroup()
                        .addComponent(teamColorsLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(teamColorsSeparator, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                )
                .addComponent(teamColorTable, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        .addComponent(removeButton, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                )
        )

        layout.setVerticalGroup(layout.createSequentialGroup()
                // log files
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(logfilesLabel)
                        .addComponent(logfilesSeparator)
                )
                .addComponent(recordLogsCb)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(logDirectoryLabel)
                        .addComponent(logDirectoryTF)
                        .addComponent(openDirectoryButton)
                )

                // team colors
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(teamColorsLabel)
                        .addComponent(teamColorsSeparator)
                )
                .addComponent(teamColorTable)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(addButton)
                        .addComponent(removeButton)
                )
        )

        return panel
    }

    private fun createGraphicsPanel(): JPanel {
        val panel = JPanel()

        val layout = GroupLayout(panel)
        panel.layout = layout

        layout.autoCreateGaps = true
        layout.autoCreateContainerGaps = true

        val space = Box.createHorizontalStrut(10)

        // lighting
        val lightingLabel = JLabel("Lighting")
        val lightingSeparator = JSeparator().apply {
            maximumSize = Dimension(0, lightingLabel.preferredSize.height)
        }
        val bloomCb = JCheckBox("Bloom", Graphics.useBloom)
        val phongCb = JCheckBox("Phong", Graphics.usePhong)
        val shadowsCb = JCheckBox("Shadows", Graphics.useShadows)
        val softShadowsCb = JCheckBox("Soft Shadows", Graphics.useSoftShadows)
        val shadowResLabel = JLabel("Shadow Resolution:", SwingConstants.RIGHT)
        val shadowResSpinner = JSpinner(SpinnerNumberModel(Graphics.shadowResolution, 1, Int.MAX_VALUE, 1))

        // fsaa
        val fsaaLabel = JLabel("Anti-Aliasing")
        val fsaaSeparator = JSeparator().apply {
            maximumSize = Dimension(0, fsaaLabel.preferredSize.height)
        }
        val fsaaCB = JCheckBox("Enabled", Graphics.useFsaa)
        val samplesLabel = JLabel("Samples:", SwingConstants.RIGHT)
        val samplesSpinner = JSpinner(SpinnerNumberModel(Graphics.fsaaSamples, 1, Int.MAX_VALUE, 1))

        // general
        val generalLabel = JLabel("General Graphics")
        val generalSeparator = JSeparator().apply {
            maximumSize = Dimension(0, generalLabel.preferredSize.height)
        }
        val stereoCb = JCheckBox("Stereo 3D", Graphics.useStereo)
        val vsyncCb = JCheckBox("V-Sync", Graphics.useVsync)
        val fpsLabel = JLabel("FPS:")
        val fpsSpinner = JSpinner(SpinnerNumberModel(Graphics.targetFPS, 1, Int.MAX_VALUE, 1))
        val fpFovLabel = JLabel("First Person FOV:")
        val fpFovSpinner = JSpinner(SpinnerNumberModel(Graphics.firstPersonFOV, 1, Int.MAX_VALUE, 1))
        val tpFovLabel = JLabel("Third Person FOV:")
        val tpFovSpinner = JSpinner(SpinnerNumberModel(Graphics.thirdPersonFOV, 1, Int.MAX_VALUE, 1))

        layout.setHorizontalGroup(layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)

                // lighting
                .addGroup(layout.createSequentialGroup()
                        .addComponent(lightingLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(lightingSeparator, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                )
                .addComponent(bloomCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addComponent(phongCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addComponent(shadowsCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addGroup(layout.createSequentialGroup()
                        .addComponent(softShadowsCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        .addComponent(shadowResLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        .addComponent(shadowResSpinner, 0, 90, 90)
                )
                .addComponent(space, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())

                // fsaa
                .addGroup(layout.createSequentialGroup()
                        .addComponent(fsaaLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(fsaaSeparator, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(fsaaCB, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        .addComponent(samplesLabel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        .addComponent(samplesSpinner, 0, 90, 90)
                )
                .addComponent(space, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())

                // general
                .addGroup(layout.createSequentialGroup()
                        .addComponent(generalLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(generalSeparator, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                )
                .addComponent(stereoCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addComponent(vsyncCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addGroup(layout.createSequentialGroup()
                        .addComponent(fpsLabel, 0, 120, 120)
                        .addComponent(fpsSpinner, 0, 90, 90)
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(fpFovLabel, 0, 120, 120)
                        .addComponent(fpFovSpinner, 0, 90, 90)
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(tpFovLabel, 0, 120, 120)
                        .addComponent(tpFovSpinner, 0, 90, 90)
                )
        )

        layout.setVerticalGroup(layout.createSequentialGroup()
                // lighting
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lightingLabel)
                        .addComponent(lightingSeparator)
                )
                .addComponent(bloomCb)
                .addComponent(phongCb)
                .addComponent(shadowsCb)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(softShadowsCb)
                        .addComponent(shadowResLabel)
                        .addComponent(shadowResSpinner)
                )
                .addComponent(space)

                // fsaa
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fsaaLabel)
                        .addComponent(fsaaSeparator)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fsaaCB)
                        .addComponent(samplesLabel)
                        .addComponent(samplesSpinner)
                )
                .addComponent(space)

                // general
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(generalLabel)
                        .addComponent(generalSeparator)
                )
                .addComponent(stereoCb)
                .addComponent(vsyncCb)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fpsLabel)
                        .addComponent(fpsSpinner)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fpFovLabel)
                        .addComponent(fpFovSpinner)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(tpFovLabel)
                        .addComponent(tpFovSpinner)
                )
        )

        return panel
    }

    private fun createWindowPanel(): JPanel {
        val panel = JPanel()

        val layout = GroupLayout(panel).apply {
            autoCreateGaps = true
            autoCreateContainerGaps = true
        }
        panel.layout = layout

        val frameLabel = JLabel("Frame")
        val frameSeparator = JSeparator().apply {
            maximumSize = Dimension(0, frameLabel.preferredSize.height)
        }
        val fxLabel = JLabel("X:")
        val fxSpinner = JSpinner(SpinnerNumberModel(Graphics.frameX, -100, 10000, 1))
        val fyLabel = JLabel("Y:")
        val fySpinner = JSpinner(SpinnerNumberModel(Graphics.frameY, -100, 10000, 1))
        val fwLabel = JLabel("Width:")
        val fwSpinner = JSpinner(SpinnerNumberModel(Graphics.frameWidth, 1, 10000, 1))
        val fhLabel = JLabel("Height:")
        val fhSpinner = JSpinner(SpinnerNumberModel(Graphics.frameHeight, 1, 10000, 1))
        val centerCb = JCheckBox("Center Position", Graphics.centerFrame)
        val saveStateCb = JCheckBox("Save Frame State", Graphics.saveFrameState)
        val maximizedCb = JCheckBox("Maximized", Graphics.isMaximized)

        layout.setHorizontalGroup(layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(frameLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(frameSeparator, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(fxLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(fxSpinner, 0, 90, 90)
                        .addComponent(fwLabel, 0, 45, 45)
                        .addComponent(fwSpinner, 0, 90, 90)
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(fyLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(fySpinner, 0, 90, 90)
                        .addComponent(fhLabel, 0, 45, 45)
                        .addComponent(fhSpinner, 0, 90, 90)
                )
                .addComponent(centerCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addComponent(saveStateCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addComponent(maximizedCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
        )

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(frameLabel)
                        .addComponent(frameSeparator)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fxLabel)
                        .addComponent(fxSpinner)
                        .addComponent(fwLabel)
                        .addComponent(fwSpinner)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fyLabel)
                        .addComponent(fySpinner)
                        .addComponent(fhLabel)
                        .addComponent(fhSpinner)
                )
                .addComponent(centerCb)
                .addComponent(saveStateCb)
                .addComponent(maximizedCb)
        )

        return panel
    }

    private fun createServerPanel(): JPanel {
        val panel = JPanel()

        val layout = GroupLayout(panel).apply {
            autoCreateGaps = true
            autoCreateContainerGaps = true
        }
        panel.layout = layout

        val serverLabel = JLabel("Server")
        val serverSeparator = JSeparator().apply {
            maximumSize = Dimension(0, serverLabel.preferredSize.height)
        }
        val autoConnectCB = JCheckBox("Auto-Connect", Networking.autoConnect)
        val autoConnectDelayLabel = JLabel("Auto-Connect Delay:")
        val autoConnectDelaySpinner = JSpinner(SpinnerNumberModel(Networking.autoConnectDelay, 0, Int.MAX_VALUE, 1))
        val defaultServerHostLabel = JLabel("Default Host:")
        val defaultServerHostTF = JTextField(Networking.defaultServerHost)
        val defaultServerPortLabel = JLabel("Default Port:")
        val defaultServerPortSpinner = JSpinner(SpinnerNumberModel(Networking.defaultServerPort, 0, Int.MAX_VALUE, 1))
        val drawingPortLabel = JLabel("Drawing Port:")
        val drawingPortSpinner = JSpinner(SpinnerNumberModel(Networking.listenPort, 0, Int.MAX_VALUE, 1))
        val serverListButton = JButton("Open Server List")

        layout.setHorizontalGroup(layout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(serverLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(serverSeparator, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                )
                .addComponent(autoConnectCB, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addGroup(layout.createSequentialGroup()
                        .addComponent(autoConnectDelayLabel, 0, 125, 125)
                        .addComponent(autoConnectDelaySpinner, 0, 120, 120)
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(defaultServerHostLabel, 0, 125, 125)
                        .addComponent(defaultServerHostTF, 0, 120, 120)
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(defaultServerPortLabel, 0, 125, 125)
                        .addComponent(defaultServerPortSpinner, 0, 120, 120)
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(drawingPortLabel, 0, 125, 125)
                        .addComponent(drawingPortSpinner, 0, 120, 120)
                )
                .addComponent(serverListButton, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
        )

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(serverLabel)
                        .addComponent(serverSeparator)
                )
                .addComponent(autoConnectCB)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(autoConnectDelayLabel)
                        .addComponent(autoConnectDelaySpinner)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(defaultServerHostLabel)
                        .addComponent(defaultServerHostTF)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(defaultServerPortLabel)
                        .addComponent(defaultServerPortSpinner)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(drawingPortLabel)
                        .addComponent(drawingPortSpinner)
                )
                .addComponent(serverListButton)
        )

        return panel
    }

}