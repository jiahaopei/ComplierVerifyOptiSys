package cn.edu.buaa.pojo;

import java.util.Map;

public class SyntaxUnitNode {

	// 节点的值，为文法中的终结符或者非终结符
	private String value;
	// 记录某些token的类型
	private String type;
	// 语义分析中记录关于token的其他一些信息，比如关键字是变量，该变量类型为int
	private Map<String, String> extraInfo;
	// token的标号
	private String label;
	
	private SyntaxUnitNode father;
	private SyntaxUnitNode left;
	private SyntaxUnitNode right;
	private SyntaxUnitNode firstSon;
	
	public SyntaxUnitNode(String value) {
		this(value, null, null);
	}
	
	public SyntaxUnitNode(String value, String type, String label) {
		this(value, type, null, label);
	}
	
	public SyntaxUnitNode(String value, String type, Map<String, String> extraInfo, String label) {
		this.value = value;
		this.type = type;
		this.extraInfo = extraInfo;
		this.label = label;
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
	
	public Map<String, String> getExtraInfo() {
		return extraInfo;
	}
	
	public void setExtraInfo(Map<String, String> extraInfo) {
		this.extraInfo = extraInfo;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public SyntaxUnitNode getFather() {
		return father;
	}
	
	public void setFather(SyntaxUnitNode father) {
		this.father = father;
	}
	public SyntaxUnitNode getLeft() {
		return left;
	}
	
	public void setLeft(SyntaxUnitNode left) {
		this.left = left;
	}
	
	public SyntaxUnitNode getRight() {
		return right;
	}
	
	public void setRight(SyntaxUnitNode right) {
		this.right = right;
	}
	
	public SyntaxUnitNode getFirstSon() {
		return firstSon;
	}
	
	public void setFirstSon(SyntaxUnitNode firstSon) {
		this.firstSon = firstSon;
	}
	
	@Override
	public String toString() {
		return value + ", " + type + ", " + label;
	}
}
