package translator;

import java.util.ArrayList;

public class IRData {
	public static ArrayList<String> rawIR;
	public IRData(){
		rawIR = new ArrayList<String>();
	}
	public void addIR(String irstmt) {
		rawIR.add(irstmt);
		System.out.println(irstmt);
	}
	public String[] getRawIR() {
		int size = rawIR.size();
		String[] ir = new String[size];
		for (int i=0;i<size;i++) {
			ir[i] = rawIR.get(i);
		}
		return ir;
	}
	

}
