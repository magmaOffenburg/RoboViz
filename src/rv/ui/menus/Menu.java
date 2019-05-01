package rv.ui.menus;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class Menu extends JMenu
{
	public Menu(String name, char mnemonic)
	{
		super(name);
		setMnemonic(mnemonic);
	}

	public void addItem(String text, String accelerator, final Runnable runnable)
	{
		addItem(text, KeyStroke.getKeyStroke(accelerator), runnable);
	}

	public void addItem(String text, KeyStroke accelerator, final Runnable runnable)
	{
		JMenuItem item = new JMenuItem(new AbstractAction(text) {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				runnable.run();
			}
		}) {
			@Override
			public void setAccelerator(KeyStroke keyStroke)
			{
				super.setAccelerator(keyStroke);
				getInputMap(WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "none");
			}
		};
		item.setAccelerator(accelerator);
		add(item);
	}
}
