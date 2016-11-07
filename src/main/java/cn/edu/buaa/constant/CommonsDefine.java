package cn.edu.buaa.constant;

public class CommonsDefine {

	public static final String OUTPUT_PATH = "output/";
	public static final String DEBUG_PATH = "debug/";
	public static final String[] names = { "if", "if_else", "while", "do_while", "for" };
	
	/**
	 * 支持到了C99，也包括C11的新增的库
	 */
	public static final String[] LIBS = {
		"assert.h", "complex.h", "ctype.h", "errno.h", "fenv.h", "float.h", 
		"inttypes.h", "ios646.h", "limits.h", "locale.h", "math.h", "setjmp.h", 
		"signal.h", "stdalign.h", "stdarg.h", "stdatomic.h", "stdbool.h", 
		"stddef.h", "stdint.h", "stdio.h", "stdlib.h", "stdnoreturn.h", 
		"string.h", "tgmath.h", "threads.h", "time.h", "uchar.h", "wchar.h",  "wctype.h",
	};

}
