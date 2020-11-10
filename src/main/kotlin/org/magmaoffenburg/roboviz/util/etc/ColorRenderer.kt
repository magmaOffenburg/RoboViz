package org.magmaoffenburg.roboviz.util.etc

import java.awt.Color
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.border.Border
import javax.swing.table.TableCellRenderer

/**
 * @see http ://www.java2s.com/Code/Java/Swing-JFC/
 * Tablewithacustomcellrendererandeditorforthecolordata .htm
 */
class ColorRenderer : JLabel(), TableCellRenderer {
    var unselectedBorder: Border? = null
    var selectedBorder: Border? = null
    override fun getTableCellRendererComponent(
            table: JTable, color: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
        val newColor = color as Color
        background = newColor
        if (isSelected) {
            if (selectedBorder == null) {
                selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.selectionBackground)
            }
            border = selectedBorder
        } else {
            if (unselectedBorder == null) {
                unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.background)
            }
            border = unselectedBorder
        }
        toolTipText = "RGB value: " + newColor.red + ", " + newColor.green + ", " + newColor.blue
        return this
    }

    init {
        isOpaque = true
    }
}