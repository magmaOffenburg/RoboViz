package rv.ui.menus;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import rv.Viewer;

public class Menu extends JMenu {
    public Menu(String name, char mnemonic, final Viewer viewer) {
        super(name);
        setMnemonic(mnemonic);
    }

    public void addItem(String text, String accelerator, final AbstractAction action) {
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
        item.setAccelerator(KeyStroke.getKeyStroke(accelerator));
        add(item);
    }
}
