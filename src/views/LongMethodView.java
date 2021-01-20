package views;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import analyseMethod.EMO;
import fileHandler.GetAST;
import identifiedOpportunityExtractor.ExtractMethodRefactoring;
import segmentation.TobeExtractedMethodList;
import workbenchInfo.WorkbenchInfo; 



/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 * 
 * The above view is modified to meet the requirements
 */

public class LongMethodView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "codeanalyzer.view.viewplugin.views.SampleView";

	@Inject IWorkbench workbench;

	private TreeViewer viewer;
	private Action identifySegment;	//Note: Segment here represents an extract method opportunity
	private Action extractSegment;
	private Action doubleClickAction;
	private EMO[] emolist; 

	//https://www.eclipse.org/articles/Article-TreeViewer/TreeViewerArticle.htm
	//Help on Treeviewer can be found on above link.	

	class ViewContentProvider implements ITreeContentProvider{
		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			ITreeContentProvider.super.dispose();
		}
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
			//		ITreeContentProvider.super.inputChanged(viewer, oldInput, newInput);
		}
		@Override
		public Object[] getElements(Object inputElement) {
			// TODO Auto-generated method stub
			if (emolist!= null) {
				return emolist;
			}
			else {
				return new EMO[] {};
			}
		}


		@Override
		public Object[] getChildren(Object parentElement) {
			// TODO Auto-generated method stub
			return new String[] {};
		}

		@Override
		public Object getParent(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			if (obj instanceof EMO) {
				EMO emo = (EMO)obj;
				switch(index) {
				case 0:
					return emo.getNativeMethod();
					//return "dummy name";
				case 1:
					return Integer.toString(emo.getSize());
					//return "15";
				}
			}
			return "No Return";
		}
		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		@Override
		public Image getImage(Object obj) {
			return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}



	@Override
	public void createPartControl(Composite parent) {
		//Earlier it was TableViewer; 
		//FULL_SELECTION is required for editable columns; At present we are not editing.
		viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);	

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());

		viewer.setInput(getViewSite());	//	For initial view
		//		viewer.setInput(new String[] { "One", "Two", "Three" });  //Original

		//Add a TableLayout in the viewpart
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(80, true));
		layout.addColumnData(new ColumnWeightData(20, true));
		viewer.getTree().setLayout(layout);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setHeaderVisible(true);

		TreeColumn column0 = new TreeColumn(viewer.getTree(),SWT.LEFT);
		column0.setText("Method Name");
		column0.setResizable(true);
		column0.pack();
		TreeColumn column2 = new TreeColumn(viewer.getTree(),SWT.LEFT);
		column2.setText("Size (#statements)");
		column2.setResizable(true);
		column2.pack();

		viewer.expandAll();

		//Note that 'No Editing permission' mentioned in doc loosely means read_only i.e. even program is not allowed to write anything. 
		viewer.setColumnProperties(new String[] {"method", "size"});
		viewer.setCellEditors(new CellEditor[] {new TextCellEditor(), new TextCellEditor()});
		makeActions();
		hookDoubleClickAction();
		contributeToActionBars();
		hookContextMenu();

		/*
		// Create the help context id for the viewer's control
		workbench.getHelpSystem().setHelp(viewer.getControl(), "org.omkar.view.viewplugin.viewer");
		getSite().setSelectionProvider(viewer);
		 */		
	}

	private void hookContextMenu() {		
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				LongMethodView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(identifySegment);
		manager.add(new Separator());
		manager.add(extractSegment);
	}
	//can be used as applying suggested refactorings
	private void fillContextMenu(IMenuManager manager) {
		manager.add(extractSegment);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(identifySegment);
	}

	private void makeActions() {
		identifySegment = new Action() {
			public void run() {
				try {
					emolist = getEMOlist();
				} catch (IOException | BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (emolist.length>0)
					showMessage(emolist[0].getStatements().toString());
				else
					showMessage("No extract method opportunity found!");
				viewer.setContentProvider(new ViewContentProvider());
				extractSegment.setEnabled(true);//Now this action can be visible
			}
		};
		identifySegment.setText("Identify Extract Methods");
		identifySegment.setToolTipText("Identify Extract Method Opportunities");
		identifySegment.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		//		identifySegment.setEnabled(true);

		extractSegment = new Action() {
			public void run() {
				//Code for applying refactoring should go here.
				IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
				if (selection != null && selection.getFirstElement() instanceof EMO) {
					EMO emo = (EMO) selection.getFirstElement();
					CompilationUnit cu = new GetAST().getcompilationUnit(emo.getIFile());
					Refactoring refactoring = new ExtractMethodRefactoring(cu, emo);

					//CHECK IT: If the user switches the files open in the editor, then it may cause a problem; 
					MyRefactoringWizard wizard = new MyRefactoringWizard(refactoring, extractSegment);
					RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard); 
					String dialogTitle = "Error!";  
					try {
						op.run(getSite().getShell(), dialogTitle);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}
			}
		};
		extractSegment.setText("Extract Method Refactoring");
		extractSegment.setToolTipText("Extract Identified Method Opportunity");
		extractSegment.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_ETOOL_SAVEAS_EDIT));


		doubleClickAction = new Action() {
			public void run() {

				IStructuredSelection selection = viewer.getStructuredSelection();

				//			showMessage("Double-click detected on "+obj.toString());
				if (selection.getFirstElement() instanceof EMO) {
					EMO emo = (EMO)selection.getFirstElement();

					IFile file = getIFile();

					try {
						IJavaElement javaElement = JavaCore.create(file);
						ITextEditor editor = (ITextEditor)JavaUI.openInEditor(javaElement);
						AnnotationModel annotationModel = (AnnotationModel)editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());

						Iterator<Annotation> annotationIterator = annotationModel.getAnnotationIterator();
						while(annotationIterator.hasNext()) {
							Annotation currentAnnotation = annotationIterator.next();
							if(currentAnnotation.getType().equals(SegmentAnnotation.SEGMENT)) {
								annotationModel.removeAnnotation(currentAnnotation);
							}
						}
						//CHECK THIS CODE HERE; 
						for(Position position : emo.getPositions()) {
							SegmentAnnotation annotation = null;
							String annotationText = emo.toString();	//This is dummy; 
							annotation = new SegmentAnnotation(SegmentAnnotation.SEGMENT, annotationText);
							annotationModel.addAnnotation(annotation, position);
						}

						int offset = emo.getPositionAt(0).getOffset();
						int length = emo.getPositionAt(0).getLength();
						editor.setHighlightRange(offset, length, true);	//Positions the cursor and thus highlights the line
					} catch (PartInitException | JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
					System.out.println("Object is not of type EMO, Class : " + selection.getClass());


			}
		};
	}
	//	Following will be used to highlight the selected segment in the source code
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(
				viewer.getControl().getShell(),
				"Long Method View",
				message);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		super.dispose();
		//	getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(selectionListener);
	}

	private EMO[] getEMOlist() throws IOException, BadLocationException {
		// TODO Auto-generated method stub

		String filepath = null;
		try {
			filepath = new WorkbenchInfo().getPath();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TobeExtractedMethodList emo = new TobeExtractedMethodList(filepath);
		EMO[] emolist = emo.getEMOList();
		
		
		return emolist;

	}

	IFile getIFile() {	//This method is introduced for testing purpose. Should be modified or removed later.
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		IEditorPart editor = page.getActiveEditor();
		IFile original = ((FileEditorInput) editor.getEditorInput()).getFile();
		return original;
	}
}

