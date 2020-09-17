package config;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import rv.util.Pair;
import rv.util.swing.SwingUtil;

public class ServerListDialog extends JDialog
{
	private List<Pair<String, Integer>> sourceData;

	private DefaultTableModel tableModel;

	private JTable table;

	public ServerListDialog(Frame owner, List<Pair<String, Integer>> data)
	{
		super(owner, "Server List", true);

		// Table model setup
		tableModel = new DefaultTableModel() {
			Class[] types = new Class[] {String.class, Integer.class};

			@Override
			public Class getColumnClass(int columnIndex)
			{
				return types[columnIndex];
			}
		};
		tableModel.addColumn("Host");
		tableModel.addColumn("Port");

		// Copy data to model
		sourceData = data;
		for (Pair<String, Integer> server : sourceData) {
			tableModel.addRow(new Object[] {server.getFirst(), server.getSecond()});
		}

		tableModel.addTableModelListener(e -> {
			if ((e.getType() == TableModelEvent.INSERT || e.getType() == TableModelEvent.UPDATE) &&
					e.getColumn() == 1) {
				// Show a warning if the port is invalid
				String portString = tableModel.getValueAt(e.getFirstRow(), 1).toString();
				try {
					int port = Integer.parseInt(portString);
					if (port < 1 || port > 65535) {
						JOptionPane.showMessageDialog(this, String.format("The entered port ('%d') is invalid", port),
								"Invalid port", JOptionPane.WARNING_MESSAGE);
					}
				} catch (NumberFormatException e2) {
					JOptionPane.showMessageDialog(this, String.format("The entered port ('%s') is invalid", portString),
							"Invalid port", JOptionPane.WARNING_MESSAGE);
				}
			}
		});

		table = new JTable(tableModel);
		table.getColumnModel().getColumn(0).setPreferredWidth(150);
		table.getColumnModel().getColumn(1).setMaxWidth(80);
		table.setDefaultEditor(Integer.class, new DefaultCellEditor(new JTextField()));

		JButton addButton = new JButton("Add");
		addButton.addActionListener(e -> {
			tableModel.addRow(new Object[] {"localhost", 3200});
			table.setRowSelectionInterval(tableModel.getRowCount() - 1, tableModel.getRowCount() - 1);
			pack();
		});
		SwingUtil.setPreferredWidth(addButton, 85);

		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(e -> {
			int selected = table.getSelectedRow();
			if (selected != -1) {
				tableModel.removeRow(selected);
				int newSelection = Math.max(0, selected - 1);
				if (tableModel.getRowCount() > 0) {
					table.setRowSelectionInterval(newSelection, newSelection);
				}
			}
			pack();
		});
		SwingUtil.setPreferredWidth(removeButton, 85);

		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (table.isEditing()) {
					table.getCellEditor().stopCellEditing();
				}

				setVisible(false);

				// Update data
				sourceData.clear();
				for (int i = 0; i < tableModel.getRowCount(); i++) {
					String host = (String) tableModel.getValueAt(i, 0);
					String portString = tableModel.getValueAt(i, 1).toString();
					portString = portString.replaceAll("[^\\d]", "");
					int port;
					try {
						port = Integer.parseInt(portString);
					} catch (NumberFormatException e2) {
						port = 3200;
					}

					sourceData.add(new Pair<>(host, port));
				}

				dispose();
			}
		});
		getRootPane().setDefaultButton(applyButton);

		// Layout

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.EAST;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(10, 10, 10, 10);
		add(table, c);

		Panel addRemoveButtonPanel = new Panel();
		addRemoveButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		addRemoveButtonPanel.add(addButton);
		addRemoveButtonPanel.add(removeButton);
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(0, 0, 0, 0);
		c.gridy = 1;
		add(addRemoveButtonPanel, c);

		c.insets = new Insets(10, 10, 10, 10);
		c.gridy = 3;
		add(applyButton, c);

		setResizable(false);
		pack();
		setLocationRelativeTo(owner);
	}
}
