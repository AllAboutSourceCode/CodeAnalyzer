package fileHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class PathList {
	public List<String> getPath() throws Exception {

		GetAllFiles jf = new GetAllFiles();
		String expr = ".+.java$";
		List<String> allJPath = jf.getInnerJFiles(inputPath(), expr);
		return allJPath;
		
	}

	public String inputPath() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter absolute path of the project/Java File...");
		return ( br.readLine());
	}
}
 