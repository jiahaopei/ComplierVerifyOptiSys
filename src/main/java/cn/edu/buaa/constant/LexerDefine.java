package cn.edu.buaa.constant;

import java.util.Map;
import java.util.HashMap;

public class LexerDefine {

	// token比较大的分类
	public static final String[] TOKEN_STYLE = 
		{ "KEY_WORD", "IDENTIFIER", "DIGIT_CONSTANT", "OPERATOR", "SEPARATOR", "STRING_CONSTANT" };

	// 将关键字、运算符、分隔符进行具体化
	public static final Map<String, String> DETAIL_TOKEN_STYLE = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("include", "INCLUDE");
			put("char", "CHAR");
			put("int", "INT");
			put("float", "FLOAT");
			put("double", "DOUBLE");
			put("for", "FOR");
			put("if", "IF");
			put("else", "ELSE");
			put("while", "WHILE");
			put("do", "DO");
			put("return", "RETURN");

			put("=", "ASSIGN");
			put("&", "ADDRESS");
			put("<", "LT");
			put(">", "GT");
			put(">=", "GET");
			put("<=", "LET");
			put("==", "EQU");
			put("+", "PLUS");
			put("-", "MINUS");
			put("*", "MUL");
			put("/", "DIV");
			put("%", "MOD");
			put("++", "SELF_PLUS");
			put("--", "SELF_MINUS");
			put("\"", "DOUBLE_QUOTE");

			put("#", "SHARP");
			put(",", "COMMA");
			put(";", "SEMICOLON");
			put("(", "LL_BRACKET");
			put(")", "RL_BRACKET");
			put("{", "LB_BRACKET");
			put("}", "RB_BRACKET");
			put("[", "LM_BRACKET");
			put("]", "RM_BRACKET");
		}
	};

	// 分隔符
	public static final String[] delimiters = { "(", ")", "{", "}", "[", "]", ",", "\"", ";" };

	// 运算符
	public static final String[] operators = { "=", "&", "<", ">", "++", "--", 
			"+", "-", "*", "/", "%", ">=", "<=", "!=", "==" };
	
	// 关键字
	public static final String[][] keywords = { 
			{ "int", "float", "double", "char", "void" },
			{ "if", "for", "while", "do", "else" }, 
			{ "include", "return" } 
	};
	
}
