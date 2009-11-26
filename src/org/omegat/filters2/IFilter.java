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

import java.awt.Dialog;
import java.io.File;
import java.util.Map;

import org.omegat.util.Language;

/**
 * Interface for filters declaration.
 * 
 * TODO: each filter should be stateless, i.e. options shouldn't be stored in
 * filter, but should be sent to filter on each parse, align, or translate
 * operation.
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
     * Returns the hint displayed while the user edits the filter, and when she
     * adds/edits the instance of this filter. The hint may be any string,
     * preferably in a non-geek language.
     * 
     * @return The hint for editing the filter in a non-geek language.
     */
    String getHint();

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
     * Define fuzzy mark prefix for source which will be stored in TM. It's
     * 'fuzzy' by default, but each filter can redefine it.
     * 
     * @return fuzzy mark prefix
     */
    String getFuzzyMark();

    /**
     * Returns whether the file is supported by the filter, given the file and
     * possible file's encoding (<code>null</code> encoding means autodetect).
     * <p>
     * For example, DocBook files have .xml extension, as possibly many other
     * XML files, so the filter should check a DTD of the document.
     * 
     * @param inFile
     *            Source file.
     * @param inEncoding
     *            Encoding of the source file.
     * @param config
     *            filter's configuration options
     * @return Does the filter support the file.
     */
    boolean isFileSupported(File inFile, String inEncoding,
            Map<String, String> config);

    /**
     * Parse single file.
     * 
     * @param inFile
     *            file to parse
     * @param inEncoding
     *            file encoding, configured by user
     * @param config
     *            filter's configuration options
     * @param callback
     *            callback for parsed data
     * @throws Exception
     */
    void parseFile(File inFile, String inEncoding, Map<String, String> config,
            IParseCallback callback) throws Exception;

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
     * @param config
     *            filter's configuration options
     * @param callback
     *            callback for get translation
     * @throws Exception
     */
    void translateFile(File inFile, String inEncoding, Language targetLang,
            File outFile, String outEncoding, Map<String, String> config,
            ITranslateCallback callback) throws Exception;

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
     * @param config
     *            filter's configuration options
     * @param callback
     *            callback for store aligned data
     * @throws Exception
     */
    void alignFile(File inFile, String inEncoding, File outFile,
            String outEncoding, Map<String, String> config,
            IAlignCallback callback) throws Exception;

    boolean hasOptions();

    /**
     * Show change options dialog for able to change options.
     * 
     * @param parent
     *            parent window
     * @param config
     *            old options
     * @return new options or null if options not changed
     */
    Map<String, String> changeOptions(Dialog parent, Map<String, String> config);
}
