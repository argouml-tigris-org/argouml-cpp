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

package org.argouml.language.cpp.profile;

import static org.argouml.language.cpp.Helper.newModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.argouml.model.Model;
import org.argouml.profile.FigNodeStrategy;
import org.argouml.profile.Profile;
import org.argouml.profile.ProfileException;

import junit.framework.TestCase;

/**
 *
 * @author Luis Sergio Oliveira (euluis)
 */
public class TestFigNodeStrategyCpp extends TestCase {

    private Profile profileCpp;
    private FigNodeStrategy figStrategy;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        newModel();
        profileCpp = new NormalProfileCpp();
        figStrategy = new FigNodeStrategyCpp();
    }

    public void testGetIconForStereotypeReturnsImagesForCertainStereotypes() 
        throws ProfileException {
        List<Object> stereotypesWithImages = new ArrayList<Object>();
        stereotypesWithImages.add(getStereotypeByName(
                ProfileCpp.STEREO_NAME_CLASS));
        stereotypesWithImages.add(getStereotypeByName(
                ProfileCpp.STEREO_NAME_ATTRIBUTE));
        stereotypesWithImages.add(getStereotypeByName(
                ProfileCpp.STEREO_NAME_OPERATION));
        stereotypesWithImages.add(getStereotypeByName(
                ProfileCpp.STEREO_NAME_PARAMETER));
        for (Object stereotype : stereotypesWithImages) {
            assertNotNull(stereotype);
            assertNotNull(figStrategy.getIconForStereotype(stereotype));
        }
    }

    public void testGetIconForStereotypeReturnsNullForStereotypesWithoutImages()
        throws ProfileException {
        List<Object> stereotypesWithoutImages = new ArrayList<Object>();
        stereotypesWithoutImages.add(getStereotypeByName(
                ProfileCpp.STEREO_NAME_GENERALIZATION));
        stereotypesWithoutImages.add(getStereotypeByName(
                ProfileCpp.STEREO_NAME_REALIZATION));
        for (Object stereotype : stereotypesWithoutImages) {
            assertNotNull(stereotype);
            assertNull(figStrategy.getIconForStereotype(stereotype));
        }
    }

    private Object getStereotypeByName(String stereotypeName) throws 
        ProfileException {
        Collection stereotypes = Model.getExtensionMechanismsHelper().
            getStereotypes(profileCpp.getProfilePackages());
        for (Object stereotype : stereotypes) {
            if (Model.getFacade().getName(stereotype).equals(stereotypeName))
                return stereotype;
        }
        return null;
    }

}
