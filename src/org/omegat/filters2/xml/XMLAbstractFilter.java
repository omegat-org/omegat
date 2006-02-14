/**************************************************************************
 * OmegaT - Java based Computer Assisted Translation (CAT) tool
 * Copyright (C) 2002-2005  Keith Godfrey et al
 * keithgodfrey@users.sourceforge.net
 * 907.223.2039
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.filters2.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.xml.DefaultEntityFilter;
import org.omegat.util.StaticUtils;

/**
 * Filter to handle plain XML files.
 * This filter is usually used as a basic class for specific filters of
 * XML-syntaxed documents (like OpenOffice).
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public abstract class XMLAbstractFilter extends AbstractFilter
{
    /**
     * Creates an abstract XML filter.
     * This filter is not usable standalone.
     */
    public XMLAbstractFilter()
    {
        m_preTextList = new ArrayList();
        m_textList = new ArrayList();
        m_postTextList = new ArrayList();
        m_tagMap = new HashMap();
        
        m_formatList = new ArrayList();
        m_formatDisplayList = new ArrayList();
        // not working in 1.4.5
        // m_verbatumList = new ArrayList();
        m_compressWhitespace = false;
    }
    
    public void reset()
    {
        m_preTextList.clear();
        m_textList.clear();
        m_postTextList.clear();
        m_tagMap.clear();
    }
    
    /**
     * Defines a tag that will be transferred into target text without 
     * any changes.
     */
    // not working in 1.4.5
    /*
    protected void defineVerbatumTag(String tag)
    {
        m_verbatumList.add(tag);
    }
     */
    
    /**
     * Defines formatting tags, that don't count for segment breaks.
     * E.g. bold, italic, other font modifiers.
     */
    protected void defineFormatTag(String tag, String display)
    {
        m_formatList.add(tag);
        m_formatDisplayList.add(display);
    }
    
    public void processFile(BufferedReader infile, BufferedWriter outfile)
            throws IOException, TranslationException
    {
        int i;
        String s;
        
        XMLStreamReader xml = new XMLStreamReader();
        xml.setEntityFilter(entityFilter);
        xml.compressWhitespace(m_compressWhitespace);
        xml.breakOnWhitespace(m_breakWhitespace);
        XMLBlock blk;
        xml.setStream(infile);
        
        // to keep track of blocks in current segment
        m_preTextList.clear();
        m_textList.clear();
        m_postTextList.clear();
        m_tagMap.clear();
        
        // this will be set to either text list or pretext list depending
        //	on whether text has been found or not
        ArrayList target = m_preTextList;
        
        // write original XML preamble as first block
        m_preTextList.add(xml.getHeadBlock());
        
        while ((blk = xml.getNextBlock()) != null)
        {
            if (target == m_preTextList)
            {
                if (blk.hasText() && !blk.isComment())
                {
                    // first real text encountered - switch list
                    target = m_textList;
                    target.add(blk);
                }
                else
                {
                    // all tags before text are stored in pre list
                    target.add(blk);
                }
            }
            else if (!blk.isTag())
            {
                target.add(blk);
            }
            else
            {
                // tag encountered
                
                // not working in 1.4.5
                /*
                // first cycle through verbatum tag list to see if match
                for (i=0; i<m_verbatumList.size(); i++)
                {
                    s = (String) m_verbatumList.get(i);
                    if (blk.getTagName().equals(s))
                    {
                        // store the identifying tag
                        target.add(blk);
                        
                        // give it a shortcut
                        //! s = (String) m_verbatumDisplayList.get(i);
                        blk.setShortcut(s);
                        
                        // copy everything until close block
                        // give imbedded format tags a shortcut
                        ArrayList lst = xml.closeBlock(blk, true);
                        XMLBlock openBlock = blk;
                        for (int j=0; j<lst.size(); j++)
                        {
                            blk = (XMLBlock) lst.get(j);
                            // if format tag, write shortcut
                            if (target == m_textList &&
                                    (blk.isTag() || blk.isComment()))
                            {
                                for (int k=0; k<m_formatList.size(); k++)
                                {
                                    s = (String) m_formatList.get(k);
                                    if (blk.getTagName().equals(s))
                                    {
                                        s = (String)
                                        m_formatDisplayList.get(k);
                                        blk.setShortcut(s);
                                        break;
                                    }
                                }
                            }
                            
                            target.add(blk);
                            if (target == m_preTextList &&
                                    blk.hasText())
                            {
                                // text encountered - switch to correct
                                //	list (if not already done so)
                                target = m_textList;
                            }
                        }
                        blk.setShortcut(openBlock.getShortcut());
                        
                        break;
                    }
                }
                // verbatum block handled - continue with new block
                if (i < m_verbatumList.size())
                    continue;
                 */
                
                // cycle through format tag list to see if match
                for (i=0; i<m_formatList.size(); i++)
                {
                    s = (String) m_formatList.get(i);
                    if (blk.getTagName().equals(s))
                    {
                        // give it a shortcut
                        s = (String) m_formatDisplayList.get(i);
                        blk.setShortcut(s);
                        
                        target.add(blk);
                        break;
                    }
                }
                // block handled - continue fresh processing of next
                if (i < m_formatList.size())
                    continue;
                
                // if we've made it this far it must be a structural tag
                // consolidate lists and write entry
                // move empty blocks at end of text list to post list
                XMLBlock end;
                for (i=m_textList.size()-1; i>=0; i--)
                {
                    end = (XMLBlock) m_textList.get(i);
                    if (!end.hasText())
                    {
                        m_postTextList.add(0, end);
                        m_textList.remove(i);
                    }
                    else
                    {
                        // last element is text - nothing there to move
                        break;
                    }
                }
                writeEntry(xml, outfile, blk);
                m_preTextList.clear();
                m_textList.clear();
                m_postTextList.clear();
                m_tagMap.clear();
                target = m_preTextList;
            }
        }
        if (m_preTextList.size() > 0)
            writeEntry(xml, outfile, null);
    }

    /** Collects tags together, translates some text and writes it to target file. */
    private void writeEntry(XMLStreamReader xmlsr, BufferedWriter m_outFile, XMLBlock breaker) 
            throws IOException
    {
        // if there's nothing interesting and no outfile, ignore it
        if (m_textList.size() == 0 && m_outFile == null)
        {
            m_preTextList.clear();
            m_postTextList.clear();
            m_tagMap.clear();
            
            return;
        }
        
        // write out ignored leading tags
        if (m_preTextList.size() > 0 && m_outFile != null)
        {
            ListIterator it = m_preTextList.listIterator();
            while (it.hasNext())
            {
                XMLBlock blk = (XMLBlock) it.next();
                String str = blk.getText();
                if (m_compressWhitespace)
                    str += "\n";	// NOI18N
                m_outFile.write(str, 0, str.length());
            }
        }
        
        // process display text
        if (m_textList.size() > 0)
        {
            int tag_number = 0;
            StringBuffer out = new StringBuffer();
            int len = m_textList.size();
            for(int i=0; i<len; i++)
            {
                XMLBlock blk = (XMLBlock) m_textList.get(i);
                // We need to "shorcutize" tags
                if (blk.isTag())
                {
                    boolean increment_tag_number = true;
                    int this_tag_number = tag_number;
                    // if this is a closing tag, trying to lookup
                    if( blk.isClose() )
                    {
                        int depth = 1;
                        for(int j=i-1; j>=0; j--)
                        {
                            XMLBlock open = (XMLBlock) m_textList.get(j);
                            if( open.isTag() && !open.isStandalone() &&
                                    open.getTagName().equals(blk.getTagName()) )
                            {
                                if( open.isClose() )
                                    depth++;
                                else
                                    depth--;
                                if( depth==0 )
                                {
                                    this_tag_number = open.getShortcutNumber();
                                    increment_tag_number = false;
                                    break;
                                }
                            }
                        }
                    }
                    if( increment_tag_number )
                        tag_number++;
                    
                    blk.setShortcutNumber(this_tag_number);
                    String display = blk.getShortcut() + this_tag_number;
                    if( blk.isStandalone() )
                        display+="/";
                    m_tagMap.put(display, blk.getText());
                    display = "<" + display + ">";	// NOI18N
                    out.append(display);
                }
                else
                {
                    out.append(blk.getText());
                }
            }
            String translation = processEntry(out.toString());
            String formatted = formatString(xmlsr, translation);
            m_outFile.write(formatted);
        }
        
        // write out ignored trailing tags
        if (m_postTextList.size() > 0 && m_outFile != null)
        {
            ListIterator it = m_postTextList.listIterator();
            while (it.hasNext())
            {
                XMLBlock blk = (XMLBlock) it.next();
                String str = blk.getText();
                m_outFile.write(str, 0, str.length());
            }
        }
        
        if (m_outFile != null && breaker != null)
        {
            String str;
            if (m_compressWhitespace)
                str = "\n" + breaker.getText();		// NOI18N
            else
                str = breaker.getText();
            m_outFile.write(str, 0, str.length());
        }
    }
    
    protected void compressWhitespace()
    {
        m_compressWhitespace = true;
    }
    
    protected void breakWhitespace()
    {
        m_breakWhitespace = true;
    }
    
    /**
     * Convert simplified formatting tags to full originals.
     * Version 2.
     *
     * @author Maxym Mykhalchuk
     */
    private String formatString(XMLStreamReader xmlsr, String text)
    {
        StringBuffer res = new StringBuffer(text.length()*2);
        for(int i=0; i<text.length(); i++)
        {
            char c = text.charAt(i);
            if( c=='<' )
            {
                try
                {
                    int tagend = text.indexOf('>', i);
                    if( tagend>i+2 )
                    {
                        String inside = text.substring(i+1, tagend);
                        String originalTag = (String) m_tagMap.get(inside);
                        if( originalTag!=null )
                        {
                            res.append(originalTag);
                            i=tagend;
                            continue;
                        }
                    }
                }
                catch( StringIndexOutOfBoundsException sioob )
                { } // do nothing
                catch( Exception e )
                {
                    // strange, as we handled all the possibilities above
                    StaticUtils.log("Exception: " + e);                      // NOI18N
                }
            }
            // else it's not a tag, so char as, converting to escaped form
            res.append(xmlsr.makeValidXML(c));
        }
        return res.toString();
    }
    
    public void setEntityFilter(DefaultEntityFilter filter)
    {
        entityFilter = filter;
    }
    
    private ArrayList	m_preTextList;
    private ArrayList	m_textList;
    private ArrayList	m_postTextList;
    
    private HashMap	m_tagMap;	// associate block shortcut with text
    
    private ArrayList	m_formatList;
    private ArrayList	m_formatDisplayList;
    // not working in 1.4.5
    // private ArrayList	m_verbatumList;
    
    private boolean	m_compressWhitespace;
    private boolean	m_breakWhitespace;
    private DefaultEntityFilter	entityFilter;
}
