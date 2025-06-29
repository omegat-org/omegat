/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017-2022 Thomas Cordonnier
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

package org.omegat.filters4.xml.xliff;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.omegat.core.Core;
import org.omegat.util.OStrings;

/**
 * Filter for support Xliff 1 files as bilingual (unlike filters3/xml/xliff).
 *
 * @author Thomas Cordonnier
 */
public class Xliff1Filter extends AbstractXliffFilter {

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(Xliff1Filter.class);
    }

    public static void unloadPlugins() {
        // there is no way to unregister the filter
    }

    // ---------------------------- IFilter API---------------------------

    @Override
    public String getFileFormatName() {
        return OStrings.getString("XLIFF1FILTER_FILTER_NAME");
    }

    protected final String versionPrefix() {
        return "1."; // can be 1.0, 1.1 or 1.2
    }

    // ----------------------- AbstractXmlFilter part----------------------

    private static final String SOURCE_ELEMENT = "source";
    private static final String TARGET_ELEMENT = "target";
    private static final String NOTE_ELEMENT = "note";
    private static final String TRANS_UNIT_ELEMENT = "trans-unit";
    private static final String ID_ATTRIBUTE = "id";
    private static final String STATE_ATTRIBUTE = "state";
    private static final String TRANSLATED_STATE = "translated";

    /** Current translation unit **/
    private String unitId = null;
    private boolean flushedUnit = false;
    private int lastGroupId = 0; // only used when group id is not present in
                                 // the file
    private final List<XMLEvent> segSource = new LinkedList<>();
    private final Map<String, List<XMLEvent>> subSegments = new TreeMap<>();
    private StartElement targetStartEvent = null;
    private int inSubSeg = 0;

    @Override
    protected void cleanBuffers() {
        source.clear();
        target = null;
        note.clear();
        segSource.clear();
        subSegments.clear();
    }

    @Override // start events on body
    protected boolean checkCurrentCursorPosition(XMLStreamReader reader, boolean doWrite) {
        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
            if (reader.getLocalName().equals("body")) {
                this.isEventMode = true;
            } else if (reader.getLocalName().equals("xliff")) {
                if (namespace == null) {
                    namespace = reader.getName().getNamespaceURI();
                }
            } else if (reader.getLocalName().equals("file") || reader.getLocalName().equals("group")
                    || reader.getLocalName().equals("unit")) {
                final List<Attribute> attributes = new LinkedList<>();
                for (int i = 0, len = reader.getAttributeCount(); i < len; i++) {
                    attributes.add(eFactory.createAttribute(reader.getAttributeName(i),
                            reader.getAttributeValue(i)));
                }
                try {
                    processStartElement(
                            eFactory.createStartElement(reader.getName(), attributes.iterator(), null), null);
                } catch (Exception ignored) {
                    // XXX: Can we really skip?
                }
            }
        }
        return isEventMode;
    }

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
            path += "/" + getRequiredAttribute(startElement, "original", "file");
            updateIgnoreScope(startElement);
            break;
        case "group":
            if (startElement.getAttributeByName(new QName(ID_ATTRIBUTE)) != null) {
                path += "/" + startElement.getAttributeByName(new QName(ID_ATTRIBUTE)).getValue();
            } else if (startElement.getAttributeByName(new QName("resname")) != null) {
                path += "/" + startElement.getAttributeByName(new QName("resname")).getValue();
            } else {
                // generate an unique id:
                // must be unique at document scope but must be identical
                // when you re-parse the document
                path += "/x-auto-" + lastGroupId;
                lastGroupId++;
            }
            updateIgnoreScope(startElement);
            break;
        case TRANS_UNIT_ELEMENT:
            unitId = getRequiredAttribute(startElement, ID_ATTRIBUTE, TRANS_UNIT_ELEMENT);
            flushedUnit = false;
            targetStartEvent = null;
            updateIgnoreScope(startElement);
            break;
        case SOURCE_ELEMENT:
            currentBuffer = source;
            source.clear();
            break;
        case TARGET_ELEMENT:
            target = new LinkedList<>();
            currentBuffer = target;
            inTarget = true;
            targetStartEvent = startElement;
            break;
        case NOTE_ELEMENT:
            currentBuffer = note;
            note.clear();
            break;
        case "seg-source":
            currentBuffer = segSource;
            segSource.clear();
            break;
        case "mrk":
            handleMrkElement(startElement, writer);
            break;
        default:
            handleDefaultElement(startElement, writer);
        }
        return !inTarget;
    }

    private void handleMrkElement(StartElement element, XMLStreamWriter writer) throws XMLStreamException {
        if (element.getAttributeByName(new QName("mtype")).getValue().equals("seg")) {
            String mid = element.getAttributeByName(new QName("mid")).getValue();
            currentBuffer.add(element);
            currentBuffer = new LinkedList<>();
            if (!inTarget) {
                subSegments.put(mid, currentBuffer);
            }
            inSubSeg++;
            return;
        }
        if (inSubSeg > 0) {
            inSubSeg++;
        }
        handleDefaultElement(element, writer);
    }

    private String getRequiredAttribute(StartElement element, String attributeName, String elementName)
            throws XMLStreamException {
        Attribute attribute = element.getAttributeByName(new QName(attributeName));
        if (attribute == null) {
            throw new XMLStreamException(OStrings.getString("XLIFF_MANDATORY_ORIGINAL_MISSING", attributeName,
                    elementName));
        }
        return attribute.getValue();
    }

    private void handleDefaultElement(StartElement element, XMLStreamWriter writer)
            throws XMLStreamException {
        if (currentBuffer != null) {
            currentBuffer.add(element);
        } else if (shouldFlushTranslations(element)) {
            flushTranslations(writer);
        }
    }

    private boolean shouldFlushTranslations(StartElement element) {
        return (ignoreScope == null || ignoreScope.startsWith("!"))
                && (unitId != null)
                && (!element.getName().getNamespaceURI()
                .equals("urn:oasis:names:tc:xliff:document:1.2"));
    }

    @Override
    protected boolean processEndElement(EndElement endElement, XMLStreamWriter writer)
            throws XMLStreamException {
        switch (endElement.getName().getLocalPart()) {
        case SOURCE_ELEMENT:
        case "seg-source":
        case NOTE_ELEMENT:
            currentBuffer = null;
            break;
        case TARGET_ELEMENT:
            currentBuffer = null;
            if (ignoreScope == null || ignoreScope.startsWith("!")) {
                flushTranslations(writer); // we are in the correct place
            }
            inTarget = false;
            return false;
        case TRANS_UNIT_ELEMENT:
            handleTransUnitEndElement(endElement, writer);
            break;
        case "file":
            handleFileEndElement(endElement);
            break;
        case "group":
            handleGroupEndElement(endElement);
            break;
        case "mrk":
            handleMrkEndElement(endElement);
            break;
        default:
            handleDefaultEndElement(endElement);
        }
        return !inTarget;
    }

    private void handleGroupEndElement(EndElement endElement) {
        path = path.substring(0, path.lastIndexOf('/'));
        cleanBuffers();
        if (endElement.getName().getLocalPart().equals(ignoreScope)) {
            ignoreScope = null;
        } else if (ignoreScope != null
                && ignoreScope.startsWith("!" + endElement.getName().getLocalPart())) {
            ignoreScope = ignoreScope.substring(endElement.getName().getLocalPart().length() + 2);
        }
    }

    private void handleTransUnitEndElement(EndElement endElement, XMLStreamWriter writer) throws XMLStreamException {
        if (ignoreScope == null || ignoreScope.startsWith("!")) {
            flushTranslations(writer); // if there was no <target> at all
        }
        if (ignoreScope == null || ignoreScope.startsWith("!")) {
            if (subSegments.isEmpty()) {
                registerCurrentTransUnit(unitId, source, target, ".*");
            } else {
                for (Map.Entry<String, List<XMLEvent>> me : subSegments.entrySet()) {
                    registerCurrentTransUnit(unitId + "/" + me.getKey(), me.getValue(),
                            findSubsegment(target, me.getKey()), "\\[(\\d+)\\](.*)\\[\\1\\]");
                }
            }
        }
        unitId = null;
        cleanBuffers();
        if (endElement.getName().getLocalPart().equals(ignoreScope)) {
            ignoreScope = null;
        } else if (ignoreScope != null
                && ignoreScope.startsWith("!" + endElement.getName().getLocalPart())) {
            ignoreScope = ignoreScope.substring(endElement.getName().getLocalPart().length() + 2);
        }
    }

    private void handleFileEndElement(EndElement endElement) {
        path = "/";
        cleanBuffers();
        if (endElement.getName().getLocalPart().equals(ignoreScope)) {
            ignoreScope = null;
        } else if (ignoreScope != null
                && ignoreScope.startsWith("!" + endElement.getName().getLocalPart())) {
            ignoreScope = ignoreScope.substring(endElement.getName().getLocalPart().length() + 2);
        }
    }

    private void handleMrkEndElement(EndElement endElement) {
        if (inSubSeg == 1) {
            List<XMLEvent> save = inTarget ? target : segSource;
            save.addAll(currentBuffer);
            currentBuffer = save;
            currentBuffer.add(endElement);
            inSubSeg = 0;
            return;
        }
        if (inSubSeg > 0) {
            inSubSeg--;
        }
        handleDefaultEndElement(endElement);
    }

    private void handleDefaultEndElement(EndElement endElement) {
        if (currentBuffer != null) {
            currentBuffer.add(endElement);
        }
    }

    // Used by formats where note is in another location
    protected void addNoteFromSource(String tag, String noteText) {
        note.add(eFactory.createCharacters("[" + tag + "]" + noteText + "[/" + tag + "]"));
    }

    @Override
    protected String[] getPairIdNames(boolean start) {
        return new String[] { "rid", "id", "i" };
    }

    protected boolean isCurrentSegmentTranslated(String mid) {
        if ((entryTranslateCallback == null) || (mid == null)) {
            return false;
        }
        if (subSegments.get(mid) == null) {
            return false; // do not crash if xliff target contains sub-segments
                          // not present in source
        }
        return entryTranslateCallback.getTranslation(unitId + "/" + mid,
                buildTags(subSegments.get(mid), false), path) != null;
    }

    /**
     * Converts List<XMLEvent> to OmegaT format, with <x0/>, <g0>...</g0>, etc.
     * Also build maps to be reused later
     **/
    protected String buildTags(List<XMLEvent> srcList, boolean reuse) {
        if (!reuse) {
            tagsMap.clear();
            tagsCount.replaceAll((c, v) -> 0);
        }
        StringBuffer res = new StringBuffer();
        LinkedList<StringBuffer> saveBuf = new LinkedList<>();
        List<XMLEvent> nativeCode = null;
        for (XMLEvent ev : srcList) {
            if (nativeCode != null) {
                nativeCode.add(ev);
            }
            if (ev.isCharacters()) {
                if (nativeCode == null) {
                    res.append(ev.asCharacters().getData());
                }
            } else if (ev.isStartElement()) {
                StartElement stEl = ev.asStartElement();
                String name = stEl.getName().getLocalPart();
                char prefix = findPrefix(stEl);
                Integer count = tagsCount.get(prefix);
                if (count == null) {
                    count = 0;
                }
                tagsCount.put(prefix, count + 1);
                switch (name) {
                case "x": // empty element
                    res.append(startPair(reuse, true, stEl, 'x', count, toPair(stEl)));
                    break;
                case "bx": // empty element, paired, start
                    res.append(startPair(reuse, false, stEl, prefix, count, toPair(stEl)));
                    break;
                case "ex": // empty element, paired, end
                    res.append(endPair(reuse, stEl, prefix, count, toPair(stEl)));
                    break;
                case "bpt": // paired native code, start
                    nativeCode = new LinkedList<>();
                    res.append(startPair(reuse, false, stEl, prefix, count, nativeCode));
                    saveBuf.push(res);
                    res = new StringBuffer();
                    nativeCode.add(ev);
                    break;
                case "ept": // paired native code, end
                    nativeCode = new LinkedList<>();
                    res.append(endPair(reuse, stEl, prefix, count, nativeCode));
                    saveBuf.push(res);
                    res = new StringBuffer();
                    nativeCode.add(ev);
                    break;
                default: // g, mrk, ph, it and other-namespace
                    if (isProtectedTag(stEl)) {
                        // ph, it, mrk:mtype=protected, maybe other
                        nativeCode = new LinkedList<>();
                        if (reuse) {
                            res.append(findKey(stEl, true));
                        } else {
                            Attribute posAttr = stEl.getAttributeByName(new QName("pos"));
                            String posVal = posAttr == null ? "" : posAttr.getValue();
                            if ("close".equals(posVal) || "end".equals(posVal)) {
                                // in OT, appear as close tag
                                tagsMap.put("/" + prefix + count, nativeCode);
                                res.append("</").append(prefix).append(count).append(">");
                            } else {
                                tagsMap.put("" + prefix + count, nativeCode);
                                if ("open".equals(posVal) || "begin".equals(posVal)) {
                                    // in OT,appear as open tag
                                    res.append("<").append(prefix).append(count).append(">");
                                } else {
                                    // in OT, appear as empty
                                    res.append("<").append(prefix).append(count).append("/>");
                                }
                            }
                        }
                        saveBuf.push(res);
                        res = new StringBuffer();
                        nativeCode.add(ev);
                        tagStack.push("mark-protected");
                        break;
                    } else if (isDeletedTag(stEl)) {
                        // generate nothing, not either the contents
                        tagStack.push("mark-deleted");
                        saveBuf.add(res);
                        res = new StringBuffer();
                        break;
                    } else if (isUntaggedTag(stEl)) {
                        tagStack.push("mark-ignored");
                        break; // generate only contents, not the tag
                    }
                    // else do not break, continue with default
                    startStackElement(reuse, stEl, prefix, count, res);
                    break;
                }
            } else if (ev.isEndElement()) {
                EndElement endEl = ev.asEndElement();
                switch (endEl.getName().getLocalPart()) {
                case "x":
                case "bx":
                case "ex":
                    break; // Should be empty!!!
                case "bpt":
                case "ept":
                    nativeCode = null;
                    res.setLength(0);
                    res = saveBuf.pop();
                    break;
                default:
                    String pop = tagStack.pop();
                    if (pop.equals("mark-protected")) { // isProtectedTag(start
                                                        // element) was true
                        nativeCode = null;
                        res.setLength(0);
                        res = saveBuf.pop();
                        break;
                    } else if (pop.equals("mark-deleted")) { // isProtectedTag(start
                                                             // element) was
                                                             // true
                        res.setLength(0);
                        res = saveBuf.pop();
                        break; // dummy res buffer is not used at all
                    } else if (!pop.equals("mark-ignored")) {
                        tagsMap.put("/" + pop, Collections.singletonList(ev));
                        res.append("</").append(pop).append(">");
                    }
                }
            }
        }
        return res.toString();
    }

    protected char findPrefix(StartElement stEl) {
        Attribute ctype = stEl.getAttributeByName(new QName("ctype"));
        if (ctype == null || ctype.getValue() == null || ctype.getValue().isEmpty()) {
            ctype = stEl.getAttributeByName(new QName("type"));
        }
        if (ctype != null && !ctype.getValue().isEmpty()) {
            if (ctype.getValue().startsWith("x-")) {
                // common prefix for non-standard type. And we want to leave x
                // for <x/>
                return Character.toLowerCase(ctype.getValue().charAt(2));
            } else { // usually: bold, italics, etc.
                return Character.toLowerCase(ctype.getValue().charAt(0));
            }
        }

        String name = stEl.getName().getLocalPart();
        if (name.equals("bx") || name.equals("ex")) {
            return 'e';
        }
        if (name.equals("bpt") || name.equals("ept")) {
            return 't';
        }
        if (name.equals("it")) {
            return 'a'; // alone
        }
        if (!stEl.getName().getNamespaceURI().equals(this.namespace)) {
            return 'o'; // other: not conform to spec, but may happen
        }
        // default
        return name.charAt(0);
    }

    // A tag whose content should be replaced by empty tag, for protection.
    // Can be overridden
    protected boolean isProtectedTag(StartElement stEl) {
        return stEl.getName().equals(new QName(namespace, "ph"))
                || stEl.getName().equals(new QName(namespace, "it"))
                || (stEl.getName().equals(new QName(namespace, "mrk"))
                        && stEl.getAttributeByName(new QName("mtype")).getValue().equals("protected"));
    }

    // A tag which should not appear in OmegaT, nor its contents. Can be
    // overridden
    protected boolean isDeletedTag(StartElement stEl) {
        return false;
    }

    // A tag which should not appear in OmegaT, but its contents, yes. Can be
    // overridden
    protected boolean isUntaggedTag(StartElement stEl) {
        return false;
    }

    /**
     * Indicates that state is in standard <target> attribute. To be overriden
     * by non-standard xliff variants
     **/
    protected boolean isStandardTranslationState() {
        return true;
    }

    @SuppressWarnings("unchecked")
    protected final void generateTargetStartElement(XMLStreamWriter writer) throws XMLStreamException {
        if (!isStandardTranslationState()) {
            writeDefaultTarget(writer);
            return;
        }

        if (checkTranslationStatus()) {
            writeTranslatedTarget(writer);
        } else {
            writeDefaultTarget(writer);
        }
    }

    private boolean checkTranslationStatus() {
        if (subSegments.isEmpty()) {
            return entryTranslateCallback.getTranslation(unitId, buildTags(source, false), path) != null;
        } else {
            // set 'translated' only if all sub-segments are translated
            return subSegments.entrySet().stream().allMatch(e -> null != entryTranslateCallback
                    .getTranslation(unitId + "/" + e.getKey(), buildTags(e.getValue(), false), path));
        }
    }

    private void writeTranslatedTarget(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(namespace, TARGET_ELEMENT);
        writer.writeAttribute(STATE_ATTRIBUTE, TRANSLATED_STATE);
        if (targetStartEvent != null) {
            for (java.util.Iterator<Attribute> iter = targetStartEvent.getAttributes(); iter.hasNext();) {
                Attribute next = iter.next();
                if (!STATE_ATTRIBUTE.equals(next.getName().getLocalPart())) {
                    writer.writeAttribute(next.getName().getPrefix(), next.getName().getNamespaceURI(),
                            next.getName().getLocalPart(), next.getValue());
                }
            }
        }
    }

    private void writeDefaultTarget(XMLStreamWriter writer) throws XMLStreamException {
        if (targetStartEvent == null) {
            writer.writeStartElement(namespace, TARGET_ELEMENT);
        } else {
            fromEventToWriter(targetStartEvent, writer);
        }
    }

    /** Replace <target> by OmegaT's translation, if found **/
    private void flushTranslations(XMLStreamWriter writer) throws XMLStreamException {
        if (writer == null) {
            return; // we are only reading the source file
        }
        if (flushedUnit) {
            return;
        }

        if (subSegments.isEmpty()) {
            String src = buildTags(source, false);
            String tra = entryTranslateCallback.getTranslation(unitId, src, path);
            if (tra != null) {
                generateTargetStartElement(writer);
                for (XMLEvent ev : restoreTags(tra)) {
                    fromEventToWriter(ev, writer);
                }
            } else {
                if (target == null) {
                    return; // <target> not present at all in source file,
                            // should remain like this in output
                }
                generateTargetStartElement(writer); // only if there was a
                                                    // target in the source file
                for (XMLEvent ev : target) {
                    fromEventToWriter(ev, writer);
                }
            }
        } else {
            inSubSeg = 0;
            generateTargetStartElement(writer);
            for (XMLEvent ev : segSource) {
                if (ev.isStartElement()) {
                    StartElement el = ev.asStartElement();
                    if (el.getName().getLocalPart().equals("mrk")) {
                        if (el.getAttributeByName(new QName("mtype")).getValue().equals("seg")) {
                            fromEventToWriter(ev, writer);
                            String mid = el.getAttributeByName(new QName("mid")).getValue();
                            String src = buildTags(subSegments.get(mid), false);
                            // First, translation from project memory
                            String tra = entryTranslateCallback.getTranslation(unitId + "/" + mid, src, path);
                            if (tra != null) {
                                for (XMLEvent tev : restoreTags(unitId, path, src, tra)) {
                                    fromEventToWriter(tev, writer);
                                }
                            } else {
                                // Second, check in the target buffer
                                // (translation in source file)
                                List<XMLEvent> fromTarget = findSubsegment(target, mid);
                                if (fromTarget != null && !fromTarget.isEmpty()) {
                                    for (XMLEvent tev : fromTarget) {
                                        fromEventToWriter(tev, writer);
                                    }
                                } else { // if failed, use the source
                                    for (XMLEvent tev : subSegments.get(mid)) {
                                        fromEventToWriter(tev, writer);
                                    }
                                }
                            }
                            inSubSeg++;
                            // writer.writeEndElement(/*mrk*/);
                        } else {
                            if (inSubSeg > 0) {
                                inSubSeg++; // avoids to crash on <mrk> inside
                                            // segment
                            }
                        }
                    }
                }
                if (ev.isEndElement()) {
                    EndElement el = ev.asEndElement();
                    if (el.getName().getLocalPart().equals("mrk")) {
                        if (inSubSeg > 0) {
                            inSubSeg--;
                        }
                    }
                }
                if (inSubSeg == 0) {
                    fromEventToWriter(ev, writer);
                }
            }
        }
        writer.writeEndElement(/* target */);
        flushedUnit = true;
    }

    /**
     * Builds target from OmegaT to XLIFF format. May be overridden in
     * subclasses
     **/
    protected List<XMLEvent> restoreTags(String aUnitId, String path, String src, String tra) {
        return restoreTags(tra);
    }

    /**
     * Extracts from <seg-source> or <target> the part between
     * <mrk type=seg mid=xxx> and </mrk>
     **/
    private List<XMLEvent> findSubsegment(List<XMLEvent> list, String mid) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        List<XMLEvent> buf = new LinkedList<>();
        int depth = 0;
        for (XMLEvent ev : target) {
            if (ev.isEndElement()) {
                EndElement el = ev.asEndElement();
                if (el.getName().getLocalPart().equals("mrk")) {
                    if (depth == 1) {
                        return buf;
                    } else if (depth > 0) {
                        depth--;
                    }
                }
            }
            if (depth > 0) {
                buf.add(ev);
            }
            if (ev.isStartElement()) {
                StartElement el = ev.asStartElement();
                if (el.getName().getLocalPart().equals("mrk")) {
                    if (el.getAttributeByName(new QName("mtype")).getValue().equals("seg")
                            && el.getAttributeByName(new QName("mid")).getValue().equals(mid)) {
                        depth = 1;
                    } else if (depth > 0) {
                        depth++;
                    }
                }
            }
        }
        return buf;
    }

}
