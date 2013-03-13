/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Didier Briel
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

package org.omegat.filters3.xml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.omegat.util.Language;
import org.xml.sax.Attributes;

/**
 * The interface to specify the method a Handler can use to translate text.
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
interface Translator {
    /**
     * The method the Handler would call to pass translatable content to OmegaT
     * core and receive translation.
     */
    String translate(String s, Map<String, String> shortcutDetails);

    /**
     * Creates a special XML-encoding-aware reader of an input file.
     * 
     * @param inFile
     *            The source file.
     * @param outEncoding
     *            Encoding of the source file, if the filter supports it.
     *            Otherwise null.
     * @return The reader of the source file.
     * 
     * @throws UnsupportedEncodingException
     *             Thrown if JVM doesn't support the specified inEncoding.
     * @throws IOException
     *             If any I/O Error occurs upon reader creation.
     */
    BufferedReader createReader(File inFile, String inEncoding) throws UnsupportedEncodingException,
            IOException;

    /**
     * Creates a writer of the translated file.
     * 
     * @param outFile
     *            The target file.
     * @param outEncoding
     *            Encoding of the target file, if the filter supports it.
     *            Otherwise null.
     * @return The writer for the target file.
     * 
     * @throws UnsupportedEncodingException
     *             Thrown if JVM doesn't support the specified outEncoding
     * @throws IOException
     *             If any I/O Error occurs upon writer creation
     */
    BufferedWriter createWriter(File outFile, String outEncoding) throws UnsupportedEncodingException,
            IOException;

    /**
     * Start tag translation.
     * 
     * @param path
     *            path in the XML
     * @param atts
     *            attributes
     */
    void tagStart(String path, Attributes atts);

    /**
     * Finish tag translation.
     * 
     * @param path
     *            path in the XML
     */
    void tagEnd(String path);

    /**
     * Process comment.
     */
    void comment(String comment);

    /**
     * Process text.
     */
    void text(String text);
    
    Language getTargetLanguage();
}
