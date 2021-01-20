import sys
import config
import graph
import copy
from collections import namedtuple

def populateCtrlStatHash(target, adj):
	dEdgeHash  = {}		# All data edges which belongs to the block; includes source input dependencies on the block too.
	producerHash = {}	# All data edges which are originated from block and used outside
	span = config.ctrlSpan[target]
	size = len(adj)
	for v in range(target, span + 1): # for every statement which is direct-indirect ctrl dependent on the target
		#get count of d-edges originating within the block
		if config.vHash.has_key(v) and config.vHash[v]<=4:
#			print "TCB: ctrl vertex",v
			continue	# vertex v is a control vertex
		else:
			for i in range(0,size):
# 				data edge originating from the statement 'v' in block rooted at 'target';
				if adj[v][i]== 2:
					dEdgeHash[v] = 1
					if i >span:	# True >> edge is originated inside but going out; thus consumed by outer statement/block
#						print "TCB: producer vertex :", v, "receiver :", i
						producerHash[v] = 1


#	dCount = float(len(dEdgeHash))
#	pCount = float(len(producerHash))
#	if dCount == 0 or pCount == 0:
#		xFactor = -1
#	else:
#		xFactor = pCount/dCount
#	print "Producer :", producerHash.keys(), "and span is :", span
	config.ctrlStatHash[target]= config.stat(target, producerHash.keys(), dEdgeHash.keys())

def AnalyseCtrlBlocks(adj, ctrList):
	computeCtrlSpan(ctrList)
	size = len(ctrList)
	config.log.write("\t Inside AnalyseCtrlBlocks()----\n")
	if size > 0:	# Checks if any control vertex is present in the program
		for index in range(size-1,-1,-1):	#	iterate in reverse order from 'size-1 to 0'
			target = int(ctrList[index][0])	#Get elements in reverse order
			if config.vHash[target] <= 4:	# for every control vertex
				populateCtrlStatHash(target, adj)
#	if config.flag['CtStHa']!=0:
#		config.log.write("\t\tCtrl Stat Hash: " + str(config.ctrlStatHash) +"\n")


def computeCtrlSpan(ctrList):#	span is the vertex id of the last direct/indirect control dependent stmt
	size = len(ctrList)
	for index in range(size-1,-1,-1):
		item = int(ctrList[index][0])
		span = int(ctrList[index][1]) + item
		i =index + 1
		while i <size and (int(ctrList[i][0])<= span):
			span = span + int(ctrList[i][1])
			i = i +1
		config.ctrlSpan[item] = span
#	if config.flag['CtSp']!=0:
#		config.log.write("\tctrlSpans" + str(config.ctrlSpan) + "\n")

def initSegments(size):
	l =[0]
	for i in range(0,size):
		l[0] = i
		config.segment[i] = copy.deepcopy(l)


'''
	At present we have not moved LoCS and PA metric thresholds to tuning.par file.
	#June 11, 2019
'''
def overrideParameters():
	 #  first two argumnets of sys.argv are (i) python file name (ii) path to input
	 #IR/Java file. Arguments begin from index 2 (after 0 and 1).
	size = len(sys.argv)
	index  = 2
	while index < size:
		if sys.argv[index] == "-relay":
			if sys.argv[index+1] == "0" or sys.argv[index+1] == "1":
				config.flag["NoPrEx"] = float(sys.argv[index+1])
			else:
				print "Invalid parameter for -relay, default value (0) is set"
				config.flag['NoPrEx'] = "0"
		index = index + 2

def initFlagOptions():
	updateParametersFromFile("options.op")	#This contains log related parameters and LoCS, PA
	updateParametersFromFile("tuning.par")	#This contains tuning parameters
	overrideParameters() #	This method will use the values passed as parameters for Relay flag and others
def updateParametersFromFile(inputFile):
	f = open(inputFile, "r")
	wholeFile = f.read()	#	Whole file as one string
	lines = wholeFile.split("\n")
	lines.remove(lines[len(lines)-1])
	size = len(lines)
	for i in range(0,size):
		opt = lines[i].split()
		config.flag[opt[1]] = float(opt[2])
	f.close()



def initCtrlParentMap(adj,ctrList):
	size = len(adj)
	config.ctrlParentMap = [-1]*size	#creates a list of size 'size' and initializes with -1
	for (v,x) in ctrList:
		v = int(v)
		for u in range(v,config.ctrlSpan[v]+1):
			if adj[v][u] ==1:
				config.ctrlParentMap[u] = v
	size = len(config.ctrlParentMap)
	for i in range(0,size):
		if i%15:
			config.log.write("\n")
		config.log.write(str(i)+ " : " + str(config.ctrlParentMap[i]) + ",")
def precomputations(adj, ctrList):
	config.log.write("\n-----Precomputations()--------\n")
	ctrlSpan = computeCtrlSpan(ctrList)
	AnalyseCtrlBlocks(adj, ctrList)
	initSegments(len(adj))
	initFlagOptions()
	initCtrlParentMap(adj,ctrList)
	config.log.write("[Tunining Parameters: LoCS : " + str(config.flag['ExTh']) +", PA : " + str(config.flag['PExTh']) + ", Flag : " + str(config.flag['NoPrEx']) + " ]")
