package graph;
public class Edge
 {
	private final Vertex vertexOne;
	private final Vertex vertexTwo;
	private String label;
	private String[] edgeInfo;

	public Edge(Vertex vertexOne, Vertex vertexTwo, String label, String[] eInfo)
	{
		this.vertexOne = vertexOne;
		this.vertexTwo = vertexTwo;
		this.label = label;
		this.edgeInfo = eInfo;
	}
	public Edge(Vertex vertexOne, Vertex vertexTwo, String label)
	{
		this.vertexOne = vertexOne;
		this.vertexTwo = vertexTwo;
		this.label = label;
		this.edgeInfo = null;
	}
	public Edge(Vertex vertexOne, Vertex vertexTwo)
	{
		this.vertexOne = vertexOne;
		this.vertexTwo = vertexTwo;
		this.label = "NL";
		this.edgeInfo = null;
	}	
	public String getLabel()
	{
		return this.label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public Vertex getVertexOne()
	{
		return this.vertexOne;
	}
	
	public Vertex getVertexTwo()
	{
		return this.vertexTwo;
	}

	public String[] getEdgeInfo()
        {
                return this.edgeInfo;
        }
	public void setEdgeInfo(String[] eInfo)
	{
	    this.edgeInfo = eInfo;
	}
	public String toString()
	{
		return "("+ vertexOne + " " + vertexTwo + ") " ; 
	}	

 }
