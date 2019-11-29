/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2006 Henry Pijffers
               2010 Alex Buloichik
               2011 Alex Buloichik, Martin Fleurke
               2012 Alex Buloichik, Didier Briel
               2013 Aaron Madlon-Kay
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.omegat.core.data.TMXEntry;

/**
 * Helper for write TMX files, using StAX.
 *
 * We can't use JAXB for writing because it changes spaces on formatted output.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 */
public class TMXWriter2 {
    static String lineSeparator = System.lineSeparator();

    public static final String PROP_ID = "id";

    private static final XMLOutputFactory FACTORY;

    private final OutputStream out;
    private final XMLStreamWriter xml;

    private final String langSrc, langTar;
    private final boolean levelTwo;
    private final boolean forceValidTMX;

    /**
     * DateFormat with format YYYYMMDDThhmmssZ able to display a date in UTC time.
     *
     * SimpleDateFormat IS NOT THREAD SAFE !!!
     */
    private final SimpleDateFormat tmxDateFormat;

    static {
        FACTORY = XMLOutputFactory.newInstance();
    }

    /**
     *
     * @param file
     * @param sourceLanguage
     * @param targetLanguage
     * @param sentenceSegmentingEnabled
     * @param levelTwo
     *            When true, the tmx is made compatible with level 2 (TMX version 1.4)
     * @param callback
     * @throws Exception
     */
    public TMXWriter2(File file, final Language sourceLanguage, final Language targetLanguage,
            boolean sentenceSegmentingEnabled, boolean levelTwo, boolean forceValidTMX) throws Exception {
        this.levelTwo = levelTwo;
        this.forceValidTMX = forceValidTMX;

        out = new BufferedOutputStream(new FileOutputStream(file));
        xml = FACTORY.createXMLStreamWriter(out, StandardCharsets.UTF_8.name());

        xml.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
        xml.writeCharacters(lineSeparator);

        if (levelTwo) {
            xml.writeDTD("<!DOCTYPE tmx SYSTEM \"tmx14.dtd\">");
            xml.writeCharacters(lineSeparator);
            xml.writeStartElement("tmx");
            xml.writeAttribute("version", "1.4");
        } else {
            xml.writeDTD("<!DOCTYPE tmx SYSTEM \"tmx11.dtd\">");
            xml.writeCharacters(lineSeparator);
            xml.writeStartElement("tmx");
            xml.writeAttribute("version", "1.1");
        }
        xml.writeCharacters(lineSeparator);

        writeHeader(sourceLanguage, targetLanguage, sentenceSegmentingEnabled);

        xml.writeCharacters("  ");
        xml.writeStartElement("body");
        xml.writeCharacters(lineSeparator);

        langSrc = sourceLanguage.toString();
        langTar = targetLanguage.toString();

        tmxDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        tmxDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void close() throws Exception {
        try {
            xml.writeCharacters("  ");
            xml.writeEndElement(); // body

            xml.writeCharacters(lineSeparator);
            xml.writeEndElement(); // tmx

            xml.writeCharacters(lineSeparator);
            xml.writeEndDocument();
        } finally {
            xml.close();
            out.close();
        }
    }

    public void writeComment(String comment) throws Exception {
        xml.writeComment(comment);
        xml.writeCharacters(lineSeparator);
    }

    /**
     * Write one entry.
     *
     * @param source
     * @param translation
     * @param propValues
     *            pairs with property name and values
     */
    public void writeEntry(String source, String translation, TMXEntry entry, List<String> propValues)
            throws Exception {
        writeEntry(source, translation, entry.note, entry.creator, entry.creationDate, entry.changer, entry.changeDate,
                propValues);
    }

    public void writeEntry(String source, String translation, String note, String creator, long creationDate,
            String changer, long changeDate, List<String> propValues) throws Exception {
        if (source == null && translation == null) {
            throw new NullPointerException(
                    "The TMX spec requires at least one <tuv> per <tu>. Source and translation can't both be null.");
        }

        xml.writeCharacters("    ");
        xml.writeStartElement("tu");
        // Output ID as tuid for level 2 only for now, because internally OmegaT
        // uses <note type="id"> and tuid is intended for external
        // interoperability
        if (levelTwo && propValues != null) {
            for (int i = 0; i < propValues.size(); i += 2) {
                String key = propValues.get(i);
                if (key.equals(PROP_ID)) {
                    String idValue = propValues.get(i + 1);
                    if (idValue != null) {
                        xml.writeAttribute("tuid", StringUtil.removeXMLInvalidChars(idValue));
                    }
                    break;
                }
            }
        }
        xml.writeCharacters(lineSeparator);

        // add properties
        if (propValues != null) {
            for (int i = 0; i < propValues.size(); i += 2) {
                String value = propValues.get(i + 1);
                if (value == null) {
                    // value is null - not need to write
                    continue;
                }
                xml.writeCharacters("      ");
                xml.writeStartElement("prop");
                String type = StringUtil.removeXMLInvalidChars(propValues.get(i));
                xml.writeAttribute("type", type);
                xml.writeCharacters(StringUtil.removeXMLInvalidChars(value));
                xml.writeEndElement(); // prop
                xml.writeCharacters(lineSeparator);
            }
        }

        // add note
        if (!StringUtil.isEmpty(note)) {
            note = StringUtil.removeXMLInvalidChars(note);
            if (forceValidTMX) {
                note = TagUtil.stripXmlTags(note);
            }
            xml.writeCharacters("      ");
            xml.writeStartElement("note");
            xml.writeCharacters(platformLineSeparator(note));
            xml.writeEndElement(); // note
            xml.writeCharacters(lineSeparator);
        }

        // write source segment
        if (source != null) {
            source = StringUtil.removeXMLInvalidChars(source);
            if (forceValidTMX) {
                source = TagUtil.stripXmlTags(source);
            }
            xml.writeCharacters("      ");
            xml.writeStartElement("tuv");
            if (levelTwo) {
                xml.writeAttribute("xml", "", "lang", langSrc);
            } else {
                xml.writeAttribute("lang", langSrc);
            }
            xml.writeCharacters(lineSeparator);
            if (levelTwo) {
                writeLevelTwo(platformLineSeparator(source));
            } else {
                writeLevelOne(platformLineSeparator(source));
            }
            xml.writeCharacters(lineSeparator);
            xml.writeCharacters("      ");
            xml.writeEndElement(); // tuv
            xml.writeCharacters(lineSeparator);
        }

        // write target segment
        if (translation != null) {
            translation = StringUtil.removeXMLInvalidChars(translation);
            if (forceValidTMX) {
                translation = TagUtil.stripXmlTags(translation);
            }

            xml.writeCharacters("      ");
            xml.writeStartElement("tuv");
            if (levelTwo) {
                xml.writeAttribute("xml", "", "lang", langTar);
            } else {
                xml.writeAttribute("lang", langTar);
            }
            if (!StringUtil.isEmpty(changer)) {
                xml.writeAttribute("changeid", changer);
            }
            if (changeDate > 0) {
                xml.writeAttribute("changedate", tmxDateFormat.format(new Date(changeDate)));
            }
            if (!StringUtil.isEmpty(creator)) {
                xml.writeAttribute("creationid", creator);
            }
            if (creationDate > 0) {
                xml.writeAttribute("creationdate", tmxDateFormat.format(new Date(creationDate)));
            }
            xml.writeCharacters(lineSeparator);

            if (levelTwo) {
                writeLevelTwo(platformLineSeparator(translation));
            } else {
                writeLevelOne(platformLineSeparator(translation));
            }
            xml.writeCharacters(lineSeparator);
            xml.writeCharacters("      ");
            xml.writeEndElement(); // tuv
            xml.writeCharacters(lineSeparator);
        }

        xml.writeCharacters("    ");
        xml.writeEndElement(); // tu
        xml.writeCharacters(lineSeparator);
    }

    private void writeHeader(final Language sourceLanguage, final Language targetLanguage,
            boolean sentenceSegmentingEnabled) throws Exception {
        xml.writeCharacters("  ");
        xml.writeEmptyElement("header");

        xml.writeAttribute("creationtool", OStrings.getApplicationName());
        xml.writeAttribute("o-tmf", "OmegaT TMX");
        xml.writeAttribute("adminlang", "EN-US");
        xml.writeAttribute("datatype", "plaintext");

        xml.writeAttribute("creationtoolversion", OStrings.getVersion());

        xml.writeAttribute("segtype", sentenceSegmentingEnabled ? "sentence" : "paragraph");

        xml.writeAttribute("srclang", sourceLanguage.toString());

        xml.writeCharacters(lineSeparator);
    }

    /**
     * Create simple segment.
     */
    private void writeLevelOne(String segment) throws Exception {
        xml.writeCharacters("        ");
        xml.writeStartElement("seg");
        xml.writeCharacters(segment);
        xml.writeEndElement();
    }

    protected static final Pattern TAGS_ANY = Pattern.compile("<(/?)([\\S&&[^/\\d]]+)(\\d+)(/?)>");

    enum TAG_TYPE {
        SINGLE, START, END
    };

    private void writeLevelTwo(String segment) throws Exception {
        xml.writeCharacters("        ");
        xml.writeStartElement("seg");

        TAG_TYPE tagType;
        int pos = 0;
        Matcher m = TAGS_ANY.matcher(segment);
        while (true) {
            if (!m.find(pos)) {
                break;
            }
            xml.writeCharacters(segment.substring(pos, m.start()));
            pos = m.end();

            if (!m.group(1).isEmpty()) {
                tagType = TAG_TYPE.END;
            } else if (!m.group(4).isEmpty()) {
                tagType = TAG_TYPE.SINGLE;
            } else {
                tagType = TAG_TYPE.START;
            }

            String tagName = m.group(2);
            String tagNumber = m.group(3);

            switch (tagType) {
            case SINGLE:
                xml.writeStartElement("ph");
                xml.writeAttribute("x", tagNumber);
                xml.writeCharacters(m.group());
                xml.writeEndElement();
                break;
            case START:
                String endTag = "</" + tagName + tagNumber + ">";
                if (segment.contains(endTag)) {
                    xml.writeStartElement("bpt");
                    xml.writeAttribute("i", tagNumber);
                    xml.writeAttribute("x", tagNumber);
                    xml.writeCharacters(m.group());
                    xml.writeEndElement();
                } else {
                    xml.writeStartElement("it");
                    xml.writeAttribute("pos", "begin");
                    xml.writeAttribute("x", tagNumber);
                    xml.writeCharacters(m.group());
                    xml.writeEndElement();
                }
                break;
            case END:
                String startTag = "<" + tagName + tagNumber + ">";
                if (segment.contains(startTag)) {
                    xml.writeStartElement("ept");
                    xml.writeAttribute("i", tagNumber);
                    xml.writeCharacters(m.group());
                    xml.writeEndElement();
                } else {
                    xml.writeStartElement("it");
                    xml.writeAttribute("pos", "end");
                    xml.writeAttribute("x", tagNumber);
                    xml.writeCharacters(m.group());
                    xml.writeEndElement();
                }
                break;
            default:
                throw new RuntimeException("Unknow tag type");
            }
        }

        xml.writeCharacters(segment.substring(pos));

        xml.writeEndElement();
    }

    /**
     * Replaces \n with platform specific end of lines
     * @param text The string to be converted
     * @return The converted string
     */
    private String platformLineSeparator(String text) {
        return text.replace("\n", lineSeparator);
    }
}
