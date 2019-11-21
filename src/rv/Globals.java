package rv;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.UIManager;

public class Globals
{
	private static Image icon;

	public static Image getIcon()
	{
		if (icon == null) {
			try {
				icon = ImageIO.read(Globals.class.getClassLoader().getResourceAsStream("resources/images/icon.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return icon;
	}

	public static void setLookFeel()
	{
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
			// If Nimbus is not available
		}
	}
}
