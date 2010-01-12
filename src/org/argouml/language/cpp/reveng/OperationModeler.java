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

package org.argouml.language.cpp.reveng;

import static org.argouml.model.Model.getCoreHelper;
import static org.argouml.model.Model.getCoreFactory;
import static org.argouml.model.Model.getFacade;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.argouml.language.cpp.profile.ProfileCpp;

/**
 * Modeler for C++ class member functions.
 *
 * @author Luis Sergio Oliveira (euluis)
 * @since 0.29.1
 */
class OperationModeler extends MemberModeler {
    private Object oper;
    private final boolean ignorable;
    
    boolean isIgnorable() {
        return ignorable;
    }

    /**
     * @return Returns the operation.
     */
    Object getOperation() {
        return oper;
    }

    OperationModeler(Object theParent, Object visibility, Object returnType, 
            boolean ignore, ProfileCpp theProfile) {
        super(theParent, visibility, theProfile);
        ignorable = ignore;
        if (!ignorable) { 
            oper = buildOperation(getOwner(), returnType);
            getCoreHelper().setLeaf(oper, true);
            if (visibility != null) {
                getCoreHelper().setVisibility(oper, visibility);
            }
        }
    }

    /**
     * Create a operation in the given model element.
     *
     * @param me the model element for which to build the operation
     * @param returnType the operation return type
     * @return the operation
     */
    Object buildOperation(Object me, Object returnType) {
        return getCoreFactory().buildOperation(me, returnType);
    }
    
    /**
     * Check if the operation is a duplicate of other already existing
     * operation and if so remove it.
     */
    void finish() {
        if (!isIgnorable()) {
            if (getFacade().isLeaf(oper) 
                && hasNonLeafBaseOperation(oper, getOwner())) {
                getCoreHelper().setLeaf(oper, false);
            }
            removeOperationIfDuplicate(oper);
        }
    }

    boolean hasNonLeafBaseOperation(Object operation, Object clazz) {
        for (Object generalization : getFacade().getGeneralizations(clazz)) {
            Object base = getFacade().getGeneral(generalization);
            for (Object baseOper : getFacade().getOperations(base)) {
                if (getFacade().getName(operation).equals(
                        getFacade().getName(baseOper))
                    && equalParameters(operation, baseOper)) {
                    return !getFacade().isLeaf(baseOper);
                }
            }
            // we need to go higher in the class hierarchy because the  
            // operation may be declared there
            if (hasNonLeafBaseOperation(operation, base)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the given operation is a duplicate of other already existing
     * operation and if so remove it.
     *
     * @param operation the operation to be checked
     */
    void removeOperationIfDuplicate(Object operation) {
        for (Object possibleDuplicate : getFacade().getOperations(getOwner()))
        {
            if (operation != possibleDuplicate
                && getFacade().getName(operation).equals(
                    getFacade().getName(possibleDuplicate))) {
                if (equalParameters(operation, possibleDuplicate)) {
                    getCoreHelper().removeFeature(getOwner(), operation);
                }
            }
        }
    }

    /**
     * Compare the parameters of two operations.
     *
     * @param oper1 left hand side operation
     * @param oper2 right hand side operation
     * @return true if the parameters are equal - the same types given in the
     *         same order
     */
    private boolean equalParameters(Object oper1, Object oper2) {
        List parameters1 = getFacade().getParametersList(oper1);
        List parameters2 = getFacade().getParametersList(oper2);
        if (parameters1.size() == parameters2.size()) {
            Iterator it1 = parameters1.iterator();
            Iterator it2 = parameters2.iterator();
            while (it1.hasNext()) {
                Object parameter1 = it1.next();
                Object parameter2 = it2.next();
                if (!equalParameter(parameter1, parameter2)) {
                    return false;
                }
            }
        }
        else {
            return false;
        }
        return true;
    }
    
    private boolean equalParameter(Object parameter1, Object parameter2) {
        if (getFacade().getName(parameter1) != null 
            && getFacade().getName(parameter1).equals(
                getFacade().getName(parameter2))) {
            return equalTaggedValues(
                getFacade().getTaggedValuesCollection(parameter1), 
                getFacade().getTaggedValuesCollection(parameter2));
        }
        if (getFacade().getName(parameter1) == null 
            && getFacade().getName(parameter2) == null) {
            return equalTaggedValues(
                getFacade().getTaggedValuesCollection(parameter1), 
                getFacade().getTaggedValuesCollection(parameter2));
        }
        return false;
    }
    
    private boolean equalTaggedValues(Collection taggedValues1, 
        Collection taggedValues2) {
        // FIXME: TODO
        return true;
    }
    
    void declarationSpecifiers(List declSpecs) {
        if (declSpecs.contains("virtual")) {
            getCoreHelper().setLeaf(oper, false);
        }
    }
    
    void setType(Object theType) {
        super.setType(theType);
        setReturnType();
    }
    
    private void setReturnType() {
        Object rv = getCoreHelper().getReturnParameters(oper).iterator().
            next();
        getCoreHelper().setType(rv, getType());
    }

    void setDefinedInClass() {
        if (!isIgnorable()) {
            getProfile().applyCppOperationStereotype(getOperation());
            getProfile().applyInlineTaggedValue2Operation(getOperation(),
                    ProfileCpp.TV_INLINE_STYLE_DEFINITION_INSIDE_CLASS);
        }
    }
}
