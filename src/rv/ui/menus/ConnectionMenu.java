package rv.ui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import rv.Configuration;
import rv.Viewer;

public class ConnectionMenu extends JMenu {
    private final Viewer                   viewer;

    private final Configuration.Networking config;

    private final List<String>             serverHosts;

    public ConnectionMenu(Viewer viewer) {
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
            final JRadioButtonMenuItem item = new JRadioButtonMenuItem(host, getItemCount() == 0);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectServer(item);
                }
            });
            add(item);
        }
    }

    private void selectServer(JRadioButtonMenuItem item) {
        int selectedIndex = -1;
        for (int i = 0; i < getItemCount(); i++) {
            JRadioButtonMenuItem currentItem = (JRadioButtonMenuItem) getItem(i);
            currentItem.setSelected(false);
            if (currentItem == item) {
                selectedIndex = i;
            }
        }
        item.setSelected(true);
        String host = serverHosts.get(selectedIndex);
        if (host.equals(config.getServerHost()))
            return;

        config.overrideServerHost(host);
        viewer.getDrawings().clearAllShapeSets();
        viewer.getNetManager().getServer().changeConnection(host, config.serverPort);
    }
}
