package translator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import util.File;

/**
 * First version of IR is generated with braces. These braces represent the scope of block statements such as IF-Else, For/While etc.
 * Since, the IR we are using require scope information in terms of an integer. We need to remove braces.
 * @input a path to .java file, which is used to locate .tk1 file
 * @output Two files are generated, one is .tk which is IR and .map which contains line numbers of each IR statement in .tk.
 * @author Omkarendra Tiwari
 *
 */
public class RawIR {

	class BraceListTuple{
		int first;
		int second;
		String brace;
		String keyword;
		public BraceListTuple(int first, int second, String brace, String keyword) {
			this.first = first;
			this.second = second;
			this.brace = brace;
			this.keyword = keyword;
		}
		public String toString() {
			return "("+ Integer.toString(first) + ", " + Integer.toString(second) + ", " + brace + ", " + keyword + ")" ;	
		}
		public int getFirst() {
			return first;
		}
		public int getSecond() {
			return second;
		}
		public String getBrace() {
			return brace;
		}
		public String getKeyword() {
			return keyword;
		}
	}

	ArrayList<String> segIR;
	ArrayList<String> sourceLineNumberList;
	ArrayList<BraceListTuple> braceList;
	Hashtable<Integer,String> IR2SourceLineMap;

	public RawIR(){
		segIR = new ArrayList<String>();
		sourceLineNumberList = new ArrayList<String>();
		braceList = new ArrayList<BraceListTuple>();
		IR2SourceLineMap = new Hashtable<Integer, String>();
	}
	public void parse(String[] fileRawContent) {

		separateIRandLineNumbers(fileRawContent);
		ArrayList<Integer> blankBlocksList = computeScope_and_getBlankBlocksList(new ArrayList<BraceListTuple>(braceList)); // Pass the clone of braceList
		updateIRFinalVersion(blankBlocksList); //It would remove braces and update block socpes for blankblocks
		mapIR2SourceLineNumbers();
	}

	private void display(ArrayList<String> segIR2, String string) {
		System.out.println("Content of "+ string);
		Iterator<String> iterator = segIR2.iterator();
		while(iterator.hasNext()) {
			System.out.println(iterator.next());
		}

	}
	private void mapIR2SourceLineNumbers() {
		int invarCount =0, size = segIR.size();
		for (int index=0; index<size;index++) {
			if (segIR.get(index).equals("$")) {
				segIR.remove(index);
				segIR.add(index, "invar");
				invarCount++;
			}
			IR2SourceLineMap.put(index,sourceLineNumberList.get(index-invarCount));
		}

	}
	/*
	 * updateIRFinalVersion is implementation of Python method "PopulateIR()"
	 */
	private void updateIRFinalVersion(ArrayList<Integer> blankBlocksList) {
		ArrayList<String> SegIRwithBraces = new ArrayList<String>(segIR);
		Iterator<String> iterator =SegIRwithBraces.iterator();
		segIR.clear();

		int blankPtr = 0, blankListSize = blankBlocksList.size();

		int bracedIRPtr = 0;
		while(iterator.hasNext()) {
			String line = iterator.next();
			if (!((line.equals("}"))|| (line.equals("{")))){ // Process only if line is not a brace '{' or '}' 
				if (blankListSize>0 && blankPtr < blankListSize) {
					if ((bracedIRPtr+1)== blankBlocksList.get(blankPtr)) {//if the block is empty then, increase span by 1 & add 'invar' to its body
						segIR.add(getIRwithIncrementedScope(line)); //Increase scope by 1
						segIR.add("$");//$ is added to distinguish blank block body statement, It will be replaced by 'invar'
						blankPtr+=1;
					}
				}
				else
					segIR.add(line);
			}
			bracedIRPtr+=1;
		}
	}
	/*
	 * Increase scope of a blank-block-entry statement by one.
	 */
	private String getIRwithIncrementedScope(String line) {
		String[] tokens = line.split(" ");
		int scope = Integer.parseInt(tokens[tokens.length-1]);
		String updatedIR="";
		for (int i=0; i<tokens.length-1;i++) {
			updatedIR+=tokens[i];
		}
		updatedIR+= Integer.toString(scope+1);
		return updatedIR;
	}
	/*
	 * Method computeScope_and_getBlankBlocksList is implemented with name "AugmentIRwithBraces"
	 */
	private ArrayList<Integer> computeScope_and_getBlankBlocksList(ArrayList<BraceListTuple> cloneBraceList) {
		int i=0, doCount =0, size = cloneBraceList.size();
		ArrayList<Integer> blankBlocks = new ArrayList<Integer>();
		BraceListTuple braceTuple;
		int orig1, loc1, loc2;
		String kwd, br1, br2;
		while(size>0) {
			braceTuple = cloneBraceList.get(i);
			orig1 = braceTuple.getFirst(); loc1 = braceTuple.getSecond(); br1 = braceTuple.getBrace(); kwd = braceTuple.getKeyword();
			braceTuple = cloneBraceList.get(i+1);
			loc2 = braceTuple.getSecond(); br2 = braceTuple.getBrace(); 
			
			if (br1.equals("{") && br2.equals("}")) {
				int stmtCount = loc2-loc1-1;
				if (stmtCount ==0) { // This block does not have any statement
					blankBlocks.add(orig1);
				}
				//	check for dependent control blocks and accordingly increase the statement count of the control block

				if(kwd.equals("case")) {
					doCount +=1;	//	In this case increase the 'doCount' and process 'case' statement
				}
				else if ((kwd.equals("if") || kwd.equals("elseif")) && (i+2<cloneBraceList.size())) {
					/*
					 * In this case inspect if another dependent control block is present at the end of the
					 * present control block (e.g catch, finally, elseif or else)
					 */

					int l = cloneBraceList.get(i+2).second;
					String w = cloneBraceList.get(i+2).keyword;
					if ((l==loc2+2) && (w.equals("else") || w.equals("elseif"))) {	// Increase the span of 'kwd' by 1
						stmtCount+=1;
					}
				}
				else if ((kwd.equals("try") || kwd.equals("catch")) && (i+2<cloneBraceList.size())) {

					int l = cloneBraceList.get(i+2).second;
					String w = cloneBraceList.get(i+2).keyword;
					if ((l==loc2+2) && (w.equals("catch") || w.equals("finally"))) {	// Increase the span of 'kwd' by 1
						stmtCount+=1;
					}
				}
				else if(kwd.equals("docase")) {
					doCount = 0; // Reset:	Initialize it again
				}

				//	update range of the control block in the list; 'i' points to openning brace, so 'i-1' will point to control block keyword
				String element = segIR.get(orig1-1);
				segIR.remove(orig1-1);

				segIR.add(orig1-1,(element+ " "+ Integer.toString(stmtCount)));
				//	update parent control block's coverage by adding offset to openning brace location
				int offset = stmtCount +2;
				//	remove the current pair of the brace entry
				cloneBraceList.remove(i);	//	Mark both the entries, so that it can be removed easily
				cloneBraceList.remove(i);	//	same is the case here as above; since 'i'th element is removed so, now 'i' points to next element.
				size-=2;
				if (i >0){
					int j = i-1;
					br1 = cloneBraceList.get(j).getBrace();
					while (br1.equals("{") &&  j>=0) {	//span for all parent block's will be shrinked by the offset by adding to location of openning brace
						braceTuple=cloneBraceList.get(j);
						orig1 = braceTuple.getFirst(); loc1 = braceTuple.getSecond(); br1 = braceTuple.getBrace(); kwd = braceTuple.getKeyword();
						//(orig,loc,br,kwd) = bracelist[j] PythonCode
						braceTuple = new BraceListTuple(orig1, loc1+offset, br1, kwd);
						//cloneBraceList.insert(j, (orig,loc+offset,br,kwd)) PythonCode
						cloneBraceList.add(j,braceTuple);
						cloneBraceList.remove(j+1);
						j-=1;
					}
				}
				if (i>0){
					i=i-1;	//update value of 'i' only if i >0
				}

			}
			else {
				i+=1;
			}
		}
		return blankBlocks;
	}
	/**
	 * Input : assign x a# 15 2 17 10 <OR> {  <OR> }
	 * Output : assign x a, 15 2 17 10 <OR> {  <OR> }
	 * @param fileRawContent : A file with raw IR, each instruction in file is similar to Input shown before.
	 */
	private void separateIRandLineNumbers(String[] lines) {
		int linecount = 0;
		String prevLine="";
		for (String line:lines) {	// Process one raw IRStatement at a time

			if (line.length() ==0)
				continue; // Skip the blank lines
			String[] lineparts = line.split("#");
			if (lineparts.length>1) {	// It has lineNumber
				segIR.add(lineparts[0]);
				sourceLineNumberList.add(lineparts[1].split(" ")[0]);
			}
			else	{
				segIR.add(line);	//	It is only braces
				addBracelist(prevLine, linecount, line);
			}
			prevLine =lineparts[0];	// This would contain only IR part of the line not the '#'
			linecount +=1;
		}
	}
	private void addBracelist(String prevLine, int linecount, String line) {
		if (line.charAt(0)=='{') {
			braceList.add(new BraceListTuple(linecount, linecount,line,prevLine.split(" ")[0]));
		}
		else {
			braceList.add(new BraceListTuple(-1, linecount,line,"X"));
		}
	}
	public  ArrayList<String> getSegmentIR() {
		return segIR;
	}
	public Hashtable<Integer, String> getIRToSourceLineMap() {
		return IR2SourceLineMap;
	}
}
