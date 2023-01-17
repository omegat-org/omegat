/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017-2019 Thomas Cordonnier
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

package org.omegat.filters4.xml.xliff;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.omegat.util.OStrings;

/**
 * Filter for support Xliff 2.0 files as bilingual.
 *
 * @author Thomas Cordonnier
 */
public class Xliff2Filter extends AbstractXliffFilter {

    // --------------------------- IFilter API ----------------------------

    @Override
    public String getFileFormatName() {
        return OStrings.getString("XLIFF2FILTER_FILTER_NAME");
    }

    protected final String versionPrefix() {
        return "2."; // can be 2.0, maybe more in the future
    }

    // ------------------- AbstractXmlFilter part -------------------------

    @Override // start events on body
    protected void checkCurrentCursorPosition(javax.xml.stream.XMLStreamReader reader, boolean doWrite) {
        super.checkCurrentCursorPosition(reader, doWrite);
        if (reader.getEventType() == StartElement.START_ELEMENT) {
            if (reader.getLocalName().equals("notes") || reader.getLocalName().equals("group")
                    || reader.getLocalName().equals("unit")) {
                this.isEventMode = true;
            }
        }
    }

    /** Current translation unit **/
    private String segId = null;
    private boolean flushedSegment = false;

    @Override
    protected boolean processStartElement(StartElement startElement, XMLStreamWriter writer)
            throws XMLStreamException {
        switch (startElement.getName().getLocalPart()) {
        case "xliff":
            if (namespace == null) {
                namespace = startElement.getName().getNamespaceURI();
            }
            break;
        case "file":
        case "group":
        case "unit":
            try {
                path += "/" + startElement.getAttributeByName(new QName("id")).getValue();
            } catch (NullPointerException noid) { // Note: in spec, id is
                                                  // REQUIRED
                throw new XMLStreamException(OStrings.getString("XLIFF_MANDATORY_ORIGINAL_MISSING", "id",
                        startElement.getName().getLocalPart()));
            }
            updateIgnoreScope(startElement);
            break;
        case "segment":
            try {
                segId = startElement.getAttributeByName(new QName("id")).getValue();
            } catch (NullPointerException noid) { // Note: in spec, id is
                                                  // OPTIONAL
                if (segId == null) {
                    segId = "1";
                } else {
                    try {
                        segId = Integer.toString(Integer.parseInt(segId) + 1);
                    } catch (NumberFormatException fmt) {
                        segId = "1";
                    }
                }
            }
            flushedSegment = false;
            break;
        case "source":
            currentBuffer = source;
            source.clear();
            break;
        case "target":
            target = new LinkedList<XMLEvent>();
            currentBuffer = target;
            inTarget = true;
            break;
        case "notes":
            note.clear();
            break;
        case "note":
            if (startElement.getAttributeByName(new QName("id")) != null) {
                note.add(eFactory.createCharacters(
                        "\n\n[" + startElement.getAttributeByName(new QName("id")).getValue() + "] "));
            } else if (!note.isEmpty()) {
                note.add(eFactory.createCharacters("\n\n"));
            }
            currentBuffer = note;
            break;
        default:
            if (currentBuffer != null) {
                currentBuffer.add(startElement);
            }
            // <target> must be before any other-namespace markup
            else if (((ignoreScope == null || ignoreScope.startsWith("!")) && (segId != null))
                    && (!startElement.getName().getNamespaceURI().equals(namespace))) {
                flushTranslations(writer);
            }
        }
        return !inTarget;
    }

    @Override
    protected boolean processEndElement(EndElement endElement, XMLStreamWriter writer)
            throws XMLStreamException {
        switch (endElement.getName().getLocalPart()) {
        case "source":
        case "note":
            currentBuffer = null;
            break;
        case "target":
            currentBuffer = null;
            if (ignoreScope == null || ignoreScope.startsWith("!")) {
                flushTranslations(writer); // we are in the correct place
            }
            inTarget = false;
            return false;
        case "segment":
            if (ignoreScope == null || ignoreScope.startsWith("!")) {
                flushTranslations(writer); // if there was no <target> at all
            }
            if (ignoreScope == null || ignoreScope.startsWith("!")) {
                registerCurrentTransUnit(segId, source, target, ".*");
            }
            segId = null;
            cleanBuffers();
            break;
        case "unit":
        case "group":
        case "file":
            segId = "";
            path = path.substring(0, path.lastIndexOf('/'));
            cleanBuffers();
            if (endElement.getName().getLocalPart().equals(ignoreScope)) {
                ignoreScope = null;
            } else if (ignoreScope != null
                    && ignoreScope.startsWith("!" + endElement.getName().getLocalPart())) {
                ignoreScope = ignoreScope.substring(endElement.getName().getLocalPart().length() + 2);
            }
            break;
        default:
            if (currentBuffer != null) {
                currentBuffer.add(endElement);
            }
        }
        return !inTarget;
    }

    @Override
    protected String[] getPairIdNames(boolean start) {
        if (start) {
            return new String[] { "id" };
        } else {
            return new String[] { "startRef", "id" };
        }
    }

    /**
     * Converts List<XMLEvent> to OmegaT format, with <x0/>, <g0>...</g0>, etc.
     * Also build maps to be reused later
     **/
    protected String buildTags(List<XMLEvent> srcList, boolean reuse) {
        if (!reuse) {
            tagsMap.clear();
            for (Character c : tagsCount.keySet()) {
                tagsCount.put(c, 0);
            }
        }
        StringBuffer res = new StringBuffer();
        for (XMLEvent ev : srcList) {
            if (ev.isCharacters()) {
                res.append(ev.asCharacters().getData());
            } else if (ev.isStartElement()) {
                StartElement stEl = ev.asStartElement();
                String name = stEl.getName().getLocalPart();
                char prefix = findPrefix(stEl);
                Integer count = tagsCount.get(prefix);
                if (count == null) {
                    count = 0;
                    tagsCount.put(prefix, count + 1);
                }
                switch (name) {
                case "mrk":
                    break;
                case "ph":
                case "cp": // empty element
                    res.append(startPair(reuse, true, stEl, prefix, count, toPair(stEl)));
                    break;
                case "sc":
                case "sm": // empty element, paired, start
                    res.append(startPair(reuse, false, stEl, prefix, count, toPair(stEl)));
                    break;
                case "ec":
                case "em": // empty element, paired, end
                    res.append(endPair(reuse, stEl, prefix, count, toPair(stEl)));
                    break;
                case "pc":
                default:
                    startStackElement(reuse, stEl, prefix, count, res);
                    break;
                }
            } else if (ev.isEndElement()) {
                EndElement endEl = ev.asEndElement();
                switch (endEl.getName().getLocalPart()) {
                case "mrk":
                    break;
                case "ph":
                case "cp":
                case "sc":
                case "ec":
                    break; // Should be empty!!!
                case "pc":
                default: {
                    String pop = tagStack.pop();
                    tagsMap.put("/" + pop, Collections.singletonList(ev));
                    res.append("</").append(pop).append(">");
                    break;
                }
                }
            }
        }
        return res.toString();
    }

    protected char findPrefix(StartElement stEl) {
        Attribute type = stEl.getAttributeByName(new QName("type"));
        if (type != null && type.getValue().equals("fmt")) {
            type = stEl.getAttributeByName(new QName("subType"));
            if (type != null && type.getValue().startsWith("xlf:")) {
                return type.getValue().charAt(4);
            }
            return 'f'; // value = fmt, so we are almost in a format, but we
                        // don't know which one
        }
        String name = stEl.getName().getLocalPart();
        if (name.equals("pc")) {
            return 'g';
        }
        if (name.equals("sc") || name.equals("ec")) {
            return 't';
        }
        if (name.equals("sm") || name.equals("em")) {
            return 'a';
        }
        if (!stEl.getName().getNamespaceURI().equals(this.namespace)) {
            return 'o'; // other (normally not allowed by specification)
        }
        // default
        return name.charAt(0);
    }

    /** Replace <target> by OmegaT's translation, if found **/
    private void flushTranslations(XMLStreamWriter writer) throws XMLStreamException {
        if (writer == null) {
            return;
        }
        if (flushedSegment) {
            return;
        }

        String src = buildTags(source, false);
        String tra = entryTranslateCallback.getTranslation(segId, src, path);
        if (tra != null) {
            writer.writeStartElement(namespace, "target");
            // even if source did not contain target,
            // here we generate translation from OmegaT
            for (XMLEvent ev : restoreTags(tra)) {
                fromEventToWriter(ev, writer);
            }
        } else {
            if (target == null) {
                return;
            }
            writer.writeStartElement(namespace, "target");
            // only if there was <target> in the source file
            for (XMLEvent ev : target) {
                fromEventToWriter(ev, writer);
            }
        }
        writer.writeEndElement(/* target */);
        flushedSegment = true;
    }

}
