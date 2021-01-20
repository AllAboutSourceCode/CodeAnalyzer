package util;

import java.io.FileReader;
import java.io.IOException;

public class File {
	String filePath;
	String content;
	public File(String path) {
		filePath = path;
		content = null;
	}
	
	public String read() throws IOException {
		FileReader reader = new FileReader(filePath);
		content = getContent(reader);
		reader.close();
		return content; 
	}
	public void write(String fileContent) {
		
	}

	
	private String getContent(FileReader reader)  {
		int letter;
		String fileContent="";
		try {
			while((letter = reader.read())!= -1) {
				fileContent += (char)letter;
			}
		} catch (IOException e) {
			System.out.println("File Reading Error: File.getContent()");
			e.printStackTrace();
		}
		return fileContent;
	}
	/**
	 * The path is of format x/y/z.java,
	 * now we would separate two components of path and produce 'x/y/' and 'z'	
	 */
	
	
}
