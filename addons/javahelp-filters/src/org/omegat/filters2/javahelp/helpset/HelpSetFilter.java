/*
 * HelpSetFilter.java
 *
 * Created on 5 Ноябрь 2005 г., 5:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.omegat.filters2.javahelp.helpset;

import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.xml2.XMLFilter2;


/**
 * Filter for JavaHelp Helpset files (*.hs).
 * 
 * @author Maxym Mykhalchuk
 */
public class HelpSetFilter extends XMLFilter2
{
    /** Creates a new instance of HelpSetFilter */
    public HelpSetFilter()
            throws TranslationException
    {
        super("helpset");
        addTranslatableTag("title");
        addTranslatableTag("label");
    }
    
    /** Human-readable name of the File Format this filter supports. */
    public String getFileFormatName()
    {
        return "Java HelpSet files";
    }

    /** The default list of filter instances that this filter class has. */
    public Instance[] getDefaultInstances()
    {
        return new Instance[] 
        {
            new Instance("*.hs", null, "UTF-8")
        };
    }

}
