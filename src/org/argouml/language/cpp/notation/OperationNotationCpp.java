/* $Id$
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mvw
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

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
import org.argouml.notation.providers.OperationNotation;

/**
 *
 * @author Luis Sergio Oliveira (euluis)
 */
public class OperationNotationCpp extends OperationNotation {

    /**
     * @param operation The Operation for which this object provides notation 
     * services.
     */
    public OperationNotationCpp(Object operation) {
        super(operation);
    }

    /* 
     * @see org.argouml.notation.providers.NotationProvider#getParsingHelp()
     */
    public String getParsingHelp() {
        // TODO: Auto-generated method stub
        return null;
    }

    /**
     * Parses the provided text and adapts the operation accordingly.
     * 
     * @param operation The Operation to be addapted according to the parsed 
     * text.
     * @param text The text to parse.
     * @see org.argouml.notation.NotationProvider#parse(java.lang.Object, java.lang.String)
     */
    public void parse(Object operation, String text) {
        // TODO: Auto-generated method stub
    }

    /**
     * Generate a string representing the C++ syntax for the given operation 
     * according to the provided settings.
     * 
     * @param operation The operation for which to generate the representation. 
     * @param settings Configuration settings that determine the notation.
     * @return The string representation of the operation.
     * @see org.argouml.notation.NotationProvider#toString(Object, NotationSettings)
     */
    @Override
    public String toString(Object operation, NotationSettings settings) {
        return GeneratorCpp.getInstance().generateOperation(operation, false);
    }

}
