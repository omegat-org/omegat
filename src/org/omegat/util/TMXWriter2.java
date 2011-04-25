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

import gen.core.tmx14.Header;
import gen.core.tmx14.Prop;
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
import java.util.Date;
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

        if (forceValidTMX) {
            source = StaticUtils.stripTags(source);
            translation = StaticUtils.stripTags(translation);
        }

        if (levelTwo) {
            source = makeLevelTwo(source);
            translation = makeLevelTwo(translation);
        }

        s.setSeg(source);
        t.setSeg(translation);

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
                p.setvalue(propValues[i + 1]);
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
     * Creates three-quarted-assed TMX level 2 segments from OmegaT internal segments
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    private static String makeLevelTwo(String segment) {
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
