package cn.edu.buaa.prover;

import java.util.ArrayList;
import java.util.List;

import cn.edu.buaa.pojo.Item;
import cn.edu.buaa.pojo.Proposition;
import cn.edu.buaa.prover.AutomaticDerivationAlgorithm;

public class TestAutomaticDerivationAlgorithm {
	
	public static void test1() {
		
		List<Proposition> propositions = new ArrayList<>();
		
		Proposition pa = new Proposition();
		List<Item> items = new ArrayList<>();
		Item a = new Item(null, "a", "b + c");
		items.add(a);
		pa.setItems(items);
		
		Proposition pb = new Proposition();
		items = new ArrayList<>();
		Item b = new Item(null, "b", "10");
		items.add(b);
		pb.setItems(items);
		
		propositions.add(pa);
		propositions.add(pb);
		
		List<Proposition> ans = AutomaticDerivationAlgorithm.process(propositions);
		show(ans);
	}
	
	public static void test2() {
		
		List<Proposition> propositions = new ArrayList<>();
		
		Proposition pa = new Proposition();
		List<Item> items = new ArrayList<>();
		Item a = new Item(null, "GPR[0]", "0");
		items.add(a);
		pa.setItems(items);
		
		Proposition pb = new Proposition();
		items = new ArrayList<>();
		Item b = new Item("GPR[0] < 0", "GPR[0]", "b100");
		items.add(b);
		b = new Item("GPR[0] > 0", "GPR[0]", "b100");
		items.add(b);
		b = new Item("GPR[0] == 0", "GPR[0]", "b100");
		items.add(b);
		pb.setItems(items);
		
		propositions.add(pa);
		propositions.add(pb);
		
		List<Proposition> ans = AutomaticDerivationAlgorithm.process(propositions);
		show(ans);
	}
	
	public static void test3() {
		
		List<Proposition> propositions = new ArrayList<>();
		
		Proposition pa = new Proposition();
		List<Item> items = new ArrayList<>();
		Item a = new Item(null, "GPR[0]", "22");
		items.add(a);
		pa.setItems(items);
		
		Proposition pb = new Proposition();
		items = new ArrayList<>();
		Item b = new Item("GPR[0] < 0", "GPR[0]", "b100");
		items.add(b);
		b = new Item("GPR[0] > 0", "GPR[2]", "GPR[0] + haha");
		items.add(b);
		b = new Item("GPR[0] == 0", "GPR[1]", "b100");
		items.add(b);
		pb.setItems(items);
		
		propositions.add(pb);
		propositions.add(pa);
		
		List<Proposition> ans = AutomaticDerivationAlgorithm.process(propositions);
		show(ans);
	}
	
	public static void test4() {
		List<Proposition> propositions = new ArrayList<>();
		
		Proposition pa = new Proposition();
		List<Item> items = new ArrayList<>();
		Item a = new Item("GPR[0] < 0", "CR[7]", "b100");
		items.add(a);
		a = new Item("GPR[0] > 0", "CR[7]", "b010");
		items.add(a);
		a = new Item("GPR[0] == 0", "CR[7]", "b001");
		items.add(a);
		pa.setItems(items);
		
		Proposition pc = new Proposition();
		items = new ArrayList<>();
		Item c = new Item("GPR[2] < 0", "CR[7]", "b100");
		items.add(c);
		c = new Item("GPR[2] > 0", "CR[7]", "b010");
		items.add(c);
		c = new Item("GPR[2] == 0", "CR[7]", "b001");
		items.add(c);
		pc.setItems(items);
		
		
		Proposition pb = new Proposition();
		items = new ArrayList<>();
		Item b = new Item("CR[7] == b100", "PC", "PC + 4");
		items.add(b);
		b = new Item("CR[7] == b010", "PC", "PC + 4");
		items.add(b);
		b = new Item("CR[7] == b001", "PC", "PC + @.L1");
		items.add(b);
		pb.setItems(items);
		
		propositions.add(pa);
		propositions.add(pc);
		propositions.add(pb);
		
		List<Proposition> ans = AutomaticDerivationAlgorithm.process(propositions);
		show(ans);
	}
	
	public static void main(String[] args) {
		test4();
	}
	
	private static void show(List<Proposition> ans) {
		for(Proposition proposition : ans) {
			System.out.println(proposition);
		}
	}
	
}
