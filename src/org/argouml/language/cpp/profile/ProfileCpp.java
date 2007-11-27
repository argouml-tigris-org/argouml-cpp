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

import java.util.HashSet;
import java.util.Set;


/**
 * This is the class that provides easy access to the other modules of the 
 * ArgoUML C++ module to the contents of the UML profile for C++.
 * 
 * Note that this class is very shallow and the {@link BaseProfile} is doing 
 * the heavy lifting.
 * 
 * TODO: this class is a candidate for being generated from the profile with 
 * a MDA tool like AndroMDA.
 * 
 * @author Luis Sergio Oliveira (euluis)
 * @since 0.19.3
 * @see Issue 3771 (http://argouml.tigris.org/issues/show_bug.cgi?id=3771)
 */
public class ProfileCpp extends BaseProfile {
    
    /**
     * Name of the C++ UML profile Class stereotype.
     */
    public static final String STEREO_NAME_CLASS = "cppClass";

    /**
     * Name of the C++ UML profile Generalization stereotype.
     */
    public static final String STEREO_NAME_GENERALIZATION = "cppGeneralization";

    /**
     * Name of the C++ UML profile Realization stereotype.
     */
    public static final String STEREO_NAME_REALIZATION = "cppRealization";

    /**
     * Name of the C++ UML profile Attribute stereotype.
     */
    public static final String STEREO_NAME_ATTRIBUTE = "cppAttribute";

    /**
     * Name of the C++ UML profile Parameter stereotype.
     */
    public static final String STEREO_NAME_PARAMETER = "cppParameter";
    
    /**
     * Name of the C++ UML profile Operation stereotype.
     */
    public static final String STEREO_NAME_OPERATION = "cppOperation";
    
    /**
     * Name of the C++ class specifier tagged value. Possible values are: class,
     * union, and struct. When this is omitted, class is used.
     * 
     * FIXME: Maybe we should have a more powerful representation of the
     * TaggedValues in the profile. I would like that restrictions, default
     * value, value range and naming format to be enforced.
     * This is possible with Typed TaggedValues and OCL constraints in UML, 
     * but, the former aren't possible and the later I haven't checked yet...
     */
    public static final String TV_NAME_CLASS_SPECIFIER = "cpp_class_specifier";
    
    /**
     * Name of the C++ source_incl TaggedValues. 
     */
    public static final String TV_NAME_SOURCE_INCL = "source_incl";
    
    /**
     * Name of the C++ header_incl TaggedValues. 
     */
    public static final String TV_NAME_HEADER_INCL = "header_incl";
    
    /**
     * Name of the C++ typedef_public TaggedValues. 
     */
    public static final String TV_NAME_TYPEDEF_PUBLIC = "typedef_public";
    
    /**
     * Name of the C++ typedef_protected TaggedValues. 
     */
    public static final String TV_NAME_TYPEDEF_PROTECTED = "typedef_protected";
    
    /**
     * Name of the C++ typedef_private TaggedValues. 
     */
    public static final String TV_NAME_TYPEDEF_PRIVATE = "typedef_private";
    
    /**
     * The TaggedValue name for the path to the template from which to 
     * generate the C++ files; applicable to classes.
     * The TaggedValue value must be a Directory – the GeneratorCpp will 
     * search in the 
     * specified directory for the template files "header_template" and 
     * "cpp_template" which are placed in top of the corresponding file. 
     * The following tags in the template file are replaced by model values: 
     * |FILENAME|, |DATE|, |YEAR|, |AUTHOR|, |EMAIL|. 
     * If no such tag is specified, the templates are searched in the 
     * subdirectory of the root directory for the code generation.
     */
    public static final String TV_NAME_TEMPLATE_PATH = "TemplatePath";
    
    /**
     * The TaggedValue name for the name of the author of a class; applicable 
     * to classes. The value in the TaggedValue replaces the tag |AUTHOR| of 
     * the template file. 
     * Note: you may simply use the Author property in the documentation 
     * property panel.
     */
    public static final String TV_NAME_AUTHOR = "author";
    
    /**
     * The TaggedValue name for the name of the author of a class; applicable 
     * to classes.
     * name@domain.country – replaces the tag |EMAIL| of the template file.
     */
    public static final String TV_NAME_EMAIL = "email";

    /**
     * Name of the C++ usage TaggedValue which is also used in the
     * <code>GeneratorCpp</code>.
     * If "header", will lead for class types to a pre-declaration in the 
     * header, and the include of the remote class header in the header of the 
     * generated class.
     */
    public static final String TV_NAME_USAGE = "usage";

    /**
     * Name of the C++ get TaggedValues.
     */
    public static final String TV_NAME_GET = "get";

    /**
     * Name of the C++ set TaggedValues.
     */
    public static final String TV_NAME_SET = "set";

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
    
    private static final Set<String> PTR_N_REF_TV_NAMES = new HashSet<String>();
    
    static {
        PTR_N_REF_TV_NAMES.add("&");
        PTR_N_REF_TV_NAMES.add("*");
        PTR_N_REF_TV_NAMES.add(ProfileCpp.TV_NAME_POINTER);
        PTR_N_REF_TV_NAMES.add(ProfileCpp.TV_NAME_REFERENCE);
    }
    
    public static boolean isPtrOrRefTVName(String name) {
        return PTR_N_REF_TV_NAMES.contains(name);
    }

    /**
     * Name of the virtual inheritance flag tagged value for a UML 
     * generalization.
     * 
     * Default value (if the tagged value doesn't exist) is false. Type is
     * obviously Boolean.
     */
    public static final String TV_NAME_VIRTUAL_INHERITANCE = 
        "cpp_virtual_inheritance";

    /**
     * Name of the inheritance visibility tagged value. Applicable to a 
     * generalization.
     * 
     * Default value is <code>public</code> and possible values are: 
     * <code>private</code>, <code>protected</code> and <code>public</code>.
     */
    public static final String TV_NAME_INHERITANCE_VISIBILITY = 
        "cpp_inheritance_visibility";
    
    /**
     * Name of the multiplicity type tagged value. Applicable to attributes.
     */
    public static final String TV_NAME_MULTIPLICITY_TYPE = "MultiplicityType";
    
    /**
     * Name of const tagged value used in <code>GeneratorCpp</code>.
     */
    public static final String TV_NAME_CONST = "const";
    	
    /**
     * Name of inline keyword (C99) tagged value used in
     * <code>GeneratorCpp</code>.
     */
    public static final String TV_NAME_INLINE = "inline";
    
    public ProfileCpp(Object projectModel) {
        super(projectModel);
    }

    public Object getCppClassStereotype() {
        return getCppStereotypeInModel(STEREO_NAME_CLASS);
    }

    public Object getCppGeneralizationStereotype() {
        return getCppStereotypeInModel(STEREO_NAME_GENERALIZATION);
    }

    public Object getCppAttributeStereotype() {
        return getCppStereotypeInModel(STEREO_NAME_ATTRIBUTE);
    }

    public Object getCppParameterStereotype() {
        return getCppStereotypeInModel(STEREO_NAME_PARAMETER);
    }

    public Object getCppOperationStereotype() {
        return getCppStereotypeInModel(STEREO_NAME_OPERATION);
    }

    public Object getVirtualInheritanceTagDefinition() {
        return getTagDefinition(STEREO_NAME_GENERALIZATION, 
                TV_NAME_VIRTUAL_INHERITANCE);
    }

    public Object getClassSpecifierTagDefinition() {
        return getTagDefinition(STEREO_NAME_CLASS, TV_NAME_CLASS_SPECIFIER);
    }

    public Object getMultiplicityTypeTagDefinition() {
        return getTagDefinition(STEREO_NAME_ATTRIBUTE, 
                TV_NAME_MULTIPLICITY_TYPE);
    }

    public Object getPointerTagDefinition4Parameter() {
        return getTagDefinition(STEREO_NAME_PARAMETER, 
                TV_NAME_POINTER);
    }

    public Object getReferenceTagDefinition4Parameter() {
        return getTagDefinition(STEREO_NAME_PARAMETER, 
                TV_NAME_REFERENCE);
    }
    
    public Object getConstTagDefinition4Parameter() {
        return getTagDefinition(STEREO_NAME_PARAMETER, 
                TV_NAME_CONST);
    }
    
    public Object getInlineTagDefinition4Operation() {
        return getTagDefinition(STEREO_NAME_OPERATION, 
                TV_NAME_INLINE);
    }

    public void applyCppClassStereotype(Object aClass) {
        applyStereotype(STEREO_NAME_CLASS, aClass);
    }

    public void applyCppAttributeStereotype(Object attribute) {
        applyStereotype(STEREO_NAME_ATTRIBUTE, attribute);
    }

    public void applyCppParameterStereotype(Object parameter) {
        applyStereotype(STEREO_NAME_PARAMETER, parameter);
    }

    public void applyCppRealizationStereotype(Object realization) {
        applyStereotype(STEREO_NAME_REALIZATION, realization);
    }

    public void applyCppGeneralizationStereotype(Object generalization) {
        applyStereotype(STEREO_NAME_GENERALIZATION, generalization);
    }

    public void applyCppOperationStereotype(Object operation) {
        applyStereotype(STEREO_NAME_OPERATION, operation);
    }

    public void applyClassSpecifierTaggedValue(Object modelElement, 
            String classSpecifier) {
        applyTaggedValue(STEREO_NAME_CLASS, TV_NAME_CLASS_SPECIFIER, 
                modelElement, classSpecifier);
    }

    public void applyMultiplicityTypeTaggedValue(Object modelElement, 
            String multiplicityType) {
        applyTaggedValue(STEREO_NAME_ATTRIBUTE, TV_NAME_MULTIPLICITY_TYPE, 
                modelElement, multiplicityType);
    }

    public void applyPointerTaggedValue2Parameter(Object param, 
            String isPointer) {
        applyTaggedValue(STEREO_NAME_PARAMETER, TV_NAME_POINTER, 
                param, isPointer);
    }

    public void applyReferenceTaggedValue2Parameter(Object param, 
            String isReference) {
        applyTaggedValue(STEREO_NAME_PARAMETER, TV_NAME_REFERENCE, 
                param, isReference);
    }

    public void applyConstTaggedValue2Parameter(Object param, 
            String isConst) {
        applyTaggedValue(STEREO_NAME_PARAMETER, TV_NAME_CONST, 
                param, isConst);
    }

    public void applyInheritanceVisibilityTaggedValue2Generalization(
            Object generalization, String visibility) {
        applyTaggedValue(STEREO_NAME_GENERALIZATION, 
                TV_NAME_INHERITANCE_VISIBILITY, generalization, visibility);
    }

    public void applyVirtualInheritanceTaggedValue(Object generalization, 
            String isVirtual) {
        applyTaggedValue(STEREO_NAME_GENERALIZATION, 
                TV_NAME_VIRTUAL_INHERITANCE, generalization, isVirtual);
    }

    public void applyInheritanceVisibilityTaggedValue2Realization(
            Object realization, String visibility) {
        applyTaggedValue(STEREO_NAME_REALIZATION, 
                TV_NAME_INHERITANCE_VISIBILITY, realization, visibility);
    }

    public void applyInlineTaggedValue2Operation(Object operation, 
            String isInline) {
        applyTaggedValue(STEREO_NAME_OPERATION, TV_NAME_INLINE, 
                operation, isInline);
    }
}
