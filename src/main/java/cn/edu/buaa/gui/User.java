package cn.edu.buaa.gui;

public class User {
	private String name;
	 
    public User(String name) {
        this.name = name;
    }
 
    public String getName() {
		return name;
	}
    
	public void setName(String name) {
		this.name = name;
	}
	
	// 重点在toString，节点的显示文本就是toString
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object obj) {
    	User b = (User) obj;
    	return b.getName().equals(name);
    }
}
