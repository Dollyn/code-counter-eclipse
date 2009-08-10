package com.google.code.jcodecounter.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class CountAllViewActionDelegate implements IViewActionDelegate {

	private StatisticsResultView view;
	
	public void init(IViewPart view) {
		this.view = (StatisticsResultView)view;
	}

	public void run(IAction action) {
		view.countAll();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		
	}

}
