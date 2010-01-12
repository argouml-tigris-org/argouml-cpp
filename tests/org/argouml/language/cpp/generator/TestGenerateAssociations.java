/* $Id$
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    euluis
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

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
        String resA[] = {"\\s*class\\s+ClassB;\\s*", 
            "\\s*ClassB\\s+\\*\\s*theAssociation\\s*;\\s*"};
        assertMatches(resA, codeA, "Mismatch in code for ClassA!");
        String codeB = getGenerator().generateH(classB);
        String resB[] = {"\\s*class\\s+ClassA;\\s*", 
            "\\s*ClassA\\s+\\*\\s*theAssociation\\s*;\\s*"};
        assertMatches(resB, codeB, "Mismatch in code for classB!");
    }

    /**
     * <a href="http://argouml.tigris.org/issues/show_bug.cgi?id=4541"> Issue
     * #4541</a> in ArgoUML's DB.
     */
    public void testGenerateBidirectionalComposition() {
        setUpComposition();
        String codeA = getGenerator().generateH(classA);
        String[] resA = {"\\s*#include\\s+\"ClassB\\.h\"\\s*",
            "\\s*ClassB\\s+theAssociation\\s*;\\s*"};
        assertMatches(resA, codeA, "Mismatch in code for ClassA!");
        String codeB = getGenerator().generateH(classB);
        String[] resB = {"\\s*class\\s+ClassA;\\s*", 
            "\\s*ClassA\\s+\\*\\s*theAssociation\\s*;\\s*"};
        assertMatches(resB, codeB, "Mismatch in code for classB!");
    }

    private void assertMatches(String[] res, String code, String logMsg) {
        int i = 0;
        boolean match = false;
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.matches(res[i])) {
                i++;
                if (i == res.length) {
                    match = true;
                    break;
                }
            }
        }
        if (!match) {
            LOG.warn(logMsg);
            LOG.warn(code);
        }
        assertTrue(logMsg, match);
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
