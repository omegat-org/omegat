/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2012 Didier Briel
               2015 Aaron Madlon-Kay
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

package org.omegat.gui.editor;

import java.util.List;
import java.util.Locale;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

import org.omegat.core.Core;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.IEditor.CHANGE_CASE_TO;
import org.omegat.gui.glossary.GlossaryEntry;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.StringUtil;
import org.omegat.util.TagUtil;
import org.omegat.util.TagUtil.Tag;
import org.omegat.util.Token;

/**
 * Some utilities methods.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 */
public final class EditorUtils {

    private EditorUtils() {
    }

    /**
     * Determines the start of a word for the given model location. This method
     * skips direction char.
     *
     * TODO: change to use document's locale
     *
     * @param c
     * @param offs
     * @return
     * @throws BadLocationException
     */
    public static int getWordStart(JTextComponent c, int offs) throws BadLocationException {
        int result = Utilities.getWordStart(c, offs);
        char ch = c.getDocument().getText(result, 1).charAt(0);
        if (isDirectionChar(ch)) {
            result++;
        }
        return result;
    }

    /**
     * Determines the end of a word for the given model location. This method
     * skips direction char.
     *
     * TODO: change to use document's locale
     *
     * @param c
     * @param offs
     * @return
     * @throws BadLocationException
     */
    public static int getWordEnd(JTextComponent c, int offs) throws BadLocationException {
        int result = Utilities.getWordEnd(c, offs);
        if (result > 0) {
            char ch = c.getDocument().getText(result - 1, 1).charAt(0);
            if (isDirectionChar(ch)) {
                result--;
            }
        }
        return result;
    }

    /**
     * Check if char is direction char(u202A,u202B,u202C).
     *
     * @param ch
     *            char to check
     * @return true if it's direction char
     */
    private static boolean isDirectionChar(final char ch) {
        return ch == '\u202A' || ch == '\u202B' || ch == '\u202C' || ch == '\u200E' || ch == '\u200F';
    }

    /**
     * Remove invisible direction chars from string.
     *
     * @param text
     *            string with direction chars
     * @return string without direction chars
     */
    public static String removeDirectionChars(String text) {
        return text.replaceAll("[\u202A\u202B\u202C\u200E\u200F]", "");
    }

    /**
     * Remove bidi chars around tags only.
     *
     * @param text
     *            string with direction chars
     * @return string without direction chars
     */
    public static String removeDirectionCharsAroundTags(String text, SourceTextEntry ste) {
        for (ProtectedPart pp : ste.getProtectedParts()) {
            int pos = -1;
            while ((pos = text.indexOf(pp.getTextInSourceSegment(), pos + 1)) >= 0) {
                if (hasBidiAroundTag(text, pp.getTextInSourceSegment(), pos)) {
                    // remove bidi chars around
                    text = text.substring(0, pos - 2) + pp.getTextInSourceSegment()
                            + text.substring(pos + pp.getTextInSourceSegment().length() + 2);
                }
            }
        }
        return text;
    }

    /**
     * Change the case of the input string to the indicated case. When toWhat is
     * {@link CHANGE_CASE_TO#CYCLE} the result will be UPPER &gt; LOWER &gt;
     * SENTENCE &gt; TITLE &gt; UPPER.
     * <p>
     * This is a convenience method for
     * {@link #doChangeCase(String, CHANGE_CASE_TO, Locale, ITokenizer)}. The
     * locale and tokenizer will be taken from the current project's target
     * language values.
     *
     * @param input
     *            The string to change
     * @param toWhat
     *            The case to change to, or {@link CHANGE_CASE_TO#CYCLE}
     * @return The modified string
     */
    public static String doChangeCase(String input, CHANGE_CASE_TO toWhat) {
        Locale locale = Core.getProject().getProjectProperties().getTargetLanguage().getLocale();
        ITokenizer tokenizer = Core.getProject().getTargetTokenizer();
        return doChangeCase(input, toWhat, locale, tokenizer);
    }

    /**
     * Change the case of the input string to the indicated case. When toWhat is
     * {@link CHANGE_CASE_TO#CYCLE} the result will be UPPER &gt; LOWER &gt;
     * SENTENCE &gt; TITLE &gt; UPPER.
     *
     * @param input
     *            The string to change
     * @param toWhat
     *            The case to change to, or {@link CHANGE_CASE_TO#CYCLE}
     * @param locale
     *            The locale of the input string
     * @param tokenizer
     *            A tokenizer for the input string language
     * @return The modified string
     */
    public static String doChangeCase(String input, CHANGE_CASE_TO toWhat, Locale locale,
            ITokenizer tokenizer) {
        // tokenize the selection
        Token[] tokenList = tokenizer.tokenizeVerbatim(input);

        if (toWhat == CHANGE_CASE_TO.CYCLE) {
            int lower = 0;
            int upper = 0;
            int title = 0;
            int ambiguous = 0; // Maybe title, maybe upper
            int mixed = 0;

            for (Token token : tokenList) {
                String word = token.getTextFromString(input);
                if (!canChangeTokenCase(word)) {
                    continue;
                }
                if (StringUtil.isLowerCase(word)) {
                    lower++;
                    continue;
                }
                boolean isTitle = StringUtil.isTitleCase(word);
                boolean isUpper = StringUtil.isUpperCase(word);
                if (isTitle && isUpper) {
                    ambiguous++;
                    continue;
                }
                if (isTitle) {
                    title++;
                    continue;
                }
                if (isUpper) {
                    upper++;
                    continue;
                }
                if (StringUtil.isMixedCase(word)) {
                    mixed++;
                }
                // Ignore other tokens as they should be caseless text
                // such as CJK ideographs or symbols only.
            }

            if (lower == 0 && title == 0 && upper == 0 && mixed == 0 && ambiguous == 0) {
                return input; // nothing to do here
            }

            toWhat = determineTargetCase(lower, upper, title, mixed, ambiguous);
        }

        StringBuilder buffer = new StringBuilder(input);
        int lengthIncrement = 0;

        for (Token token : tokenList) {
            // find out the case and change to the selected
            String tokText = token.getTextFromString(input);
            if (!canChangeTokenCase(tokText)) {
                continue;
            }
            String result;
            if (toWhat == CHANGE_CASE_TO.LOWER) {
                result = tokText.toLowerCase(locale);
            } else if (toWhat == CHANGE_CASE_TO.UPPER) {
                result = tokText.toUpperCase(locale);
            } else if (toWhat == CHANGE_CASE_TO.TITLE) {
                result = StringUtil.toTitleCase(tokText, locale);
            } else if (toWhat == CHANGE_CASE_TO.SENTENCE) {
                result = StringUtil.toTitleCase(tokText, locale);
                toWhat = CHANGE_CASE_TO.LOWER;
            } else {
                result = tokText;
            }

            // replace this token
            buffer.replace(token.getOffset() + lengthIncrement,
                    token.getLength() + token.getOffset() + lengthIncrement, result);

            lengthIncrement += result.length() - token.getLength();
        }

        return buffer.toString();
    }

    /**
     * Determine whether or not the provided token should be considered when
     * changing the case of a larger string. We don't want to consider, e.g.,
     * OmegaT tags because changing their case can break them.
     *
     * @param token
     * @return Whether or not to change case
     */
    private static boolean canChangeTokenCase(String token) {
        return Character.isLetter(token.codePointAt(0));
    }

    private static CHANGE_CASE_TO determineTargetCase(int lower, int upper, int title, int mixed,
            int ambiguous) {
        int presentCaseTypes = 0;
        if (lower > 0) {
            presentCaseTypes++;
        }
        if (upper > 0) {
            presentCaseTypes++;
        }
        if (title > 0) {
            presentCaseTypes++;
        }
        if (mixed > 0) {
            presentCaseTypes++;
        }

        if ((title > 0 || ambiguous > 0) && lower > 0 && upper == 0 && mixed == 0) {
            return CHANGE_CASE_TO.TITLE;
        }

        if (mixed > 0 || presentCaseTypes > 1) {
            return CHANGE_CASE_TO.UPPER;
        }

        if (lower > 0) {
            return CHANGE_CASE_TO.SENTENCE;
        }

        if (title > 0) {
            return CHANGE_CASE_TO.UPPER;
        }

        if (upper > 0) {
            return CHANGE_CASE_TO.LOWER;
        }

        if (ambiguous > 0) {
            // If we only have ambiguous tokens then we must go to lower so that
            // we
            // get binary upper/lower switching instead of trinary
            // upper/lower/title.
            return CHANGE_CASE_TO.LOWER;
        }

        // This should only happen if no cases are present, so it doesn't even
        // matter.
        return CHANGE_CASE_TO.UPPER;
    }

    /**
     * Convenience method for
     * {@link #replaceGlossaryEntries(String, List, Locale, ITokenizer)}.
     * Glossary entries are retrieved from {@code GlossaryManager}; the locale
     * and tokenizer are taken from the project's current values for the source
     * language.
     *
     * @param text
     *            Text in which to replace glossary hits. Assumed to be in the
     *            project's source language.
     * @return Text with source glossary terms replaced with target terms
     */
    public static String replaceGlossaryEntries(String text) {
        Locale locale = Core.getProject().getProjectProperties().getSourceLanguage().getLocale();
        ITokenizer tokenizer = Core.getProject().getSourceTokenizer();
        return replaceGlossaryEntries(text, Core.getGlossaryManager().getGlossaryEntries(text), locale,
                tokenizer);
    }

    /**
     * Given a list of glossary entries, replace any instances of the source
     * term appearing in the given text with the target term. When there are
     * multiple target terms, the first one is used.
     *
     * @param text
     *            Text in which to replace glossary hits (assumed to be in the
     *            project's source language)
     * @param entries
     *            List of glossary entries
     * @param locale
     *            Locale with which to perform capitalization matching (assumed
     *            to be source locale)
     * @param tokenizer
     *            Tokenizer with which to a split text (assumed to be project's
     *            source tokenizer)
     * @return Text with source glossary terms replaced with target terms
     */
    public static String replaceGlossaryEntries(String text, List<GlossaryEntry> entries, Locale locale,
            ITokenizer tokenizer) {
        if (StringUtil.isEmpty(text) || entries == null || entries.isEmpty()) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        String[] haystack = tokenizer.tokenizeVerbatimToStrings(text);
        for (int i = 0; i < haystack.length; i++) {
            String tok = haystack[i];
            boolean replaced = false;
            for (GlossaryEntry e : entries) {
                String[] needle = tokenizer.tokenizeVerbatimToStrings(e.getSrcText());
                if (tokensPresentAt(needle, haystack, i, true)) {
                    String toAppend = e.getLocText();
                    if (!tokensPresentAt(needle, haystack, i, false)) {
                        // If the source tokens don't have matching case, fix
                        // the replacement's case to match
                        toAppend = StringUtil.matchCapitalization(toAppend, tok, locale);
                    }
                    sb.append(toAppend);
                    replaced = true;
                    i += needle.length - 1;
                    break;
                }
            }
            if (!replaced) {
                sb.append(tok);
            }
        }
        return sb.toString();
    }

    private static boolean tokensPresentAt(String[] needle, String[] haystack, int offset,
            boolean ignoreCase) {
        if (offset < 0 || offset + needle.length > haystack.length) {
            return false;
        }
        for (int i = 0; i < needle.length; i++) {
            String hayToken = haystack[i + offset];
            String needleToken = needle[i];
            if (ignoreCase) {
                if (!hayToken.equalsIgnoreCase(needleToken)) {
                    return false;
                }
            } else if (!hayToken.equals(needleToken)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add RTL+LTR around tags. Used for display tags better in RTL text.
     */
    public static String addBidiAroundTags(String text, SourceTextEntry ste) {
        List<Tag> tags = TagUtil.buildTagList(text, ste.getProtectedParts());

        int pos = 0;
        StringBuilder s = new StringBuilder(text.length() * 12 / 10);
        for (Tag t : tags) {
            if (pos < t.pos) {
                s.append(text.substring(pos, t.pos));
            }
            s.append(SegmentBuilder.BIDI_RLM_CHAR);
            s.append(SegmentBuilder.BIDI_LRM_CHAR);
            s.append(t.tag);
            s.append(SegmentBuilder.BIDI_LRM_CHAR);
            s.append(SegmentBuilder.BIDI_RLM_CHAR);
            pos = t.pos + t.tag.length();
        }
        if (pos < text.length()) {
            s.append(text.substring(pos));
        }
        return s.toString();
    }

    public static boolean hasBidiAroundTag(String text, String tag, int pos) {
        try {
            boolean has = true;
            if (text.charAt(pos - 1) != SegmentBuilder.BIDI_LRM_CHAR
                    || text.charAt(pos - 2) != SegmentBuilder.BIDI_RLM_CHAR) {
                has = false;
            }
            if (text.charAt(pos + tag.length()) != SegmentBuilder.BIDI_LRM_CHAR
                    || text.charAt(pos + tag.length() + 1) != SegmentBuilder.BIDI_RLM_CHAR) {
                has = false;
            }
            return has;
        } catch (StringIndexOutOfBoundsException ex) {
            // before or after known string - don't have bidi chars around this
            // tag
            return false;
        }
    }
}
