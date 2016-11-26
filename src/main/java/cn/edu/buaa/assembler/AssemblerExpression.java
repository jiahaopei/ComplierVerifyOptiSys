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
	
	private static AssemblerDTO assemblerDTO;
	
	private static int memAdress;
	
	private static int bss_tmp_cnt;
	
	// 处理表达式
	public static Map<String, String> handle(SyntaxTreeNode node, AssemblerDTO assemblerDTO) {
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
		
		// 初始化
		AssemblerExpression.assemblerDTO = assemblerDTO;
		memAdress = assemblerDTO.getMemAdress();
		bss_tmp_cnt = 1;
		
		// 遍历该表达式
		traverseExpression(node);
		
		for (Map<String, String> item : optAndOpdStack) {
			// 当前遍历到的为操作符，则计算
			if(item.get("type").equals("OPERATOR")) {
				String operator = item.get("operand");
				
				// 双目运算符
				if(AssemblerDefine.DOUBLE_OPERATORS.contains(operator)) {
					solveTwoOperator(operator, item.get("label"));
					
				// 单目运算符
				} else if (AssemblerDefine.SINGLE_OPERATORS.contains(operator)) {
					solveOneOperator(operator, item.get("label"));
					
				} else {
					try {
						throw new Exception("other operator not support int expression : " + operator);
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
					
				}
				assemblerDTO.insertIntoText("", null);
				
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
					throw new Exception(
							"other operator or operand not support int expression : " + item.get("type"));
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
			item.put("label", node.getLabel());
			optAndOpdStack.push(item);
			
		} else if(node.getType().equals("_Constant")) {
			Map<String, String> item = new HashMap<>();
			item.put("type", "CONSTANT");
			item.put("operand", node.getValue());
			item.put("label", node.getLabel());
			optAndOpdStack.push(item);
			
		} else if(node.getType().equals("_Operator")) {
			Map<String, String> item = new HashMap<>();
			item.put("type", "OPERATOR");
			item.put("operand", node.getValue());
			item.put("label", node.getLabel());
			optAndOpdStack.push(item);
			
		} else if(node.getType().equals("_ArrayName")) {
			Map<String, String> item = new HashMap<>();
			item.put("type", "ARRAY_ITEM");
			item.put("operand0", node.getValue());
			item.put("operand1", node.getRight().getValue());
			item.put("label", node.getLabel());
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
	
	// 判断一个变量是不是float类型
	public static boolean isFloat(Map<String, String> operand) {
		return operand.get("type").equals("VARIABLE")
				&& assemblerDTO.getMapFromSymbolTable(operand.get("operand")).get("field_type").equals("float");
	}

	// 判断两个操作数中是否含有float类型的数
	public static boolean containFloat(Map<String, String> operand_a, Map<String, String> operand_b) {
		return isFloat(operand_a) || isFloat(operand_b);
	}
	
	// 判断一个操作数的类型；
	// 若为变量可以通过符号表判断，若为常量需要判断为整数或者double
	private static String getFieldType(Map<String, String> operand) {
		if (operand.get("type").equals("VARIABLE")) {
			return assemblerDTO.getMapFromSymbolTable(operand.get("operand")).get("field_type");
			
		} else if (operand.get("type").equals("CONSTANT")) {
			if (operand.get("operand").startsWith("'")
					&& operand.get("operand").endsWith("'")) {
				return "char";
				
			} else if (operand.get("operand").contains(".")) {
				if (operand.get("operand").endsWith("f") || operand.get("operand").endsWith("F")) {
					return "float";
				} else {
					return "double";
				}
				
			} else {
				if (operand.get("operand").endsWith("l") || operand.get("operand").endsWith("L")) {
					return "long";
				} else {
					return "int";
				}
				
			}
			
		} else {
			return "errorType";
			
		}
	}
	
	// 取两个操作数的公共类型
	private static String commonFieldType(Map<String, String> operand_a, Map<String, String> operand_b) {
		String aType = getFieldType(operand_a);
		String bType = getFieldType(operand_b);
		
		if (aType.equals("errorType") || bType.equals("errorType")) {
			return "errorType";
		}
		
		// 转换char为Ascii码，再参与计算
		if (aType.equals("char") 
				&& operand_a.get("operand").startsWith("'") 
				&& operand_a.get("operand").endsWith("'")) {
			int pos = 1;
			String word = operand_a.get("operand");
			if (word.charAt(pos) == '\\') {
				pos++;
			}
			operand_a.put("operand", Integer.toString((int) word.charAt(pos)));
		}
		if (bType.equals("char")
				&& operand_b.get("operand").startsWith("'") 
				&& operand_b.get("operand").endsWith("'")) {
			int pos = 1;
			String word = operand_b.get("operand");
			if (word.charAt(pos) == '\\') {
				pos++;
			}
			operand_b.put("operand", Integer.toString((int) word.charAt(pos)));
		}
		
		if (aType.equals("double") || bType.equals("double")) {
			return "double";
		
		} else if (aType.equals("float") || bType.equals("float")) {
			return "float";
		
		} else if (aType.equals("long") || bType.equals("long")) {
			return "long";
			
		} else if (aType.equals("int") || bType.equals("int")) {
			return "int";
			
		} else if (aType.equals("short") || bType.equals("short")) { 
			return "short";
			
		} else if (aType.equals("char") || bType.equals("char")) { 
			return "char";
			
		} else {
			return "errorType";
			
		}
		
	}
	
	// 处理双目运算
	private static void solveTwoOperator(String operator, String label) {
		Map<String, String> operand_b = operandStack.pop();
		Map<String, String> operand_a = operandStack.pop();
				
		// 由操作数的类型决定交给谁处理
		String fieldType = commonFieldType(operand_a, operand_b);
		switch (fieldType) {
		case "double":
			solveTwoOperatorDouble(operand_a, operand_b, operator, label);
			break;
		case "float":
			solveTwoOperatorFloat(operand_a, operand_b, operator, label);
			break;
		case "long":
			solveTwoOperatorLong(operand_a, operand_b, operator, label);
			break;
		case "int":
			solveTwoOperatorInt(operand_a, operand_b, operator, label);
			break;
		case "short":
			solveTwoOperatorShort(operand_a, operand_b, operator, label);
			break;
		case "char":
			solveTwoOperatorChar(operand_a, operand_b, operator, label);
			break;
		default:
			throw new RuntimeException("not support this field type : " + fieldType);
		}

	}
	
	private static void solveTwoOperatorShort(Map<String, String> operand_a, Map<String, String> operand_b,
			String operator, String label) {
		String line = null;
		if (operator.equals("&&")) {
			Map<String, String> labels = new HashMap<>();
			labels.put("label1", ".L" + assemblerDTO.getLabelCnt());
			assemblerDTO.addToLabelCnt(1);
			labels.put("label2", ".L" + assemblerDTO.getLabelCnt());
			assemblerDTO.addToLabelCnt(1);

			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lhz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "extsh 0,0";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("&& not support type : " + operand_a.get("type"));

			}

			line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "beq 7," + labels.get("label1");
			assemblerDTO.insertIntoText(line, label);

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lhz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "extsh 0,0";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("&& not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "beq 7," + labels.get("label1");
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "b " + labels.get("label2");
			assemblerDTO.insertIntoText(line, label);
			line = labels.get("label1") + ":";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = labels.get("label2") + ":";
			assemblerDTO.insertIntoText(line, label);

			// || 符号
		} else if (operator.equals("||")) {
			Map<String, String> labels = new HashMap<>();
			labels.put("label1", ".L" + assemblerDTO.getLabelCnt());
			assemblerDTO.addToLabelCnt(1);
			labels.put("label2", ".L" + assemblerDTO.getLabelCnt());
			assemblerDTO.addToLabelCnt(1);
			labels.put("label3", ".L" + assemblerDTO.getLabelCnt());
			assemblerDTO.addToLabelCnt(1);

			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lhz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "extsh 0,0";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("|| not support type : " + operand_a.get("type"));

			}

			line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "bne 7," + labels.get("label1");
			assemblerDTO.insertIntoText(line, label);

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lhz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "extsh 0,0";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("|| not support type : " + operand_b.get("type"));

			}
			
		// 其它
		} else {
			// 第一个操作数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lhz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "extsh 0,0";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("short expression not support type : " + operand_a.get("type"));

			}

			// 第二个操作数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lhz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "extsh 0,0";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("short expression not support type : " + operand_b.get("type"));

			}

			if (operator.equals("+")) {
				// 执行加法
				line = AssemblerUtils.PREFIX + "add 0,9,0";
				assemblerDTO.insertIntoText(line, label);

			} else if (operator.equals("-")) {
				// 执行减操作
				line = AssemblerUtils.PREFIX + "subf 0,9,0";
				assemblerDTO.insertIntoText(line, label);

				// 整数乘法
			} else if (operator.equals("*")) {
				// 执行乘法指令
				line = AssemblerUtils.PREFIX + "mullw 0,9,0";
				assemblerDTO.insertIntoText(line, label);

				// 整数除法
			} else if (operator.equals("/")) {
				// 执行除法指令
				line = AssemblerUtils.PREFIX + "divw 0,9,0";
				assemblerDTO.insertIntoText(line, label);

				// 取余操作
			} else if (operator.equals("%")) {
				// 取余操作转化为除法、减法等指令来操作
				line = AssemblerUtils.PREFIX + "divw 11,0,9";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "mullw 9,11,9";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "subf 0,9,0";
				assemblerDTO.insertIntoText(line, label);

				// >=关系运算
			} else if (operator.equals(">=")) {
				// 比较操作
				line = AssemblerUtils.PREFIX + "cmpl 7,0,0,9";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "li 0,1";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "isel 0,0,0,28";
				assemblerDTO.insertIntoText(line, label);

				// >关系运算符
			} else if (operator.equals(">")) {
				// 比较指令
				line = AssemblerUtils.PREFIX + "cmpl 7,0,0,9";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "li 0,0";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "li 9,1";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "isel 0,9,0,29";
				assemblerDTO.insertIntoText(line, label);

				// <=符号
			} else if (operator.equals("<=")) {
				// 执行比较操作
				line = AssemblerUtils.PREFIX + "cmp 7,0,0,9";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "li 0,1";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "isel 0,0,0,29";
				assemblerDTO.insertIntoText(line, label);

				// < 符号
			} else if (operator.equals("<")) {
				// 比较操作
				line = AssemblerUtils.PREFIX + "cmp 7,0,0,9";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "li 0,0";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "li 9,1";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "isel 0,9,0,28";
				assemblerDTO.insertIntoText(line, label);

				// == 符号
			} else if (operator.equals("==")) {
				// 比较操作
				line = AssemblerUtils.PREFIX + "cmp 7,0,0,9";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "li 0,0";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "li 9,1";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "isel 0,9,0,30";
				assemblerDTO.insertIntoText(line, label);

				// != 符号
			} else if (operator.equals("!=")) {
				// 比较操作
				line = AssemblerUtils.PREFIX + "xor 0,0,9";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "cmp 7,0,0,0";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "li 0,0";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "li 9,1";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "isel 0,9,0,30";
				assemblerDTO.insertIntoText(line, label);
				line = AssemblerUtils.PREFIX + "xori 0,0,1";
				assemblerDTO.insertIntoText(line, label);

				// << 符号
			} else if (operator.equals("<<")) {
				line = AssemblerUtils.PREFIX + "slw 0,9,0";
				assemblerDTO.insertIntoText(line, label);

				// >> 符号
			} else if (operator.equals(">>")) {
				line = AssemblerUtils.PREFIX + "sraw 0,9,0";
				assemblerDTO.insertIntoText(line, label);

				// & 符号
			} else if (operator.equals("&")) {
				line = AssemblerUtils.PREFIX + "and 0,9,0";
				assemblerDTO.insertIntoText(line, label);

				// | 符号
			} else if (operator.equals("|")) {
				line = AssemblerUtils.PREFIX + "or 0,9,0";
				assemblerDTO.insertIntoText(line, label);

				// ^ 符号
			} else if (operator.equals("^")) {
				line = AssemblerUtils.PREFIX + "xor 0,9,0";
				assemblerDTO.insertIntoText(line, label);

			} else {
				throw new RuntimeException("other operator not support in double operator : " + operator);

			}
		}

		// 赋值给临时操作数
		String bss_tmp = "bss_tmp" + bss_tmp_cnt;
		bss_tmp_cnt++;
		// 记录到符号表中
		Map<String, String> tmpMap = new HashMap<>();
		tmpMap.put("type", "IDENTIFIER");
		tmpMap.put("field_type", "short");
		tmpMap.put("register", Integer.toString(memAdress));
		memAdress += 2;
		assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

		line = AssemblerUtils.PREFIX + "sth 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
		assemblerDTO.insertIntoText(line, label);
		// 计算结果压栈
		tmpMap = new HashMap<>();
		tmpMap.put("type", "VARIABLE");
		tmpMap.put("operand", bss_tmp);
		tmpMap.put("label", operand_a.get("label"));
		operandStack.push(tmpMap);
	}

	// 处理long型的双目运算
	private static void solveTwoOperatorLong(Map<String, String> operand_a, Map<String, String> operand_b,
			String operator, String label) {
		// 同int型，测试发现对于long型power-pc支持得不是很好;
		// 过大的long不支持，比较小的long直接当做int来处理了
		if (operand_a.get("type").equals("CONSTANT")) {
			String value = operand_a.get("operand");
			if (value.endsWith("l") || value.endsWith("L")) {
				value = value.substring(0, value.length() - 1);
			}
			operand_a.put("operand", value);
		}
		
		if (operand_b.get("type").equals("CONSTANT")) {
			String value = operand_b.get("operand");
			if (value.endsWith("l") || value.endsWith("L")) {
				value = value.substring(0, value.length() - 1);
			}
			operand_b.put("operand", value);
		}
		solveTwoOperatorInt(operand_a, operand_b, operator, label);
	}

	// 处理float型的双目运算
	private static void solveTwoOperatorFloat(Map<String, String> operand_a, Map<String, String> operand_b,
			String operator, String label) {
		String line = null;
		if (operator.equals("+")) {
			// 第一个操作数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfs 13,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_a.get("operand"));
				String low = getNumberLow(operand_a.get("operand"));
				
				// 把字符常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line,label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);						
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "FLOAT_CONSTANT");
				tmpMap.put("value", operand_a.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);
				
				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "lfs 13," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				
			} else {
				throw new RuntimeException("+ not support type : " + operand_a.get("type"));
	
			}

			// 第二个操作数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfs 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_b.get("operand"));
				String low = getNumberLow(operand_b.get("operand"));
				
				// 把double常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line,label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);						
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "FLOAT_CONSTANT");
				tmpMap.put("value", operand_b.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);
				
				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "lfs 0," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("+ not support type : " + operand_b.get("type")); 
		
			}

			// 执行加法
			line = AssemblerUtils.PREFIX + "fadds 0,13,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "float");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stfs 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		} else if (operator.equals("-")) {
			// 第一个操作数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfs 13,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_a.get("operand"));
				String low = getNumberLow(operand_a.get("operand"));

				// 把字符常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "FLOAT_CONSTANT");
				tmpMap.put("value", operand_a.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "lfs 13," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("- not support type : " + operand_a.get("type"));

			}

			// 第二个操作数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfs 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_b.get("operand"));
				String low = getNumberLow(operand_b.get("operand"));

				// 把double常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "FLOAT_CONSTANT");
				tmpMap.put("value", operand_b.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "lfs 0," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("- not support type : " + operand_b.get("type"));

			}

			// 执行减法
			line = AssemblerUtils.PREFIX + "fsubs 0,13,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "float");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stfs 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// 整数乘法
		} else if (operator.equals("*")) {
			// 第一个操作数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfs 13,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_a.get("operand"));
				String low = getNumberLow(operand_a.get("operand"));

				// 把字符常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "FLOAT_CONSTANT");
				tmpMap.put("value", operand_a.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "lfs 13," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("* not support type : " + operand_a.get("type"));

			}

			// 第二个操作数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfs 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_b.get("operand"));
				String low = getNumberLow(operand_b.get("operand"));

				// 把double常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "FLOAT_CONSTANT");
				tmpMap.put("value", operand_b.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "lfs 0," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("* not support type : " + operand_b.get("type"));

			}

			// 执行乘法
			line = AssemblerUtils.PREFIX + "fmuls 0,13,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "float");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stfs 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// 整数除法
		} else if (operator.equals("/")) {
			// 第一个操作数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfs 13,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_a.get("operand"));
				String low = getNumberLow(operand_a.get("operand"));

				// 把字符常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "FLOAT_CONSTANT");
				tmpMap.put("value", operand_a.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "lfs 13," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("/ not support type : " + operand_a.get("type"));

			}

			// 第二个操作数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfs 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_b.get("operand"));
				String low = getNumberLow(operand_b.get("operand"));

				// 把double常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "FLOAT_CONSTANT");
				tmpMap.put("value", operand_b.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "lfs 0," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("/ not support type : " + operand_b.get("type"));

			}

			// 执行除法
			line = AssemblerUtils.PREFIX + "fdivs 0,13,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "float");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stfs 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// %运算
		} else if (operator.equals("%")) {
			throw new RuntimeException("C中的取余操作只能应用于int类型 : float");
			
		// 关系运算符
		} else if (operator.equals(">") || operator.equals(">=")) {
			// 第一个操作数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfs 13,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_a.get("operand"));
				String low = getNumberLow(operand_a.get("operand"));

				// 把字符常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "FLOAT_CONSTANT");
				tmpMap.put("value", operand_a.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "lfs 13," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("> not support type : " + operand_a.get("type"));

			}

			// 第二个操作数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfs 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_b.get("operand"));
				String low = getNumberLow(operand_b.get("operand"));

				// 把double常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "FLOAT_CONSTANT");
				tmpMap.put("value", operand_b.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "lfs 0," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("> not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "fcmpu 7,0,13";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 9,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,9,0,29";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);
			
		// 符号
		} else if (operator.equals("<") || operator.equals("<=")) {
			// 第一个操作数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfs 13,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_a.get("operand"));
				String low = getNumberLow(operand_a.get("operand"));

				// 把字符常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "FLOAT_CONSTANT");
				tmpMap.put("value", operand_a.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "lfs 13," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("< not support type : " + operand_a.get("type"));

			}

			// 第二个操作数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfs 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_b.get("operand"));
				String low = getNumberLow(operand_b.get("operand"));

				// 把double常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "float_CONSTANT");
				tmpMap.put("value", operand_b.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "lfs 0," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("< not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "fcmpu 7,0,13";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 9,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,9,0,28";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// == 符号
		} else if (operator.equals("==")) {
			try {
				throw new Exception("Error [" + label + "] : Floating-point expressions shall not be tested for equality!");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

		// != 符号
		} else if (operator.equals("!=")) {
			try {
				throw new Exception("Error [" + label + "] : Floating-point expressions shall not be tested for inequality!");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			
		} else {
			throw new RuntimeException("other operator not support in double operator : " + operator);

		}
		
	}

	public static String getNumberHigh(String string) {
		if (string == null || string.length() == 0) {
			return "0";
		}
		if (!string.contains(".")) {
			return string;
		}
		String str = string.substring(0, string.indexOf("."));
		long heigh = Long.parseLong(str);
		return Double.doubleToLongBits(heigh) + "";
	}
	
	public static String getNumberLow(String string) {
		if (string == null || string.length() == 0) {
			return "0";
		}
		if (!string.contains(".")) {
			return "0";
		}
		String str = string.substring(string.indexOf(".") + 1);
		// 去除末尾的"l"、"L"、"f"、"F"标记
		if (Character.isLetter(str.charAt(str.length() - 1))) {
			str = str.substring(0, str.length() - 1);
		}
		long low = Long.parseLong(str);
		return Double.doubleToLongBits(low) + "";
	}
	
	// 处理double型的双目运算
	private static void solveTwoOperatorDouble(Map<String, String> operand_a, Map<String, String> operand_b,
			String operator, String label) {
		String line = null;
		if (operator.equals("+")) {
			// 第一个操作数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfd 13,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_a.get("operand"));
				String low = getNumberLow(operand_a.get("operand"));
				
				// 把字符常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line,label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);						
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "DOUBLE_CONSTANT");
				tmpMap.put("value", operand_a.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);
				
				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "lfd 13," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				
			} else {
				throw new RuntimeException("+ not support type : " + operand_a.get("type"));
	
			}

			// 第二个操作数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfd 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_b.get("operand"));
				String low = getNumberLow(operand_b.get("operand"));
				
				// 把double常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line,label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);						
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "DOUBLE_CONSTANT");
				tmpMap.put("value", operand_b.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);
				
				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "lfd 0," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("+ not support type : " + operand_b.get("type")); 
		
			}

			// 执行加法
			line = AssemblerUtils.PREFIX + "fadd 0,13,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "double");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stfd 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		} else if (operator.equals("-")) {
			// 第一个操作数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfd 13,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_a.get("operand"));
				String low = getNumberLow(operand_a.get("operand"));

				// 把字符常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "DOUBLE_CONSTANT");
				tmpMap.put("value", operand_a.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "lfd 13," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("- not support type : " + operand_a.get("type"));

			}

			// 第二个操作数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfd 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_b.get("operand"));
				String low = getNumberLow(operand_b.get("operand"));

				// 把double常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "DOUBLE_CONSTANT");
				tmpMap.put("value", operand_b.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "lfd 0," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("- not support type : " + operand_b.get("type"));

			}

			// 执行减法
			line = AssemblerUtils.PREFIX + "fsub 0,13,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "double");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stfd 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// 整数乘法
		} else if (operator.equals("*")) {
			// 第一个操作数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfd 13,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_a.get("operand"));
				String low = getNumberLow(operand_a.get("operand"));

				// 把字符常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "DOUBLE_CONSTANT");
				tmpMap.put("value", operand_a.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "lfd 13," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("* not support type : " + operand_a.get("type"));

			}

			// 第二个操作数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfd 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_b.get("operand"));
				String low = getNumberLow(operand_b.get("operand"));

				// 把double常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "DOUBLE_CONSTANT");
				tmpMap.put("value", operand_b.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "lfd 0," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("* not support type : " + operand_b.get("type"));

			}

			// 执行乘法
			line = AssemblerUtils.PREFIX + "fmul 0,13,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "double");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stfd 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// 整数除法
		} else if (operator.equals("/")) {
			// 第一个操作数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfd 13,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_a.get("operand"));
				String low = getNumberLow(operand_a.get("operand"));

				// 把字符常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "DOUBLE_CONSTANT");
				tmpMap.put("value", operand_a.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "lfd 13," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("/ not support type : " + operand_a.get("type"));

			}

			// 第二个操作数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfd 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_b.get("operand"));
				String low = getNumberLow(operand_b.get("operand"));

				// 把double常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "DOUBLE_CONSTANT");
				tmpMap.put("value", operand_b.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "lfd 0," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("/ not support type : " + operand_b.get("type"));

			}

			// 执行除法
			line = AssemblerUtils.PREFIX + "fdiv 0,13,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "double");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stfd 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// %运算
		} else if (operator.equals("%")) {
			throw new RuntimeException("C中的取余操作只能应用于int类型 : double");
			
		// >关系运算符
		} else if (operator.equals(">") || operator.equals(">=")) {
			// 第一个操作数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfd 13,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_a.get("operand"));
				String low = getNumberLow(operand_a.get("operand"));

				// 把字符常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "DOUBLE_CONSTANT");
				tmpMap.put("value", operand_a.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "lfd 13," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("> not support type : " + operand_a.get("type"));

			}

			// 第二个操作数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfd 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_b.get("operand"));
				String low = getNumberLow(operand_b.get("operand"));

				// 把double常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "DOUBLE_CONSTANT");
				tmpMap.put("value", operand_b.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "lfd 0," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("> not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "fcmpu 7,0,13";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 9,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,9,0,29";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// < 符号
		} else if (operator.equals("<") || operator.equals("<=")) {
			// 第一个操作数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfd 13,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_a.get("operand"));
				String low = getNumberLow(operand_a.get("operand"));

				// 把字符常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "DOUBLE_CONSTANT");
				tmpMap.put("value", operand_a.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "lfd 13," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("< not support type : " + operand_a.get("type"));

			}

			// 第二个操作数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lfd 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				String high = getNumberHigh(operand_b.get("operand"));
				String low = getNumberLow(operand_b.get("operand"));

				// 把double常量添加到.data域
				String lc = ".LC" + assemblerDTO.getLabelCnt();
				assemblerDTO.addToLabelCnt(1);
				line = AssemblerUtils.PREFIX + ".align 3";
				assemblerDTO.insertIntoData(line, label);
				line = lc + ":";
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + high;
				assemblerDTO.insertIntoData(line, label);
				line = AssemblerUtils.PREFIX + low;
				assemblerDTO.insertIntoData(line, label);
				// 添加到符号表
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("type", "DOUBLE_CONSTANT");
				tmpMap.put("value", operand_b.get("operand"));
				assemblerDTO.putIntoSymbolTable(lc, tmpMap);

				line = AssemblerUtils.PREFIX + "lis 9," + lc + "@ha";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "lfd 0," + lc + "@l(9)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("< not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "fcmpu 7,0,13";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 9,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,9,0,28";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// == 符号
		} else if (operator.equals("==")) {			
			try {
				throw new Exception("Error [" + label + "] : Floating-point expressions shall not be tested for equality!");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

		// != 符号
		} else if (operator.equals("!=")) {
			try {
				throw new Exception("Error [" + label + "] : Floating-point expressions shall not be tested for inequality!");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			
		} else {
			throw new RuntimeException("other operator not support in double operator : " + operator);

		}
	}
	
	// 处理char型的双目运算
	private static void solveTwoOperatorChar(Map<String, String> operand_a, Map<String, String> operand_b,
			String operator, String label) {
		String line = null;
		if (operator.equals("+")) {
			// 第一个操作数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				
			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("+ not support type : " + operand_a.get("type"));
	
			}

			// 第二个操作数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				
			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("+ not support type : " + operand_b.get("type")); 
		
			}

			// 执行加法
			line = AssemblerUtils.PREFIX + "add 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "char");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 2;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);
			
			line = AssemblerUtils.PREFIX + "stb 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		} else if (operator.equals("-")) {
			// 被减数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("- not support type : " + operand_a.get("type"));

			}

			// 减数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("- not support type : " + operand_b.get("type"));

			}

			// 执行减操作
			line = AssemblerUtils.PREFIX + "subf 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "char");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 2;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stb 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// 整数乘法
		} else if (operator.equals("*")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("* not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("* not support type : " + operand_b.get("type"));

			}

			// 执行乘法指令
			line = AssemblerUtils.PREFIX + "mullw 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "char");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 2;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stb 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// 整数除法
		} else if (operator.equals("/")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("/ not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("/ not support type : " + operand_b.get("type"));

			}

			// 执行除法指令
			line = AssemblerUtils.PREFIX + "divw 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "char");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 2;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stb 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// 取余操作
		} else if (operator.equals("%")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("% not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("% not support type : " + operand_b.get("type"));

			}

			// 取余操作转化为除法、减法等指令来操作
			line = AssemblerUtils.PREFIX + "divw 11,0,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "mullw 9,11,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "subf 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "char");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 2;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stb 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// >=关系运算
		} else if (operator.equals(">=")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException(">= not support type : " + operand_a.get("type"));
	
			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException(">= not support type : " + operand_b.get("type"));

			}

			// 比较操作
			line = AssemblerUtils.PREFIX + "cmpl 7,0,0,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,0,0,28";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);
			
			// C中关系运算返回值为int
			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// >关系运算符
		} else if (operator.equals(">")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("> not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("> not support type : " + operand_b.get("type"));

			}

			// 比较指令
			line = AssemblerUtils.PREFIX + "cmpl 7,0,0,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 9,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,9,0,29";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// <=符号
		} else if (operator.equals("<=")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("<= not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("<= not support type : " + operand_b.get("type"));

			}

			// 执行比较操作
			line = AssemblerUtils.PREFIX + "cmp 7,0,0,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,0,0,29";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// < 符号
		} else if (operator.equals("<")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("< not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("< not support type : " + operand_b.get("type"));

			}

			// 比较操作
			line = AssemblerUtils.PREFIX + "cmp 7,0,0,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 9,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,9,0,28"; // 28 CR7 = CR[28,
															// 29, 30, 31]
			// (cr[crfD] : 有4位 : LT,GT,EQ,SO)
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// == 符号
		} else if (operator.equals("==")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("== not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("== not support type : " + operand_b.get("type"));

			}

			// 比较操作
			line = AssemblerUtils.PREFIX + "cmp 7,0,0,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 9,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,9,0,30";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

			// != 符号
		} else if (operator.equals("!=")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("!= not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("!= not support type : " + operand_b.get("type"));

			}

			// 比较操作
			line = AssemblerUtils.PREFIX + "xor 0,0,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "cmp 7,0,0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 9,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,9,0,30";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "xori 0,0,1";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// << 符号
		} else if (operator.equals("<<")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("<< not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("<< not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "slw 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "char");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 2;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stb 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// >> 符号
		} else if (operator.equals(">>")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException(">> not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException(">> not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "sraw 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "char");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 2;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stb 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// & 符号
		} else if (operator.equals("&")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("& not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("& not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "and 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "char");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 2;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stb 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// | 符号
		} else if (operator.equals("|")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("| not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("| not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "or 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "char");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 2;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stb 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

			// ^ 符号
		} else if (operator.equals("^")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("^ not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("^ not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "xor 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "char");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 2;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stb 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// && 符号
		} else if (operator.equals("&&")) {
			Map<String, String> labels = new HashMap<>();
			labels.put("label1", ".L" + assemblerDTO.getLabelCnt());
			assemblerDTO.addToLabelCnt(1);
			labels.put("label2", ".L" + assemblerDTO.getLabelCnt());
			assemblerDTO.addToLabelCnt(1);

			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("&& not support type : " + operand_a.get("type"));

			}

			line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "beq 7," + labels.get("label1");
			assemblerDTO.insertIntoText(line, label);

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("&& not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "beq 7," + labels.get("label1");
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "b " + labels.get("label2");
			assemblerDTO.insertIntoText(line, label);
			line = labels.get("label1") + ":";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = labels.get("label2") + ":";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 2;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stb 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// || 符号
		} else if (operator.equals("||")) {
			Map<String, String> labels = new HashMap<>();
			labels.put("label1", ".L" + assemblerDTO.getLabelCnt());
			assemblerDTO.addToLabelCnt(1);
			labels.put("label2", ".L" + assemblerDTO.getLabelCnt());
			assemblerDTO.addToLabelCnt(1);
			labels.put("label3", ".L" + assemblerDTO.getLabelCnt());
			assemblerDTO.addToLabelCnt(1);

			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 0,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("|| not support type : " + operand_a.get("type"));

			}

			line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "bne 7," + labels.get("label1");
			assemblerDTO.insertIntoText(line, label);

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lbz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));
				line = AssemblerUtils.PREFIX + "rlwinm 9,0,0,0xff";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("|| not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "beq 7," + labels.get("label2");
			assemblerDTO.insertIntoText(line, label);
			line = labels.get("label1") + ":";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "b" + labels.get("label3");
			assemblerDTO.insertIntoText(line, label);
			line = labels.get("label2") + ":";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = labels.get("label3") + ":";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		} else {
			throw new RuntimeException("other operator not support in double operator : " + operator);

		}

	}
	
	// 处理int型的双目运算符
	private static void solveTwoOperatorInt(Map<String, String> operand_a, Map<String, String> operand_b,
			String operator, String label) {
		String line = null;
		if (operator.equals("+")) {
			// 第一个操作数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("+ not support type : " + operand_a.get("type"));
	
			}

			// 第二个操作数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("+ not support type : " + operand_b.get("type")); 
		
			}

			// 执行加法
			line = AssemblerUtils.PREFIX + "add 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		} else if (operator.equals("-")) {
			// 被减数
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("- not support type : " + operand_a.get("type"));

			}

			// 减数
			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("- not support type : " + operand_b.get("type"));

			}

			// 执行减操作
			line = AssemblerUtils.PREFIX + "subf 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// 整数乘法
		} else if (operator.equals("*")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("* not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("* not support type : " + operand_b.get("type"));

			}

			// 执行乘法指令
			line = AssemblerUtils.PREFIX + "mullw 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// 整数除法
		} else if (operator.equals("/")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("/ not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("/ not support type : " + operand_b.get("type"));

			}

			// 执行除法指令
			line = AssemblerUtils.PREFIX + "divw 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// 取余操作
		} else if (operator.equals("%")) {

			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("% not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("% not support type : " + operand_b.get("type"));

			}

			// 取余操作转化为除法、减法等指令来操作
			line = AssemblerUtils.PREFIX + "divw 11,0,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "mullw 9,11,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "subf 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// >=关系运算
		} else if (operator.equals(">=")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException(">= not support type : " + operand_a.get("type"));
	
			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException(">= not support type : " + operand_b.get("type"));

			}

			// 比较操作
			line = AssemblerUtils.PREFIX + "cmp 7,0,0,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,0,0,28";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// >关系运算符
		} else if (operator.equals(">")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("> not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("> not support type : " + operand_b.get("type"));

			}

			// 比较指令
			line = AssemblerUtils.PREFIX + "cmp 7,0,0,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 9,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,9,0,29";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// <=符号
		} else if (operator.equals("<=")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("<= not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("<= not support type : " + operand_b.get("type"));

			}

			// 执行比较操作
			line = AssemblerUtils.PREFIX + "cmp 7,0,0,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,0,0,29";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// < 符号
		} else if (operator.equals("<")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("< not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("< not support type : " + operand_b.get("type"));

			}

			// 比较操作
			line = AssemblerUtils.PREFIX + "cmp 7,0,0,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 9,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,9,0,28"; // 28 CR7 = CR[28,
															// 29, 30, 31]
			// (cr[crfD] : 有4位 : LT,GT,EQ,SO)
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// == 符号
		} else if (operator.equals("==")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("== not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("== not support type : " + operand_b.get("type"));

			}

			// 比较操作
			line = AssemblerUtils.PREFIX + "cmp 7,0,0,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 9,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,9,0,30";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

			// != 符号
		} else if (operator.equals("!=")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("!= not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("!= not support type : " + operand_b.get("type"));

			}

			// 比较操作
			line = AssemblerUtils.PREFIX + "xor 0,0,9";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "cmp 7,0,0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 9,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,9,0,30";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "xori 0,0,1";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// << 符号
		} else if (operator.equals("<<")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("<< not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("<< not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "slw 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// >> 符号
		} else if (operator.equals(">>")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException(">> not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException(">> not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "sraw 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// & 符号
		} else if (operator.equals("&")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("& not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("& not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "and 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// | 符号
		} else if (operator.equals("|")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("| not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("| not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "or 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

			// ^ 符号
		} else if (operator.equals("^")) {
			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("^ not support type : " + operand_a.get("type"));

			}

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("^ not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "xor 0,9,0";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// && 符号
		} else if (operator.equals("&&")) {
			Map<String, String> labels = new HashMap<>();
			labels.put("label1", ".L" + assemblerDTO.getLabelCnt());
			assemblerDTO.addToLabelCnt(1);
			labels.put("label2", ".L" + assemblerDTO.getLabelCnt());
			assemblerDTO.addToLabelCnt(1);

			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("&& not support type : " + operand_a.get("type"));

			}

			line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "beq 7," + labels.get("label1");
			assemblerDTO.insertIntoText(line, label);

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("&& not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "beq 7," + labels.get("label1");
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "b " + labels.get("label2");
			assemblerDTO.insertIntoText(line, label);
			line = labels.get("label1") + ":";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = labels.get("label2") + ":";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		// || 符号
		} else if (operator.equals("||")) {
			Map<String, String> labels = new HashMap<>();
			labels.put("label1", ".L" + assemblerDTO.getLabelCnt());
			assemblerDTO.addToLabelCnt(1);
			labels.put("label2", ".L" + assemblerDTO.getLabelCnt());
			assemblerDTO.addToLabelCnt(1);
			labels.put("label3", ".L" + assemblerDTO.getLabelCnt());
			assemblerDTO.addToLabelCnt(1);

			if (operand_a.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 0,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_a.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else if (operand_a.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand_a.get("operand");
				assemblerDTO.insertIntoText(line, operand_a.get("label"));

			} else {
				throw new RuntimeException("|| not support type : " + operand_a.get("type"));

			}

			line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "bne 7," + labels.get("label1");
			assemblerDTO.insertIntoText(line, label);

			if (operand_b.get("type").equals("VARIABLE")) {
				line = AssemblerUtils.PREFIX + "lwz 9,"
						+ assemblerDTO.getVariableSymbolOrNumber(operand_b.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else if (operand_b.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 9," + operand_b.get("operand");
				assemblerDTO.insertIntoText(line, operand_b.get("label"));

			} else {
				throw new RuntimeException("|| not support type : " + operand_b.get("type"));

			}

			line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "beq 7," + labels.get("label2");
			assemblerDTO.insertIntoText(line, label);
			line = labels.get("label1") + ":";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "b" + labels.get("label3");
			assemblerDTO.insertIntoText(line, label);
			line = labels.get("label2") + ":";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = labels.get("label3") + ":";
			assemblerDTO.insertIntoText(line, label);

			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand_a.get("label"));
			operandStack.push(tmpMap);

		} else {
			throw new RuntimeException("other operator not support in double operator : " + operator);

		}

	}
	
	
	// 处理单目运算
	private static void solveOneOperator(String operator, String label) {
		// 取出操作数
		Map<String, String> operand = operandStack.pop();
		
		// 单目运算符只支持int、long、short型运算
		if (!getFieldType(operand).equals("int") && !getFieldType(operand).equals("long") 
				&& !getFieldType(operand).equals("short")) {
			throw new RuntimeException("single operator not support type : " + getFieldType(operand));
			
		}
		
		String line = null;
		if(operator.equals("++")) {
			line = AssemblerUtils.PREFIX + "lwz 0," + assemblerDTO.getVariableSymbolOrNumber(operand.get("operand")) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "addic 0,0,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(operand.get("operand")) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			operandStack.push(operand);
			
		} else if(operator.equals("--")) {
			line = AssemblerUtils.PREFIX + "lwz 0," + assemblerDTO.getVariableSymbolOrNumber(operand.get("operand")) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "addic 0,0,-1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(operand.get("operand")) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			operandStack.push(operand);
			
		} else if(operator.equals("!")) {
			if(operand.get("type").equals("VARIABLE")) {						
				line = AssemblerUtils.PREFIX + "lwz 0," + assemblerDTO.getVariableSymbolOrNumber(operand.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, label);
				
			} else if(operand.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand.get("operand");
				assemblerDTO.insertIntoText(line, label);
				
			} else {
				try {
					throw new Exception("! not support type : " + operand.get("type"));
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
				
			}
			
			line = AssemblerUtils.PREFIX + "cmpi 7,0,0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 0,0";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "li 9,1";
			assemblerDTO.insertIntoText(line, label);
			line = AssemblerUtils.PREFIX + "isel 0,9,0,30";
			assemblerDTO.insertIntoText(line, label);
			
			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);
			
			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand.get("label"));
			operandStack.push(tmpMap);
			
		} else if(operator.equals("~")) {
			if(operand.get("type").equals("VARIABLE")) {						
				line = AssemblerUtils.PREFIX + "lwz 0," + assemblerDTO.getVariableSymbolOrNumber(operand.get("operand")) + "(31)";
				assemblerDTO.insertIntoText(line, label);
				
			} else if(operand.get("type").equals("CONSTANT")) {
				line = AssemblerUtils.PREFIX + "li 0," + operand.get("operand");
				assemblerDTO.insertIntoText(line, label);
				
			} else {
				try {
					throw new Exception("! not support type : " + operand.get("type"));
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
				
			}
			
			line = AssemblerUtils.PREFIX + "nor 0,0,0";
			assemblerDTO.insertIntoText(line, label);
			
			// 赋值给临时操作数
			String bss_tmp = "bss_tmp" + bss_tmp_cnt;
			bss_tmp_cnt++;
			// 记录到符号表中
			Map<String, String> tmpMap = new HashMap<>();
			tmpMap.put("type", "IDENTIFIER");
			tmpMap.put("field_type", "int");
			tmpMap.put("register", Integer.toString(memAdress));
			memAdress += 4;
			assemblerDTO.putIntoSymbolTable(bss_tmp, tmpMap);

			line = AssemblerUtils.PREFIX + "stw 0," + assemblerDTO.getVariableSymbolOrNumber(bss_tmp) + "(31)";
			assemblerDTO.insertIntoText(line, label);
			// 计算结果压栈
			tmpMap = new HashMap<>();
			tmpMap.put("type", "VARIABLE");
			tmpMap.put("operand", bss_tmp);
			tmpMap.put("label", operand.get("label"));
			operandStack.push(tmpMap);
			
		} else {
			try {
				throw new Exception("not support this singleOperator : " + operator);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			
		}
	}
}
