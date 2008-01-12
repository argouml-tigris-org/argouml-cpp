// $Id$
// Copyright (c) 1996-2007 The Regents of the University of California. All
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.argouml.language.cpp.Helper;
import org.argouml.language.cpp.profile.ProfileCpp;
import org.argouml.model.Model;
import static org.argouml.model.Model.*;
import org.argouml.model.UUIDManager;

/**
 * Tests for GeneratorCpp file generation functionalities, i.e., generateFile2
 * method.
 * 
 * @see GeneratorCpp
 * @author euluis
 * @since 0.17.3
 */
public class TestCppFileGeneration extends BaseTestGeneratorCpp {

    /** The Logger for this class */
    private static final Logger LOG = Logger.getLogger(TestGeneratorCpp.class);

    /**
     * System newline separator.
     */
    private static final String LINE_SEPARATOR = System
            .getProperty("line.separator");

    /**
     * Creates a new instance of TestCppFileGeneration.
     * 
     * @param testName
     *            name of the test
     */
    public TestCppFileGeneration(String testName) {
        super(testName);
    }

    /**
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(TestCppFileGeneration.class);
        return suite;
    }

    /**
     * Enables debugging in IDEs that don't support debugging unit tests...
     * 
     * @param args
     *            the arguments given on the commandline
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Directory to be deleted on tearDown if not null.
     */
    private File genDir;

    /**
     * The otherPack package model element used in some tests.
     */
    private Object otherPack;

    /**
     * The OtherClass class, contained in otherpack, also used in some of the
     * tests.
     */
    private Object otherClass;

    /**
     * Setup some of the commonly used objects.
     * @see org.argouml.language.cpp.generator.BaseTestGeneratorCpp#setUp()
     * @throws Exception if something goes wrong.
     */
    protected void setUp() throws Exception {
        super.setUp();
        String packageName = "pack";
        Object aPackage = Model.getModelManagementFactory().buildPackage(
                packageName, UUIDManager.getInstance().getNewUUID());
        Model.getCoreHelper().setNamespace(getAClass(), aPackage);
    }

    /**
     * @throws IOException error deleting directory
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws IOException {
        Helper.deleteDir(genDir);
    }

    /**
     * Get a file generated for the single modelelement elem, with extension
     * 'ext' (either ".cpp" or ".h").
     * @param elem model element
     * @param ext extension of the generated file
     * @return the generated File
     */
    private File generateFile(Object elem, String ext) {
        Vector v = new Vector();
        v.add(elem);
        Collection files = getGenerator().generateFiles(v, genDir.getPath(),
                false);
        assertFalse(files.isEmpty());
        File genFile = null;
        for (Iterator it = files.iterator(); it.hasNext();) {
            String fn = (String) it.next();
            if (fn.endsWith(ext)) {
                genFile = new File(fn);
                break;
            }
        }
        return genFile;
    }

    /**
     * Test for file generation of a classifier, checking if in the second time
     * the empty method implementation is generated correctly (no extra curly
     * braces each time the class is generated - issue 2828) and the added code
     * is preserved.
     * 
     * @throws IOException
     *             some unexpected file access problem occurred
     */
    public void testGenerateAfterModifyAndIssue2828() throws IOException {
        genDir = Helper.setUpDir4Test("testIssue2828");
        // enable sections, in case they were disabled
        getGenerator().setUseSect(Section.SECT_NORMAL);
        // generate the classifier for the first time in temp dir
        File genFile = generateFile(getAClass(), ".cpp");
        // create some content in the foo method implementation
        String encoding = getEncoding(genFile);
        String originalGenerated = FileUtils
                .readFileToString(genFile, encoding);
        StringBuffer modified = new StringBuffer();
        // look for the implementation
        int fooImplIndex = originalGenerated.indexOf("::foo(");
        assertTrue(fooImplIndex != -1);
        // now look for the opening brace...
        fooImplIndex = originalGenerated.indexOf('{', fooImplIndex);
        assertTrue(fooImplIndex != -1);
        // insert some dummy content
        modified.append(originalGenerated.substring(0, ++fooImplIndex));
        String dummyContent = LINE_SEPARATOR + "\tint i = 0; // dummy content";
        modified.append(dummyContent);
        modified.append(originalGenerated.substring(fooImplIndex));
        genFile.delete();
        FileUtils.writeStringToFile(genFile, modified.toString(), encoding);

        // generate the classifier for the second time
        File newFile = generateFile(getAClass(), ".cpp");
        assertEquals(genFile, newFile);
        // check if the file generated in the second time is equal to the
        // modified file
        String secondGenerated = FileUtils.readFileToString(genFile, encoding);
        assertEquals(modified.toString(), secondGenerated);
    }

    /**
     * Test that the model isn't used as a namespace - issue #2963.
     * 
     * @throws IOException
     *             some unexpected file access problem occurred
     */
    public void testModelIsNotNamespace() throws IOException {
        final String testName = "testModelIsNotNamespace";
        setUpNamespaces(testName);

        String generated = generateAClassFile(testName);
        String modelNs = "namespace " + Model.getFacade().getName(getModel())
                + " {";
        assertTrue(generated.indexOf(modelNs) == -1);
        String packNs = "namespace " + Model.getFacade().getName(getPack())
                + " {";
        assertTrue(generated.indexOf(packNs) != -1);
    }

    /**
     * The model name shouldn't be used as namespace for types of arguments,
     * return values or attributes.
     * 
     * @throws IOException
     *             when file access goes wrong
     */
    public void testModelNameNotUsedForTypeNamespace() throws IOException {
        final String testName = "testModelNameNotUsedForTypeNamespace";

        setUpNamespaces(testName);
        setUpOtherClassInOtherPackage();
        createAClassOperationWithOtherClassAsParamAndReturn();

        String generatedCpp = generateAClassFile(testName);

        String correctName = Model.getFacade().getName(otherPack) + "::"
                + Model.getFacade().getName(otherClass);
        assertTrue(generatedCpp.indexOf(correctName) != -1);
        String wrongName = Model.getFacade().getName(getModel()) + "::"
                + correctName;
        assertTrue(generatedCpp.indexOf(wrongName) == -1);
    }

    /**
     * Create an operation of AClass.
     */
    private void createAClassOperationWithOtherClassAsParamAndReturn() {
        Object gee = buildOperation(getAClass(), otherClass, "gee");
        getFactory().buildParameter(gee, otherClass);
    }

    /**
     * Generate the source file (cpp or h) for the AClass class and return it as
     * String.
     * 
     * @param testName
     *            name of the test calling this method
     * @param header
     *            if true then generate header, else generate cpp
     * @return the generated source file as String
     * @throws IOException
     *             if something goes wrong with file access
     */
    private String generateAClassFile(String testName, boolean header)
        throws IOException {
        genDir = Helper.setUpDir4Test(testName);
        File genFile = generateFile(getAClass(), header ? ".h" : ".cpp");
        assertNotNull(genFile);
        assertTrue(genFile.exists());

        String encoding = getEncoding(genFile);
        String generated = FileUtils.readFileToString(genFile, encoding);
        return generated;
    }

    /**
     * Generate the source file (cpp) for the AClass class and return it as
     * String.
     * 
     * @param testName
     *            name of the test calling this method
     * @return the generated cpp file as String
     * @throws IOException
     *             if something goes wrong with file access
     */
    private String generateAClassFile(String testName) throws IOException {
        return generateAClassFile(testName, false);
    }

    /**
     * Create OtherClass class in otherpack package.
     */
    private void setUpOtherClassInOtherPackage() {
        otherPack = Model.getModelManagementFactory().buildPackage("otherpack",
                UUIDManager.getInstance().getNewUUID());
        Model.getCoreHelper().setNamespace(otherPack, getModel());
        otherClass = getFactory().buildClass("OtherClass", otherPack);
    }

    /**
     * Setup the namespaces, giving a name to the model and assigning the model
     * as the namespace of the pack package.
     * 
     * @param modelName
     *            name to give to the model
     */
    private void setUpNamespaces(String modelName) {
        Model.getCoreHelper().setName(getModel(), modelName);
        Model.getCoreHelper().setNamespace(getPack(), getModel());
    }

    /**
     * @return the package pack
     */
    private Object getPack() {
        return Model.getFacade().getModelElementContainer(getAClass());
    }

    /**
     * Retrieve the encoding for the given file.
     * 
     * @param f
     *            the file for which the encoding is wanted
     * @return the file encoding as a string
     * @throws IOException
     *             if something goes wrong in file access
     */
    private static String getEncoding(File f) throws IOException {
        FileReader fin = new FileReader(f);
        String encoding = fin.getEncoding();
        fin.close();
        return encoding;
    }

    /**
     * Test that headers are wrapped into #ifndef ... #endif
     * 
     * @throws IOException
     *             if something goes wrong with file access
     */
    public void testMultipleInclusionGuardAndIssue3053() throws IOException {
        final String testName = "testMultipleInclusionGuardAndIssue3053";
        setUpNamespaces(testName);
        
        /**
         * Don't test headers with header guard GUID option enabled
         * because test always will be failing (GUIDs will be different).
         */
        getGenerator().setHeaderGuardGUID(false); 
        
        String genH = generateAClassFile(testName, true).trim();
        String guard = Model.getFacade().getName(getPack()) + "_"
                + Model.getFacade().getName(getAClass()) + "_h";
       
        if (getGenerator().isHeaderGuardUpperCase()) {
            guard = guard.toUpperCase();
        }
        
        String re = "(?m)(?s)\\s*#\\s*ifndef\\s+" + guard
                + "\\s*^\\s*#\\s*define\\s+" + guard + "\\s.*^\\s*#\\s*endif"
                + "\\s*//\\s+" + guard + ".*";
        
        if (!genH.matches(re)) {
            LOG.info("generated header was:\n" + genH);
        }
        assertTrue(genH.matches(re));
    }

    /**
     * 
     */
    private void setUpOtherClassInSamePackage() {
        otherClass = getFactory().buildClass("OtherClass", getPack());
    }

    /**
     * Tests that #includes in other packages are generated as #include
     * <package/Class.h> (issue 3086). Also, test that #includes are generated
     * for parameter types (issue 3149).
     * 
     * @throws IOException
     *             if something goes wrong with file access
     */
    public void testExternalIncludes() throws IOException {
        final String testName = "testExternalIncludes";
        setUpNamespaces(testName);
        setUpOtherClassInOtherPackage();
        createAClassOperationWithOtherClassAsParamAndReturn();
        // AClass uses otherPack/OtherClass
        String genH = generateAClassFile(testName, true);
        String re = "(?m)(?s).*\\s*#\\s*include\\s*"
                + "\\<otherpack/OtherClass\\.h\\>\\s*.*";
        if (!genH.matches(re)) {
            LOG.info("generated header was:\n" + genH);
        }
        assertTrue(genH.matches(re));
    }

    /**
     * Tests that #includes in the same package or subpackages are generated as
     * #include "subpack/Class.h" (issue 3086).
     * 
     * @throws IOException
     *             if something goes wrong with file access
     */
    public void testLocalIncludes() throws IOException {
        final String testName = "testLocalIncludes";
        setUpNamespaces(testName);
        setUpOtherClassInSamePackage();
        createAClassOperationWithOtherClassAsParamAndReturn();
        // AClass uses pack/OtherClass
        String genH = generateAClassFile(testName, true);
        String re = "(?m)(?s).*\\s*#\\s*include\\s*\"OtherClass\\.h\"\\s*.*";
        if (!genH.matches(re)) {
            LOG.info("generated header was:\n" + genH);
        }
        assertTrue(genH.matches(re));
        // move OtherClass to pack/otherpack/OtherClass
        otherPack = Model.getModelManagementFactory().buildPackage("otherpack",
                UUIDManager.getInstance().getNewUUID());
        Model.getCoreHelper().setNamespace(otherPack, getPack());
        Model.getCoreHelper().setNamespace(otherClass, otherPack);
        genH = generateAClassFile(testName, true);
        re = "(?m)(?s).*\\s*#\\s*include\\s*\"otherpack/OtherClass\\.h\"\\s*.*";
        if (!genH.matches(re)) {
            LOG.info("generated header was:\n" + genH);
        }
        assertTrue(genH.matches(re));
    }

    /**
     * Changes to the model sub-system implementation make it impossible to 
     * set the TD of a TV to null, therefore, I'm adapting the test to check 
     * that this is kept in place!
     */
    public void testSetNullTagDefinition4TVIsDetectedIssue4393() {
        Object documentationTV = Model.getExtensionMechanismsFactory()
                .buildTaggedValue(
                    ProfileCpp.getTagDefinition(
                        ProfileCpp.TV_NAME_DOCUMENTATION), 
                    new String[] {"docs"});
        Object documentationTD = Model.getFacade().getTagDefinition(
                documentationTV);
        assertNotNull(documentationTD);
        Model.getExtensionMechanismsHelper().setType(documentationTV, null);
    }
    
    /**
     * It is possible to have a TD of a TV null. Test that the code 
     * generator will not throw NPE when generating an operation.
     * 
     * @throws IOException when something goes wrong with the file
     */
    public void testOperationWithNullTaggedValueTagIssue4393() 
        throws IOException {
    	final String testName = 
    		"testOperationWithNullTaggedValueTagIssue4393";
    	setUpNamespaces(testName);
    	assertGenerateAClassFileWithNullTaggedValueTag(testName, 
    			getFooMethod());
    }
    
    private void assertGenerateAClassFileWithNullTaggedValueTag(
            final String testName, Object me) throws IOException {
        Object documentationTV = getExtensionMechanismsFactory()
                .buildTaggedValue(
                    ProfileCpp.getTagDefinition(
                        ProfileCpp.TV_NAME_DOCUMENTATION), 
                    new String[] {"docs"});
        getExtensionMechanismsHelper().addTaggedValue(me, documentationTV);
    	assertEquals(1, 
    			Model.getFacade().getTaggedValuesCollection(me).size());
    	assertEquals("documentation", 
    			Model.getFacade().getTag(documentationTV));
    	Object documentationTD = Model.getFacade().getTagDefinition(
    			documentationTV);
    	assertNotNull(documentationTD);
    	Model.getExtensionMechanismsHelper().setType(documentationTV, null);
    	assertNull(Model.getFacade().getTagDefinition(documentationTV));
    	generateAClassFile(testName, true);
    }
    
    /**
     * It is possible to have a TD of a TV null. Test that the code 
     * generator will not throw NPE when generating a class.
     * 
     * @throws IOException when something goes wrong with the file.
     */
    public void testClassWithNullTaggedValueTagIssue4393() 
        throws IOException {
    	final String testName = "testClassWithNullTaggedValueTagIssue4393";
    	setUpNamespaces(testName);
    	assertGenerateAClassFileWithNullTaggedValueTag(testName, getAClass());
    }
}
