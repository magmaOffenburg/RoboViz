package org.magmaoffenburg.roboviz.gui.windows.config

import org.magmaoffenburg.roboviz.configuration.Config.Networking
import org.magmaoffenburg.roboviz.gui.dialogs.ServerListDialog
import java.awt.Dimension
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ServerPanel: JPanel() {

    private val serverLabel = JLabel("Server")
    private val serverSeparator = JSeparator().apply {
        maximumSize = Dimension(0, serverLabel.preferredSize.height)
    }
    private val autoConnectCB = JCheckBox("Auto-Connect", Networking.autoConnect)
    private val autoConnectDelayLabel = JLabel("Auto-Connect Delay:")
    private val autoConnectDelaySpinner = JSpinner(SpinnerNumberModel(Networking.autoConnectDelay, 0, Int.MAX_VALUE, 1))
    private val defaultServerHostLabel = JLabel("Default Host:")
    private val defaultServerHostTf = JTextField(Networking.defaultServerHost)
    private val defaultServerPortLabel = JLabel("Default Port:")
    private val defaultServerPortSpinner = JSpinner(SpinnerNumberModel(Networking.defaultServerPort, 0, Int.MAX_VALUE, 1))
    private val drawingPortLabel = JLabel("Drawing Port:")
    private val drawingPortSpinner = JSpinner(SpinnerNumberModel(Networking.listenPort, 0, Int.MAX_VALUE, 1))
    private val monitorStepLabel = JLabel("Monitor step duration:")
    private val monitorStepSpinner = JSpinner(SpinnerNumberModel(Networking.monitorStep, 0.0, Double.MAX_VALUE, 0.01))
    private val useBufferCB = JCheckBox("Use Buffer", Networking.useBuffer)
    private val serverListButton = JButton("Open Server List")

    init {
        initializeLayout()
        initializeActions()
    }

    /**
     * initialize the panels layout
     */
    private fun initializeLayout() {
        val layout = GroupLayout(this).apply {
            autoCreateGaps = true
            autoCreateContainerGaps = true
        }
        this.layout = layout

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
                        .addComponent(defaultServerHostTf, 0, 120, 120)
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(defaultServerPortLabel, 0, 125, 125)
                        .addComponent(defaultServerPortSpinner, 0, 120, 120)
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(drawingPortLabel, 0, 125, 125)
                        .addComponent(drawingPortSpinner, 0, 120, 120)
                )
                .addGroup(layout.createSequentialGroup()
                    .addComponent(monitorStepLabel, 0, 125, 125)
                    .addComponent(monitorStepSpinner, 0, 120, 120)
                )
                .addComponent(useBufferCB, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
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
                        .addComponent(defaultServerHostTf)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(defaultServerPortLabel)
                        .addComponent(defaultServerPortSpinner)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(drawingPortLabel)
                        .addComponent(drawingPortSpinner)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(monitorStepLabel)
                    .addComponent(monitorStepSpinner)
                )
                .addComponent(useBufferCB)
                .addComponent(serverListButton)
        )
    }

    /**
     * initialize the actions for all gui elements
     * used by this panel
     */
    private fun initializeActions() {
        autoConnectCB.addActionListener {
            Networking.autoConnect = autoConnectCB.isSelected
        }
        autoConnectDelaySpinner.addChangeListener {
            Networking.autoConnectDelay = autoConnectDelaySpinner.value as Int
        }
        defaultServerHostTf.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                Networking.defaultServerHost = defaultServerHostTf.text
            }

            override fun removeUpdate(e: DocumentEvent?) {
                Networking.defaultServerHost = defaultServerHostTf.text
            }

            override fun changedUpdate(e: DocumentEvent?) {
                Networking.defaultServerHost = defaultServerHostTf.text
            }
        })
        defaultServerPortSpinner.addChangeListener {
            Networking.defaultServerPort = defaultServerPortSpinner.value as Int
        }
        drawingPortSpinner.addChangeListener {
            Networking.listenPort = drawingPortSpinner.value as Int
        }
        serverListButton.addActionListener {
            ServerListDialog.showDialog()
        }
        monitorStepSpinner.addChangeListener {
            Networking.monitorStep = monitorStepSpinner.value as Double
        }
        useBufferCB.addActionListener {
            Networking.useBuffer = useBufferCB.isSelected
        }
    }

}