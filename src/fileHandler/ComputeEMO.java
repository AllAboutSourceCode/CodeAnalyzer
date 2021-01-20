package fileHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;


import analyseMethod.EMO;
import analyseMethod.MethodRange;
import analyseMethod.StatementExtractor;

public class ComputeEMO {
	CompilationUnit cu;
	String path;
	public ComputeEMO(String filepath) {
		// TODO Auto-generated constructor stub
		path = filepath;
		cu = new GetAST().getcompilationUnit();
	}
	
	

	//Create a list of methods & their beginLine : line method_name
	ArrayList<MethodRange> getMethodReferenceList() {
		// TODO Auto-generated method stub
		ArrayList<MethodRange> methodreference= new ArrayList<MethodRange>();

		MethodVisitor visitor = new MethodVisitor();
		cu.accept(visitor);

		ArrayList<org.eclipse.jdt.core.dom.MethodDeclaration> methodlist = visitor.getMethodDeclarationList();
		int size = methodlist.size();
		MethodDeclaration currentMethod = methodlist.get(0);
		for (int index = 0; index<size-1;index++) {
			MethodDeclaration nextMethod = methodlist.get(index+1);
			int begin = cu.getLineNumber(currentMethod.getStartPosition());
			int end = cu.getLineNumber(nextMethod.getStartPosition())-1;
			methodreference.add(new MethodRange(currentMethod,begin,end));
			currentMethod = nextMethod;
		}

		methodreference.add(new MethodRange(currentMethod,cu.getLineNumber(currentMethod.getStartPosition()),-1));

		return methodreference;
	}

	/*
	 * The getAllEMO() method reads *.map and *.result file and generates following information:
	 * EMOStatementList : An arrayList which contains source code line references which are part of a segment
	 * Method Name : With each segment will be attached a name of the method it belongs
	 */
	public EMO[] getAllEMO() throws BadLocationException  {
		//Initialize method names and positions
		ArrayList<String> segments = readSegments();
		Hashtable<Integer, Integer> ir2src = readIR2SrcMapFile();
		return populateEMOs(segments, ir2src);
	}

	public EMO[] getAllEMO(ArrayList<String> segments, Hashtable<Integer, Integer> ir2src) throws BadLocationException  {
		return populateEMOs(segments, ir2src);
	}

	private EMO[] populateEMOs(ArrayList<String> segments, Hashtable<Integer, Integer> ir2src)
			throws BadLocationException {
		//Compute EMOs and populate the fields of each EMO
		EMO allEMO[] = new EMO[segments.size()];	//Number of EMOs will be same as count of segments in input file.
		allEMO =  updateStatementsAndMethodDeclarationForAllEMOs(allEMO,segments,ir2src);

		/*TODO: */
		// This method causes removal of a good candidate from the list.
		//allEMO = removeEMOsWithMethodSignature(allEMO);	// we need to alter it's code so that EMOs which contain most of them method or whole method are omitted from suggestion.

		GetAST obj = new GetAST(); 
		allEMO = updatePositionsofEMOStatements(allEMO,obj);

		// METHOD BELWO CAUSES INFINITE LOOP in StatementExtractor.ComputSourceStatements() 	
		//	allEMO = updateSourceStatements(allEMO,obj);	//	It will populate the ArrayList<Statement> of source code statements for each EMO.

		return allEMO;
	}

	private Hashtable<Integer, Integer> readIR2SrcMapFile() {
		String st;
		File file; 
		BufferedReader br;
		Hashtable<Integer, Integer> ir2src = new Hashtable<Integer,Integer>();
		try {
			//Read all IR2Source_code_reference mapping from "input.map" file
			file = new File(path.replaceAll(".java$",".map")); 
			br = new BufferedReader(new FileReader(file)); 

			while ((st = br.readLine()) != null) {
				String[] map = st.split(" ");
				ir2src.put(Integer.parseInt(map[0]), Integer.parseInt(map[1]));
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ir2src;
	}

	private ArrayList<String> readSegments() {

		//Read all segments from "input.result" file	  
		ArrayList<String> segments = new ArrayList<String>();

		File file = new File(path.replaceAll(".java$",".result")); 
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String st;
			while ((st = br.readLine()) != null) {
				segments .add(st); 
			}
			br.close();	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return segments;
	}

	private EMO[] removeEMOsWithMethodSignature(EMO[] allEMO) {
		// TODO Auto-generated method stub
		EMO[] modifiedEMOs = new EMO[allEMO.length];
		int count=0;//count of emos with method signature
		int totalEMO = allEMO.length;
		// Skip EMOs which contain method signature; As they are already a method.
		for (int i=0,j=0; i<totalEMO;i++) {
			int methodStart = cu.getLineNumber(allEMO[i].getMethodDeclaration().getStartPosition());
			List<Statement> methodStatementList = new ArrayList<>();
			methodStatementList.addAll(allEMO[i].getMethodDeclaration().getBody().statements());//All the outer blocks/statements of the method.
			int firstStmtStart = cu.getLineNumber(methodStatementList.get(0).getStartPosition());
			int firstEMOElement = allEMO[i].get(0);
			/*
			 * Since, comments just preceded by the method body is also considered as 
			 * method beginning, so, we need to check the range of method beginning and
			 * beginning of first statement in the method.
			 */

			if ( !(firstEMOElement>=  methodStart && firstEMOElement <firstStmtStart))	{
				modifiedEMOs[j++] = allEMO[i];
			}
			else
				count++;
		}	

		if (count>0)	{	//Only if there was at least one emo with method signature in it.
			EMO[] finalEMOs = new EMO[allEMO.length-count];
			int index =0;
			for (EMO emo: modifiedEMOs) {
				if (emo!=null)
					finalEMOs[index++] = emo;
			}
			return finalEMOs;
		}
		else
			return allEMO;

	}

	private  EMO[] updateStatementsAndMethodDeclarationForAllEMOs(EMO[] allEMO, ArrayList<String> segments, Hashtable<Integer, Integer> ir2src) {
		// TODO Auto-generated method stub

		int emocount = 0;

		//Populate a list with method names, their begin and end line  in the source code
		ArrayList<MethodRange> methodreferences = getMethodReferenceList();	//Each method name with its position of beginning

		for(String string : segments) {
			allEMO[emocount] = new EMO();
			String[] irstmts =  string.split(" ");

			//map IR statement index to source code lines
			int src;
			src = ir2src.get(Integer.parseInt(irstmts[0]));
			allEMO[emocount].addStatement(src);
			for (int i=1;i< irstmts.length; i++) {
				src = ir2src.get(Integer.parseInt(irstmts[i]));
				if (!allEMO[emocount].contains(src)){
					allEMO[emocount].addStatement(src);
				}
			}

			//	populate MethodDeclaration For all EMOs
			int methodcount=0;
			int numberOfMethods = methodreferences.size();
			int first = allEMO[emocount].getStatements().get(0), last = allEMO[emocount].getStatements().get(allEMO[emocount].getStatements().size()-1);
			while (methodcount<numberOfMethods-1) {	// This condition is for all but the last method; that is for N-1 methods 
				if ((first>=methodreferences.get(methodcount).getMethodBeginLine() && last<=methodreferences.get(methodcount).getMethodEndLine())) {
					allEMO[emocount].setMethodDeclaration(methodreferences.get(methodcount).getMethodDeclaration());
					break;
				}
				methodcount++;
			}
			if (methodcount==numberOfMethods-1) {	//All remaining segment belongs to last method in the list.
				allEMO[emocount].setMethodDeclaration(methodreferences.get(methodcount).getMethodDeclaration());
			}
			emocount++;
		}

		return allEMO;
	}

	private EMO[] updateSourceStatements(EMO[] allEMO, GetAST obj) {	//It is using StatementExtractor
		CompilationUnit cu = obj.getcompilationUnit();
		for (EMO emo: allEMO) {	//For each emo populate ArrayList<Statement>
			MethodDeclaration methodDeclaration = emo.getMethodDeclaration();
			(new StatementExtractor()).ComputSourceStatements(emo, methodDeclaration, cu); 
		}
		return allEMO;
	}


	private EMO[] updatePositionsofEMOStatements(EMO[] allEMO, GetAST obj) throws BadLocationException {
		//GetAST obj = new GetAST();
		IFile file = obj.getIFile();
		ICompilationUnit icu = obj.getICompilationUnit();

		try {
			String source = icu.getSource();
			if (source != null) {
				Document document = new Document(source);
				IRegion region = null;
				for (EMO emo : allEMO) {
					emo.setIFile(file);//Update Ifile Info Also.
					//Now update the position
					for (Integer lineNumber: emo.getStatements()) {
						region = document.getLineInformation(lineNumber-1);
						Position position = new Position(region.getOffset(), region.getLength());
						emo.addPosition(position);
					}	
				}

			}

		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allEMO;
	}

}





