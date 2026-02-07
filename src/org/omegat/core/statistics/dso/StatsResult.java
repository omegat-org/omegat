/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2010 Arno Peters
               2013-2014 Alex Buloichik
               2015 Aaron Madlon-Kay
               2020 Vladimir Bychkov
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

package org.omegat.core.statistics.dso;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @author Vladimir Bychkov
 */
@XmlRootElement(name = "omegat-stats")
public class StatsResult {

    @JsonProperty("project")
    private StatProjectProperties props;

    private StatCount total;
    private StatCount remaining;
    private StatCount unique;
    @JsonProperty("unique-remaining")
    private StatCount remainingUnique;

    private Set<String> translated;
    @JsonProperty("files")
    private List<FileData> counts;

    @JsonProperty("date")
    private String date;

    public StatsResult(String projectName, String projectRoot, String sourceLanguage, String targetLanguage,
            String sourceRoot) {
        props = new StatProjectProperties(projectName, projectRoot, sourceLanguage, targetLanguage,
                sourceRoot);
        total = new StatCount();
        remaining = new StatCount();
        unique = new StatCount();
        remainingUnique = new StatCount();
        translated = new HashSet<>();
        counts = new ArrayList<>();
    }

    /**
     * Constructor.
     * 
     * @param total
     *            total number of statistics.
     * @param remaining
     *            remaining translations.
     * @param unique
     *            unique translations.
     * @param remainingUnique
     *            remaining and unique translations.
     * @param translated
     *            translated segments.
     * @param counts
     *            file counts.
     */
    public StatsResult(StatCount total, StatCount remaining, StatCount unique, StatCount remainingUnique,
            Set<String> translated, List<FileData> counts) {
        props = new StatProjectProperties();
        this.total = total;
        this.remaining = remaining;
        this.unique = unique;
        this.remainingUnique = remainingUnique;

        this.translated = translated;
        this.counts = counts;
    }

    /**
     * Update given hosStat with current stats data.
     * 
     * @param hotStat
     *            StatisticsInfo data object.
     */
    public void updateStatisticsInfo(StatisticsInfo hotStat) {
        hotStat.numberOfSegmentsTotal = total.segments;
        hotStat.numberOfTranslatedSegments = translated.size();
        hotStat.numberOfUniqueSegments = unique.segments;
        hotStat.uniqueCountsByFile.clear();
        for (FileData fd : counts) {
            hotStat.uniqueCountsByFile.put(fd.filename, fd.unique.segments);
        }
    }

    @XmlElement(name = "date")
    public String getDate() {
        return date;
    }

    @XmlElement(name = "project")
    public StatProjectProperties getProps() {
        return props;
    }

    /**
     * Return total number of segments.
     */
    @XmlElement(name = "total")
    public StatCount getTotal() {
        return total;
    }

    /**
     * Return remaining number of segments that needs translation.
     */
    @XmlElement(name = "remaining")
    public StatCount getRemaining() {
        return remaining;
    }

    /**
     * Return a number of unique segments.
     */
    @XmlElement(name = "unique")
    public StatCount getUnique() {
        return unique;
    }

    /**
     * Return a number of remaining unique segments.
     */
    @XmlElement(name = "unique-remaining")
    public StatCount getRemainingUnique() {
        return remainingUnique;
    }

    /**
     * return a statistics of each source/target files.
     */
    public List<FileData> getCounts() {
        return counts;
    }

    /**
     * Return JSON expression of stats data.
     * 
     * @return JSON string data.
     * @throws IOException
     *             when export failed.
     */
    @JsonIgnore
    public String getJsonData() throws IOException {
        setDate();
        StringWriter result = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        SequenceWriter writer = mapper.writer().writeValues(result);
        writer.write(this);
        writer.close();
        return result.toString();
    }

    /**
     * Return XML expression of Stats data.
     * 
     * @return XML expression of stats data as String.
     */
    @JsonIgnore
    public String getXmlData() throws JsonProcessingException {
        setDate();
        XmlMapper xmlMapper = XmlMapper.builder().addModule(new JakartaXmlBindAnnotationModule())
                .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
                .enable(SerializationFeature.INDENT_OUTPUT).defaultUseWrapper(false).build();
        return xmlMapper.writeValueAsString(this);

    }

    private void setDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        date = dateFormat.format(new Date());
    }

}
