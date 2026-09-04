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

package org.omegat.core.data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.omegat.util.Token;

/**
 * Shared builders for the protected-parts tests: a real SourceTextEntry
 * carrying the given placeholder texts, and a word token addressed by its
 * text content.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class ProtectedPartsFixtures {

    private ProtectedPartsFixtures() {
    }

    public static SourceTextEntry entryWithProtectedParts(String source, String... parts) {
        List<ProtectedPart> pps = Arrays.stream(parts).map(p -> {
            ProtectedPart pp = new ProtectedPart();
            pp.setTextInSourceSegment(p);
            return pp;
        }).collect(Collectors.toList());
        return new SourceTextEntry(new EntryKey("file.txt", source, null, "", "", null), 1, null, null,
                pps, true);
    }

    public static Token tokenAt(String text, String word) {
        return new Token(word, text.indexOf(word), word.length());
    }
}
