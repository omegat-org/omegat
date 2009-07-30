/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik

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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.filters3.xml.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Filter for Android resources.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class AndroidFilter extends AbstractFilter {

    /** DOM factory. */
    protected static final DocumentBuilderFactory DBF;

    static {
        DBF = DocumentBuilderFactory.newInstance();
        DBF.setNamespaceAware(true);
    }

    public AndroidFilter() {
    }

    /** Returns true if it's Android's resource file. */
    public boolean isFileSupported(File inFile, String inEncoding) {
        try {
            process(inFile, null);
            return true;
        } catch (FormatException ex) {
            return false;
        } catch (Exception ex) {
            return true;
        }
    }

    /** Human-readable filter name. */
    public String getFileFormatName() {
        return OStrings.getString("Android_FILTER_NAME");
    }

    /** Extensions... */
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.xml") };
    }

    /** Source encoding can not be varied by the user. */
    public boolean isSourceEncodingVariable() {
        return false;
    }

    /** Target encoding can not be varied by the user. */
    public boolean isTargetEncodingVariable() {
        return false;
    }

    /**
     * Process file.
     */
    public List<File> processFile(File inFile, String inEncoding, File outFile,
            String outEncoding) throws IOException, TranslationException {
        try {
            process(inFile, outFile);
        } catch (Exception ex) {
            Log.log(ex);
            throw new IOException("Error read file");
        }
        return null;
    }

    /** Not implemented. */
    protected void processFile(BufferedReader inFile, BufferedWriter outFile)
            throws IOException, TranslationException {
        throw new IOException("Not Implemented!"); // NOI18N
    }

    /**
     * Real file processing.
     * 
     * @param in
     *            input file
     * @param out
     *            output file
     * @throws Exception
     */
    protected void process(File in, File out) throws Exception {
        DocumentBuilder db = DBF.newDocumentBuilder();

        // parse input file into DOM
        Document doc = db.parse(in);

        // check tags
        if (!isNamed(doc.getDocumentElement(), null, "resources")) {
            throw new FormatException();
        }

        for (Node nstr = doc.getDocumentElement().getFirstChild(); nstr != null; nstr = nstr
                .getNextSibling()) {
            if (nstr.getNodeType() == Node.ELEMENT_NODE) {
                if (isNamed(nstr, null, "string")) {
                    // for the "string" nodes
                    ini = 1;
                    StringBuilder v = new StringBuilder(256);
                    Map<String, Node> nodes = new TreeMap<String, Node>();
                    collect(nstr, v, nodes);

                    String trans = processEntry(v.toString());
                    if (out != null) {
                        // create new node with translation
                        Node newNode = apply(trans, nodes);
                        newNode = doc.adoptNode(newNode);
                        copyNodeNameAndAttrs(doc, nstr, newNode);
                        // replace original node in document into node with
                        // translation
                        nstr.getParentNode().replaceChild(newNode, nstr);
                        nstr = newNode;
                    }
                } else if (isNamed(nstr, null, "string-array") || isNamed(nstr, null, "plurals")) {
                    for (Node nit = nstr.getFirstChild(); nit != null; nit = nit
                            .getNextSibling()) {
                        if (nit.getNodeType() == Node.ELEMENT_NODE) {
                            // items of string nodes
                            if (isNamed(nit, null, "item")) {
                                ini = 1;
                                Map<String, Node> nodes = new TreeMap<String, Node>();
                                StringBuilder v = new StringBuilder(256);
                                collect(nit, v, nodes);

                                String trans = processEntry(v.toString());
                                if (out != null) {
                                    // create new node with translation
                                    Node newNode = apply(trans, nodes);
                                    newNode = doc.adoptNode(newNode);
                                    copyNodeNameAndAttrs(doc, nit, newNode);
                                    // replace original node in document into
                                    // node with translation
                                    nit.getParentNode().replaceChild(newNode,
                                            nit);
                                    nit = newNode;
                                }
                            } else {
                                throw new FormatException("Invalid tag: "
                                        + nit.getLocalName());
                            }
                        }
                    }               
                } else if (isNamed(nstr, null, "skip")) {
                } else if (isNamed(nstr, null, "color")) {
                } else {
                    throw new FormatException("Invalid tag: "
                            + nstr.getLocalName());
                }
            }
        }

        // output result into file, if defined
        if (out != null) {
            Source source = new DOMSource(doc);
            Result result = new StreamResult(out);

            Transformer xf = TransformerFactory.newInstance().newTransformer();
            xf.setOutputProperty(OutputKeys.INDENT, "true");
            xf.transform(source, result);
        }
    }

    protected int ini;

    /**
     * Create source segment by child text and child nodes, with replace all
     * tags into OmegaT's tags.
     * 
     * @param el
     *            root element for process all childs
     * @param out
     *            result text
     * @param nodes
     *            nodes mapping for processing
     * @throws FormatException
     */
    protected void collect(Node el, StringBuilder out, Map<String, Node> nodes)
            throws FormatException {
        for (Node n = el.getFirstChild(); n != null; n = n.getNextSibling()) {
            switch (n.getNodeType()) {
            case Node.TEXT_NODE:
                out.append(n.getTextContent());
                break;
            case Node.ELEMENT_NODE:
                int v = ini++;
                nodes.put("t" + v, n);
                out.append("<t" + v + ">");
                collect(n, out, nodes);
                out.append("</t" + v + ">");
                break;
            default:
                throw new FormatException("Unknown node type: "
                        + n.getNodeType());
            }
        }
    }

    /**
     * Copy node's name and attributes from one to another. Used for replace
     * OmegaT's tags into resources tags.
     * 
     * @param doc
     * @param from
     * @param to
     */
    protected void copyNodeNameAndAttrs(Document doc, Node from, Node to) {
        Element e = (Element) doc.renameNode(to, from.getNamespaceURI(), from
                .getNodeName());
        NamedNodeMap attrs = from.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr a = (Attr) attrs.item(i);
            Attr ao = doc.createAttributeNS(a.getNamespaceURI(), a
                    .getNodeName());
            ao.setNodeValue(a.getNodeValue());
            e.setAttributeNode(ao);
        }
    }

    /**
     * Convert target segment into resource's node.
     * 
     * @param trans
     * @param nodes
     * @return
     * @throws Exception
     */
    protected Node apply(String trans, Map<String, Node> nodes)
            throws Exception {
        DocumentBuilder db = DBF.newDocumentBuilder();

        String rtrans = trans.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;");
        rtrans = rtrans.replaceAll("&lt;(t[0-9]+)&gt;", "<$1>").replaceAll(
                "&lt;/(t[0-9]+)&gt;", "</$1>");
        String txt = "<doc>" + rtrans + "</doc>";
        System.out.println(txt);
        Document doc = db.parse(new InputSource(new StringReader(txt)));
        applyNodesDeep(doc, nodes, doc.getFirstChild());
        return doc.getFirstChild().cloneNode(true);
    }

    /**
     * Convert childs.
     * 
     * @param transDoc
     * @param origNodes
     * @param transNode
     * @throws FormatException
     */
    protected void applyNodesDeep(Document transDoc,
            Map<String, Node> origNodes, Node transNode) throws FormatException {
        for (Node nstr = transNode.getFirstChild(); nstr != null; nstr = nstr
                .getNextSibling()) {
            if (nstr.getNodeType() == Node.ELEMENT_NODE) {
                Node orig = origNodes.get(nstr.getLocalName());
                if (orig == null) {
                    throw new FormatException(
                            "There is no such tags in original node");
                }
                copyNodeNameAndAttrs(transDoc, orig, nstr);
                applyNodesDeep(transDoc, origNodes, nstr);
            }
        }
    }

    /**
     * Check node name and namespace.
     * 
     * @param n
     * @param namespace
     * @param localName
     * @return
     */
    protected boolean isNamed(Node n, String namespace, String localName) {
        if (namespace == null) {
            if (n.getNamespaceURI() != null) {
                return false;
            } else {
                return localName.equals(n.getLocalName());
            }
        }
        return namespace.equals(n.getNamespaceURI())
                && localName.equals(n.getLocalName());
    }

    protected static class FormatException extends Exception {
        public FormatException() {
        }

        public FormatException(String s) {
            super(s);
        }
    }
}
