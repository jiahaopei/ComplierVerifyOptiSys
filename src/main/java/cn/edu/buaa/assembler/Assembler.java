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
	
	// 要生成的汇编文件管理器
	private AssemblerFileHandler assFileHandler;
	
	// 符号表
	private Map<String, Map<String, String>> symbolTable;
	
	// 已经声明了多少个label
	private int labelCnt;
	
	// 已经使用了多少个相对地址
	private int memAdress;							
	
	// 控制生成的汇编代码中，变量是以数字还是原始名称出现，
	// 默认false，为原始名称出现
	private boolean isVariableSymbolOrNumber = true;
	
	private static final Logger logger = LoggerFactory.getLogger(Assembler.class);
	
	public Assembler(SyntaxTree tree) {
		this.tree = tree;
		this.assFileHandler = new AssemblerFileHandler();
		this.symbolTable = new HashMap<>();
		this.labelCnt = 0;
		this.memAdress = 8;		// 以8号地址起始
		
	}
	
	public void runAssembler() {
		// 从语法树的根节点开始遍历
		traverse(tree.getRoot());
		
	}
	
	private String getVariableSymbolOrNumber(String parameter) {
		return (isVariableSymbolOrNumber ? symbolTable.get(parameter).get("register") : parameter);
				
	}
	
	// 从左向右遍历某一层的全部节点
	private void traverse(SyntaxTreeNode node) {		
		while(node != null) {
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
			if(node.getValue().equals("Sentence")) {
				traverse(node.getFirstSon());
				
			// include语句
			} else if(node.getValue().equals("Include")) {
				_include(node);
				
			// 函数定义
			} else if(node.getValue().equals("FunctionStatement")) {
				_functionStatement(node);
			
			// 声明语句(变量声明、数组声明)
			} else if(node.getValue().equals("Statement")) {
				_statement(node);
			
			// 函数调用
			} else if(node.getValue().equals("FunctionCall")) {
				_functionCall(node);
			
			// 赋值语句
			} else if(node.getValue().equals("Assignment")) {
				_assignment(node);
							
			// 控制语句
			} else if(node.getValue().equals("Control")) {
				if(node.getType().equals("IfElseControl")) {
					_controlIfElse(node);
					
				} else if(node.getType().equals("ForControl")) {
					_controlFor(node);
					
				} else if(node.getType().equals("WhileControl")) {
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
			} else if(node.getValue().equals("Expression")) {
				_expression(node);
							
			// return语句
			} else if(node.getValue().equals("Return")) {
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
		SyntaxTreeNode currentNode = node.getFirstSon();	// 第一个儿子
		String funcName = null;
		while (currentNode != null) {
			if(currentNode.getValue().equals("FunctionName")) {
				funcName = currentNode.getFirstSon().getValue();
				if (funcName.equals("main")) {
					assFileHandler.insert("	.align 2", "TEXT");
					assFileHandler.insert("	.globl main", "TEXT");
					assFileHandler.insert("	.type main, @function", "TEXT");
					assFileHandler.insert("main:", "TEXT");
					assFileHandler.insert("	stwu 1,-16(1)", "TEXT");
					assFileHandler.insert("	stw 31,12(1)", "TEXT");
					assFileHandler.insert("	mr 31,1", "TEXT");
					assFileHandler.insert("", "TEXT");
					
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
			assFileHandler.insert("	addi 11,31,16", "TEXT");
			assFileHandler.insert("	lwz 31,-4(11)", "TEXT");
			assFileHandler.insert("	mr 1,11", "TEXT");
			assFileHandler.insert("	blr", "TEXT");
			assFileHandler.insert("	.size	main, .-main", "TEXT");
			
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
		SyntaxTreeNode currentNode = node.getFirstSon();
		while (currentNode != null) {
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
				tmpMap.put("register", Integer.toString(memAdress));
				memAdress += 4;
				symbolTable.put(variableName, tmpMap);
						
			// 数组元素
			} else if (currentNode.getValue().equals("ConstantList")) {
				line = "	.align 2";
				assFileHandler.insert(line, "DATA");
				line = "." + variableName + ":";
				assFileHandler.insert(line, "DATA");
							
				SyntaxTreeNode tmpNode = currentNode.getFirstSon();
				while (tmpNode != null) {
					line = "." + variableFieldType + "	" + tmpNode.getValue();
					assFileHandler.insert(line, "DATA");
					tmpNode = tmpNode.getRight();
					
				}
				
			}
			
			currentNode = currentNode.getRight();
		}
		
	}
	
	// 函数调用
	private void _functionCall(SyntaxTreeNode node) {
		String funcName = null;
		List<String> parameterList = new ArrayList<>();
		
		String line = null;
		SyntaxTreeNode currentNode = node.getFirstSon();
		while (currentNode != null) {
			// 函数名字
			if (currentNode.getType() != null && currentNode.getType().equals("FUNCTION_NAME")) {
				funcName = currentNode.getValue();
				if(!funcName.equals("scanf") && !funcName.equals("printf")) {
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
						String label = ".LC" + labelCnt;
						labelCnt++;
						
						// 把字符常量添加到.data域
						line = "	.align 2";
						assFileHandler.insert(line, "DATA");
						line = label + ":";
						assFileHandler.insert(line, "DATA");
						line = "	.string	\"" + tmpNode.getValue() + "\"";
						assFileHandler.insert(line, "DATA");
						
						// 添加到符号表
						Map<String, String> tmpMap = new HashMap<>();
						tmpMap.put("type", "STRING_CONSTANT");
						tmpMap.put("value", tmpNode.getValue());
						symbolTable.put(label, tmpMap);
						parameterList.add(label);
						
					// 数字常量
					} else if (tmpNode.getType().equals("DIGIT_CONSTANT") ) {
						logger.debug("_functionCall [DIGIT_CONSTANT] : " + tmpNode.getValue());
					
					// 某个变量 
					} else if (tmpNode.getType().equals("IDENTIFIER")) {
						parameterList.add(tmpNode.getValue());
					
					// 地址符号，不处理
					} else if(tmpNode.getType().equals("ADDRESS")) {
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
				String parameterType = symbolTable.get(parameter).get("type");
				// 参数的类型是字符串常量
				if(parameterType.equals("STRING_CONSTANT")) {
					line = "	lis 0," + parameter + "@ha";
					assFileHandler.insert(line, "TEXT");
					line = "	addic 0,0," + parameter + "@l";
					assFileHandler.insert(line, "TEXT");
					line =  "	mr " + num + ",0";
					num++;
					assFileHandler.insert(line, "TEXT");
				
				// 参数为变量
				} else if (parameterType.equals("VARIABLE")) {
					String fieldType = symbolTable.get(parameter).get("field_type");
					if(fieldType.equals("int") || fieldType.equals("long")) {
						line = "	lwz " + num +  "," + getVariableSymbolOrNumber(parameter) + "(31)";
						num++;
						assFileHandler.insert(line, "TEXT");
						
					} else if (fieldType.equals("float") || fieldType.equals("double")) {
						logger.debug("printf parameter type : " + fieldType);
						
					} else {
						logger.debug("More type will be added to printf : " + fieldType);
						
					}
					
					
				} else {
					try {
						throw new Exception("Other variable type not support : " + symbolTable.get(parameter).get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
				
			}
			line = "	crxor 6,6,6";
			assFileHandler.insert(line, "TEXT");
			line = "	bl printf";
			assFileHandler.insert(line, "TEXT");
		
		// 	如果是scanf函数
		} else if (funcName.equals("scanf")) {
			int num = 3;
			for (int i = 0; i < parameterList.size(); i++) {
				String parameter = parameterList.get(i);
				String parameterType = symbolTable.get(parameter).get("type");
				if(parameterType.equals("STRING_CONSTANT")) {
					line = "	lis 0," + parameter + "@ha";
					assFileHandler.insert(line, "TEXT");
					line = "	addic " + (num + 7) + ",0," + parameter + "@l";
					assFileHandler.insert(line, "TEXT");
					line = "	mr " + num + "," + (num + 7);
					num++;
					assFileHandler.insert(line, "TEXT");
					
				} else if(parameterType.equals("VARIABLE")) {
					String fieldType = symbolTable.get(parameter).get("field_type");
					if(fieldType.equals("int") || fieldType.equals("long")) {
						line = "	addi " + (num + 7) + ",31," + getVariableSymbolOrNumber(parameter);
						assFileHandler.insert(line, "TEXT");
						line = "	mr " + num + "," + (num + 7);
						num++;
						assFileHandler.insert(line, "TEXT");
						
					} else if(fieldType.equals("float")) {
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
			
			line = "	crxor 6,6,6";
			assFileHandler.insert(line, "TEXT");
			line = "	bl __isoc99_scanf";
			assFileHandler.insert(line, "TEXT");
			
		} else {
			logger.debug("_functionCall funcName : " + funcName);
			
		}
		
		assFileHandler.insert("", "TEXT");	// 增加一个空行
	}
	
	// 赋值语句
	private void _assignment(SyntaxTreeNode node) {
		String line = null;
		SyntaxTreeNode currentNode = node.getFirstSon();
		if(currentNode.getType().equals("IDENTIFIER") 
				&& currentNode.getRight().getValue().equals("Expression")) {
			// 先处理右边的表达式
			Map<String, String> expres = _expression(currentNode.getRight());
			
			// 该变量的类型
			String fieldType = symbolTable.get(currentNode.getValue()).get("field_type");
			if(fieldType.equals("int")) {
				// 常数
				if(expres.get("type").equals("CONSTANT")) {
					line = "	li 0," + expres.get("value");
					assFileHandler.insert(line, "TEXT");
					line = "	stw 0," + getVariableSymbolOrNumber(currentNode.getValue()) + "(31)";
					assFileHandler.insert(line, "TEXT");

				// 变量
				} else if(expres.get("type").equals("VARIABLE")) {
					// 把数放到r0中，再把r0总的数转到目标寄存器中, 同float
					line = "	lwz 0," + getVariableSymbolOrNumber(expres.get("value")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					line = "	stw 0," + getVariableSymbolOrNumber(currentNode.getValue()) + "(31)";
					assFileHandler.insert(line, "TEXT");
				
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
		
		assFileHandler.insert("", "TEXT");
	}
	
	// if-else语句
	private void _controlIfElse(SyntaxTreeNode node) {
		// 暂存if-else中的标签
		Map<String, String> labelsIfelse = new HashMap<>();
		labelsIfelse.put("label_else", ".L" + labelCnt);
		labelCnt++;
		labelsIfelse.put("label_end", ".L" + labelCnt);
		labelCnt++;
		
		String line = null;
		SyntaxTreeNode currentNode = node.getFirstSon();
		while (currentNode != null) {
			if(currentNode.getValue().equals("IfControl")) {
				// 检验if语句是否正确
				if(!currentNode.getFirstSon().getValue().equals("Expression") 
						|| !currentNode.getFirstSon().getRight().getValue().equals("Sentence")) {
					try {
						throw new Exception("if statement is error");
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
		
				}
				
				Map<String, String> expres =  _expression(currentNode.getFirstSon());;
				line = "	lwz 0," + getVariableSymbolOrNumber(expres.get("value")) + "(31)";
				assFileHandler.insert(line, "TEXT");
				line = "	cmpi 7,0,0,0";
				assFileHandler.insert(line, "TEXT");
				line = "	beq 7," + labelsIfelse.get("label_else");
				assFileHandler.insert(line, "TEXT");
				assFileHandler.insert("", "TEXT");	// 插入一个空行
				traverse(currentNode.getFirstSon().getRight().getFirstSon());
				line = "	b " + labelsIfelse.get("label_end");
				assFileHandler.insert(line, "TEXT");
				line = labelsIfelse.get("label_else") + ":";
				assFileHandler.insert(line, "TEXT");
				assFileHandler.insert("", "TEXT");	// 插入一个空行
				
			} else if (currentNode.getValue().equals("ElseControl")) {
				traverse(currentNode.getFirstSon());
				line = labelsIfelse.get("label_end") + ":";
				assFileHandler.insert(line, "TEXT");
				assFileHandler.insert("", "TEXT");	// 插入一个空行
				
				
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
		labelsFor.put("label1", ".L" + labelCnt);
		labelCnt++;
		labelsFor.put("label2", ".L" + labelCnt);
		labelCnt++;

		// for的一、二、三部分都可缺失，故不进行语法检验
		
		// 遍历的是for循环中的第2部分，即中间的条件表达式
		int cnt = 2;
		// 保存条件表达式的入口节点，以便后续再出处理
		SyntaxTreeNode forCondition = null;
		
		String line = null;
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
					line = "	b " + labelsFor.get("label1");
					assFileHandler.insert(line, "TEXT");
					line = labelsFor.get("label2") + ":";
					assFileHandler.insert(line, "TEXT");
					
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
		assFileHandler.insert(line, "TEXT");
		// 延迟到此处才处理条件表达式
		Map<String, String> expres =  _expression(forCondition);
		line = "	lwz 0," + getVariableSymbolOrNumber(expres.get("value")) + "(31)";
		assFileHandler.insert(line, "TEXT");
		line = "	cmpi 7,0,0,0";
		assFileHandler.insert(line, "TEXT");
		line = "	bne 7," + labelsFor.get("label2");
		assFileHandler.insert(line, "TEXT");
		
		assFileHandler.insert("", "TEXT");	// 增加一个空行
	}
	
	// while语句
	private void _controlWhile(SyntaxTreeNode node) {
		// 暂存while开始和结束的标签
		Map<String, String> labelsWhile = new HashMap<>();
		labelsWhile.put("label1", ".L" + labelCnt);
		labelCnt++;
		labelsWhile.put("label2", ".L" + labelCnt);
		labelCnt++;
		
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
		SyntaxTreeNode currentNode = node.getFirstSon();
		while (currentNode != null) {
			// while第一部分
			if (currentNode.getValue().equals("Expression")) {
				line = "	b " + labelsWhile.get("label1");
				assFileHandler.insert(line, "TEXT");
				line = labelsWhile.get("label2") + ":";
				assFileHandler.insert(line, "TEXT");
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
		assFileHandler.insert(line, "TEXT");
		Map<String, String> expres =  _expression(whileCondition);
		line = "	lwz 0," + getVariableSymbolOrNumber(expres.get("value")) + "(31)";
		assFileHandler.insert(line, "TEXT");
		line = "	cmpi 7,0,0,0";
		assFileHandler.insert(line, "TEXT");
		line = "	bne 7," + labelsWhile.get("label2");
		assFileHandler.insert(line, "TEXT");
		
		assFileHandler.insert("", "TEXT");	// 增加一个空行
	}
	
	// do-while语句
	private void _controlDoWhile(SyntaxTreeNode node) {
		// 暂存标签
		Map<String, String> labelsDoWhile = new HashMap<>();
		labelsDoWhile.put("label1", ".L" + labelCnt);
		labelCnt++;
		
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
		line = labelsDoWhile.get("label1") + ":";
		assFileHandler.insert(line, "TEXT");
		
		SyntaxTreeNode currentNode = node.getFirstSon();
		while (currentNode != null) {
			// do-while的循环体
			if (currentNode.getValue().equals("Sentence")) {
				traverse(currentNode.getFirstSon());
				
			// do-while条件表达式
			} else if (currentNode.getValue().equals("Expression")) {
				Map<String, String> expres =  _expression(currentNode);
				line = "	lwz 0," + getVariableSymbolOrNumber(expres.get("value")) + "(31)";
				assFileHandler.insert(line, "TEXT");
				line = "	cmpi 7,0,0,0";
				assFileHandler.insert(line, "TEXT");
				line = "	bne 7," + labelsDoWhile.get("label1");
				assFileHandler.insert(line, "TEXT");
				
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
		
		assFileHandler.insert("", "TEXT");	// 增加一个空行
	}
	
	// return语句
	private void _return(SyntaxTreeNode node) {
		// return语句的语法检验
		if(!node.getFirstSon().getValue().equals("return") 
				|| !node.getFirstSon().getRight().getValue().equals("Expression")) {
			try {
				throw new Exception("return error");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		SyntaxTreeNode currentNode = node.getFirstSon().getRight();
		Map<String, String> expres = _expression(currentNode);
		if(expres.get("type").equals("CONSTANT")) {
			String line = "	li 0," + expres.get("value");
			assFileHandler.insert(line, "TEXT");
			line = "	mr 3,0";
			assFileHandler.insert(line, "TEXT");
			
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
		return AssemblerExpression.handle(node, memAdress, assFileHandler, 
				isVariableSymbolOrNumber, symbolTable);
		
	}
	
	private void generateAssemblerFile(String fileName) {
		assFileHandler.generateAssemblerFile(fileName);
		
	}
	
	
	private void generateSymbolTableFile() {
		assFileHandler.generateSymbolTableFile(symbolTable);
		
	}
	
	private void outputAssembler() {
		System.out.println("===================Assembler==================");
		assFileHandler.dispalyResult();
		
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
