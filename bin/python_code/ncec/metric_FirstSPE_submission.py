import sys
import graph
import config
import copy
#	GetAccumulativeDataNodeCount():
#	This method reads list of producers present in the block under consideratio.
#	extract one by one each producer and find all the data nodes supplying data to it.
#	These data nodes are the ones which are present in the block or ex. source to the block	
def GetAccumulativeDataNodeCount(target, adj, producerHash, dEdgeHash):
	count = 0
	stack = []
	datasupplier = set([])
	dataNodes = dEdgeHash.keys()
	
#	graph.display(adj)
	tmplist = []	#	keeps direct or indirect supplier of a producer
	for p in producerHash.keys():	#	For each producer vertex do following
		stack.append(p)		#	Add the producer to stack
		while stack:	#	Now find all directly or indirectly data suppliers to 'p'
			v = stack.pop()	#	Now, add this to tmplist and push adjs of 'v' to stack
			for u in dataNodes:
				if adj[u][v] == 2:
					stack.append(u)	#	u is supplying data to v directly, and to p indirectly/directly
					tmplist.append(u)
					datasupplier.add(u)	#	create a set of data suppliers then at the end we can find the remaining elements too.
		print "Producer : ", p, " and Supplier are : ", tmplist
		count = count + len(tmplist) 
	allnodes = set(dataNodes)
	nondatasuppliers = allnodes - datasupplier
	count  = count + len(nondatasuppliers)
	config.log.write("Metric.py : All nodes in block at " + str(target) + " are " + str(dataNodes) + "\n")
	config.log.write("Metric.py : NonData supplier in block at " + str(target) + " are " + str(nondatasuppliers) + "\n")
	return count
	
	
def measureAffinity(target, adj):
	dEdgeHash  = {}	# All data edges which belongs to the block; includes source input dependencies on the block too.
	producerHash = {}			# All data edges which are originated from block and used outside
	span = config.ctrlSpan[target]
	V = len(adj)
#	graph.display(adj)
	for v in range(target, span + 1): # for every statement which is direct-indirect ctrl dependent on the target
		if config.vHash.has_key(v) and config.vHash[v]<=4:
			continue	# vertex v is a control vertex
		else:
			for i in range(0,V):
# 				data edge originating from the statement 'v' in block rooted at 'target'; 
				if adj[v][i]== 2: 	
					dEdgeHash[v] = 1
					if i >span:	# True >> edge is originated inside but going out; thus consumed by outer statement/block
						producerHash[v] = 1

#				add source vertices incident to the block at 'target' to it's dataEdgeList
#				check if vertex 'i' supplies data only to the statements of the block rooted at 'target'
				if i < v and graph.isSource(i,adj): # i.e., 'i' is a source vertex
					count = 0
					for j in range(target,span + 1):
						if adj[i][j] == 2:
							count = count + 1
					if count == graph.outdeg(i,adj):
						dEdgeHash[i] = 1

#	dCount = float(len(dEdgeHash))		# It was being used in version 3.0; now we will compute accumulative 
	accuDCount = GetAccumulativeDataNodeCount(target, adj, producerHash, dEdgeHash)
	pCount = float(len(producerHash))
	
	if config.flag['PrLi']:
		config.log.write("\n\t\t Block : " + str(target) + "Producer :" + str(producerHash.keys()) + " <" + str(config.ctrlStatHash[target].producerList) + ">\n")

	
	if pCount == 0 or accuDCount == 0:	#	is dcount ==0 is sufficient condition;
#		affinity = -1
		config.log.write("\n\t\t Block :" + str(target) + "Affinity :" + str(-1) + "\n")
		return False
	else:							
		affinity = pCount/accuDCount 
		if affinity < float(config.flag['ExTh']) :		
			config.log.write("\n\t\t Block :" + str(target) + "Affinity :" + str(affinity) + "\n")
			return True
		else:
			config.log.write("\n\t\t Block :" + str(target) + "Affinity :" + str(affinity) + " "+ str(float(config.flag['ExTh'])) + "\n")
			return False
			
#-------<&>--------<&>-------<&>---------------<&>---------------<&>---------------<&>---------------<&>---------------<&>--------
#	Function		:	findDataDependenceCount()
#	Description		:	count of all the dEdges which are directly or indirectly data-connct (supply or consume) with inner control block
#					:	
#---------------------------------------------------------------------------------------------------------------------
#--------------------Code below finds direct data suppliers in parent ctrl to inner ctrl block------------------
#-------store direct suppliers in 'dConnect' and suppliers of the dconnect vertices in 'stack'-------------
def getIncomingDataDependents(target, parent, adj):
	span = config.ctrlSpan[target]
	dConnect = {}
	stack = []
	span = config.ctrlSpan[target]
	for v in range(target -1,parent, -1): # count all the edges which are supplying (or involved indirectly) data to target block
		for i in range (target,span+1):	#	Checking if this statement supplies data to target block
			if (adj[v][i]==2):	#	statement at 'v' supplies data
				dConnect [v] = 1
				for u in range(parent,v):
					if (adj[u][v]==2):	#u is supplying data to v and is in the outer ctrl block
						stack.append(u)
				break	#	This prevents counting one statement (supplying data to more than one statement) more than once
#------Now we process indirect suppliers and their suppliers up to the parent -----				
#	while len(stack):
#		v = stack.pop()
#		dConnect[v] = 1
#		for u in range(parent,v):	
#			if adj[u][v]==2:
#				stack.append(u)
	return dConnect

def getOutgoingDataDependents(target, parent, adj):
#-----Now we will find the vertices which are dependent on the values produced by inner ctrl block------
	span = config.ctrlSpan[target]
	pSpan = config.ctrlSpan[parent]
	dConnect = {}
	stack = []
	tmp = ""
	for i in range (target, span+1):
		for v in range(span+1, pSpan+1):
			if adj[i][v] == 2:
				dConnect [v] = 1
				for u in range(v+1,pSpan+1):	
					if adj[v][u] == 2:
						stack.append(u)
						
#---------Now we will find indirect dependants---------------------
#	while len(stack):
#		v = stack.pop()
#		dConnect [v] = 1 
#		for u in range(v+1, pSpan+1):
#			if adj[v][u] == 2:
#				stack.append(u)	
	return dConnect

def findDataDependenceCount(sid, parent, adj):	#	Here target is "sid"
	count = 0
	dConnect = {}	#	After contraction of inner block, All vertices with outdegree>0 and direct connected with inner block r candidate
	stack = []		
	dConnect = getIncomingDataDependents(sid, parent, adj)
	config.log.write("All supplier vertices from parent to " + str(sid) + str(dConnect.keys()) + "\n")
	tmp = getOutgoingDataDependents(sid,parent,adj)
	config.log.write("All consumers vertices in parent from " + str(sid) + str(tmp.keys()) + "\n")
	dConnect.update(tmp)
	config.log.write("All Dependency from inner to parent block is  " + str(dConnect.keys())+"\n")				
#-------CHECK ABOVE Code-------------------------------------------				
	return len(dConnect)	#	Count of all data connect elements
#---------$----------$-----------$-----------$-----------$--------------$---------------

#-------------------< Check the code below and change it for measuring parent's affinity >------------------------
	
#		Input		:	A vertex 'target' which is already selected for extraction and it's parent is to be tested for affinity.
#		Output		:
#		Detail		:	A distinctDEdge count is the count of computations which take place outside the inner control block and
#						 within the present ctrl block.
#					>	If the parent has no distinctDataEdge then it is either consecutive block or
#						 a control block which repeats the function taking place inside the inner control block.
#					>	If the parent have one or more distinctDataEdges then
#						>	Now here we find the count of distinct producers.
#						>	A producer is a statment which is assigns a value to some variable which is used by 
#							other statement(s) outside the present control block (can be parent ctrl block also).
#--------------<>--------------------------------<Parent Affinity>------------------------------<>-------------------------<>-------------
def getDistinctParentDEdgeCount(sid, parent, adj):
	#	After merging inner block, all vertices with outdegree>0, are the candidate for this.
	span = config.ctrlSpan[sid]
	count = 0
	elist = config.ctrlStatHash[parent].dEdgeList
	config.log.write("All data nodes in parent : " + str(elist) + "\n")
	tmp = []
	for v in elist:	#	All data vertices present in the parent block
		if not graph.degree.has_key(v):
			continue	#	Exclude edges present in the sid block
		(indg,outdg) = graph.degree[v]
		if indg+outdg >1:
			tmp.append(v)	#	Remaining are the edges of the parent block
			count = count +1
	config.log.write("All distinct edges in parent : " + str(tmp) + "\n")
	return count		
def getDistinctProducerCount(target, parent):
	pList = config.ctrlStatHash[parent].producerList	#Producer List
	count  = 0	#	distinct Producer count
	for p in pList:
		if not p in config.ctrlStatHash[target].producerList:
			count  = count + 1
	return count
	
def isParentExtractable(ratio, parent ):
	if config.flag['PaAf']:
		config.log.write("Affinity of Parent : " + str(parent) + "is " + str(ratio)+"\n")
	print "<----",ratio, parent,"---->"
	if ratio<float(config.flag['PExTh']):
		return True
	else:
		return False	#That is no need to check further;
	
def getParentsAffinity(target, parent, sid, adj):
	distinctDEdgeCount = getDistinctParentDEdgeCount(sid, parent,adj)	#Data vertices which are not listed to be merged with inner block
	dependentCount = findDataDependenceCount(sid, parent, adj)	#	count of total dEdges which directly data-connect (supply or consume) with inner control block
	config.log.write( "getParAffinity(): target " + str(target) + "Parent " + str(parent) + "DistDEdge " + str(distinctDEdgeCount) + "\n")
	if distinctDEdgeCount>config.flag['PaDeDi']:	
		distinctProducer = getDistinctProducerCount(sid, parent)	# THIS IS MODIFIED... INSTEAD OF 'target' --> 'sid' is used
		InDependentCount = (float)(distinctDEdgeCount - dependentCount) # (= all the vertices which do not result in a value used by inner block)

		if distinctProducer >0:
			if InDependentCount < 0:	# Either none or few statements in parent consume the producer; So, leave the parent
				config.log.write("Warning/Error: have to handle this case;" + "Dependent Edge count"+ str(dependentCount)+"\n")
				return False	#That is, no need to extract this parent with inner control block
			elif InDependentCount >0:
				ratio = InDependentCount/distinctDEdgeCount
				return isParentExtractable(ratio, parent)
			else:#	All the statements in parent consume the producer and in return produce a data
				config.log.write("Parent fully connected with child; Target " + str(target) + "Parent" + str(parent)+ "\n")
				return True
		else: # CASE of dEdge >0 and distinctProducer = 0
			ratio = InDependentCount/distinctDEdgeCount
			return isParentExtractable(ratio, parent)
	else:		
		print "Target ", target, " Consecutive (or similar) Parent:", parent
		return True

	#		print "Parent", parent, "is consecutive parent to block at ",target
def parentsAffinity(target,parent,sid, adj):
	if target< 0 or parent <0 or config.vHash[target]==4 or config.vHash[parent]==4 or parent in config.segParent:	#	target is -1 or	No parent exists or target is secondary ctrl vertex or parent has already a nested segment--> don't extract it
		return False
			
	result = getParentsAffinity(target,parent,sid, adj)
	return result
