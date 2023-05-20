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
               2022-2023 Hiroshi Miura
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

package org.omegat.util;

import static org.omegat.util.PreferencesImpl.IPrefsPersistence;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.apache.commons.io.FileUtils;

public class PreferencesXML implements IPrefsPersistence {

    private final File loadFile;
    private final File saveFile;
    private final XmlMapper mapper;

    public PreferencesXML(File loadFile, File saveFile) {
        this.loadFile = loadFile;
        this.saveFile = saveFile;
        mapper = new XmlMapper();
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void load(final List<String> keys, final List<String> values) {
        if (loadFile == null) {
            return;
        }
        try (InputStream is = Files.newInputStream(loadFile.toPath())) {
            loadXml(is, keys, values);
        } catch (IOException e) {
            Log.logErrorRB(e, "PM_ERROR_READING_FILE");
            makeBackup(loadFile);
        }
    }

    private void loadXml(InputStream is, List<String> keys, List<String> values) throws IOException {
        OmegaT rootComponent = mapper.readValue(is, OmegaT.class);
        rootComponent.preference.getRows().forEach((key, value) -> {
            if (value != null) {
                keys.add(key);
                values.add(value);
            }
        });
    }

    @Override
    public void save(final List<String> keys, final List<String> values) throws Exception {
        OmegaT rootComponent = new OmegaT();
        for (int i = 0; i < keys.size(); i++) {
            if (values.get(i) != null) {
                rootComponent.preference.put(keys.get(i), values.get(i));
            }
        }
        mapper.writeValue(saveFile, rootComponent);
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

    /**
     * POJO for omegat.prefs.
     */
    @JacksonXmlRootElement(localName = "omegat")
    static class OmegaT {
        @JacksonXmlProperty(localName = "preference")
        public Preference preference = new Preference();
    }

    /**
     * POJO for omegat.prefs, preference entries.
     */
    @JsonSerialize(using = Serializer.class)
    static class Preference {
        public String version;
        private Map<String, String> rows = new TreeMap<>();

        @JsonAnySetter
        public void put(String key, String value) {
            rows.put(key, value);
        }

        @JsonAnyGetter
        public Map<String, String> getRows() {
            return rows;
        }

        public String get(String key) {
            return rows.get(key);
        }
    }

    /**
     * Custom serializer for Preference class.
     */
    public static class Serializer extends StdSerializer<Preference> {

        private static final long serialVersionUID = 1L;

        public Serializer() {
            this(null);
        }

        public Serializer(Class<Preference> t) {
            super(t);
        }

        /**
         * Custom serialize method for preference values.
         *
         * @param preference
         *            Value to serialize; can <b>not</b> be null.
         * @param gen
         *            Generator used to output resulting Json content
         * @param provider
         *            Provider that can be used to get serializers for
         *            serializing Objects value contains, if any.
         * @throws IOException
         *             when write error.
         */
        @Override
        public void serialize(final Preference preference, final JsonGenerator gen,
                final SerializerProvider provider) throws IOException {
            if (gen instanceof ToXmlGenerator) {
                gen.writeStartObject();
                ((ToXmlGenerator) gen).setNextIsAttribute(true);
                String version = preference.version;
                if (version == null) {
                    version = "1.0";
                }
                gen.writeStringField("version", version);
                ((ToXmlGenerator) gen).setNextIsAttribute(false);
                for (Map.Entry<String, String> item : preference.getRows().entrySet()) {
                    if (item.getValue() == null) {
                        continue;
                    }
                    gen.writeFieldName(item.getKey());
                    gen.writeString(item.getValue());
                }
                gen.writeEndObject();
            }
        }
    }
}
