package cn.edu.buaa.pojo;

public class Item {
	
	private String premise; 	// 存储前提
	private String left; 		// 存储等于号左边
	private String right;		// 存储等于号右边
	
	public Item() {
	}

	public Item(String premise, String left, String right) {
		this.premise = premise;
		this.left = left;
		this.right = right;
	}

	public String getPremise() {
		return premise;
	}
	
	public void setPremise(String premise) {
		this.premise = premise;
	}
	
	public String getLeft() {
		return left;
	}
	
	public void setLeft(String left) {
		this.left = left;
	}
	
	public String getRight() {
		return right;
	}
	
	public void setRight(String right) {
		this.right = right;
	}
	
	@Override
	public String toString() {
		String str = "";
		if(premise != null) str += premise + " -> ";
		str += left;
		if(right != null) str += " = " + right;
		return str;
	}
	
}
