/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Briac Pilpre
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

package org.omegat.filters2.subtitles;

import java.util.regex.Pattern;

import org.omegat.filters2.Instance;
import org.omegat.util.OStrings;

/**
 * Filter for WebVTT subtitles files. Only simple WebVTT files are currently supported, no tags checking.
 *
 * @author Briac Pilpre (briacp@gmail.com)
 * @see <a href="https://w3c.github.io/webvtt/">Format
 *      description</a>
 */
public class WebVttFilter extends SrtFilter {
    protected static final Pattern PATTERN_TIME_INTERVAL = Pattern
            .compile("(([0-9]{2}:)?[0-9]{2}:[0-9]{2}\\.[0-9]{3})\\s*-->\\s*(([0-9]{2}:)?[0-9]{2}:[0-9]{2}\\.[0-9]{3})");

    @Override
    protected Pattern getPattern() {
        return PATTERN_TIME_INTERVAL;
    }

    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.vtt") };
    }

    @Override
    public String getFileFormatName() {
        return OStrings.getString("VTTFILTER_FILTER_NAME");
    }
}
