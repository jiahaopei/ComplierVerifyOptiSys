package cn.edu.buaa.templates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

public class Labeler {

	public void label1(String fileName) {

		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(new FileReader(fileName));
			String dir = fileName.substring(0, fileName.lastIndexOf("/")); 
			writer = new BufferedWriter(new FileWriter(
					dir + "/label_" + fileName.substring(fileName.lastIndexOf("/") + 1)));

			String line = null;
			int num = 1;
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() != 0) {
					line = num + "\t" + line;
				}
				num++;
				writer.write(line);
				writer.newLine();
				
				System.out.println(line);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void label2(String fileName) {
		
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(new FileReader(fileName));
			String dir = fileName.substring(0, fileName.lastIndexOf("/")); 
			writer = new BufferedWriter(new FileWriter(
					dir + "/label2_" + fileName.substring(fileName.lastIndexOf("/") + 1)));

			String line = null;
			int len = 50;
			Stack<Integer> stack = new Stack<>();
			stack.push(1);
			while ((line = reader.readLine()) != null) {

				if (line.contains("}")) {
					stack.pop();
					if (line.matches(".*[a-zA-Z0-9].*")) {
						int tmp = stack.pop();
						stack.push(tmp + 1);
					}
				}

				if (line.trim().length() != 0 && line.matches(".*[a-zA-Z0-9].*")) {
					String v = generateLabel(stack);
					line = lTrim(line);
					for (int i = line.length(); i < len; i++) {
						line += " ";
					}
					
					if (v.trim().length() != 0) {
						line = line + "// " + v;
					}
				}

				writer.write(line);
				writer.newLine();
				
				System.out.println(line);
				
				if (line.contains("{")) {
					stack.push(0);
				}

				if (line.trim().length() != 0) {
					int tmp = stack.pop();
					stack.push(tmp + 1);
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String generateLabel(Stack<Integer> stack) {
		String v = "";

		for (int i = 0; i < stack.size(); i++) {
			if (i == 0) {
				v += stack.get(i);
			} else {
				v += "." + stack.get(i);
			}
		}

		return v;
	}
	
	public void runLabeler(String fileName) {
		
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(new FileReader(fileName));
			String dir = fileName.substring(0, fileName.lastIndexOf("/")); 
			writer = new BufferedWriter(new FileWriter(
					dir + "/label_" + fileName.substring(fileName.lastIndexOf("/") + 1)));

			String line = null;
			int len = 50;
			Stack<Integer> stack = new Stack<>();
			stack.push(1);
			while ((line = reader.readLine()) != null) {

				if (line.contains("}")) {
					stack.pop();
					int tmp = stack.pop();
					stack.push(tmp + 1);
				}

				if (line.trim().length() != 0) {
					String v = generateLabel(stack);
					line = lTrim(line);
					for (int i = line.length(); i < len; i++) {
						line += " ";
					}
					
					if (v.trim().length() != 0) {
						line = line + "// " + v;
					}
				}

				writer.write(line);
				writer.newLine();
				
				System.out.println(line);
				
				if (line.contains("{")) {
					stack.push(0);
				}

				if (line.trim().length() != 0) {
					int tmp = stack.pop();
					stack.push(tmp + 1);
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String lTrim(String str) {

		int i = str.length() - 1;
		while (i >= 0) {
			if (str.charAt(i) != ' ' || str.charAt(i) != '\t' || str.charAt(i) != '\n') {
				break;
			}
		}

		return str.substring(0, i + 1);
	}

	public static void main(String[] args) {

		Labeler labeler = new Labeler();
		String fileName = "src/main/resources/source/evenSum.c";
		labeler.runLabeler(fileName);

	}

}
