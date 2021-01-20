package statement;

import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.EmptyStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import fileHandler.IRWriter;
import parseFile.Index;

/*
 * 		Cases that are not handled and error message is shown. Following are few of the scenarios:
 * 
 * 		Output Error:
 * 		"non-handled exception detected..." 
 * 		is the message prompted and written in the output if 
 * 		an non-handled case occurs while source-to-IR conversion.
 * 
 * 
 * -----------------------------------------------------------------------------------------------------
 * 
 * Limitations:	Possible limitations to the IR generator can be following (Some of these may not occur)
 * 1.	If a function call is made in elseif-condition
 * 2.	For(;;)	
 * 
 * 
 */


public class Stmt {
	
	Optional<Expression>  EMPTY = Optional.empty();
	public void parseStmt(Node nd, IRWriter fout) {
		// Identify what kind of the statement the node is representing.
    	
		if (nd instanceof BreakStmt) {
			fout.write("break",nd.getBegin(),nd.getEnd());
		}
		else if (nd instanceof ContinueStmt) {
			fout.write("continue",nd.getBegin(),nd.getEnd());
		}
		else if (nd instanceof DoStmt) {
			parseDoStmt(nd,fout);
		}
		else if (nd instanceof ExpressionStmt) {
			parseExprStmt(nd, fout);
		}
		else if (nd instanceof EmptyStmt) {
			fout.write("invar",nd.getBegin(),nd.getEnd());
		//	System.out.println("EmptyStmt : 'invar' is placed in output file" + nd);
		}
		else if (nd instanceof ExplicitConstructorInvocationStmt) {
			System.out.println("Explicit Constructor Invocation Satement");
		}
		else if (nd instanceof ForStmt) {
			parseForStmt(nd,fout);
		}
		else if (nd instanceof ForeachStmt) {
			parseForeachStmt(nd,fout);
		}
		else if (nd instanceof IfStmt) {
			parseIfStmt(nd,fout);
		}
		else if (nd instanceof ReturnStmt) {
			parseReturnStmt(nd,fout);
		}
		else if (nd instanceof SwitchStmt) {
			parseSwitchStmt(nd,fout);
		}
		else if (nd instanceof SwitchEntryStmt) {
			parseSwitchEntryStmt(nd,fout);
		}
		else if (nd instanceof ThrowStmt) {
			//System.out.println("Throw Statement");
			parseExpression(((ThrowStmt)nd).getExpression(),fout);
		}
		else if (nd instanceof TryStmt) {
			parseTryStmt(nd,fout);
		}
		else if (nd instanceof WhileStmt) {
			parseWhileStmt(nd,fout);
		}
		else if (nd instanceof EmptyStmt) {
			fout.write("invar",nd.getBegin(),nd.getEnd());
			System.out.println("Empty statement : " + nd);
		}	
		else if (nd instanceof BlockStmt) {
			for (Node child : nd.getChildNodes())
				parseStmt(child,fout);
		}
		else if (nd instanceof Comment)
			;		//Do Nothing ; Its a comment
		else if (nd instanceof Expression)
			parseExpression((Expression) nd, fout);
		else if (nd instanceof SynchronizedStmt) {
			parseSynchronizedStmt(nd,fout);
		}
		else
			System.out.println("ParseStmt: Unidentified Stmt"+ nd + " : " + nd.getMetaModel());
	}



	private void parseExprStmt(Node nd, IRWriter fout) {
		// TODO Auto-generated method stub
		Expression expr = ((ExpressionStmt)nd).getExpression();

		parseExpression(expr,fout);
		
	}

	public String parseExpression(Expression expr, IRWriter fout) {
		StmtExpression exprstmt = new StmtExpression();
		// TODO Auto-generated method stub
		if (expr instanceof AssignExpr) {
			
			AssignExpr e = (AssignExpr)expr;
			Expression  target = e.getTarget();
			Expression value = e.getValue();
			return exprstmt.parseAssignExpr(expr.asAssignExpr(),target,value, fout);
		}
		else if (expr instanceof ArrayAccessExpr) {
			return (exprstmt.parseArrayAccessExpr((ArrayAccessExpr) expr,fout));
		}
		else if (expr instanceof ArrayCreationExpr) {
			Optional<ArrayInitializerExpr> init = expr.asArrayCreationExpr().getInitializer();
			String str="";
			if (init.isPresent()) {
				for (Expression e:init.get().getValues()) {
					str += parseExpression(e,fout);
				}
			}
			return str;		//If we want to treat declaration of the variable as assignment then change it to "assign" array_name
		}
		else if (expr instanceof ArrayInitializerExpr) {
			return (exprstmt.parseArrayInitializeExpr((ArrayInitializerExpr) expr, fout));
		}
		else if (expr instanceof BinaryExpr) {
			
			return(exprstmt.parseBinaryExpr((BinaryExpr) expr, fout));
		}
		else if (expr instanceof CastExpr) {
			return parseExpression(expr.asCastExpr().getExpression(),fout);
		}
		else if (expr instanceof ConditionalExpr) {
			return exprstmt.parseConditionalExpr(expr.asConditionalExpr(),fout);
		}
		else if (expr instanceof EnclosedExpr) {
			return exprstmt.parseEnclosedExpr(expr.asEnclosedExpr(),fout).toString();
		}
		else if (expr instanceof FieldAccessExpr) {
			return exprstmt.parseFieldAccessExpr(expr.asFieldAccessExpr(),fout);
		}
		else if (expr instanceof InstanceOfExpr) {
			InstanceOfExpr iof = expr.asInstanceOfExpr();
			return(parseExpression(iof.getExpression(),fout));
		}
		else if (expr instanceof LambdaExpr) {
			System.out.println("Lambda Expression not yet implemented" + expr);
		}
		else if (expr instanceof LiteralExpr) {
			return "";
		}
		else if (expr instanceof MethodCallExpr ) {					//Mostly this will be invoked
			return exprstmt.parseMethodExpr((MethodCallExpr)expr, null,fout);	//only when the method call is not part of Assign
		}
		else if (expr instanceof MethodReferenceExpr) {
			System.out.println("Methodreference encountered, not handled..." + expr);
		}
		else if (expr instanceof NameExpr) {
				return expr.toString();
		}
		else if (expr instanceof ObjectCreationExpr) {
			//This will occur only if object creation is not part of assign expression
			String str = exprstmt.parseObjectCreationExpr(expr.asObjectCreationExpr(), fout);
			fout.write("assign " + "obj_tmp_$" + str,expr.getBegin(),expr.getEnd());
			return "obj_tmp_$";
			//System.out.println("Object Creation expression encountered ... Not handled" + expr);
		}
		else if (expr instanceof SuperExpr) {														//Unfinished
			System.out.println("Super Expression not implemented..." + expr);
		}
		else if (expr instanceof TypeExpr) {
			System.out.println("TypeExpr not handled..."+ expr);
		}
		else if (expr instanceof ThisExpr) {
			return expr.toString();														//	Check it : Warning
			//System.out.println("This expression encountered, not handled..." + expr);
		}
		else if (expr instanceof UnaryExpr) {
			return exprstmt.parseUnaryExpr((UnaryExpr) expr, fout);
			//UnaryExpr u = (UnaryExpr)expr;
			//fout.write(">assign " + u.getExpression().toString() + " " + u.getExpression().toString());
		}
		else if (expr instanceof VariableDeclarationExpr) {
			VariableDeclarationExpr v = (VariableDeclarationExpr)expr;
			
			for (VariableDeclarator var : v.findAll(VariableDeclarator.class)) {
				Expression target = var.getNameAsExpression();
				if ((var.getInitializer()!=EMPTY)) {		//	Initializer is an assign expression
					Expression value = var.getInitializer().get();
					exprstmt.parseAssignExpr(expr,target, value, fout);
				}	
			//	else		//This else will result in "assign" keyword for declaration statements (i.e. not initialized) 
				//	fout.write(">assign " + target.toString());
			}
		}
		else
			System.out.println("Unknown/UnHandled type of Expression Statement : Stmt.java" );
		return "";
	}

	public void parseForeachStmt(Node nd, IRWriter fout) {			//		For-each Statement
		StmtExpression st = new StmtExpression();
		String init = "";
		for(Node n : ((ForeachStmt)nd).getChildNodes()) {
			if (n instanceof VariableDeclarationExpr) {
				init = ((VariableDeclarationExpr)n).getVariables().get(0).toString();
				fout.write("assign " + init,n.getBegin(),n.getEnd());
			}
			else if (n instanceof BlockStmt) {
				for(Node stmt: n.getChildNodes()) {
					parseStmt(stmt,fout);
				}
				fout.write("}",null, null);
			}
			else {
				Expression expr = ((ForeachStmt)nd).getIterable();
				if (expr instanceof MethodCallExpr) {
					//	Call ParseMethod with return value...
					String ret_str = "_" + (new Index()).change();	//Value of tmp_index can change after method returns;
					st.parseMethodExpr((MethodCallExpr) expr, ((MethodCallExpr) expr).getNameAsString()+ret_str,fout);
					fout.write("loop " + ((MethodCallExpr) expr).getNameAsString()+ret_str + " " + init,n.getBegin(),n.getEnd());
					fout.write("{",null,null);
				}
				else {
					String str = parseExpression(expr, fout);
					fout.write("loop " + str + " " +init,expr.getBegin(),expr.getEnd());
					fout.write("{",null, null);
				}
			}
		}
	}
	

	public void parseForStmt(Node nd, IRWriter fout) {						//		ForStmt
		parseForInitExpr(((ForStmt)nd).getInitialization(),fout);
		parseForCompareExpr(((ForStmt)nd).getCompare(),fout);
		fout.write("{",null,null);
		parseForBlock(((ForStmt)nd),fout);
		parseForUpdateExpr(((ForStmt)nd).getUpdate(),fout);
		fout.write("}",null,null);
	}

	private void parseForBlock( ForStmt for_nd, IRWriter fout) {
		// TODO Auto-generated method stub
		for(Node nd: for_nd.getBody().getChildNodes()) {
			parseStmt(nd,fout);
		}
	}

	private void parseForUpdateExpr(NodeList<Expression> update, IRWriter fout) {
		// TODO Auto-generated method stub
			for (Expression e : update)
				parseExpression(e, fout);
	}

	private void parseForCompareExpr(Optional<Expression> compare, IRWriter fout) {
		try {
			if (compare.isPresent()){
				Expression cmpExpr = compare.get();					//Handle it; If no condition is present.
				if (cmpExpr instanceof MethodCallExpr) {
					StmtExpression stex = new StmtExpression();
					String ret_str = "_" + (new Index()).change();
					stex.parseMethodExpr(cmpExpr.asMethodCallExpr(),cmpExpr.asMethodCallExpr().getNameAsString()+ret_str, fout);
					fout.write("loop " + cmpExpr.asMethodCallExpr().getNameAsString()+ret_str,cmpExpr.getBegin(),cmpExpr.getEnd());
				}
				else
					fout.write("loop " + parseExpression(cmpExpr,fout),cmpExpr.getBegin(),cmpExpr.getEnd());
			}
		}
		catch(Exception e) {
			System.out.println("Exception occurred .....>");
			e.printStackTrace();
		}
	}

	private void parseForInitExpr(NodeList<Expression> initialization, IRWriter fout) {
		for (Expression e : initialization)
			parseExpression(e, fout);
	}

	public void parseIfStmt(Node nd, IRWriter fout) {								//	IF-Statement
		// TODO Auto-generated method stub
		IfStmt ifstmt = ((IfStmt)nd);
		
		Expression ex = ifstmt.getCondition();
		if (ex instanceof MethodCallExpr) {
			StmtExpression stex = new StmtExpression();
			String ret_str = "_" + (new Index()).change();
			stex.parseMethodExpr(ex.asMethodCallExpr(),ex.asMethodCallExpr().getNameAsString()+ ret_str, fout);
			fout.write("if " + ex.asMethodCallExpr().getNameAsString()+ret_str,ex.getBegin(),ex.getEnd());
		}
		else
			fout.write("if " + parseExpression(ex,fout),ex.getBegin(),ex.getEnd());
		fout.write("{",null, null);
		parseThenStmt(ifstmt,fout);
		fout.write("}",null,null);
		parseElseStmt(ifstmt,fout);
	}

	
	private void parseElseStmt(IfStmt ifst, IRWriter fout) {	//ElseIf + Else
		// TODO Auto-generated method stub
		while(ifst.getElseStmt().isPresent() && ((IfStmt)ifst).getElseStmt().get() instanceof IfStmt) {
			ifst = ifst.getElseStmt().get().asIfStmt();
			fout.write("elseif ",ifst.getBegin(),ifst.getEnd());
			fout.write("{",null,null);		// Since elseif may contain expressions which will be broken into other expressions
									//	and thus will be written before elseif statement ; so we will introduce a new elseif
									//	with assign so that overall data dependencies are same.
			Expression ex = ifst.getCondition();
			if (ex instanceof MethodCallExpr) {		//This checking ensures that 'call' keyword is not generated i.e. 
				StmtExpression stex = new StmtExpression();	//	MethodCallExpr option inside parseExpression() is not invoked.
				String ret_str = "_" + (new Index()).change();
				stex.parseMethodExpr(ex.asMethodCallExpr(),ex.asMethodCallExpr().getNameAsString()+ ret_str, fout);
			//	fout.write("assign elseif_tmp " + ex.asMethodCallExpr().getNameAsString()+ret_str);
			}
			else
				fout.write("assign elseif_tmp " + parseExpression(ifst.getCondition(), fout),ifst.getBegin(),ifst.getEnd());
			parseThenStmt(ifst,fout);
			fout.write("}",null,null);
		}
		if (ifst.getElseStmt().isPresent())
		{
			fout.write("else",ifst.getElseStmt().get().getBegin(),ifst.getElseStmt().get().getBegin());
			fout.write("{",null,null);
			parseStmt(ifst.getElseStmt().get(),fout);
			fout.write("}",null,null);
		}
	}

	private void parseThenStmt(IfStmt ifstmt, IRWriter fout) {
		// TODO Auto-generated method stub
	
		Statement then = ifstmt.getThenStmt();
		if (then.isReturnStmt()) {
			ReturnStmt rt = then.asReturnStmt();
			
			if (rt.getExpression() == EMPTY)	// Its < return ;> and should map to invar# position. ..	$$ Modified@08-DEC-2019
				fout.write("invar",rt.getBegin(),rt.getEnd());
			else 
					parseReturnStmtExpr(rt.getExpression(),new StmtExpression(),fout);
			return;
		}
		
		for (Node n : ifstmt.getThenStmt().getChildNodes()) {
			if (n instanceof Statement) {
				parseStmt(n, fout);
			}	
			else if (n instanceof Expression) {
				parseExpression ((Expression) n,fout);
			}
			else if (n instanceof Comment)
				;
			else
				System.out.println("parseThenStmt: An unknown type encountered..."+ n+ ": " + n.getMetaModel());
		}
		
	}

	public void parseSwitchStmt(Node nd, IRWriter fout) {								//Switch-Statement
		// TODO Auto-generated method stub
		SwitchStmt sw = (SwitchStmt)nd;
		fout.write ("switch " + parseExpression(sw.getSelector(), fout) + " 1",sw.getBegin(),sw.getEnd());
		for (Node entry : sw.getEntries()) 
			parseStmt(entry,fout);
	}

	public void parseSwitchEntryStmt(Node nd, IRWriter fout) {
		// TODO Auto-generated method stub
		SwitchEntryStmt entry = (SwitchEntryStmt)nd;
		fout.write("case",entry.getBegin(),entry.getEnd());
		fout.write("{",null,null);
		for (Statement st : entry.getStatements())
			parseStmt(st,fout);
		fout.write("}",null,null);
	}

	public void parseSynchronizedStmt(Node nd, IRWriter fout) {
		// TODO Auto-generated method stub
		SynchronizedStmt sync = (SynchronizedStmt)nd;								// Synchronized Statement
		String str = parseExpression(sync.getExpression(),fout);
		fout.write("synchronized " + str,sync.getBegin(),sync.getEnd());
		fout.write("{",null,null);
		parseStmt(sync.getBody(),fout);
		fout.write("}",null,null);
	}


	public void parseWhileStmt(Node nd, IRWriter fout) {                               //While-Block
		WhileStmt whilest =(WhileStmt)nd;
		Expression ex = whilest.getCondition();
		if (ex instanceof MethodCallExpr) {
			StmtExpression stex = new StmtExpression();
			String ret_str = "_" + (new Index()).change();
			stex.parseMethodExpr(ex.asMethodCallExpr(), ex.asMethodCallExpr().getNameAsString()+ret_str, fout);
			fout.write("loop " + ex.asMethodCallExpr().getNameAsString()+ret_str,whilest.getBegin(),whilest.getEnd());
		}
		else 
			fout.write("loop " + parseExpression(whilest.getCondition(), fout),whilest.getBegin(),whilest.getEnd());
		fout.write("{",null,null);
		parseStmt(whilest.getBody(),fout);
		fout.write("}",null,null);
	}
	private void parseDoStmt(Node nd, IRWriter fout) {								//	Do-While Block
		// TODO Auto-generated method stub
		DoStmt dost = ((DoStmt) nd).asDoStmt();
		Expression ex = dost.getCondition();
		if (ex instanceof MethodCallExpr) {
			StmtExpression stex = new StmtExpression();
			String ret_str = "_" + (new Index()).change();
			stex.parseMethodExpr(ex.asMethodCallExpr(), ex.asMethodCallExpr().getNameAsString()+ret_str, fout);
			fout.write("loop " + ex.asMethodCallExpr().getNameAsString()+ret_str,dost.getBegin(),dost.getEnd());
		}
		else
			fout.write("loop " + parseExpression(dost.getCondition(), fout),dost.getBegin(),dost.getEnd());
		fout.write("{",null,null);
		parseStmt(dost.getBody(),fout);
		fout.write("}",null,null);
	}
	public void parseTryStmt(Node try_nd, IRWriter fout) {
		BlockStmt trybk =  ((TryStmt)try_nd).getTryBlock();
		NodeList<CatchClause> catchlist =   ((TryStmt)try_nd).getCatchClauses();
		Optional<BlockStmt> finbk = ((TryStmt)try_nd).getFinallyBlock();

		//Parsing Try Block
		fout.write("try",trybk.getBegin(),trybk.getEnd());
		fout.write("{",null,null);
		//	Parse Resource if any 
		NodeList<Expression> resource = ((TryStmt)try_nd).getResources(); 
		for (Node nd :resource) {
			parseExpression(((Expression)nd),fout);
		}
		parseTryBlock(trybk,fout);
		fout.write("}",null,null);
		//	Parsing Catch Blocks
		for (Node catchnd : catchlist) {
			parseCatchBlock(catchnd,fout);
		}
		//		Parsing Finally Block
		parseFinallyBlock(finbk,fout);
	}
	private void parseFinallyBlock(Optional<BlockStmt> finbk, IRWriter fout) {
		// TODO Auto-generated method stub
		try {															//		Finally-Block
			BlockStmt bk = finbk.get();
			fout.write("finally",bk.getBegin(),bk.getEnd());
			fout.write("{",null,null);
			for (Node nd : bk.getChildNodes()) {
				parseStmt(nd,fout);
			}
			fout.write("}",null,null);
		}
		catch(Exception e){
		}
	}

	private void parseCatchBlock(Node catchnd, IRWriter fout) {					//	CATCH Block
		// TODO Auto-generated method stub
		fout.write("catch ",catchnd.getBegin(),catchnd.getEnd());
		fout.write("{",null,null);
		fout.write("assign " + (((CatchClause)catchnd).getParameter().getNameAsString()),catchnd.getBegin(),catchnd.getEnd());
		
		for (Node nd : ((CatchClause)catchnd).getBody().getChildNodes()) {
			parseStmt(nd,fout);
		}
		fout.write("}",null,null);
	}

	private void parseTryBlock(BlockStmt trybk, IRWriter fout) {
		List<Node> nodeList = trybk.getChildNodes();
		for (Node nd : nodeList) {
			parseStmt(nd, fout);
		}
	}
						
	public void parseReturnStmt(Node nd, IRWriter fout) {					//	Return-Statement
		// TODO Auto-generated method stub
		StmtExpression st = new StmtExpression();
		
		
		Optional<Expression> r = ((ReturnStmt)nd).getExpression();
		
		if (r == EMPTY)	// Its < return ;> and should map to invar# position. ..	$$ Modified@03-DEC-2019
			fout.write("invar",nd.getBegin(),nd.getEnd());
		else 
			parseReturnStmtExpr(r,st,fout);
		
	}



	private void parseReturnStmtExpr(Optional<Expression> r, StmtExpression st, IRWriter fout) {
		// TODO Auto-generated method stub

		Expression re = r.get();
		if (re.isCastExpr())
			re = re.asCastExpr().getExpression();
		if (re instanceof NullLiteralExpr)
			fout.write("invar",re.getBegin(),re.getEnd());
		else if (re instanceof MethodCallExpr) {
			String ret_str = "_" + (new Index()).change();
			st.parseMethodExpr((MethodCallExpr) re, ((MethodCallExpr) re).getNameAsString()+ ret_str, fout);
			fout.write("return " + ((MethodCallExpr) re).getNameAsString()+ ret_str,re.getBegin(),re.getEnd());
		}
		else if (re instanceof BinaryExpr) {
			//exprstmt.parseBinaryExpr (re.ge);
			String str = st.parseBinaryExpr((BinaryExpr) re,fout);
			fout.write("assign r_tmp_$ " + str ,re.getBegin(),re.getEnd());
			fout.write("return r_tmp_$" ,re.getBegin(),re.getEnd());
		}
		else if (re instanceof NameExpr)
			fout.write("return " + ((NameExpr)re).getNameAsString(),re.getBegin(),re.getEnd());
		else if (re instanceof Expression) {
			String ret = parseExpression(re, fout);
			if (ret!=null)
				fout.write("return " + ret,re.getBegin(),re.getEnd());
			else 
				fout.write("invar",re.getBegin(),re.getEnd());
		}
		else
		{
			System.out.println("ParseReturnStmt(): non-handled expression detected... " + re.getMetaModel());
		}

	}
}
