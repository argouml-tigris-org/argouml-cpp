// $Id: NotationModuleCpp.java 77 2006-10-26 22:37:44Z euluis $
// Copyright (c) 2006-2007 The Regents of the University of California. All
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

package org.argouml.language.cpp.notation;

import java.net.URL;

import javax.swing.ImageIcon;

import org.argouml.moduleloader.ModuleInterface;
import org.argouml.notation.Notation;
import org.argouml.notation.NotationName;
import org.argouml.notation.NotationProviderFactory2;

/**
 * 
 * @author Luis Sergio Oliveira (euluis)
 */
public class NotationModuleCpp implements ModuleInterface {

    private NotationName name;

    public NotationModuleCpp() {
        name = Notation.makeNotation("C++", null, loadIcon());
    }

    ImageIcon loadIcon() {
        URL iconUrl = NotationModuleCpp.class.getClassLoader().getResource(
            "org/argouml/Images/CppNotation.gif");
        assert iconUrl != null;
        ImageIcon icon = new ImageIcon(iconUrl);
        assert icon != null;
        return icon;
    }

    public boolean disable() {
        return getNotationProviderFactory().removeNotation(name);
    }

    public boolean enable() {
        NotationProviderFactory2 npf = getNotationProviderFactory();

        npf.addNotationProvider(NotationProviderFactory2.TYPE_NAME, name,
                ModelElementNameNotationCpp.class);
        npf.addNotationProvider(NotationProviderFactory2.TYPE_ATTRIBUTE, name,
                AttributeNotationCpp.class);
        npf.addNotationProvider(NotationProviderFactory2.TYPE_OPERATION, name,
                OperationNotationCpp.class);
        return true;
    }

    private NotationProviderFactory2 getNotationProviderFactory() {
        NotationProviderFactory2 npf = NotationProviderFactory2.getInstance();
        return npf;
    }

    public String getInfo(int type) {
        switch (type) {
        case DESCRIPTION:
            return "ArgoUML Notation Module for C++";
        case AUTHOR:
            return "Luis Sergio Oliveira";
        case VERSION:
            return "0.01.01";
        default:
            return null;
        }
    }

    public String getName() {
        return "NotationModuleCpp";
    }

}
