package parseFile;

/*
 * Since, no variable will be used uninitialized, hence we can remove the part of the code which results in 'assign var'
 * for declaration of the variable in source code.
 * 	Modify the IRGenerator so that the first occurrence of a  variable is the assignment of the variable.
 * 
 * 
 */


import fileHandler.PathList;
import java.util.List;

//Sample path		: /home/omkar/Drive/EclipsePhotonWorkspace/Examples/project_code/
//					/home/omkar/Drive/EclipsePhotonWorkspace/Examples/XDataGrading
//					/home/omkar/Drive/EclipsePhotonWorkspace/Examples/XDataGrading/src/database
//					/home/omkar/Drive/EclipsePhotonWorkspace/Examples/dummy
//					/home/omkar/Drive/EclipsePhotonWorkspace/Examples/EqSQL_Gen2-master
//					/home/omkar/Drive/EclipsePhotonWorkspace/Examples/junit3.8
//					/home/omkar/Drive/EclipsePhotonWorkspace/Examples/JHotDraw5.2
//					/home/omkar/Drive/EclipsePhotonWorkspace/Examples/MyWebMarket
//					/home/omkar/Drive/EclipsePhotonWorkspace/Examples/dummy/Cube.java

public class Test {

    public static void main(String[] args) throws Exception {
            boolean parseProject = false;	//True if whole project has to be parsed or one java file only
            PathList fp = new PathList();
            Parser p = new Parser();
            if (parseProject) {	//Whole project has to be parsed
                // Ask the user to input the Java Project/File to be parsed
                List<String> strList= fp.getPath();
	            for (String path: strList ) {
		          //  	p.processFile(path);
		            }
            }
            else {	//Only one java file will be parsed
            	String path = fp.inputPath();
//            	p.processFile(path);
            }
        }
 


        
        
}
