package rv.util;

public class MacEnhancements
{
	public static final boolean IS_MAC = System.getProperty("os.name").contains("OS X");

	public static void useSystemMenuBar()
	{
		System.setProperty("apple.laf.useScreenMenuBar", "true");
	}
}
