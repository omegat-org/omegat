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

import java.beans.XMLEncoder;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;


/**
 *
 * @author Maxym Mykhalchuk
 */
public class Instance implements Serializable
{
    
    private String sourceFilenameMask;
    public String getSourceFilenameMask()
    {
        return sourceFilenameMask;
    }
    public void setSourceFilenameMask(String sourceFilenameMask)
    {
        this.sourceFilenameMask = sourceFilenameMask;
    }

    private String sourceEncoding;
    public String getSourceEncoding()
    {
        return sourceEncoding;
    }
    public void setSourceEncoding(String sourceEncoding)
    {
        this.sourceEncoding = sourceEncoding;
    }
    
    private String targetEncoding;
    public String getTargetEncoding()
    {
        return targetEncoding;
    }
    public void setTargetEncoding(String targetEncoding)
    {
        this.targetEncoding = targetEncoding;
    }
    
    private String targetFilenamePattern;
    public String getTargetFilenamePattern()
    {
        return targetFilenamePattern;
    }
    public void setTargetFilenamePattern(String targetFilenamePattern)
    {
        this.targetFilenamePattern = targetFilenamePattern;
    }
    
    private void init(String sourceFilenameMask, String sourceEncoding, String targetEncoding, String targetFilenamePattern)
    {
        this.sourceFilenameMask = sourceFilenameMask;
        this.sourceEncoding = sourceEncoding;
        this.targetEncoding = targetEncoding;
        this.targetFilenamePattern = targetFilenamePattern;
    }

    /**
     * Creates a new instance of FilterInstance.
     */
    public Instance(String sourceFilenameMask, String sourceEncoding, String targetEncoding, String targetFilenamePattern)
    {
        init(sourceFilenameMask, sourceEncoding, targetEncoding, targetFilenamePattern);
    }
    
    /**
     * Creates a new Filter Instance with source file mask and two encodings specified,
     * and having a default target filename pattern.
     * <p>
     * The default output filename pattern is "${filename}", which means that
     * the name of the translated file should be the same as the name of the input file.
     *
     * @return Output filename pattern
     */
    public Instance(String sourceFilenameMask, String sourceEncoding, String targetEncoding)
    {
        init(sourceFilenameMask, sourceEncoding, targetEncoding, AbstractFilter.TARGET_DEFAULT);
    }
    
    /**
     * Creates a new Filter Instance with source file mask and source encoding specified,
     * and having a default target encoding and target filename pattern.
     */
    public Instance(String sourceFilenameMask, String sourceEncoding)
    {
        init(sourceFilenameMask, sourceEncoding, AbstractFilter.ENCODING_AUTO, AbstractFilter.TARGET_DEFAULT);
    }
    
    /**
     * Creates a new Filter Instance with only source file mask specified,
     * and default values for everything else.
     */
    public Instance(String sourceFilenameMask)
    {
        init(sourceFilenameMask, AbstractFilter.ENCODING_AUTO, AbstractFilter.ENCODING_AUTO, AbstractFilter.TARGET_DEFAULT);
    }
    
    /**
     * Creates a new Filter Instance, uninitialized.
     * Needed to support JavaBeans specification, don't use it in filters.
     */
    public Instance()
    {
        init("*.*", AbstractFilter.ENCODING_AUTO, AbstractFilter.ENCODING_AUTO, AbstractFilter.TARGET_DEFAULT);
    }

}

