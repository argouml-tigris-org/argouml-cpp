// $Id$
// Copyright (c) 2006-2009 The Regents of the University of California. All
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

package org.argouml.language.cpp.notation;

import static org.argouml.language.cpp.Helper.getModel;
import static org.argouml.language.cpp.Helper.getModels;
import static org.argouml.language.cpp.Helper.newModel;
import junit.framework.TestCase;

import org.argouml.model.Model;
import org.argouml.notation.NotationSettings;
import org.argouml.language.cpp.profile.ProfileCpp;

/**
 * Tests for the ModelElementNameNotationCpp class.
 * 
 * @author Luis Sergio Oliveira (euluis)
 */
public class TestModelElementNameNotationCpp extends TestCase {

    private Object theClass;

    private ModelElementNameNotationCpp meNotation;

    private NotationSettings settings;
    
    private Object baseClass;

    private Object generalization;
    
    private ProfileCpp profile;

    protected void setUp() throws Exception {
        super.setUp();
        newModel();
        profile = new ProfileCpp(getModels());
        theClass = Model.getCoreFactory().buildClass("TheClass", getModel());
        meNotation = new ModelElementNameNotationCpp(theClass);
        settings = NotationSettings.getDefaultSettings();
    }

    public void testToStringForClassEmptyArgs() {
        String meNameCpp = meNotation.toString(theClass, settings);
        assertNotNull(meNameCpp);
        assertEquals("No curly braces in Name", -1, meNameCpp.lastIndexOf('{'));
        assertEquals("class " + Model.getFacade().getName(theClass), meNameCpp);
    }

    /**
     * TODO: This test excludes the possibility to have macros within the name
     * of classes. Since this is a common thing in C++ it is too restrictive.
     */
    public void testToStringForSpecializedClassEmptyArgs() {
        setUpGeneralizationForTheClass();
        String meNameCpp = meNotation.toString(theClass, settings);
        final String ignoredMatcher = "[\\s*\\n*\\r*]*";
        String meNameCppMatcher = "class" + ignoredMatcher
                + Model.getFacade().getName(theClass) + ignoredMatcher + ":"
                + ignoredMatcher + "public" + ignoredMatcher
                + Model.getFacade().getName(baseClass);
        assertTrue("class \t TheClass:\npublic\tTheBaseClass"
                .matches(meNameCppMatcher));
        assertTrue(meNameCpp.matches(meNameCppMatcher));
    }

    private void setUpGeneralizationForTheClass() {
        baseClass = Model.getCoreFactory().buildClass("TheBaseClass", 
                getModel());
        generalization = Model.getCoreFactory().buildGeneralization(theClass,
                baseClass);
    }

    public void testToStringForUnnamedGeneralizationDoesntReturnNull() {
        setUpGeneralizationForTheClass();
        assertNotNull(meNotation.toString(generalization, settings));
    }

    public void testToStringForDocumentedClassDoesntContainDocumentationComments() {
        profile.applyDocumentationTaggedValue(theClass,
            "TheClass documentation");
        String meNameCpp = meNotation.toString(theClass, settings);
        final String ignoredMatcher = "[\\s*\\n*\\r*]*";
        String meNameCppMatcher = "^class" + ignoredMatcher
            + Model.getFacade().getName(theClass) + ignoredMatcher;
        assertTrue("meNameCpp = \"" + meNameCpp 
            + "\", doesn't match meNameCppMatcher = \"" + meNameCppMatcher 
            + "\"", 
            meNameCpp.matches(meNameCppMatcher));
    }

}
