/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2016 Aaron Madlon-Kay
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.segmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.util.Language;
import org.omegat.util.PatternConsts;

/**
 * The class that sentences the paragraphs into sentences and glues translated
 * sentences together to form a paragraph.
 *
 * @author Maxym Mykhalchuk
 */
public final class Segmenter {

    private final SRX srx;

    public Segmenter(SRX srx) {
        this.srx = srx;
    }

    public SRX getSRX() {
        return srx;
    }

    /**
     * Segments the paragraph to sentences according to currently setup rules.
     * <p>
     * Bugfix for <a href="https://sourceforge.net/p/omegat/bugs/83/">bug 83</a>
     * : Sentences are returned without spaces in the beginning and at the end
     * of a sentence.
     * <p>
     * An additional list with space information is returned to be able to glue
     * translation together with the same spaces between them as in original
     * paragraph.
     *
     * @param paragraph
     *            the paragraph text
     * @param spaces
     *            list to store information about spaces between sentences (can be null)
     * @param brules
     *            list to store rules that account to breaks (can be null)
     * @return list of sentences (String objects)
     */
    public List<String> segment(Language lang, String paragraph, List<StringBuilder> spaces,
            List<Rule> brules) {
        if (paragraph == null) {
            return null;
        }
        List<String> segments = breakParagraph(lang, paragraph, brules);
        List<String> sentences = new ArrayList<String>(segments.size());
        if (spaces != null) {
            spaces.clear();
        }
        for (String one : segments) {
            int len = one.length();
            int b = 0;
            StringBuilder bs = new StringBuilder();
            for (int cp; b < len; b += Character.charCount(cp)) {
                cp = one.codePointAt(b);
                if (!Character.isWhitespace(cp)) {
                    break;
                }
                bs.appendCodePoint(cp);
            }

            int e = len;
            StringBuilder es = new StringBuilder();
            for (int cp; e > b; e -= Character.charCount(cp)) {
                cp = one.codePointBefore(e);
                if (!Character.isWhitespace(cp)) {
                    break;
                }
                es.appendCodePoint(cp);
            }
            es.reverse();

            String trimmed = one.substring(b, e);
            sentences.add(trimmed);
            if (spaces != null) {
                spaces.add(bs);
                spaces.add(es);
            }
        }
        return sentences;
    }

    /**
     * Returns pre-sentences (sentences with spaces between), computed by breaking paragraph into chunks of
     * text. Also returns the list with "the reasons" why the breaks were made, i.e. the list of break rules
     * that contributed to each of the breaks made.
     * <p>
     * If glued back together, these strings form the same paragraph text as this function was fed.
     *
     * @param paragraph
     *            the paragraph text
     * @param brules
     *            list to store rules that account to breaks (can be null)
     */
    private List<String> breakParagraph(Language lang, String paragraph, List<Rule> brules) {
        List<Rule> rules = srx.lookupRulesForLanguage(lang);

        // determining the applicable break positions
        Set<BreakPosition> dontbreakpositions = new TreeSet<BreakPosition>();
        Set<BreakPosition> breakpositions = new TreeSet<BreakPosition>();
        for (int i = rules.size() - 1; i >= 0; i--) {
            Rule rule = rules.get(i);
            List<BreakPosition> rulebreaks = getBreaks(paragraph, rule);
            if (rule.isBreakRule()) {
                breakpositions.addAll(rulebreaks);
                dontbreakpositions.removeAll(rulebreaks);
            } else {
                dontbreakpositions.addAll(rulebreaks);
                breakpositions.removeAll(rulebreaks);
            }
        }
        breakpositions.removeAll(dontbreakpositions);

        // and now breaking the string according to the positions
        List<String> segments = new ArrayList<String>();
        if (brules != null) {
            brules.clear();
        }
        int prevpos = 0;
        for (BreakPosition bposition : breakpositions) {
            String oneseg = paragraph.substring(prevpos, bposition.position);
            segments.add(oneseg);
            if (brules != null) {
                brules.add(bposition.reason);
            }
            prevpos = bposition.position;
        }
        try {
            String oneseg = paragraph.substring(prevpos);

            // Sometimes the last segment may be empty,
            // it happens for paragraphs like "Rains. "
            // So if it's an empty segment and there's a previous segment
            if (oneseg.trim().isEmpty() && !segments.isEmpty()) {
                String prev = segments.get(segments.size() - 1);
                prev += oneseg;
                segments.set(segments.size() - 1, prev);
            } else {
                segments.add(oneseg);
            }
        } catch (IndexOutOfBoundsException iobe) {
        }

        return segments;
    }

    private static final Pattern DEFAULT_BEFOREBREAK_PATTERN = Pattern.compile(".", Pattern.DOTALL);

    /**
     * Returns the places of possible breaks between sentences.
     */
    private static List<BreakPosition> getBreaks(String paragraph, Rule rule) {
        List<BreakPosition> res = new ArrayList<BreakPosition>();

        Matcher bbm = null;
        if (rule.getBeforebreak() != null) {
            bbm = rule.getCompiledBeforebreak().matcher(paragraph);
        }
        Matcher abm = null;
        if (rule.getAfterbreak() != null) {
            abm = rule.getCompiledAfterbreak().matcher(paragraph);
        }
        if (bbm == null && abm == null) {
            return res;
        }
        if (abm != null) {
            if (!abm.find()) {
                return res;
            }
        }

        if (bbm == null) {
            bbm = DEFAULT_BEFOREBREAK_PATTERN.matcher(paragraph);
        }
        while (bbm.find()) {
            int bbe = bbm.end();
            if (abm == null) {
                res.add(new BreakPosition(bbe, rule));
            } else {
                int abs = abm.start();
                while (abs < bbe) {
                    boolean found = abm.find();
                    if (!found) {
                        return res;
                    }
                    abs = abm.start();
                }
                if (abs == bbe) {
                    res.add(new BreakPosition(bbe, rule));
                }
            }
        }

        return res;
    }

    /** A class for a break position that knows which rule contributed to it. */
    static class BreakPosition implements Comparable<BreakPosition> {
        /** Break/Exception position. */
        int position;
        /** Rule that contributed to the break. */
        Rule reason;

        /** Creates a new break position. */
        BreakPosition(int position, Rule reason) {
            this.position = position;
            this.reason = reason;
        }

        /**
         * Other BreakPosition is "equal to" this one iff it has the same position.
         */
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof BreakPosition)) {
                return false;
            }
            BreakPosition that = (BreakPosition) obj;

            return this.position == that.position;
        }

        /** Returns a hash code == position for the object. */
        public int hashCode() {
            return this.position;
        }

        /**
         * Compares this break position with another.
         *
         * @return a negative integer if its position is less than the another's, zero if they are equal, or a
         *         positive integer as its position is greater than the another's.
         * @throws ClassCastException
         *             if the specified object's type prevents it from being compared to this Object.
         */
        public int compareTo(BreakPosition that) {
            return this.position - that.position;
        }
    }

    /**
     * Glues segments back into a paragraph.
     * <p>
     * As segments are returned by
     * {@link #segment(Language, String, List, List)} without spaces before and
     * after them, this method adds spaces if needed:
     * <ul>
     * <li>For translation <i>to</i> non-space-delimited languages (Japanese,
     * Chinese, Tibetan) it does <b>not</b> add any spaces.
     * <p>
     * A special exceptions are the Break SRX rules that break on space, i.e.
     * before and after patterns consist of spaces (they get trimmed to an empty
     * string). For such rules all the spaces are added.
     * <li>For translation <i>from</i> non-space-delimited languages it adds one
     * space.
     * <li>For all other language combinations it restores the spaces present
     * before segmenting.
     * </ul>
     *
     * @param sentences
     *            list of translated sentences
     * @param spaces
     *            information about spaces in original paragraph
     * @param brules
     *            rules that account to breaks
     * @return glued translated paragraph
     */
    public String glue(Language sourceLang, Language targetLang, List<String> sentences,
            List<StringBuilder> spaces, List<Rule> brules) {
        if (sentences.size() <= 0) {
            return "";
        }
        StringBuilder res = new StringBuilder();
        res.append(sentences.get(0));

        for (int i = 1; i < sentences.size(); i++) {
            StringBuilder sp = new StringBuilder();
            sp.append(spaces.get(2 * i - 1));
            sp.append(spaces.get(2 * i));

            if (!targetLang.isSpaceDelimited()) {
                Rule rule = brules.get(i - 1);
                if (res.length() > 0) {
                    char lastChar = res.charAt(res.length() - 1);
                    Matcher matcher = LINE_BREAK_OR_TAB_PATTERN.matcher(sp.toString());
                    if (matcher.find()) {
                        // If we found line break or tab, trim left spaces.
                        // Right spaces are left for indentation of the next line.
                        String leftSpaces = matcher.group(1);
                        if (!leftSpaces.isEmpty()) {
                            sp.replace(0, leftSpaces.length(), "");
                        }
                    } else if ((lastChar != '.')
                            && (!PatternConsts.SPACY_REGEX.matcher(rule.getBeforebreak()).matches()
                            || !PatternConsts.SPACY_REGEX.matcher(rule.getAfterbreak()).matches())) {
                        sp.setLength(0);
                    }
                }
            } else if (!sourceLang.isSpaceDelimited() && sp.length() == 0) {
                sp.append(" ");
            }
            res.append(sp);
            res.append(sentences.get(i));
        }
        return res.toString();
    }

    /**
     * Segment source and target entries from TMX when counts are equals.
     */
    public void segmentEntries(boolean needResegment, Language sourceLang, String sourceEntry,
            Language targetLang, String targetEntry, List<String> sourceSegments, List<String> targetSegments) {
        if (needResegment) {
            List<String> srcSegments = segment(sourceLang, sourceEntry, null, null);
            if (targetEntry != null) { // There is no translation for this entry, because for instance it's a note
                                       // on an untranslated entry
                List<String> tarSegments = segment(targetLang, targetEntry, null, null);

                if (srcSegments.size() == tarSegments.size()) {
                    sourceSegments.addAll(srcSegments);
                    targetSegments.addAll(tarSegments);
                    return;
                }
            }
        }
        // No need to resegment, or segments counts not equals, or no translation
        sourceSegments.add(sourceEntry);
        targetSegments.add(targetEntry);

    }

    /** For non-space-delimited languages. */
    private static final Pattern LINE_BREAK_OR_TAB_PATTERN = Pattern.compile("^( *)[\\r\\n\\t]");
}
