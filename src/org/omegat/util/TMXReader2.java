/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2012 Thomas Cordonnier
               2013 Alex Buloichik
               2014 Aaron Madlon-Kay
               2018 Thomas Cordonnier
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
package org.omegat.util;

import org.apache.commons.io.input.XmlStreamReader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import static javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE;
import static javax.xml.stream.XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES;
import static javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES;
import static javax.xml.stream.XMLInputFactory.IS_VALIDATING;
import static javax.xml.stream.XMLInputFactory.SUPPORT_DTD;

/**
 * Helper for read TMX files, using StAX.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @see <a href="https://www.gala-global.org/tmx-14b">TMX1.4b specification</a>
 */
public class TMXReader2 {

    private final XMLInputFactory inputFactory;
    private final SchemaFactory schemaFactory;
    private final SimpleDateFormat dateFormat1;
    private final SimpleDateFormat dateFormat2;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(TMXReader2.class);

    /**
     * Detects charset of XML file.
     */
    public static String detectCharset(File file) throws IOException {
        try (XmlStreamReader rd = XmlStreamReader.builder().setFile(file).get()) {
            return rd.getEncoding();
        }
    }

    public TMXReader2() {
        // initialize input factory.
        inputFactory = XMLInputFactory.newFactory();
        inputFactory.setProperty(IS_NAMESPACE_AWARE, false);
        inputFactory.setProperty(IS_VALIDATING, false);
        inputFactory.setProperty(SUPPORT_DTD, false);
        inputFactory.setProperty(IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        inputFactory.setProperty(IS_REPLACING_ENTITY_REFERENCES, false);
        inputFactory.setXMLResolver(TMX_DTD_RESOLVER_2);

        inputFactory.setXMLReporter((message, errorType, info, location) -> {
            Log.logWarningRB("TMXR_WARNING_WHILE_PARSING", location.getLineNumber(), location.getColumnNumber());
            Log.log(StringUtil.format("{0}: {1}", message, info));
            warningsCount++;
        });
        schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        dateFormat1 = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        dateFormat1.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        dateFormat2.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat dateFormatOut = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        dateFormatOut.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public boolean isParagraphSegtype() {
        return isParagraphSegtype;
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

        if (forceOmegaTMX) {
            Validator validator = getValidator(forceOmegaTMX);
            XmlErrorHandler xsdErrorHandler = new XmlErrorHandler();
            validator.setErrorHandler(xsdErrorHandler);
            validator.validate(new StreamSource(getInputStream(file)));
            if (!xsdErrorHandler.getExceptions().isEmpty()) {
                for (Exception e : xsdErrorHandler.getExceptions()) {
                    Log.log(e.getLocalizedMessage());
                }
                throw new SAXException("OmegaT TMX validation failed.");
            }
        }

        // log the parsing attempt
        Log.logInfoRB("TMXR_INFO_READING_FILE", file.getAbsolutePath());

        boolean allFound = true;

        try (InputStream in = getInputStream(file)) {
            xml = inputFactory.createXMLEventReader(in);
            while (xml.hasNext()) {
                XMLEvent e = xml.nextEvent();
                if (e.getEventType() == XMLEvent.START_ELEMENT) {
                    StartElement eStart = (StartElement) e;
                    if ("tu".equals(eStart.getName().getLocalPart())) {
                        parseTu(eStart);
                        ParsedTuv origTuv = getTuvByLang(sourceLanguage);
                        ParsedTuv targetTuv = getTuvByLang(targetLanguage);
                        allFound = callback.onEntry(currentTu, origTuv, targetTuv, isParagraphSegtype)
                                && allFound;
                    } else if ("header".equals(eStart.getName().getLocalPart())) {
                        parseHeader(eStart, sourceLanguage);
                    }
                }
            }
        } catch (XMLStreamException ex) {
            Log.logErrorRB(ex, "TMXR_ERROR_XML_STREAM_ERROR", file.getAbsolutePath());
            throw ex;
        } catch (IOException ignored) {
        } finally {
            if (xml != null) {
                xml.close();
            }
        }

        if (!allFound) {
            Log.logWarningRB("TMXR_WARNING_SOURCE_NOT_FOUND");
            warningsCount++;
        }
        Log.logInfoRB("TMXR_INFO_READING_COMPLETE");
        if (errorsCount > 0 || warningsCount > 0) {
            LOGGER.atDebug().log("Errors: {}, Warnings: {}", errorsCount, warningsCount);
        }
    }

    static class DumbLSResourceResolver implements LSResourceResolver {

        private String encoding;
        private String publicId;
        private String systemId;
        private String baseURI;

        public DumbLSResourceResolver() {
            encoding = StandardCharsets.UTF_8.name();
        }

        @Override
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId,
                                       String baseURI) {
            this.publicId = publicId;
            this.systemId = systemId;
            this.baseURI = baseURI;
            if (systemId.endsWith("tmx11.dtd")) {
                return getLsInput("/schemas/tmx11.dtd");
            } else if (systemId.endsWith("tmx14.dtd")) {
                return getLsInput("/schemas/tmx14.dtd");
            } else {
                return null;
            }
        }

        private @NotNull LSInput getLsInput(String dtd) {
            return new LSInput() {
                @Override
                public Reader getCharacterStream() {
                    InputStream is = TMXReader2.class.getResourceAsStream(dtd);
                    if (is == null) {
                        return new StringReader("");
                    }
                    return new InputStreamReader(is, StandardCharsets.UTF_8);
                }

                @Override
                public void setCharacterStream(Reader characterStream) {
                }

                @Override
                public InputStream getByteStream() {
                    return TMXReader2.class.getResourceAsStream(dtd);
                }

                @Override
                public void setByteStream(InputStream byteStream) {
                }

                @Override
                public String getStringData() {
                    try (InputStream is = TMXReader2.class.getResourceAsStream(dtd)) {
                        if (is == null) {
                            return "";
                        }
                        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        return "";
                    }
                }

                @Override
                public void setStringData(String stringData) {
                }

                @Override
                public String getSystemId() {
                    return systemId;
                }

                @Override
                public void setSystemId(String newsystemId) {
                    systemId = newsystemId;
                }

                @Override
                public String getPublicId() {
                    return publicId;
                }

                @Override
                public void setPublicId(String newPublicId) {
                    publicId = newPublicId;
                }

                @Override
                public String getBaseURI() {
                    return baseURI;
                }

                @Override
                public void setBaseURI(String newBaseURI) {
                    baseURI = newBaseURI;
                }

                @Override
                public String getEncoding() {
                    return encoding;
                }

                @Override
                public void setEncoding(String newEncoding) {
                    encoding = newEncoding;
                }

                @Override
                public boolean getCertifiedText() {
                    return false;
                }

                @Override
                public void setCertifiedText(boolean certifiedText) {
                }
            };
        }
    }

    private Validator getValidator(boolean forceOmegaTMX) throws SAXException {

        String schemaPath = forceOmegaTMX ? "/schemas/tmx11.xsd" : "/schemas/tmx14.xsd";

        // Check if a resource exists
        URL schemaUrl = getClass().getResource(schemaPath);
        if (schemaUrl == null) {
            throw new SAXException("Schema resource not found: " + schemaPath);
        }
        try {
            Validator validator;
            Schema schema = schemaFactory.newSchema(schemaUrl);
            validator = schema.newValidator();
            validator.setResourceResolver(new DumbLSResourceResolver());
            return validator;
        } catch (SAXException ex) {
            throw new SAXException("Failed to create validator: " + ex.getMessage(), ex);
        }
    }

    public static class XmlErrorHandler implements ErrorHandler {

        private final List<SAXParseException> exceptions;

        public XmlErrorHandler() {
            this.exceptions = new ArrayList<>();
        }

        public List<SAXParseException> getExceptions() {
            return exceptions;
        }

        @Override
        public void warning(SAXParseException exception) {
            exceptions.add(exception);
        }

        @Override
        public void error(SAXParseException exception) {
            exceptions.add(exception);
        }

        @Override
        public void fatalError(SAXParseException exception) {
            exceptions.add(exception);
        }
    }

    private InputStream getInputStream(File file) throws IOException {
        String fileName = file.getName().toLowerCase(Locale.ENGLISH);
        if (fileName.endsWith(".gz")) {
            // BufferedInputStream.DEFAULT_BUFFER_SIZE = 8192
            return new GZIPInputStream(new FileInputStream(file), 8192);
        } else if (fileName.endsWith(".zip")) {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
            if (zis.getNextEntry() == null) {
                throw new IOException("Zipped TMX had no entry: " + file);
            }
            return zis;
        } else {
            return new BufferedInputStream(new FileInputStream(file));
        }
    }

    protected void parseHeader(StartElement element, final Language sourceLanguage) {
        isParagraphSegtype = SEG_PARAGRAPH.equals(getAttributeValue(element, "segtype"));
        isOmegaT = CT_OMEGAT.equals(getAttributeValue(element, "creationtool"));

        // log some details
        Log.logInfoRB("TMXR_INFO_CREATION_TOOL", getAttributeValue(element, "creationtool"));
        Log.logInfoRB("TMXR_INFO_CREATION_TOOL_VERSION", getAttributeValue(element, "creationtoolversion"));
        Log.logInfoRB("TMXR_INFO_SEG_TYPE", getAttributeValue(element, "segtype"));
        Log.logInfoRB("TMXR_INFO_SOURCE_LANG", getAttributeValue(element, "srclang"));

        // give a warning if the TMX source language is
        // different from the project source language
        String tmxSourceLanguage = getAttributeValue(element, "srclang");
        if (!sourceLanguage.getLanguage().equalsIgnoreCase(tmxSourceLanguage)) {
            Log.logWarningRB("TMXR_WARNING_INCORRECT_SOURCE_LANG", tmxSourceLanguage, sourceLanguage);
        }

        // give a warning that TMX file will be upgraded to sentence
        // segmentation
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

        String tuid = getAttributeValue(element, "tuid");
        if (tuid != null) {
            currentTu.props.add(new TMXProp("tuid", getAttributeValue(element, "tuid")));
        }

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
        for (Iterator<?> it = element.getAttributes(); it.hasNext();) {
            Attribute a = (Attribute) it.next();
            if ("lang".equals(a.getName().getLocalPart()) || "xml:lang".equals(a.getName().getLocalPart())) {
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
                    tuv.text = StringUtil.normalizeUnicode(segContent);
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
                    currentTu.note = noteContent.toString();
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
                    currentTu.props.add(new TMXProp(propType, propContent.toString()));
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

        // OmegaT uses 0-indexed tags while the TMX spec *implies* that tags
        // should be 1-indexed. To correctly handle both, we normalize by
        // looking ahead at all tags and finding the smallest "x" attr value,
        // then subtracting it from all tag numbers.

        int minSeenTag = Integer.MAX_VALUE;
        Deque<XMLEvent> buf = new ArrayDeque<XMLEvent>();

        outer: while (true) {
            XMLEvent e = xml.nextEvent();
            buf.add(e);
            switch (e.getEventType()) {
            case XMLEvent.END_ELEMENT:
                EndElement evEnd = (EndElement) e;
                if ("seg".equals(evEnd.getName().getLocalPart())) {
                    break outer;
                }
                break;
            case XMLEvent.START_ELEMENT:
                StartElement eStart = e.asStartElement();
                switch (eStart.getName().getLocalPart()) {
                case "bpt":
                case "it":
                case "ph":
                    String x = getAttributeValue(eStart, "x");
                    if (x != null) {
                        try {
                            int xVal = Integer.parseInt(x);
                            minSeenTag = Math.min(minSeenTag, xVal);
                        } catch (NumberFormatException ex) {
                            // Ignore
                        }
                    }
                    break;
                }
                break;
            }
        }

        if (minSeenTag == Integer.MAX_VALUE) {
            minSeenTag = 0;
        }

        int inlineLevel = 0;
        while (true) {
            XMLEvent e = buf.pop();
            switch (e.getEventType()) {
            case XMLEvent.START_ELEMENT:
                StartElement eStart = e.asStartElement();
                if ("hi".equals(eStart.getName().getLocalPart())) {
                    // tag should be skipped
                    break;
                }
                inlineLevel++;
                segInlineTag.setLength(0);
                if ("bpt".equals(eStart.getName().getLocalPart())) {
                    inlineTagHandler.startBPT(getAttributeValue(eStart, "i"), getAttributeValue(eStart, "x"));
                    inlineTagHandler.setTagShortcutLetter(
                            StringUtil.getFirstLetterLowercase(getAttributeValue(eStart, "type")));
                } else if ("ept".equals(eStart.getName().getLocalPart())) {
                    inlineTagHandler.startEPT(getAttributeValue(eStart, "i"));
                } else if ("it".equals(eStart.getName().getLocalPart())) {
                    inlineTagHandler.startIT(getAttributeValue(eStart, "x"));
                    inlineTagHandler.setOtherTagShortcutLetter(
                            StringUtil.getFirstLetterLowercase(getAttributeValue(eStart, "type")));
                    inlineTagHandler.setCurrentPos(getAttributeValue(eStart, "pos"));
                } else if ("ph".equals(eStart.getName().getLocalPart())) {
                    inlineTagHandler.startPH(getAttributeValue(eStart, "x"));
                    inlineTagHandler.setOtherTagShortcutLetter(
                            StringUtil.getFirstLetterLowercase(getAttributeValue(eStart, "type")));
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
                int tagName = StringUtil.getFirstLetterLowercase(segInlineTag.toString());
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
                    tagN = inlineTagHandler.endIT();
                    if ("end".equals(inlineTagHandler.getCurrentPos())) {
                        slashBefore = true;
                    }
                } else if ("ph".equals(eEnd.getName().getLocalPart())) {
                    if (tagName != 0) {
                        inlineTagHandler.setOtherTagShortcutLetter(tagName);
                    } else {
                        tagName = inlineTagHandler.getOtherTagShortcutLetter();
                    }
                    tagN = inlineTagHandler.endPH();
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
                    Log.logErrorRB("TMX_ERROR_READING_LEVEL2", e.getLocation().getLineNumber(),
                            e.getLocation().getColumnNumber());
                    errorsCount++;
                    segContent.setLength(0);
                    return;
                }

                if (inlineTagHandler.getIsExternallyMatched()) {
                    tagN -= minSeenTag;
                }

                segContent.append('<');
                if (slashBefore) {
                    segContent.append('/');
                }
                segContent.appendCodePoint(tagName);
                segContent.append(tagN);
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
     * <p>
     * Language chosen by:<br>
     * - with the same language+country<br>
     * - if not exist, then with the same language but without country<br>
     * - if not exist, then with the same language with whatever country<br>
     */
    protected ParsedTuv getTuvByLang(Language lang) {
        ParsedTuv tuvLC = null; // Tuv with the same language+country
        ParsedTuv tuvL = null; // Tuv with the same language only, without
                               // country
        ParsedTuv tuvLW = null; // Tuv with the same language+whatever country
        for (int i = 0; i < currentTu.tuvs.size(); i++) {
            ParsedTuv tuv = currentTu.tuvs.get(i);
            Language tuvLang = new Language(tuv.lang);
            if (!lang.isSameLanguage(tuvLang)) {
                // language not equals - there is no sense to processing
                continue;
            }
            if (tuvLang.getCountryCode().isEmpty()) {
                // language only, without country
                tuvL = tuv;
            } else if (lang.isSameCountryLanguage(tuvLang)) {
                // the same country and language
                tuvLC = tuv;
            } else {
                // other country
                if (tuvLW == null) { // take first occurrence
                    tuvLW = tuv;
                }
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
        } catch (ParseException ignored) {
        }
        try {
            return dateFormat2.parse(str).getTime();
        } catch (ParseException ignored) {
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
        public List<TMXProp> props = new ArrayList<>();
        public List<ParsedTuv> tuvs = new ArrayList<>();

        void clear() {
            changeid = null;
            changedate = 0;
            creationid = null;
            creationdate = 0;
            props = new ArrayList<>(); // do not CLEAR, because it may be
                                       // shared
            tuvs = new ArrayList<>();
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
        public InputSource resolveEntity(String publicId, String systemId) {
            if (systemId.endsWith("tmx11.dtd")) {
                return new InputSource(TMXReader2.class.getResourceAsStream("/schemas/tmx11.dtd"));
            } else if (systemId.endsWith("tmx14.dtd")) {
                return new InputSource(TMXReader2.class.getResourceAsStream("/schemas/tmx14.dtd"));
            } else {
                return null;
            }
        }
    };

    public static final XMLResolver TMX_DTD_RESOLVER_2 = new XMLResolver() {
        public Object resolveEntity(String publicId, String systemId, String baseURI, String namespace) {
            if (systemId.endsWith("tmx11.dtd")) {
                return TMXReader2.class.getResourceAsStream("/schemas/tmx11.dtd");
            } else if (systemId.endsWith("tmx14.dtd")) {
                return TMXReader2.class.getResourceAsStream("/schemas/tmx14.dtd");
            } else {
                return null;
            }
        }
    };
}
