package translator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import graph.Edge;
import graph.Graph;
import graph.Vertex;

public class SegmentGraph {
	Graph graph;

	ArrayList<ControlBlock> blockList;
	Hashtable<String,ControlBlock> blockMap;// blockIndex/Id as input and Block-object is output
	Hashtable<Integer,String> vId2SourceLineMap;
	Hashtable<String, String> ctrlParent; // All the vertices which has a control parent are present in hash
	Hashtable<String,ArrayList<String>> segments;	// contains list of segments
	ArrayList<String> extractedSegments;	// Labels of extracted EMOs/Segments
	public SegmentGraph(Graph graph){
		this.graph = graph;
		blockList = new ArrayList<ControlBlock>();
		blockMap = new Hashtable<String,ControlBlock>();
		ctrlParent = new Hashtable<String,String>();
		segments = new Hashtable<String,ArrayList<String>>();
		extractedSegments = new ArrayList<String>();

	}

	public SegmentGraph(){
		graph = new Graph();
		blockList = new ArrayList<ControlBlock>();
		blockMap = new Hashtable<String,ControlBlock>();
		ctrlParent = new Hashtable<String,String>();
		segments = new Hashtable<String,ArrayList<String>>();
		extractedSegments = new ArrayList<String>();
	}
	public void addVertex(String vId) {
		graph.addVertex(vId);
		addVertexToSegments(vId);
	}
	private void addVertexToSegments(String vId) {
		ArrayList<String> segmentElements = new ArrayList<String>();
		segmentElements.add(vId);
		segments.put(vId,segmentElements); 
	}
	public void addEdge(String vId1, String vId2, String label) {
		graph.addEdge(vId1, vId2, label);
		if (label.equals("c")) {
			ctrlParent.put(vId2, vId1);
		}
	}
	public void addBlock(ControlBlock block) {
		blockList.add(block);
		blockMap.put(Integer.toString(block.getId()),block);
	}
	public ControlBlock getBlock(String vId) {
		return blockMap.get(vId);
	}
	public boolean isSecondaryBlock(String bid) {
		return !blockMap.get(bid).isPrimaryBlock;
	}
	public ArrayList<ControlBlock> getBlocks() {
		return blockList;
	}
	public ArrayList<ControlBlock> getPrimaryBlocks() {
		ArrayList<ControlBlock> primaryBlocks = new ArrayList<ControlBlock>();
		System.out.println("translator.SegmentGraph.getPrimaryBlocks(): Entry");
		for(int i=0; i< blockList.size();i++) {
			ControlBlock block = blockList.get(i);
			if (block.isPrimaryBlock)
				primaryBlocks.add(block);
		}
		System.out.println("Identified Primary blocks are " + primaryBlocks);
		System.out.println("translator.SegmentGraph.getPrimaryBlocks(): Exit");
		return primaryBlocks;
	}

	public boolean hasControlParent(String currentBlockId) {
		 return ctrlParent.containsKey(currentBlockId);
		 

	}
	public void contractCtrlBlock(String currentBlockId) {
		Vertex source = graph.getVertexNode(currentBlockId);
		for (int vIter = Integer.parseInt(currentBlockId); vIter<graph.getNoOfVertices(); vIter++) {
			Vertex target = (Vertex) graph.getVertexNode(Integer.toString(vIter));
			if(graph.isEdgeInTheGraph(source,target,"c")) {
				graph.contractEdge(graph.getEdgeNode(source,target), source.getVertexId());
				appendVertexToSegment(source,target);
			}
		}
	}
	public void appendVertexToSegment(Vertex dominant, Vertex poor) {
		ArrayList<String> list = segments.get(dominant.getVertexId());
		if (extractedSegments.contains(poor.getVertexId())) 
			list.add(poor.getVertexId());
		else 
			list.addAll(segments.get(poor.getVertexId()));
		segments.put(dominant.getVertexId(), list);

	}
	public String getCtrlParent(String currentBlockId) {
		Vertex child = graph.getVertexNode(currentBlockId);
		Iterator<Vertex> vIter = graph.getVertexIterator();
		while(vIter.hasNext()) {
			Vertex parent = vIter.next();
			if(graph.isEdgeInTheGraph(parent,child, "c")) {
				return parent.getVertexId();
			}
		}
		return null;
	}
	public int getNumberOfVertices() {
		return graph.getNoOfVertices();
	}

	public Iterator getVertexIterator() {
		return graph.getVertexIterator();
	}
	public int[] getVertexIndexList() {
		int[] list = new int[graph.getNoOfVertices()];
		int index=0;
		Iterator vIter = graph.getVertexIterator();
		while(vIter.hasNext()) {
			list[index]  = Integer.parseInt(((Vertex) vIter.next()).getVertexId());
		}
		Arrays.sort(list);

		return list;
	}
	public boolean isCtrlBlock(String vId) {
		return blockList.contains(blockMap.get(vId));
	}
	public boolean isEdgeInTheGraph(String tail, String head, String label) {
		return graph.isEdgeInTheGraph(graph.getVertexNode(tail), graph.getVertexNode(head), label);
	}
	public int getCtrlSpan(String currentBlockId) {
		return blockMap.get(currentBlockId).getSpan();
	}
	public boolean isSource(String v) {
		int count = getOutDegCountIfSourceVertex(v);
		if (count >0)
			return true;
		return false;
	}
	public boolean isExclusiveSource(String v) {
		int count = getOutDegCountIfSourceVertex(v);
		if (count == 1)
			return true;
		return false;			
	}
	private int getOutDegCountIfSourceVertex(String v) { //Return Value>> -1: NotSource, 0:Isolated, 1:ExclusiveSource, 1+:Source
		int size = graph.getNoOfVertices(), outdeg=0;
		for (int u =0; u<size && outdeg<2; u++) {
			if(isEdgeInTheGraph(Integer.toString(u), v, "d")) {//Incoming data edge, so its not source vertex
				return -1;
			}
			if (isEdgeInTheGraph(v,Integer.toString(u), "d")) {
				outdeg++;
			}
		}
		return outdeg;
	}
	public boolean inSameCtrlRegion(int i, int v) {
		if (ctrlParent.containsKey(Integer.toString(i))&& ctrlParent.containsKey(Integer.toString(v))) {
			if (ctrlParent.get(Integer.toString(i)).equals(ctrlParent.get(Integer.toString(v)))) {
				return true;
			}
		}
		return false;
	}

	public void initAttributesOfSegmentGraph() {
		initializeCtrlSpanForAllBlocks();
		populateDataAndProducerNodesInCtrlBlocks();
		initCtrlParent();
	}
	private void populateDataAndProducerNodesInCtrlBlocks() {
		for (ControlBlock block: blockList) {
			int target = block.getIndexInIR();
			int span = block.getSpan();
			int size = graph.getNoOfVertices();
			for (int v = target; v<=span;v++) 
				if (!isCtrlBlock(Integer.toString(v))) 
					for (int i=0; i<size;i++) 
						if(isEdgeInTheGraph(Integer.toString(v), Integer.toString(i), "d")) {
							block.addDataNode(Integer.toString(v));
							if (i>span)
								block.addProducer(Integer.toString(v));
						}
		}
	}
	private void initializeCtrlSpanForAllBlocks() {
		int span, size = blockList.size();
		for (int index = size-1; index>=0; index--) {
			span = blockList.get(index).indexInIR + blockList.get(index).getScope();
			int i = index +1;
			while(i<size && blockList.get(i).getIndexInIR()<=span) {
				ControlBlock currentBlock = blockList.get(i);
				span+= currentBlock.getScope();
				i++;
			}
			blockList.get(index).setSpan(span);
		//	System.out.println("Span of " + index +" is " + span);
		}
	}
	public int getOutdegree(String vId) {
		return graph.getVertexDegree(graph.getVertexNode(vId));
	}
	public int getIndegree(String v) {
		int count=0;
		for (int u = 0; u<Integer.parseInt(v); u++) {
			if (graph.isEdgeInTheGraph(graph.getVertexNode(Integer.toString(u)), graph.getVertexNode(v)))
				count++;
		}
		return count;
	}
	public ArrayList<String> getDataPredcessors(String currentBlockId) {
		ArrayList<String> preds = graph.getDataPredecessors(currentBlockId);
		return preds;
	}
	public void initCtrlParent() {  

		Iterator vIter = graph.getVertexIterator();
		while(vIter.hasNext()) {
			Vertex v = (Vertex) vIter.next();
			ctrlParent.put(v.getVertexId(), "none");
		}

		Iterator eIter = graph.getEdgeIterator();
		while(eIter.hasNext()) {
			Edge e = (Edge) eIter.next();
			if (e.getLabel().equals("c"))
				ctrlParent.put(e.getVertexTwo().getVertexId(), e.getVertexOne().getVertexId());
		}
	}

	public ArrayList<String> getDataSuccessors(String target) {
		return graph.getDataSuccessors(target);

	}

	public int getDataIndegree(String v) {
		int count=0;
		for (int u = 0; u<Integer.parseInt(v); u++) {
			if (graph.isEdgeInTheGraph(graph.getVertexNode(Integer.toString(u)), graph.getVertexNode(v),"d"))
				count++;
		}
		return count;
	}
	public int getDataOutdegree(String v) {
		int count=0;
		for (int u = Integer.parseInt(v)+1; u<getNumberOfVertices(); u++) {
			if (graph.isEdgeInTheGraph( graph.getVertexNode(v),graph.getVertexNode(Integer.toString(u)),"d"))
				count++;
		}
		return count;
	}


	//-------------Edge Contraction related Methods ---------------------
	public Edge getEdgeNode(String vid, String successorVertex) {
		return graph.getEdgeNode(graph.getVertexNode(vid), graph.getVertexNode(successorVertex));
	}

	public void removeEdge(Edge e) {
		graph.removeEdge(e);
	}
	public void mapIncomingEdgesFromPoorToDominant(String poorVId, String dominantVId) {
		graph.mapIncomingEdgesFromPoorToDominant(poorVId, dominantVId);
	}
	public void mapOutgoingEdgesFromPoorToDominant(String poorVId, String dominantVId) {
		graph.mapOutgoingEdgesFromPoorToDominant(poorVId, dominantVId);
	}
	//------------------- Display Methods-----------------------
	public void display() {
		displayVertices();
		displayDataEdges();
		System.out.println(blockList);

	}
	public void displayDataEdges() {
		Iterator eIter = graph.getEdgeIterator();
		System.out.print("Data Edge List in the graph \n [");
		while (eIter.hasNext()) {
			Edge e = (Edge)eIter.next();
			if (e.getLabel().equals("d"))
				System.out.print(e + " ");
		}
		System.out.println("]");
	}
	public void displayControlEdges() {
		Iterator eIter = graph.getEdgeIterator();
		System.out.print("Control Edge List in the graph\n [");
		while (eIter.hasNext()) {
			Edge e = (Edge)eIter.next();
			if (e.getLabel().equals("c"))
				System.out.print(e + " ");
		}
		System.out.println("]");
	}
	private void displayVertices() {
		Iterator vIter = graph.getVertexIterator();
		System.out.println("Vertex List in the graph ");
		while (vIter.hasNext()) {
			System.out.print(vIter.next() + " ");
		}	
		
		/*---------------------------------------------------*/
	}
	
	public Hashtable<String, ArrayList<String>> getSegments(int minSize){
		Hashtable<String,ArrayList<String>> list =  new Hashtable<String,ArrayList<String>>();
		Enumeration<String> keys = segments.keys();
		while(keys.hasMoreElements()) {
			String k =  keys.nextElement();
			if (segments.get(k).size()>minSize) {
				list.put(k, segments.get(k));
			}
		}
		return list;
	}

	public void appendVertexToSegment(String target, String chainVertex) {
		appendVertexToSegment(graph.getVertexNode(target), graph.getVertexNode(chainVertex));
		
	}

	public void addExtractedSegment(String segment) {
		extractedSegments.add(segment);
	}
	public void setVId2SourceLineMap(Hashtable<Integer,String> map) {
		vId2SourceLineMap = map;
	}
	public String getSourceLine(String vId) {
		return vId2SourceLineMap.get(Integer.parseInt(vId));
	}
}

