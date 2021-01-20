import os
import sys
import config

#----------------------------------------------------
def createVMap(adj):
	vmap = []
	V = len(adj)
	for i in range(0,V):	# Initialize vmap; each vertex is mapped to itself
		vmap.append(i)
	
	#Map vertices which has been associated with edge contraction		
	for seg in components.keys():
		vmap[seg] = seg
		for v in components[seg]:
			vmap[v] = seg
	return vmap

def insertNEdges2Adj(adj,vmap):
	#add nested edges to adj
	for child in nestedseglist.keys():
		parent = vmap[nestedseglist[child]]
		adj[parent][child] =  3	#Nested Edge

def insertPEdges2Adj(adj,vmap):

	#update adj with p-edges
	for tpl in pEdgeList:
		(call,proc) =  tpl
		v1 = vmap[int(call)]	# get segment which contains vertex corresponding to 'call'
		v2 = vmap[int(proc)] # get segment corresponding to called proc
		adj[v1][v2] = 4 #P-edges
#----------------------------Generate output files----------------------------------------
def dump_output(adj, infile):

	vmap = []	# create vmap; mapping of a vertex to its segment
	
	vmap = createVMap(adj)	
	insertNEdges2Adj(adj,vmap)
	insertPEdges2Adj(adj,vmap)

#	dump nestedseglist and components in file
	dumpfile = infile + "_dump"
	f = open(dumpfile,"w")
	f.write("Nested Segment List \n")
	f.write(str(config.nestedseglist))
	f.write ("\nComponents are : \n")
	f.write(str(config.components))
	f.close();
#-----Following code create one more file containing all the nodes in one line forming a segment	

#	f = open(infile + "_component", "w")
#	for seg in cofig.components.keys():
#		f.write(str(seg) + " ")
#		for v in config.components[seg]:
#			f.write(str(v) + " ")
#		f.write("\n")
#	f.close()
	
#	generate segment graph dot file	
#	generate_dot(adj,infile)
	
#------------------------------------------------------------------------------
def remove_help_files(string):
	name = string + "CtrlLoc"
	os.remove(name)
	name = string + "_parmtr"
	os.remove(name)
		
