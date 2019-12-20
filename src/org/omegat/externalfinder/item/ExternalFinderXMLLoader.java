/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Chihiro Hio
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;

import javax.swing.KeyStroke;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.omegat.externalfinder.item.ExternalFinderItem.SCOPE;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExternalFinderXMLLoader implements IExternalFinderItemLoader {

    private static final Logger LOGGER = Logger.getLogger(ExternalFinderXMLLoader.class.getName());

    private final File file;
    private final SCOPE scope;

    public ExternalFinderXMLLoader(File file, SCOPE scope) {
        this.file = Objects.requireNonNull(file);
        this.scope = scope;
    }

    @Override
    public ExternalFinderConfiguration load() throws Exception {

        List<ExternalFinderItem> finderItems = new ArrayList<ExternalFinderItem>();
        int priority = -1;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        NodeList nodeList = document.getElementsByTagName("item");
        if (nodeList == null) {
            return ExternalFinderConfiguration.empty();
        }

        for (int i = 0, n = nodeList.getLength(); i < n; i++) {
            try {
                finderItems.add(generateFinderItem(nodeList.item(i)));
            } catch (ExternalFinderValidationException ex) {
                LOGGER.warning("ExternalFinder: " + ex.getMessage());
            }
        }

        priority = retrivePriority(document, priority);

        return new ExternalFinderConfiguration(priority, finderItems);
    }

    private static int retrivePriority(final Document document, final int defaultPriority) {
        int priority = defaultPriority;

        NodeList items = document.getElementsByTagName("items");
        if (items == null || items.getLength() != 1) {
            return priority;
        }

        Node item = items.item(0);
        if (!item.hasAttributes()) {
            return priority;
        }

        Node namedItem = item.getAttributes().getNamedItem("priority");
        if (namedItem == null) {
            return priority;
        }

        String value = namedItem.getTextContent();
        try {
            priority = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            // ignore
        }

        return priority;
    }

    private ExternalFinderItem generateFinderItem(Node item) {
        if (!item.hasChildNodes()) {
            return null;
        }
        final NodeList childNodes = item.getChildNodes();

        ExternalFinderItem.Builder builder = new ExternalFinderItem.Builder();
        builder.setScope(scope);

        // retrive popup
        if (item.hasAttributes()) {
            Node nopopup = item.getAttributes().getNamedItem("nopopup");
            if (nopopup != null) {
                String value = nopopup.getTextContent();
                if (value.equals("true")) {
                    builder.setNopopup(true);
                }
            }
        }

        for (int i = 0, n = childNodes.getLength(); i < n; i++) {
            final Node childNode = childNodes.item(i);

            final String nodeName = childNode.getNodeName();
            if (nodeName.equals("name")) {
                builder.setName(childNode.getTextContent());
            } else if (nodeName.equals("url")) {
                try {
                    builder.addURL(generateFinderURL(childNode));
                } catch (ExternalFinderValidationException ex) {
                    LOGGER.warning("ExternalFinder: " + ex.getMessage());
                }
            } else if (nodeName.equals("command")) {
                try {
                    builder.addCommand(generateFinderCommand(childNode));
                } catch (ExternalFinderValidationException ex) {
                    LOGGER.warning("ExternalFinder: " + ex.getMessage());
                }
            } else if (nodeName.equals("keystroke")) {
                KeyStroke keyStroke = KeyStroke.getKeyStroke(childNode.getTextContent());
                builder.setKeyStroke(keyStroke);
            }
        }

        return builder.build();
    }

    private static ExternalFinderItemURL generateFinderURL(Node urlNode) {
        ExternalFinderItemURL.Builder builder = new ExternalFinderItemURL.Builder();
        builder.setURL(urlNode.getTextContent());

        if (urlNode.hasAttributes()) {

            Node tAttribute = urlNode.getAttributes().getNamedItem("target");
            if (tAttribute != null) {
                String targetAttribute = tAttribute.getTextContent().toLowerCase(Locale.ENGLISH);
                if (targetAttribute.equals("ascii_only")) {
                    builder.setTarget(ExternalFinderItem.TARGET.ASCII_ONLY);
                } else if (targetAttribute.equals("non_ascii_only")) {
                    builder.setTarget(ExternalFinderItem.TARGET.NON_ASCII_ONLY);
                }
            }

            Node eAttribute = urlNode.getAttributes().getNamedItem("encoding");
            if (eAttribute != null) {
                String encodingAttribute = eAttribute.getTextContent().toLowerCase(Locale.ENGLISH);
                if (encodingAttribute.equals("escape")) {
                    builder.setEncoding(ExternalFinderItem.ENCODING.ESCAPE);
                } else if (encodingAttribute.equals("none")) {
                    builder.setEncoding(ExternalFinderItem.ENCODING.NONE);
                }
            }
        }

        return builder.build();
    }

    private static ExternalFinderItemCommand generateFinderCommand(Node commandNode) {
        ExternalFinderItemCommand.Builder builder = new ExternalFinderItemCommand.Builder();
        builder.setCommand(commandNode.getTextContent());

        if (commandNode.hasAttributes()) {

            Node tAttribute = commandNode.getAttributes().getNamedItem("target");
            if (tAttribute != null) {
                String targetAttribute = tAttribute.getTextContent().toLowerCase(Locale.ENGLISH);
                if (targetAttribute.equals("ascii_only")) {
                    builder.setTarget(ExternalFinderItem.TARGET.ASCII_ONLY);
                } else if (targetAttribute.equals("non_ascii_only")) {
                    builder.setTarget(ExternalFinderItem.TARGET.NON_ASCII_ONLY);
                }
            }

            Node eAttribute = commandNode.getAttributes().getNamedItem("encoding");
            if (eAttribute != null) {
                String encodingAttribute = eAttribute.getTextContent().toLowerCase(Locale.ENGLISH);
                if (encodingAttribute.equals("default")) {
                    builder.setEncoding(ExternalFinderItem.ENCODING.DEFAULT);
                } else if (encodingAttribute.equals("escape")) {
                    builder.setEncoding(ExternalFinderItem.ENCODING.ESCAPE);
                }
            }

            Node dAttribute = commandNode.getAttributes().getNamedItem("delimiter");
            if (dAttribute != null) {
                builder.setDelimiter(dAttribute.getTextContent());
            }
        }

        return builder.build();
    }
}
