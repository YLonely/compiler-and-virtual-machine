package myCompiler;

import java.io.BufferedWriter;

public class Writer
{
	BufferedWriter writer = null;

	@SuppressWarnings("unused")
	private Writer()
	{

	}

	public Writer(BufferedWriter writer)
	{
		this.writer = writer;
	}

	public void write(String operate, String arg1, String arg2) throws Exception
	{
		writer.write(operate + " " + arg1 + " " + arg2);
		writer.newLine();
	}

	public void write(String operate, String arg1) throws Exception
	{
		writer.write(operate + " " + arg1);
		writer.newLine();
	}
	
	public void write(String operate) throws Exception
	{
		writer.write(operate);
		writer.newLine();
	}
}
