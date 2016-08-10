package cn.edu.buaa.templates;

/**
 * Hello world!
 *
 */
public class App {
	
	public int add(int x, int y) {
		return x + y;
	}
	
	public static void main(String[] args) {
		
		String string = "aaaa";
		System.out.println(string.matches(".*[a-zA-Z0-9].*"));
		
		System.out.println("ans : " + string.substring(0, 4));
		
	}
	
}
