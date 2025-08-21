/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Thomas Huriaux
               2008 Martin Fleurke
               2009 Alex Buloichik
               2011 Didier Briel
               2013-2014 Alex Buloichik, Enrique Estevez
               2017 Didier Briel
               2023-2024 Hiroshi Miura
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

package org.omegat.filters2.po;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.Core;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SegmentProperties;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.StringUtil;
import org.omegat.util.TagUtil;

/**
 * Filter to support po files (in various encodings).
 * <p>
 * Format described on
 * <a href="https://www.gnu.org/software/hello/manual/gettext/PO-Files.html">PO
 * File format</a>
 * <p>
 * Filter is not thread-safe !
 * Filter uses msgctx field as path, and plural index as suffix of path.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Thomas Huriaux
 * @author Martin Fleurke
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Enrique Estevez
 */
public class PoFilter extends AbstractFilter {

    public static final String OPTION_ALLOW_BLANK = "disallowBlank";
    public static final String OPTION_ALLOW_EDITING_BLANK_SEGMENT = "allowEditingBlankSegment";
    public static final String OPTION_SKIP_HEADER = "skipHeader";
    public static final String OPTION_AUTO_FILL_IN_PLURAL_STATEMENT = "autoFillInPluralStatement";
    public static final String OPTION_FORMAT_MONOLINGUAL = "monolingualFormat";

    private static final String BR = System.lineSeparator();

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(PoFilter.class);
    }

    public static void unloadPlugins() {
    }

    private static class PluralInfo {
        public int plurals;
        public String expression;

        PluralInfo(int nrOfPlurals, String pluralExpression) {
            plurals = nrOfPlurals;
            expression = pluralExpression;
        }
    }

    // CHECKSTYLE.OFF: LineLength
    private static final Map<String, PluralInfo> PLURAL_INFOS;
    static {
        HashMap<String, PluralInfo> info = new HashMap<>();
        // list taken from http://translate.sourceforge.net/wiki/l10n/pluralforms d.d. 14-09-2012
        // See also http://unicode.org/repos/cldr-tmp/trunk/diff/supplemental/language_plural_rules.html
        info.put("ach", new PluralInfo(2, "(n > 1)"));
        info.put("af", new PluralInfo(2, "(n != 1)"));
        info.put("ak", new PluralInfo(2, "(n > 1)"));
        info.put("am", new PluralInfo(2, "(n > 1)"));
        info.put("an", new PluralInfo(2, "(n != 1)"));
        info.put("ar", new PluralInfo(6,
                " n==0 ? 0 : n==1 ? 1 : n==2 ? 2 : n%100>=3 && n%100<=10 ? 3 : n%100>=11 ? 4 : 5"));
        info.put("arn", new PluralInfo(2, "(n > 1)"));
        info.put("ast", new PluralInfo(2, "(n != 1)"));
        info.put("ay", new PluralInfo(1, "0"));
        info.put("az", new PluralInfo(2, "(n != 1) "));
        info.put("be", new PluralInfo(3,
                "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
        info.put("bg", new PluralInfo(2, "(n != 1)"));
        info.put("bn", new PluralInfo(2, "(n != 1)"));
        info.put("bo", new PluralInfo(1, "0"));
        info.put("br", new PluralInfo(2, "(n > 1)"));
        info.put("brx", new PluralInfo(2, "(n != 1)"));
        info.put("bs", new PluralInfo(3,
                "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2) "));
        info.put("ca", new PluralInfo(2, "(n != 1)"));
        info.put("cgg", new PluralInfo(1, "0"));
        info.put("cs", new PluralInfo(3, "(n==1) ? 0 : (n>=2 && n<=4) ? 1 : 2"));
        info.put("csb",
                new PluralInfo(3, "n==1 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2"));
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
        info.put("gd",
                new PluralInfo(4, "(n==1 || n==11) ? 0 : (n==2 || n==12) ? 1 : (n > 2 && n < 20) ? 2 : 3"));
        info.put("gl", new PluralInfo(2, "(n != 1)"));
        info.put("gu", new PluralInfo(2, "(n != 1)"));
        info.put("gun", new PluralInfo(2, "(n > 1)"));
        info.put("ha", new PluralInfo(2, "(n != 1)"));
        info.put("he", new PluralInfo(2, "(n != 1)"));
        info.put("hi", new PluralInfo(2, "(n != 1)"));
        info.put("hne", new PluralInfo(2, "(n != 1)"));
        info.put("hy", new PluralInfo(2, "(n != 1)"));
        info.put("hr", new PluralInfo(3,
                "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
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
        info.put("ln", new PluralInfo(2, "n>1"));
        info.put("lo", new PluralInfo(1, "0"));
        info.put("lt",
                new PluralInfo(3, "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && (n%100<10 or n%100>=20) ? 1 : 2)"));
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
        info.put("mt", new PluralInfo(4,
                "(n==1 ? 0 : n==0 || ( n%100>1 && n%100<11) ? 1 : (n%100>10 && n%100<20 ) ? 2 : 3)"));
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
        info.put("pl",
                new PluralInfo(3, "(n==1 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
        info.put("pms", new PluralInfo(2, "(n != 1)"));
        info.put("pt", new PluralInfo(2, "(n != 1)"));
        info.put("rm", new PluralInfo(2, "(n!=1)"));
        info.put("ro", new PluralInfo(3, "(n==1 ? 0 : (n==0 || (n%100 > 0 && n%100 < 20)) ? 1 : 2)"));
        info.put("ru", new PluralInfo(3,
                "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
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
        info.put("sr", new PluralInfo(3,
                "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
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
        info.put("ug", new PluralInfo(1, "0"));
        info.put("uk", new PluralInfo(3,
                "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
        info.put("ur", new PluralInfo(2, "(n != 1)"));
        info.put("uz", new PluralInfo(2, "(n > 1)"));
        info.put("vi", new PluralInfo(1, "0"));
        info.put("wa", new PluralInfo(2, "(n > 1)"));
        info.put("wo", new PluralInfo(1, "0"));
        info.put("yo", new PluralInfo(2, "(n != 1)"));
        info.put("zh", new PluralInfo(1, "0 "));
        PLURAL_INFOS = Collections.unmodifiableMap(info);
    }
    // CHECKSTYLE.ON: LineLength

    /**
     * If true, non-translated segments will contain the source text in ms
     */
    private boolean allowBlank = false;
    /**
     * If false, the blank source segments will be skipped (not shown in editor)
     */
    private boolean allowEditingBlankSegment = false;
    /**
     * If true, the header will be skipped (not shown in editor)
     */
    private boolean skipHeader = false;
    /**
     * If true, wrong but widely used format support, where msgid contains ID,
     * msgstr contains original text.
     */
    private boolean formatMonolingual = false;

    /**
     * If true, the "Plural-Forms: nplurals=INTEGER; plural=EXPRESSION;" section
     * in the header will be updated with the correct INTEGER and EXPRESSION
     * based on the chosen targetLanguage
     */
    private boolean autoFillInPluralStatement = false;

    protected static final Pattern COMMENT_FUZZY = Pattern.compile("#, fuzzy");
    protected static final Pattern COMMENT_FUZZY_OTHER = Pattern.compile("#,.* fuzzy.*");
    protected static final Pattern COMMENT_FUZZY_MSGID = Pattern.compile("#\\|.* msgid.*\"(.*)\"");
    protected static final Pattern COMMENT_FUZZY_MSGCTX = Pattern.compile("#\\|.* msgctxt\\s+\"(.*)\"");
    protected static final Pattern COMMENT_NOWRAP = Pattern.compile("#,.* no-wrap.*");
    protected static final Pattern COMMENT_TRANSLATOR = Pattern.compile("# (.*)");
    protected static final Pattern COMMENT_EXTRACTED = Pattern.compile("#\\. (.*)");
    protected static final Pattern COMMENT_REFERENCE = Pattern.compile("#: (.*)");
    protected static final Pattern MSG_ID = Pattern.compile("msgid(_plural)?\\s+\"(.*)\"");
    protected static final Pattern MSG_STR = Pattern.compile("msgstr(\\[([0-9]+)])?\\s+\"(.*)\"");
    protected static final Pattern MSG_CTX = Pattern.compile("msgctxt\\s+\"(.*)\"");
    protected static final Pattern MSG_OTHER = Pattern.compile("\"(.*)\"");
    protected static final Pattern PLURAL_FORMS = Pattern
            .compile("Plural-Forms: *nplurals= *([0-9]+) *; *plural", Pattern.CASE_INSENSITIVE);
    protected static final Pattern MSG_FUZZY = Pattern.compile("#\\|\\s\"(.*)\"");

    public enum MODE {
        MSGID, MSGSTR, MSGID_PLURAL, MSGSTR_PLURAL, MSGCTX
    }

    private StringBuilder[] sources;
    private StringBuilder[] targets;
    private StringBuilder translatorComments;
    private StringBuilder extractedComments;
    private StringBuilder references;
    private StringBuilder sourceFuzzyTrue;
    private int plurals = 2;
    private String path;
    private boolean nowrap, fuzzy, fuzzyTrue;
    private boolean headerProcessed;

    private BufferedWriter out;

    private MODE currentMode;
    private int currentPlural;

    @Override
    public String getFileFormatName() {
        return OStrings.getString("POFILTER_FILTER_NAME");
    }

    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] {
                new Instance("*.po", StandardCharsets.UTF_8.name(), StandardCharsets.UTF_8.name()),
                new Instance("*.pot", StandardCharsets.UTF_8.name(), StandardCharsets.UTF_8.name()) };
    }

    @Override
    public boolean isSourceEncodingVariable() {
        return true;
    }

    @Override
    public boolean isTargetEncodingVariable() {
        return true;
    }

    @Override
    public String getFuzzyMark() {
        return "PO-fuzzy";
    }

    @Override
    public void processFile(File inFile, File outFile, FilterContext fc)
            throws IOException, TranslationException {

        String allowBlankStr = processOptions.get(OPTION_ALLOW_BLANK);
        allowBlank = allowBlankStr == null || allowBlankStr.equalsIgnoreCase("true");

        String allowEditingBlankSegmentStr = processOptions.get(OPTION_ALLOW_EDITING_BLANK_SEGMENT);
        allowEditingBlankSegment = allowEditingBlankSegmentStr == null
                || allowEditingBlankSegmentStr.equalsIgnoreCase("true");

        String skipHeaderStr = processOptions.get(OPTION_SKIP_HEADER);
        skipHeader = "true".equalsIgnoreCase(skipHeaderStr);

        String autoFillInPluralStatementStr = processOptions.get(OPTION_AUTO_FILL_IN_PLURAL_STATEMENT);
        autoFillInPluralStatement = "true".equalsIgnoreCase(autoFillInPluralStatementStr);

        String formatMonolingualStr = processOptions.get(OPTION_FORMAT_MONOLINGUAL);
        formatMonolingual = "true".equalsIgnoreCase(formatMonolingualStr);

        inEncodingLastParsedFile = fc.getInEncoding();
        try (BufferedReader reader = createReader(inFile, inEncodingLastParsedFile);
                BufferedWriter writer = createWriter(outFile, fc.getOutEncoding())) {
            processFile(reader, writer, fc);
        }
    }

    @Override
    protected void alignFile(BufferedReader sourceFile, BufferedReader translatedFile, FilterContext fc)
            throws Exception {
        out = null;
        processPoFile(translatedFile, fc);
    }

    @Override
    public void processFile(BufferedReader in, BufferedWriter writer, FilterContext fc) throws IOException {
        out = writer;
        processPoFile(in, fc);
    }

    private void processPoFile(BufferedReader in, FilterContext fc) throws IOException {
        initializeProcessingState();

        String s;
        while ((s = in.readLine()) != null) {

            // We trim trailing spaces, otherwise the regexps could fail,
            // thus making some segments invisible to OmegaT
            s = s.trim();

            if (processFuzzy(s)) {
                continue;
            }
            if (processFuzzyMarkers(s, fc)) {
                continue;
            }

            if (COMMENT_FUZZY_OTHER.matcher(s).matches()) {
                currentPlural = 0;
                fuzzy = true;
                flushTranslation(currentMode, fc);
                s = s.replaceAll("(.*), fuzzy(.*)", "$1$2");
            }

            if (processNoWrapComment(s, fc)) {
                continue;
            }
            if (processMessageId(s, fc)) {
                continue;
            }
            if (processMessageString(s)) {
                continue;
            }
            if (processMessageContext(s)) {
                continue;
            }
            if (processComments(s)) {
                continue;
            }
            if (processFuzzyMessage(s)) {
                continue;
            }
            if (processOtherMessage(s)) {
                continue;
            }

            flushTranslation(currentMode, fc);
            eol(s);
        }
        flushTranslation(currentMode, fc);
    }

    private void initializeProcessingState() {
        fuzzy = false;
        fuzzyTrue = false;
        nowrap = false;
        currentMode = null;
        currentPlural = 0;
        headerProcessed = false;

        sources = new StringBuilder[2];
        sources[0] = new StringBuilder();
        sources[1] = new StringBuilder();
        // can be overridden when header has been read and the number of
        // plurals is different.
        targets = new StringBuilder[2];
        targets[0] = new StringBuilder();
        targets[1] = new StringBuilder();

        translatorComments = new StringBuilder();
        extractedComments = new StringBuilder();
        references = new StringBuilder();
        sourceFuzzyTrue = new StringBuilder();
        path = "";
    }

    private boolean processFuzzy(String line) {
        // We have a real fuzzy
        Matcher mTrueFuzzy = COMMENT_FUZZY_MSGID.matcher(line);
        if (mTrueFuzzy.matches()) {
            fuzzyTrue = true;
            sourceFuzzyTrue.append(mTrueFuzzy.group(1));
            return true;
        }
        // ignore a fuzzy context
        Matcher mFuzzyCtx = COMMENT_FUZZY_MSGCTX.matcher(line);
        return mFuzzyCtx.matches();
    }

    private boolean processFuzzyMarkers(String line, FilterContext fc) throws IOException {
        /*
         * Removing the fuzzy markers, as it has no meanings after being
         * processed by omegat
         */
        if (COMMENT_FUZZY.matcher(line).matches()) {
            currentPlural = 0;
            fuzzy = true;
            flushTranslation(currentMode, fc);
            return true;
        }
        return false;
    }

    private boolean processNoWrapComment(String line, FilterContext fc) throws IOException {
        if (COMMENT_NOWRAP.matcher(line).matches()) {
            currentPlural = 0;
            flushTranslation(currentMode, fc);
            /*
             * Read the no-wrap comment, indicating that the creator of the
             * po-file did not want long messages to be wrapped on multiple
             * lines. See 5.6.2 no-wrap of
             * http://docs.oasis-open.org/xliff/v1.2/xliff-profile-po/xliff-profile-po-1.2-cd02.html
             * for an example.
             */
            nowrap = true;
            eol(line);
            return true;
        }
        return false;
    }

    private boolean processMessageId(String line, FilterContext fc) throws IOException {
        Matcher mId = MSG_ID.matcher(line);
        if (mId.matches()) { // msg_id(_plural)
            currentPlural = 0;
            String text = mId.group(2);
            if (mId.group(1) == null) {
                // non-plural ID ('msg_id')
                // we can start a new translation. Flush current
                // translation. This has not happened when no empty
                // lines are in between 'segments'.
                if (sources[0].length() > 0) {
                    flushTranslation(currentMode, fc);
                }
                currentMode = MODE.MSGID;
                sources[0].append(text);
            } else {
                // plural ID ('msg_id_plural')
                currentMode = MODE.MSGID_PLURAL;
                sources[1].append(text);
            }
            eol(line);
            return true;
        }
        return false;
    }

    private boolean processMessageString(String line) {
        Matcher mStr = MSG_STR.matcher(line);
        if (mStr.matches()) {
            // Hack to be able to translate empty segments
            // If the source segment is empty and there is a reference then
            // it copies the reference of the segment and the localization
            // note into the source segment
            if (allowEditingBlankSegment && sources[0].length() == 0 && references.length() > 0
                    && headerProcessed) {
                String aux = references + extractedComments.toString();
                sources[0].append(aux);
            }

            String text = mStr.group(3);
            if (mStr.group(1) == null) {
                // non-plural lines
                currentMode = MODE.MSGSTR;
                targets[0].append(text);
                currentPlural = 0;
            } else {
                currentMode = MODE.MSGSTR_PLURAL;
                // plurals, i.e. msgstr[N] lines
                currentPlural = Integer.parseInt(mStr.group(2));
                if (currentPlural < plurals) {
                    targets[currentPlural].append(text);
                }
            }
            return true;
        }
        return false;
    }

    private boolean processMessageContext(String line) throws IOException {
        Matcher mCtx = MSG_CTX.matcher(line);
        if (mCtx.matches()) {
            currentMode = MODE.MSGCTX;
            currentPlural = 0;
            path = mCtx.group(1);
            eol(line);
            return true;
        }
        return false;
    }

    private boolean processComments(String line) throws IOException {
        Matcher mReference = COMMENT_REFERENCE.matcher(line);
        if (mReference.matches()) {
            currentPlural = 0;
            references.append(mReference.group(1));
            references.append("\n");
            eol(line);
            return true;
        }

        Matcher mExtracted = COMMENT_EXTRACTED.matcher(line);
        if (mExtracted.matches()) {
            currentPlural = 0;
            extractedComments.append(mExtracted.group(1));
            extractedComments.append("\n");
            eol(line);
            return true;
        }

        Matcher mTranslator = COMMENT_TRANSLATOR.matcher(line);
        if (mTranslator.matches()) {
            currentPlural = 0;
            translatorComments.append(mTranslator.group(1));
            translatorComments.append("\n");
            eol(line);
            return true;
        }

        return false;
    }

    private boolean processFuzzyMessage(String line) {
        // True fuzzy
        Matcher mMsgFuzzy = MSG_FUZZY.matcher(line);
        if (mMsgFuzzy.matches()) {
            sourceFuzzyTrue.append(mMsgFuzzy.group(1));
            return true;
        }
        return false;
    }

    private boolean processOtherMessage(String s) throws IOException {
        Matcher mOther = MSG_OTHER.matcher(s);
        if (mOther.matches()) {
            String text = mOther.group(1);
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
                path += text;
                eol(s);
                break;
            default:
                throw new IllegalArgumentException();
            }
            return true;
        }
        return false;
    }

    protected void eol(String s) throws IOException {
        if (out != null) {
            out.write(s);
            out.write(BR);
        }
    }

    protected void parseOrAlign(int pair) {
        String pathSuffix;
        String source;
        StringBuilder sb = new StringBuilder();
        if (pair > 0) {
            source = unescape(sources[1].toString());
            pathSuffix = "[" + pair + "]";
            sb.append(StringUtil.format(OStrings.getString("POFILTER_PLURAL_FORM_COMMENT"), pair)).append("\n");
        } else {
            source = unescape(sources[0].toString());
            pathSuffix = "";
            String s1 = unescape(sources[1].toString());
            if (!StringUtil.isEmpty(s1)) {
                sb.append(OStrings.getString("POFILTER_SINGULAR_COMMENT")).append("\n").append(s1).append("\n\n");
            }
        }
        String translate = unescape(targets[pair].toString());

        if (translatorComments.length() > 0) {
            sb.append(OStrings.getString("POFILTER_TRANSLATOR_COMMENTS")).append("\n").append(unescape(
                    translatorComments.toString())).append("\n");
        }
        if (extractedComments.length() > 0) {
            sb.append(OStrings.getString("POFILTER_EXTRACTED_COMMENTS")).append("\n").append(unescape(
                    extractedComments.toString())).append("\n");
        }
        if (references.length() > 0) {
            sb.append(OStrings.getString("POFILTER_REFERENCES")).append("\n").append(unescape(references
                            .toString()));
        }
        String comments = sb.toString();
        if (comments.isEmpty()) {
            comments = null;
        }
        parseOrAlign(source, translate, comments, pathSuffix);
    }

    /**
     *
     * @param source unescaped source string.
     * @param translation unescaped translated string in po file.
     * @param comments unescaped comment in po file.
     * @param pathSuffix
     *            suffix for path to distinguish plural forms. It will be empty
     *            for first one, and [1],[2],... for next
     */
    protected void parseOrAlign(String source, String translation, String comments, String pathSuffix) {
        if (translation.isEmpty()) {
            translation = null;
        }
        String omtPath = path + pathSuffix;
        if (entryParseCallback != null) {
            if (formatMonolingual) {
                List<ProtectedPart> protectedParts = TagUtil.applyCustomProtectedParts(translation,
                        PatternConsts.PRINTF_VARS, null);
                entryParseCallback.addEntry(source, translation, null, fuzzy, comments, omtPath,
                        this, protectedParts);
            } else {
                List<ProtectedPart> protectedParts = TagUtil.applyCustomProtectedParts(source,
                        PatternConsts.PRINTF_VARS, null);
                if (fuzzyTrue) { // We add a reference entry
                    String[] props = { SegmentProperties.COMMENT, comments, SegmentProperties.REFERENCE,
                            "true" };
                    entryParseCallback.addEntryWithProperties(null, sourceFuzzyTrue.toString(), translation,
                            false, props, omtPath, this, null);
                    fuzzyTrue = false;
                    // Do not load false fuzzy when there is a real one
                    fuzzy = false;
                    translation = null;
                }
                entryParseCallback.addEntry(null, source, translation, fuzzy, comments, omtPath,
                        this, protectedParts);
            }
        } else if (entryAlignCallback != null) {
            entryAlignCallback.addTranslation(null, source, translation, fuzzy, omtPath, this);
        }
    }

    /**
     * Parse PO file header.
     * @param header header block in the file.
     * @param fc filter context to process.
     */
    protected void parseHeader(String header, FilterContext fc) {
        if (entryParseCallback != null && !skipHeader) {
            header = unescape(autoFillInPluralStatement(header, fc));
            List<ProtectedPart> protectedParts = TagUtil.applyCustomProtectedParts(header,
                    PatternConsts.PRINTF_VARS, null);
            entryParseCallback.addEntry(null, header, null, false, null, path, this, protectedParts);
        }
    }

    protected void flushTranslation(MODE currentMode, FilterContext fc) throws IOException {
        if (sources[0].length() == 0 && path.isEmpty()) {
            headerProcessed = true;
            if (targets[0].length() == 0) {
                // there is no text to translate yet
                return;
            } else {
                // header

                // Check an existing plural statement. If it contains the
                // number of plurals, then use it!
                StringBuilder targets0 = targets[0];
                String header = targets[0].toString();
                Matcher pluralMatcher = PLURAL_FORMS.matcher(header);
                if (pluralMatcher.find()) {
                    String nrOfPluralsString = header.substring(pluralMatcher.start(1), pluralMatcher.end(1));
                    plurals = Integer.parseInt(nrOfPluralsString);
                } else {
                    // else use predefined number of plurals, if it exists
                    Language targetLang = fc.getTargetLang();
                    String lang = targetLang.getLanguageCode().toLowerCase(Locale.ENGLISH);
                    PluralInfo pluralInfo = PLURAL_INFOS.get(lang);
                    if (pluralInfo != null) {
                        plurals = pluralInfo.plurals;
                    }
                }
                // update the number of targets according to new plural number
                targets = new StringBuilder[plurals];
                targets[0] = targets0;
                for (int i = 1; i < plurals; i++) {
                    targets[i] = new StringBuilder();
                }

                if (out != null) {
                    // Header is always written
                    out.write("msgstr " + getTranslation(null, targets[0], false, true, fc, 0) + BR);
                } else {
                    parseHeader(targets[0].toString(), fc);
                }
            }
            fuzzy = false;
        } else {
            // source exist
            if (sources[1].length() == 0) {
                // non-plurals
                if (out != null) {
                    if (formatMonolingual) {
                        out.write("msgstr "
                                + getTranslation(sources[0].toString(), targets[0], allowBlank, false, fc, 0)
                                + BR);
                    } else {
                        out.write("msgstr " + getTranslation(null, sources[0], allowBlank, false, fc, 0)
                                + BR);
                    }
                } else {
                    parseOrAlign(0);
                }
            } else {
                // plurals
                if (out != null) {
                    out.write("msgstr[0] " + getTranslation(null, sources[0], allowBlank, false, fc, 0) + BR);
                    for (int i = 1; i < plurals; i++) {
                        out.write("msgstr[" + i + "] " + getTranslation(null, sources[1], allowBlank, false, fc, i)
                                + BR);
                    }
                } else {
                    parseOrAlign(0);
                    for (int i = 1; i < plurals; i++) {
                        parseOrAlign(i);
                    }
                }
            }
            fuzzy = false;
        }
        sources[0].setLength(0);
        sources[1].setLength(0);
        for (int i = 0; i < plurals; i++) {
            targets[i].setLength(0);
        }
        path = "";
        translatorComments.setLength(0);
        extractedComments.setLength(0);
        references.setLength(0);
        sourceFuzzyTrue.setLength(0);
    }

    protected static final Pattern R1 = Pattern.compile("(?<!\\\\)((\\\\\\\\)*)\\\\\"");
    protected static final Pattern R2 = Pattern.compile("(?<!\\\\)((\\\\\\\\)*)\\\\n");
    protected static final Pattern R3 = Pattern.compile("(?<!\\\\)((\\\\\\\\)*)\\\\t");
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
     * @param en
     *            The entire source text
     * @param allowNull
     *            Allow outputting a blank translation in msgstr
     * @param isHeader
     *            is the given string the PO-header string?
     * @param fc
     *            The FilterContext, for targetLanguage
     * @param plural
     *            if the source text is a plural, which plural number / variant
     *            are we on? 0 = no plural, 1.. are the plurals for the given
     *            target language.
     * @return The translated entry, within double quotes on each line (thus
     *         ready to be printed to target file immediately)
     **/
    private String getTranslation(String id, StringBuilder en, boolean allowNull, boolean isHeader,
            FilterContext fc, int plural) {
        String entry = unescape(en.toString());

        String pathSuffix;
        if (plural > 0) {
            pathSuffix = "[" + plural + "]";
        } else {
            pathSuffix = "";
        }

        // Do real translation
        String translation;
        if (isHeader) {
            entry = autoFillInPluralStatement(entry, fc);
        }
        if (isHeader && skipHeader) {
            translation = entry;
        } else if (entryTranslateCallback != null) {
            translation = entryTranslateCallback.getTranslation(id, entry, path + pathSuffix);
        } else {
            translation = null;
        }

        if (translation == null && !allowNull) {
            // We write the source in translation
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
     * 
     * @param header
     *            The header text that contains the Plural-forms line.
     * @return Header with the correct plural forms line according to target
     *         language.
     */
    private String autoFillInPluralStatement(String header, FilterContext fc) {
        Language targetLang = fc.getTargetLang();
        if (targetLang == null) {
            return header;
        }
        if (autoFillInPluralStatement) {
            String lang = targetLang.getLanguageCode().toLowerCase(Locale.ENGLISH);
            PluralInfo pluralInfo = PLURAL_INFOS.get(lang);
            if (pluralInfo != null) {
                return header.replaceAll("Plural-Forms: nplurals=INTEGER; plural=EXPRESSION;",
                        "Plural-Forms: nplurals=" + pluralInfo.plurals + "; plural=" + pluralInfo.expression
                                + ";");
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
        // Interprets a newline sequence, except when preceded by \
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
        // Interprets a newline sequence at the beginning of a line
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

        // Interprets newline chars.
        if (translation.contains("\n")) {
            final String newLine = "\"" + BR + "\"";
            // 'blah<br>blah' becomes 'blah\n"<br>"blah'
            translation = translation.replace("\n", "\\n" + newLine);
            // don't make empty new line at the end (in case the last 'blah' is
            // empty string)
            if (translation.endsWith(newLine)) {
                translation = translation.substring(0, translation.length() - newLine.length());
            }
            if (nowrap) {
                // start with empty string, to align all lines of translation
                translation = newLine + translation;
            }
        }

        // Interprets tab chars. 'blah<tab>blah' becomes 'blah\tblah'
        // (<tab> representing the tab character '\u0009')
        translation = translation.replace("\t", "\\t");

        return translation;
    }

    @Override
    public Map<String, String> changeOptions(Window parent, Map<String, String> config) {
        try {
            PoOptionsDialog dialog = new PoOptionsDialog(parent, config);
            dialog.setVisible(true);
            if (PoOptionsDialog.RET_OK == dialog.getReturnStatus()) {
                return dialog.getOptions();
            } else {
                return null;
            }
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

    @Override
    public boolean isBilingual() {
        return true;
    }
}
