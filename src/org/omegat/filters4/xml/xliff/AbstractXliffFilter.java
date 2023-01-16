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

package org.omegat.filters4.xml.xliff;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Attribute;

import org.omegat.filters4.xml.AbstractXmlFilter;
import org.omegat.filters2.Instance;
import org.omegat.filters2.FilterContext;

/**
 * Filter for support Xliff 1 files as bilingual (unlike filters3/xml/xliff)
 *
 * @author Thomas Cordonnier
 */
abstract class AbstractXliffFilter extends AbstractXmlFilter {

    // ---------------------------- IFilter API ----------------------------

    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[]  { new Instance("*.xlf"), new Instance("*.xliff") };
    }

    protected String namespace = null;

    @Override
    public boolean isFileSupported(java.io.File inFile, Map<String, String> config, FilterContext context) {
        try {
            StartElement el = findEvent(inFile, Pattern.compile(".*/.*:xliff"));
            if (el == null) {
                return false;
            }
            if (el.getAttributeByName(new QName("http://sdl.com/FileTypes/SdlXliff/1.0", "version")) != null) {
                return false;
            }
            namespace = el.getName().getNamespaceURI();
            if (namespace.startsWith("urn:oasis:names:tc:xliff:document:" + versionPrefix())) {
                return true;
            }
            if (namespace.startsWith("urn:oasis:names:tc:xliff:document:")) {
                return false;
            }
            String version = el.getAttributeByName(new QName("version")).getValue();
            namespace = "urn:oasis:names:tc:xliff:document:" + version;
            return version.startsWith(versionPrefix());
        } catch (Exception npe) {
            return false; // version attribute is mandatory
        }
    }

    protected abstract String versionPrefix();

    // ----------------------------- AbstractXmlFilter part ----------------------

    /* -- Data about current unit */
    protected String path = "/", ignoreScope = null;
    protected List<XMLEvent> currentBuffer = null;
    protected boolean inTarget = false;
    protected List<XMLEvent> source = new LinkedList<>(), target = null, note = new LinkedList<>();

    protected void cleanBuffers() {
        source.clear(); target = null; note.clear();
    }

    @Override    // start events on body
    protected void checkCurrentCursorPosition(XMLStreamReader reader, boolean doWrite) {
        if (reader.getEventType() == StartElement.START_ELEMENT) {
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
                    attributes.add(eFactory.createAttribute(reader.getAttributeName(i), reader.getAttributeValue(i)));
                }
                try {
                     processStartElement(eFactory.createStartElement(reader.getName(), attributes.iterator(),
                         null), null);
                } catch (Exception ex) {
                }
            }
        }
    }

    @Override
    protected boolean processCharacters(Characters event, XMLStreamWriter evWriter) throws XMLStreamException {
        if (currentBuffer != null) {
            currentBuffer.add(event);
        }
        return !inTarget;
    }

    protected void updateIgnoreScope(StartElement startElement) {
        if (startElement.getAttributeByName(new QName("translate")) != null) {
            if ("no".equals(startElement.getAttributeByName(new QName("translate")).getValue())) {
                ignoreScope = startElement.getName().getLocalPart();
            } else if ("yes".equals(startElement.getAttributeByName(new QName("translate")).getValue())) {
                if (ignoreScope != null)  {
                    ignoreScope = "!" + startElement.getName().getLocalPart() + " " + ignoreScope;
                }
            }
        }
    }

    protected abstract String buildTags(List<XMLEvent> srcList, boolean reuse);

    /** Enables not to add some entries. To be overridden by subtypes **/
    protected boolean isToIgnore(String src, String tra) {
        return false;
    }

    /** Add one unit to OmegaT, or more in case of <mrk mtype="seg"> **/
    protected void registerCurrentTransUnit(String entryId, List<XMLEvent> unitSource,
        List<XMLEvent> unitTarget, String notePattern) {
        String src = buildTags(unitSource, false);
        String tra = null;
        if (unitTarget != null && unitTarget.size() > 0) {
            tra = buildTags(unitTarget, true);
        }
        if (isToIgnore(src, tra)) {
            return; // may ignore some pre-translated src->tra pairs
        }
        if (entryParseCallback != null) {
            StringBuffer noteStr = null;
            if (notePattern != null && note != null && note.size() > 0) {
                 noteStr = new StringBuffer();
                 for (XMLEvent ev: note) {
                     noteStr.append(ev.toString());
                 }
            }
            if (notePattern != null && !".*".equals(notePattern)) {
                StringBuffer subNoteBuf = noteStr; if (subNoteBuf != null) {
                    subNoteBuf = new StringBuffer(); String noteStrStr = noteStr.toString();
                    Matcher noteMatch = Pattern.compile(notePattern).matcher(noteStr.toString());
                    while (noteMatch.find()) {
                        if (noteMatch.group(1).equals(entryId.substring(entryId.lastIndexOf("/") + 1))) {
                            noteMatch.appendReplacement(subNoteBuf, noteMatch.group(2));
                        } else {
                            noteMatch.appendReplacement(subNoteBuf, "");
                        }
                    }
                    noteMatch.appendTail(subNoteBuf);
                }
                noteStr = subNoteBuf;
            }
            entryParseCallback.addEntry(entryId, src.toString(), tra, false,
                noteStr == null ? null : noteStr.toString(), path, this, buildProtectedParts(src));
        }
        if (entryAlignCallback != null) {
            entryAlignCallback.addTranslation(entryId, src.toString(), tra.toString(), false, path, this);
        }
    }

    protected Map<Character, Integer> tagsCount = new TreeMap<>();
    protected Stack<String> tagStack = new Stack<>();
    protected Map<String, String> pairedHolders = new TreeMap<>();

    protected abstract String[] getPairIdNames(boolean start);

    // Starts an OmegaT tag based on start element and native code
    protected String startPair(boolean reuse, boolean isEmpty, StartElement stEl, char prefix,
        int count, List<XMLEvent> nativeCode) {
        if (reuse) {
            return findKey(stEl, isEmpty);
        } else {
            tagsMap.put("" + prefix + count, nativeCode);
            if (!isEmpty) {
                Attribute pairId = null;
                for (String attrName: getPairIdNames(true)) {
                     pairId = stEl.getAttributeByName(new QName(attrName));
                     if (pairId != null) {
                         break;
                     }
                }
                pairedHolders.put(pairId.getValue(), "" + prefix + count);
            }
            return "<" + prefix + count + (isEmpty ? "/" : "") + ">";
        }
    }

    // Ends an OmegaT tag based on start element and native code
    protected String endPair(boolean reuse, StartElement stEl, char prefix, int count, List<XMLEvent> nativeCode) {
        tagsCount.put(prefix, count); // this is not a new tag!
        Attribute pairId = null;
        for (String attrName: getPairIdNames(false)) {
             pairId = stEl.getAttributeByName(new QName(attrName));
             if (pairId != null) {
                 break;
             }
        }
        String key = pairedHolders.get(pairId.getValue());
        if (!reuse) {
            tagsMap.put("/" + key, nativeCode);
        }
        return "</" + key + ">";
    }

    protected void startStackElement(boolean reuse, StartElement stEl, char prefix, int count, StringBuffer res) {
        if (reuse) {
            String k = findKey(stEl, false); Matcher m = OMEGAT_TAG.matcher(k);
            if (m.matches()) {
                 tagStack.push(m.group(2)); res.append(k);
            } else {
                 tagStack.push("z" + count); res.append("<z").append(count).append(">");
            }
        } else {
            tagsMap.put("" + prefix + count, Collections.singletonList(stEl));
            res.append("<").append(prefix).append(count).append(">");
            tagStack.push("" + prefix + count);
        }
    }

    protected static final javax.xml.stream.XMLEventFactory eFactory = javax.xml.stream.XMLEventFactory.newInstance();
}
