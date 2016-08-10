package cn.edu.buaa.prover;

import java.util.ArrayList;
import java.util.List;

import cn.edu.buaa.pojo.Item;
import cn.edu.buaa.pojo.Proposition;

/**
 * 自动推理算法封装类
 * 
 * @author destiny
 *
 */
public class AutomaticDerivationAlgorithm {

	public static List<Proposition> process(List<Proposition> srcPropositions) {
		
		List<Proposition> propositions = new ArrayList<>();
		for (Proposition proposition : srcPropositions) {
			Proposition tmp = ProverHelper.cloneProposition(proposition);
			propositions.add(tmp);
		}
		
		List<Proposition> newPropositions = new ArrayList<>();
		for (int i = 0; i < propositions.size(); i++) {
			boolean isDeleteP = false;
			Proposition p = propositions.get(i);
			for (int j = 0; j < newPropositions.size(); j++) {
				Proposition q = newPropositions.get(j);
				isDeleteP = applyDerivationRuleToTwoPropositions(p, q);
				if (q.size() == 0) {
					newPropositions.remove(j);
					j--;
				}
			}
			if (!isDeleteP && p.size() != 0) {
				newPropositions.add(p);
			}

		}

		return newPropositions;
	}

	// false : 不删除pl
	// left = right
	// left = right
	private static boolean solveItems1(List<Item> pl, List<Item> ql) {
		// 左边相等
		if (pl.get(0).getLeft().equals(ql.get(0).getLeft())) {
			ql.clear();
			return false;
		}

		// p右边包括q的左边
		if (pl.get(0).getRight().contains(ql.get(0).getLeft())) {
			String right = pl.get(0).getRight().replace(ql.get(0).getLeft(), ql.get(0).getRight());
			pl.get(0).setRight(right);
			return false;
		}

		// ！q的右边包括p的左边（此种情况应该不存在）
		if (ql.get(0).getRight().contains(pl.get(0).getLeft())) {
			String right = ql.get(0).getRight().replace(pl.get(0).getLeft(), pl.get(0).getRight());
			ql.get(0).setRight(right);
			return false;
		}

		return false;
	}

	// left = right
	// premise left = right
	private static boolean solveItems2(List<Item> pl, List<Item> ql) {

		// 选定一个前提
		String premise = ql.get(0).getLeft() + " == " + ql.get(0).getRight();
		for (Item item : pl) {
			if (premise.equals(item.getPremise())) {
				Item e = new Item(null, item.getLeft(), item.getRight());
				pl.clear();
				pl.add(e);
				solveItems1(pl, ql);
				return false;
			}
		}

		// pl前提包括ql
		for (Item item : pl) {
			if (item.getPremise().contains(ql.get(0).getLeft())) {
				String tmp = item.getPremise().replace(ql.get(0).getLeft(), ql.get(0).getRight());
				item.setPremise(tmp);
			}
		}
		ql.clear();

		return false;
	}

	/*
	 * left = right 
	 * premise left = right 
	 * 如： 
	 * GPR[0] < 0 -> CR[7] = b100 
	 * GPR[0] > 0 -> CR[7] = b100 GPR[0] == 0 -> CR[8] = b100
	 * 
	 * CR[0] = 22
	 */
	private static boolean solveItems3(List<Item> pl, List<Item> ql) {

		for (Item item : ql) {
			if (item.getLeft().equals(pl.get(0).getLeft())) {
				item.setRight(pl.get(0).getRight());
			} else if (item.getRight().contains(pl.get(0).getLeft())) {
				String right = item.getRight().replace(pl.get(0).getLeft(), pl.get(0).getRight());
				item.setRight(right);
			}
		}

		return false;

	}

	private static boolean solveItems4(List<Item> pl, List<Item> ql) {

		int cnt = 0;
		for (Item itemQ : ql) {
			for (Item itemP : pl) {
				if (itemP.getPremise().equals(itemQ.getLeft() + " == " + itemQ.getRight())) {
					cnt++;
					itemQ.setLeft(itemP.getLeft());
					itemQ.setRight(itemP.getRight());
				}
			}
		}

		if (cnt == ql.size()) {
			return true;
		} else {
			return false;
		}

	}

	// p : 为待加入的； q : 为已加入的
	private static boolean applyDerivationRuleToTwoPropositions(Proposition p, Proposition q) {

		if (!checkBoundary(p, q))
			return false;

		boolean isDeleteP = false;

		// left = right
		// left = right
		if (p.getItems().get(0).getPremise() == null && q.getItems().get(0).getPremise() == null) {
			if (solveItems1(p.getItems(), q.getItems())) {
				isDeleteP = true;
			}

			// left = right : q
			// premise left = right : p
		} else if (p.getItems().get(0).getPremise() != null && q.getItems().get(0).getPremise() == null) {
			if (solveItems2(p.getItems(), q.getItems())) {
				isDeleteP = true;
			}

			// premise left = right
			// left = right
		} else if (p.getItems().get(0).getPremise() == null && q.getItems().get(0).getPremise() != null) {
			if (solveItems3(p.getItems(), q.getItems())) {
				isDeleteP = true;
			}

			// premise left = right
			// premise left = right
		} else {
			if (solveItems4(p.getItems(), q.getItems())) {
				isDeleteP = true;
			}

		}

		return isDeleteP;
	}

	private static boolean checkBoundary(Proposition p, Proposition q) {

		if (p.size() == 0) {
			return false;
		}
		if (q.size() == 0) {
			return false;
		}

		if (p.getItems().get(0).getPremise() == null && p.getItems().get(0).getLeft() == null
				&& p.getItems().get(0).getRight() == null) {
			return false;
		}
		if (q.getItems().get(0).getPremise() == null && q.getItems().get(0).getLeft() == null
				&& q.getItems().get(0).getRight() == null) {
			return false;
		}

		if (p.getItems().get(0).getPremise() == null && p.getItems().get(0).getRight() == null) {
			return false;
		}
		if (q.getItems().get(0).getPremise() == null && q.getItems().get(0).getRight() == null) {
			return false;
		}

		if (p.getItems().get(0).getPremise() == null && p.getItems().get(0).getLeft().equals("PC")) {
			return false;
		}
		if (q.getItems().get(0).getPremise() == null && q.getItems().get(0).getLeft().equals("PC")) {
			return false;
		}

		return true;
	}

}
