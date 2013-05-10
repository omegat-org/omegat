/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2008 Didier Briel, Martin Fleurke
               2010 Didier Briel
               2011 Didier Briel, Martin Fleurke
               2012 Didier Briel, Martin Fleurke
               2013 Didier Briel
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters2.html2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;

import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.visitors.NodeVisitor;
import org.omegat.core.Core;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.StaticUtils;

/**
 * The part of HTML filter that actually does the job. This class is called back
 * by HTMLParser (http://sf.net/projects/htmlparser/).
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Martin Fleurke
 */
public class FilterVisitor extends NodeVisitor {
    protected HTMLFilter2 filter;
    private BufferedWriter writer;
    private HTMLOptions options;

    public FilterVisitor(HTMLFilter2 htmlfilter, BufferedWriter bufwriter, HTMLOptions options) {
        this.filter = htmlfilter;
        this.options = options; // HHC filter has no options
        this.writer = bufwriter;
    }

    // ///////////////////////////////////////////////////////////////////////
    // Variable declaration
    // ///////////////////////////////////////////////////////////////////////

    /** Should the parser call us for this tag's ending tag and its inner tags. */
    boolean recurse = true;

    /** Do we collect the translatable text now. */
    boolean text = false;
    /** The translatable text being collected. */
    // StringBuffer paragraph;
    /** Did the PRE block start (it means we mustn't compress the spaces). */
    boolean preformatting = false;

    /**
     * The list of non-paragraph tags before a chunk of text.
     * <ul>
     * <li>If a chunk of text follows, they get prepended to the translatable
     * paragraph, (starting from the first tag having a pair inside a chunk of
     * text)
     * <li>Otherwise they are written out directly.
     * </ul>
     */
    List<Node> befors;

    /** The list of nodes forming a chunk of text. */
    List<Node> translatable;

    /**
     * The list of non-paragraph tags following a chunk of text.
     * <ul>
     * <li>If another chunk of text follows, they get appended to the
     * translatable paragraph,
     * <li>Otherwise (paragraph tag follows), they are written out directly.
     * </ul>
     */
    List<Node> afters;

    /** The tags behind the shortcuts */
    List<Tag> s_tags;
    /** The tag numbers of shorcutized tags */
    List<Integer> s_tag_numbers;
    /** The list of all the tag shortcuts */
    List<String> s_shortcuts;
    /** The number of shortcuts stored */
    int s_nshortcuts;

    /**
     * Self traversal predicate.
     * 
     * @return <code>true</code> if a node itself is to be visited.
     */
    @Override
    public boolean shouldRecurseSelf() {
        return recurse;
    }

    /**
     * Depth traversal predicate.
     * 
     * @return <code>true</code> if children are to be visited.
     */
    @Override
    public boolean shouldRecurseChildren() {
        return recurse;
    }

    /**
     * Called for each <code>Tag</code> visited.
     * 
     * @param tag
     *            The tag being visited.
     */
    @Override
    public void visitTag(Tag tag) {


        boolean intactTag = isIntactTag(tag);

        if (!intactTag) { // If it's an intact tag, no reason to check
            // Decide whether this tag should be intact, based on the key-value pairs stored in the
            // configuration
            Vector<Attribute> tagAttributes = tag.getAttributesEx();
            Iterator<Attribute> i = tagAttributes.iterator();
            while (i.hasNext() && intactTag == false) {
                Attribute attribute = i.next();
                String name = attribute.getName();
                String value = attribute.getValue();
                if (name == null || value == null)
                    continue;
                intactTag = this.filter.checkIgnoreTags(name, value);
            }
        }
        
        if (intactTag) {
            if (text)
                endup();
            else
                flushbefors();
            writeout(tag.toHtml());
            if (tag.getEndTag() != null)
                recurse = false;
        } else {
            // recurse = true;
            if (isParagraphTag(tag) && text)
                endup();

            if (isPreformattingTag(tag) || Core.getFilterMaster().getConfig().isPreserveSpaces()) {
                preformatting = true;
            }
            // Translate attributes of tags if they are not null.
            maybeTranslateAttribute(tag, "abbr");
            maybeTranslateAttribute(tag, "alt");
            if (options.getTranslateHref())
                maybeTranslateAttribute(tag, "href");
            if (options.getTranslateHreflang())
                maybeTranslateAttribute(tag, "hreflang");
            if (options.getTranslateLang()) {
                maybeTranslateAttribute(tag, "lang");
                maybeTranslateAttribute(tag, "xml:lang");
            }
            maybeTranslateAttribute(tag, "label");
            if ("IMG".equals(tag.getTagName()) && options.getTranslateSrc())
                maybeTranslateAttribute(tag, "src");
            maybeTranslateAttribute(tag, "summary");
            maybeTranslateAttribute(tag, "title");
            if ("INPUT".equals(tag.getTagName())
                    && (options.getTranslateValue() || "submit".equalsIgnoreCase(tag.getAttribute("type"))
                            || "button".equalsIgnoreCase(tag.getAttribute("type")) || "reset"
                            .equalsIgnoreCase(tag.getAttribute("type")) && options.getTranslateButtonValue()))
                maybeTranslateAttribute(tag, "value");
            // Special handling of meta-tag: depending on the other attributes
            // the contents-attribute should or should not be translated.
            // The group of attribute-value pairs indicating non-translation
            // are stored in the configuration
            if ("META".equals(tag.getTagName())) {
                Vector<Attribute> tagAttributes = tag.getAttributesEx();
                Iterator<Attribute> i = tagAttributes.iterator();
                boolean doSkipMetaTag = false;
                while (i.hasNext() && doSkipMetaTag == false) {
                    Attribute attribute = i.next();
                    String name = attribute.getName();
                    String value = attribute.getValue();
                    if (name == null || value == null)
                        continue;
                    doSkipMetaTag = this.filter.checkDoSkipMetaTag(name, value);
                }
                if (!doSkipMetaTag) {
                    maybeTranslateAttribute(tag, "content");
                }
            }

            queuePrefix(tag);
        }
    }

    /**
     * If the attribute of the tag is not empty, it translates it as a separate
     * segment.
     * 
     * @param tag
     *            the tag object
     * @param key
     *            the name of the attribute
     */
    protected void maybeTranslateAttribute(Tag tag, String key) {
        String attr = tag.getAttribute(key);
        if (attr != null) {
            String comment = OStrings.getString("HTMLFILTER_TAG") + " " + tag.getTagName() + " " + OStrings.getString("HTMLFILTER_ATTRIBUTE") + " " + key;
            String trans = filter.privateProcessEntry(entitiesToChars(attr), comment);
            tag.setAttribute(key, charsToEntities(trans));
        }
    }

    boolean firstcall = true;

    /**
     * Called for each chunk of text (<code>StringNode</code>) visited.
     * 
     * @param string
     *            The string node being visited.
     */
    @Override
    public void visitStringNode(Text string) {
        recurse = true;
        // nbsp is special case - process it like usual spaces
        String trimmedtext = entitiesToChars(string.getText()).replace((char) 160, ' ').trim();
        if (trimmedtext.length() > 0) {
            // Hack around HTMLParser not being able to handle XHTML
            // RFE pending:
            // http://sourceforge.net/tracker/index.php?func=detail&aid=1227222&group_id=24399&atid=381402
            if (firstcall && PatternConsts.XML_HEADER.matcher(trimmedtext).matches()) {
                writeout(string.toHtml());
                return;
            }

            text = true;
            firstcall = false;
        }

        if (text)
            queueTranslatable(string);
        else
            queuePrefix(string);
    }

    /**
     * Called for each comment (<code>RemarkNode</code>) visited.
     * 
     * @param remark
     *            The remark node being visited.
     */
    @Override
    public void visitRemarkNode(Remark remark) {
        recurse = true;
        if (text)
            endup();
        if (!options.getRemoveComments()) writeout(remark.toHtml());
    }

    /**
     * Called for each end <code>Tag</code> visited.
     * 
     * @param tag
     *            The end tag being visited.
     */
    @Override
    public void visitEndTag(Tag tag) {
        recurse = true;
        if (isParagraphTag(tag) && text)
            endup();
        if (isPreformattingTag(tag))
            preformatting = false;

        queuePrefix(tag);
    }

    /**
     * This method is called before the parsing.
     */
    @Override
    public void beginParsing() {
        cleanup();
    }

    /**
     * Called upon parsing completion.
     */
    @Override
    public void finishedParsing() {
        if (text)
            endup();
        else
            flushbefors();
    }

    /**
     * Does the tag lead to starting (ending) a paragraph.
     * <p>
     * Contains code donated by JC to have dictionary list parsed as segmenting.
     * http://sourceforge.net/support/tracker.php?aid=1348792
     */
    private boolean isParagraphTag(Tag tag) {
        String tagname = tag.getTagName();
        return
        // Bugfix for http://sourceforge.net/support/tracker.php?aid=1288756
        // ADDRESS tag is also a paragraph tag
        tagname.equals("ADDRESS") || tagname.equals("BLOCKQUOTE") || tagname.equals("BODY")
                || tagname.equals("CENTER") || tagname.equals("DIV") || tagname.equals("H1")
                || tagname.equals("H2") || tagname.equals("H3") || tagname.equals("H4")
                || tagname.equals("H5") || tagname.equals("H6") || tagname.equals("HTML")
                || tagname.equals("HEAD") || tagname.equals("TITLE") || tagname.equals("TABLE")
                || tagname.equals("TR") || tagname.equals("TD") || tagname.equals("TH")
                || tagname.equals("P") || tagname.equals("PRE") || tagname.equals("OL")
                || tagname.equals("UL")
                || tagname.equals("LI")
                ||
                // Added by JC to have dictionary list parsed as segmenting.
                tagname.equals("DL") || tagname.equals("DT")
                || tagname.equals("DD")
                ||
                // End of JC's contribution
                tagname.equals("FORM") || tagname.equals("TEXTAREA") || tagname.equals("FIELDSET")
                || tagname.equals("LEGEND") || tagname.equals("LABEL") || tagname.equals("SELECT")
                || tagname.equals("OPTION") || tagname.equals("HR") ||
                // Optional paragraph on BR
                (tagname.equals("BR") && options.getParagraphOnBr());

    }

    /** Should a contents of this tag be kept intact? */
    private boolean isIntactTag(Tag tag) {
        String tagname = tag.getTagName();
        return tagname.equals("!DOCTYPE")
                || tagname.equals("STYLE")
                || tagname.equals("SCRIPT")
                || tagname.equals("OBJECT")
                || tagname.equals("EMBED")
                || (tagname.equals("META") && "content-type".equalsIgnoreCase(tag.getAttribute("http-equiv")));
    }

    /** Is the tag space-preserving? */
    private boolean isPreformattingTag(Tag tag) {
        String tagname = tag.getTagName();
        return tagname.equals("PRE") || tagname.equals("TEXTAREA");
    }

    /** Writes something to writer. */
    private void writeout(String something) {
        try {
            writer.write(something);
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    /**
     * Ends the segment collection and sends the translatable text out to OmegaT
     * core, and some extra tags to writer.
     */
    protected void endup() {
        // detecting the first starting tag in 'befors'
        // that has its ending in the paragraph
        // all before this "first good" are simply written out
        List<Node> all = new ArrayList<Node>();
        all.addAll(befors);
        all.addAll(translatable);
        int firstgoodlimit = befors.size();
        int firstgood = 0;
        while (firstgood < firstgoodlimit) {
            Node good_node = all.get(firstgood);
            if (!(good_node instanceof Tag)) {
                firstgood++;
                continue;
            }
            Tag good = (Tag) good_node;

            // trying to test
            int recursion = 1;
            boolean found = false;
            for (int i = firstgood + 1; i < all.size(); i++) {
                Node cand_node = all.get(i);
                if (cand_node instanceof Tag) {
                    Tag cand = (Tag) cand_node;
                    if (cand.getTagName().equals(good.getTagName())) {
                        if (!cand.isEndTag())
                            recursion++;
                        else {
                            recursion--;
                            if (recursion == 0) {
                                if (i >= firstgoodlimit)
                                    found = true;
                                // we've found an ending tag for this "good one"
                                break;
                            }
                        }
                    }
                }
            }
            // if we could find an ending,
            // this is a "good one"
            if (found)
                break;
            firstgood++;
        }

        // detecting the last ending tag in 'afters'
        // that has its starting in the paragraph
        // all after this "last good" is simply writen out
        int lastgoodlimit = all.size() - 1;
        all.addAll(afters);
        int lastgood = all.size() - 1;
        while (lastgood > lastgoodlimit) {
            Node good_node = all.get(lastgood);
            if (!(good_node instanceof Tag)) {
                lastgood--;
                continue;
            }
            Tag good = (Tag) good_node;

            // trying to test
            int recursion = 1;
            boolean found = false;
            for (int i = lastgood - 1; i >= firstgoodlimit; i--) {
                Node cand_node = all.get(i);
                if (cand_node instanceof Tag) {
                    Tag cand = (Tag) cand_node;
                    if (cand.getTagName().equals(good.getTagName())) {
                        if (cand.isEndTag())
                            recursion++;
                        else {
                            recursion--;
                            if (recursion == 0) {
                                if (i <= lastgoodlimit)
                                    found = true;
                                // we've found a starting tag for this
                                // "good one"
                                break;
                            }
                        }
                    }
                }
            }
            // if we coud find a starting,
            // this is a "good one"
            if (found)
                break;
            lastgood--;
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            boolean removeTags = Core.getFilterMaster().getConfig().isRemoveTags();
            if (!removeTags) {
                for (int i = 0; i < firstgood; i++) {
                    Node node = all.get(i);
                    if (node instanceof Tag) {
                        firstgood = i;
                        changed = true;
                        break;
                    }
                }
                for (int i = all.size() - 1; i > lastgood; i--) {
                    Node node = all.get(i);
                    if (node instanceof Tag) {
                        lastgood = i;
                        changed = true;
                        break;
                    }
                }
            }

            boolean removeSpacesAround = Core.getFilterMaster().getConfig().isRemoveSpacesNonseg();
            if (!removeSpacesAround) {
                for (int i = 0; i < firstgood; i++) {
                    Node node = all.get(i);
                    if (node instanceof TextNode) {
                        firstgood = i;
                        changed = true;
                        break;
                    }
                }
                for (int i = all.size() - 1; i > lastgood; i--) {
                    Node node = all.get(i);
                    if (node instanceof TextNode) {
                        lastgood = i;
                        changed = true;
                        break;
                    }
                }
            }
        }

        // writing out all tags before the "first good" one
        for (int i = 0; i < firstgood; i++) {
            Node node = all.get(i);
            if (node instanceof Tag)
                writeout("<" + node.getText() + ">");
            else
                writeout(compressWhitespace(node.getText()));
        }

        // appending all tags until "last good" one to paragraph text
        StringBuffer paragraph = new StringBuffer();
        // appending all tags starting from "first good" one to paragraph text
        for (int i = firstgood; i <= lastgood; i++) {
            Node node = all.get(i);
            if (node instanceof Tag) {
                shortcut((Tag) node, paragraph);
            } else // node instanceof Text
            {
                paragraph.append(entitiesToChars(node.toHtml()));
            }
        }

        String uncompressed = paragraph.toString();
        String compressed = uncompressed;
        String spacePrefix = "";
        String spacePostfix = "";
        int size = uncompressed.length();
        // We're compressing the space if this paragraph wasn't inside <PRE> tag
        // But if the translator does not translate the paragraph,
        // then we write out the uncompressed version,
        // as documented in
        // http://sourceforge.net/support/tracker.php?aid=1364265
        // The spaces that are around the segment are not removed, unless
        // compressWhitespace option is enabled. Then the spaces are compressed to max 1.
        // (This changes the layout, therefore it is an option)
        if (!preformatting) {

            for (int i = 0; i < size; i++) {
                if (!Character.isWhitespace(uncompressed.charAt(i))) {
                    spacePrefix = uncompressed.substring(0, (options.getCompressWhitespace() ? Math.min(i,1) : i));
                    break;
                }
            }
            for (int i = size - 1; i > 0; i--) {
                if (!Character.isWhitespace(uncompressed.charAt(i))) {
                    spacePostfix = uncompressed.substring(i + 1, (options.getCompressWhitespace() ? Math.min(i + 2, size) : size));
                    break;
                }
            }
            
            if (Core.getFilterMaster().getConfig().isRemoveSpacesNonseg()) {
                compressed = StaticUtils.compressSpaces(uncompressed);
            } else {
                compressed = uncompressed;
            }
        }

        // getting the translation
        String translation = filter.privateProcessEntry(compressed, null);

        // writing out uncompressed
        if (compressed.equals(translation) && !options.getCompressWhitespace())
            translation = uncompressed;

        // converting & < and > into &amp; &lt; and &gt; respectively
        // note that this doesn't change < and > of tag shortcuts
        translation = charsToEntities(translation);
        // expands tag shortcuts into full-blown tags
        translation = unshorcutize(translation);
        // writing out the paragraph into target file
        writeout(spacePrefix);
        writeout(translation);
        writeout(spacePostfix);

        // writing out all tags after the "last good" one
        for (int i = lastgood + 1; i < all.size(); i++) {
            Node node = all.get(i);
            if (node instanceof Tag)
                writeout("<" + node.getText() + ">");
            else
                writeout(compressWhitespace(node.getText()));
        }

        cleanup();
    }

    /**
     * Inits a new paragraph.
     */
    private void cleanup() {
        text = false;
        recurse = true;
        // paragraph = new StringBuffer();
        befors = new ArrayList<Node>();
        translatable = new ArrayList<Node>();
        afters = new ArrayList<Node>();
        s_tags = new ArrayList<Tag>();
        s_tag_numbers = new ArrayList<Integer>();
        s_shortcuts = new ArrayList<String>();
        s_nshortcuts = 0;
    }

    /**
     * Creates and stores a shortcut for the tag.
     */
    private void shortcut(Tag tag, StringBuffer paragraph) {
        StringBuffer result = new StringBuffer();
        result.append('<');
        int n = -1;
        if (tag.isEndTag()) {
            result.append('/');
            // trying to lookup for appropriate starting tag
            int recursion = 1;
            for (int i = s_tags.size() - 1; i >= 0; i--) {
                Tag othertag = s_tags.get(i);
                if (othertag.getTagName().equals(tag.getTagName())) {
                    if (othertag.isEndTag())
                        recursion++;
                    else {
                        recursion--;
                        if (recursion == 0) {
                            // we've found a starting tag for this ending one
                            // !!!
                            n = s_tag_numbers.get(i);
                            break;
                        }
                    }
                }
            }
            if (n < 0) {
                // ending tag without a starting one
                n = s_nshortcuts;
                s_nshortcuts++;
            }
        } else {
            n = s_nshortcuts;
            s_nshortcuts++;
        }

        // special handling for BR tag, as it's given a two-char shortcut
        // to allow for its segmentation in sentence-segmentation mode
        // idea by Jean-Christophe Helary
        if ("BR".equals(tag.getTagName()))
            result.append("br");
        else
            result.append(Character.toLowerCase(tag.getTagName().charAt(0)));

        result.append(n);
        if (tag.isEmptyXmlTag()) // This only detects tags that already have a
                                 // slash in the source,
            result.append('/'); // but ignores HTML 4.x style <br>, <img>, and
                                // similar tags without one
                                // The code below would fix that, but breaks
                                // backwards compatibility
                                // with previously translated HTML files
        // if (tag.isEmptyXmlTag() || tag.getTagName().equals("BR") ||
        // tag.getTagName().equals("IMG"))
        // result.append('/');
        result.append('>');

        String shortcut = result.toString();
        s_tags.add(tag);
        s_tag_numbers.add(n);
        s_shortcuts.add(shortcut);
        paragraph.append(shortcut);
    }

    /**
     * Recovers tag shortcuts into full tags.
     */
    private String unshorcutize(String str) {
        for (int i = 0; i < s_shortcuts.size(); i++) {
            String shortcut = s_shortcuts.get(i);
            int pos = -1;
            while ((pos = str.indexOf(shortcut, pos + 1)) >= 0) {
                Tag tag = s_tags.get(i);
                try {
                    str = str.substring(0, pos) + "<" + tag.getText() + ">"
                            + str.substring(pos + shortcut.length());
                } catch (StringIndexOutOfBoundsException sioobe) {
                    // nothing, string doesn't change
                    // but prevent endless loop
                    break;
                }
            }
        }
        return str;
    }

    /**
     * Queues the text to the translatable paragraph.
     * <p>
     * Note that the queued text (if not-purely-whitespace) will also append the
     * previously queued tags and whitespace tags to the translatable paragraph.
     * <p>
     * Whitespace text is simply added to the queue.
     */
    private void queueTranslatable(Text text) {
        if (text.toHtml().trim().length() > 0) {
            translatable.addAll(afters);
            afters.clear();
            translatable.add(text);
        } else
            afters.add(text);
    }

    /**
     * Queues the tag to the translatable paragraph.
     * <p>
     * Note that the tag is simply added to the queue, and will be appended to
     * the translatable text only if some meaningful text follows it.
     */
    private void queueTranslatable(Tag tag) {
        afters.add(tag);
    }

    /**
     * Queues up something, possibly before a text. If the text is collected
     * now, the tag is queued up as translatable by calling
     * {@link #queueTranslatable(Tag)}, otherwise it's collected to a special
     * list that is inspected when the translatable text is sent to OmegaT core.
     */
    protected void queuePrefix(Tag tag) {
        if (text)
            queueTranslatable(tag);
        else if (isParagraphTag(tag)) {
            flushbefors();
            writeout("<" + tag.getText() + ">");
        } else
            befors.add(tag);
    }

    /**
     * Queues up some text, possibly before a meaningful text. If the text is
     * collected now, the tag is queued up as translatable by calling
     * {@link #queueTranslatable(Tag)}, otherwise it's collected to a special
     * list that is inspected when the translatable text is sent to OmegaT core.
     */
    private void queuePrefix(Text text) {
        befors.add(text);
    }

    /** Saves "Befors" to output stream and cleans the list. */
    private void flushbefors() {
        for (Node node : befors) {
            if (node instanceof Tag)
                writeout("<" + node.getText() + ">");
            else
                writeout(compressWhitespace(node.getText()));
        }
        befors.clear();
    }

    /**
     * Remove consecutive whitespace if otions.getCompressWhitespace()==true, and only space+tab is removed. 
     * Newlines are not touched, to preserve the layout a little more.
     * NB: We cannot use StaticUtils.compressSpaces, because it trims a string consisting of only whitespace to the empty string.
     * @param input some text outside / between tags where it is allowed to compress spaces.
     * @return the compressed input.
     */
    private String compressWhitespace(String input) {
        if (options.getCompressWhitespace()) {
            Matcher whitespaceMatch = PatternConsts.SPACE_TAB.matcher(input);
            return whitespaceMatch.replaceAll(" "); //keep at least 1 space, as not to change the meaning of the document.
        } else {
            return input;
        }
    }

    /** Named HTML Entities and corresponding numeric character references */
    private static final Object ENTITIES[][] = {
            { "quot", new Integer(34) },
            { "amp", new Integer(38) },
            { "lt", new Integer(60) },
            { "gt", new Integer(62) },

            // Latin Extended-A
            { "OElig", new Integer(338) }, // latin capital ligature OE, U+0152
                                           // ISOlat2
            { "oelig", new Integer(339) }, // latin small ligature oe, U+0153
                                           // ISOlat2
            // ligature is a misnomer, this is a separate character in some
            // languages
            { "Scaron", new Integer(352) }, // latin capital letter S with
                                            // caron, U+0160 ISOlat2
            { "scaron", new Integer(353) }, // latin small letter s with caron,
                                            // U+0161 ISOlat2
            { "Yuml", new Integer(376) }, // latin capital letter Y with
                                          // diaeresis, U+0178 ISOlat2

            // Spacing Modifier Letters
            { "circ", new Integer(710) }, // modifier letter circumflex accent,
                                          // U+02C6 ISOpub
            { "tilde", new Integer(732) }, // small tilde, U+02DC ISOdia

            // General Punctuation
            { "ensp", new Integer(8194) }, // en space, U+2002 ISOpub
            { "emsp", new Integer(8195) }, // em space, U+2003 ISOpub
            { "thinsp", new Integer(8201) }, // thin space, U+2009 ISOpub
            { "zwnj", new Integer(8204) }, // zero width non-joiner, U+200C NEW
                                           // RFC 2070
            { "zwj", new Integer(8205) }, // zero width joiner, U+200D NEW RFC
                                          // 2070
            { "lrm", new Integer(8206) }, // left-to-right mark, U+200E NEW RFC
                                          // 2070
            { "rlm", new Integer(8207) }, // right-to-left mark, U+200F NEW RFC
                                          // 2070
            { "ndash", new Integer(8211) }, // en dash, U+2013 ISOpub
            { "mdash", new Integer(8212) }, // em dash, U+2014 ISOpub
            { "lsquo", new Integer(8216) }, // left single quotation mark,
                                            // U+2018 ISOnum
            { "rsquo", new Integer(8217) }, // right single quotation mark,
                                            // U+2019 ISOnum
            { "sbquo", new Integer(8218) }, // single low-9 quotation mark,
                                            // U+201A NEW
            { "ldquo", new Integer(8220) }, // left double quotation mark,
                                            // U+201C ISOnum
            { "rdquo", new Integer(8221) }, // right double quotation mark,
                                            // U+201D ISOnum
            { "bdquo", new Integer(8222) }, // double low-9 quotation mark,
                                            // U+201E NEW
            { "dagger", new Integer(8224) }, // dagger, U+2020 ISOpub
            { "Dagger", new Integer(8225) }, // double dagger, U+2021 ISOpub
            { "permil", new Integer(8240) }, // per mille sign, U+2030 ISOtech
            { "lsaquo", new Integer(8249) }, // single left-pointing angle
                                             // quotation mark, U+2039 ISO
                                             // proposed
            // lsaquo is proposed but not yet ISO standardized
            { "rsaquo", new Integer(8250) }, // single right-pointing angle
                                             // quotation mark, U+203A ISO
                                             // proposed
            // rsaquo is proposed but not yet ISO standardized
            { "euro", new Integer(8364) }, // euro sign, U+20AC NEW

            { "nbsp", new Integer(160) }, { "iexcl", new Integer(161) }, { "cent", new Integer(162) },
            { "pound", new Integer(163) }, { "curren", new Integer(164) }, { "yen", new Integer(165) },
            { "brvbar", new Integer(166) }, { "sect", new Integer(167) }, { "uml", new Integer(168) },
            { "copy", new Integer(169) }, { "ordf", new Integer(170) }, { "laquo", new Integer(171) },
            { "not", new Integer(172) }, { "shy", new Integer(173) }, { "reg", new Integer(174) },
            { "macr", new Integer(175) }, { "deg", new Integer(176) }, { "plusmn", new Integer(177) },
            { "sup2", new Integer(178) }, { "sup3", new Integer(179) }, { "acute", new Integer(180) },
            { "micro", new Integer(181) }, { "para", new Integer(182) }, { "middot", new Integer(183) },
            { "cedil", new Integer(184) }, { "sup1", new Integer(185) }, { "ordm", new Integer(186) },
            { "raquo", new Integer(187) }, { "frac14", new Integer(188) }, { "frac12", new Integer(189) },
            { "frac34", new Integer(190) }, { "iquest", new Integer(191) }, { "Agrave", new Integer(192) },
            { "Aacute", new Integer(193) }, { "Acirc", new Integer(194) }, { "Atilde", new Integer(195) },
            { "Auml", new Integer(196) }, { "Aring", new Integer(197) }, { "AElig", new Integer(198) },
            { "Ccedil", new Integer(199) }, { "Egrave", new Integer(200) }, { "Eacute", new Integer(201) },
            { "Ecirc", new Integer(202) }, { "Euml", new Integer(203) }, { "Igrave", new Integer(204) },
            { "Iacute", new Integer(205) }, { "Icirc", new Integer(206) }, { "Iuml", new Integer(207) },
            { "ETH", new Integer(208) }, { "Ntilde", new Integer(209) }, { "Ograve", new Integer(210) },
            { "Oacute", new Integer(211) }, { "Ocirc", new Integer(212) }, { "Otilde", new Integer(213) },
            { "Ouml", new Integer(214) }, { "times", new Integer(215) }, { "Oslash", new Integer(216) },
            { "Ugrave", new Integer(217) }, { "Uacute", new Integer(218) }, { "Ucirc", new Integer(219) },
            { "Uuml", new Integer(220) }, { "Yacute", new Integer(221) }, { "THORN", new Integer(222) },
            { "szlig", new Integer(223) }, { "agrave", new Integer(224) }, { "aacute", new Integer(225) },
            { "acirc", new Integer(226) }, { "atilde", new Integer(227) }, { "auml", new Integer(228) },
            { "aring", new Integer(229) }, { "aelig", new Integer(230) }, { "ccedil", new Integer(231) },
            { "egrave", new Integer(232) }, { "eacute", new Integer(233) }, { "ecirc", new Integer(234) },
            { "euml", new Integer(235) }, { "igrave", new Integer(236) }, { "iacute", new Integer(237) },
            { "icirc", new Integer(238) }, { "iuml", new Integer(239) }, { "eth", new Integer(240) },
            { "ntilde", new Integer(241) }, { "ograve", new Integer(242) }, { "oacute", new Integer(243) },
            { "ocirc", new Integer(244) }, { "otilde", new Integer(245) }, { "ouml", new Integer(246) },
            { "divide", new Integer(247) }, { "oslash", new Integer(248) }, { "ugrave", new Integer(249) },
            { "uacute", new Integer(250) }, { "ucirc", new Integer(251) }, { "uuml", new Integer(252) },
            { "yacute", new Integer(253) }, { "thorn", new Integer(254) }, { "yuml", new Integer(255) },

            { "fnof", new Integer(402) },

            { "Alpha", new Integer(913) }, { "Beta", new Integer(914) }, { "Gamma", new Integer(915) },
            { "Delta", new Integer(916) }, { "Epsilon", new Integer(917) }, { "Zeta", new Integer(918) },
            { "Eta", new Integer(919) }, { "Theta", new Integer(920) }, { "Iota", new Integer(921) },
            { "Kappa", new Integer(922) }, { "Lambda", new Integer(923) }, { "Mu", new Integer(924) },
            { "Nu", new Integer(925) }, { "Xi", new Integer(926) }, { "Omicron", new Integer(927) },
            { "Pi", new Integer(928) }, { "Rho", new Integer(929) }, { "Sigma", new Integer(931) },
            { "Tau", new Integer(932) }, { "Upsilon", new Integer(933) }, { "Phi", new Integer(934) },
            { "Chi", new Integer(935) }, { "Psi", new Integer(936) }, { "Omega", new Integer(937) },
            { "alpha", new Integer(945) }, { "beta", new Integer(946) }, { "gamma", new Integer(947) },
            { "delta", new Integer(948) }, { "epsilon", new Integer(949) }, { "zeta", new Integer(950) },
            { "eta", new Integer(951) }, { "theta", new Integer(952) }, { "iota", new Integer(953) },
            { "kappa", new Integer(954) }, { "lambda", new Integer(955) }, { "mu", new Integer(956) },
            { "nu", new Integer(957) }, { "xi", new Integer(958) }, { "omicron", new Integer(959) },
            { "pi", new Integer(960) }, { "rho", new Integer(961) }, { "sigmaf", new Integer(962) },
            { "sigma", new Integer(963) }, { "tau", new Integer(964) }, { "upsilon", new Integer(965) },
            { "phi", new Integer(966) }, { "chi", new Integer(967) }, { "psi", new Integer(968) },
            { "omega", new Integer(969) }, { "thetasym", new Integer(977) }, { "upsih", new Integer(978) },
            { "piv", new Integer(982) },

            { "bull", new Integer(8226) }, { "hellip", new Integer(8230) }, { "prime", new Integer(8242) },
            { "Prime", new Integer(8243) }, { "oline", new Integer(8254) }, { "frasl", new Integer(8260) },

            { "weierp", new Integer(8472) }, { "image", new Integer(8465) }, { "real", new Integer(8476) },
            { "trade", new Integer(8482) }, { "alefsym", new Integer(8501) },

            { "larr", new Integer(8592) }, { "uarr", new Integer(8593) }, { "rarr", new Integer(8594) },
            { "darr", new Integer(8595) }, { "harr", new Integer(8596) }, { "crarr", new Integer(8629) },
            { "lArr", new Integer(8656) }, { "uArr", new Integer(8657) }, { "rArr", new Integer(8658) },
            { "dArr", new Integer(8659) }, { "hArr", new Integer(8660) },

            { "forall", new Integer(8704) }, { "part", new Integer(8706) }, { "exist", new Integer(8707) },
            { "empty", new Integer(8709) }, { "nabla", new Integer(8711) }, { "isin", new Integer(8712) },
            { "notin", new Integer(8713) }, { "ni", new Integer(8715) }, { "prod", new Integer(8719) },
            { "sum", new Integer(8722) }, { "minus", new Integer(8722) }, { "lowast", new Integer(8727) },
            { "radic", new Integer(8730) }, { "prop", new Integer(8733) }, { "infin", new Integer(8734) },
            { "ang", new Integer(8736) }, { "and", new Integer(8869) }, { "or", new Integer(8870) },
            { "cap", new Integer(8745) }, { "cup", new Integer(8746) }, { "int", new Integer(8747) },
            { "there4", new Integer(8756) }, { "sim", new Integer(8764) }, { "cong", new Integer(8773) },
            { "asymp", new Integer(8773) }, { "ne", new Integer(8800) }, { "equiv", new Integer(8801) },
            { "le", new Integer(8804) }, { "ge", new Integer(8805) }, { "sub", new Integer(8834) },
            { "sup", new Integer(8835) }, { "nsub", new Integer(8836) }, { "sube", new Integer(8838) },
            { "supe", new Integer(8839) }, { "oplus", new Integer(8853) }, { "otimes", new Integer(8855) },
            { "perp", new Integer(8869) }, { "sdot", new Integer(8901) },

            { "lceil", new Integer(8968) }, { "rceil", new Integer(8969) }, { "lfloor", new Integer(8970) },
            { "rfloor", new Integer(8971) }, { "lang", new Integer(9001) }, { "rang", new Integer(9002) },

            { "loz", new Integer(9674) },

            { "spades", new Integer(9824) }, { "clubs", new Integer(9827) }, { "hearts", new Integer(9829) },
            { "diams", new Integer(9830) }, };

    /** Converts HTML entities to normal characters */
    protected String entitiesToChars(String str) {
        int strlen = str.length();
        StringBuffer res = new StringBuffer(strlen);
        for (int i = 0; i < strlen; i++) {
            char ch = str.charAt(i);
            switch (ch) {
            case '&':
                char ch1;
                // if there's one more symbol, reading it,
                // otherwise it's a dangling '&'
                if ((i + 1) >= strlen) {
                    res.append(ch);
                    break;
                } else
                    ch1 = str.charAt(i + 1);
                if (ch1 == '#') {
                    // numeric entity
                    char ch2 = str.charAt(i + 2);
                    if (ch2 == 'x' || ch2 == 'X') {
                        // hex numeric entity
                        int n = i + 3;
                        while (n < strlen && isHexDigit(str.charAt(n)))
                            n++;
                        String s_entity = str.substring(i + 3, n);
                        try {
                            int n_entity = Integer.parseInt(s_entity, 16);
                            if (n_entity > 0 && n_entity <= 65535) {
                                res.append((char) n_entity);
                                if (n < strlen && str.charAt(n) == ';')
                                    i = n;
                                else
                                    i = n - 1;
                            } else {
                                // too big number
                                // dangling '&'
                                res.append(ch);
                            }
                        } catch (NumberFormatException nfe) {
                            // do nothing
                            // dangling '&'
                            res.append(ch);
                        }
                    } else {
                        // decimal entity
                        int n = i + 2;
                        while (n < strlen && isDecimalDigit(str.charAt(n)))
                            n++;
                        String s_entity = str.substring(i + 2, n);
                        try {
                            int n_entity = Integer.parseInt(s_entity, 10);
                            if (n_entity > 0 && n_entity <= 65535) {
                                res.append((char) n_entity);
                                if (n < strlen && str.charAt(n) == ';')
                                    i = n;
                                else
                                    i = n - 1;
                            } else {
                                // too big number
                                // dangling '&'
                                res.append(ch);
                            }
                        } catch (NumberFormatException nfe) {
                            // do nothing
                            // dangling '&'
                            res.append(ch);
                        }
                    }
                } else if (isLatinLetter(ch1)) {
                    // named entity?
                    int n = i + 1;
                    while (n < strlen && (isLatinLetter(str.charAt(n)) || // Some
                                                                          // entities
                            isDecimalDigit(str.charAt(n))) // contain numbers
                    )
                        // e.g., frac12
                        n++;
                    String s_entity = str.substring(i + 1, n);
                    int n_entity = lookupEntity(s_entity);
                    if (n_entity > 0 && n_entity <= 65535) {
                        res.append((char) n_entity);
                        if (n < strlen && str.charAt(n) == ';')
                            i = n;
                        else
                            i = n - 1;
                    } else {
                        // too big number
                        // dangling '&'
                        res.append(ch);
                    }
                } else {
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
    private boolean isLatinLetter(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    /** Returns true if a char is a decimal digit */
    private boolean isDecimalDigit(char ch) {
        return (ch >= '0' && ch <= '9');
    }

    /** Returns true if a char is a hex digit */
    private boolean isHexDigit(char ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
    }

    /**
     * returns a character for HTML entity, or -1 if the passed string is not an
     * entity
     */
    private int lookupEntity(String entity) {
        for (int i = 0; i < ENTITIES.length; i++) {
            Object[] ONENT = ENTITIES[i];
            if (entity.equals(ONENT[0]))
                return ((Integer) ONENT[1]).intValue();
        }
        return -1;
    }

    /**
     * Converts characters that must be converted (&lt; &gt; &amp; '&nbsp;'
     * (nbsp)) into HTML entities
     */
    protected String charsToEntities(String str) {
        int strlen = str.length();
        StringBuffer res = new StringBuffer(strlen * 5);
        for (int i = 0; i < strlen; i++) {
            char ch = str.charAt(i);
            switch (ch) {
            case '\u00A0':
                res.append("&nbsp;");
                break;
            case '&':
                res.append("&amp;");
                break;
            case '>':
                // If it's the end of a processing instruction
                if ((i > 0) && str.substring(i-1, i).contentEquals("?")) {
                   res.append(">"); 
                } else {
                    res.append("&gt;");
                }
                break;
            case '<':
                int qMarkPos = str.indexOf('?', i);
                // If it's the beginning of a processing instruction
                if (qMarkPos == i+1) {
                    res.append("<");
                    break;
                }
                int gtpos = str.indexOf('>', i);
                if (gtpos >= 0) {
                    String maybeShortcut = str.substring(i, gtpos + 1);
                    boolean foundShortcut = false; // here because it's
                                                   // impossible to step out of
                                                   // two loops at once
                    for (String currShortcut : s_shortcuts) {
                        if (maybeShortcut.equals(currShortcut)) {
                            // skipping the conversion of < into &lt;
                            // because it's a part of the tag
                            foundShortcut = true;
                            break;
                        }
                    }
                    if (foundShortcut) {
                        res.append(maybeShortcut);
                        i = gtpos;
                        continue;
                    } else {
                        // dangling <
                        res.append("&lt;");
                    }
                } else {
                    // dangling <
                    res.append("&lt;");
                }
                break;
            default:
                res.append(ch);
            }
        }
        String contents = res.toString();
        // Rewrite characters that cannot be encoded to html character strings.
        // Each character in the contents-string is checked. If a character
        // can't be encoded, all its occurrences are replaced with the
        // html-equivalent string.
        // Then, the next character is checked.
        // (The loop over the contents-string is restarted for the modified
        // content, but the starting-position will be the position where the
        // last unencodable character was found)
        // [1802000] HTML filter loses html-encoded characters if not supported
        String encoding = this.filter.getTargetEncoding();
        if (encoding != null) {
            CharsetEncoder charsetEncoder = Charset.forName(encoding).newEncoder();
            int i = 0;
            boolean notfinished = true;
            while (notfinished) {
                for (; i < contents.length(); i++) {
                    char x = contents.charAt(i);
                    if (!charsetEncoder.canEncode(x)) {
                        String regexp;
                        if (x == '[' || x == '\\' || x == '^' || x == '$' || x == '.' || x == '|' || x == '?'
                                || x == '*' || x == '+' || x == '(' || x == ')') {
                            // escape special regexp characters
                            regexp = "\\" + x;
                        } else
                            regexp = "" + x;
                        String replacement = "&#" + (int) x + ';';
                        contents = contents.replaceAll(regexp, replacement);
                        break;
                    }
                }
                if (i == contents.length())
                    notfinished = false;
            }
        }
        return contents;
    }

}
