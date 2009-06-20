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

package org.argouml.language.cpp.reveng;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.argouml.kernel.Project;
import org.argouml.model.IllegalModelElementConnectionException;
import org.argouml.model.Model;
import static org.argouml.model.Model.*;
import org.argouml.profile.Profile;
import org.argouml.profile.ProfileException;
import org.argouml.language.cpp.profile.ProfileCpp;
import static org.argouml.language.cpp.profile.ProfileCpp.*;

/**
 * Implementation of the <code>Modeler</code> interface. This facade
 * implements part and delegates the rest of the implementation that transforms
 * the parsed information from a C++ translation unit into UML model elements
 * and updating the model with it.
 *
 * @author euluis
 * @since 0.19.3
 */
public class ModelerImpl implements Modeler {
    /**
     * The context stack keeps track of the current parsing context in a stack
     * wise manner.
     */
    private Stack contextStack = new Stack();

    /**
     * The access specifier applicable to the parsing context. May be null if
     * not within a classifier.
     */
    private Object contextAccessSpecifier;

    /**
     * Counts the member declaration level.
     */
    private int memberDeclarationCount;

    /**
     * Counts the compound statement level.
     */
    private int compoundStatementCount;

    private boolean ignoreableFunctionDefinition;

    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(ModelerImpl.class);
    
    private Collection newElements;
    
    private ProfileCpp profile;
    
    private Project project;
    
    private AttributeModeler attributeModeler;
    
    private OperationModeler operationModeler;
    
    ModelerImpl(Project p) throws ProfileException {
        project = p;
        List<Profile> projectProfiles = 
            project.getProfileConfiguration().getProfiles();
        Profile cppProfile = null;
        for (Profile projectProfile : projectProfiles) {
            if (projectProfile.getDisplayName() != null 
                && projectProfile.getDisplayName().contains("C++")) {
                cppProfile = projectProfile;
                break;
            }
        }
        if (cppProfile != null) {
            profile = new ProfileCpp(project.getUserDefinedModelList(), 
                cppProfile.getProfilePackages().iterator().next());
        }
        else {
            profile = new ProfileCpp(project.getModels());
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#beginTranslationUnit()
     */
    public void beginTranslationUnit() {
        newElements = new HashSet();
        contextStack.push(getModel());
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#endTranslationUnit()
     */
    public void endTranslationUnit() {
        // for now we don't need to do anything here
    }
    
    /*
     * @see org.argouml.language.cpp.reveng.Modeler#getNewElements()
     */
    public Collection getNewElements() {
        return newElements;
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#enterNamespaceScope(java.lang.String)
     */
    public void enterNamespaceScope(String nsName) {
        if (!ignore()) {
            Object parentNs = getCurrentNamespace();
            Object ns = findNamespace(nsName, parentNs);
            if (ns == null) {
                ns = Model.getModelManagementFactory().buildPackage(nsName);
                newElements.add(ns);
                getCoreHelper().setNamespace(ns, parentNs);
            }
            contextStack.push(ns);
        }
    }

    /**
     * Get the current namespace from the {@link #contextStack contextStack} or
     * the model.
     * 
     * @return the parent namespace
     */
    private Object getCurrentNamespace() {
        Object parentNs = null;
        if (contextStack.isEmpty()) {
            parentNs = getModel();
        } else {
            parentNs = contextStack.peek();
            assert getFacade().isANamespace(parentNs);
        }
        return parentNs;
    }

    /**
     * Find the namespace with the given name which parent is
     * <code>parentNs</code>.
     *
     * @param nsName namespace name
     * @param parentNs the parent namespace of the namespace to get
     * @return the namespace if it exists, <code>null</code> otherwise.
     */
    private Object findNamespace(String nsName, Object parentNs) {
        Collection nss =
	    Model.getModelManagementHelper().getAllNamespaces(getModel());
        Iterator it = nss.iterator();
        Object ns = null;
        while (it.hasNext()) {
            Object tmpNs = it.next();
            if (nsName.equals(getFacade().getName(tmpNs))) {
                // NOTE: equality by reference may be deceiving if the
                // implementation uses proxies - not likely that different
                // proxies are used, so at least we should be comparing the
                // references of the same proxy object!
                if (getFacade().getNamespace(tmpNs) == parentNs) {
                    ns = tmpNs;
                    break;
                }
            }
        }
        return ns;
    }
    
    private Object model;

    /**
     * FIXME: the user model should be received via constructor.
     * @return the user model
     */
    private Object getModel() {
        if (model != null) {
            return model;
        }
        for (Object userModel : getProject().getUserDefinedModelList()) {
            if (!getModelManagementHelper().isReadOnly(userModel)) {
                model = userModel;
                return model;
            }
        }
        throw new IllegalStateException("An editable user model wasn't found!");
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#exitNamespaceScope()
     */
    public void exitNamespaceScope() {
        if (!ignore()) {
            Object ns = contextStack.pop();
            assert getFacade().isANamespace(ns) : "The popped context (\""
                + ns + "\") isn't a namespace!";
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#makeNamespaceAlias(java.lang.String,
     *      java.lang.String)
     */
    public void makeNamespaceAlias(String ns, String alias) {
        // TODO: implement after defining the way this is supposed to be
        // modeled
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#beginClassDefinition(java.lang.String,
     *      java.lang.String)
     */
    public void beginClassDefinition(String oType, String identifier) {
        if (!ignore()) {
            // a class is defined, so, check if it exists in the model or
            // create it...
            Object ns = getCurrentNamespace();
            // FIXME: we're not checking for oType here! Is it needed? Is it
            // possible to have struct X and class X in the same namespace in
            // C++?
            Object cls = findClass(identifier, ns);
            if (cls == null) {
                cls = getCoreFactory().buildClass(identifier, ns);
                profile.applyCppClassStereotype(cls);
                newElements.add(cls);
            }
            contextStack.push(cls);
            if (CPPvariables.OT_CLASS.equals(oType)) {
                // the default visibility for a C++ class
                contextAccessSpecifier = getVisibilityKind().getPrivate();
            } else if (CPPvariables.OT_STRUCT.equals(oType)) {
                contextAccessSpecifier = getVisibilityKind().getPublic();
                profile.applyClassSpecifierTaggedValue(cls, "struct");
            } else if (CPPvariables.OT_UNION.equals(oType)) {
                // TODO: implement union specifics.
                ;
            } else {
                assert false
                : "Not expecting any other oType than class, struct and "
                    + "union!";
            }
        }
    }

    /**
     * Find a class within the given namespace that has the given identifier.
     *
     * @param identifier the class identifier
     * @param ns namespace to look in
     * @return the class if found, null otherwise
     */
    private static Object findClass(String identifier, Object ns) {
        Collection classes = getCoreHelper().getAllClasses(ns);
        Iterator it = classes.iterator();
        while (it.hasNext()) {
            Object candidateClass = it.next();
            if (Model.getFacade().getName(candidateClass).equals(identifier)) {
                return candidateClass;
            }
        }
        return null;
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#endClassDefinition()
     */
    public void endClassDefinition() {
        if (!ignore()) {
            Object cls = contextStack.pop();
            assert getFacade().isAClass(cls) : "The popped context (\""
                + getFacade().getName(cls) + "\") isn't a class!";
            contextAccessSpecifier = null;
        }
    }

    /*
     * FIXME: I think that with nested classes having only one access specifier
     * won't work. This must be implemented in a stack scheme, where the
     * constructs that can work with access specifiers will need to manage the
     * stack.
     *
     * @see org.argouml.language.cpp.reveng.Modeler#accessSpecifier(java.lang.String)
     */
    public void accessSpecifier(String accessSpec) {
        if (!ignore()) {
            if ("public".equals(accessSpec)) {
                contextAccessSpecifier = Model.getVisibilityKind().getPublic();
            } else if ("protected".equals(accessSpec)) {
                contextAccessSpecifier =
		    Model.getVisibilityKind().getProtected();
            } else if ("private".equals(accessSpec)) {
                contextAccessSpecifier = Model.getVisibilityKind().getPrivate();
            } else {
                assert false : "Unknown C++ access specifier: " + accessSpec;
            }
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#beginFunctionDeclaration()
     */
    public void beginFunctionDeclaration() {
        if (!ignore()) {
            operationModeler = new OperationModeler(contextStack.peek(), 
                    contextAccessSpecifier, getVoid(), false, profile);
            contextStack.push(operationModeler.getOperation());
        }
    }
    
    private Project getProject() {
        return project;
    }

    /**
     * @return the void DataType
     */
    private Object getVoid() {
        return getProject().findType("void");
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#endFunctionDeclaration()
     */
    public void endFunctionDeclaration() {
        if (!ignore()) {
            assert operationModeler != null : "operationModeler is null.";
            Object oper = contextStack.pop();
            assert Model.getFacade().isAOperation(oper) : ""
                + "The popped context (\"" + oper + "\") isn't an operation!";
            operationModeler.finish();
            operationModeler = null;
        }
    }

    private TypedefModeler typedefModeler;

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#declarationSpecifiers(java.util.List)
     */
    public void declarationSpecifiers(List declSpecs) {
        if (declSpecs.contains("typedef")) {
            assert typedefModeler == null;
            typedefModeler = new TypedefModeler(contextStack.peek(), 
                contextAccessSpecifier, profile);
        } else if (getFacade().isAOperation(contextStack.peek())) {
            OperationModeler modeler = operationModeler != null
                ? operationModeler : xtorModeler;
            modeler.declarationSpecifiers(declSpecs);
        }
    }
    
    /*
     * @see org.argouml.language.cpp.reveng.Modeler#simpleTypeSpecifier(java.util.List)
     */
    public void simpleTypeSpecifier(List sts) {
        if (!ignore()) {
            StringBuffer stsString = new StringBuffer();
            Iterator i = sts.iterator();
            while (i.hasNext()) {
                stsString.append(i.next().toString()).append(" ");
            }
            LOG.debug("In simpleTypeSpecifier, stsString = " + stsString);
            Object theType = findOrCreateType(stsString.toString().trim());
            if (memberModeler != null) {
                memberModeler.setType(theType);
            }
            // now, depending on the context, this might be the return type of a
            // function declaration or an attribute of a class or a variable
            // declaration; of course, this is rather incomplete(!)
            Object contextModelElement = contextStack.peek();
            if (getFacade().isAOperation(contextModelElement)) {
                assert operationModeler != null
                    : "operationModeler is null in the context of operation "
                        + getFacade().getName(contextModelElement) + ".";
                operationModeler.setType(theType);
            } else if (getFacade().isAClass(contextModelElement)) {
                // an attribute or an enumeration... handled elsewhere
            } else if (getFacade().isAParameter(contextModelElement)) {
                getCoreHelper().setType(contextModelElement, theType);
            } else if (getFacade().isAModel(contextModelElement) 
                    || getFacade().isANamespace(contextModelElement)) {
                // we either have a global variable or a typedef
                if (typedefModeler != null) {
                    typedefModeler.setType(theType);
                }
            }
        }
    }

    /**
     * Finds or creates a type with the given name. This method
     * delegates the call to ArgoUML helper method, but, first takes
     * care of C++ specific issues, such as pointer and reference
     * stripping and buit-in types which shouldn't be created as
     * classes (the way ArgoUML does), but, as DataType.
     *
     * @param typeName the name of the type
     * @return A model element that represents the given type
     */
    private Object findOrCreateType(String typeName) {
        Object theType = null;
        List taggedValues = new LinkedList();
        processPtrOperators(typeName, taggedValues);
        if (profile.isBuiltIn(typeName)) {
            theType = profile.getBuiltIn(typeName);
        } else {
            theType = getProject().findType(typeName.toString(), true);
        }
        return theType;
    }

    /**
     * Process a type specification by stripping pointer operators
     * from the type name and processing them to tagged values that
     * are added to the provide list.
     *
     * @param typeName unprocessed C++ type name
     * @param taggedValues list of tagged values where any processing result is
     *            added to
     * @return the stripped type name
     */
    private String processPtrOperators(String typeName, List taggedValues) {
        // TODO: implement
        return typeName;
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#directDeclarator(java.lang.String, boolean)
     */
    public void directDeclarator(String id, boolean typedef) {
        if (!ignore()) {
            LOG.debug("In directDeclarator: id = \"" + id + "\"; typedef = " 
                + typedef);
            if (typedef) {
                assert typedefModeler != null;
                typedefModeler.directDeclarator(id);
                typedefModeler = null;
            } else {
                getCoreHelper().setName(contextStack.peek(), id);
            }
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#storageClassSpecifier(java.lang.String)
     */
    public void storageClassSpecifier(String storageClassSpec) {
        // TODO: Auto-generated method stub
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#typeQualifier(java.lang.String)
     */
    public void typeQualifier(String typeQualifier) {
        // TODO: Auto-generated method stub

    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#beginFunctionDefinition()
     */
    public void beginFunctionDefinition() {
        if (!ignore()) {
            if (isMemberDeclaration()) {
                beginFunctionDeclaration();
                operationModeler.setDefinedInClass();
            } else {
                // TODO: here we should set the method of the corresponding
                // operation, if it exists, or create a global operation and
                // set the corresponding method
                ignoreableFunctionDefinition = true;
            }
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#endFunctionDefinition()
     */
    public void endFunctionDefinition() {
        if (!ignore()) {
            if (isMemberDeclaration()) {
                endFunctionDeclaration();
            } else {
                // TODO: here we should set the method of the corresponding
                // operation, if it exists, or create a global operation and
                // set the corresponding method
                ignoreableFunctionDefinition = false;
            }
        }
    }

    /**
     * @return true if this call occurs within a member declaration
     */
    private boolean isMemberDeclaration() {
        return memberDeclarationCount > 0;
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#functionDirectDeclarator(java.lang.String)
     */
    public void functionDirectDeclarator(String identifier) {
        if (!ignore()) {
            assert getFacade().isAOperation(contextStack.peek());
            getCoreHelper().setName(contextStack.peek(), identifier);
        }
    }

    /**
     * @return true if the call should be ignored
     */
    private boolean ignore() {
        return compoundStatementCount > 0 || ignoreableFunctionDefinition;
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#beginParameterDeclaration()
     */
    public void beginParameterDeclaration() {
        if (!ignore()) {
            Object oper = contextStack.peek();
            if (Model.getFacade().isAOperation(oper)) {
                // create a parameter within the operation
                Object param =
		    Model.getCoreFactory().buildParameter(oper, getVoid());
                // add the created parameter to the stack
                contextStack.push(param);
            }
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#endParameterDeclaration()
     */
    public void endParameterDeclaration() {
        if (!ignore()) {
            // NOTE: this is different from the other endXxx() methods, since,
            // we may be called within the context of a function definition
            // without it being a member.
            Object param = contextStack.peek();
            if (Model.getFacade().isAParameter(param)) {
                contextStack.pop();
                // set the parameter kind according to the tagged value details
                // FIXME: this won't work when we have const reference
                // parameters!
                if (Model.getFacade().getTaggedValueValue(param,
                    TV_NAME_REFERENCE).equals("true")
                    || Model.getFacade().getTaggedValueValue(param,
                        TV_NAME_POINTER).equals("true")) {
                    Model.getCoreHelper().setKind(param,
                        Model.getDirectionKind().getInOutParameter());
                }
            }
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#beginInitializer()
     */
    public void beginInitializer() {
        if (!ignore()) {
            Object context = contextStack.peek();
            if (Model.getFacade().isAOperation(context)) {
                // we don't really need to see what it is being initialized
                // to, for sure it is to 0 => abstract operation
                Model.getCoreHelper().setAbstract(context, true);
            }
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#endInitializer()
     */
    public void endInitializer() {
        // do nothing
    }
    
    private MemberModeler memberModeler;
    
    /*
     * @see org.argouml.language.cpp.reveng.Modeler#beginMemberDeclaration()
     */
    public void beginMemberDeclaration() {
        Object owner = contextStack.peek();
        assertIsAClassifier(owner);
        memberModeler = new MemberModeler(owner, contextAccessSpecifier,
                profile);
        memberDeclarationCount++;
    }

    void assertIsAClassifier(Object modelElement) {
        assert getFacade().isAClassifier(modelElement) 
            : "modelElement must be a Classifier; its name is \"" 
                + getFacade().getName(modelElement) + "\".";
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#endMemberDeclaration()
     */
    public void endMemberDeclaration() {
        memberDeclarationCount--;
        memberModeler.finish();
        memberModeler = null;
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#beginCompoundStatement()
     */
    public void beginCompoundStatement() {
        compoundStatementCount++;
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#endCompoundStatement()
     */
    public void endCompoundStatement() {
        compoundStatementCount--;
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#beginPtrOperator()
     */
    public void beginPtrOperator() {
        if (!ignore()) {
            Object ptrTV = Model.getExtensionMechanismsFactory().
                buildTaggedValue(ProfileCpp.getTagDefinition("dummy"), 
                    new String[] {""});
            contextStack.push(ptrTV);
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#endPtrOperator()
     */
    public void endPtrOperator() {
        if (!ignore()) {
            Object ptrTV = contextStack.pop();
            assert Model.getFacade().isATaggedValue(ptrTV) 
                : "A Tagged Value was expected, but, got: \"" + ptrTV + "\".";
            Object meToBeTagged = contextStack.peek();
            if (getFacade().isAOperation(meToBeTagged)) {
                Collection rps = getCoreHelper().getReturnParameters(
                    meToBeTagged);
                assert rps.size() == 1;
                meToBeTagged = rps.iterator().next();
            }
            Model.getExtensionMechanismsHelper().addTaggedValue(
                meToBeTagged, ptrTV);
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#ptrOperator(java.lang.String)
     */
    public void ptrOperator(String ptrSymbol) {
        if (!ignore()) {
            if (ptrSymbol.equals("&") || ptrSymbol.equals("*")) {
                // arrrg we must discard the tagged value created before cause
                // we can't change its name
                Object discardedTV = contextStack.pop();
                assert getFacade().isATaggedValue(discardedTV);

                Object paramOrAttribute = contextStack.peek();
                assert getFacade().isAParameter(paramOrAttribute) 
                    || getFacade().isAAttribute(paramOrAttribute) 
                    || getFacade().isAOperation(paramOrAttribute);
                String stereoName = null;
                if (getFacade().isAParameter(paramOrAttribute)) {
                    stereoName = STEREO_NAME_PARAMETER;
                }
                else if (getFacade().isAAttribute(paramOrAttribute)) {
                    stereoName = STEREO_NAME_ATTRIBUTE;
                }
                else if (getFacade().isAOperation(paramOrAttribute)) {
                    Collection rps = getCoreHelper().getReturnParameters(
                        paramOrAttribute);
                    assert rps.size() == 1;
                    paramOrAttribute = rps.iterator().next();
                    stereoName = STEREO_NAME_PARAMETER;
                }
                else {
                    LOG.warn("Unexpected reveng context: " + paramOrAttribute);
                    return;
                }
                profile.applyStereotype(stereoName, paramOrAttribute);
                String tvName = null;
                if (ptrSymbol.equals("&")) {
                    tvName = TV_NAME_REFERENCE;
                } else if (ptrSymbol.equals("*")) {
                    tvName = TV_NAME_POINTER;
                }
                profile.applyTaggedValue(stereoName, tvName, paramOrAttribute, 
                        "true");
                Object tv = getFacade().getTaggedValue(paramOrAttribute, 
                        tvName);
                contextStack.push(tv);
            } else {
                LOG.warn("unprocessed ptrSymbol: " + ptrSymbol);
            }
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#ptrToMember(java.lang.String,
     *      java.lang.String)
     */
    public void ptrToMember(String scopedItem, String star) {
        // TODO: Auto-generated method stub
    }

    /**
     * Modeler for the base_specifier rule.
     */
    private BaseSpecifierModeler baseSpecifierModeler;

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#beginBaseSpecifier()
     */
    public void beginBaseSpecifier() {
        if (!ignore()) {
            baseSpecifierModeler = new BaseSpecifierModeler();
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#endBaseSpecifier()
     */
    public void endBaseSpecifier() {
        if (!ignore()) {
            baseSpecifierModeler.finish();
            baseSpecifierModeler = null;
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#baseSpecifier(java.lang.String,
     *      boolean)
     */
    public void baseSpecifier(String identifier, boolean isVirtual) {
        if (!ignore()) {
            baseSpecifierModeler.baseSpecifier(identifier, isVirtual);
	}
    }

    /**
     * A class that models the base_specifier rule, mapping it to a UML
     * generalization.
     */
    private class BaseSpecifierModeler {
        private Object previousAccessSpecifier;

        private Object generalization;

        /**
         * The constructor of the BaseSpecifierModeler retrieves information
         * about the current parsing context, storing it to enable context aware
         * processing of the baseSpecifier call, and resetting the context to
         * its previous state after processing.
         */
        BaseSpecifierModeler() {
            previousAccessSpecifier = contextAccessSpecifier;
            contextAccessSpecifier = null;
        }

        /**
         *
         * @param identifier the base class identifier
         * @param isVirtual flags virtual inheritance
         */
        void baseSpecifier(String identifier, boolean isVirtual) {
            // create a generalization for the current class
            Object parent = findOrCreateType(identifier);
            generalization =
		findOrCreateGeneralization(parent, contextStack.peek());
            profile.applyVirtualInheritanceTaggedValue(generalization, 
                    Boolean.toString(isVirtual));
        }

        /**
         * Finish processing the base specifier rule.
         */
        void finish() {
            // set the visibility of the generalization
            if (contextAccessSpecifier == null) { // default is private
                contextAccessSpecifier = Model.getVisibilityKind().getPrivate();
            }
            profile.applyInheritanceVisibilityTaggedValue2Generalization(
                    generalization, 
                    getFacade().getName(contextAccessSpecifier));
            // finish the base specifier by setting the context to the
            // previous state
            contextAccessSpecifier = previousAccessSpecifier;
        }
    }

    /**
     * Find or create a Generalization between the given parent and child
     * Classifiers.
     *
     * @param parent the parent Classifier
     * @param child the child Classifier
     * @return the found Generalization model element if found, otherwise a
     *         newly created
     */
    private Object findOrCreateGeneralization(Object parent,
            Object child) {
        Object generalization =
	    Model.getFacade().getGeneralization(child, parent);
        Object stereotype = null;
        if (generalization == null) {
            try {
                generalization = getUmlFactory().buildConnection(
                        getMetaTypes().getGeneralization(), child, null, 
                        parent, null, null, null);
            } catch (IllegalModelElementConnectionException e) {
                LOG.error("Exception while creating generalization.", e);
                throw new RuntimeException(e);
            }
	} else {
            Collection stereotypes = getFacade().getStereotypes(generalization);
            for (Object aStereotype : stereotypes) {
                if (STEREO_NAME_GENERALIZATION.equals(
                        getFacade().getName(aStereotype))) {
                    stereotype = aStereotype;
                }
            }
        }
        assert generalization != null;
        if (stereotype == null) {
            stereotype = profile.getCppGeneralizationStereotype();
            assert stereotype != null;
            getCoreHelper().addStereotype(generalization, stereotype);
        }
        return generalization;
    }
    
    private boolean isXtorIgnorable() {
        return contextStack.size() == 0 
            || getFacade().isAModel(contextStack.peek());
    }

    /**
     * Modeler for constructors and destructors.
     */
    private XtorModeler xtorModeler;
    
    private static interface XtorModelerCreator {
        XtorModeler create(Object owner, Object visibility, Object returnType, 
                boolean ignorable);
    }

    private void beginXtor(final XtorModelerCreator modelerCreator) {
        if (!ignore()) {
            assert xtorModeler == null;
            boolean ignorable = isXtorIgnorable();
            Object owner = contextStack.peek();
            xtorModeler = modelerCreator.create(owner, contextAccessSpecifier,
                getVoid(), ignorable);
            if (!ignorable) {
                assertIsAClassifier(owner);
                contextStack.push(xtorModeler.getOperation());
            }
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#beginCtorDefinition()
     */
    public void beginCtorDefinition() {
        beginCtor();
        if (!ignore()) {
            xtorModeler.setDefinedInClass();
        }
    }

    private void beginCtor() {
        final XtorModelerCreator modelerCreator = new XtorModelerCreator() {
            public XtorModeler create(Object owner, Object visibility,
                    Object returnType, boolean ignorable) {
                return new CtorModeler(owner, visibility, returnType,
                        ignorable, profile);
            }
        };
        beginXtor(modelerCreator);
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#beginDtorHead()
     */
    public void beginDtorHead() {
        final XtorModelerCreator modelerCreator = new XtorModelerCreator() {
            public XtorModeler create(Object owner, Object visibility,
                    Object returnType, boolean ignorable) {
                return new DtorModeler(owner, visibility, returnType,
                        ignorable, profile);
            }
        };
        beginXtor(modelerCreator);
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#endCtorDefinition()
     */
    public void endCtorDefinition() {
        endXtor();
    }

    private void endXtor() {
        if (!ignore()) {
            if (!xtorModeler.isIgnorable()) {
                Object poppedXtor = contextStack.pop();
                assert xtorModeler.isTheXtor(poppedXtor);
            }
            xtorModeler.finish();
            xtorModeler = null;
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#qualifiedCtorId(java.lang.String)
     */
    public void qualifiedCtorId(String identifier) {
        if (!ignore()) {
            boolean onlyDeclaration = false;
            if (xtorModeler == null) {
                beginCtor();
                onlyDeclaration = true;
            }
            xtorModeler.setName(identifier);
            if (!xtorModeler.isIgnorable()) {
                assert xtorModeler.isTheXtor(contextStack.peek());
            }
            if (onlyDeclaration) {
                endXtor();
            }
        }
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#endDtorHead()
     */
    public void endDtorHead() {
        endXtor();
    }

    /*
     * @see org.argouml.language.cpp.reveng.Modeler#dtorDeclarator(java.lang.String)
     */
    public void dtorDeclarator(String qualifiedId) {
        if (!xtorModeler.isIgnorable()) {
            assert xtorModeler.isTheXtor(contextStack.peek());
        }
        xtorModeler.setName(qualifiedId);
    }

    public void beginMemberDeclarator() {
        Object theType = memberModeler.getType();
        attributeModeler = new AttributeModeler(contextStack.peek(),
            contextAccessSpecifier, theType, profile);
        contextStack.push(attributeModeler.getAttribute());
    }

    public void endMemberDeclarator() {
        if (getFacade().isAAttribute(contextStack.peek())) {
            attributeModeler.finish();
            assert attributeModeler.getAttribute() == contextStack.peek();
            contextStack.pop();
        }
    }

    public void beginMemberDeclaratorList() {
        // TODO
    }

    public void endMemberDeclaratorList() {
        // TODO
    }

}
