package hw2.chess.utils;

import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

public class TypeConverter extends Object
{

	public static <T> Stack<T> listToStack(List<T> list)
	{
		Stack<T> stack = new Stack<T> ();

		ListIterator<T> listIt = list.listIterator(list.size());
		while(listIt.hasPrevious())
		{
			stack.push(listIt.previous());
		}
		return stack;
	}

}
