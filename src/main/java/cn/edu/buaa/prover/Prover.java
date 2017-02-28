package cn.edu.buaa.prover;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.buaa.constant.CommonsDefine;
import cn.edu.buaa.constant.ProverDefine;
import cn.edu.buaa.pojo.Item;
import cn.edu.buaa.pojo.Proposition;
import cn.edu.buaa.recorder.Recorder;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class Prover {

	// 专用公理集
	private Map<String, Proposition> axioms;
	
	// 所有语句的目标码模式
	private Map<String, List<String>> allObjectCodePatterns;
	
	// 所有的循环不变式语句
	private Map<String, List<Proposition>> loopInvariants;
		
	private Recorder recorder;
	
	private List<String> proves;
	private List<String> proveLabels;
	
	// 保存证明序列文件
	private BufferedWriter sequences;
	
	// 中间过程输出结果
	public BufferedWriter bufferedWriter;
	
	private final Logger logger = LoggerFactory.getLogger(Prover.class);
	
	public Prover(Recorder recorder, String srcPath) {
		loadAxioms("/axiom/ppcAxiom.xls");
		// showAxioms();

		loadAllObjectCodePatterns("/statement/");
		// showAllObjectCodePatterns();
		
		loadPrecoditions("/precodition/");
		// showAllLoopInvariants();
		
		this.recorder = recorder;
		this.sequences = createSequencesFile(srcPath);
		this.proves = new ArrayList<>();
		this.proveLabels = new ArrayList<>();
	}
	
	public List<String> getProves() {
		return proves;
	}

	public List<String> getProveLabels() {
		return proveLabels;
	}
	
	public boolean runProver(String key, String label, List<String> segments) {
		List<String> objectCodePatterns = getObjectCodePatterns(key, segments);
		if (objectCodePatterns != null) {
			createOutputFile(key);
			return proveProcess(objectCodePatterns, key, label);
		} else {
			proves.add("Semantic verify correct");
			proveLabels.add(label);
			
			recorder.insertLine(key + " : " + label);
			recorder.insertLine("\tSemantic verify correct");
			recorder.insertLine("");
			
			try {
				sequences.write(key + " : " + label);
				sequences.newLine();
				sequences.write("\tSemantic verify correct\n\n");
				sequences.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return true;
		}
		
	}
	
	
	/**
	 * 对输入的目标码模式进行证明： 1) 目标码映射 2) 推理证明 3) 获得语义
	 * 
	 * @param objectCodePatterns
	 * @throws IOException
	 */
	public boolean proveProcess(List<String> objectCodePatterns, String name, String label) {
		
		// 验证结果
		boolean isSame = false;
		
		recorder.insertLine(name + " : " + label);
		recorder.insertLine(null);
		recorder.insertLine("==============目标码模式===============");
		showSingleObjectCodePatterns(objectCodePatterns);
		
		logger.info(label + " : " + name);
		
		if (bufferedWriter != null) {
			try {
				bufferedWriter.write(name + " : " + label + "\n");
				bufferedWriter.newLine();
				bufferedWriter.write("目标码模式 :\n");
				saveAllString(objectCodePatterns);
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// 命题映射
		recorder.insertLine("==============目标码模式命题===============");
		logger.info("调用命题映射算法");
		List<Proposition> propositions = PropositionMappingAlgorithm.process(objectCodePatterns, axioms);
		initPropositionProof(propositions);
		showAllProposition(propositions);
				
		if (bufferedWriter != null) {
			try {
				bufferedWriter.write("目标码模式命题 :\n");
				ProverUtils.saveAllProposition(propositions, bufferedWriter);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (sequences != null) {
			try {
				sequences.write(name + " : " + label);
				sequences.newLine();
				sequences.write("目标码模式命题 :\n");
				ProverUtils.saveAllProposition(propositions, sequences);
				sequences.flush();
				
				for (Proposition proposition : propositions) {
					String line = proposition.toStr();
					if (proposition.getProof() != null) {
						line = proposition.getProof() + " = " + line;
					}
					proves.add(line);
					proveLabels.add(label);
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (!ProverDefine.LOOPS.contains(name)) {
			// 非循环命题推导
			recorder.insertLine("==============推导序列===============");
			logger.info("调用自动推理算法");
			DerivationDTO dto = AutomaticDerivationAlgorithm.process(propositions);
			// 保存推导序列
			for (int i = 0; i < dto.getProves().size(); i++) {
				String line = dto.getProves().get(i) + ProverDefine.TAB + dto.getProofs().get(i);
				recorder.insertLine(line);
			}
			recorder.insertLine(null);
			
			if (bufferedWriter != null) {
				try {
					bufferedWriter.write("推导序列 :\n");
					for (int i = 0; i < dto.getProves().size(); i++) {
						String line = dto.getProves().get(i) + ProverDefine.TAB + dto.getProofs().get(i);
						bufferedWriter.write(line);
						bufferedWriter.newLine();
					}
					bufferedWriter.newLine();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (sequences != null) {
				try {
					sequences.write("推导序列 :\n");
					for (int i = 0; i < dto.getProves().size(); i++) {
						String line = dto.getProves().get(i) + ProverDefine.TAB + dto.getProofs().get(i);
						sequences.write(line);
						sequences.newLine();
						
						proves.add(line);
						proveLabels.add(label);
					}
					sequences.newLine();
					sequences.flush();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			// 给定的目标语义
			recorder.insertLine("=============给定的目标语义================");
			List<Proposition> goals = loopInvariants.get(name);
			showAllProposition(goals);
			if (bufferedWriter != null) {
				try {
					bufferedWriter.write("给定的目标语义为 :\n");
					ProverUtils.saveAllProposition(goals, bufferedWriter);
					bufferedWriter.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			// 判断语义是否一致
			recorder.insertLine("===============结论================");
			recorder.insertLine("给定的目标语义和推理出的语义是否一致 : ");
			isSame = LoopInteractiveProvingAlgorithm.judgeSemantemes(goals, dto.getSemantemeSet());
			recorder.insertLine(Boolean.toString(isSame));
			if (bufferedWriter != null) {
				try {
					bufferedWriter.write("给定的目标语义和推理出的语义是否一致 :\n");
					bufferedWriter.write(Boolean.toString(isSame));
					bufferedWriter.newLine();
					bufferedWriter.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		} else {
			// 循环交互证明算法
			recorder.insertLine("=================循环交互证明算法===================");
			try {
				logger.info("调用循环交互证明算法");
				isSame = LoopInteractiveProvingAlgorithm.process(propositions, name, loopInvariants, bufferedWriter, recorder, 
						sequences, proves, proveLabels, label);
				recorder.insertLine("综上，给定的目标语义和推理出的语义是否一致 :");
				recorder.insertLine(Boolean.toString(isSame));
				
				if (bufferedWriter != null) {
					bufferedWriter.write("综上，给定的目标语义和推理出的语义是否一致 :\n");
					bufferedWriter.write(Boolean.toString(isSame));
					bufferedWriter.newLine();
					bufferedWriter.flush();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

		return isSame;
	}
	
	// 产生初始命题的证据
	public void initPropositionProof(List<Proposition> propositions) {
		int i = 1;
		for (Proposition proposition : propositions) {
			String f = "P" + i;
			proposition.setProof(f);
			i++;
		}
	}

	public List<String> getObjectCodePatterns(String key, List<String> segments) {
		
		if (segments != null) {
			return allObjectCodePatterns.get(key);
		} else {
			return null;
		}
	}

	public void loadAxioms(String path) {
		axioms = new HashMap<>();

		Workbook readwb = null;
		try {			
			readwb = Workbook.getWorkbook(
					this.getClass().getResourceAsStream(path));
			Sheet readsheet = readwb.getSheet(0);

			// 读取xls文档
			int rsRows = readsheet.getRows();
			int i = 1;
			while (i < rsRows) {
				String name = readsheet.getCell(0, i).getContents().trim();
				if (name == null || name.length() == 0)
					continue;

				List<Item> items = new ArrayList<Item>();
				Proposition proposition = new Proposition(items);
				while (i < rsRows) {
					String tmp = readsheet.getCell(0, i).getContents().trim();
					if (null == tmp || tmp.length() == 0 || tmp.equals(name)) {
						String premise = readsheet.getCell(1, i).getContents().trim();
						String left = readsheet.getCell(2, i).getContents().trim();
						String right = readsheet.getCell(3, i).getContents().trim();

						if (null == premise || premise.length() == 0)
							premise = null;
						if (null == left || left.length() == 0)
							left = null;
						if (null == right || right.length() == 0)
							right = null;

						if (null != premise || null != left || null != right) {
							Item item = new Item(premise, left, right);
							items.add(item);
						}

						i++;
					} else {
						break;
					}
				}

				axioms.put(name, proposition);
				name = null;
			}

		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void showAxioms() {
		for (String name : axioms.keySet()) {
			Proposition proposition = axioms.get(name);
			System.out.println(name + ":");
			System.out.println(proposition);
		}
	}

	public void loadPrecoditions(String dirName) {
		loopInvariants = new HashMap<>();
		
		for (String fileName : CommonsDefine.names) {
			fileName += ".txt";
			String path = dirName;
			if (!path.endsWith("/"))
				path += "/";
			path += fileName;			
			List<Proposition> value = loadSingleLoopInvariants(path);
			String key = fileName.substring(0, fileName.indexOf('.'));
			loopInvariants.put(key, value);
		}
	}
	
	public List<Proposition> loadSingleLoopInvariants(String path) {
		
		List<Proposition> singleLoopInvariants = new ArrayList<>();
		BufferedReader in = null;
		try {
			in =  new BufferedReader(
					new InputStreamReader(
							this.getClass().getResourceAsStream(path)));
			String line = null;
			while ((line = in.readLine()) != null) {
				Proposition proposition = LoopInteractiveProvingAlgorithm.analyzeString(line);
				singleLoopInvariants.add(proposition);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return singleLoopInvariants;
	}
	
	public void showAllLoopInvariants() {
		
		for (String key : loopInvariants.keySet()) {
			List<Proposition> values = loopInvariants.get(key);
			System.out.println(key + "::");
			for (Proposition proposition : values) {
				System.out.println(proposition);
			}
			System.out.println();
		}
		
	}

	public void loadAllObjectCodePatterns(String dirName) {
		allObjectCodePatterns = new HashMap<>();	
		
		for (String fileName : CommonsDefine.names) {
			fileName += ".txt";
			String path = dirName;
			if (!path.endsWith("/"))
				path += "/";
			path += fileName;

			List<String> objectCodePatterns = loadSingleObjectCodePatterns(path);
			String key = fileName.substring(0, fileName.indexOf('.'));
			allObjectCodePatterns.put(key, objectCodePatterns);
		}
	}
	
	public List<String> loadSingleObjectCodePatterns(String path) {

		List<String> objectCodePatterns = new ArrayList<>();
		BufferedReader reader = null;
		try {
			reader =  new BufferedReader(
					new InputStreamReader(
							this.getClass().getResourceAsStream(path)));
			String line = null;
			while (null != (line = reader.readLine())) {
				line = line.trim();
				if (line.length() == 0)
					continue;
				objectCodePatterns.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return objectCodePatterns;
	}
	
	public void showAllObjectCodePatterns() {

		for (String key : allObjectCodePatterns.keySet()) {
			List<String> objectCodePatterns = allObjectCodePatterns.get(key);
			System.out.println(key + "::");
			showSingleObjectCodePatterns(objectCodePatterns);
		}

	}
	
	public void showSingleObjectCodePatterns(List<String> objectCodePatterns) {
		for (String line : objectCodePatterns) {
			recorder.insertLine(line);
		}
		recorder.insertLine(null);
		
	}
	
	public void showAllProposition(List<Proposition> propositions) {
		for (Proposition proposition : propositions) {
			String line = proposition.toStr();
			if (proposition.getProof() != null) {
				line = proposition.getProof() + " = " + line;
			}
			recorder.insertLine(line);
		}
		recorder.insertLine(null);
	}

	public void createOutputFile(String key) {
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(CommonsDefine.DEBUG_PATH + key + ".txt"));
			bufferedWriter.write("======================结果输出=======================");
			bufferedWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public BufferedWriter createSequencesFile(String srcPath) {
		BufferedWriter bw = null;
		
		if (srcPath == null) {
			try {
				bw =  new BufferedWriter(new FileWriter(CommonsDefine.OUTPUT_PATH + "prover.v"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else {
			String fileName = srcPath.substring(srcPath.lastIndexOf("/") + 1);
			int end = fileName.indexOf(".");
			fileName = fileName.substring(0, end);
			try {
				bw = new BufferedWriter(new FileWriter(CommonsDefine.OUTPUT_PATH + fileName + ".v"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return bw;
	}
	
	public void saveAllString(List<String> objectCodePatterns) throws IOException {
		for (String str : objectCodePatterns) {
			bufferedWriter.write(str);
			bufferedWriter.newLine();
		}
		bufferedWriter.newLine();
		bufferedWriter.flush();
	}

	public static void main(String[] args) {
		Recorder recorder = new Recorder();
		Prover prover = new Prover(recorder, null);
		prover.runProver("for", "9.4", new ArrayList<>());
	}
}
