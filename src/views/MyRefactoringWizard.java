package views;

import org.eclipse.jface.action.Action;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/*
 * 
 * TOBE extended later point of the development.
 */


public class MyRefactoringWizard  extends RefactoringWizard {
	
	Action action;
	Refactoring refactoring;
	
	public MyRefactoringWizard(Refactoring refactoring, Action action) {
		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE | NO_BACK_BUTTON_ON_STATUS_DIALOG);
		setDefaultPageTitle(refactoring.getName());
		this.refactoring = refactoring;
		this.action = action;
	}

	@Override
	protected void addUserInputPages() {
		// TODO Auto-generated method stub
	}

}
