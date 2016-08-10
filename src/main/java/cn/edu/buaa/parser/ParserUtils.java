package cn.edu.buaa.parser;

import cn.edu.buaa.constant.ParserDefine;

public class ParserUtils {
	
	// 判断是否是控制跳转的关键字
	public static boolean isControl(String tokenValue) {
		for (String word : ParserDefine.CONTROL_WORDS) {
			if (word.equals(tokenValue)) {
				return true;
			}
		}
		return false;
	}
	
	// 判断是否是内置的数据类型
	public static boolean isInnerDataType(String tokenValue) {
		for (String word : ParserDefine.INNER_DATAT_YPES) {
			if (word.equals(tokenValue)) {
				return true;
			}
		}
		return false;
	}
	
	// 判断是否是运算符
	public static boolean isOperator(String word) {
		for (String str : ParserDefine.OPERATORS) {
			if (str.equals(word)) {
				return true;
			}
		}
		return false;
	}
	
}
