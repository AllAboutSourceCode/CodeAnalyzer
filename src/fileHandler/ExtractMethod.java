package fileHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;

import codeanalyzer.handlers.RelayFlagHandler;
import translator.IRData;

public class ExtractMethod {
	String filepath ;
	public ExtractMethod(String filepath){
		this.filepath= filepath;
	}
	public void setPath(String filePath){
		this.filepath = filePath;
	}
	public void segmentation() throws IOException {	// This method calls Python script
		
		segmentationPythonVersion();
	}

	
	private void segmentationPythonVersion() {
		String command = "python";
		//String cwd = System.getProperty("user.dir");	//Current Working Directory
		String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		File path = new File(workspace + "/src/python_code/ncec/");
		String prg = path+"/main.py";
		callPythonScriptUsingProcessBuilder("python", prg,"", path);
	}
	private void callPythonScriptUsingProcessBuilder(String command, String prg, String arg1, File path) {
		// TODO Auto-generated method stub
		//File-path will be required to formatted to support the python code execution.
		
		
		List<String> commands = new ArrayList<String>();
		commands.add(command);
		commands.add(prg);
		commands.add(filepath);
		commands.add("-relay");
		commands.add((new RelayFlagHandler()).getFlag());//This value is generated in org.omkar...handlers/RelayFlagHandler.java
		ProcessBuilder pb = new ProcessBuilder(commands);
//		ProcessBuilder pb = new ProcessBuilder(command , prg, filepath);
		//pb.redirectErrorStream(true); //This will merge the output of the program with error messages.
		
		
		pb.directory(path);
		try {
			FileOutputStream fout = new FileOutputStream(new File(filepath.replaceAll(".java$", ".trace")),true);
			fout.write("Python is being called\n".getBytes());
			Process p = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			System.out.println(">>"+ br.readLine());
			fout.write(br.readLine().getBytes());
			fout.write("Execution finished \n".getBytes());
			fout.close();
			System.out.println("Execution finished " + filepath);
		} catch (IOException e) {
			System.out.println("Error! ExtractMethod.java : Python script execution failed... ");
			e.printStackTrace();
		}
	}
}
