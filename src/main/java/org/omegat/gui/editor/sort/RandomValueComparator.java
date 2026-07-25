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

import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * Orders strings by a seed-derived pseudo-random rank: every distinct value
 * maps to one rank, so equal values stay grouped while the distinct values
 * shuffle. The rank is a CRC32 over the seed and the UTF-8 bytes of the
 * value - a platform- and JVM-independent function, so the same seed
 * reproduces the same order everywhere (deliberately not String.hashCode,
 * whose stability we do not want to turn into a persisted-preference
 * contract). Rank collisions fall back to the collator, keeping the order
 * total and transitive as the sort contract requires.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
class RandomValueComparator implements TextKeyComparator {

    private final Collator collator;
    private final long seed;

    /** Per-sort-pass rank cache, pre-filled via {@link #prime}. */
    private final Map<String, Long> cache = new HashMap<>();

    RandomValueComparator(Collator collator, long seed) {
        this.collator = collator;
        this.seed = seed;
    }

    @Override
    public int compare(String a, String b) {
        int byRank = Long.compare(rank(a), rank(b));
        return byRank != 0 ? byRank : collator.compare(a, b);
    }

    @Override
    public void prime(String s) {
        rank(s);
    }

    private long rank(String s) {
        return cache.computeIfAbsent(s, this::hash);
    }

    private long hash(String s) {
        CRC32 crc = new CRC32();
        for (int i = 0; i < Long.BYTES; i++) {
            crc.update((int) (seed >>> (Byte.SIZE * i)) & 0xFF);
        }
        crc.update(s.getBytes(StandardCharsets.UTF_8));
        return crc.getValue();
    }
}
