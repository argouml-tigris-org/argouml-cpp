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

import static org.argouml.model.Model.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.argouml.model.UmlException;
import org.argouml.model.XmiReader;
import org.xml.sax.InputSource;

/**
 * <p>A class that facilitates access to the UML profile for C++ 
 * (CppUmlProfile.xmi). 
 * It also provides means to overcome the current 
 * limitation of ArgoUML that prevents the existance of two models in one 
 * project.
 * This is done by copying the stereotypes, built-ins and other model elements 
 * of the profile into the model which is being used.
 * </p>
 * <p>TODO: should extend {@link org.argouml.uml.Profile}, but, I don't
 * think that what is attempted there is possible with the limitation of 
 * ArgoUML only having one profile at a given time.
 * </p>
 * <p>TODO: discuss in the mailing list the approach I'm taking about this 
 * problem. 
 * Both the generator and the importer must use the same profile, 
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

    static final String TV_NAME_DOCUMENTATION = "documentation";

    private static final Logger LOG = Logger.getLogger(BaseProfile.class);

    private static final String PROFILE_FILE_NAME = 
        "org/argouml/language/cpp/profile/CppUmlProfile.xmi";
    
    private Object model;
    
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
        if (builtInTypes == null) 
            initBuiltInTypes();
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
        Object builtinType = findDataType(typeName, model);
        if (builtinType == null) {
            builtinType = getCoreFactory().buildDataType(typeName, 
                model);
        }
        // copy the documentation from the profile if it exists
        Object profileDT = findDataType(typeName, profile);
        Object dtDocuTV = getFacade().getTaggedValue(profileDT, 
                TV_NAME_DOCUMENTATION);
        if (dtDocuTV != null) {
            Object modelDTDocuTV = getExtensionMechanismsFactory().
                buildTaggedValue(TV_NAME_DOCUMENTATION, 
                    getFacade().getValueOfTag(dtDocuTV));
            getExtensionMechanismsHelper().addTaggedValue(builtinType, 
                    modelDTDocuTV);
        }
        return builtinType;
    }

    static Object findDataType(String typeName, Object model2) {
        Collection dataTypes = getCoreHelper().getAllDataTypes(model2);
        for (Object dt : dataTypes) {
            if (getFacade().getName(dt).equals(typeName))
                return dt;
        }
        return null;
    }

    Object getProfile() {
        return profile;
    }
    
    protected BaseProfile(Object projectModel) {
        this.model = projectModel;
        profile = loadProfileModels().iterator().next();
    }

    static Collection loadProfileModels() {
        InputStream inputStream = BaseProfile.class.getClassLoader().
            getResourceAsStream(PROFILE_FILE_NAME);
        assert inputStream != null 
                : "The resource containing the C++ UML profile can't be null.";
        Collection elements = null;
        try {
            XmiReader xmiReader = getXmiReader();
            InputSource inputSource = new InputSource(inputStream);
            LOG.info("Loaded profile '" + PROFILE_FILE_NAME + "'");
            elements = xmiReader.parse(inputSource, true);
            if (elements.size() != 1) {
                LOG.error("Error loading profile '" + PROFILE_FILE_NAME
                        + "' expected 1 top level element" + " found "
                        + elements.size());
            }
        } catch (UmlException e) {
            throw new RuntimeException(e);
        }
        return elements;
    }

    protected Object getCppStereotypeInModel(String stereotypeName) {
        Object cppStereotype = getStereotype(model, stereotypeName);
        if (cppStereotype == null) {
            cppStereotype = getStereotype(profile, stereotypeName);
            assert cppStereotype != null;
            Object modelStereotype = getExtensionMechanismsFactory().
                copyStereotype(cppStereotype, model);
            Collection tagDefinitions = getFacade().getTagDefinitions(
                    cppStereotype);
            for (Object td : tagDefinitions) {
                getExtensionMechanismsFactory().copyTagDefinition(td, 
                        modelStereotype);
            }
            
            cppStereotype = modelStereotype;
        }
        return cppStereotype;
    }

    private Object getStereotype(Object aModel, String stereotypeName) {
        Collection stereotypes = getExtensionMechanismsHelper().getStereotypes(
                aModel);
        for (Object stereotype : stereotypes) {
            if (stereotypeName.equals(getFacade().getName(stereotype)))
                return stereotype;
        }
        return null;
    }

    protected Object getTagDefinition(String stereoName, String tdName) {
        Object stereo = getCppStereotypeInModel(stereoName);
        assert model.equals(getFacade().getModel(stereo));
        Collection tagDefinitions = getFacade().getTagDefinitions(stereo);
        for (Object tagDefinition : tagDefinitions) {
            if (tdName.equals(getFacade().getName(tagDefinition))) {
                return tagDefinition;
            }
        }
        return null;
    }

    public void applyStereotype(String stereoName, Object modelElement) {
        assert model.equals(getFacade().getModel(modelElement));
        Object stereo = getCppStereotypeInModel(stereoName);
        getCoreHelper().addStereotype(modelElement, stereo);
    }

    public void applyTaggedValue(String stereoName, String tdName, 
            Object me, String tvv) {
        assert model.equals(getFacade().getModel(me));
        assert getFacade().getStereotypes(me).contains(
                getCppStereotypeInModel(stereoName));
        Object td = getTagDefinition(stereoName, tdName);
        Object tv = getExtensionMechanismsFactory().createTaggedValue();
        getExtensionMechanismsHelper().setType(tv, td);
        getExtensionMechanismsHelper().setValueOfTag(tv, tvv);
        getExtensionMechanismsHelper().addTaggedValue(me, tv);
    }

    public void copyAllCppStereotypesToModel() {
        List<String> cppStereoTypesNames = getAllCppStereotypeNames();
        for (String cppStereotypeName : cppStereoTypesNames) {
            getCppStereotypeInModel(cppStereotypeName);
        }
    }

    private List<String> getAllCppStereotypeNames() {
        List<String> cppStereotypesNames = new ArrayList<String>();
        Collection stereotypes = getExtensionMechanismsHelper().getStereotypes(
                profile);
        for (Object stereotype : stereotypes) {
            String name = getFacade().getName(stereotype);
            if (name.startsWith("cpp"))
                cppStereotypesNames.add(name);
        }
        return cppStereotypesNames;
    }

    public void copyAllDataTypesToModel() {
        Collection dataTypes = getCoreHelper().getAllDataTypes(profile);
        for (Object dt : dataTypes) {
            String dtName = getFacade().getName(dt);
            getBuiltIn(dtName);
        }
    }

}