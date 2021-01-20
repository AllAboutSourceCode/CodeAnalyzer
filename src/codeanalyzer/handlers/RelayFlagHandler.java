package codeanalyzer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import fileHandler.GetAST;
import segmentation.Parameter;
import workbenchInfo.WorkbenchInfo;


public class RelayFlagHandler extends AbstractHandler{
	public static Parameter relay = new Parameter("-relay", "0");	//Need to access the value from any object.
	public Object execute(ExecutionEvent event) throws ExecutionException {
			
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page=window.getActivePage();
		int value = Integer.parseInt(relay.getValue());
		value = 1-value;	//Toggle the value.
		relay.setValue(Integer.toString(value));
		
		
		if (value==0) {
			String msg = "\n\nA Block with no outgoing data dependency will not be considered as an EMO";
			MessageDialog.openInformation(window.getShell(),"Toggle NoRelayExtract Flag","Flag is set to " + relay.getValue() + msg);
		}
		else	{
			String msg = "\n\nA Block with no outgoing data dependency will also be considered as an EMO";
			MessageDialog.openInformation(window.getShell(),"Toggle NoRelayExtract Flag","Flag is set to " + relay.getValue()+ msg);
		}
			
		
		return null;
	}
	public String getFlag() {
		return relay.getValue();
	}
	public boolean getFlagBoolean() {
		if (relay.getValue().equals("1"))
			return true;
		return false;
	}
	@SuppressWarnings("unused")
	private String getFileInfo(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		WorkbenchInfo wb = new WorkbenchInfo();
		IEditorPart editor = wb.getActivePart();
		IFile original = ((FileEditorInput) editor.getEditorInput()).getFile();

		IRegion lineInfo = HighLightLine(editor,3);
		//	 IMember member = getIMemberAt(,2);
		
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		WorkbenchInfo wi = new WorkbenchInfo();
		MessageDialog.openInformation(
				window.getShell(),
				"Toggle Relay Flag",
				wi.getFullpath(wi.getActivePart()));
		//original.getFullPath().toString());

		return original.getFullPath().toString();
	}

	private static IRegion HighLightLine(IEditorPart editorPart, int lineNumber) {	//lineNumber should be '>=0'
		if (!(editorPart instanceof ITextEditor) || lineNumber <= 0) {
			return null;
		}
		ITextEditor editor = (ITextEditor) editorPart;
		IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		editor.setHighlightRange(3, 6, false);
		editor.selectAndReveal(3, 6);
		/*		
		  if (document != null) {
		    IRegion lineInfo = null;
		    try {
		      // line count internaly starts with 0, and not with 1 like in
		      // GUI
		      lineInfo = (IRegion) document.getLineInformation(lineNumber);

		    } catch (org.eclipse.jface.text.BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    if (lineInfo != null) {
		      editor.selectAndReveal(((org.eclipse.jface.text.IRegion) lineInfo).getOffset(), ((IDocument) lineInfo).getLength());
		    }
		    return lineInfo;
		  }
		 */		  
		return null;

	}

}
