/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Didier Briel
               2011 Didier Briel, Guido Leenders
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

package org.omegat.gui.glossary;

import gen.core.tbx.Descrip;
import gen.core.tbx.DescripGrp;
import gen.core.tbx.LangSet;
import gen.core.tbx.Martif;
import gen.core.tbx.Note;
import gen.core.tbx.Ntig;
import gen.core.tbx.TermEntry;
import gen.core.tbx.TermNote;
import gen.core.tbx.Tig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.omegat.core.Core;
import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Reader for TBX glossaries.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 * @author Didier Briel
 * @author Guido Leenders
 */
public class GlossaryReaderTBX {
    protected static final JAXBContext TBX_CONTEXT;
    static {
        try {
            TBX_CONTEXT = JAXBContext.newInstance(Martif.class);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(OStrings.getString("STARTUP_JAXB_LINKAGE_ERROR"));
        }
    }

    static SAXParserFactory SAX_FACTORY = SAXParserFactory.newInstance();
    static {
        SAX_FACTORY.setNamespaceAware(true);
        SAX_FACTORY.setValidating(false);
    }

    public static List<GlossaryEntry> read(final File file) throws Exception {
        Martif tbx = load(file);

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
                    result.add(new GlossaryEntry(s, t, comment.toString()));
                    addedForLang = true;
                }
                if (!addedForLang) { // An entry is created just to get the definition
                    result.add(new GlossaryEntry(s, "", comment.toString()));
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
                line = d.getType() + ": " + readContent(d.getContent());
            } else if (o instanceof DescripGrp) {
                DescripGrp dg = (DescripGrp) o;
                line = dg.getDescrip().getType() + ": " + readContent(dg.getDescrip().getContent());
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
        if (!(line.length() > 0)) // No need to append empty lines
            return;
        if (str.length() > 0) {
            str.append('\n');
        }
        str.append(line);
    }

    protected static String readContent(final List<Object> content) {
        StringBuilder res = new StringBuilder();
        for (Object o : content) {
            res.append(o.toString());
        }
        return res.toString();
    }

    /**
     * Load tbx file, but skip DTD resolving.
     */
    static Martif load(File f) throws Exception {
        Unmarshaller unm = TBX_CONTEXT.createUnmarshaller();

        SAXParser parser = SAX_FACTORY.newSAXParser();

        NamespaceFilter xmlFilter = new NamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unm.getUnmarshallerHandler());

        FileInputStream in = new FileInputStream(f);
        try {
            SAXSource source = new SAXSource(xmlFilter, new InputSource(in));

            return (Martif) unm.unmarshal(source);
        } finally {
            in.close();
        }
    }

    public static class NamespaceFilter extends XMLFilterImpl {
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(new ByteArrayInputStream(
                new byte[0]));

        public NamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) {
            return EMPTY_INPUT_SOURCE;
        }
    }
}
