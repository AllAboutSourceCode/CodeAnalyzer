package fileHandler;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import workbenchInfo.WorkbenchInfo;

/*
 * Returns the AST of the corresponding Java file.
 */



public class GetAST {
	@SuppressWarnings("deprecation")
	public CompilationUnit getcompilationUnit() {	//Returns compilation unit for the file currently active in the editor
		// TODO Auto-generated method stub
		
        ICompilationUnit element =  getICompilationUnit();
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);
        parser.setSource(element);
        CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		return astRoot;
	}
	public CompilationUnit getcompilationUnit(IFile file) {	//Returns the compilation unit corresponding to the parameter (IFile)
		ICompilationUnit element =  JavaCore.createCompilationUnitFrom(file);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);
        parser.setSource(element);
        CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		return astRoot;
	}
	public ICompilationUnit getICompilationUnit(){
		WorkbenchInfo wb = new WorkbenchInfo();
		IFile file = wb.getIFile();
        ICompilationUnit element =  JavaCore.createCompilationUnitFrom(file);
		return element;
	}
	public ICompilationUnit getICompilationUnit(IFile file){
        ICompilationUnit element =  JavaCore.createCompilationUnitFrom(file);
		return element;
	}
	public IFile getIFile() {
		return new WorkbenchInfo().getIFile();
	}
}
