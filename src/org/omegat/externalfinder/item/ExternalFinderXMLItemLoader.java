/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Chihiro Hio
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.KeyStroke;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ExternalFinderXMLItemLoader implements IExternalFinderItemLoader {

    private final File file;

    public ExternalFinderXMLItemLoader(File file) {
        this.file = file;
    }

    @Override
    public List<ExternalFinderItem> load() {
        final List<ExternalFinderItem> finderItems = new ArrayList<ExternalFinderItem>();
        if (file == null) {
            return finderItems;
        }

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);
            NodeList nodeList = document.getElementsByTagName("item");
            if (nodeList == null) {
                return finderItems;
            }

            for (int i = 0, n = nodeList.getLength(); i < n; i++) {
                ExternalFinderItem finderItem = generateFinderItem(nodeList.item(i));

                if (finderItem != null
                        && finderItem.getName() != null
                        && !finderItem.getName().isEmpty()
                        && !finderItems.contains(finderItem)) {
                    finderItems.add(finderItem);
                }
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ExternalFinderXMLItemLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ExternalFinderXMLItemLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExternalFinderXMLItemLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return finderItems;
    }

    @Override
    public int loadPopupPriority(int defaultPriority) {
        int priority = defaultPriority;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);

            priority = retrivePriority(document, priority);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ExternalFinderXMLItemLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ExternalFinderXMLItemLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExternalFinderXMLItemLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return priority;
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

    private static ExternalFinderItem generateFinderItem(Node item) {
        if (!item.hasChildNodes()) {
            return null;
        }
        final NodeList childNodes = item.getChildNodes();

        final ExternalFinderItem finderItem = new ExternalFinderItem();

        // retrive popup
        if (item.hasAttributes()) {
            Node nopopup = item.getAttributes().getNamedItem("nopopup");
            if (nopopup != null) {
                String value = nopopup.getTextContent();
                if (value.equals("true")) {
                    finderItem.setNopopup(true);
                }
            }
        }

        for (int i = 0, n = childNodes.getLength(); i < n; i++) {
            final Node childNode = childNodes.item(i);

            final String nodeName = childNode.getNodeName();
            if (nodeName.equals("name")) {
                finderItem.setName(childNode.getTextContent());
            } else if (nodeName.equals("url")) {
                finderItem.getURLs().add(generateFinderURL(childNode));
            } else if (nodeName.equals("command")) {
                finderItem.getCommands().add(generateFinderCommand(childNode));
            } else if (nodeName.equals("keystroke")) {
                KeyStroke keyStroke = KeyStroke.getKeyStroke(childNode.getTextContent());
                finderItem.setKeystroke(keyStroke);
            }
        }

        return finderItem;
    }

    private static ExternalFinderItemURL generateFinderURL(Node urlNode) {
        String url = urlNode.getTextContent();
        ExternalFinderItem.TARGET target = ExternalFinderItem.TARGET.BOTH;
        ExternalFinderItem.ENCODING encoding = ExternalFinderItem.ENCODING.DEFAULT;

        if (urlNode.hasAttributes()) {

            Node tAttribute = urlNode.getAttributes().getNamedItem("target");
            if (tAttribute != null) {
                String targetAttribute = tAttribute.getTextContent().toLowerCase();
                if (targetAttribute.equals("ascii_only")) {
                    target = ExternalFinderItem.TARGET.ASCII_ONLY;
                } else if (targetAttribute.equals("non_ascii_only")) {
                    target = ExternalFinderItem.TARGET.NON_ASCII_ONLY;
                }
            }

            Node eAttribute = urlNode.getAttributes().getNamedItem("encoding");
            if (eAttribute != null) {
                String encodingAttribute = eAttribute.getTextContent().toLowerCase();
                if (encodingAttribute.equals("escape")) {
                    encoding = ExternalFinderItem.ENCODING.ESCAPE;
                } else if (encodingAttribute.equals("none")) {
                    encoding = ExternalFinderItem.ENCODING.NONE;
                }
            }
        }

        return new ExternalFinderItemURL(url, target, encoding);
    }

    private static ExternalFinderItemCommand generateFinderCommand(Node commandNode) {
        String command = commandNode.getTextContent();
        ExternalFinderItem.TARGET target = ExternalFinderItem.TARGET.BOTH;
        ExternalFinderItem.ENCODING encoding = ExternalFinderItem.ENCODING.NONE;
        String delimiter = "|";

        if (commandNode.hasAttributes()) {

            Node tAttribute = commandNode.getAttributes().getNamedItem("target");
            if (tAttribute != null) {
                String targetAttribute = tAttribute.getTextContent().toLowerCase();
                if (targetAttribute.equals("ascii_only")) {
                    target = ExternalFinderItem.TARGET.ASCII_ONLY;
                } else if (targetAttribute.equals("non_ascii_only")) {
                    target = ExternalFinderItem.TARGET.NON_ASCII_ONLY;
                }
            }

            Node eAttribute = commandNode.getAttributes().getNamedItem("encoding");
            if (eAttribute != null) {
                String encodingAttribute = eAttribute.getTextContent().toLowerCase();
                if (encodingAttribute.equals("default")) {
                    encoding = ExternalFinderItem.ENCODING.DEFAULT;
                } else if (encodingAttribute.equals("escape")) {
                    encoding = ExternalFinderItem.ENCODING.ESCAPE;
                }
            }

            Node dAttribute = commandNode.getAttributes().getNamedItem("delimiter");
            if (dAttribute != null) {
                delimiter = dAttribute.getTextContent();
            }
        }

        return new ExternalFinderItemCommand(command, target, encoding, delimiter);
    }
}
