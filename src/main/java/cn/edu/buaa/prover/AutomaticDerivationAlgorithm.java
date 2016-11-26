package cn.edu.buaa.prover;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.buaa.pojo.Item;
import cn.edu.buaa.pojo.Proposition;

/**
 * 自动推理算法封装类
 * 
 * @author destiny
 *
 */
public class AutomaticDerivationAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(AutomaticDerivationAlgorithm.class);
	
	// 记录两个命题时候发生了关联
	private static boolean flag;
	
	public static DerivationDTO process(List<Proposition> srcPropositions) {
		
		logger.info("AutomaticDerivationAlgorithm.process");
		
		List<Proposition> propositions = new ArrayList<>();
		for (Proposition proposition : srcPropositions) {
			Proposition tmp = ProverHelper.cloneProposition(proposition);
			propositions.add(tmp);
		}
		
		List<String> proves = new ArrayList<>();
		List<String> proofs  = new ArrayList<>();
		int step = 1;
		String line = "";
		List<Proposition> newPropositions = new ArrayList<>();
		for (int i = 0; i < propositions.size(); i++) {
			boolean isDeleteP = false;
			boolean allFlag = false;
			Proposition p = propositions.get(i);
			String pstr = p.toStr();
			for (int j = 0; j < newPropositions.size(); j++) {
				flag = false;
				Proposition q = newPropositions.get(j);
				String qstr = q.toStr();
				isDeleteP = applyDerivationRuleToTwoPropositions(p, q);
				if (q.size() == 0) {
					newPropositions.remove(j);
					j--;
				}
				
				// 两个命题相关联
				if (flag) {
					if (q.getProof().contains("P")) {
						line = "S" + step + " = " + qstr;
						proves.add(line);
						proofs.add(q.getProof());						
						q.setProof("S" + step);
						step++;
					}
					if (p.getProof().contains("P")) {
						line = "S" + step + " = " + pstr;
						proves.add(line);
						proofs.add(p.getProof());
						p.setProof("S" + step);
						step++;
					}
					Proposition t = p;
					if (t.toStr().equals(pstr)) {
						t = q;
					}
					line = "S" + step + " = " + t.toStr();
					proves.add(line);
					proofs.add(q.getProof() + "," + p.getProof() + ",MP");
					t.setProof("S" + step);
					step++;
					allFlag = true;
				}
			}
			if (!allFlag) {
				line = "S" + step + " = " + p.toStr();
				proves.add(line);
				proofs.add(p.getProof());
				p.setProof("S" + step);
				step++;
			}
			if (!isDeleteP && p.size() != 0) {
				newPropositions.add(p);
			}
		}
		
		// 实现CI
		String[] lines = conjunction(newPropositions);
		line = "S" + step + " = " + lines[0];
		proves.add(line);
		proofs.add(lines[1]);
		step++;
		
		// 化简
		List<Proposition> tmps = SemantemeObtainAlgorithm.obtainSemantemeFromProposition(newPropositions);
		lines = conjunction(tmps);
		line = "S" + step + " = " + lines[0];
		proves.add(line);
		proofs.add("S" + (step - 1) + ", REDUCE");
		step++;
		
		// 推导语义
		List<Proposition> sigmaSemantemes = SemantemeObtainAlgorithm.standardSemantemes(tmps);
		lines = conjunction(sigmaSemantemes);
		line = "S" + step + " = " + lines[0];
		proves.add(line);
		proofs.add("S" + (step - 1) + ", σ");
		step++;
		
		DerivationDTO dto = new DerivationDTO();
		dto.setSemantemeSet(sigmaSemantemes);
		dto.setProves(proves);
		dto.setProofs(proofs);
		dto.setStep(step);
		
		return dto;
	}
	
	
	public static String[] conjunction(List<Proposition> newPropositions) {
		String[] strs = new String[2];
		String pf = "";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < newPropositions.size(); i++) {
			if (i != 0) {
				sb.append(" ∧ ");
				pf += ", ";
			}
			sb.append("(" + newPropositions.get(i).toStr() + ")");
			pf += newPropositions.get(i).getProof();
			
		}
		pf += ", CI";
		strs[0] = sb.toString();
		strs[1] = pf;
		
		return strs;
	}

	// false : 不删除pl
	// left = right
	// left = right
	private static boolean solveItems1(List<Item> pl, List<Item> ql) {
//		System.out.println(1);
		
		// 左边相等
		if (pl.get(0).getLeft().equals(ql.get(0).getLeft())) {
			flag = true;
			ql.clear();
			return false;
		}

		// p右边包括q的左边
		if (pl.get(0).getRight().contains(ql.get(0).getLeft())) {
			flag = true;
			String right = pl.get(0).getRight().replace(ql.get(0).getLeft(), ql.get(0).getRight());
			pl.get(0).setRight(right);
			return false;
		}

		// ！q的右边包括p的左边（此种情况应该不存在）
		if (ql.get(0).getRight().contains(pl.get(0).getLeft())) {
			flag = true;
			String right = ql.get(0).getRight().replace(pl.get(0).getLeft(), pl.get(0).getRight());
			ql.get(0).setRight(right);
			return false;
		}

		return false;
	}
	
	// premise left = right  : pl
	// left = right          : ql
	private static boolean solveItems2(List<Item> pl, List<Item> ql) {
//		System.out.println(2);	
		
		// 选定一个前提
		String premise = ql.get(0).getLeft() + " == " + ql.get(0).getRight();
		for (Item item : pl) {
			if (premise.equals(item.getPremise())) {
				flag = true;
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
				flag = true;
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
	 * GPR[0] > 0 -> CR[7] = b100 
	 * GPR[0] == 0 -> CR[8] = b100
	 * 
	 * CR[0] = 22
	 */
	private static boolean solveItems3(List<Item> pl, List<Item> ql) {
//		System.out.println(3);

		for (Item item : ql) {
			if (item.getLeft().equals(pl.get(0).getLeft())) {
				flag = true;
				item.setRight(pl.get(0).getRight());
			} else if (item.getRight().contains(pl.get(0).getLeft())) {
				flag = true;
				String right = item.getRight().replace(pl.get(0).getLeft(), pl.get(0).getRight());
				item.setRight(right);
			}
		}

		return false;

	}

	private static boolean solveItems4(List<Item> pl, List<Item> ql) {
//		System.out.println(4);

		int cnt = 0;
		for (Item itemQ : ql) {
			for (Item itemP : pl) {
				if (itemP.getPremise().equals(itemQ.getLeft() + " == " + itemQ.getRight())) {
					flag = true;
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
			
			// premise left = right : p
			// left = right : q
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
