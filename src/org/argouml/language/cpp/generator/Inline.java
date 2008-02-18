// $Id$
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

import java.util.Iterator;

//import org.apache.log4j.Logger;

/**
 * Small class which holds parameters for generating
 * 'inline' tagged value for C++ Generator
 *
 * @since 19 January 2008
 * @author Lukasz Gromanowski
 */
public class Inline {
    /**
     * Method definition without 'inline' keyword in class 
     */
    public static final int INLINE_DEF_INSIDE_CLASS = 0;

    /**
     * Method definition with 'inline' keyword in class
     */
    public static final int INLINE_KEY_AND_DEF_INSIDE_CLASS = 1;

    /**
     * Method definition with 'inline' keyword outside class
     */
    public static final int INLINE_KEY_AND_DEF_OUTSIDE_CLASS = 2;

    /**
     * Method definition without 'inline' keyword outside class
     */
    public static final int INLINE_DEF_OUTSIDE_CLASS = 3;
    
    /**
     * Logger 
     */
    //private static final Logger LOG = Logger.getLogger(Inline.class);
    
    /**
     * @param inline 'inline' value 
     */
    static void setDefaultInline(int inline) {
        defaultInline = inline;
    }

    /**
     * @return Default 'inline' value
     */
    static int getDefaultInline() {
        return defaultInline;
    }

    /**
     * Default 'inline' value
     */
    private static int defaultInline = INLINE_DEF_INSIDE_CLASS;

    
    /**
     * @param elem element to check
     * @return Values from INLINE_DEF_IN_CLASS to INLINE_DEF_OUTSIDE_CLASS range
     *         or -1 if no specific tag is found (or if it is disabled by
     *         'false')
     */
    public static int getInlineOperationModifierType(Object elem) {
        Iterator iter = getFacade().getTaggedValues(elem);
        while (iter.hasNext()) {
            Object tv = iter.next();
            String tag = getFacade().getTagOfTag(tv);
            String val = getFacade().getValueOfTag(tv);

            /*
             * <InlineType> : defInClass|
             *                inlineKeyDefInClass|
             *                inlineKeyDefOutClass|
             *                defOutClass
             */
            if (tag != null && tag.equals(TV_NAME_INLINE)) {
                // Inlining disabled
                if (val.equals(TV_FALSE_VALUE)) {
                    return -1;
                }

                // Default 'inline' value
                if (val.equals(TV_TRUE_VALUE) || val.equals("")
                        || val == null) {
                    return defaultInline;
                }

                if (val.equals(TV_INLINE_STYLE_DEFINITION_INSIDE_CLASS)) {
                    return INLINE_DEF_INSIDE_CLASS;
                }

                if (val.equals(TV_INLINE_STYLE_KEYWORD_DEFINITION_INSIDE_CLASS)) {
                    return INLINE_KEY_AND_DEF_INSIDE_CLASS;
                }

                if (val.equals(TV_INLINE_STYLE_KEYWORD_DEFINITION_OUTSIDE_CLASS)) {
                    return INLINE_KEY_AND_DEF_OUTSIDE_CLASS;
                }

                if (val.equals(TV_INLINE_STYLE_DEFINITION_OUTSIDE_CLASS)) {
                    return INLINE_DEF_OUTSIDE_CLASS;
                }
            }
        }

        return -1; // no tag found
    }
}
