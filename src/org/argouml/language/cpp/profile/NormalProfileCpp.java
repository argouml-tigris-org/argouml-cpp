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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.argouml.profile.DefaultTypeStrategy;
import org.argouml.profile.FigNodeStrategy;
import org.argouml.profile.FormatingStrategy;
import org.argouml.profile.Profile;
import org.argouml.profile.ProfileFacade;

/**
 * This is the usual (that's where the Normal prefix comes from ;-) profile 
 * implementation, contrasting with 
 * {@link ProfileCpp}, which is useful for providing extra functionalities 
 * for reverse engineering. 
 *
 * @author Luis Sergio Oliveira (euluis)
 */
public class NormalProfileCpp extends Profile {

    private Collection profileModels;
    
    public NormalProfileCpp() {
        Profile umlProfile = ProfileFacade.getManager().getUMLProfile();
        assert umlProfile != null 
            : "I'm dependent of the UML profile!"; //$NON-NLS-1$
        addProfileDependency(umlProfile);
        profileModels = BaseProfile.loadProfileModels();
    }

    /**
     * @return GUI identifier of the profile
     * @see org.argouml.profile.Profile#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return "C++"; //$NON-NLS-1$
    }

    /**
     * @return a collection which contains the profile model
     * @see org.argouml.profile.Profile#getProfilePackages()
     */
    @Override
    public Collection getProfilePackages() {
        return Collections.unmodifiableCollection(profileModels);
    }
    
    @Override
    public FormatingStrategy getFormatingStrategy() {
        // FIXME: lots and lots of work, see JavaFormatingStrategy...
        return new FormatingStrategy() {

            public String formatCollection(Iterator iter, Object namespace) {
                // TODO: Auto-generated method stub
                return null;
            }

            public String formatElement(Object element, Object namespace) {
                // TODO: Auto-generated method stub
                return null;
            }
            
        };
    }
    
    @Override
    public DefaultTypeStrategy getDefaultTypeStrategy() {
        return new DefaultTypeStrategyCpp(profileModels);
    }
    
    @Override
    public FigNodeStrategy getFigureStrategy() {
        return new FigNodeStrategyCpp();
    }

}
