package rv;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.github.weisj.darklaf.DarkLaf;
import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import com.github.weisj.darklaf.theme.IntelliJTheme;
import com.github.weisj.darklaf.theme.SolarizedDarkTheme;
import com.github.weisj.darklaf.theme.SolarizedLightTheme;

public class Globals
{
	private static Image icon;

	public static Image getIcon()
	{
		if (icon == null) {
			try {
				icon = ImageIO.read(Globals.class.getResourceAsStream("/images/icon.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return icon;
	}

	public static void setLookFeel(String lookAndFeel)
	{
		try {
			switch (lookAndFeel) {
			case "darcula":
				LafManager.setTheme(new DarculaTheme());
				UIManager.setLookAndFeel(DarkLaf.class.getCanonicalName());
				break;
			case "intellij":
				LafManager.setTheme(new IntelliJTheme());
				UIManager.setLookAndFeel(DarkLaf.class.getCanonicalName());
				break;
			case "solarized_dark":
				LafManager.setTheme(new SolarizedDarkTheme());
				UIManager.setLookAndFeel(DarkLaf.class.getCanonicalName());
				break;
			case "solarized_light":
				LafManager.setTheme(new SolarizedLightTheme());
				UIManager.setLookAndFeel(DarkLaf.class.getCanonicalName());
				break;
			default:
				UIManager.setLookAndFeel(NimbusLookAndFeel.class.getCanonicalName());
				break;
			}
		} catch (Exception e) {
			// If Nimbus is not available
		}
	}
}
