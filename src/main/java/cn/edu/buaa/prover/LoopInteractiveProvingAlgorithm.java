package cn.edu.buaa.prover;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.buaa.constant.ProverDefine;
import cn.edu.buaa.pojo.Item;
import cn.edu.buaa.pojo.Proposition;
import cn.edu.buaa.recorder.Recorder;

public class LoopInteractiveProvingAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(LoopInteractiveProvingAlgorithm.class);
	
	public static boolean process(List<Proposition> srcPropositions, String name, Map<String, 
			List<Proposition>> loopInvariants, BufferedWriter bufferedWriter, 
			Recorder recorder, BufferedWriter sequences, 
			List<String> proves, List<String> proveLabels, String label) throws IOException {
		
		logger.info("LoopInteractiveProvingAlgorithm.process");
		
		// 拷贝一个副本
		List<Proposition> propositions = new ArrayList<>();
		for (Proposition proposition : srcPropositions) {
			Proposition tmp = ProverHelper.cloneProposition(proposition);
			propositions.add(tmp);
		}
		
//		List<Proposition> srcGoals = readFromConsole(name);
		List<Proposition> srcGoals = loopInvariants.get(name);
		String line = "";
		recorder.insertLine("用户输入的语义 :");
		for (Proposition proposition : srcGoals) {
			line = proposition.toStr();
			if (proposition.getProof() != null) {
				line = proposition.getProof() + " = " + line;
			}
			recorder.insertLine(line);
		}
		recorder.insertLine(null);
		if (bufferedWriter != null) {
			bufferedWriter.write("用户输入的语义 :\n");
			for (Proposition proposition : srcGoals) {
				line = proposition.toStr();
				if (proposition.getProof() != null) {
					line = proposition.getProof() + " = " + line;
				}
				bufferedWriter.write(line);
				bufferedWriter.newLine();
			}
			bufferedWriter.newLine();
		}
		
		recorder.insertLine("辅助前提 :");
		String[] assistPremises = AutomaticDerivationAlgorithm.conjunction(srcGoals);
		recorder.insertLine("P0 = " + assistPremises[0]);
		recorder.insertLine(null);
		if (bufferedWriter != null) {
			bufferedWriter.write("辅助前提 :\n");
			bufferedWriter.write("P0 = " + assistPremises[0] + "\n");
			bufferedWriter.newLine();
		}
		
		if (sequences != null) {
			sequences.write("辅助前提 :\n");
			sequences.write("P0 = " + assistPremises[0]);
			sequences.newLine();
			
			proves.add("P0 = " + assistPremises[0]);
			proveLabels.add(label);
		}
		
		// 生成推导序列
		DerivationDTO dto = AutomaticDerivationAlgorithm.process(propositions);
		
		// check(n == 1)
		List<Proposition> oneGoals = cloneListProposition(srcGoals);
		setLoopInvariant(oneGoals, "1");
		String oneProof = "S" + (dto.getStep() - 1);
		boolean oneResult = judgeSemantemes(oneGoals, dto.getSemantemeSet());
		
		// 生成 n = N 时的推导序列
		List<Proposition> NGoals = cloneListProposition(srcGoals);
		setLoopInvariant(NGoals, "N");
		String[] nPremises = AutomaticDerivationAlgorithm.conjunction(NGoals);
		line = "S" + dto.getStep() + " = " + nPremises[0];
		dto.getProves().add(line);
		line = "P0, n = N";
		dto.getProofs().add(line);
		dto.setStep(dto.getStep() + 1);
		
		// 把 n = N时的结果合并 n = 1时的结果
		List<Proposition> goals = mergeTwo(NGoals, dto.getSemantemeSet());
		String[] nPlusPremises = AutomaticDerivationAlgorithm.conjunction(goals);
		line = "S" + dto.getStep() + " = " + nPlusPremises[0];
		dto.getProves().add(line);
		line = "S" + (dto.getStep() - 2) + ", S" + (dto.getStep() - 1) + ", CI";
		dto.getProofs().add(line);
		dto.setStep(dto.getStep() + 1);
		
		// check(n == N + 1)
		List<Proposition> NPlusGoals = cloneListProposition(srcGoals);
		setLoopInvariant(NPlusGoals, "(N + 1)");
		String nPlusProof = "S" + (dto.getStep() - 1);
		boolean nPlusResult = judgeSemantemes(NPlusGoals, goals);
		
		// 保存推导序列
		recorder.insertLine("推导序列 :");
		for (int i = 0; i < dto.getProves().size(); i++) {
			line = dto.getProves().get(i) + ProverDefine.TAB + dto.getProofs().get(i);
			recorder.insertLine(line);
		}
		recorder.insertLine(null);
		
		if (bufferedWriter != null) {
			bufferedWriter.write("推导序列 :\n");
			for (int i = 0; i < dto.getProves().size(); i++) {
				line = dto.getProves().get(i) + ProverDefine.TAB + dto.getProofs().get(i);
				bufferedWriter.write(line);
				bufferedWriter.newLine();
			}
			bufferedWriter.newLine();
		}
		
		if (sequences != null) {
			sequences.write("推导序列 :\n");
			for (int i = 0; i < dto.getProves().size(); i++) {
				line = dto.getProves().get(i) + ProverDefine.TAB + dto.getProofs().get(i);
				sequences.write(line);
				sequences.newLine();
				
				proves.add(line);
				proveLabels.add(label);
			}
			sequences.newLine();
			sequences.flush();
		}
		
		// 输出语义比较结果
		line = AutomaticDerivationAlgorithm.conjunction(oneGoals)[0];
		recorder.insertLine("check(n == 1) :");
		recorder.insertLine("目标语义 : " + line);
		recorder.insertLine("推导序列证据 : " + oneProof);
		recorder.insertLine("目标语义和推理出的语义是否一致 : " + oneResult);
		recorder.insertLine(null);
		if (bufferedWriter != null) {
			bufferedWriter.write("check(n == 1) :\n");
			bufferedWriter.write("目标语义 : " + line);
			bufferedWriter.newLine();
			bufferedWriter.write("推导序列证据 : " + oneProof);
			bufferedWriter.newLine();
			bufferedWriter.write("目标语义和推理出的语义是否一致 : " + oneResult);
			bufferedWriter.newLine();
			bufferedWriter.newLine();
		}
		
		line = AutomaticDerivationAlgorithm.conjunction(NPlusGoals)[0];
		recorder.insertLine("check(n == N + 1) :");
		recorder.insertLine("目标语义 : " + line);
		recorder.insertLine("推导序列证据 : " + nPlusProof);
		recorder.insertLine("目标语义和推理出的语义是否一致 : " + nPlusResult);
		recorder.insertLine(null);
		if (bufferedWriter != null) {
			bufferedWriter.write("check(n == N + 1) :\n");
			bufferedWriter.write("目标语义 : " + line);
			bufferedWriter.newLine();
			bufferedWriter.write("推导序列证据 : " + nPlusProof);
			bufferedWriter.newLine();
			bufferedWriter.write("目标语义和推理出的语义是否一致 : " + nPlusResult);
			bufferedWriter.newLine();
			bufferedWriter.newLine();
		}
		
		return oneResult && nPlusResult;
	}
	
	public static List<Proposition> mergeTwo(List<Proposition> nGoals, List<Proposition> oneGoals) {
		List<Proposition> goals = cloneListProposition(nGoals);
		for (int i = 0; i < nGoals.size(); i++) {
			for (int j = 0; j < oneGoals.size(); j++) {
				Proposition n = nGoals.get(i);
				Proposition one = oneGoals.get(j);
				
				for (int ii = 0; ii < n.getItems().size(); ii++) {
					for (int jj = 0; jj < one.getItems().size(); jj++) {
						Item nItem = n.getItems().get(ii);
						Item oneItem  = one.getItems().get(jj);
						
						if (nItem.toString().contains(oneItem.toString()) 
								&& nItem.toString().contains("** N")) {
							goals.get(i).getItems().get(ii).setLeft(nItem.getLeft().replace("** N", "** (N + 1)"));
						}
						
					}
					
				}
				
			}
			
		}
		
		return goals;
	}

	public static void setLoopInvariant(List<Proposition> goals, String n) {
		
		for (Proposition proposition : goals) {
			List<Item> items = proposition.getItems();
			for (Item item : items) {
				if (item.getLeft().contains("** n")) {
					if (n.equals("1")) {
						int left = item.getLeft().indexOf("{");
						int right = item.getLeft().indexOf("}");
						item.setLeft(item.getLeft().substring(left + 1, right));
					} else {
						item.setLeft(item.getLeft().replace("** n", "** " + n));
					}
					break;
				}
				
			}
			
		}
	}

	public static List<Proposition> cloneListProposition(List<Proposition> srcGoals) {
		
		List<Proposition> goals = new ArrayList<>();
		for (Proposition proposition : srcGoals) {
			goals.add(ProverHelper.cloneProposition(proposition));
		}
		
		return goals;
	}

	public static void showAllProposition(List<Proposition> propositions, Recorder recorder) {
		String line = "";
		int i = 0;
		for (Proposition proposition : propositions) {
			if (i != 0) {
				line += " ^ ";
			}
			line += "(" + proposition.toStr() + ")";
			i++;
		}
		recorder.insertLine(line);
		recorder.insertLine(null);
	}
	
	public static List<Proposition> readFromConsole(String name) {
		System.out.println("请输入\"" + name + "\"文法单元的语义表达式：(以EOF结尾)");
		Scanner in = new Scanner(System.in);
		List<Proposition> goals = new ArrayList<>();
		while (in.hasNext()) {
			String line = in.nextLine();
			System.out.println(line);
			if (line.trim().equalsIgnoreCase("EOF")) {
				break;
			}
			Proposition proposition = analyzeString(line);
			goals.add(proposition);
		}
		in.close();
		
		return goals;
	}

	public static Proposition analyzeString(String line) {
		
		Proposition proposition = new Proposition();
		List<Item> items = new ArrayList<>();
		proposition.setItems(items);
		String[] strs = line.split("\\|\\|");

		for (String str : strs) {
			str = str.trim();
			if (str.length() == 0) continue;
			Item item = new Item();
			item.setLeft(str);
			items.add(item);
		}
	
		return proposition;
	}

	public static boolean judgeSemantemes(List<Proposition> goals, List<Proposition> sigmaSemantemes) {
		
		StringBuilder a = new StringBuilder();
		for (Proposition proposition : goals) {
			a.append(proposition);
		}
		
		StringBuilder b = new StringBuilder();
		for (Proposition proposition : sigmaSemantemes) {
			b.append(proposition);
		}
		
		return a.toString().trim().equals(b.toString().trim());
	}
	
}
