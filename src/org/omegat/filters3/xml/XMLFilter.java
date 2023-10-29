/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2008 Didier Briel
               2013 Didier Briel, Alex Buloichik
               2015 Aaron Madlon-Kay
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

package org.omegat.filters3.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import org.omegat.core.data.ProtectedPart;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.TranslationException;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.PatternConsts;

/**
 * Abstract basis filter for XML format filters: OpenDocument, DocBook etc.
 * Ideally should allow creation of a new XML dialect filter by simply
 * specifying translatable tags and attributes.
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Alex Buloichik
 * @author Aaron Madlon-Kay
 */
public abstract class XMLFilter extends AbstractFilter implements Translator {
    /** Factory for SAX parsers. */
    private final SAXParserFactory parserFactory;

    /** XML dialect this filter handles. */
    private final XMLDialect dialect;

    /** Creates a new instance of XMLFilter */
    public XMLFilter(XMLDialect dialect) {
        parserFactory = SAXParserFactory.newInstance();
        try {
            // We validate XML in default
            parserFactory.setFeature("http://xml.org/sax/features/validation", true);
            // When a driver writer wants not to validate, please override and
            // set features false.
            // ex. setSAXFeature("http://xml.org/sax/features/validation",
            // false);

            // Protecting from a XXE attack.

            // "Feature for Secure Processing (FSP)" is the central mechanism to
            // help safeguard XML processing. It instructs XML processors, such
            // as parsers,
            // validators, and transformers, to try and process XML securely.
            parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // Avoid internet connection to validate with external DTD.
            parserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            // Disable external general entities
            parserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            // Disable external parameter entities
            parserFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            // as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and
            // Entity Attacks"
            parserFactory.setXIncludeAware(false);
            // Support namespaces and xmlns:prefixes
            parserFactory.setFeature("http://xml.org/sax/features/namespaces", true);
            parserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        } catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException ex) {
            Log.logErrorRB(ex, "XML_FILTER_ERROR", ex.getMessage());
        }
        this.dialect = dialect;
    }

    /** Gives the dialect */
    public XMLDialect getDialect() {
        return dialect;
    }

    protected void setSAXFeature(String feature, boolean b)
            throws SAXNotSupportedException, SAXNotRecognizedException, ParserConfigurationException {
        parserFactory.setFeature(feature, b);
    }

    /** Detected encoding of the input XML file. */
    private String encoding;

    /** Detected EOL chars. */
    private String eol;

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
        if (outEncoding == null) {
            outEncoding = this.encoding;
        }
        if (outFile == null) {
            return new BufferedWriter(new StringWriter());
        } else {
            return new BufferedWriter(new XMLWriter(outFile, outEncoding, eol));
        }
    }

    /**
     * Target language of the project
     */
    private Language targetLanguage;

    /**
     * @return The target language of the project
     */
    @Override
    public Language getTargetLanguage() {
        return targetLanguage;
    }

    /**
     * Source language of the project
     */
    private Language sourceLanguage;

    /**
     * @return The source language of the project
     */
    @Override
    public Language getSourceLanguage() {
        return sourceLanguage;
    }

    /** Processes an XML file. */
    @Override
    public void processFile(File inFile, File outFile, FilterContext fc)
            throws IOException, TranslationException {
        try (BufferedReader inReader = createReader(inFile, fc.getInEncoding())) {
            inEncodingLastParsedFile = this.encoding;
            targetLanguage = fc.getTargetLang();
            sourceLanguage = fc.getSourceLang();
            InputSource source = new InputSource(inReader);
            source.setSystemId(inFile.toURI().toString());
            SAXParser parser = parserFactory.newSAXParser();
            Handler handler = new Handler(this, dialect, inFile, outFile, fc);
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
            parser.setProperty("http://xml.org/sax/properties/declaration-handler", handler);
            parser.parse(source, handler);
        } catch (ParserConfigurationException e) {
            throw new TranslationException(e);
        } catch (SAXException e) {
            throw new TranslationException(e.getMessage());
        }
    }

    @Override
    protected void processFile(BufferedReader inFile, BufferedWriter outFile, FilterContext fc)
            throws IOException, TranslationException {
        throw new UnsupportedOperationException(
                "XMLFilter.processFile(BufferedReader,BufferedWriter) should never be called!");
    }

    /**
     * Whether source encoding can be varied by the user. If XML file has no
     * encoding declaration, UTF-8 will be used, hence returns
     * <code>false</code> by default.
     *
     * @return <code>false</code>
     */
    @Override
    public boolean isSourceEncodingVariable() {
        return false;
    }

    /**
     * Target encoding can be varied by the user.
     *
     * @return <code>true</code>
     */
    @Override
    public boolean isTargetEncodingVariable() {
        return true;
    }

    /**
     * The method the Handler would call to pass translatable content to OmegaT
     * core and receive translation.
     */
    @Override
    public String translate(String entry, List<ProtectedPart> protectedParts) {
        if (entryParseCallback != null) {
            entryParseCallback.addEntry(null, entry, null, false, null, null, this, protectedParts);
            return entry;
        } else if (entryTranslateCallback != null) {
            String translation = entryTranslateCallback.getTranslation(null, entry, null);
            return translation != null ? translation : entry;
        } else { // We're not supposed to be there, (parsing called from inside
                 // isFileSupported, for instance)
            return entry; // so what we return is not important
        }

    }

    /**
     * Returns whether the XML file is supported by the filter. <br>
     * Reads {@link org.omegat.util.OConsts#READ_AHEAD_LIMIT} and tries to
     * detect constrained text and match constraints defined in
     * {@link XMLDialect} against them.
     */
    @Override
    public boolean isFileSupported(BufferedReader reader) {
        if (dialect.getConstraints() == null || dialect.getConstraints().isEmpty()) {
            return true;
        }

        try {
            char[] cbuf = new char[OConsts.READ_AHEAD_LIMIT];
            int cbufLen = reader.read(cbuf);
            String buf = new String(cbuf, 0, cbufLen);
            Matcher matcher = PatternConsts.XML_DOCTYPE.matcher(buf);
            if (matcher.find()) {
                Pattern doctype = dialect.getConstraints().get(XMLDialect.CONSTRAINT_DOCTYPE);
                if (doctype != null
                        && (matcher.group(1) == null || !doctype.matcher(matcher.group(1)).matches())) {
                    return false;
                }
                Pattern publicc = dialect.getConstraints().get(XMLDialect.CONSTRAINT_PUBLIC_DOCTYPE);
                if (publicc != null
                        && (matcher.group(3) == null || !publicc.matcher(matcher.group(3)).matches())) {
                    return false;
                }
                Pattern system = dialect.getConstraints().get(XMLDialect.CONSTRAINT_SYSTEM_DOCTYPE);
                if (system != null
                        && (matcher.group(5) == null || !system.matcher(matcher.group(5)).matches())) {
                    return false;
                }
            } else if (dialect.getConstraints().containsKey(XMLDialect.CONSTRAINT_DOCTYPE)
                    || dialect.getConstraints().containsKey(XMLDialect.CONSTRAINT_PUBLIC_DOCTYPE)
                    || dialect.getConstraints().containsKey(XMLDialect.CONSTRAINT_SYSTEM_DOCTYPE)) {
                return false;
            }

            matcher = PatternConsts.XML_ROOTTAG.matcher(buf);
            if (matcher.find()) {
                Pattern root = dialect.getConstraints().get(XMLDialect.CONSTRAINT_ROOT);
                if (root != null && (matcher.group(1) == null || !root.matcher(matcher.group(1)).matches())) {
                    return false;
                }
            } else if (dialect.getConstraints().containsKey(XMLDialect.CONSTRAINT_ROOT)) {
                return false;
            }

            matcher = PatternConsts.XML_XMLNS.matcher(buf);
            if (matcher.find()) {
                Pattern xmlns = dialect.getConstraints().get(XMLDialect.CONSTRAINT_XMLNS);
                return xmlns == null
                        || (matcher.group(2) != null && xmlns.matcher(matcher.group(2)).matches());
            } else {
                return !dialect.getConstraints().containsKey(XMLDialect.CONSTRAINT_XMLNS);
            }

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void tagStart(String path, Attributes atts) {
    }

    @Override
    public void tagEnd(String path) {
    }

    @Override
    public void comment(String comment) {
    }

    @Override
    public void text(String text) {
    }

    @Override
    public boolean isInIgnored() {
        return false;
    }
}
