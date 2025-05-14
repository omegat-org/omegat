/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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
package org.omegat.convert.segmentation;

import org.jetbrains.annotations.Nullable;
import org.omegat.util.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Hiroshi Miura
 */
public class SegmentationConfValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SegmentationConfValidator.class);

    private static final String XMLDECODER_CLASS = "java.beans.XMLDecoder";

    private static final Set<String> ALLOWED_TAGS = Set.of("java", "void", "object", "string", "boolean", "array");
    private static final Map<String, Set<String>> ALLOWED_ATTRIBUTES = Map.of(
            "java", Set.of("version", "class"), "object", Set.of("class", "id"),
            "void", Set.of("method", "property", "index"), "array", Set.of("class", "length", "id"));
    private static final Set<String> ACCEPT_ANY_VALUES = Set.of("string", "boolean", "id", "version", "index");
    private static final Map<String, Set<String>> ALLOWED_ATTRIBUTE_VALUES = Map.of(
            "property", Set.of("rules", "mappingRules", "language", "pattern", "afterbreak", "beforebreak", "breakRule",
                    "version"),
            "class", Set.of("java.util.ArrayList", "org.omegat.core.segmentation.SRX", XMLDECODER_CLASS,
                    "org.omegat.core.segmentation.MapRule", "org.omegat.core.segmentation.Rule"),
            "method", Set.of("add"));

    private final Path segmentationConfPath;

    /**
     * Constructs a new SegmentationConfValidator with the segmentation.conf path.
     *
     * @param configPath the path to the segmentation.conf file
     */
    public SegmentationConfValidator(Path configPath) {
        this.segmentationConfPath = configPath;
    }

    /**
     * Validates the segmentation.conf file.
     * @return the validation result as ValidationResult object
     */
    public ValidationResult validate() {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        try (FileInputStream xmlStream = new FileInputStream(segmentationConfPath.toFile())) {
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(xmlStream);

            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (!event.isStartElement()) {
                    continue;
                }

                StartElement element = event.asStartElement();
                String tagname = element.getName().getLocalPart();
                if (!ALLOWED_TAGS.contains(tagname)) {
                    return ValidationResult.failure("Unexpected element: " + tagname);
                }
                Iterator<Attribute> attributes = element.getAttributes();
                while (attributes.hasNext()) {
                    Attribute attr = attributes.next();
                    String attrName = attr.getName().getLocalPart();
                    String attrValue = attr.getValue();
                    ValidationResult result = validateAttribute(tagname, attrName, attrValue);
                    if (!result.isValid()) {
                        return result;
                    }
                }
            }
            return ValidationResult.success();
        } catch (IOException | IllegalStateException | XMLStreamException e) {
            LOGGER.error("Error occurred during file processing!", e);
            return ValidationResult.failure(e.getMessage());
        }
    }

    private ValidationResult validateAttribute(String tagname, String attrName, @Nullable String attrValue) {
        if (!ALLOWED_ATTRIBUTES.getOrDefault(tagname, Set.of()).contains(attrName)) {
            return ValidationResult.failure("Unexpected attribute: " + tagname + " " + attrName);
        }
        if (!ACCEPT_ANY_VALUES.contains(attrName) && !ALLOWED_ATTRIBUTE_VALUES.getOrDefault(attrName,
                Set.of()).contains(attrValue)) {
            return ValidationResult.failure("Unexpected attribute value: " + tagname + " "
                    + attrName + " " + attrValue);
        }
        // `java.beans.XMLDecoder` class value should not be in object tag
        if ("object".equals(tagname) && "class".equals(attrName) && XMLDECODER_CLASS.equals(attrValue)) {
            return ValidationResult.failure("Attempt malicious object creation: " + tagname + " "
                    + attrName + " " + attrValue);
        }
        return ValidationResult.success();
    }
}
