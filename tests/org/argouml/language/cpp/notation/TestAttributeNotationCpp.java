// $Id$
// Copyright (c) 2006-2007 The Regents of the University of California. All
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

import junit.framework.TestCase;

import org.argouml.language.cpp.Helper;
import org.argouml.model.Model;

/**
 * Tests for the Attribute class.
 * 
 * @author Luis Sergio Oliveira (euluis)
 */
public class TestAttributeNotationCpp extends TestCase {
    
    @Override
    protected void setUp() throws Exception {
        Helper.newModel();
    }

    public void testToStringSimpleNoArgs() {
        Object theClass = Model.getCoreFactory().buildClass("TheClass",
                Helper.getModel());
        Object attr = Model.getCoreFactory().buildAttribute2(theClass,
                theClass);
        Model.getCoreHelper().setName(attr, "attrName");
        AttributeNotationCpp notation = new AttributeNotationCpp();
        String attrNotation = notation.toString(attr, null);
        assertTrue(attrNotation.matches(Model.getFacade().getName(theClass)
                + " " + Model.getFacade().getName(attr) + ";"));
    }

}
