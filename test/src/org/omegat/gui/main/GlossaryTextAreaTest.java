/*
 * GlossaryTextAreaTest.java
 * JUnit based test
 *
 * Created on 3 Июль 2006 г., 21:18
 */

package org.omegat.gui.main;

import java.util.ArrayList;
import junit.framework.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.List;
import org.omegat.core.StringData;
import org.omegat.core.glossary.GlossaryEntry;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.Token;

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
        List entries = new ArrayList();
        entries.add(new GlossaryEntry("source1", "translation1", ""));
        entries.add(new GlossaryEntry("source2", "translation2", "comment2"));
        GlossaryTextArea gta = new GlossaryTextArea();
        gta.setGlossaryEntries(entries);
        String GTATEXT = "'source1' = 'translation1'\n\n'source2' = 'translation2'\ncomment2\n\n";
        if (!gta.getText().equals(GTATEXT))
            fail("Glossary pane doesn't show what it should.");        
    }

    /**
     * Тест метода clear класса org.omegat.gui.main.GlossaryTextArea.
     */
    public void testClear()
    {
        List entries = new ArrayList();
        entries.add(new GlossaryEntry("source1", "translation1", ""));
        entries.add(new GlossaryEntry("source2", "translation2", "comment2"));
        GlossaryTextArea gta = new GlossaryTextArea();
        gta.setGlossaryEntries(entries);
        gta.clear();
        if (gta.getText().length()>0)
            fail("Glossary pane isn't empty.");
    }
    
}
