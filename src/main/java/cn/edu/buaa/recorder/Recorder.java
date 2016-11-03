package cn.edu.buaa.recorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import cn.edu.buaa.constant.CommonsDefine;

public class Recorder {
	
	private BufferedWriter out;
	
	public static final String TAB = "  "; 
	
	public Recorder() {
		initDir();
		
		try {
			out = new BufferedWriter(
					new FileWriter(CommonsDefine.OUTPUT_PATH + "record.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		cleanLog();
	}
	
	private void initDir() {
		File outDir = new File(CommonsDefine.OUTPUT_PATH);
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
			
		File debugDir = new File(CommonsDefine.DEBUG_PATH);
		if (!debugDir.exists()) {
			debugDir.mkdirs();
		}
		
	}

	private void cleanLog() {
		String path = CommonsDefine.OUTPUT_PATH;
		String suffix = ".log";
		File dir = new File(path);
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.getName().endsWith(suffix)) {
					file.delete();
				}
			}
		}
		
	}

	public void writeToFile(String line) {
		try {
			if (line != null) {
				out.write(line);
			}
			out.newLine();
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeToConsole(String line) {
		if (line != null) {
			System.out.println(line);
		} else {
			System.out.println();
		}
	}
	
	public void insertLine(String line) {
		writeToFile(line);
//		writeToConsole(line);
	}
	
}
