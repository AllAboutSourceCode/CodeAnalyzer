package graph;

import java.util.*;

@SuppressWarnings("rawtypes")
class VertexIterator implements Iterator
{
	Enumeration en;
	public VertexIterator(Hashtable  ht)
	{
		en = ht.keys();
	}

	public boolean hasNext () 
	{
		return en.hasMoreElements();
	}

	public Vertex next () 
	{
		@SuppressWarnings("unchecked")
		Vertex v = (Vertex)en.nextElement();
		return v;
	}

	public void remove () 
	{
	}
}

class EdgeIterator implements Iterator
{
	private Enumeration en1;
	private Enumeration en2;
	private Hashtable<Vertex, Hashtable>  adjListHash;
	private Hashtable<Vertex, Edge> edgeTable;
	public EdgeIterator(Hashtable<Vertex, Hashtable>  ht)
	{
		adjListHash = ht;
		en1 = adjListHash.keys();
		if(en1.hasMoreElements())
		{
			Vertex v = (Vertex)en1.nextElement();
			edgeTable = adjListHash.get(v);
			en2 = edgeTable.keys();
		}
		else
		{
			en2 = null;
			edgeTable = null;
		}

	}

	public boolean hasNext () 
	{
		if(edgeTable == null || en2 == null)
		{
			return false;
		}
		else if(en2.hasMoreElements())
		{
			return true;
		}
		else 
		{
			boolean status = false;
			while(en1.hasMoreElements())
			{
				Vertex v = (Vertex)en1.nextElement();
				edgeTable = adjListHash.get(v);
				en2 = edgeTable.keys();
				if(en2.hasMoreElements())
				{
					status = true;
					break;
				}
			}
			return status; 
		}
	}

	public Edge next () 
	{
		if(en2.hasMoreElements())
		{
			Vertex v1 = (Vertex)en2.nextElement();
			return edgeTable.get(v1);
		}
		else 
		{
			return null; 
		}	
	}

	public void remove () 
	{
	}
}

public class Graph {

	Hashtable<Vertex, Hashtable>  adjListHash;
	Hashtable <String, Vertex> vertexTable;
	Hashtable <Vertex, Integer> visitedTable;
	Hashtable <Vertex, Vertex> parentTable;
	int vertexCount, edgeCount;
	private final String blockLabel="pb"; //pb stads for primary block. an edge originating from loop/if/switch/try block
	
	public Graph() {
		adjListHash = new Hashtable<Vertex, Hashtable>();
		vertexTable = new Hashtable<String, Vertex>();
	}
	
	public Graph(String graphInfo)	{
		adjListHash = new Hashtable<Vertex, Hashtable>();
		vertexTable = new Hashtable<String, Vertex>();
		initGraph(graphInfo);
	}		


	private void initGraph(String graphInfo) {
		String[] contentList = graphInfo.split("\n");
		vertexCount = Integer.parseInt(contentList[0].split(" ")[0]);
		edgeCount = Integer.parseInt(contentList[0].split(" ")[1]);
		addVertices();
		parseEdges(contentList);
	}
	private void addVertices() {
		for (int vId=0; vId<vertexCount;vId++) 
			addVertex(Integer.toString(vId));
		

	}
	private void parseEdges(String[] contentList) {
		for (int i =1; i<=edgeCount; i++) {
			int blockListPtr=0;
			String[] edgeParts = contentList[i].split(" ");
			addEdge(edgeParts[0], edgeParts[1], edgeParts[2]);
		}

	}


	/*----------Methods for Edges--------*/
	public int getNoOfEdges() 	{
		Enumeration e = adjListHash.keys();
		int m = 0;
		while(e.hasMoreElements())
		{

			Hashtable<Vertex, Hashtable> vertexTable = adjListHash.get((Vertex)e.nextElement());
			m = m + vertexTable.size();

		}
		return m;
	}

	public boolean isEdgeInTheGraph(Vertex v1, Vertex v2)
	{
		boolean status = false;
		Hashtable<Vertex, Edge> hTable = adjListHash.get(v1);
		if(hTable != null)
		{
			if(hTable.get(v2) != null)
			{
				status = true;
			}
		}
		return status;			
	}

	public boolean isEdgeInTheGraph(Vertex tail, Vertex head, String label) {

		if (isEdgeInTheGraph(tail, head)) {
			return (getEdgeNode(tail, head).getLabel().equals(label));
		}
		
		return false;
	}

	public Edge getEdgeNode(Vertex v1, Vertex v2)
	{
		Hashtable<Vertex, Edge> hTable = adjListHash.get(v1);
		if(hTable != null)
		{
			return hTable.get(v2);	
		}
		return null;
	}
	public void addEdge(Edge e)
	{
		Vertex v1, v2;
		v1 = e.getVertexOne();
		v2 = e.getVertexTwo();
		Hashtable<Vertex, Edge> hTable = adjListHash.get(v1);
		if(hTable != null)
		{
			hTable.put(v2, e);
		}
		else {
			System.out.println("Error: Graph.java (addEdge()): adjListHash is null");
		}

	}
	public void addEdge(String vId1, String vId2, String label) {
		addVertex(vId1);
		addVertex(vId2);
		Vertex v1 = getVertexNode(vId1);
		Vertex v2 = getVertexNode(vId2);
		Edge e =  new Edge(v1, v2, label);
		addEdge(e);
	}
	public void removeEdge(Edge e)
	{
		Vertex v1 = e.getVertexOne();
		Vertex v2 = e.getVertexTwo();
		Hashtable<Vertex, Edge> hTable = adjListHash.get(v1);
		if(hTable != null)
		{
			hTable.remove(v2);	
		}		    
	}
	public void contractEdge(Edge e, String dominantVId) {//This functionality is more close to Segment Graph than in Graph. or its behavior should be stated in generalized way
		removeEdge(e);
		String poorVId  =  getTobeEliminatedVertexId(e,dominantVId);
		mapIncomingEdgesFromPoorToDominant(poorVId, dominantVId);
		mapOutgoingEdgesFromPoorToDominant(poorVId,dominantVId);
	}
	public void mapOutgoingEdgesFromPoorToDominant(String poorVId, String dominantVId) {
		
		for (int vid = 0 ; vid<getNoOfVertices(); vid++) {
			Vertex v  = getVertexNode(Integer.toString(vid));
			
			String vId = v.getVertexId();
			if(vId.equals(dominantVId)) {
				continue;
			}
			Vertex poorVertex = getVertexNode(poorVId);
			Vertex dominantVertex = getVertexNode(dominantVId);
			if (isEdgeInTheGraph(poorVertex, v) && !isEdgeInTheGraph(dominantVertex, v)) {
				addEdge(dominantVId,vId,getEdgeNode(poorVertex, v).getLabel());
			//	removeEdge(getEdgeNode(poorVertex, v));
			}
			else if (isEdgeInTheGraph(poorVertex, v, "c") && isEdgeInTheGraph(dominantVertex, v,"d")) {
				addEdge(dominantVId,vId,getEdgeNode(poorVertex, v).getLabel());
			//	System.out.println("2) adding edge "+ dominantVId + "to " + vId);
			//	removeEdge(getEdgeNode(poorVertex, v));
			}
			if (isEdgeInTheGraph(poorVertex, v)){
				removeEdge(getEdgeNode(poorVertex, v));
			}
		}
	}

	public void mapIncomingEdgesFromPoorToDominant(String poorVId, String dominantVId) {

		for(int vid = 0; vid<getNoOfVertices();vid++) {
			Vertex v = getVertexNode(Integer.toString(vid));
			String vId = v.getVertexId();
			if(vId.equals(dominantVId)) {
				continue; // To avoid self loops
			}
			Vertex poorVertex = getVertexNode(poorVId);
			Vertex dominantVertex = getVertexNode(dominantVId);
			if(isEdgeInTheGraph(v, poorVertex) && !isEdgeInTheGraph(v, dominantVertex)) {
				addEdge(vId,dominantVId, getEdgeNode(v, poorVertex).getLabel());
			//	removeEdge(getEdgeNode(v,poorVertex));
			}
			else if (isEdgeInTheGraph(v,poorVertex, "c") && isEdgeInTheGraph(v, dominantVertex,"d")) {
				getEdgeNode(v,dominantVertex).setLabel("c");
			//	removeEdge(getEdgeNode(v,poorVertex));
			}
			if (isEdgeInTheGraph(v, poorVertex))
				removeEdge(getEdgeNode(v,poorVertex));
		}
	}

	private String getTobeEliminatedVertexId(Edge e, String resultingVertexId) {
		String vId1 = e.getVertexOne().getVertexId();
		String vId2 = e.getVertexTwo().getVertexId();
		if (vId1.equals(resultingVertexId)) {
			return vId2;
		}
		else
			return vId1;
	}

	public Iterator getEdgeIterator()
	{
		return new EdgeIterator(adjListHash);
	}

	/*--------Methods for Vertex-------*/
	public int getNoOfVertices()
	{
		return adjListHash.size();
	}
	public int getVertexDegree(Vertex v)
	{
		Hashtable<Vertex, Hashtable> vertexTable = adjListHash.get(v);
		return vertexTable.size();
	}
	public boolean hasVertex(String vId) {
		return vertexTable.containsKey(vId);
		
	}
	public Vertex getVertexNode(String vId)
	{
		return vertexTable.get(vId);
	}
	public void addVertex(String vId)
	{
		if (vertexTable.containsKey(vId)) {
	//		System.out.println("Graph.java: Vertex " + vId + " already exists!");
			return;
		}
		Vertex vertex = new Vertex(vId);
		vertexTable.put(vId, vertex);
		Hashtable<Vertex, Edge> vertexHash  = new Hashtable<Vertex, Edge>();
		adjListHash.put(vertex, vertexHash);		    		    
	}
	public boolean isVertexInTheGraph(Vertex v)
	{
		boolean status = false;
		Hashtable<Vertex, Hashtable> hTable = adjListHash.get(v);
		if(hTable != null)
		{
			status = true;		    	
		}
		return status;			
	}


	public void removeVertex(Vertex v)
	{
		adjListHash.remove(v);
		Enumeration en1 = adjListHash.keys();
		int i = 0;
		while(en1.hasMoreElements())
		{
			Vertex v1 = (Vertex)en1.nextElement();	
			Hashtable<Vertex, Edge> hTable = adjListHash.get(v1);
			hTable.remove(v);
		}
	}
	public Iterator getVertexIterator()
	{
		return new VertexIterator(adjListHash);
	}


	public Iterator getAdjacentNodesIter(Vertex v)
	{
		Hashtable<Vertex, Hashtable> vertexTable = adjListHash.get(v);
		return new VertexIterator(vertexTable);
	}

	/*--------------------Predecessor Functions ------------------------------*/
	public ArrayList<String> getDataPredecessors(String vId){
		ArrayList<String> predecessors = new ArrayList<String>();
		
		for (int i = 0 ; i<Integer.parseInt(vId);i++) 
			if (isEdgeInTheGraph(getVertexNode(Integer.toString(i)), getVertexNode(vId),"d")) 
				predecessors.add(Integer.toString(i));
		return predecessors;
		
	}
	
	
	
	
	
	/*------------------------------------------------------------------------*/

	public String toString()
	{
		String s = "[ {";
		Iterator vIter = getVertexIterator();
		while(vIter.hasNext()) {
			Vertex v1 = (Vertex)vIter.next();
			if(vIter.hasNext())
				s = s + v1 + ", ";
			else
				s = s + v1 + "} {";
		}
		Iterator eIter = getEdgeIterator();
		while(eIter.hasNext()) {
			Edge e = (Edge)eIter.next();
			if(eIter.hasNext())
				s = s + e + ", ";
			else
				s = s + e;
		}
		s = s + "} ]";
		return s;
	}

	public ArrayList<String> getDataSuccessors(String target) {
		ArrayList<String> list = new ArrayList<String>();
		Iterator vIter = getAdjacentNodesIter(getVertexNode(target));
		while(vIter.hasNext()) {
			Vertex v = (Vertex)vIter.next();
			if(isEdgeInTheGraph(getVertexNode(target), v, "d"))
				list.add(v.getVertexId());
		}
		return list;
	}
}
