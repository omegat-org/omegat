/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Zoltan Bartko
               2008-2009 Didier Briel
               2010 Wildrich Fourie, Antonio Vilei, Didier Briel
               2011 John Moran, Didier Briel
               2012 Martin Fleurke, Wildrich Fourie, Didier Briel, Thomas Cordonnier,
                    Aaron Madlon-Kay
               2013 Aaron Madlon-Kay, Zoltan Bartko
               2014 Piotr Kulik, Aaron Madlon-Kay
               2015 Aaron Madlon-Kay, Yu Tang, Didier Briel, Hiroshi Miura
               2016 Aaron Madlon-Kay
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

package org.omegat.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.omegat.util.Preferences.IPreferences;

public class PreferencesImpl implements IPreferences {

    interface IPrefsPersistence {
        void load(List<String> keys, List<String> values);

        void save(List<String> keys, List<String> values) throws Exception;
    }

    private boolean m_changed;

    // use a hash map for fast lookup of data
    // use array lists for orderly recovery of it for saving to disk
    private final List<String> m_nameList;
    private final List<String> m_valList;
    private final Map<String, Integer> m_preferenceMap;
    private final IPrefsPersistence m_backing;

    public PreferencesImpl(IPrefsPersistence backing) {
        m_preferenceMap = new HashMap<String, Integer>(64);
        m_nameList = new ArrayList<String>(32);
        m_valList = new ArrayList<String>(32);
        m_backing = backing;
        m_changed = false;
        m_backing.load(m_nameList, m_valList);
        for (int i = 0; i < m_nameList.size(); i++) {
            m_preferenceMap.put(m_nameList.get(i), i);
        }
    }

    @Override
    public String getPreference(String key) {
        if (StringUtil.isEmpty(key)) {
            return "";
        }
        Integer i = m_preferenceMap.get(key);
        Object v = "";
        if (i != null) {
            // mapping exists - recover defaultValue
            v = m_valList.get(i);
        }
        return v.toString();
    }

    @Override
    public boolean existsPreference(String key) {
        return m_preferenceMap.containsKey(key);
    }

    @Override
    public boolean isPreference(String key) {
        return "true".equals(getPreference(key));
    }

    @Override
    public boolean isPreferenceDefault(String key, boolean defaultValue) {
        String val = getPreference(key);
        if (StringUtil.isEmpty(val)) {
            setPreference(key, defaultValue);
            return defaultValue;
        }
        return "true".equals(val);
    }

    @Override
    public String getPreferenceDefault(String key, String defaultValue) {
        String val = getPreference(key);
        if (val.equals("")) {
            val = defaultValue;
            setPreference(key, defaultValue);
        }
        return val;
    }

    @Override
    public <T extends Enum<T>> T getPreferenceEnumDefault(String key, T defaultValue) {
        String val = getPreference(key);
        T r;
        try {
            r = Enum.valueOf(defaultValue.getDeclaringClass(), val);
        } catch (IllegalArgumentException ex) {
            r = defaultValue;
            setPreference(key, defaultValue);
        }
        return r;
    }

    @Override
    public int getPreferenceDefault(String key, int defaultValue) {
        String val = getPreferenceDefault(key, Integer.toString(defaultValue));
        int res = defaultValue;
        try {
            res = Integer.parseInt(val);
        } catch (NumberFormatException nfe) {
        }
        return res;
    }

    @Override
    public Object setPreference(String name, Object value) {
        if (StringUtil.isEmpty(name) || value == null) {
            return null;
        }
        if (value instanceof Enum) {
            if (!value.toString().equals(((Enum<?>) value).name())) {
                throw new IllegalArgumentException(
                        "Enum prefs must return the same thing from toString() and name()");
            }
        }
        m_changed = true;
        Object oldValue = null;
        Integer i = m_preferenceMap.get(name);
        if (i == null) {
            // defaultValue doesn't exist - add it
            i = m_valList.size();
            m_preferenceMap.put(name, i);
            m_valList.add(value.toString());
            m_nameList.add(name);
        } else {
            // mapping exists - reset defaultValue to new
            oldValue = m_valList.set(i.intValue(), value.toString());
        }
        return oldValue;
    }

    @Override
    public void save() {
        try {
            if (m_changed) {
                m_backing.save(m_nameList, m_valList);
                m_changed = false;
            }
        } catch (Exception e) {
            Log.logErrorRB("PM_ERROR_SAVE");
            Log.log(e);
        }
    }
}
