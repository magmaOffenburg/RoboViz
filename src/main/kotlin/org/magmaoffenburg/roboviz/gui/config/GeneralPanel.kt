package org.magmaoffenburg.roboviz.gui.config

import org.magmaoffenburg.roboviz.configuration.Config
import org.magmaoffenburg.roboviz.util.etc.ColorEditor
import org.magmaoffenburg.roboviz.util.etc.ColorRenderer
import java.awt.Color
import java.awt.Dimension
import javax.swing.*
import javax.swing.table.DefaultTableModel

class GeneralPanel: JPanel() {

    init {
        initializePanel()
    }

    private fun initializePanel() {
        val layout = GroupLayout(this).apply {
            autoCreateGaps = true
            autoCreateContainerGaps = true
        }
        this.layout = layout

        val logfilesLabel = JLabel("Logfiles")
        val logfilesSeparator = JSeparator().apply {
            maximumSize = Dimension(0, logfilesLabel.preferredSize.height)
        }
        val recordLogsCb = JCheckBox("Record Logfiles", Config.General.recordLogs)
        val logDirectoryLabel = JLabel("Logfiles Directory:")
        val logDirectoryTF = JTextField(Config.General.logfileDirectory)
        val openDirectoryButton = JButton("...")

        val teamColorsLabel = JLabel("Team Colors")
        val teamColorsSeparator = JSeparator().apply {
            maximumSize = Dimension(0, teamColorsLabel.preferredSize.height)
        }

        val tableModel = DefaultTableModel().apply {
            addColumn("Team Name")
            addColumn("Colors")
        }

        Config.TeamColors.byTeamNames.forEach{ (teamName, color) ->
            tableModel.addRow(arrayOf<Any>(teamName.substringAfter(":").trim(), color))
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
                .addComponent(teamColorTable, GroupLayout.Alignment.CENTER, 0, 150, GroupLayout.PREFERRED_SIZE)
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
    }

}