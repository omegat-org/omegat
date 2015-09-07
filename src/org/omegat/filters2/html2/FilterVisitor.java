/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2008 Didier Briel, Martin Fleurke
               2010 Didier Briel
               2011 Didier Briel, Martin Fleurke
               2012 Didier Briel, Martin Fleurke
               2013 Didier Briel, Alex Buloichik
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
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        // HHC filter has no options
        if (options != null) {       
            this.options = options;  
        } else {                                                           
            // To prevent a null pointer exception later, see https://sourceforge.net/p/omegat/bugs/651/
            this.options = new HTMLOptions(new TreeMap<String, String>()); 
        }
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
            if ("INPUT".equals(tag.getTagName())) { //an input element
                if (   options.getTranslateValue() //and we translate all input elements
                    || options.getTranslateButtonValue() // or we translate submit/button/reset elements ...
                        && (  "submit".equalsIgnoreCase(tag.getAttribute("type"))
                           || "button".equalsIgnoreCase(tag.getAttribute("type"))
                           || "reset".equalsIgnoreCase(tag.getAttribute("type"))
                           ) //and it is a submit/button/reset element.
                   ) {
                    //then translate the value
                    maybeTranslateAttribute(tag, "value");
                }
                maybeTranslateAttribute(tag, "placeholder");
            }
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
        if (!trimmedtext.isEmpty()) {
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
        StringBuilder paragraph = new StringBuilder();
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

            for (int cp, i = 0; i < size; i += Character.charCount(cp)) {
                cp = uncompressed.codePointAt(i);
                if (!Character.isWhitespace(cp)) {
                    spacePrefix = i == 0 ? "" : uncompressed.substring(0,
                            options.getCompressWhitespace() ? Math.min(i, uncompressed.offsetByCodePoints(i, 1)) : i);
                    break;
                }
            }
            for (int cp, i = size; i > 0; i -= Character.charCount(cp)) {
                cp = uncompressed.codePointBefore(i);
                if (!Character.isWhitespace(cp)) {
                    spacePostfix = i == size ? "" : uncompressed.substring(i,
                            options.getCompressWhitespace() ? Math.min(uncompressed.offsetByCodePoints(i, 1), size) : size);
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
    private void shortcut(Tag tag, StringBuilder paragraph) {
        StringBuilder result = new StringBuilder();
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
        if ("BR".equals(tag.getTagName())) {
            result.append("br");
        } else {
            result.appendCodePoint(Character.toLowerCase(tag.getTagName().codePointAt(0)));
        }

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
        if (!text.toHtml().trim().isEmpty()) {
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
            { "quot", 34 },
            { "amp", 38 },
            { "lt", 60 },
            { "gt", 62 },

            // Latin Extended-A
            { "OElig", 338 }, // latin capital ligature OE, U+0152
                                           // ISOlat2
            { "oelig", 339 }, // latin small ligature oe, U+0153
                                           // ISOlat2
            // ligature is a misnomer, this is a separate character in some
            // languages
            { "Scaron", 352 }, // latin capital letter S with
                                            // caron, U+0160 ISOlat2
            { "scaron", 353 }, // latin small letter s with caron,
                                            // U+0161 ISOlat2
            { "Yuml", 376 }, // latin capital letter Y with
                                          // diaeresis, U+0178 ISOlat2

            // Spacing Modifier Letters
            { "circ", 710 }, // modifier letter circumflex accent,
                                          // U+02C6 ISOpub
            { "tilde", 732 }, // small tilde, U+02DC ISOdia

            // General Punctuation
            { "ensp", 8194 }, // en space, U+2002 ISOpub
            { "emsp", 8195 }, // em space, U+2003 ISOpub
            { "thinsp", 8201 }, // thin space, U+2009 ISOpub
            { "zwnj", 8204 }, // zero width non-joiner, U+200C NEW
                                           // RFC 2070
            { "zwj", 8205 }, // zero width joiner, U+200D NEW RFC
                                          // 2070
            { "lrm", 8206 }, // left-to-right mark, U+200E NEW RFC
                                          // 2070
            { "rlm", 8207 }, // right-to-left mark, U+200F NEW RFC
                                          // 2070
            { "ndash", 8211 }, // en dash, U+2013 ISOpub
            { "mdash", 8212 }, // em dash, U+2014 ISOpub
            { "lsquo", 8216 }, // left single quotation mark,
                                            // U+2018 ISOnum
            { "rsquo", 8217 }, // right single quotation mark,
                                            // U+2019 ISOnum
            { "sbquo", 8218 }, // single low-9 quotation mark,
                                            // U+201A NEW
            { "ldquo", 8220 }, // left double quotation mark,
                                            // U+201C ISOnum
            { "rdquo", 8221 }, // right double quotation mark,
                                            // U+201D ISOnum
            { "bdquo", 8222 }, // double low-9 quotation mark,
                                            // U+201E NEW
            { "dagger", 8224 }, // dagger, U+2020 ISOpub
            { "Dagger", 8225 }, // double dagger, U+2021 ISOpub
            { "permil", 8240 }, // per mille sign, U+2030 ISOtech
            { "lsaquo", 8249 }, // single left-pointing angle
                                             // quotation mark, U+2039 ISO
                                             // proposed
            // lsaquo is proposed but not yet ISO standardized
            { "rsaquo", 8250 }, // single right-pointing angle
                                             // quotation mark, U+203A ISO
                                             // proposed
            // rsaquo is proposed but not yet ISO standardized
            { "euro", 8364 }, // euro sign, U+20AC NEW

            { "nbsp", 160 }, { "iexcl", 161 }, { "cent", 162 },
            { "pound", 163 }, { "curren", 164 }, { "yen", 165 },
            { "brvbar", 166 }, { "sect", 167 }, { "uml", 168 },
            { "copy", 169 }, { "ordf", 170 }, { "laquo", 171 },
            { "not", 172 }, { "shy", 173 }, { "reg", 174 },
            { "macr", 175 }, { "deg", 176 }, { "plusmn", 177 },
            { "sup2", 178 }, { "sup3", 179 }, { "acute", 180 },
            { "micro", 181 }, { "para", 182 }, { "middot", 183 },
            { "cedil", 184 }, { "sup1", 185 }, { "ordm", 186 },
            { "raquo", 187 }, { "frac14", 188 }, { "frac12", 189 },
            { "frac34", 190 }, { "iquest", 191 }, { "Agrave", 192 },
            { "Aacute", 193 }, { "Acirc", 194 }, { "Atilde", 195 },
            { "Auml", 196 }, { "Aring", 197 }, { "AElig", 198 },
            { "Ccedil", 199 }, { "Egrave", 200 }, { "Eacute", 201 },
            { "Ecirc", 202 }, { "Euml", 203 }, { "Igrave", 204 },
            { "Iacute", 205 }, { "Icirc", 206 }, { "Iuml", 207 },
            { "ETH", 208 }, { "Ntilde", 209 }, { "Ograve", 210 },
            { "Oacute", 211 }, { "Ocirc", 212 }, { "Otilde", 213 },
            { "Ouml", 214 }, { "times", 215 }, { "Oslash", 216 },
            { "Ugrave", 217 }, { "Uacute", 218 }, { "Ucirc", 219 },
            { "Uuml", 220 }, { "Yacute", 221 }, { "THORN", 222 },
            { "szlig", 223 }, { "agrave", 224 }, { "aacute", 225 },
            { "acirc", 226 }, { "atilde", 227 }, { "auml", 228 },
            { "aring", 229 }, { "aelig", 230 }, { "ccedil", 231 },
            { "egrave", 232 }, { "eacute", 233 }, { "ecirc", 234 },
            { "euml", 235 }, { "igrave", 236 }, { "iacute", 237 },
            { "icirc", 238 }, { "iuml", 239 }, { "eth", 240 },
            { "ntilde", 241 }, { "ograve", 242 }, { "oacute", 243 },
            { "ocirc", 244 }, { "otilde", 245 }, { "ouml", 246 },
            { "divide", 247 }, { "oslash", 248 }, { "ugrave", 249 },
            { "uacute", 250 }, { "ucirc", 251 }, { "uuml", 252 },
            { "yacute", 253 }, { "thorn", 254 }, { "yuml", 255 },

            { "fnof", 402 },

            { "Alpha", 913 }, { "Beta", 914 }, { "Gamma", 915 },
            { "Delta", 916 }, { "Epsilon", 917 }, { "Zeta", 918 },
            { "Eta", 919 }, { "Theta", 920 }, { "Iota", 921 },
            { "Kappa", 922 }, { "Lambda", 923 }, { "Mu", 924 },
            { "Nu", 925 }, { "Xi", 926 }, { "Omicron", 927 },
            { "Pi", 928 }, { "Rho", 929 }, { "Sigma", 931 },
            { "Tau", 932 }, { "Upsilon", 933 }, { "Phi", 934 },
            { "Chi", 935 }, { "Psi", 936 }, { "Omega", 937 },
            { "alpha", 945 }, { "beta", 946 }, { "gamma", 947 },
            { "delta", 948 }, { "epsilon", 949 }, { "zeta", 950 },
            { "eta", 951 }, { "theta", 952 }, { "iota", 953 },
            { "kappa", 954 }, { "lambda", 955 }, { "mu", 956 },
            { "nu", 957 }, { "xi", 958 }, { "omicron", 959 },
            { "pi", 960 }, { "rho", 961 }, { "sigmaf", 962 },
            { "sigma", 963 }, { "tau", 964 }, { "upsilon", 965 },
            { "phi", 966 }, { "chi", 967 }, { "psi", 968 },
            { "omega", 969 }, { "thetasym", 977 }, { "upsih", 978 },
            { "piv", 982 },

            { "bull", 8226 }, { "hellip", 8230 }, { "prime", 8242 },
            { "Prime", 8243 }, { "oline", 8254 }, { "frasl", 8260 },

            { "weierp", 8472 }, { "image", 8465 }, { "real", 8476 },
            { "trade", 8482 }, { "alefsym", 8501 },

            { "larr", 8592 }, { "uarr", 8593 }, { "rarr", 8594 },
            { "darr", 8595 }, { "harr", 8596 }, { "crarr", 8629 },
            { "lArr", 8656 }, { "uArr", 8657 }, { "rArr", 8658 },
            { "dArr", 8659 }, { "hArr", 8660 },

            { "forall", 8704 }, { "part", 8706 }, { "exist", 8707 },
            { "empty", 8709 }, { "nabla", 8711 }, { "isin", 8712 },
            { "notin", 8713 }, { "ni", 8715 }, { "prod", 8719 },
            { "sum", 8722 }, { "minus", 8722 }, { "lowast", 8727 },
            { "radic", 8730 }, { "prop", 8733 }, { "infin", 8734 },
            { "ang", 8736 }, { "and", 8869 }, { "or", 8870 },
            { "cap", 8745 }, { "cup", 8746 }, { "int", 8747 },
            { "there4", 8756 }, { "sim", 8764 }, { "cong", 8773 },
            { "asymp", 8773 }, { "ne", 8800 }, { "equiv", 8801 },
            { "le", 8804 }, { "ge", 8805 }, { "sub", 8834 },
            { "sup", 8835 }, { "nsub", 8836 }, { "sube", 8838 },
            { "supe", 8839 }, { "oplus", 8853 }, { "otimes", 8855 },
            { "perp", 8869 }, { "sdot", 8901 },

            { "lceil", 8968 }, { "rceil", 8969 }, { "lfloor", 8970 },
            { "rfloor", 8971 }, { "lang", 9001 }, { "rang", 9002 },

            { "loz", 9674 },

            { "spades", 9824 }, { "clubs", 9827 }, { "hearts", 9829 },
            { "diams", 9830 }, };

    /** Converts HTML entities to normal characters */
    protected String entitiesToChars(String str) {
        int strlen = str.length();
        StringBuilder res = new StringBuilder(strlen);
        for (int cp, i = 0; i < strlen; i += Character.charCount(cp)) {
            cp = str.codePointAt(i);
            switch (cp) {
            case '&':
                int cp1;
                // if there's one more symbol, reading it,
                // otherwise it's a dangling '&'
                if (str.codePointCount(i, strlen) < 2) {
                    res.appendCodePoint(cp);
                    break;
                } else {
                    cp1 = str.codePointAt(str.offsetByCodePoints(i, 1));
                }
                if (cp1 == '#') {
                    // numeric entity
                    int cp2 = str.codePointAt(str.offsetByCodePoints(i, 2));
                    if (cp2 == 'x' || cp2 == 'X') {
                        // hex numeric entity
                        int hexStart = str.offsetByCodePoints(i, 3);
                        int hexEnd = hexStart;
                        while (hexEnd < strlen) {
                            int hexCp = str.codePointAt(hexEnd);
                            if (!isHexDigit(hexCp)) {
                                break;
                            }
                            hexEnd += Character.charCount(hexCp);
                        }
                        String s_entity = str.substring(hexStart, hexEnd);
                        try {
                            int n_entity = Integer.parseInt(s_entity, 16);
                            if (n_entity > 0 && n_entity <= 0x10FFFF) {
                                res.appendCodePoint(n_entity);
                                if (hexEnd < strlen && str.codePointAt(hexEnd) == ';') {
                                    i = hexEnd;
                                } else {
                                    i = str.offsetByCodePoints(hexEnd, -1);
                                }
                            } else {
                                // too big number
                                // dangling '&'
                                res.appendCodePoint(cp);
                            }
                        } catch (NumberFormatException nfe) {
                            // do nothing
                            // dangling '&'
                            res.appendCodePoint(cp);
                        }
                    } else {
                        // decimal entity
                        int decStart = str.offsetByCodePoints(i, 2);
                        int decEnd = decStart;
                        while (decEnd < strlen) {
                            int decCp = str.codePointAt(decEnd);
                            if (!isDecimalDigit(decCp)) {
                                break;
                            }
                            decEnd += Character.charCount(decCp);
                        }
                        String s_entity = str.substring(decStart, decEnd);
                        try {
                            int n_entity = Integer.parseInt(s_entity, 10);
                            if (n_entity > 0 && n_entity <= 0x10FFFF) {
                                res.appendCodePoint(n_entity);
                                if (decEnd < strlen && str.codePointAt(decEnd) == ';') {
                                    i = decEnd;
                                } else {
                                    i = str.offsetByCodePoints(decEnd, -1);
                                }
                            } else {
                                // too big number
                                // dangling '&'
                                res.appendCodePoint(cp);
                            }
                        } catch (NumberFormatException nfe) {
                            // do nothing
                            // dangling '&'
                            res.appendCodePoint(cp);
                        }
                    }
                } else if (isLatinLetter(cp1)) {
                    // named entity?
                    int entStart = str.offsetByCodePoints(i, 1);
                    int entEnd = entStart;
                    while (entEnd < strlen) {
                        int entCp = str.codePointAt(entEnd);
                        // Some entities contain numbers, e.g. frac12
                        if (!isLatinLetter(entCp) && !isDecimalDigit(entCp)) {
                            break;
                        }
                        entEnd += Character.charCount(entCp);
                    }
                    String s_entity = str.substring(entStart, entEnd);
                    int n_entity = lookupEntity(s_entity);
                    if (n_entity > 0 && n_entity <= 65535) {
                        res.append((char) n_entity);
                        if (entEnd < strlen && str.codePointAt(entEnd) == ';') {
                            i = entEnd;
                        } else {
                            i = str.offsetByCodePoints(entEnd, -1);
                        }
                    } else {
                        // too big number
                        // dangling '&'
                        res.appendCodePoint(cp);
                    }
                } else {
                    // dangling '&'
                    res.appendCodePoint(cp);
                }
                break;
            default:
                res.appendCodePoint(cp);
            }
        }
        return res.toString();
    }

    /** Returns true if a char is a latin letter */
    private boolean isLatinLetter(int ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    /** Returns true if a char is a decimal digit */
    private boolean isDecimalDigit(int ch) {
        return (ch >= '0' && ch <= '9');
    }

    /** Returns true if a char is a hex digit */
    private boolean isHexDigit(int ch) {
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
        StringBuilder res = new StringBuilder(strlen * 5);
        for (int cp, i = 0; i < strlen; i += Character.charCount(cp)) {
            cp = str.codePointAt(i);
            switch (cp) {
            case '\u00A0':
                res.append("&nbsp;");
                break;
            case '&':
                res.append("&amp;");
                break;
            case '>':
                // If it's the end of a processing instruction
                if ((i > 0) && str.codePointBefore(i) == '?') {
                   res.append(">"); 
                } else {
                    res.append("&gt;");
                }
                break;
            case '<':
                int qMarkPos = str.indexOf('?', i);
                // If it's the beginning of a processing instruction
                if (qMarkPos == str.offsetByCodePoints(i, 1)) {
                    res.append("<");
                    break;
                }
                int gtpos = str.indexOf('>', i);
                if (gtpos >= 0) {
                    String maybeShortcut = str.substring(i, str.offsetByCodePoints(gtpos, 1));
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
                res.appendCodePoint(cp);
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
            while (true) {
                String substring;
                for (int cp; i < contents.length(); i += substring.length()) {
                    cp = contents.codePointAt(i);
                    substring = contents.substring(i, i + Character.charCount(cp));
                    if (!charsetEncoder.canEncode(substring)) {
                        String replacement = "&#" + cp + ';';
                        contents = contents.replaceAll(Pattern.quote(substring), replacement);
                        break;
                    }
                }
                if (i == contents.length()) {
                    break;
                }
            }
        }
        return contents;
    }

}
