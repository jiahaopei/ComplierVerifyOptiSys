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
    	Node b = (Node) obj;
    	if (b.getLabel() != null && b.getLabel().trim().length() != 0) {
    		return b.getLabel().equals(label);
    	} else {
    		return false;
    	}
    	
    }
    
}
