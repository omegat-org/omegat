/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 zollsoft
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

import org.omegat.core.Core;
import org.omegat.core.data.TMXEntry;
import org.omegat.gui.editor.SegmentBuilder;
import org.omegat.util.OStrings;

/**
 * A single sort criterion for ordering editor segments. Each key defines an
 * <em>ascending</em> comparator; descending order is obtained centrally via
 * {@link #comparator(Collator, boolean)}, so adding a new criterion only
 * requires defining one comparator here.
 *
 * @author zollsoft
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
    SOURCE_RHYME("SORT_KEY_SOURCE_RHYME") {
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
    TARGET_RHYME("SORT_KEY_TARGET_RHYME") {
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
    TRANSLATION_STATUS("SORT_KEY_TRANS_STATUS") {
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
    NOTE_RHYME("SORT_KEY_NOTE_RHYME") {
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
    COMMENT_RHYME("SORT_KEY_COMMENT_RHYME") {
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
    };

    private final String bundleKey;

    SortKey(String bundleKey) {
        this.bundleKey = bundleKey;
    }

    /** Localized display name for UI components. */
    public String getLocalizedName() {
        return OStrings.getString(bundleKey);
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
        Comparator<SegmentBuilder> c = ascending(collator);
        return asc ? c : c.reversed();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /** Null-safe target/translation text of a segment. */
    private static String targetText(SegmentBuilder sb) {
        return nullToEmpty(Core.getProject().getTranslationInfo(sb.getSourceTextEntry()).getTranslationText());
    }

    /** Null-safe user note text of a segment. */
    private static String noteText(SegmentBuilder sb) {
        return nullToEmpty(Core.getProject().getTranslationInfo(sb.getSourceTextEntry()).getNote());
    }

    /** Null-safe source-document comment of a segment. */
    private static String commentText(SegmentBuilder sb) {
        return nullToEmpty(sb.getSourceTextEntry().getComment());
    }
}
