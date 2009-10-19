/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
 Portions copyright 2007 Zoltan Bartko - bartkozoltan@bartkozoltan.com
               2009 Alex Buloichik
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.StringEntry;
import org.omegat.core.data.TransMemory;
import org.omegat.util.RuntimePreferences.PSEUDO_TRANSLATE_TYPE;

/**
 * Class that store TMX (Translation Memory Exchange) files.
 */
public class TMXWriter {
    /**
     * Prepare TMX data for export to file.
     * 
     * @param addOrphans
     *            When true, the segments in the m_orphanedList are added to the
     *            TMX as well.
     * @param m_strEntryList
     *            List of translated segments
     * @param m_orphanedList
     *            List of translated segments that have no match in the current
     *            sources
     * @param pseudoTranslate
     *            When true, a tu-section is created for every segment in the
     *            sources, even when there is no translation available.
     * @param pseudo_translate_type
     *            When pseudo-translate is true:<br>
     *            If 'equal' then the translation that is written is equal to
     *            the source.<br>
     *            If 'empty', the translation is an empty string.<br>
     *            When pseudoTranslate is false, this parameter is ignored.
     * @return map of strings for TMX
     */
    public static Map<String, String> prepareTMXData(final boolean addOrphans,
            final List<StringEntry> m_strEntryList,
            final List<TransMemory> m_orphanedList,
            final boolean pseudoTranslate,
            PSEUDO_TRANSLATE_TYPE pseudo_translate_type) {
        Map<String, String> result = new HashMap<String, String>();
        String source = null;
        String target = null;
        for (StringEntry se : m_strEntryList) {
            source = se.getSrcText();
            if (!pseudoTranslate) {
                target = se.getTranslation();
                if (target.length() == 0)
                    continue;
            } else {
                if (pseudo_translate_type.equals(PSEUDO_TRANSLATE_TYPE.EQUAL)) {
                    target = source;
                } else {
                    // must be PSEUDO_TRANSLATE_TYPE.EMPTY
                    target = "";
                }
            }
            result.put(source, target);
        }

        // Write orphan strings. Assume N/A when pseudo-translate.
        if (addOrphans) {
            for (TransMemory transMem : m_orphanedList) {
                if (transMem.target.length() == 0)
                    continue;
                source = transMem.source;
                target = transMem.target;
                if (target.length() == 0)
                    continue;
                result.put(source, target);
            }
        }
        return result;
    }

    /**
     * Saves a TMX file to disk
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     * @author Maxym Mykhalchuk
     * @param filename       The name of the file to create
     * @param forceValidTMX  When true, OmegaT-tags are stripped from the segments.
     * @param levelTwo    When true, the tmx is made compatible with level 2 (TMX version 1.4)
     * @param m_config    Project configuration, to get the languages
     * @param data        Data for save to TMX
     * @throws IOException
     */
    public static void buildTMXFile(final String filename, final boolean forceValidTMX, 
            final boolean levelTwo, final ProjectProperties m_config, final Map<String,String> data) throws IOException {
        // we got this far, so assume lang codes are proper
        String sourceLocale = m_config.getSourceLanguage().toString();
        String targetLocale = m_config.getTargetLanguage().toString();
        String segmenting;
        if (m_config.isSentenceSegmentingEnabled())
            segmenting = TMXReader.SEG_SENTENCE;
        else
            segmenting = TMXReader.SEG_PARAGRAPH;

        FileOutputStream fos = new FileOutputStream(filename);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8"); // NOI18N
        PrintWriter out = new PrintWriter(osw); // PW is easier to use than Buff.Writer

        String version = OStrings.VERSION;
        if (!OStrings.UPDATE.equals("0")) // NOI18N
            version = version + "_" + OStrings.UPDATE;
        // Write TMX header
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); // NOI18N
        if (levelTwo)
            out.println("<!DOCTYPE tmx SYSTEM \"tmx14.dtd\">"); // NOI18N
        else
            out.println("<!DOCTYPE tmx SYSTEM \"tmx11.dtd\">"); // NOI18N
        if (levelTwo)
            out.println("<tmx version=\"1.4\">"); // NOI18N
        else
            out.println("<tmx version=\"1.1\">"); // NOI18N
        out.println("  <header"); // NOI18N
        out.println("    creationtool=\"OmegaT\""); // NOI18N
        out.println("    creationtoolversion=\"" + version + "\""); // NOI18N
        out.println("    segtype=\"" + segmenting + "\""); // NOI18N
        out.println("    o-tmf=\"OmegaT TMX\""); // NOI18N
        out.println("    adminlang=\"EN-US\""); // NOI18N
        out.println("    srclang=\"" + sourceLocale + "\""); // NOI18N
        out.println("    datatype=\"plaintext\""); // NOI18N
        out.println("  >"); // NOI18N
        out.println("  </header>"); // NOI18N
        out.println("  <body>"); // NOI18N

        // Determine language attribute to use
        String langAttr = levelTwo ? "xml:lang" : "lang";

        // Write TUs
        String source = null;
        String target = null;
        for(Map.Entry<String, String> en:data.entrySet()) {
            source = forceValidTMX ? StaticUtils.stripTags(en.getKey()) : en
                    .getKey();
            target = forceValidTMX ? StaticUtils.stripTags(en.getValue()) : en
                    .getValue();
            if (levelTwo) {
                source = makeLevelTwo(source);
                target = makeLevelTwo(target);
            }
            source = StaticUtils.makeValidXML(source);
            target = StaticUtils.makeValidXML(target);
            out.println("    <tu>");
            out.println("      <tuv " + langAttr + "=\"" + sourceLocale + "\">");
            out.println("        <seg>" + source + "</seg>");
            out.println("      </tuv>");
            out.println("      <tuv " + langAttr + "=\"" + targetLocale + "\">");
            out.println("        <seg>" + target + "</seg>");
            out.println("      </tuv>");
            out.println("    </tu>");
        }

        // Write TMX footer
        out.println("  </body>"); // NOI18N
        out.println("</tmx>"); // NOI18N

        // Close output stream
        out.close();
    }

    /**
     * Creates three-quarted-assed TMX level 2 segments from OmegaT internal
     * segments
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    private static String makeLevelTwo(String segment) {
        // Create a storage buffer for the result
        StringBuffer result = new StringBuffer(segment.length() * 2);

        // Find all single tags
        //Matcher match = Pattern.compile("&lt;[a-zA-Z\-]+\\d+/&gt;").matcher(segment);
        Matcher match = Pattern.compile("&lt;[\\S&&[^/\\d]]+(\\d+)/&gt;").matcher(segment);
        int previousMatchEnd = 0;
        while (match.find()) {
            // get the OmegaT tag and tag number
            String tag = match.group();
            String tagNumber = match.group(1);

            // Wrap the OmegaT tag in TMX tags in the result
            result.append(segment.substring(previousMatchEnd, match.start())); // text betw. prev. & cur. match
            result.append("<ph x='"); // TMX start tag + i attribute
            result.append(tagNumber); // OmegaT tag number used as x attribute
            result.append("'>");
            result.append(tag); // OmegaT tag
            result.append("</ph>"); // TMX end tag

            // Store the current match's end positions
            previousMatchEnd = match.end();
        }

        // Append the text from the last match (single tag) to the end of the segment
        result.append(segment.substring(previousMatchEnd, segment.length()));
        segment = result.toString(); // Store intermediate result back in segment
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
            result.append(segment.substring(previousMatchEnd, match.start())); // text betw. prev. & cur. match
            if (paired) {
                result.append("<bpt i='"); // TMX start tag + i attribute
                result.append(tagNumber); // OmegaT tag number used as i attribute
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

        // Append the text from the last match (start tag) to the end of the segment
        result.append(segment.substring(previousMatchEnd, segment.length()));
        segment = result.toString(); // Store intermediate result back in segment
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
            result.append(segment.substring(previousMatchEnd, match.start())); // text betw. prev. & cur. match
            result.append(paired ? "<ept i='" : "<it pos='end' x='"); // TMX start tag + i/x attribute
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
