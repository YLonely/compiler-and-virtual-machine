package myCompiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class CompilationEngine
{
	private FileWriter fileWriter = null;
	private BufferedWriter buffWriter = null;
	private File fileout = null;
	private Tokenizer tokenizer = null;
	private Writer writer = null;
	private int labelIndex = 0;
	private boolean stepOver = false;
	private int fromCondIndex = 0;

	@SuppressWarnings("unused")
	private CompilationEngine()
	{

	}

	public CompilationEngine(String outPath, Tokenizer tokenizer)
	{
		this.tokenizer = tokenizer;
		fileout = new File(outPath);
		try
		{
			fileWriter = new FileWriter(fileout);
			buffWriter = new BufferedWriter(fileWriter);
			writer = new Writer(buffWriter);
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void stop()
	{
		tokenizer.stop();
		tokenizer.filein.delete();
		try
		{
			buffWriter.close();
			fileWriter.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void analyse()
	{
		while (tokenizer.advance() != -1)
		{
			String token = tokenizer.getItems();
			if (token.equals("static"))
			{
				compileStatic();
			} else if (token.equals("function"))
			{
				compileFunction();
			} else
			{
				System.out.println("wrong command or empty file");
				break;
			}
		}
		this.stop();
	}

	private void compileStatic()
	{
		try
		{
			writer.write("static");
		} catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		tokenizer.advance();
		while (tokenizer.advance() != -1)
		{
			String token = tokenizer.getItems();
			if (token.equals("}"))
			{
				break;
			}
			try
			{
				compileExpression();
				writer.write("endstatic");
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void compileFunction()
	{
		try
		{
			SymbolTable table = new SymbolTable();
			tokenizer.advance();
			String functionName = tokenizer.getItems();
			String token = null;
			String argName = null;
			int argIndex = 0;
			writer.write("function", functionName);
			tokenizer.advance();
			tokenizer.advance();
			/*
			 * push all the arguements from arg space then pop them to local
			 */
			while (!((argName = tokenizer.getItems()).equals(")")))
			{
				if (!argName.equals(","))
				{
					table.putLocal(argName);
					writer.write("push", "arg", String.valueOf(argIndex++));
					writer.write("pop", table.getLocalValue(argName));
				}
				tokenizer.advance();
			}
			tokenizer.advance();
			tokenizer.advance();
			while (!((token = tokenizer.getItems()).equals("}")))
			{
				if (token.equals("if") || token.equals("while") || token.equals("return") || token.equals("from"))
				{
					compileStatements(table);
				} else
					compileExpression(table);
				if (stepOver)
				{
					stepOver = false;
				} else
					tokenizer.advance();
			}
			if (functionName.equals("main"))
			{
				writer.write("end");
			} else
				writer.write("back");

		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	/**
	 * compile statements such as if,while,return,from
	 * 
	 * @param table
	 */
	private void compileStatements(SymbolTable table)
	{
		try
		{
			String statementType = tokenizer.getItems();
			if (statementType.equals("if"))
			{
				compileIf(table);
			} else if (statementType.equals("while"))
			{
				compileWhile(table);
			} else if (statementType.equals("return"))
			{
				compileReturn(table);
			} else if (statementType.equals("from"))
			{
				compileFrom(table);
			}

		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	private void compileWhile(SymbolTable table)
	{
		String index1 = String.valueOf(labelIndex++);
		String index2 = String.valueOf(labelIndex++);
		ArrayList<String> tokenList = new ArrayList<>();
		String token = null;
		try
		{
			writer.write("label", "L" + index1);
			tokenizer.advance();
			while (!((token = tokenizer.getItems()).equals("{")))
			{
				tokenList.add(token);
				tokenizer.advance();
			}
			tokenList.add(";");
			compileExpressionAfterEquality(table, tokenList);
			tokenList.clear();
			writer.write("not");
			writer.write("ifgoto", "L" + index2);
			tokenizer.advance();
			while (!((token = tokenizer.getItems()).equals("}")))
			{
				if (token.equals("if") || token.equals("while") || token.equals("return") || token.equals("from"))
				{
					compileStatements(table);
				} else
					compileExpression(table);
				if (stepOver)
				{
					stepOver = false;
				} else
					tokenizer.advance();
			}
			writer.write("goto", "L" + index1);
			writer.write("label", "L" + index2);
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void compileReturn(SymbolTable table)
	{
		ArrayList<String> list = new ArrayList<>();
		String token = null;
		try
		{
			tokenizer.advance();
			if ((token = tokenizer.getItems()).equals(";"))
			{
				writer.write("back");
			} else
			{
				while (!((token = tokenizer.getItems()).equals(";")))
				{
					list.add(token);
					tokenizer.advance();
				}
				list.add(";");
				compileExpressionAfterEquality(table, list);
			}

		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void compileIf(SymbolTable table)
	{
		String index1 = String.valueOf(labelIndex++);
		String index2 = String.valueOf(labelIndex++);
		String token = null;
		ArrayList<String> list = new ArrayList<>();
		try
		{
			tokenizer.advance();
			while (!((token = tokenizer.getItems()).equals("{")))
			{
				list.add(token);
				tokenizer.advance();
			}
			list.add(";");
			compileExpressionAfterEquality(table, list);
			writer.write("not");
			writer.write("ifgoto", "L" + index1);
			tokenizer.advance();
			while (!((token = tokenizer.getItems()).equals("}")))
			{
				if (token.equals("if") || token.equals("while") || token.equals("return") || token.equals("from"))
				{
					compileStatements(table);
				} else
					compileExpression(table);
				if (stepOver)
				{
					stepOver = false;
				} else
					tokenizer.advance();
			}
			tokenizer.advance();
			if (tokenizer.getItems().equals("else"))
			{
				writer.write("goto", "L" + index2);
				writer.write("label", "L" + index1);
				tokenizer.advance();
				tokenizer.advance();
				while (!((token = tokenizer.getItems()).equals("}")))
				{
					if (token.equals("if") || token.equals("while") || token.equals("return") || token.equals("from"))
					{
						compileStatements(table);
					} else
						compileExpression(table);
					if (stepOver)
					{
						stepOver = false;
					} else
						tokenizer.advance();
				}
				writer.write("label", "L" + index2);
			} else
			{
				stepOver = true;
				writer.write("label", "L" + index1);
			}
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * analyse the expressions in the static filed only
	 * 
	 * @throws Exception
	 */
	private void compileExpression() throws Exception
	{
		int arrayNum = 0;
		boolean arrayExpression = false;
		String token = tokenizer.getItems();
		ArrayList<String> list = null;
		if (token.matches("^[_A-Za-z][_A-Za-z$0-9]*$") && !token.endsWith("_"))
		{
			tokenizer.advance();
			if (tokenizer.getItems().equals("["))
			{
				int arrayTotalNum = 0;
				if (!SymbolTable.ifStaticContains(token + "_array_"))
				{
					SymbolTable.putStatic(token + "_array_");
					writer.write("push", "heappos");
					writer.write("pop", SymbolTable.getStaticValue(token + "_array_"));
					tokenizer.advance();
					arrayTotalNum = Integer.parseInt(tokenizer.getItems());
					for (int i = 0; i < arrayTotalNum; i++)
					{
						writer.write("pop", "heaparr");
					}
					tokenizer.advance();
					tokenizer.advance();
					if (tokenizer.getItems().equals("["))
					{
						arrayExpression = true;
						tokenizer.advance();
						arrayNum = Integer.parseInt(tokenizer.getItems());
						tokenizer.advance();
						tokenizer.advance();
					} else
						return;
				} else
				{
					tokenizer.advance();
					arrayNum = Integer.parseInt(tokenizer.getItems());
					tokenizer.advance();
					tokenizer.advance();
					if (!tokenizer.getItems().equals("="))
					{
						throw new Exception("array assignment error");
					}
				}
			}
			if (tokenizer.getItems().equals("="))
			{
				if (!SymbolTable.ifStaticContains(token))
				{
					SymbolTable.putStatic(token);
				}
				list = new ArrayList<>();
				tokenizer.advance();
				String listToken = tokenizer.getItems();
				while (!listToken.equals(";") && !listToken.equals("to") && !listToken.equals("{")
						&& !listToken.equals(","))
				{
					list.add(listToken);
					tokenizer.advance();
					listToken = tokenizer.getItems();
				}
				list.add(";");
				compileExpressionAfterEqualityInStatic(list);
			} else if (tokenizer.getItems().equals(";"))
			{
				if (!SymbolTable.ifStaticContains(token))
				{
					SymbolTable.putStatic(token);
				}
				return;
			}
			if (arrayExpression)
			{
				writer.write("push", SymbolTable.getStaticValue(token + "_array_"));
				writer.write("pop", "pointer");
				writer.write("pop", "heap", String.valueOf(arrayNum));
				return;
			}
			writer.write("pop", SymbolTable.getStaticValue(token));
		} else
			throw new Exception("wrong identifier form");

	}

	/**
	 * analyse the expressions in the function filed only
	 * 
	 * @param table
	 * @throws Exception
	 */

	private void compileExpression(SymbolTable table) throws Exception
	{

		int arrayNum = 0;
		boolean arrayExpression = false;
		ArrayList<String> list = null;

		String token = tokenizer.getItems();
		if (token.matches("^[_A-Za-z][_A-Za-z$0-9]*$") && !token.endsWith("_"))
		{
			tokenizer.advance();
			if (tokenizer.getItems().equals("["))
			{
				arrayExpression = true;
				int arrayTotalNum = 0;
				int contains = table.ifContains(token + "_array_");
				if (contains == Constant.NONE_CONTAINS)
				{
					table.putLocal(token + "_array_");
					writer.write("push", "heappos");
					writer.write("pop", table.getLocalValue(token + "_array_"));
					tokenizer.advance();
					arrayTotalNum = Integer.parseInt(tokenizer.getItems());
					for (int i = 0; i < arrayTotalNum; i++)
					{
						writer.write("pop", "heaparr");
					}
					tokenizer.advance();
					tokenizer.advance();
					if (tokenizer.getItems().equals("["))
					{
						tokenizer.advance();
						arrayNum = Integer.parseInt(tokenizer.getItems());
						tokenizer.advance();
						tokenizer.advance();
					} else
						return;
				} else
				{
					tokenizer.advance();
					arrayNum = Integer.parseInt(tokenizer.getItems());
					tokenizer.advance();
					tokenizer.advance();
					if (!tokenizer.getItems().equals("="))
					{
						throw new Exception("array assignment error");
					}
				}
			} else if (tokenizer.getItems().equals("("))
			{
				String a = null;
				if (token.equals("printchar") || token.equals("printnum"))
				{
					compilePrint(table, token);
				} else if (SymbolTable.ifFunctionContains(token))
				{
					list = new ArrayList<>();
					list.add(token);
					while (!((a = tokenizer.getItems()).equals(";")))
					{
						list.add(a);
						tokenizer.advance();
					}
					list.add(";");
					compileExpressionAfterEquality(table, list);
				} else
				{
					throw new Exception("no function name");
				}
			}
			if (tokenizer.getItems().equals("="))
			{
				if (table.ifContains(token) == Constant.NONE_CONTAINS)
				{
					table.putLocal(token);
				}
				list = new ArrayList<>();
				tokenizer.advance();
				String listToken = tokenizer.getItems();
				while (!listToken.equals(";") && !listToken.equals("to") && !listToken.equals("{")
						&& !listToken.equals(","))
				{
					list.add(listToken);
					tokenizer.advance();
					listToken = tokenizer.getItems();
				}
				list.add(";");
				compileExpressionAfterEquality(table, list);
			} else if (tokenizer.getItems().equals(";"))
			{
				if (table.ifContains(token) == Constant.NONE_CONTAINS)
				{
					table.putLocal(token);
				}
				return;
			}

			int contains = table.ifContains(token);

			if (arrayExpression)
			{
				if (contains == Constant.LOCAL_CONTAINS)
				{
					writer.write("push", table.getLocalValue(token + "_array_"));
					writer.write("pop", "pointer");
					writer.write("pop", "heap", String.valueOf(arrayNum));
				} else if (contains == Constant.STATIC_CONTAINS)
				{
					writer.write("push", SymbolTable.getStaticValue(token + "_array_"));
					writer.write("pop", "pointer");
					writer.write("pop", "heap", String.valueOf(arrayNum));
				}
				return;
			}
			if (contains == Constant.LOCAL_CONTAINS)
			{
				writer.write("pop", table.getLocalValue(token));
			} else if (contains == Constant.STATIC_CONTAINS)
			{
				writer.write("pop", SymbolTable.getStaticValue(token));
			}
		} else
			throw new Exception("wrong identifier form");
	}

	/**
	 * analyse the expressions after equlity sign,including function call,array
	 * and varies
	 * 
	 * @param table
	 * @throws Exception
	 */
	private void compileExpressionAfterEquality(SymbolTable table, ArrayList<String> list) throws Exception
	{
		Stack expressionStack = new Stack();
		String symbol = null;
		expressionStack = ExpressionToRP.toRPExpression(list);
		ArrayList<String> functionList = new ArrayList<>();
		int arguementIndex = 0;
		while ((symbol = expressionStack.get()) != null)
		{
			if (symbol.endsWith("#"))
			{
				symbol = symbol.replace("#", "");
				if (!symbol.equals("printchar") && !symbol.equals("printnum"))
				{
					writer.write("call", symbol);
					arguementIndex = 0;
				}
			} else if (symbol.matches(".{1,}#.{1,}_?"))
			{
				String[] a = symbol.split("#");
				if (!a[1].endsWith("_"))
				{
					functionList.add(a[1]);
				} else
				{
					a[1] = a[1].replace("_", "");
					functionList.add(a[1]);
					functionList.add(";");
					compileExpressionAfterEquality(table, functionList);
					functionList.clear();
					writer.write("pop", "arg", String.valueOf(arguementIndex++));
				}
			} else if (symbol.endsWith(":"))
			{
				continue;
			} else if (symbol.contains(":") && !symbol.endsWith(":") && !symbol.contains("'") && !symbol.contains("\""))
			{
				String[] a = symbol.split(":");
				String arrayName = a[0], arraynum = a[1];
				int contains = table.ifContains(arrayName + "_array_");
				if (contains == Constant.NONE_CONTAINS)
				{
					table.putLocal(arrayName + "_array_");
					writer.write("push", "heappos");
					writer.write("pop", table.getLocalValue(arrayName + "_array_"));
					for (int i = 0; i < Integer.parseInt(arraynum); i++)
					{
						writer.write("pop", "heaparr");
					}
					writer.write("push", "constant", "0");
				} else if (contains == Constant.LOCAL_CONTAINS)
				{
					writer.write("push", table.getLocalValue(arrayName + "_array_"));
					writer.write("pop", "pointer");
					writer.write("push", "heap", arraynum);
				} else
				{
					writer.write("push", SymbolTable.getStaticValue(arrayName + "_array_"));
					writer.write("pop", "pointer");
					writer.write("push", "heap", arraynum);
				}
			} else if (symbol.matches("\'.\'"))
			{
				symbol = symbol.replace("'", "");
				char[] a = symbol.toCharArray();
				int symbolASCII = a[0];
				writer.write("push", "constant", String.valueOf(symbolASCII));
			} else if (symbol.matches("\"[^\"]{0,}\""))
			{
				int j = 0;
				symbol = symbol.replace("\"", "");
				char[] a = symbol.toCharArray();
				writer.write("push", "heappos");
				writer.write("push", "heappos");
				writer.write("pop", "pointer");
				for (int i = 0; i <= a.length; i++)
				{
					writer.write("pop", "heaparr");
				}
				for (int i = 0; i < a.length; i++, j++)
				{
					if (a[i] == '\\' && a[i + 1] == 'n')
					{
						writer.write("push", "constant", "10");
						writer.write("pop", "heap", String.valueOf(i));
						i++;
						continue;
					}
					writer.write("push", "constant", String.valueOf((int) a[i]));
					writer.write("pop", "heap", String.valueOf(j));
				}
				writer.write("push", "constant", "0");
				writer.write("pop", "heap", String.valueOf(j));
			} else if (symbol.matches("\\d{1,}"))
			{
				writer.write("push", "constant", symbol);
			} else if (symbol.equals("+"))
			{
				writer.write("add");
			} else if (symbol.equals("-"))
			{
				writer.write("sub");
			} else if (symbol.equals("*"))
			{
				writer.write("mult");
			} else if (symbol.equals("/"))
			{
				writer.write("dev");
			} else if (symbol.equals("=="))
			{
				writer.write("eq");
			} else if (symbol.equals("!="))
			{
				writer.write("uneq");
			} else if (symbol.equals(">="))
			{
				writer.write("gt_eq");
			} else if (symbol.equals("<="))
			{
				writer.write("lt_eq");
			} else if (symbol.equals(">"))
			{
				writer.write("gt");
			} else if (symbol.equals("<"))
			{
				writer.write("lt");
			} else
			{
				int contains = table.ifContains(symbol);
				if (contains == Constant.NONE_CONTAINS)
				{
					table.putLocal(symbol);
					writer.write("push", table.getLocalValue(symbol));
				} else if (contains == Constant.LOCAL_CONTAINS)
				{
					writer.write("push", table.getLocalValue(symbol));
				} else
				{
					writer.write("push", SymbolTable.getStaticValue(symbol));
				}
			}
		}
	}

	/**
	 * analyse the expressions after equlity sign,including array
	 * and varies
	 * 
	 * @throws Exception
	 */
	private void compileExpressionAfterEqualityInStatic(ArrayList<String> list) throws Exception
	{
		Stack expressionStack = new Stack();
		String symbol = null;
		expressionStack = ExpressionToRP.toRPExpression(list);
		while ((symbol = expressionStack.get()) != null)
		{
			if (symbol.endsWith("#"))
			{
				throw new Exception("static:call function error");
			} else if (symbol.contains("#"))
			{
				throw new Exception("static:call function error");
			} else if (symbol.endsWith(":"))
			{
				continue;
			} else if (symbol.contains(":") && !symbol.endsWith(":"))
			{
				String[] a = symbol.split(":");
				String arrayName = a[0], arraynum = a[1];
				if (!SymbolTable.ifStaticContains(arrayName + "_array_"))
				{
					SymbolTable.putStatic(arrayName + "_array_");
					writer.write("push", "heappos");
					writer.write("pop", SymbolTable.getStaticValue(arrayName + "_array_"));
					for (int i = 0; i < Integer.parseInt(arraynum); i++)
					{
						writer.write("pop", "heaparr");
					}
					writer.write("push", "constant", "0");
				} else
				{
					writer.write("push", SymbolTable.getStaticValue(arrayName + "_array_"));
					writer.write("pop", "pointer");
					writer.write("push", "heap", arraynum);
				}
			} else if (symbol.matches("\'.\'"))
			{
				symbol = symbol.replace("'", "");
				char[] a = symbol.toCharArray();
				int symbolASCII = a[0];
				writer.write("push", "constant", String.valueOf(symbolASCII));
			} else if (symbol.matches("\"[^\"]{0,}\""))
			{
				symbol = symbol.replace("\"", "");
				char[] a = symbol.toCharArray();
				writer.write("push", "heappos");
				writer.write("push", "heappos");
				writer.write("pop", "pointer");
				for (int i = 0; i <= a.length; i++)
				{
					writer.write("pop", "heaparr");
				}
				for (int i = 0; i < a.length; i++)
				{
					writer.write("push", "constant", String.valueOf((int) a[i]));
					writer.write("pop", "heap", String.valueOf(i));
				}
				writer.write("push", "constant", "0");
				writer.write("pop", "heap", String.valueOf(a.length));
			} else if (symbol.matches("\\d{1,}"))
			{
				writer.write("push", "constant", symbol);
			} else if (symbol.equals("+"))
			{
				writer.write("add");
			} else if (symbol.equals("-"))
			{
				writer.write("sub");
			} else if (symbol.equals("*"))
			{
				writer.write("mult");
			} else if (symbol.equals("/"))
			{
				writer.write("dev");
			} else if (symbol.equals("=="))
			{
				writer.write("eq");
			} else if (symbol.equals("!="))
			{
				writer.write("uneq");
			} else if (symbol.equals(">="))
			{
				writer.write("gt_eq");
			} else if (symbol.equals("<="))
			{
				writer.write("lt_eq");
			} else if (symbol.equals(">"))
			{
				writer.write("gt");
			} else if (symbol.equals("<"))
			{
				writer.write("lt");
			} else
			{
				if (!SymbolTable.ifStaticContains(symbol))
				{
					SymbolTable.putStatic(symbol);
				}
				writer.write("push", SymbolTable.getStaticValue(symbol));
			}
		}
	}

	private void compilePrint(SymbolTable table, String token)
	{
		String token1 = null;
		ArrayList<String> list = new ArrayList<>();
		try
		{
			while (!((token1 = tokenizer.getItems()).equals(";")))
			{
				list.add(token1);
				tokenizer.advance();
			}
			list.add(";");
			compileExpressionAfterEquality(table, list);
			if (token.equals("printnum"))
			{
				writer.write("printnum");
			} else
				writer.write("printchar");
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * analyse a new form of for-statement.
	 * from (a) to (b) by (c)
	 * @param table
	 */
	private void compileFrom(SymbolTable table)
	{
		String index1 = String.valueOf(labelIndex++);
		String index2 = String.valueOf(labelIndex++);
		String token = null;
		ArrayList<String> list = new ArrayList<>();
		String condIndex1 = String.valueOf(fromCondIndex++);
		String condIndex2 = String.valueOf(fromCondIndex++);
		String condIndex3 = String.valueOf(fromCondIndex++);
		try
		{
			table.putLocal("cond" + condIndex1 + "_");
			table.putLocal("cond" + condIndex2 + "_");
			table.putLocal("cond" + condIndex3 + "_");
			table.putLocal("Loop");
			tokenizer.advance();
			while (!((token = tokenizer.getItems()).equals("to")))
			{
				list.add(token);
				tokenizer.advance();
			}
			list.add(";");
			compileExpressionAfterEquality(table, list);
			list.clear();
			writer.write("pop", table.getLocalValue("cond" + condIndex1 + "_"));
			tokenizer.advance();
			while (!((token = tokenizer.getItems()).equals("by")))
			{
				list.add(token);
				tokenizer.advance();
			}
			list.add(";");
			compileExpressionAfterEquality(table, list);
			list.clear();
			writer.write("pop", table.getLocalValue("cond" + condIndex2 + "_"));

			tokenizer.advance();
			while (!((token = tokenizer.getItems()).equals("{")))
			{
				list.add(token);
				tokenizer.advance();
			}
			list.add(";");
			compileExpressionAfterEquality(table, list);
			list.clear();
			writer.write("pop", table.getLocalValue("cond" + condIndex3 + "_"));
			writer.write("push", table.getLocalValue("cond" + condIndex1 + "_"));
			writer.write("pop", table.getLocalValue("Loop"));
			writer.write("label", "L" + index1);
			writer.write("push", table.getLocalValue("cond" + condIndex2 + "_"));
			writer.write("push", table.getLocalValue("Loop"));
			writer.write("gt_eq");
			writer.write("not");
			writer.write("ifgoto", "L" + index2);
			tokenizer.advance();
			while (!((token = tokenizer.getItems()).equals("}")))
			{
				if (token.equals("if") || token.equals("while") || token.equals("return") || token.equals("from"))
				{
					compileStatements(table);
				} else
					compileExpression(table);
				if (stepOver)
				{
					stepOver = false;
				} else
					tokenizer.advance();
			}
			writer.write("push", table.getLocalValue("cond" + condIndex3 + "_"));
			writer.write("push", table.getLocalValue("Loop"));
			writer.write("add");
			writer.write("pop", table.getLocalValue("Loop"));
			writer.write("goto", "L" + index1);
			writer.write("label", "L" + index2);
			table.deleteLocal("cond" + condIndex1 + "_");
			table.deleteLocal("cond" + condIndex2 + "_");
			table.deleteLocal("cond" + condIndex3 + "_");
			fromCondIndex = fromCondIndex - 3;
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
