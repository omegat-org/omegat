/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2012 Thomas Cordonnier
               2013 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Helper for read TMX files, using StAX.
 * 
 * TMX 1.4b specification:
 * http://www.gala-global.org/oscarStandards/tmx/tmx14b.html
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TMXReader2 {

    private final XMLInputFactory factory;
    private final SimpleDateFormat dateFormat1, dateFormat2, dateFormatOut;

    /** Segment Type attribute value: "paragraph" */
    public static final String SEG_PARAGRAPH = "paragraph";
    /** Segment Type attribute value: "sentence" */
    public static final String SEG_SENTENCE = "sentence";
    /** Creation Tool attribute value of OmegaT TMXs: "OmegaT" */
    public static final String CT_OMEGAT = "OmegaT";

    private XMLEventReader xml;

    private boolean isParagraphSegtype = true;
    private boolean isOmegaT = false;
    private boolean extTmxLevel2;
    private boolean useSlash;
    private boolean isSegmentingEnabled;
    
    private int errorsCount, warningsCount;

    ParsedTu currentTu = new ParsedTu();

    // buffers for parse texts
    StringBuilder propContent = new StringBuilder();
    StringBuilder noteContent = new StringBuilder();
    StringBuilder segContent = new StringBuilder();
    StringBuilder segInlineTag = new StringBuilder();
    InlineTagHandler inlineTagHandler = new InlineTagHandler();

    public TMXReader2() {
        factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        factory.setXMLReporter(new XMLReporter() {
            public void report(String message, String error_type, Object info, Location location)
                    throws XMLStreamException {
                Log.logWarningRB(
                        "TMXR_WARNING_WHILE_PARSING",
                        new Object[] { String.valueOf(location.getLineNumber()),
                                String.valueOf(location.getColumnNumber()) });
                Log.log(message + ": " + info);
                warningsCount++;
            }
        });
        
        dateFormat1 = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        dateFormat1.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        dateFormat2.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormatOut = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        dateFormatOut.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Read TMX file.
     */
    public void readTMX(File file, final Language sourceLanguage, final Language targetLanguage,
            boolean isSegmentingEnabled, final boolean forceOmegaTMX, final boolean extTmxLevel2,
            final boolean useSlash, final LoadCallback callback) throws Exception {
        this.extTmxLevel2 = extTmxLevel2;
        this.useSlash = useSlash;
        this.isSegmentingEnabled = isSegmentingEnabled;

        // log the parsing attempt
        Log.logRB("TMXR_INFO_READING_FILE", new Object[] { file.getAbsolutePath() });

        boolean allFound = true;

        
        InputStream in;
        if (file.getName().endsWith(".gz")) {
            in = new BufferedInputStream(new GZIPInputStream(new FileInputStream(file)));
        } else {
            in = new BufferedInputStream(new FileInputStream(file));
        }
        xml = factory.createXMLEventReader(in);
        try {
            while (xml.hasNext()) {
                XMLEvent e = xml.nextEvent();
                switch (e.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    StartElement eStart = (StartElement) e;
                    if ("tu".equals(eStart.getName().getLocalPart())) {
                        parseTu(eStart);
                        ParsedTuv origTuv = getTuvByLang(sourceLanguage);
                        ParsedTuv targetTuv = getTuvByLang(targetLanguage);
                        allFound &= callback.onEntry(currentTu, origTuv, targetTuv, isParagraphSegtype);
                    } else if ("header".equals(eStart.getName().getLocalPart())) {
                        parseHeader(eStart, sourceLanguage);
                    }
                    break;
                }
            }
        } finally {
            xml.close();
            in.close();
        }

        if (!allFound) {
            Log.logWarningRB("TMXR_WARNING_SOURCE_NOT_FOUND");
            warningsCount++;
        }
        Log.logRB("TMXR_INFO_READING_COMPLETE");
        Log.log("");
    }

    protected void parseHeader(StartElement element, final Language sourceLanguage) {
        isParagraphSegtype = SEG_PARAGRAPH.equals(getAttributeValue(element, "segtype"));
        isOmegaT = CT_OMEGAT.equals(getAttributeValue(element, "creationtool"));
        
        // log some details
        Log.logRB("TMXR_INFO_CREATION_TOOL", new Object[] { getAttributeValue(element, "creationtool") });
        Log.logRB("TMXR_INFO_CREATION_TOOL_VERSION",
                new Object[] { getAttributeValue(element, "creationtoolversion") });
        Log.logRB("TMXR_INFO_SEG_TYPE", new Object[] { getAttributeValue(element, "segtype") });
        Log.logRB("TMXR_INFO_SOURCE_LANG", new Object[] { getAttributeValue(element, "srclang") });

        // give a warning if the TMX source language is
        // different from the project source language
        String tmxSourceLanguage = getAttributeValue(element, "srclang");
        if (!tmxSourceLanguage.equalsIgnoreCase(sourceLanguage.getLanguage())) {
            Log.logWarningRB("TMXR_WARNING_INCORRECT_SOURCE_LANG", new Object[] { tmxSourceLanguage,
                    sourceLanguage });
        }

        // give a warning that TMX file will be upgraded to sentence segmentation
        if (isSegmentingEnabled && isParagraphSegtype) {
            Log.logWarningRB("TMXR_WARNING_UPGRADE_SENTSEG");
        }
    }

    protected void parseTu(StartElement element) throws Exception {
        currentTu.clear();

        currentTu.changeid = getAttributeValue(element, "changeid");
        currentTu.changedate = parseISO8601date(getAttributeValue(element, "changedate"));
        currentTu.creationid = getAttributeValue(element, "creationid");
        currentTu.creationdate = parseISO8601date(getAttributeValue(element, "creationdate"));

        while (true) {
            XMLEvent e = xml.nextEvent();
            switch (e.getEventType()) {
            case XMLEvent.START_ELEMENT:
                StartElement eStart = (StartElement) e;
                if ("tuv".equals(eStart.getName().getLocalPart())) {
                    parseTuv(eStart);
                } else if ("prop".equals(eStart.getName().getLocalPart())) {
                    parseProp(eStart);
                } else if ("note".equals(eStart.getName().getLocalPart())) {
                    parseNote(eStart);
                }
                break;
            case XMLEvent.END_ELEMENT:
                EndElement eEnd = (EndElement) e;
                if ("tu".equals(eEnd.getName().getLocalPart())) {
                    return;
                }
                break;
            }
        }
    }

    protected void parseTuv(StartElement element) throws Exception {
        ParsedTuv tuv = new ParsedTuv();
        currentTu.tuvs.add(tuv);

        tuv.changeid = getAttributeValue(element, "changeid");
        tuv.changedate = parseISO8601date(getAttributeValue(element, "changedate"));
        tuv.creationid = getAttributeValue(element, "creationid");
        tuv.creationdate = parseISO8601date(getAttributeValue(element, "creationdate"));

        // find 'lang' or 'xml:lang' attribute
        for (Iterator<Attribute> it = element.getAttributes(); it.hasNext();) {
            Attribute a = it.next();
            if ("lang".equals(a.getName().getLocalPart())) {
                tuv.lang = a.getValue();
                break;
            }
        }

        while (true) {
            XMLEvent e = xml.nextEvent();
            switch (e.getEventType()) {
            case XMLEvent.START_ELEMENT:
                StartElement eStart = (StartElement) e;
                if ("seg".equals(eStart.getName().getLocalPart())) {
                    if (isOmegaT) {
                        parseSegOmegaT();
                    } else if (extTmxLevel2) {
                        parseSegExtLevel2();
                    } else {
                        parseSegExtLevel1();
                    }
                    tuv.text = segContent.toString();
                }
                break;
            case XMLEvent.END_ELEMENT:
                EndElement eEnd = (EndElement) e;
                if ("tuv".equals(eEnd.getName().getLocalPart())) {
                    return;
                }
                break;
            }
        }
    }
    
    protected void parseNote(StartElement element) throws Exception {
        noteContent.setLength(0);

        while (true) {
            XMLEvent e = xml.nextEvent();
            switch (e.getEventType()) {
            case XMLEvent.END_ELEMENT:
                EndElement eEnd = (EndElement) e;
                if ("note".equals(eEnd.getName().getLocalPart())) {
                    currentTu.note=noteContent.toString();
                    return;
                }
                break;
            case XMLEvent.CHARACTERS:
                Characters c = (Characters) e;
                noteContent.append(c.getData());
                break;
            }
        }
    }

    protected void parseProp(StartElement element) throws Exception {
        String propType = getAttributeValue(element, "type");
        propContent.setLength(0);

        while (true) {
            XMLEvent e = xml.nextEvent();
            switch (e.getEventType()) {
            case XMLEvent.END_ELEMENT:
                EndElement eEnd = (EndElement) e;
                if ("prop".equals(eEnd.getName().getLocalPart())) {
                    currentTu.props.put(propType, propContent.toString());
                    return;
                }
                break;
            case XMLEvent.CHARACTERS:
                Characters c = (Characters) e;
                propContent.append(c.getData());
                break;
            }
        }
    }

    /**
     * OmegaT TMX - just read full text.
     */
    protected void parseSegOmegaT() throws Exception {
        segContent.setLength(0);

        while (true) {
            XMLEvent e = xml.nextEvent();
            switch (e.getEventType()) {
            case XMLEvent.END_ELEMENT:
                EndElement eEnd = (EndElement) e;
                if ("seg".equals(eEnd.getName().getLocalPart())) {
                    return;
                }
                break;
            case XMLEvent.CHARACTERS:
                Characters c = (Characters) e;
                segContent.append(c.getData());
                break;
            }
        }
    }

    /**
     * External TMX - level 1. Skip text inside inline tags.
     */
    protected void parseSegExtLevel1() throws Exception {
        segContent.setLength(0);

        int inlineLevel = 0;

        while (true) {
            XMLEvent e = xml.nextEvent();
            switch (e.getEventType()) {
            case XMLEvent.START_ELEMENT:
                inlineLevel++;
                break;
            case XMLEvent.END_ELEMENT:
                inlineLevel--;
                EndElement eEnd = (EndElement) e;
                if ("seg".equals(eEnd.getName().getLocalPart())) {
                    return;
                }
                break;
            case XMLEvent.CHARACTERS:
                if (inlineLevel == 0) {
                    Characters c = (Characters) e;
                    segContent.append(c.getData());
                }
                break;
            }
        }
    }

    /**
     * External TMX - level 2. Replace all tags into shortcuts.
     */
    protected void parseSegExtLevel2() throws Exception {
        segContent.setLength(0);
        segInlineTag.setLength(0);
        inlineTagHandler.reset();

        int inlineLevel = 0;
        StartElement currentElement;
        while (true) {
            XMLEvent e = xml.nextEvent();
            switch (e.getEventType()) {
            case XMLEvent.START_ELEMENT:
                StartElement eStart = e.asStartElement();
                currentElement = eStart;
                if ("hi".equals(eStart.getName().getLocalPart())) {
                    // tag should be skipped
                    break;
                }
                inlineLevel++;
                segInlineTag.setLength(0);
                if ("bpt".equals(eStart.getName().getLocalPart())) {
                    inlineTagHandler.startBPT(getAttributeValue(eStart, "i"));
                    inlineTagHandler.setTagShortcutLetter(StringUtil.getFirstLetterLowercase(getAttributeValue(eStart,
                            "type")));
                } else if ("ept".equals(eStart.getName().getLocalPart())) {
                    inlineTagHandler.startEPT(getAttributeValue(eStart, "i"));
                } else if ("it".equals(eStart.getName().getLocalPart())) {
                    inlineTagHandler.startOTHER();
                    inlineTagHandler.setOtherTagShortcutLetter(StringUtil.getFirstLetterLowercase(getAttributeValue(eStart,
                            "type")));
                    inlineTagHandler.setCurrentPos(getAttributeValue(eStart, "pos"));
                } else if ("ph".equals(eStart.getName().getLocalPart())) {
                    inlineTagHandler.startOTHER();
                    inlineTagHandler.setOtherTagShortcutLetter(StringUtil.getFirstLetterLowercase(getAttributeValue(eStart,
                            "type")));
                } else {
                    inlineTagHandler.startOTHER();
                }
                break;
            case XMLEvent.END_ELEMENT:
                EndElement eEnd = e.asEndElement();
                if ("hi".equals(eEnd.getName().getLocalPart())) {
                    // tag should be skipped
                    break;
                }
                inlineLevel--;
                if ("seg".equals(eEnd.getName().getLocalPart())) {
                    return;
                }
                boolean slashBefore = false;
                boolean slashAfter = false;
                char tagName = StringUtil.getFirstLetterLowercase(segInlineTag);
                Integer tagN;
                if ("bpt".equals(eEnd.getName().getLocalPart())) {
                    if (tagName != 0) {
                        inlineTagHandler.setTagShortcutLetter(tagName);
                    } else {
                        tagName = inlineTagHandler.getTagShortcutLetter();
                    }
                    tagN = inlineTagHandler.endBPT();
                } else if ("ept".equals(eEnd.getName().getLocalPart())) {
                    slashBefore = true;
                    tagName = inlineTagHandler.getTagShortcutLetter();
                    tagN = inlineTagHandler.endEPT();
                } else if ("it".equals(eEnd.getName().getLocalPart())) {
                    if (tagName != 0) {
                        inlineTagHandler.setOtherTagShortcutLetter(tagName);
                    } else {
                        tagName = inlineTagHandler.getOtherTagShortcutLetter();
                    }
                    tagN = inlineTagHandler.endOTHER();
                    if ("end".equals(inlineTagHandler.getCurrentPos())) {
                        slashBefore = true;
                    } else {
                        if (useSlash) {
                            slashAfter = true;
                        }
                    }
                } else if ("ph".equals(eEnd.getName().getLocalPart())) {
                    if (tagName != 0) {
                        inlineTagHandler.setOtherTagShortcutLetter(tagName);
                    } else {
                        tagName = inlineTagHandler.getOtherTagShortcutLetter();
                    }
                    tagN = inlineTagHandler.endOTHER();
                    if (useSlash) {
                        slashAfter = true;
                    }
                } else {
                    tagN = inlineTagHandler.endOTHER();
                    if (useSlash) {
                        slashAfter = true;
                    }
                }
                if (tagName == 0) {
                    tagName = 'f';
                }
                if (tagN == null) {
                    // check error of TMX reading
                    Log.logErrorRB("TMX_ERROR_READING_LEVEL2", e.getLocation().getLineNumber(), e
                            .getLocation().getColumnNumber());
                    errorsCount++;
                    segContent.setLength(0);
                    // wait for end seg
                    while (true) {
                        XMLEvent ev = xml.nextEvent();
                        switch (ev.getEventType()) {
                        case XMLEvent.END_ELEMENT:
                            EndElement evEnd = (EndElement) ev;
                            if ("seg".equals(evEnd.getName().getLocalPart())) {
                                return;
                            }
                        }
                    }
                }

                segContent.append('<');
                if (slashBefore) {
                    segContent.append('/');
                }
                segContent.append(tagName);
                segContent.append(Integer.toString(tagN));
                if (slashAfter) {
                    segContent.append('/');
                }
                segContent.append('>');
                break;
            case XMLEvent.CHARACTERS:
                Characters c = (Characters) e;
                if (inlineLevel == 0) {
                    segContent.append(c.getData());
                } else {
                    segInlineTag.append(c.getData());
                }
                break;
            }
        }
    }

    /**
     * Get ParsedTuv from list of Tuv for specific language.
     * 
     * Language choosed by:<br>
     * - with the same language+country<br>
     * - if not exist, then with the same language but without country<br>
     * - if not exist, then with the same language with whatever country<br>
     */
    protected ParsedTuv getTuvByLang(Language lang) {
        String langLanguage = lang.getLanguageCode();
        String langCountry = lang.getCountryCode();
        ParsedTuv tuvLC = null; // Tuv with the same language+country
        ParsedTuv tuvL = null; // Tuv with the same language only, without country
        ParsedTuv tuvLW = null; // Tuv with the same language+whatever country
        for (int i = 0; i < currentTu.tuvs.size(); i++) {
            ParsedTuv tuv = currentTu.tuvs.get(i);
            String tuvLang = tuv.lang;
            if (!langLanguage.regionMatches(true, 0, tuvLang, 0, 2)) {
                // language not equals - there is no sense to processing
                continue;
            }
            if (tuvLang.length() < 3) {
                // language only, without country
                tuvL = tuv;
            } else if (langCountry.regionMatches(true, 0, tuvLang, 3, 2)) {
                // the same country
                tuvLC = tuv;
            } else {
                // other country
                tuvLW = tuv;
            }
        }
        ParsedTuv bestTuv;
        if (tuvLC != null) {
            bestTuv = tuvLC;
        } else if (tuvL != null) {
            bestTuv = tuvL;
        } else {
            bestTuv = tuvLW;
        }
        return bestTuv;
    }

    public long parseISO8601date(String str) {
        if (str == null) {
            return 0;
        }
        try {
            return dateFormat1.parse(str).getTime();
        } catch (ParseException ex) {
        }
        try {
            return dateFormat2.parse(str).getTime();
        } catch (ParseException ex) {
        }

        return 0;
    }

    private static String getAttributeValue(StartElement e, String attrName) {
        Attribute a = e.getAttributeByName(new QName(attrName));
        return a != null ? a.getValue() : null;
    }

    /**
     * Callback for receive data from TMX.
     */
    public interface LoadCallback {
        /**
         * @return true if TU contains required source and target info
         */
        boolean onEntry(ParsedTu tu, ParsedTuv tuvSource, ParsedTuv tuvTarget, boolean isParagraphSegtype);
    }

    public static class ParsedTu {
        public String changeid;
        public long changedate;
        public String creationid;
        public long creationdate;
        public String note;
        public Map<String, String> props = new TreeMap<String, String>();
        public List<ParsedTuv> tuvs = new ArrayList<ParsedTuv>();

        void clear() {
            changeid = null;
            changedate = 0;
            creationid = null;
            creationdate = 0;
            props = new TreeMap<String, String>(); // do not CLEAR, because it may be shared
            tuvs = new ArrayList<ParsedTuv>();
            note = null;
        }
    }

    public static class ParsedTuv {
        public String lang;
        public String changeid;
        public long changedate;
        public String creationid;
        public long creationdate;
        public String text;
    }

    public static final EntityResolver TMX_DTD_RESOLVER = new EntityResolver() {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if (systemId.endsWith("tmx11.dtd")) {
                return new InputSource(TMXReader2.class.getResourceAsStream("/schemas/tmx11.dtd"));
            } else if (systemId.endsWith("tmx14.dtd")) {
                return new InputSource(TMXReader2.class.getResourceAsStream("/schemas/tmx14.dtd"));
            } else {
                return null;
            }
        }
    };
}
