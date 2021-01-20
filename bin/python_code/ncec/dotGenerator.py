import config

def generate_dot(adj,path):
	path = path + "_ncec.dot"
	f = open (path,"w")	
	f.write("Digraph G {\n")
	vList = []	#This list contains only those vertices which are isolated and not connected with any edge
	V = len(adj)
	for v in range(0,V):
		vList.append(v)	#Initially every vertex is assumed to be isolated
	#Write all vertices labels in dot (This way isolated vertices also appear in segment graph)
	for v in config.components.keys():
		line = str(v) + ' [label="' + str(v) +'"]\n'
		f.write(line)
		
		vList[v] = -1	#mark segment 'v' for not to be written in dot file additionally
		seg = config.components[v]
		for i in seg:	#mark all the vertices present in the segment 'v'
			vList[i] = -1

	for v in vList:		#This handles isolated vertices (write in dot file) ; specially non-bounded 'invar' statements
		if (v>0):
			line = str(v) + ' [label="' + str(v) +'"]\n'
			f.write(line)
						
	#Read adj and write all edges present in the segment graph
	V=len(adj)
	for i in range(0,V):
		for j in range (0,V):
			if adj[i][j]==2:
				line = str(i) + ' -> ' + str(j) + ' [label="D"]\n'
				f.write(line)
			else:
				if adj[i][j] == 3:
					line = str(i) + ' -> ' + str(j) + ' [label="N"]\n'
					f.write(line)
				else:
					if adj[i][j] == 1:
						line = str(i) + ' -> ' + str(j) + ' [label="C"]\n'
						f.write(line)
					else:
					 	if adj[i][j] == 4:
							line = str(i) + ' -> ' + str(j) + ' [label="P"]\n'
							f.write(line)

	f.write("}")
	f.close()
