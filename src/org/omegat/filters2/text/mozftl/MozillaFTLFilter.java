/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2019 Enrique Estevez
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

package org.omegat.filters2.text.mozftl;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.util.LinebreakPreservingReader;
import org.omegat.util.Log;
import org.omegat.util.NullBufferedWriter;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Filter to support Files with Key=Value pairs, which are sometimes used for
 * i18n of software.
 *
 * @author Enrique Estevez
 */
public class MozillaFTLFilter extends AbstractFilter {

    public static final String OPTION_REMOVE_STRINGS_UNTRANSLATED = "unremoveStringsUntranslated";

    protected Map<String, String> align;
    // For the entries with attributes. Example: .title = When using
    // But, it also can be of type: .tooltiptext =
    // There always is something, then dot, then the key + space + = and
    // Nothing or space + value. For this reason, the last group is with * (0 or
    // n times)
    protected static final Pattern ATTRIBUTES = Pattern.compile(" +\\.([^ ]+) =(.*)");

    /**
     * If true, will remove non-translated segments in the target files
     */
    private boolean removeStringsUntranslated = false;

    public String getFileFormatName() {
        return OStrings.getString("MOZFTL_FILTER_NAME");
    }

    public boolean isSourceEncodingVariable() {
        return true;
    }

    public boolean isTargetEncodingVariable() {
        return true;
    }

    public Instance[] getDefaultInstances() {
        return new Instance[] {
                new Instance("*.ftl", StandardCharsets.UTF_8.name(), StandardCharsets.UTF_8.name()) };
    }

    /**
     * Trims the string from left.
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
        return s.substring(i, s.length());
    }

    /**
     * Doing the processing of the file...
     */
    @Override
    public void processFile(BufferedReader reader, BufferedWriter outfile, FilterContext fc)
            throws IOException {

        // Parameter in the options of filter to customize the target file
        removeStringsUntranslated = processOptions != null
                && "true".equalsIgnoreCase(processOptions.get(OPTION_REMOVE_STRINGS_UNTRANSLATED));

        String str;
        String comments = null;
        int identation = 1;
        String key = null;
        String k = null;
        String key_attr = "";
        String value = null;
        boolean multiline = false;

        LinebreakPreservingReader lbpr = new LinebreakPreservingReader(reader);
        str = lbpr.readLine();
        while (str != null) {
            String trimmed = str.trim();

            // skipping empty strings
            if (trimmed.isEmpty()) {
                outfile.write(str);
                outfile.write(lbpr.getLinebreak());
                // Delete the comments
                comments = null;
                str = lbpr.readLine();
                continue;
            }

            // skipping comments
            int firstCp = trimmed.codePointAt(0);
            if (firstCp == '#') {
                outfile.write(str);
                outfile.write(lbpr.getLinebreak());
                // Save the comments
                comments = (comments == null ? str : comments + "\n" + str);
                str = lbpr.readLine();
                continue;
            }

            // Variable to check if a segment is translated
            boolean translatedSegment = true;

            // key=value pairs
            int equalsPos = str.indexOf('=');

            // if there's no separator, assume it's a key w/o a value
            if (equalsPos == -1 || multiline) {
                multiline = true;
                equalsPos = str.offsetByCodePoints(str.length(), -1);
            } else {
                key = str.substring(0, equalsPos).trim();
            }

            // advance if there're spaces after =
            while (str.codePointCount(equalsPos, str.length()) > 1) {
                int nextOffset = str.offsetByCodePoints(equalsPos, 1);
                if (str.codePointAt(nextOffset) != ' ') {
                    break;
                }
                equalsPos = nextOffset;
            }

            int afterEqualsPos = str.offsetByCodePoints(equalsPos, 1);

            String v;
            if (multiline) {
                v = str;
                int ide = v.length() - leftTrim(v).length();
                if (identation == 1) {
                    identation = ide;
                } else {
                    identation = (identation < ide ? identation : ide);
                }
                // writing out everything before = (and = itself)
            } else {
                // outfile.write(str.substring(0, afterEqualsPos));
                if (k == null) {
                    k = "";
                } else {
                    k += "\n";
                }
                k += str.substring(0, afterEqualsPos);
                v = str.substring(afterEqualsPos);
            }
            if (value == null) {
                value = v;
            } else {
                String aux = value.concat("\n").concat(v);
                value = aux;
            }
            if (!multiline) {
                key = (Objects.equals(key, key_attr) ? key : key_attr + key);
            }
            str = lbpr.readLine();
            if (str != null && !str.isEmpty()) {
                int cp = str.codePointAt(0);
                if (cp == ' ' && !ATTRIBUTES.matcher(str).matches()) {
                    multiline = true;
                    continue;
                }
                if (cp == ' ') {
                    key_attr = (Objects.equals(key_attr, "") ? key : key_attr);
                    if (value.isEmpty()) {
                        value = null;
                        // outfile.write(lbpr.getLinebreak());
                        continue;
                    }
                } else {
                    key_attr = "";
                }
            } else {
                key_attr = "";
            }
            if (entryAlignCallback != null) {
                align.put(key, value);
            } else if (entryParseCallback != null) {
                entryParseCallback.addEntry(key, value, null, false, comments, null, this, null);
            } else if (entryTranslateCallback != null) {
                String trans = entryTranslateCallback.getTranslation(key, value, null);
                if (trans == null) {
                    trans = value;
                    translatedSegment = false;
                }
                // Non-translated segments are written based on the
                // filter options
                if (translatedSegment || !removeStringsUntranslated) {
                    outfile.write(k);
                    outfile.write(trans);
                    outfile.write(lbpr.getLinebreak());
                }
                k = null;
            }
            multiline = false;
            identation = 1;
            value = null;
            comments = null;
        }
        lbpr.close();
    }

    @Override
    protected void alignFile(BufferedReader sourceFile, BufferedReader translatedFile,
            org.omegat.filters2.FilterContext fc) throws Exception {
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
    public Map<String, String> changeOptions(Window parent, Map<String, String> config) {
        try {
            MozillaFTLOptionsDialog dialog = new MozillaFTLOptionsDialog(parent, config);
            dialog.setVisible(true);
            if (MozillaFTLOptionsDialog.RET_OK == dialog.getReturnStatus()) {
                return dialog.getOptions();
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.log(OStrings.getString("MOZFTL_FILTER_EXCEPTION"));
            Log.log(e);
            return null;
        }
    }

    /**
     * Returns true to indicate that Mozilla FTL filter has options.
     *
     */
    @Override
    public boolean hasOptions() {
        return true;
    }

}
