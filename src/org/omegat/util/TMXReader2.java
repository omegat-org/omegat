/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
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
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TMXReader2 {

    static final XMLInputFactory FACTORY;

    private static final SimpleDateFormat DATE_FORMAT1, DATE_FORMAT2, DATE_FORMAT_OUT;

    /** Segment Type attribute value: "paragraph" */
    public static final String SEG_PARAGRAPH = "paragraph";
    /** Segment Type attribute value: "sentence" */
    public static final String SEG_SENTENCE = "sentence";
    /** Creation Tool attribute value of OmegaT TMXs: "OmegaT" */
    public static final String CT_OMEGAT = "OmegaT";

    private XMLEventReader xml;

    static {
        FACTORY = XMLInputFactory.newInstance();
        FACTORY.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

        DATE_FORMAT1 = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        DATE_FORMAT1.setTimeZone(TimeZone.getTimeZone("UTC"));
        DATE_FORMAT2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        DATE_FORMAT2.setTimeZone(TimeZone.getTimeZone("UTC"));
        DATE_FORMAT_OUT = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        DATE_FORMAT_OUT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private boolean isParagraphSegtype = true;
    private boolean isOmegaT = false;
    private boolean extTmxLevel2;
    private boolean useSlash;

    private ParsedTu currentTu = new ParsedTu();
    private ParsedTuv origTuv = new ParsedTuv();
    private ParsedTuv targetTuv = new ParsedTuv();

    private List<Tuv> currentTuTuvs = new ArrayList<Tuv>();

    StringBuilder propContent = new StringBuilder();
    StringBuilder noteContent = new StringBuilder();
    StringBuilder segContent = new StringBuilder();
    StringBuilder segInlineTag = new StringBuilder();
    // map of 'i' attributes to tag numbers
    Map<String, Integer> pairTags = new TreeMap<String, Integer>();

    /**
     * Read TMX file.
     */
    public void readTMX(File file, final Language sourceLanguage, final Language targetLanguage,
            boolean isSegmentingEnabled, final boolean forceOmegaTMX, final boolean extTmxLevel2,
            final boolean useSlash, final LoadCallback callback) throws Exception {
        this.extTmxLevel2 = extTmxLevel2;
        this.useSlash = useSlash;

        InputStream in = new BufferedInputStream(new FileInputStream(file));
        xml = FACTORY.createXMLEventReader(in);
        try {
            while (xml.hasNext()) {
                XMLEvent e = xml.nextEvent();
                switch (e.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    StartElement eStart = (StartElement) e;
                    if ("tu".equals(eStart.getName().getLocalPart())) {
                        parseTu(eStart);
                        if (fillTuv(origTuv, sourceLanguage) && fillTuv(targetTuv, targetLanguage)) {
                            callback.onEntry(currentTu, origTuv, targetTuv, isParagraphSegtype);
                        }
                    } else if ("header".equals(eStart.getName().getLocalPart())) {
                        parseHeader(eStart);
                    }
                    break;
                }
            }
        } finally {
            xml.close();
            in.close();
        }
    }

    protected void parseHeader(StartElement element) {
        isParagraphSegtype = SEG_PARAGRAPH.equals(getAttributeValue(element, "segtype"));
        isOmegaT = CT_OMEGAT.equals(getAttributeValue(element, "creationtool"));
    }

    protected void parseTu(StartElement element) throws Exception {
        currentTu.changeid = getAttributeValue(element, "changeid");
        currentTu.changedate = parseISO8601date(getAttributeValue(element, "changedate"));
        currentTu.creationid = getAttributeValue(element, "creationid");
        currentTu.creationdate = parseISO8601date(getAttributeValue(element, "creationdate"));

        currentTu.clear();
        currentTuTuvs.clear();

        while (true) {
            XMLEvent e = xml.nextEvent();
            switch (e.getEventType()) {
            case XMLEvent.START_ELEMENT:
                StartElement eStart = (StartElement) e;
                if ("tuv".equals(eStart.getName().getLocalPart())) {
                    currentTuTuvs.add(parseTuv(eStart));
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

    protected Tuv parseTuv(StartElement element) throws Exception {
        Tuv tuv = new Tuv();

        tuv.changeid = getAttributeValue(element, "changeid");
        tuv.changedate = getAttributeValue(element, "changedate");
        tuv.creationid = getAttributeValue(element, "creationid");
        tuv.creationdate = getAttributeValue(element, "creationdate");

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
                    tuv.seg = segContent.toString();
                }
                break;
            case XMLEvent.END_ELEMENT:
                EndElement eEnd = (EndElement) e;
                if ("tuv".equals(eEnd.getName().getLocalPart())) {
                    return tuv;
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
        pairTags.clear();

        int tagNumber = 0;
        int inlineLevel = 0;
        String currentI = null;
        String currentPos = null;

        while (true) {
            XMLEvent e = xml.nextEvent();
            switch (e.getEventType()) {
            case XMLEvent.START_ELEMENT:
                inlineLevel++;
                StartElement eStart = (StartElement) e;
                segInlineTag.setLength(0);
                if ("bpt".equals(eStart.getName().getLocalPart())) {
                    currentI = getAttributeValue(eStart, "i");
                    pairTags.put(currentI, tagNumber);
                    tagNumber++;
                } else if ("ept".equals(eStart.getName().getLocalPart())) {
                    currentI = getAttributeValue(eStart, "i");
                } else if ("it".equals(eStart.getName().getLocalPart())) {
                    currentPos = getAttributeValue(eStart, "pos");
                } else {
                    currentI = null;
                }
                break;
            case XMLEvent.END_ELEMENT:
                inlineLevel--;
                EndElement eEnd = (EndElement) e;
                if ("seg".equals(eEnd.getName().getLocalPart())) {
                    return;
                }
                boolean slashBefore = false;
                boolean slashAfter = false;
                char tagName = getFirstLetter(segInlineTag);
                Integer tagN;
                if ("bpt".equals(eEnd.getName().getLocalPart())) {
                    tagN = pairTags.get(currentI);
                } else if ("ept".equals(eEnd.getName().getLocalPart())) {
                    slashBefore = true;
                    tagN = pairTags.get(currentI);
                } else if ("it".equals(eEnd.getName().getLocalPart())) {
                    tagN = tagNumber;
                    if ("end".equals(currentPos)) {
                        slashBefore = true;
                    }
                } else {
                    tagN = tagNumber;
                    if (useSlash) {
                        slashAfter = true;
                    }
                }
                if (tagN == null) {
                    // check error of TMX reading
                    Log.logErrorRB("TMX_ERROR_READING_LEVEL2", e.getLocation().getLineNumber(), e
                            .getLocation().getColumnNumber());
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

    protected static char getFirstLetter(StringBuilder s) {
        char f = 0;

        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetter(s.charAt(i))) {
                f = Character.toLowerCase(s.charAt(i));
                break;
            }
        }

        return f != 0 ? f : 'f';
    }

    /**
     * Fill ParsedTuv from list of Tuv for specific language.
     * 
     * Language choosed by:<br>
     * - with the same language+country<br>
     * - if not exist, then with the same language but without country<br>
     * - if not exist, then with the same language with whatever country<br>
     */
    private boolean fillTuv(ParsedTuv parsedTuv, Language lang) {
        String langLanguage = lang.getLanguageCode();
        String langCountry = lang.getCountryCode();
        Tuv tuvLC = null; // Tuv with the same language+country
        Tuv tuvL = null; // Tuv with the same language only, without country
        Tuv tuvLW = null; // Tuv with the same language+whatever country
        for (int i = 0; i < currentTuTuvs.size(); i++) {
            Tuv tuv = currentTuTuvs.get(i);
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
        Tuv bestTuv;
        if (tuvLC != null) {
            bestTuv = tuvLC;
        } else if (tuvL != null) {
            bestTuv = tuvL;
        } else {
            bestTuv = tuvLW;
        }
        if (bestTuv != null) {
            parsedTuv.fillFrom(bestTuv);
            return true;
        } else {
            return false;
        }
    }

    public static long parseISO8601date(String str) {
        if (str == null) {
            return 0;
        }
        try {
            synchronized (DATE_FORMAT1) {
                return DATE_FORMAT1.parse(str).getTime();
            }
        } catch (ParseException ex) {
        }
        try {
            synchronized (DATE_FORMAT2) {
                return DATE_FORMAT2.parse(str).getTime();
            }
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
        void onEntry(ParsedTu tu, ParsedTuv tuvSource, ParsedTuv tuvTarget, boolean isParagraphSegtype);
    }

    public static class ParsedTu {
        public String changeid;
        public long changedate;
        public String creationid;
        public long creationdate;
        public String note;
        public Map<String, String> props = new TreeMap<String, String>();

        void clear() {
            changeid = null;
            changedate = 0;
            creationid = null;
            creationdate = 0;
            props.clear();
            note = null;
        }
    }

    public static class ParsedTuv {
        public String changeid;
        public long changedate;
        public String creationid;
        public long creationdate;
        public String text;

        void fillFrom(Tuv tuv) {
            changeid = tuv.changeid;
            changedate = parseISO8601date(tuv.changedate);
            creationid = tuv.creationid;
            creationdate = parseISO8601date(tuv.creationdate);
            text = tuv.seg.toString();
        }
    }

    public static class Tuv {
        String lang;
        String changeid;
        String changedate;
        String creationid;
        String creationdate;
        String seg;
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
