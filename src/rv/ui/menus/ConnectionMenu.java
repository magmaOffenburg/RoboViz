package rv.ui.menus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
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

        for (String host : serverHosts) {
            addHostItem(host, config.getServerPort());
        }

        add(new JSeparator());
        JMenuItem m = new JMenuItem("Connect to ...");
        m.addActionListener((e) -> {
            int port = config.serverPort; // the default port
            String message = "<html>Enter an simspark IP address or host name<br><small>You can also specify a port (e.g. 'localhost:4200', 'example.com:4321')</small></html>";
            String host = JOptionPane.showInputDialog(null, message, "Simspark server", JOptionPane.PLAIN_MESSAGE);
            
            // canceled
            if(host == null) { return; }

            // check if host string contains port info
            if (host.contains(":")) {
                String[] parts = host.split(":");
                
                if(parts.length != 2) {
                    JOptionPane.showMessageDialog(null, String.format("The entered server address ('%s') is invalid", host), "Invalid server address", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                host = parts[0];
                
                // if an exception is thrown, the port part is invalid!
                try {
                    port = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, String.format("The entered port ('%s') is invalid", parts[1]), "Invalid port", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // if an exception is thrown, the host part is invalid!
            try {
                InetAddress.getByName(host);
            } catch (UnknownHostException ex) {
                JOptionPane.showMessageDialog(null, String.format("The entered host ('%s') is invalid", host), "Invalid host", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // add the valid host to the menu and host list
            serverHosts.add(host);
            selectServer(addHostItem(host, port));
        });
        add(m);
    }

    /**
     * Adds a new remote menu item to this menu.
     * 
     * @param host
     * @param port
     * @return 
     */
    private RemoteMenuItem addHostItem(String host, int port)
    {
        final RemoteMenuItem item = new RemoteMenuItem(host, port, getItemCount() == 0);
        item.addActionListener(e -> SwingUtilities.invokeLater(() -> selectServer(item)));
        add(item, 0);
        return item;
    }

    /**
     * Selects the given remote menu item and connect to the corresponding server.
     * 
     * @param item 
     */
    private void selectServer(RemoteMenuItem item)
    {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i) instanceof JRadioButtonMenuItem) {
                ((JRadioButtonMenuItem) getItem(i)).setSelected(false);
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

        public RemoteMenuItem(String host, int port, boolean selected) {
            super(String.format("%s:%d", host, port), selected);
            
            // omit the default port
            if(port == config.serverPort) {
                setText(host);
            }

            this.host = host;
            this.port = port;
        }
    }
}
