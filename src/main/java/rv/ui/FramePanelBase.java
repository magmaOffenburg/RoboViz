package rv.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rv.util.swing.SwingUtil;

public class FramePanelBase
{
	private static final Logger LOGGER = LogManager.getLogger();

	protected final JFrame frame;

	public void showFrame(JFrame parent)
	{
		frame.setVisible(true);
		if (parent != null) {
			frame.setLocation(SwingUtil.getCurrentScreenLocation(parent));
		}
	}

	public void hideFrame(JFrame frame)
	{
		frame.setVisible(false);
	}

	public FramePanelBase(String title)
	{
		frame = new JFrame(title);
		try {
			frame.setIconImage(ImageIO.read(FramePanelBase.class.getResourceAsStream("/images/icon.png")));
		} catch (IOException e) {
			LOGGER.error("Unable to set icon image", e);
		}
	}

	public void addCloseHotkey()
	{
		String actionName = "ClosePanel" + frame.getTitle();
		frame.getRootPane()
				.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), actionName);
		frame.getRootPane().getActionMap().put(actionName, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				frame.setVisible(false);
			}
		});
	}
}
