/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.convert.v20to21.data;

import org.omegat.util.OStrings;

/**
 * 
 * @author Maxym Mykhalchuk
 */
public class Instance {

    /**
     * This value represents to the user that the encoding is determined by the
     * filter itself. "In code" the <code>null</code> is used to represent
     * automatic encoding selection.
     */
    public static String ENCODING_AUTO_HUMAN = OStrings.getString("ENCODING_AUTO");

    /** The original filename (with extension). */
    public static final String TFP_FILENAME = "${filename}";

    /**
     * The default output filename pattern.
     * <p>
     * It is equal to "${filename}", which means that the name of the translated
     * file should be the same as the name of the input file.
     */
    public static String TARGET_DEFAULT = TFP_FILENAME;

    private String sourceFilenameMask;

    public String getSourceFilenameMask() {
        return sourceFilenameMask;
    }

    public void setSourceFilenameMask(String sourceFilenameMask) {
        this.sourceFilenameMask = sourceFilenameMask;
    }

    private String sourceEncoding;

    public String getSourceEncodingHuman() {
        if (sourceEncoding == null)
            return ENCODING_AUTO_HUMAN;
        else
            return sourceEncoding;
    }

    public String getSourceEncoding() {
        return sourceEncoding;
    }

    public void setSourceEncoding(String sourceEncoding) {
        if (sourceEncoding == null || sourceEncoding.equals(ENCODING_AUTO_HUMAN))
            this.sourceEncoding = null;
        else
            this.sourceEncoding = sourceEncoding;
    }

    private String targetEncoding;

    public String getTargetEncodingHuman() {
        if (targetEncoding == null)
            return ENCODING_AUTO_HUMAN;
        else
            return targetEncoding;
    }

    public String getTargetEncoding() {
        return targetEncoding;
    }

    public void setTargetEncoding(String targetEncoding) {
        if (targetEncoding == null || targetEncoding.equals(ENCODING_AUTO_HUMAN))
            this.targetEncoding = null;
        else
            this.targetEncoding = targetEncoding;
    }

    private String targetFilenamePattern;

    public String getTargetFilenamePattern() {
        return targetFilenamePattern;
    }

    public void setTargetFilenamePattern(String targetFilenamePattern) {
        this.targetFilenamePattern = targetFilenamePattern;
    }

    private void init(String sourceFilenameMask, String sourceEncoding, String targetEncoding,
            String targetFilenamePattern) {
        setSourceFilenameMask(sourceFilenameMask);
        setSourceEncoding(sourceEncoding);
        setTargetEncoding(targetEncoding);
        setTargetFilenamePattern(targetFilenamePattern);
    }

    /**
     * Creates a new Filter Instance, uninitialized. Is here to support
     * JavaBeans specification, <b>don't use</b> it in filters.
     */
    public Instance() {
        init("*.*", null, null, TARGET_DEFAULT);
    }
}
