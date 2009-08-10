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

/**
 * The info of code.
 * @author Dollyn
 */
public class CodeInfo {

	public int lines_all;
	public int lines_code;
	public int lines_comment;
	public int lines_blank;
	
	public CodeInfo() {
		lines_all = -1;
		lines_code = -1;
		lines_comment = -1;
		lines_blank = -1;
	}
	
	public void reset() {
		lines_all = 0;
		lines_code = 0;
		lines_comment = 0;
		lines_blank = 0;
	}
	
	public String toString() {
		return "lines_all: " + lines_all + "\n" + 
			   "lines_code: " + lines_code + "\n" +
			   "lines_coment: " + lines_comment + "\n" +
			   "lines_blank: " + lines_blank + "\n";
	}
}
