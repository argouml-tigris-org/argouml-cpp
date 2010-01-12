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

// Copyright (c) 2009 The Regents of the University of California. All
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

package org.argouml.language.cpp.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;

import org.argouml.model.CoreHelper;
import org.argouml.model.Facade;
import org.argouml.model.Model;
import org.argouml.model.ModelImplementation;
import org.argouml.profile.DefaultTypeStrategy;

import org.easymock.MockControl;

/**
 * Unit tests for the DefaultTypeStrategyCpp class.
 * 
 * @author Luis Sergio Oliveira (euluis)
 */
public class TestDefaultTypeStrategyCpp extends TestCase {
    
    MockControl modelImplCtrl;
    ModelImplementation modelImpl;
    MockControl facadeCtrl;
    Facade facade;
    MockControl coreHelperCtrl;
    CoreHelper coreHelper;
    
    Collection profileModels;
    Object profileModel;
    Collection dataTypes;
    
    DefaultTypeStrategy typeStrategy;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        profileModels = new ArrayList();
        profileModel = new String("I'm the profileModel mock");
        profileModels.add(profileModel);
        typeStrategy = new DefaultTypeStrategyCpp(profileModels);
        dataTypes = new ArrayList();
        
        modelImplCtrl = MockControl.createNiceControl(
            ModelImplementation.class);
        modelImpl = (ModelImplementation) modelImplCtrl.getMock();
        facadeCtrl = MockControl.createStrictControl(Facade.class);
        facade = (Facade) facadeCtrl.getMock();
        coreHelperCtrl = MockControl.createNiceControl(CoreHelper.class);
        coreHelper = (CoreHelper) coreHelperCtrl.getMock();
    }

    public void testCtorWithEmptyProfileModelsShallThrow() {
        profileModels = Collections.emptyList();
        try {
            typeStrategy = new DefaultTypeStrategyCpp(profileModels);
            fail("An IllegalArgumentException shall be thrown when the " 
                + "DefaultTypeStrategyCpp ctor is called with an empty " 
                + "profileModelss argument.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testGetDefaultAttributeTypeShallFindAndReturnInt() {
        Object dataTypeVoid = new String("void");
        Object dataTypeInt = new String("int");
        dataTypes.add(dataTypeVoid);
        dataTypes.add(dataTypeInt);
        
        facadeCtrl.expectAndReturn(facade.getName(dataTypeVoid), dataTypeVoid);
        facadeCtrl.expectAndReturn(facade.getName(dataTypeInt), dataTypeInt);
        coreHelperCtrl.expectAndReturn(coreHelper.getAllDataTypes(profileModel),
            dataTypes);
        modelImplCtrl.expectAndReturn(modelImpl.getFacade(), facade, 2);
        modelImplCtrl.expectAndReturn(modelImpl.getCoreHelper(), coreHelper);
        modelImplCtrl.setDefaultReturnValue(null);
        modelImplCtrl.replay();
        coreHelperCtrl.replay();
        facadeCtrl.replay();
        Model.setImplementation(modelImpl);
        Object defaultAttributeType = typeStrategy.getDefaultAttributeType();
        modelImplCtrl.verify();
        facadeCtrl.verify();
        coreHelperCtrl.verify();
        assertNotNull("The default attribute type shall not be null.", 
            defaultAttributeType);
    }

    public void testGetDefaultParameterTypeShallFindAndReturnInt() {
        Object dataTypeVoid = new String("void");
        Object dataTypeInt = new String("int");
        Object dataTypeChar = new String("char");
        dataTypes.add(dataTypeVoid);
        dataTypes.add(dataTypeInt);
        dataTypes.add(dataTypeChar);
        
        // TODO: order of calls shouldn't matter 
        facadeCtrl.expectAndReturn(facade.getName(dataTypeVoid), dataTypeVoid);
        facadeCtrl.expectAndReturn(facade.getName(dataTypeInt), dataTypeInt);
        coreHelperCtrl.expectAndReturn(coreHelper.getAllDataTypes(profileModel),
            dataTypes);
        // TODO: shouldn't matter how many calls are made
        modelImplCtrl.expectAndReturn(modelImpl.getFacade(), facade, 2);
        modelImplCtrl.expectAndReturn(modelImpl.getCoreHelper(), coreHelper);
        modelImplCtrl.setDefaultReturnValue(null);
        modelImplCtrl.replay();
        coreHelperCtrl.replay();
        facadeCtrl.replay();
        Model.setImplementation(modelImpl);
        Object defaultParameterType = typeStrategy.getDefaultParameterType();
        modelImplCtrl.verify();
        facadeCtrl.verify();
        coreHelperCtrl.verify();
        assertNotNull("The default parameter type shall not be null.", 
            defaultParameterType);
    }

    public void testGetDefaultReturnTypeShallFindAndReturnInt() {
        dataTypes = setUpDataTypes(new String[] {"1", "2", "3", "4", "5", });
        // TODO: order of calls shouldn't matter 
        for (Object dataType : dataTypes) {
            facadeCtrl.expectAndReturn(facade.getName(dataType), dataType);
        }
        coreHelperCtrl.expectAndReturn(coreHelper.getAllDataTypes(profileModel),
            dataTypes);
        // TODO: shouldn't matter how many calls are made
        modelImplCtrl.expectAndReturn(modelImpl.getFacade(), facade, 
            dataTypes.size());
        modelImplCtrl.expectAndReturn(modelImpl.getCoreHelper(), coreHelper);
        modelImplCtrl.setDefaultReturnValue(null);
        modelImplCtrl.replay();
        coreHelperCtrl.replay();
        facadeCtrl.replay();
        Model.setImplementation(modelImpl);
        Object defaultReturnType = typeStrategy.getDefaultParameterType();
        modelImplCtrl.verify();
        facadeCtrl.verify();
        coreHelperCtrl.verify();
        assertNull("The default return type shall be null.", 
            defaultReturnType);
    }
    
    static Collection setUpDataTypes(String[] dataTypesNames) {
        Collection dataTypes = new ArrayList();
        for (String dataTypeName : dataTypesNames) {
            Object dataType = new String(dataTypeName);
            dataTypes.add(dataType);
        }
        return dataTypes;
    }

}
