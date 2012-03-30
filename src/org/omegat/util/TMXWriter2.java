/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2006 Henry Pijffers
               2010 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/
package org.omegat.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
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
 */
public class TMXWriter2 {
    private static XMLOutputFactory FACTORY;

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
        xml = FACTORY.createXMLStreamWriter(out, OConsts.UTF8);

        xml.writeStartDocument(OConsts.UTF8, "1.0");
        xml.writeCharacters(FileUtil.LINE_SEPARATOR);

        if (levelTwo) {
            xml.writeDTD("<!DOCTYPE tmx SYSTEM \"tmx14.dtd\">");
            xml.writeCharacters(FileUtil.LINE_SEPARATOR);
            xml.writeStartElement("tmx");
            xml.writeAttribute("version", "1.4");
        } else {
            xml.writeDTD("<!DOCTYPE tmx SYSTEM \"tmx11.dtd\">");
            xml.writeCharacters(FileUtil.LINE_SEPARATOR);
            xml.writeStartElement("tmx");
            xml.writeAttribute("version", "1.1");
        }
        xml.writeCharacters(FileUtil.LINE_SEPARATOR);

        writeHeader(sourceLanguage, targetLanguage, sentenceSegmentingEnabled);

        xml.writeCharacters("  ");
        xml.writeStartElement("body");
        xml.writeCharacters(FileUtil.LINE_SEPARATOR);

        langSrc = sourceLanguage.toString();
        langTar = targetLanguage.toString();

        tmxDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        tmxDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void close() throws Exception {
        try {
            xml.writeCharacters("  ");
            xml.writeEndElement(); // body

            xml.writeCharacters(FileUtil.LINE_SEPARATOR);
            xml.writeEndElement(); // tmx

            xml.writeCharacters(FileUtil.LINE_SEPARATOR);
            xml.writeEndDocument();
        } finally {
            xml.close();
            out.close();
        }
    }

    public void writeComment(String comment) throws Exception {
        xml.writeComment(comment);
        xml.writeCharacters(FileUtil.LINE_SEPARATOR);
    }

    /**
     * Write one entry.
     * 
     * @param source
     * @param translation
     * @param propValues
     *            pairs with property name and values
     */
    public void writeEntry(String source, String translation, TMXEntry entry, String[] propValues)
            throws Exception {
        xml.writeCharacters("    ");
        xml.writeStartElement("tu");
        xml.writeCharacters(FileUtil.LINE_SEPARATOR);

        // add properties
        if (propValues != null) {
            for (int i = 0; i < propValues.length; i += 2) {
                if (propValues[i + 1] == null) {
                    // value is null - not need to write
                    continue;
                }
                xml.writeCharacters("      ");
                xml.writeStartElement("prop");
                xml.writeAttribute("type", propValues[i]);
                xml.writeCharacters(propValues[i + 1]);
                xml.writeEndElement(); // prop
                xml.writeCharacters(FileUtil.LINE_SEPARATOR);
            }
        }
        
        // add note
        if (entry.note != null && !entry.note.equals("")) {
            String note = StaticUtils.fixChars(entry.note);
            if (forceValidTMX) {
                note = StaticUtils.stripTags(note);
            }
            xml.writeCharacters("      ");
            xml.writeStartElement("note");
            xml.writeCharacters(note);
            xml.writeEndElement(); // note
            xml.writeCharacters(FileUtil.LINE_SEPARATOR);
        }

        // write source segment
        source = StaticUtils.fixChars(source);
        if (forceValidTMX) {
            source = StaticUtils.stripTags(source);
        }
        xml.writeCharacters("      ");
        xml.writeStartElement("tuv");
        if (levelTwo) {
            xml.writeAttribute("xml", "", "lang", langSrc);
        } else {
            xml.writeAttribute("lang", langSrc);
        }
        xml.writeCharacters(FileUtil.LINE_SEPARATOR);
        if (levelTwo) {
            writeLevelTwo(source);
        } else {
            writeLevelOne(source);
        }
        xml.writeCharacters(FileUtil.LINE_SEPARATOR);
        xml.writeCharacters("      ");
        xml.writeEndElement(); // tuv
        xml.writeCharacters(FileUtil.LINE_SEPARATOR);

        // write target segment
        if (translation != null) {
            translation = StaticUtils.fixChars(translation);
            if (forceValidTMX) {
                translation = StaticUtils.stripTags(translation);
            }

            xml.writeCharacters("      ");
            xml.writeStartElement("tuv");
            if (levelTwo) {
                xml.writeAttribute("xml", "", "lang", langTar);
            } else {
                xml.writeAttribute("lang", langTar);
            }
            if (!StringUtil.isEmpty(entry.changer)) {
                xml.writeAttribute("changeid", entry.changer);
            }
            if (entry.changeDate > 0) {
                xml.writeAttribute("changedate", tmxDateFormat.format(new Date(entry.changeDate)));
            }
            xml.writeCharacters(FileUtil.LINE_SEPARATOR);

            if (levelTwo) {
                writeLevelTwo(translation);
            } else {
                writeLevelOne(translation);
            }
            xml.writeCharacters(FileUtil.LINE_SEPARATOR);
            xml.writeCharacters("      ");
            xml.writeEndElement(); // tuv
            xml.writeCharacters(FileUtil.LINE_SEPARATOR);
        }

        xml.writeCharacters("    ");
        xml.writeEndElement(); // tu
        xml.writeCharacters(FileUtil.LINE_SEPARATOR);
    }

    private void writeHeader(final Language sourceLanguage, final Language targetLanguage,
            boolean sentenceSegmentingEnabled) throws Exception {
        xml.writeCharacters("  ");
        xml.writeEmptyElement("header");

        xml.writeAttribute("creationtool", "OmegaT");
        xml.writeAttribute("o-tmf", "OmegaT TMX");
        xml.writeAttribute("adminlang", "EN-US");
        xml.writeAttribute("datatype", "plaintext");

        String version = OStrings.VERSION;
        if (!OStrings.UPDATE.equals("0"))
            version = version + "_" + OStrings.UPDATE;

        xml.writeAttribute("creationtoolversion", version);

        xml.writeAttribute("segtype", sentenceSegmentingEnabled ? TMXReader.SEG_SENTENCE
                : TMXReader.SEG_PARAGRAPH);

        xml.writeAttribute("srclang", sourceLanguage.toString());

        xml.writeCharacters(FileUtil.LINE_SEPARATOR);
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

            if (m.group(1).length() > 0) {
                tagType = TAG_TYPE.END;
            } else if (m.group(4).length() > 0) {
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
}
