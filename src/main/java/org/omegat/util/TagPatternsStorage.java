/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import org.jspecify.annotations.Nullable;

/**
 * Storage for the project-specific custom tag and removed-text regular
 * expressions (feature requests #926, #1427, #824).
 *
 * The expressions live in their own file in the project's omegat folder, like
 * filters.xml and segmentation.conf, so that older OmegaT versions can still
 * open the project: they simply never read the file.
 *
 * @author Stephan Pakebusch
 */
public final class TagPatternsStorage {

    /** Name of the tag patterns file in the project's omegat folder. */
    public static final String FILE_TAG_PATTERNS = "tag_patterns.xml";

    private static final XmlMapper MAPPER;

    static {
        MAPPER = XmlMapper.xmlBuilder().build();
        MAPPER.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        // An absent element means the global preference applies, while an
        // empty element is a real override, so nulls must be omitted but
        // empty strings written.
        MAPPER.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private TagPatternsStorage() {
    }

    /**
     * Loads the tag patterns file. Returns null when the file does not
     * exist. A file that cannot be parsed raises an IOException, so the
     * caller can fall back to the global preferences while the file stays
     * in place for repair.
     */
    public static @Nullable TagPatterns load(File configFile) throws IOException {
        if (!configFile.exists()) {
            return null;
        }
        try {
            return MAPPER.readValue(configFile, TagPatterns.class);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Parses tag patterns from XML content, as written by
     * {@link #writeToString}. A file's content that cannot be parsed raises
     * an IOException, like {@link #load}.
     */
    public static TagPatterns loadFromString(String content) throws IOException {
        try {
            return MAPPER.readValue(content, TagPatterns.class);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Serializes the tag patterns to the canonical XML content of the tag
     * patterns file. Together with {@link #loadFromString} this yields a
     * canonical form: differently formatted files with equal expressions
     * serialize identically.
     */
    public static String writeToString(TagPatterns patterns) throws IOException {
        return MAPPER.writeValueAsString(patterns);
    }

    /**
     * Saves the tag patterns to the given file, or deletes the file when both
     * expressions are null, i.e. the project inherits the global preferences.
     */
    public static void save(@Nullable TagPatterns patterns, File configFile) throws IOException {
        if (patterns == null || patterns.isEmpty()) {
            Files.deleteIfExists(configFile.toPath());
            return;
        }
        MAPPER.writeValue(configFile, patterns);
    }

    /**
     * The content of the tag patterns file. Null fields mean the global
     * preference applies; an empty string is a real override that switches
     * the expression off for the project.
     */
    @JacksonXmlRootElement(localName = "tag_patterns")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TagPatterns {

        @JacksonXmlProperty(localName = "custom_tag_pattern")
        private @Nullable String customTagPattern;

        @JacksonXmlProperty(localName = "remove_text_pattern")
        private @Nullable String removeTextPattern;

        public @Nullable String getCustomTagPattern() {
            return customTagPattern;
        }

        public void setCustomTagPattern(@Nullable String customTagPattern) {
            this.customTagPattern = customTagPattern;
        }

        public @Nullable String getRemoveTextPattern() {
            return removeTextPattern;
        }

        public void setRemoveTextPattern(@Nullable String removeTextPattern) {
            this.removeTextPattern = removeTextPattern;
        }

        @JsonIgnore
        public boolean isEmpty() {
            return customTagPattern == null && removeTextPattern == null;
        }
    }
}
