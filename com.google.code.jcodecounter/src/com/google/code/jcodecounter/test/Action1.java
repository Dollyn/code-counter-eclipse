package com.google.code.jcodecounter.test;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.google.code.jcodecounter.core.CodeCounter;

public class Action1 implements IObjectActionDelegate{

	private CodeCounter statistician = new CodeCounter();
	
	public Action1() {
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		
	}

	public void run(IAction action) {
		
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			Object obj = ss.getFirstElement();
			if (obj instanceof ICompilationUnit) {
				ICompilationUnit cu = (ICompilationUnit)ss.getFirstElement();
				statistician.runCounting(cu);
				statistician.printResult(cu);
			} else if (obj instanceof IProject) {
				IJavaProject javaProject = JavaCore.create((IProject)obj);
				statistician.runCounting(javaProject);
				statistician.printResult(javaProject);
			}
			
		}
	}

}
