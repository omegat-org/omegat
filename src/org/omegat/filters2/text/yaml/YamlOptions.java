/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura.
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
package org.omegat.filters2.text.yaml;

import java.util.regex.Pattern;
import org.omegat.filters2.AbstractOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class YamlOptions extends AbstractOptions {

    public static final String OPTION_INCLUDE = "include";
    public static final String OPTION_EXCLUDE = "exclude";

    public YamlOptions(Map<String, String> options) {
        super(options);
    }

    public List<String> getIncludeKeys() {
        return getKeys(OPTION_INCLUDE);
    }

    public void setIncludeKeys(List<String> includeKeys) {
        setKeys(OPTION_INCLUDE, includeKeys);
    }

    public List<String> getExcludeKeys() {
        return getKeys(OPTION_EXCLUDE);
    }

    public void setExcludeKeys(List<String> excludeKeys) {
        setKeys(OPTION_EXCLUDE, excludeKeys);
    }

    private List<String> getKeys(String option) {
        String value = getString(option, null);
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (escaped) {
                if (c == ';' || c == '\\') {
                    sb.append(c);
                } else {
                    sb.append('\\').append(c);
                }
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == ';') {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        if (escaped) {
            sb.append('\\');
        }
        result.add(sb.toString());
        return List.copyOf(result);
    }

    private void setKeys(String option, List<String> keys) {
        if (keys.isEmpty()) {
            setString(option, "");
        } else {
            List<String> escapedKeys = new ArrayList<>();
            for (String key : keys) {
                escapedKeys.add(key.replace("\\", "\\\\").replace(";", "\\;"));
            }
            setString(option, String.join(";", escapedKeys));
        }
    }

    /**
     * Check if the path matches any of the patterns.
     * Support "*" (matches one path segment) and "**" (matches multiple path segments).
     */
    public boolean match(String path, List<String> patterns) {
        for (String pattern : patterns) {
            if (matchPattern(path, pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchPattern(String path, String pattern) {
        // Simple case: exact match
        if (path.equals(pattern)) {
            return true;
        }
        // Wildcard match
        String regex = patternToRegex(pattern);
        return Pattern.compile(regex).matcher(path).matches();
    }

    private String patternToRegex(String pattern) {
        StringBuilder sb = new StringBuilder();
        if (pattern.startsWith("**/")) {
            sb.append("(?:.*/)?");
            pattern = pattern.substring(3);
        } else {
            sb.append("^");
        }
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '*') {
                if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '*') {
                    sb.append(".*");
                    i++;
                } else {
                    sb.append("[^/]*");
                }
            } else if (c == '/') {
                sb.append("/");
            } else if (c == '[') {
                sb.append("\\[");
            } else if (c == ']') {
                sb.append("\\]");
            } else if ("\\.[]{}()^$+?|".indexOf(c) != -1) {
                sb.append("\\").append(c);
            } else {
                sb.append(c);
            }
        }
        sb.append("$");
        return sb.toString();
    }
}
