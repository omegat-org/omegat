/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

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

package org.omegat.util;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import org.omegat.filters2.TranslationException;
import org.xml.sax.InputSource;

/**
 * Class that load up TMX (Translation Memory) files (any version)
 *
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 */
public class TMXReader extends DefaultHandler
{
    
    /**
     * Creates a new TMX Reader.
     *
     * @param encoding -- encoding to allow specification of alternative encodings (i.e. wordfast)
     */
    public TMXReader(String encoding)
    {
        m_encoding = encoding;
        m_srcList = new ArrayList(512);
        m_tarList = new ArrayList(512);
    }
    
    /** Returns a number of segments */
    public int numSegments()
    {
        return m_srcList.size();
    }
    
    /** Returns an original text of a target segment #n */
    public String getSourceSegment(int n)
    {
        if (n < 0)
            return "";								// NOI18N
        else if (n >= m_srcList.size())
            return "";								// NOI18N
        else
            return (String) m_srcList.get(n);
    }
    
    /** Returns a translation of a target segment #n */
    public String getTargetSegment(int n)
    {
        if (n < 0)
            return "";								// NOI18N
        else if (n >= m_tarList.size())
            return "";								// NOI18N
        else
            return (String) m_tarList.get(n);
    }
    
    private String sourceLanguage;
    
    /** Returns a source language of a TMX file */
    public String getSourceLanguage()
    {
        return sourceLanguage;
    }
    
    private String targetLanguage = null;
    
    /** Returns a target language of a TMX file.
     * <p>
     * <b>Non-standard</b>: there's no 'target' language of a TMX,
     * as TMX files may contain translations of the same segment
     * to several languages, but <i>our</i> TMX files contain only
     * a single translation.
     * <p>
     * Note: This attribute will be available only after the call to
     * {@link #loadFile(String)}.
     */
    public String getTargetLanguage()
    {
        return targetLanguage;
    }
    
    private String creationtool;
    /** Creation Tool attribute value of OmegaT TMXs: "OmegaT" */
    public static final String CT_OMEGAT = "OmegaT";                            // NOI18N
    /** Returns Creation Tool attribute of TMX file */
    public String getCreationTool()
    {
        return creationtool;
    }
    
    private String creationtoolversion;
    /** Creation Tool Version attribute value of OmegaT TMXs: "1" for 1.4.5 and earlier */
    public static final String CTV_OMEGAT_1 = "1";                              // NOI18N
    /** Creation Tool Version attribute value of OmegaT TMXs: "1.6" for 1.6 */
    public static final String CTV_OMEGAT_1_6 = "1.6";                          // NOI18N
    /** Returns Creation Tool attribute of TMX file */
    public String getCreationToolVersion()
    {
        return creationtoolversion;
    }
    
    private String segtype;
    /** Segment Type attribute value: "paragraph" */
    public static final String SEG_PARAGRAPH = "paragraph";                    // NOI18N
    /** Segment Type attribute value: "sentence" */
    public static final String SEG_SENTENCE = "sentence";                      // NOI18N
    /** Returns Segment Type attribute of TMX file */
    public String getSegType()
    {
        return segtype;
    }
    
    /**
     * Loads the specified TMX file by using a SAX parser.
     *
     * The parser makes callbacks to the TMXReader, to the methods
     * warning, error, fatalError, startDocument, endDocument,
     * startElement, endElement, characters, ignorableWhiteSpace,
     * and resolveEntity. Together these methods implement the
     * parsing of the TMX file.
     */
    public void loadFile(String filename) throws IOException, TranslationException
    {
        // parse the TMX file
        try
        {
            // log the parsing attempt
            StaticUtils.log(MessageFormat.format(
                    OStrings.getString("TMXR_INFO_READING_FILE"),
                    new Object[]{filename}));
                    
                    // create a new SAX parser factory
                    javax.xml.parsers.SAXParserFactory parserFactory =
                            javax.xml.parsers.SAXParserFactory.newInstance();
                    
                    // configure the factory
                    parserFactory.setValidating(false); // skips TMX validity checking
                    
                    // create a new SAX parser
                    javax.xml.parsers.SAXParser parser = parserFactory.newSAXParser();
                    
                    // make this TMX reader the default entity resolver for the parser,
                    // so we can handle DTD declarations ourselves
                    parser.getXMLReader().setEntityResolver(this);
                    
                    // parse the TM, provide the current TMX reader as notification handler
                    parser.parse(new java.io.File(filename), this);
                    
                    // log the fact that parsing is done
                    StaticUtils.log(OStrings.getString("TMXR_INFO_READING_COMPLETE"));
                    StaticUtils.log("");                                        // NOI18N
        }
        catch (Exception exception)
        {
            StaticUtils.log(MessageFormat.format(
                    OStrings.getString("TMXR_EXCEPTION_WHILE_PARSING"),
                    new Object[]{exception.getLocalizedMessage()}));
        }
    }
    
    /**
     * Receives notification of a parser warning. Called by SAX parser.
     */
    public void warning(SAXParseException exception) throws SAXException
    {
        StaticUtils.log(MessageFormat.format(
                OStrings.getString("TMXR_WARNING_WHILE_PARSING"),
                new Object[]{exception.getLocalizedMessage()}));
    }
    
    /**
     * Receives notification of a recoverable XML parsing error. Called by SAX parser.
     */
    public void error(SAXParseException exception) throws SAXException
    {
        StaticUtils.log(MessageFormat.format(
                OStrings.getString("TMXR_RECOVERABLE_ERROR_WHILE_PARSING"),
                new Object[]{exception.getLocalizedMessage()}));
    }
    
    /**
     * Receives notification of a fatal XML parsing error. Called by SAX parser.
     */
    public void fatalError(SAXParseException exception) throws SAXException
    {
        StaticUtils.log(MessageFormat.format(
                OStrings.getString("TMXR_FATAL_ERROR_WHILE_PARSING"),
                new Object[]{exception.getLocalizedMessage()}));
    }
    
    /**
     * Receives notification of the start of the XML document. Called by SAX parser.
     * Initialises variables needed for parsing of the TMX file.
     */
    public void startDocument()
    {
        headerParsed      = false;
        inTU              = false;
        inTUV             = false;
        inSegment         = false;
        sourceSegment     = new StringBuffer(1024); // allocate some memory for source segments
        targetSegment     = new StringBuffer(1024); // allcoate some memory for target segments
        subSourceSegments = new ArrayList();
        subTargetSegments = new ArrayList();
        currentElement    = new Stack();
        currentSub        = new Stack();
    }
    
    /**
     * Receives notification of the end of the XML document. Called by SAX parser.
     */
    public void endDocument()
    {
        // clear allocated memory for source/target/sub segments
        sourceSegment     = null;
        targetSegment     = null;
        subSourceSegments = null;
        subTargetSegments = null;
        currentElement    = null;
        currentSub        = null;
    }
    
    /**
     * Receives notification of the start of an element. Called by SAX parser.
     */
    public void startElement(String uri,
            String localName,
            String qName,
            Attributes attributes) throws SAXException
    {
        // determine the type of element and handle it, if required
        if (qName.equals(TMX_TAG_HEADER))
            startElementHeader(attributes);
        else if (qName.equals(TMX_TAG_TU))
            startElementTU(attributes);
        else if (qName.equals(TMX_TAG_TUV))
            startElementTUV(attributes);
        else if (qName.equals(TMX_TAG_SEG))
            startElementSegment(attributes);
        else if (   qName.equals(TMX_TAG_BPT)
        || qName.equals(TMX_TAG_EPT)
        || qName.equals(TMX_TAG_HI)
        || qName.equals(TMX_TAG_IT)
        || qName.equals(TMX_TAG_PH)
        || qName.equals(TMX_TAG_UT))
            startElementInline(attributes);
        else if (qName.equals(TMX_TAG_SUB))
            startElementSub(attributes);
    }
    
    /**
     * Receives notification of the end of an element. Called by SAX parser.
     */
    public void endElement(String uri,
            String localName,
            String qName) throws SAXException
    {
        // determine the type of element and handle it, if required
        if (qName.equals(TMX_TAG_HEADER))
            endElementHeader();
        else if (qName.equals(TMX_TAG_TU))
            endElementTU();
        else if (qName.equals(TMX_TAG_TUV))
            endElementTUV();
        else if (qName.equals(TMX_TAG_SEG))
            endElementSegment();
        else if (   qName.equals(TMX_TAG_BPT)
        || qName.equals(TMX_TAG_EPT)
        || qName.equals(TMX_TAG_HI)
        || qName.equals(TMX_TAG_IT)
        || qName.equals(TMX_TAG_PH)
        || qName.equals(TMX_TAG_UT))
            endElementInline();
        else if (qName.equals(TMX_TAG_SUB))
            endElementSub();
    }
    
    /**
     * Receives character data in element content. Called by the SAX parser.
     */
    public void characters(char[] ch,
            int    start,
            int    length) throws SAXException
    {
        // if not in a segment, or when in an inline element other than sub, do nothing
        if (!inSegment || ((String)currentElement.peek()).equals(TMX_TAG_INLINE))
            return;
        
        // determine the correct segment to add the data to
        StringBuffer segment = !((String)currentElement.peek()).equals(TMX_TAG_SUB)
        ? (segmentIsSource
                ? sourceSegment
                : targetSegment)
                : (StringBuffer)currentSub.peek();
        
        // append the data
        segment.append(ch, start, length);
    }
    
    /**
     * Receives ignorable whitespace in element content. Called by the SAX parser.
     */
    public void ignorableWhitespace(char[] ch,
            int    start,
            int    length) throws SAXException
    {
        // if not in a segment, or when in an inline element other than sub, do nothing
        if (!inSegment || ((String)currentElement.peek()).equals(TMX_TAG_INLINE))
            return;
        
        // determine the correct segment to add the data to
        StringBuffer segment = !((String)currentElement.peek()).equals(TMX_TAG_SUB)
        ? (segmentIsSource
                ? sourceSegment
                : targetSegment)
                : (StringBuffer)currentSub.peek();
        
        // append the data
        segment.append(ch, start, length);
    }
    
    /**
     * Handles the start of a header element in a TMX file.
     */
    private void startElementHeader(Attributes attributes)
    {
        // get the header attributes
        creationtool        = attributes.getValue(TMX_ATTR_CREATIONTOOL);
        creationtoolversion = attributes.getValue(TMX_ATTR_CREATIONTOOLVERSION);
        segtype             = attributes.getValue(TMX_ATTR_SEGTYPE);
        sourceLanguage      = attributes.getValue(TMX_ATTR_SRCLANG);
        
        // mark the header as parsed
        headerParsed = true;
        
        // log some details
        StaticUtils.log(MessageFormat.format(
                OStrings.getString("TMXR_INFO_CREATION_TOOL"),
                new Object[]{creationtool}));
        StaticUtils.log(MessageFormat.format(
                OStrings.getString("TMXR_INFO_CREATION_TOOL_VERSION"),
                new Object[]{creationtoolversion}));
        StaticUtils.log(MessageFormat.format(
                OStrings.getString("TMXR_INFO_SEG_TYPE"),
                new Object[]{segtype}));
        StaticUtils.log(MessageFormat.format(
                OStrings.getString("TMXR_INFO_SOURCE_LANG"),
                new Object[]{sourceLanguage}));
    }
    
    /**
     * Handles the end of a header element.
     */
    private void endElementHeader()
    {
    }
    
    /**
     * Handles the start of a translation unit.
     */
    private void startElementTU(Attributes attributes) throws SAXException
    {
        currentElement.push(TMX_TAG_TU);
        
        // ensure the header has been parsed
        // without the header info, we can't determine what's source and what's target
        if (!headerParsed)
            throw new SAXException(OStrings.getString("TMXR_ERROR_TU_BEFORE_HEADER"));
        
        // mark the current position as in a translation unit
        inTU = true;
        
        // clear the source, target, and sub segment buffers
        sourceSegment.setLength(0);
        targetSegment.setLength(0);
        subSourceSegments.clear();
        subTargetSegments.clear();
    }
    
    /**
     * Handles the end of a translation unit.
     */
    private void endElementTU()
    {
        currentElement.pop();
        
        // mark the current position as *not* in a translation unit
        inTU = false;
        
        // add source and target segment to lists
        if (sourceSegment.length() > 0)
        {
            m_srcList.add(sourceSegment.toString());
            m_tarList.add(targetSegment.toString());
        }
        
        // create a separate segment for each sub segment
        if (subSourceSegments.size() > 0)
        {
            for (int i = 0; i < subSourceSegments.size(); i++)
            {
                m_srcList.add(subSourceSegments.get(i).toString());
                m_tarList.add(subTargetSegments.get(i).toString());
            }
        }
    }
    
    /**
     * Handles the start of a tuv element.
     */
    private void startElementTUV(Attributes attributes)
    {
        currentElement.push(TMX_TAG_TUV);
        
        // ensure we're in a translation unit
        if (!inTU)
        {
            StaticUtils.log(OStrings.getString("TMXR_WARNING_TUV_NOT_IN_TU"));
            return;
        }
        
        // get the language of the tuv
        // try "lang" first, then "xml:lang"
        String language = attributes.getValue(TMX_ATTR_LANG);
        if (language == null)
            language = attributes.getValue(TMX_ATTR_LANG_NS);
        
        // if the language is not specified, skip the tuv
        if (language == null)
        {
            StaticUtils.log(
                    OStrings.getString("TMXR_WARNING_TUV_LANG_NOT_SPECIFIED"));
            return;
        }
        
        // check if the tuv is the source or target segment
        segmentIsSource = language.regionMatches(0, sourceLanguage, 0, 2);
        
        // set the target language, if it's not set yet
        if (   targetLanguage == null
                && !segmentIsSource)
        {
            targetLanguage = language;
            
            // log the target language
            StaticUtils.log(MessageFormat.format(
                    OStrings.getString("TMXR_INFO_TARGET_LANG"),
                    new Object[]{targetLanguage}));
        }
        
        // mark the current position as in a tuv
        inTUV = true;
        
        // clear the stack of sub segments
        currentSub.clear();
    }
    
    /**
     * Handles the end of a tuv element.
     */
    private void endElementTUV()
    {
        currentElement.pop();
        
        // mark the current position as *not* in a tuv element
        inTUV = false;
    }
    
    /**
     * Handles the start of a segment.
     */
    private void startElementSegment(Attributes attributes)
    {
        currentElement.push(TMX_TAG_SEG);
        
        // ensure we are currently in a tuv
        if (!inTUV)
        {
            StaticUtils.log(OStrings.getString("TMXR_WARNING_SEG_NOT_IN_TUV"));
            return;
        }
        
        // mark the current position as in a segment
        inSegment = true;
    }
    
    /**
     * Handles the end of a segment.
     */
    private void endElementSegment()
    {
        currentElement.pop();
        
        // mark the current position as *not* in a segment
        inSegment = false;
    }
    
    /**
     * Handles the start of a TMX inline element (<bpt>,  <ept>, <hi>,  <it>, <ph>,  <ut>).
     */
    private void startElementInline(Attributes attributes)
    {
        currentElement.push(TMX_TAG_INLINE);
    }
    
    /**
     * Handles the end of a TMX inline element (<bpt>,  <ept>, <hi>,  <it>, <ph>,  <ut>).
     */
    private void endElementInline()
    {
        currentElement.pop();
    }
    
    /**
     * Handles the start of a SUB inline element.
     */
    private void startElementSub(Attributes attributes)
    {
        currentElement.push(TMX_TAG_SUB);
        
        // create new entries in the sub segment lists/stack
        // NOTE: the assumption is made here that sub segments are
        // in the same order in both source and target segments
        StringBuffer sub = new StringBuffer();
        currentSub.push(sub);
        if (segmentIsSource)
            subSourceSegments.add(sub);
        else
            subTargetSegments.add(sub);
    }
    
    /**
     * Handles the end of a SUB inline element.
     */
    private void endElementSub() throws SAXException
    {
        currentElement.pop();
        
        // remove the current sub from the sub stack
        currentSub.pop();
    }
    
    /**
     * Makes the parser skip DTDs.
     */
    public InputSource resolveEntity(String publicId, String systemId) 
            throws SAXException
    {
        // simply return an empty dtd
        return new InputSource(new StringReader(""));                           // NOI18N
    }
    
    ////////////////////////////////////////////////////
    // Some variables needed while parsing a TMX file
    
    /** True if the TMX header has been parsed correctly. */
    private boolean      headerParsed;
    /** True if the current parsing position is in a TU element */
    private boolean      inTU;
    /** True if in a TUV element */
    private boolean      inTUV;
    /** True if in a SEG element */
    private boolean      inSegment;
    /** True if the segment currently being parsed is the source segment */
    private boolean      segmentIsSource;
    /** Contains the source text of the current TU */
    private StringBuffer sourceSegment;
    /** Contains the target text of the current TU */
    private StringBuffer targetSegment;
    /** Contains the source texts of sub segments of the current TU */
    private ArrayList    subSourceSegments;
    /** Contains the target texts of sub segments of the current TU */
    private ArrayList    subTargetSegments;
    /** Contains a stack of the tag names up to the current parsing point */
    private Stack        currentElement;
    /** Contains a stack of the sub segment buffers */
    private Stack        currentSub;

    ///////////////////////////////////////////////////
    // Constants for certain TMX tag names/attributes
    
    private final static String TMX_TMX_TAG    = "tmx";                         // NOI18N
    private final static String TMX_TAG_HEADER = "header";                      // NOI18N
    private final static String TMX_TAG_BODY   = "body";                        // NOI18N
    private final static String TMX_TAG_TU     = "tu";                          // NOI18N
    private final static String TMX_TAG_TUV    = "tuv";                         // NOI18N
    private final static String TMX_TAG_SEG    = "seg";                         // NOI18N
    private final static String TMX_TAG_INLINE = "inline"; // made up for convenience // NOI18N
    private final static String TMX_TAG_BPT    = "bpt";                         // NOI18N
    private final static String TMX_TAG_EPT    = "ept";                         // NOI18N
    private final static String TMX_TAG_HI     = "hi";                          // NOI18N
    private final static String TMX_TAG_IT     = "it";                          // NOI18N
    private final static String TMX_TAG_PH     = "ph";                          // NOI18N
    private final static String TMX_TAG_UT     = "ut";                          // NOI18N
    private final static String TMX_TAG_SUB    = "sub";                         // NOI18N
    
    private final static String TMX_ATTR_LANG                = "lang";          // NOI18N
    private final static String TMX_ATTR_LANG_NS             = "xml:lang";      // NOI18N
    private final static String TMX_ATTR_CREATIONTOOL        = "creationtool";  // NOI18N
    private final static String TMX_ATTR_CREATIONTOOLVERSION = "creationtoolversion"; // NOI18N
    private final static String TMX_ATTR_SEGTYPE             = "segtype";       // NOI18N
    private final static String TMX_ATTR_SRCLANG             = "srclang";       // NOI18N

    /** Encoding to use while reading TMX file, if not found in TMX itself. */
    private String          m_encoding;
    /** The list of source segments. */
    private ArrayList       m_srcList;
    /** The list of target segments (translations). */
    private ArrayList       m_tarList;
}

