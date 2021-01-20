import sys;
import copy;
'''
Program			:	Remove braces for block statements and place number of statements
Input			:	File path usually java file path
Output			:	A new IR file with no braces but span count & other file 'File.map' with IR index to source code line number
Created			:	20/10/2018
last modified		:	7 June, 2019
Version			:	3.0
			--------
Appendix			:	This file is being modified in response to the newly found irregularity in IR index to
					source code line number mapping. All previous functionalities will remain same other than following:
						1. Each IR in input file contains a string X_Y_Z_W where, X and Z are line numbers, Y and W are columns
Modifications		:	To keep the working code untouched as much as possible, we are writing keyword "_" instead of "invar" for blank blocks.
					Further, this file will be called "File.tk2" and we introduce a method which will read ".tk2" file and will produce ".tk"
					Whenever, there is "$" keyword found in ".tk2" file, it will be replaced with "invar".

Note				:	Unlike earlier, now we are storing IR in an array of Strings, IR. After processing IR array, we write final result to file.
					Later the same IR file is used to read the content, and generate SDG. we can avoid timeconsuming process or generating IR and SDG
					by just avoiding them writing to files and pass the arguments into other modules.

'''
IR = []
sourceCodeLineNumberList = []	#Format of the list is W_X_Y_Z, where W and Y are line numbers and X and Z are columns

def extractPath(filepath):	#	'filepath' is supposed to be of the form x/y/z.java	and output will be 'x/y/' and 'z.tk'
	args = filepath.split("/")
	filename = args[len(args)-1]
	path = ""
	for arg in args[:len(args)-1]:
		path+= arg
		path +="/"
	return (path,filename)	#	path - path to the parent folder, irname - name of the java file but with extension 'tk'

def getBraceList(IRwithBraces):	#	IRwithBraces contains list of lines, each entry contains one line of the input file
	linecount = 0
	bracelist = []
	prev = ""
	for line in IRwithBraces:
		wordlist = line.split()#Assumes well formatted lines, no extra space or other characters
		#skip blank lines
		if (len(wordlist)==0):
			continue
		fw = wordlist[0]	#First word
		if (fw == '{'):
			bracelist.append((linecount,linecount,'{',prev.strip().split()[0]))
		elif fw == '}':
			bracelist.append(("",linecount,'}',""))
		linecount+=1
		prev = line
	return bracelist		#	returns a list of tuple

def augemntIRwithBraces(bracelist,IRwithBraces):
	size = len(bracelist)
	i = 0
	doCount =0;
	blankblocks = []
#	print "Bracelist for scan : ", bracelist
	while (size>0):
		(orig1,loc1, br1,kwd) = bracelist[i]
		(orig2, loc2, br2,tmp) = bracelist[i+1]
	#	print orig1,loc1,orig2,loc2
		if (br1 == '{' and br2 == '}'):
			#	initialize variables
			stmtcount = loc2-loc1-1
			if stmtcount ==0:	#	This block doesn't have any stmt
				blankblocks.append(orig1)
		#	check for dependent control blocks and accordingly increase the statement count of the control block
			if ( kwd == 'case'):
				doCount +=1	#	In this case increase the 'doCount' and process 'case' statement
			elif ((kwd == 'if' or kwd == 'elseif') and i+2 < len(bracelist)):	#	In this case inspect if another dependent control block is present at the end of the
				(o,l,b,w) = bracelist[i+2]			#-(end of the) present control block (e.g catch, finally, elseif or else)
				if (l==loc2+2 and (w =='else' or w == 'elseif')):	# Increase the span of 'kwd' by 1
					stmtcount+=1
			elif (kwd == 'try' or kwd == 'catch')and i+2 < len(bracelist):
				(o,l,b,w) = bracelist[i+2]
				if (l==loc2+2 and (w =='catch' or w == 'finally')):	# Increase the span of 'kwd' by 1
					stmtcount +=1
			elif (kwd == 'docase'):
			#	stmtcount+=doCount	#	Add doCount to span of 'docase'
				doCount =0		#	Initialize it again

		#	update range of the control block in the list; 'i' points to openning brace, so 'i-1' will point to control block keyword
			IRwithBraces[orig1-1]+= " "+ str(stmtcount)
		#	update parent control block's coverage by adding offset to openning brace location
			offset = stmtcount +2

		#	print "> " , IRwithBraces[orig1-1]
		#	remove the current pair of the brace entry
			bracelist.pop(i)	#	Mark both the entries, so that it can be removed easily
			bracelist.pop(i)	#	same is the case here as above; since 'i'th element is removed so, now 'i' points to next element.
			size-=2
			if i >0:
				j = i-1
				(orig,loc,br,kwd) = bracelist[j]
				while (br == '{' and j>=0):	#span for all parent block's will be shrinked by the offset by adding to location of openning brace
					(orig,loc,br,kwd) = bracelist[j]
				#	print "[",orig,loc,br,kwd,"] is changed to [ ",orig,loc+offset,"]"
					bracelist.insert(j, (orig,loc+offset,br,kwd))
					bracelist.pop(j+1)
					j-=1
			if i>0:
				i=i-1	#update value of 'i' only if i >0
		else:
			i+=1
	return blankblocks


#	populateIR(IRwithBraces, filepath,blankblocks) method populates IR-array with IR statements without braces ('{' or'}').
def populateIR(IRwithBraces, blankblocks):
	# f = open(filepath[:len(filepath)-1],"w")
	ptr = 0	#	pointer for blankblocks
	size = len(IRwithBraces)
	blanksize = len(blankblocks)
	#print "blankblocks", blankblocks
	for i in range(0,size):
		line = IRwithBraces[i]
		if len(line) ==0:
			continue
		line = line.strip()
		words = line.split()
		first = words[0]
		if first!='}' and first !='{':	#	if line is openning or closing braces don't do anything
			if blanksize > 0 and ptr<blanksize:		#	its possible that there are no blank blocks (i.e. if (x) {} )
				if i +1 ==blankblocks[ptr]:	#	if the block is empty or blank then have to increase span by 1 & add 'invar' to its body
					last = int(words[len(words)-1])
					s = " ".join(str(x) for x in (words[0:len(words)-1]))
				#	f.write(s + " " + str(last+1)+"\ninvar\n")
					IR.append(s + " " + str(last+1))
					IR.append("$")
					ptr+=1
				else:
				#	f.write(line + "\n")
					IR.append(line )
			else:		#	otherwise print as it is. IRwithBraces is already been modified and span of blocks is written in it.
			#	f.write(line + "\n")
				IR.append(line)
	# f.close()

'''
seperateIRandSourceCodeLineNumbers() separates IR statemnts and source code line numbers.
It stores source code line numbers in list called as 'sourceCodeLineNumberList'.
Both informations are separated by a '#'.
Once IR is separated next objective is to remove braces and augment control block range.
If a block does not have any statement within it, braceremover places an 'invar'  stateement in it.
In first phase such 'invar' is placed as a $ sign, after that when creating a mapping of IR index and
corresponding source code line numbers, $ sign is replaced with 'invar' and source line number for
control block and 'invar' is set to the same line number as of the control block.
'''
def seperateIRandSourceCodeLineNumbers(IRwithBraces):
	IRcontent = []
	size = len(IRwithBraces)
	for index in range(0,size):
		if len(IRwithBraces[index])>1:
			words = IRwithBraces[index].split('#')
			IRcontent.append(words[0])
			sourceCodeLineNumberList.append(words[1])
		else:
			IRcontent.append(IRwithBraces[index])
	return IRcontent

#Read the file.tk1; return the content of the file.
def read_tk1_file (path, fname):
	filepath = path+fname
	f = open(filepath,"r")
	IRwithBraces = f.read().split("\n")	#	split the content based on lines.
	f.close()
	return IRwithBraces

def writeIR2File(filename):
	f = open(filename[:len(filename)-1],"w")	# This will create a file with name "@@@@.tk"
	for line in IR:
		f.write(line +"\n")
	f.close()

def generateIRIndex2SrcCodeLineNumberMap(filename):
	f = open(filename,"w")
	invarCount = 0
	size = len(IR)
	for index in range(0,size):
		if IR[index]=='$':
			IR[index] = "invar"
			invarCount+=1
			f.write(str(index) + " " + sourceCodeLineNumberList[index-invarCount] +"\n")
		else:
			f.write(str(index) + " " + sourceCodeLineNumberList[index-invarCount] +"\n")
	f.close()


def remove():
	(path,filename) = extractPath(sys.argv[1])
	print "Path : ", sys.argv[1]
	fname = filename.split(".")[0]+ ".tk1"

	# Read the input file '@@@.tk1' and then seaprate the source code line numbers from IR statements
	IRwithBraces = seperateIRandSourceCodeLineNumbers(read_tk1_file(path, fname))	# For example, else#W X Y Z ====>>   else & W X Y Z
	bracelist = getBraceList(IRwithBraces)	#	Read the IR file and return bracelist
	blankblocks = augemntIRwithBraces(copy.deepcopy(bracelist),IRwithBraces)	#	Process bracelist and get spanlist
	# Refine IR by removing braces and adding control block ranges and then write it to global array 'IR'
	populateIR(IRwithBraces,blankblocks)

	# Before writing IR to file we need to find the $ in the IR and map the IR to source code line numbers.
	generateIRIndex2SrcCodeLineNumberMap(path+filename.split(".")[0]+ ".map")
	writeIR2File(path+fname)



def main():
	remove()
	print "Program exiting normally! :)"

main()
