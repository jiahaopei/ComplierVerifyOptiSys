package cn.edu.buaa.gui;

public class Node {
	private String value;
	private String label;
	private boolean flag;	// flag为false时才显示标号

	public Node(String value) {
    	this(value, "");
	}
	
    public Node(String value, String label) {
    	this(value, label, false);
	}
    
    public Node(String value, String label, boolean flag) {
    	this.value = value.trim();
    	this.label = label.trim();
    	this.flag = flag;
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
	
	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	// 重点在toString，节点的显示文本就是toString
    public String toString() {
    	if (!flag && label != null && label.trim().length() != 0) {
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
    	} else if (label.equals(bNode.getLabel())) {
    		return true;
    	} else {
    		String[] as = label.split(",");
    		String[] bs = bNode.getLabel().split(",");
    		for (int i = 0; i < as.length; i++) {
    			if (as[i] == null || as[i].trim().length() == 0) {
    				continue;
    			}
    			for (int j = 0; j < bs.length; j++) {
    				if (bs[j] == null || bs[j].trim().length() == 0) {
    					continue;
    				}
    				String a = as[i].contains("_") ?
    	        			as[i].substring(0, as[i].lastIndexOf("_")) : as[i];
    	        	String b = bs[j].contains("_") ? 
    	        			bs[j].substring(0, bs[j].lastIndexOf("_")) : bs[j];
    				if (a.trim().equals(b.trim())) return true;
    			}
    		}
        	return false;
    	}
    }
    
}
