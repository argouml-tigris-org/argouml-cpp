// $Id$
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
import static org.argouml.model.Model.getFacade;

import java.util.Collection;
import java.util.Iterator;

import org.argouml.model.Model;
import org.argouml.uml.StereotypeUtility;

/**
 * Base Modeler class for constructor and destructor modelers.
 *
 * @author Luis Sergio Oliveira (euluis)
 * @since 0.29.1
 */
abstract class XtorModeler extends OperationModeler {

    /**
     * Initializes the XtorModeler with the stereotypeName, creates the
     * operation in the class and sets the stereotype with the given name.
     * TODO: process definitions made outside of the class definition!
     * TODO: how will I get the class name?
     *
     * @param stereotypeName "create" for ctors and "destroy" for dtors.
     */
    XtorModeler(Object theParent, Object visibility, Object returnType, 
            String stereotypeName, boolean ignorable) {
        super(theParent, visibility, returnType, ignorable);
        if (isIgnorable()) {
            return;
        }
        assert getFacade().isAClass(theParent);
        Model.getExtensionMechanismsHelper().addCopyStereotype(getOperation(),
            getStereotype(getOperation(), stereotypeName));
    }

    /**
     * Helper method that gets a stereotype for the given model
     * element with the given name.
     *
     * TODO: this might be a performance bottleneck. If so, caching of
     * stereotypes for model elements by their stereotypes names could fix it.
     *
     * @param modelElement The model element for which to look for the
     *            stereotype with the given name
     * @param stereotypeName The name of the stereotype.
     * @return the stereotype model element or null if not found.
     */
    private Object getStereotype(Object modelElement, String stereotypeName) {
        Object stereotype = null;
        Collection stereotypes = StereotypeUtility.getAvailableStereotypes(
                modelElement);
        for (Iterator it = stereotypes.iterator(); it.hasNext();) {
            Object candidateStereotype = it.next();
            if (getFacade().getName(candidateStereotype).equals(
                stereotypeName)) {
                stereotype = candidateStereotype;
                break;
            }
        }
        return stereotype;
    }

    /**
     * Checks if the given modelElement is the xtor (e.g. the modeled
     * constructor or destructor).
     *
     * @param modelElement the operation to check
     * @return true if the modelElement is the modeled xtor
     */
    boolean isTheXtor(Object modelElement) {
        return getOperation() == modelElement;
    }

    /**
     * Sets the name of the operation.
     *
     * @param name
     */
    void setName(String name) {
        if (isIgnorable()) {
            return;
        }
        getCoreHelper().setName(getOperation(), name);
    }
}
