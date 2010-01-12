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

// Copyright (c) 2007-2009 The Regents of the University of California. All
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

package org.argouml.language.cpp;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.argouml.kernel.Project;
import org.argouml.kernel.ProjectManager;
import org.argouml.model.Model;
import org.argouml.model.ModelImplementation;
import org.argouml.profile.init.InitProfileSubsystem;

/**
 * An Helper for test classes.
 * 
 * TODO: this helper should be split into several classes, each dealing with a
 * specific functionality - model, project, JUnit addition, directories and
 * files, etc.
 *
 * @author Luis Sergio Oliveira (euluis)
 */
public class Helper {

    public static Object getModel() {
        return ProjectManager.getManager().getCurrentProject()
                .getUserDefinedModelList().get(0);
    }

    public static Collection<Object> getModels() {
        return ProjectManager.getManager().getCurrentProject()
                .getUserDefinedModelList();
    }

    public static void newModel() {
        createProject();
    }

    public static Project createProject() {
        ensureModelSubsystemInitialized();
        new InitProfileSubsystem().init();
        Project p = ProjectManager.getManager().makeEmptyProject();
        ProjectManager.getManager().setCurrentProject(p);
        return p;
    }
    
    public static void deleteCurrentProject() {
        if (!Model.isInitiated()) {
            return;
        }
        Project p = ProjectManager.getManager().getCurrentProject();
        ProjectManager.getManager().removeProject(p);
    }
    
    static void ensureModelSubsystemInitialized() {
        if (!Model.isInitiated()) {
            initializeMDR();
        }
    }

    /**
     * Initialize the Model subsystem with the MDR ModelImplementation.
     */
    public static void initializeMDR() {
        // TODO: Modules shouldn't have a dependency on internal implementation
        // artifacts of ArgoUML (and shouldn't use reflection to hide the fact
        // that they have that dependency).
        initializeModelImplementation(
                "org.argouml.model.mdr.MDRModelImplementation");
    }

    private static ModelImplementation initializeModelImplementation(
            String name) {
        ModelImplementation impl = null;

        Class implType;
        try {
            implType =
                Class.forName(name);
        } catch (ClassNotFoundException e) {
            TestCase.fail(e.toString());
            return null;
        }

        try {
            impl = (ModelImplementation) implType.newInstance();
        } catch (InstantiationException e) {
            TestCase.fail(e.toString());
        } catch (IllegalAccessException e) {
            TestCase.fail(e.toString());
        }
        Model.setImplementation(impl);
        return impl;
    }

    public static void assertNotEmpty(String string) {
        TestCase.assertNotNull(
            "Ha! The freaking string is null and you're asking about its " 
            + "emptyness!", string);
        TestCase.assertTrue("The string size must be bigger than 0.", 
                string.length() > 0);
    }

    /**
     * System temporary directory property name.
     */
    public static final String SYSPROPNAME_TMPDIR = "java.io.tmpdir";


    public static File getTmpDir() {
        return new File(System.getProperty(Helper.SYSPROPNAME_TMPDIR));
    }

    /**
     * Setup a directory with the given name for the caller test.
     * 
     * @param dirName
     *            the directory to be created in the system temporary dir
     * @return the created directory
     */
    public static File setUpDir4Test(String dirName) {
        File generationDir = new File(getTmpDir(), dirName);
        generationDir.mkdirs();
        return generationDir;
    }
    
    public static void deleteDir(File dir) throws IOException {
        if (dir != null && dir.exists()) {
            FileUtils.deleteDirectory(dir);
        }
    }
}
