package fileHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GetAllFiles {
	
	
/*
	public static void main(String args[]) {
		String path = "/home/omkar/Drive/EclipsePhotonWorkspace/Examples/project_code/";
		String expr = ".+.java$";	//At least one character before the ".java" extension
		
	//	List<String> allJPath = getInnerJFiles(path, expr);
			
	}
*/	
		
	@SuppressWarnings("null")
	public List<String> getInnerJFiles(String projectPath, String expr){
		List<String> allJPath = null;
		
		File dir = new File(projectPath);
		if (dir.isDirectory()) {		//	is a directory so look for project root.
			allJPath = getAllPath(dir, expr);	
		}
		else		//Is a file; So add it and return
		{
			allJPath.add(projectPath);
		}
			
		return allJPath;
		
	}

	private static List<String> getAllPath(File dir, String expr) {
		// TODO Auto-generated method stub
		List<String> allJPath = new ArrayList<String>();
		Stack<File> stack = new Stack<File>();
		stack.push(dir);
		File[] files;
		while(!stack.isEmpty()) {
			dir = stack.pop();
			files = dir.listFiles();
			for (File file : files) {
				if (file.isFile() ) {
					if (file.getName().matches(expr)) 
						allJPath.add(file.getAbsolutePath());
				}
				else
					stack.push(file);	//Push all directories	
			}
		}
		return allJPath;
	}

}
