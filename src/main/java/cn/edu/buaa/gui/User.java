package cn.edu.buaa.gui;

public class User {
	private String name;
	 
    public User(String n) {
        name = n;
    }
 
    // 重点在toString，节点的显示文本就是toString
    public String toString() {
        return name;
    }
}
