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
               Support center: https://omegat.org/support

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
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
import org.omegat.util.StringUtil;

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

    public FilterVisitor(HTMLFilter2 htmlfilter, BufferedWriter bufwriter, HTMLOptions opts) {
        this.filter = htmlfilter;
        // HHC filter has no options
        if (opts != null) {
            this.options = opts;
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
    protected boolean recurse = true;

    /** Do we collect the translatable text now. */
    protected boolean text = false;
    /** The translatable text being collected. */
    // StringBuffer paragraph;
    /** Did the PRE block start (it means we mustn't compress the spaces). */
    protected boolean preformatting = false;

    /**
     * The list of non-paragraph tags before a chunk of text.
     * <ul>
     * <li>If a chunk of text follows, they get prepended to the translatable
     * paragraph, (starting from the first tag having a pair inside a chunk of
     * text)
     * <li>Otherwise they are written out directly.
     * </ul>
     */
    protected List<Node> befors;

    /** The list of nodes forming a chunk of text. */
    protected List<Node> translatable;

    /**
     * The list of non-paragraph tags following a chunk of text.
     * <ul>
     * <li>If another chunk of text follows, they get appended to the
     * translatable paragraph,
     * <li>Otherwise (paragraph tag follows), they are written out directly.
     * </ul>
     */
    protected List<Node> afters;

    /** The tags behind the shortcuts */
    protected List<Tag> sTags;
    /** The tag numbers of shorcutized tags */
    protected List<Integer> sTagNumbers;
    /** The list of all the tag shortcuts */
    protected List<String> sShortcuts;
    /** The number of shortcuts stored */
    int sNumShortcuts;

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
            Vector<?> tagAttributes = tag.getAttributesEx();
            Iterator<?> i = tagAttributes.iterator();
            while (i.hasNext() && !intactTag) {
                Attribute attribute = (Attribute) i.next();
                String name = attribute.getName();
                String value = attribute.getValue();
                if (name == null || value == null) {
                    continue;
                }
                intactTag = this.filter.checkIgnoreTags(name, value);
            }
        }

        if (intactTag) {
            if (text) {
                endup();
            } else {
                flushbefors();
            }
            writeout(tag.toHtml());
            if (tag.getEndTag() != null) {
                recurse = false;
            }
        } else {
            // recurse = true;
            if (isParagraphTag(tag) && text) {
                endup();
            }
            if (isPreformattingTag(tag) || Core.getFilterMaster().getConfig().isPreserveSpaces()) {
                preformatting = true;
            }
            // Translate attributes of tags if they are not null.
            maybeTranslateAttribute(tag, "abbr");
            maybeTranslateAttribute(tag, "alt");
            if (options.getTranslateHref()) {
                maybeTranslateAttribute(tag, "href");
            }
            if (options.getTranslateHreflang()) {
                maybeTranslateAttribute(tag, "hreflang");
            }
            if (options.getTranslateLang()) {
                maybeTranslateAttribute(tag, "lang");
                maybeTranslateAttribute(tag, "xml:lang");
            }
            maybeTranslateAttribute(tag, "label");
            if ("IMG".equals(tag.getTagName()) && options.getTranslateSrc()) {
                maybeTranslateAttribute(tag, "src");
            }
            maybeTranslateAttribute(tag, "summary");
            maybeTranslateAttribute(tag, "title");
            if ("INPUT".equals(tag.getTagName())) { //an input element
                if (options.getTranslateValue() //and we translate all input elements
                        || options.getTranslateButtonValue() // or we translate submit/button/reset elements ...
                                && ("submit".equalsIgnoreCase(tag.getAttribute("type"))
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
                Vector<?> tagAttributes = tag.getAttributesEx();
                Iterator<?> i = tagAttributes.iterator();
                boolean doSkipMetaTag = false;
                while (i.hasNext() && !doSkipMetaTag) {
                    Attribute attribute = (Attribute) i.next();
                    String name = attribute.getName();
                    String value = attribute.getValue();
                    if (name == null || value == null) {
                        continue;
                    }
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
            String comment = OStrings.getString("HTMLFILTER_TAG") + " " + tag.getTagName() + " "
                    + OStrings.getString("HTMLFILTER_ATTRIBUTE") + " " + key;
            String trans = filter.privateProcessEntry(HTMLUtils.entitiesToChars(attr), comment);
            tag.setAttribute(key, HTMLUtils.charsToEntities(trans, filter.getTargetEncoding(), sShortcuts));
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
        String trimmedtext = HTMLUtils.entitiesToChars(string.getText()).replace((char) 160, ' ').trim();
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

        if (text) {
            queueTranslatable(string);
        } else {
            queuePrefix(string);
        }
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
        if (text) {
            endup();
        }
        if (!options.getRemoveComments()) {
            writeout(remark.toHtml());
        }
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
        if (isParagraphTag(tag) && text) {
            endup();
        }
        if (isPreformattingTag(tag)) {
            preformatting = false;
        }
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
        if (text) {
            endup();
        } else {
            flushbefors();
        }
    }

    /**
     * Does the tag lead to starting (ending) a paragraph.
     * <p>
     * Contains code donated by JC to have dictionary list parsed as segmenting.
     *
     * @see <a href="https://sourceforge.net/p/omegat/feature-requests/102/">RFE
     *      #102</a>
     */
    private boolean isParagraphTag(Tag tag) {
        String tagname = tag.getTagName();
        return
        // Bugfix for https://sourceforge.net/p/omegat/bugs/84/
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
                || tagname.equals("OPTION") || tagname.equals("HR")
                // Optional paragraph on BR
                || (tagname.equals("BR") && options.getParagraphOnBr());

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
            Node goodNode = all.get(firstgood);
            if (!(goodNode instanceof Tag)) {
                firstgood++;
                continue;
            }
            Tag good = (Tag) goodNode;

            // trying to test
            int recursion = 1;
            boolean found = false;
            for (int i = firstgood + 1; i < all.size(); i++) {
                Node candNode = all.get(i);
                if (candNode instanceof Tag) {
                    Tag cand = (Tag) candNode;
                    if (cand.getTagName().equals(good.getTagName())) {
                        if (!cand.isEndTag()) {
                            recursion++;
                        } else {
                            recursion--;
                            if (recursion == 0) {
                                if (i >= firstgoodlimit) {
                                    found = true;
                                }
                                // we've found an ending tag for this "good one"
                                break;
                            }
                        }
                    }
                }
            }
            // if we could find an ending,
            // this is a "good one"
            if (found) {
                break;
            }
            firstgood++;
        }

        // detecting the last ending tag in 'afters'
        // that has its starting in the paragraph
        // all after this "last good" is simply writen out
        int lastgoodlimit = all.size() - 1;
        all.addAll(afters);
        int lastgood = all.size() - 1;
        while (lastgood > lastgoodlimit) {
            Node goodNode = all.get(lastgood);
            if (!(goodNode instanceof Tag)) {
                lastgood--;
                continue;
            }
            Tag good = (Tag) goodNode;

            // trying to test
            int recursion = 1;
            boolean found = false;
            for (int i = lastgood - 1; i >= firstgoodlimit; i--) {
                Node candNode = all.get(i);
                if (candNode instanceof Tag) {
                    Tag cand = (Tag) candNode;
                    if (cand.getTagName().equals(good.getTagName())) {
                        if (cand.isEndTag()) {
                            recursion++;
                        } else {
                            recursion--;
                            if (recursion == 0) {
                                if (i <= lastgoodlimit) {
                                    found = true;
                                }
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
            if (found) {
                break;
            }
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
            if (node instanceof Tag) {
                writeout("<" + node.getText() + ">");
            } else {
                writeout(compressWhitespace(node.getText()));
            }
        }

        // appending all tags until "last good" one to paragraph text
        StringBuilder paragraph = new StringBuilder();
        // appending all tags starting from "first good" one to paragraph text
        for (int i = firstgood; i <= lastgood; i++) {
            Node node = all.get(i);
            if (node instanceof Tag) {
                shortcut((Tag) node, paragraph);
            } else { // node instanceof Text
                paragraph.append(HTMLUtils.entitiesToChars(node.toHtml()));
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
        // https://sourceforge.net/p/omegat/bugs/108/
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
                    spacePostfix = i == size ? ""
                            : uncompressed.substring(i, options.getCompressWhitespace()
                                    ? Math.min(uncompressed.offsetByCodePoints(i, 1), size) : size);
                    break;
                }
            }

            if (Core.getFilterMaster().getConfig().isRemoveSpacesNonseg()) {
                compressed = StringUtil.compressSpaces(uncompressed);
            } else {
                compressed = uncompressed;
            }
        }

        // getting the translation
        String translation = filter.privateProcessEntry(compressed, null);

        // writing out uncompressed
        if (compressed.equals(translation) && !options.getCompressWhitespace()) {
            translation = uncompressed;
        }

        // converting & < and > into &amp; &lt; and &gt; respectively
        // note that this doesn't change < and > of tag shortcuts
        translation = HTMLUtils.charsToEntities(translation, filter.getTargetEncoding(), sShortcuts);
        // expands tag shortcuts into full-blown tags
        translation = unshorcutize(translation);
        // writing out the paragraph into target file
        writeout(spacePrefix);
        writeout(translation);
        writeout(spacePostfix);

        // writing out all tags after the "last good" one
        for (int i = lastgood + 1; i < all.size(); i++) {
            Node node = all.get(i);
            if (node instanceof Tag) {
                writeout("<" + node.getText() + ">");
            } else {
                writeout(compressWhitespace(node.getText()));
            }
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
        befors = new ArrayList<>();
        translatable = new ArrayList<>();
        afters = new ArrayList<>();
        sTags = new ArrayList<>();
        sTagNumbers = new ArrayList<>();
        sShortcuts = new ArrayList<>();
        sNumShortcuts = 0;
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
            for (int i = sTags.size() - 1; i >= 0; i--) {
                Tag othertag = sTags.get(i);
                if (othertag.getTagName().equals(tag.getTagName())) {
                    if (othertag.isEndTag()) {
                        recursion++;
                    } else {
                        recursion--;
                        if (recursion == 0) {
                            // we've found a starting tag for this ending one
                            // !!!
                            n = sTagNumbers.get(i);
                            break;
                        }
                    }
                }
            }
            if (n < 0) {
                // ending tag without a starting one
                n = sNumShortcuts;
                sNumShortcuts++;
            }
        } else {
            n = sNumShortcuts;
            sNumShortcuts++;
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
        if (tag.isEmptyXmlTag()) { // This only detects tags that already have a
                                   // slash in the source,
            result.append('/'); // but ignores HTML 4.x style <br>, <img>, and
                                // similar tags without one
                                // The code below would fix that, but breaks
                                // backwards compatibility
                                // with previously translated HTML files
        }
        // if (tag.isEmptyXmlTag() || tag.getTagName().equals("BR") ||
        // tag.getTagName().equals("IMG"))
        // result.append('/');
        result.append('>');

        String shortcut = result.toString();
        sTags.add(tag);
        sTagNumbers.add(n);
        sShortcuts.add(shortcut);
        paragraph.append(shortcut);
    }

    /**
     * Recovers tag shortcuts into full tags.
     */
    private String unshorcutize(String str) {
        for (int i = 0; i < sShortcuts.size(); i++) {
            String shortcut = sShortcuts.get(i);
            int pos = -1;
            while ((pos = str.indexOf(shortcut, pos + 1)) >= 0) {
                Tag tag = sTags.get(i);
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
    private void queueTranslatable(Text txt) {
        if (!txt.toHtml().trim().isEmpty()) {
            translatable.addAll(afters);
            afters.clear();
            translatable.add(txt);
        } else {
            afters.add(txt);
        }
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
        if (text) {
            queueTranslatable(tag);
        } else if (isParagraphTag(tag)) {
            flushbefors();
            writeout("<" + tag.getText() + ">");
        } else {
            befors.add(tag);
        }
    }

    /**
     * Queues up some text, possibly before a meaningful text. If the text is
     * collected now, the tag is queued up as translatable by calling
     * {@link #queueTranslatable(Tag)}, otherwise it's collected to a special
     * list that is inspected when the translatable text is sent to OmegaT core.
     */
    private void queuePrefix(Text txt) {
        befors.add(txt);
    }

    /** Saves "Befors" to output stream and cleans the list. */
    private void flushbefors() {
        for (Node node : befors) {
            if (node instanceof Tag) {
                writeout("<" + node.getText() + ">");
            } else {
                writeout(compressWhitespace(node.getText()));
            }
        }
        befors.clear();
    }

    /**
     * Remove consecutive whitespace if
     * {@code options.getCompressWhitespace()==true}, and only space+tab is
     * removed. Newlines are not touched, to preserve the layout a little more.
     * <p>
     * NB: We cannot use {@code StaticUtils.compressSpaces}, because it trims a
     * string consisting of only whitespace to the empty string.
     *
     * @param input
     *            some text outside / between tags where it is allowed to
     *            compress spaces.
     * @return the compressed input.
     */
    private String compressWhitespace(String input) {
        if (options.getCompressWhitespace()) {
            Matcher whitespaceMatch = PatternConsts.SPACE_TAB.matcher(input);
            // keep at least 1 space, as not to change the meaning of the document.
            return whitespaceMatch.replaceAll(" ");
        } else {
            return input;
        }
    }
}
