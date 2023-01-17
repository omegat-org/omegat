/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2018 Thomas Cordonnier
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

package org.omegat.filters4.xml.openxml;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Attribute;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.filters3.xml.openxml.OpenXMLOptions;
import org.omegat.filters4.xml.AbstractXmlFilter;
import org.omegat.filters2.Instance;
import org.omegat.filters2.FilterContext;
import org.omegat.util.OStrings;

/**
 * Filter for MS Office's XML files (those which are inside the DOCX, XLSX,
 * PPTX...).
 *
 * @author Thomas Cordonnier
 */
class OpenXmlFilter extends AbstractXmlFilter {
    private boolean removeComments;

    OpenXmlFilter(boolean withComments) {
        this.removeComments = !withComments;
    }

    // ---------------------------- IFilter API ----------------------------

    @Override
    public String getFileFormatName() {
        return OStrings.getString("OPENXML4_FILTER_NAME");
    }

    @Override
    protected boolean requirePrevNextFields() {
        return true;
    }

    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.xml") };
    }

    private boolean doCompactTags = false;

    @Override
    public boolean isFileSupported(java.io.File inFile, Map<String, String> config, FilterContext context) {
        this.doCompactTags = new OpenXMLOptions(config).getAggregateTags();
        return inFile.getName().toLowerCase().endsWith(".xml");
    }

    // ----------------------------- AbstractXmlFilter part
    // ----------------------

    private QName OOXML_MAIN_PARA_ELEMENT = null;

    private LinkedList<List<XMLEvent>> currentPara = null;
    private List<XMLEvent> currentBuffer = null;

    @Override // start events on body
    protected void checkCurrentCursorPosition(javax.xml.stream.XMLStreamReader reader, boolean doWrite) {
        this.isEventMode = true; // for the moment, always work in events mode
    }

    @Override
    protected boolean processStartElement(StartElement startElement, XMLStreamWriter writer)
            throws XMLStreamException {
        if (OOXML_MAIN_PARA_ELEMENT == null) {
            OOXML_MAIN_PARA_ELEMENT = startElement.getName();
        }
        if (OOXML_MAIN_PARA_ELEMENT.getNamespaceURI().contains("presentation")) { // powerpoint
            OOXML_MAIN_PARA_ELEMENT = new QName(startElement.getNamespaceContext().getNamespaceURI("a"),
                    OOXML_MAIN_PARA_ELEMENT.getLocalPart(), "a");
        }
        QName name = startElement.getName();
        if (OOXML_MAIN_PARA_ELEMENT.getNamespaceURI().equals(name.getNamespaceURI())) {
            if ("p".equals(name.getLocalPart()) // word
                    || "si".equals(name.getLocalPart()) || "comment".equals(name.getLocalPart()) // excel
            ) {
                currentBuffer = new LinkedList<>();
                currentPara = new LinkedList<>();
                currentPara.add(currentBuffer);
                currentBuffer.add(startElement);
                return false;
            }
            if ("r".equals(name.getLocalPart())) {
                if (!currentBuffer.isEmpty()) {
                    currentBuffer = new LinkedList<>();
                    currentPara.add(currentBuffer);
                }
                currentBuffer.add(startElement);
                return false;
            }
            if ((writer != null)
                    && ("lang".equals(name.getLocalPart()) || "themeFontLang".equals(name.getLocalPart()))) {
                fromEventToWriterOrBuffer(eFactory.createStartElement(startElement.getName(), null,
                        startElement.getNamespaces()), writer);
                for (Iterator<Attribute> iter = startElement.getAttributes(); iter.hasNext();) {
                    Attribute attr = iter.next();
                    ProjectProperties prop = Core.getProject().getProjectProperties();
                    String aval = attr.getValue(), pval = prop.getSourceLanguage().toString();
                    if (aval.equalsIgnoreCase(pval)) {
                        fromEventToWriterOrBuffer(
                                eFactory.createAttribute(attr.getName(), prop.getTargetLanguage().toString()),
                                writer);
                    } else {
                        if ((aval.length() > 2) && (aval.charAt(2) == '-')) {
                            aval = aval.substring(0, 2);
                        }
                        if ((pval.length() > 2) && (pval.charAt(2) == '-')) {
                            pval = pval.substring(0, 2);
                        }
                        if (aval.equalsIgnoreCase(pval)) {
                            fromEventToWriterOrBuffer(eFactory.createAttribute(attr.getName(),
                                    prop.getTargetLanguage().toString()), writer);
                        } else {
                            // if not in source language, should not have been
                            // translated
                            fromEventToWriterOrBuffer(attr, writer);
                        }
                    }
                }
                return false; // we already added current element
            }
            if (removeComments) { // add comment reference only if we transtate
                                  // them
                if ("commentRangeStart".equals(name.getLocalPart())) {
                    return false;
                }
                if ("commentRangeEnd".equals(name.getLocalPart())) {
                    return false;
                }
                if ("commentReference".equals(name.getLocalPart())) {
                    return false;
                }
            }
            if ("ins".equals(name.getLocalPart())) {
                return false; // same, because we remove track changes but keep
                              // last version
            }
            if ("del".equals(name.getLocalPart())) { // contents of del is
                                                     // totally removed
                currentBuffer = new LinkedList<>();
                return false;
            }
        }
        if (currentBuffer != null) {
            currentBuffer.add(startElement);
            return false;
        }
        return true;
    }

    protected void fromEventToWriterOrBuffer(XMLEvent ev, XMLStreamWriter writer) throws XMLStreamException {
        if (currentBuffer != null) {
            currentBuffer.add(ev);
        } else if (writer != null) {
            fromEventToWriter(ev, writer);
        }
    }

    @Override
    protected boolean processEndElement(EndElement endElement, XMLStreamWriter writer)
            throws XMLStreamException {
        QName name = endElement.getName();
        if (OOXML_MAIN_PARA_ELEMENT.getNamespaceURI().equals(name.getNamespaceURI())) {
            if ("p".equals(name.getLocalPart()) // word
                    || "si".equals(name.getLocalPart()) || "comment".equals(name.getLocalPart()) // excel
            ) {
                flushTranslation(writer);
                currentBuffer = null;
                return true;
            }
            if ("r".equals(name.getLocalPart())) {
                currentBuffer.add(endElement);
                currentBuffer = new LinkedList<>();
                currentPara.add(currentBuffer);
                return false;
            }
            if (removeComments) {
                // add comment reference only if we translate them
                if ("commentRangeStart".equals(name.getLocalPart())) {
                    return false;
                }
                if ("commentRangeEnd".equals(name.getLocalPart())) {
                    return false;
                }
                if ("commentReference".equals(name.getLocalPart())) {
                    return false;
                }
            }
            if ("ins".equals(name.getLocalPart())) {
                // same, because we remove track changes but keep last version
                return false;
            }
            if ("del".equals(name.getLocalPart())) {
                // end of deletion, restore normal behaviour
                currentBuffer = currentPara.getLast();
                return false;
            }
        }
        if (currentBuffer != null) {
            currentBuffer.add(endElement);
            return false;
        }
        return true;
    }

    @Override
    protected boolean processCharacters(Characters event, XMLStreamWriter writer) {
        if (currentBuffer != null) {
            currentBuffer.add(event);
        }
        return currentBuffer == null;
    }

    private void flushTranslation(XMLStreamWriter writer) throws XMLStreamException {
        String src = buildTags();
        if (writer != null) {
            for (XMLEvent ev : currentPara.getFirst()) {
                fromEventToWriter(ev, writer);
            }
            if (currentPara.size() == 1) {
                return;
            }
            String tra = entryTranslateCallback
                    .getTranslation(null /* entryId */, src, null /* path */);
            if (tra == null) {
                tra = src;
            }
            for (XMLEvent ev : restoreTags(tra)) {
                fromEventToWriter(ev, writer);
            }
            for (XMLEvent ev : currentPara.getLast()) {
                fromEventToWriter(ev, writer);
            }
        }
        if (entryParseCallback != null) {
            entryParseCallback.addEntry(null /* entryId */, src,
                    null /* translation */, false, null /* note */,
                    null /* path */, this, buildProtectedParts(src));
        }
    }

    protected Map<Character, Integer> tagsCount = new TreeMap<>();
    private List<XMLEvent> defaultsForParagraph = new LinkedList<>();

    private QName TEXT_ELEMENT;
    private static final Pattern PTN_EMPTY_AND_START = Pattern
            .compile("((?:<[a-zA-Z]+[0-9]+/>)*)<([a-zA-Z]+[0-9]+)>((?:<[a-zA-Z]+[0-9]+/>)*)"),
            PTN_EMPTY_AND_END = Pattern
                    .compile("((?:<[a-zA-Z]+[0-9]+/>)*)<(/[a-zA-Z]+[0-9]+)>((?:<[a-zA-Z]+[0-9]+/>)*)");

    /**
     * Converts List<XMLEvent> to OmegaT format, with <x0/>, <g0>...</g0>, etc.
     * Also build maps to be reused later
     **/
    protected String buildTags() {
        if (TEXT_ELEMENT == null) {
            TEXT_ELEMENT = new QName(OOXML_MAIN_PARA_ELEMENT.getNamespaceURI(), "t");
        }
        tagsMap.clear();
        for (Character c : tagsCount.keySet()) {
            tagsCount.put(c, 0);
        }
        StringBuffer res = new StringBuffer();
        defaultsForParagraph = null;
        for (int i = 0; i < currentPara.size(); i++) {
            List<XMLEvent> run = currentPara.get(i);
            if (run.isEmpty()) {
                continue;
            }
            if (run.get(0).isStartElement()
                    && ((StartElement) run.get(0)).asStartElement().getName().getLocalPart().equals("r")) {
                ListIterator<XMLEvent> runIter = run.listIterator();
                char prefix = findPrefix(runIter);
                Integer tc = tagsCount.get(prefix);
                if (tc == null) {
                    tc = 0;
                }
                if ((prefix == 'n') || (prefix == 'd') || (prefix == 'e')) { // empty
                                                                             // tags
                    res.append("<" + prefix + tc + "/>");
                    tagsMap.put("" + prefix + tc, run);
                    tagsCount.put(prefix, tc + 1);
                } else { // contains text
                    if (prefix != '\u0000') { // add begin tag
                        res.append("<" + prefix + tc + ">");
                        tagsMap.put("" + prefix + tc, run.subList(0, runIter.nextIndex()));
                    }
                    browseRunContents(run, runIter, res);
                    if (prefix != '\u0000') { // add end tag
                        res.append("</" + prefix + tc + ">");
                        tagsMap.put("/" + prefix + tc, run.subList(runIter.previousIndex(), run.size()));
                        tagsCount.put(prefix, tc + 1);
                    }
                }
            } else {
                if (i == 0) {
                    if ((run.size() > 1) && run.get(1).isStartElement() && run.get(1)
                            .asStartElement().getName().getLocalPart().equals("pPr")) {
                        defaultsForParagraph = run; // looks like defaults,
                                                    // but...
                        LOOP2: for (int j = 1; j < currentPara.size(); j++) {
                            ListIterator<XMLEvent> ir = currentPara.get(j).listIterator();
                            if (!ir.hasNext()) {
                                currentPara.remove(j);
                                continue LOOP2;
                            }
                            XMLEvent ev = ir.next();
                            if (!(ev.isStartElement()
                                    && ev.asStartElement().getName().getLocalPart().equals("r"))) {
                                continue LOOP2;
                            }
                            ev = ir.next();
                            if (!(ev.isStartElement()
                                    && ev.asStartElement().getName().getLocalPart().equals("rPr"))) {
                                // Segments contain <w:r> without <w:rPr>:
                                // cannot use defaults
                                defaultsForParagraph = null;
                                break LOOP2;
                            }
                            LOOP3: while (ir.hasNext()) {
                                ev = ir.next();
                                if (ev.isEndElement()
                                        && ev.asEndElement().getName().getLocalPart().equals("rPr")) {
                                    break LOOP3;
                                }
                                // We keep defaultsForParagraph only if all
                                // elements from it
                                // remain unchanged in current run
                                if (ev.isEndElement()) {
                                    continue LOOP3; // in StaX, empty tags are
                                                    // represented by open+close
                                }
                                if (!ev.isStartElement()) {
                                    defaultsForParagraph = null;
                                    break LOOP2; // we compare only start
                                                 // elements
                                }
                                if (isInDefaults(ev.asStartElement()) == 1) {
                                    // Check whenever attributes are in separate
                                    // StaX events
                                    List<Attribute> la = new LinkedList<>();
                                    XMLEvent ev2 = ir.next();
                                    while (ev2.isAttribute()) {
                                        la.add((Attribute) ev2);
                                        ev2 = ir.next();
                                    }
                                    while (ev2 != ev) {
                                        ir.remove();
                                        ir.previous();
                                    }
                                    ir.remove();
                                    ev = eFactory.createStartElement(ev.asStartElement().getName(),
                                            la.iterator(), ev.asStartElement().getNamespaces());
                                    ir.add(ev);
                                    // Test again with eventually collapsed
                                    // event
                                    if (isInDefaults(ev.asStartElement()) == 1) {
                                        // present but different
                                        defaultsForParagraph = null;
                                        break LOOP2;
                                    }
                                }
                                // if isInDefaults = 0:
                                // ev is not at all in defaults, it will be used
                                // to find the prefix.
                                // if isInDefaults = 2:
                                // ev is in defaults, it will be excluded from
                                // prefix generation
                            }
                        }
                    }
                    continue;
                }
                if (i == currentPara.size() - 1) {
                    break;
                }
                // Something between two <w:r>
                if ((run.size() == 1) && run.get(0).isCharacters()
                        && (0 == run.get(0).toString().trim().length())) {
                    continue;
                }
                Integer tc = tagsCount.get('x');
                if (tc == null) {
                    tc = 0;
                }
                res.append("<x" + tc + "/>");
                tagsMap.put("x" + tc, run);
                tagsCount.put('x', tc + 1);
            }
        }
        // compact result
        if (!this.doCompactTags) {
            return res.toString();
        }
        compactBuiltTags(res, PTN_EMPTY_AND_START);
        compactBuiltTags(res, PTN_EMPTY_AND_END);
        for (Map.Entry<Character, Integer> me : tagsCount.entrySet()) {
            char key = me.getKey();
            int count = me.getValue();
            // Search for removed tags...
            for (int i = count - 2; i >= 0; i--) {
                if (!res.toString().contains("<" + key + i)) {
                    // found removed tag, shift number of next tags
                    for (int j = i + 1; j < count; j++) {
                        tagsMap.put("" + key + (j - 1), tagsMap.get("" + key + j));
                        tagsMap.put("/" + key + (j - 1), tagsMap.get("/" + key + j));
                        tagsMap.put("" + key + (j - 1), tagsMap.get("" + key + j));
                        // res.replaceAll("(</?)$key$j(/?>)", "$1$key${j-1}$2")
                        Pattern PTN_THIS_TAG = Pattern.compile("(</?)" + key + j + "(/?>)");
                        Matcher mThisTag = PTN_THIS_TAG.matcher(res);
                        while (mThisTag.find()) {
                            res.replace(mThisTag.start(), mThisTag.end(),
                                    mThisTag.group(1) + key + (j - 1) + mThisTag.group(2));
                            mThisTag.reset(res);
                        }
                    }
                }
            }
        }
        return res.toString();
    }

    private void browseRunContents(List<XMLEvent> run, ListIterator<XMLEvent> runIter, StringBuffer res) {
        XMLEvent next;
        while (runIter.hasNext()) {
            next = runIter.next();
            if (next.isEndElement()) {
                if (next.asEndElement().getName().getLocalPart().equals("r")) {
                    runIter.previous(); // used if prefix != 0
                    break;
                }
            } else if (next.isStartElement()) {
                char prefixInt = '\u0000';
                final int idx = runIter.previousIndex();
                String name = next.asStartElement().getName().getLocalPart();
                switch (name) {
                    case "footnoteRef":
                        prefixInt = 'n';
                        break;
                    case "tab":
                    case "br":
                        prefixInt = 'd';
                        break;
                    case "drawing":
                        prefixInt = 'g';
                        break;
                    case "t":
                        continue;
                    default:
                        prefixInt = 'e';
                }
                while (!(next.isEndElement() && next.asEndElement().getName().getLocalPart().equals(name))) {
                    next = runIter.next();
                }
                Integer tcInt = tagsCount.get(prefixInt);
                if (tcInt == null) {
                    tcInt = 0;
                }
                LinkedList<XMLEvent> nList = new LinkedList<>();
                nList.addAll(run.subList(idx, runIter.nextIndex()));
                QName qR = new QName(OOXML_MAIN_PARA_ELEMENT.getNamespaceURI(), "r",
                        OOXML_MAIN_PARA_ELEMENT.getPrefix());
                nList.add(0, eFactory.createStartElement(qR, null, null));
                nList.add(eFactory.createEndElement(qR, null));
                res.append("<" + prefixInt + tcInt + "/>");
                tagsMap.put("" + prefixInt + tcInt, nList);
            } else {
                res.append(next); // character data
            }
        }
    }

    private void compactBuiltTags(StringBuffer res, Pattern PTN) {
        Matcher mFull = PTN.matcher(res), mUniq;
        while (mFull.find()) {
            if (!mFull.group().contains("/>")) {
                continue;
            }
            List<XMLEvent> lGlobal = tagsMap.get(mFull.group(2));
            if (!(lGlobal instanceof LinkedList)) { // subList: copy because it
                                                    // will be modified
                lGlobal = new LinkedList<>();
                lGlobal.addAll(tagsMap.get(mFull.group(2)));
                tagsMap.put(mFull.group(2), lGlobal);
            }
            mUniq = OMEGAT_TAG.matcher(mFull.group(3));
            while (mUniq.find()) {
                lGlobal.addAll(tagsMap.get(mUniq.group(2)));
            }
            LinkedList<XMLEvent> lToAdd = new LinkedList<>();
            mUniq = OMEGAT_TAG.matcher(mFull.group(1));
            while (mUniq.find()) {
                lToAdd.addAll(tagsMap.get(mUniq.group(2)));
            }
            for (java.util.Iterator<XMLEvent> iAdd = lToAdd.descendingIterator(); iAdd.hasNext();) {
                lGlobal.add(0, iAdd.next());
            }
            res.replace(mFull.start(), mFull.end(), "<" + mFull.group(2) + ">");
            mFull.reset(res);
        }
    }

    protected char findPrefix(ListIterator<XMLEvent> wr) {
        XMLEvent next = wr.next();
        next = wr.next(); // pass w:r
        if (next.isStartElement() && next.asStartElement().getName().getLocalPart().equals("t")) {
            return '\u0000';
        }
        if (next.isStartElement() && next.asStartElement().getName().getLocalPart().equals("rPr")) {
            List<String> attrs = new LinkedList<String>();
            RPR_LOOP: while (wr.hasNext()) { // read w:rPr
                next = wr.next();
                if (next.isEndElement() && next.asEndElement().getName().getLocalPart().equals("rPr")) {
                    break;
                }
                if (!next.isStartElement()) {
                    continue;
                }
                if ((defaultsForParagraph != null) && (isInDefaults(next.asStartElement()) > 1)) {
                    continue RPR_LOOP; // Remove all elements which are
                                       // identical in defaultsForParagraph
                }
                String name = next.asStartElement().getName().getLocalPart();
                if ("lang".equals(name)) {
                    continue;
                }
                attrs.add(name);
            }
            while (wr.hasNext()) { // now, search for cases of empty runs with
                                   // special markup
                if ((next = wr.next()).isStartElement()) {
                    QName name = next.asStartElement().getName();
                    if (name.equals(TEXT_ELEMENT)) {
                        break;
                    }
                    if (name.getLocalPart().startsWith("footnoteRef")) {
                        return 'n';
                    }
                    if (name.getLocalPart().equals("tab")) {
                        return 'd';
                    }
                    if (name.getLocalPart().equals("br")) {
                        return 'd';
                    }
                    if (name.getLocalPart().equals("fldChar")) {
                        return 'e';
                    }
                    if (name.getLocalPart().equals("instrText")) {
                        return 'e'; // instrText should NOT be translated!!!
                    }
                }
            }
            if (next.isEndElement() && next.asEndElement().getName().getLocalPart().equals("r")) {
                return 'e';
            }
            if (attrs.size() < 1) {
                return '\u0000'; // none
            }
            if (attrs.size() > 1) {
                return 'p'; // plural
            }
            switch (attrs.get(0)) {
            case "rStyle":
                return 's';
            case "rFonts":
            case "sz":
                return 'f';
            case "b":
            case "bCs":
                return 'b'; // bold
            case "i":
            case "iCs":
                return 'i'; // italic
            case "u":
            case "uCs":
                return 'u'; // underline
            case "caps":
            case "smallCaps":
                return 'C'; // uppercase
            case "color":
                return 'c';
            case "strike":
            case "dStrike":
                return 'l'; // line
            case "vertAlign":
                return 'v'; // exposant or subscript
            case "lang":
                return '\u0000';
            }
        }
        while (wr.hasNext()) { // not a w:rPr, generate something else
            next = wr.next();
            if (next.isStartElement()) {
                QName name = next.asStartElement().getName();
                if (name.equals(TEXT_ELEMENT)) {
                    break;
                }
                if (name.getLocalPart().startsWith("footnoteRef")) {
                    return 'n';
                }
                if (name.getLocalPart().equals("tab")) {
                    return 'd';
                }
                if (name.getLocalPart().equals("br")) {
                    return 'd';
                }
                if (name.getLocalPart().equals("fldChar")) {
                    return 'e';
                }
                if (name.getLocalPart().equals("instrText")) {
                    return 'e'; // instrText should NOT be translated!!!
                }
            } else if (next.isCharacters()) {
                wr.previous();
                return 'o';
            }
        }
        return 'e'; // other, non-text
    }

    // True if we can find in defaults this element with same attributes.
    // 0 = not found, 1 = found but differs, 2 = found equal
    private int isInDefaults(StartElement stEl) {
        for (XMLEvent dev : defaultsForParagraph) {
            if (dev.isStartElement() && dev.asStartElement().getName().equals(stEl.getName())) {
                Map<QName, String> mapNext = new java.util.HashMap<>(), mapDev = new java.util.HashMap<>();
                for (Iterator<Attribute> iter = dev.asStartElement().getAttributes(); iter.hasNext();) {
                    Attribute attr0 = iter.next();
                    mapDev.put(attr0.getName(), attr0.getValue());
                }
                for (Iterator<Attribute> iter = stEl.getAttributes(); iter.hasNext();) {
                    Attribute attr0 = iter.next();
                    mapNext.put(attr0.getName(), attr0.getValue());
                }
                return mapNext.equals(mapDev) ? 2 : 1;
            }
        }
        return 0;
    }

    /**
     * Produces xliff content for the translated text. Note: must be called
     * after buildTags(src, true) to have the necessary variables filled!
     **/
    protected List<XMLEvent> restoreTags(String tra) {
        LinkedList<XMLEvent> res = new LinkedList<XMLEvent>();
        while (tra.length() > 0) {
            Matcher m = OMEGAT_TAG.matcher(tra);
            if (m.find()) {
                if (m.start() > 0) {
                    addSimpleRun(res, tra.substring(0, m.start()));
                }
                List<XMLEvent> saved = tagsMap.get(m.group(1) + m.group(2));
                if (saved != null) {
                    res.addAll(saved);
                }
                boolean isAlone = m.group().endsWith("/>");
                tra = tra.substring(m.end());
                m.reset(tra);
                if (!isAlone) {
                    if (!m.find()) {
                        this.addCharacters(res, tra);
                        return res;
                    } else {
                        this.addCharacters(res, tra.substring(0, m.start()));
                        saved = tagsMap.get(m.group(1) + m.group(2));
                        if (saved != null) {
                            res.addAll(saved);
                        }
                        tra = tra.substring(m.end());
                    }
                }
            } else {
                addSimpleRun(res, tra);
                return res;
            }
        }
        return res;
    }

    private void addSimpleRun(LinkedList<XMLEvent> res, String text) {
        QName qR = new QName(OOXML_MAIN_PARA_ELEMENT.getNamespaceURI(), "r",
                OOXML_MAIN_PARA_ELEMENT.getPrefix());
        QName qT = new QName(OOXML_MAIN_PARA_ELEMENT.getNamespaceURI(), "t",
                OOXML_MAIN_PARA_ELEMENT.getPrefix());
        res.add(eFactory.createStartElement(qR, null, null));
        if (defaultsForParagraph != null) {
            // these are not really defaults, we must repeat it when generating
            // the target file
            boolean inRpr = false;
            for (XMLEvent ev : defaultsForParagraph) {
                if (ev.isStartElement() && ev.asStartElement().getName().getLocalPart().equals("rPr")) {
                    inRpr = true;
                }
                if (inRpr) {
                    res.add(ev);
                }
                if (ev.isEndElement() && ev.asEndElement().getName().getLocalPart().equals("rPr")) {
                    break;
                }
            }
        }
        res.add(eFactory.createStartElement(qT, null, null));
        this.addCharacters(res, text);
        res.add(eFactory.createEndElement(qT, null));
        res.add(eFactory.createEndElement(qR, null));
    }

    // Add characters with eventually xml:space=preserve
    private void addCharacters(LinkedList<XMLEvent> res, String text) {
        if (text.trim().equals(text)) {
            res.add(eFactory.createCharacters(text));
            return;
        }
        XMLEvent lastEv = res.getLast();
        if (lastEv.isStartElement() && lastEv.asStartElement().getName().getLocalPart().equals("t")) {
            boolean hasPreserve = false;
            for (Iterator<Attribute> I = lastEv.asStartElement().getAttributes(); I.hasNext();) {
                Attribute attr = I.next();
                hasPreserve = "space".equals(attr.getName().getLocalPart());
                if (hasPreserve) {
                    break;
                }
            }
            if (!hasPreserve) {
                res.add(eFactory.createAttribute("xml", "http://www.w3.org/XML/1998/namespace", "space",
                        "preserve"));
            }
        }
        res.add(eFactory.createCharacters(text));
    }

}
