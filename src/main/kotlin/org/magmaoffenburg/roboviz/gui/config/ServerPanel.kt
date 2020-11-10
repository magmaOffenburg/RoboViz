package org.magmaoffenburg.roboviz.gui.config

import org.magmaoffenburg.roboviz.configuration.Config
import java.awt.Dimension
import javax.swing.*

class ServerPanel: JPanel() {

    init {
        initializePanel()
    }

    private fun initializePanel() {
        val layout = GroupLayout(this).apply {
            autoCreateGaps = true
            autoCreateContainerGaps = true
        }
        this.layout = layout

        val serverLabel = JLabel("Server")
        val serverSeparator = JSeparator().apply {
            maximumSize = Dimension(0, serverLabel.preferredSize.height)
        }
        val autoConnectCB = JCheckBox("Auto-Connect", Config.Networking.autoConnect)
        val autoConnectDelayLabel = JLabel("Auto-Connect Delay:")
        val autoConnectDelaySpinner = JSpinner(SpinnerNumberModel(Config.Networking.autoConnectDelay, 0, Int.MAX_VALUE, 1))
        val defaultServerHostLabel = JLabel("Default Host:")
        val defaultServerHostTF = JTextField(Config.Networking.defaultServerHost)
        val defaultServerPortLabel = JLabel("Default Port:")
        val defaultServerPortSpinner = JSpinner(SpinnerNumberModel(Config.Networking.defaultServerPort, 0, Int.MAX_VALUE, 1))
        val drawingPortLabel = JLabel("Drawing Port:")
        val drawingPortSpinner = JSpinner(SpinnerNumberModel(Config.Networking.listenPort, 0, Int.MAX_VALUE, 1))
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
    }

}