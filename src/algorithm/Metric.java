package algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle.Control;
import java.util.Set;
import java.util.Stack;

import graph.Graph;
import translator.ControlBlock;
import translator.SegmentGraph;

public class Metric {

	public boolean qualifiedForExtraction(String currentBlockId, SegmentGraph graph, Flag flag) {//IT is Java implementation of MeasureAffinity() of Python code
		Set dEdge = new HashSet<String>();  // Name of this variable is dEdgeHash in corresponding Python code
		ArrayList<String> producers = new ArrayList<String>(); // Name of this variable is producerHash in corresponding Python code
		int span = graph.getCtrlSpan(currentBlockId);
		int[] vlist = graph.getVertexIndexList();
		int vCount = vlist.length;
		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (int v=Integer.parseInt(currentBlockId);v<=span;v++) {
			if(graph.isCtrlBlock(Integer.toString(v)))
				continue;
			for (int i =v+1;i<vCount;i++) 
				if (graph.isEdgeInTheGraph(Integer.toString(v), Integer.toString(i), "d")) {
					dEdge.add(Integer.toString(v));
					if (i>span)
						producers.add(Integer.toString(v));
				}
		}
		
		System.out.println("At block: " + currentBlockId+ "Data Nodes ="+ dEdge + "Producers: " + producers);
		int beginLocation=0;
		String pid = graph.getCtrlParent(currentBlockId);
		if (pid!=null) 
			beginLocation = Integer.parseInt(pid);
		for(int i = beginLocation; i<Integer.parseInt(currentBlockId);i++ ) {
			if (ExclusiveDataSupplerToTheBlock(i,currentBlockId,span,graph)) {
				//System.out.println("Exclusive node at block " + currentBlockId + ":" + i);
				dEdge.add(Integer.toString(i));
			}
		}
		System.out.println("Data nodes including exclusive Sources :" +dEdge);
		
		
		int accuDCount = getAccumulativeDataNodeCount(currentBlockId,graph, dEdge,producers);
		
		Set producerSet = new HashSet<String>();
		//int pCount = producers.size();
		producerSet.addAll(producers);
		int pCount = producerSet.size();
		System.out.println("Producerset : " + producerSet);
		if (flag.noRelayExtract) {
			if (pCount == 0 && accuDCount>0) {
				float affinity = (float) (1.0/accuDCount);
				return getAffinityFlag(affinity,currentBlockId, flag.locs);
			}
			else if (accuDCount == 0 ) //	pCount == 0 or accuDCount == 0 was the condition earlier ; modified on Oct 30th, 2018
				return false;
			else {							
				float affinity = (float)pCount/accuDCount; 
				System.out.println("pCount and accuDCount : " + pCount +", " + accuDCount);
				return getAffinityFlag(affinity,currentBlockId, flag.locs);
			}
		}
		else {
			if (pCount == 0 || accuDCount == 0)	
				return false;
			else {							
				float affinity = (float)pCount/accuDCount; 
				System.out.println("pCount and accuDCount : " + pCount +", " + accuDCount);
				return getAffinityFlag(affinity,currentBlockId,flag.locs);	
			}			
		}
	}
	private boolean getAffinityFlag(float affinity, String currentBlockId,float locs) {
		System.out.println("algorithm.Metric.getAffinityFlag():----- Affinity of Block "+ currentBlockId + " is "+ affinity);
		if (affinity<locs)
			return true;
		else
			return false;
	}
	private int getAccumulativeDataNodeCount(String currentBlockId, SegmentGraph graph, Set dEdge, ArrayList<String> producers) {
		System.out.println("Data Nodes : " + dEdge);
		
		graph.displayDataEdges();
		int count = 0;
		Stack<String> stack=new Stack<String>();
		Set<String> dataSuppliers = new HashSet<String>();
		ArrayList<String> tmplist = new ArrayList<String>();

		for(String p:producers) {
			stack.add(p);
			while(!stack.isEmpty()) {
				String v = stack.pop();
				for (Object o: dEdge.toArray()) {
					String u = (String) o;
					if(graph.isEdgeInTheGraph(u, v, "d")) {
						stack.add(u);
						tmplist.add(u);
						dataSuppliers.add(u);
					}
				}

			}
			count+=tmplist.size();
		}
		Set<String> allNodes = new HashSet<String>();
		allNodes.addAll(dEdge);
		Set<String> nonDataSuppliers = new HashSet<String>();
		nonDataSuppliers.addAll(allNodes);
		nonDataSuppliers.removeAll(dataSuppliers);
		count = count + nonDataSuppliers.size();
		
		System.out.println("Tmplist: " + tmplist);
		System.out.println("Data Suppliers : " + dataSuppliers);
		System.out.println("NonData Supplier : " + nonDataSuppliers );
		
		return count;
	}
	private boolean ExclusiveDataSupplerToTheBlock(int i, String currentBlockId, int span, SegmentGraph graph) {
		if ( graph.isSource(Integer.toString(i)) && graph.inSameCtrlRegion(i,Integer.parseInt(currentBlockId))) {
			//System.out.println(i + "is source and now checking if its exclusive at block " + currentBlockId);
			int count =0;
			for (int j=Integer.parseInt(currentBlockId); j<=span;j++) {
				if (graph.isEdgeInTheGraph(Integer.toString(i), Integer.toString(j), "d")) {
					count++;
				}
			}
			if (count == graph.getOutdegree(Integer.toString(i)))
				return true;
		}
		
		return false;
	}
	//--------------------------------------------Parent Affinity----------------------------------------------------//
	public static boolean parentsAffinity(String currentBlockId, String parent, String sid, SegmentGraph graph, ArrayList<String> segParent, Flag flag) {
		System.out.println("In Parents Affinity....");
		if(Integer.parseInt(currentBlockId)<0 || (parent!=null &&Integer.parseInt(parent)<0)||graph.isSecondaryBlock(currentBlockId)||(parent!= null && graph.isSecondaryBlock(parent))|| (parent!=null && segParent.contains(parent)))
			return false;
		else
			return getParentsAffinity(currentBlockId,parent,sid,graph, flag);
	}
	private static boolean getParentsAffinity(String currentBlockId, String parent, String sid, SegmentGraph graph, Flag flag) {
		int distinctDEdgeCount = getDistinctParentDEdgeCount(sid, parent,graph);	//Data vertices which are not listed to be merged with inner block
		int dependentCount = findDataDependenceCount(sid, parent, graph);	//	count of total dEdges which directly data-connect (supply or consume) with inner control block
		float InDependentCount, ratio;
		int ParentDEdgeDiff = 1; // This was a flag in Python code.
		System.out.println("--distinctDEdgeCount"+distinctDEdgeCount);
		if (distinctDEdgeCount>ParentDEdgeDiff) {	
			int distinctProducer = getDistinctProducerCount(sid, parent, graph);	// THIS IS MODIFIED... INSTEAD OF 'target' --> 'sid' is used
			InDependentCount = (float)(distinctDEdgeCount - dependentCount); // (= all the vertices which do not result in a value used by inner block)

			if (distinctProducer >0){
				if (InDependentCount < 0) { // Either none or few statements in parent consume the producer; So, leave the parent
					//System.out.println("Warning/Error: have to handle this case;" + "Dependent Edge count"+ str(dependentCount)+"\n");
					return false;	//That is, no need to extract this parent with inner control block
				}
				else if ( InDependentCount >0) {
					ratio = InDependentCount/distinctDEdgeCount;
					System.out.println("2>"+ratio);
					return isParentExtractable(ratio, parent, flag);
				}
				else { // All the statements in parent consume the producer and in return produce a data
					System.out.println("3> true");
					return true;
				}
			}
			else { // CASE of dEdge >0 and distinctProducer = 0
				ratio = InDependentCount/distinctDEdgeCount;
				return isParentExtractable(ratio, parent,flag);
			}
		}
		else {		
			System.out.println("jgjhgjgjgjgjgjgjgjg");
			return true;
		}

	}
	private static boolean isParentExtractable(float ratio, String parent, Flag flag) {
		System.out.println("()()parent affinity computed : " + ratio);
		if (ratio<flag.pa)
			return true;
		else
			return false;	//That is no need to check further;
	}
	private static int getDistinctProducerCount(String sid, String parent, SegmentGraph graph) {
		int count  = 0;	//	distinct Producer count
		for (String p: graph.getBlock(parent).getAllProducers()) {
			if (!graph.getBlock(sid).hasProducer(p))
				count++;
		}
		return count;

	}
	private static int findDataDependenceCount(String sid, String parent, SegmentGraph graph) {
		int count = 0;
		count = getIncomingDataDependents(sid, parent, graph);
		count += getOutgoingDataDependents(sid,parent,graph);
		return count;	//	Count of all data connect elements
	}
	private static int getOutgoingDataDependents(String sid, String parent, SegmentGraph graph) {
		int count =0;
		int span = graph.getBlock(sid).getSpan();
		int pspan = graph.getBlock(parent).getSpan() ;
		for (int i = Integer.parseInt(sid); i<=span;i++) {
			for (int v = span+1; v<=pspan;v++) {
				if (graph.isEdgeInTheGraph(Integer.toString(i), Integer.toString(v), "d"))
					count++;
			}
		}
		return count;
	}
	private static int getIncomingDataDependents(String sid, String parent, SegmentGraph graph) {
		int span = graph.getBlock(sid).getSpan();
		int count = 0;
		for (int v = Integer.parseInt(sid); v<Integer.parseInt(parent);v--) {
			for (int i =Integer.parseInt(sid); i<span; i++) {
				if(graph.isEdgeInTheGraph(Integer.toString(v), Integer.toString(i), "d")){
					count++;
				}
			}
		}
		return count;
	}
	private static int getDistinctParentDEdgeCount(String sid, String parent, SegmentGraph graph) {
		//After merging inner block, all vertices with outdegree>0, are the candidate for this.
		System.out.println("Metric.getDistinctParentDEdgeCount(): sid="+ sid+ " and parent= " + parent);
		ControlBlock parentBlock = graph.getBlock(parent);
		int count=0;
		for (String v : parentBlock.getDataNodes()) {
			int in = graph.getIndegree(v);
			int out = graph.getOutdegree(v);
			if (in+out>0)
				count++;
		}
		return count;
	}

}
