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

package org.argouml.language.cpp.generator;

import static org.argouml.model.Model.getAggregationKind;
import static org.argouml.model.Model.getExtensionMechanismsFactory;
import static org.argouml.model.Model.getExtensionMechanismsHelper;
import static org.argouml.model.Model.getFacade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.argouml.language.cpp.profile.ProfileCpp;

/**
 * Handler for pre and post processing the generation of an AssociationEnd, 
 * specifically, it will modify the AssociationEnd before generation of C++ 
 * to enable semantically correct C++ code generation.
 * 
 * See <a href="http://argouml.tigris.org/issues/show_bug.cgi?id=4541">issue 
 * #4541</a> for details on why this is needed.
 * 
 * @author Luís Sérgio Oliveira (euluis)
 */
class AssociationEndHandler {

    private Object associationEnd;

    private List<Object[]> modifiedObjects = new ArrayList<Object[]>();

    AssociationEndHandler(Object theAssociationEnd) {
        this.associationEnd = theAssociationEnd;
    }

    void pre() {
        if (!getAggregationKind().getComposite().equals(
                getFacade().getAggregation(associationEnd))) {
            Collection otherAssociationEnds = 
                getFacade().getOtherAssociationEnds(associationEnd);
            for (Object otherAE : otherAssociationEnds) {
                Iterator taggedValues = getFacade().getTaggedValues(
                        otherAE);
                boolean addPtrTV = true;
                while (taggedValues.hasNext()) {
                    Object tv = taggedValues.next();
                    if (ProfileCpp.isPtrOrRefTVName(getFacade().getName(tv))) {
                        addPtrTV = false;
                        break;
                    }
                }
                if (addPtrTV) {
                    Object ptrTV = getExtensionMechanismsFactory().
                        buildTaggedValue(ProfileCpp.TV_NAME_POINTER, "true");
                    getExtensionMechanismsHelper().addTaggedValue(
                            otherAE, ptrTV);
                    modifiedObjects.add(new Object[] {otherAE, ptrTV});
                }
                
            }
        }
    }

    void post() {
        for (Object[] modifiedOtherAE : modifiedObjects) {
            getExtensionMechanismsHelper().removeTaggedValue(
                    modifiedOtherAE[0], modifiedOtherAE[1]);
        }
    }

}
