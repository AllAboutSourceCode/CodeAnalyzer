package views;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.widgets.Composite;

import identifiedOpportunityExtractor.ExtractMethodRefactoring;

public class ExtractSegmentInputPage extends UserInputWizardPage {
	private ExtractMethodRefactoring refactoring;
	public ExtractSegmentInputPage(ExtractMethodRefactoring refactoring) {
		super("Segment to be extracted");
		this.refactoring = refactoring;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		
	}

}
