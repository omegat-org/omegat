/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Maxym Mykhalchuk
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

package org.omegat.gui.glossary;

import java.util.ArrayList;
import java.util.List;

import org.omegat.core.glossary.GlossaryEntry;
import org.omegat.gui.glossary.GlossaryTextArea;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Maxym Mykhalchuk
 */
public class GlossaryTextAreaTest extends TestCase
{
    
    public GlossaryTextAreaTest(String testName)
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
        TestSuite suite = new TestSuite(GlossaryTextAreaTest.class);
        
        return suite;
    }

    /**
     * Testing setGlossaryEntries of org.omegat.gui.main.GlossaryTextArea.
     */
    public void testSetGlossaryEntries()
    {
        List<GlossaryEntry> entries = new ArrayList<GlossaryEntry>();
        entries.add(new GlossaryEntry("source1", "translation1", ""));
        entries.add(new GlossaryEntry("source2", "translation2", "comment2"));
        GlossaryTextArea gta = new GlossaryTextArea();
        gta.setGlossaryEntries(entries);
        String GTATEXT = "source1 = translation1\n\nsource2 = translation2\ncomment2\n\n";
        if (!gta.getText().equals(GTATEXT))
            fail("Glossary pane doesn't show what it should.");        
    }

    /**
     * Testing clear in org.omegat.gui.main.GlossaryTextArea.
     */
    public void testClear()
    {
        List<GlossaryEntry> entries = new ArrayList<GlossaryEntry>();
        entries.add(new GlossaryEntry("source1", "translation1", ""));
        entries.add(new GlossaryEntry("source2", "translation2", "comment2"));
        GlossaryTextArea gta = new GlossaryTextArea();
        gta.setGlossaryEntries(entries);
        gta.clear();
        if (gta.getText().length()>0)
            fail("Glossary pane isn't empty.");
    }
    
}
