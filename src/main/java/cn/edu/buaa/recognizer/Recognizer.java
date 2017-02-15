package cn.edu.buaa.recognizer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.buaa.constant.CommonsDefine;
import cn.edu.buaa.constant.RecognizerDefine;
import cn.edu.buaa.lexer.Lexer;
import cn.edu.buaa.pojo.SyntaxUnitCollections;
import cn.edu.buaa.pojo.SyntaxUnitNode;
import cn.edu.buaa.pojo.Token;
import cn.edu.buaa.recorder.Recorder;

public class Recognizer {
		
	private List<Token> tokens;
	private int index;
	private SyntaxUnitCollections collections;
	private Map<String, String> variableTable;
	private Map<String, String> globalVariableTable;
	private boolean isGlobal;
	
	private Map<String, String> functions;
	private Map<Token, List<Token>> recursions;	// <函数, 调用函数>
	private Token recur;
	
	private Recorder recorder;
	
	private final Logger logger = LoggerFactory.getLogger(Recognizer.class);
	
	public Recognizer(List<Token> tokens, Recorder recorder) {
		this.tokens = tokens;
		this.index = 0;
		this.collections = null;
		this.variableTable = new HashMap<>();
		this.globalVariableTable = new HashMap<>();
		this.functions = new HashMap<>();
		this.recursions = new HashMap<>();
		this.recorder = recorder;
	}
	
	public SyntaxUnitCollections getCollections() {
		return collections;
	}
	
	public String getTokenValue(int i) {
		return tokens.get(i).getValue();
	}
	
	public String getTokenType(int i) {
		return tokens.get(i).getType();
	}
	
	public String getTokenLabel(int i) {
		return tokens.get(i).getLabel();
	}
	
	// include句型
	private void _include(SyntaxUnitNode father) {
		
		if (father == null) {
			father = collections.getRoot();
		}
		
		SyntaxUnitCollections includeTree = new SyntaxUnitCollections();
		SyntaxUnitNode root = new SyntaxUnitNode("Include");
		includeTree.setRoot(root);
		includeTree.setCurrent(root);
		collections.addChildNode(root, father);
		
		// include语句中双引号的个数
		int cnt = 0;
		while (index < tokens.size()) {
			
			if (getTokenValue(index).equals("\"")) {
				cnt++;
			}
			
			SyntaxUnitNode node = new SyntaxUnitNode(
							getTokenValue(index), 
							getTokenType(index), 
							getTokenLabel(index) + "_in"
						);
			includeTree.addChildNode(node, root);
			
			if (cnt == 2 || getTokenValue(index).equals(">")) {
				index++;
				break;
			}
			
			index++;
		}
		
		if (index < tokens.size()) {
			recorder.insertLine(Recorder.TAB + "include语句 : 语法合法");
			logger.info("include语句 : 语法合法");
		} else {
			recorder.insertLine(Recorder.TAB + "include语句 : 语法非法");
			logger.info("include语句 : 语法非法");
			try {
				throw new Exception("include语句未正确结束");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
	}
	
	// 函数定义
	private void _functionStatement(SyntaxUnitNode father) {
		if (father == null) {
			father = collections.getRoot();
		}
		
		SyntaxUnitCollections funcStatementTree = new SyntaxUnitCollections();
		SyntaxUnitNode root = new SyntaxUnitNode("FunctionStatement");
		funcStatementTree.setRoot(root);
		funcStatementTree.setCurrent(root);
		collections.addChildNode(root, father);
		variableTable.clear();
		String funcName = "";
		
		while (index < tokens.size()) {
			// 如果是函数返回类型
			if (RecognizerUtils.isInnerDataType(getTokenValue(index))) {
				SyntaxUnitNode typeRoot = new SyntaxUnitNode("Type");
				funcStatementTree.addChildNode(typeRoot, root);
				
				HashMap<String, String> extraInfo = new HashMap<>();
				extraInfo.put("type", getTokenValue(index));
				funcStatementTree.addChildNode(
						new SyntaxUnitNode(
								getTokenValue(index), 
								"FIELD_TYPE", 
								extraInfo,
								getTokenLabel(index) + "_fs"), 
						typeRoot);
				index++;
				
			// 如果是函数名
			} else if (getTokenType(index).equals("IDENTIFIER")) {
				SyntaxUnitNode funcNameRoot = new SyntaxUnitNode("FunctionName");
				funcStatementTree.addChildNode(funcNameRoot, root);
				funcName = getTokenValue(index);
				recur = tokens.get(index);
				
				HashMap<String, String> extraInfo = new HashMap<>();
				extraInfo.put("type", "FUNCTION_NAME");
				funcStatementTree.addChildNode(
						new SyntaxUnitNode(
								getTokenValue(index), 
								"IDENTIFIER", 
								extraInfo,
								getTokenLabel(index) + "_fs"), 
						funcNameRoot);
				index++;
				
			// 如果是参数序列
			} else if (getTokenType(index).equals("LL_BRACKET")) {
				SyntaxUnitNode paramsList = new SyntaxUnitNode("FunctionParameterList");
				funcStatementTree.addChildNode(paramsList, root);
				
				index++;
				while (!getTokenType(index).equals("RL_BRACKET")) {
					if (RecognizerUtils.isInnerDataType(getTokenValue(index))) {
						SyntaxUnitNode param = new SyntaxUnitNode("Parameter");
						funcStatementTree.addChildNode(param, paramsList);
						
						// extra_info
						HashMap<String, String> extraInfo = new HashMap<>();
						extraInfo.put("type", getTokenValue(index));
						funcStatementTree.addChildNode(
								new SyntaxUnitNode(
										getTokenValue(index), 
										"FIELD_TYPE", 
										extraInfo,
										getTokenLabel(index) + "_fs"), 
								param);
						
						if (tokens.get(index + 1).getType().equals("IDENTIFIER")) {
							extraInfo = new HashMap<>();
							extraInfo.put("type", "VARIABLE");
							extraInfo.put("variable_type", getTokenValue(index));
							funcStatementTree.addChildNode(
									new SyntaxUnitNode(
											getTokenValue(index + 1), 
											"IDENTIFIER", 
											extraInfo,
											getTokenLabel(index + 1) + "_fs"), 
									param);
							variableTable.put(getTokenValue(index + 1), getTokenValue(index));
							
						} else {
							recorder.insertLine(Recorder.TAB + funcName + "函数定义 : 语法非法");
							logger.info(funcName + "函数定义 : 语法非法");
							try {
								throw new Exception(funcName + "函数参数定义错误, 未给出参数的标识符");
							} catch (Exception e) {
								e.printStackTrace();
								System.exit(1);
							}
						}
						index++;
					}
					index++;
				}
				index++;
				
			// 如果是遇见了左大括号
			} else if (getTokenType(index).equals("LB_BRACKET")) {
				// 规则 8.1（强制）： 函数应当具有原型声明，且原型在函数的定义和调用范围内都是可见的。
				if (!funcName.equals("main") && !functions.containsKey(funcName)) {
					try {
						throw new Exception(
								"Error [" + getTokenLabel(index) + "]: The function should have a prototype declaration '" + funcName + "'");
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				recursions.put(recur, new ArrayList<>());
				
				// 跳过左大括号
				index++;
				_block(funcStatementTree);
				break;
			
			// 如果是分号，即函数声明
			} else if (getTokenType(index).equals("SEMICOLON")) {
				SyntaxUnitNode end = new SyntaxUnitNode("SEMICOLON");
				funcStatementTree.addChildNode(end, root);
				functions.put(funcName, funcName);
				index++;
				break;
				
			} else {
				recorder.insertLine(Recorder.TAB + funcName + "函数定义 : 语法非法");
				logger.info(funcName + "函数定义 : 语法非法");
				try {
					throw new Exception("Error in functionStatement! : " + getTokenType(index));
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
				
			}
		}
		
		recorder.insertLine(Recorder.TAB + funcName + "函数定义 : 语法合法");
		logger.info(funcName + "函数定义 : 语法合法");
	}
	
	// 处理大括号里的部分
	private void _block(SyntaxUnitCollections fatherTree) {
		SyntaxUnitCollections sentenceTree = new SyntaxUnitCollections();
		SyntaxUnitNode root = new SyntaxUnitNode("Sentence");
		sentenceTree.setRoot(root);
		sentenceTree.setCurrent(root);
		fatherTree.addChildNode(root, fatherTree.getRoot());
		
		while(true) {
			String sentencePattern = judgeSentencePattern();
			// 声明语句
			if(sentencePattern.equals("STATEMENT")) {
				_statement(root);
				
			// 赋值语句
			} else if(sentencePattern.equals("ASSIGNMENT")) {
				_assignment(root);
				
			// 函数调用
			} else if(sentencePattern.equals("FUNCTION_CALL")) {
				_functionCall(root);
				
			// 控制流语句
			} else if(sentencePattern.equals("CONTROL")) {
				_control(root);
				
			// return语句
			} else if(sentencePattern.equals("RETURN")) {
				_return(root);
				
			// 自增或自减运算
			} else if(sentencePattern.equals("SELF_OPT")) {
				_expression(root, null);
				index++;
			
			// 双目运算 
			} else if (sentencePattern.equals("DOUBLE_OPT")) {
				_expression(root, null);
				index++;
				
			// 右大括号，函数结束
			} else if(sentencePattern.equals("RB_BRACKET")) {
				break;
				
			} else {
				try {
					throw new Exception("Block Error : " + sentencePattern);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
	}

	// 声明语句(变量声明、数组声明)
	private void _statement(SyntaxUnitNode father) {
		if (father == null) {
			father = collections.getRoot();
		}
				
		SyntaxUnitCollections statementTree = new SyntaxUnitCollections();
		SyntaxUnitNode root = new SyntaxUnitNode("Statement");
		statementTree.setRoot(root);
		statementTree.setCurrent(root);
		collections.addChildNode(root, father);
		
		// 暂时用来保存当前声明语句的类型，以便于识别多个变量的声明
		String tmpVariableType = null;
		while (index < tokens.size() && !getTokenType(index).equals("SEMICOLON")) {
			
			// 变量类型
			if (RecognizerUtils.isInnerDataType(getTokenValue(index))) {
				tmpVariableType = getTokenValue(index);
				SyntaxUnitNode variableType = new SyntaxUnitNode("Type");
				statementTree.addChildNode(variableType, root);
				
				HashMap<String, String> extraInfo = new HashMap<>();
				extraInfo.put("type", getTokenValue(index));
				statementTree.addChildNode(
						new SyntaxUnitNode(
								getTokenValue(index), 
								"FIELD_TYPE", 
								extraInfo,
								getTokenLabel(index) + "_st"), 
						variableType);
				
			// 变量名
			} else if (getTokenType(index).equals("IDENTIFIER")) {
				HashMap<String, String> extraInfo = new HashMap<>();
				extraInfo.put("type", "VARIABLE");
				extraInfo.put("variable_type", tmpVariableType);
				statementTree.addChildNode(
						new SyntaxUnitNode(
								getTokenValue(index), 
								"IDENTIFIER", 
								extraInfo,
								getTokenLabel(index) + "_st"), 
						root);
				if (isGlobal) {
					if (globalVariableTable.containsKey(getTokenValue(index))) {
						try {
							throw new Exception(
									"Error ["+ getTokenLabel(index) + "] : Repeat variable definition " 
											+ getTokenValue(index));
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(1);
						}
						
					} else if (variableTable.containsKey(getTokenValue(index))) {
						System.err.print("Warning ["+ getTokenLabel(index) 
							+ "] : the identifier of the internal scope should not be the same as the identifier with an external scope! '" 
								+ getTokenValue(index) + "'\n");
					}
					
					globalVariableTable.put(getTokenValue(index), tmpVariableType);
				} else {
					if (variableTable.containsKey(getTokenValue(index))) {
						try {
							throw new Exception(
									"Error ["+ getTokenLabel(index) + "] : Repeat variable definition " 
											+ getTokenValue(index));
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(1);
						}
						
					} else if (globalVariableTable.containsKey(getTokenValue(index))) {
						System.err.print("Warning ["+ getTokenLabel(index) 
						+ "] : the identifier of the internal scope should not be the same as the identifier with an external scope! '" 
							+ getTokenValue(index) + "'\n");
					}
					
					variableTable.put(getTokenValue(index), tmpVariableType);
				}
				
			// 数组大小	
			} else if (getTokenType(index).equals("DIGIT_CONSTANT")) {
				HashMap<String, String> extraInfo = new HashMap<>();
				extraInfo.put("type", "LIST");
				extraInfo.put("list_type", tmpVariableType);
				statementTree.addChildNode(
						new SyntaxUnitNode(
								getTokenValue(index), 
								"DIGIT_CONSTANT",
								extraInfo,
								getTokenLabel(index) + "_st"), 
						root);
			
			// 数组元素
			} else if (getTokenType(index).equals("LB_BRACKET")) {
				index++;
				SyntaxUnitNode constantList = new SyntaxUnitNode("ConstantList");
				statementTree.addChildNode(constantList, root);
				
				while(!getTokenType(index).equals("RB_BRACKET")) {
					if(getTokenType(index).equals("DIGIT_CONSTANT")) {
						statementTree.addChildNode(
								new SyntaxUnitNode(
										tokens.get(index).getValue(), 
										"DIGIT_CONSTANT", 
										null,
										getTokenLabel(index) + "_st"), 
								constantList);
						
					} else {
						recorder.insertLine(Recorder.TAB + "变量声明语句 : 语法非法");
						logger.info("变量声明语句 : 语法非法");
						try {
							throw new Exception("Error in array declare! : " + getTokenLabel(index));
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
					index++;
				}
				
			// 多个变量声明
			} else if (getTokenType(index).equals("COMMA")) {
				while (!getTokenType(index).equals("SEMICOLON")) {
					if (getTokenType(index).equals("IDENTIFIER")) {
						SyntaxUnitCollections tmpTree = new SyntaxUnitCollections();
						tmpTree.setRoot(new SyntaxUnitNode("Statement"));
						tmpTree.setCurrent(tmpTree.getRoot());
						collections.addChildNode(tmpTree.getRoot(), father);
						
						// 类型
						SyntaxUnitNode variableType = new SyntaxUnitNode("Type");
						tmpTree.addChildNode(variableType, null);
						
						// extra_info
						HashMap<String, String> extraInfo = new HashMap<>();
						extraInfo.put("type", tmpVariableType);
						tmpTree.addChildNode(
								new SyntaxUnitNode(
										tmpVariableType, 
										"FIELD_TYPE", 
										extraInfo,
										null), 
								null);
						
						// 变量名
						extraInfo = new HashMap<>();
						extraInfo.put("type", "VARIABLE");
						extraInfo.put("variable_type", tmpVariableType);
						tmpTree.addChildNode(
								new SyntaxUnitNode(
										getTokenValue(index), 
										"IDENTIFIER", 
										extraInfo,
										getTokenLabel(index) + "_st"), 
								tmpTree.getRoot());
						
						if (isGlobal) {
							if (globalVariableTable.containsKey(getTokenValue(index))) {
								try {
									throw new Exception(
											"Error ["+ getTokenLabel(index) + "] : Repeat variable definition " 
													+ getTokenValue(index));
								} catch (Exception e) {
									e.printStackTrace();
									System.exit(1);
								}
								
							} else if (variableTable.containsKey(getTokenValue(index))) {
								System.err.print("Warning ["+ getTokenLabel(index) 
								+ "] : the identifier of the internal scope should not be the same as the identifier with an external scope! '" 
									+ getTokenValue(index) + "'\n");
							}
							
							globalVariableTable.put(getTokenValue(index), tmpVariableType);
						} else {
							if (variableTable.containsKey(getTokenValue(index))) {
								try {
									throw new Exception(
											"Error ["+ getTokenLabel(index) + "] : Repeat variable definition " 
													+ getTokenValue(index));
								} catch (Exception e) {
									e.printStackTrace();
									System.exit(1);
								}
								
							} else if (globalVariableTable.containsKey(getTokenValue(index))) {
								System.err.print("Warning ["+ getTokenLabel(index) 
								+ "] : the identifier of the internal scope should not be the same as the identifier with an external scope! '" 
									+ getTokenValue(index) + "'\n");
							}
							
							variableTable.put(getTokenValue(index), tmpVariableType);
						}
						
					} else if (getTokenType(index).equals("COMMA")) { 
						// 继续执行
						
					} else {
						recorder.insertLine(Recorder.TAB + "变量声明语句 : 语法非法");
						logger.info("变量声明语句 : 语法非法");
						
						try {
							throw new Exception("Error in multiple variable statmement : " 
									+ getTokenValue(index) + " " + getTokenType(index));
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
					
					index++;
				}
				break;	// 到达了末尾的SEMICOLON
				
			} else {
				recorder.insertLine(Recorder.TAB + "变量声明语句 : 语法非法");
				logger.info("变量声明语句 : 语法非法");
				
				try {
					throw new Exception("Error in statement!");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
			index++;
		}
		index++;
		
		recorder.insertLine(Recorder.TAB + "变量声明语句 : 语法合法");
		logger.info("变量声明语句 : 语法合法");
	}

	// 赋值语句
	private void _assignment(SyntaxUnitNode father) {
		if(father == null) {
			father = collections.getRoot();
		}
		
		SyntaxUnitCollections assignTree = new SyntaxUnitCollections();
		SyntaxUnitNode root = new SyntaxUnitNode("Assignment");
		assignTree.setRoot(root);
		assignTree.setCurrent(root);
		collections.addChildNode(root, father);
				
		while(!getTokenType(index).equals("SEMICOLON")) {	
			// 被赋值的变量
			if(getTokenType(index).equals("IDENTIFIER")) {
				if (!variableTable.containsKey(getTokenValue(index)) 
						&& !globalVariableTable.containsKey(getTokenValue(index))) {
					try {
						throw new Exception(
								"Undefined variable [" + getTokenLabel(index) + "] : " + getTokenValue(index));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
				
				assignTree.addChildNode(
						new SyntaxUnitNode(
								getTokenValue(index), 
								"IDENTIFIER", 
								null,
								getTokenLabel(index) + "_as"), 
						null);
				index++;
				
			// 等于号的右边为表达式
			} else if(getTokenType(index).equals("ASSIGN")) {
				index++;
				_expression(root, null);
				
			} else {
				recorder.insertLine(Recorder.TAB + "赋值语句 : 语法非法");
				logger.info("赋值语句 : 语法非法");
				
				try {
					throw new Exception("非法的赋值语句 : " + getTokenType(index));
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
				
			}
		}
		index++;
		
		recorder.insertLine(Recorder.TAB + "赋值语句 : 语法合法");
		logger.info("赋值语句 : 语法合法");
	}
	
	// while语句
	private void _while(SyntaxUnitNode father, String label) {
		
		SyntaxUnitCollections whileTree = new SyntaxUnitCollections();
		SyntaxUnitNode root = new SyntaxUnitNode(
				"Control", 
				"WhileControl", 
				null, 
				label + "_wh"
				);
		whileTree.setRoot(root);
		whileTree.setCurrent(root);
		collections.addChildNode(root, father);
		
		index++;
		if (getTokenType(index).equals("LL_BRACKET")) {
			index++;
			int tmpIndex = index;
			while (!getTokenType(tmpIndex).equals("LB_BRACKET") 
					&& !getTokenType(tmpIndex).equals("SEMICOLON")) {
				tmpIndex++;
			}
			tmpIndex--;
			_expression(root, tmpIndex);
			index++;
			
			// 为左大括号，while的主体
			if (getTokenType(index).equals("LB_BRACKET")) {
				index++;
				_block(whileTree);
				
			} else {
				recorder.insertLine(Recorder.TAB + "while语句 : 语法非法");
				logger.info("while语句 : 语法非法");
				try {
					throw new Exception(
							"Error [" + getTokenLabel(index) + "] : while statement shall be a compound statement!({..})");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
		} else {
			recorder.insertLine(Recorder.TAB + "while语句 : 语法非法");
			logger.info("while语句 : 语法非法");
			try {
				throw new Exception("while statement error [" + getTokenLabel(index) + "] : " + getTokenType(index));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		recorder.insertLine(Recorder.TAB + "while语句 : 语法合法");
		logger.info("while语句 : 语法合法");
	}
	
	// do-while语句
	private void _doWhile(SyntaxUnitNode father, String label) {
		
		SyntaxUnitCollections doWhileTree = new SyntaxUnitCollections();
		SyntaxUnitNode root = new SyntaxUnitNode(
				"Control", 
				"DoWhileControl", 
				null, 
				label + "_dw"
				);
		doWhileTree.setRoot(root);
		doWhileTree.setCurrent(root);
		collections.addChildNode(root, father);
		
		index++;
		// do后面必须为 {
		if (getTokenType(index).equals("LB_BRACKET")) {
			index++;
			_block(doWhileTree);
			
			// {..}后必须为while
			if (getTokenType(index).equals("WHILE")) {
				index++;
			} else {
				recorder.insertLine(Recorder.TAB + "do-while语句 : 语法非法");
				logger.info("do-while语句 : 语法非法");
				try {
					throw new Exception("Invalid do while [" + getTokenLabel(index) + "] : " + getTokenType(index));
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
			if (getTokenType(index).equals("LL_BRACKET")) {
				index++;
				int tmpIndex = index;
				while (!getTokenType(tmpIndex).equals("LB_BRACKET") 
						&& !getTokenType(tmpIndex).equals("SEMICOLON")) {
					tmpIndex++;
				}
				tmpIndex--;
				_expression(root, tmpIndex);
				index += 2;  // 跳过 );
				
			} else {
				recorder.insertLine(Recorder.TAB + "do-while语句 : 语法非法");
				logger.info("do-while语句 : 语法非法");
				try {
					throw new Exception("do-while statement error [" + getTokenLabel(index) + "] : " + getTokenType(index));
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
		} else {
			recorder.insertLine(Recorder.TAB + "do-while语句 : 语法非法");
			logger.info("do-while语句 : 语法非法");
			try {
				throw new Exception("error in do-while [" + getTokenLabel(index) + "] : " + getTokenType(index));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		recorder.insertLine(Recorder.TAB + "do-while语句 : 语法合法");
		logger.info("do-while语句 : 语法合法");
	}
	
	// if语句
	private void _if_else(SyntaxUnitNode father, String label) {
		
		SyntaxUnitCollections ifElseTree = new SyntaxUnitCollections();
		SyntaxUnitNode root = new SyntaxUnitNode("Control", "IfElseControl", null, null);
		ifElseTree.setRoot(root);
		ifElseTree.setCurrent(root);
		collections.addChildNode(root, father);
		
		SyntaxUnitCollections ifTree = new SyntaxUnitCollections();
		SyntaxUnitNode ifRoot = new SyntaxUnitNode(
				"IfControl", 
				null, 
				null, 
				label + "_if"
				);
		ifTree.setRoot(ifRoot);
		ifTree.setCurrent(ifRoot);
		ifElseTree.addChildNode(ifRoot, root);
		
		// if标识
		if (getTokenType(index).equals("IF")) {
			index++;
			
			// 左小括号
			if (getTokenType(index).equals("LL_BRACKET")) {
				index++;
				
				// 右小括号位置
				int tmpIndex = index;
				while(!getTokenType(tmpIndex).equals("LB_BRACKET") 
						&& !getTokenType(tmpIndex).equals("SEMICOLON")) {
					tmpIndex++;
				}
				_expression(ifRoot, tmpIndex - 1);
				index++;
				
			} else {
				recorder.insertLine(Recorder.TAB + "if-else语句 : 语法非法");
				logger.info("if-else语句 : 语法非法");
				try {
					throw new Exception("error : lack of left less bracket!");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
			// 处理大括号中的
			if(getTokenType(index).equals("LB_BRACKET")) {
				index++;
				_block(ifTree);
				
			} else {
				recorder.insertLine(Recorder.TAB + "if-else语句 : 语法非法");
				logger.info("if-else语句 : 语法非法");
				try {
					throw new Exception(
							"Error [" + getTokenLabel(index) + "] : if statement shall be a compound statement!({..})");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
			
		} else {
			recorder.insertLine(Recorder.TAB + "if-else语句 : 语法非法");
			logger.info("if-else语句 : 语法非法");
			try {
				throw new Exception("error : Lack 'if' in if statement!");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		// 如果有else关键字，就接着处理
		if (getTokenType(index).equals("ELSE")) {
			index++;
			
			SyntaxUnitCollections elseTree = new SyntaxUnitCollections();
			SyntaxUnitNode elseRoot = new SyntaxUnitNode(
					"ElseControl", 
					null, 
					null, 
					getTokenLabel(index) + "_el"
					);
			elseTree.setRoot(elseRoot);
			elseTree.setCurrent(elseRoot);
			ifElseTree.addChildNode(elseRoot, root);
			
			// 左大括号
			 if(getTokenType(index).equals("LB_BRACKET")) {
				 index++;
				 _block(elseTree);
				 
			 } else {
				 recorder.insertLine(Recorder.TAB + "if-else语句 : 语法非法");
				 logger.info("if-else语句 : 语法非法");
				 try {
					 throw new Exception(
								"Error [" + getTokenLabel(index) + "] : else statement shall be a compound statement!({..})");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			 }
			 
		}
		
		recorder.insertLine(Recorder.TAB + "if-else语句 : 语法合法");
		logger.info("if-else语句 : 语法合法");
		
	}
	
	// for语句
	private void _for(SyntaxUnitNode father, String label) {
		
		SyntaxUnitCollections forTree = new SyntaxUnitCollections();
		SyntaxUnitNode root = new SyntaxUnitNode(
				"Control", 
				"ForControl", 
				null, 
				label + "_fo"
				);
		forTree.setRoot(root);
		forTree.setCurrent(root);
		forTree.addChildNode(root, father);
		
		if (getTokenType(index).equals("FOR")) {
			index++;
			
			if (getTokenType(index).equals("LL_BRACKET")) {
				index++;
				
				// 首先找到右小括号的位置
				int tmpIndex = index;
				while(!getTokenType(tmpIndex).equals("LB_BRACKET")) {
					tmpIndex++;
				}
				tmpIndex--;
				
				// for语句中的第一个分号前的部分
				_assignment(root);
				
				// 两个分号中间的部分
				_expression(root, null);
				index++;
				
				// 第二个分号后的部分
				_expression(root, tmpIndex);
				index++;
				
			} else {
				recorder.insertLine(Recorder.TAB + "for语句 : 语法非法");
				logger.info("for语句 : 语法非法");
				try {
					throw new Exception("error : lack of left less bracket!");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
			// 处理for的大括号的部分，如果为左大括号
			if (getTokenType(index).equals("LB_BRACKET")) {
				// 跳过左大括号
				index++;
				_block(forTree);
				
			} else {
				recorder.insertLine(Recorder.TAB + "for语句 : 语法非法");
				logger.info("for语句 : 语法非法");
				try {
					throw new Exception(
							"Error [" + getTokenLabel(index) + "] : for statement shall be a compound statement!({..})");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
			// 交换for语句下第三个子节点和第四个子节点
			SyntaxUnitNode currentNode = forTree.getRoot().getFirstSon().getRight().getRight();
			SyntaxUnitNode nextNode = currentNode.getRight();
			forTree.switchTwoSubTree(currentNode, nextNode);
			
		} else {
			recorder.insertLine(Recorder.TAB + "for语句 : 语法非法");
			logger.info("for语句 : 语法非法");
			try {
				throw new Exception("error : lack for in the statement");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		recorder.insertLine(Recorder.TAB + "for语句 : 语法合法");
		logger.info("for语句 : 语法合法");
	}
	
	// 处理控制语句
	private void _control(SyntaxUnitNode father) {
		
		String tokenType = getTokenType(index);
		
		if(tokenType.equals("WHILE")) {
			_while(father, getTokenLabel(index));
			
		} else if (tokenType.equals("DO")) { 
			_doWhile(father, getTokenLabel(index));
			
		} else if(tokenType.equals("IF")) {
			_if_else(father, getTokenLabel(index));
			
		} else if(tokenType.equals("FOR")) {
			_for(father, getTokenLabel(index));
			
		} else {
			try {
				throw new Exception("error: control style not supported!");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
	}
	
	// return语句
	private void _return(SyntaxUnitNode father) {
		if (father == null) {
			father = collections.getRoot();
		}
		
		SyntaxUnitCollections returnTree = new SyntaxUnitCollections();
		SyntaxUnitNode root = new SyntaxUnitNode("Return");
		returnTree.setRoot(root);
		returnTree.setCurrent(root);
		returnTree.addChildNode(root, father);
		
		if (getTokenType(index).equals("RETURN")) {
			SyntaxUnitNode returnNode = new SyntaxUnitNode(
					getTokenValue(index), 
					getTokenType(index), 
					null, 
					getTokenLabel(index) + "_re"
					);
			returnTree.addChildNode(returnNode, root);
			index++;
			
		} else {
			recorder.insertLine(Recorder.TAB + "return语句 : 语法非法");
			logger.info("return语句 : 语法非法");
			try {
				throw new Exception("error : lack return in the statement!");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			
		}
		
		// 处理return后面返回的语句
		_expression(root, null);
		index++;
		
		recorder.insertLine(Recorder.TAB + "return语句 : 语法合法");
		logger.info("return语句 : 语法合法");
	}
	
	// 表达式
	private void _expression(SyntaxUnitNode father, Integer ind) {
		if (father == null) {
			father = collections.getRoot();
		}
		
		// 如果是函数调用
		if (getTokenType(index).equals("IDENTIFIER") 
				&& getTokenType(index + 1).equals("LL_BRACKET")) {
			_functionCall(father);
			index--;
			return;
		}
		
		// 运算符栈
		Stack<SyntaxUnitCollections> operatorStack = new Stack<SyntaxUnitCollections>();
		// 转换成的逆波兰表达式结果
		List<SyntaxUnitCollections> reversePolishExpression = new ArrayList<SyntaxUnitCollections>();
	
		// 中缀表达式转为后缀表达式，即逆波兰表达
		while (!getTokenType(index).equals("SEMICOLON") 
				&& !getTokenType(index).equals("COMMA")) {
			if (ind != null && index >= ind) {
				break;
			}
			
			// 如果是数字常量
			if (getTokenType(index).equals("DIGIT_CONSTANT")) {
				SyntaxUnitCollections tmpTree = new SyntaxUnitCollections();
				SyntaxUnitNode constantRoot = new SyntaxUnitNode("Expression", "Constant", null, null);
				tmpTree.setRoot(constantRoot);
				tmpTree.setCurrent(constantRoot);
				
				SyntaxUnitNode node = new SyntaxUnitNode(
						getTokenValue(index), 
						"_Constant", 
						null, 
						getTokenLabel(index) + "_ex"
						);
				tmpTree.addChildNode(node, null);
				reversePolishExpression.add(tmpTree);
				
			// 如果是变量或者数组的某元素
			} else if (getTokenType(index).equals("IDENTIFIER")) {
				// 变量
				if (RecognizerUtils.isOperator(getTokenValue(index + 1))
						|| getTokenType(index + 1).equals("SEMICOLON")
						|| getTokenType(index + 1).equals("RL_BRACKET")) {
					
					if (!variableTable.containsKey(getTokenValue(index))
							&& !globalVariableTable.containsKey(getTokenValue(index))) {
						try {
							throw new Exception(
									"Undefined variable [" + getTokenLabel(index) + "] : " + getTokenValue(index));
		
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(1);
						}
						
					}
					
					SyntaxUnitCollections tmpTree = new SyntaxUnitCollections();
					SyntaxUnitNode variableRoot = new SyntaxUnitNode("Expression", "Variable", null, null);
					tmpTree.setRoot(variableRoot);
					tmpTree.setCurrent(variableRoot);
					
					SyntaxUnitNode node = new SyntaxUnitNode(
							getTokenValue(index), 
							"_Variable", 
							null, 
							getTokenLabel(index) + "_ex"
							);
					tmpTree.addChildNode(node, null);
					reversePolishExpression.add(tmpTree);
					
				// 数组的某一个元素ID[i]
				} else if (getTokenType(index + 1).equals("LM_BRACKET")) {
					SyntaxUnitCollections tmpTree = new SyntaxUnitCollections();
					SyntaxUnitNode arrayItemRoot = new SyntaxUnitNode("Expression", "ArrayItem", null, null);
					tmpTree.setRoot(arrayItemRoot);
					tmpTree.setCurrent(arrayItemRoot);
					
					// 数组的名字
					SyntaxUnitNode node = new SyntaxUnitNode(
							getTokenValue(index), 
							"_ArrayName", 
							null, 
							getTokenLabel(index) + "_ex"
							);
					tmpTree.addChildNode(node, null);
					
					index += 2;
					if (getTokenType(index).equals("DIGIT_CONSTANT")
							|| getTokenType(index).equals("IDENTIFIER")) {
						node = new SyntaxUnitNode(
								getTokenValue(index), 
								"_ArrayIndex", 
								null, 
								getTokenLabel(index) + "_ex"
								);
						tmpTree.addChildNode(node, null);
						reversePolishExpression.add(tmpTree);
						
					} else {
						recorder.insertLine(Recorder.TAB + "表达式语句 : 语法非法");
						logger.info("表达式语句 : 语法非法");
						try {
							throw new Exception("error: 数组下表必须为常量或标识符 : " + getTokenType(index));
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
					
				} else {
					recorder.insertLine(Recorder.TAB + "表达式语句 : 语法非法");
					logger.info("表达式语句 : 语法非法");
					try {
						throw new Exception(
								"not support identifer ["+ getTokenLabel(index) + "] : " + getTokenValue(index + 1));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			
			// 如果是运算符
			} else if (RecognizerUtils.isOperator(getTokenValue(index))
					|| getTokenType(index).equals("LL_BRACKET")
					|| getTokenType(index).equals("RL_BRACKET")) {
				SyntaxUnitCollections tmpTree = new SyntaxUnitCollections();
				SyntaxUnitNode root = new SyntaxUnitNode("Operator", "Operator", null, null);
				tmpTree.setRoot(root);
				tmpTree.setCurrent(root);
				
				SyntaxUnitNode node = new SyntaxUnitNode(
						getTokenValue(index), 
						"_Operator", 
						null, 
						getTokenLabel(index) + "_ex"
						);
				tmpTree.addChildNode(node, null);
				
				// 如果是左括号，直接压栈
				if (getTokenType(index).equals("LL_BRACKET")) {
					operatorStack.push(tmpTree);
					
				// 如果是右括号，弹栈直到遇到左括号为止
				} else if (getTokenType(index).equals("RL_BRACKET")) {
					while (!operatorStack.empty() && !operatorStack.peek().getCurrent().getValue().equals("(")) {
						reversePolishExpression.add(operatorStack.pop());
					}
					
					// 将左括号弹出来
					if (!operatorStack.empty()) {
						operatorStack.pop();
					}
					
				// 只能是其它运算符
				} else {
					while (!operatorStack.empty()
							&& !operatorStack.peek().getCurrent().getValue().equals("(")
							&& RecognizerDefine.OPERATOR_PRIORITY.get(tmpTree.getCurrent().getValue())
								< RecognizerDefine.OPERATOR_PRIORITY.get(operatorStack.peek().getCurrent().getValue())) {
						reversePolishExpression.add(operatorStack.pop());
						
					}
					operatorStack.add(tmpTree);
				}
				
			} else if(getTokenType(index).equals("COMMA")) {
				try {
					throw new Exception(
							"Error : [" + getTokenLabel(index) + "]The security C subset does not allowed ',' operator!");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			
			} else {
				recorder.insertLine(Recorder.TAB + "表达式语句 : 语法非法");
				logger.info("表达式语句 : 语法非法");
				try {
					throw new Exception("Unsupport character in the expression! : " + getTokenValue(index));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			
			index++;
		}
		
		// 最后将符号栈清空，最终得到逆波兰表达式reverse_polish_expression
		while (!operatorStack.empty()) {
			reversePolishExpression.add(operatorStack.pop());
			
		}
		
		// 把逆波兰表达式存入语法树
		SyntaxUnitCollections newTree = new SyntaxUnitCollections();
		SyntaxUnitNode newRoot = new SyntaxUnitNode("Expression", "SingleOrDoubleOperand", null, null);
		newTree.setRoot(newRoot);
		newTree.setCurrent(newRoot);
		for (SyntaxUnitCollections item : reversePolishExpression) {
			newTree.addChildNode(item.getRoot(), newRoot);
		}
		collections.addChildNode(newTree.getRoot(), father);
		
		recorder.insertLine(Recorder.TAB + "表达式语句 : 语法合法");
		logger.info("表达式语句 : 语法合法");
	}
	
	// 函数调用
	private void _functionCall(SyntaxUnitNode father) {
		if (father == null) {
			father = collections.getRoot();
		}
		
		SyntaxUnitCollections funcCallTree = new SyntaxUnitCollections();
		SyntaxUnitNode root = new SyntaxUnitNode("FunctionCall");
		funcCallTree.setRoot(root);
		funcCallTree.setCurrent(root);
		collections.addChildNode(root, father);
		
		while(!getTokenType(index).equals("SEMICOLON")) {
			// 函数名
			if(getTokenType(index).equals("IDENTIFIER")) {
				if (getTokenValue(index).equals("longjmp")) {
					try {
						throw new Exception(
								"Error [" + getTokenLabel(index) + 
								"] : The longjmp function shall not be used in the secure C! '" + getTokenValue(index) + "'");
					} catch (Exception e1) {
						e1.printStackTrace();
						System.exit(1);
					}
					
				}
				
				if (getTokenValue(index).equals("atof")
						|| getTokenValue(index).equals("atoi")
						|| getTokenValue(index).equals("atol")) {
					try {
						throw new Exception(
								"Error [" + getTokenLabel(index) 
								+ "] : The library functions atof, atoi and atol from library <stdlib.h> shall not be used! '" 
								+ getTokenValue(index) + "'");
					} catch (Exception e1) {
						e1.printStackTrace();
						System.exit(1);
					}
					
				}
				
				if ( getTokenValue(index).equals("abort")
						|| getTokenValue(index).equals("exit")
						|| getTokenValue(index).equals("getenv")
						|| getTokenValue(index).equals("system")) {
					try {
						throw new Exception(
								"Error [" + getTokenLabel(index) 
								+ "] : The library functions abort, exit, getenv and system from library <stdlib.h> shall not be used! '" 
								+ getTokenValue(index) + "'");
					} catch (Exception e1) {
						e1.printStackTrace();
						System.exit(1);
					}
					
				}
				
				funcCallTree.addChildNode(
							new SyntaxUnitNode(
									getTokenValue(index), 
									"FUNCTION_NAME", 
									null,
									getTokenLabel(index) + "_fc"), 
							null);
				
				// 封装函数调用名
				List<Token> tmpValues = recursions.get(recur);
				tmpValues.add(tokens.get(index));
				
			// 左小括号， 为函数参数
			} else if(tokens.get(index).getType().equals("LL_BRACKET")) {
				index++;
				SyntaxUnitNode paramsList = new SyntaxUnitNode("CallParameterList");
				funcCallTree.addChildNode(paramsList, funcCallTree.getRoot());

				while (!getTokenType(index).equals("RL_BRACKET")) {			
					if (getTokenType(index).equals("LL_BRACKET") ||
							getTokenType(index).equals("IDENTIFIER") 
							&& RecognizerUtils.isOperator(getTokenValue(index + 1))) {
						int limit = index;
						while (!getTokenType(limit).equals("LB_BRACKET") 
								&& !getTokenType(limit).equals("SEMICOLON")) {
							limit++;
						}
						_expression(paramsList, limit - 1);
						index--;
						
					} else if (getTokenType(index).equals("IDENTIFIER")
							|| getTokenType(index).equals("DIGIT_CONSTANT")
							|| getTokenType(index).equals("STRING_CONSTANT")) {
						
						if (getTokenType(index).equals("IDENTIFIER") 
								&& !variableTable.containsKey(getTokenValue(index))
								&& !globalVariableTable.containsKey(getTokenValue(index))) {
							try {
								throw new Exception(
										"Undefined variable [" + getTokenLabel(index) + "] : " + getTokenValue(index));
							} catch (Exception e) {
								e.printStackTrace();
								System.exit(1);
							}
						
						}
						
						funcCallTree.addChildNode(
								new SyntaxUnitNode(
										getTokenValue(index), 
										getTokenType(index),
										null,
										getTokenLabel(index) + "_fc"),
								paramsList);
						
					} else if (getTokenType(index).equals("DOUBLE_QUOTE")) {
						index++;
						funcCallTree.addChildNode(
								new SyntaxUnitNode(
										getTokenValue(index), 
										getTokenType(index),
										null,
										getTokenLabel(index) + "_fc"),
								paramsList);
						index++;
						
					} else if (getTokenType(index).equals("BIT_AND")) {
						funcCallTree.addChildNode(
								new SyntaxUnitNode(
										getTokenValue(index),
										"BIT_AND", 
										null,
										getTokenLabel(index) + "_fc"),
								paramsList);
						
					} else if (getTokenType(index).equals("COMMA")) {
						// do nothing
						
					} else {
						recorder.insertLine(Recorder.TAB + "函数调用语句 : 语法非法");
						logger.info("函数调用语句 : 语法非法");
						try {
							throw new Exception(
									"functionCall statement not support : " + getTokenType(index));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							System.exit(1);
						}
						
					}
					index++;
				}
				
			} else {
				recorder.insertLine(Recorder.TAB + "函数调用语句 : 语法非法");
				logger.info("函数调用语句 : 语法非法");
				try {
					throw new Exception("function call error");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
			index++;
		}
		index++;
		
		recorder.insertLine(Recorder.TAB + "函数调用语句 : 语法合法");
		logger.info("函数调用语句 : 语法合法");
	}
	
	// 根据一个语句的句首判断句型
	public String judgeSentencePattern() {
		
		String tokenValue = getTokenValue(index);
		String tokenType = getTokenType(index);
				
		// include句型
		if (tokenType.equals("SHARP") && getTokenType(index + 1).equals("INCLUDE")) {
			return "INCLUDE";
			
		// 控制句型
		} else if (RecognizerUtils.isControl(tokenValue)) {
			return "CONTROL";
			
		// 可能是声明语句或函数声明语句
		} else if (RecognizerUtils.isInnerDataType(tokenValue) 
				&& getTokenType(index + 1).equals("IDENTIFIER")) {
			String index2TokenType = getTokenType(index + 2);
						
			if(index2TokenType.equals("LL_BRACKET")) {
				return "FUNCTION_STATEMENT";
			} else if (index2TokenType.equals("SEMICOLON") || index2TokenType.equals("LM_BRACKET")
					|| index2TokenType.equals("COMMA")) {
				return "STATEMENT";
			} else {
				return "ERROR";
			}
			
		// 可能为函数调用或者赋值语句或单目运算	
		} else if (tokenType.equals("IDENTIFIER")) {
			String index1TokenType = getTokenType(index + 1);
			String index1TokenValue = getTokenValue(index + 1);
			
			if(index1TokenType.equals("LL_BRACKET")) {
				return "FUNCTION_CALL";
			} else if(index1TokenType.equals("ASSIGN")) {
				return "ASSIGNMENT";
			} else if(index1TokenType.equals("SELF_PLUS") 
					|| index1TokenType.equals("SELF_MINUS")) { 
				return "SELF_OPT";
			} else if (!index1TokenType.equals("SELF_PLUS") 
					&& !index1TokenType.equals("SELF_MINUS")
					&& RecognizerUtils.isOperator(index1TokenValue)) { 
				return "DOUBLE_OPT";
			} else {
				return "ERROR";
			}
		
		// return语句
		} else if (tokenType.equals("RETURN")) {
			return "RETURN";
			
		// 右大括号，表明函数的结束
		} else if (tokenType.equals("RB_BRACKET")) {
			index++;
			return "RB_BRACKET";
		
		// 其它的为错误
		} else {
			return "ERROR : (" + getTokenType(index) + ", " + getTokenValue(index) + ", " + getTokenLabel(index) + ")";
		}
		
	}	

	public void runRecognizer() {
		
		logger.info("=========Recognizer=========");
		logger.info("文法单元识别开始...");
		recorder.insertLine("文法单元识别开始...");
		
		// 创建树的根节点
		SyntaxUnitNode root = new SyntaxUnitNode("Sentence");
		collections = new SyntaxUnitCollections(root);
		collections.setCurrent(root);
		
		// 遍历所有的token
		while (index < tokens.size()) {
			String sentencePattern = judgeSentencePattern();
						
			// include语句
			if(sentencePattern.equals("INCLUDE")) {
				_include(root);
				
			// 函数定义语句
			} else if(sentencePattern.equals("FUNCTION_STATEMENT")) {
				_functionStatement(root);
				
			// 声明语句
			} else if(sentencePattern.equals("STATEMENT")) {
				isGlobal = true;
				_statement(root);
				isGlobal = false;
				
			// 函数调用
			} else if(sentencePattern.equals("FUNCTION_CALL")) {
				_functionCall(root);
				
			} else {
				try {
					throw new Exception("Error token! ： \'" + sentencePattern + "\'");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		// 安全C检查
		secureCheck();
		
		recorder.insertLine("文法单元识别结束!");
		logger.info("文法单元识别结束!");
	}
	
	public void secureCheck() {
		for (Token entry : recursions.keySet()) {
			Stack<Token> visits = new Stack<>();
			visits.add(entry);
			if (!dfs(entry, visits)) {
				try {
					throw new Exception(
							"Error [" + entry.getLabel() + "] : Functions shall not call themselves, either directly or indirectly! '"+ entry.getValue() +"(..)'");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
	}
	
	private boolean dfs(Token key, Stack<Token> visits) {
		if (!recursions.containsKey(key)) {
			return true;
		}
		
		boolean flag = true;
		for (Token entry : recursions.get(key)) {			
			if (visits.contains(entry)) {
				return false;
			}
			visits.push(entry);
			if (!dfs(entry, visits)) {
				flag = false;
				break;
			}
			visits.pop();
		}
		
		return flag;
	}

	// 递归输出语法树
	private void display(SyntaxUnitNode node, BufferedWriter writer) {
		if(null == node) return;
		
		try {
			writer.write("( self: " + node.getValue() + " " + node.getType() + " " + node.getLabel()
								+ ", father: " + (node.getFather() == null ? null : node.getFather().getValue())
								+ ", left: " + (node.getLeft() == null ? null : node.getLeft().getValue())
								+ ", right: " + (node.getRight() == null ? null : node.getRight().getValue())
								+ " )");
			writer.newLine();
			
			recorder.insertLine("( self: " + node.getValue() + " " + node.getType() + " " + node.getLabel()
								+ ", father: " + (node.getFather() == null ? null : node.getFather().getValue())
								+ ", left: " + (node.getLeft() == null ? null : node.getLeft().getValue())
								+ ", right: " + (node.getRight() == null ? null : node.getRight().getValue())
								+ " )");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SyntaxUnitNode child = node.getFirstSon();
		while(child != null) {
			display(child, writer);
			child = child.getRight();
		}
	}
	
	public void outputRecognizer() {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(CommonsDefine.DEBUG_PATH + "recognizer.txt"));
			writer.write("====================Recognizer==================");
			writer.newLine();
			
			recorder.insertLine("====================Recognizer==================");
			display(collections.getRoot(), writer);
			
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
		
		recorder.insertLine(null);
	}

	public static void main(String[] args) {
		// 公共记录
		Recorder recorder = new Recorder();

		String srcPath = "conf/input/test9.c";
		Lexer lexer = new Lexer(srcPath, recorder);
		lexer.runLexer();
		lexer.outputSrc();
		lexer.outputLabelSrc();
		lexer.outputLexer();

		Recognizer parser = new Recognizer(lexer.getTokens(), recorder);
		parser.runRecognizer();
		parser.outputRecognizer();	
	}
}
