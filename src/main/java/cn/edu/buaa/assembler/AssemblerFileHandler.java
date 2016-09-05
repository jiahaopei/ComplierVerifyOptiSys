package cn.edu.buaa.assembler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.edu.buaa.constant.CommonsDefine;

/**
 * 保存汇编文件
 * @author destiny
 *
 */
public class AssemblerFileHandler {
	
	private List<String> result;
	private int dataPointer;		// .data域
	private int textPointer;		// .text域
	
	public AssemblerFileHandler() {
		this.result = new ArrayList<>();	
		this.dataPointer = 0;
		this.textPointer = 0;
		
	}
	
	public List<String> getResult() {
		return result;
	}
	
	public void generateHeader() {
		this.result.add("");
		this.result.add("	.section .rodata");
		// 加一行空格，分开数据域和代码域
		this.result.add("");
		this.result.add("	.section \".text\"");
		
		this.dataPointer = textPointer + 2;
		this.textPointer = dataPointer + 2;
	}
	
	// 插入一行生成的汇编代码
	public void insert(String value, String type) {
		// 插入到data域
		if(type.equals("DATA")) {
			result.add(dataPointer, value);
			dataPointer++;
			textPointer++;
		
		// 插入到代码段
		} else if (type.equals("TEXT")) {
			result.add(textPointer, value);
			textPointer++;
			
		} else {
			try {
				throw new Exception("Error insert type : " + type);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
	}

	// 将结果保存到文件中
	public void generateAssemblerFile(String fileName) {
		BufferedWriter writer = null; 
		try {
			writer = new BufferedWriter(
					new FileWriter(
							CommonsDefine.OUTPUT_PATH + fileName.substring(0, fileName.lastIndexOf(".")) + ".s"));
			writer.write("	.file	\"" + fileName + "\"");
			writer.newLine();
			
			for (String item : result) {
				writer.write(item);
				writer.newLine();
			}
			writer.newLine();
			writer.write("	.ident	\"powerpc-e500v2-linux-gnuspe-gcc\"");
			writer.newLine();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
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
	
	// 将符号表中的内容保存到文件
	public void generateSymbolTableFile(Map<String, Map<String, String>> symbolTable) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(CommonsDefine.DEBUG_PATH + "symboltable.txt"));
			writer.write("变量名称\t" + "变量类型\t" + "内存地址");
			writer.newLine();
			for(String variableName : symbolTable.keySet()) {
				Map<String, String> value = symbolTable.get(variableName);
				writer.write(variableName + "\t" + value.get("field_type") + "\t" + value.get("register"));
				writer.newLine();
			}
			writer.close();
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
	
}
