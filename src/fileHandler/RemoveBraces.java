package fileHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.ResourcesPlugin;

public class RemoveBraces {
	String filepath ;
	public RemoveBraces(String filepath){
		this.filepath= filepath.replaceAll(".java$",".tk1");	
	}
	public void setPath(String filePath){
		this.filepath = filePath;
	}
	public void remove() throws IOException {	// This method calls Python script
		String command = "python";
		//String cwd = System.getProperty("user.dir");	//Current Working Directory
		String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
		File path = new File(workspace + "/src/python_code/brace_remover/");
		String prg = path.toString()+"/removeBraces.py";
		System.out.println(">>Calling removeBraces.py" + filepath);
		ProcessBuilder pb = new ProcessBuilder(command,prg, filepath);
		pb.directory(path);
		System.out.println("[]"+filepath);
		try {
			Process p = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			System.out.println(br.readLine());
		} catch (IOException e) {
			System.out.println("Error! RemoveBraces.java : Python script execution failed... ");
			e.printStackTrace();
		}
	}
}	


