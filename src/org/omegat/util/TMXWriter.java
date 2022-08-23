/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
 Portions copyright 2007 Zoltan Bartko - bartkozoltan@bartkozoltan.com
               2009 Alex Buloichik
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.ProjectProperties;

/**
 * Class that store TMX (Translation Memory Exchange) files.
 *
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Maxym Mykhalchuk
 *
 */
@Deprecated
public final class TMXWriter {

    private TMXWriter() {
    }

    /**
     * Saves a TMX file to disk
     *
     * @param filename
     *            The name of the file to create
     * @param forceValidTMX
     *            When true, OmegaT-tags are stripped from the segments.
     * @param levelTwo
     *            When true, the tmx is made compatible with level 2 (TMX
     *            version 1.4)
     * @param config
     *            Project configuration, to get the languages
     * @param data
     *            Data for save to TMX, a map of {source segments, translation}
     * @throws IOException
     */
    public static void buildTMXFile(final String filename, final boolean forceValidTMX,
            final boolean levelTwo, final ProjectProperties config, final Map<String, PrepareTMXEntry> data)
            throws IOException {
        // we got this far, so assume lang codes are proper
        String sourceLocale = config.getSourceLanguage().toString();
        String targetLocale = config.getTargetLanguage().toString();
        String segmenting;
        if (config.isSentenceSegmentingEnabled()) {
            segmenting = "sentence";
        } else {
            segmenting = "paragraph";
        }
        FileOutputStream fos = new FileOutputStream(filename);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        PrintWriter out = new PrintWriter(osw); // PW is easier to use than
                                                // Buff.Writer

        String version = OStrings.VERSION;
        if (!OStrings.UPDATE.equals("0")) {
            version = version + "_" + OStrings.UPDATE;
        }
        // Write TMX header
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        if (levelTwo) {
            out.println("<!DOCTYPE tmx SYSTEM \"tmx14.dtd\">");
        } else {
            out.println("<!DOCTYPE tmx SYSTEM \"tmx11.dtd\">");
        }
        if (levelTwo) {
            out.println("<tmx version=\"1.4\">");
        } else {
            out.println("<tmx version=\"1.1\">");
        }
        out.println("  <header");
        out.println("    creationtool=\"OmegaT\"");
        out.println("    creationtoolversion=\"" + version + "\"");
        out.println("    segtype=\"" + segmenting + "\"");
        out.println("    o-tmf=\"OmegaT TMX\"");
        out.println("    adminlang=\"EN-US\"");
        out.println("    srclang=\"" + sourceLocale + "\"");
        out.println("    datatype=\"plaintext\"");
        out.println("  >");
        out.println("  </header>");
        out.println("  <body>");

        // Determine language attribute to use
        String langAttr = levelTwo ? "xml:lang" : "lang";

        // Write TUs
        String source = null;
        String target = null;
        String note = null;
        TMXDateParser dateParser = new TMXDateParser();
        for (Map.Entry<String, PrepareTMXEntry> en : data.entrySet()) {
            PrepareTMXEntry transEntry = en.getValue();
            source = forceValidTMX ? TagUtil.stripXmlTags(en.getKey()) : en.getKey();
            target = forceValidTMX ? TagUtil.stripXmlTags(transEntry.translation) : transEntry.translation;
            source = StringUtil.makeValidXML(source);
            target = StringUtil.makeValidXML(target);
            if (transEntry.note != null) {
                note = forceValidTMX ? TagUtil.stripXmlTags(transEntry.note) : transEntry.note;
                note = StringUtil.makeValidXML(note);
            }
            // TO DO: This *possibly* converts occurrences in the actual text of
            // &lt;fX&gt;
            // which it should not.
            if (levelTwo) {
                source = makeLevelTwo(source);
                target = makeLevelTwo(target);
            }
            String changeIdPropertyString = (transEntry.changer != null && !"".equals(transEntry.changer)
                    ? " changeid=\"" + transEntry.changer + "\"" : "");
            String changeDatePropertyString = (transEntry.changeDate != 0 ? " changedate=\""
                    + dateParser.getTMXDate(transEntry.changeDate) + "\"" : "");
            out.println("    <tu>");
            out.println("      <tuv " + langAttr + "=\"" + sourceLocale + "\">");
            out.println("        <seg>" + source + "</seg>");
            out.println("      </tuv>");
            out.println("      <tuv " + langAttr + "=\"" + targetLocale + "\"" + changeDatePropertyString
                    + changeIdPropertyString + ">");
            out.println("        <seg>" + target + "</seg>");
            out.println("      </tuv>");
            if (note != null) {
                out.println("      <note>" + note + "</note>");
            }
            out.println("    </tu>");
        }

        // Write TMX footer
        out.println("  </body>");
        out.println("</tmx>");

        // Close output stream
        out.close();
    }

    /**
     * Creates three-quarted-assed TMX level 2 segments from OmegaT internal
     * segments
     */
    private static String makeLevelTwo(String segment) {
        // Create a storage buffer for the result
        StringBuilder result = new StringBuilder(segment.length() * 2);

        // Find all single tags
        // Matcher match =
        // Pattern.compile("&lt;[a-zA-Z\-]+\\d+/&gt;").matcher(segment);
        Matcher match = Pattern.compile("&lt;[\\S&&[^/\\d]]+(\\d+)/&gt;").matcher(segment);
        int previousMatchEnd = 0;
        while (match.find()) {
            // get the OmegaT tag and tag number
            String tag = match.group();
            String tagNumber = match.group(1);

            // Wrap the OmegaT tag in TMX tags in the result
            result.append(segment.substring(previousMatchEnd, match.start())); // text
                                                                               // betw.
                                                                               // prev.
                                                                               // &
                                                                               // cur.
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
        segment = result.toString(); // Store intermediate result back in
                                     // segment
        result.setLength(0); // Clear result buffer

        // Find all start tags
        match = Pattern.compile("&lt;[\\S&&[^/\\d]]+(\\d+)&gt;").matcher(segment);
        previousMatchEnd = 0;
        while (match.find()) {
            // get the OmegaT tag and tag number
            String tag = match.group();
            String tagNumber = match.group(1);

            // Check if the corresponding end tag is in this segment too
            String endTag = "&lt;/" + tag.substring(4);
            boolean paired = segment.indexOf(endTag) > -1;

            // Wrap the OmegaT tag in TMX tags in the result
            result.append(segment.substring(previousMatchEnd, match.start())); // text
                                                                               // betw.
                                                                               // prev.
                                                                               // &
                                                                               // cur.
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
        match = Pattern.compile("&lt;/[\\S&&[^\\d]]+(\\d+)&gt;").matcher(segment);
        previousMatchEnd = 0;
        while (match.find()) {
            // get the OmegaT tag and tag number
            String tag = match.group();
            String tagNumber = match.group(1);

            // Check if the corresponding start tag is in this segment too
            String startTag = "&lt;" + tag.substring(5);
            boolean paired = segment.indexOf(startTag) > -1;

            // Wrap the OmegaT tag in TMX tags in the result
            result.append(segment.substring(previousMatchEnd, match.start())); // text
                                                                               // betw.
                                                                               // prev.
                                                                               // &
                                                                               // cur.
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

        // Append the text from the last match (end tag) to the end of the
        // segment
        result.append(segment.substring(previousMatchEnd, segment.length()));

        // Done, return result
        return result.toString();
    }
}
