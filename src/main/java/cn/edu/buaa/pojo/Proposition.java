package cn.edu.buaa.pojo;

import java.util.List;

public class Proposition {
	
	private List<Item> items;		// 每个命题可能包含多项
	
	public Proposition() {
	}
	
	public Proposition(List<Item> items) {
		this.items = items;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public int size() {
		return items.size();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(Item item : items) {
			sb.append(item.toString()).append("\n");
		}
		return sb.toString();
	}
	
}
