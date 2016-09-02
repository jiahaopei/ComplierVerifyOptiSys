package cn.edu.buaa.pojo;

import java.util.List;

public class Proposition {
	
	private List<Item> items;		// 每个命题可能包含多项
	private String proof;

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
	
	public String getProof() {
		return proof;
	}

	public void setProof(String proof) {
		this.proof = proof;
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
	
	public String toStr() {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < items.size(); i++) {
			sb.append(items.get(i).toString());
			if (i < items.size() - 1) {
				sb.append(" || ");
			}
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || !(obj instanceof Proposition)) return false;
		
		Proposition proposition = (Proposition) obj;
		if (!this.toStr().equals(proposition.toStr())) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return this.toStr().hashCode();
	}
}
