package analyseMethod;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

public class MethodRange {
	MethodDeclaration methodDeclaration;
	int methodBeginLine;
	int methodEndLine;
	public MethodRange(MethodDeclaration md) {
		methodDeclaration = md;
	}
	
	public MethodRange(MethodDeclaration methodDeclaration, int begin, int end) {
		this.methodDeclaration = methodDeclaration;
		methodBeginLine =  begin;
		methodEndLine = end;
	}
	public void setMethodBeginLine(int begin) {
		methodBeginLine = begin;
	}
	public int getMethodBeginLine() {
		return methodBeginLine;
	}
	public int getMethodEndLine() {
		return methodEndLine;
	}
	public SimpleName getName() {
		return methodDeclaration.getName();
	}
	public MethodDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}
	public String toString() {
		String methodInfo=null;
		
		methodInfo = getName().toString() + "begins at " + getMethodBeginLine();
		return methodInfo;
	}
}
