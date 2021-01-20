package analyseMethod;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.text.Position;
/**
 * An EMO consists of source code statements (or their line numbers) from the input source code method.
 */
public class EMO {
	ArrayList<Integer> EMOstatements;	//StatementsLineNumbers
	ArrayList<Position> EMOpositions;
	IFile file;
	MethodDeclaration methodDeclaration;
	ArrayList<Statement> sourceStatements;
	int size;
	public EMO(){
		EMOstatements = new ArrayList<Integer>();
		EMOpositions = new ArrayList<Position>();
		sourceStatements = new ArrayList<Statement>();
		size = 0;
		file = null;
	}
	//getter and setter for native method name
/*
	public void setNativeMethod(String methodName) {
		nativeMethod = methodName;
	}
*/
	public String getNativeMethod(){
		return methodDeclaration.getName().toString();
	}
	
	
	public void setMethodDeclaration(MethodDeclaration md) {
		methodDeclaration = md;
	}
	public MethodDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}
	//getter and setter EMOstatements
	public void addStatement(int statement) {	//Add an element to the list
		EMOstatements.add(statement);
		updateSize();
	}
	public int get(int index) {	//Returns element at a specific position
		return EMOstatements.get(index);
	}
	public ArrayList<Integer> getStatements() {//Returns whole list
		return EMOstatements;
	}
	//getter and setter EMOpositions
	public void addPosition(Position position) {	//Add an element to the list
		EMOpositions.add(position);
	}
	public Position getPositionAt(int index) {	//Returns element at a specific position
		return EMOpositions.get(index);
	}
	public ArrayList<Position> getPositions() {//Returns whole list
		return EMOpositions;
	}

	//	getter and setter for sourceStatement ArrayList. It consists of Statement/ASTNodes corresponding to the EMO
	public void addSourceStatements(Statement sourceStatement) {
		this.sourceStatements.add(sourceStatement); 
	}
	public ArrayList<Statement> getSourceCodeStatements(){
		return sourceStatements;
	}
	
	
	//getter and setter of size
	private void updateSize() {
		size++;
	}
	public int getSize() {
		return size;
	}
	//getter and setter of 'IFile file'
	public void setIFile(IFile file) {
		this.file = file;
	}
	public IFile getIFile() {
		return file;
	}
	
	public boolean contains(int statement) {
		// TODO Auto-generated method stub
		return EMOstatements.contains(statement);
	}
	public String toString() {
		return "( "+ getNativeMethod() +" " + EMOstatements.toString()+ " )";
	}

}
