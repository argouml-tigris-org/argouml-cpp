// $Id$
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

import static org.argouml.model.Model.getCoreFactory;
import static org.argouml.model.Model.getCoreHelper;
import static org.argouml.model.Model.getExtensionMechanismsFactory;
import static org.argouml.model.Model.getExtensionMechanismsHelper;
import static org.argouml.model.Model.getFacade;
import static org.argouml.model.Model.getModelManagementHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.argouml.profile.ProfileException;
import org.argouml.profile.ProfileModelLoader;
import org.argouml.profile.ProfileReference;
import org.argouml.profile.ResourceModelLoader;
import org.argouml.model.Model;

/**
 * <p>A class that facilitates access to the UML profile for C++ 
 * (CppUmlProfile.xmi). 
 * This is done by facilitating the use of the stereotypes, built-ins 
 * and other model elements of the profile in the model which is being used.
 * </p>
 * <p>Both the generator and the importer must use the same profile, 
 * if not we are going to make future RTE very difficult. 
 * Also, the users of the module are going to be
 * confused. 
 * The main point in favor of this is that there is no open source UML
 * profile for C++. 
 * Lets be pioneers here ;-)
 * </p>
 * 
 * @author Luis Sergio Oliveira (euluis)
 * @since 0.25.4
 * @see Issue 3771 (http://argouml.tigris.org/issues/show_bug.cgi?id=3771)
 */
public class BaseProfile {

    private static final Logger LOG = Logger.getLogger(BaseProfile.class);

    /**
     * The name of the documentation Tagged Value. 
     */
    public static final String TV_NAME_DOCUMENTATION = "documentation";

    static final String PROFILE_FILE_NAME = 
        "/org/argouml/language/cpp/profile/CppUmlProfile.xmi";

    static {
        URL profileURL = null;
        try {
            profileURL = new URL(
                "http://argouml-cpp.tigris.org/profile/CppUmlProfile.xmi");
        } catch (MalformedURLException e) {
            LOG.error("Exception", e);
        }
        PROFILE_REFERENCE = new ProfileReference(PROFILE_FILE_NAME, 
            profileURL);
    }
    
    static final ProfileReference PROFILE_REFERENCE;
    
    private Collection<Object> models;
    
    /**
     * The C++ UML profile as loaded from CppUmlProfile.xmi.
     */
    private Object profile;

    /**
     * Set of built in types.
     */
    private Set builtInTypes;

    private void initBuiltInTypes() {
        builtInTypes = new HashSet();
        Collection allDataTypes = getCoreHelper().getAllDataTypes(profile);
        for (Object dt : allDataTypes) {
            builtInTypes.add(getFacade().getName(dt));
        }
    }

    /**
     * Checks if the given type is a C++ builtin type.
     * 
     * @param typeName name of the type to check
     * @return true if typeName is a builtin type, false otherwise
     */
    public boolean isBuiltIn(String typeName) {
        if (builtInTypes == null) {
            initBuiltInTypes();
        }
        typeName = trimAndEnsureOneSpaceOnlyBetweenTokens(typeName);
        return builtInTypes.contains(typeName);
    }

    static String trimAndEnsureOneSpaceOnlyBetweenTokens(String typeName) {
        return typeName.trim().replaceAll("\\s+", " ");
    }

    /**
     * Retrieves the given builtin type model element representation as a
     * DataType.
     * 
     * @param typeName name of the type
     * @return the model element that models the C++ builtin type
     */
    public Object getBuiltIn(String typeName) {
        assert isBuiltIn(typeName) : "Must be a C++ built in!";
        Object builtinType = null;
        for (Object model : models) {
            builtinType = findDataType(typeName, model);
            if (builtinType != null) {
                break;
            }
        }
        if (builtinType == null) {
            builtinType = getCoreFactory().buildDataType(typeName, 
                getFacade().getRootElements().iterator().next());
        }
        // copy the documentation from the profile if it exists
        Object profileDT = findDataType(typeName, profile);
        Object dtDocuTV = getFacade().getTaggedValue(profileDT, 
                TV_NAME_DOCUMENTATION);
        if (dtDocuTV != null) {
            Object tdDocumentation = getTagDefinition(TV_NAME_DOCUMENTATION);
            Object modelDTDocuTV = getExtensionMechanismsFactory().
                buildTaggedValue(tdDocumentation, 
                    new String[] {getFacade().getValueOfTag(dtDocuTV)});
            getExtensionMechanismsHelper().addTaggedValue(builtinType, 
                    modelDTDocuTV);
        }
        return builtinType;
    }

    public static Object getTagDefinition(String tdName) {
        Collection tagDefinitions = getModelManagementHelper().
            getAllModelElementsOfKindWithModel(
                getFacade().getRootElements().iterator().next(), 
                Model.getMetaTypes().getTagDefinition());
        for (Object td : tagDefinitions) {
            if (tdName.equals(getFacade().getName(td))) {
                return td;
            }
        }
        return getExtensionMechanismsFactory().buildTagDefinition(tdName, 
                null, getFacade().getRootElements().iterator().next());
    }

    static Object findDataType(String typeName, Object model2) {
        Collection dataTypes = getCoreHelper().getAllDataTypes(model2);
        for (Object dt : dataTypes) {
            if (getFacade().getName(dt).equals(typeName)) {
                return dt;
            }
        }
        return null;
    }

    Object getProfile() {
        return profile;
    }

    protected BaseProfile(Collection<Object> theModels) {
        this.models = theModels;
        profile = loadProfileModels().iterator().next();
    }

    /**
     * @return the Collection containing the profile models.
     */
    static Collection loadProfileModels() {
        ProfileModelLoader profileModelLoader = new ResourceModelLoader(
                BaseProfile.class);
        Collection elements;
        try {
            elements = profileModelLoader.loadModel(PROFILE_REFERENCE);
        } catch (ProfileException e) {
            throw new RuntimeException(e);
        }
        return elements;
    }

    protected Object getCppStereotypeInModel(String stereotypeName) {
        Object cppStereotype = null;
        for (Object model : models) {
            cppStereotype = getStereotype(model, stereotypeName);
            if (cppStereotype != null) {
                break;
            }
        }
        if (cppStereotype == null) {
            cppStereotype = getStereotype(profile, stereotypeName);
        }
        return cppStereotype;
    }

    private Object getStereotype(Object aModel, String stereotypeName) {
        Collection stereotypes = getExtensionMechanismsHelper().getStereotypes(
                aModel);
        for (Object stereotype : stereotypes) {
            if (stereotypeName.equals(getFacade().getName(stereotype))) {
                return stereotype;
            }
        }
        return null;
    }

    protected Object getTagDefinition(String stereoName, String tdName) {
        Object stereo = getCppStereotypeInModel(stereoName);
        assertModelElementContainedInModels(stereo);
        Collection tagDefinitions = getFacade().getTagDefinitions(stereo);
        for (Object tagDefinition : tagDefinitions) {
            if (tdName.equals(getFacade().getName(tagDefinition))) {
                return tagDefinition;
            }
        }
        return null;
    }

    public void applyStereotype(String stereoName, Object modelElement) {
        assertModelElementContainedInModels(modelElement);
        Object stereo = getCppStereotypeInModel(stereoName);
        getCoreHelper().addStereotype(modelElement, stereo);
    }

    @SuppressWarnings("serial")
    private void assertModelElementContainedInModels(Object modelElement) {
        boolean contained = false;
        ArrayList<Object> modelsAndProfile = new ArrayList<Object>(models) {
            { add(profile); }
        };
        for (Object model : modelsAndProfile) {
            contained = model.equals(getFacade().getRoot(modelElement));
            if (contained) {
                break;
            }
        }
        assert contained : "model element (" + modelElement + ") not contained "
        		+ "in models.";
    }

    public void applyTaggedValue(String stereoName, String tdName, 
            Object me, String tvv) {
        assertModelElementContainedInModels(me);
        assert getFacade().getStereotypes(me).contains(
                getCppStereotypeInModel(stereoName));
        Object td = getTagDefinition(stereoName, tdName);
        Object tv = getExtensionMechanismsFactory().createTaggedValue();
        getExtensionMechanismsHelper().setType(tv, td);
        getExtensionMechanismsHelper().setDataValues(tv, new String[] {tvv});
        getExtensionMechanismsHelper().addTaggedValue(me, tv);
    }
    
    /**
     * Applies a newlly created tagged value with value tvv related to the
     * tag definition with name tdName to the given model element me.
     *
     * @param tdName the name of the tag definition for which to create a
     * tagged value
     * @param me the model element to which to apply the tagged value that will
     * be created
     * @param tvv the value that will be set in the tagged value
     */
    public void applyTaggedValue(String tdName, Object me, String tvv) {
        assertModelElementContainedInModels(me);
        Object td = getTagDefinition(tdName);
        Object tv = getExtensionMechanismsFactory().createTaggedValue();
        getExtensionMechanismsHelper().setType(tv, td);
        getExtensionMechanismsHelper().setDataValues(tv, new String[] {tvv});
        getExtensionMechanismsHelper().addTaggedValue(me, tv);
    }

}
