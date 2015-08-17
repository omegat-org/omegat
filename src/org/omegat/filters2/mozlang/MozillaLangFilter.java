/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Enrique Estévez Fernández 
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

package org.omegat.filters2.mozlang;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.data.ProtectedPart;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.OStrings;
import org.omegat.util.OConsts;
import org.omegat.util.PatternConsts;
import org.omegat.util.TagUtil;

/**
 * Filter for support Mozilla lang files.
 * 
 * Filter for lang files. Something about the format described on
 * https://sourceforge.net/p/omegat/feature-requests/962/
 * https://developer.mozilla.org/en-US/docs/Web_Localizability/Localization_formats
 * http://bedrock.readthedocs.org/en/latest/l10n.html
 * 
 * Code adapted from the files: PoFilter.java and SrtFilter.java
 *
 * @author Enrique Estévez (keko.gl@gmail.com)
 */
public class MozillaLangFilter extends AbstractFilter {

    protected static Pattern LOCALIZATION_NOTE = Pattern.compile("# (.*)");
    protected static Pattern PATTERN_SOURCE= Pattern.compile("^;(.*)");
    
    enum READ_STATE {
        WAIT_SOURCE, WAIT_TARGET
    };
    
    private StringBuilder source, target, localizationNote;

    private BufferedWriter out;

    @Override
    public String getFileFormatName() {
        return OStrings.getString("MOZLANG_FILTER_NAME");
    }

    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] 
            { new Instance("*.lang") };
    }
    
    /**
     * Creating an input stream to read the source .lang file.
     * <p>
     * NOTE: Mozilla lang files use always UTF-8 encoding without BOM.
     */
    @Override
    public BufferedReader createReader(File infile, String encoding) throws UnsupportedEncodingException,
            IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(infile), OConsts.UTF8));
    }

    /**
     * Creating an output stream to save a localized .lang file.
     * <p>
     * NOTE: Mozilla lang files use always UTF-8 encoding without BOM.
     * <p>
     */
    @Override
    public BufferedWriter createWriter(File outfile, String encoding) throws UnsupportedEncodingException,
            IOException {
        // lang file use UTF8 encoding
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), OConsts.UTF8));
    }
    
    @Override
    public boolean isSourceEncodingVariable() {
        return false;
    }

    @Override
    public boolean isTargetEncodingVariable() {
        return false;
    }
    
    @Override
    public String getInEncodingLastParsedFile() {
        return OConsts.UTF8;
    }


    @Override
    public void processFile(File inFile, File outFile, FilterContext fc) throws IOException,
            TranslationException {

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
    protected void processFile(BufferedReader inFile, BufferedWriter outFile, FilterContext fc) throws IOException, TranslationException {
        source = new StringBuilder();
        target = new StringBuilder();
        localizationNote = new StringBuilder();
        
        out = outFile;

        READ_STATE state = READ_STATE.WAIT_SOURCE;
        
        String s;
        while ((s = inFile.readLine()) != null) {

            // We trim trailing spaces, otherwise the regexps could fail, thus making some segments
            // invisible to OmegaT
            s = s.trim();

            Matcher m;

            switch (state) {
            case WAIT_SOURCE:
                if ((m = PATTERN_SOURCE.matcher(s)).matches()) {
                    source.append(m.group(1));
                    state = READ_STATE.WAIT_TARGET;
                }
                if (LOCALIZATION_NOTE.matcher(s).matches()) {
                    localizationNote.append(s);
                }
                target.setLength(0);
                eol(s);
                break;
            case WAIT_TARGET:
                target.append(s);
                flushTranslation(fc);
                state = READ_STATE.WAIT_SOURCE;
                break;
            default:
                eol(s);
                break;
            }
        }
    }
           

    protected void eol(String s) throws IOException {
        if (out != null) {
            out.write(s);
            out.write('\n');
        }
    }

    protected void align(int pair) {
        String s = source.toString();
        String c = "";
        String t;
        if ( s.equals(target.toString()) ) {
            t = null;
        } else {
            t = target.toString();
        }
        if (localizationNote.length() > 0) {
            c += "\n" + OStrings.getString("LANGFILTER_LOCALIZATION_NOTE") + "\n" + localizationNote.toString();
        }
        if (c.length()==0) {
            c = null;
        }
        align(s, t, c);
    }

    /**
     *
     * @param source
     * @param translation
     * @param comments
     */
    protected void align(String source, String translation, String comments) {
        if (entryParseCallback != null) {       
            List<ProtectedPart> protectedParts = TagUtil.applyCustomProtectedParts(source, PatternConsts.PRINTF_VARS, null);
            entryParseCallback.addEntry(null, source, translation, false, comments, null, this, protectedParts);
        } else if (entryAlignCallback != null) {
            entryAlignCallback.addTranslation(null, source, translation, false, null, this);
        }
    }

    protected void flushTranslation(FilterContext fc) throws IOException {
        if (out != null) {
            String tr;
            tr = entryTranslateCallback.getTranslation(null, source.toString(), null);
            if (tr == null) {
                tr = source.toString();
            }
            else if ( tr.equals(source.toString()) )
            {
                tr += " {ok}";
            }
            eol(tr);
        } else {
            align(0);
        }
        source.setLength(0);
        target.setLength(0);
        localizationNote.setLength(0);
    }

    /**
     * Returns true to indicate that Text filter has options.
     * 
     * @return False, because the LANG filter has not options.
     */
    @Override
    public boolean hasOptions() {
        return false;
    }
}
