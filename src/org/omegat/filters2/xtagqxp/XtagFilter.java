/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Didier Briel
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

package org.omegat.filters2.xtagqxp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.OStrings;

/**
 * Filter to support Xtag files generated by CopyFlow Gold for QuarkXPress
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class XtagFilter extends AbstractFilter {

    protected static final String EOL = "\r\n";

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(XtagFilter.class);
    }

    public static void unloadPlugins() {
    }

    @Override
    public String getFileFormatName() {
        return OStrings.getString("XTAGFILTER_FILTER_NAME");
    }

    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] {
                new Instance("*.tag", StandardCharsets.UTF_16LE.name(), StandardCharsets.UTF_16LE.name()),
                new Instance("*.xtg", StandardCharsets.UTF_16LE.name(), StandardCharsets.UTF_16LE.name()), };
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
    protected boolean requirePrevNextFields() {
        return true;
    }

    @Override
    public void processFile(BufferedReader in, BufferedWriter out, FilterContext fc)
            throws IOException, TranslationException {
        // BOM (byte order mark) bugfix
        in.mark(1);
        int ch = in.read();
        if (ch != 0xFEFF) {
            in.reset();
        } else {
            out.write(ch); // If there was a BOM, we rewrite it
        }

        processXtagFile(in, out);
    }

    /**
     * Processes a CopyFlow Gold for QuarkXpress document. Transmits the
     * translatable parts to privateProcessEntry.
     *
     * @param inFile
     *            Source document
     * @param outFile
     *            Target document
     */
    private void processXtagFile(BufferedReader inFile, Writer outFile) throws IOException,
            TranslationException {
        final int stateWaitText = 1;
        final int stateReadText = 2;
        int state = stateWaitText;
        String tr;

        String s;
        s = inFile.readLine();
        while (s != null) {
            // Translatable text
            if (s.startsWith("@$:")) {
                outFile.write("@$:");
                s = s.substring(3);
                state = stateReadText;
            } else if (s.startsWith("#boxname")) {
                state = stateWaitText;
            }
            if (state == stateReadText) {
                tr = privateProcessEntry(s);
            } else {
                tr = s;
            }
            outFile.write(tr);
            s = inFile.readLine();
            if (s != null) {
                outFile.write(EOL);
            }
        }
    }

    /**
     * Lists of Xtags in an entry
     */
    private List<Xtag> listTags = new ArrayList<Xtag>();

    /**
     * Finds the Xtag corresponding to an OmegaT tag
     *
     * @param tag
     *            OmegaT tag, without &lt; and &gt;
     * @return either the original Xtag, or the tag with &lt; and &gt;
     *         characters converted to the Xtag equivalent
     */
    private String findTag(StringBuilder tag) {
        for (Xtag oneTag : listTags) {
            if (oneTag.toShortcut().equals(tag.toString())) {
                return oneTag.toOriginal();
            }
        }
        // It was not a real tag
        // We must convert < to <\<> and > to <\>>
        StringBuilder changedString = new StringBuilder();
        for (int cp, i = 0; i < tag.length(); i += Character.charCount(cp)) {
            cp = tag.codePointAt(i);
            changedString.append(convertSpecialCharacter(cp));
        }
        return changedString.toString();
    }

    /**
     * Receives a character, and convert it to the Xtag equivalent if necessary
     *
     * @param cp
     *            A character
     * @return either the original character, or an Xtag version of that
     *         character
     */
    private String convertSpecialCharacter(int cp) {
        if (cp == '<') {
            return "<\\<>";
        } else if (cp == '>') {
            return "<\\>>";
        } else {
            return String.valueOf(Character.toChars(cp));
        }
    }

    /**
     * Receives an entry with CopyFlow Gold for QuarkXpress pseudo tags (Xtags)
     * Transforms the Xtags into OmegaT tags
     *
     * @param s
     *            An entry with Xtags to process
     * @return the entry with OmegaT tags
     */
    private String convertToTags(String s) {
        StringBuilder changedString = new StringBuilder();
        final int stateNormal = 1;
        final int stateCollectTag = 2;

        int state = stateNormal;
        int num = 0;
        listTags.clear();

        StringBuilder tag = new StringBuilder(s.length());
        for (int cp, i = 0; i < s.length(); i += Character.charCount(cp)) {
            cp = s.codePointAt(i);
            // Start of a tag
            if ((cp == '<') && !(state == stateCollectTag)) {
                tag.setLength(0);
                state = stateCollectTag;
                // Possible end of a tag
                // Exception for <\>>, which is how CopyFlow stores a >
            } else if (cp == '>' && tag.lastIndexOf("\\") != tag.offsetByCodePoints(tag.length(), -1)) {
                num++;
                Xtag oneTag = new Xtag(tag.toString(), num);
                changedString.append(oneTag.toShortcut());
                listTags.add(oneTag);
                tag.setLength(0);
                state = stateNormal;
            } else if (state == stateCollectTag) {
                tag.appendCodePoint(cp);
            } else {
                changedString.appendCodePoint(cp);
            }
        }

        return changedString.toString();
    }

    /**
     * Receives an entry with OmegaT tags. Transorms the OmegaT tags back into
     * the original Xtags
     *
     * @param s
     *            An entry with OmegaT tags to process
     * @return the entry with the original Xtags
     */
    private String convertToXtags(String s) {
        StringBuilder changedString = new StringBuilder();
        final int stateNormal = 1;
        final int stateCollectTag = 2;

        int state = stateNormal;

        StringBuilder tag = new StringBuilder(s.length());
        for (int cp, i = 0; i < s.length(); i += Character.charCount(cp)) {
            cp = s.codePointAt(i);
            // Start of a tag
            if ((cp == '<') && (state != stateCollectTag)) {
                tag.setLength(0);
                tag.appendCodePoint(cp);
                state = stateCollectTag;
                // End of a tag
            } else if ((cp == '>') && (state == stateCollectTag)) {
                tag.appendCodePoint(cp);
                changedString.append(findTag(tag));
                state = stateNormal;
                tag.setLength(0);
            } else if (state == stateCollectTag) {
                tag.appendCodePoint(cp);
            } else {
                changedString.append(convertSpecialCharacter(cp));
            }
        }
        // Copy what might remain at the end of the string
        changedString.append(findTag(tag));
        return changedString.toString();
    }

    /**
     * Processes Xtags before and after sending the entry to OmegaT. The Xtags
     * in the entry are converted to OmegaT tags, then the entry is sent to
     * OmegaT, and the OmegaT tags are converted back to Xtags.
     *
     * @param entry
     *            An entry to process
     * @return The entry for the target document
     */
    private String privateProcessEntry(String entry) {
        entry = convertToTags(entry);
        entry = processEntry(entry);
        entry = convertToXtags(entry);
        return entry;
    }

}
