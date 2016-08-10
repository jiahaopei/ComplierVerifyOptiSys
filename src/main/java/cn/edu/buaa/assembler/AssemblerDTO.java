package cn.edu.buaa.assembler;

import java.util.Map;

/**
 * Assembler数据封装成一个DTO，便于后续代码的封装
 * @author destiny
 *
 */
public class AssemblerDTO {

	// 要生成的汇编文件管理器
	private AssemblerFileHandler assFileHandler;

	// 符号表
	private Map<String, Map<String, String>> symbolTable;

	// 已经声明了多少个label
	private int labelCnt;

	// 已经使用了多少个相对地址
	private int memAdress;

	// 控制生成的汇编代码中，变量是以数字还是原始名称出现, 默认false，为原始名称出现
	private boolean isVariableSymbolOrNumber = true;
	
	/**
	 * 获得参数的原始名称或数字
	 * @param parameter
	 * @return
	 */
	public String getVariableSymbolOrNumber(String parameter) {
		return (isVariableSymbolOrNumber ? symbolTable.get(parameter).get("register") : parameter);
				
	}
	
	public void addToMemAdress(int tmp) {
		this.memAdress += tmp;
	}
	
	public void addToLabelCnt(int tmp) {
		this.labelCnt += tmp;
	}
	
	public AssemblerFileHandler getAssFileHandler() {
		return assFileHandler;
	}

	public void setAssFileHandler(AssemblerFileHandler assFileHandler) {
		this.assFileHandler = assFileHandler;
	}

	public Map<String, Map<String, String>> getSymbolTable() {
		return symbolTable;
	}

	public void setSymbolTable(Map<String, Map<String, String>> symbolTable) {
		this.symbolTable = symbolTable;
	}

	public int getLabelCnt() {
		return labelCnt;
	}

	public void setLabelCnt(int labelCnt) {
		this.labelCnt = labelCnt;
	}

	public int getMemAdress() {
		return memAdress;
	}

	public void setMemAdress(int memAdress) {
		this.memAdress = memAdress;
	}

	public boolean isVariableSymbolOrNumber() {
		return isVariableSymbolOrNumber;
	}

	public void setVariableSymbolOrNumber(boolean isVariableSymbolOrNumber) {
		this.isVariableSymbolOrNumber = isVariableSymbolOrNumber;
	}

}
