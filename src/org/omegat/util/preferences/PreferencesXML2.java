/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2022 Hiroshi Miura
 *                Home page: http://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.omegat.util.preferences;

import static org.omegat.util.preferences.PreferencesImpl.IPrefsPersistence;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.apache.commons.io.FileUtils;

import org.omegat.util.Log;
import org.omegat.util.Preferences;

public class PreferencesXML2 implements IPrefsPersistence {

    private final File loadFile;
    private final File saveFile;

    public PreferencesXML2(File loadFile, File saveFile) {
        this.loadFile = loadFile;
        this.saveFile = saveFile;
    }

    @Override
    public void load(final List<String> keys, final List<String> values) {
        if (loadFile != null ) {
            try (InputStream is = Files.newInputStream(loadFile.toPath())) {
                loadXml(is, keys, values);
            } catch (IOException e) {
                Log.logErrorRB(e, "PM_ERROR_READING_FILE");
                makeBackup(loadFile);
            }
        } else {
            // If no prefs file is present, look inside JAR for defaults.
            try (InputStream is = getClass().getResourceAsStream(Preferences.FILE_PREFERENCES)) {
                if (is != null) {
                    loadXml(is, keys, values);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadXml(InputStream is, List<String> keys, List<String> values) throws IOException {
        OmegaT rootComponent;
        XmlMapper mapper = new XmlMapper();
        rootComponent = mapper.readValue(is, OmegaT.class);
        Preference preference = rootComponent.preference;
        if (preference != null) {
            preference.records.forEach((key, value) -> {
                keys.add(key);
                values.add(value);
            });
        }
    }

    @Override
    public void save(final List<String> keys, final List<String> values) throws Exception {
        OmegaT rootComponent = new OmegaT();
        rootComponent.preference = new Preference();
        rootComponent.preference.version = "1.0";
        Map<String, String> records = new TreeMap<>();
        for (int i = 0; i < keys.size(); i++) {
            records.put(keys.get(i), values.get(i));
        }
        rootComponent.preference.records = records;
        XmlMapper mapper = new XmlMapper();
        String xmlString = mapper.writeValueAsString(rootComponent);
        try (FileWriter writer = new FileWriter(saveFile)) {
            writer.write(xmlString);
        }
    }

    @JacksonXmlRootElement(localName = "omegat")
    static class OmegaT {
        @JacksonXmlProperty(localName = "preference")
        public Preference preference;
    }

    static class Preference {
        @JacksonXmlProperty(localName = "version", isAttribute = true)
        public String version;
        @JacksonXmlElementWrapper(useWrapping=false)
        public Map<String, String> records;
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

}
