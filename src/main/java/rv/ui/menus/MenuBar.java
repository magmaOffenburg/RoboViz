package rv.ui.menus;

import javax.swing.JMenuBar;
import rv.Viewer;

public class MenuBar extends JMenuBar
{
	private Menu serverMenu;
	private Menu viewMenu;
	private Menu cameraMenu;

	public MenuBar(Viewer viewer)
	{
		if (viewer.getMode() == Viewer.Mode.LIVE) {
			add(new ConnectionMenu(viewer));
			serverMenu = new Menu("Server", 'S');
			add(serverMenu);
		}

		viewMenu = new Menu("View", 'V');
		add(viewMenu);

		cameraMenu = new Menu("Camera", 'm');
		add(cameraMenu);
	}

	public Menu getServerMenu()
	{
		return serverMenu;
	}

	public Menu getViewMenu()
	{
		return viewMenu;
	}

	public Menu getCameraMenu()
	{
		return cameraMenu;
	}
}
