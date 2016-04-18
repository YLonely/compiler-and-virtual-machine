package myCompiler;

import java.util.ArrayList;

public class ExpressionToRP
{
	private static Stack symbolStack = null;
	private static Stack expreStack = null;

	/**
	 * change the normal expression to RPExpression,every tokenList that send in
	 * must add a ";" at the end of the list.
	 * 
	 * @param tokenList
	 * @return
	 * @throws Exception
	 */
	public static Stack toRPExpression(ArrayList<String> tokenList) throws Exception
	{
		symbolStack = new Stack();
		expreStack = new Stack();
		symbolStack.push("#");
		String temp = null;
		String token = null;
		int index = 0;
		while (true)
		{
			token = tokenList.get(index++);
			if (token.equals(";"))
			{
				break;
			} else if (token.equals("("))
			{
				symbolStack.push(token);
			} else if (token.equals(")"))
			{
				while (!symbolStack.topObject().equals("("))
				{
					temp = symbolStack.pop();
					expreStack.push(temp);
				}
				symbolStack.pop();
			} else if (token.equals("+") || token.equals("-"))
			{
				for (temp = symbolStack.topObject(); !temp.equals("#") && !temp.equals("==") && !temp.equals("!=")
						&& !temp.equals(">=") && !temp.equals("<=") && !temp.equals(">")
						&& !temp.equals("<"); temp = symbolStack.topObject())
				{
					if (temp.equals("("))
					{
						break;
					} else
					{
						temp = symbolStack.pop();
						expreStack.push(temp);
					}
				}
				symbolStack.push(token);
			} else if (token.equals("!"))
			{
				for (temp = symbolStack.topObject(); !temp.equals("#") && !temp.equals("==") && !temp.equals("!=")
						&& !temp.equals(">=") && !temp.equals("<=") && !temp.equals(">") && !temp.equals("<")
						&& !temp.equals("+") && !temp.equals("-") && !temp.equals("*")
						&& !temp.equals("/"); temp = symbolStack.topObject())
				{
					if (temp.equals("("))
					{
						break;
					} else
					{
						temp = symbolStack.pop();
						expreStack.push(temp);
					}
				}
				symbolStack.push(token);
			} else if (token.equals("*") || token.equals("/"))
			{
				for (temp = symbolStack.topObject(); !temp.equals("#") && !temp.equals("==") && !temp.equals("!=")
						&& !temp.equals(">=") && !temp.equals("<=") && !temp.equals(">") && !temp.equals("<")
						&& !temp.equals("+") && !temp.equals("-"); temp = symbolStack.topObject())
				{
					if (temp.equals("("))
					{
						break;
					} else
					{
						temp = symbolStack.pop();
						expreStack.push(temp);
					}
				}
				symbolStack.push(token);
			} else if (token.equals("==") || token.equals("!=") || token.equals(">") || token.equals("<")
					|| token.equals(">=") || token.equals("<="))
			{
				for (temp = symbolStack.topObject(); !temp.equals("#"); temp = symbolStack.topObject())
				{
					if (temp.equals("("))
					{
						break;
					} else
					{
						temp = symbolStack.pop();
						expreStack.push(temp);
					}
				}
				symbolStack.push(token);
			}

			// judge the type of the identifier.is it a function call or data
			// array
			else if (token.matches("^[_A-Za-z][_A-Za-z$0-9]*$") && !token.endsWith("_"))
			{
				String temp2 = tokenList.get(index);
				if (temp2.equals("("))
				{
					if (!SymbolTable.ifFunctionContains(token))
					{
						throw new Exception("no function name");
					} else
					{
						String s1 = tokenList.get(index + 1);
						String s2 = null;
						if (!s1.equals(")"))
						{
							index++;
							s1 = tokenList.get(index++);
							s2 = tokenList.get(index);
							/*
							 * if it's a function call,bind all the parameters
							 * with function name and symbol '#'.add another
							 * more symbol '_' when reaching the end of the
							 * param.
							 */
							while (!(s2.equals(")") && tokenList.get(index + 1).equals(";")))
							{
								if (s2.equals(","))
								{
									expreStack.push(token + "_functionPara_" + s1 + "_End_");
									index++;
									s1 = tokenList.get(index++);
									s2 = tokenList.get(index);
								} else
								{
									expreStack.push(token + "_functionPara_" + s1);
									s1 = tokenList.get(index++);
									s2 = tokenList.get(index);
								}
							}
							expreStack.push(token + "_functionPara_" + s1 + "_End_");
						} else
							index++;
						expreStack.push(token + "_functionName_");
					}
					index++;
				} /*
					 * if it's a array,bind all the parameters with array name
					 * and symbol ':'
					 */
				else if (temp2.equals("["))
				{
					expreStack.push(token + ":");
					expreStack.push(token + ":" + tokenList.get(++index));
					index++;
					index++;
				} else
					expreStack.push(token);

			} else
			{
				expreStack.push(token);
			}
		}
		while (!symbolStack.ifEmpty() && symbolStack.topObject() != "#")
		{
			temp = symbolStack.pop();
			expreStack.push(temp);
		}
		return expreStack;
	}
}
