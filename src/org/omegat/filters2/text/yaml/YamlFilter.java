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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.jspecify.annotations.Nullable;
import org.omegat.core.Core;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.OStrings;

/**
 * YAML filter plugin based on ADR 2025009.
 * <ul>
 *   <li>Only string scalar values are extracted for translation.</li>
 *   <li>Mapping keys (section titles and entry keys) are not translated.</li>
 *   <li>Alignment support is not implemented.</li>
 * </ul>
 * @author Hiroshi Miura
 */
public class YamlFilter extends AbstractFilter {

    private final ObjectMapper mapper;
    private final Map<String, Integer> counters;

    public YamlFilter() {
        mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        counters = new HashMap<>();
    }

    /** Register plugin into OmegaT. */
    public static void loadPlugins() {
        Core.registerFilterClass(YamlFilter.class);
    }

    public static void unloadPlugins() {
        // Nothing to do
    }

    @Override
    public String getFileFormatName() {
        return OStrings.getString("YAML_FILTER_NAME");
    }

    @Override
    public boolean isSourceEncodingVariable() {
        return true;
    }

    @Override
    public boolean isTargetEncodingVariable() {
        return true;
    }

    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.yml"), new Instance("*.yaml") };
    }

    @Override
    protected void processFile(BufferedReader inFile, @Nullable BufferedWriter outFile, FilterContext fc)
            throws TranslationException {
        JsonNode root;
        try {
            root = mapper.readTree(inFile);
        } catch (IOException e) {
            throw new TranslationException(OStrings.getString("YAML_PARSE_ERROR", e.getMessage()), e);
        }
        JsonNode translated = translateNode(root, null);
        if (outFile != null) {
            try {
                mapper.writeValue(outFile, translated);
            } catch (IOException e) {
                throw new TranslationException(OStrings.getString("YAML_WRITE_ERROR", e.getMessage()), e);
            }
        }
    }

    /**
     * Traverse the YAML tree depth-first. For every string scalar, ask OmegaT
     * for translation and replace the value with the translation (or same as
     * source when parsing).
     */
    private JsonNode translateNode(JsonNode node, @Nullable String path) {
        if (node.isTextual()) {
            String src = node.asText();
            String currentPath = path == null ? "" : path;
            int index = counters.getOrDefault(currentPath, 0);
            counters.put(currentPath, index + 1);

            String id = currentPath + "_" + index;
            String comment = "name=" + currentPath;

            String trg = processEntry(id, src, comment);
            return TextNode.valueOf(trg);
        } else if (node.isArray()) {
            ArrayNode array = node.deepCopy();
            for (int i = 0; i < array.size(); i++) {
                JsonNode item = array.get(i);
                String nextPath = (path == null || path.isEmpty()) ? "[" + i + "]" : path + "[" + i + "]";
                JsonNode newItem = translateNode(item, nextPath);
                if (!newItem.equals(item)) {
                    array.set(i, newItem);
                }
            }
            return array;
        } else if (node.isObject()) {
            ObjectNode obj = node.deepCopy();
            for (Iterator<String> it = obj.fieldNames(); it.hasNext();) {
                String key = it.next();
                // Keys are not translated. Only process values.
                JsonNode v = obj.get(key);
                String nextPath = (path == null || path.isEmpty()) ? key : path + "/" + key;
                JsonNode newV = translateNode(v, nextPath);
                if (!newV.equals(v)) {
                    obj.set(key, newV);
                }
            }
            return obj;
        } else {
            // numbers, booleans, null, etc. — leave as is
            return node;
        }
    }

    private String processEntry(String id, String entry, String comment) {
        if (entryParseCallback != null) {
            entryParseCallback.addEntry(id, entry, null, false, comment, null, this, null);
            return entry;
        } else if (entryTranslateCallback != null) {
            String translation = entryTranslateCallback.getTranslation(id, entry, null);
            return translation != null ? translation : entry;
        } else {
            return entry;
        }
    }
}
