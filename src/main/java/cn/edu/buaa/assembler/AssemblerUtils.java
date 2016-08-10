package cn.edu.buaa.assembler;

import cn.edu.buaa.constant.AssemblerDefine;

public class AssemblerUtils {

	// 判断是是否是一种语法类型
	public static boolean isSentenceType(String type) {
		for (String str : AssemblerDefine.SENTENCE_TYPES) {
			if (type.equals(str)) {
				return true;
			}
		}
		
		return false;
	}
	
}
