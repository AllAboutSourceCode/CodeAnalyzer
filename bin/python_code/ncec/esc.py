'''
	Exclusive Source/Sink Edge Contraction
	It merges the source/Sink vertices to the 'target' vertex (generally a new vertex resulted from control edge contraction)
	Last Modified	:	17.12.17
'''

import sys
import graph
import config

def selectiveESC(target, adj):
	size = len(adj)
	parent = graph.get_ctrl_parent(target, adj)
	tmp = ""	#	used for log file
	if parent == -1:
		parent = 0
	for u in range(parent, target):	#	Source vertices
		if adj[u][target]==2 and graph.isExcSource(u,adj) and config.ctrlParentMap[u] == config.ctrlParentMap[target]:
			adj = graph.edge_contraction(u,target,target,adj)
			tmp = tmp + " " + str(u)
			
	if len(tmp):
		config.log.write("\tAll Exclusive source at " + str(target) + " are " + str(tmp) +"\n")
	else:
		config.log.write("\tNo Exclusive Source at " + str(target)+ "\n")

	return adj		
	
#	if parent<0:	#	We are not addressing 'sink' for now
#		return
#	tmp = ""
#	span = config.ctrlSpan[parent]
#	for u in range(0,span):	#	Sink vertices
#		if adj[target][u]==2 and graph.isExcSink(u,adj):
#			adj = graph.edge_contraction(target,u,target,adj)
#			tmp = tmp + " " + str(u)
#	config.log.write("\t All Exclusive sink at " + str(target) + " are " + str(tmp) +"\n")
	
#----------------$------------------%---------------^--------------@--------------------#

def esc(adj):	#	Global ESC is applied when there is no control edges are present; Thats why range varies 0 to len(adj)
	vrange = len(adj)
	config.log.write("----------------------------------------ESC---------------------------------------------\n")
#	graph.display(adj)
#	print config.segment
	for target in range(0, vrange):
		tmp1 = ""
#		tmp2 = ""		#	Sink is not being addressed in this version
		for v in range(0,vrange):
			if adj[v][target]==2 and graph.isExcSource(v,adj):	# <v,target> is d-edge and v is exclusive source vertex 

				adj = graph.edge_contraction(v,target,target,adj)
				tmp1 = tmp1 + " " + str(v)
#			if config.flag['SiVe'] and adj[target][v]==2 and graph.isExcSink(v,adj):# <target,v> is d-edge and v is exclusive sink vertex 
#				adj = graph.edge_contraction(target,v,target,adj)
#				tmp2 = tmp2 + " " + str(v)
		if len(tmp1) >0:		
			config.log.write("\tAll Exclusive source at " + str(target) + " are " + str(tmp1) +"\n")
#		if len(tmp2) > 0:
#			config.log.write("\tAll Exclusive sink at " + str(target) + " are " + str(tmp2) +"\n")
	config.log.write("-----------------------------------End of ESC-------------------------------------------\n")
	return adj

