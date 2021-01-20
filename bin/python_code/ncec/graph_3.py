import sys
import numpy
import config
import copy

import inspect
'''
Definitions		:
			>	Source		:	A vertex with no predecessor and at least one successor.
				Exc Source	:	A source vertex with exactly one successor.
			>	Sink		:	A vertex with no successor and at least one predecessor.
				Exc Sink	:	A sink with no successor and exactly one predecessor.
'''
degree = {}	#	This hash will keep the indegree and outdegree information of each vertex in the graph. With Each edge contraction we have to update it. [key: (indegree, outdegree)]

def init_degree(g):
	size = len(g)
	d = numpy.zeros((2,size))
	for i in range(0,size):
		for j in range(0,size):
			if g[i,j]:
				d[1,i] = d[1,i] + 1	
			if g[j,i]:
				d[0,i] = d[0,i] + 1
	for i in range(0,size):
		if d[0,i] >0 or d[1,i] >0 :
			degree[i] = (d[0,i], d[1,i])	#	(indeg, outdeg)
	

def init_graph(infile):
	f = open (infile, "r")
	fstr = f.read()		#a big string
	x = fstr.split("\n")	#extracted all words from infile
	V = int(x[0].split()[0])	# no of vertices
	E = int(x[0].split()[1])	# no of edges
#	print (V,E)
	g = []
	for l in range(1,E+1):
		e = tuple (x[l].split())
		#print (e)
		g = g  + [e]
	m = numpy.zeros((V,V))
	#print(g)
	for x in g:
		i = int(x[0])
		j = int(x[1])
		if (x[2]=='c'):
			m[i,j] = 1 	#control edge
		else:	
			m[i,j]= 2 #data edge
	f.close()
	return m

def read_graph (infile):
	m = init_graph(infile)
	init_degree(m)
	return m
	
#------------------------------------------------------------	
def display(adj):
	V=len(adj)
	for i in range(0,V):
		for j in range (0,V):
			if adj[i][j]>0:
				print(i,j,adj[i][j])

def write(adj, fp):
	size = len(adj)
	count = 0
	fp.write("\n")
	for i in range(0,size):
		for j in range (0,size):
			if adj[i][j]>0:
				fp.write("<" + str(i) + "," + str(j) + ": " + str(adj[i][j]) + ">,\t")
				count = count +1
				if count%5 ==0:
					fp.write("\n")
	fp.write("Count of edges in graph : " + str(count) + "\n")


#------------------------------------------------------------
def getSubgraph(start, end, adj):	#	This method will be useful in extracting a chain in which more than one vertex shares common source vertex
	size = end - start + 1		#	by checking the outdegree(source, subgraph) and outdegree (source, graph) ; for extraction both should be same 
	s = numpy.zeros((size,size))
	for r in range(0, size):
		for c in range(0, size):
			s[r][c] = adj[r+start][c+start]
	return s
	
def extractCtrlBlock(target, adj, ctrList):
	adj = contractCtrlBlock(target,adj,True)
	adj = mergeSourceVertices(target,adj)
#	mergeChain(target,adj)	


def get_ctrl_parent(target, adj):
	v = target - 1		#	Created in version 1.0.1
	while (v>=0):
		if adj[v][target]==1:
			return v
		v = v-1
	return -1
					
def grand_parent(adj, parent):	#	Returns the control vertex at the top or at outermost layer
	V = len(adj)
	j=0
	while(j<V):	#This loop can be optimized by making it iterate from V-1 to zero. 
		if adj[j][parent]==1:
			parent = j
			j = 0	#	in optimized version we need to initialize it to (parent - 1)
			continue
		j = j+1
	return parent
#---------------------------------------------------------
def adjacent(v, adj):
	V = len(adj)
	for i in range(0,V):
		if (adj[v][i]):
			return i


def removeSelfLoop(adj):
	size = len(adj)
	for v in range(0,size):
		adj[v][v] = 0
	return adj
#---------------------------------------------------------
def indeg(v,adj):  #self loop is counted
	V = len(adj)
	d = 0
	for i in range(0,V):#This loop should be executing till 'V' not beyond
		if(adj[i][v]!=0): d = d +1
	return d
	

def outdeg(v,adj):  #self loop is counted
	V = len(adj)
	d = 0
	for i in range(0,V): # This loop can be restricted between 'v' to V
		if(adj[v][i]!=0): d = d+1
	return d
#--------------------------------------------------------------------------------------------------
def checkSink(v,adj):	
	size = len(adj)
	indeg = 0
	u = 0
	while  u < size and indeg <2:
		if adj[v][u] == 2:
			return -1	# 'v' has an outgoing d-Edge hence its not sink vertex
		if adj[u][v] == 2:
			indeg = indeg + 1
		u = u + 1
	return indeg	#	if indeg ==1 then its excSink ; if outdeg is more than 1 then its sink but not exclusive
	
def isExcSink(v,adj):# excSource is different from source in the way that it supplies data to exactly one vertex
	count = checkSink(v,adj)
	if count==1:
		return True	#	'v' is excSsink
	else:
		return False		#	'v' not excSink

def isSink(v,adj)	:	#'v' is qualified for sink if it has more than one incoming d-edge but no outgoing d-edge
	count = checkSink(v,adj)
	if count>0:
		return True	#	'v' is sink
	else:
		return False		#	'v' not sink

#-----#-------#--------#--------#-------#-------

def checkSource(v,adj):	
	size = len(adj)
	outdeg = 0
	u = 0
	while  u < size and outdeg <2:
		if adj[u][v] == 2:
			return -1	# 'v' has an incoming d-Edge hence its not source vertex
		if adj[v][u] == 2:
			outdeg = outdeg + 1
		u = u +1
	return outdeg	#	if outdeg ==1 then its excSource ; if outdeg is more than 1 then its source but not exclusive

def isExcSource(v,adj):# excSource is different from source in the way that it supplies data to exactly one vertex
	count = checkSource(v,adj)
	if count==1:
		return True	#	'v' is excSource
	else:
		return False		#	'v' not excSource

def isSource(v,adj)	:	#'v' is qualified for source if it has more than one out d-edge but no incoming d-edge
	count = checkSource(v,adj)
	if count>0:
		return True	#	'v' is source
	else:
		return False		#	'v' not source
#---------------------------------------------------------		
def source(adj):
	V = len(adj)
	ins =[]	#indegree list
	outs = [] #outdegree list
	sourceList = []	# sourceList
	for i in range(0,V):	#	actual range is from 0 to V-1
		ins.append(indeg(i,adj))
		outs.append(outdeg(i,adj))
#	print ("Indegree and outdegree of vertices:\n")
#	print (ins, outs)
	for i in range(0,V):	#Extract SourceList
		if ins[i]==0 and outs[i] ==1:
			sourceList.append(i)
	return sourceList		 
#--------------------------------Edge Contraction and similar contraction Procedures-------------------------

def contractCtrlBlock(target, adj,flag):	#	CHECK THIS CODE IF THE NESTED CTRL BLOCKS ARE CONTRACTING
#	tmplist = []	#	It is used for temprary purpose
	size = len(adj)
	for v in range(0,size):
		if adj[target][v] ==1:
			edge_contraction(target,v,target,adj,flag)
			
#			tmplist.append((target,v))	#	its temporary and can be deleted with the following print statement
#	config.log.write("Graph.ContractCtrlBlock : Edges in segment rooted at " + str(target) + "are " + str(tmplist) + "\n")
	return adj

def mergecomponents(to, frm):

	#print (to,frm)
	if to in config.components:
		config.components [to] = config.components[to] | {frm}
	else:
		config.components [to] = {frm}	#following condition helps in merging series of source vertices.
	if frm in config.components:	# merging a non-singleton segment (i.e. frm) to other vertex
		seg = config.components[frm]
		for i in seg:
			config.components [to] = config.components[to] | {i}
		del config.components [frm]	# remove old vertex 

def adjustDegree(i,j, opt, adj):
	if opt :	#	<i,j>	a new edge is added
		if adj[i,j]:
			return
		(indg,outdg) = degree[i]
		degree[i] = (indg, outdg + 1)
		(indg,outdg) = degree[j]
		degree[j] = (indg + 1, outdg)
	else:
		if not degree.has_key(i): #	<i,j> an edge is removed
			print "Graph.py: Caution! Edge <",i,",",j,"> does not exists...Due to Vertex = ", i
			return
		if not degree.has_key(j): #	<i,j> an edge is removed
			print "Graph.py: Caution! Edge <",i,",",j,"> does not exists...Due to Vertex = ", j
			return
		(indg,outdg) = degree[i]
		degree[i] = (indg, outdg - 1)
		(indg,outdg) = degree[j]
		degree[j] = (indg - 1, outdg)
		
def edge_contraction(i,j,dom,adj, flag):	
# i is tail, j is head, dom is w of paper- it can be either i or j
#	flag - if true then write to log and make an entry in segment list
	adj[i,j]=0	#edge is removed from i to j for contraction
	adjustDegree(i,j,0,adj)	# Edge <i,j> is removed. Zero in third argument represents removal of edge

	V = len(adj)

	if(dom==i):
		poor = j
	else:
		poor = i
		
	for x in range(0,V):  # all incoming to poor tranferred to dom
		if(x==dom):	# to avoid self loop
			continue
		if ((adj[x][poor]>0)and(adj[x][dom]==0)):  # i doesnt have incoming from x, transfer whatever
			adjustDegree(x,dom,1, adj)	#	Function for keeping track of degree
			adj[x][dom]=adj[x][poor]
		else:
			if ((adj[x][poor]==1)and(adj[x][dom]==2)): 
				#i has data incoming, transfer control incoming
				adjustDegree(x,dom,1, adj)	#	Function for keeping track of degree
				adj[x][dom]=adj[x][poor]


		if adj[x,poor]:
			adjustDegree(x,poor,0,adj)	#	Function for keeping track of degree
		adj[x][poor]=0

		
	for x in range(0,V):  # all outgoing tranferred from j to i
		if(x==dom):# to avoid self loop
			continue
		if ((adj[poor][x]>0)and(adj[dom][x]==0)): #i has no outgoing to x, transfer whatever
			adjustDegree(dom,x,1,adj)
			adj[dom][x]=adj[poor][x]

		else:
			if ((adj[poor][x]==1)and(adj[dom][x]==2)): #Seems parent edge will not be affected
				#i has outgoing data,  transfer control outgoing
				adjustDegree(dom,x,1,adj)
				adj[dom][x]=adj[poor][x]	


		if adj[poor,x]:
			adjustDegree(poor,x,0,adj)
		adj[poor][x]=0

		
	if flag:		
		#	code below is merging two vertices/segments and deleting 'poor' segment
		if poor in config.segList:
			l  = config.segment[dom] + [poor]
		else:		
			l = config.segment[dom]+ config.segment[poor]  
			del config.segment[poor]
		
		if config.flag['EdCo']:
			config.log.write("Edge <" + str(i) + "," + str(j) + "> Contracted, dom : " + str(dom) + 'caller :'+ inspect.stack()[1][3] + "\n")
		config.segment[dom] = copy.deepcopy(l)
	
	
#	mergecomponents(dom, poor)
	return adj
	
#----------------Following are different kind of Successor functions--------------------
def getDataSuccessors(target,adj):
	size = len(adj)
	scsr = []
	for v in range(0,size):
		if adj[target][v] == 2:
			scsr.append(v)
	return scsr

def getNonSinkSuccessors(target, ubound, adj):
	size = len(adj)
	nonsink = []
	for v in range(target,ubound):
		if adj[target][v] ==2 and outdeg(v,adj)>0:
			nonsink.append(v)
	return nonsink

def getSinkSuccessors(target, ubound, adj):
	sink = []
	for v in range(target, ubound):
		if adj[target][v] == 2 and outdeg(v,adj)==0:
			sink.append(v)
	return sink

def getExcSinkSuccessors(target, ubound, adj):
	sink = []
	for v in range(target, ubound):
		if adj[target][v] == 2 and isExcSink(v,adj):
			sink.append(v)
	return sink
			
#----------------Following are different kind of predecessor functions------------------				
				
def getDataPredecessors(target,adj):
	size = len(adj)
	pred = []
	for v in range(0,size):
		if adj[v][target] ==2:
			pred.append(v)
	return pred

def getNonSourcePredecessors(target, lbound, adj):
	size = len(adj)
	nonsource = []
	for v in range(lbound,target):
		if adj[v][target] ==2 and not isSource(v,adj):
			nonsource.append(v)
	return nonsource

def getSourcePredecessors(target, lbound, adj):
	source = []
	for v in range(lbound,target):
		if adj[v][target] == 2 and indeg(v)==0:
			source.append(v)
	return source
	
def getExcSourcePredecessors(target, lbound, adj):
	source = []
	for v in range(lbound,target):
		if adj[v][target] == 2 and isExcSource(v,adj):
			source.append(v)
	return source	
