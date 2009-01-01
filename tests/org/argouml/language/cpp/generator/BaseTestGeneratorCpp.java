// $Id$
// Copyright (c) 1996-2009 The Regents of the University of California. All
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

import java.io.IOException;
import java.util.Collection;

import junit.framework.TestCase;

import org.argouml.kernel.ProjectManager;
import org.argouml.language.cpp.Helper;
import static org.argouml.language.cpp.Helper.*;
import org.argouml.language.cpp.profile.ProfileCpp;
import org.argouml.model.CoreFactory;
import static org.argouml.model.Model.*;
import org.argouml.moduleloader.ModuleInterface;
import org.argouml.uml.generator.GeneratorManager;

/**
 * The Base class of all the TestCases for the GeneratorCpp class.
 * @see GeneratorCpp
 * @author euluis
 * @since 0.17.2
 */
class BaseTestGeneratorCpp extends TestCase {
    /**
     * The constructor.
     *
     * @param testName the name of the test
     */
    public BaseTestGeneratorCpp(String testName) {
        super(testName);
    }

    /**
     * Factory for model elements.
     */
    private CoreFactory factory;

    /**
     * The venerable C++ generator instance used in the test fixtures.
     */
    private GeneratorCpp generator;

    /**
     * The C++ module used in the test fixtures..
     */
    private ModuleInterface module;

    /**
     * The AClass model element.
     */
    private Object aClass;

    /**
     * The AClass::foo() operation.
     */
    private Object fooMethod;

    /**
     * The C++ profile class, usefull to apply programatically tags and
     * stereotypes contained in the profile.
     */
    protected ProfileCpp profile;

    /*
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        module = new ModuleCpp();
        getModule().enable();

        generator = ((GeneratorCpp) GeneratorManager.getInstance()
                .getGenerator(GeneratorCpp.LANGUAGE_NAME));
        newModel();
        factory = getCoreFactory();
        aClass = factory.buildClass("AClass");

        Object me = getAClass();
        profile = new ProfileCpp(getModels());
        Object voidType = profile.getBuiltIn("void");
        fooMethod = buildOperation(me, voidType, "foo");
    }
    
    protected void tearDown() throws Exception {
        deleteCurrentProject();
        super.tearDown();
    }

    /**
     * Create a operation in the given model element. 
     * @param me the model element for which to build the operation
     * @param returnType the operation return type
     * @param opName operation name
     * @return the operation
     */
    protected Object buildOperation(Object me, Object returnType, 
            String opName) {
        return factory.buildOperation2(me, returnType, opName);
    }

    /**
     * Create a attribute in the given model element. 
     * @param me the model element for which to build the attribute
     * @param type type of the attribute
     * @param attrName attribute name
     * @return the attribute
     */
    protected Object buildAttribute(Object me, Object type, 
            String attrName) {
        Object attr = getCoreFactory().buildAttribute2(me, type);
        getCoreHelper().setName(attr, attrName);
        return attr;
    }

    /**
     * Retrieve the property change listeners for the given model element.
     * @param me the modelelement
     * @return property change listeners for me
     */
    protected Collection getPropertyChangeListeners(Object me) {
        return ProjectManager.getManager().getCurrentProject()
            .findFigsForMember(me);
    }

    /**
     * @return the model
     */
    protected Object getModel() {
        return Helper.getModel();
    }

    /**
     * @return the model models
     */
    protected Collection<Object> getModels() {
        return Helper.getModels();
    }

    /**
     * @return Returns the factory.
     */
    protected CoreFactory getFactory() {
        return factory;
    }

    /**
     * @return Returns the generator.
     */
    protected GeneratorCpp getGenerator() {
        return generator;
    }

    /**
     * @return Returns the module.
     */
    protected ModuleInterface getModule() {
        return module;
    }

    
    /**
     * @return Returns the aClass.
     */
    protected Object getAClass() {
        return aClass;
    }

    /**
     * @return Returns the fooMethod.
     */
    protected Object getFooMethod() {
        return fooMethod;
    }
}
