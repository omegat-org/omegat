/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.filters2;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an instance of a filter configuration with attributes such as
 * source filename mask, source encoding, target encoding, and target filename
 * pattern. This class is used to define how files are handled during a
 * filtering process. It supports several constructors allowing different levels
 * of initialization.
 * 
 * @author Maxym Mykhalchuk
 */
public class Instance implements Serializable {

    private static final long serialVersionUID = -8290853406593590600L;

    private @Nullable String sourceFilenameMask;

    public @Nullable String getSourceFilenameMask() {
        return sourceFilenameMask;
    }

    public void setSourceFilenameMask(@Nullable String sourceFilenameMask) {
        this.sourceFilenameMask = sourceFilenameMask;
    }

    private @Nullable String sourceEncoding;

    public String getSourceEncodingHuman() {
        return Objects.requireNonNullElse(sourceEncoding, AbstractFilter.ENCODING_AUTO_HUMAN);
    }

    public @Nullable String getSourceEncoding() {
        return sourceEncoding;
    }

    public void setSourceEncoding(String sourceEncoding) {
        if (sourceEncoding == null || sourceEncoding.equals(AbstractFilter.ENCODING_AUTO_HUMAN)) {
            this.sourceEncoding = null;
        } else {
            this.sourceEncoding = sourceEncoding;
        }
    }

    private @Nullable String targetEncoding;

    public String getTargetEncodingHuman() {
        return Objects.requireNonNullElse(targetEncoding, AbstractFilter.ENCODING_AUTO_HUMAN);
    }

    public @Nullable String getTargetEncoding() {
        return targetEncoding;
    }

    public void setTargetEncoding(String targetEncoding) {
        if (targetEncoding == null || targetEncoding.equals(AbstractFilter.ENCODING_AUTO_HUMAN)) {
            this.targetEncoding = null;
        } else {
            this.targetEncoding = targetEncoding;
        }
    }

    private String targetFilenamePattern;

    public String getTargetFilenamePattern() {
        return targetFilenamePattern;
    }

    public void setTargetFilenamePattern(String targetFilenamePattern) {
        this.targetFilenamePattern = targetFilenamePattern;
    }

    private void init(String newSourceFilenameMask, String newSourceEncoding, String newTargetEncoding,
            String newTargetFilenamePattern) {
        setSourceFilenameMask(newSourceFilenameMask);
        setSourceEncoding(newSourceEncoding);
        setTargetEncoding(newTargetEncoding);
        setTargetFilenamePattern(newTargetFilenamePattern);
    }

    /**
     * Creates a new instance of FilterInstance.
     * <p>
     * Value <code>null</code> for source and target encoding means that the
     * filter selects encodings automatically.
     * <p>
     * Output (target) filename pattern cannot be null.
     */
    public Instance(String sourceFilenameMask, @Nullable String sourceEncoding,
            @Nullable String targetEncoding, String targetFilenamePattern) {
        init(sourceFilenameMask, sourceEncoding, targetEncoding, targetFilenamePattern);
    }

    /**
     * Creates a new Filter Instance with source file mask and two encodings
     * specified, and having a default target filename pattern.
     * <p>
     * Value <code>null</code> for source and target encoding means that the
     * filter selects encodings automatically.
     * <p>
     * The default output filename pattern is "${filename}", which means that
     * the name of the translated file should be the same as the name of the
     * input file.
     */
    public Instance(String sourceFilenameMask, @Nullable String sourceEncoding, @Nullable String targetEncoding) {
        init(sourceFilenameMask, sourceEncoding, targetEncoding, AbstractFilter.TARGET_DEFAULT);
    }

    /**
     * Creates a new Filter Instance with source file mask and source encoding
     * specified, and having a default target encoding and target filename
     * pattern.
     * <p>
     * Value <code>null</code> (default value) for source encoding means that
     * the filter selects encoding of the source file automatically.
     * <p>
     * Default value for target encoding is <code>null</code>, meaning that the
     * filter selects encoding of the target file automatically.
     * <p>
     * The default output filename pattern is "${filename}", which means that
     * the name of the translated file should be the same as the name of the
     * input file.
     */
    public Instance(String sourceFilenameMask, String sourceEncoding) {
        init(sourceFilenameMask, sourceEncoding, null, AbstractFilter.TARGET_DEFAULT);
    }

    /**
     * Creates a new Filter Instance with only source file mask specified, and
     * default values for everything else.
     * <p>
     * Default value for source and target encoding is <code>null</code>,
     * meaning that the filter selects encodings automatically.
     * <p>
     * The default output filename pattern is "${filename}", which means that
     * the name of the translated file should be the same as the name of the
     * input file.
     */
    public Instance(String sourceFilenameMask) {
        init(sourceFilenameMask, null, null, AbstractFilter.TARGET_DEFAULT);
    }

    /**
     * Creates a new Filter Instance, uninitialized. Is here to support
     * JavaBeans specification, <b>don't use</b> it in filters.
     */
    public Instance() {
        init("*.*", null, null, AbstractFilter.TARGET_DEFAULT);
    }

}
