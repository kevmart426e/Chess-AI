package hw2.chess.utils;

import java.util.HashMap;
import java.util.Map;

public class Copier extends Object
{

	public static <K,V> Map<K, V> copyMap(Map<K, V> mapToCopy)
	{
		Map<K, V> copy = new HashMap<K, V>(mapToCopy.size());

		for(Map.Entry<K, V> entry : mapToCopy.entrySet())
		{
			copy.put(entry.getKey(), entry.getValue());
		}

		return copy;
	}

}
