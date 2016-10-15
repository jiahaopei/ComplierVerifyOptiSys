package cn.edu.buaa.gui;

public class Node {
	private String value;
	private String label;
	
	public Node(String value) {
    	this(value, "");
	}
	
    public Node(String value, String label) {
    	this.value = value.trim();
    	this.label = label.trim();
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value.trim();
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label.trim();
	}

	// 重点在toString，节点的显示文本就是toString
    public String toString() {
    	if (label != null && label.trim().length() != 0) {
    		return value + "        // " + label;
    	}
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
    	Node bNode = (Node) obj;
    	if (label == null || label.trim().length() == 0) {
    		return false;
    	} else if (bNode.getLabel() == null || bNode.getLabel().trim().length() == 0) {
    		return false;
    	} else {
        	String a = label.contains("_") ?  
        			label.substring(0, label.lastIndexOf("_")) : label;
        	String b = bNode.getLabel().contains("_") ? 
        			bNode.getLabel().substring(0, bNode.getLabel().lastIndexOf("_")) : bNode.getLabel();
        	return a.equals(b);
    	}
    	
    }
    
}
