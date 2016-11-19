package cn.edu.buaa.templates;

public class Test {
	
	
	public static void main(String[] args) {
		double a = 1.2;
		double b = 1.3;
		double c = 1;
		
		float d = 1.2f;
		
		System.out.println(Double.toHexString(a));
		System.out.println(Double.toString(a));
		System.out.println(Double.doubleToLongBits(a));
		System.out.println(Double.doubleToRawLongBits(a));
		System.out.println();
		
		System.out.println(Double.doubleToLongBits(1.2));
		System.out.println(Double.doubleToLongBits(1));
		System.out.println(Double.doubleToLongBits(2));
		System.out.println(Double.doubleToLongBits(0.2));
		
		System.out.println();
		System.out.println(Float.intBitsToFloat(1072902963));
		System.out.println(Float.intBitsToFloat(858993459));
		
		System.out.println(b + " " + c + " " + d);
		
		
	}
	
}
