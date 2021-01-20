import sys;
import copy;
'''
Program			:	Remove braces for block statements and place number of statements
Input			:	File path usually java file path
Output			:	A new IR file with no braces but span count
Created			:	20/10/2018
last modified	:	24 Oct, 2018
Version			:	2.0
			--------
Warning			:	Working for all test cases so far. [Oct 27, 2018]
				:-----------------------------------------------------------------------------
Test Report		:	IR is generated correctly, for all provided combinations
				:	Braces are removed and span of the control blocks are identified correctly.		
'''

def extractPath(filepath):	#	'filepath' is supposed to be of the form x/y/z.java	and output will be 'x/y/' and 'z.tk'
	args = filepath.split("/")
	filename = args[len(args)-1]
	path = ""
	for arg in args[:len(args)-1]:
		path+= arg
		path +="/"
	irname = filename.split(".")[0]+ ".tk1"
	return (path,irname)	#	path - path to the parent folder, irname - name of the java file but with extension 'tk'
	
def getBraceList(fcontent):	#	fcontent contains list of lines, each entry contains one line of the input file
	linecount = 0
	bracelist = []
	prev = ""
	for line in fcontent:
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
	
def augemntFContent(bracelist,fcontent):	
	size = len(bracelist)	
	i = 0
	doCount =0;
	blankblocks = []
#	print "Bracelist for scan : ", bracelist
	while (size>0):
		(orig1,loc1, br1,kwd) = bracelist[i]
		(orig2, loc2, br2,tmp) = bracelist[i+1]
		print orig1,loc1,orig2,loc2
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
			fcontent[orig1-1]+= " "+ str(stmtcount)
		#	update parent control block's coverage by adding offset to openning brace location
			offset = stmtcount +2

		#	print "> " , fcontent[orig1-1] 
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

def generateIR(fcontent,filepath,blankblocks):
	f = open(filepath[:len(filepath)-1],"w")
	ptr = 0	#	pointer for blankblocks
	size = len(fcontent)
	blanksize = len(blankblocks)
	#print "blankblocks", blankblocks
	for i in range(0,size):
		line = fcontent[i]
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
					f.write(s + " " + str(last+1)+"\ninvar\n")
					ptr+=1
				else:
					f.write(line + "\n")		
			else:		#	otherwise print as it is. fcontent is already been modified and span of blocks is written in it.
				f.write(line + "\n")
	f.close()
	
def remove():
	(path,fname) = extractPath(sys.argv[1])	
	filepath = path+fname
	f = open(filepath,"r")
	fcontent = f.read().split("\n")	#	split the content based on lines.
	f.close()
	bracelist = getBraceList(fcontent)	#	Read the IR file and return bracelist
	blankblocks = augemntFContent(copy.deepcopy(bracelist),fcontent)	#	Process bracelist and get spanlist
	#print blankblocks
	generateIR(fcontent,filepath,blankblocks)
		
def main():
	remove()
	print "Program exiting normally! :)"

main()
