package parseFile;
/*
 * This class is responsible for extracting the elements of the class. which are fields, methods, constructors and sub class etc.
 * 
 */

import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import analyseMethod.MethodElements;

public class ClassElements {	
	public String getClassName(CompilationUnit cu) {
		return null;
	}
	public List<MethodDeclaration>  getMethods(CompilationUnit cu){
		return cu.findAll(MethodDeclaration.class);
		
	}
	public List<ConstructorDeclaration>  getConstructors(CompilationUnit cu){
		return cu.findAll(ConstructorDeclaration.class);
	}
	
	public void  displayConstructors(CompilationUnit cu){
		List<ConstructorDeclaration> constructorlist = cu.findAll(ConstructorDeclaration.class);
		System.out.println("List of constructors are here");
		for (ConstructorDeclaration constructor : constructorlist) {
			System.out.println(constructor.getNameAsString());
		}
	}
	public void displayMethods(CompilationUnit cu) {
		List<MethodDeclaration> methodlist = cu.findAll(MethodDeclaration.class);
		System.out.println("List of Methods are here");
		for (MethodDeclaration method : methodlist) {
			System.out.println(method.getNameAsString());
	}
	}
	
}
