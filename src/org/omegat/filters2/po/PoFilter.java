/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Thomas Huriaux
               2008 Martin Fleurke
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

package org.omegat.filters2.po;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.OStrings;

/**
 * Filter to support po files (in various encodings).
 * 
 * Format described on
 * http://www.gnu.org/software/hello/manual/gettext/PO-Files.html
 * 
 * Filter is not thread-safe !
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Thomas Huriaux
 * @author Martin Fleurke
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class PoFilter extends AbstractFilter {

    protected static Pattern COMMENT_FUZZY = Pattern.compile("#, fuzzy");
    protected static Pattern COMMENT_FUZZY_OTHER = Pattern
            .compile("#,.* fuzzy.*");
    protected static Pattern COMMENT_NOWRAP = Pattern.compile("#,.* no-wrap.*");
    protected static Pattern MSG_ID = Pattern
            .compile("msgid(_plural)? \"(.*)\"");
    protected static Pattern MSG_STR = Pattern
            .compile("msgstr(\\[[0-9]+\\])? \"(.*)\"");
    protected static Pattern MSG_OTHER = Pattern.compile("\"(.*)\"");
    
    /** Prefix to source for fuzzy segments. */
    protected static String FUZZY_SOURCE_PREFIX = "[PO-fuzzy] ";
    /** Add fuzzy segments to legacy TM instead translations. */
    protected static boolean FUZZY_TO_LEGACY = true;

    enum MODE {
        MSGID, MSGSTR, MSGID_PLURAL, MSGSTR_PLURAL
    };

    private StringBuilder[] sources, targets;
    private boolean nowrap, fuzzy;

    private BufferedWriter out;

    public String getFileFormatName() {
        return OStrings.getString("POFILTER_FILTER_NAME");
    }

    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.po"), // NOI18N
                new Instance("*.pot") // NOI18N
        };
    }

    public boolean isSourceEncodingVariable() {
        return true;
    }

    public boolean isTargetEncodingVariable() {
        return true;
    }

    public List<File> processFile(File inFile, String inEncoding, File outFile,
            String outEncoding) throws IOException, TranslationException {
        BufferedReader reader = createReader(inFile, inEncoding);
        BufferedWriter writer;

        if (outFile != null)
            writer = createWriter(outFile, outEncoding);
        else
            writer = null;

        processFile(reader, writer);

        reader.close();
        if (writer != null) {
            writer.close();
        }
        return null;
    }

    public void processFile(BufferedReader in, BufferedWriter out)
            throws IOException {
        // BOM (byte order mark) bugfix
        in.mark(1);
        int ch = in.read();
        if (ch != 0xFEFF)
            in.reset();

        this.out = out;
        processPoFile(in);
    }

    private void processPoFile(BufferedReader in) throws IOException {
        fuzzy = false;
        nowrap = false;
        MODE currentMode = null;
        int currentPlural = 0;

        sources = new StringBuilder[2];
        sources[0] = new StringBuilder();
        sources[1] = new StringBuilder();
        targets = new StringBuilder[2];
        targets[0] = new StringBuilder();
        targets[1] = new StringBuilder();

        String s;
        while ((s = in.readLine()) != null) {
            /*
             * Removing the fuzzy markers, as it has no meanings after being
             * processed by omegat
             */
            if (COMMENT_FUZZY.matcher(s).matches()) {
                fuzzy = true;
                flushTranslation(currentMode);
                continue;
            } else if (COMMENT_FUZZY_OTHER.matcher(s).matches()) {
                fuzzy = true;
                flushTranslation(currentMode);
                s = s.replaceAll("(.*), fuzzy(.*)", "$1$2");
            }

            // FSM for po files
            if (COMMENT_NOWRAP.matcher(s).matches()) {
                flushTranslation(currentMode);
                /*
                 * Read the no-wrap comment, indicating that the creator of the
                 * po-file did not want long messages to be wrapped on multiple
                 * lines. See 5.6.2 no-wrap of http://docs.oasis-open
                 * .org/xliff/v1.2/xliff-profile-po/xliff
                 * -profile-po-1.2-cd02.html for an example.
                 */
                nowrap = true;
                eol(s);
                continue;
            }
            Matcher m;

            if ((m = MSG_ID.matcher(s)).matches()) {
                String text = m.group(2);
                if (m.group(1) == null) {
                    // non-plural ID
                    currentMode = MODE.MSGID;
                    sources[0].append(text);
                } else {
                    // plural ID
                    currentMode = MODE.MSGID_PLURAL;
                    sources[1].append(text);
                }
                eol(s);

                continue;
            }

            if ((m = MSG_STR.matcher(s)).matches()) {
                String text = m.group(2);
                if (m.group(1) == null) {
                    // non-plural lines
                    currentMode = MODE.MSGSTR;
                    targets[0].append(text);
                } else {
                    currentMode = MODE.MSGSTR_PLURAL;
                    // plurals, i.e. msgstr[N] lines
                    if ("[0]".equals(m.group(1))) {
                        targets[0].append(text);
                        currentPlural = 0;
                    } else if ("[1]".equals(m.group(1))) {
                        targets[1].append(text);
                        currentPlural = 1;
                    }
                }
                continue;
            }

            if ((m = MSG_OTHER.matcher(s)).matches()) {
                String text = m.group(1);
                switch (currentMode) {
                case MSGID:
                    sources[0].append(text);
                    eol(s);
                    break;
                case MSGID_PLURAL:
                    sources[1].append(text);
                    eol(s);
                    break;
                case MSGSTR:
                    targets[0].append(text);
                    break;
                case MSGSTR_PLURAL:
                    targets[currentPlural].append(text);
                    break;
                }
                continue;
            }

            flushTranslation(currentMode);
            eol(s);
        }
        flushTranslation(currentMode);
    }

    protected void eol(String s) throws IOException {
        if (out != null) {
            out.write(s);
            out.write('\n');
        }
    }

    protected void align(int pair) {
        String s = unescape(sources[pair].toString());
        String t = unescape(targets[pair].toString());
        align(s, t);
    }

    protected void align(String source, String translation) {
        if (translation.length() == 0) {
            translation = null;
        }
        if (!fuzzy) {
            // add to real translation
            entryProcessingCallback.addEntry(null, source, translation, null);
        } else {
            if (FUZZY_TO_LEGACY) {
                // add to real list without translation
                entryProcessingCallback.addEntry(null, source, null, null);
                // add to legacy TM instead real translation
                entryProcessingCallback.addLegacyTMXEntry(FUZZY_SOURCE_PREFIX
                        + source, translation);
            } else {
                // add to real translation
                entryProcessingCallback.addEntry(null, FUZZY_SOURCE_PREFIX
                        + source, translation, null);
            }
        }
    }
    
    protected void alignHeader(String header) {
        entryProcessingCallback.addEntry(null, unescape(header), null, null);
    }

    protected void flushTranslation(MODE currentMode) throws IOException {
        if (sources[0].length() == 0) {
            if (targets[0].length() == 0) {
                // there is no text to translate yet
                return;
            } else {
                // header
                if (out != null) {
                    out.write("msgstr " + getTranslation(targets[0]) + "\n");
                } else {
                    alignHeader(targets[0].toString());
                }
            }
            fuzzy = false;
        } else {
            // source exist
            if (sources[1].length() == 0) {
                // non-plurals
                if (out != null) {
                    out.write("msgstr " + getTranslation(sources[0]) + "\n");
                } else {
                    align(0);
                }
            } else {
                // plurals
                if (out != null) {
                    out.write("msgstr[0] " + getTranslation(sources[0]) + "\n");
                    out.write("msgstr[1] " + getTranslation(sources[1]) + "\n");
                } else {
                    align(0);
                    align(1);
                }
            }
            fuzzy = false;
        }
        sources[0].setLength(0);
        sources[1].setLength(0);
        targets[0].setLength(0);
        targets[1].setLength(0);
    }

    protected static final Pattern R1 = Pattern
            .compile("(?<!\\\\)((\\\\\\\\)*)\\\\\"");
    protected static final Pattern R2 = Pattern
            .compile("(?<!\\\\)((\\\\\\\\)*)\\\\n");
    protected static final Pattern R3 = Pattern
            .compile("(?<!\\\\)((\\\\\\\\)*)\\\\t");
    protected static final Pattern R4 = Pattern.compile("^\\\\n");

    /**
     * Private processEntry to do pre- and postprocessing.<br>
     * The given entry is interpreted to a string (e.g. escaped quotes are
     * unescaped, '\n' is translated into newline character, '\t' into tab
     * character.) then translated and then returned as a PO-string-notation
     * (e.g. double quotes escaped, newline characters represented as '\n' and
     * surrounded by double quotes, possibly split up over multiple lines)<Br>
     * Long translations are not split up over multiple lines as some PO editors
     * do, but when there are newline characters in a translation, it is split
     * up at the newline markers.<Br>
     * If the nowrap parameter is true, a translation that exists of multiple
     * lines starts with an empty string-line to left-align all lines. [With
     * nowrap set to true, long lines are also never wrapped (except for at
     * newline characters), but that was already not done without nowrap.] [
     * 1869069 ] Escape support for PO
     * 
     * @param entry
     *            The entire source text, without it's surrounding double
     *            quotes, but otherwise not-interpreted
     * @param nowrap
     *            gives indication if the translation should not be wrapped over
     *            multiple lines and all lines be left-aligned.
     * @return The translated entry, within double quotes on each line (thus
     *         ready to be printed to target file immediately)
     **/
    private String getTranslation(StringBuilder en) {
        String entry = unescape(en.toString());

        // Do real translation
        String translation = entryProcessingCallback
                .getTranslation(null, entry);

        if (translation != null) {
            return "\"" + escape(translation) + "\"";
        } else {
            return "\"\"";
        }
    }

    /**
     * Unescape text from .po format.
     */
    private String unescape(String entry) {
        // Removes escapes from quotes. ( \" becomes " unless the \
        // was escaped itself.) The number of preceding slashes before \"
        // should not be odd, else the \ is escaped and not part of \".
        // The regex is: no backslash before an optional even number
        // of backslashes before \". Replace only the \" with " and keep the
        // other escaped backslashes )
        entry = R1.matcher(entry).replaceAll("$1\"");
        // Interprets newline sequence, except when preceded by \
        // \n becomes Linefeed, unless the \ was escaped itself.
        // The number of preceding slashes before \n should not be odd,
        // else the \ is escaped and not part of \n.
        // The regex is: no backslash before an optional even number of
        // backslashes before \n. Replace only the \n with <newline> and
        // keep
        // the other escaped backslashes.
        entry = R2.matcher(entry).replaceAll("$1\n");
        // same for \t, the tab character
        entry = R3.matcher(entry).replaceAll("$1\t");
        // Interprets newline sequence at the beginning of a line
        entry = R4.matcher(entry).replaceAll("\\\n");
        // Removes escape from backslash
        entry = entry.replace("\\\\", "\\");

        return entry;
    }

    /**
     * Escape text to .po format.
     */
    private String escape(String translation) {
        // Escapes backslash
        translation = translation.replace("\\", "\\\\");
        // Adds escapes to quotes. ( " becomes \" )
        translation = translation.replace("\"", "\\\"");
        // AB: restore \r
        translation = translation.replace("\\\\r", "\\r");

        /*
         * Normally, long lines are wrapped at 'output page width', which
         * defaults to ?76?, and always at newlines. IF the no-wrap indicator is
         * present, long lines should not be wrapped, except on newline
         * characters, in which case the first line should be empty, so that the
         * different lines are aligned the same. OmegaT < 2.0 has never wrapped
         * any line, and it is quite useless when the po-file is not edited with
         * a plain-text-editor. But it is simple to wrap at least at newline
         * characters (which is necessary for the translation of the po-header
         * anyway) We can also honor the no-wrap instruction at least by letting
         * the first line of a multi-line translation not be on the same line as
         * 'msgstr'.
         */
        // Interprets newline chars. 'blah<br>blah' becomes
        // 'blah\n"<br>"blah'
        translation = translation.replace("\n", "\\n\"\n\"");
        // don't make empty new line at the end (in case the last 'blah' is
        // empty string)
        if (translation.endsWith("\"\n\"")) {
            translation = translation.substring(0, translation.length() - 3);
        }
        if (nowrap && translation.contains("\n")) {
            // start with empty string, to align all lines of translation
            translation = "\"\n\"" + translation;
        }
        // Interprets tab chars. 'blah<tab>blah' becomes 'blah\tblah'
        // (<tab> representing the tab character '\u0009')
        translation = translation.replace("\t", "\\t");

        return translation;
    }
}
