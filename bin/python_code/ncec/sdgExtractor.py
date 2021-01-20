'''

Version		:	1.0.0
Program		:	This program parses the token file and generates a file with edge connections and label. That can be used to generate SDG.
				It handles 'proc', 'call', 'rcall' and 'return' keywords but does not establishes 'p-edge' between procedures.
				>	So, this program must not be used if the token file contains more than one procedure.

Input		:	Token file is provided as input. Extension is not added to command line argument. ".tk" is added by the program.

Output		:	Graph file is produced as output. First line shows no. of vertices and edges, rest of the lines shows edges <v1,v2>
			CtrlLoc_caseXX	file is produced with the format [ keyword startline statementCount ]
			Sometimes file does not produce any output, so, redirect output from terminal to file.


Last Modified	:	Dec 25, 2017, [27-10-2018 (Minor: added synchronized keyword)]

How to Run	:	python sdgExtractor.py path_to_input_file

NOTE		:	This file starts vertex from 0 unlike earlier versions

InterProcedural	:	This file considers that input file contains only one procedure but possibly some function calls and return statements.
					>	For this purpose function calls are considered as an "assignment" statement.
					>	If function call does not returns any value it is represented by 'call' keyword in token file otherwise 'rcall' is used
					>	whereas returns statement is treated as "output" statement.
					
'''
import sys, os

#-----------------Defining global variable------------------

parfile = ""


#-------------------------Stack Handling methods are here---------------------------
def pop_count(ctrldpndCount):
	global topcount 
	if topcount == -1:
		print "Stack Underflow"
		return -99
	topcount = topcount - 1
	return ctrldpndCount.pop()


def push_count(ctrldpndCount, value):
	global topcount
	topcount = topcount +1
	ctrldpndCount.append(value)

def pop_lineptr(ctrllineptr):
	global topptr 
	if topptr == -1:
		print "Stack Underflow"
		return -99
	topptr = topptr -1
	return ctrllineptr.pop()

def push_lineptr(ctrllineptr, value):
	global topptr
	topptr = topptr +1
	ctrllineptr.append(value)
		
#---------------------------Output handler--------------------
def output(wordlist, fout, defineTable,linecount): # Return behaves in a very same way as output
	global edges
	parFile.write(str(linecount)+ " ")
	for word in wordlist[1:]:
		value = defineTable.get(word)
		if value:
			fout.write (str(value-1) + " " + str(linecount-1) + " d\n")
			edges = edges + 1
		parFile.write(word + "@ ")
	parFile.write("\n")
#------------------Procedure call Handler----------------------
def call(wordlist, fout, defineTable,linecount):
	global edges
	global procTable
	global pEdgeList
	parFile.write(str(linecount)+ " ")
	for word in wordlist[2:]:
		value = defineTable.get(word)
		if value:
			fout.write (str(value-1) + " " + str(linecount-1) + " d\n")
			edges = edges + 1
		parFile.write(word + "@ ")
		parFile.write("\n")

#	fout.write (str(linecount-1) + " " + str(int(procTable[wordlist[1]]) - 1 ) + " p\n")
#	edges = edges + 1	
#	tupl = (str(linecount-1) + " " + str(int(procTable[wordlist[1]]) - 1 ))
#	pEdgeList.append(tupl)
	
#------------------Procedure call/rcall Handler----------------------
def rcall(wordlist, fout, defineTable,linecount):
	global edges
	global procTable
	global pEdgeList
	parFile.write(str(linecount)+ " ")
	for word in wordlist[3:]:
		value = defineTable.get(word)
		if value:
			fout.write (str(value-1) + " " + str(linecount-1) + " d\n")
			edges = edges + 1
		parFile.write(word + "@ ")
	parFile.write(wordlist[2] + "$ ")
	parFile.write("\n")
	defineTable[wordlist[2]] = linecount	#	update information of variable being defined in this line
		
#	code below is commented as  they establish p-edge between the procedure-vertices and are not to be addressed in this version	
#	fout.write (str(linecount-1) + " " + str(int(procTable[wordlist[1]]) - 1 ) + " p\n")
#	edges = edges + 1

#	tupl = (str(linecount-1) + " " + str(int(procTable[wordlist[1]]) - 1 ))
#	pEdgeList.append(tupl)
	
#------------------------Procedure Definition Handler-----------
procRange = {}
def proc(wordlist, fout, defineTable,linecount):
	global edges
	global procTable
	parFile.write(str(linecount)+ " ")
	print ">> ", wordlist[2:]
	
	defineTable.clear()	#	This clears all the old variable definitions at the beginning of each procedure.
	for word in wordlist[2:len(wordlist)-1]:	
		defineTable[word] = linecount	# update information of variable being defined in this line
		parFile.write(word + "$ ")
	procRange[linecount] = wordlist[len(wordlist)-1]
	parFile.write("\n")
#---------------------------Assign Handler----------------------
def assign(wordlist, fout, defineTable,linecount):
	global edges

	parFile.write(str(linecount)+ " ")

	for word in wordlist[2:]:
		value = defineTable.get(word)
		if value:
			fout.write (str(value-1) + " " + str(linecount-1) + " d\n")
			edges = edges + 1
		parFile.write(str(word)+"@ ")	#	variable is used

	parFile.write(str(wordlist[1])+"$ ")
	parFile.write("\n")

	defineTable[wordlist[1]] = linecount	#	update information of variable being defined in this line

#-------------------------------Input Handler------------------------
def input(wordlist, defineTable, linecount):
	parFile.write (str(linecount) + " ")
	for word in wordlist[1:]:
		defineTable[word] = linecount

		parFile.write(str(word)+"$ ")	#parameter writing
	parFile.write("\n")			#parameter writing
	
#-----------------------------------All cases are handled here-------------------------------------
def HandleCases(ctrldpndCount, ctrllineptr, wordlist, fout, defineTable, linecount, listCtrlLoc):

	global edges
	global topcount
	global topptr
	global procTable
	
	if topcount >=0:	#	There is an entry in stack.
		value = pop_lineptr(ctrllineptr)
		fout.write (str(value-1) + " " + str(linecount-1) + " c\n")
		edges = edges + 1
		#	Draw an edge bw source control statement to dependent (Above). Decrease the count (below).
		count = pop_count(ctrldpndCount)
		if count > 1:
			push_count(ctrldpndCount, count-1)
			push_lineptr(ctrllineptr, value)
		
	option = wordlist[0]	
	if option == "output" or option == "return" or option =="use":
		output(wordlist, fout, defineTable, linecount)
	else:
		if option == "assign":
			assign(wordlist, fout, defineTable,linecount)
		else:
			if option == "if" or option == "else" or option == "elseif" or option == "loop" or option == "docase" or option =="case" or option == "try" or option == "catch"or option == "finally" or option == "synchronized":
				
				string = wordlist[0][0] + " " + str(linecount-1) + " " + wordlist[len(wordlist)-1]
#				print string	#	If there is some issue in reading ctrl statement location, uncomment it
#				if option == "loop" or option == "if" or option == "docase":###	condition is extended for 'if'&'docase' in Ver. 1.0.1
				listCtrlLoc.append(string)
				

				stmtcount = int(wordlist[len(wordlist)-1])	#	Count of control dpnd stmts 
				push_count(ctrldpndCount, stmtcount)
				push_lineptr(ctrllineptr, linecount)
				
				parFile.write(str(linecount)+ " ")	
				for word in wordlist[1:len(wordlist)-1]:
					value = defineTable.get(word)
					if value:
						fout.write (str(value-1) + " " + str(linecount-1) + " d\n")
						edges = edges + 1
					parFile.write(word + "@ ")
				parFile.write("\n")
			else:
				if option == "input":
					input(wordlist, defineTable, linecount)
				else:
					if option == "call":
						call(wordlist, fout, defineTable,linecount)
					else:
						if option == "rcall":
							rcall(wordlist, fout, defineTable,linecount)	# call with return
						else:
							if option == "proc":
								proc(wordlist, fout, defineTable,linecount)	# argument list is same as assign
						
#------------Get line number of procedure definitions-------------------------------------
def ReadProcLine(finname):
	command = 'grep ^proc -n ' + finname + " > proc_loc"
	os.system(command)

#---------------------------------Initialize ProcTable------------------------------------

def InitProcTable(finname):
	global procTable
	ReadProcLine(finname)
	fin = open("proc_loc","r")
	for line in fin:
		wordlist = line.split()
		word = wordlist[0]
		words = wordlist[0].split(":")
#		print int(words[0]) +100
		procTable[wordlist[1]] = words[0]
	fin.close()
	os.remove("proc_loc")

#-------------------------Generate SDG File (Input to Segmentation Algo)-------------------
def GenerateSDGFile(foutname, linecount):

	global edges
	
	fin = open("tmp","r")
	fout = open(foutname,"w")
	fout.write(str(linecount) + " " + str(edges) +"\n")
	for line in fin:
		fout.write(line)
	fin.close()
	fout.close()
	os.remove("tmp")
#---------------------------Read IR and extract data and control edges---------------------
def ExtractSDG(foutname, listCtrlLoc):
	global procTable	#	It contains the list of procedure definitions; Its a dictionary in python	
	global topcount
	global topptr		#	Will be used for both ctrldpndCount and ctrllineptr
	global edges

	defineTable = {}	#	It contains the list of variables defined so far
	
			#	Following two lists are implemented as STACK.
	ctrldpndCount = []	#	Contains the number of control dependent statements on specific statement
	ctrllineptr = []	#	Contains the line number of the IF/ElseIf/Else/loop statements in segment IR

	finname = foutname + ".tk"
	fin = open(finname,"r")
	fout = open ("tmp","w+")
	linecount = 0
	
	for line in fin:
		wordlist = line.split()
		if len(wordlist) <= 0:		#	Skip all blank lines
			continue
			
		linecount = linecount + 1
		HandleCases(ctrldpndCount, ctrllineptr, wordlist,fout,defineTable,linecount, listCtrlLoc)

	fin.close()
	fout.close()
	
	GenerateSDGFile(foutname, linecount)	
#-------------------------------Generating Control Locations File-------------------------
def WriteCtrlLoc(foutname, listCtrlLoc):
	#	Write locations of control characters in file.
	string = foutname + "CtrlLoc"
	fout = open ( string ,"w")
	fout.write(str(len(listCtrlLoc)) + "\n")
	for string in listCtrlLoc:
		fout.write(string + "\n")
	fout.close()
#--------------------- p-edges are written in form of end vertex tuple--------------------
def WritePEdges2File(foutname):
	fout = open(foutname + "pEdgeList","w")
	fout.write(str(len(pEdgeList))+"\n")
	for tupl in pEdgeList:
		fout.write(tupl + "\n")
	fout.close()
#-----------------------Write range of each procedure in the file-------------------------	
def WriteProcRange2File(foutname):	
	fout = open(foutname + "ProcRange","w")
	fout.write(str(len(procRange))+"\n")
	for key in procRange.keys():
		fout.write (str(key) + " " + str(procRange[key]) + "\n" )
	fout.close()
#---------------------------------------Mainmethod()--------------------------------------
def extractSDG(fpath):	
	global pEdgeList
	global procTable	#	It contains the list of procedure definitions; Its a dictionary in python	
	global topcount
	global topptr		#	Will be used for both ctrldpndCount and ctrllineptr
	global edges

	global parFile 	#	File name for writing variables for parameter generation for newly created functions

	pEdgeList = []
	procTable = {}
	topcount = topptr = -1
	edges = 0

	foutname = fpath
	listCtrlLoc = []

	print "sdgExtractor.py : execution begins"
	#parFile = foutname + "_parmtr"

	finname = foutname + ".tk"

	InitProcTable(finname)

	parFile = open (foutname + "_parmtr", "w")
	ExtractSDG(foutname, listCtrlLoc)
	parFile.close()

	WriteCtrlLoc(foutname, listCtrlLoc)

	#WritePEdges2File (foutname)

	#WriteProcRange2File (foutname)

#=============================================================================================
'''
def main():
	extractSDG(sys.argv[1])
	
if __name__=="__main__":
	main()


Update History

21 Sept	:
	1) Inserted switch case handler
27 Oct, 2018:
	1)	Inserted synchronized keyword
	2)	Modified loop range in method proc 'wordlist[2:len(wordlist)-1]' to wordlist[2:] to cover all the parameters with the proc statement
	3)	Another change is new method extractSDG is created and global part of the code is moved into it. Also, main() is created.

'''
