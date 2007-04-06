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

package org.argouml.language.cpp.profile;

import static org.argouml.language.cpp.Helper.*;

import java.util.Collection;

import org.argouml.model.Model;
import static org.argouml.model.Model.*;
import org.argouml.model.UmlException;

import junit.framework.TestCase;

/**
 * The tests for the C++ UML profile and its helper class {@link ProfileCpp}.
 * 
 * @author Luis Sergio Oliveira (euluis)
 * @since 0.25.2
 */
public class TestProfileCpp extends TestCase {
    private Object model;
    private ProfileCpp profile;

    protected void setUp() throws Exception {
        newModel();
        model = Model.getModelManagementFactory().getRootModel();
        profile = new ProfileCpp(model);
    }

    public void testCtorHappyPath() throws UmlException {
        assertNotNull(model);
        assertTrue(Model.getFacade().isAModel(model));
        assertNotNull(profile);
        assertNotNull(profile.getProfile());
    }
    
    public void testGetVirtualInheritanceTagDefinition() throws Exception {
        Object tagDefinition = profile.getVirtualInheritanceTagDefinition();
        assertNotNull(tagDefinition);
        assertEquals(model, getFacade().getModel(tagDefinition));
    }
    
    public void testGetCppClassStereotype() throws Exception {
        Object stereotype = profile.getCppClassStereotype();
        assertNotNull(stereotype);
        assertEquals(model, getFacade().getModel(stereotype));
        Collection tagDefinitions = getFacade().getTagDefinitions(stereotype);
        assertNotNull(tagDefinitions);
        assertTrue(tagDefinitions.size() > 0);
    }
}
