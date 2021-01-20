package workbenchInfo;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
/*
 * This file will be re-designed for better support.
 */

public class WorkbenchInfo {
	public IEditorPart getActivePart() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		return win.getActivePage().getActiveEditor();
	}
	
	public IFile getIFile() {
		IEditorPart editor = (IEditorPart) getActivePart();
		return ((FileEditorInput) editor.getEditorInput()).getFile();
	}
	public String getPath() throws ExecutionException {
		// TODO Auto-generated method stub
//		IFile file = getIFile();
	//	return file.getFullPath().toString();
		
		return getFullpath(getActivePart());
	}
	
	public String getFullpath(IEditorPart iEditorPart) {
        IEditorInput input = iEditorPart.getEditorInput();

        IPath path = ((FileEditorInput) input).getPath();
        if (path != null) {
        	return path.toOSString();
        }
		return "Look for Error: WorkbenchInfo.java";

    }
}

/*
The method getFullpath() will return path of the files which are part of workspace. For other kind of files explore "FileStoreEditorInput".
*/