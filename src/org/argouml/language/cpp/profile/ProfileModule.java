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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.argouml.i18n.Translator;
import org.argouml.moduleloader.ModuleInterface;
import org.argouml.profile.Profile;
import org.argouml.profile.ProfileFacade;

/**
 * This is the UML profile for C++ module. This being a module, provides the 
 * ArgoUML C++ module to offer the C++ profile to the application by 
 * registering it on enable and unregistering on disable.
 *
 * @author Luis Sergio Oliveira (euluis)
 */
public class ProfileModule implements ModuleInterface {
    
    private static final String REVISION_DATE = 
        "$Date$"; //$NON-NLS-1$

    private static final Logger LOG = Logger.getLogger(ProfileModule.class);

    private Profile profileCpp;

    private Map<Integer, String> moduleInfo;

    private String moduleName;

    /**
     * @return <code>false</code> either if constructing the 
     *         {@link NormalProfileCpp} or registering it fails.
     * @see org.argouml.moduleloader.ModuleInterface#enable()
     */
    public boolean enable() {
        try {
            profileCpp = new NormalProfileCpp();
            register(profileCpp);
        } catch (Exception e) {
            LOG.error("Failed to enable myself!", e);
            return false;
        }
        return true;
    }

    /**
     * @return <code>false</code> if unregistering the profileCpp fails.
     * @see org.argouml.moduleloader.ModuleInterface#disable()
     */
    public boolean disable() {
        boolean removed = profileCpp == null;
        if (!removed) {
            try {
                remove(profileCpp);
                removed = true;
            } catch (Exception e) {
                LOG.error("Failed to remove the C++ profile.", e);
            }
            profileCpp = null;
        }
        return removed;
    }
    
    /**
     * TODO: consider revision of the other C++ modules to use a similar 
     * mechanism. Preferably this should be abstracted into a base module 
     * implementation class.
     * 
     * @param type the type of information to retrieve.
     * @return the information of the type requested or null if type is 
     *         invalid.
     * @see org.argouml.moduleloader.ModuleInterface#getInfo(int)
     */
    @SuppressWarnings("serial")
    public String getInfo(int type) {
        if (moduleInfo == null) {
            moduleInfo = new HashMap<Integer, String>() {
                {
                    put(ModuleInterface.AUTHOR, 
                            "Luís Sérgio Oliveira (euluis)"); //$NON-NLS-1$
                    put(ModuleInterface.DESCRIPTION, 
                        Translator.localize(
                            "cpp.profile.module.description")); //$NON-NLS-1$
                    // TODO: remove duplication here and in 
                    // SettingsTabCpp.getInfo.
                    put(ModuleInterface.DOWNLOADSITE, 
                        "http://argouml-downloads.tigris.org/"); //$NON-NLS-1$
                    put(ModuleInterface.VERSION, "Revision date: " 
                        + REVISION_DATE);
                }
            };
        }
        return moduleInfo.get(type);
    }

    public String getName() {
        if (moduleName == null)
            moduleName = Translator.localize(
                "cpp.profile.module.name"); //$NON-NLS-1$
        return moduleName;
    }

    void register(Profile profile) {
        ProfileFacade.register(profile);
    }

    void remove(Profile profile) {
        ProfileFacade.remove(profile);
    }

}
