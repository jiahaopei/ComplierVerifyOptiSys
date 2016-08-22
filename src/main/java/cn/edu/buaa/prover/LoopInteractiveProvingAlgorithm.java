package cn.edu.buaa.prover;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.buaa.pojo.Item;
import cn.edu.buaa.pojo.Proposition;
import cn.edu.buaa.recorder.Recorder;

public class LoopInteractiveProvingAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(LoopInteractiveProvingAlgorithm.class);
	
	public static boolean process(List<Proposition> srcPropositions, String name, Map<String, 
			List<Proposition>> loopInvariants, BufferedWriter bufferedWriter, Recorder recorder) throws IOException {
		
		logger.info("LoopInteractiveProvingAlgorithm.process");
		
		// 拷贝一个副本
		List<Proposition> propositions = new ArrayList<>();
		for (Proposition proposition : srcPropositions) {
			Proposition tmp = ProverHelper.cloneProposition(proposition);
			propositions.add(tmp);
		}
		
//		List<Proposition> srcGoals = readFromConsole(name);
		List<Proposition> srcGoals = loopInvariants.get(name);
		recorder.insertLine("源目标语义 :");
		showAllProposition(srcGoals, recorder);
		if (bufferedWriter != null) {
			bufferedWriter.write("源目标语义 :\n");
			ProverHelper.saveAllProposition(srcGoals, bufferedWriter);
		}
		
	
		List<Proposition> oneGoals = cloneListProposition(srcGoals);
		recorder.insertLine("(1) n == 1");
		recorder.insertLine("目标语义取 n = 1 :");
		setLoopInvariant(oneGoals, "1");
		showAllProposition(oneGoals, recorder);
		if (bufferedWriter != null) {
			bufferedWriter.write("(1) n == 1\n");
			bufferedWriter.write("目标语义取 n = 1 :\n");
			ProverHelper.saveAllProposition(oneGoals, bufferedWriter);
		}
		
		
		recorder.insertLine("推理出的语义为 :");
		// n = 1时, 进行一遍推理
		List<Proposition> simplifiedPropositions = AutomaticDerivationAlgorithm.process(propositions);
		List<Proposition> semantemes = SemantemeObtainAlgorithm.obtainSemantemeFromProposition(simplifiedPropositions);
		showAllProposition(semantemes, recorder);	
		recorder.insertLine("σ-transfer :");
		List<Proposition> semantemeSet = SemantemeObtainAlgorithm.standardSemantemes(semantemes);
		showAllProposition(semantemeSet, recorder);
		if (bufferedWriter != null) {
			bufferedWriter.write("推理出的语义为 :\n");
			ProverHelper.saveAllProposition(semantemes, bufferedWriter);
			bufferedWriter.write("σ-transfer :\n");
			ProverHelper.saveAllProposition(semantemeSet, bufferedWriter);
		}
		
		recorder.insertLine("结论 :");
		if (bufferedWriter != null) {
			bufferedWriter.write("结论 :\n");
		}
		if (!judgeSemantemes(oneGoals, semantemeSet)) {
			recorder.insertLine("n = 1时, 目标语义和推理出的语义不一致");
			if (bufferedWriter != null) {
				bufferedWriter.write("n = 1时, 目标语义和推理出的语义不一致\n");
				bufferedWriter.newLine();
			}
			return false;
		} else {
			recorder.insertLine("n = 1时, 目标语义和推理出的语义一致\n");
			bufferedWriter.write("n = 1时, 目标语义和推理出的语义一致\n");
			bufferedWriter.newLine();
		}
		
		// n == N时，假设目标语义成立
		recorder.insertLine("(2) n = N");
		recorder.insertLine("假设成立 :");
		List<Proposition> NGoals = cloneListProposition(srcGoals);
		setLoopInvariant(NGoals, "N");
		showAllProposition(NGoals, recorder);
		if (bufferedWriter != null) {
			bufferedWriter.write("(2) n = N\n");
			bufferedWriter.write("假设成立 :\n");
			ProverHelper.saveAllProposition(NGoals, bufferedWriter);
		}
		
		// 推理 n == (N + 1)时，语义是否保持一致
		recorder.insertLine("(3) n = N + 1");
		List<Proposition> NPlusGoals = cloneListProposition(srcGoals);
		setLoopInvariant(NPlusGoals, "(N + 1)");
		recorder.insertLine("目标语义 :");
		showAllProposition(NPlusGoals, recorder);
		if (bufferedWriter != null) {
			bufferedWriter.write("(3) n = N + 1\n");
			bufferedWriter.write("目标语义 :\n");
			ProverHelper.saveAllProposition(NPlusGoals, bufferedWriter);
		}
		
		
		List<Proposition> goals = mergeTwo(NGoals, semantemeSet);
		recorder.insertLine("合并推理出的 n = 1 和 假设的 n = N 的语义 :");
		showAllProposition(goals, recorder);
		recorder.insertLine("结论 :");
		if (bufferedWriter != null) {
			bufferedWriter.write("合并推理出的 n = 1 和 假设的 n = N 的语义 :\n");
			ProverHelper.saveAllProposition(goals, bufferedWriter);
			bufferedWriter.write("结论 :\n");
		}
		
		if (judgeSemantemes(NPlusGoals, goals)) {
			recorder.insertLine("n = N + 1时, 目标语义和推理出的语义一致\n");
			if (bufferedWriter != null) {
				bufferedWriter.write("n = N + 1时, 目标语义和推理出的语义一致\n");
				bufferedWriter.newLine();
			}
			return true;
		} else {
			recorder.insertLine("n = N + 1时, 目标语义和推理出的语义不一致\n");
			if (bufferedWriter != null) {
				bufferedWriter.write("n = N + 1时, 目标语义和推理出的语义不一致\n");
				bufferedWriter.newLine();
			}
			return false;
		}
		
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
		for (Proposition proposition : propositions) {
			recorder.insertLine(proposition.toString());
		}
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
