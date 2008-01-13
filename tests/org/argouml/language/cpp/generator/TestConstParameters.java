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
 * Tests for const parameters generation
 * 
 * @see GeneratorCpp
 * @author Lukasz Gromanowski
 * @since 0.25.5
 */
public class TestConstParameters extends BaseTestGeneratorCpp {
    private Object classWithConstParam;
    private ProfileCpp profileCpp;

    /** The Logger for this class */
    private static final Logger LOG = Logger.getLogger(
            TestConstParameters.class);

    /**
     * The constructor.
     * 
     * @param testName the name of the test
     */
    public TestConstParameters(java.lang.String testName) {
        super(testName);
    }

    /**
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(TestConstParameters.class);
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
        classWithConstParam = getCoreFactory().buildClass(
                "ClassWithConstParam", getModel());
        profileCpp = new ProfileCpp(getModels());
    }
    
    /**
     * Test if const arguments are generated correctly
     */
    public void testGeneratorConstArguments() {
        Object voidType = ProjectManager.getManager().getCurrentProject()
                .findType("void");

        Object intType = ProjectManager.getManager().getCurrentProject()
                .findType("int");
        
        Object fooWithConstParam = buildOperation(classWithConstParam,
                voidType, "fooWithConstParam");
        
        Object constParam = getFactory().buildParameter(fooWithConstParam,
                intType);
        
        profileCpp.applyCppParameterStereotype(constParam);
        profileCpp.applyConstTaggedValue2Parameter(constParam, "true");
        
        Model.getCoreHelper().setType(constParam, intType);

        String re = "virtual\\svoid\\sfooWithConstParam"
            + "\\(const\\sint\\sarg2\\)";
        
        String code = getGenerator().generateOperation(
                fooWithConstParam, false);

        if (!code.matches(re)) {
            LOG.info("Code for 'fooWithConstParam':\n");
            LOG.info(code);
        }

        assertTrue(code.matches(re));
    }
}
