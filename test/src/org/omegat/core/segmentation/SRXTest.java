/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.core.segmentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

/**
 * @author Aaron Madlon-Kay
 */
public class SRXTest {

    @Test
    public void testSRXComparison() {
        SRX orig = SRX.getDefault();
        SRX clone = orig.copy();
        assertNotSame(orig, clone);
        assertEquals(orig, clone);
        assertEquals(orig.hashCode(), clone.hashCode());

        // Shallow change
        clone.setIncludeEndingTags(!clone.isIncludeEndingTags());
        assertNotEquals(orig, clone);

        // Deep change
        clone = orig.copy();
        Rule rule = clone.getMappingRules().get(0).getRules().get(0);
        rule.setAfterbreak(rule.getAfterbreak() + "foo");
        assertNotEquals(orig, clone);
    }
}
