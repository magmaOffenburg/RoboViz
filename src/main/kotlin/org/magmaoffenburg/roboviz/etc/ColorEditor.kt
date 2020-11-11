package org.magmaoffenburg.roboviz.etc

import java.awt.Color
import java.awt.Component
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.table.TableCellEditor

/**
 * @see https://www.java2s.com/Code/Java/Swing-JFC/
 * Tablewithacustomcellrendererandeditorforthecolordata.htm
 */
class ColorEditor : AbstractCellEditor(), TableCellEditor {
    private var currentColor: Color? = null

    private val colorChooser = JColorChooser()
    private val button: JButton = JButton().apply {
        isBorderPainted = false
    }
    private val dialog: JDialog = JColorChooser.createDialog(
            button, "Pick a Color", true, colorChooser,
            { currentColor = colorChooser.color }, null
    ).apply {
        setIconImage(ImageIO.read(ColorEditor::class.java.getResource("/images/icon.png")))
    }

    init {
        button.addActionListener {
            button.background = currentColor
            colorChooser.color = currentColor
            //SwingUtil.setLocationRelativeTo(dialog, configProg)
            dialog.isVisible = true
            fireEditingStopped()
        }
    }

    override fun getCellEditorValue(): Any {
        return currentColor!!
    }

    override fun getTableCellEditorComponent(table: JTable, value: Any, isSelected: Boolean, row: Int, column: Int): Component {
        currentColor = value as Color
        return button
    }

}