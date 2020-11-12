package org.magmaoffenburg.roboviz.gui.dialogs

import org.magmaoffenburg.roboviz.configuration.Config.Networking
import org.magmaoffenburg.roboviz.gui.windows.config.ConfigWindow
import org.magmaoffenburg.roboviz.util.SwingUtils
import java.awt.*
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.table.DefaultTableModel


object ServerListDialog : JDialog() {

    private val tableModel = DefaultTableModel().apply {
        addColumn("Host")
        addColumn("Port")
    }
    private val table = JTable(tableModel).apply {
        columnModel.getColumn(0).preferredWidth = 150
        columnModel.getColumn(1).maxWidth = 80
        setDefaultEditor(Int::class.java, DefaultCellEditor(JTextField()))
    }
    private val addButton = JButton("Add")
    private val removeButton = JButton("Remove")
    private val applyButton = JButton("Apply")
    private val addRemoveBtnPanel = Panel().apply {
        layout = FlowLayout(FlowLayout.RIGHT, 10, 0)
        add(addButton)
        add(removeButton)
    }

    init {
        initializeDialog()
        initializeData()
        initializeLayout()
        initializeActions()
        this.pack()
    }

    private fun initializeDialog() {
        title = "Server List"
        setIconImage(ImageIO.read(ConfigWindow::class.java.getResource("/images/icon.png")))
        isResizable = false
        rootPane.defaultButton = applyButton
    }

    private fun initializeLayout() {
        layout = GridBagLayout()
        val c = GridBagConstraints()

        c.apply {
            fill = GridBagConstraints.BOTH
            anchor = GridBagConstraints.EAST
            gridx = 0
            gridy = 0
            insets = Insets(10, 10, 10, 10)
        }
        add(table, c)

        c.apply {
            fill = GridBagConstraints.NONE
            insets = Insets(0, 0, 0, 0)
            gridy = 1
        }
        add(addRemoveBtnPanel, c)

        c.apply {
            insets = Insets(10, 10, 10, 10)
            gridy = 3
        }
        add(applyButton, c)

    }

    private fun initializeActions() {
        addButton.addActionListener {
            tableModel.addRow(arrayOf<Any>("localhost", 3200))
            pack()
        }
        removeButton.addActionListener {
            val selectedIndex = table.selectedRow
            if (selectedIndex != -1) {
                tableModel.removeRow(selectedIndex)
                pack()
            }
        }
        applyButton.addActionListener {
            if (table.isEditing) table.cellEditor.stopCellEditing()

            Networking.servers.clear()
            for (i in 0 until tableModel.rowCount) {
                val key = tableModel.getValueAt(i, 0).toString()
                val port = when (val portAny = tableModel.getValueAt(i, 1)) {
                    is String -> portAny.toString().replace(Regex("[^\\d]"), "").toInt()
                    is Int -> portAny
                    else -> 3200
                }

                Networking.servers.add(Pair(key, port))
            }

            dispose()
        }
    }

    private fun initializeData() {
        Networking.servers.forEach { (hostname, port) ->
            tableModel.addRow(arrayOf<Any>(hostname, port))
        }
    }

    /**
     * if the dialog is visible already, call toFront(), else set visible to true
     */
    fun showDialog(): ServerListDialog = apply {
        if (!isVisible) {
            location = SwingUtils.centerWindowOnScreen(this@ServerListDialog, Point(0, 0))
            isVisible = true
        } else {
            toFront()
        }
    }
}