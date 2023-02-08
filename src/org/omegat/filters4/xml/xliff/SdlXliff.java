/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017-2020 Thomas Cordonnier
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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import org.omegat.core.Core;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.IProject;
import org.omegat.core.data.TMXEntry;
import org.omegat.filters2.Instance;
import org.omegat.filters2.FilterContext;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * Overrides XLiff-1 for features which only work with SDL-Xliff.
 *
 * @author Thomas Cordonnier
 */
public class SdlXliff extends Xliff1Filter {

    private final SimpleDateFormat TRADOS_DATE_FORMAT = new SimpleDateFormat("M/d/y H:m:s");

    // ---------------------------- IFilter API ----------------------------

    @Override
    public String getFileFormatName() {
        return OStrings.getString("SDLXLIFF_FILTER_NAME");
    }

    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.sdlxliff") };
    }

    @Override
    public boolean isFileSupported(java.io.File inFile, Map<String, String> config, FilterContext context) {
        try {
            StartElement el = findEvent(inFile, java.util.regex.Pattern.compile(".*/.*:xliff"));
            if (el == null) {
                return false;
            }
            namespace = el.getName().getNamespaceURI();
            if (el.getAttributeByName(
                    new QName("http://sdl.com/FileTypes/SdlXliff/1.0", "version")) != null) {
                return true;
            }
            return super.isFileSupported(inFile, config, context);
        } catch (Exception npe) {
            return false; // version attribute is mandatory
        }
    }

    // ----------------------------- specific part ----------------------

    private String currentMid = null;
    private Map<String, StringBuffer> sdlComments = new TreeMap<>();
    private StringBuffer commentBuf = null;
    private Map<UUID, String> omegatNotes = new TreeMap<>();
    private Map<String, UUID> defaultNoteLocations = new TreeMap<>();
    private Map<EntryKey, UUID> altNoteLocations = new TreeMap<>();
    private Map<String, List<XMLEvent>> tagDefs = new TreeMap<>();
    private String currentProp = null;
    private Set<String> midSet = new java.util.HashSet<>();
    private boolean has_seg_defs = false;
    private boolean mid_has_modifier = false;
    private boolean mid_has_modif_date = false;


    /**
     * Also starts on cmt-defs or tag-defs, else like in standard XLIFF.
     */
    @Override
    protected void checkCurrentCursorPosition(javax.xml.stream.XMLStreamReader reader, boolean doWrite) {
        if (reader.getEventType() == StartElement.START_ELEMENT) {
            String name = reader.getLocalName();
            if (name.equals("cmt-defs")) {
                this.isEventMode = true;
            }
            if (name.equals("tag-defs")) {
                this.isEventMode = true;
            }
        }
        super.checkCurrentCursorPosition(reader, doWrite);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean processStartElement(StartElement startElement, XMLStreamWriter writer)
            throws XMLStreamException {
        if (startElement.getName().getLocalPart().equals("cmt-def")) {
            commentBuf = new StringBuffer();
            sdlComments.put(startElement.getAttributeByName(new QName("id")).getValue(), commentBuf);
            return true;
        }
        if (startElement.getName().getLocalPart().equals("mrk")) {
            if (startElement.getAttributeByName(new QName("mtype")).getValue().equals("seg")) {
                currentMid = startElement.getAttributeByName(new QName("mid")).getValue();
                midSet.add(currentMid);
            } else if (startElement.getAttributeByName(new QName("mtype")).getValue()
                    .equals("x-sdl-comment")) {
                String id = startElement
                        .getAttributeByName(new QName("http://sdl.com/FileTypes/SdlXliff/1.0", "cid"))
                        .getValue();
                this.addNoteFromSource(currentMid, sdlComments.get(id).toString());
            }
        }
        if (startElement.getName().getLocalPart().equals("tag")) {
            currentBuffer = new java.util.LinkedList<XMLEvent>();
            tagDefs.put(startElement.getAttributeByName(new QName("id")).getValue(), currentBuffer);
        }
        if (writer != null) {
            if (startElement.getName().equals(new QName("http://sdl.com/FileTypes/SdlXliff/1.0", "seg"))) {
                mid_has_modifier = false;
                mid_has_modif_date = false;
                currentProp = null;
                has_seg_defs = true; // start a new set of properties
                fromEventToWriter(eFactory.createStartElement(startElement.getName(), null,
                        startElement.getNamespaces()), writer);
                String id = null;
                for (java.util.Iterator<Attribute> iter = startElement.getAttributes(); iter.hasNext();) {
                    Attribute attr = iter.next();
                    if (attr.getName().getLocalPart().equals("id")) {
                        id = attr.getValue();
                    }
                    if (!attr.getName().getLocalPart().equals("conf")) {
                        writer.writeAttribute(attr.getName().getPrefix(), attr.getName().getNamespaceURI(),
                                attr.getName().getLocalPart(), attr.getValue());
                    }
                }
                if ((id != null) && this.isCurrentSegmentTranslated(id)) {
                    writer.writeAttribute("conf", "Translated");
                }
                return false; // we already added current element
            }
        }
        if (startElement.getName().getLocalPart().equals("trans-unit")) {
            has_seg_defs = false;
        }
        if (startElement.getName().equals(new QName("http://sdl.com/FileTypes/SdlXliff/1.0", "value"))) {
            currentProp = startElement.getAttributeByName(new QName("key")).getValue();
        }
        return super.processStartElement(startElement, writer);
    }

    @Override
    protected boolean processEndElement(EndElement endElement, XMLStreamWriter writer)
            throws XMLStreamException {
        if (endElement.getName().getLocalPart().equals("seg")) {
            if ((writer != null) && isCurrentSegmentTranslated(currentMid)) {
                if (!mid_has_modifier) { // no such value in the file
                    writer.writeStartElement("http://sdl.com/FileTypes/SdlXliff/1.0", "value");
                    writer.writeAttribute("key", "last_modified_by");
                    writer.writeCharacters(Preferences.getPreferenceDefault(Preferences.TEAM_AUTHOR,
                            System.getProperty("user.name")));
                    writer.writeEndElement(/* "sdl:value */);
                }
                if (!mid_has_modif_date) { // no such value in the file
                    writer.writeStartElement("http://sdl.com/FileTypes/SdlXliff/1.0", "value");
                    writer.writeAttribute("key", "modified_on");
                    writer.writeCharacters(TRADOS_DATE_FORMAT.format(new java.util.Date()));
                    writer.writeEndElement(/* "sdl:value */);
                }
            }
            midSet.remove(currentMid);
            currentMid = null;
        }
        if (endElement.getName().getLocalPart().equals("trans-unit")) {
            if (writer != null) {
                if ((midSet.size() > 0) && (!has_seg_defs)) {
                    writer.writeStartElement("http://sdl.com/FileTypes/SdlXliff/1.0", "seg-defs");
                }
                for (String mid0 : midSet) { // those which were not generated
                                             // by previous lines
                    writer.writeStartElement("http://sdl.com/FileTypes/SdlXliff/1.0", "seg");
                    writer.writeAttribute("id", mid0);
                    writer.writeAttribute("conf", "Translated");
                    if (isCurrentSegmentTranslated(mid0)) {
                        writer.writeStartElement("http://sdl.com/FileTypes/SdlXliff/1.0", "value");
                        writer.writeAttribute("key", "last_modified_by");
                        writer.writeCharacters(Preferences.getPreferenceDefault(Preferences.TEAM_AUTHOR,
                                System.getProperty("user.name")));
                        writer.writeEndElement(/* "sdl:value */);

                        writer.writeStartElement("http://sdl.com/FileTypes/SdlXliff/1.0", "value");
                        writer.writeAttribute("key", "modified_on");
                        writer.writeCharacters(TRADOS_DATE_FORMAT.format(new java.util.Date()));
                        writer.writeEndElement(/* "sdl:value */);
                    }
                    writer.writeEndElement(/* "sdl:seg */);
                }
                if ((midSet.size() > 0) && (!has_seg_defs)) {
                    writer.writeEndElement(/* "sdl:seg-defs */);
                }
            }
            midSet.clear();
        }
        if (endElement.getName().getLocalPart().equals("tag")) {
            currentBuffer = null;
        }
        if (endElement.getName().getLocalPart().equals("cmt-def")) {
            commentBuf = null;
        }
        if (endElement.getName().getLocalPart().equals("cmt-defs")) {
            this.isEventMode = false;
            if (writer != null) {
                IProject proj = Core.getProject();
                proj.iterateByDefaultTranslations((String source, TMXEntry trans) -> {
                    if (!trans.hasNote()) {
                        return;
                    }

                    UUID id = UUID.randomUUID();
                    omegatNotes.put(id, trans.note);
                    defaultNoteLocations.put(source, id);
                    createSdlNote(id, trans, writer);
                });
                proj.iterateByMultipleTranslations((EntryKey key, TMXEntry trans) -> {
                    if (!trans.hasNote()) {
                        return;
                    }

                    UUID id = UUID.randomUUID();
                    omegatNotes.put(id, trans.note);
                    altNoteLocations.put(key, id);
                    createSdlNote(id, trans, writer);
                });
            }
            return false; // when isEventMode changes, next iteration will send
                          // the current event
        }
        return super.processEndElement(endElement, writer);
    }

    // Do not generate tag for comment inside source
    protected boolean isUntaggedTag(StartElement stEl) {
        return (stEl.getName().equals(new QName("urn:oasis:names:tc:xliff:document:1.2", "mrk"))
                && (stEl.getAttributeByName(new QName("mtype")).getValue().equals("x-sdl-comment")
                        || stEl.getAttributeByName(new QName("mtype")).getValue().equals("x-sdl-added")))
                || super.isUntaggedTag(stEl);
    }

    // Track change 'DELETED' should not appear at all in the
    protected boolean isDeletedTag(StartElement stEl) {
        return (stEl.getName().equals(new QName("urn:oasis:names:tc:xliff:document:1.2", "mrk"))
                && stEl.getAttributeByName(new QName("mtype")).getValue().equals("x-sdl-deleted"))
                || super.isUntaggedTag(stEl);
    }

    @Override
    protected char findPrefix(StartElement stEl) {
        if (stEl.getName().getLocalPart().equals("g")) {
            try {
                String tagId = stEl.getAttributeByName(new QName("id")).getValue();
                List<XMLEvent> contents = tagDefs.get(tagId);
                for (XMLEvent ev : contents) {
                    if (ev.isCharacters()) {
                        String txt = ev.asCharacters().getData();
                        if (txt.contains("italic") && !txt.contains("bold")) {
                            return 'i';
                        }
                        if (!txt.contains("italic") && (txt.contains("bold") || txt.contains("strong"))) {
                            return 'b';
                        }
                        if (txt.contains("size")) {
                            return 's';
                        }
                        if (txt.contains("color")) {
                            return 'c';
                        }
                        if (txt.contains("footnote")) {
                            return 'n';
                        }
                        if (txt.contains("cf")) {
                            return 'f'; // format
                        }
                    } else if (ev.isStartElement()) {
                        String name = ev.asStartElement().getName().getLocalPart();
                        if (name.equals("bpt") || name.equals("ept")) {
                            name = ev.asStartElement().getAttributeByName(new QName("name")).getValue();
                            if (name.equals("italic") || name.equals("em")) {
                                return 'i';
                            }
                            if (name.equals("bold") || name.equals("strong")) {
                                return 'b';
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        // default
        return super.findPrefix(stEl);
    }

    private static void createSdlNote(UUID id, TMXEntry trans, XMLStreamWriter writer) {
        try {
            writer.writeStartElement("http://sdl.com/FileTypes/SdlXliff/1.0", "cmt-def");
            writer.writeAttribute("id", id.toString());
            writer.writeStartElement("http://sdl.com/FileTypes/SdlXliff/1.0", "Comments");
            writer.writeStartElement("http://sdl.com/FileTypes/SdlXliff/1.0", "Comment");
            writer.writeCharacters(trans.note);
            writer.writeEndElement(/* Comment */);
            writer.writeEndElement(/* Comments */);
            writer.writeEndElement(/* cmt-def */);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean processCharacters(Characters event, XMLStreamWriter writer) throws XMLStreamException {
        if (commentBuf != null) {
            commentBuf.append(event.toString());
            if ((writer != null) && isCurrentSegmentTranslated(currentMid)) {
                if ("last_modified_by".equals(currentProp)) {
                    writer.writeCharacters(Preferences.getPreferenceDefault(Preferences.TEAM_AUTHOR,
                            System.getProperty("user.name")));
                    mid_has_modifier = true;
                    return false;
                }
                if ("modified_on".equals(currentProp)) {
                    writer.writeCharacters(TRADOS_DATE_FORMAT.format(new java.util.Date()));
                    mid_has_modif_date = true;
                    return false;
                }
            }
        }
        return super.processCharacters(event, writer);
    }

    // This method is called only during translation generation: so we can use
    // it to add notes!
    @Override
    protected List<XMLEvent> restoreTags(String unitId, String path, String src, String tra) {
        List<XMLEvent> res = super.restoreTags(unitId, path, src, tra);
        EntryKey key = new EntryKey("", src, unitId, null, null, path);
        UUID addNote = null;
        if (altNoteLocations.get(key) != null) {
            addNote = altNoteLocations.get(key);
        } else if (defaultNoteLocations.get(src) != null) {
            addNote = defaultNoteLocations.get(src);
        }
        if ((addNote != null) && (omegatNotes.get(addNote) != null)) {
            List<Attribute> attr = new java.util.LinkedList<Attribute>();
            attr.add(eFactory.createAttribute("sdl", "http://sdl.com/FileTypes/SdlXliff/1.0", "cid",
                    addNote.toString()));
            attr.add(eFactory.createAttribute(new QName("mtype"), "x-sdl-comment"));
            res.add(0, eFactory.createStartElement(new QName("urn:oasis:names:tc:xliff:document:1.2", "mrk"),
                    attr.iterator(), null));
            res.add(eFactory.createEndElement(new QName("urn:oasis:names:tc:xliff:document:1.2", "mrk"),
                    null));
        }
        return res;
    }

    /** Remove entries with only tags **/
    @Override
    protected boolean isToIgnore(String src, String tra) {
        if (tra == null) {
            return false;
        }
        while (src.startsWith("<")) {
            src = src.substring(Math.max(1, src.indexOf(">") + 1));
        }
        while (tra.startsWith("<")) {
            tra = tra.substring(Math.max(1, tra.indexOf(">") + 1));
        }
        return (src.length() == 0) && (tra.length() == 0);
    }

    @Override
    protected String buildProtectedPartDetails(List<XMLEvent> saved) {
        String base = super.buildProtectedPartDetails(saved);
        Matcher matcher = Pattern.compile("(\\w) id=\"?([\\d\\w\\-]+)\"?").matcher(base);
        if (matcher.find()) {
            List<XMLEvent> tagDefList = tagDefs.get(matcher.group(2));
            if (tagDefList != null) {
                java.io.Writer writer = new java.io.StringWriter();
                try {
                    javax.xml.stream.XMLEventWriter eventWriter = oFactory.createXMLEventWriter(writer);
                    for (XMLEvent ev : tagDefList) {
                        eventWriter.add(ev);
                    }
                } catch (XMLStreamException xe) {
                    try {
                        for (XMLEvent ev : saved)
                            if (ev.isEndElement()) {
                                writer.write("</" + ev.asEndElement().getName().getPrefix() + ":"
                                        + ev.asEndElement().getName().getLocalPart() + ">");
                            } else {
                                writer.write(ev.toString());
                            }
                    } catch (Exception ignored) {
                    }
                }
                return base + ": " + writer.toString();
            }
        }
        return base;
    }

    @Override
    protected boolean isStandardTranslationState() {
        return false; // because SDLXLIFF does not have attributes in target
    }
}
