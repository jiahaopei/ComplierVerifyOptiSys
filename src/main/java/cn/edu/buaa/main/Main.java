package cn.edu.buaa.main;

import cn.edu.buaa.assembler.Assembler;
import cn.edu.buaa.lexer.Lexer;
import cn.edu.buaa.parser.Parser;
import cn.edu.buaa.prover.Prover;
import cn.edu.buaa.recorder.Recorder;

/**
 * 程序运行入口
 * @author destiny
 *
 */
public class Main {
	
	public static void main(String[] args) {
		// 公共记录
		Recorder recorder = new Recorder();
		String fileName = "evenSum.c";

		Lexer lexer = new Lexer(fileName, recorder);
		lexer.outputSrc();
		lexer.labelSrc(fileName);
		lexer.outputLabelSrc(fileName);
		lexer.runLexer();
		lexer.outputLexer();

		Parser parser = new Parser(lexer.getTokens(), recorder);
		parser.runParser();
		parser.outputParser();

		Prover prover = new Prover(recorder, fileName);
		Assembler assembler = new Assembler(parser.getTree(), recorder, prover);
		assembler.runAssembler();
		assembler.generateAssemblerFile(fileName);
		assembler.generateSymbolTableFile();
		assembler.outputAssembler();
		
	}
}
