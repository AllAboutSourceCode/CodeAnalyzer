package statement;

import java.util.Optional;
import java.util.Stack;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.AssignExpr.Operator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.UnaryExpr;

import fileHandler.IRWriter;
import parseFile.Index;

public class StmtExpression {
	public String parseAssignExpr(Expression expr,Expression target,Expression value, IRWriter fout) {			//Assign Expression
			Stmt st = new Stmt();
			String tstr;
		//	System.out.println("Target : " + target + " Value : " + value );
			boolean flag =false;
			if (target instanceof ArrayAccessExpr)
				tstr = parseArrayAccessExpr(target.asArrayAccessExpr(),fout);
			else if (expr instanceof AssignExpr) {
				Operator op = expr.asAssignExpr().getOperator();
				tstr = target.toString();
				String opstr = op.name();
				if (opstr.equals("PLUS") || opstr.equals("MINUS") || opstr.equals("REMAINDER") || opstr.equals("MULTIPLY") || opstr.equals("DIVIDE")) {
					tstr += " " + target.toString();
					flag = true;
				}
			}
			else
				tstr = target.toString();
			
			if (value instanceof CastExpr)
				value = value.asCastExpr().getExpression();
			if (value instanceof IntegerLiteralExpr || value instanceof LiteralExpr) {
				//System.out.println(target.getMetaModel()+ ">"+ value.toString());
				fout.write("assign " + tstr,value.getBegin(),value.getEnd());
			}
			else {
				if (value instanceof BinaryExpr) {
					String str = parseBinaryExpr((BinaryExpr) value,fout);
					fout.write("assign " + tstr + " " + str,value.getBegin(),value.getEnd() );
				}
				else if (value instanceof MethodCallExpr) {
					if (flag) {
						String ret_str= value.asMethodCallExpr().getNameAsString()+"_" + (new Index()).change();
						parseMethodExpr((MethodCallExpr) value, ret_str,fout);
						fout.write("assign " + tstr + " " + ret_str,value.getBegin(),value.getEnd());
						flag = false;
					}
					else
						parseMethodExpr((MethodCallExpr) value, tstr,fout);
					
				}
				else if (value instanceof NameExpr)
					fout.write("assign " + tstr + " " + value,value.getBegin(),value.getEnd());
				else if (value instanceof ArrayInitializerExpr) {
					String str =  parseArrayInitializeExpr((ArrayInitializerExpr) value, fout);
					if (str!=null)
						fout.write("assign " + tstr + " " +str,value.getBegin(),value.getEnd());
					else
						fout.write("assign " + tstr,value.getBegin(),value.getEnd());
				}
				else if (value instanceof ObjectCreationExpr) {
					String argstr = parseObjectCreationExpr(value,fout);
					fout.write("assign " + tstr + " " + argstr,value.getBegin(),value.getEnd());
				}
				else if (value instanceof ArrayCreationExpr)
					return "";//If we want to treat declaration of the variable as assignment then change it to "assign" array_name
				else if (value instanceof Expression)
					fout.write("assign " + tstr + " " + st.parseExpression(value, fout),value.getBegin(),value.getEnd());
				else
					System.out.println("ParseAssignExpr: "+ " Unhandled case"+ value.getMetaModel());
			}
		return tstr;
	}

	public String parseObjectCreationExpr(Expression value, IRWriter fout) {
		// TODO Auto-generated method stub
		Stmt st = new Stmt();
		ObjectCreationExpr ob =value.asObjectCreationExpr();
		String argstr = "", str = "";
		for (Expression ex : ob.getArguments()) {
			if (ex instanceof ObjectCreationExpr) {
				String ret = parseObjectCreationExpr(ex, fout);
				fout.write("assign Obj_tmp_$" + ret,ex.getBegin(),ex.getEnd());
				str+= " " +"Obj_tmp_$";
			}
			else
				str = st.parseExpression(ex,fout);
			
			if (str!=null)
				argstr += " " + str;
		}
		return argstr;
	}

	public String parseMethodExpr(MethodCallExpr m, String name, IRWriter fout) {			//Method Expression
		String str ="";
		String appendix = "";
		String methodName = m.getNameAsString();
		Optional<Expression> scope = m.getScope();
		if (scope.isPresent()) {	//	This block is added in version 1.7
			//System.out.println(scope + " <> " + scope.get().getMetaModel());
			if (scope.get() instanceof MethodCallExpr) {
				MethodCallExpr mt = scope.get().asMethodCallExpr();
				appendix = mt.getNameAsString()+"_"+((new Index()).change());
				//System.out.println("< "+ mt + ">");
				parseMethodExpr(mt, appendix, fout);
				
			}
			else if (scope.get() instanceof NameExpr) {
				NameExpr nm = scope.get().asNameExpr();
				appendix = nm.toString();
			}
		}
			
		
		for (Expression arg : m.getArguments()) {
			if (arg.isCastExpr()) {
				arg = arg.asCastExpr().getExpression();
			}
			if (arg instanceof LiteralExpr)
				;	//Do Nothing
			else if(arg instanceof NameExpr)
				str += " " + arg.toString();
			else if (arg instanceof BinaryExpr)
				str += " " + parseBinaryExpr((BinaryExpr) arg,fout);
			else if (arg instanceof MethodCallExpr) { 
				String ret_str = "_" + (new Index()).change();
				str += " " + ((MethodCallExpr) arg).getNameAsString()+ret_str;
				parseMethodExpr((MethodCallExpr) arg, ((MethodCallExpr) arg).getNameAsString()+ret_str,fout);
			}
			else if (arg instanceof ArrayAccessExpr)
				str+= " " + parseArrayAccessExpr((ArrayAccessExpr)arg,fout);
			else if (arg instanceof CastExpr)
				;	//Do Nothing
			else if (arg instanceof FieldAccessExpr)
				str+= " " + arg.toString();
			else if (arg instanceof ObjectCreationExpr) {
				String ret = parseObjectCreationExpr(arg, fout);
				fout.write("assign Obj_tmp_$" + ret,arg.getBegin(),arg.getEnd());
				str+= " " +"Obj_tmp_$";
			}
			else if (arg instanceof EnclosedExpr) {
				str += " " +parseEnclosedExpr((EnclosedExpr) arg,fout);
			}
			else if (arg instanceof ArrayCreationExpr) {			//	Check this output :	Warning
				str += " "+  parseArrayCreationExpr(arg,fout);
			}
			else if (arg instanceof ClassExpr)
				;
			else
				System.out.println("An unhandled case in ParseMethodExpr: StmtExpression.java " +arg + ": "+ arg.getMetaModel());
		}
		
		if (name!=null)  
			fout.write("rcall " + methodName + " " + name + " " + str + " "+ appendix,m.getBegin(),m.getEnd());
		else {
			fout.write("call " + methodName + " " + str +" "+ appendix,m.getBegin(),m.getEnd());	//	appendix added in version 1.7
			name =  methodName+"_" + (new Index()).change();
		}
		return name;
	}
	private String parseArrayCreationExpr(Expression arg, IRWriter fout) {
		String str="";
		Stmt st =  new Stmt();
		for ( ArrayCreationLevel level:  arg.asArrayCreationExpr().getLevels()) {
			Optional<Expression> e = level.getDimension();
			if (e.isPresent()) {
				Expression exp = e.get();
				if ( exp instanceof MethodCallExpr) {
					String ret_str = exp.asMethodCallExpr().getNameAsString()+"_" + (new Index()).change();
					str +=" " + ret_str;
					parseMethodExpr(exp.asMethodCallExpr(), ret_str , fout);
					
				}
				else
					str += " " +st.parseExpression(e.get(),fout) + " ";
			}
			
		}
		return str;
	}

	public String parseArrayAccessExpr(ArrayAccessExpr arg, IRWriter fout) {			//	Array Access Expression
		Stmt st = new Stmt();
		String argstr = "";
		String str = "";
		argstr += " " +st.parseExpression(arg.getName(),fout);
		if (arg.getIndex() instanceof MethodCallExpr) {	//Separately checked bcoz it will be rcall,
			String ret_str = ((MethodCallExpr) arg.getIndex()).getNameAsString()+"_" + (new Index()).change();
			str += " " + ret_str;
			parseMethodExpr( arg.getIndex().asMethodCallExpr(), ret_str,fout);
		}
		else
			 str = st.parseExpression(arg.getIndex(), fout);
		if (str!=null)
			argstr += " " +str;
		return argstr;
	}
	public String parseArrayInitializeExpr(ArrayInitializerExpr arexpr,IRWriter fout) {		//Array Initializer
		Stmt st = new Stmt();
		String argstr="",str;
		for (Expression expr : arexpr.getValues()) {
			str = st.parseExpression(expr,fout);
			if (str!=null)
				argstr += " " +str;
		}
		return argstr;
	}
	public String parseBinaryExpr(BinaryExpr bexpr, IRWriter fout) {					//Binary Expression
		Stack<Expression> st = new Stack<Expression>();
		st.push(bexpr);
		Expression expr;
		String argstr = "";
		
		while(!st.empty()) {
			expr = st.pop();
			if (expr instanceof CastExpr) 
				expr = expr.asCastExpr().getExpression();
			if (expr instanceof NameExpr) {
				argstr += " " +expr.toString();
			}
			else if (expr instanceof MethodCallExpr) {
				String name = ((MethodCallExpr) expr).getNameAsString()+"_" + (new Index()).change();
				parseMethodExpr((MethodCallExpr) expr, name,fout);
				argstr+= " " + name;	//Adding a temporary variable 
				//	We might need to pass parseMethodCallExpr() two arguments; one representing the return variable
			}
			else if (expr instanceof BinaryExpr){
				bexpr = (BinaryExpr)expr;
				for (Node n : bexpr.getChildNodes()) {
					if (!(n instanceof Comment))
						st.push((Expression)n);
				}
			}
			else if (expr instanceof LiteralExpr)						
				;		//Do Nothing															
			else if (expr instanceof FieldAccessExpr)							//ChEcK iF Return statements are right here.
				argstr+= " " +expr.toString();
			else if (expr instanceof ArrayAccessExpr) {
				argstr+= " " +parseArrayAccessExpr(expr.asArrayAccessExpr(),fout);
			}
			else if (expr instanceof EnclosedExpr)
				argstr+= " " +(parseEnclosedExpr(expr.asEnclosedExpr(), fout));
			else if (expr instanceof UnaryExpr)
				argstr+= " " + parseUnaryExpr((UnaryExpr) expr, fout);
			else if (expr instanceof InstanceOfExpr) {
				Stmt stm = new Stmt();
				InstanceOfExpr ins = expr.asInstanceOfExpr();
				argstr+=" " +stm.parseExpression(ins.getExpression(),fout);
			}
			else {
				System.out.println("ParseBinaryExpr : Unhandled binary expression " +expr  + expr.getMetaModel());
			}
		}
		return argstr;
	}

	public String parseConditionalExpr(ConditionalExpr cexpr, IRWriter fout) {
		Stmt st = new Stmt();
		String argstr = st.parseExpression(cexpr.getCondition(), fout).toString();
		String str;
		if ((str = st.parseExpression(cexpr.getThenExpr(),fout).toString())!=null)
			argstr += " " + str;
		if ((str = st.parseExpression(cexpr.getElseExpr(), fout).toString())!=null)
			argstr +=" " +str;
		return argstr;
	}

	public String parseEnclosedExpr(EnclosedExpr expr, IRWriter fout) {
		Stmt st = new Stmt();
		return st.parseExpression(expr.asEnclosedExpr().getInner(),fout);		//Example : (fun()>(a+b))
	}
	public String parseUnaryExpr(UnaryExpr expr, IRWriter fout) {
		Stmt st = new Stmt();
		String str;
		com.github.javaparser.ast.expr.UnaryExpr.Operator op = expr.getOperator();
		Expression ex = expr.getExpression();
		if (ex instanceof MethodCallExpr) {
			StmtExpression stex = new StmtExpression();
			stex.parseMethodExpr(ex.asMethodCallExpr(), ((MethodCallExpr) ex).getNameAsString()+"_"+(new Index()).change(), fout);
			str =  ex.asMethodCallExpr().getNameAsString()+"_" + (new Index()).get();
		}
		else
			str = st.parseExpression(expr.getExpression(),fout);
		if (op.name() == "POSTFIX_INCREMENT" || op.name() == "POSTFIX_DECREMENT") {
			fout.write("assign " + str + " " + str,ex.getBegin(),ex.getEnd());
		}
		return str;		
	}

	public String parseFieldAccessExpr(FieldAccessExpr expr, IRWriter fout) {		//Field Access Expression
		// TODO Auto-generated method stub
		Expression scope = expr.getScope();
		if (scope instanceof EnclosedExpr) {
			scope = scope.asEnclosedExpr().getInner();
		}
		if (scope instanceof MethodCallExpr) {
			String ret_str= scope.asMethodCallExpr().getNameAsString()+"_" + (new Index()).change();
			parseMethodExpr((MethodCallExpr) scope, ret_str,fout);
			return ret_str+"." + expr.getNameAsString();
		}
		else
			return expr.toString();
	}
}
