package translator;

import java.util.ArrayList;

public class ControlBlock{
	String name;
	int indexInIR;
	int scope;
	int span;
	ArrayList<String> producers; // In Python metric code these are referred as vertices with outgoing edges outside the block
	ArrayList<String> dataNodes; // These are also referred as dEdgeHash.keys() in python code 
	boolean isPrimaryBlock;
	String[] primaryKeywords= {"if", "loop", "docase", "try","synchronize"};
	String[] secondaryKeywords = {"else", "elseif", "case","catch","finally"};
	public ControlBlock(String name, int indexInIR, int scope) {
		this.name = name;
		this.indexInIR =indexInIR;
		this.scope = scope;
		isPrimaryBlock = isPrimaryBlock();
		producers = new ArrayList<String>();
		dataNodes = new ArrayList<String>();
	}
	
	private boolean isPrimaryBlock() {
		for (String kwd: primaryKeywords) {
			if (kwd.equals(name))
				return true;
		}
		return false;
	}
	public int getScope() {
		return scope;
	}
	public void toggleBlockType() {
		isPrimaryBlock = !isPrimaryBlock;
	}
	public String toString() {
		return name + ":"+ Integer.toString(indexInIR) + "," + Integer.toString(scope)+ "," + Integer.toString(span);
	}
	public int getId() {
		return indexInIR;
	}
	public int getIndexInIR() {
		return indexInIR;
	}
	public void setSpan(int span) {
		this.span = span;
	}
	public int getSpan() {
		return span;
	}
	public void addProducer(String producer) {
		producers.add(producer);
	}
	public  ArrayList<String> getAllProducers() {
		return producers;
	}
	public boolean hasProducer(String p) {
		return producers.contains(p);
	}
	public void addDataNode(String dnode) {
		dataNodes.add(dnode);
	}
	public ArrayList<String> getDataNodes() {
		return dataNodes;
	}
}