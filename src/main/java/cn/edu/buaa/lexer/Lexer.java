package cn.edu.buaa.lexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.buaa.constant.CommonsDefine;
import cn.edu.buaa.pojo.Token;

public class Lexer {
	
	private static final Logger logger = LoggerFactory.getLogger(Lexer.class);
	private static final String INPUT_PATH = "src/main/resources/input/";
	
	private List<String> src;
	private List<Token> tokens;

	public Lexer(String fileName) {
		tokens = new ArrayList<Token>();
		src = getContent(fileName);
	}

	public List<String> getSrc() {
		return src;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public void runLexer() {
		
		logger.info("Lexer analyze starting...");
		
		Stack<Integer> stack = new Stack<>();
		stack.push(1);
		for (String line : src) {

			line = line.trim();

			if (line.contains("}")) {
				stack.pop();
				int tmp = stack.pop();
				stack.push(tmp + 1);
			}

			if (line.length() != 0) {
				String label = generateLabel(stack);
				solveLine(line, label);
			}

			if (line.contains("{")) {
				stack.push(0);
			}

			if (line.length() != 0) {
				int tmp = stack.pop();
				stack.push(tmp + 1);
			}

		}
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

	private void solveLine(String line, String label) {

		int i = 0;
		Token token = null;
		while (i < line.length()) {
			i = LexerUtils.skipBlank(i, line);

			// 如果是引入头文件
			if (i == 0 && line.charAt(i) == '#') {
				token = new Token(4, line.charAt(i), label);
				tokens.add(token);

				i = LexerUtils.skipBlank(i + 1, line);
				// 匹配和处理"include"
				if ((i + 7) <= line.length() && line.substring(i, i + 7).equals("include")) {
					token = new Token(0, "include", label);
					tokens.add(token);

					i = LexerUtils.skipBlank(i + 7, line);
					if (line.charAt(i) == '\"' || line.charAt(i) == '<') {
						token = new Token(4, line.charAt(i), label);
						tokens.add(token);

						char close_flag = line.charAt(i) == '\"' ? '\"' : '>';
						i = LexerUtils.skipBlank(i + 1, line);

						// 找到include的头文件
						String lib = "";
						while (line.charAt(i) != close_flag) {
							lib += line.charAt(i);
							i++;
						}

						token = new Token(1, lib, label);
						tokens.add(token);
						token = new Token(4, close_flag, label);
						tokens.add(token);

						i = LexerUtils.skipBlank(i + 1, line);
						
					} else {
						try {
							int tmp = line.length() - i;
							if (tmp > 7) {
								tmp = 7;
							}
							throw new Exception("include error [" + label + "] : " + line.substring(i, i + tmp));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				} else {
					try {
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
					token = new Token(0, word, label);
					tokens.add(token);
					// 标识符
				} else {
					token = new Token(1, word, label);
					tokens.add(token);
				}
				i = LexerUtils.skipBlank(i, line);

				// 如果是数字开头
			} else if (Character.isDigit(line.charAt(i))) {
				String word = "";
				boolean pointExist = false;
				while (i < line.length()) {

					if (Character.isDigit(line.charAt(i))) {
						word += line.charAt(i);
					} else if (!pointExist && line.charAt(i) == '.' 
							&& Character.isDigit(line.charAt(i + 1))) {
						pointExist = true;
						word += line.charAt(i);
					} else {
						if (line.charAt(i) == '.') {
							try {
								throw new Exception("float number error [" + label + "] : " + line.substring(i));
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else
							break;
					}
					i++;
				}

				// 常量
				token = new Token(2, word, label);
				tokens.add(token);
				i = LexerUtils.skipBlank(i, line);

				// 如果是分隔符
			} else if (LexerUtils.isDelimiter(line.charAt(i))) {
				token = new Token(4, line.charAt(i), label);
				tokens.add(token);

				// 如果是字符串常量
				if (line.charAt(i) == '\"') {
					i++;
					String word = "";
					while (i < line.length() && line.charAt(i) != '\"') {
						word += line.charAt(i);
						i++;
					}

					if (i >= line.length()) {
						try {
							throw new Exception("Can't find the end character of the string constant [" + label + "]");
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						token = new Token(5, word, label);
						tokens.add(token);
						token = new Token(4, '\"', label);
						tokens.add(token);
					}
				}
				i = LexerUtils.skipBlank(i + 1, line);

				// 如果是运算符
			} else if (LexerUtils.isOperator(line.charAt(i))) {

				// 如果是++或者--
				if ((line.charAt(i) == '+' || line.charAt(i) == '-') && i + 1 < line.length()
						&& line.charAt(i) == line.charAt(i + 1)) {
					token = new Token(3, line.substring(i, i + 2), label);
					tokens.add(token);
					i = LexerUtils.skipBlank(i + 2, line);

					// 如果是>=或者<=
				} else if ((line.charAt(i) == '>' || line.charAt(i) == '<' || line.charAt(i) == '=')
						&& line.charAt(i + 1) == '=') {
					token = new Token(3, line.substring(i, i + 2), label);
					tokens.add(token);
					i = LexerUtils.skipBlank(i + 2, line);

				} else {
					token = new Token(3, line.charAt(i), label);
					tokens.add(token);
					i = LexerUtils.skipBlank(i + 1, line);
				}

			} else {
				try {
					throw new Exception("Unrecognized symbol [" + label + " ] : " + line.charAt(i));
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}

	}


	private List<String> getContent(String fileName) {
		BufferedReader reader = null;
		List<String> codes = new ArrayList<>();

		try {
			reader = new BufferedReader(new FileReader(INPUT_PATH + fileName));
			String line = null;
			while ((line = reader.readLine()) != null) {
				codes.add(line);
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

		codes = delComments(codes);

		return codes;
	}

	private List<String> delComments(List<String> codes) {

		int i = 0;
		while (i < codes.size()) {

			String line = codes.get(i);
			boolean isInDoubleQuote = false;
			for (int j = 0; j < line.length(); j++) {
				char ch = line.charAt(j);
				if (ch == '"') {
					isInDoubleQuote = !isInDoubleQuote;
					// 删去 // xxx 式注释
				} else if (ch == '/' && j + 1 < line.length() && line.charAt(j + 1) == '/' && !isInDoubleQuote) {
					line = line.substring(0, j);
					codes.remove(i);
					codes.add(i, line);
					break;

					// 删除 /* xxx */ 式注释
				} else if (ch == '/' && j + 1 < line.length() && line.charAt(j + 1) == '*' && !isInDoubleQuote) {
					int k = j + 2;
					boolean isEnd = false;

					while (k < line.length()) {
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
		}

		return codes;
	}



	public void labelSrc(String fileName) {

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(
					new FileWriter(CommonsDefine.OUTPUT_PATH + "/label_" + fileName));

			int len = 50;
			Stack<Integer> stack = new Stack<>();
			stack.push(1);
			int i = 0;
			while (i < src.size()) {
				String line = src.get(i);

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
	}

	public void outputSrc() {
		System.out.println("====================Source C Code==================");
		for (String str : src) {
			System.out.println(str);
		}
		System.out.println();
	}

	public void outputLexer() {
		BufferedWriter writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(CommonsDefine.OUTPUT_PATH + "lexer.txt"));
			System.out.println("====================Lexer==================");
			for (Token e : tokens) {
				writer.write("(" + e.getValue() + ", " + e.getType() + ", " + e.getLabel() + ")");
				writer.newLine();
				System.out.println("(" + e.getValue() + ", " + e.getType() + ", " + e.getLabel() + ")");
			}
			System.out.println();

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

	}

	public static void main(String[] args) {

		String fileName = "evenSum.c";

		Lexer lexer = new Lexer(fileName);
		lexer.outputSrc();
		lexer.labelSrc(fileName);
		lexer.runLexer();
		lexer.outputLexer();

	}

}
