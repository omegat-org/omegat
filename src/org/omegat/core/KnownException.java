/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
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
package org.omegat.core;

import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Know exception for some problems.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
@SuppressWarnings("serial")
public class KnownException extends RuntimeException {
    protected final List<Object> parameters = new ArrayList<>(0);

    public KnownException(String errorCode, Object... params) {
        super(errorCode);
        if (params != null) {
            this.parameters.addAll(List.of(params));
        }
    }

    public KnownException(Throwable ex, String errorCode, Object... params) {
        super(errorCode, ex);
        if (params != null) {
            this.parameters.addAll(List.of(params));
        }
    }

    public Object[] getParams() {
        return parameters.toArray();
    }

    @Override
    public String getLocalizedMessage() {
        return StringUtil.format(OStrings.getString(getMessage()), parameters);
    }
}
