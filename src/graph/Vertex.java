package graph;

public class Vertex
 {
	private final String vertexId;
	private String[] vertexInfo;
	
	public Vertex()
	{
		this.vertexId = null;
		this.vertexInfo = null;
	}	

	public Vertex(String vId, String[] vInfo)
	{
		this.vertexId = vId;
		this.vertexInfo = vInfo;
	}
	public  Vertex(String vId)
	{
		this.vertexId = vId;
		this.vertexInfo = null;
	}
	public String[] getVertextInfo()
	{
		return this.vertexInfo;
	}
	public String getVertexId()
	{ 
		return this.vertexId;
	}
	
	public void setVertexInfo(String[] vInfo)
	{
		this.vertexInfo = vInfo;	
	}
	public String toString()
	{
		return this.vertexId+""; 
	}
 }



