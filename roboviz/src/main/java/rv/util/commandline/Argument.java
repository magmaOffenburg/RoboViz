package rv.util.commandline;

/**
 * Generic base class for command line arguments.
 */
public abstract class Argument<T>
{
	/**
	 * Should be called when parsing is finished. Checks for any unrecognized arguments and prints a
	 * warning if one is found.
	 */
	public static void endParse(String... args)
	{
		for (String arg : args) {
			if (!arg.equals("")) {
				System.err.println(String.format("Unknown argument '%s'.", arg));
			}
		}
	}

	protected final String name;

	/** value to use if parsing fails */
	protected final T defaultValue;

	public Argument(String name, T defaultValue)
	{
		this.name = name;
		this.defaultValue = defaultValue;
	}

	/**
	 * Attempts to find and parse this argument from the args array. Parsed arguments are replaced
	 * with an empty string.
	 */
	public T parse(String... args)
	{
		if (args == null) {
			return defaultValue;
		}

		T result = defaultValue;
		boolean found = false;

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (matchesName(arg)) {
				args[i] = "";
				String stringValue = extractStringValue(arg);
				if (found) {
					System.err.println(String.format(
							"Duplicate '--%s' argument with value '%s' found, using the first value '%s' instead.",
							name, stringValue, result));
					continue;
				}
				found = true;

				if (stringValue != null) {
					result = extractValue(stringValue);
				}
			}
		}
		return result;
	}

	/**
	 * The name as it's used on the command line: <br>
	 * <code>--argumentname=</code>
	 */
	protected String getFormattedName()
	{
		return "--" + name + "=";
	}

	protected boolean matchesName(String arg)
	{
		return arg.startsWith(getFormattedName());
	}

	/** To be overridden in subclasses. */
	protected abstract T extractValue(String value);

	protected String extractStringValue(String arg)
	{
		String value = arg.replaceFirst(getFormattedName(), "");
		if (value.equals("")) {
			printInvalidArgInfo("");
			return null;
		}
		return value;
	}

	protected String getInvalidArgString(Object value)
	{
		return String.format("Invalid '--%s' value: '%s'.", name, value);
	}

	protected void printInvalidArgInfo(Object value)
	{
		System.err.println(getInvalidArgString(value));
	}
}
