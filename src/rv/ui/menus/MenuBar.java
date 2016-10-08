package rv.ui.menus;

import javax.swing.JMenuBar;
import rv.Viewer;

public class MenuBar extends JMenuBar {
    private Menu viewMenu;

    public MenuBar(Viewer viewer) {
        if (viewer.getMode() == Viewer.Mode.LIVE)
            add(new ServerMenu(viewer));
        viewMenu = new Menu("View", 'V', viewer);
        add(viewMenu);
    }

    public Menu getViewMenu() {
        return viewMenu;
    }
}
