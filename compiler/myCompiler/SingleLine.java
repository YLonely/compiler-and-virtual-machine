package myCompiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SingleLine
{

	private BufferedReader buffReader = null;
	private BufferedWriter buffWriter = null;
	private FileWriter fileWriter = null;
	private FileReader fileReader = null;
	private File fileout = null;
	private int[] keyWords =
	{ 32, 123, 125, 40, 41, 91, 93, 46, 44, 59, 43, 45, 42, 47, 38, 124, 60, 62, 61, 126 };

	@SuppressWarnings("unused")
	private SingleLine()
	{
	}

	public SingleLine(File filein, String outPath)
	{
		fileout = new File(outPath);
		try
		{
			fileReader = new FileReader(filein);
			buffReader = new BufferedReader(fileReader);

			fileWriter = new FileWriter(fileout);
			buffWriter = new BufferedWriter(fileWriter);
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void toSingleLine() throws IOException
	{
		int currentChar = 0;
		int nextChar = 0;
		String currentWords = "";
		boolean stepOver = false;
		while (true)
		{
			// System.out.print((int) ',');
			if (!stepOver)
			{
				currentChar = buffReader.read();
			} else
			{
				stepOver = false;
			}
			if (currentChar == -1)
			{
				break;
			}
			if (currentChar != 10 && currentChar != 13)
			{
				if (match(currentChar))
				{
					if (!currentWords.equals("") && !currentWords.matches("\"[^\"]{0,}"))
					{
						buffWriter.write(currentWords);
						buffWriter.newLine();
						currentWords = "";
					}
					if (currentChar != 32 && !currentWords.matches("\"[^\"]{0,}"))
					{
						nextChar = buffReader.read();
						if (currentChar == 93)
						{
							buffWriter.write(currentChar);
							buffWriter.newLine();
							currentChar = nextChar;
							stepOver = true;
							continue;
						}

						if ((currentChar == 43 && nextChar == 43) || (currentChar == 45 && nextChar == 45)
								|| (currentChar == 62 && nextChar == 61) || (currentChar == 60 && nextChar == 61)
								|| (currentChar == 124 && nextChar == 124) || (currentChar == 38 && nextChar == 38)
								|| (currentChar == 61 && nextChar == 61))
						{
							buffWriter.write(currentChar);
							currentChar = 0;
							buffWriter.write(nextChar);
							// nextChar = 0;
							buffWriter.newLine();
						} else
						{
							buffWriter.write(currentChar);
							currentChar = 0;
							buffWriter.newLine();
							if (match(nextChar) && nextChar != 32 && nextChar != 9)
							{
								buffWriter.write(nextChar);
								// nextChar = 0;
								buffWriter.newLine();
							} else if (nextChar != 10 && nextChar != 13 && nextChar != 32 && nextChar != -1
									&& nextChar != 9)
							{
								currentWords = currentWords + (char) nextChar;
							}
						}
						// buffWriter.newLine();
					}
				}
				if (((currentChar != 32 && !currentWords.matches("\"[^\"]{0,}")) || currentWords.matches("\"[^\"]{0,}"))
						&& currentChar != 0 && currentChar != 9)
				{
					currentWords = currentWords + (char) currentChar;
				}
			}
		}
		if (!currentWords.equals(""))
		{
			// buffWriter.newLine();
			buffWriter.write(currentWords);
		}

		buffReader.close();
		fileReader.close();

		buffWriter.close();
		fileWriter.close();

	}

	private boolean match(int currentChar)
	{
		boolean match = false;
		for (int i = 0; i < keyWords.length; i++)
		{
			if (currentChar == keyWords[i])
			{
				match = true;
				return match;
			}
		}
		return match;
	}

}
