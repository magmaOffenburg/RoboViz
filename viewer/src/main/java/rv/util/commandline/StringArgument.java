package rv.util.commandline;

public class StringArgument extends Argument<String>
{
	public StringArgument(String name, String defaultValue)
	{
		super(name, defaultValue);
	}

	@Override
	protected String extractValue(String value)
	{
		return value;
	}
}
