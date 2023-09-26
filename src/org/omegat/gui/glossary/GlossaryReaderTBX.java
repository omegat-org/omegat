/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Didier Briel
               2011 Didier Briel, Guido Leenders
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

package org.omegat.gui.glossary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import org.omegat.core.Core;
import org.omegat.util.Language;
import org.omegat.util.Preferences;

import gen.core.tbx.Descrip;
import gen.core.tbx.DescripGrp;
import gen.core.tbx.Hi;
import gen.core.tbx.LangSet;
import gen.core.tbx.Martif;
import gen.core.tbx.Note;
import gen.core.tbx.Ntig;
import gen.core.tbx.TermEntry;
import gen.core.tbx.TermNote;
import gen.core.tbx.Tig;

/**
 * Reader for TBX glossaries.
 *
 * @author Alex Buloichik <alex73mail@gmail.com>
 * @author Didier Briel
 * @author Guido Leenders
 */
public final class GlossaryReaderTBX {

    private GlossaryReaderTBX() {
    }


    static final SAXParserFactory SAX_FACTORY = SAXParserFactory.newInstance();
    static {
        SAX_FACTORY.setNamespaceAware(true);
        SAX_FACTORY.setValidating(false);
    }

    public static List<GlossaryEntry> read(final File file, boolean priorityGlossary) throws Exception {
        Martif tbx = load(file);
        return readMartif(tbx, priorityGlossary, file.getPath());
    }

    public static List<GlossaryEntry> read(final String data, boolean priorityGlossary, String origin)
            throws Exception {
        Martif tbx = loadFromString(data);
        return readMartif(tbx, priorityGlossary, origin);
    }

    public static List<GlossaryEntry> readMartif(final Martif tbx, boolean priorityGlossary, String origin)
            throws Exception {
        if (tbx.getText() == null) {
            return Collections.emptyList();
        }
        String sLang = Core.getProject().getProjectProperties().getSourceLanguage().getLanguageCode();
        String tLang = Core.getProject().getProjectProperties().getTargetLanguage().getLanguageCode();

        StringBuilder note = new StringBuilder();
        StringBuilder descTerm = new StringBuilder();
        StringBuilder descTig = new StringBuilder();
        List<GlossaryEntry> result = new ArrayList<GlossaryEntry>();
        List<String> sTerms = new ArrayList<String>();
        List<String> tTerms = new ArrayList<String>();
        for (TermEntry te : tbx.getText().getBody().getTermEntry()) {
            note.setLength(0);
            descTerm.setLength(0);
            descTig.setLength(0);
            appendDescOrNote(te.getDescripOrDescripGrpOrAdmin(), descTerm);
            for (LangSet ls : te.getLangSet()) {
                Language termLanguage = new Language(ls.getLang());
                // We use only the language code
                String lang = termLanguage.getLanguageCode();
                appendDescOrNote(ls.getDescripOrDescripGrpOrAdmin(), descTig);
                for (Object o : ls.getTigOrNtig()) {
                    if (o instanceof Tig) {
                        Tig t = (Tig) o;
                        if (sLang.equalsIgnoreCase(lang)) {
                            sTerms.add(readContent(t.getTerm().getContent()));
                        } else if (tLang.equalsIgnoreCase(lang)) {
                            tTerms.add(readContent(t.getTerm().getContent()));
                            appendDescOrNote(t.getTermNote(), note);
                        }
                        appendDescOrNote(t.getDescripOrDescripGrpOrAdmin(), descTig);
                    } else if (o instanceof Ntig) {
                        Ntig n = (Ntig) o;
                        if (sLang.equalsIgnoreCase(lang)) {
                            sTerms.add(readContent(n.getTermGrp().getTerm().getContent()));
                        } else if (tLang.equalsIgnoreCase(lang)) {
                            tTerms.add(readContent(n.getTermGrp().getTerm().getContent()));
                            appendDescOrNote(n.getTermGrp().getTermNoteOrTermNoteGrp(), note);
                        }
                        appendDescOrNote(n.getDescripOrDescripGrpOrAdmin(), descTig);
                    }
                }
            }
            StringBuilder comment = new StringBuilder();
            appendLine(comment, descTerm.toString());
            appendLine(comment, descTig.toString());
            appendLine(comment, note.toString());
            for (String s : sTerms) {
                boolean addedForLang = false;
                for (String t : tTerms) {
                    result.add(new GlossaryEntry(s, t, comment.toString(), priorityGlossary, origin));
                    addedForLang = true;
                }
                if (!addedForLang) { // An entry is created just to get the definition
                    result.add(new GlossaryEntry(s, "", comment.toString(), priorityGlossary, origin));
                }
            }
            sTerms.clear();
            tTerms.clear();
        }

        return result;
    }

    /**
     * Add description or note into StringBuilder.
     */
    protected static void appendDescOrNote(final List<?> list, StringBuilder str) {
        for (Object o : list) {
            String line = null;
            if (o instanceof Descrip) {
                Descrip d = (Descrip) o;
                if ("context".equalsIgnoreCase(d.getType())) {
                    if (Preferences.isPreferenceDefault(Preferences.GLOSSARY_TBX_DISPLAY_CONTEXT,
                            Preferences.GLOSSARY_TBX_DISPLAY_CONTEXT_DEFAULT)) {
                        line = d.getType() + ": " + readContent(d.getContent());
                    }
                } else {
                    line = d.getType() + ": " + readContent(d.getContent());
                }
            } else if (o instanceof DescripGrp) {
                DescripGrp dg = (DescripGrp) o;
                if (dg.getDescrip() != null) {
                    if ("context".equalsIgnoreCase(dg.getDescrip().getType())) {
                        if (Preferences.isPreferenceDefault(Preferences.GLOSSARY_TBX_DISPLAY_CONTEXT,
                                Preferences.GLOSSARY_TBX_DISPLAY_CONTEXT_DEFAULT)) {
                            line = dg.getDescrip().getType() + ": " + readContent(dg.getDescrip().getContent());
                        }
                    } else {
                        line = dg.getDescrip().getType() + ": " + readContent(dg.getDescrip().getContent());
                    }
                }
            } else if (o instanceof TermNote) {
                TermNote tn = (TermNote) o;
                line = readContent(tn.getContent());
            } else if (o instanceof Note) {
                Note n = (Note) o;
                line = readContent(n.getContent());
            }
            if (line != null) {
                appendLine(str, line);
            }
        }
    }

    protected static void appendLine(final StringBuilder str, String line) {
        if (line.isEmpty()) { // No need to append empty lines
            return;
        }
        if (str.length() > 0) {
            str.append('\n');
        }
        str.append(line);
    }

    protected static String readContent(final List<Object> content) {
        StringBuilder res = new StringBuilder();
        for (Object o : content) {
            if (o instanceof Hi) {
                Hi hi = (Hi) o;
                res.append(" *").append(hi.getContent()).append("* ");
            } else {
                res.append(o.toString());
            }
        }
        return res.toString();
    }

    private static Unmarshaller createUnmarshaller() throws JAXBException {
        final JAXBContext tbxContext;
        Thread thread = Thread.currentThread();
        ClassLoader originalClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(Martif.class.getClassLoader());
        tbxContext = JAXBContext.newInstance(Martif.class);
        thread.setContextClassLoader(originalClassLoader);
        return tbxContext.createUnmarshaller();
    }

    /**
     * Load tbx file, but skip DTD resolving.
     */
    static Martif load(File f) throws Exception {
        Unmarshaller unm = createUnmarshaller();

        SAXParser parser = SAX_FACTORY.newSAXParser();

        NamespaceFilter xmlFilter = new NamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unm.getUnmarshallerHandler());

        try (FileInputStream in = new FileInputStream(f)) {
            SAXSource source = new SAXSource(xmlFilter, new InputSource(in));
            return (Martif) unm.unmarshal(source);
        }
    }

    static Martif loadFromString(String data) throws Exception {
        Unmarshaller unm = createUnmarshaller();

        SAXParser parser = SAX_FACTORY.newSAXParser();

        NamespaceFilter xmlFilter = new NamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unm.getUnmarshallerHandler());

        try (StringReader in = new StringReader(data)) {
            SAXSource source = new SAXSource(xmlFilter, new InputSource(in));
            return (Martif) unm.unmarshal(source);
        }
    }

    public static class NamespaceFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(new byte[0]));

        public NamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) {
            return EMPTY_INPUT_SOURCE;
        }
    }
}
