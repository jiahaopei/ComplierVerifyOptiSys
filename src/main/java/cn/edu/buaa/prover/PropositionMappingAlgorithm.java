package cn.edu.buaa.prover;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.buaa.pojo.Item;
import cn.edu.buaa.pojo.Proposition;

/**
 * 命题映射算法封装类
 * 
 * @author destiny
 *
 */
public class PropositionMappingAlgorithm {

	public static List<Proposition> process(List<String> objectCodePatterns, Map<String, Proposition> axioms) {

		List<Proposition> propositions = new ArrayList<>();
		String regex = "\t| |,|\\(|\\)";

		for (String line : objectCodePatterns) {
			String[] lines = line.split(regex);
			lines = filterOtherSignal(lines);

			if (lines.length == 0) {
				continue;
			} else if (lines.length == 1) {
				Proposition prop = new Proposition();
				List<Item> items = new ArrayList<>();
				Item item = new Item();
				item.setLeft(lines[0]); // 单项语义放在Item.left中

				// 若为逻辑表达式，则把表达式的结果放到GPR[0]中
				if (lines[0].contains("LOG-EXP")) {
					item.setLeft("GPR[0]");
					item.setRight(lines[0]);
				}
				items.add(item);
				prop.setItems(items);
				propositions.add(prop);
			} else {
				Map<String, String> paras = ProverHelper.generateParas(lines);
				Proposition prop = createProposition(lines[0], paras, axioms);
				propositions.add(prop);
			}
		}

		return propositions;
	}

	private static Proposition createProposition(String name, Map<String, String> paras,
			Map<String, Proposition> axioms) {

		if (!axioms.containsKey(name)) {
			try {
				throw new Exception("createProposition()缺少指称语义：" + name);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Proposition prop = ProverHelper.cloneProposition(axioms.get(name));
		ProverHelper.updatePropositionWithParas(name, prop, paras);

		for (String key : paras.keySet()) {
			String value = paras.get(key);
			String nKey = "\\b" + key + "\\b";

			for (Item item : prop.getItems()) {
				if (item.getPremise() != null) {
					String tmp = item.getPremise().replaceAll(nKey, value);
					item.setPremise(tmp);
				}
				if (item.getLeft() != null) {
					String tmp = item.getLeft().replaceAll(nKey, value);
					item.setLeft(tmp);
				}
				if (item.getRight() != null) {
					String tmp = item.getRight().replaceAll(nKey, value);
					item.setRight(tmp);
				}
			}
		}

		return prop;
	}

	private static String[] filterOtherSignal(String[] lines) {

		List<String> tmp = new ArrayList<String>();
		for (String line : lines) {
			line = line.trim();
			if (line.length() == 0 || line.equals(",") || line.equals("(") || line.equals(")")) {
				continue;
			}
			tmp.add(line);
		}

		String[] nLines = new String[tmp.size()];
		for (int i = 0; i < tmp.size(); i++) {
			nLines[i] = tmp.get(i);
		}

		return nLines;
	}

}
