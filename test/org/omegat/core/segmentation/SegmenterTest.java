/*
 * SegmenterTest.java
 * JUnit based test
 *
 * Created on 18 Декабрь 2005 г., 12:09
 */

package org.omegat.core.segmentation;

import junit.framework.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.omegat.core.threads.CommandThread;
import org.omegat.gui.ProjectProperties;
import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;

/**
 *
 * @author Maxym Mykhalchuk
 */
public class SegmenterTest extends TestCase
{
    
    public SegmenterTest(String testName)
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
        TestSuite suite = new TestSuite(SegmenterTest.class);
        
        return suite;
    }

    /**
     * Test of segment method, of class org.omegat.core.segmentation.Segmenter.
     */
    public void testSegment()
    {
        List spaces = new ArrayList();
        List segments = Segmenter.segment("<br7>\n\n<br5>\n\nother", spaces);
        if(segments.size()!=3 || !segments.get(0).toString().equals("<br7>") || 
                !segments.get(1).toString().equals("<br5>") ||
                !segments.get(2).toString().equals("other"))
            fail("Bug XXXXXX.");
    }
    
    /**
     * Test of glue method, of class org.omegat.core.segmentation.Segmenter.
     */
    public void testGlue()
    {
        List spaces = new ArrayList();
        String oldString = "<br7>\n\n<br5>\n\nother";
        List segments = Segmenter.segment(oldString, spaces);
        String newString = Segmenter.glue(segments, spaces);
        if(!newString.equals(oldString))
            fail("Glue failed.");
    }

}
