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

import gen.core.tmx14.Bpt;
import gen.core.tmx14.Ept;
import gen.core.tmx14.Header;
import gen.core.tmx14.Hi;
import gen.core.tmx14.It;
import gen.core.tmx14.Ph;
import gen.core.tmx14.Tmx;
import gen.core.tmx14.Tu;
import gen.core.tmx14.Tuv;
import gen.core.tmx14.Ut;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Helper for read TMX files, using JAXB.
 * 
 * TODO: make TMX Compliance Verification as described on
 * http://www.lisa.org/fileadmin/standards/tmx1.4/comp.htm and http://www.lisa.org/tmx/specification.html.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TMXReader2 {

    static final JAXBContext CONTEXT;

    private static final SimpleDateFormat DATE_FORMAT1, DATE_FORMAT2, DATE_FORMAT_OUT;

    /** Segment Type attribute value: "paragraph" */
    public static final String SEG_PARAGRAPH = "paragraph";
    /** Segment Type attribute value: "sentence" */
    public static final String SEG_SENTENCE = "sentence";
    /** Creation Tool attribute value of OmegaT TMXs: "OmegaT" */
    public static final String CT_OMEGAT = "OmegaT";

    static {
        try {
            CONTEXT = JAXBContext.newInstance(Tmx.class);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }

        DATE_FORMAT1 = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        DATE_FORMAT1.setTimeZone(TimeZone.getTimeZone("UTC"));
        DATE_FORMAT2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        DATE_FORMAT2.setTimeZone(TimeZone.getTimeZone("UTC"));
        DATE_FORMAT_OUT = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        DATE_FORMAT_OUT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Read TMX file.
     */
    public static void readTMX(File file, final Language sourceLanguage, final Language targetLanguage,
            boolean isSegmentingEnabled, final boolean extTmxLevel2, final boolean useSlash,
            final LoadCallback callback) throws Exception {
        Unmarshaller un = CONTEXT.createUnmarshaller();

        // create a new XML parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XMLReader reader = factory.newSAXParser().getXMLReader();
        reader.setContentHandler(un.getUnmarshallerHandler());

        reader.setEntityResolver(TMX_DTD_RESOLVER);

        // install the callback on all PurchaseOrders instances
        un.setListener(new Unmarshaller.Listener() {
            boolean isParagraphSegtype = true;
            boolean isOmegaT = false;
            StringBuilder sb = new StringBuilder();
            StringBuilder tb = new StringBuilder();

            public void beforeUnmarshal(Object target, Object parent) {
            }

            public void afterUnmarshal(Object target, Object parent) {
                if (target instanceof Tu) {
                    Tu tu = (Tu) target;
                    Tuv s = getTuv(tu, sourceLanguage);
                    Tuv t = getTuv(tu, targetLanguage);
                    if (s != null && t != null) {
                        sb.setLength(0);
                        tb.setLength(0);
                        if (isOmegaT) {
                            collectSegOmegaT(sb, s.getSeg().getContent());
                            collectSegOmegaT(tb, t.getSeg().getContent());
                        } else if (extTmxLevel2) {
                            collectSegExtLevel2(sb, s.getSeg().getContent(), useSlash);
                            collectSegExtLevel2(tb, t.getSeg().getContent(), useSlash);
                        } else {
                            collectSegExtLevel1(sb, s.getSeg().getContent());
                            collectSegExtLevel1(tb, t.getSeg().getContent());
                        }
                        callback.onEntry(tu, s, t, sb.toString(), tb.toString(), isParagraphSegtype);
                    }
                } else if (target instanceof Header) {
                    Header h = (Header) target;
                    isParagraphSegtype = SEG_PARAGRAPH.equals(h.getSegtype());
                    isOmegaT = CT_OMEGAT.equals(h.getCreationtool());
                }
            }
        });

        if (file.getName().endsWith(OConsts.TMW_EXTENSION)) {
            // WordFast
            BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                    "ISO-8859-1"));
            try {
                reader.parse(new InputSource(rd));
            } finally {
                rd.close();
            }
        } else {
            // TMX
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            try {
                reader.parse(new InputSource(in));
            } finally {
                in.close();
            }
        }
    }

    protected static void collectSegOmegaT(StringBuilder str, List<Object> content) {
        for (Object c : content) {
            if (c instanceof String) {
                str.append(c);
            } else if (c instanceof Ph) {
                collectSegOmegaT(str, ((Ph) c).getContent());
            } else if (c instanceof Hi) {
                collectSegOmegaT(str, ((Hi) c).getContent());
            } else if (c instanceof It) {
                collectSegOmegaT(str, ((It) c).getContent());
            } else if (c instanceof Ut) {
                collectSegOmegaT(str, ((Ut) c).getContent());
            } else if (c instanceof Bpt) {
                collectSegOmegaT(str, ((Bpt) c).getContent());
            } else if (c instanceof Ept) {
                collectSegOmegaT(str, ((Ept) c).getContent());
            } else {
                throw new RuntimeException("Unknown class in TMX content: " + c.getClass());
            }
        }
    }

    protected static void collectSegExtLevel1(StringBuilder str, List<Object> content) {
        for (Object c : content) {
            if (c instanceof String) {
                str.append(c);
            }
        }
    }

    protected static void collectSegExtLevel2(StringBuilder str, List<Object> content, boolean useSlash) {
        int tagNumber = 0;

        // map of 'i' attributes to tag numbers
        Map<String, Integer> pairTags = new TreeMap<String, Integer>();

        for (Object c : content) {
            if (c instanceof String) {
                str.append(c);
            } else {
                Integer tagEnd = null;
                List<Object> co = null;
                if (c instanceof Ph) {
                    co = ((Ph) c).getContent();
                } else if (c instanceof Hi) {
                    co = ((Hi) c).getContent();
                } else if (c instanceof It) {
                    co = ((It) c).getContent();
                } else if (c instanceof Ut) {
                    co = ((Ut) c).getContent();
                } else if (c instanceof Bpt) {
                    String i = ((Bpt) c).getI();
                    pairTags.put(i, tagNumber);
                    co = ((Bpt) c).getContent();
                } else if (c instanceof Ept) {
                    String i = ((Ept) c).getI();
                    tagEnd = pairTags.get(i);
                    co = ((Ept) c).getContent();
                }
                if (co == null) {
                    throw new RuntimeException("Unknown class in TMX content: " + c.getClass());
                }
                str.append('<');
                char tagName = getFirstLetter(co);
                if (c instanceof Bpt) {
                    str.append(tagName);
                    str.append(Integer.toString(tagNumber));
                    tagNumber++;
                } else if (c instanceof Ept) {
                    str.append('/');
                    str.append(tagName);
                    if (tagEnd != null) {
                        str.append(Integer.toString(tagEnd));
                    } else {
                        str.append(Integer.toString(tagNumber));
                        tagNumber++;
                    }
                } else {
                    str.append(tagName);
                    str.append(Integer.toString(tagNumber));
                    tagNumber++;
                    if (useSlash) {
                        str.append('/');
                    }
                }
                str.append('>');
            }
        }
    }

    protected static char getFirstLetter(List<Object> content) {
        for (Object c : content) {
            char f = 0;
            if (c instanceof String) {
                String s = (String) c;
                for (int i = 0; i < s.length(); i++) {
                    if (Character.isLetter(s.charAt(i))) {
                        f = Character.toLowerCase(s.charAt(i));
                        break;
                    }
                }
            } else {
                List<Object> co = null;
                if (c instanceof Ph) {
                    co = ((Ph) c).getContent();
                } else if (c instanceof Hi) {
                    co = ((Hi) c).getContent();
                } else if (c instanceof It) {
                    co = ((It) c).getContent();
                } else if (c instanceof Ut) {
                    co = ((Ut) c).getContent();
                } else if (c instanceof Bpt) {
                    co = ((Bpt) c).getContent();
                } else if (c instanceof Ept) {
                    co = ((Ept) c).getContent();
                }
                if (co == null) {
                    throw new RuntimeException("Unknown class in TMX content: " + c.getClass());
                }
                f = getFirstLetter(co);
            }
            if (f != 0) {
                return f;
            }
        }
        return 'f';
    }

    /**
     * Returns Tuv from Tu for specific language.
     * 
     * Language choosed by:<br>
     * - with the same language+country<br>
     * - if not exist, then with the same language but without country<br>
     * - if not exist, then with the same language with whatever country<br>
     */
    private static Tuv getTuv(Tu tu, Language lang) {
        String langLanguage = lang.getLanguageCode();
        String langCountry = lang.getCountryCode();
        Tuv tuvLC = null; // Tuv with the same language+country
        Tuv tuvL = null; // Tuv with the same language only, without country
        Tuv tuvLW = null; // Tuv with the same language+whatever country
        for (int i = 0; i < tu.getTuv().size(); i++) {
            Tuv tuv = tu.getTuv().get(i);
            String tuvLang = tuv.getXmlLang();
            if (tuvLang == null) {
                tuvLang = tuv.getLang();
            }
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
        if (tuvLC != null) {
            return tuvLC;
        }
        if (tuvL != null) {
            return tuvL;
        }
        return tuvLW;
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

    /**
     * Callback for receive data from TMX.
     */
    public interface LoadCallback {
        void onEntry(Tu tu, Tuv tuvSource, Tuv tuvTarget, String sourceText, String targetText,
                boolean isParagraphSegtype);
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
