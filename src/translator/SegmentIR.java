package translator;
import util.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;
import java.util.StringTokenizer;

public class SegmentIR {
	ArrayList<String> segIR;
	Hashtable<Integer,String> IR2SourceLineMap;
	String[] rawIR; // It would contain braces and source code line number references
	SegmentGraph segmentGraph = new SegmentGraph();
	
	String[] edgeType = {"d","c"};
	String[] irBlockPrimitives = {"if","else", "elseif", "loop", "try", "catch", "finally", "docase", "case","synchronized"};
	String[] irUsePrimitives = {"output", "return", "use"};

	Stack controlBlock = new Stack<Integer>();	// Index of a control block in IR
	Stack controlBlockScope = new Stack<Integer>(); // Scope of a control block in IR
	Hashtable<String, Integer> variableDefinition = new Hashtable<String,Integer>();
	
	public SegmentIR(String rawIRFilePath) {
		rawIR = readRawIRFile(rawIRFilePath);
		init();
	}
	
	public SegmentIR(String[] rawIR) {
		this.rawIR = rawIR;
		init();
	}
	
	private void init() {
		RawIR rawIRObject = new RawIR();
		rawIRObject.parse(rawIR);
		segIR=rawIRObject.getSegmentIR();
		IR2SourceLineMap = rawIRObject.getIRToSourceLineMap();
		segmentGraph.setVId2SourceLineMap(IR2SourceLineMap);
		parseIR();
		
	}
	private void parseIR() {
		int currentLineIndex =0;
		for (String irLine: segIR) {
			currentLineIndex++;
			if (!controlBlock.isEmpty()) {
				addBlockEdge(currentLineIndex);
			}
			segmentGraph.addVertex(Integer.toString(currentLineIndex-1));
			parseIRStatement(irLine, currentLineIndex);
		}
		
	}
	private void parseIRStatement(String irLine, int currentLineIndex) {
		String[] irParts = splitString(irLine);
		if (contains(irUsePrimitives,irParts[0])) {
			outputHandler(irParts,currentLineIndex);
		}
		else if(contains(irBlockPrimitives,irParts[0])) {
			blockHandler(irParts,currentLineIndex);
		}
		else if (irParts[0].equals("assign")) {
			assignHandler(irParts, currentLineIndex);
		}
		else if (irParts[0].equals("input")) {
			inputHandler(irParts,currentLineIndex);
		}
		else if (irParts[0].equals("call")) {
			callHandler(irParts, currentLineIndex);
		}
		else if (irParts[0].equals("rcall")) {
			rcallHandler(irParts, currentLineIndex);
		}
		else if (irParts[0].equals("proc")) {
			procHandler(irParts, currentLineIndex);
		}
	}
	private String[] splitString(String irLine) {
		StringTokenizer tk = new StringTokenizer(irLine);
		String[] tokens = new String[tk.countTokens()];
		for(int i=0; tk.hasMoreElements();i++) {
			tokens[i]=tk.nextToken();
		}
		return tokens;
	}

	private void display(String[] irParts) {
		// TODO Auto-generated method stub
		System.out.println("[");
		for (String str: irParts) {
			System.out.println(str);
		}
		System.out.println("]");
	}

	private void procHandler(String[] irParts, int currentLineIndex) {
		variableDefinition.clear();
		for (int i=2;i<irParts.length-1;i++) {	//To keep the java and python implementation same the condition is kept (irParts.length-1); should be irParts.length
			variableDefinition.put(irParts[i], currentLineIndex);
		}
	}

	private void rcallHandler(String[] irParts, int currentLineIndex) {
		addDataEdges(irParts, currentLineIndex, 3, irParts.length);
		variableDefinition.put(irParts[2], currentLineIndex);
	}

	private void callHandler(String[] irParts, int currentLineIndex) {
		addDataEdges(irParts, currentLineIndex, 2, irParts.length);
	}

	private void inputHandler(String[] irParts, int currentLineIndex) {
		for (String variable: irParts) {
			variableDefinition.put(variable, currentLineIndex);
		}
	}

	private void blockHandler(String[] irParts, int currentLineIndex) {
		segmentGraph.addBlock(new ControlBlock(irParts[0], currentLineIndex-1, Integer.parseInt(irParts[irParts.length-1])));
		controlBlock.push(currentLineIndex);
		controlBlockScope.push(Integer.parseInt(irParts[irParts.length-1]));
		addDataEdges(irParts, currentLineIndex, 1, irParts.length-1);
	}



	private void outputHandler(String[] irParts, int currentLineIndex) {
		addDataEdges(irParts,currentLineIndex, 1, irParts.length);
	}

	private void assignHandler(String[] irParts, int currentLineIndex) {
		addDataEdges(irParts, currentLineIndex, 2, irParts.length);
		variableDefinition.put(irParts[1], currentLineIndex);
	}

	private void addDataEdges(String[] irParts, int currentLineIndex,int begin, int end) {
		// TODO Auto-generated method stub
		int i;
		for (i=begin;i<end;i++) {
			if(variableDefinition.containsKey(irParts[i])) {
				int definitionLocation = (Integer)variableDefinition.get(irParts[i]);
				if (definitionLocation >0)
					segmentGraph.addEdge(Integer.toString(definitionLocation-1), Integer.toString(currentLineIndex-1), edgeType[0]);
			}
		}	
	}

	private boolean contains(String[] irUsePrimitives2, String primitive) {
		for (String element: irUsePrimitives2) {
			if (element.equals(primitive))
				return true;
		}
		return false;
	}

	private void addBlockEdge(int currentLineIndex) {
		String controlBlockId = controlBlock.peek().toString();
		segmentGraph.addEdge(Integer.toString((Integer.parseInt(controlBlockId)-1)), Integer.toString(currentLineIndex-1), edgeType[1]);
		decreaseScopeOfBlock();
	}
	private void decreaseScopeOfBlock() {
		int scopeOfBlock = (int) controlBlockScope.pop();
		if (scopeOfBlock>1) {
			controlBlockScope.push(scopeOfBlock-1);
		}
		else {
			controlBlock.pop();
		}
	}

	private String[] readRawIRFile(String rawIRFileName) {
		try {
			return new File(rawIRFileName.replaceAll(".java$", ".tk1")).read().split("\n");
		} catch (IOException e) {
			System.out.println("Error!! File name with raw IR content is : " + rawIRFileName);
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<String> getIR() {
		return segIR;
	}
	public SegmentGraph getSegmentGraph() {
		return segmentGraph;
	}
}