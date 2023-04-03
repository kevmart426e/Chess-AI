package hw2.chess.utils;

public class Triple<K, V, T> extends Object
{

	private final K first;
	private final V second;
	private final T third;

	public Triple(K first, V second, T third)
	{
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public K getFirst() { return this.first; }
	public V getSecond() { return this.second; }
	public T getThird() { return this.third; }
}
