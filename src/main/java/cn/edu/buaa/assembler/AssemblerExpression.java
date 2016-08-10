package cn.edu.buaa.assembler;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.buaa.constant.AssemblerDefine;
import cn.edu.buaa.pojo.SyntaxTreeNode;

public class AssemblerExpression {
			
	private static final Logger logger = LoggerFactory.getLogger(AssemblerExpression.class);
	
	// 表达式中的操作数栈
	private static Stack<Map<String, String>> operandStack = new Stack<>();
			
	// 操作符和操作数栈
	private static Stack<Map<String, String>> optAndOpdStack = new Stack<>();
	
	// 要生成的汇编文件管理器
	private static AssemblerFileHandler assFileHandler;
		
	// 符号表
	private static Map<String, Map<String, String>> symbolTable;
	
	private static boolean isVariableSymbolOrNumber = true;
	
	private static int memAdress;
	
	private static int bss_tmp_cnt;
	
	// 处理表达式
	public static Map<String, String> handle(SyntaxTreeNode node, int memAdress, AssemblerFileHandler assFileHandler,
			boolean isVariableSymbolOrNumber, Map<String, Map<String, String>> symbolTable) {
		// 处理常量
		if(node.getType().equals("Constant")) {
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "CONSTANT");
			tmpMap.put("value", node.getFirstSon().getValue());
			return tmpMap;
		}
		
		// 初始化，清空栈
		operandStack.clear();
		optAndOpdStack.clear();
		
		// 把Assembler中传过来的变量保存到静态变量中
		AssemblerExpression.assFileHandler = assFileHandler;
		AssemblerExpression.isVariableSymbolOrNumber = isVariableSymbolOrNumber;
		AssemblerExpression.symbolTable = symbolTable;
		AssemblerExpression.memAdress = memAdress;
		AssemblerExpression.bss_tmp_cnt = 1;
		
		// 遍历该表达式
		traverseExpression(node);
		
		for (Map<String, String> item : optAndOpdStack) {
			// 当前遍历到的为操作符，则计算
			if(item.get("type").equals("OPERATOR")) {
				String operator = item.get("operand");
				
				// 双目运算符
				if(AssemblerDefine.DOUBLE_OPERATORS.contains(operator)) {
					solveDoubleOperator(operator);
					
				// 单目运算符
				} else if (AssemblerDefine.SINGLE_OPERATORS.contains(operator)) {
					solveSingleOperator(operator);
					
				} else {
					try {
						throw new Exception("other operator not support int expression : " + operator);
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				assFileHandler.insert("", "TEXT");
				
			// 变量直接放入操作数栈
			} else if (item.get("type").equals("VARIABLE")) {
				operandStack.push(item);
			
			// 常量直接放入操作数栈
			} else if (item.get("type").equals("CONSTANT")) {
				operandStack.push(item);
			
			// 数组项直接放入操作数栈
			} else if(item.get("type").equals("ARRAY_ITEM")) {
				logger.debug("AssemblerExpression : ARRAY_ITEM");
				
			} else {
				try {
					throw new Exception("other operator or operand not support int expression : " + item.get("type"));
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			
			}

		}
		
		Map<String, String> result = new HashMap<>();
		if(!operandStack.empty()) {
			result.put("type", operandStack.get(0).get("type"));
			result.put("value", operandStack.get(0).get("operand"));
		} else {
			result.put("type", "");
			result.put("value", "");
		}
		
		return result;
	}

	// 从表达式中识别出操作数和操作符号
	private static void traverseExpression(SyntaxTreeNode node) {
		if (node == null) {
			return;
		}
		
		if(node.getType().equals("_Variable")) {
			Map<String, String> item = new HashMap<>();
			item.put("type", "VARIABLE");
			item.put("operand", node.getValue());
			optAndOpdStack.push(item);
			
		} else if(node.getType().equals("_Constant")) {
			Map<String, String> item = new HashMap<>();
			item.put("type", "CONSTANT");
			item.put("operand", node.getValue());
			optAndOpdStack.push(item);
			
		} else if(node.getType().equals("_Operator")) {
			Map<String, String> item = new HashMap<>();
			item.put("type", "OPERATOR");
			item.put("operand", node.getValue());
			optAndOpdStack.push(item);
			
		} else if(node.getType().equals("_ArrayName")) {
			Map<String, String> item = new HashMap<>();
			item.put("type", "ARRAY_ITEM");
			item.put("operand0", node.getValue());
			item.put("operand1", node.getRight().getValue());
			optAndOpdStack.push(item);
		
		// 为非叶子节点，故需要递归遍历
		} else {
			SyntaxTreeNode currentNode = node.getFirstSon();
			while(currentNode != null) {
				traverseExpression(currentNode);
				currentNode = currentNode.getRight();
			}
			
		} 
		
	}
	
	private static String getVariableSymbolOrNumber(String parameter) {
		return (isVariableSymbolOrNumber ? symbolTable.get(parameter).get("register") : parameter);
				
	}
	
	// 判断一个变量是不是float类型
	private static boolean isFloat(Map<String, String> operand) {
		return operand.get("type").equals("VARIABLE")
				&& symbolTable.get(operand.get("operand")).get("field_type").equals("float");
	}

	// 判断两个操作数中是否含有float类型的数
	private static boolean containFloat(Map<String, String> operand_a, Map<String, String> operand_b) {
		return isFloat(operand_a) || isFloat(operand_b);
	}
	
	// 处理双目运算
	private static void solveDoubleOperator(String operator) {
		Map<String, String> operand_b = operandStack.pop();
		Map<String, String> operand_a = operandStack.pop();
		
		// 判断是否含有浮点数
		boolean containFloat = containFloat(operand_a, operand_b);
		String line = null;
		if(operator.equals("+")) {
			if (containFloat) {
				logger.debug("solveDoubleOperator : float");
				
			} else {
				// 第一个操作数
				if (operand_a.get("type").equals("VARIABLE")) {
					line = "	lwz 9," + getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if (operand_a.get("type").equals("CONSTANT")) {
					line = "	li 9," + operand_a.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("+ not support type : " + operand_a.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				// 第二个操作数
				if (operand_b.get("type").equals("VARIABLE")) {
					line = "	lwz 0," + getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if (operand_b.get("type").equals("CONSTANT")) {
					line = "	li 0," + operand_b.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("+ not support type : " + operand_b.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
						
					}
					
				}
				
				// 执行加法
				line = "	add 0,9,0";
				assFileHandler.insert(line, "TEXT");
				
				// 赋值给临时操作数
				String bss_tmp = "bss_tmp" + bss_tmp_cnt;
				bss_tmp_cnt++;
				// 记录到符号表中
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "IDENTIFIER");
				tmpMap.put("field_type", "int");
				tmpMap.put("register", Integer.toString(memAdress));
				memAdress += 4;
				symbolTable.put(bss_tmp, tmpMap);
				
				line = "	stw 0," + getVariableSymbolOrNumber(bss_tmp) + "(31)";
				assFileHandler.insert(line, "TEXT");
				// 计算结果压栈
				tmpMap = new HashMap<>();
				tmpMap.put("type", "VARIABLE");
				tmpMap.put("operand", bss_tmp);
				operandStack.push(tmpMap);
			}
			
		} else if (operator.equals("-")) {
			if(containFloat) {
				logger.debug("solveDoubleOperator : float");
				
			} else {
				// 被减数
				if(operand_a.get("type").equals("VARIABLE")) {						
					line = "	lwz 9," + getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_a.get("type").equals("CONSTANT")) {
					line = "	li 9," + operand_a.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("- not support type : " + operand_a.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				// 减数
				if(operand_b.get("type").equals("VARIABLE")) {
					line = "	lwz 0," + getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_b.get("type").equals("CONSTANT")) {
					line = "	li 0," + operand_b.get("operand");
					assFileHandler.insert(line, "TEXT");
	
				} else {
					try {
						throw new Exception("- not support type : " + operand_b.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				// 执行减操作
				line = "	subf 0,9,0";
				assFileHandler.insert(line, "TEXT");
				
				// 赋值给临时操作数
				String bss_tmp = "bss_tmp" + bss_tmp_cnt;
				bss_tmp_cnt++;
				// 记录到符号表中
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "IDENTIFIER");
				tmpMap.put("field_type", "int");
				tmpMap.put("register", Integer.toString(memAdress));
				memAdress += 4;
				symbolTable.put(bss_tmp, tmpMap);
				
				line = "	stw 0," + getVariableSymbolOrNumber(bss_tmp) + "(31)";
				assFileHandler.insert(line, "TEXT");
				// 计算结果压栈
				tmpMap = new HashMap<>();
				tmpMap.put("type", "VARIABLE");
				tmpMap.put("operand", bss_tmp);
				operandStack.push(tmpMap);
			}
			
		// 整数乘法
		} else if (operator.equals("*")) {
			if(containFloat) {
				logger.debug("solveDoubleOperator : float");
				
			} else {
				if(operand_a.get("type").equals("VARIABLE")) {						
					line = "	lwz 9," + getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");				
					
				} else if(operand_a.get("type").equals("CONSTANT")) {
					line = "	li 9," + operand_a.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("* not support type : " + operand_a.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				if(operand_b.get("type").equals("VARIABLE")) {
					line = "	lwz 0," + getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_b.get("type").equals("CONSTANT")) {
					line = "	li 0," + operand_b.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("* not support type : " + operand_b.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				// 执行乘法指令
				line = "	mullw 0,9,0";
				assFileHandler.insert(line, "TEXT");
				
				// 赋值给临时操作数
				String bss_tmp = "bss_tmp" + bss_tmp_cnt;
				bss_tmp_cnt++;
				// 记录到符号表中
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "IDENTIFIER");
				tmpMap.put("field_type", "int");
				tmpMap.put("register", Integer.toString(memAdress));
				memAdress += 4;
				symbolTable.put(bss_tmp, tmpMap);
				
				line = "	stw 0," + getVariableSymbolOrNumber(bss_tmp)  + "(31)";
				assFileHandler.insert(line, "TEXT");
				// 计算结果压栈
				tmpMap = new HashMap<>();
				tmpMap.put("type", "VARIABLE");
				tmpMap.put("operand", bss_tmp);
				operandStack.push(tmpMap);
				
			}
			
		// 整数除法
		} else if (operator.equals("/")) {
			if(containFloat) {
				logger.debug("float /");
				
			} else {
				if(operand_a.get("type").equals("VARIABLE")) {						
					line = "	lwz 9," + getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_a.get("type").equals("CONSTANT")) {
					line = "	li 9," + operand_a.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("/ not support type : " + operand_a.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				if(operand_b.get("type").equals("VARIABLE")) {
					line = "	lwz 0," + 
							(isVariableSymbolOrNumber ? symbolTable.get(operand_b.get("operand")).get("register") : operand_b.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_b.get("type").equals("CONSTANT")) {
					line = "	li 0," + operand_b.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("/ not support type : " + operand_b.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				// 执行除法指令
				line = "	divw 0,9,0";
				assFileHandler.insert(line, "TEXT");
				
				// 赋值给临时操作数
				String bss_tmp = "bss_tmp" + bss_tmp_cnt;
				bss_tmp_cnt++;
				// 记录到符号表中
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "IDENTIFIER");
				tmpMap.put("field_type", "int");
				tmpMap.put("register", Integer.toString(memAdress));
				memAdress += 4;
				symbolTable.put(bss_tmp, tmpMap);
				
				line = "	stw 0," + (isVariableSymbolOrNumber ? symbolTable.get(bss_tmp).get("register") : bss_tmp) + "(31)";
				assFileHandler.insert(line, "TEXT");
				// 计算结果压栈
				tmpMap = new HashMap<>();
				tmpMap.put("type", "VARIABLE");
				tmpMap.put("operand", bss_tmp);
				operandStack.push(tmpMap);
				
			}
			
		// 取余操作
		} else if (operator.equals("%")) {
			if(containFloat) {
				logger.debug("float %");
				
			} else {
				if(operand_a.get("type").equals("VARIABLE")) {						
					line = "	lwz 0," + getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_a.get("type").equals("CONSTANT")) {
					line = "	li 0," + operand_a.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("% not support type : " + operand_a.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				if(operand_b.get("type").equals("VARIABLE")) {
					line = "	lwz 9," + getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_b.get("type").equals("CONSTANT")) {
					line = "	li 9," + operand_b.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("% not support type : " + operand_b.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				// 取余操作转化为除法、减法等指令来操作
				line = "	divw 11,0,9";
				assFileHandler.insert(line, "TEXT");
				line = "	mullw 9,11,9";
				assFileHandler.insert(line, "TEXT");
				line = "	subf 0,9,0";
				assFileHandler.insert(line, "TEXT");
				
				// 赋值给临时操作数
				String bss_tmp = "bss_tmp" + bss_tmp_cnt;
				bss_tmp_cnt++;
				// 记录到符号表中
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "IDENTIFIER");
				tmpMap.put("field_type", "int");
				tmpMap.put("register", Integer.toString(memAdress));
				memAdress += 4;
				symbolTable.put(bss_tmp, tmpMap);
				
				line = "	stw 0," + getVariableSymbolOrNumber(bss_tmp) + "(31)";
				assFileHandler.insert(line, "TEXT");
				// 计算结果压栈
				tmpMap = new HashMap<>();
				tmpMap.put("type", "VARIABLE");
				tmpMap.put("operand", bss_tmp);
				operandStack.push(tmpMap);
				
			}
			
		//  >=关系运算
		} else if (operator.equals(">=")) {
			if(containFloat) {
				logger.debug("float >=");
				
			} else {
				if(operand_a.get("type").equals("VARIABLE")) {						
					line = "	lwz 0," + getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_a.get("type").equals("CONSTANT")) {
					line = "	li 0," + operand_a.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception(">= not support type : " + operand_a.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				if(operand_b.get("type").equals("VARIABLE")) {
					line = "	lwz 9," 
							+ (isVariableSymbolOrNumber ? symbolTable.get(operand_b.get("operand")).get("register") : operand_b.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_b.get("type").equals("CONSTANT")) {
					line = "	li 9," + operand_b.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception(">= not support type : " + operand_b.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				// 比较操作
				line = "	cmp 7,0,0,9";
				assFileHandler.insert(line, "TEXT");
				line = "	li 0,1";
				assFileHandler.insert(line, "TEXT");
				line = "	isel 0,0,0,28";
				assFileHandler.insert(line, "TEXT");
				
				// 赋值给临时操作数
				String bss_tmp = "bss_tmp" + bss_tmp_cnt;
				bss_tmp_cnt++;
				// 记录到符号表中
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "IDENTIFIER");
				tmpMap.put("field_type", "int");
				tmpMap.put("register", Integer.toString(memAdress));
				memAdress += 4;
				symbolTable.put(bss_tmp, tmpMap);
				
				line = "	stw 0," + getVariableSymbolOrNumber(bss_tmp) + "(31)";
				assFileHandler.insert(line, "TEXT");
				// 计算结果压栈
				tmpMap = new HashMap<>();
				tmpMap.put("type", "VARIABLE");
				tmpMap.put("operand", bss_tmp);
				operandStack.push(tmpMap);
				
			}
			
		// >关系运算符
		} else if(operator.equals(">")) {
			if(containFloat) {
				logger.debug("float >");
				
			} else {
				if(operand_a.get("type").equals("VARIABLE")) {						
					line = "	lwz 0," + getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_a.get("type").equals("CONSTANT")) {
					line = "	li 0," + operand_a.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("> not support type : " + operand_a.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				if(operand_b.get("type").equals("VARIABLE")) {
					line = "	lwz 9," + getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_b.get("type").equals("CONSTANT")) {
					line = "	li 9," + operand_b.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("> not support type : " + operand_b.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				// 比较指令
				line = "	cmp 7,0,0,9";
				assFileHandler.insert(line, "TEXT");
				line = "	li 0,0";
				assFileHandler.insert(line, "TEXT");
				line = "	li 9,1";
				assFileHandler.insert(line, "TEXT");
				line = "	isel 0,9,0,29";
				assFileHandler.insert(line, "TEXT");
				
				// 赋值给临时操作数
				String bss_tmp = "bss_tmp" + bss_tmp_cnt;
				bss_tmp_cnt++;
				// 记录到符号表中
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "IDENTIFIER");
				tmpMap.put("field_type", "int");
				tmpMap.put("register", Integer.toString(memAdress));
				memAdress += 4;
				symbolTable.put(bss_tmp, tmpMap);
				
				line = "	stw 0," 
						+ (isVariableSymbolOrNumber ? symbolTable.get(bss_tmp).get("register") : bss_tmp) + "(31)";
				assFileHandler.insert(line, "TEXT");
				// 计算结果压栈
				tmpMap = new HashMap<>();
				tmpMap.put("type", "VARIABLE");
				tmpMap.put("operand", bss_tmp);
				operandStack.push(tmpMap);
				
			}
			
		// <=符号
		} else if (operator.equals("<=")) {
			if(containFloat) {
				logger.debug("float <=");
				
			} else {
				if(operand_a.get("type").equals("VARIABLE")) {						
					line = "	lwz 0," + getVariableSymbolOrNumber(operand_a.get("operand"))  + "(31)";
					assFileHandler.insert(line, "TEXT");							
					
				} else if(operand_a.get("type").equals("CONSTANT")) {
					line = "	li 0," + operand_a.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("<= not support type : " + operand_a.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				if(operand_b.get("type").equals("VARIABLE")) {
					line = "	lwz 9," + getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_b.get("type").equals("CONSTANT")) {
					line = "	li 9," + operand_b.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("> not support type : " + operand_b.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				// 执行比较操作
				line = "	cmp 7,0,0,9";
				assFileHandler.insert(line, "TEXT");
				line = "	li 0,1";
				assFileHandler.insert(line, "TEXT");
				line = "	isel 0,0,0,29";
				assFileHandler.insert(line, "TEXT");
				
				// 赋值给临时操作数
				String bss_tmp = "bss_tmp" + bss_tmp_cnt;
				bss_tmp_cnt++;
				// 记录到符号表中
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "IDENTIFIER");
				tmpMap.put("field_type", "int");
				tmpMap.put("register", Integer.toString(memAdress));
				memAdress += 4;
				symbolTable.put(bss_tmp, tmpMap);
				
				line = "	stw 0," + getVariableSymbolOrNumber(bss_tmp) + "(31)";
				assFileHandler.insert(line, "TEXT");
				// 计算结果压栈
				tmpMap = new HashMap<>();
				tmpMap.put("type", "VARIABLE");
				tmpMap.put("operand", bss_tmp);
				operandStack.push(tmpMap);
				
			}
			
		// < 符号
		} else if (operator.equals("<")) {
			if(containFloat) {
				logger.debug("float <");
				
			} else {
				if(operand_a.get("type").equals("VARIABLE")) {						
					line = "	lwz 0," + getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_a.get("type").equals("CONSTANT")) {
					line = "	li 0," + operand_a.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("< not support type : " + operand_a.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				if(operand_b.get("type").equals("VARIABLE")) {
					line = "	lwz 9," + getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_b.get("type").equals("CONSTANT")) {
					line = "	li 9," + operand_b.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("< not support type : " + operand_b.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				// 比较操作
				line = "	cmp 7,0,0,9";
				assFileHandler.insert(line, "TEXT");
				line = "	li 0,0";
				assFileHandler.insert(line, "TEXT");
				line = "	li 9,1";
				assFileHandler.insert(line, "TEXT");
				line = "	isel 0,9,0,28";   // 28   CR7 = CR[28, 29, 30, 31] 
											  // (cr[crfD] : 有4位 : LT,GT,EQ,SO)
				assFileHandler.insert(line, "TEXT");
				
				// 赋值给临时操作数
				String bss_tmp = "bss_tmp" + bss_tmp_cnt;
				bss_tmp_cnt++;
				// 记录到符号表中
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "IDENTIFIER");
				tmpMap.put("field_type", "int");
				tmpMap.put("register", Integer.toString(memAdress));
				memAdress += 4;
				symbolTable.put(bss_tmp, tmpMap);
				
				line = "	stw 0," + getVariableSymbolOrNumber(bss_tmp) + "(31)";
				assFileHandler.insert(line, "TEXT");
				// 计算结果压栈
				tmpMap = new HashMap<>();
				tmpMap.put("type", "VARIABLE");
				tmpMap.put("operand", bss_tmp);
				operandStack.push(tmpMap);
				
			} 
			
		// == 符号
		} else if (operator.equals("==")) {
			if(containFloat) {
				logger.debug("float <");
				
			} else {
				if(operand_a.get("type").equals("VARIABLE")) {						
					line = "	lwz 0," + getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_a.get("type").equals("CONSTANT")) {
					line = "	li 0," + operand_a.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("== not support type : " + operand_a.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				if(operand_b.get("type").equals("VARIABLE")) {
					line = "	lwz 9," + getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
					assFileHandler.insert(line, "TEXT");
					
				} else if(operand_b.get("type").equals("CONSTANT")) {
					line = "	li 9," + operand_b.get("operand");
					assFileHandler.insert(line, "TEXT");
					
				} else {
					try {
						throw new Exception("== not support type : " + operand_b.get("type"));
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				
				// 比较操作
				line = "	cmp 7,0,0,9";
				assFileHandler.insert(line, "TEXT");
				line = "	li 0,0";
				assFileHandler.insert(line, "TEXT");
				line = "	li 9,1";
				assFileHandler.insert(line, "TEXT");
				line = "	isel 0,9,0,30";
				assFileHandler.insert(line, "TEXT");
				
				// 赋值给临时操作数
				String bss_tmp = "bss_tmp" + bss_tmp_cnt;
				bss_tmp_cnt++;
				// 记录到符号表中
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "IDENTIFIER");
				tmpMap.put("field_type", "int");
				tmpMap.put("register", Integer.toString(memAdress));
				memAdress += 4;
				symbolTable.put(bss_tmp, tmpMap);
				
				line = "	stw 0," + getVariableSymbolOrNumber(bss_tmp) + "(31)";
				assFileHandler.insert(line, "TEXT");
				// 计算结果压栈
				tmpMap = new HashMap<>();
				tmpMap.put("type", "VARIABLE");
				tmpMap.put("operand", bss_tmp);
				operandStack.push(tmpMap);
				
			}
			
		} else {
			try {
				throw new Exception("other operator not support in double operator : " + operator);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			
		}
		
	}
	
	// 处理单目运算
	private static void solveSingleOperator(String operator) {
		// 取出操作数
		Map<String, String> operand = operandStack.peek();
		String line = null;
		if(operator.equals("++")) {
			line = "	lwz 0," + getVariableSymbolOrNumber(operand.get("operand")) + "(31)";
			assFileHandler.insert(line, "TEXT");
			line = "	addic 0,0,1";
			assFileHandler.insert(line, "TEXT");
			line = "	stw 0," + getVariableSymbolOrNumber(operand.get("operand")) + "(31)";
			assFileHandler.insert(line, "TEXT");
			
		} else if(operator.equals("--")) {
			line = "	lwz 0," + getVariableSymbolOrNumber(operand.get("operand")) + "(31)";
			assFileHandler.insert(line, "TEXT");
			line = "	addic 0,0,-1";
			assFileHandler.insert(line, "TEXT");
			line = "	stw 0," + getVariableSymbolOrNumber(operand.get("operand")) + "(31)";
			assFileHandler.insert(line, "TEXT");
			
		} else {
			try {
				throw new Exception("Only suport ++ and -- singleOperator : " + operator);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			
		}
		
	}

}
