package com.google.code.jcodecounter.ui;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.filters.EmptyInnerPackageFilter;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;

import com.google.code.jcodecounter.core.CodeInfo;
import com.google.code.jcodecounter.core.CodeCounter;


public class StatisticsResultView extends ViewPart {

	private TreeViewer viewer;
	private CodeCounter statistician = new CodeCounter();
	private JavaElementLabelProvider delegateLabelProvider = new JavaElementLabelProvider();
	private StatusLineContributionItem statusLine;
	
	@Override
	public void createPartControl(Composite parent) {
		FilteredTree tree = new FilteredTree(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL, new PatternFilter());
		viewer = tree.getViewer();
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setHeaderVisible(true);
		viewer.setContentProvider(new StandardJavaElementContentProvider());
		createColumns();
		//viewer.setLabelProvider(new JavaElementLabelProvider());
		viewer.setInput(JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()));
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				updateStatusLine();
			}
			
		});
		
		viewer.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof IJavaElement) {
					if (element instanceof IPackageFragmentRoot) {
						try {
							if (((IPackageFragmentRoot)element).getKind() == IPackageFragmentRoot.K_SOURCE) {
								return true;
							}
						} catch (JavaModelException e) {
							e.printStackTrace();
							return false;
						}
					} else {
						return true;
					}
				}
				return false;
			}
			
		});

		viewer.addFilter(new EmptyInnerPackageFilter());
		
		viewer.setComparator(new JavaElementComparator());
		IActionBars actionbar = getViewSite().getActionBars();
		IStatusLineManager statusManager = actionbar.getStatusLineManager();
		statusLine = new StatusLineContributionItem("Statistics");
		
		statusManager.add(statusLine);
	}
	
	private void createColumns() {
		TreeViewerColumn elementNameColumn = new TreeViewerColumn(viewer, SWT.NONE);
		elementNameColumn.getColumn().setText("Element");
		elementNameColumn.getColumn().setWidth(200);
		elementNameColumn.setLabelProvider(new ColumnLabelProvider() {		
			
			@Override
			public String getText(Object element) {
				return delegateLabelProvider.getText(element);
			}
			
			@Override
			public Image getImage(Object element) {
				return delegateLabelProvider.getImage(element);
			}
			
		});
		
		TreeViewerColumn linesOfAllColumn = new TreeViewerColumn(viewer, SWT.NONE);
		linesOfAllColumn.getColumn().setText("All");
		linesOfAllColumn.getColumn().setWidth(50);
		linesOfAllColumn.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element) {
				if (!(element instanceof IJavaElement)) {
					return "";
//					return super.getText(element);
				}

				CodeInfo info = statistician.getCodeInfo((IJavaElement)element);
				return String.valueOf(info.lines_all);
			}
			
		});
		
		TreeViewerColumn linesOfCodeColumn = new TreeViewerColumn(viewer, SWT.NONE);
		linesOfCodeColumn.getColumn().setText("Code");
		linesOfCodeColumn.getColumn().setWidth(50);
		linesOfCodeColumn.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element) {
				if (!(element instanceof IJavaElement)) {
					return "";
//					return super.getText(element);
				}

				CodeInfo info = statistician.getCodeInfo((IJavaElement)element);
				return String.valueOf(info.lines_code);
			}
			
		});
		
		TreeViewerColumn linesOfCommentsColumn = new TreeViewerColumn(viewer, SWT.NONE);
		linesOfCommentsColumn.getColumn().setText("Comments");
		linesOfCommentsColumn.getColumn().setWidth(70);
		linesOfCommentsColumn.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element) {
				if (!(element instanceof IJavaElement)) {
					return "";
//					return super.getText(element);
				}

				CodeInfo info = statistician.getCodeInfo((IJavaElement)element);
				return String.valueOf(info.lines_comment);
			}
			
		});
		
		TreeViewerColumn linesOfBlankLinesColumn = new TreeViewerColumn(viewer, SWT.NONE);
		linesOfBlankLinesColumn.getColumn().setText("Blank Lines");
		linesOfBlankLinesColumn.getColumn().setWidth(80);
		linesOfBlankLinesColumn.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element) {
				if (!(element instanceof IJavaElement)) {
					return "";
//					return super.getText(element);
				}

				CodeInfo info = statistician.getCodeInfo((IJavaElement)element);
				return String.valueOf(info.lines_blank);
			}
			
		});		
		
	}

	public CodeCounter getStatistician() {
		return statistician;
	}
	
	public TreeViewer getViewer() {
		return viewer;
	}

	public void countAll() {

		Job count = new Job("count") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("counting", IProgressMonitor.UNKNOWN);
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IJavaModel model = JavaCore.create(root);
				try {
					statistician.runCounting(model.getJavaProjects());
					getSite().getShell().getDisplay().asyncExec(new Runnable() {

						public void run() {
							getViewer().refresh();
						}
						
					});
					
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
			
		};
		count.setUser(true);
		count.schedule();
	}

	
	private void updateStatusLine() {
		IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
		int lines_all = 0;
		int lines_code = 0;
		int lines_comment = 0;
		int lines_blank = 0;
		if (ss != null && !ss.isEmpty()) {
			for (Object obj : ss.toArray()) {
				if (obj instanceof IJavaElement) {
					IJavaElement je = (IJavaElement)obj;
					CodeInfo info = statistician.getCodeInfo(je);
					if (info.lines_all != -1) lines_all += info.lines_all;
					if (info.lines_blank != -1) lines_blank += info.lines_blank;
					if (info.lines_code != -1) lines_code += info.lines_code;
					if (info.lines_comment != -1) lines_comment += info.lines_comment;
				}
			}
		}
		statusLine.setText(lines_all + ", " + lines_code + ", " + lines_comment + ", " + lines_blank);
	}	
	
	@Override
	public void setFocus() {
		
	}

}
