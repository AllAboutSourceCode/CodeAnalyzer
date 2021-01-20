from collections import namedtuple


#This way this method is called once in main method and initialzes the global variables once. After that it is only used

def init():
	global components 
	components = {}
	global vHash	# this will make searching for a control or source vertices easy
	vHash = {}
	global nestedseglist
	nestedseglist = {}
	
	
	global ctrlParentMap	#	each index contains -1 or an int representing its control parent
	
	global segParent	#	used to keep track of control blocks which has a nested segment; 
	segParent = []
	global ctrlSpan	#	Span of a ctr vrtx 'p' is the id of the vertex 'v' where 'v' is the last statement occurring in the prog and ctrl depen-
	ctrlSpan = {}	#	-dent directly or indirectly
	
# namedtuple is python equivalent to struct in C
	global stat 
	stat = namedtuple('stat','target producerList dEdgeList')				
#span-#all stmts directly/indirectly dpnd on it, type-(1-4), dependentList-All the stmts/blocks consuming data from it and should be extracted from, AttachedCtrList- parent ctrl blocks to extracted with it


	global ctrlStatHash	#	keeps various statistics about the control vertices
	ctrlStatHash = {}	#	Values of this hash will be of named tuple 'stat' type and keys will be control vertex ids
	global log
	
	
	global segment
	segment = {}
	global segList
	segList = []
	
	global flag
	#	flag contains flags for different options related to contents to be printed in log file and other operations in the file.
	#	like for this version sink vertices are not being considered but code is implemented so we can flag them.
	#	Sequence for options are as follows:
	#	InGr	:	Input Graph ; VeHa	:	Vertex Hash ; CtSp	Ctrl Span ; CtStHa	:	Ctrl Stat Hash
	#	GrSeEsc	:	Graph after Selective ESC	;	GrSeSdd :	Graph after selective SDDC
	
	flag = {}
