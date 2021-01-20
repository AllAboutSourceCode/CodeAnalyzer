package identifiedOpportunityExtractor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import analyseMethod.EMO;

public class ExtractSegmentRefactoringDescriptor extends RefactoringDescriptor {
	
	static String REFACTORING_ID = "org.eclipse.extract.segment";
	CompilationUnit cu;
	EMO emo;
	protected ExtractSegmentRefactoringDescriptor(String project, String comment, String description, CompilationUnit cu,
			EMO emo) {
		
		super(REFACTORING_ID, project, description, comment, RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
		this.cu = cu;
		this.emo = emo;

		// TODO Auto-generated constructor stub
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
