/* $Id$
 *****************************************************************************
 * Copyright (c) 2009-2013 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Luis Sergio Oliveira (euluis)
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

// Copyright (c) 1996-2008 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

// Issue #19 - 'inline' tv tests

package org.argouml.language.cpp.generator;

import java.util.logging.Logger;

import org.argouml.kernel.ProjectManager;
import static org.argouml.model.Model.*;

/**
 * Tests for inline TV
 * 
 * @see GeneratorCpp
 * @author Lukasz Gromanowski
 * @since 0.25.5
 */
public class TestInlineTaggedValue extends BaseTestGeneratorCpp {
    private Object classA;

    /** The Logger for this class */
    private static final Logger LOG = Logger.getLogger(
            TestInlineTaggedValue.class.getName());

    private Object voidType;

    /**
     * The constructor.
     * 
     * @param testName the name of the test
     */
    public TestInlineTaggedValue(java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        classA = getCoreFactory().buildClass("ClassA", getModel());
        voidType = ProjectManager.getManager().getCurrentProject().findType(
                "void");
        getGenerator().setUseSect(Section.SECT_NONE);
        getGenerator().setVerboseDocs(false);
    }
    
    /**
     * Test 1 - not inlined method
     */
    public void testNotInlined() {
        String opName = "fooNotInlined";
        buildOperation(classA, voidType, opName);
        
        String re = "(?m)(?s).*class\\s+ClassA\\s+\\{\\s+public\\:"
            + "\\s+virtual\\s+void\\s+fooNotInlined\\(\\);\\s+\\};(?s).*";
        
        assertMatches(re, getGenerator().generateH(classA), opName);
    }

    /**
     * Test 2 - inlined method, defInClass - method definition inside class
     */
    public void testDefInsideClass() {
        String opName = "fooInlined_Style1";
        Object defInClassOp = buildOperation(classA, voidType, opName);
        
        profile.applyCppOperationStereotype(defInClassOp);
        profile.applyInlineTaggedValue2Operation(defInClassOp, "defInClass");

        String re = "(?m)(?s).*class\\s+ClassA\\s+\\{\\s+public\\:"
            + "\\s+virtual\\s+void\\s+fooInlined_Style1\\(\\)"
            + "\\s+\\/\\*\\s+\\{inline=defInClass\\}\\*\\/\\s+\\{\\s*\\}"
            + "\\s+\\};(?s).*";
        
        assertMatches(re, getGenerator().generateH(classA), opName);
    }

    /**
     * Test 3 - inlined method, inlineKeyDefInClass - method definition inside 
     * class with 'inline' keyword
     */
    public void testKeyAndDefInsideClass() {
        String opName = "fooInlined_Style2";
        Object inlineKeyDefInClassOp = buildOperation(classA, voidType,
                opName);
        
        profile.applyCppOperationStereotype(inlineKeyDefInClassOp);
        profile.applyInlineTaggedValue2Operation(inlineKeyDefInClassOp,
                "inlineKeyDefInClass");

        String re = "(?m)(?s).*class\\s+ClassA\\s+\\{\\s+public\\:"
            + "\\s+virtual\\s+inline\\s+void\\s+fooInlined_Style2\\(\\)\\s+"
            + "\\/\\*\\s+\\{inline=inlineKeyDefInClass\\}\\*\\/\\s+\\{\\s+\\}"
            + "\\s+\\};(?s).*";
        
        assertMatches(re, getGenerator().generateH(classA), opName);
    }

    /**
     * Test 4 - inlined method, inlineKeyDefOutClass - method definition 
     * outside class with 'inline' keyword.
     */
    public void testKeyAndDefOutsideClass() {
        String opName = "fooInlined_Style3";
        Object inlineKeyDefOutClassOp = buildOperation(classA, voidType,
                opName);
        
        profile.applyCppOperationStereotype(inlineKeyDefOutClassOp);
        profile.applyInlineTaggedValue2Operation(inlineKeyDefOutClassOp,
                "inlineKeyDefOutClass");

        String re = "(?m)(?s).*class\\s+ClassA\\s+\\{\\s+public\\:"
            + "\\s+virtual\\s+inline\\s+void\\s+fooInlined_Style3\\(\\);"
            + "\\s+\\/\\*\\s+\\{inline=inlineKeyDefOutClass\\}\\*\\/\\s+\\};"
            + "\\s+inline\\s+void\\s+ClassA\\:\\:fooInlined_Style3\\(\\)\\s+"
            + "\\/\\*\\s+\\{inline=inlineKeyDefOutClass\\}\\*\\/\\s+\\{\\s+\\}"
            + "(?s).*";
        
        assertMatches(re, getGenerator().generateH(classA), opName);
    }

    /**
     * Test 5 - inlined method, defOutClass - method definition outside class
     * without 'inline' keyword
     */
    public void testDefOutsideClass() {
        String opName = "fooInlined_Style4";
        Object defOutClassOp = buildOperation(classA, voidType, opName);
        
        profile.applyCppOperationStereotype(defOutClassOp);
        profile.applyInlineTaggedValue2Operation(defOutClassOp,
                "defOutClass");

        String re = "(?m)(?s).*class\\s+ClassA\\s+\\{\\s+public\\:"
            + "\\s+virtual\\s+void\\s+fooInlined_Style4\\(\\);"
            + "\\s+\\/\\*\\s+\\{inline=defOutClass\\}\\*\\/\\s+\\};"
            + "\\s+void\\s+ClassA\\:\\:fooInlined_Style4\\(\\)"
            + "\\s+\\/\\*\\s+\\{inline=defOutClass\\}\\*\\/\\s+\\{\\s+\\}"
            + "(?s).*";
        
        assertMatches(re, getGenerator().generateH(classA), opName);
    }

    private void assertMatches(String re, String code, String opName) {
        if (!code.matches(re)) {
            LOG.info("Code for '" + opName + "':\n");
            LOG.info(code);
        }
        assertTrue(code.matches(re));
    }

}
