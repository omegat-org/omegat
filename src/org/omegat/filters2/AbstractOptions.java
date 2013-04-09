/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
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

package org.omegat.filters2;

import java.util.Map;

/**
 * Class for wrap text options for read default values, parse int, boolean, enum
 * values, etc.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public abstract class AbstractOptions {
    private final Map<String, String> options;

    public AbstractOptions(Map<String, String> options) {
        this.options = options;
    }

    public Map<String, String> getOptionsMap() {
        return options;
    }

    /**
     * Parse boolean value from string option.
     * 
     * @param key
     *            value key
     * @param defaultValue
     *            default value, if value will not be "true" or "false"
     * @return parsed value
     */
    protected boolean getBoolean(String key, boolean defaultValue) {
        String value = options.get(key);
        if ("true".equalsIgnoreCase(value)) {
            return true;
        } else if ("false".equalsIgnoreCase(value)) {
            return false;
        } else {
            return defaultValue;
        }
    }

    /**
     * Save boolean value to string option.
     * 
     * @param key
     * @param value
     */
    protected void setBoolean(String key, boolean value) {
        options.put(key, Boolean.toString(value));
    }

    /**
     * Get string from string option.
     * 
     * @param key
     *            value key
     * @param defaultValue
     *            default value, if value will be null
     * @return parsed value
     */
    protected String getString(String key, String defaultValue) {
        String value = options.get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Save string value to string option.
     * 
     * @param key
     * @param value
     */
    protected void setString(String key, String value) {
        options.put(key, value);
    }

    /**
     * Get string from string option.
     * 
     * @param key
     *            value key
     * @param defaultValue
     *            default value, if value will be null
     * @return parsed value
     */
    protected <T extends Enum<T>> T getEnum(Class<T> enumType, String key, T defaultValue) {
        String value = options.get(key);
        try {
            return Enum.valueOf(enumType, value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * Save string value to string option.
     * 
     * @param key
     * @param value
     */
    protected <T extends Enum<T>> void setEnum(String key, T value) {
        options.put(key, value.name());
    }
}
