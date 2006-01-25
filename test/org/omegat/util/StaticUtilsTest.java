/*
 * StaticUtilsTest.java
 * JUnit based test
 *
 * Created on January 21, 2006, 4:42 PM
 */

package org.omegat.util;

import junit.framework.*;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 *
 * @author Maxym
 */
public class StaticUtilsTest extends TestCase
{
    
    public StaticUtilsTest(String testName)
    {
        super(testName);
    }

    protected void setUp() throws Exception
    {
    }

    protected void tearDown() throws Exception
    {
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite(StaticUtilsTest.class);
        
        return suite;
    }

    /**
     * Test of buildTagList method, of class org.omegat.util.StaticUtils.
     */
    public void testBuildTagList()
    {
        // TODO add your test code below by replacing the default call to fail.
        String str = "Tag <test> case <b0>one</b0>.";
        ArrayList tagList = new ArrayList();
        StaticUtils.buildTagList(str, tagList);
        if (tagList.size()!=2 ||
                (! tagList.get(0).toString().equals("b0")) ||
                (! tagList.get(1).toString().equals("/b0")) )
            fail("Wrong tags found in '"+str+"': " + tagList.toString());
    }

}
