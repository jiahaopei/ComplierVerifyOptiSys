package cn.edu.buaa.parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import cn.edu.buaa.constant.CommonsDefine;
import cn.edu.buaa.constant.ParserDefine;
import cn.edu.buaa.lexer.Lexer;
import cn.edu.buaa.pojo.SyntaxTree;
import cn.edu.buaa.pojo.SyntaxTreeNode;
import cn.edu.buaa.pojo.Token;

public class Parser {
		
	private List<Token> tokens;
	private int index;
	private SyntaxTree tree;
	
	public Parser(List<Token> tokens) {
		this.tokens = tokens;
		this.index = 0;
		this.tree = null;
	}
	
	public SyntaxTree getTree() {
		return tree;
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
	private void _include(SyntaxTreeNode father) {
		if (father == null) {
			father = tree.getRoot();
		}
		
		SyntaxTree includeTree = new SyntaxTree();
		SyntaxTreeNode root = new SyntaxTreeNode("Include");
		includeTree.setRoot(root);
		includeTree.setCurrent(root);
		tree.addChildNode(root, father);
		
		// include语句中双引号的个数
		int cnt = 0;
		while (index < tokens.size()) {
			
			if (getTokenValue(index).equals("\"")) {
				cnt++;
			}
			
			SyntaxTreeNode node = new SyntaxTreeNode(
					getTokenValue(index), getTokenType(index), getTokenLabel(index));
			includeTree.addChildNode(node, root);
			
			if (cnt == 2 || getTokenValue(index).equals(">")) {
				index++;
				break;
			}
			
			index++;
		}
		
	}
	
	// 函数定义
	private void _functionStatement(SyntaxTreeNode father) {
		if (father == null) {
			father = tree.getRoot();
		}
		
		SyntaxTree funcStatementTree = new SyntaxTree();
		SyntaxTreeNode root = new SyntaxTreeNode("FunctionStatement");
		funcStatementTree.setRoot(root);
		funcStatementTree.setCurrent(root);
		tree.addChildNode(root, father);
		
		while (index < tokens.size()) {
			// 如果是函数返回类型
			if (ParserUtils.isInnerDataType(getTokenValue(index))) {
				SyntaxTreeNode typeRoot = new SyntaxTreeNode("Type");
				funcStatementTree.addChildNode(typeRoot, root);
				
				HashMap<String, String> extraInfo = new HashMap<>();
				extraInfo.put("type", getTokenValue(index));
				funcStatementTree.addChildNode(
						new SyntaxTreeNode(
								getTokenValue(index), 
								"FIELD_TYPE", 
								extraInfo,
								getTokenLabel(index)), 
						typeRoot);
				index++;
				
			// 如果是函数名
			} else if (getTokenType(index).equals("IDENTIFIER")) {
				SyntaxTreeNode funcNameRoot = new SyntaxTreeNode("FunctionName");
				funcStatementTree.addChildNode(funcNameRoot, root);

				HashMap<String, String> extraInfo = new HashMap<>();
				extraInfo.put("type", "FUNCTION_NAME");
				funcStatementTree.addChildNode(
						new SyntaxTreeNode(
								tokens.get(index).getValue(), 
								"IDENTIFIER", 
								extraInfo,
								getTokenLabel(index)), 
						funcNameRoot);
				index++;
				
			// 如果是参数序列
			} else if (getTokenType(index).equals("LL_BRACKET")) {
				SyntaxTreeNode paramsList = new SyntaxTreeNode("StateParameterList");
				funcStatementTree.addChildNode(paramsList, root);
				
				index++;
				while (!getTokenType(index).equals("RL_BRACKET")) {
					if (ParserUtils.isInnerDataType(getTokenValue(index))) {
						SyntaxTreeNode param = new SyntaxTreeNode("Parameter");
						funcStatementTree.addChildNode(param, paramsList);
						
						// extra_info
						HashMap<String, String> extraInfo = new HashMap<>();
						extraInfo.put("type", tokens.get(index).getValue());
						funcStatementTree.addChildNode(
								new SyntaxTreeNode(
										getTokenValue(index), 
										"FIELD_TYPE", 
										extraInfo,
										getTokenLabel(index)), 
								param);
						
						if (tokens.get(index + 1).getType().equals("IDENTIFIER")) {
							extraInfo = new HashMap<>();
							extraInfo.put("type", "VARIABLE");
							extraInfo.put("variable_type", getTokenValue(index));
							funcStatementTree.addChildNode(
									new SyntaxTreeNode(
											getTokenValue(index + 1), 
											"IDENTIFIER", 
											extraInfo,
											getTokenLabel(index + 1)), 
									param);
						} else {
							try {
								throw new Exception("函数定义参数错误");
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
				// 跳过左大括号
				index++;
				_block(funcStatementTree);
				
			} else {
				try {
					throw new Exception("Error in functionStatement! : [" + getTokenLabel(index) + "]");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
				
			}
		}
		
	}
	
	// 处理大括号里的部分
	private void _block(SyntaxTree fatherTree) {
		SyntaxTree sentenceTree = new SyntaxTree();
		SyntaxTreeNode root = new SyntaxTreeNode("Sentence");
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
	private void _statement(SyntaxTreeNode father) {
		if (father == null) {
			father = tree.getRoot();
		}
		
		SyntaxTree statementTree = new SyntaxTree();
		SyntaxTreeNode root = new SyntaxTreeNode("Statement");
		statementTree.setRoot(root);
		statementTree.setCurrent(root);
		tree.addChildNode(root, father);
		
		// 暂时用来保存当前声明语句的类型，以便于识别多个变量的声明
		String tmpVariableType = null;
		while (index < tokens.size() && !getTokenType(index).equals("SEMICOLON")) {
			
			// 变量类型
			if (ParserUtils.isInnerDataType(getTokenValue(index))) {
				tmpVariableType = getTokenValue(index);
				SyntaxTreeNode variableType = new SyntaxTreeNode("Type");
				statementTree.addChildNode(variableType, root);
				
				HashMap<String, String> extraInfo = new HashMap<>();
				extraInfo.put("type", getTokenValue(index));
				statementTree.addChildNode(
						new SyntaxTreeNode(
								getTokenValue(index), 
								"FIELD_TYPE", 
								extraInfo,
								getTokenLabel(index)), 
						variableType);
				
			// 变量名
			} else if (getTokenType(index).equals("IDENTIFIER")) {
				HashMap<String, String> extraInfo = new HashMap<>();
				extraInfo.put("type", "VARIABLE");
				extraInfo.put("variable_type", tmpVariableType);
				statementTree.addChildNode(
						new SyntaxTreeNode(
								getTokenValue(index), 
								"IDENTIFIER", 
								extraInfo,
								getTokenLabel(index)), 
						root);
			
			// 数组大小	
			} else if (getTokenType(index).equals("DIGIT_CONSTANT")) {
				HashMap<String, String> extraInfo = new HashMap<>();
				extraInfo.put("type", "LIST");
				extraInfo.put("list_type", tmpVariableType);
				statementTree.addChildNode(
						new SyntaxTreeNode(
								getTokenValue(index), 
								"DIGIT_CONSTANT",
								extraInfo,
								getTokenLabel(index)), 
						root);
			
			// 数组元素
			} else if (getTokenType(index).equals("LB_BRACKET")) {
				index++;
				SyntaxTreeNode constantList = new SyntaxTreeNode("ConstantList");
				statementTree.addChildNode(constantList, root);
				
				while(!getTokenType(index).equals("RB_BRACKET")) {
					if(getTokenType(index).equals("DIGIT_CONSTANT")) {
						statementTree.addChildNode(
								new SyntaxTreeNode(
										tokens.get(index).getValue(), 
										"DIGIT_CONSTANT", 
										null,
										getTokenLabel(index)), 
								constantList);
						
					} else {
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
					if (getTokenType(index).equals("IDENTIFIE")) {
						SyntaxTree tmpTree = new SyntaxTree();
						tmpTree.setRoot(new SyntaxTreeNode("Statement"));
						tmpTree.setCurrent(tmpTree.getRoot());
						tree.addChildNode(tmpTree.getRoot(), father);
						
						// 类型
						SyntaxTreeNode variableType = new SyntaxTreeNode("Type");
						tmpTree.addChildNode(variableType, null);
						
						// extra_info
						HashMap<String, String> extraInfo = new HashMap<>();
						extraInfo.put("type", tmpVariableType);
						tmpTree.addChildNode(
								new SyntaxTreeNode(
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
								new SyntaxTreeNode(
										getTokenValue(index), 
										"IDENTIFIER", 
										extraInfo,
										getTokenLabel(index)), 
								tmpTree.getRoot());
						
					} else if (getTokenType(index).equals("COMMA")) { 
						continue;
					}else {
						try {
							throw new Exception("Error in multiple variable statmement!");
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
					
					index++;
				}
				break;	// 到达了末尾的SEMICOLON
				
			} else {
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
		
	}

	// 赋值语句
	private void _assignment(SyntaxTreeNode father) {
		if(father == null) {
			father = tree.getRoot();
		}
		
		SyntaxTree assignTree = new SyntaxTree();
		SyntaxTreeNode root = new SyntaxTreeNode("Assignment");
		assignTree.setRoot(root);
		assignTree.setCurrent(root);
		tree.addChildNode(root, father);
		
		while(!getTokenType(index).equals("SEMICOLON")) {			
			// 被赋值的变量
			if(getTokenType(index).equals("IDENTIFIER")) {
				assignTree.addChildNode(
						new SyntaxTreeNode(
								getTokenValue(index), 
								"IDENTIFIER", 
								null,
								getTokenLabel(index)), 
						null);
				index++;
				
			// 等于号的右边为表达式
			} else if(getTokenType(index).equals("ASSIGN")) {
				index++;
				_expression(root, null);
			}
		}
		index++;
		
	}
	
	// while语句
	private void _while(SyntaxTreeNode father) {
		
		SyntaxTree whileTree = new SyntaxTree();
		SyntaxTreeNode root = new SyntaxTreeNode("Control", "WhileControl", null, null);
		whileTree.setRoot(root);
		whileTree.setCurrent(root);
		tree.addChildNode(root, father);
		
		index++;
		if (getTokenType(index).equals("LL_BRACKET")) {
			index++;
			int tmpIndex = index;
			while (getTokenType(tmpIndex).equals("RL_BRACKET")) {
				tmpIndex++;
			}
			_expression(root, tmpIndex);
			index++;
			
			// 为左大括号，while的主体
			if (getTokenType(index).equals("LB_BRACKET")) {
				index++;
				_block(whileTree);
				
			} else {
				try {
					throw new Exception("while statement body must be surrouded by '{}'");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
		} else {
			try {
				throw new Exception("while statement error [" + getTokenLabel(index) + "] : " + getTokenType(index));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
	}
	
	// do-while语句
	private void _doWhile(SyntaxTreeNode father) {
		
		SyntaxTree doWhileTree = new SyntaxTree();
		SyntaxTreeNode root = new SyntaxTreeNode("Control", "DoWhileControl", null, null);
		doWhileTree.setRoot(root);
		doWhileTree.setCurrent(root);
		tree.addChildNode(root, father);
		
		index++;
		// do后面必须为 {
		if (getTokenType(index).equals("LB_BRACKET")) {
			index++;
			_block(doWhileTree);
			
			// {..}后必须为while
			if (getTokenType(index).equals("WHILE")) {
				index++;
			} else {
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
				while (!getTokenType(tmpIndex).equals("RL_BRACKET")) {
					tmpIndex++;
				}
				_expression(root, tmpIndex);
				index += 2;  // 跳过 );
				
			} else {
				try {
					throw new Exception("do-while statement error [" + getTokenLabel(index) + "] : " + getTokenType(index));
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
		} else {
			try {
				throw new Exception("error in do-while [" + getTokenLabel(index) + "] : " + getTokenType(index));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
	}
	
	// if语句
	private void _if_else(SyntaxTreeNode father) {
		
		SyntaxTree ifElseTree = new SyntaxTree();
		SyntaxTreeNode root = new SyntaxTreeNode("Control", "IfElseControl", null, null);
		ifElseTree.setRoot(root);
		ifElseTree.setCurrent(root);
		tree.addChildNode(root, father);
		
		SyntaxTree ifTree = new SyntaxTree();
		SyntaxTreeNode ifRoot = new SyntaxTreeNode("IfControl");
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
				while(!getTokenType(tmpIndex).equals("RL_BRACKET")) {
					tmpIndex++;
				}
				_expression(ifRoot, tmpIndex);
				index++;
				
			} else {
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
				try {
					throw new Exception("if statement must be surrounded by '{}'");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
			
		} else {
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
			
			SyntaxTree elseTree = new SyntaxTree();
			SyntaxTreeNode elseRoot = new SyntaxTreeNode("ElseControl");
			elseTree.setRoot(elseRoot);
			elseTree.setCurrent(elseRoot);
			ifElseTree.addChildNode(elseRoot, root);
			
			// 左大括号
			 if(getTokenType(index).equals("LB_BRACKET")) {
				 index++;
				 _block(elseTree);
				 
			 } else {
				 try {
					throw new Exception("else statement must be surrounded by '{}'");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			 }
			 
		}
		
	}
	
	// for语句
	private void _for(SyntaxTreeNode father) {
		
		SyntaxTree forTree = new SyntaxTree();
		SyntaxTreeNode root = new SyntaxTreeNode("Control", "ForControl", null, null);
		forTree.setRoot(root);
		forTree.setCurrent(root);
		forTree.addChildNode(root, father);
		
		if (getTokenType(index).equals("FOR")) {
			index++;
			
			if (getTokenType(index).equals("LL_BRACKET")) {
				index++;
				
				// 首先找到右小括号的位置
				int tmpIndex = index;
				while(!getTokenType(tmpIndex).equals("RL_BRACKET")) {
					tmpIndex++;
				}
				
				// for语句中的第一个分号前的部分
				_assignment(root);
				
				// 两个分号中间的部分
				_expression(root, null);
				index++;
				
				// 第二个分号后的部分
				_expression(root, tmpIndex);
				index++;
				
			} else {
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
				try {
					throw new Exception("for statement must be surrounded by '{}'");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
			// 交换for语句下第三个子节点和第四个子节点
			SyntaxTreeNode currentNode = forTree.getRoot().getFirstSon().getRight().getRight();
			SyntaxTreeNode nextNode = currentNode.getRight();
			forTree.switchTwoSubTree(currentNode, nextNode);
			
		} else {
			try {
				throw new Exception("error : lack for in the statement");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	// 处理控制语句
	private void _control(SyntaxTreeNode father) {
		
		String tokenType = getTokenType(index);
		
		if(tokenType.equals("WHILE")) {
			_while(father);
			
		} else if (tokenType.equals("DO")) { 
			_doWhile(father);
			
		} else if(tokenType.equals("IF")) {
			_if_else(father);
			
		} else if(tokenType.equals("FOR")) {
			_for(father);
			
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
	private void _return(SyntaxTreeNode father) {
		if (father == null) {
			father = tree.getRoot();
		}
		
		SyntaxTree returnTree = new SyntaxTree();
		SyntaxTreeNode root = new SyntaxTreeNode("Return");
		returnTree.setRoot(root);
		returnTree.setCurrent(root);
		returnTree.addChildNode(root, father);
		
		if (getTokenType(index).equals("RETURN")) {
			SyntaxTreeNode returnNode = new SyntaxTreeNode(getTokenValue(index));
			returnTree.addChildNode(returnNode, root);
			index++;	
		} else {
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
		
	}
	
	// 表达式
	private void _expression(SyntaxTreeNode father, Integer ind) {
		if (father == null) {
			father = tree.getRoot();
		}
		
		// 运算符栈
		Stack<SyntaxTree> operatorStack = new Stack<SyntaxTree>();
		// 转换成的逆波兰表达式结果
		List<SyntaxTree> reversePolishExpression = new ArrayList<SyntaxTree>();
	
		// 中缀表达式转为后缀表达式，即逆波兰表达
		while (!getTokenType(index).equals("SEMICOLON")) {
			if (ind != null && index >= ind) {
				break;
			}
			
			// 如果是数字常量
			if (getTokenType(index).equals("DIGIT_CONSTANT")) {
				SyntaxTree tmpTree = new SyntaxTree();
				SyntaxTreeNode constantRoot = new SyntaxTreeNode("Expression", "Constant", null, null);
				tmpTree.setRoot(constantRoot);
				tmpTree.setCurrent(constantRoot);
				
				SyntaxTreeNode node = new SyntaxTreeNode(getTokenValue(index), "_Constant", null, getTokenLabel(index));
				tmpTree.addChildNode(node, null);
				reversePolishExpression.add(tmpTree);
				
			// 如果是变量或者数组的某元素
			} else if (getTokenType(index).equals("IDENTIFIER")) {
				// 变量
				if (ParserUtils.isOperator(getTokenValue(index + 1))
						|| getTokenType(index + 1).equals("SEMICOLON")
						|| getTokenType(index + 1).equals("RL_BRACKET")) {
					SyntaxTree tmpTree = new SyntaxTree();
					SyntaxTreeNode variableRoot = new SyntaxTreeNode("Expression", "Variable", null, null);
					tmpTree.setRoot(variableRoot);
					tmpTree.setCurrent(variableRoot);
					
					SyntaxTreeNode node = new SyntaxTreeNode(getTokenValue(index), "_Variable", null, getTokenLabel(index));
					tmpTree.addChildNode(node, null);
					reversePolishExpression.add(tmpTree);
					
				// 数组的某一个元素ID[i]
				} else if (getTokenType(index + 1).equals("LM_BRACKET")) {
					SyntaxTree tmpTree = new SyntaxTree();
					SyntaxTreeNode arrayItemRoot = new SyntaxTreeNode("Expression", "ArrayItem", null, null);
					tmpTree.setRoot(arrayItemRoot);
					tmpTree.setCurrent(arrayItemRoot);
					
					// 数组的名字
					SyntaxTreeNode node = new SyntaxTreeNode(getTokenValue(index), "_ArrayName", null, getTokenLabel(index));
					tmpTree.addChildNode(node, null);
					
					index += 2;
					if (getTokenType(index).equals("DIGIT_CONSTANT")
							|| getTokenType(index).equals("IDENTIFIER")) {
						node = new SyntaxTreeNode(getTokenValue(index), "_ArrayIndex", null, getTokenLabel(index));
						tmpTree.addChildNode(node, null);
						reversePolishExpression.add(tmpTree);
						
					} else {
						try {
							throw new Exception("error: 数组下表必须为常量或标识符 : " + getTokenType(index));
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(1);
						}
					}
					
				} else {
					 
					try {
						throw new Exception("not support identifer : " + getTokenType(index + 1));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
			
			// 如果是运算符
			} else if (ParserUtils.isOperator(getTokenValue(index))
					|| getTokenType(index).equals("LL_BRACKET")
					|| getTokenType(index).equals("RL_BRACKET")) {
				SyntaxTree tmpTree = new SyntaxTree();
				SyntaxTreeNode root = new SyntaxTreeNode("Operator", "Operator", null, null);
				tmpTree.setRoot(root);
				tmpTree.setCurrent(root);
				
				SyntaxTreeNode node = new SyntaxTreeNode(getTokenValue(index), "_Operator", null, getTokenLabel(index));
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
							&& ParserDefine.OPERATOR_PRIORITY.get(tmpTree.getCurrent().getValue())
								< ParserDefine.OPERATOR_PRIORITY.get(operatorStack.peek().getCurrent().getValue())) {
						reversePolishExpression.add(operatorStack.pop());
						
					}
					operatorStack.add(tmpTree);
				}
			} else {
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
		SyntaxTree newTree = new SyntaxTree();
		SyntaxTreeNode newRoot = new SyntaxTreeNode("Expression", "SingleOrDoubleOperand", null, null);
		newTree.setRoot(newRoot);
		newTree.setCurrent(newRoot);
		for (SyntaxTree item : reversePolishExpression) {
			newTree.addChildNode(item.getRoot(), newRoot);
		}
		tree.addChildNode(newTree.getRoot(), father);
		
	}
	
	// 函数调用
	private void _functionCall(SyntaxTreeNode father) {
		if (father == null) {
			father = tree.getRoot();
		}
		
		SyntaxTree funcCallTree = new SyntaxTree();
		SyntaxTreeNode root = new SyntaxTreeNode("FunctionCall");
		funcCallTree.setRoot(root);
		funcCallTree.setCurrent(root);
		tree.addChildNode(root, father);
		
		while(!getTokenType(index).equals("SEMICOLON")) {
			// 函数名
			if(getTokenType(index).equals("IDENTIFIER")) {
				funcCallTree.addChildNode(
							new SyntaxTreeNode(
									getTokenValue(index), 
									"FUNCTION_NAME", 
									null,
									getTokenLabel(index)), 
							null);
			
			// 左小括号
			} else if(tokens.get(index).getType().equals("LL_BRACKET")) {
				index++;
				SyntaxTreeNode paramsList = new SyntaxTreeNode("CallParameterList");
				funcCallTree.addChildNode(paramsList, funcCallTree.getRoot());

				while (!getTokenType(index).equals("RL_BRACKET")) {
					if (getTokenType(index).equals("IDENTIFIER")
							|| getTokenType(index).equals("DIGIT_CONSTANT")
							|| getTokenType(index).equals("STRING_CONSTANT")) {
						funcCallTree.addChildNode(
								new SyntaxTreeNode(
										getTokenValue(index), 
										getTokenType(index),
										null,
										getTokenLabel(index)),
								paramsList);
						
					} else if (getTokenType(index).equals("DOUBLE_QUOTE")) {
						index++;
						funcCallTree.addChildNode(
								new SyntaxTreeNode(
										getTokenValue(index), 
										getTokenType(index),
										null,
										getTokenLabel(index)),
								paramsList);
						index++;
						
					} else if (getTokenType(index).equals("ADDRESS")) {
						funcCallTree.addChildNode(
								new SyntaxTreeNode(
										getTokenValue(index),
										"ADDRESS", 
										null,
										getTokenLabel(index)),
								paramsList);
					}
					index++;
				}
			} else {
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
		
	}
	
	// 根据一个语句的句首判断句型
	private String judgeSentencePattern() {
		
		String tokenValue = getTokenValue(index);
		String tokenType = getTokenType(index);
		
		// include句型
		if (tokenType.equals("SHARP") && getTokenType(index + 1).equals("INCLUDE")) {
			return "INCLUDE";
			
		// 控制句型
		} else if (ParserUtils.isControl(tokenValue)) {
			return "CONTROL";
			
		// 可能是声明语句或函数声明语句
		} else if (ParserUtils.isInnerDataType(tokenValue) && getTokenType(index + 1).equals("IDENTIFIER")) {
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
			
			if(index1TokenType.equals("LL_BRACKET")) {
				return "FUNCTION_CALL";
			} else if(index1TokenType.equals("ASSIGN")) {
				return "ASSIGNMENT";
			} else if(index1TokenType.equals("SELF_PLUS") 
					|| index1TokenType.equals("SELF_MINUS")) { 
				return "SELF_OPT";
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

	public void runParser() {
		
		// 创建树的根节点
		SyntaxTreeNode root = new SyntaxTreeNode("Sentence");
		tree = new SyntaxTree(root);
		tree.setCurrent(root);
		
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
				_statement(root);
				
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
		
	}
	
	// 递归输出语法树
	private void display(SyntaxTreeNode node, BufferedWriter writer) {
		if(null == node) return;
		
		System.out.printf("( self: %s %s %s, father: %s, left: %s, right: %s )\n", 
				node.getValue(), node.getType(), node.getLabel(),
				node.getFather() == null ? null : node.getFather().getValue(), 
				node.getLeft() == null ? null : node.getLeft().getValue(), 
				node.getRight() == null ? null : node.getRight().getValue());
		try {
			writer.write("( self: " + node.getValue() + " " + node.getType() + " " + node.getLabel()
								+ ", father: " + (node.getFather() == null ? null : node.getFather().getValue())
								+ ", left: " + (node.getLeft() == null ? null : node.getLeft().getValue())
								+ ", right: " + (node.getRight() == null ? null : node.getRight().getValue())
								+ " )");
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SyntaxTreeNode child = node.getFirstSon();
		while(child != null) {
			display(child, writer);
			child = child.getRight();
		}
	}
	
	public void outputParser() {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(CommonsDefine.OUTPUT_PATH + "parser.txt"));
			System.out.println("====================Parser==================");
			display(tree.getRoot(), writer);
			
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

	public static void main(String[] args) {
		String fileName = "evenSum.c";
		Lexer lexer = new Lexer(fileName);
		lexer.runLexer();
		lexer.labelSrc(fileName);
		
		Parser parser = new Parser(lexer.getTokens());
		parser.runParser();
		parser.outputParser();
		
	}
	
}
