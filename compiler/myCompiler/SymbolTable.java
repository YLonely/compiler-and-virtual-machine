package myCompiler;

import java.util.Hashtable;

public class SymbolTable
{
	private static int staticNum = 0, functionNum = 0;
	private int localNum, argNum;
	private static Hashtable<String, String> staticTable = new Hashtable<>();
	private static Hashtable<String, String> argTable = new Hashtable<>();
	private static Hashtable<String, Integer> functionTable = new Hashtable<>();
	private Hashtable<String, String> localTable = null;

	public static boolean ifFunctionContains(String name)
	{
		boolean contain = false;
		if (functionTable.containsKey(name))
		{
			contain = true;
		}
		return contain;
	}

	public static void putFunction(String name)
	{
		functionTable.put(name, functionNum++);
	}

	/**
	 * Confirm the type of variable.is it global variable or local variable.
	 * method will ignore the variable in static filed when a local variable
	 * exists
	 * 
	 * @param name
	 *            ±äÁ¿Ãû
	 * @return Constant.NONE_CONTAINS Constant.LOCAL_CONTAINS
	 *         Constant.STATIC_CONTAINS
	 */
	public int ifContains(String name)
	{
		int contains = Constant.NONE_CONTAINS;
		if (localTable != null && localTable.containsKey(name))
		{
			contains = Constant.LOCAL_CONTAINS;
		} else if (staticTable.containsKey(name))
		{
			contains = Constant.STATIC_CONTAINS;
		}
		return contains;
	}

	public static boolean ifStaticContains(String name)
	{
		boolean contain = false;
		if (staticTable.containsKey(name))
		{
			contain = true;
		}
		return contain;
	}

	/**
	 * must be used after method: ifContains() to make sure that the name is
	 * contained in the table
	 * 
	 */

	public static String getStaticValue(String name)
	{
		String value = null;
		value = staticTable.get(name);
		return value;
	}

	public String getArgValue(String name)
	{
		String value = null;
		value = argTable.get(name);
		return value;
	}

	public String getLocalValue(String name) throws Exception
	{
		String value = null;
		if (localTable == null)
		{
			throw new Exception("localTable is null");
		} else
			value = localTable.get(name);
		return value;
	}

	public SymbolTable()
	{
		localTable = new Hashtable<>();
		localNum = 0;
		argNum = 0;
	}

	public void putLocal(String name)
	{
		localTable.put(name, "local " + String.valueOf(localNum));
		localNum++;
	}

	public static void putStatic(String name)
	{
		staticTable.put(name, "static " + String.valueOf(staticNum));
		staticNum++;
	}

	public void putArg(String name)
	{
		argTable.put(name, "arg " + String.valueOf(argNum));
		argNum++;
	}

	public void deleteLocal(String name)
	{
		localTable.remove(name);
	}

}
