package algorithm;

import java.util.ArrayList;
import java.util.Hashtable;

import translator.ControlBlock;
import translator.SegmentGraph;

public class Segmentation {
	SegmentGraph graph;
	int blockPointer;
	ArrayList<String> segParent;
	Flag flag;

	public void run(SegmentGraph graph, Flag flag) {
		this.graph = graph;
		this.flag=flag;
		segParent = new ArrayList<String>();
		String[] blockIds  = getBlockIds(graph.getPrimaryBlocks());
		graph.initAttributesOfSegmentGraph();
		System.out.println("Calling markExtractableEMOs : ");
		graph.displayDataEdges();
		markExtractableEMOs(blockIds);
	}

	private void markExtractableEMOs(String[] blockIds) {
		 blockPointer = blockIds.length-1;
		 System.out.println("Now, marking blocks by analysing in reverse order of appearance:");
		while (blockPointer>=0) {
			System.out.println("\nNext block to process is : " + blockIds[blockPointer] + "at index " + blockPointer);
			String currentBlockId = blockIds[blockPointer];
			//graph.displayDataEdges();
			Metric mt = new Metric();
			System.out.println("\n\ntranslator.Segmentation.MarkExtractableEMOs()----------Processing block : " + currentBlockId+"-----------");
			System.out.println("Parent of the block :"+graph.getCtrlParent(currentBlockId)+"\n");
			if (mt.qualifiedForExtraction(currentBlockId, graph,flag)) {
				System.out.println("Block " + currentBlockId + "is qualified for extraction" );
				applyNestedExtraction(currentBlockId,blockIds);
				graph.addExtractedSegment(blockIds[blockPointer]);
			}
			else {
				System.out.println("Block " + currentBlockId + " is not qualified for extraction");
				System.out.println("Hasparent:" + graph.hasControlParent(currentBlockId));
				if(!graph.hasControlParent(currentBlockId)) {
					System.out.print("Block " + currentBlockId +" has no parent hence being contracted and ");
					graph.contractCtrlBlock(currentBlockId);
					System.out.println("it is successfully contracted.");
					
				}
			}
			blockPointer--;
		}
	}



	public void applyNestedExtraction(String currentBlockId, String[] blockIds) {
		contractBlock(currentBlockId);
		System.out.println("Back from contractBlock to applyNestedExtraction with Id" + currentBlockId + "\n calling extract()");
		int extractedEMOId = extract(currentBlockId,blockIds, flag);
		System.out.println("Back from extract to applyNestedExtraction & Now calling markParent with extractedEMOId" + extractedEMOId);
		markParent(extractedEMOId);
		System.out.println("Exiting applyNestedExtraction():");
	}

	private void markParent(int extractedEMOId) {
		String pid = graph.getCtrlParent(Integer.toString(extractedEMOId));
		while(pid!=null) {
			segParent.add(pid);
			pid = graph.getCtrlParent(pid);
		}
	}

	/*
	 * currentBlockId : A block which is qualified for extraction.
	 * Return : Sid, currentBlockId or a predecessor parent Id which is selected for extraction with inner block with Id-currentBlockId
	 */
	private int extract(String currentBlockId, String[] blockIds, Flag flag) {
		String sid = currentBlockId;
		String parent =  graph.getCtrlParent(currentBlockId);
		System.out.println("In Segmentation.extract(): BlockId " + currentBlockId + " and parent : " + parent);
		while(parent!=null && !segParent.contains(parent) && Metric.parentsAffinity(currentBlockId,parent,sid,graph,segParent, flag)) {
			System.out.println("ParentsAffinity is qualified for parent " + parent);
			blockPointer++;
			contractBlock(parent);
			sid = parent;
			parent = graph.getCtrlParent(parent);
		}
		if(parent!=null && graph.isSecondaryBlock(parent)) {
			int result = checkSecondaryParentsFunctionalCoherence(currentBlockId,sid,parent, flag);
			if (result>0) {
				sid = Integer.toString(result);
				contractBlock(sid);
			}
		}
		
		return Integer.parseInt(sid);
	}

	private int checkSecondaryParentsFunctionalCoherence(String currentBlockId, String sid, String parent, Flag flag) {
		String gpid = graph.getCtrlParent(parent);
		if ( segParent.contains(gpid)) {
			return -1;
		}
		boolean parentAffinity = Metric.parentsAffinity(currentBlockId, parent, sid, graph, segParent, flag);
		if (!parentAffinity)
				return -1;
		while(parentAffinity) {
			if (!graph.isSecondaryBlock(parent)) {
				sid = parent;
				blockPointer--;
			}
			String tmp = gpid;
			gpid = graph.getCtrlParent(parent);
			parent = gpid; //CHECK THIS CODE FOR ITS APPLICATION AND CORRECTNESS
			
			
			if (parent!=null) {
				parentAffinity = Metric.parentsAffinity(currentBlockId, gpid,parent, graph, segParent, flag);
			}
			else
				break;
		}

		return Integer.parseInt(sid);
	}

	private void contractBlock(String currentBlockId) {
			System.out.println("\n\n\nInside contractBlock and now about to contract block at "+ currentBlockId);
			graph.contractCtrlBlock(currentBlockId);
			System.out.println("Contracted the Block at " + currentBlockId);
			Chain chainObj = new Chain(graph, currentBlockId);
			chainObj.mergeChains();
			System.out.println("Exiting contractBlock after contracting "+ currentBlockId);
	}

	
	private String[] getBlockIds(ArrayList<ControlBlock> primaryBlocks) {
		int size = primaryBlocks.size(), index = 0;
		String[] blockIds = new String[size];
		System.out.println("\ntranslator.SegmentGraph.getBlockIds: Entry ");
		while(index<size) {
			ControlBlock block = primaryBlocks.get(index);
			blockIds[index] = Integer.toString(block.getId());
			index++;
		}
		System.out.println("Total of " + size + " blocks were processed for extracting their Ids");
		System.out.println("translator.SegmentGraph.getBlockIds: Exit\n");
		return blockIds;
	}
	public Hashtable<String, ArrayList<String>> getSegments(){
		return graph.getSegments(1); // Minimum size of segments
	}
}
