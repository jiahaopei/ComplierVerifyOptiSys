package cn.edu.buaa.constant;

import java.util.List;
import java.util.ArrayList;

public class AssemblerDefine {

	public static final String[] SENTENCE_TYPES = { "Sentence", "Include", "FunctionStatement", "Statement",
			"FunctionCall", "Assignment", "Control", "Expression", "Return" };
	
	// 双目运算符
	public static final List<String> DOUBLE_OPERATORS = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("+");
			add("-");
			add("*");
			add("/");
			add("%");
			add(">");
			add("<");
			add(">=");
			add("<=");
			add("==");
		}
	};

	// 单目运算符
	public static final List<String> SINGLE_OPERATORS = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("++");
			add("--");
		}
	};

}
