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
import java.util.Arrays;
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
import org.omegat.util.HTMLUtils;
import org.omegat.util.Log;
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
            // To prevent a null pointer exception later, see
            // https://sourceforge.net/p/omegat/bugs/651/
            this.options = new HTMLOptions(new TreeMap<>());
        }
        this.writer = bufwriter;
    }

    // ///////////////////////////////////////////////////////////////////////
    // Variable declaration
    // ///////////////////////////////////////////////////////////////////////

    /** Should the parser call us for this tag's ending tag. */
    protected boolean recurseSelf = true;

    /**
     * Should the parser call us for this tag's inner tags.
     */
    protected boolean recurseChildren = true;

    /** Do we collect the translatable text now. */
    protected boolean isTextUpForCollection = false;

    /** Did the PRE block start (it means we mustn't compress the spaces). */
    protected boolean betweenPreformattingTags = false;

    /**
     * The list of non-paragraph tags before a chunk of text.
     * <ul>
     * <li>If a chunk of text follows, they get prepended to the translatable
     * paragraph, (starting from the first tag having a pair inside a chunk of
     * text)
     * <li>Otherwise they are written out directly.
     * </ul>
     */
    protected List<Node> precedingNodes;

    /** The list of nodes forming a chunk of text. */
    protected List<Node> translatableNodes;

    /**
     * The list of non-paragraph tags following a chunk of text.
     * <ul>
     * <li>If another chunk of text follows, they get appended to the
     * translatable paragraph,
     * <li>Otherwise (eg if a paragraph tag follows), they are written out directly.
     * </ul>
     */
    protected List<Node> followingNodes;

    /** The tags behind the shortcuts */
    protected List<Node> sTags;
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

        return recurseSelf;
    }

    /**
     * Depth traversal predicate.
     *
     * @return <code>true</code> if children are to be visited.
     */
    @Override
    public boolean shouldRecurseChildren() {
        return recurseChildren;
    }

    /**
     * Called for each <code>Tag</code> visited.
     *
     * @param tag
     *            The tag being visited.
     */
    @Override
    public void visitTag(Tag tag) {


        if (isProtectedTag(tag)) {
            if (isTextUpForCollection) {
                endup();
            } else {
                writeOutPrecedingNodes();
            }
            writeout(tag.toHtml());
            if (hasAnEndTag(tag)) {
                recurseSelf = true;
                recurseChildren = false;
            }
        } else {
            if (isParagraphTag(tag)) {
                handleParagraphTag();
            }
            if (isPreformattingTag(tag)) {
                betweenPreformattingTags = true;
            }
            // Translate attributes of tags if they are not null.
            maybeTranslateAttribute(tag, "abbr");
            maybeTranslateAttribute(tag, "alt");
            maybeTranslateAttribute(tag, "dir");
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
     * Returns true if the tag is the opening tag of a composite tag pair.
     */
    private boolean hasAnEndTag(Tag tag) {
        return tag.getEndTag() != null;
    }

    private void handleParagraphTag() {
        recurseChildren = true;
        if (isTextUpForCollection) {
            endup();
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
        recurseSelf = true;
        recurseChildren = true;
        // nbsp is special case - process it like usual spaces
        String textAsCleanedString = HTMLUtils.entitiesToChars(string.getText()).replace((char) 160, ' ');
        if (hasMoreThanJustWhitepaces(textAsCleanedString)) {
            // Hack around HTMLParser not being able to handle XHTML
            // RFE:
            // http://sourceforge.net/tracker/index.php?func=detail&aid=1227222&group_id=24399&atid=381402
            if (firstcall && PatternConsts.XML_HEADER.matcher(textAsCleanedString.trim()).matches()) {
                writeout(string.toHtml());
                return;
            }

            isTextUpForCollection = true;
            firstcall = false;
        } else if (betweenPreformattingTags) {
            isTextUpForCollection = true;
        }

        if (isTextUpForCollection) {
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
        if (shouldKeepComments()) {
            recurseSelf = true;
            recurseChildren = true;
            if (betweenPreformattingTags) {
                isTextUpForCollection = true;
            }

            if (isTextUpForCollection) {
                queueTranslatable(remark);
            } else {
                queuePrefix(remark);
            }
        }
    }

    private boolean shouldKeepComments() {
        return !options.getRemoveComments();
    }

    /**
     * Called for each end <code>Tag</code> visited.
     *
     * @param tag
     *            The end tag being visited.
     */
    @Override
    public void visitEndTag(Tag tag) {
        recurseSelf = true;
        recurseChildren = true;
        if (isParagraphTag(tag) && isTextUpForCollection) {
            endup();
        }
        if (isPreformattingTag(tag)) {
            betweenPreformattingTags = false;
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
        if (isTextUpForCollection) {
            endup();
        } else {
            writeOutPrecedingNodes();
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

        String[] blockElementTags = { "ADDRESS", "ARTICLE", "ASIDE", "BLOCKQUOTE", "BODY", "CANVAS", "CENTER",
                "DD", "DIV", "DL", "DT", "FIELDSET", "FIGCAPTION", "FIGURE", "FOOTER", "FORM", "H1", "H2",
                "H3", "H4", "H5", "H6", "HEADER", "HR", "LABEL", "LEGEND", "LI", "MAIN", "NAV", "NOSCRIPT",
                "OL", "OPTION", "P", "PRE", "SECTION", "SELECT", "TABLE", "TD", "TEXTAREA", "TFOOT", "TH",
                "TITLE", "TR", "UL", "VIDEO" };
        String[] parentElementTags = { "HEAD", "HTML" };

        return (tagname.equals("BR") && options.getParagraphOnBr())
                || Arrays.stream(parentElementTags).anyMatch(tagname::equals)
                || Arrays.stream(blockElementTags).anyMatch(tagname::equals);
    }

    /** Should the content of this tag be kept intact? */
    private boolean isProtectedTag(Tag tag) {
        String tagname = tag.getTagName();

        String[] noEditTags = { "!DOCTYPE", "STYLE", "SCRIPT", "OBJECT", "EMBED" };
        boolean keepIntact = Arrays.stream(noEditTags).anyMatch(tagname::equals) || (tagname.equals("META")
                && "content-type".equalsIgnoreCase(tag.getAttribute("http-equiv")));

        if (!keepIntact) {
            keepIntact = hasSpecialAttributes(tag);
        }
        return keepIntact;
    }

    private boolean hasSpecialAttributes(Tag tag) {
        boolean attributeIsOnIgnoreTagsList = false;
        Vector<?> tagAttributes = tag.getAttributesEx();
        Iterator<?> i = tagAttributes.iterator();

        while (i.hasNext() && !attributeIsOnIgnoreTagsList) {
            Attribute attribute = (Attribute) i.next();
            String name = attribute.getName();
            String value = attribute.getValue();
            if (name != null && value != null) {
                attributeIsOnIgnoreTagsList = this.filter.checkIgnoreTags(name, value);
            }
        }
        return attributeIsOnIgnoreTagsList;
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
            Log.log(ioe);
        }
    }

    /**
     * Ends the segment collection and sends the translatable text out to OmegaT
     * core, and some extra tags to writer.
     */
    protected void endup() {
        // detecting the first starting tag in 'precedingNodes'
        // that has its ending in the paragraph
        // all before this "first good" are simply written out

        // SETUP
        List<Node> allNodesInParagraph = new ArrayList<>();
        allNodesInParagraph.addAll(precedingNodes);
        allNodesInParagraph.addAll(translatableNodes);
        allNodesInParagraph.addAll(followingNodes);
        int lastPrecedingNodePosition = precedingNodes.size() - 1;
        int lastTranslatableNodePosition = lastPrecedingNodePosition + translatableNodes.size();
        int lastFollowingPosition = allNodesInParagraph.size() - 1;

        // DETERMINE FIRST TAG IN PRECEDING TO INCLUDE
        int firstTagToIncludeFromPreceding;
        for (firstTagToIncludeFromPreceding = 0; firstTagToIncludeFromPreceding <= lastPrecedingNodePosition; firstTagToIncludeFromPreceding++) {
            Node startNode = allNodesInParagraph.get(firstTagToIncludeFromPreceding);
            if (startNode instanceof Tag) {
                Tag openingTag = (Tag) startNode;
                int recursion = 1;
                boolean found = false;
                for (int i = firstTagToIncludeFromPreceding + 1; i <= lastTranslatableNodePosition; i++) {
                    Node candidateNode = allNodesInParagraph.get(i);
                    if (candidateNode instanceof Tag) {
                        Tag candidateTag = (Tag) candidateNode;
                        if (candidateTag.getTagName().equals(openingTag.getTagName())) {
                            if (candidateTag.isEndTag()) {
                                recursion--;
                                if (recursion == 0) {
                                    if (i > lastPrecedingNodePosition) {
                                        found = true;
                                    }
                                    break;
                                }
                            } else {
                                recursion++;
                            }
                        }
                    }
                }
                if (found) {
                    break;
                }
            }
        }

        // detecting the last ending tag in 'followingNodes'
        // that has its opening in the paragraph
        // all after this "last good" is simply writen out

        // DETERMINE LAST TAG IN FOLLOWING TO INCLUDE
        int lastTagKeptInFollowing;
        for (lastTagKeptInFollowing = lastFollowingPosition; lastTagKeptInFollowing > lastTranslatableNodePosition; lastTagKeptInFollowing--) {
            Node endNode = allNodesInParagraph.get(lastTagKeptInFollowing);
            if (endNode instanceof Tag) {
                Tag closingTag = (Tag) endNode;

                int recursion = 1;
                boolean found = false;
                for (int i = lastTagKeptInFollowing - 1; i > lastPrecedingNodePosition; i--) {
                    Node candidateNode = allNodesInParagraph.get(i);
                    if (candidateNode instanceof Tag) {
                        Tag candidateTag = (Tag) candidateNode;
                        if (candidateTag.getTagName().equals(closingTag.getTagName())) {
                            if (candidateTag.isEndTag()) {
                                recursion++;
                            } else {
                                recursion--;
                                if (recursion == 0) {
                                    if (i <= lastTranslatableNodePosition) {
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
            }
        }

        // SET POSITION OF FIRST AND LAST TAGS INCLUDED
        boolean changed = true;
        while (changed) {
            changed = false;
            boolean removeTags = Core.getFilterMaster().getConfig().isRemoveTags();
            if (!removeTags) {
                for (int i = 0; i < firstTagToIncludeFromPreceding; i++) {
                    Node node = allNodesInParagraph.get(i);
                    if (node instanceof Tag) {
                        firstTagToIncludeFromPreceding = i;
                        changed = true;
                        break;
                    }
                }
                for (int i = lastFollowingPosition; i > lastTagKeptInFollowing; i--) {
                    Node node = allNodesInParagraph.get(i);
                    if (node instanceof Tag) {
                        lastTagKeptInFollowing = i;
                        changed = true;
                        break;
                    }
                }
            }

            boolean removeSpacesAround = Core.getFilterMaster().getConfig().isRemoveSpacesNonseg();
            if (!removeSpacesAround) {
                for (int i = 0; i < firstTagToIncludeFromPreceding; i++) {
                    Node node = allNodesInParagraph.get(i);
                    if (node instanceof TextNode) {
                        firstTagToIncludeFromPreceding = i;
                        changed = true;
                        break;
                    }
                }
                for (int i = allNodesInParagraph.size() - 1; i > lastTagKeptInFollowing; i--) {
                    Node node = allNodesInParagraph.get(i);
                    if (node instanceof TextNode) {
                        lastTagKeptInFollowing = i;
                        changed = true;
                        break;
                    }
                }
            }
        }

        // WRITING OUT ALL TAGS BEFORE THE FIRST ONE INCLUDED
        for (int i = 0; i < firstTagToIncludeFromPreceding; i++) {
            Node node = allNodesInParagraph.get(i);
            if (node instanceof Tag) {
                writeout("<" + node.getText() + ">");
            } else if (node instanceof Remark) {
                writeout(node.toHtml());
            } else {
                writeout(compressWhitespace(node.getText()));
            }
        }

        // APPENDING TO PARAGRAPH TEXT ALL TAGS UNTIL THE LAST INCLUDED ONE
        StringBuilder paragraph = new StringBuilder();
        // appending all tags starting from "first good" one to paragraph text
        for (int i = firstTagToIncludeFromPreceding; i <= lastTagKeptInFollowing; i++) {
            Node node = allNodesInParagraph.get(i);
            if (node instanceof Tag) {
                assignShortcut((Tag) node, paragraph);
            } else if (node instanceof Remark) {
                assignShortcut((Remark) node, paragraph);
            } else { // node instanceof Text
                paragraph.append(HTMLUtils.entitiesToChars(node.toHtml()));
            }
        }

        // COMPRESS SPACES IF NEEDED
        String uncompressed = paragraph.toString();
        String compressed = uncompressed;
        String spacePrefix = "";
        String spacePostfix = "";

        // We're compressing the space if this paragraph wasn't inside <PRE> tag
        // But if the translator does not translate the paragraph,
        // then we write out the uncompressed version,
        // as documented in
        // https://sourceforge.net/p/omegat/bugs/108/
        // The spaces that are around the segment are not removed, unless
        // compressWhitespace option is enabled. Then the spaces are compressed to max 1.
        // (This changes the layout, therefore it is an option. NB: an alternative implementation is to compress by
        // default, and use Core.getFilterMaster().getConfig().isPreserveSpaces() option instead to compress if
        // not checked.)
        if (!betweenPreformattingTags) {

            spacePrefix = HTMLUtils.getSpacePrefix(uncompressed, options.getCompressWhitespace());
            spacePostfix = HTMLUtils.getSpacePostfix(uncompressed, options.getCompressWhitespace());

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
            //uncompressed contains pre/postfix whitespace, so do not add that extra!
            spacePrefix = "";
            spacePostfix = "";
        }

        // converting & < and > into &amp; &lt; and &gt; respectively
        // note that this doesn't change < and > of tag shortcuts
        translation = HTMLUtils.charsToEntities(translation, filter.getTargetEncoding(), sShortcuts);
        // expands tag shortcuts into full-blown tags
        translation = revertShortcut(translation);
        // writing out the paragraph into target file
        writeout(spacePrefix);
        writeout(translation);
        writeout(spacePostfix);

        // writing out all tags after the "last good" one
        for (int i = lastTagKeptInFollowing + 1; i < allNodesInParagraph.size(); i++) {
            Node node = allNodesInParagraph.get(i);
            if (node instanceof Tag) {
                writeout("<" + node.getText() + ">");
            } else if (node instanceof Remark) {
                writeout(node.toHtml());
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
        isTextUpForCollection = false;
        recurseSelf = true;
        recurseChildren = true;
        precedingNodes = new ArrayList<>();
        translatableNodes = new ArrayList<>();
        followingNodes = new ArrayList<>();
        sTags = new ArrayList<>();
        sTagNumbers = new ArrayList<>();
        sShortcuts = new ArrayList<>();
        sNumShortcuts = 0;
    }

    /**
     * Creates and stores a shortcut for the tag.
     */
    private void assignShortcut(Tag tag, StringBuilder paragraph) {
        StringBuilder result = new StringBuilder();
        result.append('<');
        int n = -1;
        if (tag.isEndTag()) {
            result.append('/');
            // trying to lookup for appropriate starting tag
            int recursion = 1;
            for (int i = sTags.size() - 1; i >= 0; i--) {
                if (sTags.get(i) instanceof Tag) {
                    Tag othertag = (Tag) sTags.get(i);
                    if (othertag.getTagName().equals(tag.getTagName())) {
                        if (othertag.isEndTag()) {
                            recursion++;
                        } else {
                            recursion--;
                            if (recursion == 0) {
                                // found starting tag for this endTag
                                n = sTagNumbers.get(i);
                                break;
                            }
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
     * Creates and stores a shortcut for the comment (Remark node).
     */
    private void assignShortcut(Remark remark, StringBuilder paragraph) {
        StringBuilder result = new StringBuilder();
        int n = sNumShortcuts++;
        result.append("<c");
        result.append(n);
        result.append("/>");
        String shortcut = result.toString();
        sTags.add(remark);
        sTagNumbers.add(n);
        sShortcuts.add(shortcut);
        paragraph.append(shortcut);
    }

    /**
     * Recovers tag shortcuts into full tags.
     */
    private String revertShortcut(String str) {
        for (int i = 0; i < sShortcuts.size(); i++) {
            String shortcut = sShortcuts.get(i);
            int pos = -1;
            while ((pos = str.indexOf(shortcut, pos + 1)) >= 0) {
                if (sTags.get(i) instanceof Tag) {
                    Tag tag = (Tag) sTags.get(i);
                    try {
                        str = str.substring(0, pos) + "<" + tag.getText() + ">"
                                + str.substring(pos + shortcut.length());
                    } catch (StringIndexOutOfBoundsException sioobe) {
                        // nothing, string doesn't change
                        // but prevent endless loop
                        break;
                    }
                } else if (sTags.get(i) instanceof Remark) {
                     Remark comment = (Remark) sTags.get(i);
                     try {
                         str = str.substring(0, pos) + comment.toHtml()
                                 + str.substring(pos + shortcut.length());
                     } catch (StringIndexOutOfBoundsException sioobe) {
                         // nothing, string doesn't change
                         // but prevent endless loop
                         break;
                     }
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
        if (hasMoreThanJustWhitepaces(txt.toHtml()) || betweenPreformattingTags) {
            translatableNodes.addAll(followingNodes);
            followingNodes.clear();
            translatableNodes.add(txt);
        } else {
            followingNodes.add(txt);
        }
    }

    private boolean hasMoreThanJustWhitepaces(String string) {
        return !string.trim().isEmpty();
    }

    private void queueTranslatable(Remark remark) {
        if (betweenPreformattingTags) {
            translatableNodes.addAll(followingNodes);
            followingNodes.clear();
            translatableNodes.add(remark);
        } else {
            followingNodes.add(remark);
        }
    }

    /**
     * Queues the tag to the translatable paragraph.
     * <p>
     * Note that the tag is simply added to the queue, and will be appended to
     * the translatable text only if some meaningful text follows it.
     */
    private void queueTranslatable(Tag tag) {
        followingNodes.add(tag);
    }

    /**
     * Queues up something, possibly before a text. If the text is collected
     * now, the tag is queued up as translatable by calling
     * {@link #queueTranslatable(Tag)}, otherwise it's collected to a special
     * list that is inspected when the translatable text is sent to OmegaT core.
     */
    protected void queuePrefix(Tag tag) {
        if (isTextUpForCollection) {
            queueTranslatable(tag);
        } else if (isParagraphTag(tag)) {
            writeOutPrecedingNodes();
            writeout("<" + tag.getText() + ">");
        } else {
            precedingNodes.add(tag);
        }
    }

    /**
     * Queues up some Text node, possibly before more meaningful text.
     * The Text node is added to the precedingNodes list.
     */
    private void queuePrefix(Text txt) {
        precedingNodes.add(txt);
    }

    /**
     * Queues up some Remark node (HTML comment), possibly before more meaningful
     * text. The Remark node is added to the precedingNodes list.
     */
    private void queuePrefix(Remark remark) {
        precedingNodes.add(remark);
    }

    /** Saves "precedingNodes" to output stream and cleans the list. */
    private void writeOutPrecedingNodes() {
        for (Node node : precedingNodes) {
            if (node instanceof Tag) {
                writeout("<" + node.getText() + ">");
            } else if (node instanceof Remark) {
                writeout(node.toHtml());
            } else {
                writeout(compressWhitespace(node.getText()));
            }
        }
        precedingNodes.clear();
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
