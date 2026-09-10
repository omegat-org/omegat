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

package org.omegat.gui.editor.sort;

import java.text.Collator;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.omegat.core.Core;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.gui.editor.SegmentBuilder;
import org.omegat.util.OStrings;

/**
 * A single sort criterion for ordering editor segments. Each key defines an
 * <em>ascending</em> comparator; descending order is obtained centrally via
 * {@link #comparator(Collator, boolean)}, so adding a new criterion only
 * requires defining one comparator here.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public enum SortKey {

    /** Natural project/file order (by {@code entryNum}). The default. */
    NATURAL("SORT_KEY_NATURAL") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparingInt(sb -> sb.getSourceTextEntry().entryNum());
        }
    },

    /** Alphabetical by source text, using locale-aware collation. */
    SOURCE_ALPHA("SORT_KEY_SOURCE_ALPHA") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparing(sb -> sb.getSourceTextEntry().getSrcText(), collator);
        }
    },

    /**
     * Reverse-string ("rhyming dictionary") order: the source text is reversed
     * character-wise before collation, grouping segments by their endings.
     */
    SOURCE_RHYME("SORT_KEY_SOURCE_RHYME", "SORT_KEY_SOURCE_RHYME_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparing(
                    sb -> new StringBuilder(sb.getSourceTextEntry().getSrcText()).reverse().toString(),
                    collator);
        }
    },

    /** Source length in characters. */
    SOURCE_LENGTH("SORT_KEY_SOURCE_LENGTH") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparingInt(sb -> sb.getSourceTextEntry().getSrcText().length());
        }
    },

    /** Alphabetical by target (translation) text, using locale-aware collation. */
    TARGET_ALPHA("SORT_KEY_TARGET_ALPHA") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparing(SortKey::targetText, collator);
        }
    },

    /** Reverse-string ("rhyming") order of the target text. */
    TARGET_RHYME("SORT_KEY_TARGET_RHYME", "SORT_KEY_TARGET_RHYME_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparing(sb -> new StringBuilder(targetText(sb)).reverse().toString(),
                    collator);
        }
    },

    /** Target length in characters. */
    TARGET_LENGTH("SORT_KEY_TARGET_LENGTH") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparingInt(sb -> targetText(sb).length());
        }
    },

    /** Translation status: untranslated segments first, translated last. */
    TRANSLATION_STATUS("SORT_KEY_TRANS_STATUS", "SORT_KEY_TRANS_STATUS_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparingInt(sb -> {
                TMXEntry e = Core.getProject().getTranslationInfo(sb.getSourceTextEntry());
                return e.isTranslated() ? 1 : 0;
            });
        }
    },

    /** Alphabetical by user note text, using locale-aware collation. */
    NOTE_ALPHA("SORT_KEY_NOTE_ALPHA") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparing(SortKey::noteText, collator);
        }
    },

    /** Reverse-string ("rhyming") order of the user note text. */
    NOTE_RHYME("SORT_KEY_NOTE_RHYME", "SORT_KEY_NOTE_RHYME_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparing(sb -> new StringBuilder(noteText(sb)).reverse().toString(),
                    collator);
        }
    },

    /** User note length in characters. */
    NOTE_LENGTH("SORT_KEY_NOTE_LENGTH") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparingInt(sb -> noteText(sb).length());
        }
    },

    /** Alphabetical by source-document comment, using locale-aware collation. */
    COMMENT_ALPHA("SORT_KEY_COMMENT_ALPHA") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparing(SortKey::commentText, collator);
        }
    },

    /** Reverse-string ("rhyming") order of the source-document comment. */
    COMMENT_RHYME("SORT_KEY_COMMENT_RHYME", "SORT_KEY_COMMENT_RHYME_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparing(sb -> new StringBuilder(commentText(sb)).reverse().toString(),
                    collator);
        }
    },

    /** Source-document comment length in characters. */
    COMMENT_LENGTH("SORT_KEY_COMMENT_LENGTH") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparingInt(sb -> commentText(sb).length());
        }
    },

    /** Translation change date (oldest first). */
    CHANGE_DATE("SORT_KEY_CHANGE_DATE") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparingLong(
                    sb -> Core.getProject().getTranslationInfo(sb.getSourceTextEntry()).getChangeDate());
        }
    },

    /** Translation creation date (oldest first). */
    CREATION_DATE("SORT_KEY_CREATION_DATE") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparingLong(
                    sb -> Core.getProject().getTranslationInfo(sb.getSourceTextEntry()).getCreationDate());
        }
    },

    /** Alphabetical by the author who last changed the translation. */
    CHANGER("SORT_KEY_CHANGER") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparing(sb -> nullToEmpty(
                    Core.getProject().getTranslationInfo(sb.getSourceTextEntry()).getChanger()), collator);
        }
    },

    /** Alphabetical by the author who created the translation. */
    CREATOR("SORT_KEY_CREATOR") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparing(sb -> nullToEmpty(
                    Core.getProject().getTranslationInfo(sb.getSourceTextEntry()).getCreator()), collator);
        }
    },

    /** Alphabetical by translation origin ("Herkunft", e.g. the machine-translation engine). */
    ORIGIN_ALPHA("SORT_KEY_ORIGIN_ALPHA", "SORT_KEY_ORIGIN_ALPHA_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparing(SortKey::originText, collator);
        }
    },

    /** Whether the segment has a user note (segments without a note first). */
    HAS_NOTE("SORT_KEY_HAS_NOTE", "SORT_KEY_HAS_NOTE_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparingInt(sb -> noteText(sb).isEmpty() ? 0 : 1);
        }
    },

    /** External link/leverage status of the translation (ICE, 100%, auto, enforced...). */
    LINK_STATUS("SORT_KEY_LINK_STATUS", "SORT_KEY_LINK_STATUS_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparingInt(sb -> {
                TMXEntry e = Core.getProject().getTranslationInfo(sb.getSourceTextEntry());
                return e.linked == null ? -1 : e.linked.ordinal();
            });
        }
    },

    /** Duplicate status: unique / first occurrence / repetition. */
    DUPLICATE_STATUS("SORT_KEY_DUPLICATE_STATUS", "SORT_KEY_DUPLICATE_STATUS_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparingInt(sb -> sb.getSourceTextEntry().getDuplicate().ordinal());
        }
    },

    /** Number of duplicate occurrences of the segment. */
    DUPLICATE_COUNT("SORT_KEY_DUPLICATE_COUNT", "SORT_KEY_DUPLICATE_COUNT_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparingInt(sb -> sb.getSourceTextEntry().getNumberOfDuplicates());
        }
    },

    /** Number of tags (protected parts) in the source segment. */
    TAG_COUNT("SORT_KEY_TAG_COUNT", "SORT_KEY_TAG_COUNT_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparingInt(sb -> {
                ProtectedPart[] pp = sb.getSourceTextEntry().getProtectedParts();
                return pp == null ? 0 : pp.length;
            });
        }
    },

    /** Whether the segment starts a paragraph. */
    PARAGRAPH_START("SORT_KEY_PARAGRAPH_START", "SORT_KEY_PARAGRAPH_START_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparingInt(sb -> sb.getSourceTextEntry().isParagraphStart() ? 1 : 0);
        }
    },

    /** Whether the source-provided translation is fuzzy. */
    SOURCE_FUZZY("SORT_KEY_SOURCE_FUZZY", "SORT_KEY_SOURCE_FUZZY_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparingInt(sb -> sb.getSourceTextEntry().isSourceTranslationFuzzy() ? 1 : 0);
        }
    },

    /** Alphabetical by source file name/path. */
    SOURCE_FILE("SORT_KEY_SOURCE_FILE", "SORT_KEY_SOURCE_FILE_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparing(sb -> nullToEmpty(sb.getSourceTextEntry().getKey().file), collator);
        }
    },

    /** Alphabetical by sub-path within the source file. */
    PATH_ALPHA("SORT_KEY_PATH_ALPHA", "SORT_KEY_PATH_ALPHA_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparing(sb -> nullToEmpty(sb.getSourceTextEntry().getKey().path), collator);
        }
    },

    /** Alphabetical by segment id (e.g. XLIFF/PO id). */
    ID_ALPHA("SORT_KEY_ID_ALPHA", "SORT_KEY_ID_ALPHA_TT") {
        @Override
        Comparator<SegmentBuilder> ascending(Collator collator) {
            return Comparator.comparing(sb -> nullToEmpty(sb.getSourceTextEntry().getKey().id), collator);
        }
    };

    private final String bundleKey;
    /** Optional bundle key for a longer explanatory tooltip, or null for none. */
    private final @Nullable String tooltipKey;

    SortKey(String bundleKey) {
        this(bundleKey, null);
    }

    SortKey(String bundleKey, @Nullable String tooltipKey) {
        this.bundleKey = bundleKey;
        this.tooltipKey = tooltipKey;
    }

    /** Localized display name for UI components. */
    public String getLocalizedName() {
        return OStrings.getString(bundleKey);
    }

    /**
     * Localized explanatory tooltip for the less obvious criteria, or {@code null}
     * when the display name alone is clear enough.
     */
    public @Nullable String getTooltip() {
        return tooltipKey == null ? null : OStrings.getString(tooltipKey);
    }

    /** The ascending comparator for this key. {@code collator} may be unused for non-text keys. */
    abstract Comparator<SegmentBuilder> ascending(Collator collator);

    /**
     * Comparator for this key in the requested direction.
     *
     * @param collator
     *            locale-aware collator for text keys
     * @param asc
     *            ascending if true, descending otherwise
     */
    public Comparator<SegmentBuilder> comparator(Collator collator, boolean asc) {
        return comparator(collator, asc, false);
    }

    /**
     * Comparator for this key. When {@code numeric} is true and this key sorts
     * textual values, the values are ordered by the number they contain (via
     * {@link NumericValueComparator}); otherwise the plain ascending comparator
     * is used. The requested direction is applied last.
     */
    public Comparator<SegmentBuilder> comparator(Collator collator, boolean asc, boolean numeric) {
        return comparator(collator, asc, numeric, false);
    }

    /**
     * Like {@link #comparator(Collator, boolean, boolean)}; with
     * {@code ignoreRoman} true the numeric mode treats Roman numerals as plain
     * text instead of numbers.
     */
    public Comparator<SegmentBuilder> comparator(Collator collator, boolean asc, boolean numeric,
            boolean ignoreRoman) {
        return comparator(collator, asc,
                numeric && supportsNumeric() ? new NumericValueComparator(collator, !ignoreRoman) : null);
    }

    /**
     * Comparator variant taking a caller-owned text comparator (or null for
     * the plain built-in ordering), so the caller can keep the instance and
     * pre-compute its per-string key cache before the sort runs. The text
     * comparator orders the value of {@link #sortTextExtractor()}.
     */
    Comparator<SegmentBuilder> comparator(Collator collator, boolean asc,
            @Nullable TextKeyComparator textComparator) {
        Comparator<SegmentBuilder> c;
        Optional<Function<SourceTextEntry, String>> extractor = sortTextExtractor();
        if (textComparator != null && extractor.isPresent()) {
            Function<SourceTextEntry, String> f = extractor.get();
            c = Comparator.comparing(sb -> f.apply(sb.getSourceTextEntry()), textComparator);
        } else {
            c = ascending(collator);
        }
        return asc ? c : c.reversed();
    }

    /** True if this key sorts textual values that can also be ordered numerically. */
    public boolean supportsNumeric() {
        return entryTextExtractor().isPresent();
    }

    /**
     * True if this key orders target-language text; its collation and number
     * parsing then follow the project's target locale, not the source locale.
     */
    public boolean usesTargetText() {
        return this == TARGET_ALPHA || this == TARGET_RHYME || this == TARGET_LENGTH;
    }

    /**
     * The entry-level extractor of the text this key actually orders by (for
     * the rhyme keys: the reversed text); empty for non-text keys. Every key
     * with such an extractor can have its sort key pre-computed by the sort
     * bar's background preparation pass.
     */
    Optional<Function<SourceTextEntry, String>> sortTextExtractor() {
        switch (this) {
            case SOURCE_RHYME:
                return Optional.of(ste -> reverse(nullToEmpty(ste.getSrcText())));
            case TARGET_RHYME:
                return Optional.of(ste -> reverse(targetText(ste)));
            case NOTE_RHYME:
                return Optional.of(ste -> reverse(noteText(ste)));
            case COMMENT_RHYME:
                return Optional.of(ste -> reverse(commentText(ste)));
            default:
                return entryTextExtractor();
        }
    }

    private static String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    /**
     * The entry-level text extractor for keys that sort text; empty for the
     * others. Working on {@link SourceTextEntry} (all text criteria only depend
     * on it) lets the sort bar pre-compute numeric sort keys in the background
     * without needing the editor's segment builders.
     */
    Optional<Function<SourceTextEntry, String>> entryTextExtractor() {
        switch (this) {
            case SOURCE_ALPHA:
                return Optional.of(ste -> nullToEmpty(ste.getSrcText()));
            case TARGET_ALPHA:
                return Optional.of(SortKey::targetText);
            case NOTE_ALPHA:
                return Optional.of(SortKey::noteText);
            case COMMENT_ALPHA:
                return Optional.of(SortKey::commentText);
            case ORIGIN_ALPHA:
                return Optional.of(SortKey::originText);
            case SOURCE_FILE:
                return Optional.of(ste -> nullToEmpty(ste.getKey().file));
            case PATH_ALPHA:
                return Optional.of(ste -> nullToEmpty(ste.getKey().path));
            case ID_ALPHA:
                return Optional.of(ste -> nullToEmpty(ste.getKey().id));
            default:
                return Optional.empty();
        }
    }

    private static String nullToEmpty(@Nullable String s) {
        return s == null ? "" : s;
    }

    /** Null-safe target/translation text of a segment. */
    private static String targetText(SegmentBuilder sb) {
        return targetText(sb.getSourceTextEntry());
    }

    private static String targetText(SourceTextEntry ste) {
        return nullToEmpty(Core.getProject().getTranslationInfo(ste).getTranslationText());
    }

    /** Null-safe user note text of a segment. */
    private static String noteText(SegmentBuilder sb) {
        return noteText(sb.getSourceTextEntry());
    }

    private static String noteText(SourceTextEntry ste) {
        return nullToEmpty(Core.getProject().getTranslationInfo(ste).getNote());
    }

    /** Null-safe source-document comment of a segment. */
    private static String commentText(SegmentBuilder sb) {
        return commentText(sb.getSourceTextEntry());
    }

    private static String commentText(SourceTextEntry ste) {
        return nullToEmpty(ste.getComment());
    }

    /** Null-safe translation origin ("Herkunft") of a segment, e.g. the MT engine name. */
    private static String originText(SegmentBuilder sb) {
        return originText(sb.getSourceTextEntry());
    }

    private static String originText(SourceTextEntry ste) {
        return nullToEmpty(Core.getProject().getTranslationInfo(ste).getPropValue(ProjectTMX.PROP_ORIGIN));
    }
}
