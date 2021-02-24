package rv.util;

public class StringUtil
{
	public static String capitalize(String s)
	{
		if (s == null)
			return null;
		if (s.isEmpty())
			return s;
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}
}
