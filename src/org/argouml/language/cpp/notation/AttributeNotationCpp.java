// $Id$
// Copyright (c) 2006-2009 The Regents of the University of California. All
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

import org.argouml.language.cpp.generator.GeneratorCpp;
import org.argouml.notation.NotationSettings;
import org.argouml.notation.providers.AttributeNotation;

/**
 * 
 * @author Luis Sergio Oliveira (euluis)
 */
public class AttributeNotationCpp extends AttributeNotation {

    /**
     * 
     */
    public AttributeNotationCpp() {
    }

    private static final AttributeNotationCpp INSTANCE = 
        new AttributeNotationCpp();

    public static final AttributeNotationCpp getInstance() {
        return INSTANCE;
    }

    /*
     * @see org.argouml.notation.providers.NotationProvider#getParsingHelp()
     */
    public String getParsingHelp() {
        /* TODO: MVW: This used to return null. However that 
         * causes issue 5557. Do we need a different string here?
         */
        return "Parsing in C++ not yet supported";
    }

    /*
     * @see org.argouml.notation.providers.NotationProvider#parse(java.lang.Object,
     *      java.lang.String)
     */
    public void parse(Object attribute, String text) {
        // TODO: Auto-generated method stub
    }

    /**
     * @param attribute
     *            The Attribute for which to return the syntax representation in
     *            C++.
     * @param settings
     *            Configuration parameters for the notation.
     * @return The C++ representation of attribute as a String.
     * @see org.argouml.notation.NotationProvider#toString(Object, NotationSettings)
     */    
    public String toString(Object attribute, NotationSettings settings) {
        return GeneratorCpp.getInstance().generateAttribute(attribute, false);
    }

}
