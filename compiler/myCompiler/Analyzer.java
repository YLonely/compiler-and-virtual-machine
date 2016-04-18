package myCompiler;

import java.io.File;

public class Analyzer
{
	private String inPath = null;
	private String outPath = null;

	@SuppressWarnings("unused")
	private Analyzer()
	{
	}

	public Analyzer(String filePath)
	{
		inPath = filePath;
	}

	/**
	 * the toppest driver of the compiler
	 * 
	 * @throws Exception
	 */
	public void analyse() throws Exception
	{
		File fileIn = new File(inPath);
		String[] a = fileIn.getName().split("\\.");
		String fileName = a[0];
		String fileParentPath = fileIn.getParent();

		outPath = fileParentPath + "\\" + "out2";

		SingleLine single = new SingleLine(fileIn, fileParentPath + "\\" + "out");
		single.toSingleLine();
		
		SymbolTable.putFunction("print");// avoid the exception in compiling
		FunctionCounter counter = new FunctionCounter(fileParentPath + "\\" + "out");
		counter.count();
		
		Tokenizer tokenizer = new Tokenizer(fileParentPath + "\\" + "out");
		CompilationEngine engine = new CompilationEngine(outPath, tokenizer);
		engine.analyse();
		
		FileImporter importer = new FileImporter("resource/print.fuck", outPath);
		importer.fileImport();
		
		LabelSwitcher switcher = new LabelSwitcher(outPath, fileParentPath + "\\" + fileName + ".fuck");
		switcher.labelSwitch();
		
		System.out.println("Finish!");
	}

}
