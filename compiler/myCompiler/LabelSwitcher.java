package myCompiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Hashtable;
/**
 * Replace the label name with the index of line
 * @author Administrator
 *
 */
public class LabelSwitcher
{
	String inPath = null;
	String outPath = null;
	private File fileIn = null;
	private File fileOut = null;
	private FileWriter fileWriter = null;
	private BufferedWriter buffWriter = null;
	private FileReader fileReader = null;
	private BufferedReader buffReader = null;
	private Hashtable<String, Integer> labelTable = null;

	@SuppressWarnings("unused")
	private LabelSwitcher()
	{

	}

	public LabelSwitcher(String inPath, String outPath)
	{
		this.inPath = inPath;
		this.outPath = outPath;
		fileIn = new File(inPath);
		labelTable = new Hashtable<>();
	}

	private void labelCount()
	{
		String currentLine = null;
		int lineIndex = 0;
		try
		{
			fileReader = new FileReader(fileIn);
			buffReader = new BufferedReader(fileReader);
			while ((currentLine = buffReader.readLine()) != null)
			{
				String[] token = currentLine.split("\\s");
				if (token[0].equals("function") || token[0].equals("label"))
				{
					labelTable.put(token[1], lineIndex);
				}
				lineIndex++;
			}
			buffReader.close();
			fileReader.close();
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void labelSwitch()
	{
		this.labelCount();
		String line = null;
		fileOut = new File(outPath);
		try
		{
			fileReader = new FileReader(fileIn);
			buffReader = new BufferedReader(fileReader);
			fileWriter = new FileWriter(fileOut);
			buffWriter = new BufferedWriter(fileWriter);
			while ((line = buffReader.readLine()) != null)
			{
				String[] token = line.split("\\s");
				if (token[0].equals("call") || token[0].equals("goto") || token[0].equals("ifgoto"))
				{
					buffWriter.write(token[0] + " " + String.valueOf(labelTable.get(token[1])));
				} else
					buffWriter.write(line);
				buffWriter.newLine();
			}
			buffReader.close();
			fileReader.close();
			buffWriter.close();
			fileWriter.close();
			fileIn.delete();
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}

		
	}
}
