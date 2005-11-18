/*
 * XMLHandler2.java
 *
 * Created on 5 Ноябрь 2005 г., 5:37
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.omegat.filters2.xml2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Callback to be called by SAX Parser.
 * Does the real job of feeding strings to OmegaT core.
 *
 * @author Maxym Mykhalchuk
 */
public class XMLHandler2 extends DefaultHandler2
{
    /** My parent XML Filter. */
    XMLFilter2 filter;
    /** The writer I write translated file into. */
    BufferedWriter writer;
    /** Tags with translatable content. */
    private HashSet translatableTags;
    /** Tags whose attributes are translatable. */
    private HashMap translatableAttributes;

    
    /**
     * Creates a new instance of XMLHandler2 
     */
    public XMLHandler2(XMLFilter2 filter, BufferedWriter writer,
            HashSet translatableTags, HashMap translatableAttributes)
    {
        this.filter = filter;
        this.writer = writer;
        this.translatableTags = translatableTags;
        this.translatableAttributes = translatableAttributes;
    }
    
    /** Utility Method: Writes some data into the supplied writer. */
    private void write(char[] ch, int start, int length) throws SAXException
    {
        try
        {
            writer.write(ch, start, length);
        }
        catch( IOException ioe )
        {
            throw new SAXException(ioe);
        }
    }
    /** Utility Method: Writes some data into the supplied writer. */
    private void write(String s) throws SAXException
    {
        try
        {
            writer.write(s);
        }
        catch( IOException ioe )
        {
            throw new SAXException(ioe);
        }
    }

    /** A buffer to collect a translatable string. */
    StringBuffer collected;
    /** If a collectable element started. */
    boolean started = false;
    /** The collectable element. */
    String element;
    
    /** Receive notification of the start of an element. */
    public void startElement(String uri, String localName, String qName, 
            Attributes attributes) throws SAXException
    {
        if(weAreInEntity)
            return;
        
        if( translatableTags.contains(qName) && !started )
        {
            started = true;
            element = qName;
            collected = new StringBuffer();
        }
        write("<"+qName);
        for(int i=0; i<attributes.getLength();i++)
        {
            String attr = attributes.getQName(i);
            String value = attributes.getValue(i);
            write(" ");
            write(attr);
            write("=\"");
            if( translatableAttributes.containsKey(qName) )
            {
                HashSet hs = (HashSet)translatableAttributes.get(qName);
                if( hs.contains(attr) )
                    value = filter.privateProcessEntry(value);
            }
            write(value);
            write("\"");
        }
        write(">");
    }
    
    /** Receive notification of character data inside an element. */
    public void characters(char[] ch, int start, int length) 
            throws SAXException
    {
        if(weAreInEntity)
            return;
        
        if( started )
            collected.append(ch, start, length);
        else
            write(ch, start, length);
    }

    /** Receive notification of the end of an element. */
    public void endElement(String uri, String localName, String qName) 
            throws SAXException
    {
        if(weAreInEntity)
            return;
        
        if( started && element.equals(qName) )
        {
            started = false;
            write(filter.privateProcessEntry(collected.toString()));
        }
        write("</"+qName+">");
    }

    /** Resolve an external entity to work offline or behind proxy. */
    public InputSource resolveEntity(String publicId, String systemId) 
            throws IOException, SAXException
    {
        String orig = systemId;
        int lastslash = systemId.lastIndexOf('/');
        if( lastslash>=0 )
        {
            String name = orig.substring(lastslash+1);
            String res = "/org/omegat/filters2/javahelp/resources/"+name;
            InputStream stream = getClass().getResourceAsStream(res);
            return new InputSource(stream);
        }
        return null;
    }
    /** The older {@link #resolveEntity} method is overridden to call this one. */
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) 
            throws SAXException, IOException
    {
        return resolveEntity(publicId, systemId);
    }

    /** Receive notification of ignorable whitespace in element content.*/
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
        if(!weAreInEntity)
            write(ch, start, length);
    }

    /** Receive notification of the beginning of the document. */
    public void startDocument() throws SAXException
    {
        write("<?xml version='1.0' encoding='UTF-8'?>\n");
    }

    /** Receive notification of the comments in XML file. */
    public void comment(char[] ch, int start, int length) throws SAXException
    {
        if (!weAreInEntity)
        {
            write("<!--");
            write(ch, start, length);
            write("-->");
        }
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException
    {
        if(!weAreInEntity)
        {
            write("\n<!DOCTYPE ");
            write(name);
            write("\n");
            write("PUBLIC \""+publicId+"\"\n");
            write("\""+systemId+"\">\n");
        }
    }

    boolean weAreInEntity = false;
    String entityName;
    public void startEntity(String name) throws SAXException
    {
        weAreInEntity = true;
        entityName = name;
    }
    public void endEntity(String name) throws SAXException
    {
        if (entityName.equals(name))
            weAreInEntity = false;
    }

    public void endDocument() throws SAXException
    {
        super.endDocument();
    }

}
