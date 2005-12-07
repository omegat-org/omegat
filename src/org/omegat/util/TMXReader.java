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
import java.text.MessageFormat;
import java.util.ArrayList;

import org.omegat.filters2.TranslationException;
import org.omegat.filters2.xml.XMLBlock;
import org.omegat.filters2.xml.XMLStreamReader;

/**
 * Class that loads up TMX 1.1 (Translation Memory) files.
 * <p>
 * Since OmegaT 1.6 does not check the TMX version.
 *
 * @author Keith Godfrey
 */
public class TMXReader
{
    
    /** 
     * Creates a new TMX Reader.
     * 
     * @param encoding -- encoding to allow specification of alternative encodings (i.e. wordfast)
     */
    public TMXReader(String encoding)
    {
        m_reader = new XMLStreamReader();
        m_reader.killEmptyBlocks();
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
    
    /** Loads only the header of TMX file */
    public void loadHeader(String filename) throws IOException, TranslationException
    {
        m_reader.setStream(filename, m_encoding);
        
        XMLBlock blk;
        
        // advance to tmx tag
        if ((blk = m_reader.advanceToTag("tmx")) == null) // NOI18N
        {
            throw new TranslationException(
                    MessageFormat.format(OStrings.getString("TMXR_ERROR_INVALID_TMX"),
                    new Object[]{filename}));
        }
        
        // advance to header
        if ((blk=m_reader.advanceToTag("header")) == null)			// NOI18N
        {
            throw new TranslationException( MessageFormat.format(
                    OStrings.getString("TMXR_ERROR_INVALID_TMX"),
                    new Object[]{filename}) );
        }
        
        creationtool = blk.getAttribute("creationtool");                        // NOI18N
        creationtoolversion = blk.getAttribute("creationtoolversion");          // NOI18N
        segtype = blk.getAttribute("segtype");                                  // NOI18N
        sourceLanguage = blk.getAttribute("srclang");                           // NOI18N
    }
    
    /** Loads the TMX */
    public void loadFile(String filename) throws IOException, TranslationException
    {
        loadHeader(filename);

        XMLBlock blk;
        ArrayList lst;
        m_tarList.clear();
        m_srcList.clear();
        
        // advance to body
        if (m_reader.advanceToTag("body") == null)				// NOI18N
        {
            throw new TranslationException( MessageFormat.format(
                    OStrings.getString("TMXR_ERROR_INVALID_TMX"),
                    new Object[]{filename}) );
        }
        
        int seg = 0;
        int ctr;
        int srcPos;
        String tarSeg;
        String srcSeg;
        String lang;
        while (true)
        {
            seg++;
            // advance to next tu element
            if ((blk=m_reader.advanceToTag("tu")) == null)			// NOI18N
                break;
            
            lst = m_reader.closeBlock(blk);
            tarSeg = "";							// NOI18N
            srcSeg = "";							// NOI18N
            
            try
            {
                // now go through tu block
                // accept first non-src lang as target
                srcPos = -1;
                ctr = 0;
                
                // tuv 1
                while (!blk.getTagName().equals("tuv"))                         // NOI18N
                    blk = (XMLBlock) lst.get(ctr++);
                lang = blk.getAttribute("lang");                                // NOI18N
                if( lang==null )
                    lang = blk.getAttribute("xml:lang");                        // NOI18N
                
                if( lang==null )
                {
                    String blktext = "<segment text not found>";                // NOI18N
                    try
                    {
                        // advance to segment marker
                        while( !blk.getTagName().equals("seg") )                // NOI18N
                            blk = (XMLBlock) lst.get(ctr++);
                        blktext = blk.getText();
                    }
                    catch( Exception e )
                    { }
                    
                    // source language segment not specified
                    StaticUtils.log(
                            MessageFormat.format(
                            OStrings.getString("TMX_READER_WARNING_Language_Attribute_Missing"),
                            new Object[]{blktext}) );
                            continue;
                }
                
                if (sourceLanguage.regionMatches(0, lang, 0, 2))
                    srcPos = 0;
                else if( targetLanguage==null )
                    targetLanguage = lang;
                
                // advance to segment marker
                while (!blk.getTagName().equals("seg"))				// NOI18N
                    blk = (XMLBlock) lst.get(ctr++);
                
                // next non-tag block is text
                blk = (XMLBlock) lst.get(ctr++);
                while (blk.isTag())
                    blk = (XMLBlock) lst.get(ctr++);
                if (srcPos == 0)
                    srcSeg = blk.getText();
                else
                    tarSeg = blk.getText();
                
                // close tuv tag
                while (!blk.getTagName().equals("tuv"))                         // NOI18N
                    blk = (XMLBlock) lst.get(ctr++);
                
                // open next tuv tag
                blk = (XMLBlock) lst.get(ctr++);
                while (!blk.getTagName().equals("tuv"))                         // NOI18N
                    blk = (XMLBlock) lst.get(ctr++);
                
                lang = blk.getAttribute("lang");                                // NOI18N
                if( lang==null )
                    lang = blk.getAttribute("xml:lang");                        // NOI18N
                
                if( lang==null )
                {
                    String blktext = "<segment text not found>";                // NOI18N
                    try
                    {
                        // advance to segment marker
                        while( !blk.getTagName().equals("seg") )                // NOI18N
                            blk = (XMLBlock) lst.get(ctr++);
                        blktext = blk.getText();
                    }
                    catch( Exception e )
                    { }
                    
                    // source language segment not specified
                    StaticUtils.log(
                            MessageFormat.format(
                            OStrings.getString("TMX_READER_WARNING_Language_Attribute_Missing"),
                            new Object[]{blktext}) );
                            continue;
                }
                
                if (sourceLanguage.regionMatches(0, lang, 0, 2))
                {
                    if (srcPos == -1)
                        srcPos = 1;
                    else 
                    {
                        // target language segment not specified
                        StaticUtils.log( MessageFormat.format(
                                OStrings.getString("TMXR_WARNING_UNABLE_TO_LOCATE_SRC_LANG"), 
                                new Object[]{new Integer(seg)}) );
                        continue;
                    }
                }
                else if (srcPos == -1)
                {
                    // source language segment not specified
                    StaticUtils.log( MessageFormat.format(
                            OStrings.getString("TMXR_WARNING_UNABLE_TO_LOCATE_TARGET_LANG"), 
                            new Object[]{new Integer(seg)}) );
                    continue;
                }
                else if( targetLanguage==null )
                    targetLanguage = lang;
                
                // advance to segment marker
                while (!blk.getTagName().equals("seg"))					// NOI18N
                    blk = (XMLBlock) lst.get(ctr++);
                
                // next non-tag block is text
                blk = (XMLBlock) lst.get(ctr++);
                while (blk.isTag())
                    blk = (XMLBlock) lst.get(ctr++);
                if (srcPos == 1)
                    srcSeg = blk.getText();
                else
                    tarSeg = blk.getText();
                
                // ignore the rest
            }
            catch (Exception e)
            {
                StaticUtils.log( MessageFormat.format(
                        OStrings.getString("TMXR_WARNING_SKIPPING_SEGMENT"), 
                        new Object[]{new Integer(seg), filename}) );
                continue;
            }
            
            m_srcList.add(srcSeg);
            m_tarList.add(tarSeg);
        }
        
        m_reader.close();
    }
    
    private XMLStreamReader m_reader;
    private String          m_encoding;
    private ArrayList       m_srcList;
    private ArrayList       m_tarList;
}


