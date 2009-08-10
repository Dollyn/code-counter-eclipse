/*******************************************************************************
 * Copyright (c) 2008, 2009 Dollyn and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Dollyn ---- dollyn.sun@gmail.com
 *******************************************************************************/
package com.google.code.jcodecounter.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;

/**
 * The cache of the statistics result.
 * @author Dollyn
 */
public class ResultCache {
	
	private Map<IJavaElement, CodeInfo> compilationUnitCache = new HashMap<IJavaElement, CodeInfo>();
	
	private Map<IJavaElement, CodeInfo> otherCache = new HashMap<IJavaElement, CodeInfo>();
	
	public CodeInfo getInfo(IJavaElement element) {
		switch(element.getElementType()) {
		case IJavaElement.COMPILATION_UNIT:
			return compilationUnitCache.get(element);
		default:
			return otherCache.get(element);
		}		
	}
	
	public void putInfo(IJavaElement element, CodeInfo info) {
		switch(element.getElementType()) {
		case IJavaElement.COMPILATION_UNIT:
			compilationUnitCache.put(element, info);
			break;
		default:
			otherCache.put(element, info);
		}		
	}
}
