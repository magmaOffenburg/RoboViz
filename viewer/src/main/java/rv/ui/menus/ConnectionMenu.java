package rv.ui.menus;

import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import rv.Configuration;
import rv.Viewer;
import rv.util.Pair;

public class ConnectionMenu extends JMenu
{
	private final Viewer viewer;

	private final Configuration.Networking config;

	private final List<Pair<String, Integer>> serverHosts;

	public ConnectionMenu(Viewer viewer)
	{
		super("Connection");
		this.viewer = viewer;
		this.config = viewer.getConfig().networking;

		setMnemonic('C');
		serverHosts = config.servers;

		add(new JSeparator());
		JMenuItem connectTo = new JMenuItem("Connect to...");
		connectTo.addActionListener(e -> {
			JComboBox<String> hostsComboBox = new JComboBox<>();
			for (int i = 0; i < getItemCount(); i++) {
				if (getItem(i) instanceof RemoteMenuItem) {
					RemoteMenuItem item = (RemoteMenuItem) getItem(i);
					hostsComboBox.addItem(item.host + ":" + item.port);
					if (item.isSelected()) {
						hostsComboBox.setSelectedIndex(hostsComboBox.getItemCount() - 1);
					}
				}
			}

			hostsComboBox.setEditable(true);
			SwingUtilities.invokeLater(hostsComboBox::requestFocusInWindow);

			JCheckBox saveCheckBox = new JCheckBox("Add to config file");

			Object[] paneContent = {hostsComboBox, saveCheckBox};
			if (JOptionPane.showConfirmDialog(viewer.getFrame(), paneContent, "Connect to...",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) != JOptionPane.OK_OPTION) {
				return;
			}

			String host = (String) hostsComboBox.getSelectedItem();
			if (host != null) {
				connectTo(host, saveCheckBox.isSelected());
			}
		});
		connectTo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
		add(connectTo);

		for (Pair<String, Integer> server : serverHosts) {
			addHostItem(server.getFirst(), server.getSecond());
		}
		if (config.getOverriddenServerHost() != null && config.getOverriddenServerPort() != null) {
			// Server host overridden, add to list and select the new item
			for (int i = 0; i < getItemCount(); i++) {
				if (getItem(i) instanceof RemoteMenuItem) {
					getItem(i).setSelected(false);
				}
			}
			addHostItem(config.getOverriddenServerHost(), config.getOverriddenServerPort()).setSelected(true);
		}
	}

	private void connectTo(String host, boolean save)
	{
		int port = config.defaultServerPort; // the default port

		// check if host string contains port info
		if (host.contains(":")) {
			String[] parts = host.split(":");

			if (parts.length != 2) {
				JOptionPane.showMessageDialog(viewer.getFrame(),
						String.format("The entered server address ('%s') is invalid", host), "Invalid server address",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			host = parts[0];

			// if an exception is thrown, the port part is invalid!
			try {
				port = Integer.parseInt(parts[1]);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(viewer.getFrame(),
						String.format("The entered port ('%s') is invalid", parts[1]), "Invalid port",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		// if an exception is thrown, the host part is invalid!
		try {
			InetAddress.getByName(host);
		} catch (UnknownHostException ex) {
			JOptionPane.showMessageDialog(viewer.getFrame(), String.format("The entered host ('%s') is invalid", host),
					"Invalid host", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// add the valid host to the menu (and to the configuration if save is true)
		if (save) {
			serverHosts.add(new Pair<>(host, port));
		}
		selectServer(addHostItem(host, port));
	}

	/**
	 * Adds a new remote menu item to this menu, or returns an existing one.
	 */
	private RemoteMenuItem addHostItem(String host, int port)
	{
		for (int i = 0; i < getItemCount(); i++) {
			if (getItem(i) instanceof RemoteMenuItem) {
				RemoteMenuItem item = (RemoteMenuItem) getItem(i);
				if (item.host.equals(host) && item.port == port) {
					return item;
				}
			}
		}
		final RemoteMenuItem item = new RemoteMenuItem(host, port, getItemCount() == 2);
		item.addActionListener(e -> SwingUtilities.invokeLater(() -> selectServer(item)));
		add(item, getItemCount() - 2); // append to the end, but before the separator
		return item;
	}

	/**
	 * Selects the given remote menu item and connect to the corresponding server.
	 */
	private void selectServer(RemoteMenuItem item)
	{
		for (int i = 0; i < getItemCount(); i++) {
			if (getItem(i) instanceof RemoteMenuItem) {
				getItem(i).setSelected(false);
			}
		}
		item.setSelected(true);

		viewer.getDrawings().clearAllShapeSets();
		viewer.getNetManager().getServer().changeConnection(item.host, item.port);
	}

	/**
	 * Class representing a remote menu item, containing the host and port info.
	 */
	private class RemoteMenuItem extends JRadioButtonMenuItem
	{
		public final String host;
		public final int port;

		public RemoteMenuItem(String host, int port, boolean selected)
		{
			super(String.format("%s:%d", host, port), selected);

			// omit the default port
			if (port == config.defaultServerPort) {
				setText(host);
			}

			this.host = host;
			this.port = port;
		}
	}
}
