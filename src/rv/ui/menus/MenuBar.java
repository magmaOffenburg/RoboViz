package rv.ui.menus;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import javax.swing.JMenuBar;
import javax.swing.MenuSelectionManager;
import rv.Viewer;

public class MenuBar extends JMenuBar {
    public MenuBar(Viewer viewer) {
        if (viewer.getMode() == Viewer.Mode.LIVE)
            add(new ServerMenu(viewer));

        // properly close open menus if clicking outside
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof MouseEvent && event.getID() == MouseEvent.MOUSE_PRESSED
                        && !MenuBar.this.contains(((MouseEvent) event).getPoint())) {
                    MenuSelectionManager.defaultManager().clearSelectedPath();
                } else if (event instanceof FocusEvent && event.getID() == FocusEvent.FOCUS_LOST) {
                    MenuSelectionManager.defaultManager().clearSelectedPath();
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK);
    }
}
