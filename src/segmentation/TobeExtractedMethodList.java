package segmentation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.jface.text.BadLocationException;

import algorithm.Flag;
import algorithm.Segmentation;
import analyseMethod.EMO;
import codeanalyzer.handlers.RelayFlagHandler;
import fileHandler.ComputeEMO;
import fileHandler.ExtractMethod;
import fileHandler.IRWriter;
import parseFile.Parser;
import translator.IRData;
import translator.SegmentGraph;
import translator.SegmentIR;

public class TobeExtractedMethodList {
	boolean debug;
	private String filepath;
	public TobeExtractedMethodList(String path){
		filepath = path;
		
	}
	public EMO[] getEMOList() throws IOException, BadLocationException {
		debug= true;	//It allows print statements to print values to help in debug and flow of information
		System.out.println("TobeExtractedMethodList.getEMOList(): File to be processed is "+ filepath +"\n");
		IRWriter fout = new IRWriter();
		(new Parser()).parse(filepath,fout);
		return applyExtractMethodRefactoring(fout);
	}


	public EMO[] applyExtractMethodRefactoring(IRWriter fout) throws BadLocationException {
		SegmentGraph graph = getSegmentGraph(fout);
		Segmentation algoHandler = new Segmentation();
		algoHandler.run(graph, getFlag());
		if (debug)
			System.out.println("------Segments---------\n"+algoHandler.getSegments()+"-------End--------");

		ComputeEMO emo = new ComputeEMO(filepath);
		EMO [] allemo = emo.getAllEMO(getFormattedSegments(graph),getFormattedIR2Src(graph));
		return allemo;
	}
	private Hashtable<Integer,Integer> getFormattedIR2Src(SegmentGraph graph) {
		Hashtable<Integer,Integer> ir2Src = new Hashtable<Integer,Integer>();
		for (int v =0; v<graph.getNumberOfVertices();v++) {
			ir2Src.put(v, Integer.parseInt(graph.getSourceLine(Integer.toString(v))));
		}
		if (debug)
			System.out.println("----------IR->SRC---------\n"+ir2Src + "--------End--------");
		return ir2Src;
	}
	private ArrayList<String> getFormattedSegments(SegmentGraph graph) {
		ArrayList<String> segments = new ArrayList<String>(); 
		Hashtable<String, ArrayList<String>> segList = graph.getSegments(1);
		System.out.println("getFormattedSegments():SegList: "+segList);
		Enumeration<String> keys = segList.keys();
		while(keys.hasMoreElements()) {
			ArrayList<String> segs = segList.get(keys.nextElement());
			int size = segs.size();
			int[] segSorted = new int[size];
			for(int i=0;i<size;i++) {
				segSorted[i]=Integer.parseInt(segs.get(i));
			}
			Arrays.sort(segSorted);
			String str = "";
			for (int i =0; i<size-1;i++) {
				str+=segSorted[i]+" ";
			}
			str+=segSorted[size-1];
			segments.add(str);
		}
		
		return segments;
	}
	private SegmentGraph getSegmentGraph(IRWriter fout) {
		String[] bracedIR = fout.getRawIR();
		
		if (debug)
			fout.display();
		
		SegmentIR ir = new SegmentIR(bracedIR);
		SegmentGraph graph = ir.getSegmentGraph();
		return graph;
	}
	private Flag getFlag() {
		float locs = 0.41f, parentAffinity = 0.34f;
		boolean relay = new RelayFlagHandler().getFlagBoolean();
		Flag flag = new Flag(locs,parentAffinity,relay);
		return flag;
	}
}
