/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Arno Peters
               2015 Aaron Madlon-Kay
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

package org.omegat.filters2.pdf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;

import org.omegat.core.Core;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.OStrings;

/**
 * PDF input filter
 * @author Arno Peters
 * @author Aaron Madlon-Kay
 */
public class PdfFilter  extends AbstractFilter {

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(PdfFilter.class);
    }

    public static void unloadPlugins() {
    }

    private static final Pattern LINEBREAK_PATTERN = Pattern.compile("^\\s*?$");

    @Override
    public String getFileFormatName() {
        return OStrings.getString("PDFFILTER_FILTER_NAME");
    }

    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.pdf", null, null, TFP_NAMEONLY + ".txt") };
    }

    @Override
    public boolean isSourceEncodingVariable() {
        return false;
    }

    @Override
    public boolean isTargetEncodingVariable() {
        return true;
    }

    @Override
    public BufferedReader createReader(File infile, String encoding)
            throws IOException, TranslationException {
        PDFTextStripper stripper;
        stripper = new PDFTextStripper();
        stripper.setLineSeparator("\n");
        stripper.setSortByPosition(true);

        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(infile))) {
            String text = stripper.getText(document);
            return new BufferedReader(new StringReader(text));
        } catch (InvalidPasswordException ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                    OStrings.getString("PDFFILTER_ENCRYPTED_FILE"), infile);
            throw new TranslationException(ex);
        }
    }

    @Override
    public void processFile(BufferedReader in, BufferedWriter out, FilterContext fc) {
        StringBuilder sb = new StringBuilder();

        String s = "";
        try {
            while ((s = in.readLine()) != null) {
                Matcher m = LINEBREAK_PATTERN.matcher(s);

                if (m.find()) {
                    out.write(processEntry(sb.toString()));
                    sb.setLength(0);
                    out.write("\n\n");
                } else {
                    sb.append(s);
                    sb.append(" ");
                }
            }

            if (sb.length() > 0) {
                out.write(processEntry(sb.toString()));
                sb.setLength(0);
                out.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
