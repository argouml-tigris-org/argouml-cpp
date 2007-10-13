// $Id$
// Copyright (c) 2007 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason. IN NO EVENT SHALL THE
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

package org.argouml.language.cpp.generator;

import static org.argouml.model.Model.getAggregationKind;
import static org.argouml.model.Model.getCoreFactory;
import static org.argouml.model.Model.getCoreHelper;
import static org.argouml.model.Model.getFacade;

import org.apache.log4j.Logger;

/**
 * Tests for the GeneratorCpp class in what regards generation of associations.
 * 
 * @see GeneratorCpp
 * @author Luis Sergio Oliveira (euluis)
 * @since 0.25.4
 */
public class TestGenerateAssociations extends BaseTestGeneratorCpp {

    /** The Logger for this class */
    private static final Logger LOG = Logger
            .getLogger(TestGenerateAssociations.class);
    private Object classA;
    private Object classB;

    /**
     * The constructor.
     * 
     * @param testName the name of the test
     */
    public TestGenerateAssociations(String testName) {
        super(testName);
    }

    /**
     * <a href="http://argouml.tigris.org/issues/show_bug.cgi?id=4541"> Issue
     * #4541</a> in ArgoUML's DB.
     */
    public void testGenerateBidirectionalAggregation() {
        setUpAggregation();
        String codeA = getGenerator().generateH(classA);
        String reA = "(?m)(?s)#ifndef\\s+ClassA_h\\s+"
                + "#define\\s+ClassA_h\\s+"
                + "class\\s+ClassB;\\s+" 
                + "class\\s+ClassA\\s*\\{"
                + "\\s*public\\s*:\\s*"
                + "ClassB\\s+\\*\\s*theAssociation\\s*;\\s*"
                + "}\\s*;\\s+#endif\\s*";
        assertMatches(reA, codeA, "Mismatch in code for ClassA!");
        String codeB = getGenerator().generateH(classB);
        String reB = "(?m)(?s)#ifndef\\s+ClassB_h\\s+"
            + "#define\\s+ClassB_h\\s+"
            + "class\\s+ClassA;\\s+"
            + "class\\s+ClassB\\s*\\{"
            + "\\s*public\\s*:\\s*" 
            + "ClassA\\s+\\*\\s*theAssociation\\s*;\\s*"
            + "}\\s*;\\s*#endif\\s*";
        assertTrue(codeB.matches(reB));
        assertMatches(reB, codeB, "Mismatch in code for classB!");
    }

    /**
     * <a href="http://argouml.tigris.org/issues/show_bug.cgi?id=4541"> Issue
     * #4541</a> in ArgoUML's DB.
     */
    public void testGenerateBidirectionalComposition() {
        setUpComposition();
        String codeA = getGenerator().generateH(classA);
        String reA = "(?m)(?s)#ifndef\\s+ClassA_h\\s+"
            + "#define\\s+ClassA_h\\s+"
            + "#include\\s+\"ClassB\\.h\"\\s+" 
            + "class\\s+ClassA\\s*\\{"
            + "\\s*public\\s*:\\s*"
            + "ClassB\\s+theAssociation\\s*;\\s*"
            + "}\\s*;\\s+#endif\\s*";
        assertMatches(reA, codeA, "Mismatch in code for ClassA!");
        String codeB = getGenerator().generateH(classB);
        String reB = "(?m)(?s)#ifndef\\s+ClassB_h\\s+"
            + "#define\\s+ClassB_h\\s+"
            + "class\\s+ClassA;\\s+"
            + "class\\s+ClassB\\s*\\{"
            + "\\s*public\\s*:\\s*" 
            + "ClassA\\s+\\*\\s*theAssociation\\s*;\\s*"
            + "}\\s*;\\s*#endif\\s*";
        assertMatches(reB, codeB, "Mismatch in code for classB!");
    }

    private void assertMatches(String re, String code, String logMsg) {
        if (!code.matches(re)) {
            LOG.warn(logMsg);
            LOG.warn(code);
        }
        assertTrue(code.matches(re));
    }

    private void setUpAggregation() {
        Object classAAssociationEnd = setUpAssociation();
        getCoreHelper().setAggregation(classAAssociationEnd,
                getAggregationKind().getAggregate());
    }

    private Object setUpAssociation() {
        classA = getCoreFactory().buildClass("ClassA", getModel());
        classB = getCoreFactory().buildClass("ClassB", getModel());
        Object association = getCoreFactory().buildAssociation(classA, true,
                classB, true, "theAssociation");
        Object classAAssociationEnd = getFacade().getAssociationEnd(classA,
                association);
        return classAAssociationEnd;
    }

    private void setUpComposition() {
        Object classAAssociationEnd = setUpAssociation();
        getCoreHelper().setAggregation(classAAssociationEnd,
                getAggregationKind().getComposite());
    }

}
