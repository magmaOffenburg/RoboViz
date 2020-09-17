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

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Pair) {
			@SuppressWarnings("rawtypes")
			Pair otherPair = (Pair) obj;

			boolean equal = true;
			if (first != null) {
				equal &= first.equals(otherPair.getFirst());
			} else {
				equal &= otherPair.getFirst() == null;
			}
			if (second != null) {
				equal &= second.equals(otherPair.getSecond());
			} else {
				equal &= otherPair.getSecond() == null;
			}

			return equal;
		} else {
			return false;
		}
	}
}
