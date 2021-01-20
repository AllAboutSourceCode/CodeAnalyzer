package fileHandler;

import java.util.ArrayList;
import java.util.Optional;

import com.github.javaparser.Position;

public class IRWriter {
	ArrayList<String> ir;	//This is added to remove dependency of writing/reading IR to/from files.
	private int ircount;
	private int bracecount;
	public IRWriter(){
		ir = new ArrayList<String>();
		ircount =0;
	}

/*public void write(String  irstmt, Optional<Position> begin, Optional<Position> end)
 * This method writes IR of the source code into File.tk1.
 * with each IR it also appends the corresponding line number of the IR in input source code 
 * 	For example, else begin_col1_end_col2 --> this represents a typical IR statement in File.tk1.
 * 
 * Here, control statements like if, else, for are not associated with block range but with open and close braces.
 * File.tk1 is processed again to produce File.tk.
 * At present this is being written in file, otherwise it can be written in a DS and processed info can be written to the file.
 * 
 * Further, 'begin' indicates the starting line of IR 'else' in source code, and 'col1' represents its column. Similarly, 
 * 'end' represents the end of the IR in source code.
 */
	public void write(String  irstmt, Optional<Position> begin, Optional<Position> end)  {
		Position start = null;
		Position last=null;
		bracecount++;
			if (begin!=null && end!=null) {
				if (begin.isPresent())
					start = begin.get();
				if (end.isPresent()) 
					last = end.get();
				irstmt += "#" +start.line + " " +start.column + " " + last.line + " " + last.column;
			}

			
			ir.add(irstmt);//It will write IRData object
			
			if (!( irstmt.equals("{") || irstmt.equals("}"))) {
				ircount++;
			}
	
	}
	public String[] getRawIR() {
		int size = ir.size();
		String[] rawir = new String[size];
		for (int i=0;i<size;i++) {
			rawir[i] = ir.get(i);
		}
		return rawir;
	}
	public void display(){
		for (String s: ir) {
			System.out.println(s);
		}
	}
}