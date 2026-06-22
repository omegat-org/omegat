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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.Nullable;
import org.omegat.gui.editor.IEditorSorter;
import org.omegat.gui.editor.SegmentBuilder;

/**
 * An {@link IEditorSorter} that orders segments by a combinable list of
 * {@link SortKey}s (primary, secondary, tertiary, ...), each ascending or
 * descending. Text keys collate using a {@link Collator} built for the source
 * language so that accents and language-specific ordering are respected.
 *
 * @author zollsoft
 */
public class MultiKeySorter implements IEditorSorter {

    /** One (key, direction) pair of the sort chain. */
    public static final class KeySpec {
        public final SortKey key;
        public final boolean ascending;

        public KeySpec(SortKey key, boolean ascending) {
            this.key = key;
            this.ascending = ascending;
        }
    }

    private final List<KeySpec> keys;
    private final Collator collator;

    /**
     * @param keys
     *            ordered list of sort keys (primary first); may be empty
     * @param sourceLocale
     *            locale used for text collation
     */
    public MultiKeySorter(List<KeySpec> keys, Locale sourceLocale) {
        this.keys = new ArrayList<>(keys);
        this.collator = Collator.getInstance(sourceLocale);
    }

    @Override
    public Comparator<SegmentBuilder> getComparator() {
        Comparator<SegmentBuilder> cmp = null;
        for (KeySpec ks : keys) {
            Comparator<SegmentBuilder> next = ks.key.comparator(collator, ks.ascending);
            cmp = (cmp == null) ? next : cmp.thenComparing(next);
        }
        // Stable tiebreaker: equal keys keep natural project order. This also keeps
        // gotoEntry()'s exact-match relocation deterministic.
        Comparator<SegmentBuilder> natural = Comparator
                .comparingInt(sb -> sb.getSourceTextEntry().entryNum());
        return (cmp == null) ? natural : cmp.thenComparing(natural);
    }

    /** The configured sort keys, in priority order. */
    public List<KeySpec> getKeys() {
        return Collections.unmodifiableList(keys);
    }

    /** True if no effective sort key is configured (i.e. natural order). */
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    /**
     * Serializes the sort chain to a compact, preference-friendly string such as
     * {@code "SOURCE_ALPHA:asc;SOURCE_LENGTH:desc"}.
     */
    public String toPreferenceString() {
        return toPreferenceString(keys);
    }

    /** Serializes a sort chain to its preference string form. */
    public static String toPreferenceString(List<KeySpec> keys) {
        StringBuilder sb = new StringBuilder();
        for (KeySpec ks : keys) {
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(ks.key.name()).append(':').append(ks.ascending ? "asc" : "desc");
        }
        return sb.toString();
    }

    /**
     * Parses a preference string produced by {@link #toPreferenceString()} back
     * into a list of {@link KeySpec}. Unknown or malformed entries are skipped.
     */
    public static List<KeySpec> fromPreferenceString(@Nullable String s) {
        List<KeySpec> result = new ArrayList<>();
        if (s == null || s.trim().isEmpty()) {
            return result;
        }
        for (String part : s.split(";")) {
            String[] kv = part.split(":");
            if (kv.length != 2) {
                continue;
            }
            try {
                SortKey key = SortKey.valueOf(kv[0].trim());
                boolean asc = !"desc".equalsIgnoreCase(kv[1].trim());
                result.add(new KeySpec(key, asc));
            } catch (IllegalArgumentException ex) {
                // unknown key name - skip
            }
        }
        return result;
    }
}
