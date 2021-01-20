package identifiedOpportunityExtractor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.refactoring.changes.CompilationUnitChange;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import analyseMethod.EMO;
import analyseMethod.StatementExtractor;
import fileHandler.GetAST;

@SuppressWarnings({ "deprecation", "restriction" })
public class ExtractMethodRefactoring extends Refactoring{
	EMO emo;
	CompilationUnit cu;
	IFile file;
	MethodDeclaration methodDeclaration;
	CompilationUnitChange compilationUnitChange;
	TypeDeclaration sourceTypeDeclaration;

	public ExtractMethodRefactoring(CompilationUnit cu, EMO emo){
		this.emo = emo;
		this.cu = cu;
		file = emo.getIFile();
		methodDeclaration = emo.getMethodDeclaration();
		if (methodDeclaration==null) {
			System.out.println("MethodDeclaration is Null....");
		}
		ICompilationUnit icompilationUnit = new GetAST().getICompilationUnit(file);
		compilationUnitChange = new CompilationUnitChange ("", icompilationUnit);
		//	System.out.println(methodDeclaration.getName() + " > " + methodDeclaration.getParent().getNodeType());
		sourceTypeDeclaration = (TypeDeclaration) methodDeclaration.getParent();
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Extract Method Refactoring";
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		// TODO Auto-generated method stub
		RefactoringStatus status= new RefactoringStatus();
		pm.beginTask("Initial condition cheking : ", 1);
		return status;
		//return null;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		// TODO Auto-generated method stub
		RefactoringStatus status= new RefactoringStatus();
		pm.beginTask("Final condition checking : ", 2);
		refactor();
		return status;
	}

	private void refactor() {
		// TODO Auto-generated method stub
		MultiTextEdit root = new MultiTextEdit();
		compilationUnitChange.setEdit(root);
		extractSegment(root);
		ChangeNativeMethod(root);
	}

	private void ChangeNativeMethod(MultiTextEdit root) {
		// TODO Auto-generated method stub

	}

	private void extractSegment(MultiTextEdit root) {
		// TODO Auto-generated method stub
		String newMethodName = "newlyExtractedMethod";
		AST ast = sourceTypeDeclaration.getAST();
		MethodDeclaration newMethodDeclaration = ast.newMethodDeclaration();	//New node of methodDeclaration
		ASTRewrite writer = ASTRewrite.create(sourceTypeDeclaration.getAST());	
		writer.set(newMethodDeclaration, MethodDeclaration.NAME_PROPERTY, ast.newSimpleName(newMethodName), null);
		writer.set(newMethodDeclaration, MethodDeclaration.RETURN_TYPE2_PROPERTY, ast.newPrimitiveType(PrimitiveType.VOID), null);	//Setting return Type
		ListRewrite modifierRewrite = writer.getListRewrite(newMethodDeclaration, MethodDeclaration.MODIFIERS2_PROPERTY);
		Modifier accessModifier = newMethodDeclaration.getAST().newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
		modifierRewrite.insertLast(accessModifier, null);
		Block newMethodBody = newMethodDeclaration.getAST().newBlock();
		ListRewrite methodBodywriter = writer.getListRewrite(newMethodBody, Block.STATEMENTS_PROPERTY);

		//Below is demo code  to add the statements from Native method to newlyExtractedMethod
		for (Statement stmt : emo.getSourceCodeStatements()){
			methodBodywriter.insertLast(stmt, null);
		}


		//	Code for removal of the statements from the native method
		for (Statement stmt : emo.getSourceCodeStatements()){
			writer.remove(stmt, null);
		}

		//	Code for insertion of methodInvocation-statement

		MethodInvocation extractedMethodInvocation = ast.newMethodInvocation();
		writer.set(extractedMethodInvocation, MethodInvocation.NAME_PROPERTY, ast.newSimpleName( newMethodName), null);






		writer.set(newMethodDeclaration, MethodDeclaration.BODY_PROPERTY, newMethodBody, null);

		ListRewrite methodDeclarationRewrite = writer.getListRewrite(sourceTypeDeclaration, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		methodDeclarationRewrite.insertAfter(newMethodDeclaration, methodDeclaration, null);

		try {
			TextEdit textEdit = writer.rewriteAST();
			root.addChild(textEdit);
			compilationUnitChange.addTextEditGroup(new TextEditGroup("Insert new method", new TextEdit[] {textEdit}));
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}


	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		// TODO Auto-generated method stub
		try {
			pm.beginTask("Creating change...", 1);
			final Collection<TextFileChange> changes = new ArrayList<TextFileChange>();
			changes.add(compilationUnitChange);
			CompositeChange change = new CompositeChange(getName(), changes.toArray(new Change[changes.size()])) {
				@Override
				public ChangeDescriptor getDescriptor() {
					ICompilationUnit sourceICompilationUnit = (ICompilationUnit)cu.getJavaElement();
					String project = sourceICompilationUnit.getJavaProject().getElementName();
					String description = MessageFormat.format("Extract from method ''{0}''", new Object[] { methodDeclaration.getName().getIdentifier()});
					return new RefactoringChangeDescriptor(new ExtractSegmentRefactoringDescriptor(project, "Extract Segment Operation", "This is comment",
							cu, emo));
				}
			};
			return change;
		} finally {
			pm.done();
		}
	}
}

