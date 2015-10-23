/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Alex Buloichik
               2011 Martin Fleurke
               2013-2014 Enrique Estevez, Didier Briel
               2015 Aaron Madlon-Kay, Enrique Estevez
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.filters2.text.bundles;

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.omegat.core.data.ProtectedPart;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.Log;
import org.omegat.util.LinebreakPreservingReader;
import org.omegat.util.NullBufferedWriter;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.StringUtil;
import org.omegat.util.TagUtil;

/**
 * Filter to support Java Resource Bundles - the files that are used to I18ze
 * Java applications.
 * 
 * @author Maxym Mykhalchuk
 * @author Keith Godfrey
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Enrique Estevez (keko.gl@gmail.com)
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 *
 * Option to remove untranslated segments in the target files
 * Code adapted from the file: MozillaDTDFilter.java
 * Support for encoding outside the ASCII encoding. The management depends of the user.
 * The user have to choose the encoding of the file, source and target. 
 * The default is ASCII, which corresponds to the standard behaviour: in that case, any character above 127 is encoded
 * according to the specifications of the bundle files. If another character set is chosen, no encoding takes place
 * and it's up to the user to select a charset compatible with the characters used.
 * "auto" for the target encoding is considered as being ASCII.
 *
 * Support for the comments into the Comments panel (localization notes).
 * Optionally can leave Unicode literals (\\uXXXX) unescaped.
 */
public class ResourceBundleFilter extends AbstractFilter {

    public static final String OPTION_REMOVE_STRINGS_UNTRANSLATED = "unremoveStringsUntranslated";
    public static final String OPTION_DONT_UNESCAPE_U_LITERALS = "dontUnescapeULiterals";
    public static final String DEFAULT_TARGET_ENCODING = OConsts.ASCII;

    protected Map<String, String> align;
    
    private String targetEncoding = DEFAULT_TARGET_ENCODING;
    
    /**
     * If true, will remove non-translated segments in the target files
     */
    private boolean removeStringsUntranslated = false;
    
    /**
     * If true, will not convert characters into \\uXXXX notation
     */
    private boolean dontUnescapeULiterals = false;

    @Override
    public String getFileFormatName() {
        return OStrings.getString("RBFILTER_FILTER_NAME");
    }

    /**
     * 
     * @return true, because it is possible to change source encoding
     */
    @Override
    public boolean isSourceEncodingVariable() {
        return true;
    }

    /**
     * 
     * @return true, because it is possible to change target encoding
     */
    @Override
    public boolean isTargetEncodingVariable() {
        return true;
    }

    /**
     * The default encoding is OConsts.ASCII 
    */
    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.properties", OConsts.ASCII, OConsts.ASCII, TFP_NAMEONLY + "_"
                + TFP_TARGET_LOCALE + "." + TFP_EXTENSION) };
    }

    /**
     * Creating an output stream to save a localized resource bundle.
     * <p>
     * NOTE: the name of localized resource bundle is different from the name of
     * original one. e.g. "Bundle.properties" -> Russian =
     * "Bundle_ru.properties"
     */
    @Override
    public BufferedWriter createWriter(File outfile, String encoding) throws UnsupportedEncodingException,
            IOException {
        if (encoding != null) {
            targetEncoding = encoding;
        }
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), targetEncoding));
    }

    @Override
    protected String getOutputEncoding(FilterContext fc) {
        String encoding = fc.getOutEncoding();
        // Use default if the user didn't specify anything ("<auto>")
        return encoding == null ? DEFAULT_TARGET_ENCODING : encoding;
    }

    /**
     * Reads next line from the input and:
     * <ul>
     * <li>Converts ascii-encoded \\uxxxx chars to normal characters.
     * <li>Converts \r, \n and \t to CR, line feed and tab.
     * <li>But! Keeps a backspace in '\ ', '\=', '\:' etc (non-trimmable space
     * or non-key-value-breaking :-) equals).
     * <ul>
     * Change from BufferedReader to LinebreakPreservingReader was part of fix
     * for bug 1462566
     */
    protected String getNextLine(LinebreakPreservingReader reader) throws IOException, TranslationException {
        String ascii = reader.readLine();
        if (ascii == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        for (int cp, len = ascii.length(), i = 0; i < len; i += Character.charCount(cp)) {
            cp = ascii.codePointAt(i);
            if (cp == '\\' && ascii.codePointCount(i, len) > 1) {
                i += Character.charCount(cp);
                cp = ascii.codePointAt(i);
                if (cp != 'u') {
                    if (cp == 'n') {
                        cp = '\n';
                    } else if (cp == 'r') {
                        cp = '\r';
                    } else if (cp == 't') {
                        cp = '\t';
                    } else {
                        result.append('\\');
                    }
                } else if (dontUnescapeULiterals) {
                    // Put back the \ we swallowed
                    result.append('\\');
                } else {
                    // checking if the string is long enough
                    if (ascii.codePointCount(i, len) < 1 + 4) {
                        throw new TranslationException(OStrings.getString("RBFH_ERROR_ILLEGAL_U_SEQUENCE"));
                    }
                    int uStart = ascii.offsetByCodePoints(i, 1);
                    int uEnd = ascii.offsetByCodePoints(uStart, 4);
                    String uStr = ascii.substring(uStart, uEnd);
                    try {
                        cp = Integer.parseInt(uStr, 16);
                        if (!Character.isValidCodePoint(cp)) {
                            throw new TranslationException(OStrings.getString("RBFH_ERROR_ILLEGAL_U_SEQUENCE"));
                        }
                        i = uEnd - Character.charCount(cp);
                    } catch (NumberFormatException ex) {
                        throw new TranslationException(OStrings.getString("RBFH_ERROR_ILLEGAL_U_SEQUENCE"), ex);
                    }
                }
            }
            result.appendCodePoint(cp);
        }

        return result.toString();
    }

    /**
     * Converts normal strings to ascii-encoded ones.
     * 
     * @param text
     *            Text to convert.
     * @param key
     *            Whether it's a key of the key-value pair (' ', ':', '=' MUST
     *            be escaped in a key and MAY be escaped in value, but we don't
     *            escape these).
     * @param encodingAscii
     *            If false, keep the text in the source encoding (if assume what
     *            it is UTF-8, what is the another supported encoding)
     */
    private String toAscii(String text, boolean key) {
        CharsetEncoder charsetEncoder = Charset.forName(targetEncoding).newEncoder();
        
        StringBuilder result = new StringBuilder();

        for (int cp, len = text.length(), i = 0; i < len; i += Character.charCount(cp)) {
            cp = text.codePointAt(i);
            if (cp == '\\') {
                if (dontUnescapeULiterals && containsUEscapeAt(text, i)) {
                    result.append("\\");
                } else {
                    result.append("\\\\");
                }
            } else if (cp == '\n') {
                result.append("\\n");
            } else if (cp == '\r') {
                result.append("\\r");
            } else if (cp == '\t') {
                result.append("\\t");
            } else if (key && cp == ' ') {
                result.append("\\ ");
            } else if (key && cp == '=') {
                result.append("\\=");
            } else if (key && cp == ':') {
                result.append("\\:");
            } else if ((cp >= 32 && cp < 127) || charsetEncoder.canEncode(text.substring(i, i + Character.charCount(cp)))) {
                result.appendCodePoint(cp);
            } else {
                for (char c : Character.toChars(cp)) {
                    String code = Integer.toString(c, 16);
                    while (code.codePointCount(0, code.length()) < 4) {
                        code = '0' + code;
                    }
                    result.append("\\u" + code);
                }
            }
        }

        return result.toString();
        
    }
    
    private static boolean containsUEscapeAt(String text, int offset) {
        if (text.codePointCount(offset, text.length()) < 1 + 1 + 4) {
            return false;
        }
        if (text.codePointAt(text.offsetByCodePoints(offset, 1)) != 'u') {
            return false;
        }
        int uStart = text.offsetByCodePoints(offset, 2);
        int uEnd = text.offsetByCodePoints(uStart, 4);
        String uStr = text.substring(uStart, uEnd);
        try {
            int uChr = Integer.parseInt(uStr, 16);
            return Character.isValidCodePoint(uChr);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Removes extra slashes from, e.g. "\ ", "\=" and "\:" typical in
     * machine-generated resource bundles. A slash at the end of a string means
     * a mandatory space has been trimmed.
     * <p>
     * See also bugreport <a
     * href="http://sourceforge.net/support/tracker.php?aid=1606595"
     * >#1606595</a>.
     */
    private String removeExtraSlashes(String string) {
        StringBuilder result = new StringBuilder(string.length());
        for (int cp, len = string.length(), i = 0; i < len; i += Character.charCount(cp)) {
            cp = string.codePointAt(i);
            if (cp == '\\') {
                if (dontUnescapeULiterals && containsUEscapeAt(string, i)) {
                    // Don't remove \ before \\uXXXX if we are not unescaping
                } else if (string.codePointCount(i, len) > 1) {
                    // Fix for [ 1812183 ] Properties: space before "=" shouldn't
                    // be part of the key, contributed by Arno Peters
                    i += Character.charCount(cp);
                    cp = string.codePointAt(i);
                } else {
                    cp = ' ';
                }
            }
            result.appendCodePoint(cp);
        }
        return result.toString();
    }

    /**
     * Trims the string from left. Also contains some code to strip a backspace
     * from '\ ' (non-trimmable space), but doesn't trim this space.
     */
    private String leftTrim(String s) {
        int i = 0;
        while (i < s.length()) {
            int cp = s.codePointAt(i);
            if (cp != ' ' && cp != '\t') {
                break;
            }
            i += Character.charCount(cp);
        }
        s = s.replaceAll("\\\\ ", " ");
        return s.substring(i, s.length());
    }

    /**
     * Doing the processing of the file...
     */
    @Override
    public void processFile(BufferedReader reader, BufferedWriter outfile, FilterContext fc)
            throws IOException, TranslationException {
        LinebreakPreservingReader lbpr = new LinebreakPreservingReader(reader); // fix for bug 1462566
        String str;
        // Support to show the comments (localization notes) into the Comments panel
        String comments;
        boolean noi18n = false;

        // Parameter in the options of filter to customize the target file
        removeStringsUntranslated = processOptions != null && "true".equalsIgnoreCase(processOptions.get(OPTION_REMOVE_STRINGS_UNTRANSLATED));
        
        // Parameter in the options of filter to customize the behavior of the filter
        dontUnescapeULiterals = processOptions != null && "true".equalsIgnoreCase(processOptions.get(OPTION_DONT_UNESCAPE_U_LITERALS));
        
        // Initialize the comments
        comments = null;
        while ((str = getNextLine(lbpr)) != null) {

            // Variable to check if a segment is translated
            boolean translatedSegment = true;

            String trimmed = str.trim();

            // skipping empty strings
            if (trimmed.isEmpty()) {
                outfile.write(str + lbpr.getLinebreak());
                // Delete the comments
                comments = null;
                continue;
            }

            // skipping comments
            int firstCp = trimmed.codePointAt(0);
            if (firstCp == '#' || firstCp == '!') {
                outfile.write(toAscii(str, false) + lbpr.getLinebreak());
                // Save the comments
                comments = (comments == null ? str : comments + "\n" + str);
                // checking if the next string shouldn't be internationalized
                if (trimmed.indexOf("NOI18N") >= 0) {
                    noi18n = true;
                }
                continue;
            }

            // reading the glued lines
            while (str.codePointBefore(str.length()) == '\\') {
                String next = getNextLine(lbpr);
                if (next == null) {
                    next = "";
                }
                // gluing this line (w/o '\' on this line)
                // with next line (w/o leading spaces)
                str = str.substring(0, str.offsetByCodePoints(str.length(), -1)) + leftTrim(next);
            }

            // key=value pairs
            int equalsPos = searchEquals(str);

            // writing out key
            String key;
            if (equalsPos >= 0) {
                key = str.substring(0, equalsPos).trim();
            } else {
                key = str.trim();
            }
            key = removeExtraSlashes(key);
            // writing segment is delayed until verifying that the translation was made
            // outfile.write(toAscii(key, true));

            // advance if there're spaces or tabs after =
            if (equalsPos >= 0) {
                int equalsEnd = str.offsetByCodePoints(equalsPos, 1);
                while (equalsEnd < str.length()) {
                    int cp = str.codePointAt(equalsEnd);
                    if (cp != ' ' && cp != '\t') {
                        break;
                    }
                    equalsEnd += Character.charCount(cp);
                }
                String equals = str.substring(equalsPos, equalsEnd);
                // writing segment is delayed until verifying that the translation was made
                // outfile.write(equals);

                // value, if any
                String value;
                if (equalsEnd < str.length()) {
                    value = removeExtraSlashes(str.substring(equalsEnd));
                } else {
                    value = "";
                }

                if (noi18n) {
                    // if we don't need to internationalize
                    outfile.write(toAscii(value, false));
                    noi18n = false;
                } else {
                    value = value.replaceAll("\\n\\n", "\n \n");
                    // If there is a comment, show it into the Comments panel
                    String trans = process(key, value, comments);
                    // Delete the comments
                    comments = null;
                    // Check if the segment is not translated
                    if ("--untranslated_yet--".equals(trans)) {
                        translatedSegment = false;
                        trans = value;
                    }
                    trans = trans.replaceAll("\\n\\s\\n", "\n\n");
                    trans = toAscii(trans, false);
                    if (!trans.isEmpty() && trans.codePointAt(0) == ' ') {
                        trans = '\\' + trans;
                    }
                    // Non-translated segments are written based on the filter options 
                    if (translatedSegment == true || removeStringsUntranslated == false) {
                        outfile.write(toAscii(key, true));
                        outfile.write(equals);
                        outfile.write(trans);
                        outfile.write(lbpr.getLinebreak()); // fix for bug 1462566
                    }
                }
            }
            // This line of code is moved up to avoid blank lines
            // outfile.write(lbpr.getLinebreak()); // fix for bug 1462566
        }
    }

    /**
     * Looks for the key-value separator (=,: or ' ') in the string.
     * <p>
     * See also bugreport <a
     * href="http://sourceforge.net/support/tracker.php?aid=1606595"
     * >#1606595</a>.
     * 
     * @return The char number of key-value separator in a string. Not that if
     *         the string does not contain any separator this string is
     *         considered to be a key with empty string value, and this method
     *         returns <code>-1</code> to indicate there's no equals.
     */
    private int searchEquals(String str) {
        int prevCp = 'a';
        for (int cp, i = 0; i < str.length(); i += Character.charCount(cp)) {
            cp = str.codePointAt(i);
            if (prevCp != '\\') {
                if (cp == '=' || cp == ':') {
                    return i;
                } else if (cp == ' ' || cp == '\t') {
                    for (int cp2, j = str.offsetByCodePoints(i, 1); j < str.length(); j += Character.charCount(cp2)) {
                        cp2 = str.codePointAt(j);
                        if (cp2 == ':' || cp2 == '=') {
                            return j;
                        }
                        if (cp2 != ' ' && cp2 != '\t') {
                            return i;
                        }
                    }
                    return i;
                }
            }
            prevCp = cp;
        }
        return -1;
    }

    // Support to show the comments (localization notes) into the Comments panel
    // Added the c parameter, of type String, which is the comment showed in the interface
    protected String process(String key, String value, String c) {
        if (entryParseCallback != null) {
            List<ProtectedPart> protectedParts = TagUtil.applyCustomProtectedParts(value,
                    PatternConsts.SIMPLE_JAVA_MESSAGEFORMAT_PATTERN_VARS, null);
            entryParseCallback.addEntry(key, value, null, false, c, null, this, protectedParts);
            return value;
        } else if (entryTranslateCallback != null) {
            String trans = entryTranslateCallback.getTranslation(key, value, null);
            return trans != null ? trans : "--untranslated_yet--";
        } else if (entryAlignCallback != null) {
            align.put(key, value);
        }
        return value;
    }

    @Override
    protected void alignFile(BufferedReader sourceFile, BufferedReader translatedFile, org.omegat.filters2.FilterContext fc) throws Exception {
        Map<String, String> source = new HashMap<String, String>();
        Map<String, String> translated = new HashMap<String, String>();

        align = source;
        processFile(sourceFile, new NullBufferedWriter(), fc);
        align = translated;
        processFile(translatedFile, new NullBufferedWriter(), fc);
        for (Map.Entry<String, String> en : source.entrySet()) {
            String tr = translated.get(en.getKey());
            if (!StringUtil.isEmpty(tr)) {
                entryAlignCallback.addTranslation(en.getKey(), en.getValue(), tr, false, null, this);
            }
        }
    }

    
    @Override
    public Map<String, String> changeOptions(Dialog parent, Map<String, String> config) {
        try {
            ResourceBundleOptionsDialog dialog = new ResourceBundleOptionsDialog(parent, config);
            dialog.setVisible(true);
            if (ResourceBundleOptionsDialog.RET_OK == dialog.getReturnStatus())
                return dialog.getOptions();
            else
                return null;
        } catch (Exception e) {
            Log.log(OStrings.getString("RB_FILTER_EXCEPTION"));
            Log.log(e);
            return null;
        }
    }

    /**
     * Returns true to indicate that Java Resource Bundles filter has options.
     * 
     */
    @Override
    public boolean hasOptions() {
        return true;
    }
    
}
