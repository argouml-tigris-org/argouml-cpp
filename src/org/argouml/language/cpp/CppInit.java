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

import org.argouml.language.cpp.generator.ModuleCpp;
import org.argouml.language.cpp.notation.NotationModuleCpp;
import org.argouml.language.cpp.reveng.CppImport;
import org.argouml.language.cpp.ui.SettingsTabCpp;
import org.argouml.moduleloader.ModuleInterface;

/**
 * Initiates all the Cpp modules.<p>
 *
 * This is a class that initiates all the parts of the Cpp modules. If you
 * only want to enable some of the modules, you need to handle them
 * individually.
 */
public class CppInit implements ModuleInterface {
    
    /**
     * The list of modules:
     */
    private ModuleInterface[] modules = {
	new ModuleCpp(),
	new NotationModuleCpp(),
	new CppImport(),
	new SettingsTabCpp(),
    };

    public boolean enable() {
	boolean result = true;
	for (int i = 0; i < modules.length; i++) {
	    if (!modules[i].enable()) {
		result = false;
	    }
	}
	return result;
    }

    public boolean disable() {
	boolean result = true;
	for (int i = 0; i < modules.length; i++) {
	    if (!modules[i].disable()) {
		result = false;
	    }
	}
	return result;
    }

    public String getInfo(int type) {
	StringBuffer result = new StringBuffer();
	for (int i = 0; i < modules.length; i++) {
	    String res = modules[i].getInfo(type);
	    if (res != null) {
		result.append(res + "\n");
	    }
	}
	if (result.length() > 0) {
	    return result.toString();
	}
	return null;
    }

    public String getName() {
	return "Cpp";
    }

}
