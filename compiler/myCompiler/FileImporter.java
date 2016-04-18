package myCompiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
/**
 * Import the file data into the end of the object file
 * @author YLonely
 *
 */
public class FileImporter
{

	private BufferedReader buffReader = null;
	private BufferedWriter buffWriter = null;
	private FileWriter fileWriter = null;
	private FileReader fileReader = null;
	private File filein = null;

	@SuppressWarnings("unused")
	private FileImporter()
	{

	}

	public FileImporter(String inPath, String outPath)
	{
		filein = new File(inPath);
		try
		{
			fileReader = new FileReader(filein);
			fileWriter = new FileWriter(outPath, true);
			buffReader = new BufferedReader(fileReader);
			buffWriter = new BufferedWriter(fileWriter);
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void stop()
	{
		try
		{
			buffReader.close();
			fileReader.close();
			buffWriter.close();
			fileWriter.close();
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void fileImport()
	{
		String line = null;
		try
		{
			while ((line = buffReader.readLine()) != null)
			{
				buffWriter.write(line);
				buffWriter.newLine();
			}
		} catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}	
		this.stop();
	}
}
