package fileHandler;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodVisitor extends ASTVisitor{
	ArrayList<MethodDeclaration> methodDeclarationList = new ArrayList<MethodDeclaration>();
	public boolean visit(MethodDeclaration md) {
		methodDeclarationList.add(md);
		return super.visit(md);
	}
	public ArrayList<MethodDeclaration> getMethodDeclarationList(){
		return methodDeclarationList;
		
	}
}



/*
 * 
 * CompilationUnit parse = parse(unit);
            MethodVisitor visitor = new MethodVisitor();
            parse.accept(visitor);
 * 
 * */
