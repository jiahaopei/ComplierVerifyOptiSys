package cn.edu.buaa.prover;

import java.util.ArrayList;
import java.util.List;

import cn.edu.buaa.pojo.Item;
import cn.edu.buaa.pojo.Proposition;

public class SemantemeObtainAlgorithm {
	
	public static List<Proposition> standardSemantemes(List<Proposition> semantemes) {
		
		List<Proposition> standards = new ArrayList<>();
		
		for (Proposition proposition : semantemes) {
			Proposition tmp = ProverHelper.cloneProposition(proposition);
			applySigma(tmp);
			standards.add(tmp);
		}
		
		return standards;
	}

	private static void applySigma(Proposition tmp) {
		
		for (Item item : tmp.getItems()) {
			if (item.getPremise() == null 
					&& item.getLeft() != null && item.getRight() == null) {
				item.setLeft("σ(" + item.getLeft() + ")");
			} else if (item.getPremise() == null 
					&& item.getLeft() != null && item.getRight() != null) {
				item.setLeft("σ(" + item.getLeft());
				item.setRight(item.getRight() + ")");
			} else if (item.getPremise() != null 
					&& item.getLeft() == null && item.getRight() == null) {
				item.setLeft("skip");
			} else if (item.getPremise() != null
					&& item.getLeft() != null && item.getRight() == null) {
				String[] strs = item.getLeft().split(";");
				List<String> rights = new ArrayList<>();
				for (String str : strs) {
					if (str == null || str.trim().length() == 0) continue; 
					str = str.trim();
					rights.add(str);
				}
				
				if (rights.size() == 1) {
					item.setLeft("σ(" + item.getLeft() + ")");
				} else {
					StringBuilder sb = new StringBuilder(); 
					sb.append("[");
					sb.append("σ(" + rights.get(0) + ")");
					for (int i = 1; i < rights.size(); i++) {
						sb.append("; " + "σ(" + rights.get(i) + ")");
					}
					sb.append("]");
					item.setLeft(sb.toString());
				}
			} else if (item.getPremise() != null
					&& item.getLeft() != null && item.getRight() != null) {
				item.setLeft("σ(" + item.getLeft());
				item.setRight(item.getRight() + ")");
			}
		}
		
	}

	public static List<Proposition> obtainSemantemeFromProposition(List<Proposition> simplifiedPropositions) {
		
		// 复制一份新的
		List<Proposition> semantemes = new ArrayList<>();
		for (Proposition proposition : simplifiedPropositions) {
			Proposition tmp = ProverHelper.cloneProposition(proposition);
			if (tmp.size() == 3) {
				ProverHelper.reducePropositionOfThree(tmp);
			}
			semantemes.add(tmp);
		}
		
		// 首先处理单条的情况
		int ind = -1;
		for (int i = 0; i < semantemes.size(); i++) {
			if (semantemes.get(i).size() > 1) {
				break;
			}
			
			if (semantemes.get(i).getItems().get(0).getLeft().equalsIgnoreCase("PC")) {
				break;
			}
			
			if (semantemes.get(i).getItems().get(0).getLeft().matches("\\.L(\\d)+:")) {
				ind = i;
				continue;
			}
			
			if (ind >= 0) {
				Proposition tmp = ProverHelper.cloneProposition(semantemes.get(i));
				semantemes.add(ind, tmp);
				ind++;
				i++;
			}
		}
		
		// 从含有多条语句的命题开始处理
		for (int i = 0; i < semantemes.size(); i++) {
			Proposition pro = semantemes.get(i);
			if (pro.size() > 1) {
				solveMultiple(pro, i, semantemes);
			}
		}

		// 清理无用的命题
		for (int i = 0; i < semantemes.size(); i++) {
			Proposition pro = semantemes.get(i);
			if (pro.size() == 1) {
				if (pro.getItems().get(0).getPremise() == null && pro.getItems().get(0).getLeft() != null
						&& pro.getItems().get(0).getRight() == null
						&& pro.getItems().get(0).getLeft().matches("\\.L(\\d)+:")) {
					pro.getItems().clear();
				} else if (pro.getItems().get(0).getPremise() == null && pro.getItems().get(0).getLeft() != null
						&& pro.getItems().get(0).getRight() != null) {
					if (pro.getItems().get(0).getLeft().equals("PC")
							&& pro.getItems().get(0).getRight().contains("PC")) {
						pro.getItems().clear();
					}
				}
			}
			if (pro.size() == 0) {
				semantemes.remove(i);
				i--;
			}
		}

		return semantemes;
	}

	private static void solveMultiple(Proposition pro, int i, List<Proposition> semantemes) {

		for (int j = 0; j < pro.size(); j++) {
			Item item = pro.getItems().get(j);
			if (item.getLeft().equals("PC")) {
				List<Proposition> S = new ArrayList<>();
				// 左右两边含有PC
				if (item.getRight().contains("PC")) {
					if (item.getRight().contains("PC + 4")) {

						obtainsProposition(S, semantemes, i + 1, semantemes.size());

					} else if (item.getRight().contains("@")) {
						String address = item.getRight().substring(item.getRight().indexOf("@") + 1).trim();

						for (int k = 0; k < semantemes.size(); k++) {
							if (i == k)
								continue;
							if (semantemes.get(k).size() == 1
									&& semantemes.get(k).getItems().get(0).getLeft().contains(address)) {
								if (k < i) {
									obtainsProposition(S, semantemes, k + 1, i);
								} else {
									obtainsProposition(S, semantemes, k + 1, semantemes.size());
								}
								break;
							}
						}
					}
				}

				if (S.size() > 0) {
					item.setRight(null);
					StringBuffer sb = new StringBuffer();
					sb.append(S.get(0).getItems().get(0).toString());
					for (int k = 1; k < S.size(); k++) {
						sb.append("; " + S.get(k).getItems().get(0).toString());
					}

					item.setLeft(sb.toString().trim());

					for (int k = 0; k < S.size(); k++) {
						S.get(k).getItems().clear();
					}
				} else {
					item.setLeft(null);
					item.setRight(null);
				}
			}

		}
	}

	private static void obtainsProposition(List<Proposition> S, List<Proposition> semantemes, int i, int limit) {

		for (; i < limit; i++) {
			if (semantemes.get(i).size() == 1) {
				Item item = semantemes.get(i).getItems().get(0);
				if (item.getPremise() == null && item.getLeft() != null && item.getRight() == null) {
					if (item.getLeft().contains("<") && item.getLeft().contains(">")) {
						S.add(semantemes.get(i));
					} else {
						continue;
					}
				} else if (item.getPremise() == null && item.getLeft() != null && item.getRight() != null) {
					if (item.getLeft().equals("PC") && item.getRight().contains("PC")
							&& item.getRight().contains("@")) {
						String address = item.getRight().substring(item.getRight().indexOf("@") + 1).trim();

						for (int k = 0; k < semantemes.size(); k++) {
							if (i == k)
								continue;
							if (semantemes.get(k).size() == 1
									&& semantemes.get(k).getItems().get(0).getLeft().contains(address)) {
								if (k < i) {
									obtainsProposition(S, semantemes, k + 1, i);
								} else {
									obtainsProposition(S, semantemes, k + 1, semantemes.size());
								}
								semantemes.get(i).getItems().clear();
								return;
							}
						}
					} else {
						S.add(semantemes.get(i));
					}
				} else {
					return;
				}
			} else {
				return;
			}
		}
	}
}
