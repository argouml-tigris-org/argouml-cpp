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

import org.apache.log4j.Logger;
import org.argouml.language.cpp.profile.ProfileCpp;
import org.argouml.model.Model;

class TypedefModeler extends MemberModeler {
    private static final Logger LOG = Logger.getLogger(TypedefModeler.class);
    
    TypedefModeler(Object theOwner, Object accessSpecifier, 
            ProfileCpp theProfile) {
        super(theOwner, accessSpecifier, theProfile);
    }
    
    void directDeclarator(String id) {
        LOG.debug("Got the name: " + id);
        Object typedef = Model.getCoreFactory().buildDataType(id, getOwner());
        // TODO: set the tagged value typedef to the name of type.
//        profile.applyCppDatatypeStereotype(typedef);
//        profile.applyTypedefTaggedValue(typedef, 
//            getFacade().getName(getType()));
    }
}
