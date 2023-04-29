/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura.
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

package org.omegat.util;

import javax.xml.stream.XMLInputFactory;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

public final class JaxbXmlMapper {

    private static final XmlMapper mapper;

    private JaxbXmlMapper() {
    }

    static {
        JacksonXmlModule xmlModule = new JacksonXmlModule();
        xmlModule.setDefaultUseWrapper(false);
        XmlFactory xmlFactory = new XmlFactory();
        XMLInputFactory xmlIn = xmlFactory.getXMLInputFactory();
        xmlIn.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
        mapper = new XmlMapper(xmlFactory, xmlModule);
        mapper.registerModule(new JaxbAnnotationModule());
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static XmlMapper getXmlMapper() {
        return mapper;
    }
}
