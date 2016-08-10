package cn.edu.buaa.pojo;

import cn.edu.buaa.constant.LexerDefine;

/**
 * 记录分析出来的单词
 * @author destiny
 *
 */
public class Token {
	
	private String value;
	private String type;
	private String label;
	
	public Token() {
		
	}
	
	public Token(String type, String value, String label) {
		this.type = type;
		this.value = value;
		this.label = label;
	}

	public Token(int type_index, String value, String label) {
		if(type_index == 0 || type_index == 3 || type_index == 4) {
			this.type = LexerDefine.DETAIL_TOKEN_STYLE.get(value);
		} else {
			this.type = LexerDefine.TOKEN_STYLE[type_index];
		}
		this.value = value;
		this.label = label;
	}
	
	public Token(int type_index, char value, String label) {
		this(type_index, value + "", label);
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
}
