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

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import static org.argouml.model.Model.*;

import org.argouml.kernel.ProjectManager;
import org.argouml.model.Model;
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
 * confused. The main point in favor of this is that there is no open source UML
 * profile for C++. 
 * Lets be pioneers here ;-)
 * </p>
 * 
 * @author Luis Sergio Oliveira (euluis)
 * @since 0.19.3
 * @see Issue 3771 (http://argouml.tigris.org/issues/show_bug.cgi?id=3771)
 */
public class ProfileCpp {

    private static final Logger LOG = Logger.getLogger(ProfileCpp.class);
    
    private static final String PROFILE_FILE_NAME = 
            "org/argouml/language/cpp/profile/CppUmlProfile.xmi";
    
    /**
     * Name of the C++ UML profile Class stereotype.
     */
    public static final String STEREO_NAME_CLASS = "cppClass";

    /**
     * Name of the C++ UML profile Generalization stereotype.
     */
    public static final String STEREO_NAME_GENERALIZATION = "cppGeneralization";
    
    /**
     * Name of the C++ class specifier tagged value. Possible values are: class,
     * union, and struct. When this is ommited, class is used.
     * 
     * FIXME: Maybe we should have a more powerfull representation of the
     * TaggedValues in the profile. I would like that restrictions, default
     * value, value range and naming format to be enforced.
     */
    public static final String TV_NAME_CLASS_SPECIFIER = "cpp_class_specifier";

    /**
     * Name of the C++ reference tagged value which is also used in the
     * <code>GeneratorCpp</code>.
     */
    public static final String TV_NAME_REFERENCE = "reference";

    /**
     * Name of the C++ pointer tagged value which is also used in the
     * <code>GeneratorCpp</code>.
     */
    public static final String TV_NAME_POINTER = "pointer";

    /**
     * Name of the virtual inheritance flag tagged value for a UML 
     * generalization.
     * 
     * Default value (if the tagged value doesn't exist) is false. Type is
     * obviously Boolean.
     */
    public static final String TV_VIRTUAL_INHERITANCE = 
        "cpp_virtual_inheritance";

    /**
     * Name of the inheritance visibility tagged value. Applicable to a 
     * generalization.
     * 
     * Default value is <code>public</code> and possible values are: 
     * <code>private</code>, <code>protected</code> and <code>public</code>.
     */
    public static final String TV_INHERITANCE_VISIBILITY = 
        "cpp_inheritance_visibility";

    /**
     * Set of built in types tokens.
     * 
     * FIXME: replace by usage of the datatypes contained in CppUmlProfile.xmi.
     * Thus, remove the static nature.
     */
    private static final Set BUILT_IN_TYPES;

    static {
        BUILT_IN_TYPES = new HashSet();
        BUILT_IN_TYPES.add("char");
        BUILT_IN_TYPES.add("wchar_t");
        BUILT_IN_TYPES.add("bool");
        BUILT_IN_TYPES.add("short");
        BUILT_IN_TYPES.add("int");
        BUILT_IN_TYPES.add("__int64");
        BUILT_IN_TYPES.add("__w64");
        BUILT_IN_TYPES.add("long");
        BUILT_IN_TYPES.add("signed");
        BUILT_IN_TYPES.add("unsigned");
        BUILT_IN_TYPES.add("float");
        BUILT_IN_TYPES.add("double");
        BUILT_IN_TYPES.add("void");
    }

    /**
     * Checks if the given type is a C++ builtin type.
     * 
     * @param typeName name of the type to check
     * @return true if typeName is a builtin type, false otherwise
     */
    public static boolean isBuiltIn(String typeName) {
        if (BUILT_IN_TYPES.contains(typeName.split(" ")[0])) {
            return true;
        }
        return false;
    }

    /**
     * Retrieves the given builtin type model element representation as a
     * DataType.
     * 
     * @param typeName name of the type
     * @return the model element that models the C++ builtin type
     */
    public static Object getBuiltIn(String typeName) {
        assert isBuiltIn(typeName) : "Must be a C++ built in!";
        Object builtinType = ProjectManager.getManager().getCurrentProject()
                .findType(typeName.toString(), false);
        if (builtinType == null) {
            builtinType = Model.getCoreFactory().buildDataType(typeName,
                getModel());
        }
        return builtinType;
    }
    
    private Object model;
    
    /**
     * The C++ UML profile as loaded from CppUmlProfile.xmi.
     */
    private Object profile;

    /**
     * FIXME: legacy method to be used in static initialization. Remove when 
     * the need for static initialization goes away.
     * 
     * @return the model
     */
    private static Object getModel() {
        return ProjectManager.getManager().getCurrentProject().getModel();
    }

    public ProfileCpp(Object projectModel) {
        this.model = projectModel;
        InputStream inputStream = getClass().getClassLoader().
            getResourceAsStream(PROFILE_FILE_NAME);
        assert inputStream != null 
                : "The resource containing the C++ UML profile can't be null.";
        try {
            XmiReader xmiReader = getXmiReader();
            InputSource inputSource = new InputSource(inputStream);
            LOG.info("Loaded profile '" + PROFILE_FILE_NAME + "'");
            Collection elements = xmiReader.parse(inputSource, true);
            if (elements.size() != 1) {
                LOG.error("Error loading profile '" + PROFILE_FILE_NAME
                        + "' expected 1 top level element" + " found "
                        + elements.size());
            }
            profile = elements.iterator().next();
        } catch (UmlException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getCppClassStereotype() {
        return getCppStereotypeInModel(STEREO_NAME_CLASS);
    }

    public Object getCppGeneralizationStereotype() {
        return getCppStereotypeInModel(STEREO_NAME_GENERALIZATION);
    }

    private Object getCppStereotypeInModel(String stereotypeName) {
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

    public Object getVirtualInheritanceTagDefinition() {
        Object cppGeneralization = getCppGeneralizationStereotype();
        assert model.equals(getFacade().getModel(cppGeneralization));
        Collection tagDefinitions = getFacade().getTagDefinitions(
                cppGeneralization);
        for (Object tagDefinition : tagDefinitions) {
            if (TV_VIRTUAL_INHERITANCE.equals(getFacade().getName(
                    tagDefinition))) {
                return tagDefinition;
            }
        }
        return null;
    }

    public Object getProfile() {
        return profile;
    }

}
