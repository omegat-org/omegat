/*
 * OmegaT - Computer Assisted Translation (CAT) tool
 *          with fuzzy matching, translation memory, keyword search,
 *          glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2024-2026 Hiroshi Miura
 *                Home page: https://www.omegat.org/
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
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.core.statistics.writer;

import java.io.IOException;
import java.io.Writer;

import org.omegat.core.statistics.dso.StatsResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;

/**
 * Save project statistic into XML file.
 *
 * @author Hiroshi Miura
 */
public class StatisticsXmlWriter extends AbstractStatisticsWriter {

    @Override
    public void write(StatsResult result, Writer out) throws IOException {
        out.write(getXmlData(result));
    }

    public String getXmlData(StatsResult result) throws JsonProcessingException {
        setDate(result);
        XmlMapper xmlMapper = XmlMapper.builder().addModule(new JakartaXmlBindAnnotationModule())
                .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
                .enable(SerializationFeature.INDENT_OUTPUT).defaultUseWrapper(false).build();
        return xmlMapper.writeValueAsString(result);
    }
}
