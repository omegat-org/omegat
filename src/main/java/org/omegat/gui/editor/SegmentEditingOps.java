/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import org.omegat.util.PatternConsts;

/**
 * Pure text computations behind the accelerating editing shortcuts: the
 * placeable cycle and the token swap. Kept free of any Swing or document
 * state so the semantics stay unit-testable and rebuild-safe: callers
 * recompute on every invocation instead of holding positions across
 * segment rebuilds.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class SegmentEditingOps {

    /**
     * Untranslatables recognised in plain text, tried in this order; earlier
     * matches shadow later ones on overlap (a number inside a URL is not a
     * separate placeable). Deliberately simple: false negatives only make
     * the cycle skip a candidate, never corrupt text.
     */
    private static final Pattern[] PLACEABLE_PATTERNS = { PatternConsts.OMEGAT_TAG,
            Pattern.compile("(?:https?|ftp)://[^\\s<>\"]+"),
            Pattern.compile("[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)+"),
            Pattern.compile("\\d+(?:[.,]\\d+)*"), };

    private SegmentEditingOps() {
    }

    /**
     * The placeables of the source text in source order, one entry per
     * occurrence: protected parts (tags, custom patterns) first at their
     * positions, then URLs, e-mail addresses and numbers found by pattern,
     * skipping matches that overlap an already claimed span.
     *
     * @param source
     *            source segment text
     * @param protectedTexts
     *            the source-text forms of the segment's protected parts
     */
    public static List<String> extractPlaceables(String source, List<String> protectedTexts) {
        List<int[]> spans = occurrenceSpans(source, protectedTexts);
        for (Pattern p : PLACEABLE_PATTERNS) {
            Matcher m = p.matcher(source);
            while (m.find()) {
                addIfFree(spans, m.start(), m.end());
            }
        }
        spans.sort((a, b) -> Integer.compare(a[0], b[0]));
        List<String> result = new ArrayList<>(spans.size());
        for (int[] span : spans) {
            result.add(source.substring(span[0], span[1]));
        }
        return result;
    }

    /**
     * All non-overlapping occurrences of the given texts as {@code [start,
     * end)} spans, earlier texts claiming their spans first. Null or empty
     * entries are skipped: filters may deliver protected parts without a
     * source-text form.
     */
    private static List<int[]> occurrenceSpans(String text, List<String> texts) {
        List<int[]> spans = new ArrayList<>();
        for (String t : texts) {
            if (t == null || t.isEmpty()) {
                continue;
            }
            int from = 0;
            int at;
            while ((at = text.indexOf(t, from)) >= 0) {
                addIfFree(spans, at, at + t.length());
                from = at + t.length();
            }
        }
        return spans;
    }

    private static void addIfFree(List<int[]> spans, int start, int end) {
        for (int[] span : spans) {
            if (start < span[1] && end > span[0]) {
                return;
            }
        }
        spans.add(new int[] { start, end });
    }

    /**
     * The source placeables not yet present in the target, in source order,
     * counting occurrences: a placeable appearing twice in the source and
     * once in the target is reported missing once. Recomputed from scratch
     * on every call, so inserting the head of the list and calling again
     * naturally works through the list.
     */
    public static List<String> missingPlaceables(String source, String target,
            List<String> protectedTexts) {
        List<String> ordered = extractPlaceables(source, protectedTexts);
        Map<String, Integer> remaining = new HashMap<>();
        for (String p : ordered) {
            remaining.computeIfAbsent(p, key -> countOccurrences(target, key));
        }
        List<String> missing = new ArrayList<>();
        for (String p : ordered) {
            int r = remaining.get(p);
            if (r > 0) {
                remaining.put(p, r - 1);
            } else {
                missing.add(p);
            }
        }
        return missing;
    }

    private static int countOccurrences(String text, String needle) {
        int count = 0;
        int from = 0;
        int at;
        while ((at = text.indexOf(needle, from)) >= 0) {
            count++;
            from = at + needle.length();
        }
        return count;
    }

    /**
     * The first tag pair among the grouped missing-tag offerings of
     * {@code TagUtil.getGroupedMissingTagsFromTarget()}: pair entries carry
     * the separator sentinel between opening and closing tag. Returns
     * {@code [open, close]}, or null when no pair is on offer.
     */
    public static String @Nullable [] firstMissingTagPair(List<String> groupedMissingTags,
            String separatorSentinel) {
        for (String entry : groupedMissingTags) {
            int sep = entry.indexOf(separatorSentinel);
            if (sep >= 0) {
                return new String[] { entry.substring(0, sep),
                        entry.substring(sep + separatorSentinel.length()) };
            }
        }
        return null;
    }

    /** Result of {@link #computeTokenSwap}: one region, one replacement. */
    public static final class TokenSwap {
        /** Start of the region to replace, in text coordinates. */
        public final int regionStart;
        /** End (exclusive) of the region to replace. */
        public final int regionEnd;
        /** The swapped text for the region. */
        public final String replacement;
        /** Caret position after the swap, keeping it inside the moved token. */
        public final int caretAfter;

        TokenSwap(int regionStart, int regionEnd, String replacement, int caretAfter) {
            this.regionStart = regionStart;
            this.regionEnd = regionEnd;
            this.replacement = replacement;
            this.caretAfter = caretAfter;
        }
    }

    /**
     * Swap the token at the caret with its neighbour, as a single
     * replacement over the covering region so the caller can apply it with
     * one document mutation (one undo step). The separator between the
     * tokens is preserved. Protected parts (tags, placeholders) count as
     * atomic tokens: a swap moves a whole tag or hops over it, but never
     * splits it. Null when the caret is not on a token or there is no
     * neighbour in the requested direction.
     *
     * @param text
     *            the editable text (translation only)
     * @param caret
     *            caret offset within {@code text}
     * @param forward
     *            true to swap with the following token, false the preceding
     * @param locale
     *            target-language locale for word breaking
     * @param atomicTexts
     *            texts treated as indivisible tokens wherever they occur
     */
    public static @Nullable TokenSwap computeTokenSwap(String text, int caret, boolean forward,
            Locale locale, List<String> atomicTexts) {
        List<int[]> tokens = tokenize(text, locale, atomicTexts);
        int idx = -1;
        for (int i = 0; i < tokens.size(); i++) {
            if (caret >= tokens.get(i)[0] && caret <= tokens.get(i)[1]) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            return null;
        }
        int otherIdx = forward ? idx + 1 : idx - 1;
        if (otherIdx < 0 || otherIdx >= tokens.size()) {
            return null;
        }
        int[] first = tokens.get(Math.min(idx, otherIdx));
        int[] second = tokens.get(Math.max(idx, otherIdx));
        String firstText = text.substring(first[0], first[1]);
        String gap = text.substring(first[1], second[0]);
        String secondText = text.substring(second[0], second[1]);
        int caretInToken = caret - tokens.get(idx)[0];
        int caretAfter;
        if (forward) {
            caretAfter = first[0] + secondText.length() + gap.length() + caretInToken;
        } else {
            caretAfter = first[0] + caretInToken;
        }
        return new TokenSwap(first[0], second[1], secondText + gap + firstText, caretAfter);
    }

    /**
     * Word tokens (runs containing at least one letter or digit) as
     * {@code [start, end)} pairs via the locale's word BreakIterator, plus
     * the occurrences of the atomic texts as indivisible tokens; word
     * tokens intersecting an atomic span are dropped in its favour.
     */
    private static List<int[]> tokenize(String text, Locale locale, List<String> atomicTexts) {
        List<int[]> tokens = new ArrayList<>(occurrenceSpans(text, atomicTexts));
        List<int[]> atomicSpans = new ArrayList<>(tokens);
        BreakIterator bi = BreakIterator.getWordInstance(locale);
        bi.setText(text);
        int start = bi.first();
        for (int end = bi.next(); end != BreakIterator.DONE; start = end, end = bi.next()) {
            if (text.substring(start, end).codePoints().anyMatch(Character::isLetterOrDigit)
                    && !intersectsAny(atomicSpans, start, end)) {
                tokens.add(new int[] { start, end });
            }
        }
        tokens.sort((a, b) -> Integer.compare(a[0], b[0]));
        return tokens;
    }

    private static boolean intersectsAny(List<int[]> spans, int start, int end) {
        for (int[] span : spans) {
            if (start < span[1] && end > span[0]) {
                return true;
            }
        }
        return false;
    }
}
