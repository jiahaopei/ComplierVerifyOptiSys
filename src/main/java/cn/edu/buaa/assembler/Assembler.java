package cn.edu.buaa.assembler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.buaa.lexer.Lexer;
import cn.edu.buaa.parser.Parser;
import cn.edu.buaa.pojo.SyntaxTree;
import cn.edu.buaa.pojo.SyntaxTreeNode;

public class Assembler {

	// 语法分析传过来的语法树
	private SyntaxTree tree;

	// 汇编代码生成过程中需要用到的公共数据
	private AssemblerDTO assemblerDTO;

	private static final Logger logger = LoggerFactory.getLogger(Assembler.class);

	public Assembler(SyntaxTree tree) {
		this.tree = tree;

		AssemblerDTO assemblerDTO = new AssemblerDTO();
		assemblerDTO.setLabelCnt(0);
		assemblerDTO.setMemAdress(8); // 以8号地址起始
		assemblerDTO.setVariableSymbolOrNumber(true); // true，表示以数字出现
		this.assemblerDTO = assemblerDTO;

	}

	public void runAssembler() {
		// 从语法树的根节点开始遍历
		traverse(tree.getRoot());

	}
	
	// 从左向右遍历某一层的全部节点
	private void traverse(SyntaxTreeNode node) {
		while (node != null) {
			handlerSentenceblock(node);
			node = node.getRight();
		}
		
	}

	// 处理某一种句型
	private void handlerSentenceblock(SyntaxTreeNode node) {
		if (node == null) {
			return;
		}

		// 将要遍历的节点
		if (AssemblerUtils.isSentenceType(node.getValue())) {
			// 如果是根节点
			if (node.getValue().equals("Sentence")) {
				traverse(node.getFirstSon());

				// include语句
			} else if (node.getValue().equals("Include")) {
				_include(node);

				// 函数定义
			} else if (node.getValue().equals("FunctionStatement")) {
				_functionStatement(node);

				// 声明语句(变量声明、数组声明)
			} else if (node.getValue().equals("Statement")) {
				_statement(node);

				// 函数调用
			} else if (node.getValue().equals("FunctionCall")) {
				_functionCall(node);

				// 赋值语句
			} else if (node.getValue().equals("Assignment")) {
				_assignment(node);

				// 控制语句
			} else if (node.getValue().equals("Control")) {
				if (node.getType().equals("IfElseControl")) {
					_controlIfElse(node);

				} else if (node.getType().equals("ForControl")) {
					_controlFor(node);

				} else if (node.getType().equals("WhileControl")) {
					_controlWhile(node);

				} else if (node.getType().equals("DoWhileControl")) {
					_controlDoWhile(node);

				} else {
					try {
						throw new Exception("control type not supported" + node.getType());
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}

				}

				// 表达式语句
			} else if (node.getValue().equals("Expression")) {
				_expression(node);

				// return语句
			} else if (node.getValue().equals("Return")) {
				_return(node);

			} else {
				try {
					throw new Exception("sentenct type not supported yet : " + node.getValue());
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}

			}

		} else {
			try {
				throw new Exception("Unsupport sentence type : " + node.getValue());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

	}

	// include句型
	private void _include(SyntaxTreeNode node) {
		// 不用处理，不会生成对应的汇编代码

	}

	// 函数定义句型，暂时只能处理main函数
	private void _functionStatement(SyntaxTreeNode node) {
		SyntaxTreeNode currentNode = node.getFirstSon(); // 第一个儿子
		String funcName = null;
		String label = null;
		String line = null;
		while (currentNode != null) {
			if (currentNode.getValue().equals("FunctionName")) {
				funcName = currentNode.getFirstSon().getValue();
				label = currentNode.getFirstSon().getLabel();
				if (funcName.equals("main")) {
					line = AssemblerUtils.PREFIX + ".align 2";
					assemblerDTO.insertIntoText(line, label);
					line = AssemblerUtils.PREFIX + ".globl main";
					assemblerDTO.insertIntoText(line, label);
					line = AssemblerUtils.PREFIX + ".type main, @function";
					assemblerDTO.insertIntoText(line, label);
					line = "main:";
					assemblerDTO.insertIntoText(line, label);
					line = AssemblerUtils.PREFIX + "stwu 1,-16(1)";
					assemblerDTO.insertIntoText(line, label);
					line = AssemblerUtils.PREFIX + "stw 31,12(1)";
					assemblerDTO.insertIntoText(line, label);
					line = AssemblerUtils.PREFIX + "mr 31,1";
					assemblerDTO.insertIntoText(line, label);
					
					// 增加空行
					assemblerDTO.insertIntoText("", null);

				} else {
					try {
						throw new Exception("Only support main function!");
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			} else if (currentNode.getValue().equals("Sentence")) {
				traverse(currentNode.getFirstSon());

			}

			currentNode = currentNode.getRight();
		}

		if (funcName != null && funcName.equals("main")) {
			line = AssemblerUtils.PREFIX + "addi 11,31,16";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "lwz 31,-4(11)";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "mr 1,11";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "blr";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + ".size main,.-main";
			assemblerDTO.insertIntoText(line, label);
			
		}

	}

	// 变量声明
	private void _statement(SyntaxTreeNode node) {
		// 变量数据类型
		String variableFieldType = null;

		// 变量类型（是数组还是单个变量）
		String variableType = null;

		// 变量名
		String variableName = null;

		String line = null;
		String label = null;
		SyntaxTreeNode currentNode = node.getFirstSon();
		while (currentNode != null) {
			label = currentNode.getLabel();
					
			// 类型
			if (currentNode.getValue().equals("Type")) {
				variableFieldType = currentNode.getFirstSon().getValue();

				// 变量名
			} else if (currentNode.getType().equals("IDENTIFIER")) {
				variableName = currentNode.getValue();
				variableType = currentNode.getExtraInfo().get("type");

				// 将该变量存入符号表
				Map<String, String> tmpMap = new HashMap<String, String>();
				tmpMap.put("type", variableType);
				tmpMap.put("field_type", variableFieldType);
				tmpMap.put("register", Integer.toString(assemblerDTO.getMemAdress()));
				assemblerDTO.addToMemAdress(4);
				assemblerDTO.putIntoSymbolTable(variableName, tmpMap);

				// 数组元素
			} else if (currentNode.getValue().equals("ConstantList")) {
				line = AssemblerUtils.PREFIX + ".align 2";
				assemblerDTO.insertIntoData(line, label);
				line = "." + variableName + ":";
				assemblerDTO.insertIntoData(line, label);

				SyntaxTreeNode tmpNode = currentNode.getFirstSon();
				while (tmpNode != null) {
					line = "." + variableFieldType + "	" + tmpNode.getValue();
					assemblerDTO.insertIntoData(line, tmpNode.getLabel());
					tmpNode = tmpNode.getRight();

				}

			}

			currentNode = currentNode.getRight();
		}

	}

	// 函数调用
	private void _functionCall(SyntaxTreeNode node) {
		String funcName = null;
		String label = null;
		List<String> parameterList = new ArrayList<>();

		String line = null;
		SyntaxTreeNode currentNode = node.getFirstSon();
		while (currentNode != null) {
			// 函数名字
			if (currentNode.getType() != null && currentNode.getType().equals("FUNCTION_NAME")) {
				funcName = currentNode.getValue();
				label = currentNode.getLabel();
				if (!funcName.equals("scanf") && !funcName.equals("printf")) {
					try {
						throw new Exception("function call except scanf and printf not supported yet");
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
				}

				// 函数参数
			} else if (currentNode.getValue().equals("CallParameterList")) {
				SyntaxTreeNode tmpNode = currentNode.getFirstSon();
				while (tmpNode != null) {
					// 字符串常量
					if (tmpNode.getType().equals("STRING_CONSTANT")) {
						// 汇编中字符常量用.LC标号表示
						String lc = ".LC" + assemblerDTO.getLabelCnt();
						assemblerDTO.addToLabelCnt(1);

						// 把字符常量添加到.data域
						line = AssemblerUtils.PREFIX + ".align 2";
						assemblerDTO.insertIntoData(line, tmpNode.getLabel());
						line = lc + ":";
						assemblerDTO.insertIntoData(line, tmpNode.getLabel());						
						line = AssemblerUtils.PREFIX + ".string	\"" + tmpNode.getValue() + "\"";
						assemblerDTO.insertIntoData(line, tmpNode.getLabel());
						
						// 添加到符号表
						Map<String, String> tmpMap = new HashMap<>();
						tmpMap.put("type", "STRING_CONSTANT");
						tmpMap.put("value", tmpNode.getValue());
						assemblerDTO.putIntoSymbolTable(lc, tmpMap);
						parameterList.add(lc);

						// 数字常量
					} else if (tmpNode.getType().equals("DIGIT_CONSTANT")) {
						logger.debug("_functionCall [DIGIT_CONSTANT] : " + tmpNode.getValue());

						// 某个变量
					} else if (tmpNode.getType().equals("IDENTIFIER")) {
						parameterList.add(tmpNode.getValue());

						// 地址符号，不处理
					} else if (tmpNode.getType().equals("ADDRESS")) {
						logger.info("ADDRESS : " + tmpNode.getValue());

					} else {
						try {
							throw new Exception("Error in _functionCall : " + tmpNode.getType());
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(1);
						}

					}

					tmpNode = tmpNode.getRight();
				}

			}

			currentNode = currentNode.getRight();
		}

		// 如果是printf函数
		if (funcName.equals("printf")) {
			int num = 3;
			for (int i = 0; i < parameterList.size(); i++) {
				String parameter = parameterList.get(i);
				String parameterType = assemblerDTO.getMapFromSymbolTable(parameter).get("type");
				
				
				// 参数的类型是字符串常量
				if (parameterType.equals("STRING_CONSTANT")) {
					line = AssemblerUtils.PREFIX + "lis 0," + parameter + "@ha";
					assemblerDTO.insertIntoText(line, label);
					line = AssemblerUtils.PREFIX + "addic 0,0," + parameter + "@l";
					assemblerDTO.insertIntoText(line, label);
					line = AssemblerUtils.PREFIX + "mr " + num + ",0";
					num++;
					assemblerDTO.insertIntoText(line, label);

					// 参数为变量
				} else if (parameterType.equals("VARIABLE")) {
					String fieldType = assemblerDTO.getMapFromSymbolTable(parameter).get("field_type");
					if (fieldType.equals("int") || fieldType.equals("long")) {
						line = AssemblerUtils.PREFIX + "lwz " + num + "," + assemblerDTO.getVariableSymbolOrNumber(parameter) + "(31)";
						num++;
						assemblerDTO.insertIntoText(line, label);

					} else if (fieldType.equals("float") || fieldType.equals("double")) {
						logger.debug("printf parameter type : " + fieldType);

					} else {
						logger.debug("More type will be added to printf : " + fieldType);

					}

				} else {
					try {
						throw new Exception("Other variable type not support : "
								+ assemblerDTO.getMapFromSymbolTable(parameter).get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
				}

			}
			line = AssemblerUtils.PREFIX + "crxor 6,6,6";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "bl printf";
			assemblerDTO.insertIntoText(line, label);

			// 如果是scanf函数
		} else if (funcName.equals("scanf")) {
			int num = 3;
			for (int i = 0; i < parameterList.size(); i++) {
				String parameter = parameterList.get(i);
				String parameterType = assemblerDTO.getMapFromSymbolTable(parameter).get("type");
				if (parameterType.equals("STRING_CONSTANT")) {
					line = AssemblerUtils.PREFIX + "lis 0," + parameter + "@ha";
					assemblerDTO.insertIntoText(line, label);
					line = AssemblerUtils.PREFIX + "addic " + (num + 7) + ",0," + parameter + "@l";
					assemblerDTO.insertIntoText(line, label);
					line = AssemblerUtils.PREFIX + "mr " + num + "," + (num + 7);
					num++;
					assemblerDTO.insertIntoText(line, label);

				} else if (parameterType.equals("VARIABLE")) {
					String fieldType = assemblerDTO.getMapFromSymbolTable(parameter).get("field_type");
					if (fieldType.equals("int") || fieldType.equals("long")) {
						line = AssemblerUtils.PREFIX + "addi " + (num + 7) + ",31," + assemblerDTO.getVariableSymbolOrNumber(parameter);
						assemblerDTO.insertIntoText(line, label);
						line = AssemblerUtils.PREFIX + "mr " + num + "," + (num + 7);
						num++;
						assemblerDTO.insertIntoText(line, label);

					} else if (fieldType.equals("float")) {
						logger.debug("scanf float");

					} else {
						try {
							throw new Exception("data type in scanf is not supported : " + fieldType);
						} catch (Exception e) {
							e.printStackTrace();
							System.exit(1);
						}

					}

				} else {
					try {
						throw new Exception(funcName + "not support this parameter type : " + parameterType);
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}

				}

			}

			line = AssemblerUtils.PREFIX + "crxor 6,6,6";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "bl __isoc99_scanf";
			assemblerDTO.insertIntoText(line, label);

		} else {
			logger.debug("_functionCall funcName : " + funcName);

		}

		assemblerDTO.insertIntoText("", null);  // 增加一个空行
	}

	// 赋值语句
	private void _assignment(SyntaxTreeNode node) {
		String line = null;
		SyntaxTreeNode currentNode = node.getFirstSon();
		String label = currentNode.getLabel();
		if (currentNode.getType().equals("IDENTIFIER") && currentNode.getRight().getValue().equals("Expression")) {
			// 先处理右边的表达式
			Map<String, String> expres = _expression(currentNode.getRight());

			// 该变量的类型
			String fieldType = assemblerDTO.getMapFromSymbolTable(currentNode.getValue()).get("field_type");
			if (fieldType.equals("int")) {
				// 常数
				if (expres.get("type").equals("CONSTANT")) {
					line = AssemblerUtils.PREFIX + "li 0," + expres.get("value");
					assemblerDTO.insertIntoText(line, label);
					line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(currentNode.getValue()) + "(31)";
					assemblerDTO.insertIntoText(line, label);

					// 变量
				} else if (expres.get("type").equals("VARIABLE")) {
					// 把数放到r0中，再把r0总的数转到目标寄存器中, 同float
					line = AssemblerUtils.PREFIX + "lwz 0," + assemblerDTO.getVariableSymbolOrNumber(expres.get("value")) + "(31)";
					assemblerDTO.insertIntoText(line, label);
					line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(currentNode.getValue()) + "(31)";
					assemblerDTO.insertIntoText(line, label);

				} else {
					try {
						throw new Exception("_assignment only support constant and varivale : " + expres.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}

				}

			} else if (fieldType.equals("float")) {
				logger.debug("float expression!");

			} else {
				try {
					throw new Exception("Not support this type : " + fieldType);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}

			}

		} else {
			try {
				throw new Exception("error : assignment statement !");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

		}

		assemblerDTO.insertIntoText("", null);
	}

	// if-else语句
	private void _controlIfElse(SyntaxTreeNode node) {
		// 暂存if-else中的标签
		Map<String, String> labelsIfelse = new HashMap<>();
		labelsIfelse.put("label_else", ".L" + assemblerDTO.getLabelCnt());
		assemblerDTO.addToLabelCnt(1);
		labelsIfelse.put("label_end", ".L" + assemblerDTO.getLabelCnt());
		assemblerDTO.addToLabelCnt(1);

		String line = null;
		String ifLabel = null;
		String elseLabel = null;
		boolean isIfElse = false;		// 	区分if语句和if-else语句
		SyntaxTreeNode currentNode = node.getFirstSon();
		while (currentNode != null) {
			if (currentNode.getValue().equals("IfControl")) {
				// 检验if语句是否正确
				if (!currentNode.getFirstSon().getValue().equals("Expression")
						|| !currentNode.getFirstSon().getRight().getValue().equals("Sentence")) {
					try {
						throw new Exception("if statement is error");
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}

				}
				
				ifLabel = currentNode.getLabel();
				if (currentNode.getValue().equals("IfControl") 
						&& currentNode.getRight().getValue().equals("ElseControl")) {
					isIfElse = true;
					elseLabel = currentNode.getRight().getLabel();
				}
				
				Map<String, String> expres = _expression(currentNode.getFirstSon());
				
				line = AssemblerUtils.PREFIX + "lwz 0," + assemblerDTO.getVariableSymbolOrNumber(expres.get("value")) + "(31)";
				assemblerDTO.insertIntoText(line,ifLabel);
				line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
				assemblerDTO.insertIntoText(line, ifLabel);
				line = AssemblerUtils.PREFIX + "beq 7," + labelsIfelse.get("label_else");
				assemblerDTO.insertIntoText(line, ifLabel);
				assemblerDTO.insertIntoText("", null);// 插入一个空行
				
				traverse(currentNode.getFirstSon().getRight().getFirstSon());
				
				// 只有是if-else语句才能加跳转到结尾的语句
				if (isIfElse) {
					line = AssemblerUtils.PREFIX + "b " + labelsIfelse.get("label_end");
					assemblerDTO.insertIntoText(line, elseLabel);
				}
				
				line = labelsIfelse.get("label_else") + ":";
				assemblerDTO.insertIntoText(line, ifLabel);
				
				// 插入一个空行
				assemblerDTO.insertIntoText("", null);

			} else if (currentNode.getValue().equals("ElseControl")) {
				traverse(currentNode.getFirstSon());
				
				line = labelsIfelse.get("label_end") + ":";
				assemblerDTO.insertIntoText(line, elseLabel);
				// 插入一个空行
				assemblerDTO.insertIntoText("", null);

			} else {
				try {
					throw new Exception("Error : if-else statement!");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}

			}

			currentNode = currentNode.getRight();
		}

	}

	// for语句
	private void _controlFor(SyntaxTreeNode node) {
		// 暂存for开始和结束的标签
		Map<String, String> labelsFor = new HashMap<>();
		labelsFor.put("label1", ".L" + assemblerDTO.getLabelCnt());
		assemblerDTO.addToLabelCnt(1);
		labelsFor.put("label2", ".L" + assemblerDTO.getLabelCnt());
		assemblerDTO.addToLabelCnt(1);

		// for的一、二、三部分都可缺失，故不进行语法检验

		// 遍历的是for循环中的第2部分，即中间的条件表达式
		int cnt = 2;
		// 保存条件表达式的入口节点，以便后续再出处理
		SyntaxTreeNode forCondition = null;

		String line = null;
		String label = node.getLabel();
		SyntaxTreeNode currentNode = node.getFirstSon();
		while (currentNode != null) {
			// for第一部分
			if (currentNode.getValue().equals("Assignment")) {
				_assignment(currentNode);

				// for第二、三部分
			} else if (currentNode.getValue().equals("Expression")) {
				// 如果是第2部分
				if (cnt == 2) {
					cnt++;
					line = AssemblerUtils.PREFIX + "b " + labelsFor.get("label1");
					assemblerDTO.insertIntoText(line, label);
					line = labelsFor.get("label2") + ":";
					assemblerDTO.insertIntoText(line, label);

					// 留到后面再处理
					forCondition = currentNode;
					// _expression(currentNode);

					// 第3部分
				} else {
					_expression(currentNode);

				}

				// for语句块的部分
			} else if (currentNode.getValue().equals("Sentence")) {
				traverse(currentNode.getFirstSon());

			} else {
				try {
					throw new Exception("Error : for statement");
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}

			}

			currentNode = currentNode.getRight();
		}

		line = labelsFor.get("label1") + ":";
		assemblerDTO.insertIntoText(line, label);
		// 延迟到此处才处理条件表达式
		Map<String, String> expres = _expression(forCondition);
		line = AssemblerUtils.PREFIX + "lwz 0," + assemblerDTO.getVariableSymbolOrNumber(expres.get("value")) + "(31)";
		assemblerDTO.insertIntoText(line, label);
		line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
		assemblerDTO.insertIntoText(line, label);
		line = AssemblerUtils.PREFIX + "bne 7," + labelsFor.get("label2");
		assemblerDTO.insertIntoText(line, label);

		// 增加一个空行
		assemblerDTO.insertIntoText("", null);
	}

	// while语句
	private void _controlWhile(SyntaxTreeNode node) {
		// 暂存while开始和结束的标签
		Map<String, String> labelsWhile = new HashMap<>();
		labelsWhile.put("label1", ".L" + assemblerDTO.getLabelCnt());
		assemblerDTO.addToLabelCnt(1);
		labelsWhile.put("label2", ".L" + assemblerDTO.getLabelCnt());
		assemblerDTO.addToLabelCnt(1);

		// while语法检验
		if (!(node.getFirstSon().getValue().equals("Expression")
				&& node.getFirstSon().getRight().getValue().equals("Sentence"))) {
			try {
				throw new Exception("error : check while statement");
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		SyntaxTreeNode whileCondition = null;
		String line = null;
		String label = node.getLabel();
		SyntaxTreeNode currentNode = node.getFirstSon();
		while (currentNode != null) {
			// while第一部分
			if (currentNode.getValue().equals("Expression")) {
				line = AssemblerUtils.PREFIX + "b " + labelsWhile.get("label1");
				assemblerDTO.insertIntoText(line, label);
				line = labelsWhile.get("label2") + ":";
				assemblerDTO.insertIntoText(line, label);
				whileCondition = currentNode;

				// while循环体
			} else if (currentNode.getValue().equals("Sentence")) {
				traverse(currentNode.getFirstSon());

			} else {
				try {
					throw new Exception("error while statement : " + currentNode.getValue());
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}

			}

			currentNode = currentNode.getRight();
		}

		line = labelsWhile.get("label1") + ":";
		assemblerDTO.insertIntoText(line, label);
		Map<String, String> expres = _expression(whileCondition);
		line = AssemblerUtils.PREFIX + "lwz 0," + assemblerDTO.getVariableSymbolOrNumber(expres.get("value")) + "(31)";
		assemblerDTO.insertIntoText(line, label);
		line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
		assemblerDTO.insertIntoText(line, label);
		line = AssemblerUtils.PREFIX + "bne 7," + labelsWhile.get("label2");
		assemblerDTO.insertIntoText(line, label);

		// 增加一个空行
		assemblerDTO.insertIntoData("", null);
	}

	// do-while语句
	private void _controlDoWhile(SyntaxTreeNode node) {
		// 暂存标签
		Map<String, String> labelsDoWhile = new HashMap<>();
		labelsDoWhile.put("label1", ".L" + assemblerDTO.getLabelCnt());
		assemblerDTO.addToLabelCnt(1);

		// do-while语法检查
		if (!(node.getFirstSon().getValue().equals("Sentence")
				&& node.getFirstSon().getRight().getValue().equals("Expression"))) {
			try {
				throw new Exception("error : check do-while statement!");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

		}

		String line = null;
		String label = node.getLabel();
		line = labelsDoWhile.get("label1") + ":";
		assemblerDTO.insertIntoText(line, label);

		SyntaxTreeNode currentNode = node.getFirstSon();
		while (currentNode != null) {
			// do-while的循环体
			if (currentNode.getValue().equals("Sentence")) {
				traverse(currentNode.getFirstSon());

				// do-while条件表达式
			} else if (currentNode.getValue().equals("Expression")) {
				Map<String, String> expres = _expression(currentNode);
				line = AssemblerUtils.PREFIX + "lwz 0," + assemblerDTO.getVariableSymbolOrNumber(expres.get("value")) + "(31)";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "bne 7," + labelsDoWhile.get("label1");
				assemblerDTO.insertIntoText(line, label);

			} else {
				try {
					throw new Exception("error do while statement : " + currentNode.getValue());
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}

			}

			currentNode = currentNode.getRight();
		}

		// 增加一个空行
		assemblerDTO.insertIntoText("", null);
	}

	// return语句
	private void _return(SyntaxTreeNode node) {
		// return语句的语法检验
		if (!node.getFirstSon().getValue().equals("return")
				|| !node.getFirstSon().getRight().getValue().equals("Expression")) {
			try {
				throw new Exception("return error");
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		String line = null;
		String label = node.getFirstSon().getLabel();
		SyntaxTreeNode currentNode = node.getFirstSon().getRight();
		Map<String, String> expres = _expression(currentNode);
		if (expres.get("type").equals("CONSTANT")) {
			line = AssemblerUtils.PREFIX + "li 0," + expres.get("value");
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "mr 3,0";
			assemblerDTO.insertIntoText(line, label);

		} else {
			try {
				throw new Exception("return type not supported");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

	}

	// 表达式（封装到静态类中）
	private Map<String, String> _expression(SyntaxTreeNode node) {
		return AssemblerExpression.handle(node, assemblerDTO);

	}

	private void generateAssemblerFile(String fileName) {
		assemblerDTO.generateAssemblerFile(fileName);

	}

	private void generateSymbolTableFile() {
		assemblerDTO.generateSymbolTableFile();

	}

	private void outputAssembler() {
		System.out.println("===================Assembler==================");
		assemblerDTO.dispalyResult();

	}

	public static void main(String[] args) {
		String fileName = "evenSum.c";
		Lexer lexer = new Lexer(fileName);
		lexer.runLexer();
		lexer.labelSrc(fileName);

		Parser parser = new Parser(lexer.getTokens());
		parser.runParser();

		Assembler assembler = new Assembler(parser.getTree());
		assembler.runAssembler();
		assembler.generateAssemblerFile(fileName);
		assembler.generateSymbolTableFile();
		assembler.outputAssembler();

	}

}
