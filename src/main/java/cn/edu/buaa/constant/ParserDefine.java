package cn.edu.buaa.constant;

import java.util.HashMap;
import java.util.Map;

public class ParserDefine {

	public static final String[] CONTROL_WORDS = { "if", "for", "while", "do", "else" };

	public static final String[] INNER_DATAT_YPES = { "int", "float", "double", "char", "void" };

	// 运算符优先级
	public static final Map<String, Integer> OPERATOR_PRIORITY = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 1L;
		{
			put(">", 0);
			put("<", 0);
			put(">=", 0);
			put("<=", 0);
			put("==", 0);
			put("+", 1);
			put("-", 1);
			put("*", 2);
			put("/", 2);
			put("%", 2);
			put("++", 3);
			put("--", 3);
			put("!", 3);
		}
	};
	
	// 运算符
	public static final String[] OPERATORS = {
		"=", "&", "<", ">", "++", "--", "+", "-", "*", "/", "%", ">=", "<=", "!=", "=="
	};
}
