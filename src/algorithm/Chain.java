package algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import graph.Edge;
import translator.SegmentGraph;

public class Chain {
	SegmentGraph graph;
	String target;
	Hashtable<String,ArrayList<String>> incomingChains, outgoingChains;
	
	/*--------------------------Constructors -------------------------------*/
	
	public Chain() {

	}
	public Chain(SegmentGraph graph, String target) {
		this.graph = graph;
		this.target = target;
	}
	
	/*--------------------------- Method to call ----------------------------*/
	
	public void mergeChains(){
		incomingChains = identifyIncomingChains();
		mergeIncomingChains();
		outgoingChains = identifyOutgoingChains();
		mergeOutgoingChains();

		updateSegments(incomingChains);
		updateSegments(outgoingChains);
		System.out.println("Inside Chains () : Target :" +target);
		System.out.println("Incoming chains: " + incomingChains);
		System.out.println("Outgoing chains : " + outgoingChains);
	}
	private void updateSegments(Hashtable<String, ArrayList<String>> chains) {
		Enumeration<String> keys = chains.keys();
		while(keys.hasMoreElements()) {
			ArrayList<String> list = chains.get(keys.nextElement());
			if (list.size()>0) {
				for (String chainVertex:list) {
					graph.appendVertexToSegment(target, chainVertex);
				}
			}
		}
	}
	public void mergeChains(SegmentGraph graph,String target) {
		this.graph = graph;
		this.target = target;
		mergeChains();
	}
	
	/*---------------------Outgoing Chains Handler ---------------------------*/
	
	private void mergeOutgoingChains() {
		Enumeration keys = outgoingChains.keys();
		while(keys.hasMoreElements()) {
			ArrayList<String> chainVertices = outgoingChains.get(keys.nextElement());
			int[] list = convertToIntegerAndSortChainVertices(chainVertices);
			merge(list,"outgoing");
		}
	}
	
	private Hashtable<String, ArrayList<String>> identifyOutgoingChains() {
		Hashtable<String,ArrayList<String>> chains = computeImmediateChainSuccessors();
		Enumeration<String> keys = chains.keys();
		while (keys.hasMoreElements()) {
			String v = keys.nextElement();
			chains.get(v).addAll(getOutgoingChainVerticesBeginningFrom(v));
		}
		return chains;
	}
	private  ArrayList<String> getOutgoingChainVerticesBeginningFrom(String vid) {
		ArrayList<String> chainVertices = new ArrayList<String>();
		ArrayList<String> successors = graph.getDataSuccessors(vid);
		int v =  Integer.parseInt(vid);
		while(successors.size()==1) 
		{
			String successorVertex = successors.get(0);
			if (graph.inSameCtrlRegion(Integer.parseInt(successorVertex),v) && graph.getDataIndegree(successorVertex)==1) {
				chainVertices.add(successorVertex);
				successors = graph.getDataSuccessors(successorVertex);
			}
			else
				break;
		}
		
		return chainVertices;
	}
	private Hashtable<String, ArrayList<String>> computeImmediateChainSuccessors() {
		Hashtable<String,ArrayList<String>> chains = new Hashtable<String,ArrayList<String>>();
		for (String chainSuccessor:getChainSuccessors()) {
			ArrayList<String> list =	new ArrayList<String>();
			list.add( chainSuccessor);
			chains.put(chainSuccessor, list);
		}
		
		return chains;
	}
	private ArrayList<String> getChainSuccessors() {
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> successorList = graph.getDataSuccessors(target);
		if (successorList!=null)
			for(String s: successorList )  {
				if (graph.getDataIndegree(s)==1 && graph.inSameCtrlRegion(Integer.parseInt(target), Integer.parseInt(s)))
					list.add(s);//Add new successors to the list if they have same control parent
			}
		return list;
	}
	/*-------------------------Incoming Chains -------------------------------*/		
	
	private ArrayList<String> getIncomingChainVerticesEndingAt(String v) {
		ArrayList<String> chainVertices = new ArrayList<String>();
		ArrayList<String> preds = graph.getDataPredcessors(v);
		while(preds.size()==1) {
			String predVertex = preds.get(0);
			if ( graph.inSameCtrlRegion(Integer.parseInt(predVertex), Integer.parseInt(v)) && graph.getDataOutdegree(predVertex)==1) {
				chainVertices.add(predVertex);
				preds = graph.getDataPredcessors(predVertex);
			}
			else
				break;
		}
		
		return chainVertices;
	}

	private Hashtable<String, ArrayList<String>> computeImmediateChainPredecessors() {
		Hashtable<String,ArrayList<String>> chains = new Hashtable<String,ArrayList<String>>();
		for (String chainPred:getChainPredecessors()) {
			ArrayList<String> list =	new ArrayList<String>();
			list.add(chainPred);
			chains.put(chainPred, list);
		}
		return chains;
	}
	private ArrayList<String> getChainPredecessors() {
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> predList = graph.getDataPredcessors(target);
		if (predList!=null)
			for(String s: predList )  //Add new predecessors to the list
				if (graph.getDataOutdegree(s)==1 && graph.inSameCtrlRegion(Integer.parseInt(target), Integer.parseInt(s)))
					list.add(s);
		return list;
	}
	private void mergeIncomingChains() {
		Enumeration keys = incomingChains.keys();
		while(keys.hasMoreElements()) {
			ArrayList<String> chainVertices = incomingChains.get(keys.nextElement());
			int[] list = convertToIntegerAndSortChainVertices(chainVertices);
			merge(list,"incoming");
		}
	}
	private int[] convertToIntegerAndSortChainVertices(ArrayList<String> chainVertices) {
		int size = chainVertices.size();
		int[] list = new int[size+1];
		for (int i =0;i<size;i++) 
			list[i] = Integer.parseInt(chainVertices.get(i));
		list[size]= Integer.parseInt(target);
		Arrays.sort(list);
		return list;
	}
	private void merge(int[] list, String chainType) {
		if (chainType.equals("incoming")) {
			removeChainEdges(list);
			graph.mapIncomingEdgesFromPoorToDominant(Integer.toString(list[0]), target);
			
		}
		else if (chainType.equals("outgoing")) {
			removeChainEdges(list);
			graph.mapOutgoingEdgesFromPoorToDominant(Integer.toString(list[list.length-1]), target);
		}
		else
			System.out.println("Error! Chain.merge():" + "An unknown chaintype is provided");
	}
	private void removeChainEdges(int[] list) {
		int size = list.length;
		for(int i=size-2;i>=0;i--) {
			Edge e = graph.getEdgeNode(Integer.toString(list[i]), Integer.toString(list[i+1]));
			graph.removeEdge(e);
		}
	}
	private Hashtable<String, ArrayList<String>> identifyIncomingChains() {
		Hashtable<String,ArrayList<String>> chains = computeImmediateChainPredecessors();
		Enumeration<String> keys = chains.keys();
		while (keys.hasMoreElements()) {
			String v = keys.nextElement();
			chains.get(v).addAll(getIncomingChainVerticesEndingAt(v));
		}
		return chains;
	}

	public Hashtable<String, ArrayList<String>> getIncomingChains(){
		return incomingChains;
	}
	
	public void display() {
		Enumeration<String> keys = incomingChains.keys();
		System.out.println("\n----------------Chain---Display(): -------------\nAll IncomingChains at " + target);
		
		while (keys.hasMoreElements()) {
			String s = keys.nextElement();
			System.out.println("Chain ending at : " + s +" : "+ incomingChains.get(s));
		}
		
		System.out.println("\nAll OutgoingChains at "+ target);
		keys = outgoingChains.keys();
		while (keys.hasMoreElements()) {
			String s = keys.nextElement();
			System.out.println("Chain starting at : " + s +" : "+ outgoingChains.get(s));
		}
	}
}
