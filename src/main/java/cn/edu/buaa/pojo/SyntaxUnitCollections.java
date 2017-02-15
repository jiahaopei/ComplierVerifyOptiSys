package cn.edu.buaa.pojo;

public class SyntaxUnitCollections {

	private SyntaxUnitNode root;

	private SyntaxUnitNode current;
	
	public SyntaxUnitCollections() {
		
	}
	
	public SyntaxUnitCollections(SyntaxUnitNode root) {
		this.root = root;
	}
	
	public SyntaxUnitNode getRoot() {
		return root;
	}

	public void setRoot(SyntaxUnitNode root) {
		this.root = root;
	}

	public SyntaxUnitNode getCurrent() {
		return current;
	}

	public void setCurrent(SyntaxUnitNode current) {
		this.current = current;
	}

	// 添加一个子节点
	public void addChildNode(SyntaxUnitNode newNode, SyntaxUnitNode father) {
		if (null == father) {
			father = current;
		}

		// 认祖归宗
		newNode.setFather(father);
		if (null == father.getFirstSon()) {
			father.setFirstSon(newNode);
		} else {
			SyntaxUnitNode currentNode = father.getFirstSon();
			while (null != currentNode.getRight()) {
				currentNode = currentNode.getRight();
			}
			currentNode.setRight(newNode);
			newNode.setLeft(currentNode);
		}
		current = newNode;
	}

	// 交换
	public void switchTwoSubTree(SyntaxUnitNode left, SyntaxUnitNode right) {
		SyntaxUnitNode left_left = left.getLeft();
		SyntaxUnitNode right_right = right.getRight();
		left.setLeft(right);
		left.setRight(right_right);
		right.setLeft(left_left);
		right.setRight(left);

		if (left_left != null) {
			left_left.setRight(right);
		}
		
		if (right_right != null) {
			right_right.setLeft(left);
		}
	}

}
