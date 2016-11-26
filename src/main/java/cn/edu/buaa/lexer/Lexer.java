package cn.edu.buaa.lexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.buaa.constant.CommonsDefine;
import cn.edu.buaa.constant.LexerDefine;
import cn.edu.buaa.pojo.Token;
import cn.edu.buaa.recorder.Recorder;

public class Lexer {
	
	private Recorder recorder;
	private String srcDir;
	private String srcName;
	private List<String> srcs;
	
	private List<String> sources;
	private List<String> labels;
	
	private List<Token> tokens;
	
	private Set<String> fileNames;
	
	private final Logger logger = LoggerFactory.getLogger(Lexer.class);

	public Lexer(String srcPath, Recorder recorder) {
		this.recorder = recorder;
		this.srcDir = srcPath.substring(0, srcPath.lastIndexOf("/") + 1);
		this.srcName = srcPath.substring(srcPath.lastIndexOf("/") + 1);
		
		sources = new ArrayList<>();
		labels = new ArrayList<>();
		tokens = new ArrayList<Token>();
		fileNames = new HashSet<>();
		
		srcs = getContent(srcName);
	}

	public List<String> getSrcs() {
		return srcs;
	}
	
	public List<String> getSources() {
		return sources;
	}

	public List<String> getLabels() {
		return labels;
	}

	public List<Token> getTokens() {
		return tokens;
	}
	
	// 词法分析的同时进行编号
	public void runLexer() {
		logger.info("=========Lexer=========");
		logger.info("词法分析开始...");
		recorder.insertLine("词法分析开始...");
		
		List<String> libs = new ArrayList<>();
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(
					new FileWriter(CommonsDefine.OUTPUT_PATH + "label_" + srcName));
			fileNames.add(srcName.substring(0, srcName.indexOf(".")));
			
			Stack<Integer> stack = new Stack<>();
			stack.push(1);
			for (int i = 0; i < srcs.size(); i++) {
				String line = srcs.get(i);
				String label = "";
				String tmpLine = line;
				
				if (line.contains("}")) {
					stack.pop();
					int tmp = stack.pop();
					stack.push(tmp + 1);
				}

				if (line.trim().length() != 0) {
					//String label = generateLabel(stack);
					List<String> tmpLibs = solveLine(line.trim(), stack);
					libs.addAll(tmpLibs);
					
					line = LexerUtils.RTrim(line);
					for (int j = line.length(); j < LexerUtils.LEN; j++) {
						line += " ";
					}
					
					label = generateLabel(stack);
					if (label.trim().length() != 0) {
						line = line + "// " + label.trim();
					}
				}
				
				// 写入label文件
				writer.write(line);
				writer.newLine();
				writer.flush();
				
				sources.add(tmpLine);
				labels.add(label);

				if (line.contains("{")) {
					stack.push(0);
				}

				if (line.trim().length() != 0) {
					int tmp = stack.pop();
					stack.push(tmp + 1);
				}
			}
			
			// 最后处理所有的library
			for (String lib : libs) {
				solveMultipleFile(lib, stack);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		secureCheck();
		
		recorder.insertLine("词法分析结束!");
		logger.info("词法分析结束!");
	}
	
	// 安全C子集集中检测，注意还有一些安全C子集规则在词法分析中检测完成
	private void secureCheck() {
		for (Token e : tokens) {
			
			if (!checkTrigraphs(e)) {
				try {
					throw new Exception(
							"Error [" + e.getLabel() +"] : not allowned using trigraphs " + e.getValue());
				} catch (Exception e1) {
					e1.printStackTrace();
					System.exit(1);
				}
			}
			
			// 规则 5.1（强制）： 标识符（内部的和外部的）的有效字符不能多于 31。
			if (e.getType().equals("IDENTIFIER") && e.getValue().length() > 31) {
				try {
					throw new Exception(
							"Error [" + e.getLabel() +"] : the length of identifier should not more than 31 " + e.getValue());
				} catch (Exception e1) {
					e1.printStackTrace();
					System.exit(1);
				}
			}
			
			if (e.getType().equals("CONTINUE")) {
				try {
					throw new Exception(
							"Error [" + e.getLabel() +"] : The continue statement shall not be used! '" + e.getValue() + "'");
				} catch (Exception e1) {
					e1.printStackTrace();
					System.exit(1);
				}
				
			}
			
			if (e.getType().equals("GOTO")) {
				try {
					throw new Exception(
							"Error [" + e.getLabel() +"] : The goto statement shall not be used! '" + e.getValue() + "'");
				} catch (Exception e1) {
					e1.printStackTrace();
					System.exit(1);
				}
			}
			
			if (e.getType().equals("UNION")) {
				try {
					throw new Exception(
							"Error [" + e.getLabel() +"] : Unions shall not be used! '" + e.getValue() + "'");
				} catch (Exception e1) {
					e1.printStackTrace();
					System.exit(1);
				}
			}
			
			if (e.getValue().equals("alloc") || e.getValue().equals("malloc")
					|| e.getValue().equals("realloc") || e.getValue().equals("free")) {
				try {
					throw new Exception(
							"Error [" + e.getLabel() +"] : Dynamic heap memory allocation shall not be used! '" + e.getValue() + "'");
				} catch (Exception e1) {
					e1.printStackTrace();
					System.exit(1);
				}
			}
			
		}
	}

	private boolean checkTrigraphs(Token e) {
		for (String string : LexerDefine.trigraphs) {
			if (e.getValue().contains(string)) {
				return false;
			}
		}
		return true;
	}

	private String generateLabel(Stack<Integer> stack) {
		String v = "";

		for (int i = 0; i < stack.size(); i++) {
			if (i == 0) {
				v += stack.get(i);
			} else {
				v += "." + stack.get(i);
			}
		}

		return v;
	}

	private List<String> solveLine(String line, Stack<Integer> stack) {
		
		List<String> libs = new ArrayList<>();
		int i = 0;
		Token token = null;
		while (i < line.length()) {
			i = LexerUtils.skipBlank(i, line);

			// 如果是引入头文件
			if (i == 0 && line.charAt(i) == '#') {
				token = new Token(4, line.charAt(i), generateLabel(stack));
				tokens.add(token);

				i = LexerUtils.skipBlank(i + 1, line);
				// 匹配和处理"include"
				if ((i + 7) <= line.length() && line.substring(i, i + 7).equals("include")) {
					token = new Token(0, "include", generateLabel(stack));
					tokens.add(token);

					i = LexerUtils.skipBlank(i + 7, line);
					if (line.charAt(i) == '\"' || line.charAt(i) == '<') {
						token = new Token(4, line.charAt(i), generateLabel(stack));
						tokens.add(token);

						char close_flag = line.charAt(i) == '\"' ? '\"' : '>';
						i = LexerUtils.skipBlank(i + 1, line);

						// 找到include的头文件
						String lib = "";
						while (line.charAt(i) != close_flag) {
							if (line.charAt(i) == '\'' || line.charAt(i) == ','
									|| line.charAt(i) == '/' || line.charAt(i) == '*') {
								try {
									throw new Exception(
											"Error [" + generateLabel(stack) + 
											"] : Non-standard characters should not occur in header file names in #include directive! '" + line.charAt(i) + "'");
								} catch (Exception e1) {
									e1.printStackTrace();
									System.exit(1);
								}
							}
							
							lib += line.charAt(i);
							i++;
						}

						token = new Token(1, lib, generateLabel(stack));
						tokens.add(token);
						
						token = new Token(4, close_flag, generateLabel(stack));
						tokens.add(token);

						i = LexerUtils.skipBlank(i + 1, line);
						
						// 增加处理多个文件的逻辑
						libs.add(lib);
						
					} else {
						try {
							int tmp = line.length() - i;
							if (tmp > 7) {
								tmp = 7;
							}
							String label = generateLabel(stack);
							throw new Exception("include error [" + label + "] : " + line.substring(i, i + tmp));
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(1);
						}
					}

				} else {
					try {
						String label = generateLabel(stack);
						throw new Exception("Error include [" + label + "] : #");
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
				}

			// 如果是字母或者是以下划线开头
			} else if (Character.isLetter(line.charAt(i)) || line.charAt(i) == '_') {
				String word = "";
				while (i < line.length() && (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_')) {
					word += line.charAt(i);
					i++;
				}

				// 关键字
				if (LexerUtils.isKeyword(word)) {
					token = new Token(0, word, generateLabel(stack));
					tokens.add(token);
				
				// 标识符
				} else {
					token = new Token(1, word, generateLabel(stack));
					tokens.add(token);
					
				}
				i = LexerUtils.skipBlank(i, line);

			// 如果是数字开头
			} else if (Character.isDigit(line.charAt(i))) {
				String word = "";
				boolean pointExist = false;
				boolean suffix = false;
				while (i < line.length()) {
					if (Character.isDigit(line.charAt(i))) {
						word += line.charAt(i);
						
					} else if (!pointExist && line.charAt(i) == '.' 
							&& Character.isDigit(line.charAt(i + 1))) {
						pointExist = true;
						word += line.charAt(i);
						
					} else if (!suffix && line.charAt(i) == 'f' || line.charAt(i) == 'F'
							|| line.charAt(i) == 'l' || line.charAt(i) == 'F') {
						suffix = true;
						word += line.charAt(i);
					
					// 其它符号表示常量识别结束
					} else {
						break;
					}
					
					i++;
				}

				// 常量
				token = new Token(2, word, generateLabel(stack));
				tokens.add(token);
				i = LexerUtils.skipBlank(i, line);

				// 如果是分隔符
			} else if (LexerUtils.isDelimiter(line.charAt(i))) {
				// 如果是字符常量
				if (line.charAt(i) == '\'') {
					i++;
					String word = "";
					while (i < line.length() && line.charAt(i) != '\'') {
						if (line.charAt(i) == '\\') {
							word += line.charAt(i);
							i++;
						}
						word += line.charAt(i);
						i++;
					}

					if (i >= line.length()) {
						try {
							String label = generateLabel(stack);
							throw new Exception(
									"Can't find the end character of the char [" + label + "] : " + word);
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(1);
						}
						
					} else {
						token = new Token(2,
								"'" + word + "'",
								generateLabel(stack));
						tokens.add(token);
					}
				
				// 字符串常量 
				} else if (line.charAt(i) == '\"') {
					token = new Token(4, line.charAt(i), generateLabel(stack));
					tokens.add(token);
					i++;
					String word = "";
					while (i < line.length() && line.charAt(i) != '\"') {
						if (line.charAt(i) == '\\') {
							word += line.charAt(i);
							i++;
						}
						word += line.charAt(i);
						i++;
					}

					if (i >= line.length()) {
						try {
							String label = generateLabel(stack);
							throw new Exception(
									"Can't find the end character of the string constant [" + label + "]");
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(1);
						}

					} else {
						token = new Token(5, word, generateLabel(stack));
						tokens.add(token);
						token = new Token(4, '\"', generateLabel(stack));
						tokens.add(token);

					}
				
				// 其它分隔符
				} else {
					token = new Token(4, line.charAt(i), generateLabel(stack));
					tokens.add(token);
					
				}
				
				i = LexerUtils.skipBlank(i + 1, line);

			// 如果是运算符
			} else if (LexerUtils.isOperator(line.charAt(i))) {
				// 如果是++、--、<<、>>、&&、||
				if ((line.charAt(i) == '+' || line.charAt(i) == '-' 
						|| line.charAt(i) == '<' || line.charAt(i) == '>'
						|| line.charAt(i) == '&' || line.charAt(i) == '|')
						&& i + 1 < line.length() 
						&& line.charAt(i) == line.charAt(i + 1)) {
					token = new Token(3, line.substring(i, i + 2), generateLabel(stack));
					tokens.add(token);
					i = LexerUtils.skipBlank(i + 2, line);

					// 如果是>=或者<=或者==或者!=
				} else if ((line.charAt(i) == '>' || line.charAt(i) == '<' 
						|| line.charAt(i) == '=' || line.charAt(i) == '!')
						&& line.charAt(i + 1) == '=') {
					token = new Token(3, line.substring(i, i + 2), generateLabel(stack));
					tokens.add(token);
					i = LexerUtils.skipBlank(i + 2, line);

				} else {
					token = new Token(3, line.charAt(i), generateLabel(stack));
					tokens.add(token);
					i = LexerUtils.skipBlank(i + 1, line);
				}

			} else {
				try {
					String label = generateLabel(stack);
					throw new Exception("Unrecognized symbol [" + label + " ] : " + line.charAt(i));
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		return libs;
	}

	// 处理多文件连编
	private void solveMultipleFile(String libName, Stack<Integer> stack) {
		if (libName.contains(".")) {
			libName = libName.substring(0, libName.indexOf("."));
			// 是否是C语言自带的头文件
			for (String header : CommonsDefine.LIBS) {
				if (header.equals(libName + ".h")) {
					return;
				}
			}
			
			// 为自定义的头文件
			if (!fileNames.contains(libName)) {
				fileNames.add(libName);
				
			} else {
				return;		// 表示此文件已经处理过了
			}
			
		}
		
		// 找不到头文件
		String headName = libName + ".h";
		File file = new File(srcDir + libName + ".h");
		if (!file.exists()) {
			try {
				throw new Exception("Can't find header file : " + libName + ".h");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// 找不到源文件
		libName += ".c";
		file = new File(srcDir + libName);
		if (!file.exists()) {
			try {
				throw new Exception("Can't find source file : " + libName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		List<String> tmpSources = new ArrayList<>();
		List<String> tmpLabels = new ArrayList<>();
		
		handleFile(headName, tmpSources, tmpLabels, stack);
		handleFile(libName, tmpSources, tmpLabels, stack);
		
		sources.addAll(tmpSources);
		labels.addAll(tmpLabels);
	}


	private void handleFile(String libName, List<String> tmpSources, List<String> tmpLabels, Stack<Integer> stack) {
		List<String> contents = getContent(libName);
		BufferedWriter writer = null;
		List<String> libs = new ArrayList<>();
		try {
			writer = new BufferedWriter(
					new FileWriter(CommonsDefine.OUTPUT_PATH + "/label_" + libName));
			
			for (String line : contents) {
				String label = "";
				String tmpLine = line;
				
				if (line.contains("}")) {
					stack.pop();
					int tmp = stack.pop();
					stack.push(tmp + 1);
				}

				if (line.trim().length() != 0) {
					List<String> tmpLibs = solveLine(line.trim(), stack);
					libs.addAll(tmpLibs);
					
					line = LexerUtils.RTrim(line);
					for (int j = line.length(); j < LexerUtils.LEN; j++) {
						line += " ";
					}
					
					label = generateLabel(stack);
					if (label.trim().length() != 0) {
						line = line + "// " + label.trim();
					}
				}
				
				// 写入label文件
				writer.write(line);
				writer.newLine();
				writer.flush();
				
				tmpSources.add(tmpLine);
				tmpLabels.add(label);
				
				if (line.contains("{")) {
					stack.push(0);
				}

				if (line.trim().length() != 0) {
					int tmp = stack.pop();
					stack.push(tmp + 1);
				}
			}
			
			for (String lib : libs) {
				solveMultipleFile(lib, stack);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	private List<String> getContent(String srcName) {
		logger.info("预处理源代码开始...(" + srcName + ")");
		recorder.insertLine("预处理源代码开始...(" + srcName + ")");
		
		BufferedReader reader = null;
		List<String> codes = new ArrayList<>();

		try {
			reader =  new BufferedReader(new FileReader(srcDir + srcName));
			String line = null;
			while ((line = reader.readLine()) != null) {
				codes.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		codes = delComments(codes);
		recorder.insertLine("预处理源代码结束");
		logger.info("预处理源代码结束");
		
		return codes;
	}

	private List<String> delComments(List<String> codes) {

		int i = 0;
		int tmp = 1;
		while (i < codes.size()) {
			String line = codes.get(i);
			boolean isInDoubleQuote = false;
			for (int j = 0; j < line.length(); j++) {
				char ch = line.charAt(j);
				if (ch == '"') {
					isInDoubleQuote = !isInDoubleQuote;
					
					// 删去 // xxx 式注释
				} else if (ch == '/' && j + 1 < line.length() && line.charAt(j + 1) == '/' && !isInDoubleQuote) {
					// 规则 2.2（强制）： 源代码应该使用 /*…*/ 类型的注释
					System.err.print("Warning [" + tmp + "] : The security C subset does not allowed comments of type '//'!\n");
					
					line = line.substring(0, j);
					codes.remove(i);
					if (line.trim().length() != 0) {
						codes.add(i, line);
					} else {
						i--;
					}
					
					break;

					// 删除 /* xxx */ 式注释
				} else if (ch == '/' && j + 1 < line.length() && line.charAt(j + 1) == '*' && !isInDoubleQuote) {
					int k = j + 2;
					boolean isEnd = false;

					while (k < line.length()) {
						// 则 2.3（强制）： 字符序列 /* 不应出现在注释中。
						if (line.charAt(k) == '/' && k + 1 < line.length() && line.charAt(k + 1) == '*') {							
							try {
								throw new Exception("Error [" + i + "] : / * Should not appear in comments");
							} catch (Exception e) {
								e.printStackTrace();
								System.exit(1);
							}
						}
						
						if (line.charAt(k) == '*' && k + 1 < line.length() && line.charAt(k + 1) == '/') {
							isEnd = true;
							break;
						}
						k++;
					}

					// 一行的注释
					if (isEnd) {
						line = line.substring(0, j) + line.substring(k + 2);
						codes.remove(i);
						codes.add(i, line);

					// 多行的注释
					} else {
						int endLine = i + 1;
						for (; endLine < codes.size(); endLine++) {
							String str = codes.get(endLine);
							for (k = 0; k < str.length(); k++) {
								if (str.charAt(k) == '/' && k + 1 < str.length() && str.charAt(k + 1) == '*') {							
									try {
										throw new Exception("Error [" + endLine + "] : / * Should not appear in comments");
									} catch (Exception e) {
										e.printStackTrace();
										System.exit(1);
									}
								}
								
								if (str.charAt(k) == '*' && k + 1 < str.length() && str.charAt(k + 1) == '/') {
									break;
								}
							}
							if (k < str.length()) {
								break;
							}
						}

						line = line.substring(0, j);
						codes.remove(i);
						if (line.trim().length() != 0) {
							codes.add(i, line);
						} else {
							i--;
							endLine--;
						}

//						for (int t = i + 1; t < endLine; t++) {
//							codes.remove(t);
//							codes.add(t, "");
//						}

						line = codes.get(endLine);
						line = line.substring(k + 2);
						codes.remove(endLine);
						if (line.trim().length() != 0) {
							codes.add(endLine, line);
						}
						
						for (int t = i + 1; t < endLine; t++) {
							codes.remove(i + 1);
						}
						
					}

					i--;
					break;
				}
			}
			i++;
			tmp++;
		}

		return codes;
	}
	
	public void labelSrc(String fileName) {
		logger.info("源代码标号开始...");
		recorder.insertLine("源代码标号开始...");
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(
					new FileWriter(CommonsDefine.OUTPUT_PATH + "label_" + fileName));

			int len = 50;
			Stack<Integer> stack = new Stack<>();
			stack.push(1);
			int i = 0;
			while (i < srcs.size()) {
				String line = srcs.get(i);

				if (line.contains("}")) {
					stack.pop();
					int tmp = stack.pop();
					stack.push(tmp + 1);
				}

				if (line.trim().length() != 0) {
					String v = generateLabel(stack);
					line = LexerUtils.RTrim(line);
					for (int j = line.length(); j < len; j++) {
						line += " ";
					}

					if (v.trim().length() != 0) {
						line = line + "// " + v;
					}
				}

				writer.write(line);
				writer.newLine();

				if (line.contains("{")) {
					stack.push(0);
				}

				if (line.trim().length() != 0) {
					int tmp = stack.pop();
					stack.push(tmp + 1);
				}

				i++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		recorder.insertLine("源代码编号结束!");
		logger.info("源代码编号结束!");
		
	}
	
	public void outputLabelSrc() {
		recorder.insertLine("====================Labeled C Code==================");
		
		String path = CommonsDefine.OUTPUT_PATH + "label_" + srcName;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line = null;
			while ((line = reader.readLine()) != null) {
				recorder.insertLine(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (recorder != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		recorder.insertLine(null);		// 插入一个空行
	}

	// 输出源代码
	public void outputSrc() {
		recorder.insertLine("====================Source C Code==================");
		for (String str : srcs) {
			recorder.insertLine(str);
		}
		recorder.insertLine(null);
	}
	
	// 输出标号
	public void outputLabels() {
		for (String str : labels) {
			System.out.println(str);
		}
	}
	
	public void printAll() {
		for (String string : sources) {
			System.out.println(string);
		}
	}
	
	// 输出词法分析后的结果
	public void outputLexer() {
		BufferedWriter writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(CommonsDefine.DEBUG_PATH + "lexer.txt"));
			recorder.insertLine("====================Lexer==================");
			for (Token e : tokens) {
				writer.write("(" + e.getValue() + ", " + e.getType() + ", " + e.getLabel() + ")");
				writer.newLine();
				
				recorder.insertLine("(" + e.getValue() + ", " + e.getType() + ", " + e.getLabel() + ")");
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		recorder.insertLine(null);
	}

	public static void main(String[] args) {
		// 公共记录
		Recorder recorder = new Recorder();
		
		String srcPath = "conf/input/test9.c";
		Lexer lexer = new Lexer(srcPath, recorder);
		lexer.runLexer();
		lexer.outputSrc();
//		lexer.outputLabels();
		lexer.outputLabelSrc();
		lexer.outputLexer();
	}
}
