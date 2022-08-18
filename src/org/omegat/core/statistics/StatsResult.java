/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2010 Arno Peters
               2013-2014 Alex Buloichik
               2015 Aaron Madlon-Kay
               2020 Vladimir Bychkov
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

package org.omegat.core.statistics;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.omegat.core.data.ProjectProperties;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.TextUtil;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Vladimir Bychkov
 */
public class StatsResult {
    public static final String[] HT_HEADERS = {
        "",
        OStrings.getString("CT_STATS_Segments"),
        OStrings.getString("CT_STATS_Words"),
        OStrings.getString("CT_STATS_Characters_NOSP"),
        OStrings.getString("CT_STATS_Characters"),
        OStrings.getString("CT_STATS_Files"),
    };

    private static final String[] HT_ROWS = {
        OStrings.getString("CT_STATS_Total"),
        OStrings.getString("CT_STATS_Remaining"),
        OStrings.getString("CT_STATS_Unique"),
        OStrings.getString("CT_STATS_Unique_Remaining"),
    };

    private static final boolean[] HT_ALIGN = new boolean[] { false, true, true, true, true, true };

    public static final String[] FT_HEADERS = {
        OStrings.getString("CT_STATS_FILE_Name"),
        OStrings.getString("CT_STATS_FILE_Total_Segments"),
        OStrings.getString("CT_STATS_FILE_Remaining_Segments"),
        OStrings.getString("CT_STATS_FILE_Unique_Segments"),
        OStrings.getString("CT_STATS_FILE_Unique_Remaining_Segments"),
        OStrings.getString("CT_STATS_FILE_Total_Words"),
        OStrings.getString("CT_STATS_FILE_Remaining_Words"),
        OStrings.getString("CT_STATS_FILE_Unique_Words"),
        OStrings.getString("CT_STATS_FILE_Unique_Remaining_Words"),
        OStrings.getString("CT_STATS_FILE_Total_Characters_NOSP"),
        OStrings.getString("CT_STATS_FILE_Remaining_Characters_NOSP"),
        OStrings.getString("CT_STATS_FILE_Unique_Characters_NOSP"),
        OStrings.getString("CT_STATS_FILE_Unique_Remaining_Characters_NOSP"),
        OStrings.getString("CT_STATS_FILE_Total_Characters"),
        OStrings.getString("CT_STATS_FILE_Remaining_Characters"),
        OStrings.getString("CT_STATS_FILE_Unique_Characters"),
        OStrings.getString("CT_STATS_FILE_Unique_Remaining_Characters"),
    };

    private static final boolean[] FT_ALIGN = { false, true, true, true, true, true, true, true, true, true, true, true,
            true, true, true, true, true, };

    private StatCount total = new StatCount();
    private StatCount remaining = new StatCount();
    private StatCount unique = new StatCount();
    private StatCount remainingUnique = new StatCount();

    private Set<String> translated;
    private List<FileData> counts;

    public StatsResult(StatCount total, StatCount remaining, StatCount unique, StatCount remainingUnique,
            Set<String> translated, List<FileData> counts) {
        this.total = total;
        this.remaining = remaining;
        this.unique = unique;
        this.remainingUnique = remainingUnique;

        this.translated = translated;
        this.counts = counts;
    }

    public void updateStatisticsInfo(StatisticsInfo hotStat) {
        hotStat.numberOfSegmentsTotal = total.segments;
        hotStat.numberOfTranslatedSegments = translated.size();
        hotStat.numberOfUniqueSegments = unique.segments;
        hotStat.uniqueCountsByFile.clear();
        for (FileData fd : counts) {
            hotStat.uniqueCountsByFile.put(fd.filename, fd.unique.segments);
        }
    }

    public StatCount getTotal() {
        return total;
    }

    public StatCount getRemaining() {
        return remaining;
    }

    public StatCount getUnique() {
        return unique;
    }

    public StatCount getRemainingUnique() {
        return remainingUnique;
    }

    public List<FileData> getCounts() {
        return counts;
    }

    public String getTextData(final ProjectProperties config) {
        StringBuilder result = new StringBuilder();

        result.append(OStrings.getString("CT_STATS_Project_Statistics"));
        result.append("\n\n");

        result.append(TextUtil.showTextTable(HT_HEADERS, getHeaderTable(), HT_ALIGN));
        result.append("\n\n");

        // STATISTICS BY FILE
        result.append(OStrings.getString("CT_STATS_FILE_Statistics"));
        result.append("\n\n");
        result.append(TextUtil.showTextTable(FT_HEADERS, getFilesTable(config), FT_ALIGN));
        return result.toString();
    }

    public String getXmlData(final ProjectProperties props) throws XMLStreamException {

        StringWriter result = new StringWriter();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        XMLStreamWriter xml = XMLOutputFactory.newInstance().createXMLStreamWriter(result);

        xml.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
        xml.writeCharacters(System.lineSeparator());

        xml.writeStartElement("omegat-stats");
        xml.writeAttribute("date", dateFormat.format(new Date()));
        xml.writeCharacters(System.lineSeparator());

        xml.writeStartElement("project");
        xml.writeAttribute("name", props.getProjectName());
        xml.writeAttribute("root", props.getProjectRoot());
        xml.writeAttribute("source", props.getSourceLanguage().toString());
        xml.writeAttribute("target", props.getTargetLanguage().toString());
        xml.writeCharacters(System.lineSeparator());

        // Header stats
        String[][] headerTable = getHeaderTable();
        String[] headers = { "segments", "words", "characters-nosp", "characters" };
        String[] attrs = { "total", "remaining", "unique", "unique-remaining" };

        for (int h = 0; h < headers.length; h++) {
            xml.writeEmptyElement(headers[h]);

            for (int a = 1; a < attrs.length; a++) {
                xml.writeAttribute(attrs[a], headerTable[h][a]);
            }
            xml.writeCharacters(System.lineSeparator());
        }
        xml.writeEndElement();
        xml.writeCharacters(System.lineSeparator());

        // STATISTICS BY FILE
        xml.writeStartElement("files");
        xml.writeCharacters(System.lineSeparator());

        String[] fileAttrs = { "name", "total-segments", "remaining-segments", "unique-segments",
                "unique-remaining-segments", "total-words", "remaining-words", "unique-words", "unique-remaining-words",
                "total-characters-nosp", "remaining-characters-nosp", "unique-characters-nosp",
                "unique-remaining-characters-nosp", "total-characters", "remaining-characters", "unique-characters",
                "unique-remaining-characters" };

        String[][] filesTable = getFilesTable(props);
        for (int f = 0; f < filesTable.length; f++) {
            xml.writeStartElement("file");
            xml.writeAttribute(fileAttrs[0], filesTable[f][0]); // name
            xml.writeCharacters(System.lineSeparator());
            for (int h = 0; h < headers.length; h++) {
                xml.writeEmptyElement(headers[h]);

                for (int a = 0; a < attrs.length; a++) {
                    xml.writeAttribute(attrs[a], filesTable[f][1 + a + (h * attrs.length)]);
                }
                xml.writeCharacters(System.lineSeparator());
            }
            xml.writeEndElement();

            xml.writeCharacters(System.lineSeparator());
        }

        xml.writeEndElement();

        xml.writeEndElement();
        xml.writeEndDocument();
        xml.close();

        return result.toString();
    }

    public String[][] getHeaderTable() {
        StatCount[] result = new StatCount[] { total, remaining, unique, remainingUnique };
        String[][] table = new String[result.length][6];

        for (int i = 0; i < result.length; i++) {
            table[i][0] = HT_ROWS[i];
            table[i][1] = Integer.toString(result[i].segments);
            table[i][2] = Integer.toString(result[i].words);
            table[i][3] = Integer.toString(result[i].charsWithoutSpaces);
            table[i][4] = Integer.toString(result[i].charsWithSpaces);
            table[i][5] = Integer.toString(result[i].files);
        }
        return table;
    }

    public String[][] getFilesTable(final ProjectProperties config) {
        String[][] table = new String[counts.size()][17];

        int r = 0;
        for (FileData numbers : counts) {
            table[r][0] = StaticUtils.makeFilenameRelative(numbers.filename, config.getSourceRoot());
            table[r][1] = Integer.toString(numbers.total.segments);
            table[r][2] = Integer.toString(numbers.remaining.segments);
            table[r][3] = Integer.toString(numbers.unique.segments);
            table[r][4] = Integer.toString(numbers.remainingUnique.segments);
            table[r][5] = Integer.toString(numbers.total.words);
            table[r][6] = Integer.toString(numbers.remaining.words);
            table[r][7] = Integer.toString(numbers.unique.words);
            table[r][8] = Integer.toString(numbers.remainingUnique.words);
            table[r][9] = Integer.toString(numbers.total.charsWithoutSpaces);
            table[r][10] = Integer.toString(numbers.remaining.charsWithoutSpaces);
            table[r][11] = Integer.toString(numbers.unique.charsWithoutSpaces);
            table[r][12] = Integer.toString(numbers.remainingUnique.charsWithoutSpaces);
            table[r][13] = Integer.toString(numbers.total.charsWithSpaces);
            table[r][14] = Integer.toString(numbers.remaining.charsWithSpaces);
            table[r][15] = Integer.toString(numbers.unique.charsWithSpaces);
            table[r][16] = Integer.toString(numbers.remainingUnique.charsWithSpaces);
            r++;
        }
        return table;
    }
}
