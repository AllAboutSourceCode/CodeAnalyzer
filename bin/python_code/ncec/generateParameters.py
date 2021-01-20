import config
import graph
import copy

'''
	This code is written for identification and generation of input/output parameters and local variable for the extracted segments.
	We will use config.segment, config.seglist collections and input_parmtr file for extracting the information about variables usage and definition history. 
	
	
NOTE:	IR index of each variable's definition/usage starts from 1 not from 0. Thus, must be reduced by one when reading.

Input data to be used:

xyz_parmtr  :   It contains the variable usage and definition history.


'''
varUsage = {}	#	It contains the list of variables and the vertices where they are used.
varDefinition = {}	#	It contains the list of variables and the vertices where they are defined.
varHistory = {} #   Contains all kind of access to variables present in file <XYZ>_parmtr 
indexOfMethodsInIR = [] #   Contains beginning of each method: Last entry in the list is end boundary of last method
segmentMap = {} #   A dictionary which stores segment list corressponding to each segId.


#----Method for reading method boundaries in the input IR------#
# This method reads all the method's begining line in IR file. Also, the counting begins from ZERO. Thus, the IR-index will be reduced by One while reading

def readMethodIndicesFromIRFile(filepath):
    global indexOfMethodsInIR
    fp = open(filepath+".tk","r")
    tkFileContent = fp.read().split("\n")
    # irStatements = tkFileContent.split()
    linecount = 0
    for line in tkFileContent:
        keywords = line.split()
        if len(keywords) >0 and keywords[0]=='proc':
            indexOfMethodsInIR.append(linecount)
        linecount = linecount +1
        
    indexOfMethodsInIR.append(linecount)  #End boundary of the IR file OR end boundary of the last method in the file. 
    fp.close()



#	This function reads the file "<XYZ>_parmtr" and populates the dictionaries "varUsage" and "varDefinition"
#   Also, it will populated the varHistory dictionary, where key is IR-index (starting from ZERO i.e. one less than what is recorded in the file)

def readVariableHistory(filepath):
	global varUsage		#	keyword global is used because we will modify the dictionary
	global varDefinition
	fp = open(filepath+"_parmtr","r")
	for line  in fp:
		words = line.split()
		if len(words)>1:
			ir = int(words[0])-1
			for var in words[1:]:
				last = len(var)-1
				if var[last] == '@':
					if var[:last] in varUsage:
						varUsage[var[:last]].append(ir)
					else:
						varUsage[var[:last]] = [ir]
				elif var[last] == '$':
					if var[:last] in varDefinition:
						varDefinition[var[:last]].append(ir)
					else:
						varDefinition[var[:last]] = [ir]
				else:
					print "Unknown variable detected", var
			varHistory[ir] = words[1:]	#This will store the variable history as it is present in the file.
	fp.close()


#   Create a new hash table which contains entry for only extracted segments; key-segId and vlaue-IR indices of the segment
def readSegmentsMap():
    global segmentMap
    for segId in config.segList:
        segmentMap[segId] = copy.deepcopy(config.segment[segId])


#	This method generates the input arguments and local variables for the block
def computeInputAndLocalArgumentsOfBlock(segment):
	defined = set()
	inputArg = set()
	segment.sort()	#	Elements in the segment are not always sorted
	first = segment[0]
	last = segment[len(segment)-1]
	for v in segment:	#	For every IR-index in the statement inspect the variables used inside
		if varHistory.has_key(v):	#	if corressponding IR statement accesses any variable
			variables = varHistory[v]	#	get the list of variables 
			for var in variables:	#read each variable used/defined at the IR-index,
				size = len(var)
				if (var[size-1]=='@'):
					var = var[:size-1]
					if var not in defined:	#	'var' is used before it is defined in the segment;
						inputArg.add(var)	#	hence, it is inputArgument
				elif(var[size-1]=='$'):		#	'var' is being defined
					defined.add(var[:size-1])
	localVars = defined-inputArg
	return (inputArg,localVars, defined)
#	This method generates the input arguments and local variables for the segment corresponding to segId	
def findInputAndLocalArguments(segId):
	segment = segmentMap[segId]
	(inputArg,localVars,definedVars) = computeInputAndLocalArgumentsOfBlock(segment)	
	return (inputArg,localVars)


#-----------------------------	
def findReturnArguments(segId, methodEnd, localVarOfSegment):
	returnArguments = set()
	segment = segmentMap[segId]
	segment.sort()
	segmentEnd = segment[len(segment)-1]

	successorBlockOfSegment = []	#	All the indices/IR statements which are present after the segment and before the method ends
	for irIndex in range(	segmentEnd+1,methodEnd):
		successorBlockOfSegment.append(irIndex)
	
	(inputArgs, localVars, definedVars) = computeInputAndLocalArgumentsOfBlock(successorBlockOfSegment)
	
	returnArgumentsOfSegment = inputArgs & definedVars
	return returnArguments

# A method to concatenate all the kind of variables/parameters for the segment/EMO into a formatted string

def concatenateAllArguments(seg, inputArgs, localVars, returnArgs):
    arguments = ""
    arguments += str(seg) +" : "    #   Begin with segmentId and the seaprator symbol ":"
    if not inputArgs is None:
        for arg in inputArgs:    #   Add all the input parameters present in the segment
            arguments += arg + " "
    arguments +=": " #Its a separator symbol
    if not localVars is None:
        for arg in localVars:    #Add all the Local variables present in the segment/EMO
            arguments += arg + " "
    arguments+= ": "    #   Add a separator symbol 
    
    if not returnArgs is None:
        for arg in returnArgs:    #   Add all the return variables are present in the corresponding segment/EMO
            arguments+= arg + " "
    return arguments


#   This method returns the beginning and ending location of the method containing the segId

def getMethodBoundaries(segId, currentMethodIndex):
    size = len(indexOfMethodsInIR)-1    #Last entry is not a method beginning but end of the IR File; Hence, One is reduced.
    if (currentMethodIndex ==size):
        currentMethodIndex = 0
    begin = indexOfMethodsInIR[currentMethodIndex]
    end = indexOfMethodsInIR[currentMethodIndex+1]    
    while (not (segId >= begin and segId < end)):    #check against Infinite-loop
        currentMethodIndex = (currentMethodIndex+1)%size
        begin = indexOfMethodsInIR[currentMethodIndex]
        end = indexOfMethodsInIR[currentMethodIndex+1]-1	#After reducing One it will point to last IR-statement in the corressponding method
    return (currentMethodIndex, begin, end)

#-------------------------------------------------------------
def RemoveTempVars(inputArgs):
	outputVar = set()
	for var in inputArgs:
		strings = var.split("_")
		size = len(strings)
		if size>1 and unicode(strings[size-1], 'utf-8').isnumeric():
			tmp = strings[0]
			for next in strings[1:size-2]:	#Last string was added by IR-generator; if the user has a var like name_11 then the output is wrong
				tmp+="_"+next
			outputVar.add(tmp)
		else:
			outputVar.add(var)
	return outputVar

#	This method computes the arguments for all the segments 
def computeArguments(filepath):
	fp = open(filepath+".arguments","w")
	currentMethodIndex = 0
	seglist = config.segList
	seglist.sort()
	for seg in seglist:
		arguments = ""
		(currentMethodIndex, methodBegin, methodEnd) = getMethodBoundaries(seg, currentMethodIndex)
		(inputArgs,localVars) = findInputAndLocalArguments(seg)
		returnArgs = findReturnArguments(seg,methodEnd,localVars)
		
		inputArgs = RemoveTempVars(inputArgs)	#	It removes temporary vars added at the time of IR-generation.
		localVars = RemoveTempVars(localVars)
		returnArgs = RemoveTempVars(returnArgs)

		arguments = concatenateAllArguments(seg, inputArgs, localVars,returnArgs)
		fp.write(arguments +"\n")
		config.log.write(str(seg) + arguments + "\n")
		

	#	print "After Temp Var removal; For seg ", seg, "\n Input arguments ", inputArgs, "\n and Local vars : ", localVars, "return vars : ",returnArgs
	
	fp.close()

#-----------------------------				
def generate(adj,filepath):
	segmentArgumentHistory ={}
	
	readMethodIndicesFromIRFile(filepath)
	readVariableHistory(filepath)
	readSegmentsMap()
	computeArguments(filepath)
#-------End	of the	Code-----------#	
