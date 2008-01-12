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

package org.argouml.language.cpp.profile;

import static org.argouml.language.cpp.Helper.assertNotEmpty;
import static org.argouml.language.cpp.Helper.newModel;

import java.util.Collection;
import java.util.Set;

import junit.framework.TestCase;

import org.argouml.profile.FigNodeStrategy;
import org.argouml.profile.Profile;
import org.argouml.profile.ProfileException;

/**
 * Tests for the {@link NormalProfileCpp} class.
 *
 * @author Luis Sergio Oliveira (euluis)
 */
public class TestNormalProfileCpp extends TestCase {

    private Profile profileCpp;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        newModel();
        profileCpp = new NormalProfileCpp();
    }

    /**
     * Test method for {@link NormalProfileCpp#getDisplayName()}.
     */
    public void testGetDisplayName() {
        assertNotEmpty(profileCpp.getDisplayName());
    }

    /**
     * Test method for {@link NormalProfileCpp#getProfilePackages()}.
     * @throws ProfileException when something goes wrong...
     */
    public void testGetProfilePackages() throws ProfileException {
        Collection packages = profileCpp.getProfilePackages();
        assertNotNull(packages);
        assertTrue(packages.size() > 0);
    }

    /**
     * Test method for {@link org.argouml.profile.Profile#getDependencies()}.
     */
    public void testGetDependencies() {
        Set<Profile> dependencies = profileCpp.getDependencies();
        assertEquals(1, dependencies.size());
        assertTrue(dependencies.iterator().next().getDisplayName().
                matches("UML.*"));
    }

    /**
     * Test method for {@link Profile#getFormatingStrategy()}.
     */
    public void testGetFormatingStrategy() {
        assertNotNull(profileCpp.getFormatingStrategy());
    }

    /**
     * Test method for {@link Profile#getFigureStrategy()}.
     * @throws ProfileException if retrieving the profile packages goes wrong 
     */
    public void testGetFigureStrategy() throws ProfileException {
        FigNodeStrategy figStrategy = profileCpp.getFigureStrategy();
        assertNotNull(figStrategy);
    }

    /**
     * Test method for {@link Profile#getDefaultTypeStrategy()}.
     */
    public void testGetDefaultTypeStrategy() {
        assertNotNull(profileCpp.getDefaultTypeStrategy());
    }

    /**
     * Test method for {@link org.argouml.profile.Profile#toString()}.
     */
    public void testToString() {
        assertNotEmpty(profileCpp.toString());
    }

}
