// $Id$
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

import static org.argouml.model.Model.getCoreHelper;
import static org.argouml.model.Model.getMetaTypes;
import static org.argouml.model.Model.getUmlFactory;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.argouml.kernel.ProjectManager;
import org.argouml.language.cpp.profile.ProfileCpp;
import org.argouml.model.IllegalModelElementConnectionException;
import org.argouml.model.Model;
import static org.argouml.model.Model.*;
import org.argouml.moduleloader.ModuleInterface;

/**
 * Tests for inline TV
 * 
 * @see GeneratorCpp
 * @author Lukasz Gromanowski
 * @since 0.25.5
 */
public class TestInlineTaggedValue extends BaseTestGeneratorCpp {
    private Object class_A;
    private Object class_B;
    private Object class_C;
    private Object class_D;
    private Object class_E;
    
    private ProfileCpp profileCpp;

    /** The Logger for this class */
    private static final Logger LOG = Logger.getLogger(
            TestInlineTaggedValue.class);

    /**
     * The constructor.
     * 
     * @param testName the name of the test
     */
    public TestInlineTaggedValue(java.lang.String testName) {
        super(testName);
    }

    /**
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(TestInlineTaggedValue.class);
        return suite;
    }

    /**
     * to enable debugging in poor IDEs...
     * 
     * @param args the arguments given on the command line
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        class_A = getCoreFactory().buildClass("Class_A", getModel());
        class_B = getCoreFactory().buildClass("Class_B", getModel());
        class_C = getCoreFactory().buildClass("Class_C", getModel());
        class_D = getCoreFactory().buildClass("Class_D", getModel());
        class_E = getCoreFactory().buildClass("Class_E", getModel());
        profileCpp = new ProfileCpp(getModels());
    }
    
    /**
     * Test 1 - not inlined method
     */
    public void testNotInlined() {
        Object voidType = ProjectManager.getManager().getCurrentProject()
                .findType("void");

        Object fooNotInlined = buildOperation(class_A, voidType,
                "fooNotInlined");
        
        String re = "(?m)(?s).*class\\s+Class_A\\s+\\{\\s+public\\:"
            + "\\s+virtual\\s+void\\s+fooNotInlined\\(\\);\\s+\\};(?s).*";
        
        String code = getGenerator().generateH(class_A);
        
        if (!code.matches(re)) {
            LOG.info("Code for 'fooNotInlined':\n");
            LOG.info(code);
        }

        assertTrue(code.matches(re));
    }

    /**
     * Test 2 - inlined method, style 1 - method definition inside class
     */
    public void testDefInsideClass() {
        Object voidType = ProjectManager.getManager().getCurrentProject()
                .findType("void");

        Object fooInlined_Style_1 = buildOperation(class_B, voidType,
                "fooInlined_Style1");
        
        profileCpp.applyInlineTaggedValue2Operation(fooInlined_Style_1,
                "defInClass");

        String re = "(?m)(?s).*class\\s+Class_B\\s+\\{\\s+public\\:"
            + "\\s+virtual\\s+void\\s+fooInlined_Style1\\(\\)"
            + "\\s+\\/\\*\\s+\\{inline=defInClass\\}\\*\\/\\s+\\{\\s*\\}"
            + "\\s+\\};(?s).*";
        
        String code = getGenerator().generateH(class_B);
        
        if (!code.matches(re)) {
            LOG.info("Code for 'fooInlined_Style1':\n");
            LOG.info(code);
        }

        assertTrue(code.matches(re));
    }

    /**
     * Test 3 - inlined method, style 2 - method definition inside class
     * with 'inline' keyword
     */
    public void testKeyAndDefInsideClass() {
        Object voidType = ProjectManager.getManager().getCurrentProject()
                .findType("void");

        Object fooInlined_Style_2 = buildOperation(class_C, voidType,
                "fooInlined_Style2");
        
        profileCpp.applyInlineTaggedValue2Operation(fooInlined_Style_2,
                "inlineKeyDefInClass");

        String re = "(?m)(?s).*class\\s+Class_C\\s+\\{\\s+public\\:"
            + "\\s+virtual\\s+inline\\s+void\\s+fooInlined_Style2\\(\\)\\s+"
            + "\\/\\*\\s+\\{inline=inlineKeyDefInClass\\}\\*\\/\\s+\\{\\s+\\}"
            + "\\s+\\};(?s).*";
        
        String code = getGenerator().generateH(class_C);

        if (!code.matches(re)) {
            LOG.info("Code for 'fooInlined_Style2':\n");
            LOG.info(code);
        }

        assertTrue(code.matches(re));
    }

    /**
     * Test 4 - inlined method, style 3 - method definition outside class
     * with 'inline' keyword
     */
    public void testKeyAndDefOutsideClass() {
        Object voidType = ProjectManager.getManager().getCurrentProject()
                .findType("void");

        Object fooInlined_Style_3 = buildOperation(class_D, voidType,
                "fooInlined_Style3");
        
        profileCpp.applyInlineTaggedValue2Operation(fooInlined_Style_3,
                "inlineKeyDefOutClass");

        String re = "(?m)(?s).*class\\s+Class_D\\s+\\{\\s+public\\:"
            + "\\s+virtual\\s+inline\\s+void\\s+fooInlined_Style3\\(\\);"
            + "\\s+\\/\\*\\s+\\{inline=inlineKeyDefOutClass\\}\\*\\/\\s+\\};"
            + "\\s+inline\\s+void\\s+Class_D\\:\\:fooInlined_Style3\\(\\)\\s+"
            + "\\/\\*\\s+\\{inline=inlineKeyDefOutClass\\}\\*\\/\\s+\\{\\s+\\}"
            + "(?s).*";
        
        String code = getGenerator().generateH(class_D);

        if (!code.matches(re)) {
            LOG.info("Code for 'fooInlined_Style3':\n");
            LOG.info(code);
        }

        assertTrue(code.matches(re));
    }

    /**
     * Test 5 - inlined method, style 4 - method definition outside class
     * without 'inline' keyword
     */
    public void testDefOutsideClass() {
        Object voidType = ProjectManager.getManager().getCurrentProject()
                .findType("void");

        Object fooInlined_Style_4 = buildOperation(class_E, voidType,
                "fooInlined_Style4");
        
        profileCpp.applyInlineTaggedValue2Operation(fooInlined_Style_4,
                "defOutClass");

        String re = "(?m)(?s).*class\\s+Class_E\\s+\\{\\s+public\\:"
            + "\\s+virtual\\s+void\\s+fooInlined_Style4\\(\\);"
            + "\\s+\\/\\*\\s+\\{inline=defOutClass\\}\\*\\/\\s+\\};"
            + "\\s+void\\s+Class_E\\:\\:fooInlined_Style4\\(\\)"
            + "\\s+\\/\\*\\s+\\{inline=defOutClass\\}\\*\\/\\s+\\{\\s+\\}"
            + "(?s).*";
        
        String code = getGenerator().generateH(class_E);

        if (!code.matches(re)) {
            LOG.info("Code for 'fooInlined_Style4':\n");
            LOG.info(code);
        }

        assertTrue(code.matches(re));
    }

}
