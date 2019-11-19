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

package org.omegat.filters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.FiltersUtil;
import org.omegat.filters2.text.TextFilter;
import org.omegat.filters2.text.bundles.ResourceBundleFilter;

import gen.core.filters.Files;
import gen.core.filters.Filters;

/**
 * @author Aaron Madlon-Kay
 */
public class FiltersTest {

    @Before
    public final void setUp() {
        FilterMaster.setFilterClasses(Arrays.asList(TextFilter.class, ResourceBundleFilter.class));
    }

    @Test
    public void testFiltersComparison() {
        Filters orig = FilterMaster.createDefaultFiltersConfig();
        Filters clone = FilterMaster.createDefaultFiltersConfig();
        assertNotSame(orig, clone);
        // Filters does not itself implement equals() with the semantics we want
        assertNotEquals(orig, clone);
        // Use external implementation instead
        assertTrue(FiltersUtil.filtersEqual(orig, clone));

        // Shallow change
        clone.setIgnoreFileContext(!clone.isIgnoreFileContext());
        assertFalse(FiltersUtil.filtersEqual(orig, clone));

        // Deep change
        clone = FilterMaster.createDefaultFiltersConfig();
        Files file = clone.getFilters().get(0).getFiles().get(0);
        file.setTargetEncoding(file.getTargetEncoding() + "foo");
        assertFalse(FiltersUtil.filtersEqual(orig, clone));
    }
}
