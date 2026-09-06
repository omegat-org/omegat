/**************************************************************************
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
 **************************************************************************/
package org.omegat.filters2;

import java.io.File;

import org.jspecify.annotations.Nullable;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

@SuppressWarnings("serial")
public class FilterEncodingException extends TranslationException {
    private final File sourceFile;
    private final @Nullable File targetFile;
    private final String filterName;
    private final String sourceEncoding;
    private final String targetEncoding;

    public FilterEncodingException(File sourceFile, @Nullable File targetFile, String filterName,
                                   String sourceEncoding, String targetEncoding, Throwable cause) {
        super(cause);
        this.sourceFile = sourceFile;
        this.targetFile = targetFile;
        this.filterName = filterName;
        this.sourceEncoding = sourceEncoding;
        this.targetEncoding = targetEncoding;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public @Nullable File getTargetFile() {
        return targetFile;
    }

    public String getFilterName() {
        return filterName;
    }

    public String getSourceEncoding() {
        return sourceEncoding;
    }

    public String getTargetEncoding() {
        return targetEncoding;
    }
}