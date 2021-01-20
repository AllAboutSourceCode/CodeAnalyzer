import sys
import numpy
import config
import copy
import graph
import esc
import metric
import sddc
'''
Description		:	Implements control block extraction; Starts from the control block which occurs at the end and goes back up to the first control block in the program. Every control block is measured for affinity with its surrounding statements. Blocks with affinity below a threshold are considered to have implementing functionality worth extracting and separable from the parent program. 

High Level View	:	>	Iterate over list of ctrl vertices in reverse order of their occurrence/appearance in the program
					>	If a vertex is eligible to be extracted,
						>	1. mark exclusive source vertices and chain incident/originating on/from it.
						>	2. Inspect if the parent control block can also be extracted along with it.
						>	Go to the step 1
					>	If the vertex is not eligible for extraction
							>	check if it is outermost control block 
								>	a. compress it to one single node
								>	b. store its state in 'controlstathas'
							>	otherwise
								>	No Action, return to next control block in iteration (i.e. to the parent of this control block)

Algorithmic View	:	ncec():

Last Modified	:	March 28, 2017
'''


def contractBlock(target, adj):

	adj = graph.contractCtrlBlock(target,adj,True)	#	Now contract the whole control block rooted at 'sid'
	config.log.write("---After contracting control block at " + str(target) + "\n")
#	graph.write(adj,config.log)
	adj = esc.selectiveESC(target, adj)
	adj = sddc.selectiveSDDC(target, adj)
	return adj
#-----------------------------------------------------------------------------------------------
#	Check if the parent is secondary control vertex and it can be part of the task.

def checkSecondaryParentsFunctionalCoherence(target, parent, sid, adj,index):
	pparent = graph.get_ctrl_parent(parent,adj)
	if  pparent in config.segParent:	#	parent is secondary and grand parent has a nested seg so this parent can't get extracted
		return (-1,index)
	result = metric.getParentsAffinity(target,parent,sid, adj)
	#	If the secondary parent does not qualifies the affinity then return -1
	if not result:
		return (-1,index)
	while(result):
		if config.vHash[parent] <=3 :	#	parent is primary control vertex, so mark it as latest 'sid'
			sid = parent
			index = index -1
		tmp = pparent
		pparent = graph.get_ctrl_parent(parent,adj)
		parent = pparent
		if parent>=0:
			result = metric.getParentsAffinity(target, pparent, parent, adj)
		else:
			break
	return (sid,index)	#	check it.
#-----------------extract()-------------------------
#		
#		Target		:	Inner control block identified to be extracted.
#		sid			:	'sid' contains the recent parent which is closely associated with control block at 'target'
#		return sid	:	This statement returns the id of the top most parent control block to be extracted with 'target'
def extract(target,adj,ctrList,index):
	sid = target
	parent = graph.get_ctrl_parent(target, adj)
	tmp = ""	#	used for writing into log file. no role in segmentation
	config.log.write("target: " + str(target) + " Inspecting parent " + str(parent) + "\n")
	while (not parent in config.segParent and metric.parentsAffinity(target, parent, sid, adj)):	#	It returns 'True' or 'False'
		index = index-1
		sid = parent
		adj = contractBlock(sid, adj)
		parent = graph.get_ctrl_parent(parent, adj)		
		tmp = tmp + " " + str(sid)	#	used to write in log file
		config.log.write("target: " + str(target) + " Inspecting parent " + str(parent) + "Sid "+ str(sid) + "\n")	
	config.log.write("\tParents extracted with "  + "target :" + str(target) +" are <" + str(tmp))
	#	few parents may be written inside the above method 	
	if config.vHash.has_key(parent) and config.vHash[parent]==4:
		(result,index) = checkSecondaryParentsFunctionalCoherence(target, parent, sid, adj, index)
		if result>=0:
			sid = result
			adj = contractBlock(sid, adj)
	return (adj,sid, index)

	
def markParent(sid,adj):
	p = graph.get_ctrl_parent(sid,adj)
	while (p!=-1):
		config.segParent.append(p)	#	It will prevent the parent control blocks to get extracted with other child segments
		sid = p
		p = graph.get_ctrl_parent(sid,adj)	
	
def ncec(adj, ctrList):
	config.log.write(str(graph.degree) + "\n")
	config.log.write("\n\n-------------------------------NCEC----------------------------------------------\n\n")
	index = len(ctrList) -1
	while(index>=0):
		target = int(ctrList[index][0])	#Get elements in reverse order
		if config.vHash[target] <= 3:	# for every primary control vertex
			#check if the target is the candidate to be compressed.
			config.log.write("\tTarget : [" + str(target) + "] \n")
			if config.flag['CtSp']:
				config.log.write("\tSpan :" + str(config.ctrlSpan[target]) + "\n")
			if metric.measureAffinity(target,adj):	#	If return is True then extract
				config.log.write("\t" + str(target) + " qualifies for extraction\n")
#				graph.write(adj, config.log)
				(adj,sid,index) = extract(target,adj,ctrList,index)	#sid is the id of the extracted segment
				#	Add all the vertices to the segement with id 'sid'
				config.segList.append(sid)	#	Segment is extracted.
				config.log.write("\t SegID of Extracted block is  " + str(sid) +"\n")
				markParent(sid, adj)
				if config.flag['GrPoEx']:
					config.log.write("\tRemaining Edges in Graph After extraction of " + str(sid) + "\t")
					graph.write(adj,config.log)	#
			else:
				config.log.write("\n\t"+ str(target) + " Disqualified for extraction block :"  + "\n")
				parent = graph.get_ctrl_parent(target, adj)
				if parent == -1:	# 'target' is the outer control block and not extracted so it can be contracted.
					adj = graph.contractCtrlBlock(target,adj,True)
					config.log.write("\t Block at " + str(target) + " is outer and is contracted\n")
					#	Add this entry to Hash
		index = index -1	#	Move to previous control block in program
	config.log.write("\n----------------------------------End of NCEC-------------------------------------------\n\n")
	config.log.write(str(graph.degree))
	return adj	
