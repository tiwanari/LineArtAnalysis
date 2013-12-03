package jp.narit.lineartanalysis.analyze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ラベル辞書
 * 
 * @author tatsuya
 * 
 */
public class LabelDictionary {
	
	public static final int L = 0;
	public static final int A = 1;
	public static final int Y = 2;
	public static final int T = 3;
	
	public static enum Label {
		PLUS("+"), MINUS("-"), OUTWARD("->"), INWARD("<-");
		
		private String name;
		
		private Label(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	};

	public static int getLabelValue(String label) {
		if (label.equals("L")) return L;
		else if (label.equals("A")) return A;
		else if (label.equals("Y")) return Y;
		else return T;
	}
	
	public static String getLabelString(int label) {
		switch (label) {
			case L:
				return "L";
			case A:
				return "A";
			case Y:
				return "Y";
			case T:
				return "T";
		}
		return "ERROR";
	}
	
	public static final List<Label> L1 = Arrays.asList(Label.OUTWARD, Label.INWARD);
	public static final List<Label> L2 = Arrays.asList(Label.INWARD, Label.OUTWARD);
	public static final List<Label> L3 = Arrays.asList(Label.PLUS, Label.OUTWARD);
	public static final List<Label> L4 = Arrays.asList(Label.INWARD, Label.PLUS);
	public static final List<Label> L5 = Arrays.asList(Label.MINUS, Label.INWARD);
	public static final List<Label> L6 = Arrays.asList(Label.OUTWARD, Label.MINUS);
	
	public static final List<Label> A1 = Arrays.asList(Label.INWARD, Label.PLUS, Label.OUTWARD);
	public static final List<Label> A2 = Arrays.asList(Label.PLUS, Label.MINUS, Label.PLUS);
	public static final List<Label> A3 = Arrays.asList(Label.MINUS, Label.PLUS, Label.MINUS);
	
	public static final List<Label> Y1 = Arrays.asList(Label.PLUS, Label.PLUS, Label.PLUS);
	public static final List<Label> Y2 = Arrays.asList(Label.INWARD, Label.MINUS, Label.OUTWARD);
	public static final List<Label> Y3 = Arrays.asList(Label.OUTWARD, Label.INWARD, Label.MINUS);
	public static final List<Label> Y4 = Arrays.asList(Label.MINUS, Label.OUTWARD, Label.INWARD);
	public static final List<Label> Y5 = Arrays.asList(Label.MINUS, Label.MINUS, Label.MINUS);
	
	public static final List<Label> T1 = Arrays.asList(Label.OUTWARD, Label.OUTWARD, Label.INWARD);
	public static final List<Label> T2 = Arrays.asList(Label.OUTWARD, Label.INWARD, Label.INWARD);
	public static final List<Label> T3 = Arrays.asList(Label.OUTWARD, Label.PLUS, Label.INWARD);
	public static final List<Label> T4 = Arrays.asList(Label.OUTWARD, Label.MINUS, Label.INWARD);
	
	@SuppressWarnings("serial")
	public static final ArrayList<List<Label>> LTYPES = new ArrayList<List<Label>>() {
		{
			add(LabelDictionary.L1);
			add(LabelDictionary.L2);
			add(LabelDictionary.L3);
			add(LabelDictionary.L4);
			add(LabelDictionary.L5);
			add(LabelDictionary.L6);
		}
	};
	@SuppressWarnings("serial")
	public static final ArrayList<List<Label>> ATYPES = new ArrayList<List<Label>>() {
		{
			add(LabelDictionary.A1);
			add(LabelDictionary.A2);
			add(LabelDictionary.A3);
		}
	};
	@SuppressWarnings("serial")
	public static final ArrayList<List<Label>> YTYPES = new ArrayList<List<Label>>() {
		{
			add(LabelDictionary.Y1);
			add(LabelDictionary.Y2);
			add(LabelDictionary.Y3);
			add(LabelDictionary.Y4);
		}
	};;
	@SuppressWarnings("serial")
	public static final ArrayList<List<Label>> TTYPES = new ArrayList<List<Label>>() {
		{
			add(LabelDictionary.T1);
			add(LabelDictionary.T2);
			add(LabelDictionary.T3);
			add(LabelDictionary.T4);
		}
	};;
}
