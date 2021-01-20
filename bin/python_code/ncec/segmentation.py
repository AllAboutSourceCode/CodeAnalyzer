'''
	This file contains code for segmentation approach
	1. Visit all the primary control vertices in reverse order of their occurance in the program.
	2. Check if the selected control vertex is qualifies for extraction.
		a. Yes	:	apply exclusive source and sink contraction of degree one.
					apply sequential data dependence edge contraction reaching and leaving from 'target'.
					evaluate the parent for extraction with 'target' and if it qualifies repeat the steps.
					if parent doesn't qualifies return 'parent'
		b. No	:	return next in queue primary control vertex to be tested.			

'''

'''---------More to Add to NCEC/Segementation--------------
	1. if outermost control block is not extracted compress it to one single node;
	and store its state in 'controlstathas'.
	2. After all 3 stages has been applied (ncec,esc,sddc) nodes formed in 3rd stage (i.e. sddc) are re 

'''


'''----------Answers to find------------------

	1.	When we merge a chain or source to inner control block, do we need to take only those vertices into account who share the same parent control block as the inner control block or it does not alter the output.
	2.	In chain we consider the branch in terms of only immediate source branch, why not also consider immediate sink branch.
	
	3. how to break the object name with variable name or breaking the variable names formed with '.'; specially in Java or other object oriented language
	
'''

import sys
import config
import graph
import ncec
import sddc
import esc
'''
def segmentation (adj, ctrList):
	sddc.selectiveSDDC(3,adj)

'''
#---------------------------------------------------------------------------
def segmentation(adj,ctrList):
#	config.log.write("\n--------Inside Segmentation--------\n")
	adj = ncec.ncec(adj,ctrList)
	if config.flag['GrNc']!=0:
		config.log.write("\n---------Graph Post NCEC--------\n")
		graph.write(adj, config.log)
	
	adj = esc.esc(adj)	#if argument is  -1, then have to apply it on whole graph
	if config.flag['GrEsc']!=0:
		config.log.write("\n--------Graph Post ESC--------\n")
		graph.write(adj, config.log)

	
	adj = sddc.sddc(adj)	#if argument is -1, then have to apply it on whole graph
	if config.flag['GrSdd']!=0:
		config.log.write("\n--------Graph Post SDDC--------\n" )
		graph.write(adj, config.log)

	
	if config.flag['SeGr']!=0:
		config.log.write("\n\n-----------------------------------------------------------------------\n\n")
		config.log.write("Segment Graph is as follows: ")
		config.log.write(str(config.segment))
	
	if config.flag['SeLi']!=0:
		config.log.write("\nList of segments extracted are as follows: ")
		config.log.write(str(config.segList))

	return adj
