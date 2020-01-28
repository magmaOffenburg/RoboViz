package rv.ui.menus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import rv.Configuration;
import rv.Viewer;

public class ConnectionMenu extends JMenu
{
	private final Viewer viewer;

	private final Configuration.Networking config;

	private final List<String> serverHosts;

	public ConnectionMenu(Viewer viewer)
	{
		super("Connection");
		this.viewer = viewer;
		this.config = viewer.getConfig().networking;

		setMnemonic('C');
		serverHosts = new ArrayList<>(config.serverHosts);

		String overriddenHost = config.overriddenServerHost;
		if (overriddenHost != null) {
			serverHosts.remove(overriddenHost);
			serverHosts.add(0, overriddenHost);
		}

		add(new JSeparator());
		JMenuItem connectTo = new JMenuItem("Connect to...");
		connectTo.addActionListener(e -> {
			JComboBox<String> hostsComboBox = new JComboBox<>();
			for (String host : config.serverHosts) {
				hostsComboBox.addItem(host + ":" + config.serverPort);
			}
			hostsComboBox.setEditable(true);
			hostsComboBox.setSelectedIndex(config.serverHosts.indexOf(config.getServerHost()));
			SwingUtilities.invokeLater(hostsComboBox::requestFocusInWindow);

			if (JOptionPane.showConfirmDialog(viewer.getFrame(), hostsComboBox, "Connect to...",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) != JOptionPane.OK_OPTION) {
				return;
			}

			String host = (String) hostsComboBox.getSelectedItem();
			if (host != null) {
				connectTo(host);
			}
		});
		add(connectTo);

		for (String host : serverHosts) {
			addHostItem(host, config.getServerPort());
		}
	}

	private void connectTo(String host)
	{
		int port = config.serverPort; // the default port

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

		// add the valid host to the menu and host list
		serverHosts.add(host);
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

		if (item.host.equals(config.getServerHost()) && item.port == config.getServerPort()) {
			return;
		}

		config.overrideServerHost(item.host);
		config.overrideServerPort(item.port);
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
			if (port == config.serverPort) {
				setText(host);
			}

			this.host = host;
			this.port = port;
		}
	}
}
