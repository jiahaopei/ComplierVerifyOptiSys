package cn.edu.buaa.pojo;

public class SyntaxUnitCollections {

	// 树的根节点
	private SyntaxUnitNode root;
	// 现在遍历到的节点
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
		// 未指定父节点，就设为当前节点
		if (null == father) {
			father = current;
		}

		// 认祖归宗
		newNode.setFather(father);
		// 如果father节点没有儿子，则将其赋值为其第一个儿子
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

	// 交换相邻的两棵兄弟子树
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
