/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017-2020 Thomas Cordonnier
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

package org.omegat.filters4.xml.xliff;

import java.util.zip.ZipEntry;

import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters4.AbstractZipFilter;
import org.omegat.util.OStrings;

/**
 * Filter for SDL project.
 *
 * @author Thomas Cordonnier
 */
public class SdlProject extends AbstractZipFilter {

    @Override
    public String getFileFormatName() {
        return OStrings.getString("SDLPROJECT_FILTER_NAME");
    }

    @Override
    protected boolean acceptInternalFile(ZipEntry entry, FilterContext fc) {
        return entry.getName().endsWith(".sdlxliff");
    }

    protected boolean mustTranslateInternalFile(ZipEntry entry, boolean writeMode, FilterContext fc) {
        return entry.getName().startsWith(fc.getTargetLang().getLanguage())
                && entry.getName().endsWith(".sdlxliff");
    }

    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.sdlppx") };
    }

    @Override
    protected SdlXliff getFilter(ZipEntry ze) {
        SdlXliff xmlfilter = new SdlXliff();
        xmlfilter.setCallbacks(entryParseCallback, entryTranslateCallback);
        return xmlfilter;
    }
}
