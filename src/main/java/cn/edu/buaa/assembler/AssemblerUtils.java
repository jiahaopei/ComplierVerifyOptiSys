package cn.edu.buaa.assembler;

import cn.edu.buaa.constant.AssemblerDefine;
import cn.edu.buaa.lexer.LexerUtils;

public class AssemblerUtils {
	
	public static final int WIDTH = 40;
	public static final String PREFIX = "\t";
	
	// 判断是是否是一种语法类型
	public static boolean isSentenceType(String type) {
		for (String str : AssemblerDefine.SENTENCE_TYPES) {
			if (type.equals(str)) {
				return true;
			}
		}
		
		return false;
	}
	
	// 统一生成汇编代码中的label
	public static String generateLabel(String str, String msg) {
		str = LexerUtils.RTrim(str);
		if (!str.startsWith(PREFIX)) {
			str += PREFIX;
		}
		StringBuilder sb = new StringBuilder(str);
		for (int i = str.length(); i < WIDTH; i++) {
			sb.append(" ");
			
		}
		sb.append("# " + msg);
		
		return sb.toString();
	}
}
