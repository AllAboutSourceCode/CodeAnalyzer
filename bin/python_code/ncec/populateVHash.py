import sys
import config
'''
	populates vHash (or vertexHash); it is a hash for control vertices and source vertices.
	It is called from main.py
	17 Dec, 2017
'''
def read_CtrlLoc(fin):
	infile = fin + "CtrlLoc"
	f = open (infile,"r")
	line = f.readlines()	#	Line contains whole file,
	count = int(line[0])	#	First Line
	tup = ();
	m = []	

	for i in range(0,count):	# Iterate from second line of the file till end
		elt = line[i+1].split()
		tup = (elt[1],elt[2])
		if elt[0]=='l' or elt[0] == 't' or elt[0]=='s':	#	't' represents try, s is for synchronized
			config.vHash[int(elt[1])] = 1	#1 for loop, 2 for if and so on
		else:
			if elt[0]=='i':		#	2 for if
				config.vHash[int(elt[1])] = 2
			else:
				if elt[0] == 'd':	#	3 for docase
					config.vHash[int(elt[1])] = 3
				else:
					if elt[0] == 'e' or elt[0] == 'c'or elt[0] == 'f':	#	
						config.vHash[int(elt[1])] = 4	#	For all secondary control vertices e.g., else, elseif, case, catch.
		m.append(tup)
	f.close()
	return m
	
	
def InsertSource(adj):		
	V = len(adj)
	for i in range(0,V):
		indeg = 0;
		outdeg = 0;
		for j in range(0,V):
			if adj[i][j]==2:
				outdeg = outdeg + 1
			if adj [j][i] == 2:
				indeg = indeg + 1
				break
		if indeg == 0 and outdeg > 0:
			config.vHash[i] = 9 # reflects the code for source vertex (not isolated)	
