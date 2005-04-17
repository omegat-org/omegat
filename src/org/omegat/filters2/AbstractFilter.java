/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2004  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.filters2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import org.omegat.filters2.master.FilterMaster;

/**
 * The base class for all filters (aka file handlers).
 * Each filter should extend this class or one of its decendants.
 * @author Maxym Mykhalchuk
 */
public abstract class AbstractFilter
{
    
    /**
     * This value means that the encoding is determined by the filter itself.
     */
    public static String ENCODING_AUTO = "<auto>";

    /**
     * The default output filename pattern.
     * <p>
     * It is equal to "${filename}", which means that the name of the
     * translated file should be the same as the name of the input file.
     */
    public static String TARGET_DEFAULT = "${filename}";

    /**
     * Human-readable name of the File Format this filter supports.
     *
     * @return File format name
     */
    public abstract String getFileFormatName();
    
    /**
     * The default list of filter instances that this filter class has.
     * One filter class may have different filter instances, different
     * by source file mask, encoding of the source file etc.
     * <p>
     * Note that the user may change the instances freely.
     *
     * @return Default filter instances
     */
    public abstract Instance[] getDefaultInstances();
    
    /**
     * Whether source encoding can be varied by the user.
     * <p>
     * True means that OmegaT should handle all the encoding mess.
     * <p>
     * Return false to state that your filter doesn't need
     * encoding management provided by OmegaT, because it either autodetects
     * the encoding based on file contents (like HTML filter does) 
     * or the encoding is fixed (like in OpenOffice files).
     * 
     * @return whether source encoding can be changed by the user
     */
    public abstract boolean isSourceEncodingVariable();
    
    /**
     * Whether target encoding can be varied by the user.
     * <p>
     * True means that OmegaT should handle all the encoding mess.
     * <p>
     * Return false to state that your filter doesn't need
     * encoding management provided by OmegaT, because 
     * the encoding is fixed (like in OpenOffice files),
     * or for some other reason.
     * 
     * @return whether target encoding can be changed by the user
     */
    public abstract boolean isTargetEncodingVariable();
    
    /**
     * Returns whether the file is supported by the filter.
     * <p>
     * By default returns true, because this method should be overriden
     * only by filters that differentiate input files not by extensions,
     * but by file's content.
     * <p>
     * For example, DocBook files have .xml extension, as possibly many other 
     * XML files, so the filter should check a DTD of the document.
     *
     * @param infile The source file
     * @return       Does the filter support the file
     */
    public boolean isFileSupported(File infile)
    {
        return true;
    }
    
    /**
     * Creates a reader of an input file.
     *
     * @param infile    The source file
     * @param encoding  Encoding of the input file, if the filter supports it. Otherwise null.
     * @return The reader for the source file
     *
     * @throws UnsupportedEncodingException Thrown if JVM doesn't support the specified encoding
     * @throws IOException If any I/O Error occurs upon reader creation
     */
    public Reader createReader(File infile, String encoding)
            throws UnsupportedEncodingException, IOException
    {
        if( encoding.equals(ENCODING_AUTO) )
            return new InputStreamReader(new FileInputStream(infile));
        else
            return new InputStreamReader(new FileInputStream(infile), encoding);
    }

	/**
     * Creates a writer of the translated file.
     *
     * @param outfile   The target file
     * @param encoding  Encoding of the target file, if the filter supports it. Otherwise null.
     * @return The writer for the target file
     *
     * @throws UnsupportedEncodingException Thrown if JVM doesn't support the specified encoding
     * @throws IOException If any I/O Error occurs upon writer creation
     */
    public Writer createWriter(File outfile, String encoding)
            throws UnsupportedEncodingException, IOException
    {
        if( encoding.equals(ENCODING_AUTO) )
            return new OutputStreamWriter(new FileOutputStream(outfile));
        else
            return new OutputStreamWriter(new FileOutputStream(outfile), encoding);
    }
    
    /**
     * Processes a single file.
     * Generally this method should read strings from the input file 
     * and write them to the output file. In order to let OmegaT know
     * what strings are translatable and to get thair translation,
     * filter should call {@link #processEntry(String)} method.
     * <p>
     * Note that outfile is never null, even when the project is loading.
     * (in this case it writes no nowhere, but anyway you may use it...)
     *
     * @param infile Reader of the source file. It's the result of calling {@link #createReader(InputStream)}.
     * @param outfile Writer of the target file on compilation (the result of calling {@link #createWriter(OutputStream)}), or a fictive writer to /dev/null.
     * @throws TranslationException Should be thrown when processed file has any format defects.
     * @throws IOException Thrown in case of any I/O error.
     */
    public abstract void processFile(Reader infile, Writer outfile)
            throws IOException, TranslationException;

    /**
     * Call this method to:
     * <ul>
     * <li>Instruct OmegaT what source strings are translatable.
     * <li>Get the translation of each source string.
     * </ul>
     *
     * @param entry Translatable source string
     * @return Translation of the source string. If there's no translation, returns the source string itself.
     */
    protected final String processEntry(String entry)
    {
        return FilterMaster.getInstance().processEntry(entry);
    }
    
}
