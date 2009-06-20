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

import static org.argouml.model.Model.getCoreHelper;
import static org.argouml.model.Model.getCoreFactory;
import static org.argouml.model.Model.getFacade;

import java.util.Collection;

import org.argouml.language.cpp.profile.ProfileCpp;

/**
 * Modeler for C++ class member variables.
 *
 * @author Luis Sergio Oliveira (euluis)
 * @since 0.28.0
 */
class AttributeModeler extends MemberModeler {
    
    private Object attr;

    /**
     * @return Returns the attribute model element.
     */
    Object getAttribute() {
        return attr;
    }
    
    AttributeModeler(Object theOwner, Object accessSpecifier, Object theType,
            ProfileCpp theProfile) {
        super(theOwner, accessSpecifier, theProfile);
        attr = getCoreFactory().buildAttribute2(getOwner(), theType);
        if (accessSpecifier != null) {
            getCoreHelper().setVisibility(attr, accessSpecifier);
        }
    }
    
    void finish() {
        removeAttributeIfDuplicate();
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
    private void removeAttributeIfDuplicate() {
        Collection attrs = getFacade().getAttributes(getOwner());

        for (Object possibleDuplicateAttr : attrs) {
            if (getAttribute() != possibleDuplicateAttr
                && getFacade().getName(getAttribute()).equals(
                    getFacade().getName(possibleDuplicateAttr))) {
                getCoreHelper().removeFeature(getOwner(), getAttribute());
            }
        }
    }
}
