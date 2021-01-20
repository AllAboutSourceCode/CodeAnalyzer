package analyseMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import fileHandler.IRWriter;
import parseFile.ClassElements;
import parseFile.Index;
import statement.Stmt;


public class MethodElements {
	IRWriter fout;
	public void source2IR(CompilationUnit cu, IRWriter obj) {
		System.out.println("MethodElements.java (One with 3 Arguments): ----------------");
		fout = obj;
		transformJavaClass2IR(cu);
	}

	private void transformJavaClass2IR(CompilationUnit cu) {
		//	Read each method and constructor one by one and generate IR
		ClassElements ce = new ClassElements();
		List<MethodDeclaration> methodlist = ce.getMethods(cu);

		for (MethodDeclaration method: methodlist) {
			Optional<Position> position =  method.getBegin();
			String instruction = "proc " + getMethodSignature(method);
			fout.write(instruction,method.getBegin(),method.getEnd());
			(new Index()).reset();
			parseMethod(method,fout);
		}
	}

	public void parseMethod(MethodDeclaration method, IRWriter fout2) {

		Optional<BlockStmt> block ;
		block = method.getBody();
		if (block.isPresent()) {
			List<Node> nodeList = block.get().getChildNodes();
			Stmt s = new Stmt();
			for (Node nd : nodeList) {
				s.parseStmt(nd, fout);
			}
		}
	}       
	        


	String getMethodSignature(MethodDeclaration method) {
		String instruction = method.getNameAsString() + " "; 
        String arglist = getArgsAsString(method);
        instruction+= arglist;
        return instruction;
	}
    @SuppressWarnings("unused")
	private List<String> getArgs(MethodDeclaration method) {	//Returns a list of argument names to the method
		// TODO Auto-generated method stub
    	List<String> arglist= new ArrayList<String>();
    	NodeList<Parameter> pmtrs = method.getParameters();	//Extract each argument with Type.
        for (Node p : pmtrs) {	
        	for (Node a : p.getChildNodes()) {
        			if(a instanceof SimpleName)
        				arglist.add(a.toString());
        	}
        }
        return arglist;
    }
	private String getArgsAsString(MethodDeclaration method) {	//Returns a list of argument names to the method
		// TODO Auto-generated method stub
    	
    	String arglist= "";
    	NodeList<Parameter> pmtrs = method.getParameters();	//Extract each argument with Type.
        for (Node p : pmtrs) {	
        	for (Node a : p.getChildNodes()) {
        			if(a instanceof SimpleName) 
        				arglist+=a.toString() + " ";
        	}
        }
        return arglist;
    }

}
