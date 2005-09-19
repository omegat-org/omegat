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

package org.omegat.filters2.html2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.visitors.NodeVisitor;

import org.omegat.util.PatternConsts;

/**
 *
 * @author Maxym Mykhalchuk
 */
class FilterVisitor extends NodeVisitor
{
    private HTMLFilter2 filter;
    private BufferedWriter writer;
    public FilterVisitor(HTMLFilter2 htmlfilter, BufferedWriter bufwriter)
    {
        this.filter = htmlfilter;
        this.writer = bufwriter;
    }

    /////////////////////////////////////////////////////////////////////////
    // Variable declaration
    /////////////////////////////////////////////////////////////////////////
    
    /** Should the parser call us for this tag's ending tag and its inner tags. */
    boolean recurse = true;
    
    /** Do we collect the translatable text now. */
    boolean text = false;
    /** The translatable text being collected. */
    StringBuffer paragraph;
    /** Did the PRE block start (it means we mustn't compress the spaces). */
    boolean preformatting = false;
    
    /** 
     * The list of non-paragraph tags following a chunk of text.
     * <ul>
     * <li>If another chunk of text follows, they get appended to the translatable paragraph,
     * <li>Otherwise (paragraph tag follows), they are written out directly.
     * </ul>
     */
    ArrayList afters;
    
    /** The tags behind the shortcuts */
    ArrayList s_tags;
    /** The list of all the tag shortcuts */
    ArrayList s_shortcuts;
    /** The number of shortcuts stored */
    int s_nshortcuts;
    
    /**
     * Self traversal predicate.
     * @return <code>true</code> if a node itself is to be visited.
     */
    public boolean shouldRecurseSelf()
    {
        return recurse;
    }

    /**
     * Depth traversal predicate.
     * @return <code>true</code> if children are to be visited.
     */
    public boolean shouldRecurseChildren()
    {
        return recurse;
    }

    /**
     * Called for each <code>Tag</code> visited.
     * @param tag The tag being visited.
     */
    public void visitTag(Tag tag)
    {
        if( isIntactTag(tag) )
        {
            if( text )
                endup();
            writeout(tag.toHtml());
            if( tag.getEndTag()!=null )
                recurse = false;
        }
        else
        {
            // recurse = true;
            if( isParagraphTag(tag) && text )
                endup();

            if( isPreformattingTag(tag) )
                preformatting = true;

            if( text )
                queueTranslatable(tag);
            else
                writeout("<"+tag.getText()+">");                                // NOI18N
        }
    }

    boolean firstcall = true;
    
    /**
     * Called for each chunk of text (<code>StringNode</code>) visited.
     * @param string The string node being visited.
     */
    public void visitStringNode(Text string)
    {
        recurse = true;
        String trimmedtext = string.getText().trim();
        if( trimmedtext.length()>0 )
        {
            // Hack around HTMLParser not being able to handle XHTML 
            // RFE pending: 
            // http://sourceforge.net/tracker/index.php?func=detail&aid=1227222&group_id=24399&atid=381402
            if( firstcall && PatternConsts.XML_HEADER.matcher(trimmedtext).matches() )
            {
                writeout(string.toHtml());
                return;
            }
            
            if(!text)
                initnow();
            text = true;
            firstcall = false;
        }

        
        if( text )
            queueTranslatable(string);
        else
            writeout(string.toHtml());
    }

    /**
     * Called for each comment (<code>RemarkNode</code>) visited.
     * @param remark The remark node being visited.
     */
    public void visitRemarkNode(Remark remark)
    {
        recurse = true;
        if( text )
            endup();
        writeout(remark.toHtml());
    }

    /**
     * Called for each end <code>Tag</code> visited.
     * @param tag The end tag being visited.
     */
    public void visitEndTag(Tag tag)
    {
        recurse = true;
        if( isParagraphTag(tag) && text )
            endup();
        if( isPreformattingTag(tag) )
            preformatting = false;

        if( text )
            queueTranslatable(tag);
        else
            writeout(tag.toHtml());
    }

    /**
     * This method is called before the parsing.
     */
    public void beginParsing()
    {
        initnow();
    }
    
    /**
     * Called upon parsing completion.
     */
    public void finishedParsing()
    {
        if( text )
            endup();
    }
    
    
    /** Does the tag lead to starting (ending) a paragraph. */
    private boolean isParagraphTag(Tag tag)
    {
        String tagname = tag.getTagName();
        return
                // Bugfix for http://sourceforge.net/support/tracker.php?aid=1288756
                // ADDRESS tag is also a paragraph tag
                tagname.equals("ADDRESS") ||                                    // NOI18N
                tagname.equals("BLOCKQUOTE") ||                                 // NOI18N
                tagname.equals("BODY") ||                                       // NOI18N
                tagname.equals("CENTER") ||                                     // NOI18N
                tagname.equals("H1") || tagname.equals("H2") ||                 // NOI18N
                    tagname.equals("H3") || tagname.equals("H4") ||             // NOI18N
                    tagname.equals("H5") || tagname.equals("H6") ||             // NOI18N
                tagname.equals("HTML") ||                                       // NOI18N
                tagname.equals("HEAD") || tagname.equals("TITLE") ||            // NOI18N
                tagname.equals("TABLE") || tagname.equals("TR") ||              // NOI18N
                    tagname.equals("TD") || tagname.equals("TH") ||             // NOI18N
                tagname.equals("P") ||                                          // NOI18N
                tagname.equals("PRE") ||                                        // NOI18N
                tagname.equals("OL") || tagname.equals("UL") ||                 // NOI18N
                    tagname.equals("LI") ||                                     // NOI18N
                tagname.equals("FORM") || tagname.equals("TEXTAREA")            // NOI18N
                ;
    }
    
    /** Should a contents of this tag be kept intact? */
    private boolean isIntactTag(Tag tag)
    {
        String tagname = tag.getTagName();
        return
                tagname.equals("!DOCTYPE") ||                                   // NOI18N
                tagname.equals("STYLE") ||                                      // NOI18N
                tagname.equals("META") ||                                       // NOI18N
                tagname.equals("SCRIPT") ||                                     // NOI18N
                tagname.equals("OBJECT") ||                                     // NOI18N
                tagname.equals("EMBED")                                         // NOI18N
                ;
    }
    
    /** Is the tag space-preserving? */
    private boolean isPreformattingTag(Tag tag)
    {
        String tagname = tag.getTagName();
        return
                tagname.equals("PRE") ||                                        // NOI18N
                tagname.equals("TEXTAREA")                                      // NOI18N
                ;
    }
    
    /** Writes something to writer. */
    private void writeout(String something)
    {
        try
        {
            writer.write(something);
        }
        catch( IOException ioe )
        {
            System.out.println(ioe);
        }
    }
    
    /**
     * Ends the segment collection and sends the translatable text out 
     * to OmegaT core,
     * and some extra tags to writer.
     */
    private void endup()
    {
        String para;
        para = paragraph.toString();
        
        if( !preformatting )
        {
            // compressing the space if this paragraph wasn't inside <PRE> tag
            para = compressSpaces(para);
        }
        
        // getting the translation
        para = filter.privateProcessEntry(para);
        
        // converting & < and > into &amp; &lt; and &gt; respectively
        // note that this doesn't change < and > of tag shortcuts
        para = charsToEntities(para);
        
        // expands tag shortcuts into full-blown tags
        para = unshorcutize(para);
        
        // writing out the paragraph into target file
        writeout(para);
        
        // writing out the tags that followed
        for(int i=0; i<afters.size(); i++)
            writeout(((Node)afters.get(i)).toHtml());
        
        initnow();
    }
    
    /**
     * Inits a new paragraph.
     */
    private void initnow()
    {
        text = false;
        recurse = true;
        paragraph = new StringBuffer();
        afters = new ArrayList();
        s_tags = new ArrayList();
        s_shortcuts = new ArrayList();
        s_nshortcuts = 0;
    }
    
    /**
     * Creates a shortcut for the tag.
     */
    private String shortcut(Tag tag)
    {
        StringBuffer result = new StringBuffer();
        result.append('<');
        int n = -1;
        if(tag.isEndTag())
        {
            result.append('/');
            // trying to lookup for appropriate starting tag
            int recursion = 1;
            for(int i=s_tags.size()-1; i>=0; i--)
            {
                Tag othertag = (Tag)s_tags.get(i);
                if( othertag.getTagName().equals(tag.getTagName()) )
                {
                    if( othertag.isEndTag() )
                        recursion++;
                    else
                    {
                        recursion--;
                        if( recursion==0 )
                        {
                            // we've found a starting tag for this ending one !!!
                            n = i;
                            break;
                        }
                    }
                }
            }
            if( n<0 )
            {
                // ending tag without a starting one
                n = s_nshortcuts;
                s_nshortcuts++;
            }
        }
        else
        {
            n = s_nshortcuts;
            s_nshortcuts++;
        }

        result.append(Character.toLowerCase(tag.getTagName().charAt(0)));
        result.append(n);
        if(tag.isEmptyXmlTag())
            result.append('/');
        result.append('>');
        
        return result.toString();
    }
    
    /**
     * Recovers shortcuts into full tags.
     */
    private String unshorcutize(String str)
    {
        for(int i=0; i<s_shortcuts.size(); i++)
        {
            String shortcut = (String)s_shortcuts.get(i);
            int pos = str.indexOf(shortcut);
            Tag tag = (Tag)s_tags.get(i);
            try
            {
                str = str.substring(0, pos) + 
                        "<" + tag.getText() + ">" +                             // NOI18N
                        str.substring(pos+shortcut.length());
            }
            catch( StringIndexOutOfBoundsException sioobe )
            {
                // nothing, string doesn't change
            }
        }
        return str;
    }
    
    /** Appends something to the translatable paragraph text. */
    private void appendTranslatable(Node something)
    {
        if( something instanceof Tag )
        {
            String shortcut = shortcut((Tag)something);
            s_tags.add(something);
            s_shortcuts.add(shortcut);
            paragraph.append(shortcut);
        }
        else if( something instanceof Text )
        {
            paragraph.append(
                    entitiesToChars(something.toHtml()));
        }
        else
            System.out.println("ERROR");                                        // NOI18N
    }
    
    /** 
     * Queues the text to the translatable paragraph.
     * <p>
     * Note that the queued text (if not-purely-whitespace) 
     * will also append the previously queued tags and whitespace tags
     * to the translatable paragraph.
     * <p>
     * Whitespace text is simply added to the queue.
     */
    private void queueTranslatable(Text text)
    {
        if( text.toHtml().trim().length()>0 )
        {
            for(int i=0; i<afters.size(); i++)
                appendTranslatable((Node)afters.get(i));
            afters.clear();
            appendTranslatable(text);
        }
        else
            afters.add(text);
    }
    
    /** 
     * Queues the tag to the translatable paragraph. 
     * <p>
     * Note that the tag is simply added to the queue,
     * and will be appended to the translatable text only 
     * if some meaningful text follows it.
     */
    private void queueTranslatable(Tag tag)
    {
        afters.add(tag);
    }
    
    /** Compresses spaces in case of non-preformatting paragraph. */
    private String compressSpaces(String str)
    {
        int strlen = str.length();
        StringBuffer res = new StringBuffer(strlen);
        boolean wasspace = true;
        for(int i=0; i<strlen; i++)
        {
            char ch = str.charAt(i);
            boolean space = Character.isWhitespace(ch);
            if( space )
            {
                if( !wasspace )
                    wasspace = true;
            }
            else
            {
                if( wasspace && res.length()>0 )
                    res.append(' ');
                res.append(ch);
                wasspace = false;
            }
        }
        return res.toString();
    }

    /** Named HTML Entities and corresponding numeric character references */
    private static final Object ENTITIES[][] =
    {
        {"quot", new Integer(34)},                          // NOI18N
        {"amp", new Integer(38)},                          // NOI18N
        {"lt", new Integer(60)},                          // NOI18N
        {"gt", new Integer(62)},                          // NOI18N
                
        //  Latin Extended-A 
        {"OElig", new Integer(338)},                          // NOI18N // latin capital ligature OE, U+0152 ISOlat2 
        {"oelig", new Integer(339)},                          // NOI18N // latin small ligature oe, U+0153 ISOlat2 
        //  ligature is a misnomer, this is a separate character in some languages 
        {"Scaron", new Integer(352)},                          // NOI18N // latin capital letter S with caron, U+0160 ISOlat2 
        {"scaron", new Integer(353)},                          // NOI18N // latin small letter s with caron, U+0161 ISOlat2 
        {"Yuml", new Integer(376)},                          // NOI18N // latin capital letter Y with diaeresis, U+0178 ISOlat2 

        //  Spacing Modifier Letters 
        {"circ", new Integer(710)},                          // NOI18N // modifier letter circumflex accent, U+02C6 ISOpub 
        {"tilde", new Integer(732)},                          // NOI18N // small tilde, U+02DC ISOdia 

        //  General Punctuation 
        {"ensp", new Integer(8194)},                          // NOI18N // en space, U+2002 ISOpub 
        {"emsp", new Integer(8195)},                          // NOI18N // em space, U+2003 ISOpub 
        {"thinsp", new Integer(8201)},                          // NOI18N // thin space, U+2009 ISOpub 
        {"zwnj", new Integer(8204)},                          // NOI18N // zero width non-joiner, U+200C NEW RFC 2070 
        {"zwj", new Integer(8205)},                          // NOI18N // zero width joiner, U+200D NEW RFC 2070 
        {"lrm", new Integer(8206)},                          // NOI18N // left-to-right mark, U+200E NEW RFC 2070 
        {"rlm", new Integer(8207)},                          // NOI18N // right-to-left mark, U+200F NEW RFC 2070 
        {"ndash", new Integer(8211)},                          // NOI18N // en dash, U+2013 ISOpub 
        {"mdash", new Integer(8212)},                          // NOI18N // em dash, U+2014 ISOpub 
        {"lsquo", new Integer(8216)},                          // NOI18N // left single quotation mark, U+2018 ISOnum 
        {"rsquo", new Integer(8217)},                          // NOI18N // right single quotation mark, U+2019 ISOnum 
        {"sbquo", new Integer(8218)},                          // NOI18N // single low-9 quotation mark, U+201A NEW 
        {"ldquo", new Integer(8220)},                          // NOI18N // left double quotation mark, U+201C ISOnum 
        {"rdquo", new Integer(8221)},                          // NOI18N // right double quotation mark, U+201D ISOnum 
        {"bdquo", new Integer(8222)},                          // NOI18N // double low-9 quotation mark, U+201E NEW 
        {"dagger", new Integer(8224)},                          // NOI18N // dagger, U+2020 ISOpub 
        {"Dagger", new Integer(8225)},                          // NOI18N // double dagger, U+2021 ISOpub 
        {"permil", new Integer(8240)},                          // NOI18N // per mille sign, U+2030 ISOtech 
        {"lsaquo", new Integer(8249)},                          // NOI18N // single left-pointing angle quotation mark, U+2039 ISO proposed 
                                       //  lsaquo is proposed but not yet ISO standardized 
        {"rsaquo", new Integer(8250)},                          // NOI18N // single right-pointing angle quotation mark, U+203A ISO proposed 
                                       //  rsaquo is proposed but not yet ISO standardized 
        {"euro", new Integer(8364)},                          // NOI18N   // euro sign, U+20AC NEW                 
                
        {"nbsp", new Integer(160)},                          // NOI18N
        {"iexcl", new Integer(161)},                          // NOI18N
        {"cent", new Integer(162)},                          // NOI18N
        {"pound", new Integer(163)},                          // NOI18N
        {"curren", new Integer(164)},                          // NOI18N
        {"yen", new Integer(165)},                          // NOI18N
        {"brvbar", new Integer(166)},                          // NOI18N
        {"sect", new Integer(167)},                          // NOI18N
        {"uml", new Integer(168)},                          // NOI18N
        {"copy", new Integer(169)},                          // NOI18N
        {"ordf", new Integer(170)},                          // NOI18N
        {"laquo", new Integer(171)},                          // NOI18N
        {"not", new Integer(172)},                          // NOI18N
        {"shy", new Integer(173)},                          // NOI18N
        {"reg", new Integer(174)},                          // NOI18N
        {"macr", new Integer(175)},                          // NOI18N
        {"deg", new Integer(176)},                          // NOI18N
        {"plusmn", new Integer(177)},                          // NOI18N
        {"sup2", new Integer(178)},                          // NOI18N
        {"sup3", new Integer(179)},                          // NOI18N
        {"acute", new Integer(180)},                          // NOI18N
        {"micro", new Integer(181)},                          // NOI18N
        {"para", new Integer(182)},                          // NOI18N
        {"middot", new Integer(183)},                          // NOI18N
        {"cedil", new Integer(184)},                          // NOI18N
        {"sup1", new Integer(185)},                          // NOI18N
        {"ordm", new Integer(186)},                          // NOI18N
        {"raquo", new Integer(187)},                          // NOI18N
        {"frac14", new Integer(188)},                          // NOI18N
        {"frac12", new Integer(189)},                          // NOI18N
        {"frac34", new Integer(190)},                          // NOI18N
        {"iquest", new Integer(191)},                          // NOI18N
        {"Agrave", new Integer(192)},                          // NOI18N
        {"Aacute", new Integer(193)},                          // NOI18N
        {"Acirc", new Integer(194)},                          // NOI18N
        {"Atilde", new Integer(195)},                          // NOI18N
        {"Auml", new Integer(196)},                          // NOI18N
        {"Aring", new Integer(197)},                          // NOI18N
        {"AElig", new Integer(198)},                          // NOI18N
        {"Ccedil", new Integer(199)},                          // NOI18N
        {"Egrave", new Integer(200)},                          // NOI18N
        {"Eacute", new Integer(201)},                          // NOI18N
        {"Ecirc", new Integer(202)},                          // NOI18N
        {"Euml", new Integer(203)},                          // NOI18N
        {"Igrave", new Integer(204)},                          // NOI18N
        {"Iacute", new Integer(205)},                          // NOI18N
        {"Icirc", new Integer(206)},                          // NOI18N
        {"Iuml", new Integer(207)},                          // NOI18N
        {"ETH", new Integer(208)},                          // NOI18N
        {"Ntilde", new Integer(209)},                          // NOI18N
        {"Ograve", new Integer(210)},                          // NOI18N
        {"Oacute", new Integer(211)},                          // NOI18N
        {"Ocirc", new Integer(212)},                          // NOI18N
        {"Otilde", new Integer(213)},                          // NOI18N
        {"Ouml", new Integer(214)},                          // NOI18N
        {"times", new Integer(215)},                          // NOI18N
        {"Oslash", new Integer(216)},                          // NOI18N
        {"Ugrave", new Integer(217)},                          // NOI18N
        {"Uacute", new Integer(218)},                          // NOI18N
        {"Ucirc", new Integer(219)},                          // NOI18N
        {"Uuml", new Integer(220)},                          // NOI18N
        {"Yacute", new Integer(221)},                          // NOI18N
        {"THORN", new Integer(222)},                          // NOI18N
        {"szlig", new Integer(223)},                          // NOI18N
        {"agrave", new Integer(224)},                          // NOI18N
        {"aacute", new Integer(225)},                          // NOI18N
        {"acirc", new Integer(226)},                          // NOI18N
        {"atilde", new Integer(227)},                          // NOI18N
        {"auml", new Integer(228)},                          // NOI18N
        {"aring", new Integer(229)},                          // NOI18N
        {"aelig", new Integer(230)},                          // NOI18N
        {"ccedil", new Integer(231)},                          // NOI18N
        {"egrave", new Integer(232)},                          // NOI18N
        {"eacute", new Integer(233)},                          // NOI18N
        {"ecirc", new Integer(234)},                          // NOI18N
        {"euml", new Integer(235)},                          // NOI18N
        {"igrave", new Integer(236)},                          // NOI18N
        {"iacute", new Integer(237)},                          // NOI18N
        {"icirc", new Integer(238)},                          // NOI18N
        {"iuml", new Integer(239)},                          // NOI18N
        {"eth", new Integer(240)},                          // NOI18N
        {"ntilde", new Integer(241)},                          // NOI18N
        {"ograve", new Integer(242)},                          // NOI18N
        {"oacute", new Integer(243)},                          // NOI18N
        {"ocirc", new Integer(244)},                          // NOI18N
        {"otilde", new Integer(245)},                          // NOI18N
        {"ouml", new Integer(246)},                          // NOI18N
        {"divide", new Integer(247)},                          // NOI18N
        {"oslash", new Integer(248)},                          // NOI18N
        {"ugrave", new Integer(249)},                          // NOI18N
        {"uacute", new Integer(250)},                          // NOI18N
        {"ucirc", new Integer(251)},                          // NOI18N
        {"uuml", new Integer(252)},                          // NOI18N
        {"yacute", new Integer(253)},                          // NOI18N
        {"thorn", new Integer(254)},                          // NOI18N
        {"yuml", new Integer(255)},                          // NOI18N
                
        {"fnof", new Integer(402)},                          // NOI18N
                
        {"Alpha", new Integer(913)},                          // NOI18N
        {"Beta", new Integer(914)},                          // NOI18N
        {"Gamma", new Integer(915)},                          // NOI18N
        {"Delta", new Integer(916)},                          // NOI18N
        {"Epsilon", new Integer(917)},                          // NOI18N
        {"Zeta", new Integer(918)},                          // NOI18N
        {"Eta", new Integer(919)},                          // NOI18N
        {"Theta", new Integer(920)},                          // NOI18N
        {"Iota", new Integer(921)},                          // NOI18N
        {"Kappa", new Integer(922)},                          // NOI18N
        {"Lambda", new Integer(923)},                          // NOI18N
        {"Mu", new Integer(924)},                          // NOI18N
        {"Nu", new Integer(925)},                          // NOI18N
        {"Xi", new Integer(926)},                          // NOI18N
        {"Omicron", new Integer(927)},                          // NOI18N
        {"Pi", new Integer(928)},                          // NOI18N
        {"Rho", new Integer(929)},                          // NOI18N
        {"Sigma", new Integer(931)},                          // NOI18N
        {"Tau", new Integer(932)},                          // NOI18N
        {"Upsilon", new Integer(933)},                          // NOI18N
        {"Phi", new Integer(934)},                          // NOI18N
        {"Chi", new Integer(935)},                          // NOI18N
        {"Psi", new Integer(936)},                          // NOI18N
        {"Omega", new Integer(937)},                          // NOI18N
        {"alpha", new Integer(945)},                          // NOI18N
        {"beta", new Integer(946)},                          // NOI18N
        {"gamma", new Integer(947)},                          // NOI18N
        {"delta", new Integer(948)},                          // NOI18N
        {"epsilon", new Integer(949)},                          // NOI18N
        {"zeta", new Integer(950)},                          // NOI18N
        {"eta", new Integer(951)},                          // NOI18N
        {"theta", new Integer(952)},                          // NOI18N
        {"iota", new Integer(953)},                          // NOI18N
        {"kappa", new Integer(954)},                          // NOI18N
        {"lambda", new Integer(955)},                          // NOI18N
        {"mu", new Integer(956)},                          // NOI18N
        {"nu", new Integer(957)},                          // NOI18N
        {"xi", new Integer(958)},                          // NOI18N
        {"omicron", new Integer(959)},                          // NOI18N
        {"pi", new Integer(960)},                          // NOI18N
        {"rho", new Integer(961)},                          // NOI18N
        {"sigmaf", new Integer(962)},                          // NOI18N
        {"sigma", new Integer(963)},                          // NOI18N
        {"tau", new Integer(964)},                          // NOI18N
        {"upsilon", new Integer(965)},                          // NOI18N
        {"phi", new Integer(966)},                          // NOI18N
        {"chi", new Integer(967)},                          // NOI18N
        {"psi", new Integer(968)},                          // NOI18N
        {"omega", new Integer(969)},                          // NOI18N
        {"thetasym", new Integer(977)},                          // NOI18N
        {"upsih", new Integer(978)},                          // NOI18N
        {"piv", new Integer(982)},                          // NOI18N

        {"bull", new Integer(8226)},                          // NOI18N
        {"hellip", new Integer(8230)},                          // NOI18N
        {"prime", new Integer(8242)},                          // NOI18N
        {"Prime", new Integer(8243)},                          // NOI18N
        {"oline", new Integer(8254)},                          // NOI18N
        {"frasl", new Integer(8260)},                          // NOI18N
	
        {"weierp", new Integer(8472)},                          // NOI18N
        {"image", new Integer(8465)},                          // NOI18N
        {"real", new Integer(8476)},                          // NOI18N
        {"trade", new Integer(8482)},                          // NOI18N
        {"alefsym", new Integer(8501)},                          // NOI18N
	
        {"larr", new Integer(8592)},                          // NOI18N
        {"uarr", new Integer(8593)},                          // NOI18N
        {"rarr", new Integer(8594)},                          // NOI18N
        {"darr", new Integer(8595)},                          // NOI18N
        {"harr", new Integer(8596)},                          // NOI18N
        {"crarr", new Integer(8629)},                          // NOI18N
        {"lArr", new Integer(8656)},                          // NOI18N
        {"uArr", new Integer(8657)},                          // NOI18N
        {"rArr", new Integer(8658)},                          // NOI18N
        {"dArr", new Integer(8659)},                          // NOI18N
        {"hArr", new Integer(8660)},                          // NOI18N
	
        {"forall", new Integer(8704)},                          // NOI18N
        {"part", new Integer(8706)},                          // NOI18N
        {"exist", new Integer(8707)},                          // NOI18N
        {"empty", new Integer(8709)},                          // NOI18N
        {"nabla", new Integer(8711)},                          // NOI18N
        {"isin", new Integer(8712)},                          // NOI18N
        {"notin", new Integer(8713)},                          // NOI18N
        {"ni", new Integer(8715)},                          // NOI18N
        {"prod", new Integer(8719)},                          // NOI18N
        {"sum", new Integer(8722)},                          // NOI18N
        {"minus", new Integer(8722)},                          // NOI18N
        {"lowast", new Integer(8727)},                          // NOI18N
        {"radic", new Integer(8730)},                          // NOI18N
        {"prop", new Integer(8733)},                          // NOI18N
        {"infin", new Integer(8734)},                          // NOI18N
        {"ang", new Integer(8736)},                          // NOI18N
        {"and", new Integer(8869)},                          // NOI18N
        {"or", new Integer(8870)},                          // NOI18N
        {"cap", new Integer(8745)},                          // NOI18N
        {"cup", new Integer(8746)},                          // NOI18N
        {"int", new Integer(8747)},                          // NOI18N
        {"there4", new Integer(8756)},                          // NOI18N
        {"sim", new Integer(8764)},                          // NOI18N
        {"cong", new Integer(8773)},                          // NOI18N
        {"asymp", new Integer(8773)},                          // NOI18N
        {"ne", new Integer(8800)},                          // NOI18N
        {"equiv", new Integer(8801)},                          // NOI18N
        {"le", new Integer(8804)},                          // NOI18N
        {"ge", new Integer(8805)},                          // NOI18N
        {"sub", new Integer(8834)},                          // NOI18N
        {"sup", new Integer(8835)},                          // NOI18N
        {"nsub", new Integer(8836)},                          // NOI18N
        {"sube", new Integer(8838)},                          // NOI18N
        {"supe", new Integer(8839)},                          // NOI18N
        {"oplus", new Integer(8853)},                          // NOI18N
        {"otimes", new Integer(8855)},                          // NOI18N
        {"perp", new Integer(8869)},                          // NOI18N
        {"sdot", new Integer(8901)},                          // NOI18N
	
        {"lceil", new Integer(8968)},                          // NOI18N
        {"rceil", new Integer(8969)},                          // NOI18N
        {"lfloor", new Integer(8970)},                          // NOI18N
        {"rfloor", new Integer(8971)},                          // NOI18N
        {"lang", new Integer(9001)},                          // NOI18N
        {"rang", new Integer(9002)},                          // NOI18N
	
        {"loz", new Integer(9674)},                          // NOI18N
	
        {"spades", new Integer(9824)},                          // NOI18N
        {"clubs", new Integer(9827)},                          // NOI18N
        {"hearts", new Integer(9829)},                          // NOI18N
        {"diams", new Integer(9830)},                          // NOI18N
    };

    /** Converts HTML entities to normal characters */
    private String entitiesToChars(String str)
    {
        int strlen = str.length();
        StringBuffer res = new StringBuffer(strlen);
        for(int i=0; i<strlen; i++)
        {
            char ch = str.charAt(i);
            switch( ch )
            {
                case '&':
                    char ch1 = str.charAt(i+1);
                    if( ch1=='#' )
                    {
                        // numeric entity
                        char ch2 = str.charAt(i+2);
                        if( ch2=='x' || ch2=='X' )
                        {
                            // hex numeric entity
                            int n = i+3;
                            char chh;
                            while( isHexDigit(chh=str.charAt(n)) && n<strlen )
                                n++;
                            String s_entity = str.substring(i+3, n);
                            try
                            {
                                int n_entity = Integer.parseInt(s_entity, 16);
                                if( n_entity>0 && n_entity<=65535 )
                                {
                                    res.append((char)n_entity);
                                    if( n<strlen && str.charAt(n)==';' )
                                        i = n;
                                    else
                                        i = n-1;
                                }
                                else
                                {
                                    // too big number
                                    // dangling '&'
                                    res.append(ch);
                                }
                            }
                            catch( NumberFormatException nfe )
                            {
                                // do nothing
                                // dangling '&'
                                res.append(ch);
                            }
                        }
                        else
                        {
                            // decimal entity
                            int n = i+2;
                            char chh;
                            while( isDecimalDigit(chh=str.charAt(n)) && n<strlen )
                                n++;
                            String s_entity = str.substring(i+2, n);
                            try
                            {
                                int n_entity = Integer.parseInt(s_entity, 10);
                                if( n_entity>0 && n_entity<=65535 )
                                {
                                    res.append((char)n_entity);
                                    if( n<strlen && str.charAt(n)==';' )
                                        i = n;
                                    else
                                        i = n-1;
                                }
                                else
                                {
                                    // too big number
                                    // dangling '&'
                                    res.append(ch);
                                }
                            }
                            catch( NumberFormatException nfe )
                            {
                                // do nothing
                                // dangling '&'
                                res.append(ch);
                            }
                        }
                    }
                    else if( isLatinLetter(ch1) )
                    {
                        // named entity?
                        int n = i+1;
                        char chh;
                        while( isLatinLetter(chh=str.charAt(n)) && n<strlen )
                            n++;
                        String s_entity = str.substring(i+1, n);
                        int n_entity = lookupEntity(s_entity);
                        if( n_entity>0 && n_entity<=65535 )
                        {
                            res.append((char)n_entity);
                            if( n<strlen && str.charAt(n)==';' )
                                i = n;
                            else
                                i = n-1;
                        }
                        else
                        {
                            // too big number
                            // dangling '&'
                            res.append(ch);
                        }
                    }
                    else
                    {
                        // dangling '&'
                        res.append(ch);
                    }
                    break;
                default: 
                    res.append(ch);
            }
        }
        return res.toString();
    }
    
    /** Returns true if a char is a latin letter */
    private boolean isLatinLetter(char ch)
    {
        return (ch>='a' && ch<='z') || (ch>='A' && ch<='Z');
    }
    /** Returns true if a char is a decimal digit */
    private boolean isDecimalDigit(char ch)
    {
        return (ch>='0' && ch<='9');
    }
    /** Returns true if a char is a hex digit */
    private boolean isHexDigit(char ch)
    {
        return (ch>='0' && ch<='9') || (ch>='a' && ch<='f') || (ch>='A' && ch<='F');
    }
    
    /** returns a character for HTML entity, or -1 if the passed string is not an entity */
    private int lookupEntity(String entity)
    {
        for(int i=0; i<ENTITIES.length; i++)
        {
            Object[] ONENT = ENTITIES[i];
            if( entity.equals(ONENT[0]) )
                return ((Integer)ONENT[1]).intValue();
        }
        return -1;
    }

    /** 
     * Converts characters that must be converted 
     * (&lt; &gt; &amp; '&nbsp;' (nbsp)) 
     * into HTML entities 
     */
    private String charsToEntities(String str)
    {
        int strlen = str.length();
        StringBuffer res = new StringBuffer(strlen*5);
        for(int i=0; i<strlen; i++)
        {
            char ch = str.charAt(i);
            switch( ch )
            {
                case '\u00A0':
                    res.append("&nbsp;");                                       // NOI18N
                    break;
                case '&':
                    res.append("&amp;");                                        // NOI18N
                    break;
                case '>':
                    res.append("&gt;");                                         // NOI18N
                    break;
                case '<':
                    int gtpos = str.indexOf('>', i);
                    if( gtpos>=0 )
                    {
                        String maybeShortcut = str.substring(i, gtpos+1);
                        boolean foundShortcut = false;                          // here because it's impossible to step out of two loops at once
                        for(int j=0; j<s_shortcuts.size(); j++)
                        {
                            String currShortcut = (String)s_shortcuts.get(j);
                            if( maybeShortcut.equals(currShortcut) )
                            {
                                // skipping the conversion of < into &lt;
                                // because it's a part of the tag
                                foundShortcut = true;
                                break;
                            }
                        }
                        if( foundShortcut )
                        {
                            res.append(maybeShortcut);
                            i = gtpos;
                            continue;
                        }
                        else
                        {
                            // dangling <
                            res.append("&lt;");                                 // NOI18N
                        }
                    }
                    else
                    {
                        // dangling <
                        res.append("&lt;");                                     // NOI18N
                    }
                    break;
                default:
                    res.append(ch);
            }
        }
        return res.toString();
    }
    
    
}

