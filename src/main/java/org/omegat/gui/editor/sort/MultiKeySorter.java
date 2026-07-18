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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.IEditorSorter;
import org.omegat.gui.editor.SegmentBuilder;

/**
 * An {@link IEditorSorter} that orders segments by a combinable list of
 * {@link SortKey}s (primary, secondary, tertiary, ...), each ascending or
 * descending. Text keys collate using a {@link Collator} built for the source
 * language so that accents and language-specific ordering are respected.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class MultiKeySorter implements IEditorSorter {

    /** One (key, direction, numeric) entry of the sort chain. */
    public static final class KeySpec {
        public final SortKey key;
        public final boolean ascending;
        public final boolean numeric;

        public KeySpec(SortKey key, boolean ascending) {
            this(key, ascending, false);
        }

        public KeySpec(SortKey key, boolean ascending, boolean numeric) {
            this.key = key;
            this.ascending = ascending;
            this.numeric = numeric;
        }
    }

    private final List<KeySpec> keys;
    private final Collator collator;

    /**
     * Per text key: the entry-level text extractor and the shared comparator
     * whose per-string key cache is pre-filled via {@link #prepare} (numeric
     * value or collation key). Parallel to the text entries of {@link #keys}.
     */
    private final List<PreparableKey> preparableKeys = new ArrayList<>();

    /**
     * The comparator chain, built once and reused across re-sorts so the
     * pre-computed key caches keep paying off for the lifetime of this sorter.
     */
    private @Nullable Comparator<SegmentBuilder> comparator;

    private static final class PreparableKey {
        private final Function<SourceTextEntry, String> extractor;
        private final TextKeyComparator textComparator;

        PreparableKey(Function<SourceTextEntry, String> extractor, TextKeyComparator textComparator) {
            this.extractor = extractor;
            this.textComparator = textComparator;
        }
    }

    /** The primable text comparator of each key, or null for non-text keys. Parallel to {@link #keys}. */
    private final List<@Nullable TextKeyComparator> textComparators = new ArrayList<>();

    /**
     * @param keys
     *            ordered list of sort keys (primary first); may be empty
     * @param sourceLocale
     *            locale used for text collation
     */
    public MultiKeySorter(List<KeySpec> keys, Locale sourceLocale) {
        this.keys = new ArrayList<>(keys);
        this.collator = Collator.getInstance(sourceLocale);
        for (KeySpec ks : this.keys) {
            TextKeyComparator tc = null;
            Optional<Function<SourceTextEntry, String>> extractor = ks.key.sortTextExtractor();
            if (extractor.isPresent()) {
                tc = ks.numeric && ks.key.supportsNumeric() ? new NumericValueComparator(collator)
                        : new CachingCollatorComparator(collator);
                preparableKeys.add(new PreparableKey(extractor.get(), tc));
            }
            textComparators.add(tc);
        }
    }

    @Override
    public Comparator<SegmentBuilder> getComparator() {
        if (comparator == null) {
            Comparator<SegmentBuilder> cmp = null;
            for (int i = 0; i < keys.size(); i++) {
                KeySpec ks = keys.get(i);
                Comparator<SegmentBuilder> next = ks.key.comparator(collator, ks.ascending,
                        textComparators.get(i));
                cmp = (cmp == null) ? next : cmp.thenComparing(next);
            }
            // Stable tiebreaker: equal keys keep natural project order. This also keeps
            // gotoEntry()'s exact-match relocation deterministic.
            Comparator<SegmentBuilder> natural = Comparator
                    .comparingInt(sb -> sb.getSourceTextEntry().entryNum());
            comparator = (cmp == null) ? natural : cmp.thenComparing(natural);
        }
        return comparator;
    }

    /**
     * True when applying this sort benefits from a background preparation pass
     * (some key orders text, whose sort keys - numeric values or collation
     * keys - can be pre-computed).
     */
    public boolean needsPreparation() {
        return !preparableKeys.isEmpty();
    }

    /**
     * Pre-computes the sort keys of one entry. Called from a background worker
     * for every entry before the sort is applied, so the expensive per-string
     * work (numeral parsing, collation keys) does not block the UI; the later
     * sort then only reads the cached values. Must not run concurrently with
     * the sort itself.
     */
    public void prepare(SourceTextEntry ste) {
        for (PreparableKey pk : preparableKeys) {
            pk.textComparator.prime(pk.extractor.apply(ste));
        }
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
            if (ks.numeric) {
                sb.append(":num");
            }
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
            if (kv.length < 2) {
                continue;
            }
            try {
                SortKey key = SortKey.valueOf(kv[0].trim());
                boolean asc = !"desc".equalsIgnoreCase(kv[1].trim());
                boolean numeric = kv.length >= 3 && "num".equalsIgnoreCase(kv[2].trim());
                result.add(new KeySpec(key, asc, numeric));
            } catch (IllegalArgumentException ex) {
                // unknown key name - skip
            }
        }
        return result;
    }
}
