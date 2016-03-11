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

package org.omegat.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.omegat.core.segmentation.SRX;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.util.Preferences.IPreferences;
import org.omegat.util.xml.XMLBlock;
import org.omegat.util.xml.XMLStreamReader;

import gen.core.filters.Filters;

public class PreferencesXML implements IPreferences {
    private boolean m_changed;

    // use a hash map for fast lookup of data
    // use array lists for orderly recovery of it for saving to disk
    private List<String> m_nameList;
    private List<String> m_valList;
    private Map<String, Integer> m_preferenceMap;

    // Support for firing property change events
    private PropertyChangeSupport m_propChangeSupport;

    private SRX srx;
    private Filters filters;

    public PreferencesXML(File prefsFile) {
        m_preferenceMap = new HashMap<String, Integer>(64);
        m_nameList = new ArrayList<String>(32);
        m_valList = new ArrayList<String>(32);
        m_propChangeSupport = new PropertyChangeSupport(this);
        m_changed = false;
        doLoad(prefsFile);
    }

    /**
     * Loads the preferences from disk, from the specified file or, if the
     * file is null, it attempts to load from a prefs file bundled inside
     * the JAR (not supplied by default).
     */
    private void doLoad(File prefsFile) {
        XMLStreamReader xml = new XMLStreamReader();
        xml.killEmptyBlocks();

        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            if (prefsFile == null) {
                // If no prefs file is present, look inside JAR for
                // defaults. Useful for e.g. Web Start.
                is = getClass().getResourceAsStream(Preferences.FILE_PREFERENCES);
                if (is != null) {
                    isr = new InputStreamReader(is);
                    br = new BufferedReader(isr);
                    xml.setStream(br);
                    readXmlPrefs(xml);
                    br.close();
                    isr.close();
                    is.close();
                    xml.close();
                }
            } else {
                xml.setStream(prefsFile);
                readXmlPrefs(xml);
                xml.close();
            }
        } catch (TranslationException te) {
            // error loading preference file - keep whatever was
            // loaded then return gracefully to calling function
            // print an error to the console as an FYI
            Log.logWarningRB("PM_WARNING_PARSEERROR_ON_READ");
            Log.log(te);
            makeBackup(prefsFile);
        } catch (IndexOutOfBoundsException e3) {
            // error loading preference file - keep whatever was
            // loaded then return gracefully to calling function
            // print an error to the console as an FYI
            Log.logWarningRB("PM_WARNING_PARSEERROR_ON_READ");
            Log.log(e3);
            makeBackup(prefsFile);
        } catch (UnsupportedEncodingException e3) {
            // unsupported encoding - forget about it
            Log.logErrorRB(e3, "PM_UNSUPPORTED_ENCODING");
            makeBackup(prefsFile);
        } catch (IOException e4) {
            // can't read file - forget about it and move on
            Log.logErrorRB(e4, "PM_ERROR_READING_FILE");
            makeBackup(prefsFile);
        } finally {
            IOUtils.closeQuietly(xml);
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(isr);
            IOUtils.closeQuietly(br);
        }

        File srxFile = new File(StaticUtils.getConfigDir(), SRX.CONF_SENTSEG);
        srx = SRX.loadSRX(srxFile);
        if (srx == null) {
            srx = SRX.getDefault();
        }
        File filtersFile = new File(StaticUtils.getConfigDir(), FilterMaster.FILE_FILTERS);
        try {
            filters = FilterMaster.loadConfig(filtersFile);
        } catch (Exception ex) {
            Log.log(ex);
        }
        if (filters == null) {
            filters = FilterMaster.createDefaultFiltersConfig();
        }
    }

    private static void makeBackup(File file) {
        if (file == null || !file.isFile()) {
            return;
        }
        String timestamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        File bakFile = new File(file.getAbsolutePath() + "." + timestamp + ".bak");
        try {
            FileUtils.copyFile(file, bakFile);
            Log.logWarningRB("PM_BACKED_UP_PREFS_FILE", bakFile.getAbsolutePath());
        } catch (IOException ex) {
            Log.logErrorRB(ex, "PM_ERROR_BACKING_UP_PREFS_FILE");
        }
    }

    private void readXmlPrefs(XMLStreamReader xml) throws TranslationException {
        XMLBlock blk;
        List<XMLBlock> lst;

        m_preferenceMap.clear();
        String pref;
        String val;
        // advance to omegat tag
        if (xml.advanceToTag("omegat") == null) {
            return;
        }
        // advance to project tag
        if ((blk = xml.advanceToTag("preference")) == null) {
            return;
        }
        String ver = blk.getAttribute("version");
        if (ver != null && !ver.equals("1.0")) {
            // unsupported preference file version - abort read
            return;
        }
        lst = xml.closeBlock(blk);
        if (lst == null) {
            return;
        }
        for (int i = 0; i < lst.size(); i++) {
            blk = lst.get(i);
            if (blk.isClose()) {
                continue;
            }
            if (!blk.isTag()) {
                continue;
            }
            pref = blk.getTagName();
            blk = lst.get(++i);
            if (blk.isClose()) {
                // allow empty string as a preference value
                val = "";
            } else {
                val = blk.getText();
            }
            if (pref != null && val != null) {
                // valid match - record these
                m_preferenceMap.put(pref, m_valList.size());
                m_nameList.add(pref);
                m_valList.add(val);
            }
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
        boolean exists = false;
        if (key == null)
            exists = false;
        Integer i = m_preferenceMap.get(key);
        if (i != null) {
            exists = true;
        }
        return exists;
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
    public void setPreference(String name, Object value) {
        if (StringUtil.isEmpty(name) || value == null) {
            return;
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
        m_propChangeSupport.firePropertyChange(name, oldValue, value);
    }

    @Override
    public void save() {
        try {
            if (m_changed) {
                doSave(new File(StaticUtils.getConfigDir(), Preferences.FILE_PREFERENCES));
            }
        } catch (IOException e) {
            Log.logErrorRB("PM_ERROR_SAVE");
            Log.log(e);
        }
    }

    private void doSave(File outFile) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
        try {
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            out.write("<omegat>\n");
            out.write("  <preference version=\"1.0\">\n");

            for (int i = 0; i < m_nameList.size(); i++) {
                String name = m_nameList.get(i);
                String val = StringUtil.makeValidXML(m_valList.get(i).toString());
                out.write("    <" + name + ">");
                out.write(val);
                out.write("</" + name + ">\n");
            }
            out.write("  </preference>\n");
            out.write("</omegat>\n");
        } finally {
            out.close();
        }
        m_changed = false;
    }

    @Override
    public void setFilters(Filters newFilters) {
        Filters oldValue = filters;
        filters = newFilters;

        File filtersFile = new File(StaticUtils.getConfigDir(), FilterMaster.FILE_FILTERS);
        try {
            FilterMaster.saveConfig(filters, filtersFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        m_propChangeSupport.firePropertyChange(Preferences.PROPERTY_FILTERS, oldValue, newFilters);
    }

    @Override
    public Filters getFilters() {
        return filters;
    }

    @Override
    public void setSRX(SRX newSrx) {
        SRX oldValue = srx;
        srx = newSrx;

        File srxFile = new File(StaticUtils.getConfigDir() + SRX.CONF_SENTSEG);
        try {
            SRX.saveTo(srx, srxFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        m_propChangeSupport.firePropertyChange(Preferences.PROPERTY_SRX, oldValue, newSrx);
    }

    @Override
    public SRX getSRX() {
        return srx;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        m_propChangeSupport.addPropertyChangeListener(listener);
    }
}
