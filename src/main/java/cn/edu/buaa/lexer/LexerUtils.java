package cn.edu.buaa.lexer;

import cn.edu.buaa.constant.LexerDefine;

public class LexerUtils {
	
	// 跳过空字符
	public static int skipBlank(int i, String line) {
		while (i < line.length() && isBlank(i, line)) {
			i++;
		}

		return i;
	}
	
	// 是否是空字符
	public static boolean isBlank(int i, String line) {
		char ch = line.charAt(i);
		return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
	}

	// 判断是否是分隔符
	public static boolean isDelimiter(String word) {
		for (String str : LexerDefine.delimiters) {
			if (str.equals(word)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isDelimiter(char ch) {
		return isDelimiter(ch + "");
	}

	// 判断是否是运算符
	public static boolean isOperator(String word) {
		for (String str : LexerDefine.operators) {
			if (str.equals(word)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isOperator(char ch) {
		return isOperator(ch + "");
	}

	// 判断是否是关键字
	public static boolean isKeyword(String word) {
		for (int i = 0; i < LexerDefine.keywords.length; i++) {
			for (int j = 0; j < LexerDefine.keywords[i].length; j++) {
				if (word.equals(LexerDefine.keywords[i][j])) {
					return true;
				}
			}
		}
		return false;
	}
	
	// 去掉字符串右边的空字符
	public static String RTrim(String str) {
		int i = str.length() - 1;
		while (i >= 0) {
			if (str.charAt(i) != ' ' || str.charAt(i) != '\t' || str.charAt(i) != '\n') {
				break;
			}
		}

		return str.substring(0, i + 1);
	}

}
