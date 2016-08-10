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
		this.result.add("	.section .rodata");
		this.result.add("	.section \".text\"");
		
		this.dataPointer = 1;
		this.textPointer = 2;
		
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
					new FileWriter(CommonsDefine.OUTPUT_PATH + "assembler.txt"));
			writer.write("	.file	\"" + fileName + "\"");
			writer.newLine();
			
			for (String item : result) {
				writer.write(item);
				writer.newLine();
			}
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
			writer = new BufferedWriter(new FileWriter(CommonsDefine.OUTPUT_PATH + "symboltable.txt"));
			for(String variableName : symbolTable.keySet()) {
				Map<String, String> value = symbolTable.get(variableName);
				writer.write(variableName + " " + value.get("register"));
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
	
	// 输出result中保存的汇编代码
	public void dispalyResult() {
		for (String line : result) {
			System.out.println(line);
		}
	
	}
	
}
