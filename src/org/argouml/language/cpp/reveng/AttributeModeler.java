// $Id$
// Copyright (c) 2009 The Regents of the University of California. All
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

import org.argouml.model.Model;

/**
 * Modeler for C++ class member variables.
 *
 * @author Luis Sergio Oliveira (euluis)
 * @since 0.28.0
 */
class AttributeModeler {
    
    Object attr;
    Object type;
    Object owner;
    
    AttributeModeler(Object owner, Object type, Object accessSpecifier) {
        this.owner = owner;
        this.type = type;
        attr = Model.getCoreFactory().buildAttribute2(owner, type);
        if (accessSpecifier != null) {
            Model.getCoreHelper().setVisibility(attr, accessSpecifier);
        }
    }

    /**
     * Check if the given attribute is a duplicate of other already existing
     * attribute and if so remove it.
     *
     * FIXME: this method is very similar to the {@link
     * ModelerImpl#removeOperationIfDuplicate(Object) 
     * removeOperationIfDuplicate}
     * method. Both are related to the removal of a Feature from a
     * Classifier if the Feature is duplicated in the Classifier. I
     * think this may be refactored in the future...
     *
     */
    void removeAttributeIfDuplicate() {
        Collection attrs = Model.getFacade().getAttributes(owner);

        for (Object possibleDuplicateAttr : attrs) {
            if (attr != possibleDuplicateAttr
                && Model.getFacade().getName(attr).equals(
                    Model.getFacade().getName(possibleDuplicateAttr))) {
                Model.getCoreHelper().removeFeature(owner, attr);
            }
        }
    }
    
}
