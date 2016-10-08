package rv.ui.menus;

import javax.swing.JMenuBar;
import rv.Viewer;

public class MenuBar extends JMenuBar {
    private Menu viewMenu;
    private Menu commandMenu;

    public MenuBar(Viewer viewer) {
        if (viewer.getMode() == Viewer.Mode.LIVE) {
            add(new ServerMenu(viewer));
            commandMenu = new Menu("Command", 'C');
            add(commandMenu);
        }

        viewMenu = new Menu("View", 'V');
        add(viewMenu);
    }

    public Menu getViewMenu() {
        return viewMenu;
    }

    public Menu getCommandMenu() {
        return commandMenu;
    }
}
