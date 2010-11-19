/*
 * NBEdgeFilter.java
 * Created on 27 April 2005
 * Last modified on 8 October 2005
 * 
 * License: Public Domain
 * Author: Maxym Mykhalchuk
 */

package org.omegat.filters2.xml.edge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.omegat.filters2.Instance;
import org.omegat.filters2.xml.DefaultEntityFilter;
import org.omegat.filters2.xml.XMLAbstractFilter;
import org.omegat.filters2.xml.XMLWriter;
import org.omegat.filters2.xml.XMLReader;

/**
 * Filter for NetBeans EDGE Newsletter.
 * <p>
 * Is an example of filter plugin for OmegaT.
 *
 * @author Maxym Mykhalchuk
 */
public class NBEdgeFilter extends XMLAbstractFilter
{
    
    /** Creates a new instance of NBEdgeFilter */
    public NBEdgeFilter()
    {
        defineFormatTag("a", "a");	 
        defineFormatTag("abbr", "abbr");	 
        defineFormatTag("acronym", "acronym");	 
        defineFormatTag("b", "b");	 
        defineFormatTag("big", "big");	 
        defineFormatTag("code", "code");	 
        defineFormatTag("cite", "cite");	 
        defineFormatTag("em", "em");	 
        defineFormatTag("font", "f");	 
        defineFormatTag("i", "i");	 
        defineFormatTag("kbd", "k");	 
        defineFormatTag("samp", "samp");	 
        defineFormatTag("strike", "strike");	 
        defineFormatTag("s", "s");	 
        defineFormatTag("small", "small");	 
        defineFormatTag("span", "span");	 
        defineFormatTag("sub", "sub");	 
        defineFormatTag("sup", "sup");	 
        defineFormatTag("strong", "strong");	 
        defineFormatTag("tt", "tt");	 
        defineFormatTag("u", "u");	 
        defineFormatTag("var", "var");	 
        
        setEntityFilter(new DefaultEntityFilter());
    }
    
    /**
     * Whether target encoding can be varied by the user.
     */
    public boolean isTargetEncodingVariable()
    {
        return false;
    }
    
    /**
     * Whether source encoding can be varied by the user.
     */
    public boolean isSourceEncodingVariable()
    {
        return true;
    }
    
    /**
     * Human-readable name of the File Format this filter supports.
     */
    public String getFileFormatName()
    {
        return "NetBeans EDGE Newsletters";
    }
    
    /**
     * The default list of filter instances that this filter class has.
     */
    public Instance[] getDefaultInstances()
    {
        return new Instance[]
        {
            new Instance("*.xml", null, null,
                    TFP_NAMEONLY+"_"+TFP_TARGET_LOCALE+"."+TFP_EXTENSION),
        };
    }
    
    /**
     * Creates a reader of an input file.
     */
    public BufferedReader createReader(java.io.File infile, String encoding)
            throws UnsupportedEncodingException, IOException
    {
        return new BufferedReader(new XMLReader(infile.getAbsolutePath(), encoding));
    }
    /**
     * Creates a writer of the translated file.
     */
    public BufferedWriter createWriter(File outfile, String encoding)
            throws UnsupportedEncodingException, IOException
    {
        return new BufferedWriter(new XMLWriter(outfile.getAbsolutePath(), "UTF-8"));
    }
    
}
