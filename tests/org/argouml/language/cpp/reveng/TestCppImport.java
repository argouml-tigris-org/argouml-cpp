// $Id$
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

package org.argouml.language.cpp.reveng;

import static org.argouml.language.cpp.Helper.createProject;
import static org.argouml.language.cpp.Helper.deleteCurrentProject;
import static org.argouml.model.Model.getFacade;
import static org.argouml.model.Model.getCoreHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.FileUtils;
import org.argouml.kernel.Project;
import org.argouml.language.cpp.profile.ProfileCpp;
import org.argouml.model.Model;
import org.argouml.taskmgmt.ProgressEvent;
import org.argouml.taskmgmt.ProgressMonitor;
import org.argouml.uml.reveng.ImportSettings;

/**
 * Tests the {@link CppImport} class.
 *
 * NOTE: this is more like a module test, since here we also test the
 * {@link Modeler} implementation.
 *
 * FIXME: duplicate code from TestCppFileGeneration and BaseTestGeneratorCpp.
 *
 * @author Luis Sergio Oliveira (euluis)
 * @since 0.19.3
 * @see CppImport
 */
public class TestCppImport extends TestCase {

    /**
     * Constructor.
     *
     * @param testName The name of the test.
     */
    public TestCppImport(String testName) {
        super(testName);
    }

    /**
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(TestCppImport.class);
        return suite;
    }

    /**
     * Enables debugging in IDEs that don't support debugging unit tests...
     *
     * @param args the arguments given on the commandline
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * System temporary directory property name.
     */
    static final String SYSPROPNAME_TMPDIR = "java.io.tmpdir";

    /**
     * Path of the temporary directory in the system.
     */
    private File tmpDir;

    /**
     * Directory to be deleted on tearDown if not null.
     */
    private File genDir;

    /**
     * The ArgoUML C++ reveng module.
     */
    private CppImport cppImp;

    /**
     * The ArgoUML project.
     */
    private Project proj;


    /**
     * The import settings to be used for tests.
     */
    private ImportSettings settings; 

    /*
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        tmpDir = new File(System.getProperty(SYSPROPNAME_TMPDIR));
        proj = createProject();
        cppImp = new CppImport();
        settings = new DummySettings();
    }

    /**
     * <p>IMPORTANT: if you get a failure inline with the following:
     * java.io.IOException: Unable to delete file:</p><p> 
     * D:\tmp\testIssue0006DeleteFileFails\issue0006_test_preprocessed2.cpp<br>
     *  at org.apache.commons.io.FileUtils.forceDelete(FileUtils.java:659)<br> 
     *  at org.apache.commons.io.FileUtils.cleanDirectory(FileUtils.java:540) 
     *  <br> 
     *  at org.apache.commons.io.FileUtils.deleteDirectory(FileUtils.java:509) 
     *  <br> 
     *  at org.argouml.language.cpp.reveng.TestCppImport.tearDown(
     *  TestCppImport.java:139)</p>
     * <p>It is because either: </p>
     * <ul>
     * <li>you must add the file copy to the Ant's script 
     * task copy-tests-resources;</li>
     * <li>or the file was still open by some of the objects that interacted 
     * with it during the tests.</li>
     * </ul>
     * 
     * @throws Exception When there is a problem in deleting the directory.
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        if (genDir != null && genDir.exists()) {
            FileUtils.deleteDirectory(genDir);
        }
        deleteCurrentProject();
        super.tearDown();
    }

    /**
     * Setup a directory with the given name for the caller test.
     *
     * @param dirName the directory to be created in the system temporary dir
     * @return the created directory
     */
    private File setUpDirectory4Test(String dirName) {
        File generationDir = new File(tmpDir, dirName);
        generationDir.mkdirs();
        return generationDir;
    }

    /**
     * Simple test for the <code>CppImport.parseFile(xxx)</code> method, using
     * the SimpleClass.cpp source file.
     *
     * @throws Exception something went wrong
     */
    public void testParseFileSimpleClass() throws Exception {
        genDir = setUpDirectory4Test("testParseFileSimpleClass");
        File srcFile = setupSrcFile4Reverse("SimpleClass.cpp");
        Collection<File> files = new ArrayList<File>();
        files.add(srcFile);
        
        cppImp.parseFiles(proj, files, settings, new DummyMonitor());

        Collection nss =
            Model.getModelManagementHelper().getAllNamespaces(getRootModel());
        Object pack = findModelElementWithName(nss, "pack");
        assertNotNull("The pack namespace wasn't found in the model!", pack);

        Collection clss = Model.getCoreHelper().getAllClasses(pack);
        Object simpleClass = findModelElementWithName(clss, "SimpleClass");
        assertNotNull("The pack::SimpleClass class wasn't found in the model!",
            simpleClass);
        assertTrue(Model.getFacade().isPublic(simpleClass));

        Collection opers =
            Model.getCoreHelper().getBehavioralFeatures(simpleClass);
        Object newOperation = findModelElementWithName(opers, "newOperation");
        assertNotNull("The pack::SimpleClass::newOperation() model element "
            + "doesn't exist!", newOperation);
        assertTrue(Model.getFacade().isPublic(newOperation));

        Collection attrs = Model.getCoreHelper().getAllAttributes(simpleClass);
        Object newAttr = findModelElementWithName(attrs, "newAttr");
        assertNotNull(
            "The pack::SimpleClass::newAttr attribute doesn't exist!", newAttr);
        assertTrue(Model.getFacade().isPublic(newAttr));
        // TODO: verify reveng of SimpleClass.newOperation definition
        // TODO: 1. how to model a namespace alias in UML? A variation of the
        // import or access relationship would be nice, but, I don't know if
        // that would be to force things a bit...
        // TODO: 2. verify namespace alias p = pack.
    }

    /**
     * Ditto for DerivedFromAbstract.cxx translation unit.
     *
     * @throws Exception something wen't wrong
     */
    public void testParseFileDerivedFromAbstract() throws Exception {
        genDir = setUpDirectory4Test("testParseFileDerivedFromAbstract");
        File srcFile = setupSrcFile4Reverse("DerivedFromAbstract.cxx");
        Collection<File> files = new ArrayList<File>();
        files.add(srcFile);
        cppImp.parseFiles(proj, files, settings, new DummyMonitor());

        // verify the Dummy struct reveng
        Collection classes = Model.getCoreHelper().getAllClasses(
            getRootModel());
        Object dummyStruct = findModelElementWithName(classes, "Dummy");
        assertNotNull("The Dummy structure doesn't exist in the model!",
            dummyStruct);
        assertHasTaggedValueWithValue(dummyStruct,
            ProfileCpp.TV_NAME_CLASS_SPECIFIER, "struct");

        Collection attributes =
            Model.getCoreHelper().getAllAttributes(dummyStruct);
        Object c = findModelElementWithName(attributes, "c");
        assertNotNull("The Dummy::c attribute doesn't exist in the model!", c);
        assertTrue("Dummy::c must be public!", Model.getFacade().isPublic(c));
        Object signedChar = Model.getFacade().getType(c);
        assertEquals("signed char", Model.getFacade().getName(signedChar));
        assertTrue("signed char must be a DataType!", Model.getFacade()
                .isADataType(signedChar));

        // verify Base reveng
        Object baseClass = findModelElementWithName(classes, "Base");
        assertNotNull("The Base class doesn't exist in the model!", baseClass);
        assertNull(Model.getFacade().getTaggedValue(baseClass,
            ProfileCpp.TV_NAME_CLASS_SPECIFIER));

        Collection opers =
            Model.getCoreHelper().getBehavioralFeatures(baseClass);
        Object baseFooOper = findModelElementWithName(opers, "foo");
        assertNotNull(
            "The Base::foo(xxx) operation doesn't exist in the model!",
            baseFooOper);
        assertTrue(Model.getFacade().isAbstract(baseFooOper));
        Collection returnParams = 
            Model.getCoreHelper().getReturnParameters(baseFooOper);
        assertEquals("Unexpected number of return parameters", 
                1, returnParams.size());
        Object baseFooRv = returnParams.iterator().next();
        assertEquals("unsigned int", Model.getFacade().getName(
            Model.getFacade().getType(baseFooRv)));
        Collection params = Model.getFacade().getParameters(baseFooOper);
        Object baseFooOtherParam = findModelElementWithName(params, "other");
        assertNotNull(baseFooOtherParam);
        assertEquals(baseClass, Model.getFacade().getType(baseFooOtherParam));
        assertHasTaggedValueWithValue(baseFooOtherParam,
            ProfileCpp.TV_NAME_REFERENCE, "true");
        assertEquals("inout", Model.getFacade().getName(
            Model.getFacade().getKind(baseFooOtherParam)));

        attributes = Model.getCoreHelper().getAllAttributes(baseClass);
        Object baseUiAttr = findModelElementWithName(attributes, "ui");
        assertNotNull("The Base::ui attribute doesn't exist in the model!",
            baseUiAttr);
        assertTrue(Model.getFacade().isProtected(baseUiAttr));
        assertEquals("unsigned long", Model.getFacade().getName(
            Model.getFacade().getType(baseUiAttr)));

        Object baseMakeMeADummyOper =
            findModelElementWithName(opers, "makeMeADummy");
        assertNotNull(
            "The Base::makeMeADummy() operation doesn't exit in the model!",
            baseMakeMeADummyOper);
        assertTrue(Model.getFacade().isProtected(baseMakeMeADummyOper));
        
        returnParams = 
            Model.getCoreHelper().getReturnParameters(baseMakeMeADummyOper);
        assertEquals("Unexpected number of return parameters", 1, returnParams
                .size());
        assertEquals(dummyStruct, 
                Model.getFacade().getType(returnParams.iterator().next()));

        Object baseHelperMethodOper =
            findModelElementWithName(opers, "helperMethod");
        assertNotNull(
            "The Base::helperMethod(xxx) operation doesn't exist in the model!",
            baseHelperMethodOper);
        returnParams = Model.getCoreHelper().getReturnParameters(
                baseHelperMethodOper);
        assertEquals("Unexpected number of return parameters", 1, 
                returnParams.size());
        assertEquals("void", Model.getFacade()
                .getName(
                    Model.getFacade().getType(returnParams.iterator().next())));
        assertTrue(Model.getFacade().isPrivate(baseHelperMethodOper));
        params = Model.getFacade().getParameters(baseHelperMethodOper);
        Object baseHelperMethodCstrParam =
            findModelElementWithName(params, "cstr");
        assertNotNull("Base::helperMethod(xxx) cstr parameter doesn't exist!",
            baseHelperMethodCstrParam);
        assertEquals("signed char", Model.getFacade().getName(
            Model.getFacade().getType(baseHelperMethodCstrParam)));
        assertHasTaggedValueWithValue(baseHelperMethodCstrParam,
            ProfileCpp.TV_NAME_POINTER, "true");
        assertEquals("inout", Model.getFacade().getName(
            Model.getFacade().getKind(baseHelperMethodCstrParam)));

        // verify Derived reveng
        Object derivedClass = findModelElementWithName(classes, "Derived");
        assertNotNull("The Derived class doesn't exist in the model!",
            derivedClass);
        assertNull(Model.getFacade().getTaggedValue(derivedClass,
            ProfileCpp.TV_NAME_CLASS_SPECIFIER));
        // verify generalization relationship
        Collection derivedGeneralizations =
            Model.getFacade().getGeneralizations(derivedClass);
        assertEquals(1, derivedGeneralizations.size());
        Object baseGeneralization = derivedGeneralizations.iterator().next();
        assertNotNull("The Base generalization wasn't found!",
            baseGeneralization);
        assertEquals("Derived", Model.getFacade().getName(
            Model.getFacade().getSpecific(baseGeneralization)));
        assertEquals("Base", Model.getFacade().getName(
            Model.getFacade().getGeneral(baseGeneralization)));
        assertEquals("false", Model.getFacade().getTaggedValueValue(
            baseGeneralization, ProfileCpp.TV_NAME_VIRTUAL_INHERITANCE));
        assertHasTaggedValueWithValue(baseGeneralization, 
            ProfileCpp.TV_NAME_INHERITANCE_VISIBILITY, "public");
        // verify Derived constructor
        Collection derivedOpers =
            Model.getCoreHelper().getBehavioralFeatures(derivedClass);
        Object derivedCtor = findModelElementWithName(derivedOpers, "Derived");
        assertNotNull("The Derived constructor wasn't found!", derivedCtor);
        Collection derivedCtorStereotypes =
            Model.getFacade().getStereotypes(derivedCtor);
        assertNotNull(
                findModelElementWithName(derivedCtorStereotypes, "create"));
        // verify Derived destructor
        Object derivedDtor = findModelElementWithName(derivedOpers, 
                "~Derived");
        assertNotNull("The Derived destructor wasn't found!", derivedDtor);
        Collection derivedDtorStereotypes =
            Model.getFacade().getStereotypes(derivedDtor);
        assertNotNull(findModelElementWithName(derivedDtorStereotypes,
            "destroy"));
        Object derivedFooOper = findModelElementWithName(derivedOpers, "foo");
        assertNotNull(
            "The Derived::foo(xxx) operation doesn't exist in the model!",
            derivedFooOper);
        assertFalse("The Derived::foo(xxx) operation shouldn't be leaf.", 
            Model.getFacade().isLeaf(derivedFooOper));

        // TODO: function bodies as UML Methods
    }

    private Object getRootModel() {
        return Model.getModelManagementFactory().getRootModel();
    }

    /**
     * Assert that a tagged value exists in a model element and that its value
     * is equal to the given value.
     *
     * @param me the model element to check
     * @param tvName name of the tagged value
     * @param tvValue value of the tagged value
     */
    private void assertHasTaggedValueWithValue(Object me, String tvName,
            Object tvValue) {
        Object tv = Model.getFacade().getTaggedValue(me, tvName);
        assertNotNull("The tagged value " + tvName
            + " doesn't exist for the model element \"" 
            + Model.getFacade().getName(me) + "\".", tv);
        assertEquals("Unexpected value of the tagged value \"" + tvName 
            + "\" of model element \"" + Model.getFacade().getName(me) + "\".",
            tvValue, Model.getFacade().getValueOfTag(tv));
    }

    /**
     * When a file must be reversed, it must be open by the CppImport,
     * therefore, a copy of the file name given, which is a resource in the
     * package of this class, must be prepared. The <code>File</code> object
     * for this copy is returned, with its absolute path set.
     *
     * @param fn name of the source file which exists as a resource within the
     *            package of this class
     * @return the <code>File</code> object for the copy of the source file
     * @throws IOException if there are problems finding or reading the file.
     */
    private File setupSrcFile4Reverse(String fn) throws IOException {
        InputStream in = getClass().getResourceAsStream(fn);
        File srcFile = new File(genDir, fn);
        OutputStream out = null;
        try {
            out = new FileOutputStream(srcFile);
        } catch (FileNotFoundException e) {
            in.close();
            throw e;
        }
        try {
            CopyUtils.copy(in, out);
        } finally {
            in.close();
            out.close();
        }
        return srcFile;
    }

    /**
     * Find in a <code>Collection</code> of model elements one with the
     * specified name.
     *
     * @param mes the model elements in which to search
     * @param meName simple name of the ME
     * @return the ME if found or null
     */
    private Object findModelElementWithName(Collection mes, String meName) {
        Iterator it = mes.iterator();
        Object me = null;
        while (it.hasNext()) {
            Object possibleME = it.next();
            if (meName.equals(Model.getFacade().getName(possibleME))) {
                me = possibleME;
                break;
            }
        }
        return me;
    }

    /**
     * Test two passes - call twice the
     * {@link CppImport#parseFile(Project, Object, ImportSettings)
     * CppImport.parseFile(xxx)}
     * method on the same translation unit. The model elements shouldn't get
     * duplicated.
     *
     * @throws Exception something went wrong
     */
    public void testCallParseFileTwiceCheckingNoDuplicationOfModelElements()
        throws Exception {
        genDir = setUpDirectory4Test("testParseFileSimpleClass");
        File srcFile = setupSrcFile4Reverse("SimpleClass.cpp");
        Collection<File> files = new ArrayList<File>();
        files.add(srcFile);
        
        cppImp.parseFiles(proj, files, settings, new DummyMonitor());
        // 2nd call on purpose!
        cppImp.parseFiles(proj, files, settings, new DummyMonitor());

        Collection nss =
            Model.getModelManagementHelper().getAllNamespaces(getRootModel());
        Object pack = getModelElementAndAssertNotDuplicated(nss, "pack");

        Collection clss = Model.getCoreHelper().getAllClasses(pack);
        Object simpleClass =
            getModelElementAndAssertNotDuplicated(clss, "SimpleClass");

        Collection opers =
            Model.getCoreHelper().getBehavioralFeatures(simpleClass);
        getModelElementAndAssertNotDuplicated(opers, "newOperation");

        Collection attrs = Model.getCoreHelper().getAllAttributes(simpleClass);
        getModelElementAndAssertNotDuplicated(attrs, "newAttr");
    }
    
    /**
     * A user reported this very simple example which was failing to be 
     * imported.
     * The error was caused by unfinished work in the import of constructors 
     * and destructors which should support separate definition, outside of 
     * the class definition. 
     * 
     * @throws Exception Something went wrong.
     * @see <a 
     * href="http://argouml-cpp.tigris.org/issues/show_bug.cgi?id=6">issue 
     * 6</a> 
     */
    public void testIssue0006() throws Exception {
        genDir = setUpDirectory4Test("testIssue0006");
        File srcFile = setupSrcFile4Reverse("issue0006_test_preprocessed.cpp");
        Collection<File> files = new ArrayList<File>();
        files.add(srcFile);
        
        cppImp.parseFiles(proj, files, settings, new DummyMonitor());
        Collection clss = Model.getCoreHelper().getAllClasses(getRootModel());
        Object clazzTest = getModelElementAndAssertNotDuplicated(clss, "Test");
        Collection opers =
            Model.getCoreHelper().getBehavioralFeatures(clazzTest);
        Object ctor = getModelElementAndAssertNotDuplicated(opers, "Test");
        Collection ctorStereotypes = Model.getFacade().getStereotypes(ctor);
        assertNotNull(findModelElementWithName(ctorStereotypes, "create"));
        Object dtor = getModelElementAndAssertNotDuplicated(opers, "~Test");
        Collection dtorStereotypes = Model.getFacade().getStereotypes(dtor);
        assertNotNull(findModelElementWithName(dtorStereotypes, "destroy"));
    }
    
    /**
     * TODO: test parse twice and check duplicates
     * 
     * @throws Exception when things go wrong
     */
    public void testIssue0025() throws Exception {
        genDir = setUpDirectory4Test("testIssue0025");
        File srcFile = setupSrcFile4Reverse("issue0025.cpp");
        Collection<File> files = new ArrayList<File>();
        files.add(srcFile);
        
        cppImp.parseFiles(proj, files, settings, new DummyMonitor());
        
        // check reveng of myThing typedef
        // Model in UML a typedef as a Data Type with a tagged value with the 
        // name of the type.
        // For this simple case we could simply define it as an alias for the 
        // type, i.e., int
        Object myThing = proj.findType("myThing", false);
        assertNotNull(myThing);
        assertTrue(Model.getFacade().isADataType(myThing));
        // TODO: check the stereotype and tagged value
        Object model = getRootModel();
        assertEquals(model, Model.getFacade().getNamespace(myThing));
        
        Object blablaClass = proj.findType("blabla", false);
        assertNotNull(blablaClass);
        assertEquals(model, Model.getFacade().getNamespace(blablaClass));
        List operations = Model.getFacade().getOperations(blablaClass);
        assertEquals(1, operations.size());
        Object nameOperation = operations.get(0);
        assertEquals(myThing, Model.getFacade().getType(
                Model.getFacade().getParameter(nameOperation, 0)));
    }

    /**
     * Test import of issue4932.cpp.
     * 
     * @throws Exception when things go wrong
     */
    public void testIssue4932() throws Exception {
        genDir = setUpDirectory4Test("testIssue4932");
        File srcFile = setupSrcFile4Reverse("issue4932.cpp");
        Collection<File> files = new ArrayList<File>();
        files.add(srcFile);
        
        cppImp.parseFiles(proj, files, settings, new DummyMonitor());
        Collection classes = getCoreHelper().getAllClasses(getRootModel());
        Object cBraceNode = getModelElementAndAssertNotDuplicated(classes, 
            "CBraceNode");
        assertTrue(getFacade().isAClass(cBraceNode));
        // check attributes
        Collection attributes = getCoreHelper().getAllAttributes(cBraceNode);
        assertEquals("Unexpected number of attributes.", 4, attributes.size());
        Object pnext = getModelElementAndAssertNotDuplicated(attributes, 
            "pnext");
        assertEquals("pnext's type must be CBraceNode", cBraceNode, 
            getFacade().getType(pnext));
        Collection pnextStereotypes = getFacade().getStereotypes(pnext);
        assertNotNull(findModelElementWithName(pnextStereotypes, 
            ProfileCpp.STEREO_NAME_ATTRIBUTE));
        assertEquals("Only expecting one tagged value.", 1, 
            getFacade().getTaggedValuesCollection(pnext).size());
        assertHasTaggedValueWithValue(pnext, ProfileCpp.TV_NAME_POINTER, 
            ProfileCpp.TV_TRUE_VALUE);
        String[] attributeNames = {"lineNumber", "columnNumber", "ch"};
        for (String attributeName : attributeNames) {
            Object attr = getModelElementAndAssertNotDuplicated(attributes, 
                attributeName);
            assertEquals("type must be int", "int", 
                getFacade().getName(getFacade().getType(attr)));
        }
        // check constructors
        Collection opers = getCoreHelper().getBehavioralFeatures(cBraceNode);
        List ctors = new ArrayList();
        for (Object operation : opers) {
            if ("CBraceNode".equals(getFacade().getName(operation)) 
		&& null != findModelElementWithName(
                    getFacade().getStereotypes(operation), "create")) {
                ctors.add(operation);
            }
        }
        assertEquals("Unexpected number of constructors.", 2, ctors.size());
        Object noParamsCtor = null;
        Object fourParamsCtor = null;
        for (Object ctor : ctors) {
            int paramsNumber = getFacade().getParameters(ctor).size();
            if  (paramsNumber == 1) {
                noParamsCtor = ctor;
            } else if (paramsNumber == 5) {
                fourParamsCtor = ctor;
            } else {
                fail("Constructor with unexpected number of parameters - " 
                    + paramsNumber + ".");
            }
        }
        assertNotNull("Constructor without parameters wasn't found.", 
            noParamsCtor);
        assertParameters(getFacade().getParameters(fourParamsCtor), 
            new String[][] {{"ln", "int"}, {"col", "int"}, {"c", "int"}, 
                            {"next", "CBraceNode"}});
        // check operations
        List nonCtorOpers = new ArrayList(opers);
        assertTrue("Constructors weren't included in the original opers.", 
            nonCtorOpers.removeAll(ctors));
        assertEquals("Unexpected number of non-constructor operations.", 
            4, nonCtorOpers.size());
        // check that operations are non-leaf
        for (Object oper : nonCtorOpers) {
            assertTrue("Operation is expected to be leaf (non-virtual).", 
                getFacade().isLeaf(oper));
        }
        // check that operations are inline definition in class
        for (Object oper : nonCtorOpers) {
            // TODO: this causes the test to fail - continue working to make it 
            //       work correctly.
//            assertHasTaggedValueWithValue(oper, ProfileCpp.TV_NAME_INLINE,
//                ProfileCpp.TV_INLINE_STYLE_DEFINITION_INSIDE_CLASS);
        }
        // TODO: check details on each operation
    }

    private void assertParameters(Collection params, 
        String[][] paramsNameAndTypeName) {
        for (String[] paramNameAndTypeName : paramsNameAndTypeName) {
            Object param =
                findModelElementWithName(params, paramNameAndTypeName[0]);
            assertNotNull("Parameter \"" + paramNameAndTypeName[0] 
                + "\" doesn't exist!", param);
            assertEquals("Unexpected type of parameter.", 
                paramNameAndTypeName[1], Model.getFacade().getName(
                Model.getFacade().getType(param)));
        }
    }

    /**
     * @param modelElements collection of model elements in which to look for
     * @param modelElementName the model element name
     * @return the model element with the given name
     */
    private Object getModelElementAndAssertNotDuplicated(
            Collection modelElements, String modelElementName) {
        Object me = findModelElementWithName(modelElements, modelElementName);
        assertNotNull("Model element with name \"" + modelElementName + 
            "\" wasn't found.", me);
        Collection mes2 = new ArrayList(modelElements);
        assertTrue(mes2.remove(me));
        assertNull(findModelElementWithName(mes2, modelElementName));
        return me;
    }
    
    /**
     * We don't make use of any settings currently, so this throws an exception
     * if any calls are made to it. If the importer implements support for some
     * settings, this class must be modified to return appropriate defaults for
     * the test setup.
     */
    private class DummySettings implements ImportSettings {

        public int getImportLevel() {
            throw new RuntimeException(
                    "Unexpected call to ImportSettings method");
        }

        public String getInputSourceEncoding() {
            throw new RuntimeException(
                    "Unexpected call to ImportSettings method");
        }

        public boolean isAttributeSelected() {
            throw new RuntimeException(
                    "Unexpected call to ImportSettings method");
        }

        public boolean isDatatypeSelected() {
            throw new RuntimeException(
                    "Unexpected call to ImportSettings method");
        }

        public boolean isCreateDiagramsSelected() {
            throw new RuntimeException(
                    "Unexpected call to ImportSettings method");
        }

        public boolean isMinimizeFigsSelected() {
            throw new RuntimeException(
                    "Unexpected call to ImportSettings method");
        }
    }
    
    private class DummyMonitor implements ProgressMonitor {

        public void close() {
        }

        public boolean isCanceled() {
            return false;
        }

        public void notifyMessage(String title, String introduction, 
                String message) {
        }

        public void notifyNullAction() {
        }

        public void setMaximumProgress(int max) { 
        }

        public void updateMainTask(String name) {
        }

        public void updateProgress(int progress) {
        }

        public void updateSubTask(String name) {
        }

        public void progress(ProgressEvent event) throws InterruptedException {
        }
        
    }

}
