package codeanalyzer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;

public class LongMethodHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page=window.getActivePage();
		try {
			page.showView("codeanalyzer.views.LongMethodView");
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			System.out.println("Exception occured: codeanalyzer.handlers.LongMethodHandler.java" );
			e.printStackTrace();
		}
	//	MessageDialog.openInformation(window.getShell(), "Segmentation","Hello, Eclipse world");
		return null;
	}
}
