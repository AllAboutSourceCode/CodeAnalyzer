package parseFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;

import analyseMethod.MethodElements;
import fileHandler.ExtractMethod;
import fileHandler.IRWriter;
import fileHandler.RemoveBraces;
import translator.IRData;
import translator.SegmentIR;

public class Parser {
	public  CompilationUnit getCompilationUnit(String path) {	//Return the compilation unit for the java file.
		FileInputStream in = null;
		try {
			in = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		CompilationUnit cu = JavaParser.parse(in);// parse the file and returns root of AST
		return cu;
	}


	public  void parse(String path, IRWriter fout) {
		CompilationUnit cu = 	getCompilationUnit(path);
		MethodElements me = new MethodElements();
		me.source2IR(cu,fout);

	}


	public void processFile(String path,IRWriter fout) {
		// Parse the file with path 'path' and apply segmentation over it.
		System.out.println("Parser.processFile()------------------");
		parse(path,fout);
		callPythonModule(path);
	}


	private void callPythonModule(String path) {
		try {
			System.out.println("Parser.processFile(): Brace remover is being called\n");
			(new RemoveBraces(path)).remove();


			int last = path.lastIndexOf(".");
			path = path.substring(0, last);
			System.out.println("Parser.processFile(): Segmentation is being called");
			(new ExtractMethod(path)).segmentation();
			System.out.println("Parser.processFile(): Execution of python module is over\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
