package myCompiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Tokenizer
{

	public File filein = null;
	private FileReader fileReader = null;
	private BufferedReader buffReader = null;
	private String currentTokens = null;

	private void setCurrentTokens(String currentTokens)
	{
		this.currentTokens = currentTokens;
	}

	@SuppressWarnings("unused")
	private Tokenizer()
	{
	}

	public Tokenizer(String filePath)
	{
		filein = new File(filePath);
		try
		{
			fileReader = new FileReader(filein);
			buffReader = new BufferedReader(fileReader);
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public int advance()
	{
		if (!hasMoreTokens())
		{
			return -1;
		} else
			return 1;
	}

	private boolean hasMoreTokens()
	{
		boolean hasTokens;
		String lines = null;
		try
		{
			lines = buffReader.readLine();
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
		if (lines != null)
		{
			hasTokens = true;
			setCurrentTokens(lines);
		} else
		{
			hasTokens = false;
			setCurrentTokens(null);
		}
		return hasTokens;
	}

	public String tokenType()
	{
		if (currentTokens == null)
		{
			return "unknown";
		}
		if (currentTokens.matches("\\d{1,}"))
		{
			return "intergerConstant";
		}
		if (currentTokens.matches("\"[^\"]{0,}\""))
		{
			return "stringConstant";
		}
		for (int i = 0; i < Constant.KEYWORDS.length; i++)
		{
			if (currentTokens.equals(Constant.KEYWORDS[i]))
			{
				return "keyword";
			}
		}
		for (int i = 0; i < Constant.SYMBOLS.length; i++)
		{
			if (currentTokens.equals(Constant.SYMBOLS[i]))
			{
				return "symbol";
			}
		}
		return "identifier";
	}

	public String getItems()
	{
		String newtoken = currentTokens;
		return newtoken;
	}

	public void stop()
	{
		try
		{
			buffReader.close();
			fileReader.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
