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

// Copyright (c) 1996-2009 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
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

package org.argouml.language.cpp.generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.argouml.configuration.Configuration;
import org.argouml.configuration.ConfigurationKey;
import static org.argouml.language.cpp.profile.ProfileCpp.*;

import static org.argouml.model.Model.*;
import org.argouml.uml.DocumentationManager;
import org.argouml.uml.UUIDHelper;
import org.argouml.uml.generator.CodeGenerator;
import org.argouml.uml.generator.SourceUnit;

/**
 * Class to generate C++ source code.
 *
 * WARNING: Don't know if this is a threat (and I think it's not), but this
 * class is NOT THREAD SAFE. DO NOT CALL METHODS FROM DIFFERENT THREADS.
 * At the moment, it works, but this is just in case someone in the future
 * tries to generate 1265 files in parallel and guesses why it doesn't work :-)
 */
public class GeneratorCpp implements CodeGenerator {

    /**
     * The logger.
     */
    private static final Logger LOG = Logger.getLogger(GeneratorCpp.class);
    
    static final String LANGUAGE_NAME = "cpp";

    // Customizable variables

    private boolean verboseDocs = false;
    private boolean lfBeforeCurly = false;
    private String indent = "    "; // customizable (non final) indent
    
    private boolean hdrGuardUpperCase = false;
    private boolean hdrGuardGUID = false;
    
    // Configuration keys for the above configurable variables
    private static final ConfigurationKey KEY_CPP_INDENT =
        Configuration.makeKey(LANGUAGE_NAME, "indent");
    private static final ConfigurationKey KEY_CPP_LF_BEFORE_CURLY =
        Configuration.makeKey(LANGUAGE_NAME, "lf-before-curly");
    private static final ConfigurationKey KEY_CPP_VERBOSE_COMM =
        Configuration.makeKey(LANGUAGE_NAME, "verbose-comments");
    private static final ConfigurationKey KEY_CPP_SECT =
        Configuration.makeKey(LANGUAGE_NAME, "sections");
    private static final ConfigurationKey KEY_CPP_HEADER_GUARD_UPPERCASE =
        Configuration.makeKey(LANGUAGE_NAME, "header-guard-case");
    private static final ConfigurationKey KEY_CPP_HEADER_GUARD_GUID =
        Configuration.makeKey(LANGUAGE_NAME, "header-guard-guid");
    private static final ConfigurationKey KEY_CPP_DEFAULT_INLINE =
        Configuration.makeKey(LANGUAGE_NAME, "default-inline");

    private static Section sect;

    /**
     * Store actual namespace, to avoid unneeded curly braces.
     *
     * @author Achim Spangler
     * @since 2002-12-07
     */
    private Object actualNamespace;

    /** Current classifier, for which the code is being generated.
     */
    private Object currClass = null;

    /**
     * Set of the local files to include with #include "file.h"
     * Automatically generated from classifier dependencies.
     */
    private Set localInc = new TreeSet();

    /**
     * Set of the external files to include with #include <file.h>
     * Automatically generated from classifier dependencies.
     */
    private Set extInc = new TreeSet();

    /**
     * Set of the system files to include, like #include <vector>
     * Automatically generated.
     */
    private Set systemInc = new TreeSet();
 
    /** Set of classifier that needs to be defined (i.e. #included)
     */
    private Set includeCls = new LinkedHashSet();

    /** Set of classifier that only needs to be predeclared
     */
    private Set predeclCls = new LinkedHashSet();

    /**
     * System newline separator.
     */
    private static final String LINE_SEPARATOR =
        System.getProperty("line.separator");

    /**
     * C++ doesn't place visibility information for each class member
     * --> sort items during generation and store visibility state
     * of lastly generated member in central class variable, so that
     * the appropriate lines: "public:", "protected:", "private:"
     * can be created.
     *
     * @author Achim Spangler
     * @since 2002-11-28
     */
    private static final int PUBLIC_PART = 0;
    private static final int PROTECTED_PART = 1;
    private static final int PRIVATE_PART = 2;

    private static final int[] ALL_PARTS = {
        PUBLIC_PART,
        PROTECTED_PART,
        PRIVATE_PART,
    };

    private static final String[] PART_NAME = {
        "public", "protected", "private"
    };

    /**
     * C++ uses two files for each class: header (.h) with class definition
     * and source (.cpp) with methods implementation
     * --> two generation passes are needed.
     *
     * @author Achim Spangler
     * @since 2002-11-28
     */
    private static final int NONE_PASS = 1;
    private static final int HEADER_PASS = 2;
    private static final int SOURCE_PASS = 3;
    private static int generatorPass = NONE_PASS;

    /**
     * use Tag generation for generation of: doccomment, simple tags of
     * tags which are not used for document or simple tags for all.
     *
     * @author Achim Spangler
     * @since 2002-12-05
     */
    private static final int DOC_COMMENT_TAGS = 1;
    private static final int ALL_BUT_DOC_TAGS = 2;
    private static final int ALL_TAGS = 3;

    /**
     * C++ developers need to specify for parameters whether they are
     * pointers or references (especially for class-types)
     * -> a general check function must get the searched tag.
     *
     * @author Achim Spangler
     * @since 2002-12-06
     */
    private static final int NORMAL_MOD = 0;
    private static final int REFERENCE_MOD = 1;
    private static final int POINTER_MOD = 2;
    private static final int CONST_MOD = 3;

    private static GeneratorCpp singleton;

    /**
     * Prefix for names in the std namespace. Defaults to "std::",
     * but could be "" if a "using namsepace std;" directive is used.  
     */
    private String stdPrefix = "std::";
    
    /**
     * Get the instance of the singleton for the C++ generator.
     *
     * @return the singleton of the generator.
     */
    public static synchronized GeneratorCpp getInstance() {
        if (singleton != null)
            return singleton;
        return new GeneratorCpp(); // the constructor will set singleton
    }

    /**
     * Constructor.
     */
    protected GeneratorCpp() {
        singleton = this;
        loadConfig();
    }

    /** Reset the generator in the initial state before
     * starting to generate code.
     */
    protected void cleanupGenerator() {
        // clears collections of dependencies
        localInc.clear();
        extInc.clear();
        systemInc.clear();
        includeCls.clear();
        predeclCls.clear();
        // set currClass to null, so if it's used when it shouldn't
        // it will raise a NullPointerException
        currClass = null;
        actualNamespace = null;
    }

    /** Set up the generator in order to generate the code
     * for 'cls'.
     * @param cls The classifier to generate the code for
     */
    protected void setupGenerator(Object cls) {
        cleanupGenerator();
        currClass = cls;
    }

    /** Internal helper that generates the file content (.cpp or .h)
     * and returns it as a String, without actually creating a file.
     */
    private String generateFileAsString(Object o, String pathname) {
        setupGenerator(o);
        if (generatorPass == SOURCE_PASS  && getFacade().isAInterface(o))
            return ""; // don't generate the .cpp, it's useless.

        String headerTop = generateHeaderTop(pathname);
        String header = generateHeader(o);
        // This can only be a classifier, right? - tfm
        String src = generateClassifier(o);
        
        String inlinedMethods = null;
        if (generatorPass == HEADER_PASS) {
            inlinedMethods = generateInlinedMethodsOutsideClass(o);
        }
        
        String footer = generateFooter();
        // generate #includes and predeclarations
        // this must be *after* generate()
        StringBuffer incl = new StringBuffer();
        if (generatorPass == SOURCE_PASS) {
            localInc.add(getFacade().getName(o) + ".h");
        }
        generateIncludes(incl);
        if (generatorPass == HEADER_PASS) {
            if (incl.length() > 0) incl.append(LINE_SEPARATOR);
            generatePredeclare(incl);
        }
        // paste all the pieces in the final result
        StringBuffer result = new StringBuffer();
        String guard = new String();
        if (generatorPass == HEADER_PASS) {
            String name = getFacade().getName(o);
            String guardPack =
                generateRelativePackage(o, null, "_").substring(1);
            guard = name + getFileExtension().replace('.', '_');
            if (guardPack.length() > 0) {
                guard = guardPack + "_" + guard;
            }
            if (hdrGuardGUID) {
                guard = guard + "_" 
                    + UUID.randomUUID().toString().replace("-", "_");
            }
            if (hdrGuardUpperCase) {
                guard = guard.toUpperCase();
            }
            
            result.append("#ifndef " + guard + LINE_SEPARATOR 
                      + "#define " + guard 
                      + LINE_SEPARATOR + LINE_SEPARATOR);
        }
        result.append(headerTop);
        result.append(incl.toString());
        result.append(header);
        result.append(src);
        
        if (generatorPass == HEADER_PASS && inlinedMethods != null) {
            result.append(inlinedMethods);
        }
        
        result.append(footer);
        if (generatorPass == HEADER_PASS) {
            result.append(LINE_SEPARATOR);
            result.append("#endif" + " // " + guard);
            result.append(LINE_SEPARATOR);
        }
        return result.toString();
    }

    /**
     * Generate the source code (.cpp) for the given object
     * @param o the object to be generated
     * @return the generated code as a string
     */
    String generateCpp(Object o) {
        generatorPass = SOURCE_PASS;
        String name =
            generateRelativePackage(o, null, "/").substring(1);
        if (name.length() > 0) name += "/";
        name += getFacade().getName(o) + ".cpp";
        String ret = generateFileAsString(o, name);
        cleanupGenerator();
        generatorPass = NONE_PASS;
        return ret;
    }

    /**
     * Generate the header code (.h) for the given object
     * @param o the object to be generated
     * @return the generated header as a string
     */
    String generateH(Object o) {
        generatorPass = HEADER_PASS;
        String name =
            generateRelativePackage(o, null, "/").substring(1);
        if (name.length() > 0) name += "/";
        name += getFacade().getName(o) + ".h";
        String ret = generateFileAsString(o, name);
        cleanupGenerator();
        generatorPass = NONE_PASS;
        return ret;
    }

    /**
     * Generate the package name for the specified object,
     * relative to the specified package. Use sep as the
     * package separator. 
     * @param cls Object to generate the path for
     * @param pack Generate path relative to this package
     * @param sep package separator
     * @return path relative to pack, if pack is a parent of
     *         cls, else relative to the project root. If the
     *         path is relative to the project root, it's prefixed
     *         with sep.
     */
    private String generateRelativePackage(Object cls, Object pack,
            String sep) {
        StringBuffer packagePath = new StringBuffer();
        // avoid model being used as a package name
        Object parent = getFacade().getNamespace(cls);

        while (parent != null && parent != pack) {
            // ommit root package name; it's the model's root
            Object grandParent = getFacade().getNamespace(parent); 
            if (grandParent != null) {
                String name = getFacade().getName(parent);
                if (packagePath.length() > 0) {
                    packagePath.insert(0, sep);
                }
                packagePath.insert(0, name);
            }
            parent = grandParent;
        }
        if (parent == null) { // relative to root, prefix with sep
            packagePath.insert(0, sep);
        }
        return packagePath.toString();
    }

    /** 2002-11-28 Achim Spangler
     * @return file extension for actual generation pass
     */
    private String getFileExtension() {
        if (generatorPass == HEADER_PASS) return ".h";
        return ".cpp";
    }

    /**
     * Generates the relative path for the specified classifier.
     * @param cls The classifier. 
     * @return Returns relative path of cls (without filename).
     */
    private String generatePath(Object cls) {
        String packagePath =
            generateRelativePackage(cls, null, CodeGenerator.FILE_SEPARATOR);
        packagePath = packagePath.substring(1);
        return packagePath;
    }

    /**
     * create the needed directories for the derived appropriate pathname
     * @return Returns the filename with full path of cls.
     */
    private String createDirectoriesPathname(Object cls, String path) {
        String name = getFacade().getName(cls);
        if (name == null || name.length() == 0) {
            return "";
        }

        if (!path.endsWith (CodeGenerator.FILE_SEPARATOR)) {
            path += CodeGenerator.FILE_SEPARATOR;
        }

        String packagePath = generateRelativePackage(cls, null, ".");
        packagePath = packagePath.substring(1);
        String filename = name + getFileExtension();

        int lastIndex = -1;
        do {
            File f = new File (path);
            if (!f.isDirectory()) {
                if (!f.mkdir()) {
                    LOG.error(" could not make directory " + path);
                    return null;
                }
            }

            if (lastIndex == packagePath.length()) {
                break;
            }

            int index = packagePath.indexOf (".", lastIndex + 1);
            if (index == -1) {
                index = packagePath.length();
            }

            path += packagePath.substring(lastIndex + 1, index)
                + CodeGenerator.FILE_SEPARATOR;
            lastIndex = index;
        } while (true);

        String pathname = path + filename;
        //LOG.info("-----" + pathname + "-----");
        return pathname;
    }

    /* Returns true if the given object is a class (or interface) within
     * another class (not within a package).
     */
    private static boolean isAInnerClass(Object cls) {
        Object parent = getFacade().getNamespace(cls);
        return parent != null && !getFacade().isAPackage(parent);
    }

    /** 2002-12-07 Achim Spangler
     * @return date
     */
    private String getDate() {
        GregorianCalendar cal = new GregorianCalendar();
        DateFormat df;
        df = DateFormat.getDateInstance(DateFormat.DEFAULT);
        return df.format(cal.getTime());
    }

    /** 2002-12-07 Achim Spangler
     * @return year
     */
    private String getYear() {
        GregorianCalendar cal = new GregorianCalendar();
        return Integer.toString(cal.get(Calendar.YEAR));
    }

    /** 2002-12-07 Achim Spangler
     * write template content on top of file
     */
    private void writeTemplate(Object cls, String path, BufferedWriter fos) {
        String templatePathName = path + "/templates/";
        String fileName = getFacade().getName(cls);
        String tagTemplatePathName = getFacade().getTaggedValueValue(
                cls, TV_NAME_TEMPLATE_PATH);
        String authorTag = getFacade().getTaggedValueValue(cls, 
                TV_NAME_AUTHOR);
        String emailTag = getFacade().getTaggedValueValue(cls, 
                TV_NAME_EMAIL);
        if (tagTemplatePathName != null && tagTemplatePathName.length() > 0)
            templatePathName = tagTemplatePathName;
        if (generatorPass == HEADER_PASS) {
            templatePathName = templatePathName + "header_template";
            fileName = fileName + ".h";
        }
        else {
            templatePathName = templatePathName + "cpp_template";
            fileName = fileName + ".cpp";
        }
        
        File templateFile = new File(templatePathName);
        if (templateFile.exists()) {
            boolean eof = false;
            BufferedReader templateFileReader = null;
            try {
                // TODO: This is using the default platform character encoding
                // specifying an encoding will produce more predictable results
                templateFileReader =
                    new BufferedReader(new FileReader(
                        templateFile.getAbsolutePath()));
                while (!eof) {
                    String lineStr = templateFileReader.readLine();
                    if (lineStr == null) {
                        eof = true;
                    } else {
                        StringBuffer line = new StringBuffer(lineStr);
                        replaceToken(line, "|FILENAME|", fileName);
                        replaceToken(line, "|DATE|", getDate());
                        replaceToken(line, "|YEAR|", getYear());
                        replaceToken(line, "|AUTHOR|", authorTag);
                        replaceToken(line, "|EMAIL|", emailTag);

                        fos.write(line + LINE_SEPARATOR);
                    }
                }
                templateFileReader.close();
            }
            catch (IOException exp) { }
            finally {
                try {
                    if (templateFileReader != null) templateFileReader.close();
                }
                catch (IOException exp) {
                    LOG.error("FAILED: " + templateFile.getPath());
                }
            }
        }
    }

    /**
     * Replace the first occurrences of tokenName with tokenValue.
     *
     * @param line is the line where we do the replacing.
     * @param tokenName is the string we search for.
     * @param tokenValue is the value we replace.
     */
    private void replaceToken(StringBuffer line, String tokenName,
            String tokenValue) {
        int tokenStart;
        tokenStart = line.toString().indexOf(tokenName);
        if ((tokenStart != -1)
            && (tokenValue != null && tokenValue.length() > 0)) {
            line.replace(tokenStart,
                 tokenStart + tokenName.length(),
                 tokenValue);
        }
    }

    /** 2002-11-28 Achim Spangler
     * separate constant Header Top into function
     */
    private String generateHeaderTop(String pathname) {
        StringBuffer sb = new StringBuffer(80);
        //TODO: add user-defined copyright
        if (verboseDocs) {
            // FIXME: replace hard-coded path separator with 
            // CodeGenerator.FILE_SEPARATOR
            sb.append("// FILE: ").append(pathname.replace('\\', '/'));
            sb.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    /** 
     * Helper for checkIncludeNeeded4Element. Returns true if
     * an #include is needed.
     */
    private boolean checkInclude4UsageIndirection(boolean isIndirect,
            String usageTag) {
        if (isIndirect) {
            // needs only to be included in the .cpp
            if (usageTag.indexOf("header") != -1) {
                // but user explicitly requested its presence in the header
                return generatorPass == HEADER_PASS;
            } else {
                return generatorPass == SOURCE_PASS;
            }
        }

        // must be included in the header, whatever usageTag is
        return generatorPass == HEADER_PASS;
    }

    private boolean checkIncludeNeeded4Element(Object cls) {
        String usageTag = "";
        boolean predeclareCandidate = false;

        Iterator iter = getFacade().getTaggedValues(cls);
        while (iter.hasNext()) {
            Object tv = iter.next();
            String tag = getFacade().getTagOfTag(tv);
            if (tag != null) {
                if (tag.equals(TV_NAME_USAGE)) {
                    usageTag = getFacade().getValueOfTag(tv);
                }

                if (tag.indexOf(TV_NAME_REFERENCE) != -1 || tag.equals("&")
                        || tag.indexOf(TV_NAME_POINTER) != -1 
                        || tag.equals("*")) {
                    predeclareCandidate = true;
                }
            }
        }
        return checkInclude4UsageIndirection(predeclareCandidate, usageTag);
    }

    private StringBuffer generateIncludes(StringBuffer sb) {
        for (Iterator it = systemInc.iterator(); it.hasNext(); ) {
            String inc = (String) it.next();
            sb.append("#include <");
            sb.append(inc).append('>').append(LINE_SEPARATOR);
        }
        // separate system from external headers
        if (systemInc.size() > 0) sb.append(LINE_SEPARATOR);
        for (Iterator it = extInc.iterator(); it.hasNext(); ) {
            String inc = (String) it.next();
            sb.append("#include <");
            sb.append(inc).append('>').append(LINE_SEPARATOR);
        }
        // separate external from local headers
        if (extInc.size() > 0) sb.append(LINE_SEPARATOR);
        for (Iterator it = localInc.iterator(); it.hasNext(); ) {
            String inc = (String) it.next();
            sb.append("#include \"").append(inc).append("\"" + LINE_SEPARATOR);
        }
        return sb;
    }

    private StringBuffer generatePredeclare(StringBuffer sb) {
        for (Iterator it = predeclCls.iterator(); it.hasNext(); ) {
            Object cls = it.next();
            String name = getFacade().getName(cls);
            sb.append(generateHeaderPackageStart(cls));
            sb.append("class ").append(name);
            sb.append(";").append(LINE_SEPARATOR);
        }
        sb.append(generateHeaderPackageEnd());
        return sb;
    }

    /**
     * Parses header_incl or source_incl tags and adds the
     * user-specified headers to localInc or systemInc.
     * @param cls The classifier which code is being generated for.
     * @param source if true parses source_incl tags, else header_incl.
     */
    private void addUserHeaders(Object cls, boolean source) {
        Iterator iter = getFacade().getTaggedValues(cls);
        String tagPrefix;
        if (source)
            tagPrefix = TV_NAME_SOURCE_INCL;
        else
            tagPrefix = TV_NAME_HEADER_INCL;
            
        while (iter.hasNext()) {
            Object tv = iter.next();
            String tag = getFacade().getTagOfTag(tv);
            if (tag != null && tag.equals(tagPrefix)) {
                String name = getFacade().getValueOfTag(tv);
                if (name.length() > 2 && name.charAt(0) == '<') {
                    systemInc.add(name.substring(1, name.length() - 1));
                } else if (name.length() > 2 && name.charAt(0) == '"') {
                    localInc.add(name.substring(1, name.length() - 1));
                } else if (name.length() > 0) { // skip empty values
                    localInc.add(name);
                }
            }
        }
    }

    /**
     * Adds dep to the set of dependencies of currClass. If predecl
     * then only a predeclaration is generated, else an #include.
     * @param dep The classifier whose currClass depends on.
     * @param predecl If true then only a predeclaration is needed
     */
    private void addDependency(Object dep, boolean predecl) {
        if (generatorPass == NONE_PASS) {
            return; // skip dependencies if generating notation
        }
        if (!(getFacade().isAClass(dep)) && !(getFacade().isAInterface(dep))) {
            // Do nothing for things such as datatypes, etc.
            // TODO: check for namespace when using directives are implemented
            return;
        }
        if (predecl && !includeCls.contains(dep)) {
            if (generatorPass == HEADER_PASS) {
                predeclCls.add(dep);
            }
        } else {
            if (predeclCls.contains(dep)) {
                predeclCls.remove(dep);
            }
            if (includeCls.add(dep)) {
                // dep was not already in includeCls
                Object ns = getFacade().getNamespace(currClass);
                String name = getFacade().getName(dep);
                // use '/', not FILE_SEPARATOR (this is intentional)
                String path =
                    generateRelativePackage(dep, ns, "/");
                Set inc = localInc;
                if (path.startsWith("/")) { // external include
                    path = path.substring(1); // remove leading /
                    inc = extInc;
                }
                if (path.length() > 0) {
                    inc.add(path + "/" + name + ".h");
                } else {
                    inc.add(name + ".h");
                }
            }
        }
    }

    private String generateHeaderPackageStartSingle(Object pkg) {
        StringBuffer sb = new StringBuffer(30);
        String packageName = getFacade().getName(pkg);
        StringTokenizer st = new StringTokenizer(packageName, ".");
        String token = "";

        sb.append(generateTaggedValues(pkg, DOC_COMMENT_TAGS));
        while (st.hasMoreTokens()) {
            token = st.nextToken();
            // create line: namespace FOO {"
            sb.append("namespace ").append(token).append(" {")
                .append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    private String generateHeaderPackageEndSingle(Object pkg) {
        StringBuffer sb = new StringBuffer(30);
        String packageName = getFacade().getName(pkg);
        StringTokenizer st = new StringTokenizer(packageName, ".");
        String token = "";
        while (st.hasMoreTokens()) {
            token = st.nextToken();
            StringBuffer tempBuf = new StringBuffer(20);
            String absoluteName = generatePackageAbsoluteName(pkg);
            if (absoluteName.indexOf(token) != -1) {
                absoluteName =
                    absoluteName.substring(0,
                               (absoluteName.indexOf(token)
                                + token.length()));
            }

            // create line: namespace FOO {"
            tempBuf.append("} /* End of namespace ").append(absoluteName);
            tempBuf.append(" */").append(LINE_SEPARATOR);
            sb.insert(0, tempBuf.toString());
        }
        return sb.toString();
    }

    private String generatePackageAbsoluteName(Object pkg) {
        String pack = generateRelativePackage(pkg, null, "::");
        pack = pack.substring(2); // remove leading ::
        if (pack.length() > 0) {
            pack += "::";
        }
        return pack + getFacade().getName(pkg);
    }

    /** Generate the name of item with package prefix relative to localPkg.
     * localPkg may be null, meaning the global namespace.
     */
    private String generateNameWithPkgSelection(Object item, Object localPkg) {
        if (item == null) {
            return "void ";
        }
        Object pkg = null;
        if (getFacade().isADataType(item)) {
            return getFacade().getName(item);
        } else if (getFacade().isAParameter(item)
                   || getFacade().isAAttribute(item)
                   || getFacade().isAAssociationEnd(item)
                   || getFacade().isAClassifier(item)) {
            pkg = getNamespaceWithoutModel(item);
        }

        if (pkg == null) {
            return getFacade().getName(item);
        }
        String packPrefix = generateRelativePackage(item, localPkg, "::");
        if (packPrefix.length() > 0) {
            packPrefix += "::";
        }
        return packPrefix + getFacade().getName(item);
    }

    /** Generate name with package specs, relative to actualNamespace.
     */
    private String generateNameWithPkgSelection(Object item) {
        Object pkg = actualNamespace;
        String name = generateNameWithPkgSelection(item, pkg);
        if (name.startsWith("::")) {
            name = name.substring(2); // remove leading ::
            // the leading :: is just ugly to see, but it could
            // be left there to emphasize that item is in the
            // global namespace
        }
        return name;
    }

    /** Generate the code to go from the current namespace to cls's one
     */
    private String generateHeaderPackageStart(Object cls) {
        StringBuffer sb = new StringBuffer(80);

        if (actualNamespace != null) {
            Object lastSearch = actualNamespace;
            // iterate while fromSearch != null, but iterate one time
            // when it is null too, because it's the global namespace
            for (Object fromSearch = actualNamespace;
                    fromSearch != null;
                    lastSearch = getNamespaceWithoutModel(fromSearch)) {
                fromSearch = lastSearch;
                StringBuffer contPath = new StringBuffer(80);
                Object toSearch = getNamespaceWithoutModel(cls);
                for (; (toSearch != null) && (toSearch != fromSearch);
                        toSearch = getNamespaceWithoutModel(toSearch)) {
                    contPath.insert(0,
                            generateHeaderPackageStartSingle(toSearch));
                }
                if (toSearch == fromSearch) {
                    sb.append(contPath.toString());
                    break;
                }
                // close one namespace
                sb.append(generateHeaderPackageEndSingle(fromSearch));
            }
        }
        else { // initial start
            for (Object toSearch = getNamespaceWithoutModel(cls);
                    toSearch != null;
                    toSearch = getNamespaceWithoutModel(toSearch)) {
                sb.insert(0, generateHeaderPackageStartSingle(toSearch));
            }
        }
        actualNamespace = getNamespaceWithoutModel(cls);
        return sb.toString();
    }

    /**
     * Retrieve the namespace of the given model element, excluding the model,
     * which shouldn't be considered a namespace.
     * @param me the model element for which to get the namespace
     * @return the namespace if it exists or null if the model is the
     * containing namespace for <code>me</code>
     */
    private Object getNamespaceWithoutModel(Object me) {
        Object parent = getFacade().getNamespace(me);
        if (parent != null && getFacade().getNamespace(parent) != null) 
            return parent;
        return null;
    }

    private String generateHeaderPackageEnd() {
        StringBuffer sb = new StringBuffer(20);

        for (Object closeIt = actualNamespace;
                closeIt != null;
                closeIt = getNamespaceWithoutModel(closeIt)) {
            sb.append(generateHeaderPackageEndSingle(closeIt));
        }
        actualNamespace = null;
        return sb.toString();
    }

    /* This generates the file internal header, not the .h file!
     * That is, things before the class declaration.
     */
    private String generateHeader(Object cls) {
        StringBuffer sb = new StringBuffer(240);

        addUserHeaders(cls, generatorPass == SOURCE_PASS);

        if (getNamespaceWithoutModel(cls) != null) {
            String pkgstart = generateHeaderPackageStart(cls);
            if (pkgstart.length() > 0) {
                sb.append(LINE_SEPARATOR);
                sb.append(pkgstart);
            }
        }

        return sb.toString();
    }

    /* 
     * This generates all the things that go after the class declaration.
     */
    private String generateFooter() {
        StringBuffer sb = new StringBuffer(80);
        sb.append(generateHeaderPackageEnd());
        if (sb.length() > 0) {
            sb.insert(0, LINE_SEPARATOR);
        }
        return sb.toString();
    }

    /** 2002-11-28 Achim Spangler
     * separate generation of Operation Prefix from generateOperation
     * so that generateOperation is language independent
     */
    private String generateOperationPrefix(Object op) {
        StringBuffer sb = new StringBuffer(80);
        // c++ doesn't have any builtin construct for concurrency
        //sb.append(generateConcurrency(op));
        if (generatorPass != SOURCE_PASS) {
            // make all operations to virtual - as long as they are not "leaf"
            if (getFacade().isLeaf(op) && !getFacade().isRoot(op)) {
                // there's no way to make a leaf method that it's not root in
                // c++, so warn the user and ignore the 'root' attribute
                // (or it may be better to ignore the 'leaf' attribute?)
                LOG.warn(op + " is leaf but not root: "
                         + "C++ can't handle this properly");
                LOG.warn("    Ignoring the 'root' attribute");
            }
            // generate a function as virtual, if it can be overridden
            // or override another function AND if this function is
            // not marked as static, which disallows "virtual"
            // alternatively every abstract function is defined as
            // virtual
            if ((!getFacade().isLeaf(op) && !getFacade().isConstructor(op)
                    && (!(getFacade().isStatic(op))))
                    || (getFacade().isAbstract(op))) {
                sb.append("virtual ");
            }
            sb.append(generateScope(op));
        }
        return sb.toString();
    }

    /** 2002-11-28 Achim Spangler
     * separate generation of Operation Suffix from generateOperation
     * so that generateOperation is language independent
     */
    private String generateOperationSuffix(Object op) {
        StringBuffer sb = new StringBuffer(80);
        sb.append(generateOperationChangeability(op));
        sb.append(generateAbstractness(op));
        return sb.toString();
    }

    /** 2002-11-28 Achim Spangler
     * separate generation of Operation Name from generateOperation
     * so that generateOperation is language independent
     * -> for C++: if we create .cpp we must prepend Owner name
     *
     * @param sb Where to put the result.
     */
    private void generateOperationNameAndTestForConstructor(Object op,
            StringBuffer sb) {
        if (generatorPass == SOURCE_PASS) {
            Object cls = getFacade().getOwner(op);
            String prefix = new String();
            while (!getFacade().isAPackage(cls)) {
                prefix = getFacade().getName(cls) + "::" + prefix;
                cls = getFacade().getNamespace(cls);
            }
            sb.append(prefix);
        }
        String name;
        if (getFacade().isConstructor(op)) {
            name = getFacade().getName(getFacade().getOwner(op));
        } else if (isDestructor(op)) {
            name = "~" + getFacade().getName(getFacade().getOwner(op));
        } else {
            name = getFacade().getName(op);
        }
        sb.append(name);
    }

    private boolean isDestructor(Object op) {
        return getExtensionMechanismsHelper().hasStereotype(op, "destroy");
    }

    /**
     * Generate code for an operation.
     * 
     * 2002-11-28 Achim Spangler
     * modified version from Jaap Branderhorst
     * -> generateOperation is language independent and separates
     *    different tasks
     * @param op The operation for which to generate code.
     * @param documented If the documentation should be included in the 
     * generated code or not.
     * @return The C++ code for the operation.
     */
    public String generateOperation(Object op, boolean documented) {
        // generate nothing for abstract functions, if we generate the
        // source .cpp file at the moment
        if ((generatorPass == SOURCE_PASS) && (getFacade().isAbstract(op))) {
            return "";
        }
        StringBuffer sb = new StringBuffer(80);
        StringBuffer nameBuffer = new StringBuffer(20);
        String operationIndent = (generatorPass == HEADER_PASS) ? indent : "";
        generateOperationNameAndTestForConstructor(op, nameBuffer);

        // if generating a file always document
        if (documented || generatorPass != NONE_PASS) {
            // generate DocComment from tagged values
            String tv = generateTaggedValues (op, DOC_COMMENT_TAGS);
            if (tv != null && tv.length() > 0) {
                sb.append (LINE_SEPARATOR).append(operationIndent).append (tv);
            }
        }

        sb.append(operationIndent)
            .append(generateOperationPrefix(op));

        // pick out return type
        Collection returnParams = getCoreHelper().getReturnParameters(op);
        Object rp;
        if (returnParams.size() == 0) {
            rp = null;
        } else {
            rp = returnParams.iterator().next();
        } 
        if (returnParams.size() > 1)  {
            LOG.warn("C++ generator only handles one return parameter"
                    + " - Found " + returnParams.size()
                    + " for " + getFacade().getName(op));
        }
        if (!getFacade().isConstructor(op) && !isDestructor(op)) {
            Inline inlineStyle = Inline.getInlineOperationModifierType(op);
            sb.append(inlineStyle.getInlineKeyword4Declaration());
            
            if (rp != null) {
                Object returnType = getFacade().getType(rp);
                if (returnType == null) {
                    sb.append("void ");
                }
                else if (returnType != null) {
                    sb.append(generateNameWithPkgSelection(returnType))
                        .append(' ');
                    /* fixing 2862 - apply modifiers, 
                     * i.e. pointer or reference TV */
                    sb.append(generateAttributeParameterModifier(rp));
                }
                boolean predecl = !checkIncludeNeeded4Element(rp);
                addDependency(returnType, predecl);
            }
        }

        // name and params
        Vector params = new Vector(getFacade().getParameters(op));
        params.remove(rp); // If there are several return parameters, just
                           // the one found above will be removed.

        sb.append(nameBuffer.toString()).append('(');

        if (params != null) {
            boolean first = true;

            for (int i = 0; i < params.size(); i++) {
                Object p = params.elementAt (i);

                if (!first) sb.append(", ");

                sb.append(generateParameter(p));
                first = false;
            }
        }

        String suffix = generateOperationSuffix(op);
        if (suffix.equals(""))
            sb.append(")");
        else
            sb.append(") ").append(suffix);

        return sb.toString();
    }

    /** 2002-12-06 Achim Spangler
     * check if a parameter is tagged as pointer or reference (not
     * part of UML - as far as author knows - but important for C++
     * developers)
     * @param elem element to check
     * @return one of NORMAL_MOD, REFERENCE_MOD, POINTER_MOD, or -1 if
     *         no specific tag is found
     */
    private int getAttributeModifierType(Object elem) {
        // first check whether the parameter shall be a pointer of reference
        Iterator iter = getFacade().getTaggedValues(elem);
        while (iter.hasNext()) {
            Object tv = iter.next();
            String tag = getFacade().getTagOfTag(tv);
            String val = getFacade().getValueOfTag(tv);
            if (tag != null) {
                if (tag.equals(TV_NAME_REFERENCE) || tag.equals("&")) {
                    return val.equals("false") ? NORMAL_MOD
                            : REFERENCE_MOD;
                } else if (tag.equals(TV_NAME_POINTER) || tag.equals("*")) {
                    return val.equals("false") ? NORMAL_MOD
                            : POINTER_MOD;
                }
            }
        }
        return -1; /* no tag found */
    }


    private String generateAttributeParameterModifier(Object attr,
                                                      String def) {
        int modType = getAttributeModifierType(attr);

        // if attr has an abstract type it must be pointer or reference
        if (modType == NORMAL_MOD || modType == -1) {
            // this is used for association classes to; skip them
            if (!getFacade().isAAssociationClass(attr)) {
                Object type = getFacade().getType(attr);
                if (type == null) {
                    // model corrupt (this really happened -- aslo)
                    LOG.error(attr + " has no type!");
                    return "";
                }
                if (getFacade().isAbstract(type)
                    || getFacade().isAInterface(type)) {
                    if (modType == NORMAL_MOD) {
                        // user explicitly requested no modifier
                        LOG.warn("Requested no reference or pointer "
                                + "modifier, but");
                        LOG.warn("\t" + type + " cannot be instantiated, "
                                + "using reference");
                    }
                    modType = REFERENCE_MOD;
                }
            }
        }

        if (modType == NORMAL_MOD) {
            return "";
        } else if (modType == REFERENCE_MOD) {
            return "&";
        } else if (modType == POINTER_MOD) {
            return "*";
        } else if (def.length() == 0) {
            if (getFacade().isAParameter(attr)
                    && (getDirectionKind().getOutParameter().equals(
                        getFacade().getKind(attr))
                    || getDirectionKind().getInOutParameter().equals(
                        getFacade().getKind(attr)))) {
                // out or inout parameters are defaulted to reference if
                // not specified else
                return "&";
            }
        }
        return def;
    }

    /**
     * @param elem element to check
     * @return CONST_MOD or -1 if no specific tag is found
     */
    private int getConstAttributeModifierType(Object elem) {
        Iterator iter = getFacade().getTaggedValues(elem);
        while (iter.hasNext()) {
            Object tv = iter.next();
            String tag = getFacade().getTagOfTag(tv);
            String val = getFacade().getValueOfTag(tv);
            if (tag != null) {
                if (tag.equals(TV_NAME_CONST)) {
                    return val.equals("false") ? NORMAL_MOD : CONST_MOD;
                }
            }
        }    
        return -1; /* no tag found */
    }

    private String generateConstAttributeParameterModifier(Object attr) {
    	if (getConstAttributeModifierType(attr) == CONST_MOD) {
    	    return "const";
    	}
    	return null;
    }

    private String generateAttributeParameterModifier(Object attr) {
        return generateAttributeParameterModifier(attr, "");
    }


    public String generateAttribute(Object attr, boolean documented) {
        StringBuffer sb = new StringBuffer(80);

        // list tagged values for documentation
        if (documented || generatorPass != NONE_PASS) {
            String tv = generateTaggedValues (attr, DOC_COMMENT_TAGS);
            if (tv != null && tv.length() > 0) {
                sb.append (LINE_SEPARATOR).append(indent).append(tv).
                    append(indent);
            }
        }
        // cat.info("generate Visibility for Attribute");
        sb.append(generateVisibility(attr));
        sb.append(generateScope(attr));
        sb.append(generateStructuralFeatureChangeability(attr));
        sb.append(
                generateMultiplicity(attr, getFacade().getName(attr), 
                        getFacade().getMultiplicity(attr),
                        generateAttributeParameterModifier(attr)));
        sb.append(";");
        if (generatorPass != NONE_PASS)
            sb.append(LINE_SEPARATOR);

        // add the type of the attribute in the dependency list
        boolean predecl = !checkIncludeNeeded4Element(attr);
        addDependency(getFacade().getType(attr), predecl);

        return sb.toString();
    }


    private String generateParameter(Object param) {
        StringBuffer sb = new StringBuffer(20);
        //TODO: qualifiers (e.g., const)
        // generate const for references or pointers which are
        // defined as IN - other qualifiers are not important for
        // C++ parameters
        Object type = getFacade().getType(param);
        sb.append(generateParameterChangeability(param));
        //TODO: stereotypes...
        String constModifier = generateConstAttributeParameterModifier(param); 
        if (constModifier != null) {
            sb.append(constModifier).append(' ');
        }
        sb.append(generateNameWithPkgSelection(type));
        sb.append(' ');
        sb.append(generateAttributeParameterModifier(param));
        sb.append(getFacade().getName(param));

        // insert default value, if we are generating the header or notation
        if (generatorPass != SOURCE_PASS) {
            Object defvalObj = getFacade().getDefaultValue(param);
            if (defvalObj != null) {
                String defval =
                    getFacade().getBody(defvalObj).toString().trim();
                if (defval.length() > 0) {
                    sb.append(" = ").append(defval);
                }
            }
        }

        // add the type of the parameter in the dependency list
        boolean predecl = !checkIncludeNeeded4Element(param);
        addDependency(type, predecl);

        return sb.toString();
    }


    /**
     * Generate the start sequence for a classifier. The start sequence is
     * everything from the preceding javadoc comment to the opening curly brace.
     * Start sequences are non-empty for classes and interfaces only.
     *
     * This method is intended for package internal usage only.
     *
     * @param cls the classifier for which to generate the start sequence
     *
     * @return the generated start sequence
     */
    StringBuffer generateClassifierStart(Object cls) {
        StringBuffer sb = new StringBuffer (80);
        // Add the comments for this classifier first.
        sb.append(LINE_SEPARATOR)
            .append (DocumentationManager.getComments(cls));

        // list tagged values for documentation
        String tv = generateTaggedValues (cls, DOC_COMMENT_TAGS);
        if (tv != null && tv.length() > 0) {
            sb.append (LINE_SEPARATOR).append (indent).append (tv);
        }
        
        sb.append(generateClassifierNameAndAncestors(cls));

        // add opening brace
        if (lfBeforeCurly) {
            sb.append(LINE_SEPARATOR).append('{');
        } else if (generatorPass != SOURCE_PASS) {
            sb.append(" {");
        }

        // list tagged values for documentation
        tv = generateTaggedValues (cls, ALL_BUT_DOC_TAGS);
        if (tv != null && tv.length() > 0) {
            sb.append(LINE_SEPARATOR).append (indent).append (tv);
        }

        return sb;
    }
    
    /**
     * Generate the classifier name and ancestors, i.e., the keyword defining 
     * the class, the class name and the name of the classes from which this
     * one derives.
     *
     * @param cls the classifier for which to generate the name and ancestors
     *
     * @return the generated name and ancestors
     */
    public StringBuffer generateClassifierNameAndAncestors(Object cls) {
        StringBuffer sb = new StringBuffer (80);

        // don't create class-Start for implementation in .cpp
        if (generatorPass == SOURCE_PASS) return sb;

        String sClassifierKeyword;
        if (getFacade().isAClass(cls) || getFacade().isAInterface(cls)) {
            sClassifierKeyword = "class";
        } else {
            return null; // actors, use cases etc.
        }
        boolean hasBaseClass = false;

        // add classifier keyword and classifier name
        sb.append(sClassifierKeyword).append(" ");
        sb.append(getFacade().getName(cls));

        // add base class/interface
        String baseClass =
            generateGeneralization(getFacade().getGeneralizations(cls));
        if (!baseClass.equals ("")) {
            sb.append (" : ")
                .append (baseClass);
            hasBaseClass = true;
        }

        // add implemented interfaces, if needed (uml: realizations)
        if (getFacade().isAClass(cls)) {
            String interfaces = generateSpecification(cls);
            if (!interfaces.equals ("")) {
                if (!hasBaseClass) sb.append (" : ");
                else sb.append (", ");
                sb.append (interfaces);
            }
        }
        return sb;
    }

    private StringBuffer generateClassifierEnd(Object cls) {
        StringBuffer sb = new StringBuffer();
        if (getFacade().isAClass(cls) || getFacade().isAInterface(cls)) {
            if ((verboseDocs) && (generatorPass != SOURCE_PASS)) {
                String classifierkeyword = null;
                if (getFacade().isAClass(cls)) {
                    classifierkeyword = "class";
                } else {
                    classifierkeyword = "class";
                }
                sb.append(LINE_SEPARATOR)
                    .append("//end of ")
                        .append(classifierkeyword)
                            .append(" ").append(getFacade().getName(cls))
                                .append(LINE_SEPARATOR);
            }
            if (generatorPass != SOURCE_PASS)
                sb.append("};").append(LINE_SEPARATOR);
        }
        return sb;
    }

    /**
     * Generate three parts under public, protected and private visibility,
     * adding the visibility keywords on top of each part. 
     * @param parts the parts to output
     * @return the composed parts
     */
    private String generateAllParts(StringBuffer[] parts) {
        StringBuffer sb = new StringBuffer();
        // generate all parts in order: public, protected, private
        for (int i = 0; i < ALL_PARTS.length; i++) {
            if (parts[i].toString().trim().length() > 0) {
                if (generatorPass != SOURCE_PASS) {
                    if (i != 0) sb.append(LINE_SEPARATOR);
                    sb.append(' ').append(PART_NAME[i]).append(':');
                    sb.append(LINE_SEPARATOR);
                }
                sb.append(parts[i]);
            }
        }
        return sb.toString();
    }
    
    private int getVisibilityPart(Object o) {
        if (getFacade().isPublic(o)) {
            return PUBLIC_PART;
        } else if (getFacade().isProtected(o)) {
            return PROTECTED_PART;
        } else if (getFacade().isPrivate(o)) {
            return PRIVATE_PART;
        } else {
            LOG.warn(getFacade().getName(o)
                    + " is not public, nor protected, "
                    + "nor private!!! (ignored)");
            return -1;
        }
    }
    
    /* Indent each line of the given string by n indent spaces.
     */
    private String indentString(String s, int n) {
        String ind = new String();
        for (; n > 0; n--)
            ind += indent;
        // This works only with jdk 1.5: return s.replace("\n", "\n" + ind);
        StringBuffer result = new StringBuffer();
        // FIXME: replace hard-coded '\n' with LINE_SEPARATOR
        for (int i = s.indexOf('\n'); i != -1; i = s.indexOf('\n')) {
            result.append(ind).append(s.substring(0, i + 1));
            s = s.substring(i + 1);
        }
        if (s.length() > 0) {
            result.append(ind).append(s);
        }
        return result.toString();
    }

    /*
     * Generates code for a classifier, for classes and interfaces only
     * at the moment.
     */
    public String generateClassifier(Object cls) {
        // If we're in the notation pane, do a special trick
        // to show both header and source
        if (generatorPass == NONE_PASS && (getFacade().isAClass(cls) 
                || getFacade().isAInterface(cls))) {
            // for inner classes, show source of top level class
            // TODO: don't know if this is the best thing to do
            while (isAInnerClass(cls)) {
                cls = getFacade().getNamespace(cls);
            }
            StringBuffer sb = new StringBuffer();
            String name = getFacade().getName(cls);
            sb.append("// ").append(name).append(".h");
            sb.append(LINE_SEPARATOR);
            sb.append(generateH(cls));
            if (getFacade().isAClass(cls)) {
                sb.append(LINE_SEPARATOR);
                sb.append("// ").append(name).append(".cpp");
                sb.append(LINE_SEPARATOR);
                sb.append(generateCpp(cls));
            }
            return sb.toString();
        }
        
        StringBuffer returnValue = new StringBuffer();
        StringBuffer start = generateClassifierStart(cls);
        if (((start != null) && (start.length() > 0))
            || (generatorPass == SOURCE_PASS)) {
            StringBuffer typedefs = generateGlobalTypedefs(cls);
            StringBuffer body = generateClassifierBody(cls);
            StringBuffer end = generateClassifierEnd(cls);
            returnValue.append((typedefs != null) ? typedefs.toString() : "");
            returnValue.append(start);
            if ((body != null) && (body.length() > 0)) {
                returnValue.append(LINE_SEPARATOR);
                returnValue.append(body);
                if (lfBeforeCurly) {
                    returnValue.append(LINE_SEPARATOR);
                }
            }
            returnValue.append((end != null) ? end.toString() : "");
        }
        return returnValue.toString();
    }

    /** 2002-12-12 Achim Spangler
     * generate global typedefs
     */
    private StringBuffer generateGlobalTypedefs(Object cls) {
        StringBuffer sb = new StringBuffer();
        if (getFacade().isAClass(cls) || getFacade().isAInstance(cls)) {
            // add typedefs
            if (generatorPass == HEADER_PASS) {
                Collection globalTypedefStatements =
                    findTagValues(cls, "typedef_global_header");
                if (!globalTypedefStatements.isEmpty()) {
                    sb.append("// global type definitions for header defined "
                              + "by Tag entries in ArgoUML");
                    sb.append(LINE_SEPARATOR);
                    sb.append("// Result: typedef <typedef_global_header> "
                              + "<tag_value>;");
                    sb.append(LINE_SEPARATOR);
                    Iterator typedefEnum =
                        globalTypedefStatements.iterator();
                    while (typedefEnum.hasNext()) {
                        sb.append("typedef ").append(typedefEnum.next());
                        sb.append(";").append(LINE_SEPARATOR);
                    }
                }
            }
            else {
                Collection globalTypedefStatements =
                    findTagValues(cls, "typedef_global_source");
                if (!globalTypedefStatements.isEmpty()) {
                    sb.append("// global type definitions for class "
                            + "implementation in source file defined by Tag "
                            + "entries in ArgoUML");
                    sb.append(LINE_SEPARATOR);
                    sb.append("// Result: typedef <typedef_global_source> "
                            + "<tag_value>;");
                    sb.append(LINE_SEPARATOR);
                    Iterator typedefEnum = globalTypedefStatements.iterator();
                    while (typedefEnum.hasNext()) {
                        sb.append("typedef ").append(typedefEnum.next());
                        sb.append(";").append(LINE_SEPARATOR);
                    }
                }
            }
        }
        return sb;
    }

    /**
     * Generates the attributes of the body of a class or interface.
     * @param cls
     * @param sb Where to put the result.
     */
    private void generateClassifierBodyAttributes(Object cls,
            StringBuffer sb) {
        Collection attrs = getFacade().getAttributes(cls);
        if (attrs.isEmpty() || (generatorPass != HEADER_PASS)) {
            return;
        }
        String tv = null; // helper for tagged values
        sb.append(LINE_SEPARATOR);
        if (verboseDocs && getFacade().isAClass(cls)) {
            sb.append(indent).append("// Attributes").append(LINE_SEPARATOR);
        }

        // generate attributes in order public, protected, private
        StringBuffer part[] = new StringBuffer[ALL_PARTS.length];
        for (int i = 0; i < part.length; i++)
            part[i] = new StringBuffer(80);
        
        Iterator attrIter = attrs.iterator();
        while (attrIter.hasNext()) {
            Object attr = attrIter.next();
            int i = getVisibilityPart(attr);

            part[i].append(indent).append(generateAttribute(attr, false));

            tv = generateTaggedValues(attr, ALL_BUT_DOC_TAGS);
            if (tv != null && tv.length() > 0) {
                part[i].append(indent).append(tv);
            }
        }
        sb.append(generateAllParts(part));
    }

    /**
     * Generates the association ends of the body of a class or interface.
     * @param cls The classifier to generate.
     * @param sb Where to put the result.
     */
    private void generateClassifierBodyAssociations(Object cls,
            StringBuffer sb) {

        if (generatorPass == SOURCE_PASS)
            return;

        Collection ends = getFacade().getAssociationEnds(cls);
        if (!ends.isEmpty()) {

            sb.append(LINE_SEPARATOR);
            if (verboseDocs && getFacade().isAClass(cls)) {
                sb.append(indent).append("// Associations").append(
                        LINE_SEPARATOR);
            }

            StringBuffer part[] = new StringBuffer[3];
            for (int i = 0; i < ALL_PARTS.length; i++)
                part[i] = new StringBuffer(80);
            
            Iterator endEnum = ends.iterator();
            while (endEnum.hasNext()) {
                Object ae = endEnum.next();
                Object a = getFacade().getAssociation(ae);
                AssociationEndHandler aeHandler = new AssociationEndHandler(
                        ae);
                try {
                    aeHandler.pre();
                    generateAssociationFrom(a, ae, part);
                } finally {
                    aeHandler.post();
                }
            }
            sb.append(generateAllParts(part));
        }
        // if this is an association class, generate attributes for
        // all the AssociationEnds
        if (getFacade().isAAssociationClass(cls)) {
            if (verboseDocs) {
                sb.append(LINE_SEPARATOR).append(indent);
                sb.append("// AssociationClass associated classes");
            }
            // make all ends public... does it make sense?
            // should we declare friend all the associated and make
            // these protected? (private is too restrictive anyway, IMHO)
            // TODO: make it configurable, with a tag 
            sb.append(LINE_SEPARATOR).append(" public:").append(LINE_SEPARATOR);
            ends = getFacade().getConnections(cls);
            Iterator iter = ends.iterator();
            while (iter.hasNext()) {
                Object ae = iter.next();
                sb.append(LINE_SEPARATOR);
                String comment = generateConstraintEnrichedDocComment(cls, ae);
                if (comment.length() > 0)
                    sb.append(comment).append(indent);

                String n = getFacade().getName(ae);
                String name;
                Object type = getFacade().getType(ae);

                if (n != null && n.length() > 0) {
                    name = n;
                } else {
                    name = "my" + generateClassifierRef(type);
                }

                sb.append(generateNameWithPkgSelection(type));
                sb.append(generateAttributeParameterModifier(ae));
                sb.append(" ").append(name);
                sb.append(";").append(LINE_SEPARATOR);

                // add the type of the association end in the dependency list
                addDependency(type, !checkIncludeNeeded4Element(type));

                String tv = generateTaggedValues(ae, ALL_BUT_DOC_TAGS);
                if (tv != null && tv.length() > 0) {
                    sb.append(indent).append(tv);
                }
            }
        }
    }

    /**
     * Check whether an operation body shall be generated within the actual
     * pass. This is normally done during the implementation path.
     * But if the Tag "inline" exists, the method body shall be defined as
     * as inline in header file
     * @return true -> generate body in actual path
     */
    private boolean checkGenerateOperationBody(Object op) {
        boolean result = !((generatorPass == HEADER_PASS)
            || (getFacade().isAbstract(op))
                || (getFacade().isAInterface(getFacade().getOwner(op))));

        // if this operation has Tag "inline" the method shall be
        // generated in header
        Inline inlineStyle = Inline.getInlineOperationModifierType(op);
        if (generatorPass == HEADER_PASS) {
            result = inlineStyle.isMethodBodyInsideClass();
        } else if (generatorPass == NONE_PASS) {
            result = inlineStyle.isMethodBodyOutsideClass();
        }
        
        return result;
    }

    /** 2002-12-13 Achim Spangler
     * generate a single set function for a given attribute and StringBuffer
     */
    private void generateSingleAttributeSet(Object attr, StringBuffer sb) {
        if (getFacade().getType(attr) == null) {
            return;
        }
        // generate for attributes with class-type:
        // "indent void set_<name>( const <type> &value ) { <name> = value; };"
        // generate for other (small) data types:
        // "indent void set_<name>( <type> value ) { <name> = value; };"
        // generate: "indent void set_<name>( "
        sb.append(LINE_SEPARATOR).append(indent);
        sb.append("/** simple access function to set the attribute ");
        sb.append(getFacade().getName(attr));
        sb.append(" by function").append(LINE_SEPARATOR).append(indent);
        sb.append("  * @param value value to set for the attribute ");
        sb.append(getFacade().getName(attr)).append(LINE_SEPARATOR);
        sb.append(indent).append("  */").append(LINE_SEPARATOR);
        sb.append(indent);
        sb.append("void set_").append(getFacade().getName(attr))
            .append("( ");
        String modifier = generateAttributeParameterModifier(attr);
        if (modifier != null && modifier.length() > 0) {
            // generate: "const <type> <modifier>value"
            if (modifier.equals("&")) sb.append("const ");
            sb.append(generateClassifierRef(getFacade().getType(attr)))
                .append(' ').append(modifier).append("value");
        } else if (getFacade().isAClass(getFacade().getType(attr))) {
            // generate: "const <type> &value"
            sb.append("const ");
            sb.append(generateClassifierRef(getFacade().getType(attr)));
            sb.append(" &value");
        } else {
            // generate: "<type> value"
            sb.append(generateClassifierRef(getFacade().getType(attr)))
                .append(" value");
        }
        // generate: " ) { <name> = value; };"
        sb.append(" ) { ").append(getFacade().getName(attr));
        sb.append(" = value; };").append(LINE_SEPARATOR);
    }

    /** 2002-12-13 Achim Spangler
     * generate a single get function for a given attribute and StringBuffer
     */
    private void generateSingleAttributeGet(Object attr, StringBuffer sb) {
        if (getFacade().getType(attr) == null) return;
        // generate for attributes with class-type:
        // "const <type>& get_<name>( void ) { return <name>; };"
        // generate for other (small) data types
        // "<type> get_<name>( void ) { return <name>; };"
        // generate: "indent"
        sb.append(LINE_SEPARATOR).append(indent);
        sb.append("/** simple access function to get the attribute ");
        sb.append(getFacade().getName(attr));
        sb.append(" by function */").append(LINE_SEPARATOR).append(indent);
        String modifier = generateAttributeParameterModifier(attr);
        if (modifier != null && modifier.length() > 0) {
            // generate: "const <type><modifier>"
            sb.append("const ");
            sb.append(generateClassifierRef(getFacade().getType(attr)));
            sb.append(modifier);
        } else if (getFacade().isAClass(getFacade().getType(attr))) {
            // generate: "const <type>&"
            sb.append("const ");
            sb.append(generateClassifierRef(getFacade().getType(attr)));
            sb.append("&");
        } else {
            // generate: "<type>"
            sb.append(generateClassifierRef(getFacade().getType(attr)));
        }
        // generate: " get_<name>( void ) const { return <name>; };"
        sb.append(" get_").append(getFacade().getName(attr));
        sb.append("( void ) const { return ")
            .append(getFacade().getName(attr));
        sb.append("; };").append(LINE_SEPARATOR);
    }

    /**
     * Generates the attributes of the body of a class or interface.
     * @param cls
     */
    private void generateClassifierBodyTaggedAccess4Attributes(
            Object cls,
            StringBuffer funcPrivate,
            StringBuffer funcProtected,
            StringBuffer funcPublic) {
        Collection strs = getFacade().getAttributes(cls);
        if (strs.isEmpty() || (generatorPass != HEADER_PASS)) {
            return;
        }
        String accessTag = null;

        Iterator strEnum = strs.iterator();
        while (strEnum.hasNext()) {
            Object attr = strEnum.next();
            accessTag = getFacade().getTaggedValueValue(attr, 
                    TV_NAME_SET);
            if (accessTag != null && accessTag.length() > 0) {
                if (accessTag.indexOf("public") != -1) {
                    generateSingleAttributeSet(attr, funcPublic);
                }
                if (accessTag.indexOf("protected") != -1) {
                    generateSingleAttributeSet(attr, funcProtected);
                }
                if (accessTag.indexOf("private") != -1) {
                    generateSingleAttributeSet(attr, funcPrivate);
                }
            }

            accessTag = getFacade().getTaggedValueValue(attr, 
                    TV_NAME_GET);
            if (accessTag != null && accessTag.length() > 0) {
                if (accessTag.indexOf("public") != -1) {
                    generateSingleAttributeGet(attr, funcPublic);
                }
                if (accessTag.indexOf("protected") != -1) {
                    generateSingleAttributeGet(attr, funcProtected);
                }
                if (accessTag.indexOf("private") != -1) {
                    generateSingleAttributeGet(attr, funcPrivate);
                }
            }
        }
    }

    /**
     * Generates the association ends of the body of a class or interface.
     * @param cls
     * @param sb Where to put the result.
     */
    private void generateClassifierBodyOperations(Object cls,
            StringBuffer sb) {
        Collection behs = getFacade().getOperations(cls);
        if (behs.isEmpty()) return;
        sb.append(LINE_SEPARATOR);
        if (verboseDocs) {
            sb.append(indent).append("// Operations").append(LINE_SEPARATOR);
        }

        // generate tag controlled access functions for attributes
        StringBuffer[] funcs = new StringBuffer[3]; 
        funcs[0] = new StringBuffer(80);
        funcs[1] = new StringBuffer(80);
        funcs[2] = new StringBuffer(80);
        generateClassifierBodyTaggedAccess4Attributes(cls, funcs[PRIVATE_PART],
                                                      funcs[PROTECTED_PART],
                                                      funcs[PUBLIC_PART]);

        Iterator behEnum = behs.iterator();
        while (behEnum.hasNext()) {
            Object bf = behEnum.next();
            StringBuffer tb = null;

            int p = getVisibilityPart(bf);
            if (p < 0) continue;
            tb = funcs[p];

            boolean mustGenBody = checkGenerateOperationBody(bf);
            if (tb != null && ((generatorPass == HEADER_PASS) || mustGenBody)) {
                tb.append(LINE_SEPARATOR);
                tb.append(generateOperation(bf, false));
                
                // helper for tagged values
                String tv = generateTaggedValues(bf, ALL_BUT_DOC_TAGS);
                
                if (mustGenBody && (getFacade().isAClass(cls))
                        && (getFacade().isAOperation(bf))
                        && (!getFacade().isAbstract(bf))) {
                    // there is no ReturnType in behavioral feature (uml)
                    tb.append(LINE_SEPARATOR).append(generateMethodBody(bf));
                } else {
                    tb.append(";").append(LINE_SEPARATOR);
                    if (tv.length() > 0) {
                        tb.append(indent).append(tv).append(LINE_SEPARATOR);
                    }
                }
            }
        } // end loop through all operations

        sb.append(generateAllParts(funcs));
    }

    /**
     * Generates the association ends of the body of a class or interface.
     * @param cls
     * @param sb Where to put the result.
     */
    private void generateClassifierBodyTypedefs(Object cls, StringBuffer sb) {
        if (generatorPass == HEADER_PASS) {
            Collection publicTypedefStatements =
                findTagValues(cls, TV_NAME_TYPEDEF_PUBLIC);
            Collection protectedTypedefStatements =
                findTagValues(cls, TV_NAME_TYPEDEF_PROTECTED);
            Collection privateTypedefStatements =
                findTagValues(cls, TV_NAME_TYPEDEF_PRIVATE);
            if (!publicTypedefStatements.isEmpty()) {
                sb.append(LINE_SEPARATOR).append(" public:")
                    .append(LINE_SEPARATOR).append(indent);
                sb.append("// public type definitions for header defined "
                      + "by Tag entries in ArgoUML").append(LINE_SEPARATOR);
                sb.append(indent);
                sb.append("// Result: typedef <typedef_public> "
                      + "<tag_value>;").append(LINE_SEPARATOR);
                Iterator typedefEnum = publicTypedefStatements.iterator();

                while (typedefEnum.hasNext()) {
                    sb.append(indent).append("typedef ");
                    sb.append(typedefEnum.next())
                        .append(";").append(LINE_SEPARATOR);
                }
            }
            if (!protectedTypedefStatements.isEmpty()) {
                sb.append(LINE_SEPARATOR).append(" protected:")
                    .append(LINE_SEPARATOR).append(indent);
                sb.append("// protected type definitions for header defined "
                      + "by Tag entries in ArgoUML").append(LINE_SEPARATOR);
                sb.append(indent);
                sb.append("// Result: typedef <typedef_protected> "
                      + "<tag_value>;").append(LINE_SEPARATOR);
                Iterator typedefEnum = protectedTypedefStatements.iterator();

                while (typedefEnum.hasNext()) {
                    sb.append(indent).append("typedef ");
                    sb.append(typedefEnum.next())
                        .append(";").append(LINE_SEPARATOR);
                }
            }
            if (!privateTypedefStatements.isEmpty()) {
                sb.append(LINE_SEPARATOR).append(" private:")
                    .append(LINE_SEPARATOR).append(indent);
                sb.append("// private type definitions for header defined "
                      + "by Tag entries in ArgoUML").append(LINE_SEPARATOR);
                sb.append(indent);
                sb.append("// Result: typedef <typedef_private> "
                      + "<tag_value>;").append(LINE_SEPARATOR);
                Iterator typedefEnum = privateTypedefStatements.iterator();

                while (typedefEnum.hasNext()) {
                    sb.append(indent).append("typedef ");
                    sb.append(typedefEnum.next()).append(";")
                        .append(LINE_SEPARATOR);
                }
            }
        }
    }

    /**
     * Generates a virtual destructor when the classifier is an interface.
     * @param cls the classifier object
     * @param sb the buffer to where the generate code goes
     */
    private void generateClassifierDestructor(Object cls, StringBuffer sb) {
        if (getFacade().isAInterface(cls) 
                && generatorPass == HEADER_PASS) {
            sb.append(LINE_SEPARATOR).append("public:").append(LINE_SEPARATOR);
            sb.append(indent).append("// virtual destructor for interface ")
                .append(LINE_SEPARATOR);
            sb.append(indent).append("virtual ").append('~').append(
                getFacade().getName(cls)).append("() { }")
                    .append(LINE_SEPARATOR);
        }
    }

    private void generateClassifierInnerClasses(Object cls, StringBuffer sb) {
        StringBuffer part[] = new StringBuffer[ALL_PARTS.length];
        for (int i = 0; i < part.length; i++)
            part[i] = new StringBuffer(80);

        Collection inners = getFacade().getOwnedElements(cls);
        for (Iterator it = inners.iterator(); it.hasNext();) {
            Object inner = it.next();
            if (getFacade().isAClass(inner)
                || getFacade().isAInterface(inner)) {
                String innerCode = generateClassifier(inner);
                int p = getVisibilityPart(inner);
                part[p].append(LINE_SEPARATOR);
                if (generatorPass == HEADER_PASS) {
                    part[p].append(indentString(innerCode, 1));
                } else {
                    part[p].append(innerCode);
                }
                part[p].append(LINE_SEPARATOR);
            }
        }
        sb.append(generateAllParts(part));
    }

    /**
     * Generates the body of a class or interface.
     * @param cls
     * @return a StringBuffer with the result.
     */
    private StringBuffer generateClassifierBody(Object cls) {
        StringBuffer sb = new StringBuffer();
        if (getFacade().isAClass(cls) || getFacade().isAInterface(cls))
        { 
            // Inner classes
            generateClassifierInnerClasses(cls, sb);

            // add operations
            // TODO: constructors
            generateClassifierBodyOperations(cls, sb);

            // fixing issue #2587
            generateClassifierDestructor(cls, sb);

            // add attributes
            generateClassifierBodyAttributes(cls, sb);

            // add attributes implementing associations
            generateClassifierBodyAssociations(cls, sb);

            // add typedefs
            generateClassifierBodyTypedefs(cls, sb);
        }
        return sb;
    }

    /**
     * Generate the body of a method associated with the given
     * operation.  This assumes there's at most one method associated!
     *
     * If no method is associated with the operation, a default method
     * body will be generated.
     */
    private String generateMethodBody(Object op) {
        if (op != null) {
            StringBuffer sb = new StringBuffer(80);
            Collection methods = getFacade().getMethods(op);
            Iterator i = methods.iterator();
            Object method = null;
            boolean methodFound = false;
            String tv = generateTaggedValues(op, ALL_BUT_DOC_TAGS);
            String operationIndent =
                (generatorPass == HEADER_PASS) ? indent : "";

            // append tags which are not Doc-Comments
            if (tv.length() > 0) {
                sb.append(operationIndent).append(tv).append(LINE_SEPARATOR);
            }

            // place the curly braces within the protected area, to
            // allow placement of preserved constructor initializers in
            // this area otherwise all possible constructor-attribute
            // initializers would have to be autogenerated with an
            // army of special tags
            sb.append(generateSectionTop(op, operationIndent))
                .append(operationIndent).append("{").append(LINE_SEPARATOR);

            while (i != null && i.hasNext()) {
                method = i.next();

                if (method != null) {
                    if ((getFacade().getBody(method) != null)
                            && (!methodFound)) {
                        Object body = getFacade().getBody(method);
                        sb.append(getFacade().getBody(body));
                        methodFound = true;
                        break;
                    }
                }
            }

            if (!methodFound) {
                // pick out return type as default method body
                Collection returnParams = getCoreHelper()
                        .getReturnParameters(op);
                Object rp;
                if (returnParams.size() == 0) {
                    rp = null;
                } else {
                    rp = returnParams.iterator().next();
                } 
                if (returnParams.size() > 1)  {
                    LOG.warn("C++ generator only handles one return parameter"
                            + " - Found " + returnParams.size()
                            + " for " + getFacade().getName(op));
                }
                if (rp != null) {
                    Object returnType = getFacade().getType(rp);
                    sb.append(generateDefaultReturnStatement(returnType));
                }
            }
            sb.append(operationIndent).append("}").append(LINE_SEPARATOR)
                .append(generateSectionBottom(op, operationIndent));
            return sb.toString();
        }
        return generateDefaultReturnStatement (null);
    }


    private String generateSectionTop(Object op, String localIndent) {
        String id = UUIDHelper.getUUID(op);
        return Section.generateTop(id, localIndent);
    }

    private String generateSectionBottom(Object op, String localIndent) {
        String id = UUIDHelper.getUUID(op);
        return Section.generateBottom(id, localIndent);
    }

    private String generateDefaultReturnStatement(Object cls) {
        if (cls == null) return "";

        String clsName = getFacade().getName(cls);
        String res = null;
        if (clsName.equals("void")) res = "";
        else if (clsName.equals("char")) res = "return 'x';";
        else if (clsName.equals("int")) res = "return 0;";
        else if (clsName.equals("bool")) res = "return false;";
        else if (clsName.equals("byte")) res = "return 0;";
        else if (clsName.equals("long")) res = "return 0;";
        else if (clsName.equals("float")) res = "return 0.0;";
        else if (clsName.equals("double")) res = "return 0.0;";

        if (res == null) {
            return ""; // in doubt, let the choice to the user
        }
        return indent + res + LINE_SEPARATOR;
    }

    private String generateTaggedValues(Object e, int tagSelection) {
        Iterator iter = getFacade().getTaggedValues(e);
        if (!iter.hasNext()) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        boolean first = true;

        String s = null;
        while (iter.hasNext()) {
            s = generateTaggedValue(iter.next(), tagSelection);
            if (s != null && s.length() > 0) {
                if (first) {
                    if (tagSelection == DOC_COMMENT_TAGS) {
                        // insert main documentation for DocComment at first
                        String doc =
                            (DocumentationManager.hasDocs(e))
                            ? DocumentationManager.getDocs(e, indent)
                            : null;
                        if (doc != null && doc.trim().length() > 0) {
                            buf.append(doc.substring(0, doc.indexOf("*/") + 1));
                            buf.append("  ");
                        }
                        else {
                            buf.append("/** ");
                        }
                    }
                    else {
                        buf.append("/* {");
                    }
                    first = false;
                } // end first
                else {
                    if (tagSelection == DOC_COMMENT_TAGS) {
                        buf.append(LINE_SEPARATOR)
                            .append(indent).append(" *  ");
                    }
                    else {
                        buf.append(", ");
                    }
                } // end not first tag
                buf.append(s);
            } // end tag not empty
        } // end while

        if (!first) {
            if (tagSelection == DOC_COMMENT_TAGS) {
                buf.append(LINE_SEPARATOR).append(indent).append(" */")
                    .append(LINE_SEPARATOR);
            } else {
                buf.append ("}*/").append(LINE_SEPARATOR);
            }
        }
        else if (tagSelection == DOC_COMMENT_TAGS) {
            // create at least main documentation field, if no other tag found
            String doc = (DocumentationManager.hasDocs(e))
                ? DocumentationManager.getDocs(e, indent)
                : null;
            if (doc != null && doc.trim().length() > 0) {
                buf.append(doc).append(LINE_SEPARATOR);
            }
        }

        return buf.toString();
    }

    private String generateTaggedValue(Object tv, int tagSelection) {
        if (tv == null) return "";
        String s = generateUninterpreted(getFacade().getValueOfTag(tv));

        String tagName = getFacade().getTagOfTag(tv);
        if (s == null || s.length() == 0 || s.equals("/** */")
            || tagName == null 
            || (tagName.indexOf("include") != -1)
            || (tagName.indexOf("_incl") != -1)) {
            return "";
        }
        if ((tagSelection == DOC_COMMENT_TAGS)
                && (isDocCommentTag(tagName))) {
            return generateDocComment4Tag(tagName) + s;
        } else if (((tagSelection == ALL_BUT_DOC_TAGS)
                     && (!isDocCommentTag(tagName))
                     && (!tagName.equals("documentation"))
                     && (!tagName.equals("javadocs"))
                    )
                   || (tagSelection == ALL_TAGS)) {
            return tagName + "=" + s;
        } else {
            return "";
        }
    }

    private Collection findTagValues(Object item, String searchedName) {
        Collection result = new Vector();

        Iterator iter = getFacade().getTaggedValues(item);
        String s = null;
        while (iter.hasNext()) {
            Object tag = iter.next();
            String tagOfTag = getFacade().getTagOfTag(tag);
            if (tagOfTag != null && tagOfTag.equals(searchedName)) {
                s = getFacade().getValueOfTag(tag);
                if (s != null && s.length() != 0) result.add(s);
            }
        }
        return result;
    }

    private boolean isDocCommentTag(String tagName) {
        boolean result = false;
        if (tagName.equals ("inv")) {
            result = true;
        }
        else if (tagName.equals ("post")) {
            result = true;
        }
        else if (tagName.equals ("pre")) {
            result = true;
        }
        else if (tagName.equals ("author")) {
            result = true;
        }
        else if (tagName.equals ("version")) {
            result = true;
        }
        else if (tagName.equals ("see")) {
            result = true;
        }
        else if (tagName.equals ("param")) {
            result = true;
        }
        return result;
    }

    private String generateDocComment4Tag(String tagName) {
        if (tagName.equals ("inv")) {
            return "@invariant ";
        }
        else if (tagName.equals ("post")) {
            return "@postcondition ";
        }
        else if (tagName.equals ("pre")) {
            return "@precondition ";
        }
        else if (tagName.equals ("author")) {
            return "@author ";
        }
        else if (tagName.equals ("version")) {
            return "@version ";
        }
        else if (tagName.equals ("see")) {
            return "@see ";
        }
        else if (tagName.equals ("param")) {
            return "@param ";
        }
        else return "";
    }

    /**
     * Enhance/Create the doccomment for the given model element,
     * including tags for any OCL constraints connected to the model
     * element. The tags generated are suitable for use with the ocl
     * injector which is part of the Dresden OCL Toolkit and are in
     * detail:
     *
     * &nbsp;@invariant for each invariant specified
     * &nbsp;@precondition for each precondition specified
     * &nbsp;@postcondition for each postcondition specified
     * &nbsp;@key-type specifying the class of the keys of a mapped association
     * &nbsp; Currently mapped associations are not supported yet...
     * &nbsp;@element-type specifying the class referenced in an association
     *
     * @since 2001-09-26 ArgoUML 0.9.3
     * @author Steffen Zschaler
     *
     * @param me the model element for which the documentation comment is needed
     * @param ae the association end which is represented by the model element
     * @return the documentation comment for the specified model element, either
     * enhanced or completely generated
     */
    private String generateConstraintEnrichedDocComment(Object me, Object ae) {
        // list tagged values for documentation
        String s = generateTaggedValues (me, DOC_COMMENT_TAGS);

        if (getFacade().getUpper(ae) != 1) {
            // Multiplicity greater 1, that means we will generate some sort of
            // collection, so we need to specify the element type tag
            StringBuffer sDocComment = new StringBuffer(80);

            // Prepare doccomment
            if (!(s == null || "".equals(s))) {
                // Just remove closing "*/"
                sDocComment.append(indent)
                    .append(s.substring(0, s.indexOf("*/") + 1));
            }
            else {
                sDocComment.append(indent).append("/**").append(LINE_SEPARATOR);
                sDocComment.append(indent).append(" *");
            }

            // Build doccomment
            Object type = getFacade().getType(ae);
            if (type != null) {
                sDocComment.append(" @element-type ");
                sDocComment.append(getFacade().getName(type));
            }
            sDocComment.append(LINE_SEPARATOR).append(indent)
                .append(" */").append(LINE_SEPARATOR);
            return sDocComment.toString();
        }
        return (s != null) ? s : "";
    }

    /**
     * 
     * @param a association object
     * @param ae association end attached to the classifier
     *        for which the code is to be generated
     * @param parts the buffers associated with the public, protected
     *        and private parts, where the code is to be written.
     */
    private void generateAssociationFrom(Object a, Object ae,
            StringBuffer[] parts) {
        // TODO: does not handle n-ary associations

        Collection connections = getFacade().getConnections(a);
        Iterator connEnum = connections.iterator();
        while (connEnum.hasNext()) {
            Object ae2 = connEnum.next();
            if (ae2 != ae) {
                int p = getVisibilityPart(ae2);
                if (p >= 0) {
                    StringBuffer sb = parts[p];
                    sb.append(LINE_SEPARATOR);
                    String assend = generateAssociationEnd(ae2);
                    if (assend.length() > 0) {
                        String comment =
                            generateConstraintEnrichedDocComment(a, ae2);
                        if (comment.length() > 0)
                            sb.append(comment);
                        // both comment and assend ends with simple newline
                        sb.append(indent).append(assend);
                    }

                    String tv = generateTaggedValues(a, ALL_BUT_DOC_TAGS);
                    if (tv != null && tv.length() > 0) {
                        sb.append(indent).append(tv);
                    }
                }
            }
        }
    }

    private String generateAssociationEnd(Object ae) {
        if (!getFacade().isNavigable(ae)) {
            return "";
        }
        if (getFacade().isAbstract(
                getFacade().getAssociation(ae))) {
            return "";
        }
        StringBuffer sb = new StringBuffer(80);

        if (getFacade().isStatic(ae)) {
            sb.append("static ");
        }

        String n = getFacade().getName(ae);
        Object asc = getFacade().getAssociation(ae);
        String ascName = getFacade().getName(asc);
        String name = null;

        if (n != null && n.length() > 0) {
            name = n;
        } else if (ascName != null && ascName.length() > 0) {
            name = ascName;
        } else {
            name = "my" + generateClassifierRef(getFacade().getType(ae));
        }

        String modifier;
        if (getFacade().isAAssociationClass(asc)) {
            // With an association class, we actually make an association
            // between us and the association class itself.
            // Usually, this is a pointer or a reference, so default
            // to a pointer.
            modifier = generateAttributeParameterModifier(asc, "*");
            // add the association class in the dependency list
            addDependency(asc, !checkIncludeNeeded4Element(ae)); 
        } else {
            modifier = generateAttributeParameterModifier(ae);
            // add the type of the association end in the dependency list
            boolean predecl = !checkIncludeNeeded4Element(ae);
            addDependency(getFacade().getType(ae), predecl);
        }
        
        sb.append(generateMultiplicity(ae, name,
                             getFacade().getMultiplicity(ae),
                             modifier));

        return (sb.append(";").append(LINE_SEPARATOR)).toString();
    }


    private String generateGeneralization(Collection generalizations) {
        if (generalizations == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer(80);
        Iterator genEnum = generalizations.iterator();
        while (genEnum.hasNext()) {
            Object generalization = genEnum.next();
            Object ge = getFacade().getGeneral(generalization);
            if (ge != null) {
                if (sb.length() > 0) sb.append(", ");
                String visTag =
                    getFacade().getTaggedValueValue(generalization,
                            TV_NAME_INHERITANCE_VISIBILITY).trim();
                if (visTag != null && !visTag.equals("")) {
                    sb.append(visTag).append(" ");
                } else {
                    if (getFacade().isAInterface(ge)) {
                        sb.append("virtual public ");
                    } else {
                        sb.append("public ");
                    }
                }
                sb.append(generateNameWithPkgSelection(ge));
                // add the type of the base class in the dependency list
                addDependency(ge, false);
            }
        }
        return sb.toString();
    }

    private String generateSpecification(Object cls) {
        Collection deps = getFacade().getClientDependencies(cls);
        Iterator depIterator = deps.iterator();
        StringBuffer sb = new StringBuffer(80);

        while (depIterator.hasNext()) {
            Object dependency = depIterator.next();
            if (getFacade().isAAbstraction(dependency)
                    && getFacade().isRealize(dependency)) {
                if (sb.length() > 0) sb.append(", ");
                Object iFace = getFacade().getSuppliers(dependency)
                    .iterator().next();
                String visTag =
                    getFacade().getTaggedValueValue(dependency,
                            TV_NAME_INHERITANCE_VISIBILITY).trim();
                if (visTag != null && !visTag.equals("")) {
                    sb.append(visTag).append(" ");
                } else {
                    sb.append("virtual public ");
                }
                sb.append(generateNameWithPkgSelection(iFace));
                // add the type of the interface in the dependency list
                addDependency(iFace, false);
            }
        }
        return sb.toString();
    }

    private String generateVisibility(Object o) {
        if (getFacade().isAAttribute(o)) {
            return "";
        }
        // cut'n'pasted from GeneratorJava.java
        if (getFacade().isAFeature(o)) {
            // TODO: The src_visibility tag doesn't appear to be created
            // anywhere by ArgoUML currently
            Object tv = getFacade().getTaggedValue(o, "src_visibility");
            if (tv != null) {
                String tagged = (String) getFacade().getValue(tv);
                if (tagged != null) {
                    if (tagged.trim().equals("")
                            || tagged.trim().toLowerCase().equals("default")
                            // We should never get "package", but just in case
                            || tagged.trim().toLowerCase().equals("package")) {
                        return "";
                    } 
                    return tagged + ": ";
                }
            }
        }
        if (getFacade().isAModelElement(o)) {
            if (getFacade().isPublic(o))
                return "public: ";
            if (getFacade().isPrivate(o))
                return "private: ";
            if (getFacade().isProtected(o))
                return "protected: ";
            if (getFacade().isPackage(o))
                // TODO: is default visibility the right thing here?
                return "";
        }
        if (getFacade().isAVisibilityKind(o)) {
            if (getVisibilityKind().getPublic().equals(o))
                return "public: ";
            if (getVisibilityKind().getPrivate().equals(o))
                return "private: ";
            if (getVisibilityKind().getProtected().equals(o))
                return "protected: ";
            if (getVisibilityKind().getPackage().equals(o))
                // TODO: is default visibility the right thing here?
                return "";
        }
        return "";
    }

    /**
     * @param me The model element to generate scope for.
     * @return The generated text representing the scope.
     */
    private String generateScope(Object me) {
        if (getFacade().isStatic(me)) {
            return "static ";
        }
        return "";
    }


    /**
     * Generate "abstract" keyword for an abstract operation.
     * In C++, since it does not have an explicit "interface" keyword, we must
     * check against this and set the operation to abstract if so.
     */
    private String generateAbstractness(Object op) {
        // use Model subsystem Facade to check if the operation is 
        // owned by an interface
        Object opOwner = getFacade().getOwner(op);
        if (getFacade().isAbstract(op) || getFacade().isAInterface(opOwner)) {
            return " = 0";
        }
        return "";
    }

    /**
     * Generate "const" keyword for query operations.
     */
    private String generateOperationChangeability(Object op) {
        if (getFacade().isQuery(op)) {
            return "const ";
        }
        return "";
    }

    /**
     * Generate "const" keyword for const pointer/reference parameters.
     */
    private String generateParameterChangeability(Object par) {
        int parType = getAttributeModifierType(par);
        if (parType != -1 && parType != NORMAL_MOD 
                && getFacade().getKind(par) != null 
                && (getFacade().getKind(par)).equals(
                        getDirectionKind().getInParameter())) {
            return "const ";
        }
        return "";
    }

    private String generateStructuralFeatureChangeability(Object sf) {
        if (getFacade().isReadOnly(sf)) {
            return "const ";
        }
        return "";
    }

    private String generateMultiplicity(Object item, String name,
            Object m, String modifier) {
        String type = null;
        String containerType = null;
        Object typeCls = null;
        if (getFacade().isAAssociationEnd(item)) {
            // take into account association classes
            Object assoc = getFacade().getAssociation(item);
            if (getFacade().isAAssociationClass(assoc)) {
                typeCls = assoc;
                name += "Assoc";
            }
            else typeCls = getFacade().getType(item);
        } else if (getFacade().isAAttribute(item)) {
            typeCls = getFacade().getType(item);
        } else if (getFacade().isAClassifier(item)) {
            typeCls = item;
        } else {
            type = "";
        }
        if (typeCls != null) {
            type = generateNameWithPkgSelection(typeCls);
        }
        if (m == null) {
            return (type + " " + modifier + name);
        }
        StringBuffer sb = new StringBuffer(80);
        int countUpper = getFacade().getUpper(m);
        int countLower = getFacade().getLower(m);

        if (countUpper == 1 && countLower == 1) {
            // simple generate identifier for default 1:1 multiplicity
            sb.append(type).append(' ').append(modifier).append(name);
        } else if (countUpper == 1 && countLower == 0) {
            // use a simple pointer for 0:1 multiplicity
            // TODO: use an auto_ptr in case of attributes or compositions
            sb.append(type).append(' ').append(modifier)
                .append("* ").append(name);
        } else if (countUpper == countLower) {
            // fixed array -> <type> <name>[<count>];
            sb.append(type).append(' ').append(modifier).append(name)
                .append("[ " + countUpper + "]");
        } else {
            // variable association -> if no tag found use vector
            // else search for tag:
            // <MultipliciyType> : vector|list|slist|map|stack|stringmap
            String multType =
                getFacade().getTaggedValueValue(item, 
                        TV_NAME_MULTIPLICITY_TYPE);
            if (multType != null && multType.length() > 0) {
                if (multType.equals("vector")) {
                    containerType = "vector";
                } else if (multType.equals("list")) {
                    containerType = "list";
                } else if (multType.equals("slist")) {
                    containerType = "slist";
                } else if (multType.equals("map")) {
                    // FIXME: map does not work this way, needs a index type
                    containerType = "map";
                } else if (multType.equals("stack")) {
                    containerType = "stack";
                } else if (multType.equals("stringmap")) {
                    systemInc.add("string");
                    systemInc.add("map");
                    sb.append(stdPrefix + "map<" + stdPrefix + "string, ");
                    if (modifier.indexOf('&') != -1) {
                        LOG.warn("cannot generate STL container "
                                + "with references, using pointers");
                        modifier = "*";
                    }
                    sb.append(type).append(modifier);
                    sb.append(" > ").append(name);
                } else {
                    LOG.warn("unknown " + TV_NAME_MULTIPLICITY_TYPE + " \"" 
                            + multType + "\", using default");
                    containerType = "vector";
                }
            } else {
                containerType = "vector";
            }

            if (containerType != null) {
                // these container are declared the same except the name
                systemInc.add(containerType);
                sb.append(stdPrefix).append(containerType).append("< ");
                if (modifier.indexOf('&') != -1) {
                    LOG.warn("cannot generate STL container "
                            + "with references, using pointers");
                    modifier = "*";
                }
                sb.append(type).append(modifier);
                sb.append(" > ").append(name);
            }
        }
        return sb.toString();
    }
    
    /** 
     * Load configurable parameters.
     * TODO: Why is this public? - tfm
     */
    public void loadConfig() {
        int indWidth = Configuration.getInteger(KEY_CPP_INDENT, 4);
        char[] ind = new char[indWidth];
        for (int i = 0; i < indWidth; i++)
            ind[i] = ' ';
        this.indent = new String(ind);
        lfBeforeCurly =
            Configuration.getBoolean(KEY_CPP_LF_BEFORE_CURLY, false);
        verboseDocs = Configuration.getBoolean(KEY_CPP_VERBOSE_COMM, false);
        int useSect = Configuration.getInteger(KEY_CPP_SECT,
                                               Section.SECT_NORMAL);
        Section.setUseSect(useSect);
        hdrGuardUpperCase = Configuration.getBoolean(
                KEY_CPP_HEADER_GUARD_UPPERCASE, false);
        hdrGuardGUID = Configuration.getBoolean(
                KEY_CPP_HEADER_GUARD_GUID, false);
        
        int defaultInlineStyle = Configuration.getInteger(
                KEY_CPP_DEFAULT_INLINE, Inline.getDefaultDefaultStyle());
        Inline.setDefaultStyle(defaultInlineStyle);
    }

    // Methods used by Settings dialog
    
    /**
     * @return true if the generator outputs a newline before the
     * curly brace starting a class declaration.
     */
    public boolean isLfBeforeCurly() {
        return lfBeforeCurly;
    }

    /**
     * Sets whether to output a newline character before the curly
     * brace starting a class declaration.
     * @param beforeCurly true to generate the '{' on a line on its own.
     */
    public void setLfBeforeCurly(boolean beforeCurly) {
        this.lfBeforeCurly = beforeCurly;
        Configuration.setBoolean(KEY_CPP_LF_BEFORE_CURLY, beforeCurly);
    }

    /**
     * @return If the generator generates verbose comments and docs or not.
     */
    public boolean isVerboseDocs() {
        return verboseDocs;
    }
    
    /**
     * Tells whether to generate verbose comments and docs or not.
     * @param verbose true to generate more verbose comments.
     */
    public void setVerboseDocs(boolean verbose) {
        this.verboseDocs = verbose;
        Configuration.setBoolean(KEY_CPP_VERBOSE_COMM, verbose);
    }

    /**
     * @return The current width of the indentation.
     */
    public int getIndent() {
        return indent.length();
    }

    /**
     * Sets the indentation width.
     * @param indWidth The number of spaces to use for indentation.
     */
    public void setIndent(int indWidth) {
        char[] ind = new char[indWidth];
        for (int i = 0; i < indWidth; i++)
            ind[i] = ' ';
        this.indent = new String(ind);
        Configuration.setInteger(KEY_CPP_INDENT, indWidth);
    }

    /**
     * @return Whether sections are generated and how.
     * @see #setUseSect(int)
     */
    public int getUseSect() {
        return Section.getUseSect();
    }

    /**
     * Tells the generator if and how to generate sections.
     * @param use Must be one of: 
     * <ul>
     *   <li>SECT_NONE: sections are not generated;</li>
     *   <li>SECT_NORMAL: sections are generated;</li>
     *   <li>SECT_BRIEF: sections are generated, but the "do not delete..."
     *       line is skipped.</li>
     * </ul>
     */
    public void setUseSect(int use) {
        Section.setUseSect(use);
        Configuration.setInteger(KEY_CPP_SECT, use);
    }

    /**
     * @return true if the generator outputs a header guard in upper case.
     */
    public boolean isHeaderGuardUpperCase() {
        return hdrGuardUpperCase;
    }

    /**
     * Sets how to output header guard.
     * @param upperCase true to generate upper case header guard.
     */
    public void setHeaderGuardUpperCase(boolean upperCase) {
        this.hdrGuardUpperCase = upperCase;
        Configuration.setBoolean(KEY_CPP_HEADER_GUARD_UPPERCASE, upperCase);
    }
    
    /**
     * @return true if the generator should add GUID to header guard.
     */
    public boolean isHeaderGuardGUID() {
        return hdrGuardGUID;
    }

    /**
     * Sets how to output header guard.
     * @param addGUID true to add GUID to header guard.
     */
    public void setHeaderGuardGUID(boolean addGUID) {
        this.hdrGuardGUID = addGUID;
        Configuration.setBoolean(KEY_CPP_HEADER_GUARD_GUID, addGUID);
    }

    public int getDefaultInlineStyle() {
        return Inline.getDefaultStyle();
    }

    public void setDefaultInlineStyle(int inline) {
        Inline.setDefaultStyle(inline);
        Configuration.setInteger(KEY_CPP_DEFAULT_INLINE, inline);
    }
    
    /*
     * Set of already-generated classifiers.
     */
    private Set generatedFiles = null;
    /*
     * Number of calls to startGenerateFile non closed.
     */
    private int generateRecur = 0;

    /* implementation of CodeGenerator */
    
    /*
     *  Start generating files. Needed to track dependencies.
     */
    private void startFileGeneration() {
        if (generateRecur++ == 0) {
            generatedFiles = new HashSet();
        }
    }
    
    /*
     * End generating files. Needed to track dependencies.
     */
    private void endFileGeneration() {
        if (--generateRecur == 0) {
            generatedFiles = null;
        }
    }
    
    /*
     * Generate files for element 'o' (and dependencies, eventually).
     * Return the collection of files (as Strings).
     * Do nothing (and return an empty collection) it 'o' is in generatedFiles.
     */
    private Collection generateFilesForElem(Object o,
                                            String path, boolean deps) {
        Vector ret = new Vector();
        if (generatedFiles.contains(o)) {
            return ret; // generated already
        }
        if (!getFacade().isAClass(o) && !getFacade().isAInterface(o)) {
            return ret; // not a class or interface
        }
        while (isAInnerClass(o)) {
            o = getFacade().getNamespace(o);
        }
        String pathname = null;

        // use unique section for both passes -> allow move of
        // normal function body to inline and vice versa
        if (Section.getUseSect() != Section.SECT_NONE) {
            sect = new Section();

            /*
             * 2002-11-28 Achim Spangler
             * first read header and source file into global/unique section
             */
            for (generatorPass = HEADER_PASS;
                 generatorPass <= SOURCE_PASS;
                 generatorPass++) {
                pathname = createDirectoriesPathname(o, path);
                //String pathname = path + filename;
                // TODO: package, project basepath, tagged values to configure
                File f = new File(pathname);
                if (f.exists()) {
                    LOG.info("Generating (updated) " + f.getPath());
                    sect.read(pathname);
                    File bakFile = new File(pathname + ".bak");
                    if (bakFile.exists()) {
                        bakFile.delete();
                    }
                    f.renameTo(bakFile);
                } else {
                    LOG.info("Generating (new) " + f.getPath());
                }
            }
        }

        Set dependencies = null;
        if (deps) dependencies = new TreeSet();
        /**
         * 2002-11-28 Achim Spangler
         * run basic generation function two times for header and implementation
         */
        for (generatorPass = HEADER_PASS;
             generatorPass <= SOURCE_PASS;
             generatorPass++) {
            pathname = createDirectoriesPathname(o, path);
            String fileContent = generateFileAsString(o, pathname);
            if (fileContent.length() == 0) continue;
            BufferedWriter fos = null;
            //String pathname = path + filename;
            // TODO: package, project basepath, tagged values to configure
            File f = new File(pathname);
            try {
                // TODO: This is using the default platform character encoding
                // specifying an encoding will produce more predictable results
                fos = new BufferedWriter (new FileWriter (f));
                writeTemplate(o, path, fos);
                fos.write(fileContent);
                fos.newLine();
            }
            catch (IOException exp) { }
            finally {
                try {
                    if (fos != null) fos.close();
                }
                catch (IOException exp) {
                    LOG.error("FAILED: " + f.getPath());
                }
            }

            LOG.info("written: " + pathname);

            if (Section.getUseSect() != Section.SECT_NONE) {
                // output lost sections only in the second path
                // -> sections which are moved from header(inline) to source
                // file are prevented to be outputted in header pass
                File outFile = new File(pathname + ".out");
                if (outFile.exists()) {
                    outFile.delete(); // remove junk
                }
                if (generatorPass == HEADER_PASS)   {
                    sect.write(pathname, indent, false);
                } else {
                    sect.write(pathname, indent, true);
                }

                if (outFile.exists()) {
                    assert f.exists();
                    f.delete();
                    outFile.renameTo(f);
                    LOG.info("added sections to: " + pathname);
                }
            }
            LOG.info("----- end updating " + pathname + "-----");
            ret.add(pathname);
            if (deps) {
                dependencies.add(includeCls);
                dependencies.add(predeclCls);
            }
        }
        cleanupGenerator();
        // reset generator pass to NONE for the notation to be correct
        generatorPass = NONE_PASS;
        generatedFiles.add(o);
        if (deps) {
            Iterator it = dependencies.iterator();
            while (it.hasNext()) {
                ret.add(generateFilesForElem(it.next(), path, deps));
            }
        }
        
        return ret;
    }
    
    /*
     * @see org.argouml.uml.generator.CodeGenerator#generate(java.util.Collection, boolean)
     */
    public Collection generate(Collection elements, boolean deps) {
        List ret = new ArrayList();
        startFileGeneration();
        for (Iterator it = elements.iterator(); it.hasNext(); ) {
            Object elem = it.next();
            String path = generatePath(elem);
            Set dependencies = null;
            if (deps) dependencies = new TreeSet();

            for (generatorPass = HEADER_PASS;
                 generatorPass <= SOURCE_PASS;
                 generatorPass++) {
                String name = getFacade().getName(elem) + getFileExtension();
                String content = generateFileAsString(elem, path + name);
                SourceUnit su = new SourceUnit(name, path, content);
                ret.add(su);
            }
            generatorPass = NONE_PASS;
            generatedFiles.add(elem);
            if (deps) {
                ret.add(generate(dependencies, deps));
            }
        }
        endFileGeneration();
        return ret;
    }

    /*
     * @see org.argouml.uml.generator.CodeGenerator#generateFiles(java.util.Collection, java.lang.String, boolean)
     */
    public Collection generateFiles(Collection elements, String path,
            boolean deps) {
        List ret = new ArrayList();
        startFileGeneration();
        for (Iterator it = elements.iterator(); it.hasNext(); ) {
            Object elem = it.next();
            ret.addAll(generateFilesForElem(elem, path, deps));
        }
        endFileGeneration();
        return ret;
    }

    /*
     * @see org.argouml.uml.generator.CodeGenerator#generateFileList(java.util.Collection, boolean)
     */
    public Collection generateFileList(Collection elements, boolean deps) {
        List ret = new ArrayList();
        startFileGeneration();
        for (Iterator it = elements.iterator(); it.hasNext(); ) {
            Object elem = it.next();
            // FIXME: check for interfaces, inner classes, deps, etc. 
            ret.add(getFacade().getName(elem) + ".cpp");
            ret.add(getFacade().getName(elem) + ".h");
        }
        endFileGeneration();
        return null;
    }
    
    private static String generateUninterpreted(String un) {
        if (un == null)
            return "";
        return un;
    }

    private static String generateClassifierRef(Object cls) {
        if (cls == null)
            return "";
        return getFacade().getName(cls);
    }
    
    /**
     * 
     * @param cls object
     * @return String with generated methods
     */
    private String generateInlinedMethodsOutsideClass(Object cls) {
        String s = new String();
        Collection op = getFacade().getOperations(cls);
        if (op.isEmpty()) {
            return null;
        }

        s = LINE_SEPARATOR;
        if (verboseDocs) {
            s += indent + "// Operations" + LINE_SEPARATOR;
        }
        
        int tmpGeneratorPass = generatorPass;
        generatorPass = SOURCE_PASS;
        
        String opString = new String();
        Iterator opIterator = op.iterator();
        while (opIterator.hasNext()) {
            Object bf = opIterator.next();
        
            if (!getFacade().isAbstract(bf)) {
                Inline inlineStyle = Inline.getInlineOperationModifierType(bf);
                if (inlineStyle.isMethodBodyOutsideClass()) {
                    opString += generateOperation(bf, false);
                    opString += LINE_SEPARATOR + generateMethodBody(bf)
                                + LINE_SEPARATOR;
                }
            }
        } // end loop through all operations
        
        generatorPass = tmpGeneratorPass;
        
        if (opString.length() != 0) {
            s += opString;
            return s;
        } else {
            return null;
        }
    }
    
} /* end class GeneratorCpp */
