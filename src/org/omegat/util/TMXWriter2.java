/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2006 Henry Pijffers
               2010 Alex Buloichik
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
import gen.core.tmx14.It;
import gen.core.tmx14.Ph;
import gen.core.tmx14.Prop;
import gen.core.tmx14.Seg;
import gen.core.tmx14.Tu;
import gen.core.tmx14.Tuv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.Marshaller;

import org.omegat.core.data.TMXEntry;

/**
 * Helper for write TMX files, using JAXB.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TMXWriter2 {
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final String HEADER_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    private final Writer wr;
    private final Marshaller m;
    private final String langSrc, langTar;
    private final boolean levelTwo;
    private boolean forceValidTMX;

    /**
     * DateFormat with format YYYYMMDDThhmmssZ able to display a date in UTC time.
     * 
     * SimpleDateFormat IS NOT THREAD SAFE !!!
     */
    private final SimpleDateFormat tmxDateFormat;

    /**
     * 
     * @param file
     * @param sourceLanguage
     * @param targetLanguage
     * @param sentenceSegmentingEnabled
     * @param levelTwo
     *            When true, the tmx is made compatible with level 2 (TMX version 1.4)
     * @param callback
     * @throws Exception
     */
    public TMXWriter2(File file, final Language sourceLanguage, final Language targetLanguage,
            boolean sentenceSegmentingEnabled, boolean levelTwo, boolean forceValidTMX) throws Exception {
        this.levelTwo = levelTwo;

        m = TMXReader2.CONTEXT.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_FRAGMENT, true);

        wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), UTF8));

        wr.write(HEADER_XML);
        if (levelTwo) {
            wr.write("<!DOCTYPE tmx SYSTEM \"tmx14.dtd\">\n");
            wr.write("<tmx version=\"1.4\">\n");
        } else {
            wr.write("<!DOCTYPE tmx SYSTEM \"tmx11.dtd\">\n");
            wr.write("<tmx version=\"1.1\">\n");
        }

        m.marshal(createHeader(sourceLanguage, targetLanguage, sentenceSegmentingEnabled), wr);
        wr.write("\n  <body>\n\n");

        langSrc = sourceLanguage.toString();
        langTar = targetLanguage.toString();

        tmxDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
        tmxDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void close() throws IOException {
        try {
            wr.write("\n  </body>\n");
            wr.write("</tmx>\n");
        } finally {
            wr.close();
        }
    }

    public void writeComment(String comment) throws IOException {
        wr.write("\n<!-- " + comment + " -->\n");
    }

    /**
     * Write one entry.
     * 
     * @param source
     * @param translation
     * @param propValues
     *            pairs with property name and values
     */
    public void writeEntry(String source, String translation, TMXEntry entry, String[] propValues)
            throws Exception {
        Tu tu = new Tu();
        Tuv s = new Tuv();
        Tuv t = new Tuv();
        tu.getTuv().add(s);
        tu.getTuv().add(t);

        source = StaticUtils.fixChars(source);
        translation = StaticUtils.fixChars(translation);

        if (forceValidTMX) {
            source = StaticUtils.stripTags(source);
            translation = StaticUtils.stripTags(translation);
        }

        if (levelTwo) {
            s.setSeg(makeLevelTwo(source));
            t.setSeg(makeLevelTwo(translation));
        } else {
            s.setSeg(makeLevelOne(source));
            t.setSeg(makeLevelOne(translation));
        }

        if (!StringUtil.isEmpty(entry.changer)) {
            t.setChangeid(entry.changer);
        }
        if (entry.changeDate > 0) {
            t.setChangedate(tmxDateFormat.format(new Date(entry.changeDate)));
        }

        if (levelTwo) {
            s.setXmlLang(langSrc);
            t.setXmlLang(langTar);
        } else {
            s.setLang(langSrc);
            t.setLang(langTar);
        }

        // add properties
        if (propValues != null) {
            for (int i = 0; i < propValues.length; i += 2) {
                if (propValues[i + 1] == null) {
                    // value is null - not need to write
                    continue;
                }
                Prop p = new Prop();
                p.setType(propValues[i]);
                p.setContent(propValues[i + 1]);
                tu.getNoteOrProp().add(p);
            }
        }

        m.marshal(tu, wr);
        wr.write('\n');
    }

    private static Header createHeader(final Language sourceLanguage, final Language targetLanguage,
            boolean sentenceSegmentingEnabled) {
        Header h = new Header();
        h.setCreationtool("OmegaT");
        h.setOTmf("OmegaT TMX");
        h.setAdminlang("EN-US");
        h.setDatatype("plaintext");

        String version = OStrings.VERSION;
        if (!OStrings.UPDATE.equals("0"))
            version = version + "_" + OStrings.UPDATE;
        h.setCreationtoolversion(version);

        h.setSegtype(sentenceSegmentingEnabled ? TMXReader.SEG_SENTENCE : TMXReader.SEG_PARAGRAPH);

        h.setSrclang(sourceLanguage.toString());

        return h;
    }

    /**
     * Create simple segment.
     */
    private static Seg makeLevelOne(String segment) {
        Seg s = new Seg();
        s.getContent().add(segment);
        return s;
    }

    protected static final Pattern TAGS_START = Pattern.compile("<[\\S&&[^/\\d]]+(\\d+)>");
    protected static final Pattern TAGS_ANY = Pattern.compile("<(/?)[\\S&&[^/\\d]]+(\\d+)(/?)>");

    /**
     * Split to subtags.
     */
    private static Seg makeLevelTwo(final String segmentText) {
        Seg result = new Seg();
        result.getContent().add(segmentText);

        // Find paired tags first
        for (int i = 0; i < result.getContent().size(); i++) {
            if (result.getContent().get(i) instanceof String) {
                List<Object> replace = replacePairedTags((String) result.getContent().get(i));
                if (replace != null) {
                    result.getContent().remove(i);
                    result.getContent().addAll(i, replace);
                }
            }
        }
        // Then find all other tags
        for (int i = 0; i < result.getContent().size(); i++) {
            if (result.getContent().get(i) instanceof String) {
                List<Object> replace = replaceAnyTags((String) result.getContent().get(i));
                if (replace != null) {
                    result.getContent().remove(i);
                    result.getContent().addAll(i, replace);
                }
            }
        }

        return result;
    }

    /**
     * This method finds pair tags and wrap they by <bpt> and <ept> tags.
     * 
     * @param s
     *            source string
     * @return list of elements for replace or null if don't need to replace
     */
    private static List<Object> replacePairedTags(String s) {
        Matcher m = TAGS_START.matcher(s);
        if (m.find()) {
            // get the OmegaT tag and tag number
            String tag = m.group();
            String tagNumber = m.group(1);
            // Check if the corresponding end tag is in this segment too
            String endTag = "</" + tag.substring(1);
            int endTagPos = s.indexOf(endTag);
            boolean paired = endTagPos > m.start();
            if (paired) {
                List<Object> res = new ArrayList<Object>();
                res.add(s.substring(0, m.start()));// Text before start tag

                Bpt bpt = new Bpt();
                bpt.setI(tagNumber);
                bpt.setX(tagNumber);
                bpt.getContent().add(tag);
                res.add(bpt);

                res.add(s.substring(m.start() + tag.length(), endTagPos));// Text inside tags

                Ept ept = new Ept();
                ept.setI(tagNumber);
                ept.getContent().add(endTag);
                res.add(ept);

                res.add(s.substring(endTagPos + endTag.length()));// Text after end tag
                return res;
            }
        }
        return null;
    }

    enum TAG_TYPE {
        SINGLE, START, END
    };

    /**
     * This method finds any non-paired or sigle tags and wrap they by <ph> and <it> tags.
     * 
     * @param s
     *            source string
     * @return list of elements for replace or null if don't need to replace
     */
    private static List<Object> replaceAnyTags(String s) {

        Matcher m = TAGS_ANY.matcher(s);
        if (m.find()) {
            // get the OmegaT tag and tag number
            String tag = m.group();
            String tagNumber = m.group(2);
            TAG_TYPE tagType;
            if (m.group(3).length() > 0) {
                tagType = TAG_TYPE.SINGLE;
            } else if (m.group(1).length() > 0) {
                tagType = TAG_TYPE.END;
            } else {
                tagType = TAG_TYPE.START;
            }

            List<Object> res = new ArrayList<Object>();
            res.add(s.substring(0, m.start()));// Text before tag

            switch (tagType) {
            case SINGLE:
                Ph ph = new Ph();
                ph.setX(tagNumber);
                ph.getContent().add(tag);
                res.add(ph);
                break;
            case START:
                It itbeg = new It();
                itbeg.setPos("begin");
                itbeg.setX(tagNumber);
                itbeg.getContent().add(tag);
                res.add(itbeg);
                break;
            case END:
                It itend = new It();
                itend.setPos("end");
                itend.setX(tagNumber);
                itend.getContent().add(tag);
                res.add(itend);
                break;
            default:
                throw new RuntimeException("Unknow tag type");
            }

            res.add(s.substring(m.end()));// Text after tag
            return res;
        }
        return null;
    }

    /**
     * Creates three-quarted-assed TMX level 2 segments from OmegaT internal segments
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    private static String makeLevelTwoOld(String segment) {
        // Create a storage buffer for the result
        StringBuffer result = new StringBuffer(segment.length() * 2);

        // Find all single tags
        // Matcher match =
        // Pattern.compile("&lt;[a-zA-Z\-]+\\d+/&gt;").matcher(segment);
        Matcher match = Pattern.compile("<[\\S&&[^/\\d]]+(\\d+)/>").matcher(segment);
        int previousMatchEnd = 0;
        while (match.find()) {
            // get the OmegaT tag and tag number
            String tag = match.group();
            String tagNumber = match.group(1);

            // Wrap the OmegaT tag in TMX tags in the result
            result.append(segment.substring(previousMatchEnd, match.start())); // text between prev. & cur.
                                                                               // match
            result.append("<ph x='"); // TMX start tag + i attribute
            result.append(tagNumber); // OmegaT tag number used as x attribute
            result.append("'>");
            result.append(tag); // OmegaT tag
            result.append("</ph>"); // TMX end tag

            // Store the current match's end positions
            previousMatchEnd = match.end();
        }

        // Append the text from the last match (single tag) to the end of the
        // segment
        result.append(segment.substring(previousMatchEnd, segment.length()));
        segment = result.toString(); // Store intermediate result back in segment
        result.setLength(0); // Clear result buffer

        // Find all start tags
        match = Pattern.compile("<[\\S&&[^/\\d]]+(\\d+)>").matcher(segment);
        previousMatchEnd = 0;
        while (match.find()) {
            // get the OmegaT tag and tag number
            String tag = match.group();
            String tagNumber = match.group(1);

            // Check if the corresponding end tag is in this segment too
            String endTag = "</" + tag.substring(4);
            boolean paired = segment.indexOf(endTag) > -1;

            // Wrap the OmegaT tag in TMX tags in the result
            result.append(segment.substring(previousMatchEnd, match.start())); // text between prev. & cur.
                                                                               // match

            if (paired) {
                result.append("<bpt i='"); // TMX start tag + i attribute
                result.append(tagNumber); // OmegaT tag number used as i
                                          // attribute
                result.append("'");
            } else {
                result.append("<it pos='begin'"); // TMX start tag
            }
            result.append(" x='"); // TMX x attribute
            result.append(tagNumber); // OmegaT tag number used as x attribute
            result.append("'>");
            result.append(tag); // OmegaT tag
            result.append(paired ? "</bpt>" : "</it>"); // TMX end tag

            // Store the current match's end positions
            previousMatchEnd = match.end();
        }

        // Append the text from the last match (start tag) to the end of the
        // segment
        result.append(segment.substring(previousMatchEnd, segment.length()));
        segment = result.toString(); // Store intermediate result back in
                                     // segment
        result.setLength(0); // Clear result buffer

        // Find all end tags
        match = Pattern.compile("</[\\S&&[^\\d]]+(\\d+)>").matcher(segment);
        previousMatchEnd = 0;
        while (match.find()) {
            // get the OmegaT tag and tag number
            String tag = match.group();
            String tagNumber = match.group(1);

            // Check if the corresponding start tag is in this segment too
            String startTag = "<" + tag.substring(5);
            boolean paired = segment.indexOf(startTag) > -1;

            // Wrap the OmegaT tag in TMX tags in the result
            result.append(segment.substring(previousMatchEnd, match.start())); // text between prev. & cur.
                                                                               // match

            result.append(paired ? "<ept i='" : "<it pos='end' x='"); // TMX
                                                                      // start
                                                                      // tag +
                                                                      // i/x
                                                                      // attribute
            result.append(tagNumber); // OmegaT tag number used as i/x attribute
            result.append("'>");
            result.append(tag); // OmegaT tag
            result.append(paired ? "</ept>" : "</it>"); // TMX end tag

            // Store the current match's end positions
            previousMatchEnd = match.end();
        }

        // Append the text from the last match (end tag) to the end of the segment
        result.append(segment.substring(previousMatchEnd, segment.length()));

        // Done, return result
        return result.toString();
    }
}
