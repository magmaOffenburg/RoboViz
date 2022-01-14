package org.magmaoffenburg.roboviz.gui.windows.config

import org.magmaoffenburg.roboviz.configuration.Config.General
import org.magmaoffenburg.roboviz.configuration.Config.TeamColors
import org.magmaoffenburg.roboviz.etc.ColorEditor
import org.magmaoffenburg.roboviz.etc.ColorRenderer
import java.awt.Color
import java.awt.Dimension
import java.awt.Image
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.DefaultTableModel
import org.apache.logging.log4j.kotlin.logger

class GeneralPanel: JPanel() {
    private val logger = logger()

    // log files
    private val logfilesLabel = JLabel("Logfiles")
    private val logfilesSeparator = JSeparator().apply {
        maximumSize = Dimension(0, logfilesLabel.preferredSize.height)
    }
    private val recordLogsCb = JCheckBox("Record Logfiles", General.recordLogs)
    private val logDirectoryLabel = JLabel("Logfiles Directory:")
    private val logDirectoryTf = JTextField(General.logfileDirectory)
    private val openDirectoryButton = JButton().apply {
        // resize the image, else the button is way to big
        icon = ImageIcon(
                ImageIcon(GeneralPanel::class.java.getResource("/images/baseline_folder_open_black_24dp.png"))
                .image.getScaledInstance(14,14, Image.SCALE_DEFAULT)
        )
    }

    // team colors
    private val teamColorsLabel = JLabel("Team Colors")
    private val teamColorsSeparator = JSeparator().apply {
        maximumSize = Dimension(0, teamColorsLabel.preferredSize.height)
    }

    private val tableModel = TeamColorsTableModel().apply {
        addColumn("Team Name")
        addColumn("Colors")
    }
    private val teamColorTable = JTable(tableModel).apply {
        setDefaultRenderer(Color::class.java, ColorRenderer())
        setDefaultEditor(Color::class.java, ColorEditor())
        columnModel.getColumn(1).maxWidth = 30
    }
    private val addColorButton = JButton("Add")
    private val removeColorButton = JButton("Remove")

    init {
        TeamColors.byTeamNames.forEach{ (teamName, color) ->
            tableModel.addRow(arrayOf<Any>(teamName.substringAfter(":").trim(), color))
        }

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

                // log files
                .addGroup(layout.createSequentialGroup()
                        .addComponent(logfilesLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(logfilesSeparator, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                )
                .addComponent(recordLogsCb, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                .addGroup(layout.createSequentialGroup()
                        .addComponent(logDirectoryLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(logDirectoryTf, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        .addComponent(openDirectoryButton, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                )

                // team colors
                .addGroup(layout.createSequentialGroup()
                        .addComponent(teamColorsLabel, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(teamColorsSeparator, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                )
                .addComponent(teamColorTable, GroupLayout.Alignment.CENTER, 0, 150, GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(addColorButton, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
                        .addComponent(removeColorButton, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE.toInt())
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
                        .addComponent(logDirectoryTf)
                        .addComponent(openDirectoryButton)
                )

                // team colors
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(teamColorsLabel)
                        .addComponent(teamColorsSeparator)
                )
                .addComponent(teamColorTable)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(addColorButton)
                        .addComponent(removeColorButton)
                )
        )
    }

    /**
     * initialize the actions for all gui elements
     * used by this panel
     */
    private fun initializeActions() {
        // log files
        recordLogsCb.addActionListener {
            General.recordLogs = recordLogsCb.isSelected
        }
        logDirectoryTf.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                General.logfileDirectory = logDirectoryTf.text
                //onChange.run()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                General.logfileDirectory = logDirectoryTf.text
                //onChange.run()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                General.logfileDirectory = logDirectoryTf.text
                //onChange.run()
            }
        })
        openDirectoryButton.addActionListener {
            val fileChooser = JFileChooser(General.logfileDirectory).apply {
                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                isAcceptAllFileFilterUsed = false
            }

            if (fileChooser.showOpenDialog(ConfigWindow) != JFileChooser.CANCEL_OPTION) {
                logDirectoryTf.text = fileChooser.selectedFile.absolutePath
            }
        }

        // team colors
        tableModel.addTableModelListener {
            TeamColors.byTeamNames.clear()
            for (i in 0 until tableModel.rowCount) {
                val key = tableModel.getValueAt(i,0).toString()
                val color = tableModel.getValueAt(i,1) as Color

                TeamColors.byTeamNames[key] = color
            }

            logger.info("New team colors:")
            TeamColors.byTeamNames.forEach {
                logger.info("${it.key} = ${it.value}")
            }
        }
        addColorButton.addActionListener {
            tableModel.addRow(arrayOf("New Team", Color.blue))
            ConfigWindow.pack()
        }
        removeColorButton.addActionListener {
            val selectedIndex = teamColorTable.selectedRow
            if (selectedIndex != -1) {
                tableModel.removeRow(selectedIndex)
                ConfigWindow.pack()
            }
        }
    }

    private class TeamColorsTableModel : DefaultTableModel() {
        override fun getColumnClass(columnIndex: Int): Class<*> {
            return getValueAt(0, columnIndex).javaClass
        }
    }

}