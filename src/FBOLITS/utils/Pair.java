package FBOLITS.utils;

public class Pair<T, U>
{

	private T first;
	private U second;

	public Pair(T f, U s)
	{
		this.first = f;
		this.second = s;
	}

	public T first()
	{
		return first;
	}

	public U second()
	{
		return second;
	}

	public void set(Pair<T, U> pair)
	{
		first = pair.first;
		second = pair.second;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (obj == null || !(getClass().isInstance(obj)))
			return false;

		@SuppressWarnings("unchecked")
		Pair<T, U> other = getClass().cast(obj);
		return (first == null ? other.first == null : first.equals(other.first))
				&& (second == null ? other.second == null : second.equals(other.second));
	}

}
