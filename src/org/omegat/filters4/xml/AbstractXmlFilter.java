/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Thomas Cordonnier
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
package org.omegat.filters4.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;

import org.omegat.core.data.ProtectedPart;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.TranslationException;
import org.omegat.filters3.xml.XMLReader;
import org.omegat.filters3.xml.XMLWriter;
import org.omegat.util.StaticUtils;

/**
 * Abstract for StaX-based filters.
 * <p>
 * This class is more low-level than 'filters3' API, but this is necessary to
 * implement bilingual or very complex XML formats. <br/>
 * To implement your own filter based on this class you must implement
 * {@link org.omegat.filters4.xml.AbstractXmlFilter#processStartElement processStartElement},
 * {@link org.omegat.filters4.xml.AbstractXmlFilter#processEndElement processEndElement} and
 * {@link org.omegat.filters4.xml.AbstractXmlFilter#processCharacters processCharacters} <br/>
 * Boolean return value will be ignored during reading process (when event
 * writer is null), while during project compilation, it says whenever the event
 * must be kept in the result or not (generally, parts of the XML file which are
 * translated must return false).
 *
 * @author Thomas Cordonnier
 */
public abstract class AbstractXmlFilter extends AbstractFilter {

    /** Detected encoding and eol of the input XML file. */
    private String encoding, eol;

    @Override
    public boolean isSourceEncodingVariable() {
        return true;
    }

    @Override
    public boolean isTargetEncodingVariable() {
        return true;
    }

    /**
     * Creates a special XML-encoding-aware reader of an input file.
     *
     * @param inFile
     *            The source file.
     * @return The reader of the source file.
     *
     * @throws UnsupportedEncodingException
     *             Thrown if JVM doesn't support the specified inEncoding.
     * @throws IOException
     *             If any I/O Error occurs upon reader creation.
     */
    @Override
    public BufferedReader createReader(File inFile, String inEncoding)
            throws UnsupportedEncodingException, IOException {
        XMLReader xmlreader = new XMLReader(inFile, inEncoding);
        this.encoding = xmlreader.getEncoding();
        this.eol = xmlreader.getEol();
        return new BufferedReader(xmlreader);
    }

    /**
     * Creates a writer of the translated file. Accepts <code>null</code> output
     * file -- returns a writer to <code>/dev/null</code> in this case ;-)
     *
     * @param outFile
     *            The target file.
     * @param outEncoding
     *            Encoding of the target file, if the filter supports it.
     *            Otherwise, null.
     * @return The writer for the target file.
     *
     * @throws UnsupportedEncodingException
     *             Thrown if JVM doesn't support the specified outEncoding
     * @throws IOException
     *             If any I/O Error occurs upon writer creation
     */
    @Override
    public BufferedWriter createWriter(File outFile, String outEncoding)
            throws UnsupportedEncodingException, IOException {
        if (outFile == null) {
            return new BufferedWriter(new StringWriter());
        } else {
            if (outEncoding == null) {
                outEncoding = this.encoding;
            }
            return new BufferedWriter(new XMLWriter(outFile, outEncoding, eol));
        }
    }

    /** Processes an XML file. Does encoding/EOL detection, if necessary **/
    @Override
    public void processFile(File inFile, File outFile, FilterContext fc)
            throws IOException, TranslationException {
        try (BufferedReader inReader = createReader(inFile, fc.getInEncoding())) {
            inEncodingLastParsedFile = this.encoding;
            if (outFile != null) {
                try (BufferedWriter writer = createWriter(outFile, fc.getOutEncoding())) {
                    processFile(inReader, writer, fc);
                }
            } else {
                processFile(inReader, null, fc);
            }
        }
    }

    protected XMLInputFactory iFactory = XMLInputFactory.newInstance();
    protected XMLOutputFactory oFactory = XMLOutputFactory.newInstance();

    /**
     * Processes a buffer.
     * <p>
     * This method works only if buffered reader and writer
     * are already configured with correct encoding and EOL.
     **/
    @Override
    public void processFile(BufferedReader inReader, BufferedWriter writer, FilterContext fc)
            throws IOException, TranslationException {
        try {
            XMLStreamReader strReader = null;
            XMLStreamWriter strWriter = null;
            XMLEventReader eventReader = null;
            try {
                strReader = iFactory.createXMLStreamReader(inReader);
                eventReader = iFactory.createXMLEventReader(strReader);
                isEventMode = false; // always start like this, even with new
                                     // file
                if (writer == null) {
                    while (strReader.hasNext()) {
                        if (!isEventMode) {
                            checkCurrentCursorPosition(strReader, false);
                        }
                        if (isEventMode) { // calculated after
                                           // checkCurrentCursorPosition, may
                                           // have changed!
                            XMLEvent event = eventReader.nextEvent();
                            if (event.isStartElement()) {
                                processStartElement(event.asStartElement(), null);
                            } else if (event.isEndElement()) {
                                processEndElement(event.asEndElement(), null);
                            } else if (event.isCharacters()) {
                                processCharacters(event.asCharacters(), null);
                            }
                        } else {
                            strReader.next();
                        }
                    }
                } else {
                    strWriter = oFactory.createXMLStreamWriter(writer);
                    while (strReader.hasNext()) {
                        if (strReader.getEventType() == XMLEvent.START_DOCUMENT) {
                            // special case: don't use strWriter because StaX
                            // has a bug!
                            String toWrite = "<?xml ";
                            String version = strReader.getVersion();
                            if (version != null) {
                                toWrite += " version=\"" + version + "\"";
                            }
                            String escheme = strReader.getCharacterEncodingScheme();
                            if (escheme != null) {
                                toWrite += " encoding=\"" + escheme + "\"";
                            }
                            if (strReader.standaloneSet()) {
                                toWrite += " standalone=\"" + (strReader.isStandalone() ? "yes" : "no")
                                        + "\"";
                                // not possible using strWriter!!!
                            }
                            toWrite += " ?>";
                            writer.write(toWrite);
                            strReader.next();
                            continue;
                        }
                        if (!isEventMode) {
                            checkCurrentCursorPosition(strReader, true);
                            // in non-event mode, we always write exactly what
                            // was in the source
                            fromReaderToWriter(strReader, strWriter);
                        }
                        if (isEventMode) { // calculated after
                                           // checkCurrentCursorPosition, may
                                           // have changed!
                            XMLEvent event = eventReader.nextEvent();
                            boolean keep;
                            if (event.isStartElement()) {
                                keep = processStartElement(event.asStartElement(), strWriter);
                            } else if (event.isEndElement()) {
                                keep = processEndElement(event.asEndElement(), strWriter);
                            } else if (event.isCharacters()) {
                                keep = processCharacters(event.asCharacters(), strWriter);
                            } else {
                                keep = true;
                            }
                            if (keep) {
                                fromEventToWriter(event, strWriter); // convert
                                                                     // current
                                                                     // event to
                                                                     // stream
                            }
                        } else {
                            strReader.next();
                        }
                    }
                }
            } finally {
                if (eventReader != null) {
                    eventReader.close();
                }
                if (strReader != null) {
                    strReader.close();
                }
                if (strWriter != null) {
                    strWriter.flush();
                    strWriter.close();
                }
            }
        } catch (XMLStreamException e) {
            throw new TranslationException(e);
        }
    }

    /**
     * Indicates whenever we are in event mode or not.
     * <p>
     * We always start with
     * false, and checkCurrentCursorPosition may set it to true
     **/
    protected boolean isEventMode = false;

    /** Called for each cursor step when we are NOT in event mode **/
    protected abstract void checkCurrentCursorPosition(XMLStreamReader reader, boolean doWrite);

    /**
     * Called for each start element (including empty ones) when we are in event
     * mode.
     * 
     * @return true if the element must be kept in translation, false otherwise
     **/
    protected abstract boolean processStartElement(StartElement el, XMLStreamWriter evWriter)
            throws IOException, XMLStreamException;

    /**
     * Called for each end element (including empty ones) when we are in event
     * mode.
     * 
     * @return true if the element must be kept in translation, false otherwise
     **/
    protected abstract boolean processEndElement(EndElement el, XMLStreamWriter evWriter)
            throws IOException, XMLStreamException;

    /**
     * Called for each sequence of characters when we are in event mode.
     * 
     * @return true if the element must be kept in translation, false otherwise
     **/
    protected abstract boolean processCharacters(Characters el, XMLStreamWriter evWriter)
            throws IOException, XMLStreamException;

    // Inspired from http://www.java2s.com/Code/Java/XML/XmlReaderToWriter.htm
    private void fromReaderToWriter(XMLStreamReader xmlr, XMLStreamWriter writer) throws XMLStreamException {
        switch (xmlr.getEventType()) {
        case XMLEvent.ENTITY_REFERENCE:
            writer.writeEntityRef(xmlr.getLocalName());
            break;
        case XMLEvent.DTD:
            writer.writeDTD(xmlr.getText());
            break;
        case XMLEvent.START_ELEMENT:
            final String localName = xmlr.getLocalName(), namespaceURI = xmlr.getNamespaceURI();
            if (namespaceURI != null && namespaceURI.length() > 0) {
                final String prefix = xmlr.getPrefix();
                if (prefix != null) {
                    writer.writeStartElement(prefix, localName, namespaceURI);
                } else {
                    writer.writeStartElement(namespaceURI, localName);
                }
            } else {
                writer.writeStartElement(localName);
            }
            for (int i = 0, len = xmlr.getNamespaceCount(); i < len; i++) {
                writer.writeNamespace(xmlr.getNamespacePrefix(i), xmlr.getNamespaceURI(i));
            }
            for (int i = 0, len = xmlr.getAttributeCount(); i < len; i++) {
                String attUri = xmlr.getAttributeNamespace(i);
                if (attUri != null) {
                    writer.writeAttribute(attUri, xmlr.getAttributeLocalName(i), xmlr.getAttributeValue(i));
                } else {
                    writer.writeAttribute(xmlr.getAttributeLocalName(i), xmlr.getAttributeValue(i));
                }
            }
            break;
        case XMLEvent.END_ELEMENT:
            writer.writeEndElement();
            break;
        case XMLEvent.SPACE:
        case XMLEvent.CHARACTERS:
            writer.writeCharacters(xmlr.getTextCharacters(), xmlr.getTextStart(), xmlr.getTextLength());
            break;
        case XMLEvent.PROCESSING_INSTRUCTION:
            writer.writeProcessingInstruction(xmlr.getPITarget(), xmlr.getPIData());
            break;
        case XMLEvent.CDATA:
            writer.writeCData(xmlr.getText());
            break;
        case XMLEvent.COMMENT:
            writer.writeComment(xmlr.getText());
            break;
        case XMLEvent.END_DOCUMENT:
            writer.writeEndDocument();
            break;
        }
    }

    @SuppressWarnings("unchecked")
    protected final void fromEventToWriter(XMLEvent ev, XMLStreamWriter writer) throws XMLStreamException {
        switch (ev.getEventType()) {
        case XMLEvent.ENTITY_REFERENCE:
            writer.writeEntityRef(((EntityReference) ev).getName());
            break;
        // case XMLEvent.DTD: writer.writeDTD(((DTD) ev).getText()); break;
        case XMLEvent.START_ELEMENT:
            StartElement el = ev.asStartElement();
            QName name = el.getName();
            writer.writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());
            for (Iterator<Namespace> iter = el.getNamespaces(); iter.hasNext();) {
                Namespace ns = iter.next();
                writer.writeNamespace(ns.getPrefix(), ns.getNamespaceURI());
            }
            for (Iterator<Attribute> iter = el.getAttributes(); iter.hasNext();) {
                Attribute attr = iter.next();
                writer.writeAttribute(attr.getName().getPrefix(), attr.getName().getNamespaceURI(),
                        attr.getName().getLocalPart(), attr.getValue());
            }
            break;
        case XMLEvent.ATTRIBUTE:
            writer.writeAttribute(((Attribute) ev).getName().getPrefix(),
                    ((Attribute) ev).getName().getNamespaceURI(), ((Attribute) ev).getName().getLocalPart(),
                    ((Attribute) ev).getValue());
            break;
        case XMLEvent.END_ELEMENT:
            writer.writeEndElement();
            break;
        case XMLEvent.SPACE:
        case XMLEvent.CHARACTERS:
            writer.writeCharacters(((Characters) ev).getData());
            break;
        case XMLEvent.PROCESSING_INSTRUCTION:
            writer.writeProcessingInstruction(((ProcessingInstruction) ev).getTarget(),
                    ((ProcessingInstruction) ev).getData());
            break;
        case XMLEvent.CDATA:
            writer.writeCData(((Characters) ev).getData());
            break;
        case XMLEvent.COMMENT:
            writer.writeComment(((Comment) ev).getText());
            break;
        case XMLEvent.END_DOCUMENT:
            writer.writeEndDocument();
            break;
        }
    }

    /** Used for file type detection **/
    protected StartElement findEvent(File inputFile, Pattern path) throws IOException, TranslationException {
        try {
            XMLEventReader eventReader = null;
            try {
                eventReader = iFactory.createXMLEventReader(new java.io.FileInputStream(inputFile));
                String curPath = "/";
                while (eventReader.hasNext()) {
                    XMLEvent event = eventReader.nextEvent();
                    if (event.isStartElement()) {
                        StartElement el = event.asStartElement();
                        curPath += "/" + el.getName().getNamespaceURI() + ":" + el.getName().getLocalPart();
                        if (path.matcher(curPath).matches()) {
                            return el;
                        }
                    } else if (event.isEndElement()) {
                        curPath = curPath.substring(0, curPath.lastIndexOf("/"));
                    }
                }
            } finally {
                if (eventReader != null) {
                    eventReader.close();
                }
            }
        } catch (XMLStreamException e) {
            throw new TranslationException(e);
        }
        return null;
    }

    // Memorize association between tags and list of events
    // This map is used by restoreTags
    // but it is the responsability of child class to fill it during segment
    // processing and to clean it after
    protected Map<String, List<XMLEvent>> tagsMap = new TreeMap<>();

    protected static final Pattern OMEGAT_TAG = Pattern.compile("<(\\/?)([a-z]\\d+)\\/?>");
    protected static final XMLEventFactory eFactory = XMLEventFactory.newInstance();

    /**
     * Produces xliff content for the translated text. Note: must be called
     * after buildTags(src, true) to have the necessary variables filled!
     **/
    protected List<XMLEvent> restoreTags(String tra) {
        List<XMLEvent> res = new LinkedList<XMLEvent>();
        while (tra.length() > 0) {
            Matcher m = OMEGAT_TAG.matcher(tra);
            if (m.find()) {
                res.add(eFactory.createCharacters(tra.substring(0, m.start())));
                List<XMLEvent> saved = tagsMap.get(m.group(1) + m.group(2));
                if (saved != null) {
                    res.addAll(saved);
                }
                tra = tra.substring(m.end());
            } else {
                res.add(eFactory.createCharacters(tra));
                return res;
            }
        }
        return res;
    }

    protected String buildProtectedPartDetails(List<XMLEvent> saved) {
        StringWriter writer = new StringWriter();
        try {
            XMLEventWriter eventWriter = oFactory.createXMLEventWriter(writer);
            for (XMLEvent ev : saved) {
                eventWriter.add(ev);
            }
        } catch (Exception e) {
            for (XMLEvent ev : saved) {
                if (ev.isEndElement()) {
                    writer.write("</" + ev.asEndElement().getName().getPrefix() + ":"
                            + ev.asEndElement().getName().getLocalPart() + ">");
                } else {
                    writer.write(ev.toString());
                }
            }
        }
        return writer.toString();
    }

    protected List<ProtectedPart> buildProtectedParts(String src) {
        List<ProtectedPart> protectedParts = new LinkedList<ProtectedPart>();
        while (src.length() > 0) {
            Matcher m = OMEGAT_TAG.matcher(src);
            if (!m.find()) {
                break;
            }
            List<XMLEvent> saved = tagsMap.get(m.group(1) + m.group(2));
            if (saved != null) {
                ProtectedPart pp = new ProtectedPart();
                pp.setTextInSourceSegment(m.group());
                pp.setDetailsFromSourceFile(buildProtectedPartDetails(saved));
                if (org.omegat.core.statistics.StatisticsSettings.isCountingStandardTags()) {
                    pp.setReplacementWordsCountCalculation(
                            StaticUtils.TAG_REPLACEMENT_CHAR + m.group().replace('<', '_').replace('>', '_')
                                    + StaticUtils.TAG_REPLACEMENT_CHAR);
                } else {
                    pp.setReplacementWordsCountCalculation(StaticUtils.TAG_REPLACEMENT);
                }
                pp.setReplacementUniquenessCalculation(StaticUtils.TAG_REPLACEMENT);
                pp.setReplacementMatchCalculation(StaticUtils.TAG_REPLACEMENT);
                protectedParts.add(pp);
            }
            src = src.substring(m.end());
        }
        return protectedParts;
    }

    /** Convert <xxx/> to <xxx></xxx> **/
    protected static List<XMLEvent> toPair(StartElement ev) {
        List<XMLEvent> l = new LinkedList<XMLEvent>();
        l.add(ev);
        l.add(eFactory.createEndElement(ev.getName(), null));
        return l;
    }

    @SuppressWarnings("unchecked")
    protected String findKey(StartElement findEl, boolean isEmpty) {
        for (Map.Entry<String, List<XMLEvent>> me : tagsMap.entrySet()) {
            try {
                StartElement mapEl = me.getValue().get(0).asStartElement();
                if (mapEl.getName().equals(findEl.getName())) {
                    boolean foundDiff = false;
                    for (Iterator<Attribute> iter = mapEl.getAttributes(); iter.hasNext();) {
                        Attribute curAttr = iter.next();
                        Attribute findAttr = findEl.getAttributeByName(curAttr.getName());
                        if (findAttr == null) {
                            foundDiff = true;
                        } else if (!findAttr.getValue().equals(curAttr.getValue())) {
                            foundDiff = true;
                        }
                    }
                    if (!foundDiff) {
                        return "<" + me.getKey() + (isEmpty ? "/>" : ">");
                    }
                }
            } catch (Exception e) {
                // on exception: continue loop
            }
        }
        return "";
    }

}
