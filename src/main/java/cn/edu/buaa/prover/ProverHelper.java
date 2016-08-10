package cn.edu.buaa.prover;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.buaa.pojo.Item;
import cn.edu.buaa.pojo.Proposition;

public class ProverHelper {

	public static Map<String, String> generateParas(String[] lines) {
		Map<String, String> paras = new HashMap<String, String>();
		switch (lines[0]) {
		case "cmp":
			paras.put("crfD", lines[1]);
			paras.put("L", lines[2]);
			paras.put("rA", lines[3]);
			paras.put("rB", lines[4]);
			break;
		case "cmpi":
			paras.put("crfD", lines[1]);
			paras.put("L", lines[2]);
			paras.put("rA", lines[3]);
			paras.put("SIMM", lines[4]);
			break;
		case "b":
			paras.put("LI", lines[1]);
			paras.put("AA", "0");
			paras.put("LK", "0");
			break;
		case "beq":
			paras.put("crfD", lines[1]);
			paras.put("target", lines[2]);
			break;
		case "bne":
			paras.put("crfD", lines[1]);
			paras.put("target", lines[2]);
			break;
		case "li":
			paras.put("rD", lines[1]);
			paras.put("SIMM", lines[2]);
			break;
		case "lwz":
			paras.put("rD", lines[1]);
			paras.put("D", lines[2]);
			if (lines.length > 3)
				paras.put("rA", lines[3]);
			break;
		case "stw":
			paras.put("rS", lines[1]);
			paras.put("D", lines[2]);
			paras.put("rA", lines[3]);
			break;
		case "addi": // rD,rA,SIMM
		case "mulli":
			paras.put("rD", lines[1]);
			paras.put("rA", lines[2]);
			paras.put("SIMM", lines[3]);
			break;
		case "divw":
		case "mullw":
		case "subf":
		case "add":
			paras.put("rD", lines[1]);
			paras.put("rA", lines[2]);
			paras.put("rB", lines[3]);
			paras.put("OE", "0");
			paras.put("Rc", "0");
			break;
		case "isel":
			paras.put("rD", lines[1]);
			paras.put("rA", lines[2]);
			paras.put("rB", lines[3]);
			paras.put("crfD", Integer.toString(Integer.parseInt(lines[4]) / 4));
			paras.put("crb", lines[4]);
			break;
		case "xori":
		case "andi.":
		case "ori":
		case "slwi":
		case "srawi":
			paras.put("rA", lines[1]);
			paras.put("rS", lines[2]);
			paras.put("UIMM", lines[3]);
			break;
		case "xor":
		case "and":
		case "or":
		case "slw":
		case "sraw":
		case "nor":
			paras.put("rA", lines[1]);
			paras.put("rS", lines[2]);
			paras.put("rB", lines[3]);
			break;

		case "lhz":
			paras.put("rD", lines[1]);
			paras.put("D", lines[2]);
			if (lines.length > 3)
				paras.put("rA", lines[3]);
			break;
		case "rlwinm":
			paras.put("rA", lines[1]);
			paras.put("rS", lines[2]);
			paras.put("SH", lines[3]);
			paras.put("MBE", lines[4]);
			break;
		case "extsh":
			paras.put("rA", lines[1]);
			paras.put("rS", lines[2]);
			break;
		case "neg":
			paras.put("rD", lines[1]);
			paras.put("rA", lines[2]);
			break;
		case "srwi":
			paras.put("rA", lines[1]);
			paras.put("rS", lines[2]);
			paras.put("SH", lines[3]);
			break;

		case "lfs":
		case "lis":
		case "lfd":
			paras.put("frD", lines[1]);
			paras.put("D", lines[2]);
			if (lines.length > 3)
				paras.put("rA", lines[3]);
			break;
		case "fadds":
		case "fsubs":
		case "fdivs":
			paras.put("frD", lines[1]);
			paras.put("frA", lines[2]);
			paras.put("frB", lines[3]);
			break;
		case "fmuls":
			paras.put("frD", lines[1]);
			paras.put("frA", lines[2]);
			paras.put("frC", lines[3]);
			break;
		case "fcmpu":
			paras.put("crfD", lines[1]);
			paras.put("frA", lines[2]);
			paras.put("frB", lines[3]);
			break;

		case "fadd":
		case "fsub":
		case "fdiv":
			paras.put("frD", lines[1]);
			paras.put("frA", lines[2]);
			paras.put("frB", lines[3]);
			break;
		case "fmul":
			paras.put("frD", lines[1]);
			paras.put("frA", lines[2]);
			paras.put("frC", lines[3]);
			break;

		case "lbz":
			paras.put("rD", lines[1]);
			paras.put("D", lines[2]);
			if (lines.length > 3)
				paras.put("rA", lines[3]);
			break;

		case "cmpl":
			paras.put("crfD", lines[1]);
			paras.put("L", lines[2]);
			paras.put("rA", lines[3]);
			paras.put("rB", lines[4]);
			break;

		case "cmpli":
			paras.put("crfD", lines[1]);
			paras.put("L", lines[2]);
			paras.put("rA", lines[3]);
			paras.put("UIMM", lines[4]);
			break;

		default:
			try {
				throw new Exception("generateParams()中未找到指令： " + lines[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return paras;
	}

	public static void updatePropositionWithParas(String name, Proposition prop, Map<String, String> paras) {

		switch (name) {
		case "isel":
			if (paras.get("crb").equals("28")) {
				Item item = prop.getItems().get(0);
				item.setRight("GPR[rA]");
				if (paras.get("rA").equals("0")) {
					item.setRight("0");
				}

				item = prop.getItems().get(1);
				item.setRight("GPR[rB]");

				item = prop.getItems().get(2);
				item.setRight("GPR[rB]");

			} else if (paras.get("crb").equals("29")) {

				Item item = prop.getItems().get(0);
				item.setRight("GPR[rB]");

				item = prop.getItems().get(1);
				item.setRight("GPR[rA]");
				if (paras.get("rA").equals("0")) {
					item.setRight("0");
				}

				item = prop.getItems().get(2);
				item.setRight("GPR[rB]");

			} else {
				for (Item item : prop.getItems()) {
					if (item.getRight().equals("GPR[rA]") && paras.get("rA").equals("0")) {
						item.setRight("0");
					}
				}
			}

			break;
		case "addi":
			for (Item item : prop.getItems()) {
				if (item.getRight().contains("rA") && paras.get("rA").equals("0")) {
					item.setRight("0");
				}
			}

			break;
		case "rlwinm":

			for (Item item : prop.getItems()) {
				if (item.getRight().contains("SH") && paras.get("SH").equals("0")) {
					item.setRight("GPR[rS] & MBE");
				}
			}
			break;
		default:
			break;
		}

	}

	public static Proposition cloneProposition(Proposition proposition) {
		Proposition prop = new Proposition();
		List<Item> items = new ArrayList<>();

		for (Item e : proposition.getItems()) {
			Item item = new Item();
			item.setPremise(e.getPremise());
			item.setLeft(e.getLeft());
			item.setRight(e.getRight());
			items.add(item);
		}
		prop.setItems(items);

		return prop;
	}
	
	public static void saveAllProposition(List<Proposition> propositions, BufferedWriter bufferedWriter) throws IOException {
		for (Proposition proposition : propositions) {
			bufferedWriter.write(proposition.toString());
		}
		bufferedWriter.newLine();
	}
	
	public static void reducePropositionOfThree(Proposition proposition) {
		
		Item a = proposition.getItems().get(0);
		Item b = proposition.getItems().get(1);
		Item c = proposition.getItems().get(2);
		
		if (a.getRight() != null && b.getRight() != null && c.getRight() != null
				&& a.getRight().equals(b.getRight()) && a.getRight().equals(c.getRight())) {
			a.setPremise(null);
			proposition.getItems().remove(b);
			proposition.getItems().remove(c);
		} else if (a.getRight() != null && b.getRight() != null 
				&& a.getRight().equals(b.getRight())) {
			if (a.getPremise().contains(" < ") && b.getPremise().contains(" > ")) {
				a.setPremise(a.getPremise().replace(" < ", " != "));
				proposition.getItems().remove(b);
			} else if (a.getPremise().contains(" < ") && b.getPremise().contains(" == ")) {
				a.setPremise(a.getPremise().replace(" < ", " <= "));
				proposition.getItems().remove(b);
			} else if (a.getPremise().contains(" > ") && b.getPremise().contains(" < ")) {
				a.setPremise(a.getPremise().replace(" > ", " != "));
				proposition.getItems().remove(b);
			} else if (a.getPremise().contains(" > ") && b.getPremise().contains(" == ")) {
				a.setPremise(a.getPremise().replace(" > ", " >= "));
				proposition.getItems().remove(b);
			} else if (a.getPremise().contains(" == ") && b.getPremise().contains(" > ")) {
				a.setPremise(a.getPremise().replace(" == ", " >= "));
				proposition.getItems().remove(b);
			} else if (a.getPremise().contains(" == ") && b.getPremise().contains(" < ")) {
				a.setPremise(a.getPremise().replace(" == ", " <= "));
				proposition.getItems().remove(b);
			}
		} else if (a.getRight() != null && c.getRight() != null
				&& a.getRight().equals(c.getRight())) {
			if (a.getPremise().contains(" < ") && c.getPremise().contains(" > ")) {
				a.setPremise(a.getPremise().replace(" < ", " != "));
				proposition.getItems().remove(c);
			} else if (a.getPremise().contains(" < ") && c.getPremise().contains(" == ")) {
				a.setPremise(a.getPremise().replace(" < ", " <= "));
				proposition.getItems().remove(c);
			} else if (a.getPremise().contains(" > ") && c.getPremise().contains(" < ")) {
				a.setPremise(a.getPremise().replace(" > ", " != "));
				proposition.getItems().remove(c);
			} else if (a.getPremise().contains(" > ") && c.getPremise().contains(" == ")) {
				a.setPremise(a.getPremise().replace(" > ", " >= "));
				proposition.getItems().remove(c);
			} else if (a.getPremise().contains(" == ") && c.getPremise().contains(" > ")) {
				a.setPremise(a.getPremise().replace(" == ", " >= "));
				proposition.getItems().remove(c);
			} else if (a.getPremise().contains(" == ") && c.getPremise().contains(" < ")) {
				a.setPremise(a.getPremise().replace(" == ", " <= "));
				proposition.getItems().remove(c);
			}
		} else if (b.getRight() != null && c.getRight() != null
				&& b.getRight().equals(c.getRight())) {
			if (b.getPremise().contains(" < ") && c.getPremise().contains(" > ")) {
				b.setPremise(b.getPremise().replace(" < ", " != "));
				proposition.getItems().remove(c);
			} else if (b.getPremise().contains(" < ") && c.getPremise().contains(" == ")) {
				b.setPremise(b.getPremise().replace(" < ", " <= "));
				proposition.getItems().remove(c);
			} else if (b.getPremise().contains(" > ") && c.getPremise().contains(" < ")) {
				b.setPremise(b.getPremise().replace(" > ", " != "));
				proposition.getItems().remove(c);
			} else if (b.getPremise().contains(" > ") && c.getPremise().contains(" == ")) {
				b.setPremise(b.getPremise().replace(" > ", " >= "));
				proposition.getItems().remove(c);
			} else if (b.getPremise().contains(" == ") && c.getPremise().contains(" > ")) {
				b.setPremise(b.getPremise().replace(" == ", " >= "));
				proposition.getItems().remove(c);
			} else if (b.getPremise().contains(" == ") && c.getPremise().contains(" < ")) {
				b.setPremise(b.getPremise().replace(" == ", " <= "));
				proposition.getItems().remove(c);
			}
		}
		
	}

}
