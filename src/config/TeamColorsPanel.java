package config;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import rv.Configuration;
import rv.Globals;
import rv.util.SwingUtil;
import config.RVConfigure.SaveListener;

public class TeamColorsPanel extends JPanel implements SaveListener {

    final RVConfigure              configProg;
    final Configuration.TeamColors config;

    JButton                        removeButton;
    JButton                        addButton;
    JTable                         teamColorTable;
    DefaultTableModel              tableModel;

    public TeamColorsPanel(RVConfigure configProg) {
        super();
        this.configProg = configProg;
        config = configProg.config.teamColors;
        initGUI();
        configProg.listeners.add(this);
    }

    void initGUI() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;

        add(initPanel(), c);
    }

    JPanel initPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Team Colors"));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 10;

        addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.addRow(new Object[] { "New Team", Color.blue });
                configProg.pack();
                selectLastRow();
            }
        });
        SwingUtil.setPreferredWidth(addButton, 80);

        removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selected = teamColorTable.getSelectedRow();
                if (selected != -1) {
                    tableModel.removeRow(selected);
                    int newSelection = Math.max(0, selected - 1);
                    if (tableModel.getRowCount() > 0)
                        teamColorTable.setRowSelectionInterval(newSelection, newSelection);
                    configProg.pack();
                }
            }
        });
        SwingUtil.setPreferredWidth(removeButton, 80);

        tableModel = new TeamColorsTableModel();
        tableModel.addColumn("Team Name");
        tableModel.addColumn("Colors");

        int i = 0;
        for (String teamName : config.colorByTeamName.keySet()) {
            float[] color = config.colorByTeamName.get(teamName);
            tableModel.addRow(new Object[] { teamName, SwingUtil.toColor(color) });
            i++;
        }

        teamColorTable = new JTable(tableModel);
        teamColorTable.setDefaultRenderer(Color.class, new ColorRenderer());
        teamColorTable.setDefaultEditor(Color.class, new ColorEditor());
        teamColorTable.getColumnModel().getColumn(1).setMaxWidth(30);
        teamColorTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                removeButton.setEnabled(teamColorTable.getSelectedRow() != -1);
            }
        });

        selectLastRow();

        c.gridwidth = 2;
        panel.add(teamColorTable, c);

        c.gridwidth = 1;
        c.fill = GridBagConstraints.EAST;
        c.gridy += 2;
        panel.add(addButton, c);
        panel.add(removeButton, c);

        return panel;
    }

    void selectLastRow() {
        if (tableModel.getRowCount() > 0)
            teamColorTable.setRowSelectionInterval(tableModel.getRowCount() - 1,
                    tableModel.getRowCount() - 1);
    }

    @Override
    public void configSaved(RVConfigure configProg) {
        config.colorByTeamName.clear();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String teamName = (String) tableModel.getValueAt(i, 0);
            Color color = (Color) tableModel.getValueAt(i, 1);
            config.colorByTeamName.put(teamName, color.getRGBComponents(null));
        }
    }

    private class TeamColorsTableModel extends DefaultTableModel {
        @Override
        public Class getColumnClass(int columnIndex) {
            return getValueAt(0, columnIndex).getClass();
        }
    }

    /**
     * @see http 
     *      ://www.java2s.com/Code/Java/Swing-JFC/Tablewithacustomcellrendererandeditorforthecolordata
     *      .htm
     */
    private class ColorRenderer extends JLabel implements TableCellRenderer {

        Border unselectedBorder = null;
        Border selectedBorder   = null;

        public ColorRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object color,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Color newColor = (Color) color;
            setBackground(newColor);
            if (isSelected) {
                if (selectedBorder == null) {
                    selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                            table.getSelectionBackground());
                }
                setBorder(selectedBorder);
            } else {
                if (unselectedBorder == null) {
                    unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                            table.getBackground());
                }
                setBorder(unselectedBorder);
            }

            setToolTipText("RGB value: " + newColor.getRed() + ", " + newColor.getGreen() + ", "
                    + newColor.getBlue());
            return this;
        }
    }

    /**
     * @see http 
     *      ://www.java2s.com/Code/Java/Swing-JFC/Tablewithacustomcellrendererandeditorforthecolordata
     *      .htm
     */
    private class ColorEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

        Color                         currentColor;
        JButton                       button;
        JColorChooser                 colorChooser;
        JDialog                       dialog;
        protected static final String EDIT = "edit";

        public ColorEditor() {
            button = new JButton();
            button.setActionCommand(EDIT);
            button.addActionListener(this);
            button.setBorderPainted(false);

            colorChooser = new JColorChooser();
            dialog = JColorChooser.createDialog(button, "Pick a Color", true, colorChooser, this,
                    null);
            dialog.setIconImage(Globals.getIcon());
        }

        public void actionPerformed(ActionEvent e) {
            if (EDIT.equals(e.getActionCommand())) {
                button.setBackground(currentColor);
                colorChooser.setColor(currentColor);
                dialog.setLocationRelativeTo(configProg);
                dialog.setVisible(true);

                fireEditingStopped();

            } else {
                currentColor = colorChooser.getColor();
            }
        }

        public Object getCellEditorValue() {
            return currentColor;
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentColor = (Color) value;
            return button;
        }
    }
}
