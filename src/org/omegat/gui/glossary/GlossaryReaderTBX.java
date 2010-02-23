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

package org.omegat.gui.glossary;

import gen.core.tbx.LangSet;
import gen.core.tbx.Martif;
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
import org.omegat.util.OStrings;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Reader for TBX glossaries.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class GlossaryReaderTBX {
    protected static final JAXBContext TBX_CONTEXT;
    static {
        try {
            TBX_CONTEXT = JAXBContext.newInstance(Martif.class);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(OStrings
                    .getString("STARTUP_JAXB_LINKAGE_ERROR"));
        }
    }

    static SAXParserFactory SAX_FACTORY = SAXParserFactory.newInstance();
    static {
        SAX_FACTORY.setNamespaceAware(true);
        SAX_FACTORY.setValidating(false);
    }

    public static List<GlossaryEntry> read(final File file) throws Exception {

        Martif tbx = load(file);

        String sLang = Core.getProject().getProjectProperties()
                .getSourceLanguage().getLanguageCode();
        String tLang = Core.getProject().getProjectProperties()
                .getTargetLanguage().getLanguageCode();

        StringBuilder note = new StringBuilder();
        List<GlossaryEntry> result = new ArrayList<GlossaryEntry>();
        for (TermEntry te : tbx.getText().getBody().getTermEntry()) {
            String sTerm = null;
            String tTerm = null;
            note.setLength(0);
            for (LangSet ls : te.getLangSet()) {
                String lang = ls.getLang();
                for (Object o : ls.getTigOrNtig()) {
                    if (o instanceof Tig) {
                        Tig t = (Tig) o;
                        if (sLang.equalsIgnoreCase(lang)) {
                            sTerm = readContent(t.getTerm().getContent());
                        } else if (tLang.equalsIgnoreCase(lang)) {
                            tTerm = readContent(t.getTerm().getContent());
                        }
                        for (TermNote tn : t.getTermNote()) {
                            if (note.length() > 0) {
                                note.append('\n');
                            }
                            note.append(readContent(tn.getContent()));
                        }
                    }
                }
            }
            if (sTerm != null && tTerm != null) {
                result.add(new GlossaryEntry(sTerm, tTerm, note.toString()));
            }
        }

        return result;
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
        private static final InputSource EMPTY_INPUT_SOURCE = new InputSource(
                new ByteArrayInputStream(new byte[0]));

        public NamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        public InputSource resolveEntity(String publicId, String systemId) {
            return EMPTY_INPUT_SOURCE;
        }
    }
}
