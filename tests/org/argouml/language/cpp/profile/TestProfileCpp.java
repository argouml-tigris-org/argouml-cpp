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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Object aClass;
    private Object attribute;
    private Object operation;
    private Object param;

    protected void setUp() throws Exception {
        newModel();
        model = Model.getModelManagementFactory().getRootModel();
        profile = new ProfileCpp(model);
        aClass = getCoreFactory().buildClass("AClass", getModel());
        attribute = getCoreFactory().buildAttribute2(aClass, aClass);
        operation = getCoreFactory().buildOperation2(aClass, 
                ProfileCpp.getBuiltIn("int"), "anInt");
        param = getCoreFactory().buildParameter(operation, 
                ProfileCpp.getBuiltIn("int"));
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
        validateCppStereotypeGetter(stereotype, ProfileCpp.STEREO_NAME_CLASS);
    }
    
    public void testGetCppAttributeStereotype() throws Exception {
        Object stereotype = profile.getCppAttributeStereotype();
        validateCppStereotypeGetter(stereotype, 
                ProfileCpp.STEREO_NAME_ATTRIBUTE);
    }
    
    void validateCppStereotypeGetter(Object stereotype, String stereoName) {
        assertNotNull(stereotype);
        assertEquals(stereoName, getFacade().getName(stereotype));
        assertEquals(model, getFacade().getModel(stereotype));
        Collection tagDefinitions = getFacade().getTagDefinitions(stereotype);
        assertNotNull(tagDefinitions);
        assertTrue(tagDefinitions.size() > 0);
    }
    
    public void testGetClassSpecifierTagDefinition() throws Exception {
        Object tagDefinition = profile.getClassSpecifierTagDefinition();
        assertNotNull(tagDefinition);
        assertEquals(model, getFacade().getModel(tagDefinition));
    }
    
    public void testApplyCppClassStereotype() throws Exception {
        profile.applyCppClassStereotype(aClass);
        Collection stereotypes = getFacade().getStereotypes(aClass);
        Object cppClassStereotype = profile.getCppClassStereotype();
        assertTrue(stereotypes.contains(cppClassStereotype));
        assertEquals(1, Collections.frequency(stereotypes, cppClassStereotype));
        profile.applyCppClassStereotype(aClass);
        assertEquals(1, Collections.frequency(stereotypes, cppClassStereotype));
    }
    
    public void testApplyCppAttributeStereotype() throws Exception {
        profile.applyCppAttributeStereotype(attribute);
        assertTrue(getFacade().getStereotypes(attribute).contains(
                profile.getCppAttributeStereotype()));
    }
    
    public void testApplyClassSpecifierTaggedValue() throws Exception {
        profile.applyCppClassStereotype(aClass);
        profile.applyClassSpecifierTaggedValue(aClass, "struct");
        Object taggedValue = getFacade().getTaggedValue(aClass, 
                ProfileCpp.TV_NAME_CLASS_SPECIFIER);
        assertNotNull(taggedValue);
        assertEquals(profile.getClassSpecifierTagDefinition(), 
                getFacade().getType(taggedValue));
        assertEquals("struct", getFacade().getValueOfTag(taggedValue));
    }
    
    public void testApplyMultiplicityTypeTaggedValue() throws Exception {
        profile.applyCppAttributeStereotype(attribute);
        profile.applyMultiplicityTypeTaggedValue(attribute, "vector");
        Object taggedValue = getFacade().getTaggedValue(attribute, 
                ProfileCpp.TV_NAME_MULTIPLICITY_TYPE);
        assertNotNull(taggedValue);
        assertEquals(profile.getMultiplicityTypeTagDefinition(), 
                getFacade().getType(taggedValue));
        assertEquals("vector", getFacade().getValueOfTag(taggedValue));
    }
    
    public void testApplyCppParameterStereotype() throws Exception {
        profile.applyCppParameterStereotype(param);
        assertTrue(getFacade().getStereotypes(param).contains(
                profile.getCppParameterStereotype()));
    }
    
    public void testApplyPointerTaggedValue2Parameter() throws Exception {
        profile.applyCppParameterStereotype(param);
        profile.applyPointerTaggedValue2Parameter(param, "true");
        Object taggedValue = getFacade().getTaggedValue(param, 
                ProfileCpp.TV_NAME_POINTER);
        assertNotNull(taggedValue);
        assertEquals(profile.getPointerTagDefinition4Parameter(), 
                getFacade().getType(taggedValue));
        assertEquals("true", getFacade().getValueOfTag(taggedValue));
    }
    
    public void testApplyReferenceTaggedValue2Parameter() throws Exception {
        profile.applyCppParameterStereotype(param);
        profile.applyReferenceTaggedValue2Parameter(param, "true");
        Object taggedValue = getFacade().getTaggedValue(param, 
                ProfileCpp.TV_NAME_REFERENCE);
        assertNotNull(taggedValue);
        assertEquals(profile.getReferenceTagDefinition4Parameter(), 
                getFacade().getType(taggedValue));
        assertEquals("true", getFacade().getValueOfTag(taggedValue));
    }
    
    @SuppressWarnings("serial")
    public void testCopyAllCppStereotypesToModel() throws Exception {
        profile.copyAllCppStereotypesToModel();
        Collection stereotypes = getExtensionMechanismsHelper().getStereotypes(
                getModel());
        // Initialize a Map with the names of the C++ stereotypes contained in 
        // the profile with all values false.
        // NOTE: uses initializer block syntax, described in 
        // http://java.sun.com/docs/books/tutorial/java/javaOO/initial.html
        Map<String, Boolean> stereotypesFoundMap = new 
        HashMap<String, Boolean>() { {
                Field[] fields = ProfileCpp.class.getDeclaredFields();
                List<String> stereoNames = new ArrayList<String>();
                for (Field field : fields) {
                    if (field.getName().contains("STEREO_NAME_"))
                        stereoNames.add((String) field.get(null));
                }
                for (String stereoName : stereoNames) {
                    put(stereoName, false);
                }
            } 
        };
        for (Object stereotype : stereotypes) {
            String stereoName = getFacade().getName(stereotype);
            if (stereotypesFoundMap.containsKey(stereoName)) {
                stereotypesFoundMap.put(stereoName, true);
            }
        }
        Set<String> stereotypeNames = stereotypesFoundMap.keySet();
        for (String stereotypeName : stereotypeNames) {
            assertTrue("Stereotype " + stereotypeName + " not found in model!",
                    stereotypesFoundMap.get(stereotypeName));
        }
    }
    
    @SuppressWarnings("serial")
    public void testCopyAllDataTypesToModel() {
        profile.copyAllDataTypesToModel();
        Collection dataTypes = getCoreHelper().getAllDataTypes(model);
        Map<String, Integer> dtNames2Check = new HashMap<String, Integer>() { {
                put("__int64", 0);
                put("signed", 0);
                put("int", 0);
            } 
        };
        for (Object dt : dataTypes) {
            String dtName = getFacade().getName(dt);
            if (dtNames2Check.containsKey(dtName))
                dtNames2Check.put(dtName, dtNames2Check.get(dtName) + 1);
        }
        for (String dtName : dtNames2Check.keySet()) {
            assertEquals("DataType " + dtName 
                    + " found in model with different number than expected!", 
                    1, (int) dtNames2Check.get(dtName));
        }
    }
}
