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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class SimpleJavaPartitionScanner extends RuleBasedPartitionScanner {

	public static final String JAVA_DOC = "P_ID_JAVADOC";
	public static final String JAVA_MULTILINE_COMMENT = "P_ID_MULTILINE_COMMENT";
	public static final String JAVA_SINGLELINE_COMMENT = "P_ID_SINGLELINE_COMMENT";
	
	public final static String[] JAVA_PARTITION_TYPES= new String[] { JAVA_MULTILINE_COMMENT, JAVA_DOC, JAVA_SINGLELINE_COMMENT };
	
	@SuppressWarnings("unchecked")
	public SimpleJavaPartitionScanner() {
		super();
		IToken javaDoc= new Token(JAVA_DOC);
		IToken comment= new Token(JAVA_MULTILINE_COMMENT);
		IToken singleLineComment = new Token(JAVA_SINGLELINE_COMMENT);

		List rules= new ArrayList();
		// Add rule for single line comments.
		rules.add(new EndOfLineRule("//", singleLineComment)); 

		// Add rule for strings and character constants.
		//rules.add(new SingleLineRule("\"", "\"", Token.UNDEFINED, '\\')); 
		//rules.add(new SingleLineRule("'", "'", Token.UNDEFINED, '\\')); 

		// Add special case word rule.
		///rules.add(new WordPredicateRule(comment));

		// Add rules for multi-line comments and javadoc.
		rules.add(new MultiLineRule("/**", "*/", javaDoc, (char) 0, true)); 
		rules.add(new MultiLineRule("/*", "*/", comment, (char) 0, true)); 

		IPredicateRule[] result= new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);

	}
}
