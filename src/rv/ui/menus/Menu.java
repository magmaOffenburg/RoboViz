package rv.ui.menus;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class Menu extends JMenu {
    public Menu(String name, char mnemonic) {
        super(name);
        setMnemonic(mnemonic);
    }

    public void addItem(String text, String accelerator, final AbstractAction action) {
        addItem(text, KeyStroke.getKeyStroke(accelerator), action);
    }

    public void addItem(String text, KeyStroke accelerator, final AbstractAction action) {
        JMenuItem item = new JMenuItem(new AbstractAction(text) {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.actionPerformed(null);
            }
        }) {
            @Override
            public void setAccelerator(KeyStroke keyStroke) {
                super.setAccelerator(keyStroke);
                getInputMap(WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "none");
            }
        };
        item.setAccelerator(accelerator);
        add(item);
    }
}
