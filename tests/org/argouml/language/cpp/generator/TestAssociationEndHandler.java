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

import org.argouml.language.cpp.profile.ProfileCpp;

/**
 * Unit tests for AssociationEndHandler. 
 *
 * @author Luis Sergio Oliveira (euluis)
 */
public class TestAssociationEndHandler extends BaseTestGeneratorCpp {

    private Object classA;
    private Object classB;
    private Object association;
    private Object classAAssociationEnd;
    private Object classBAssociationEnd;

    /**
     * The constructor.
     * 
     * @param testName the name of the test
     */
    public TestAssociationEndHandler(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        classA = getCoreFactory().buildClass("ClassA", getModel());
        classB = getCoreFactory().buildClass("ClassB", getModel());
        association = getCoreFactory().buildAssociation(classA, true,
                classB, true, "theComposition");
        classAAssociationEnd = getFacade().getAssociationEnd(classA,
                association);
        classBAssociationEnd = getFacade().getAssociationEnd(classB,
                association);
    }

    public void testSimpleComposed() {
        getCoreHelper().setAggregation(classAAssociationEnd,
                getAggregationKind().getComposite());
        AssociationEndHandler classAHandler = new AssociationEndHandler(
                classAAssociationEnd);
        classAHandler.pre();
        assertTaggedValuesNumEquals(0, classAAssociationEnd);
        assertTaggedValuesNumEquals(0, classBAssociationEnd);
        classAHandler.post();
        assertTaggedValuesNumEquals(0, classAAssociationEnd);
        assertTaggedValuesNumEquals(0, classBAssociationEnd);
    }

    public void testSimpleComposedOtherEnd() {
        getCoreHelper().setAggregation(classAAssociationEnd,
                getAggregationKind().getComposite());
        AssociationEndHandler handler = new AssociationEndHandler(
                classBAssociationEnd);
        handler.pre();
        assertEquals("true", getFacade().getTaggedValueValue(
                classAAssociationEnd, ProfileCpp.TV_NAME_POINTER));
        assertTaggedValuesNumEquals(1, classAAssociationEnd);
        assertTaggedValuesNumEquals(0, classBAssociationEnd);
        handler.post();
        assertTaggedValuesNumEquals(0, classAAssociationEnd);
        assertTaggedValuesNumEquals(0, classBAssociationEnd);
    }

    private void assertTaggedValuesNumEquals(int expectedNum,
            Object modelElement) {
        assertEquals(expectedNum, getFacade().getTaggedValuesCollection(
                modelElement).size());
    }

}
