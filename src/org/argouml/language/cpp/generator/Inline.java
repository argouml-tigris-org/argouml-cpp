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

// Copyright (c) 2008 The Regents of the University of California. All
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

// Issue #19 - 'inline' keyword support

package org.argouml.language.cpp.generator;

import static org.argouml.language.cpp.profile.ProfileCpp.*;
import static org.argouml.model.Model.getFacade;

/**
 * Small class which holds parameters for generating
 * 'inline' tagged value for C++ Generator
 *
 * @since 19 January 2008
 * @author Lukasz Gromanowski
 */
public enum Inline {
    /**
     * Not inline method definition.
     */
    notInline(-1, null, null, false, MethodDefinitionPlace.inCpp), 
    /**
     * Method definition without 'inline' keyword in class 
     */
    defInsideClass(0, TV_INLINE_STYLE_DEFINITION_INSIDE_CLASS, 
            "cpp.default-inline.definition-inside-class", 
            false, MethodDefinitionPlace.inHeaderInsideClass),
    /**
     * Method definition with 'inline' keyword in class
     */
    keyAndDefInsideClass(1, TV_INLINE_STYLE_KEYWORD_DEFINITION_INSIDE_CLASS, 
            "cpp.default-inline.keyword-and-definition-inside-class", 
            true, MethodDefinitionPlace.inHeaderInsideClass),
    /**
     * Method definition with 'inline' keyword outside class
     */
    keyAndDefOutsideClass(2, TV_INLINE_STYLE_KEYWORD_DEFINITION_OUTSIDE_CLASS, 
            "cpp.default-inline.keyword-and-definition-outside-class", 
            true, MethodDefinitionPlace.inHeaderOutsideClass),
    /**
     * Method definition without 'inline' keyword outside class
     */
    defOutsideClass(3, TV_INLINE_STYLE_DEFINITION_OUTSIDE_CLASS, 
            "cpp.default-inline.definition-outside-class", 
            false, MethodDefinitionPlace.inHeaderOutsideClass);
    
    private static enum MethodDefinitionPlace { inHeaderInsideClass, 
        inHeaderOutsideClass, inCpp; 
    }

    private Inline(int theId, String theTVName, String theLabel, 
            boolean isKeyword, MethodDefinitionPlace theDefPlace) {
        id = theId;
        tvName = theTVName;
        label = theLabel;
        keyword = isKeyword;
        defPlace = theDefPlace;
    }
    
    /**
     * @param inline 'inline' value 
     */
    static void setDefaultStyle(int inline) {
        for (Inline style : Inline.values()) {
            if (style.id == inline) {
                defaultInline = style;
                return;
            }
        };
        assert false : "Unexpected Inline style ID: " + inline;
    }

    /**
     * @return Default 'inline' value
     */
    static int getDefaultStyle() {
        return defaultInline.id;
    }
    
    private int id;

    private String tvName;

    private String label;

    private boolean keyword;

    private MethodDefinitionPlace defPlace;

    /**
     * Default 'inline' value
     */
    private static Inline defaultInline = defInsideClass;
    
    /**
     * Get the inline operation modifier type for op.
     * 
     * @param op operation to check
     * @return One of the Inline enumerators.
     */
    static Inline getInlineOperationModifierType(Object op) {
        Object tv = getFacade().getTaggedValue(op, TV_NAME_INLINE);
        if (tv != null) {
            String val = getFacade().getValueOfTag(tv);
            // Inlining disabled
            if (val.equals(TV_FALSE_VALUE)) {
                return notInline;
            }

            // Default 'inline' value
            if (val.equals(TV_TRUE_VALUE) || val.equals("") || val == null) {
                return defaultInline;
            }

            if (val.equals(defInsideClass.tvName)) {
                return defInsideClass;
            }

            if (val.equals(keyAndDefInsideClass.tvName)) {
                return keyAndDefInsideClass;
            }

            if (val.equals(keyAndDefOutsideClass.tvName)) {
                return keyAndDefOutsideClass;
            }

            if (val.equals(defOutsideClass.tvName)) {
                return defOutsideClass;
            }
        }

        return notInline; // no tag found
    }

    Object getInlineKeyword4Declaration() {
        return keyword ? TV_NAME_INLINE + " " : "";
    }

    boolean isMethodBodyInsideClass() {
        return defPlace == MethodDefinitionPlace.inHeaderInsideClass;
    }

    boolean isMethodBodyOutsideClass() {
        return defPlace == MethodDefinitionPlace.inHeaderOutsideClass;
    }

    static int getDefaultDefaultStyle() {
        return defInsideClass.id;
    }

    public static String[] getStyleLabels() {
        // TODO: ids must match the indexes! This isn't very solid...
        return new String [] {defInsideClass.label, 
            keyAndDefInsideClass.label, 
            keyAndDefOutsideClass.label, 
            defOutsideClass.label};
    }
}
