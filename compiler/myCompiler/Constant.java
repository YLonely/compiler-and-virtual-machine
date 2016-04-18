package myCompiler;

public class Constant
{

	public static final String[] KEYWORDS =
	{ "function", "static", "if", "else", "while", "return", "from" };

	public static final String[] SYMBOLS =
	{ "{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "|", "<", ">", "=", "~", "++", "--", ">=",
			"<=", "&&", "||", "==" };

	public static final int STATIC_CONTAINS = 0;
	public static final int LOCAL_CONTAINS = 1;
	public static final int NONE_CONTAINS = -1;

}
