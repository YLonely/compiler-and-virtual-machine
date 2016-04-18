package myCompiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
/**
 *Record the name of each function in file;
 *Put the name into a hashTable;
 * @author Administrator
 *
 */
public class FunctionCounter
{
	private BufferedReader buffReader = null;
	private FileReader fileReader = null;
	private File filein = null;

	@SuppressWarnings("unused")
	private FunctionCounter()
	{

	}

	public FunctionCounter(String filePath)
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

	public void count() throws Exception
	{
		String lines = null;
		try
		{
			while ((lines = buffReader.readLine()) != null)
			{
				if (lines.equals("function"))
				{
					lines = buffReader.readLine();
					if (!SymbolTable.ifFunctionContains(lines))
					{
						SymbolTable.putFunction(lines);
					} else
					{
						this.counterStop();
						throw new Exception("same function name");
					}
				}

			}
			this.counterStop();

		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void counterStop()
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
