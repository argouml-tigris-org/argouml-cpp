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

// Copyright (c) 2007-2009 The Regents of the University of California. All
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

import static org.argouml.language.cpp.Helper.getModel;
import static org.argouml.language.cpp.Helper.newModel;
import static org.argouml.model.Model.getCoreFactory;
import static org.argouml.model.Model.getCoreHelper;
import static org.argouml.model.Model.getExtensionMechanismsFactory;
import static org.argouml.model.Model.getExtensionMechanismsHelper;
import static org.argouml.model.Model.getFacade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.argouml.kernel.Project;
import org.argouml.kernel.ProjectManager;
import org.argouml.language.cpp.Helper;
import org.argouml.model.Model;
import org.argouml.model.UmlException;
import org.argouml.profile.Profile;
import org.argouml.profile.ResourceModelLoader;

/**
 * The tests for the C++ UML profile and its helper classes {@link ProfileCpp} 
 * and {@link BaseProfile}.
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
    private Collection<Object> allModels;

    @SuppressWarnings("serial")
    protected void setUp() throws Exception {
        newModel();
        model = getFacade().getRootElements().iterator().next();
        profile = new ProfileCpp(Helper.getModels());
        allModels = new ArrayList<Object>(Helper.getModels()) {
            { add(profile.getProfile()); }
        };
        aClass = getCoreFactory().buildClass("AClass", getModel());
        attribute = getCoreFactory().buildAttribute2(aClass, aClass);
        operation = getCoreFactory().buildOperation2(aClass, 
                profile.getBuiltIn("int"), "anInt");
        param = getCoreFactory().buildParameter(operation, 
                profile.getBuiltIn("int"));
    }

    public void testCtorHappyPath() throws UmlException {
        assertNotNull(model);
        assertTrue(getFacade().isAModel(model));
        assertNotNull(profile);
        assertNotNull(profile.getProfile());
    }

    public void testCtorThatReceivesTheProfileModel() throws Exception {
        Profile normalProfileCpp = new NormalProfileCpp();
        Object normalProfileCppModel = 
            normalProfileCpp.getProfilePackages().iterator().next();
        profile = new ProfileCpp(Helper.getModels(), normalProfileCppModel);
        assertNotNull(profile.getProfile());
        assertEquals("The name of the profile models must be the same.", 
            getFacade().getName(normalProfileCppModel), 
            getFacade().getName(profile.getProfile()));
    }
    
    public void testGetVirtualInheritanceTagDefinition() throws Exception {
        Object tagDefinition = profile.getVirtualInheritanceTagDefinition();
        assertNotNull(tagDefinition);
        assertTrue(allModels.contains(getFacade().getRoot(tagDefinition)));
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
    
    public void testGetCppOperationStereotype() throws Exception {
        Object stereotype = profile.getCppOperationStereotype();
        validateCppStereotypeGetter(stereotype, 
                ProfileCpp.STEREO_NAME_OPERATION);
    }
    
    void validateCppStereotypeGetter(Object stereotype, String stereoName) {
        assertNotNull(stereotype);
        assertEquals(stereoName, getFacade().getName(stereotype));
        assertTrue(allModels.contains(getFacade().getRoot(stereotype)));
        Collection tagDefinitions = getFacade().getTagDefinitions(stereotype);
        assertNotNull(tagDefinitions);
        assertTrue(tagDefinitions.size() > 0);
    }
    
    public void testGetClassSpecifierTagDefinition() throws Exception {
        Object tagDefinition = profile.getClassSpecifierTagDefinition();
        assertNotNull(tagDefinition);
        assertTrue(allModels.contains(getFacade().getRoot(tagDefinition)));
    }
    
    public void testApplyCppClassStereotype() throws Exception {
        profile.applyCppClassStereotype(aClass);
        Collection stereotypes = getFacade().getStereotypes(aClass);
        Object cppClassStereotype = profile.getCppClassStereotype();
        assertTrue(stereotypes.contains(cppClassStereotype));
        assertEquals(1, Collections.frequency(stereotypes, cppClassStereotype));
        profile.applyCppClassStereotype(aClass);
        stereotypes = getFacade().getStereotypes(aClass);
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
    
    public void testApplyConstTaggedValue2Parameter() throws Exception {
        profile.applyCppParameterStereotype(param);
        profile.applyConstTaggedValue2Parameter(param, "true");
        Object taggedValue = getFacade().getTaggedValue(param, 
                ProfileCpp.TV_NAME_CONST);
        assertNotNull(taggedValue);
        assertEquals(profile.getConstTagDefinition4Parameter(), 
                getFacade().getType(taggedValue));
        assertEquals("true", getFacade().getValueOfTag(taggedValue));
    }
    
    public void testApplyInlineTaggedValue2Operation() throws Exception {
        profile.applyCppOperationStereotype(operation);
        profile.applyInlineTaggedValue2Operation(operation, "true");
        Object taggedValue = getFacade().getTaggedValue(operation, 
                ProfileCpp.TV_NAME_INLINE);
        assertNotNull(taggedValue);
        assertEquals(profile.getInlineTagDefinition4Operation(), 
                getFacade().getType(taggedValue));
        assertEquals("true", getFacade().getValueOfTag(taggedValue));
    }
    
    public void testGetBuiltIn() {
        String longDouble = "long double";
        Object builtIn = profile.getBuiltIn(longDouble);
        assertNotNull(builtIn);
        assertEquals(longDouble, getFacade().getName(builtIn));
    }
    
    public void testIsBuiltInWithInvalidModifiers() {
        assertFalse(profile.isBuiltIn("long float"));
        assertFalse(profile.isBuiltIn("signed float"));
        assertFalse(profile.isBuiltIn("unsigned double"));
    }
    
    public void testTrimAndEnsureOneSpaceOnlyBetweenTokens() {
        assertEquals("bla foo bla", 
                ProfileCpp.trimAndEnsureOneSpaceOnlyBetweenTokens(
                    " bla\t\tfoo  bla   "));
    }
    
    /**
     * There are so many combinations, that, I might introduce duplicates by 
     * mistake.
     * The computer and this test checks it ;-)
     */
    public void testNoDuplicateDataTypesInProfile() {
        Collection allDataTypes = getCoreHelper().getAllDataTypes(
                profile.getProfile());
        Map<String, Integer> dataTypeCounts = new HashMap<String, Integer>();
        for (Object dt : allDataTypes) {
            String dtName = getFacade().getName(dt);
            if (dataTypeCounts.containsKey(dtName)) {
                dataTypeCounts.put(dtName, dataTypeCounts.get(dtName) + 1);
            } else {
                dataTypeCounts.put(dtName, 1);
            }
        }
        for (String dtName : dataTypeCounts.keySet()) {
            assertEquals(1, (int) dataTypeCounts.get(dtName));
        }
    }

    // TODO: Profiles are read-only, so we aren't able to add
    // a TaggedValue to elements they contain.  This test needs
    // to be reworked.- tfm
    public void notestGetBuiltInCopiesDataTypeDocumentation() {
        Object originalDT = ProfileCpp.findDataType("int", 
                profile.getProfile());
        Object originalDocuTV = getExtensionMechanismsFactory().
            buildTaggedValue(
                ProfileCpp.getTagDefinition(ProfileCpp.TV_NAME_DOCUMENTATION), 
                new String[] {"the C++ standard integer built-in type"});
        getExtensionMechanismsHelper().addTaggedValue(originalDT, 
                originalDocuTV);
        
        Object modelDT = profile.getBuiltIn("int");
        
        Object modelDocuTV = getFacade().getTaggedValue(modelDT, 
                ProfileCpp.TV_NAME_DOCUMENTATION);
        assertNotNull(modelDocuTV);
        assertEquals(getFacade().getValueOfTag(originalDocuTV), 
                getFacade().getValueOfTag(modelDocuTV));
    }
    
    /**
     * Tests the loading of the UML profile for C++ using the 
     * {@link ResourceModelLoader}. 
     * 
     * @throws Exception when a loading error occurs.
     */
    public void testResourceModelLoaderFromModule() throws Exception {
        ResourceModelLoader loader = new ResourceModelLoader(getClass());
        assertNotNull(loader.loadModel(BaseProfile.PROFILE_REFERENCE));
    }
    
    public void testCtorWithModelCollectionAndProfiles() {
        Project proj = ProjectManager.getManager().getCurrentProject();
        NormalProfileCpp normalProfileCpp = new NormalProfileCpp();
        boolean cppProfileApplied = false;
        for (Profile p : proj.getProfileConfiguration().getProfiles()) {
            if (p.getDisplayName().equals(normalProfileCpp.getDisplayName())) {
                cppProfileApplied = true;
            }
        }
        if (!cppProfileApplied) {
            proj.getProfileConfiguration().addProfile(normalProfileCpp);
        }
        ProfileCpp profileCpp = new ProfileCpp(proj.getUserDefinedModelList());
        profileCpp.applyCppOperationStereotype(operation);
        Collection stereotypes = getExtensionMechanismsHelper().
            getStereotypes(getFacade().getRootElements().iterator().next());
        for (Object stereotype : stereotypes) {
            // TODO: Since applyStereotype calls getCppStereotypeInModel which
            // copies stereotypes into the user model, this test will fail - tfm
            assertFalse(ProfileCpp.STEREO_NAME_OPERATION.equals( 
                    Model.getFacade().getName(stereotype)));
        }
    }

    public void testApplyDocumentationTaggedValue() {
	final String docTVValue = "aClass documentation";
        profile.applyDocumentationTaggedValue(aClass, docTVValue);
        Object docTV = getFacade().getTaggedValue(aClass, 
                ProfileCpp.TV_NAME_DOCUMENTATION);
        assertNotNull(docTV);
        assertEquals(docTVValue, getFacade().getValueOfTag(docTV));
    }
}
