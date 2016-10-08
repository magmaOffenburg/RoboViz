package rv.ui.menus;

import javax.swing.JMenuBar;
import rv.Viewer;

public class MenuBar extends JMenuBar {
    public MenuBar(Viewer viewer) {
        if (viewer.getMode() == Viewer.Mode.LIVE)
            add(new ServerMenu(viewer));
    }
}
