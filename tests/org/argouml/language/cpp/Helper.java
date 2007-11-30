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

package org.argouml.language.cpp;

import junit.framework.TestCase;

import org.argouml.kernel.Project;
import org.argouml.kernel.ProjectManager;
import org.argouml.model.Model;
import org.argouml.model.ModelImplementation;
import org.argouml.profile.InitProfileSubsystem;

/**
 * An Helper for test classes.
 *
 * @author Luis Sergio Oliveira (euluis)
 */
public class Helper {

    public static Object getModel() {
        return ProjectManager.getManager().getCurrentProject().getModel();
    }

    public static void newModel() {
        createProject();
    }

    public static Project createProject() {
        ensureModelSubsystemInitialized();
        new InitProfileSubsystem().init();
        return ProjectManager.getManager().makeEmptyProject();
    }
    
    static void ensureModelSubsystemInitialized() {
        if (!Model.isInitiated())
            initializeMDR();
    }

    /**
     * Initialize the Model subsystem with the MDR ModelImplementation.
     */
    static void initializeMDR() {
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

}
