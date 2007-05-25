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

package org.argouml.language.cpp.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

import org.argouml.kernel.ProjectManager;
import org.argouml.language.cpp.profile.ProfileCpp;
import org.argouml.model.Model;

/**
 * Copies the stereotypes from the UML Profile for C++ into the model.
 * 
 * @author Luis Sergio Oliveira (euluis)
 * @since 0.25.3
 * @see issue 3771 (http://argouml.tigris.org/issues/show_bug.cgi?id=3771)
 */
public class CopyCppProfileToModelAction implements Action {

    public void addPropertyChangeListener(PropertyChangeListener arg0) {
    }

    public Object getValue(String arg0) {
        return null;
    }

    public boolean isEnabled() {
        return getModel() != null;
    }

    private Object getModel() {
        return Model.getModelManagementFactory().getRootModel();
    }

    public void putValue(String arg0, Object arg1) {
    }

    public void removePropertyChangeListener(PropertyChangeListener arg0) {
    }

    public void setEnabled(boolean arg0) {
    }

    public void actionPerformed(ActionEvent arg0) {
        ProfileCpp profileCpp = new ProfileCpp(getModel());
        profileCpp.copyAllCppStereotypesToModel();
        profileCpp.copyAllDataTypesToModel();
        ProjectManager.getManager().setSaveEnabled(true);
    }

}
