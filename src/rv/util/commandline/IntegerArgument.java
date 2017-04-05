package rv.util.commandline;

public class IntegerArgument extends Argument<Integer>
{
	private final int minValue;

	private final int maxValue;

	public IntegerArgument(String name, Integer defaultValue)
	{
		this(name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	public IntegerArgument(String name, Integer defaultValue, int minValue)
	{
		this(name, defaultValue, minValue, Integer.MAX_VALUE);
	}

	public IntegerArgument(String name, Integer defaultValue, int minValue, int maxValue)
	{
		super(name, defaultValue);
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	@Override
	protected Integer extractValue(String value)
	{
		try {
			return verifyValue(Integer.valueOf(value));
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private Integer verifyValue(Integer value)
	{
		if (value < minValue || value > maxValue) {
			System.out.println(String.format("%s Range is %d-%d, using default value %d instead.",
					getInvalidArgString(value), minValue, maxValue, defaultValue));
			return defaultValue;
		}
		return value;
	}
}