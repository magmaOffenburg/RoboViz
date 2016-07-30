package rv.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import rv.Configuration;
import rv.Viewer;

public class MenuBar extends JMenuBar {
    private final Viewer                   viewer;

    private final JMenu                    server;

    private final Configuration.Networking config;

    private final List<String>             serverHosts;

    public MenuBar(Viewer viewer) {
        this.viewer = viewer;
        server = new JMenu("Server");
        config = viewer.getConfig().networking;
        serverHosts = new ArrayList<>(config.serverHosts);

        String overriddenHost = config.overriddenServerHost;
        if (overriddenHost != null) {
            serverHosts.remove(overriddenHost);
            serverHosts.add(0, overriddenHost);
        }

        for (String host : serverHosts) {
            final JRadioButtonMenuItem item = new JRadioButtonMenuItem(host,
                    server.getItemCount() == 0);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectServer(item);
                }
            });
            server.add(item);
        }

        add(server);
    }

    private void selectServer(JRadioButtonMenuItem item) {
        int selectedIndex = -1;
        for (int i = 0; i < server.getItemCount(); i++) {
            JRadioButtonMenuItem currentItem = (JRadioButtonMenuItem) server.getItem(i);
            currentItem.setSelected(false);
            if (currentItem == item) {
                selectedIndex = i;
            }
        }
        item.setSelected(true);
        String host = serverHosts.get(selectedIndex);
        config.overrideServerHost(host);
        viewer.getDrawings().clearAllShapeSets();
        viewer.getNetManager().getServer().changeConnection(host, config.serverPort);
    }
}
