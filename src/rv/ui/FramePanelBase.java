package rv.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import rv.Globals;

public class FramePanelBase {
    protected JFrame frame;

    public void showFrame() {
        frame.setVisible(true);
    }

    public FramePanelBase(String title) {
        frame = new JFrame(title);
        frame.setIconImage(Globals.getIcon());
    }

    public void addCloseHotkey() {
        String actionName = "ClosePanel" + frame.getTitle();
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), actionName);
        frame.getRootPane().getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
            }
        });
    }
}
