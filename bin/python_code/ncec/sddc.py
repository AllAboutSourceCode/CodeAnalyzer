import sys
import copy
import graph
import numpy
import config

import inspect

'''
	Sequential data dependence Contraction (SDDC)
	>	It merges all such 'chain structures' originating and ending in the parent control block of 'target' to 'target' (a specified vertex, e.g. a new vertex created after CEC)
	>	Chain structure may contain brances of a certain degree (in our case it is d = 1)
	>	A Branch of a chain is an incoming/outgoing chain in which head/tail vertex is source/sink. Such a branch will be considered as part of chain only if dth node in the branch is a source/sink vertex, where d is the degree of branch.
	>	A chain which does not begins/ends with source/sink heads/tails is called LinkChain
	Last Modified	:	28 Dec, 17
'''

def selectiveSDDC(target,adj):
	parent = graph.get_ctrl_parent(target,adj)
	config.log.write("\n\tIn Selective SDDC called by " + inspect.stack()[1][3] + "\n")
	if parent <0:	#	IF THERE IS NO PARENT THEN WE SHOULD CHECK FROM 0 AND TILL SIZE OF ADJ
		lbound = 0
		ubound = len(adj)-1
	else:
		ubound = config.ctrlSpan[parent]
		lbound = parent +1
		
	adj = producerSDDC(target, lbound, ubound, adj)	#	incident (to 'target') chain is merged
	adj = consumerSDDC(target, lbound, ubound, adj)	#	leaving (from 'target') chain is merged

	return adj


#-------------------------------getProducerChain()----------------------------------------------
#	ARGUMENTs involve a vertex 'tail' from which we have to start looking for chain.
#	next vertex is the predecessor of 'tail' and so on.
#	second parameter is the limit to which we can add a vertex to chain.
#	----------------
#	RETRUNs a dictionary 'branch'.
#	Each positive key (which is part of chain) contains two lists one for source vertices and other for sink vertices incident/originating on/from it.
#	whereas key '-1' contains list of all the vertices which form the chain.
#	If there is an entry for key '-2' then it means that vertex 'tail' leads to more than one non-source head chains (Thus only one of them will be merged with successor vertex of 'tail'. For now first chain in the list is merged, whereas the longest chain can also be merged. Thus, leaving a higher probability for the parent control vertex to be extracted with the inner control vertex(successor of 'tail'))

def getProducerChain(tail,lbound,ubound, adj):
	chain = {}	#contains the branch vertices of the chain {target: (source,sink)}; key -1 contains all the vertices of the chain excluding branches
	lst = []
	lst.append(tail)
	nonsource = graph.getNonSourcePredecessors(tail,0, adj)	#inspect vertices falling between 'lbound' and 'tail' 
	source = graph.getExcSourcePredecessors(tail,lbound,adj)	#	inspect vertices falling between 'lbound' and 'tail'
	pred = graph.getDataPredecessors(tail,adj)
	pred.sort()

#	sink = graph.getExcSinkSuccessors(tail, ubound, adj)	#	All sink/source vertices are not exclusive; so, we will merge them once the chain is contracted.
	sink = []	
	chain[tail] = (copy.deepcopy(source),copy.deepcopy(sink))	
	config.log.write("tail :" + str(tail)+ " nonsource : " + str(nonsource)+ "successors " +str(graph.getDataSuccessors(tail,adj)) + "\n")
	while len(nonsource) ==1 and  len(graph.getDataSuccessors(nonsource[0],adj)) == 1 and nonsource[0]>=lbound and (config.ctrlParentMap[tail]== config.ctrlParentMap[nonsource[0]]):	# That is the vertex in the chain is not outside (before) the parent block
		tail = nonsource[0]
		lst.append(tail)
		nonsource = graph.getNonSourcePredecessors(tail,0, adj)#using lbound here prevents us from getting a link chain, for link chain use lb=0
		source = graph.getExcSourcePredecessors(tail,lbound, adj)
		pred = graph.getDataPredecessors(tail,adj)
		pred.sort()
#		print tail,nonsource, source
		config.log.write("tail " + str(tail) + " nonsource : " + str(nonsource)+"\n")
#		sink = graph.getExcSinkSuccessors(tail,ubound, adj)
		chain[tail] = (copy.deepcopy(source), copy.deepcopy(sink))
		source = sink = []
	if pred[0]<lbound or len(nonsource)>1 or (len(nonsource)==1 and nonsource[0]<=lbound) or len(nonsource)>0 and len(graph.getDataSuccessors(nonsource[0],adj)) > 1:	#	It reflects the fact that the chain is linkchain
		chain[-2] = 1
	chain[-1] = copy.deepcopy(lst)		
	return chain

def producerSDDC(target,lbound, ubound,adj):
	count  =0
	linkChain = {}
	pred = graph.getDataPredecessors(target,adj)
	config.log.write("Producer SDDC : Target = " + str(target) + " & data predecessors are " + str(pred) + " & " +"<lbound = " + str(lbound) + "ubound = " + str(ubound) + " >\n")
	tmp = ""
	for v in pred:
		flag = (config.ctrlParentMap[v]== config.ctrlParentMap[target])	#checking whether they both share same ctrl region or not
		if v>=lbound and flag and graph.outdeg(v,adj) == 1:
			chain = getProducerChain(v,lbound,ubound, adj)	#	get chain ending at vertex 'v'
			if config.flag['PrCh']:
				tmp = tmp + str(chain) + "\n"
			if count<2 and chain.has_key(-2):	#	its a linkchain
				if not count :
					linkChain = chain
				count = count +1
				if config.flag['InLi'] and len(chain[-1])<=int(config.flag['InLi']):	#	Include Linkchain of length config.flag['InLi']
					adj = mergeChain(target, chain, adj,1)	#	chain with non-source head but one length; so merged it
					count = count - 1
					config.log.write("Producer SDDC: Link chain at " + str(target) + "merged to it; chain :" + str(linkChain) )
				continue	#	don't merge this chain
			else:
				config.log.write("\nProducer : source chain" + str(chain) + "is being merged...\n")
				adj = mergeChain(target, chain, adj,1)	#	chain with source head, so merge it
	if count == 1:
		config.log.write( "\nProducer: Link chain is being merged" +str(linkChain))
		adj = mergeChain(target, linkChain, adj,1)	#	chain with source head, so merge it
	else:
		if count == 0:
			config.log.write("\nProducer : No LinkChain is found\n")
		else:
			config.log.write("\nProducer : More than one LinkChain is found")
#	if len(tmp)>0:
#		config.log.write("\nProducer chains are :" + tmp)			
#	else:
#		config.log.write("\nNo Producer chains are Found")			
	return adj				


#--------&-----------&---------&----------&-----------&------------&----------&-----------&---------&----------&-----------&---------


def getConsumerChain(head, target, lbound, ubound,adj):
	chain = {}	#contains the branch vertices of the chain {target: (source,sink)}; key -1 contains all the vertices of the chain excluding branches
	lst = []
	linkChain = {}
	cflag = False
	lst.append(head)
	sink = []
	source = graph.getExcSourcePredecessors(head,lbound,adj)
	if target in source:
		source.remove(target)
	chain[head] = (copy.deepcopy(source),copy.deepcopy(sink))	
	succ = graph.getDataSuccessors(head,adj)	#	gives all the successors of 'head'
	while len(succ) ==1 and succ[0]<=ubound and (config.ctrlParentMap[head]== config.ctrlParentMap[succ[0]]):	# That is the vertex in the chain is not outside of the parent block
		head = succ[0]
		if config.ctrlSpan.has_key(head):
			cflag = True
			config.log.write("A control block is encountered in consumer chain; Id = " + str(head)+ "\n")
			break
		lst.append(head)
		source = graph.getExcSourcePredecessors(head,lbound, adj)
		chain[head] = (copy.deepcopy(source), copy.deepcopy(sink))
		succ = graph.getDataSuccessors(head,adj)
		source = sink = []
	chain[-1] = copy.deepcopy(lst)		
	slen = len(succ)
#	Link chain checking or non-source chain
	if cflag or len(succ)>1 or (len(succ)>0 and succ[0]>ubound):	#	It reflects the fact that the chain is linkchain
		chain[-2] = 1
	return chain

def getDupAdj(v,lbound, adj):	#	Exclusive source predecessors on a control block rooted at v
		dupadj = np.copy(adj)
		span = config.ctrlSpan[v]
		for u in range(v,span+1):
			graph.edge_contraction(v,u,v,dupadj)
		return dupadj

def consumerSDDC(target, lbound, ubound, adj):
	count = 0
	size = len(adj)
	allChainNodes = {}
	successors = graph.getDataSuccessors(target,adj)
	config.log.write("\nConsumer SDDC : Target =" + str(target) + " & data successors are " + str(successors)  + " & <lbound = " + str(lbound) + "ubound = " + str(ubound) + " >\n")
	tmp = ""
	for v in successors:
		if config.ctrlSpan.has_key(v):	#	'v' is a block
#			dupadj = getDupAdj(v,lbound,adj)
#			exsrc = getXSrcPred_on_CtrlBlock(v,lbound, dupadj)
#			dataPred = getDataPred_on_CtrlBlock(v,dupadj)
			continue
		else:
			exsrc = graph.getExcSourcePredecessors(v, lbound, adj)	#	If we use 0 inplace of lbound then one parameter to new segment will increase
			dataPred = graph.getDataPredecessors(v,adj)
		
		flag = (config.ctrlParentMap[v]==config.ctrlParentMap[target])
		diff = (len(dataPred) - len(exsrc))	#	Mostly it is checking if the next vertex has only exc source incoming not 2 data predecessor(1 will always be)	
		if flag and (diff==1 or diff == 0):
			chain = getConsumerChain(v, target, lbound, ubound,adj)	#	get chain starting from vertex 'v'
			if count<2 and chain.has_key(-2):	#	its a linkchain
				if not count:
					linkChain = chain
				count = count +1
				continue	#	don't merge linkchain
			else:	
				
				adj = mergeChain(target, chain, adj,0)	#	chain with source head, so merge it
#				config.log.write("Consumenr : Sink Chain is being merged" + str(chain))
			if config.flag['CoCh']:
				tmp = tmp + str(chain) + "\n"
	if count ==1:	#	Only one linkChain, hence merging
		adj = mergeChain(target, linkChain, adj,0)	
		config.log.write("\nConsumer: Merged LinkChain" + str(chain))
	else:
		if count == 0:
			config.log.write("\nNo LinkChain Found\n")
		else:
			config.log.write("\nMore than one LinkChain Found\n")
	if len(tmp)>0:
		config.log.write("\nConsumer : Sink chains are :" + tmp +"\n")			
	else:
		config.log.write("\nConsumer: No sink chains are Found\n" )
	return adj


#--------&-----------&---------&----------&-----------&------------&----------&-----------&---------&----------&-----------&---------
def mergeChain(target, chain, adj, option):
	tmp = ""
#	config.log.write("\n\tIn Merge Chain called by " + inspect.stack()[1][3] + "\n")
#	config.log.write("Chain is "+ str(chain))
#	config.log.write("\t chain to be merged to :" +str(target) + "is " +str(chain)+"\n")
	chainlist = chain[-1]
	keys = chain.keys()
	keys.sort(reverse = True)
#	print "Entry MergeChain : option = " , str(option), "chain list ", chain
	
	for v in keys:
		if v>=0:	#	'v' is the vertex in chain with branch
			(source, sink) = chain[v]		#	branches can be source (incoming) or sink (outgoing)
			for value in source:	#	merge all excsource branches on 'v' to 'v'
				if value not in chainlist and (config.ctrlParentMap[value]==config.ctrlParentMap[v]):	#	if the head of the chain is source then it is present in two places in branch and in main chain
					adj = graph.edge_contraction(value,v,v,adj)
					tmp = tmp + str(value) + "  " 
				else:
					config.log.write("\nvertex" + str(value) + "is either present twice in chain OR is not in same control region with " + str(v) + "\n")
#				count = count +1
#			for value in sink:	#	merge all excsink branches from 'v' to 'v'
				#	in case of consumer we need to put similar check as we did for source "if value not in chainlist:"
#				adj = graph.edge_contraction(v,value,v,adj)
		else:
			 if v == -1:
#				chainlist = chain[v]	# or say chain[-1]
#				print chainlist, ")", option
				for u in chainlist:	#	contract sequential depedence chain of edges to one vertex 'target'
					if option:
#						print "contracting<> ", target, u
						adj = graph.edge_contraction(u,target,target,adj)	#For Producer Chain
					else:
#						print "contracting " , str(target), " ",str(u)
						adj = graph.edge_contraction(target,u,target,adj)	#For consumer chain
					tmp = tmp  + str(u) + " " 
#					print "----------------------"
#					graph.display(adj)
	if config.flag['ChVe']:	#	For printing chain vertices in log file
		if len(tmp)>0:
			config.log.write("Vertices Merged to the  target : " + str(target) + "are : \n[ " + tmp + " ]")
		else:
			config.log.write("No chain at target : " + str(target))
			
#	print "-------Exit Merge Chain--------------"		
	return adj

#-----(*&^)------(*&^)------(*&^)------(*&^)------(*&^)------Global SDDC------(*&^)------(*&^)------(*&^)------(*&^)------

#		This part of the code is taken from older version of segmentation algorithm.
#		It identifies and extracts chains with no branches.
#		Our added module will identify and append the exclusive source and sinks to the existing chains.
	
def removeNonChainVertices(g):
	removed = []
	V = len (g)
	for v in range(0,V): #any join and fork edges cannot be part of chain
	                         #because chain collapses all into one only
	                         # it will create conflict with other branches!
	                         #internal excluive edges are to be collapsed	                        
		if graph.outdeg(v,g) > 1:
			removed.append((v,0))
		if graph.indeg(v,g)>1:
			removed.append((v,1))
		
	for (v,x) in removed:	
		if x==0:
			for j in range(0,V):
				g[v][j]=0;	#some may be already 0.. bit of redundant work	
		else:
			for i in range(0,V):
				g[i][v]=0;
		
		#now what remains is only chains

def pickchain(v,g,l):
	V =len(g)
	for i in range(0,V):
		if g[v][i]>0:
			l.append((v,i))
			pickchain(i,g,l)
			break
			
def all_maximal_chains(adj):
	V = len (adj)
	g = numpy.zeros((V,V))
	c=[]
	for  i in range(0,V):
		for j in range (0,V):
			g[i][j] = adj[i][j]
#	print ("graph for chains:",graph)
#	branches = extractBranches(g) #	It removes the source and sink branches from the graph; after this we extract chains with no branches and later we add proper brances to the extracted chains [This way existing code was reused]
	removeNonChainVertices(g)
	for i in range(0,V):
		if graph.outdeg(i,g)==1 and graph.indeg(i,g)==0:
			l = []
			pickchain(i,g,l)
			c.append(l)	
	return c

#-------()-------()-------()-------()-------()-------()-------()-------()-------()-------()-------()-------()-------
#	Given the sequence that first ESC() will be applied then there will actually be no chain which will have branches. 
#	and ESC has to be extended to support merging ExcSink vertices also.
#	Or in other words sequence of ESC() and SDDC() actually contracts the chains with branches.

def sddc(adj):
	config.log.write("---------------------------------------SDDC---------------------------------------------\n")
	chainList = all_maximal_chains(adj)
#	print ("chains found are :")
#	print (chainList)	# format of output is : [[(0,1), (1,2)],[(4,5),(5,6),(6,7)]]

	clen = len(chainList)
	while (clen>0):
		c = chainList[clen-1]

#		print ("chain : ",c)
		del(chainList[clen-1])
#	Process the chain in reverse order (bcoz of list)
		clength = len(c)

		(chain_head,v) = c[0]
		clen = len(chainList)
		
		for i in range (0,clength):
			(u,v) = c[i]
			adj = graph.edge_contraction(chain_head,v,chain_head,adj)
	#	each time we have to contract an edge  (chain_head,v) bcoz list is computed before middle vertices are removed
		clen = len(chainList)
	config.log.write("-----------------------------------End of SDDC------------------------------------------\n")
	return adj
	
'''
def getConsumerChain(head,lbound, ubound,adj):
	chain = {}	#contains the branch vertices of the chain {target: (source,sink)}; key -1 contains all the vertices of the chain excluding branches
	lst = []
	lst.append(head)
	nonsink = graph.getNonSinkSuccessors(head,ubound,adj)
	sink = graph.getExcSinkSuccessors(head,ubound, adj)	#	sink:	A node with no sucessor and only one predecessor 'head'
	source = graph.getExcSourcePredecessors(head,lbound,adj)

	chain[head] = (copy.deepcopy(source),copy.deepcopy(sink))	
	source = sink = []
	while len(nonsink) ==1 and nonsink[0]<ubound:	# That is the vertex in the chain is not outside of the parent block
		head = nonsink[0]
		lst.append(head)
		nonsink = graph.getNonSinkSuccessors(head,ubound,adj)
		sink = graph.getExcSinkSuccessors(head,ubound, adj)
		source = graph.getExcSourcePredecessors(head,lbound, adj)
		chain[head] = (copy.deepcopy(source), copy.deepcopy(sink))
		source = sink = []
	if len(nonsink)>1 or (len(nonsink)>0 and nonsink[0]>=ubound):	#	It reflects the fact that the chain is linkchain
		chain[-2] = 1
	chain[-1] = copy.deepcopy(lst)		
	return chain

'''	
	
