// $Id: eclipse-argo-codetemplates.xml 11347 2006-10-26 22:37:44Z linus $
// Copyright (c) 2006 The Regents of the University of California. All
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

package org.argouml.language.cpp.notation;

import java.util.HashMap;

import org.argouml.language.cpp.generator.GeneratorCpp;
import org.argouml.model.Model;
import org.argouml.uml.notation.ModelElementNameNotation;

/**
 * 
 * @author Luis Sergio Oliveira (euluis)
 */
public class ModelElementName extends ModelElementNameNotation {

    public ModelElementName(Object modelElement) {
        super(modelElement);
        // TODO: Auto-generated constructor stub
    }

    public String getParsingHelp() {
        // TODO: Auto-generated method stub
        return null;
    }

    /*
     * @see org.argouml.uml.notation.NotationProvider#parse(java.lang.Object,
     *      java.lang.String)
     */
    public void parse(Object modelElement, String text) {
        // TODO: Auto-generated method stub

    }

    /*
     * @see org.argouml.uml.notation.NotationProvider#toString(java.lang.Object,
     *      java.util.HashMap)
     */
    public String toString(Object me, HashMap args) {
        if (Model.getFacade().isAClass(me)
                || Model.getFacade().isAInterface(me)) {
            String className = GeneratorCpp.getInstance()
                    .generateClassifierStart(me).toString();
            int leftCurlyIndex = className.indexOf('{');
            className = className.substring(0, leftCurlyIndex);
            return className.trim();
        }
        return Model.getFacade().getName(me);
    }

}
