/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
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

package org.omegat.filters2;

import java.io.File;

/**
 * Interface for filters declaration.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public interface IFilter {
    /**
     * Human-readable name of the File Format this filter supports.
     * 
     * @return File format name
     */
    String getFileFormatName();

    /**
     * The default list of filter instances that this filter class has. One
     * filter class may have different filter instances, different by source
     * file mask, encoding of the source file etc.
     * <p>
     * Note that the user may change the instances freely.
     * 
     * @return Default filter instances
     */
    Instance[] getDefaultInstances();

    /**
     * Whether source encoding can be varied by the user.
     * <p>
     * True means that OmegaT should handle all the encoding mess.
     * <p>
     * Return false to state that your filter doesn't need encoding management
     * provided by OmegaT, because it either autodetects the encoding based on
     * file contents (like HTML filter does) or the encoding is fixed (like in
     * OpenOffice files).
     * 
     * @return whether source encoding can be changed by the user
     */
    boolean isSourceEncodingVariable();

    /**
     * Whether target encoding can be varied by the user.
     * <p>
     * True means that OmegaT should handle all the encoding mess.
     * <p>
     * Return false to state that your filter doesn't need encoding management
     * provided by OmegaT, because the encoding is fixed (like in OpenOffice
     * files), or for some other reason.
     * 
     * @return whether target encoding can be changed by the user
     */
    boolean isTargetEncodingVariable();

    /**
     * Parse single file.
     * 
     * @param inFile
     *            file to parse
     * @param inEncoding
     *            file encoding, configured by user
     * @param callback
     *            callback for parsed data
     * @throws Exception
     */
    void parseFile(File inFile, String inEncoding, IParseCallback callback)
            throws Exception;

    /**
     * Create translated file.
     * 
     * @param inFile
     *            source file
     * @param inEncoding
     *            source file encoding, configured by user
     * @param targetLang
     *            language for translated file
     * @param outFile
     *            output file
     * @param outEncoding
     *            output file encoding, configured by user
     * @param callback
     *            callback for get translation
     * @throws Exception
     */
    void translateFile(File inFile, String inEncoding, String targetLang,
            File outFile, String outEncoding, ITranslateCallback callback)
            throws Exception;

    /**
     * Align source and translated files.
     * 
     * @param inFile
     *            source file
     * @param inEncoding
     *            source file encoding, configured by user
     * @param outFile
     *            translated file
     * @param outEncoding
     *            translated file encoding, configured by user
     * @param callback
     *            callback for store aligned data
     * @throws Exception
     */
    void alignFile(File inFile, String inEncoding, File outFile,
            String outEncoding, IAlignCallback callback) throws Exception;
}
