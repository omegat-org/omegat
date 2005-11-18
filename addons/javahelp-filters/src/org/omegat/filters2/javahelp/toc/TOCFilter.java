/*
 * TOCFilter.java
 *
 * Created on 5 Ноябрь 2005 г., 5:11
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.omegat.filters2.javahelp.toc;

import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.xml2.XMLFilter2;

/**
 * JavaHelp TOC (Table of Contents) filter.
 *
 * @author Maxym Mykhalchuk
 */
public class TOCFilter extends XMLFilter2
{
    
    /** Creates a new instance of TOCFilter */
    public TOCFilter()
            throws TranslationException
    {
        super("toc");
        addTranslatableAttribute("text", "tocitem");
    }

    public String getFileFormatName()
    {
        return "JavaHelp Table of Contents files";
    }

    public Instance[] getDefaultInstances()
    {
        return new Instance[]
        {
            new Instance("*.xml", null, "UTF-8")
        };
    }
    
}
