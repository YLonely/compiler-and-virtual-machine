package myCompiler;

public class Main
{
	public static void main(String[] args)
	{
		String inPath = args[0];
		Analyzer analyzer = new Analyzer(inPath);
		try
		{
			analyzer.analyse();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
