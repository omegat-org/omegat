/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Thomas Huriaux
               2008 Martin Fleurke
               2009 Alex Buloichik
               2011 Didier Briel
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.filters2.po;

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.Log;

/**
 * Filter to support po files (in various encodings).
 * 
 * Format described on http://www.gnu.org/software/hello/manual/gettext/PO-Files.html
 * 
 * Filter is not thread-safe !
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Thomas Huriaux
 * @author Martin Fleurke
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 */
public class PoFilter extends AbstractFilter {

    public static final String OPTION_ALLOW_BLANK = "disallowBlank";
    public static final String OPTION_SKIP_HEADER = "skipHeader";
    public static final String OPTION_AUTO_FILL_IN_PLURAL_STATEMENT = "autoFillInPluralStatement";

    private static class PluralInfo {
        public int plurals;
        public String  expression;
        public PluralInfo(int nrOfPlurals, String pluralExpression) {
            plurals = nrOfPlurals;
            expression = pluralExpression;
        }
    }

    private static final Map<String, PluralInfo> pluralInfos;
    static {
        HashMap<String, PluralInfo> info = new HashMap<String, PluralInfo>();
        //list taken from http://translate.sourceforge.net/wiki/l10n/pluralforms d.d. 14-09-2012
        info.put("ach", new PluralInfo(2, "(n > 1)"));
        info.put("af", new PluralInfo(2, "(n != 1)"));
        info.put("ak", new PluralInfo(2, "(n > 1)"));
        info.put("am", new PluralInfo(2, "(n > 1)"));
        info.put("an", new PluralInfo(2, "(n != 1)"));
        info.put("ar", new PluralInfo(6, " n==0 ? 0 : n==1 ? 1 : n==2 ? 2 : n%100>=3 && n%100<=10 ? 3 : n%100>=11 ? 4 : 5;"));
        info.put("arn", new PluralInfo(2, "(n > 1)"));
        info.put("ast", new PluralInfo(2, "(n != 1)"));
        info.put("ay", new PluralInfo(1, "0"));
        info.put("az", new PluralInfo(2, "(n != 1) "));
        info.put("be", new PluralInfo(3, "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
        info.put("bg", new PluralInfo(2, "(n != 1)"));
        info.put("bn", new PluralInfo(2, "(n != 1)"));
        info.put("bo", new PluralInfo(1, "0"));
        info.put("br", new PluralInfo(2, "(n > 1)"));
        info.put("brx", new PluralInfo(2, "(n != 1)"));
        info.put("bs", new PluralInfo(3, "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2) "));
        info.put("ca", new PluralInfo(2, "(n != 1)"));
        info.put("cgg", new PluralInfo(1, "0"));
        info.put("cs", new PluralInfo(3, "(n==1) ? 0 : (n>=2 && n<=4) ? 1 : 2"));
        info.put("csb", new PluralInfo(3, "n==1 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2"));
        info.put("cy", new PluralInfo(4, " (n==1) ? 0 : (n==2) ? 1 : (n != 8 && n != 11) ? 2 : 3"));
        info.put("da", new PluralInfo(2, "(n != 1)"));
        info.put("de", new PluralInfo(2, "(n != 1)"));
        info.put("doi", new PluralInfo(2, "(n != 1)"));
        info.put("dz", new PluralInfo(1, "0"));
        info.put("el", new PluralInfo(2, "(n != 1)"));
        info.put("en", new PluralInfo(2, "(n != 1)"));
        info.put("eo", new PluralInfo(2, "(n != 1)"));
        info.put("es", new PluralInfo(2, "(n != 1)"));
        info.put("et", new PluralInfo(2, "(n != 1)"));
        info.put("eu", new PluralInfo(2, "(n != 1)"));
        info.put("fa", new PluralInfo(1, "0"));
        info.put("ff", new PluralInfo(2, "(n != 1)"));
        info.put("fi", new PluralInfo(2, "(n != 1)"));
        info.put("fil", new PluralInfo(2, "n > 1"));
        info.put("fo", new PluralInfo(2, "(n != 1)"));
        info.put("fr", new PluralInfo(2, "(n > 1)"));
        info.put("fur", new PluralInfo(2, "(n != 1)"));
        info.put("fy", new PluralInfo(2, "(n != 1)"));
        info.put("ga", new PluralInfo(5, "n==1 ? 0 : n==2 ? 1 : n<7 ? 2 : n<11 ? 3 : 4"));
        info.put("gd", new PluralInfo(4, "(n==1 || n==11) ? 0 : (n==2 || n==12) ? 1 : (n > 2 && n < 20) ? 2 : 3"));
        info.put("gl", new PluralInfo(2, "(n != 1)"));
        info.put("gu", new PluralInfo(2, "(n != 1)"));
        info.put("gun", new PluralInfo(2, "(n > 1)"));
        info.put("ha", new PluralInfo(2, "(n != 1)"));
        info.put("he", new PluralInfo(2, "(n != 1)"));
        info.put("hi", new PluralInfo(2, "(n != 1)"));
        info.put("hne", new PluralInfo(2, "(n != 1)"));
        info.put("hy", new PluralInfo(2, "(n != 1)"));
        info.put("hr", new PluralInfo(3, "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
        info.put("hu", new PluralInfo(2, "(n != 1)"));
        info.put("ia", new PluralInfo(2, "(n != 1)"));
        info.put("id", new PluralInfo(1, "0"));
        info.put("is", new PluralInfo(2, "(n%10!=1 || n%100==11)"));
        info.put("it", new PluralInfo(2, "(n != 1)"));
        info.put("ja", new PluralInfo(1, "0"));
        info.put("jbo", new PluralInfo(1, "0"));
        info.put("jv", new PluralInfo(2, "n!=0"));
        info.put("ka", new PluralInfo(1, "0"));
        info.put("kk", new PluralInfo(1, "0"));
        info.put("km", new PluralInfo(1, "0"));
        info.put("kn", new PluralInfo(2, "(n!=1)"));
        info.put("ko", new PluralInfo(1, "0"));
        info.put("ku", new PluralInfo(2, "(n!= 1)"));
        info.put("kw", new PluralInfo(4, " (n==1) ? 0 : (n==2) ? 1 : (n == 3) ? 2 : 3"));
        info.put("ky", new PluralInfo(1, "0"));
        info.put("lb", new PluralInfo(2, "(n != 1)"));
        info.put("ln", new PluralInfo(2, "n>1;"));
        info.put("lo", new PluralInfo(1, "0"));
        info.put("lt", new PluralInfo(3, "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && (n%100<10 or n%100>=20) ? 1 : 2)"));
        info.put("lv", new PluralInfo(3, "(n%10==1 && n%100!=11 ? 0 : n != 0 ? 1 : 2)"));
        info.put("mai", new PluralInfo(2, "(n != 1)"));
        info.put("mfe", new PluralInfo(2, "(n > 1)"));
        info.put("mg", new PluralInfo(2, "(n > 1)"));
        info.put("mi", new PluralInfo(2, "(n > 1)"));
        info.put("mk", new PluralInfo(2, " n==1 || n%10==1 ? 0 : 1"));
        info.put("ml", new PluralInfo(2, "(n != 1)"));
        info.put("mn", new PluralInfo(2, "(n != 1)"));
        info.put("mni", new PluralInfo(2, "(n != 1)"));
        info.put("mnk", new PluralInfo(3, "(n==0 ? 0 : n==1 ? 1 : 2"));
        info.put("mr", new PluralInfo(2, "(n != 1)"));
        info.put("ms", new PluralInfo(1, "0"));
        info.put("mt", new PluralInfo(4, "(n==1 ? 0 : n==0 || ( n%100>1 && n%100<11) ? 1 : (n%100>10 && n%100<20 ) ? 2 : 3)"));
        info.put("my", new PluralInfo(1, "0"));
        info.put("nah", new PluralInfo(2, "(n != 1)"));
        info.put("nap", new PluralInfo(2, "(n != 1)"));
        info.put("nb", new PluralInfo(2, "(n != 1)"));
        info.put("ne", new PluralInfo(2, "(n != 1)"));
        info.put("nl", new PluralInfo(2, "(n != 1)"));
        info.put("se", new PluralInfo(2, "(n != 1)"));
        info.put("nn", new PluralInfo(2, "(n != 1)"));
        info.put("no", new PluralInfo(2, "(n != 1)"));
        info.put("nso", new PluralInfo(2, "(n != 1)"));
        info.put("oc", new PluralInfo(2, "(n > 1)"));
        info.put("or", new PluralInfo(2, "(n != 1)"));
        info.put("ps", new PluralInfo(2, "(n != 1)"));
        info.put("pa", new PluralInfo(2, "(n != 1)"));
        info.put("pap", new PluralInfo(2, "(n != 1)"));
        info.put("pl", new PluralInfo(3, "(n==1 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
        info.put("pms", new PluralInfo(2, "(n != 1)"));
        info.put("pt", new PluralInfo(2, "(n != 1)"));
        info.put("rm", new PluralInfo(2, "(n!=1);"));
        info.put("ro", new PluralInfo(3, "(n==1 ? 0 : (n==0 || (n%100 > 0 && n%100 < 20)) ? 1 : 2);"));
        info.put("ru", new PluralInfo(3, "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
        info.put("rw", new PluralInfo(2, "(n != 1)"));
        info.put("sah", new PluralInfo(1, "0"));
        info.put("sat", new PluralInfo(2, "(n != 1)"));
        info.put("sco", new PluralInfo(2, "(n != 1)"));
        info.put("sd", new PluralInfo(2, "(n != 1)"));
        info.put("si", new PluralInfo(2, "(n != 1)"));
        info.put("sk", new PluralInfo(3, "(n==1) ? 0 : (n>=2 && n<=4) ? 1 : 2"));
        info.put("sl", new PluralInfo(4, "(n%100==1 ? 1 : n%100==2 ? 2 : n%100==3 || n%100==4 ? 3 : 0)"));
        info.put("so", new PluralInfo(2, "n != 1"));
        info.put("son", new PluralInfo(2, "(n != 1)"));
        info.put("sq", new PluralInfo(2, "(n != 1)"));
        info.put("sr", new PluralInfo(3, "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
        info.put("su", new PluralInfo(1, "0"));
        info.put("sw", new PluralInfo(2, "(n != 1)"));
        info.put("sv", new PluralInfo(2, "(n != 1)"));
        info.put("ta", new PluralInfo(2, "(n != 1)"));
        info.put("te", new PluralInfo(2, "(n != 1)"));
        info.put("tg", new PluralInfo(2, "(n > 1)"));
        info.put("ti", new PluralInfo(2, "n > 1"));
        info.put("th", new PluralInfo(1, "0"));
        info.put("tk", new PluralInfo(2, "(n != 1)"));
        info.put("tr", new PluralInfo(2, "(n>1)"));
        info.put("tt", new PluralInfo(1, "0"));
        info.put("ug", new PluralInfo(1, "0;"));
        info.put("uk", new PluralInfo(3, "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
        info.put("ur", new PluralInfo(2, "(n != 1)"));
        info.put("uz", new PluralInfo(2, "(n > 1)"));
        info.put("vi", new PluralInfo(1, "0"));
        info.put("wa", new PluralInfo(2, "(n > 1)"));
        info.put("wo", new PluralInfo(1, "0"));
        info.put("yo", new PluralInfo(2, "(n != 1)"));
        info.put("zh", new PluralInfo(1, "0 "));
        pluralInfos = Collections.unmodifiableMap(info);
    }

    /**
     * If true, non-translated segments will contain the source text in ms
     */
    public static boolean allowBlank = false;
    /**
     * If true, the header will be skipped (not shown in editor)
     */
    public static boolean skipHeader = false;
    /**
     * If true, the "Plural-Forms: nplurals=INTEGER; plural=EXPRESSION;" section
     * in the header will be updated with the correct INTEGER and EXPRESSION
     * based on the chosen targetLanguage
     */
    public static boolean autoFillInPluralStatement = false;

    protected static Pattern COMMENT_FUZZY = Pattern.compile("#, fuzzy");
    protected static Pattern COMMENT_FUZZY_OTHER = Pattern.compile("#,.* fuzzy.*");
    protected static Pattern COMMENT_NOWRAP = Pattern.compile("#,.* no-wrap.*");
    protected static Pattern COMMENT_TRANSLATOR = Pattern.compile("# (.*)");
    protected static Pattern COMMENT_EXTRACTED = Pattern.compile("#\\. (.*)");
    protected static Pattern COMMENT_REFERENCE = Pattern.compile("#: (.*)");
    protected static Pattern MSG_ID = Pattern.compile("msgid(_plural)?\\s+\"(.*)\"");
    protected static Pattern MSG_STR = Pattern.compile("msgstr(\\[[0-9]+\\])?\\s+\"(.*)\"");
    protected static Pattern MSG_CTX = Pattern.compile("msgctxt\\s+\"(.*)\"");
    protected static Pattern MSG_OTHER = Pattern.compile("\"(.*)\"");

    enum MODE {
        MSGID, MSGSTR, MSGID_PLURAL, MSGSTR_PLURAL, MSGCTX
    };

    private StringBuilder[] sources, targets;
    private StringBuilder translatorComments, extractedComments, references;
    private String path;
    private boolean nowrap, fuzzy;

    private BufferedWriter out;

    public String getFileFormatName() {
        return OStrings.getString("POFILTER_FILTER_NAME");
    }

    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.po"), new Instance("*.pot") };
    }

    public boolean isSourceEncodingVariable() {
        return true;
    }

    public boolean isTargetEncodingVariable() {
        return true;
    }

    @Override
    public String getFuzzyMark() {
        return "PO-fuzzy";
    }

    @Override
    public void processFile(File inFile, File outFile, FilterContext fc) throws IOException,
            TranslationException {

        String disallowBlankStr = processOptions.get(OPTION_ALLOW_BLANK);
        if ((disallowBlankStr == null) || (disallowBlankStr.equalsIgnoreCase("true"))) {
            allowBlank = true;
        } else {
            allowBlank = false;
        }
        String skipHeaderStr = processOptions.get(OPTION_SKIP_HEADER);
        if ("true".equalsIgnoreCase(skipHeaderStr)) {
            skipHeader = true;
        } else {
            skipHeader = false;
        }
        String autoFillInPluralStatementStr = processOptions.get(OPTION_AUTO_FILL_IN_PLURAL_STATEMENT);
        if ("true".equalsIgnoreCase(autoFillInPluralStatementStr)) {
            autoFillInPluralStatement = true;
        } else {
            autoFillInPluralStatement = false;
        }

        inEncodingLastParsedFile = fc.getInEncoding();
        BufferedReader reader = createReader(inFile, inEncodingLastParsedFile);
        try {
            BufferedWriter writer;

            if (outFile != null) {
                writer = createWriter(outFile, fc.getOutEncoding());
            } else {
                writer = null;
            }

            try {
                processFile(reader, writer, fc);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } finally {
            reader.close();
        }
    }

    @Override
    protected void alignFile(BufferedReader sourceFile, BufferedReader translatedFile, FilterContext fc) throws Exception {
        // BOM (byte order mark) bugfix
        translatedFile.mark(1);
        int ch = translatedFile.read();
        if (ch != 0xFEFF)
            translatedFile.reset();

        this.out = null;
        processPoFile(translatedFile, fc);
    }

    @Override
    public void processFile(BufferedReader in, BufferedWriter out, FilterContext fc) throws IOException {
        // BOM (byte order mark) bugfix
        in.mark(1);
        int ch = in.read();
        if (ch != 0xFEFF)
            in.reset();

        this.out = out;
        processPoFile(in, fc);
    }

    private void processPoFile(BufferedReader in, FilterContext fc) throws IOException {
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
        translatorComments = new StringBuilder();
        extractedComments = new StringBuilder();
        references = new StringBuilder();
        path = null;

        String s;
        while ((s = in.readLine()) != null) {

            // We trim trailing spaces, otherwise the regexps could fail, thus making some segments
            // invisible to OmegaT
            s = s.trim();

            /*
             * Removing the fuzzy markers, as it has no meanings after being processed by omegat
             */
            if (COMMENT_FUZZY.matcher(s).matches()) {
                fuzzy = true;
                flushTranslation(currentMode, fc);
                continue;
            } else if (COMMENT_FUZZY_OTHER.matcher(s).matches()) {
                fuzzy = true;
                flushTranslation(currentMode, fc);
                s = s.replaceAll("(.*), fuzzy(.*)", "$1$2");
            }

            // FSM for po files
            if (COMMENT_NOWRAP.matcher(s).matches()) {
                flushTranslation(currentMode, fc);
                /*
                 * Read the no-wrap comment, indicating that the creator of the po-file did not want long
                 * messages to be wrapped on multiple lines. See 5.6.2 no-wrap of http://docs.oasis-open
                 * .org/xliff/v1.2/xliff-profile-po/xliff -profile-po-1.2-cd02.html for an example.
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

            if ((m = MSG_CTX.matcher(s)).matches()) {
                currentMode = MODE.MSGCTX;
                path = m.group(1);
                eol(s);

                continue;
            }

            if ((m = COMMENT_REFERENCE.matcher(s)).matches()) {
                references.append(m.group(1));
                references.append("\n");
                eol(s);

                continue;
            }
            if ((m = COMMENT_EXTRACTED.matcher(s)).matches()) {
                extractedComments.append(m.group(1));
                extractedComments.append("\n");
                eol(s);

                continue;
            }
            if ((m = COMMENT_TRANSLATOR.matcher(s)).matches()) {
                translatorComments.append(m.group(1));
                translatorComments.append("\n");
                eol(s);

                continue;
            }

            if ((m = MSG_OTHER.matcher(s)).matches()) {
                String text = m.group(1);
                if (currentMode == null) {
                    throw new IOException(OStrings.getString("POFILTER_INVALID_FORMAT"));
                }
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
                case MSGCTX:
                    eol(s);
                    break;
                }
                continue;
            }

            flushTranslation(currentMode, fc);
            eol(s);
        }
        flushTranslation(currentMode, fc);
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
        String c = "";
        if (translatorComments.length() > 0) {
            c += OStrings.getString("POFILTER_TRANSLATOR_COMMENTS") + "\n" + unescape(translatorComments.toString() + "\n"); 
        }
        if (extractedComments.length() > 0) {
            c += OStrings.getString("POFILTER_EXTRACTED_COMMENTS") + "\n" + unescape(extractedComments.toString() + "\n"); 
        }
        if (references.length() > 0) {
            c += OStrings.getString("POFILTER_REFERENCES") + "\n" + unescape(references.toString() + "\n"); 
        }
        if (c.length()==0) {
            c = null;
        }
        align(s, t, c);
    }

    protected void align(String source, String translation, String comments) {
        if (translation.length() == 0) {
            translation = null;
        }
        if (entryParseCallback != null) {
            entryParseCallback.addEntry(null, source, translation, fuzzy, comments, path, this);
        } else if (entryAlignCallback != null) {
            entryAlignCallback.addTranslation(null, source, translation, fuzzy, null, this);
        }
    }

    protected void alignHeader(String header, FilterContext fc) {
        if (entryParseCallback != null && !PoFilter.skipHeader) {
            header = autoFillInPluralStatement(header, fc);
            entryParseCallback.addEntry(null, unescape(header), null, false, null, path, this);
        }
    }

    protected void flushTranslation(MODE currentMode, FilterContext fc) throws IOException {
        if (sources[0].length() == 0) {
            if (targets[0].length() == 0) {
                // there is no text to translate yet
                return;
            } else {
                // header
                if (out != null) {
                    // Header is always written
                    out.write("msgstr " + getTranslation(targets[0], false, true, fc) + "\n");
                } else {
                    alignHeader(targets[0].toString(), fc);
                }
            }
            fuzzy = false;
        } else {
            // source exist
            if (sources[1].length() == 0) {
                // non-plurals
                if (out != null) {
                    out.write("msgstr " + getTranslation(sources[0], allowBlank, false, fc) + "\n");
                } else {
                    align(0);
                }
            } else {
                // plurals
                if (out != null) {
                    out.write("msgstr[0] " + getTranslation(sources[0], allowBlank, false, fc) + "\n");
                    out.write("msgstr[1] " + getTranslation(sources[1], allowBlank, false, fc) + "\n");
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
        path = null;
        translatorComments.setLength(0);
        extractedComments.setLength(0);
        references.setLength(0);
    }

    protected static final Pattern R1 = Pattern.compile("(?<!\\\\)((\\\\\\\\)*)\\\\\"");
    protected static final Pattern R2 = Pattern.compile("(?<!\\\\)((\\\\\\\\)*)\\\\n");
    protected static final Pattern R3 = Pattern.compile("(?<!\\\\)((\\\\\\\\)*)\\\\t");
    protected static final Pattern R4 = Pattern.compile("^\\\\n");

    /**
     * Private processEntry to do pre- and postprocessing.<br>
     * The given entry is interpreted to a string (e.g. escaped quotes are unescaped, '\n' is translated into
     * newline character, '\t' into tab character.) then translated and then returned as a PO-string-notation
     * (e.g. double quotes escaped, newline characters represented as '\n' and surrounded by double quotes,
     * possibly split up over multiple lines)<Br>
     * Long translations are not split up over multiple lines as some PO editors do, but when there are
     * newline characters in a translation, it is split up at the newline markers.<Br>
     * If the nowrap parameter is true, a translation that exists of multiple lines starts with an empty
     * string-line to left-align all lines. [With nowrap set to true, long lines are also never wrapped
     * (except for at newline characters), but that was already not done without nowrap.] [ 1869069 ] Escape
     * support for PO
     * 
     * @param en
     *            The entire source text
     * @param allowNull
     *            Allow to output a blank translation in msgstr
     * @return The translated entry, within double quotes on each line (thus ready to be printed to target
     *         file immediately)
     **/
    private String getTranslation(StringBuilder en, boolean allowNull, boolean isHeader, FilterContext fc) {
        String entry = unescape(en.toString());

        // Do real translation
        String translation = null;
        if (isHeader) {
            entry = autoFillInPluralStatement(entry, fc);
        }
        if (isHeader && PoFilter.skipHeader) {
            translation = entry;
        } else {
            translation = entryTranslateCallback.getTranslation(null, entry, path);
        }

        if (translation == null && !allowNull) { // We write the source in translation
            translation = entry;
        }

        if (translation != null) {
            return "\"" + escape(translation) + "\"";
        } else {
            return "\"\"";
        }
    }

    /**
     * Replaces Plural-Forms: nplurals=INTEGER; plural=EXPRESSION; when selected
     * @param header The header text that contains the Plural-forms line.
     * @return Header with the correct plural forms line according to target language.
     */
    private String autoFillInPluralStatement(String header, FilterContext fc) {
        if (PoFilter.autoFillInPluralStatement) {
            Language targetLang = fc.getTargetLang();
            String lang = targetLang.getLanguageCode().toLowerCase();
            PluralInfo pluralInfo = pluralInfos.get(lang);
            if (pluralInfo != null) {
                return header.replaceAll("Plural-Forms: nplurals=INTEGER; plural=EXPRESSION;", "Plural-Forms: nplurals="+pluralInfo.plurals+"; plural="+pluralInfo.expression+";");
            }
        }
        return header;
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

        /*
         * Normally, long lines are wrapped at 'output page width', which defaults to ?76?, and always at
         * newlines. IF the no-wrap indicator is present, long lines should not be wrapped, except on newline
         * characters, in which case the first line should be empty, so that the different lines are aligned
         * the same. OmegaT < 2.0 has never wrapped any line, and it is quite useless when the po-file is not
         * edited with a plain-text-editor. But it is simple to wrap at least at newline characters (which is
         * necessary for the translation of the po-header anyway) We can also honor the no-wrap instruction at
         * least by letting the first line of a multi-line translation not be on the same line as 'msgstr'.
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

    @Override
    public Map<String, String> changeOptions(Dialog parent, Map<String, String> config) {
        try {
            PoOptionsDialog dialog = new PoOptionsDialog(parent, config);
            dialog.setVisible(true);
            if (PoOptionsDialog.RET_OK == dialog.getReturnStatus())
                return dialog.getOptions();
            else
                return null;
        } catch (Exception e) {
            Log.log(OStrings.getString("POFILTER_EXCEPTION"));
            Log.log(e);
            return null;
        }
    }

    /**
     * Returns true to indicate that Text filter has options.
     * 
     * @return True, because the PO filter has options.
     */
    @Override
    public boolean hasOptions() {
        return true;
    }
}
