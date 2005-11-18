/*
 * XMLFilter2.java
 *
 * Created on 5 Ноябрь 2005 г., 5:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.omegat.filters2.xml2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.xml.XMLWriter;
import org.omegat.util.AntiCRReader;
import org.omegat.util.EncodingAwareReader;
import org.omegat.util.OConsts;
import org.omegat.util.StaticUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Next Generation Filter for XML Files.
 * It uses SAX Parser to process XML file.
 * 
 * @author Maxym Mykhalchuk
 */
public abstract class XMLFilter2 extends AbstractFilter
{
    /** SAX Parser to parse XML file. */
    SAXParser parser;
    /** 
     * Root Element of the handled XML file. 
     * It's used to differentiate between filters so that a filter doesn't 
     * handle the XML file that is not his to handle.
     */
    String rootElement;

    /**
     * Creates a new instance of XMLFilter2.
     * <p>
     * Needs a root element of the XML file that it should handle.
     * Root element is used to differentiate between filters so that a filter 
     * doesn't handle the XML file that is not his to handle.
     * If root element is null, all the files are handled.
     * <p>
     * Subclasses have to setup translatable tags and attributes
     * by calling {@link #addTranslatableTag(String)} and 
     * {@link #addTranslatableAttribute(String,String)}.
     */
    public XMLFilter2(String rootElement)
            throws TranslationException
    {
        try
        {
            this.rootElement = rootElement;
            SAXParserFactory factory = SAXParserFactory.newInstance();
            parser = factory.newSAXParser();
        }
        catch( Exception e )
        {
            throw new TranslationException(e.getLocalizedMessage());
        }
    }
    
    /** 
     * Returns whether the file is supported by the filter.
     * <p>
     * XMLFilter2 uses supplied rootElement to tell whether it's his file or no.
     */
    public boolean isFileSupported(BufferedReader reader)
    {
        if( rootElement==null )
            return true;
        
        try
        {
            char[] buf = new char[OConsts.READ_AHEAD_LIMIT];
            int len = reader.read(buf);
            String s = new String(buf, 0, len);
            int pos = -1;
            while( (pos=s.indexOf('<', pos+1))>=0 )
            {
                char ch = s.charAt(pos+1);
                if( ch=='!' )
                {
                    // skipping comments and DTD
                    char ch2 = s.charAt(pos+2);
                    if( ch2=='-' )
                        pos = s.indexOf("-->", pos)+2; // comment
                    else
                        pos = s.indexOf('>', pos); // DTD
                    if( pos<0 )
                        break;
                }
                else if( Character.isJavaIdentifierStart(ch) )
                {
                    // found <"english", testing
                    int pos2 = pos+1;
                    while( Character.isJavaIdentifierPart(s.charAt(pos2)) ) 
                        pos2++;
                    String id = s.substring(pos+1, pos2);
                    return id.equals(rootElement);
                }
            }
            return false;
        }
        catch( Exception e )
        {
            return false;
        }
    }

    /** Tags with translatable content. */
    private HashSet translatableTags = new HashSet();
    /** Tags whose attributes are translatable. */
    private HashMap translatableAttributes = new HashMap();
    
    /** 
     * Adds a tag, whose content is translatable.
     * <p>
     * For example, in XHTML the text in 
     * <code>&lt;h1&gt;Something here&lt;/h1&gt;</code>
     * needs to be translated, 
     * so XHTML filter should call 
     * <code>addTranslatableTag("h1");</code>.
     */
    protected void addTranslatableTag(String tag)
    {
        translatableTags.add(tag);
    }
    /** 
     * Adds an attribute, whose content is translatable.
     * <p>
     * For example, in XHTML the alternative text of an image 
     * <code>&lt;img src="..." alt="image description"&gt;</code>
     * needs to be translated, 
     * so XHTML filter should call 
     * <code>addTranslatableAttribute("alt", "img");</code>.
     */
    protected void addTranslatableAttribute(String attribute, String tag)
    {
        HashSet hs = (HashSet)translatableAttributes.get(tag);
        if( hs==null )
            hs = new HashSet();
        hs.add(attribute);
        translatableAttributes.put(tag, hs);
    }
    
    /**
     * Package private processEntry to give it to XMLHandler2 
     */
    String privateProcessEntry(String entry)
    {
        return processEntry(entry);
    }

    /** Processes a single file. */
    public void processFile(BufferedReader infile, BufferedWriter outfile) 
            throws IOException, TranslationException
    {
        XMLHandler2 handler;
        handler = new XMLHandler2(this, outfile, translatableTags, translatableAttributes);
        InputSource is = new InputSource(infile);
        try
        {
            XMLReader reader = parser.getXMLReader();
            reader.setContentHandler(handler);
            reader.setDTDHandler(handler);
            reader.setEntityResolver(handler);
            reader.setErrorHandler(handler);
            reader.setProperty("http://xml.org/sax/properties/lexical-handler",  handler);
            reader.setProperty("http://xml.org/sax/properties/declaration-handler",  handler);
            parser.parse(is, handler);
        }
        catch( SAXException saxe )
        {
            saxe.printStackTrace(StaticUtils.getLogStream());
            throw new TranslationException(saxe.getLocalizedMessage());
        }
    }

    /** 
     * Whether source encoding can be varied by the user.
     * <p>
     * Default is true, but note that user-supplied encoding will be used 
     * only if XML reader class fails to find encoding declaration in XML file.
     */
    public boolean isSourceEncodingVariable()
    {
        return true;
    }

    /** 
     * Whether target encoding can be varied by the user.
     * <p>
     * Default is true. XML writer class will write out encoding declaration.
     */
    public boolean isTargetEncodingVariable()
    {
        return true;
    }

    /** 
     * Creates a reader of an input file. 
     * <p>
     * By default creates an encoding-aware reader, that is a reader that
     * finds and uses encoding declaration in XML file.
     */
    public BufferedReader createReader(File infile, String encoding) 
            throws UnsupportedEncodingException, IOException
    {
        return new BufferedReader(
                new AntiCRReader(
                new EncodingAwareReader(
                    infile.getAbsolutePath(), 
                    EncodingAwareReader.ST_XML, 
                    encoding)));
    }

    /** 
     * Creates a writer of the translated file. 
     * <p>
     * By default creates a writer that will write out encoding declaration.
     */
    public BufferedWriter createWriter(File outfile, String encoding) 
            throws UnsupportedEncodingException, IOException
    {
        return new BufferedWriter(
                new XMLWriter(outfile.getAbsolutePath(), 
                encoding));
    }

    /** 
     * Returns the hint displayed while the user edits the filter. 
     * <p>
     * By default returns a note stating that user-supplied encoding will be used 
     * only if XML reader class fails to find encoding declaration in XML file.
     */
    public String getHint()
    {
        return "Note: Source File Encoding setting will be used only if the file does not have an encoding declaration.";
    }
    
}
