/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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
package org.omegat.convert.segmentation;

import org.junit.Test;
import org.omegat.util.ValidationResult;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SegmentationConfValidatorTest {
    private static final String MALFORMED0 = "test/data/segmentation/malformed0/segmentation.conf";
    private static final String MALFORMED1 = "test/data/segmentation/malformed1/segmentation.conf";

    @Test
    public void testMalformedSegmentConf0() {
        Path segmentconf = Paths.get(MALFORMED0);
        SegmentationConfValidator validator = new SegmentationConfValidator(segmentconf);
        ValidationResult result = validator.validate();
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("java.lang.ProcessBuilder"));
    }

    @Test
    public void testMalformedSegmentConf1() {
        Path segmentconf = Paths.get(MALFORMED1);
        SegmentationConfValidator validator = new SegmentationConfValidator(segmentconf);
        ValidationResult result = validator.validate();
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("java.beans.XMLDecoder"));
    }

    @Test
    public void testSegmentionConf() {
        Path segmentconf = Paths.get("test/data/segmentation/locale_de_54/segmentation.conf");
        SegmentationConfValidator validator = new SegmentationConfValidator(segmentconf);
        ValidationResult result = validator.validate();
        assertTrue(result.isValid());
    }
}
