/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017-2022 Thomas Cordonnier
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
import java.util.Map;
import java.util.TreeMap;
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
 * Filter for support Xliff 1 files as bilingual (unlike filters3/xml/xliff).
 *
 * @author Thomas Cordonnier
 */
public class Xliff1Filter extends AbstractXliffFilter {

    // ---------------------------- IFilter API---------------------------

    @Override
    public String getFileFormatName() {
        return OStrings.getString("XLIFF1FILTER_FILTER_NAME");
    }

    protected final String versionPrefix() {
        return "1."; // can be 1.0, 1.1 or 1.2
    }

    // ----------------------- AbstractXmlFilter part----------------------

    /** Current translation unit **/
    private String unitId = null;
    private boolean flushedUnit = false;
    private int lastGroupId = 0; // only used when group id is not present in
                                 // the file
    private final List<XMLEvent> segSource = new LinkedList<>();
    private final Map<String, List<XMLEvent>> subSegments = new TreeMap<>();
    private StartElement targetStartEvent = null;
    private int inSubSeg = 0;

    protected void cleanBuffers() {
        source.clear();
        target = null;
        note.clear();
        segSource.clear();
        subSegments.clear();
    }

    @Override
    @SuppressWarnings("fallthrough")
    protected boolean processStartElement(StartElement startElement, XMLStreamWriter writer)
            throws XMLStreamException {
        switch (startElement.getName().getLocalPart()) {
        case "xliff":
            if (namespace == null) {
                namespace = startElement.getName().getNamespaceURI();
            }
            break;
        case "file":
            try {
                path += "/" + startElement.getAttributeByName(new QName("original")).getValue();
            } catch (NullPointerException noid) {
                // Note: in spec, original is REQUIRED
                throw new XMLStreamException(
                        OStrings.getString("XLIFF_MANDATORY_ORIGINAL_MISSING", "original", "file"));
            }
            updateIgnoreScope(startElement);
            break;
        case "group":
            try {
                path += "/" + startElement.getAttributeByName(new QName("id")).getValue();
            } catch (NullPointerException noid) {
                // in XLIFF 1, this attribute is not REQUIRED
                try {
                    path += "/" + startElement.getAttributeByName(new QName("resname")).getValue();
                } catch (NullPointerException noresname) {
                    // generate an unique id:
                    // must be unique at document scope but must be identical
                    // when you re-parse the document
                    path += "/x-auto-" + lastGroupId;
                    lastGroupId++;
                }
            }
            updateIgnoreScope(startElement);
            break;
        case "trans-unit":
            try {
                unitId = startElement.getAttributeByName(new QName("id")).getValue();
            } catch (NullPointerException noid) { // Note: in spec, original is
                                                  // REQUIRED
                throw new XMLStreamException(
                        OStrings.getString("XLIFF_MANDATORY_ORIGINAL_MISSING", "id", "trans-unit"));
            }
            flushedUnit = false;
            targetStartEvent = null;
            updateIgnoreScope(startElement);
            break;
        case "source":
            currentBuffer = source;
            source.clear();
            break;
        case "target":
            target = new LinkedList<XMLEvent>();
            currentBuffer = target;
            inTarget = true;
            targetStartEvent = startElement;
            break;
        case "note":
            currentBuffer = note;
            note.clear();
            break;
        case "seg-source":
            currentBuffer = segSource;
            segSource.clear();
            break;
        case "mrk":
            if (startElement.getAttributeByName(new QName("mtype")).getValue().equals("seg")) {
                String mid = startElement.getAttributeByName(new QName("mid")).getValue();
                currentBuffer.add(startElement);
                currentBuffer = new LinkedList<XMLEvent>();
                if (!inTarget) {
                    subSegments.put(mid, currentBuffer);
                }
                inSubSeg++;
                break;
            } else if (inSubSeg > 0) {
                inSubSeg++; // avoids to crash on <mrk> inside segment.
            }
            // Do not break because inside segment we want <m0>
        default:
            if (currentBuffer != null) {
                currentBuffer.add(startElement);
            } else if (((ignoreScope == null || ignoreScope.startsWith("!")) && (unitId != null))
                    && (!startElement.getName().getNamespaceURI()
                            .equals("urn:oasis:names:tc:xliff:document:1.2"))) {
                flushTranslations(writer);
                // <target> must be before any oter-namespace markup
            }
        }
        return !inTarget;
    }

    @Override
    @SuppressWarnings("fallthrough")
    protected boolean processEndElement(EndElement endElement, XMLStreamWriter writer)
            throws XMLStreamException {
        switch (endElement.getName().getLocalPart()) {
        case "source":
        case "seg-source":
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
        case "trans-unit":
            if (ignoreScope == null || ignoreScope.startsWith("!")) {
                flushTranslations(writer); // if there was no <target> at all
            }
            if (ignoreScope == null || ignoreScope.startsWith("!")) { // registerCurrentTransUnit(unitId);
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
            break;
        case "group":
        case "file":
            path = path.substring(0, path.lastIndexOf('/'));
            cleanBuffers();
            if (endElement.getName().getLocalPart().equals(ignoreScope)) {
                ignoreScope = null;
            } else if (ignoreScope != null
                    && ignoreScope.startsWith("!" + endElement.getName().getLocalPart())) {
                ignoreScope = ignoreScope.substring(endElement.getName().getLocalPart().length() + 2);
            }
            break;
        case "mrk":
            if (inSubSeg == 1) {
                List<XMLEvent> save = inTarget ? target : segSource;
                save.addAll(currentBuffer);
                currentBuffer = save;
                currentBuffer.add(endElement);
                inSubSeg = 0;
                break;
            } else {
                if (inSubSeg > 0)
                    inSubSeg--;
            } // avoids to crash on <mrk> inside segment.
              // Do not break because inside segment we want </m0>
        default:
            if (currentBuffer != null) {
                currentBuffer.add(endElement);
            }
        }
        return !inTarget;
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
        /*
         * if (target == null) return false; if (subSegments.isEmpty()) return
         * true; // target not null
         */
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
            for (Character c : tagsCount.keySet()) {
                tagsCount.put(c, 0);
            }
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
                    nativeCode = new LinkedList<XMLEvent>();
                    res.append(startPair(reuse, false, stEl, prefix, count, nativeCode));
                    saveBuf.push(res);
                    res = new StringBuffer();
                    nativeCode.add(ev);
                    break;
                case "ept": // paired native code, end
                    nativeCode = new LinkedList<XMLEvent>();
                    res.append(endPair(reuse, stEl, prefix, count, nativeCode));
                    saveBuf.push(res);
                    res = new StringBuffer();
                    nativeCode.add(ev);
                    break;
                default: // g, mrk, ph, it and other-namespace
                    if (isProtectedTag(stEl)) { // ph, it, mrk:mtype=protected,
                                                // maybe other
                        nativeCode = new LinkedList<XMLEvent>();
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
                default: {
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
        }
        return res.toString();
    }

    protected char findPrefix(StartElement stEl) {
        Attribute ctype = stEl.getAttributeByName(new QName("ctype"));
        if (ctype == null || ctype.getValue() == null || ctype.getValue().length() == 0) {
            ctype = stEl.getAttributeByName(new QName("type"));
        }
        if (ctype != null && ctype.getValue().length() > 0) {
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

    protected final void generateTargetStartElement(XMLStreamWriter writer) throws XMLStreamException {
        if (!isStandardTranslationState()) {
            if (targetStartEvent == null) {
                writer.writeStartElement(namespace, "target");
            } else {
                fromEventToWriter(targetStartEvent, writer);
            }
            return;
        }

        boolean isTranslated;
        if (subSegments.isEmpty()) {
            isTranslated = entryTranslateCallback.getTranslation(unitId, buildTags(source, false),
                    path) != null;
        } else { // set 'translated' only if all sub-segments are translated
            isTranslated = true;
            for (String mid : subSegments.keySet()) {
                isTranslated = isTranslated && (null != entryTranslateCallback
                        .getTranslation(unitId + "/" + mid, buildTags(subSegments.get(mid), false), path));
            }
        }
        if (isTranslated) {
            writer.writeStartElement(namespace, "target");
            writer.writeAttribute("state", "translated");
            if (targetStartEvent != null) {
                for (java.util.Iterator<Attribute> iter = targetStartEvent.getAttributes(); iter.hasNext();) {
                    Attribute next = iter.next();
                    if (!"state".equals(next.getName().getLocalPart())) {
                        writer.writeAttribute(next.getName().getPrefix(), next.getName().getNamespaceURI(),
                                next.getName().getLocalPart(), next.getValue());
                    }
                }
            }
        } else {
            if (targetStartEvent == null) {
                writer.writeStartElement(namespace, "target");
            } else {
                fromEventToWriter(targetStartEvent, writer);
            }
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
                                if (fromTarget != null && fromTarget.size() > 0) {
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
    protected List<XMLEvent> restoreTags(String unitId, String path, String src, String tra) {
        return restoreTags(tra);
    }

    /**
     * Extracts from <seg-source> or <target> the part between
     * <mrk type=seg mid=xxx> and </mrk>
     **/
    private List<XMLEvent> findSubsegment(List<XMLEvent> list, String mid) {
        if (list == null) {
            return null;
        }
        if (list.size() == 0) {
            return null;
        }

        List<XMLEvent> buf = new LinkedList<XMLEvent>();
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
