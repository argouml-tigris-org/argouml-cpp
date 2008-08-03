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

import java.io.File;
import java.lang.reflect.Method;

import org.argouml.application.helpers.ApplicationVersion;
import org.argouml.kernel.Project;
import org.argouml.language.cpp.Helper;
import org.argouml.model.Model;
import org.argouml.persistence.AbstractFilePersister;
import org.argouml.persistence.PersistenceManager;
import org.argouml.profile.Profile;
import org.argouml.profile.ProfileFacade;

import junit.framework.TestCase;

/**
 * This is a functional test that verifies that a ArgoUML project that is saved
 * with a model that depends of the UML profile for C++ loads correctly
 * afterwards.
 * 
 * See <a href="http://argouml.tigris.org/issues/show_bug.cgi?id=4946">Issue
 * #4946</a>.
 * 
 * @author Luis Sergio Oliveira (euluis)
 */
public class TestPersistencyWithNormalProfileCpp extends TestCase {

    private Project project;
    private File dir4Test;
    
    /*
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // this must be done so that we don't get trash in the persisted XMI 
        // file
        Helper.initializeMDR();
        project = Helper.createProject();
        
        // FIXME: duplicated code from TestProjectWithProfiles.
        if (ApplicationVersion.getVersion() == null) {
            Class<?> argoVersionClass = 
                Class.forName("org.argouml.application.ArgoVersion");
            Method initMethod = argoVersionClass.getDeclaredMethod("init");
            initMethod.setAccessible(true);
            initMethod.invoke(null);
            assertNotNull(ApplicationVersion.getVersion());
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        // FIXME: fails to delete the directory!
        //Helper.deleteDir(dir4Test);
        super.tearDown();
    }
    
    public void testSaveAndOpenProjectWithUMLProfileForCpp() throws Exception {
        Profile profileCpp = new NormalProfileCpp();
        ProfileFacade.register(profileCpp);
        project.getProfileConfiguration().addProfile(profileCpp);
        Object model = Model.getModelManagementFactory().getRootModel();
        String fooName = "SaveAndOpenProjectWithUMLProfileForCpp";
        Object foo = Model.getCoreFactory().buildClass(fooName, model);
        ProfileCpp profileCpp2 = new ProfileCpp(project
                .getUserDefinedModelList());
        profileCpp2.applyCppClassStereotype(foo);
        dir4Test = Helper.setUpDir4Test(getClass().getName());
        File file = new File(dir4Test, 
                "testSaveAndOpenProjectWithUMLProfileForCpp.zargo");
        AbstractFilePersister persister = 
            PersistenceManager.getInstance().getPersisterFromFileName(
                    file.getAbsolutePath());
        project.setVersion(ApplicationVersion.getVersion());
        persister.save(project, file);
        // load the project from the saved file
        project = persister.doLoad(file);
        assertNotNull(project);
        assertNotNull(project.findType(fooName, false));
    }
}
