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

import junit.framework.TestCase;

import org.argouml.language.cpp.Helper;
import org.argouml.moduleloader.ModuleInterface;
import org.argouml.profile.Profile;
import org.argouml.profile.ProfileFacade;

/**
 * 
 * @author Luis Sergio Oliveira (euluis)
 */
public class TestProfileModule extends TestCase {
    
    private ModuleInterface module;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Helper.newModel();
        module = new ProfileModule();
    }
    
    public void testCtor() {
        assertNotNull(module.toString());
    }
    
    public void testEnable() {
        assertNormalProfileCppNotRegistered();
        assertTrue(module.enable());
        assertNormalProfileCppRegistered();
    }
    
    public void testProfileRegistrationThrowsWithinEnable() {
        module = new ProfileModule() {
            @Override
            void register(Profile profile) {
                throw new RuntimeException();
            }
        };
        assertFalse(module.enable());
    }
    
    public void testDisable() {
        assertTrue(module.enable());
        assertNormalProfileCppRegistered();
        assertTrue(module.disable());
        assertNormalProfileCppNotRegistered();
    }
    
    public void testDisableNotEnabled() {
        assertTrue(module.disable());
    }
    
    public void testProfileRemovalThrowsWithinDisable() {
        module = new ProfileModule() {
            @Override
            void remove(Profile profile) {
                throw new RuntimeException();
            }
        };
        module.enable();
        assertFalse(module.disable());
    }
    
    public void testGetInfo() {
        int[] infoTypes = {ModuleInterface.AUTHOR, ModuleInterface.DESCRIPTION, 
            ModuleInterface.DOWNLOADSITE, ModuleInterface.VERSION};
        int invalidInfoType = 0;
        for (int infoType : infoTypes) {
            String info = module.getInfo(infoType);
            assertNotNull("Failed to retrieve info " + infoType + ".", 
                    info);
            assertTrue(info.length() > 0);
            invalidInfoType += Math.abs(infoType);
        }
        assertNull(module.getInfo(invalidInfoType));
    }
    
    public void testGetName() {
        String name = module.getName();
        assertNotNull(name);
        assertTrue(name.length() > 0);
    }

    private void assertNormalProfileCppRegistered() {
        assertNotNull(ProfileFacade.getManager().getProfileForClass(
                NormalProfileCpp.class.getName()));
    }

    private void assertNormalProfileCppNotRegistered() {
        assertNull(ProfileFacade.getManager().getProfileForClass(
                NormalProfileCpp.class.getName()));
    }
}
