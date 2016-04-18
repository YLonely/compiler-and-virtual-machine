package myCompiler;

import java.util.ArrayList;

public class Stack
{
	private ArrayList<String> array = null;
	int top = -1;
	int getNum = 0;

	public Stack()
	{
		array = new ArrayList<>();
	}

	public void push(String s)
	{
		array.add(s);
		top++;
	}

	public String pop()
	{
		String a = array.get(top);
		array.remove(top--);
		return a;
	}

	public boolean ifEmpty()
	{
		return top == -1;
	}

	public String topObject()
	{
		if (top == -1)
		{
			return "null";
		} else
			return array.get(top);
	}

	public String get()
	{
		if (getNum > top)
		{
			return null;
		} else
			return array.get(getNum++);
	}
}
