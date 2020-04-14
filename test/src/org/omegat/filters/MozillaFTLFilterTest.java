/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2019 Enrique Estevez
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

import org.junit.Test;
import org.omegat.core.data.IProject;
import org.omegat.filters2.text.mozftl.MozillaFTLFilter;

public class MozillaFTLFilterTest extends TestFilterBase {
    @Test
    public void testParse() throws Exception {
        parse(new MozillaFTLFilter(), "test/data/filters/MozillaFTL/MozillaFTLFilter.ftl");
    }

    @Test
    public void testTranslate() throws Exception {
        translateText(new MozillaFTLFilter(), "test/data/filters/MozillaFTL/MozillaFTLFilter.ftl");
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/MozillaFTL/MozillaFTLFilter.ftl";
        IProject.FileInfo fi = loadSourceFiles(new MozillaFTLFilter(), f);

        checkMultiStart(fi, f);
        checkMulti("Sync", "-sync-brand-short-name", null, null, null, null);
        checkMulti(
                "See <a data-l10n-name=\"logging\">HTTP Logging</a>\n    for instructions on how to use this tool.",
                "log-tutorial", null, null, null, null);
        checkMulti("When using Group Policy, this policy can only be set at the computer level.",
                "gpo-machine-only.title", null, null, null,
                "# 'gpo-machine-only' policies are related to the Group Policy features");
        checkMulti("{ $total ->\n        [one] { $total } star\n       *[other] { $total } stars\n    }",
                "cfr-doorhanger-extension-rating.tooltiptext", null, null, null,
                "# Variables:\n" + "#   $total (Number) - The rating of the add-on from 1 to 5");
        checkMulti("{ $total ->\n      [one] { $total } user\n     *[other] { $total } users\n  }",
                "cfr-doorhanger-extension-total-users", null, null, null,
                "# Variables:\n" + "#   $total (Number) - The total number of users using the add-on");
        checkMulti(
                "{ PLATFORM() ->\n        [macos] Show in Finder\n        [windows] Open Folder\n       *[other] Open Directory\n    }",
                "profiles-opendir", null, null, null, null);
        checkMultiEnd();
    }
}
