/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.externalfinder.item;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Aaron Madlon-Kay
 */
public class ExternalFinderXMLWriter {

    private final File file;

    public ExternalFinderXMLWriter(File file) {
        this.file = Objects.requireNonNull(file);
    }

    public void write(ExternalFinderConfiguration config) throws Exception {
        Document doc = createDocument(config);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    private Document createDocument(ExternalFinderConfiguration config) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        // items
        Element root = doc.createElement("items");
        doc.appendChild(root);

        // priority
        if (config.getPriority() >= 0) {
            root.setAttribute("priority", Integer.toString(config.getPriority()));
        }

        for (ExternalFinderItem i : config.getItems()) {
            // item
            root.appendChild(createItem(doc, i));
        }

        return doc;
    }

    private Element createItem(Document doc, ExternalFinderItem i) {
        Element item = doc.createElement("item");

        // nopopup
        if (i.isNopopup()) {
            item.setAttribute("nopopup", Boolean.TRUE.toString());
        }
        {
            // name
            Element name = doc.createElement("name");
            name.setTextContent(i.getName());
            item.appendChild(name);
        }
        for (ExternalFinderItemURL u : i.getURLs()) {
            // url
            item.appendChild(createUrl(doc, u));
        }
        for (ExternalFinderItemCommand c : i.getCommands()) {
            // command
            item.appendChild(createCommand(doc, c));
        }
        {
            // keystroke
            if (i.getKeystroke() != null) {
                Element keystroke = doc.createElement("keystroke");
                keystroke.setTextContent(i.getKeystroke().toString());
                item.appendChild(keystroke);
            }
        }

        return item;
    }

    private Element createUrl(Document doc, ExternalFinderItemURL u) {
        Element url = doc.createElement("url");

        // target
        if (u.getTarget() != null) {
            url.setAttribute("target", u.getTarget().name().toLowerCase(Locale.ENGLISH));
        }

        // encoding
        if (u.getEncoding() != null) {
            url.setAttribute("encoding", u.getEncoding().name().toLowerCase(Locale.ENGLISH));
        }

        url.setTextContent(u.getURL());

        return url;
    }

    private Element createCommand(Document doc, ExternalFinderItemCommand c) {
        Element command = doc.createElement("command");

        // target
        if (c.getTarget() != null) {
            command.setAttribute("target", c.getTarget().name().toLowerCase(Locale.ENGLISH));
        }

        // encoding
        if (c.getEncoding() != null) {
            command.setAttribute("encoding", c.getEncoding().name().toLowerCase(Locale.ENGLISH));
        }

        command.setAttribute("delimiter", c.getDelimiter());

        command.setTextContent(c.getCommand());

        return command;
    }
}
