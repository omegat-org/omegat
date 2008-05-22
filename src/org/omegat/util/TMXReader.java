/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
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

package org.omegat.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;

import org.omegat.core.Core;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.util.xml.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Class that loads TMX (Translation Memory Exchange) files (any version, we're cool).
 * <p>
 * TMX reader loads all TUVs in a TU first, then tries to decide 
 * what's source and what's target, by first matching against the full 
 * source/target language string, then against language codes only. 
 * This improves TMX handling in a number of ways:
 * <ol>
 * <li>We now support multiple TUVs in a TU, which makes us more TMX compliant.
 * <li>If an exact language match cannot be found, the best alternative is
 *     loaded, if present. This means that if you set the source or target
 *     language to EN-US, and all you have is EN-GB, EN-GB will be loaded. 
 *     Or if you've set it to EN, and you have both, the first of either will 
 *     be loaded.
 * </ol>
 *
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Maxym Mykhalchuk
 */
public class TMXReader extends org.xml.sax.helpers.DefaultHandler 
{
    /** 
      * Creates a new TMX Reader.
      * 
      * @param encoding -- encoding to allow specification of alternative encodings (i.e. wordfast)
      */
    public TMXReader(String   encoding,
                     Language sourceLanguage,
                     Language targetLanguage) 
    {
        m_encoding          = encoding;
        m_srcList           = new ArrayList<String>();
        m_tarList           = new ArrayList<String>();
        m_properties        = new HashMap<String, String>();
        m_variantLanguages  = new HashSet<String>();
        this.sourceLang = sourceLanguage;
        this.targetLang = targetLanguage;
        this.sourceLanguage = sourceLanguage.getLanguage();
        this.targetLanguage = targetLanguage.getLanguage();
    }
    
    /** Returns the source language */
    public String getSourceLanguage() 
    {
        return sourceLanguage;
    }
    
    /** Returns the target language */
    public String getTargetLanguage() 
    {
        return targetLanguage;
    }
    
    /** Returns the number of segments */
    public int numSegments() 
    {
        return m_srcList.size();        
    }
    
    /** Returns an original text of a source segment #n */
    public String getSourceSegment(int n) 
    {
        if (n < 0 || n >= numSegments())
            return new String();
        else
            return m_srcList.get(n);
    }
    
    /** Returns a translation of a target segment #n */
    public String getTargetSegment(int n) 
    {
        if (n < 0 || n >= numSegments())
            return new String();
        else
            return m_tarList.get(n);
    }
    
    private String creationtool = null;
    /** Creation Tool attribute value of OmegaT TMXs: "OmegaT" */
    public static final String CT_OMEGAT = "OmegaT";                            // NOI18N
    /** Returns Creation Tool attribute of TMX file */
    public String getCreationTool() 
    {
        return creationtool;
    }
    
    private String creationtoolversion = null;
    /** "1" for OmegaT 1.4.5 and earlier (Creation Tool Version attribute). */
    public static final String CTV_OMEGAT_1 = "1";                              // NOI18N
    /** "1.6" for OmegaT 1.6 RC3 to 1.6.0 RC11 (Creation Tool Version attribute). Pretty misleading. */
    public static final String CTV_OMEGAT_1_6_RC3_RC11 = "1.6";                 // NOI18N
    /** "1.6 RC12" for OmegaT 1.6 RC12 and later RCs (Creation Tool Version attribute). */
    public static final String CTV_OMEGAT_1_6_RC12 = "1.6 RC12";                // NOI18N
    /** 1.6.0 for OmegaT 1.6.0-final */
    public static final String CTV_OMEGAT_1_6_0_FINAL = "1.6.0";                // NOI18N
    /** 1.6.1 for OmegaT 1.6.1 */
    public static final String CTV_OMEGAT_1_6_1 = "1.6.1"; // NOI18N
    /** Current version */
    /** This number is not updated anymore. OStrings.VERSION + OStrings.UPDATE
        is used instead */
    public static final String CTV_OMEGAT_CURRENT = CTV_OMEGAT_1_6_1;           // NOI18N
    /** Returns Creation Tool attribute of TMX file */
    public String getCreationToolVersion() 
    {
        return creationtoolversion;
    }
    
    /**
      * Retrieves the value for the specified property.
      *
      * Properties can be specified in the TMX header,
      * please refer to the TMX specification for more information.
      *
      * @param  name - The name of the property to retrieve the value for
      * @return The value of the specified property, or null if the property is not present
      */
    public String getProperty(String name) {
        return m_properties.get(name);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // TMX Upgrades between OmegaT versions
    
    boolean upgradeCheckComplete = false;
    boolean upgrade14X = false;
    boolean upgradeSentSeg = false;
    
    /** 
     * Checks whether any compatibility upgrades are necessary for 
     * OmegaT-generated TMX files.
     */
    private void checkForUpgrades()
    {
        if (!upgradeCheckComplete)
        {
            if (getCreationTool()==null || getCreationToolVersion()==null)
                return; // we can't check
            
            if (CT_OMEGAT.equals(getCreationTool()))
            {
                upgrade14X = getCreationToolVersion().compareTo(CTV_OMEGAT_1) <= 0;
                upgradeSentSeg = SEG_PARAGRAPH.equals(getSegType()) && 
                        Core.getDataEngine().getProjectProperties().isSentenceSegmentingEnabled();
            }
            upgradeCheckComplete = true;
        }
    }
    
    /** 
     * Do we need to upgrade old TMX files with paragraph segmentation
     * to new sentence segmentation.
     * The upgrade simply breaks source and target segments into sentences,
     * and if there's the same number of segments in target as in source,
     * several segments are added to memory.
     */
    private boolean isUpgradeSentSeg()
    {
        checkForUpgrades();
        return upgradeSentSeg;
    }
    
    /** 
     * Do we need to upgrade old TMX files from OmegaT 1.4.x series.
     * The upgrade cannot be done absolutely reliably, but there're some
     * heuristics there... For example,
     * the old form can be "Bold&lt;/b1&gt; text here.", and the new form
     * should be "&lt;b0&gt;Bold&lt;/b0&gt; text here.".
     */
    private boolean isUpgrade14X()
    {
        checkForUpgrades();
        return upgrade14X;
    }
    
    /** 
     * Upgrades segment if required.
     */
    private String upgradeSegment(String segment)
    {
        if (isUpgrade14X()) // if that's 1.4.x, doing both upgrades...
            segment = upgradeOldTagsNumberingAndPairs(segment);
        return segment;
    }
    
    /** Internal class for OmegaT tag */
    class Tag
    {
        /** is this an ending tag, e.g. &lt;/b4&gt; */
        public boolean end;
        /** name of the tag, e.g. "b" for &lt;/b4&gt; */
        public String name;
        /** number of the tag, e.g. 4 for &lt;/b4&gt; */
        public int num;
        /** is this a standalone tag, e.g. &lt;br4/&gt; */
        public boolean alone;
        
        /** Creates a tag */
        public Tag(boolean end, String name, int num, boolean alone)
        {
            this.end = end;
            this.name = name;
            this.num = num;
            this.alone = alone;
        }
        
        /** String form. */
        public String toString()
        {
            return
                    "<" +                                                       // NOI18N
                    (end ? "/" : "") +                                          // NOI18N
                    name +
                    num +
                    (alone ? "/" : "") +                                        // NOI18N
                    ">";                                                        // NOI18N
        }
        
        /** 
         * String form of a paired tag: if this is a start tag,
         * returns corresponding end tag, if this is an end tag,
         * returns corresponding start tag, if this is a standalone tag,
         * returns the same as {@link #toString()}.
         */
        public String toStringPaired()
        {
            if (alone)
                return toString();
            else
                return
                        "<" +                                                   // NOI18N
                        (end ? "" : "/") +                                      // NOI18N
                        name +
                        num +
                        ">";                                                   // NOI18N
        }
    }
    
    /** 
     * Upgrades segments of OmegaT's 1.4.x series to new tag numbering,
     * and to new paired tag policy.
     */
    private String upgradeOldTagsNumberingAndPairs(String segment)
    {
        if (!PatternConsts.OMEGAT_TAG.matcher(segment).find())
            return segment;
        
        try
        {
            StringBuffer buf = new StringBuffer(segment);
            Matcher matcher = PatternConsts.OMEGAT_TAG_DECOMPILE.matcher(segment);

            int tagstart = matcher.start();
            int tagend = matcher.end();
            boolean end = matcher.group(1).length()>0;
            String name = matcher.group(2);
            int num = Integer.parseInt(matcher.group(3));
            boolean alone = matcher.group(4).length()>0;

            if (num==1)
                num = 0;

            Tag tag = new Tag(end, name, num, alone);

            List<Tag> unclosedTags = new ArrayList<Tag>();
            List<Tag> unopenedTags = new ArrayList<Tag>();

            Map<String,Tag> unclosedTagsNames = new HashMap<String, Tag>();
            Map<String,Tag> unopenedTagsNames = new HashMap<String,Tag>();
            if (end)
            {
                unopenedTags.add(tag);
                unopenedTagsNames.put(name, tag);
            }
            else if (!alone)
            {
                unclosedTags.add(tag);
                unclosedTagsNames.put(name, tag);
            }
            int maxnum = num;

            buf.replace(tagstart, tagend, tag.toString());

            while (matcher.find())
            {
                tagstart = matcher.start();
                tagend = matcher.end();
                end = matcher.group(1).length()>0;
                name = matcher.group(2);
                alone = matcher.group(4).length()>0;
                tag = new Tag(end, name, num, alone);

                if (end && unclosedTagsNames.containsKey(name))
                {
                    Tag starttag = unclosedTagsNames.get(name);
                    num = starttag.num;
                    unclosedTagsNames.remove(name);
                    unclosedTags.remove(starttag);
                }
                else
                {
                    num = maxnum + 1;
                    if (end)
                    {
                        unopenedTags.add(tag);
                        unopenedTagsNames.put(name, tag);
                    }
                    else if (!alone)
                    {
                        unclosedTags.add(tag);
                        unclosedTagsNames.put(name, tag);
                    }
                }
                if (maxnum < num)
                    maxnum = num;

                buf.replace(tagstart, tagend, tag.toString());
            }

            StringBuffer res = new StringBuffer();
            for (int i = unopenedTags.size()-1; i>0; i--)
            {
                tag = unopenedTags.get(i);
                res.append(tag.toStringPaired());
            }
            res.append(buf);
            for (int i = unclosedTags.size()-1; i>0; i--)
            {
                tag = unclosedTags.get(i);
                res.append(tag.toStringPaired());
            }

            return res.toString();
        }
        catch (Exception e)
        {
            return segment;
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    /** Collects a segment from TMX. Performs upgrades of a segment if needed. */
    private void storeSegment(String source, String translation)
    {
        source = upgradeSegment(source);
        translation = upgradeSegment(translation);
        
        if (isUpgradeSentSeg())
        {
            List<String> srcSegments = Segmenter.segment(sourceLang, source, null, null);
            List<String> tarSegments = Segmenter.segment(sourceLang, translation, null, null);

            int n = srcSegments.size();
            if( n==tarSegments.size() )
            {
                for(int j=0; j<n; j++)
                {
                    String srcseg = srcSegments.get(j);
                    String tarseg = tarSegments.get(j);
                    m_srcList.add(srcseg);
                    m_tarList.add(tarseg);
                }
            }
            else
            {
                m_srcList.add(source);
                m_tarList.add(translation);
            }
        }
        else
        {
            m_srcList.add(source);
            m_tarList.add(translation);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    private String segtype;
    /** Segment Type attribute value: "paragraph" */
    public static final String SEG_PARAGRAPH = "paragraph";                     // NOI18N
    /** Segment Type attribute value: "sentence" */
    public static final String SEG_SENTENCE = "sentence";                       // NOI18N
    /** Returns Segment Type attribute of TMX file */
    public String getSegType() 
    {
        return segtype;
    }
    
    /**
      * Determins if TMX level 2 codes should be included or skipped.
      */
    private void checkLevel2() {
        includeLevel2 = creationtool.equals(CT_OMEGAT);
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
    public void loadFile(String filename, boolean isProjectTMX) 
            throws IOException 
    {
        this.isProjectTMX = isProjectTMX;

        // Hack to ommit .tmp extensions from displayed file names,
        // so we don't confuse the user. Part of a quick fix for 1583560
        String displayFilename = filename.endsWith(".tmp")
                                     ? filename.substring(0, filename.length() -4)
                                     : filename;

        // parse the TMX file
        try 
        {
            // log the parsing attempt
            Log.logRB("TMXR_INFO_READING_FILE", new Object[]{displayFilename});

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
            InputSource is = new InputSource(new XMLReader(filename, m_encoding));
            is.setSystemId("");                                                 // NOI18N
            parser.parse(is, this);

            // if no source could be found for 1 or more TUs, log this fact
            // don't do this if the TMX file is the project TM
            if (!isProjectTMX && sourceNotFound)
                Log.logWarningRB("TMXR_WARNING_SOURCE_NOT_FOUND");

            // log the fact that parsing is done
            Log.logRB("TMXR_INFO_READING_COMPLETE");
            Log.log("");
        }
        catch (SAXParseException ex) {
            Log.logErrorRB(ex, "TMXR_FATAL_ERROR_WHILE_PARSING", ex
                    .getLineNumber(), ex.getColumnNumber());
            Core.getMainWindow().displayErrorRB(ex,
                    "TMXR_FATAL_ERROR_WHILE_PARSING", ex.getLineNumber(),
                    ex.getColumnNumber());
        } catch (Exception ex) {
            Log.logErrorRB(ex, "TMXR_EXCEPTION_WHILE_PARSING", displayFilename,
                    Log.getLogLocation());
            Core.getMainWindow().displayErrorRB(ex,
                    "TMXR_EXCEPTION_WHILE_PARSING", displayFilename,
                    Log.getLogLocation());
        }
    }

    /**
      * Receives notification of a parser warning. Called by SAX parser.
      */
    public void warning(SAXParseException exception) throws SAXException 
    {
        Log.logWarningRB("TMXR_WARNING_WHILE_PARSING",
            new Object[]{String.valueOf(exception.getLineNumber()),
                         String.valueOf(exception.getColumnNumber())});
        Log.log(exception);
    }

    /**
      * Receives notification of a recoverable XML parsing error. Called by SAX parser.
      */
    public void error(SAXParseException exception) throws SAXException 
    {
        Log.logErrorRB("TMXR_RECOVERABLE_ERROR_WHILE_PARSING",
            new Object[]{String.valueOf(exception.getLineNumber()),
                         String.valueOf(exception.getColumnNumber())});
        Log.log(exception);
    }
    
    /**
      * Receives notification of a fatal XML parsing error. Called by SAX parser.
      */
    public void fatalError(SAXParseException exception) throws SAXException 
    {
        Log.logErrorRB("TMXR_FATAL_ERROR_WHILE_PARSING",
            new Object[]{String.valueOf(exception.getLineNumber()),
                         String.valueOf(exception.getColumnNumber())});
        Log.log(exception);
    }

    /**
      * Receives notification of the start of the XML document. Called by SAX parser.
      */    
    public void startDocument() 
    {
        // initialise variables needed for parsing of the TMX file
        headerParsed   = false;
        inHeader       = false;
        inTU           = false;
        inTUV          = false;
        inSegment      = false;
        tuvs           = new ArrayList<TUV>();
        currentElement = new Stack<String>();
        currentSub     = new Stack<StringBuffer>();
        currentElement.push(TMX_TAG_NONE);
    }

    /**
      * Receives notification of the end of the XML document. Called by SAX parser.
      */
    public void endDocument() 
    {
        // deallocate temp storage
        tuvs           = null;
        currentElement = null;
        currentSub     = null;
    }

    /**
      * Receives notification of the start of an element. Called by SAX parser.
      */
    public void startElement(String     uri,
                             String     localName,
                             String     qName,
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
        else if (qName.equals(TMX_TAG_PROP))
            startElementProperty(attributes);
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
        else if (qName.equals(TMX_TAG_PROP))
            endElementProperty();
    }

    /**
      * Receives character data in element content. Called by the SAX parser.
      */
    public void characters(char[] ch,
                           int    start,
                           int    length) throws SAXException 
    {
        // only read (sub)segments, properties, and inline codes (if required)
        if (   inSegment
            || inProperty
            || (includeLevel2 && (currentElement.peek()).equals(TMX_TAG_INLINE)))
        {
            // append the data to the current buffer
            currentSub.peek().append(ch, start, length);
        }
    }

    /**
      * Receives ignorable whitespace in element content. Called by the SAX parser.
      */
    public void ignorableWhitespace(char[] ch,
                                    int    start,
                                    int    length) throws SAXException 
    {
        // only read (sub)segments, properties, and inline codes (if required)
        if (   inSegment
            || inProperty
            || (includeLevel2 && (currentElement.peek()).equals(TMX_TAG_INLINE)))
        {
            // append the data to the current buffer
            currentSub.peek().append(ch, start, length);
        }
    }

    /**
      * Handles the start of the header of a TMX file.
      */
    private void startElementHeader(Attributes attributes) 
    {
        // mark the current position as in the header
        inHeader = true;

        // get the header attributes
        creationtool        = attributes.getValue(TMX_ATTR_CREATIONTOOL);
        creationtoolversion = attributes.getValue(TMX_ATTR_CREATIONTOOLVERSION);
        segtype             = attributes.getValue(TMX_ATTR_SEGTYPE);
        tmxSourceLanguage   = attributes.getValue(TMX_ATTR_SRCLANG);

        // log some details
        Log.logRB("TMXR_INFO_CREATION_TOOL",         new Object[]{creationtool});
        Log.logRB("TMXR_INFO_CREATION_TOOL_VERSION", new Object[]{creationtoolversion});
        Log.logRB("TMXR_INFO_SEG_TYPE",              new Object[]{segtype});
        Log.logRB("TMXR_INFO_SOURCE_LANG",           new Object[]{tmxSourceLanguage});

        // give a warning if the TMX source language is
        // different from the project source language
        if (!tmxSourceLanguage.equalsIgnoreCase(sourceLanguage)) 
        {
            Log.logWarningRB("TMXR_WARNING_INCORRECT_SOURCE_LANG",
                new Object[]{tmxSourceLanguage, sourceLanguage});
        }

        // give a warning that TMX file will be upgraded from 1.4.x
        if (isUpgrade14X())
            Log.logWarningRB("TMXR_WARNING_UPGRADE_14X");

        // give a warning that TMX file will be upgraded to sentence segmentation
        if (isUpgradeSentSeg())
            Log.logWarningRB("TMXR_WARNING_UPGRADE_SENTSEG");

        // check if level 2 codes should be included (only for OmegaT 1.6.0-final and higher)
        checkLevel2();
    }

    /**
      * Handles the end of the TMX header
      */
    private void endElementHeader() {
        // mark the header as parsed
        headerParsed = true;

        // mark the current position as *not* in the header
        inHeader = false;

        // check for property OmegaT:VariantLanguages (only variants in these languages are read)
        // if present, parse the property value, and get each separate language
        String propVariantLanguages = getProperty(PROPERTY_VARIANT_LANGUAGES);
        if (propVariantLanguages != null) {
            int commaPos = 0;
            StringBuffer languages = new StringBuffer();
            do {
                // get the position of the next comma
                commaPos = propVariantLanguages.indexOf(',');

                // get the next language
                String language = commaPos > 0 ? propVariantLanguages.substring(0, commaPos).trim()
                                               : propVariantLanguages.trim();

                // add the language to the set, but trim off any region indicators
                // for simplicity's sake, and convert it to all uppercase
                if (language.length() > 0) {
                    m_variantLanguages.add(language.substring(0, 2).toUpperCase());
                    if (languages.length() > 0)
                        languages.append(", ");
                    languages.append(language.substring(0, 2).toUpperCase());
                }

                // trim the list of languages
                propVariantLanguages = propVariantLanguages.substring(commaPos + 1);
            }
            while (commaPos > 0);

            // Log presence of preferred variant languages
            Log.logRB("TMXR_INFO_VARIANT_LANGUAGES_DISPLAYED",
                new Object[]{languages.toString()});
        }
    }

    /**
      * Handles the start of a property
      */
    private void startElementProperty(Attributes attributes) throws SAXException {
        currentElement.push(TMX_TAG_PROP);

        // ignore all properties outside the TMX header
        if (!inHeader)
            return;

        // get the property name
        currentProperty = attributes.getValue(TMX_ATTR_TYPE);

        // put a property value buffer on the subsegment stack
        // (ISSUE: field currentSub should be renamed to reflect this more general use)
        currentSub.push(new StringBuffer());

        // mark the current position as in a property
        inProperty = true;
    }

    /**
      * Handles the end of a property
      */
    private void endElementProperty() {
        currentElement.pop();

        // mark the current position as *not* in a property
        inProperty = false;

        // ignore all properties outside the TMX header
        if (!inHeader)
            return;

        // add the current property to the property map
        StringBuffer propertyValue = currentSub.pop();
        m_properties.put(currentProperty, propertyValue.toString());

        currentProperty = null;
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
                
        // clear the TUV list
        tuvs.clear();
    }

    /**
      * Handles the end of a translation unit.
      */
    private void endElementTU() 
    {
        currentElement.pop();
        
        // mark the current position as *not* in a translation unit
        inTU = false;
        
        // determine source and (primary) target tuv
        TUV source   = null; // source TUV according to source language set by OmT
        TUV target   = null; // target TUV according to target language set by OmT
        TUV sourceC  = null; // candidate for source TUV according to source language set by OmT
        TUV targetC  = null; // candidate for target TUV according to target language set by OmT
        TUV sourceT  = null; // source TUV according to TMX source language
        TUV sourceTC = null; // candidate for source TUV according to TMX source language
        TUV targetT  = null; // target TUV according to TMX source language
        for (TUV tuv : tuvs) 
        {
            // first match TUV language against entire source language (lang code + reg code)
            if ((source == null) && tuv.language.equalsIgnoreCase(sourceLanguage))
                // the current TUV is the source
                source = tuv;
            // against entire target language           
            else if ((target == null) && tuv.language.equalsIgnoreCase(targetLanguage))
                // the current TUV is the target
                target = tuv;
            // against source language code only
            else if ((sourceC == null) && tuv.language.regionMatches(true, 0, sourceLanguage, 0, 2))
                // the current TUV is a candidate for the source
                sourceC = tuv;
            // against target language code only
            else if ((targetC == null) && tuv.language.regionMatches(true, 0, targetLanguage, 0, 2))
                // the current TUV is a candidate for the target
                targetC = tuv;
            // if nothing matches, then try matching against the TMX source language
            else if (isProjectTMX) 
            {
                // match against entire TMX source language
                if (   (sourceT == null)
                    && (   tuv.language.equalsIgnoreCase(tmxSourceLanguage)
                        || tmxSourceLanguage.equalsIgnoreCase("*all*")))   // NOI18N
                    // the current TUV is the source according to the TMX source language
                    sourceT = tuv;
                // match against TMX source language code only
                else if (   (sourceTC == null)
                         && tuv.language.regionMatches(true, 0, tmxSourceLanguage, 0, 2))
                    // the current TUV is a candidate for the source according to the TMX source language
                    sourceTC = tuv;
 
                if (   (targetT == null)
                    && !tuv.language.equalsIgnoreCase(tmxSourceLanguage))
                    targetT = tuv;
            }

            // stop looking for source and target if both have been located
            if ((source != null) && (target != null))
                break;
        }

        // determine which source TUV to use
        if (source == null)
            source = sourceC; // try source candidate
        if (source == null)
            source = sourceT; // try source according to TMX
        if (source == null)
            source = sourceTC; // try source candidate according to TMX

        // if no source was found, log a warning
        if (source == null) 
        {
            sourceNotFound = true;
            return;
        }

        // determine what target TUV to use
        // if none was found, create a temporary, empty one, for ease of coding
        if (target == null)
            target = targetC; // try target candidate
        if (target == null)
            target = targetT; // try target according to TMX
        if (target == null)
            target = new TUV(); // empty target

        // store the source & target segment
        storeSegment(source.text.toString(), target.text.toString());

        // store the source & target sub segments
        // create exactly as many target subs as there are source subs
        // "pad" with empty strings, or ommit segments if necessary
        // NOTE: this is not the most ideal solution, but the best possible
        for (int i = 0; i < source.subSegments.size(); i++) 
        {
            String starget = new String();
            if (i < target.subSegments.size())
                starget = target.subSegments.get(i).toString();
            storeSegment(source.subSegments.get(i).toString(), starget);
        }

        // store alternative translations
        if (!isProjectTMX) {
            for (TUV tuv : tuvs) {
                // store the TUV as alternative if it's source nor target
                // and its language appears in the set of useful variant languages
                if (   (tuv != source)
                    && (tuv != target)
                    && (   m_variantLanguages.isEmpty()
                        || m_variantLanguages.contains(tuv.language.substring(0, 2).toUpperCase())))
                   storeSegment(source.text.toString(), tuv.text.toString());
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
            Log.logWarningRB("TMXR_WARNING_TUV_NOT_IN_TU");
            return;
        }
    
        // get the language of the tuv
        // try "xml:lang" first, then "lang"
        String language = attributes.getValue(TMX_ATTR_LANG_NS);
        if (language == null)
            language = attributes.getValue(TMX_ATTR_LANG);

        // if the language is not specified, skip the tuv
        if (language == null) 
        {
            Log.logWarningRB("TMXR_WARNING_TUV_LANG_NOT_SPECIFIED");
            return;
        }

        // put a new TUV in the TUV list
        TUV tuv = new TUV();
        tuv.language = language;
        tuvs.add(tuv);
        
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
            Log.logWarningRB("TMXR_WARNING_SEG_NOT_IN_TUV");
            return;
        }

        // put the current TUV's segment buffer on the subsegment stack
        // (ISSUE: field currentSub should be renamed to reflect this more general use)
        currentSub.push(tuvs.get(tuvs.size() - 1).text);

        // mark the current position as in a segment
        inSegment = true;
    }

    /**
      * Handles the end of a segment.
      */
    private void endElementSegment() 
    {
        currentElement.pop();
        
        // do nothing if not in a TUV
        if (!inTUV)
            return;

        // remove the current TUV's segment buffer from the subsegment stack
        currentSub.pop();

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
        
        // create new entries in the current TUV's sub segment list and on the stack
        // NOTE: the assumption is made here that sub segments are
        // in the same order in both source and target segments
        StringBuffer sub = new StringBuffer();                                  // NOI18N
        tuvs.get(tuvs.size() - 1).subSegments.add(sub);
        currentSub.push(sub);
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
    public org.xml.sax.InputSource resolveEntity(String publicId,
                                                 String systemId) 
                                                 throws SAXException 
    {
        // simply return an empty dtd
        return new org.xml.sax.InputSource(new java.io.StringReader(""));       // NOI18N
    }

    // Constants for certain TMX tag names/attributes
    private final static String TMX_TMX_TAG    = "tmx";                         // NOI18N
    private final static String TMX_TAG_HEADER = "header";                      // NOI18N
    private final static String TMX_TAG_BODY   = "body";                        // NOI18N
    private final static String TMX_TAG_TU     = "tu";                          // NOI18N
    private final static String TMX_TAG_TUV    = "tuv";                         // NOI18N
    private final static String TMX_TAG_SEG    = "seg";                         // NOI18N
    private final static String TMX_TAG_BPT    = "bpt";                         // NOI18N
    private final static String TMX_TAG_EPT    = "ept";                         // NOI18N
    private final static String TMX_TAG_HI     = "hi";                          // NOI18N
    private final static String TMX_TAG_IT     = "it";                          // NOI18N
    private final static String TMX_TAG_PH     = "ph";                          // NOI18N
    private final static String TMX_TAG_UT     = "ut";                          // NOI18N
    private final static String TMX_TAG_SUB    = "sub";                         // NOI18N
    private final static String TMX_TAG_PROP   = "prop";                        // NOI18N
    private final static String TMX_TAG_INLINE = "inline"; // made up for convenience // NOI18N
    private final static String TMX_TAG_NONE   = "none";   // made up for convenience // NOI18N

    private final static String TMX_ATTR_LANG                = "lang";          // NOI18N
    private final static String TMX_ATTR_LANG_NS             = "xml:lang";      // NOI18N
    private final static String TMX_ATTR_CREATIONTOOL        = "creationtool";  // NOI18N
    private final static String TMX_ATTR_CREATIONTOOLVERSION = "creationtoolversion";   // NOI18N
    private final static String TMX_ATTR_SEGTYPE             = "segtype";       // NOI18N
    private final static String TMX_ATTR_SRCLANG             = "srclang";       // NOI18N
    private final static String TMX_ATTR_TYPE                = "type";          // NOI18N

    private final static String PROPERTY_VARIANT_LANGUAGES = "OmegaT:VariantLanguages"; // NOI18N

    private String  m_encoding;
    private List<String>    m_srcList;
    private List<String>    m_tarList;
    private Map<String,String>     m_properties;       // Map<String, String> of TMX properties, specified in the header
    private Set<String>     m_variantLanguages; // Set of (user) acceptable variant languages
    private Language sourceLang, targetLang;
    private String  sourceLanguage;     // Language/country code set by OmT: LL(-CC)
    private String  targetLanguage;     // Language/country code set by OmT: LL(-CC)
    private String  tmxSourceLanguage;  // Language/country code as specified in TMX header: LL(-CC)
    private boolean includeLevel2;      // True if TMX level 2 markup must be interpreted
    private boolean isProjectTMX;       // True if the TMX file being loaded is the project TMX
    private boolean headerParsed;       // True if the TMX header has been parsed correctly
    private boolean inHeader;           // True if the current parsing point is in the HEADER element
    private boolean inProperty;         // True if in a PROP element
    private boolean inTU;               // True if in a TU element
    private boolean inTUV;              // True if in a TUV element
    private boolean inSegment;          // True if in a SEG element
    private boolean sourceNotFound;     // True if no source segment was found for one or more TUs
    private List<TUV>    tuvs;               // Contains all TUVs of the current TU
    private Stack<String>   currentElement;     // Stack of tag names up to the current parsing point
    private Stack<StringBuffer>   currentSub;         // Stack of sub segment buffers
    private String  currentProperty;    // Name of the current property being parsed (null if none)

    /**
      * Internal class to represent translation unit variants
      */
    private class TUV 
    {
        /**
          * Language and (optional) country code: LL(C-CC)
          */
        public String language;
        
        /**
          * Segment text
          */
        public StringBuffer text;
        
        /**
          * Contains StringBuffers for subsegments
          */
        public ArrayList<StringBuffer> subSegments;
        
        /**
          * Default constructor
          */
        public TUV() 
        {
            super();
            text = new StringBuffer();
            subSegments = new ArrayList<StringBuffer>();
        }
    }
    
}

