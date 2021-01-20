package analyseMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
/*
 * This class takes a statement and puts it into the list. Further, if the statement comprises other statements within
 * the block then those are recursively, extracted and added to the list. Further, to support the extraction of EMO,
 * we need to make map of the line number to statements, Thus the list of extracted
 * statements is used to generate a hash table. 
 */

public class StatementExtractor {

	//This method extracts 'Statements' from input method and place it into a corresponding EMO. 
	@SuppressWarnings("unchecked")
	public void ComputSourceStatements(EMO emo, MethodDeclaration methodDeclaration, CompilationUnit cu) {
		//Get the statements of the method body. Statements/blocks enclosed within other statement will require further extraction
		List<Statement> methodStatementList = new ArrayList<>();
		methodStatementList.addAll(methodDeclaration.getBody().statements());//All the outer blocks/statements of the method.
		int methodStatementIndex = 0;	//Method statement Index
		//Get the line number for statements that are required to be moved as an EMO
		ArrayList<Integer> emo2beExtracted = emo.getStatements();//Line numbers of the statements to be extracted.
		Collections.sort(emo2beExtracted);
		int emoIndex = 0;	//	EMO statement Index		
		int emoSize = emo2beExtracted.size();
		int tobeExtracted;	//Line Number of first statement to be extracted.
		//get the first statement in the methodStatementList
		Statement statement = methodStatementList.get(methodStatementIndex);
		int statementLineNumber = cu.getLineNumber(statement.getStartPosition());	//Line Number of first statement in the Method
		while (emoIndex<emoSize) {
			tobeExtracted = emo2beExtracted.get(emoIndex);
			
			if (tobeExtracted == statementLineNumber) {	//Match, add this statement to EMO
				emo.addSourceStatements(statement);	//Adding to the EMO

				//   Now, skip 'emoIndex' till the beginning of next statement Line Number in the methodStatementList
				//	As those statements have already been added (case of a block statement)
				methodStatementIndex++;	//points to next statement of the methodStatementList
				
				if (methodStatementIndex< methodStatementList.size())
					statementLineNumber = cu.getLineNumber(methodStatementList.get(methodStatementIndex).getStartPosition());
				else
					break;	//No more statements are left
				while(tobeExtracted < statementLineNumber) {
					emoIndex++;	//Move to next EMO statement
					if (emoIndex<emoSize)
						tobeExtracted = emo2beExtracted.get(emoIndex);	//Get next EMO Statement to be extracted
					else
						break;
				}
			}
			else {
				/*
				 * 			Locate the statement/block corresponding to 'tobeExtracted'.
				 * If 'tobeExtracted' is within a block then we need to extract inside 
				 * statements and add those to methodStatementList.
				 * 			In other words, skip 'methodStatementIndex' until it points to the statement
				 * that is to be extracted i.e. 'emoIndex' and 'methodStatementIndex' points to
				 * the same statement.
				 * 			On both entry points to the else block, following variables will have some values:
				 * emoIndex, tobeExtracted, methodStatementIndex, statementLineNumber.
				 * 			Now, value of 'tobeExtracted' can NEVER be less than the value of statementLineNumber as 
				 * 'methodStatementIndex' will point to the first statement at the beginning. 
				 */
				//Get Line number of statement next to the statement pointed by 'methodStatementIndex'
				int nextStatementLineNumber = cu.getLineNumber(methodStatementList.get(methodStatementIndex+1).getStartPosition());
				//Keep increasing the 'methodStatementIndex' until it points to the same statement as 'tobeExtracted'
				while(tobeExtracted!= statementLineNumber) { 
					
					/* 
					 * while(nextStatementLineNumber<tobeExtracted && presentIndex < methodStatementList.size())
					 * {	presentIndex = nextIndex	
					 * 		statementLineNumber = nextStatementLineNumber;
					 * 		nextStatementLineNumber = .....;
					 * }
					 */

					while(nextStatementLineNumber < tobeExtracted && methodStatementIndex <methodStatementList.size()) {
						statementLineNumber = nextStatementLineNumber;
						methodStatementIndex++;	//It points to current statement
						if (methodStatementIndex+1 < methodStatementList.size())
							nextStatementLineNumber = cu.getLineNumber(methodStatementList.get(methodStatementIndex+1).getStartPosition());
						else {
							nextStatementLineNumber = -1;	//Represents end of the method or next can be found.
							break;
						}
					}
					/* if (nextStatementLineNumber==tobeExtracted){
					 * 		presentIndex = nextIndex
					 * }
					 * else if (Inbetween(tobeExtract,present, nextStatementLineNumbear))	{
					 * 			extract inner statements of the block and insert them so that statements in the list
					 * 		are in ascending order. Add the contents after the first and before the next.
					 * }
					 * else if(OR presentIndex==methodStatementList.size())	{
					 * 			Add the content of the block at the end of the list
					 * 	}	*/
					if (nextStatementLineNumber==tobeExtracted) {
						statementLineNumber = nextStatementLineNumber;
						methodStatementIndex++;
					}
					else if(InBetween(tobeExtracted,statementLineNumber,nextStatementLineNumber)) {
						List<Statement> list;
						list = getStatementList(methodStatementList.get(methodStatementIndex));
						methodStatementList.addAll(methodStatementIndex+1, list);
						methodStatementIndex++;	//Now it points to first newly added statement 
						statementLineNumber = cu.getLineNumber(methodStatementList.get(methodStatementIndex).getStartPosition());
						nextStatementLineNumber = cu.getLineNumber(methodStatementList.get(methodStatementIndex+1).getStartPosition());		
					}
					else if (methodStatementIndex==methodStatementList.size()) {
						List<Statement> list;
						list = getStatementList(methodStatementList.get(methodStatementIndex));
						methodStatementList.addAll(list);
						methodStatementIndex++;	//Now it points to first newly added statement 
						statementLineNumber = cu.getLineNumber(methodStatementList.get(methodStatementIndex).getStartPosition());
						nextStatementLineNumber = cu.getLineNumber(methodStatementList.get(methodStatementIndex+1).getStartPosition());
					}
					else {
						System.out.println("Unaccounted case has occurred...StatementExtractor()\n");
						System.out.println("tobeExtracted :" +tobeExtracted+ "\n nextStatementLineNumber " +nextStatementLineNumber);
						System.out.println("\nstatementLineNumber " + statementLineNumber);
						//TODO: This error has occurred for one of the class in XData, for which we would not be able to extract statements
//						System.exit(0);
					}
					

				}//End of while(tobeExtracted!=statementLineNumber)
			}//End of Else
			
		}//End of while(emoIndex<emoSize)
			
		
		
	/*	for (; emoStatementIndex<emoLineNumbers.size(); ) {
			emoLineNumber = emoLineNumbers.get(emoStatementIndex);
			//escape method statements not in the EMO
			//Escape only when current element is at lower line Number than 'line' and next element is not lower than the 'line'
			while ( emoLineNumber > methodStatementLineNumber && methodStatementIndex+1 < blockStatementList.size()&& 
					emoLineNumber >= cu.getLineNumber(blockStatementList.get(methodStatementIndex+1).getStartPosition()))
			{	//Its easy to understand the predicates if the first variable in comparison is the loop index
				methodStatementIndex++;
				stmt = blockStatementList.get(methodStatementIndex);	
				methodStatementLineNumber = cu.getLineNumber(stmt.getStartPosition());
			}
			//	
			if (emoLineNumber == methodStatementLineNumber) { // Add this statement/block to EMO
				emo.addSourceStatements(stmt);	//Add the statement/block
				methodStatementIndex++;	//Move to next statement
				stmt = blockStatementList.get(methodStatementIndex);	
				methodStatementLineNumber = cu.getLineNumber(stmt.getStartPosition());
				emoStatementIndex++;
				emoLineNumber = emoLineNumbers.get(emoStatementIndex);				
				//Now we will need to skip the lineNumbers which are covered in the above statement added to emo.addSourceStatements()
				if (emoStatementIndex==emoLineNumbers.size()|| methodStatementIndex == blockStatementList.size()) {
					//If either of the list is finished then all the statements should be added to the EMO
					break; 
				}
				else {
					for (;emoLineNumber<methodStatementLineNumber &&emoStatementIndex< emoLineNumbers.size(); emoStatementIndex++ ) {
						emoLineNumber = emoLineNumbers.get(emoStatementIndex);
					}	
				}
			}
			else {
				System.out.println("StatementExtractor.java: Else part");
				// method statement is a block-statement; Will need to break it and then add all the statements 
				//of the body at the beginning of  methodStatementList and continue. This approach should work.
				List<Statement> list;
				list = getStatementList(blockStatementList.get(methodStatementIndex));
				
				blockStatementList.addAll(methodStatementIndex+1,list);
				stmt = blockStatementList.get(methodStatementIndex);
				methodStatementLineNumber = cu.getLineNumber(stmt.getStartPosition());
				count++;
				if (count==10)
					break;
			}
			
		}*/
	}
	
	private boolean InBetween(int tobeExtracted, int statementLineNumber, int nextStatementLineNumber) {
		/*
		 * 	It returns true if the statement to be extracted is inside the block.
		 *  So, the block has to be unfolded and inner statements are required 
		 *  to be placed back in the methodSatementList. 
		 */
		
		if (tobeExtracted>statementLineNumber && tobeExtracted<nextStatementLineNumber)
			return true;
		else if (tobeExtracted>statementLineNumber && nextStatementLineNumber==-1)
			return true;
		else
			return false;
	}

	//This is a testing method; once the purpose is done, it can be removed from the file.
	private ArrayList<Integer> getBlocksIndexes(List<Statement> blockStatementList, CompilationUnit cu) {
		// TODO Auto-generated method stub
		ArrayList<Integer> blockStatementIndexList = new ArrayList<Integer>();
		for (Statement stmt : blockStatementList) {
			blockStatementIndexList.add(cu.getLineNumber(stmt.getStartPosition()));
		}
		
		return blockStatementIndexList;
	}
	
	
	
	//Extract Block-statement at location statementIndex, and insert all the statements in its body at the beginning
	private List<Statement> getStatementList(Statement statement) {
		// TODO Auto-generated method stub
		List<Statement> statementList = new ArrayList<Statement>();
		
		if(statement instanceof Block) {
			statementList.addAll( ((Block)statement).statements());
		}
		else if(statement instanceof ExpressionStatement) {
			ExpressionStatement expressionStatement = (ExpressionStatement)statement;
		}
		else if(statement instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement)statement;
			statementList.addAll(getStatementList(ifStatement.getThenStatement()));
		}
		else if(statement instanceof ForStatement) {
			ForStatement forStatement = (ForStatement)statement;
			statementList.addAll(getStatementList(forStatement.getBody()));
		}
		else if(statement instanceof EnhancedForStatement) {
			EnhancedForStatement enhancedForStatement = (EnhancedForStatement)statement;
			statementList.addAll(getStatementList(enhancedForStatement.getBody()));
		}
		else if(statement instanceof WhileStatement) {
			WhileStatement whileStatement = (WhileStatement)statement;
			statementList.addAll(getStatementList(whileStatement.getBody()));
		}
		else if(statement instanceof DoStatement) {
			DoStatement doStatement = (DoStatement)statement;
			statementList.addAll(getStatementList(doStatement.getBody()));
		}
		else if(statement instanceof SwitchStatement) {		//	#ToBe Checked
			List<Statement> switchStatements = ((SwitchStatement)statement).statements();
			for(Statement stmt : switchStatements)
				statementList.addAll(getStatementList(stmt));
		}
		else if(statement instanceof SwitchCase) {
			SwitchCase switchCase = (SwitchCase)statement;
		}
		else if(statement instanceof ReturnStatement) {
			ReturnStatement returnStatement = (ReturnStatement)statement;
		}
		else if(statement instanceof SynchronizedStatement) {
			SynchronizedStatement synchronizedStatement = (SynchronizedStatement)statement;
			statementList.addAll(synchronizedStatement.getBody().statements());	//ToBe Checked
		}
		else if(statement instanceof TryStatement) {	//Try Statements are required to be checked for inclusion of Try-statement
			TryStatement tryStatement = (TryStatement)statement;
			statementList.addAll(tryStatement.getBody().statements());
	/*	
			List<Statement> catchlist = tryStatement.catchClauses();
			if (catchlist.size()>0)
				statementList.addAll(catchlist);
	*/	
			Block finallyBlock = tryStatement.getFinally();
			if (finallyBlock!=null) {
				List<Statement> finallyList = tryStatement.getFinally().statements();
				statementList.addAll(finallyList);
			}
		}
		
		else  {
			System.out.println("Error! Statement : " + statement + " is detected and class type is  " + statement.getClass());
		}
		return statementList;
	}
	
}

