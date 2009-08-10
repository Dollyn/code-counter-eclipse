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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.rules.FastPartitioner;

/**
 * The statistician(Í³¼ÆÆ÷)
 * @author Dollyn
 */
public class CodeCounter {
	
	private static final SimpleJavaPartitionScanner patitionScanner = new SimpleJavaPartitionScanner();
	private static final String JAVA_PARTITIONING = "java_partitioning";
	
	private ResultCache cache = new ResultCache();

	public void runCounting(IJavaElement[] elements) {
		for (IJavaElement element : elements) {
			switch (element.getElementType()) {
			case IJavaElement.JAVA_PROJECT:
				runCounting((IJavaProject)element);
				break;
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				runCounting((IPackageFragmentRoot)element);
				break;
			case IJavaElement.PACKAGE_FRAGMENT:
				runCounting((IPackageFragment)element);
				break;
			case IJavaElement.COMPILATION_UNIT:
				runCounting((ICompilationUnit)element);
				break;
			}
		}
	}
	
	public void runCounting(IJavaProject project) {
		CodeInfo projectInfo = getCodeInfo(project);
		projectInfo.reset();
		
		try {
			for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					runCounting(root);
					CodeInfo rootInfo = getCodeInfo(root);
					projectInfo.lines_all += rootInfo.lines_all;
					projectInfo.lines_blank += rootInfo.lines_blank;
					projectInfo.lines_code += rootInfo.lines_code;
					projectInfo.lines_comment += rootInfo.lines_comment;
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	} 
	
	public void runCounting(IPackageFragmentRoot root) {
		// Only statistic the source pkg root.
		try {
			if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
				return;
			}
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
		
		CodeInfo rootInfo = getCodeInfo(root);
		rootInfo.reset();
		try {
			for (IJavaElement pkg : root.getChildren()) {
				if (pkg instanceof IPackageFragment) {
					runCounting((IPackageFragment)pkg);
					CodeInfo pkgInfo = getCodeInfo(pkg);
					rootInfo.lines_all += pkgInfo.lines_all;
					rootInfo.lines_blank += pkgInfo.lines_blank;
					rootInfo.lines_code += pkgInfo.lines_code;
					rootInfo.lines_comment += pkgInfo.lines_comment;
				}				
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	} 
	
	public void runCounting(IPackageFragment pkg) {
		CodeInfo pkgInfo = getCodeInfo(pkg);
		pkgInfo.reset();
		try {
			for (ICompilationUnit cu : pkg.getCompilationUnits()) {
				runCounting(cu);
				CodeInfo cuInfo = getCodeInfo(cu);
				pkgInfo.lines_all += cuInfo.lines_all;
				pkgInfo.lines_blank += cuInfo.lines_blank;
				pkgInfo.lines_code += cuInfo.lines_code;
				pkgInfo.lines_comment += cuInfo.lines_comment;
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	} 
	
	public void runCounting(ICompilationUnit cu) {
		// Get the info corresponding to the compilation unit
		CodeInfo info = getCodeInfo(cu);
		info.reset();	
		
		// 
		try {
			String source = cu.getSource();
			// Use the JFace class Document to count lines of source code.
			Document doc = new Document(source);
			info.lines_all = doc.getNumberOfLines();

			// Use the JFace-Text Document partitioner to partition the document.
			IDocumentExtension3 extension3= (IDocumentExtension3) doc;
			IDocumentPartitioner partitioner= new FastPartitioner(patitionScanner, SimpleJavaPartitionScanner.JAVA_PARTITION_TYPES);
			extension3.setDocumentPartitioner(JAVA_PARTITIONING, partitioner);
			partitioner.connect(doc);
		
			// line-by-line
			for (int i = 0; i < info.lines_all; i++) {
				try {
					IRegion region = doc.getLineInformation(i);
					String line = doc.get(region.getOffset(), region.getLength());
//					String t = doc.getContentType(JAVA_PARTITIONING, region.getOffset()+1, false);
//					System.out.println(line);				
//					System.out.println(t);					
					// check blank lines
					if (StringUtil.isEmpty(line)) {
						// blank lines in comments are also treated as BLANK_LINE
						info.lines_blank++;
						String type = doc.getContentType(JAVA_PARTITIONING, region.getOffset(), false);
//						if (type.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
//							
//						}
						// If the blank line is in comments, the comments line-number should also grow up.
						if (SimpleJavaPartitionScanner.JAVA_DOC.equals(type) || SimpleJavaPartitionScanner.JAVA_MULTILINE_COMMENT.equals(type)) {
							info.lines_comment++;
						}
					} else {
						// Check each position of the line, in case that the line has both comments and code.
						// Or in case that the line is where the comment begins, if we only check the first position,
						// the content type will be IDocument.DEFAULT_CONTENT_TYPE, so...
						boolean comment = false;
						boolean singleLineComment = false;
						boolean code = false;
						for (int j = 0; j < region.getLength(); j++) {
							String type = doc.getContentType(JAVA_PARTITIONING, region.getOffset() + j, false);
							if (SimpleJavaPartitionScanner.JAVA_DOC.equals(type) 
									|| SimpleJavaPartitionScanner.JAVA_MULTILINE_COMMENT.equals(type)) {
								comment = true;
							} else if (SimpleJavaPartitionScanner.JAVA_SINGLELINE_COMMENT.equals(type)) {
								singleLineComment = true;
							} else {
								if (doc.getChar(region.getOffset() + j) != ' ') {
									code = true;
								}
							}
							
							// if single line comment, then the right-part of this line are all single-line comment
							if (singleLineComment) {
								break;
							}
							
							if ((singleLineComment || comment) && code) {
								break;
							}
						}
						
						if (code) {
							info.lines_code++;
						}
						
						if (comment || singleLineComment) {
							info.lines_comment++;
						}
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				} catch (BadPartitioningException e) {
					e.printStackTrace();
				}				
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
	}
		
	public void printResult(IJavaElement cu) {
		CodeInfo info = getCodeInfo(cu);
		System.out.println(info.toString());
	}
	
	public CodeInfo getCodeInfo(IJavaElement element) {
		// if there is not any info in the cache, then create one
		CodeInfo info = cache.getInfo(element);
		if (info == null) {
			info = new CodeInfo();
			cache.putInfo(element, info);
		}		
		return info;
	}
	
//	private int determinElementsType(IJavaElement[] elements) {
//		if (elements.length == 0) {
//			return UNKNOWN;
//		}
//		
//		int type = UNKNOWN;
//		for (IJavaElement element : elements) {
//			if (type == UNKNOWN) {
//				type = element.getElementType();
//			} else {
//				if (type == element.getElementType()) {
//					continue;
//				} else {
//					return UNKNOWN;
//				}				
//			}
//		}
//		return type;
//	}
}
