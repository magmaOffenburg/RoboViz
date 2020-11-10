package org.magmaoffenburg.roboviz.util.etc

import java.awt.Color
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.table.TableCellEditor


/**
 * @see http ://www.java2s.com/Code/Java/Swing-JFC/
 * Tablewithacustomcellrendererandeditorforthecolordata .htm
 */
class ColorEditor : AbstractCellEditor(), TableCellEditor, ActionListener {
    var currentColor: Color? = null
    var button: JButton
    var colorChooser: JColorChooser
    var dialog: JDialog
    override fun actionPerformed(e: ActionEvent) {
        if (EDIT == e.actionCommand) {
            button.background = currentColor
            colorChooser.color = currentColor
            //SwingUtil.setLocationRelativeTo(dialog, configProg)
            dialog.isVisible = true
            fireEditingStopped()
        } else {
            currentColor = colorChooser.color
        }
    }

    override fun getCellEditorValue(): Any {
        return currentColor!!
    }

    override fun getTableCellEditorComponent(
            table: JTable, value: Any, isSelected: Boolean, row: Int, column: Int): Component {
        currentColor = value as Color
        return button
    }

    companion object {
        protected const val EDIT = "edit"
    }

    init {
        button = JButton()
        button.actionCommand = EDIT
        button.addActionListener(this)
        button.isBorderPainted = false
        colorChooser = JColorChooser()
        dialog = JColorChooser.createDialog(button, "Pick a Color", true, colorChooser, this, null)
        dialog.setIconImage(ImageIO.read(ColorEditor::class.java.getResource("/images/icon.png")))
    }
}