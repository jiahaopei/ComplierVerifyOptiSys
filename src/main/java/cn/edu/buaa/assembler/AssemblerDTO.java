package cn.edu.buaa.assembler;

import java.util.HashMap;
import java.util.List;
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
	
	public AssemblerDTO() {
		assFileHandler = new AssemblerFileHandler();
		symbolTable = new HashMap<>();
		
	}
	
	/**
	 * 获得参数的原始名称或数字
	 * @param parameter
	 * @return
	 */
	public String getVariableSymbolOrNumber(String parameter) {
		return (isVariableSymbolOrNumber ? symbolTable.get(parameter).get("register") : parameter);
		
	}
	
	/**
	 * add to MemAdress
	 * @param tmp
	 */
	public void addToMemAdress(int tmp) {
		this.memAdress += tmp;
	}
	
	/**
	 * add to labelCnt
	 * @param tmp
	 */
	public void addToLabelCnt(int tmp) {
		this.labelCnt += tmp;
	}
	
	/**
	 * 产生每个函数的.data和.text代码
	 */
	public void insertHeader() {
		assFileHandler.generateHeader();
	}
	
	/**
	 * 把line插入data域
	 * @param line
	 * @param label
	 */
	public void insertIntoData(String value, String label) {
		assFileHandler.insert(value, label, "DATA");
		
	}
	
	/**
	 * 把line插入text域
	 * @param line
	 * @param label
	 */
	public void insertIntoText(String value, String label) {
		assFileHandler.insert(value, label, "TEXT");
		
	}
	
	/**
	 * 插入符号表
	 * @param key
	 * @param value
	 */
	public void putIntoSymbolTable(String key, Map<String, String> value) {
		symbolTable.put(key, value);
		
	}
	
	/**
	 * 由key获得符号表中的value (Map)
	 * @param key
	 * @return
	 */
	public Map<String, String> getMapFromSymbolTable(String key) {
		if (!symbolTable.containsKey(key)) {
			throw new RuntimeException("Undefine variable : " + key);
		}
		
		return symbolTable.get(key);
	}
	
	/**
	 * 产生汇编文件
	 * @param fileName
	 */
	public void generateAssemblerFile(String fileName) {
		assFileHandler.generateAssemblerFile(fileName);
	}
	
	/**
	 * 保存符号表中的内容
	 */
	public void generateSymbolTableFile() {
		assFileHandler.generateSymbolTableFile(symbolTable);
		
	}
	
	/**
	 * 在控制台打印汇编结果
	 */
	public List<String> getResult() {
		return assFileHandler.getResult();
		
	}
	
	public List<String> getValues() {
		return assFileHandler.getValues();
	}
	
	public List<String> getLabels() {
		return assFileHandler.getLabels();
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
}
