/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
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

package org.omegat.filters3.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.TranslationException;
import org.omegat.util.OConsts;
import org.omegat.util.PatternConsts;
import org.omegat.util.StaticUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;


/**
 * Abstract basis filter for XML format filters: OpenDocument, DocBook etc.
 * Ideally should allow creation of a new XML dialect filter by simply
 * specifying translatable tags and attributes.
 *
 * @author Maxym Mykhalchuk
 */
public abstract class XMLFilter extends AbstractFilter implements Translator
{
    /** Factory for SAX parsers. */
    private SAXParserFactory parserFactory;
    
    /** XML dialect this filter handles. */
    private XMLDialect dialect;
    
    /** Creates a new instance of XMLFilter2 */
    public XMLFilter(XMLDialect dialect)
    {
        parserFactory = SAXParserFactory.newInstance();
        //parserFactory.setValidating(false);
        try
        {
            parserFactory.setFeature("http://xml.org/sax/features/validation", true);   // NOI18N
        } 
        catch (Exception e) {}
        this.dialect = dialect;
    }

    /** Detected encoding of the input XML file. */
    private String encoding;
    
    /**
     * Creates a special XML-encoding-aware reader of an input file.
     * 
     * @param inFile The source file.
     * @param outEncoding Encoding of the source file, if the filter supports it. Otherwise null.
     * @return The reader of the source file.
     *
     * @throws UnsupportedEncodingException Thrown if JVM doesn't support the specified inEncoding.
     * @throws IOException If any I/O Error occurs upon reader creation.
     */
    public BufferedReader createReader(File inFile, String inEncoding) 
            throws UnsupportedEncodingException, IOException 
    {
        XMLReader xmlreader = new XMLReader(inFile, inEncoding);
        this.encoding = xmlreader.getEncoding();
        return new BufferedReader(xmlreader);
    }

    /**
     * Creates a writer of the translated file.
     * Accepts <code>null</code> output file -- returns a writer to
     * <code>/dev/null</code> in this case ;-)
     * 
     * @param outFile The target file.
     * @param outEncoding Encoding of the target file, if the filter supports it. Otherwise null.
     * @return The writer for the target file.
     *
     * @throws UnsupportedEncodingException Thrown if JVM doesn't support the specified outEncoding
     * @throws IOException If any I/O Error occurs upon writer creation
     */
    public BufferedWriter createWriter(File outFile, String outEncoding) 
            throws UnsupportedEncodingException, IOException 
    {
        if (outEncoding==null)
            outEncoding = this.encoding;
        
        if (outFile==null)
            return new BufferedWriter(new StringWriter());
        else
            return new BufferedWriter(new XMLWriter(outFile, outEncoding));
    }

    /** Processes an XML file. */
    public List processFile(File inFile, String inEncoding, File outFile, String outEncoding) 
            throws IOException, TranslationException
    {
        try
        {
            BufferedReader inReader = createReader(inFile, inEncoding);
            InputSource source = new InputSource(inReader);
            source.setSystemId("file:///"+                                      // NOI18N
                    inFile.getCanonicalPath().replace(File.separatorChar, '/'));
            SAXParser parser = parserFactory.newSAXParser();
            Handler handler = new Handler(parser, this, dialect, inFile, inEncoding, outFile, outEncoding);
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);     // NOI18N
            parser.setProperty("http://xml.org/sax/properties/declaration-handler", handler); // NOI18N
            parser.parse(source, handler);
            inReader.close();
            return handler.getProcessedFiles();
        }
        catch(ParserConfigurationException e)
        {
            throw new TranslationException(e.getLocalizedMessage());
        }
        catch(SAXException e)
        {
            e.printStackTrace(StaticUtils.getLogStream());
            e.printStackTrace();
            throw new TranslationException(e.getLocalizedMessage());
        }
    }
    
    protected void processFile(BufferedReader inFile, BufferedWriter outFile) throws IOException, TranslationException
    {
        throw new IOException("XMLFilter.processFile(BufferedReader,BufferedWriter) should never be called!");  // NOI18N
    }

    /** 
     * Whether source encoding can be varied by the user.
     * If XML file has no encoding declaration, user-specified will be used, 
     * hence returns <code>true</code> by default.
     * @return <code>true</code>
     */
    public boolean isSourceEncodingVariable()
    {
        return true;
    }

    /**
     * Target encoding can be varied by the user.
     * @return <code>true</code>
     */
    public boolean isTargetEncodingVariable()
    {
        return true;
    }
    
    /**
     * The method the Handler would call to pass translatable content to 
     * OmegaT core and receive translation.
     */
    public String translate(String entry)
    {
        return super.processEntry(entry);
    }
    
    /** 
     * Returns whether the XML file is supported by the filter.
     * <br>
     * Reads {@link org.omegat.util.OConsts#READ_AHEAD_LIMIT}
     * and tries to detect constrained text and match constraints defined
     * in {@link XMLDialect} against them.
     */
    public boolean isFileSupported(BufferedReader reader) 
    {
        if (dialect.getConstraints()==null || dialect.getConstraints().size()==0)
            return true;
        
        try
        {
            char[] cbuf = new char[OConsts.READ_AHEAD_LIMIT];
            int cbuf_len = reader.read(cbuf);
            String buf = new String(cbuf, 0, cbuf_len);
            Matcher matcher = PatternConsts.XML_DOCTYPE.matcher(buf);
            if (matcher.find())
            {
                Pattern doctype = (Pattern) dialect.getConstraints().get(XMLDialect.CONSTRAINT_DOCTYPE);
                if (doctype!=null && 
                        (matcher.group(1)==null || !doctype.matcher(matcher.group(1)).matches()))
                    return false;
                Pattern publicc = (Pattern) dialect.getConstraints().get(XMLDialect.CONSTRAINT_PUBLIC_DOCTYPE);
                if (publicc!=null && 
                        (matcher.group(3)==null || !publicc.matcher(matcher.group(3)).matches()))
                    return false;
                Pattern system = (Pattern) dialect.getConstraints().get(XMLDialect.CONSTRAINT_SYSTEM_DOCTYPE);
                if (system!=null && 
                        (matcher.group(5)==null || !system.matcher(matcher.group(5)).matches()))
                    return false;
            }
            else if (dialect.getConstraints().containsKey(XMLDialect.CONSTRAINT_DOCTYPE) || 
                    dialect.getConstraints().containsKey(XMLDialect.CONSTRAINT_PUBLIC_DOCTYPE) || 
                    dialect.getConstraints().containsKey(XMLDialect.CONSTRAINT_SYSTEM_DOCTYPE))
            {
                return false;
            }
            
            matcher = PatternConsts.XML_ROOTTAG.matcher(buf);
            if (matcher.find())
            {
                Pattern root = (Pattern) dialect.getConstraints().get(XMLDialect.CONSTRAINT_ROOT);
                if (root!=null && 
                        (matcher.group(1)==null || !root.matcher(matcher.group(1)).matches()))
                    return false;
            }
            else if (dialect.getConstraints().containsKey(XMLDialect.CONSTRAINT_ROOT))
            {
                return false;
            }
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }
}
