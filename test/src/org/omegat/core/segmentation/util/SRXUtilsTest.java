/*
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Hiroshi Miura
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
 */
package org.omegat.core.segmentation.util;

import org.junit.Assert;
import org.junit.Test;
import org.omegat.core.segmentation.SRX;

import java.io.IOException;

/**
 * Unit tests for the {@link SRXUtils#getDefault()} method.
 * This method loads the default segmentation rules and configures the SRX object accordingly.
 */
public class SRXUtilsTest {

    /**
     * Test that the {@link SRXUtils#getDefault()} method successfully loads the default SRX object.
     */
    @Test
    public void testGetDefaultLoadsSRXSuccessfully() throws IOException {
        SRX srx = SRXUtils.getDefault();
        Assert.assertNotNull("The SRX object should not be null.", srx);
    }

    /**
     * Test that the {@link SRXUtils#getDefault()} method sets includeEndingTags to true.
     */
    @Test
    public void testGetDefaultIncludeEndingTagsIsTrue() throws IOException {
        SRX srx = SRXUtils.getDefault();
        Assert.assertTrue("The includeEndingTags property should be true.", srx.isIncludeEndingTags());
    }

    /**
     * Test that the {@link SRXUtils#getDefault()} method sets segmentSubflows to true.
     */
    @Test
    public void testGetDefaultSegmentSubflowsIsTrue() throws IOException {
        SRX srx = SRXUtils.getDefault();
        Assert.assertTrue("The segmentSubflows property should be true.", srx.isSegmentSubflows());
    }

    /**
     * Test that the {@link SRXUtils#getDefault()} method returns an SRX object with a non-null and non-empty version string.
     */
    @Test
    public void testGetDefaultVersion() throws IOException {
        SRX srx = SRXUtils.getDefault();
        Assert.assertEquals("The SRX version should be 2.0.", "2.0", srx.getVersion());
    }

    /**
     * Test that the {@link SRXUtils#getDefault()} method returns an SRX object with non-null mapping rules.
     */
    @Test
    public void testGetDefaultMappingRulesIsNotNull() throws IOException {
        SRX srx = SRXUtils.getDefault();
        Assert.assertNotNull("The mapping rules should not be null.", srx.getMappingRules());
    }

    /**
     * Test that the {@link SRXUtils#getDefault()} method returns an SRX object with mapping rules containing at least one rule.
     */
    @Test
    public void testGetDefaultMappingRulesHas18() throws IOException {
        SRX srx = SRXUtils.getDefault();
        Assert.assertEquals("The mapping rules should contain at least one rule.", 18, srx.getMappingRules().size());
    }
}