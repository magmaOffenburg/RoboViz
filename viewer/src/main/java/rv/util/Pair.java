package rv.util;

public class Pair<T, H>
{
	private T first;
	private H second;

	public Pair(final T first, final H second)
	{
		this.first = first;
		this.second = second;
	}

	public H getSecond()
	{
		return second;
	}

	public T getFirst()
	{
		return first;
	}

	public void setFirst(final T first)
	{
		this.first = first;
	}

	public void setSecond(final H second)
	{
		this.second = second;
	}

	@Override
	public String toString()
	{
		return "[" + first.toString() + "," + second.toString() + "]";
	}
}
