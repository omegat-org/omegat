/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.core.segmentation;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.omegat.core.data.CommandThread;
import org.omegat.gui.main.MainWindow;

/**
 * Tests for OmegaT segmentation.
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
        List segments = Segmenter.segment("<br7>\n\n<br5>\n\nother", spaces, new ArrayList());
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
        MainWindow mw = new MainWindow();
        CommandThread.core=new CommandThread(mw);
        List spaces = new ArrayList();
        List brules = new ArrayList();
        String oldString = "<br7>\n\n<br5>\n\nother";
        List segments = Segmenter.segment(oldString, spaces, brules);
        String newString = Segmenter.glue(segments, spaces, brules);
        if(!newString.equals(oldString))
            fail("Glue failed.");
    }

}
