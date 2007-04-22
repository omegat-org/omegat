/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel
               Home page: http://www.omegat.org/omegat/omegat.html
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

package org.omegat.filters3.xml.xhtml;

import java.awt.Dialog;
import java.io.File;
import java.io.Serializable;

import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.filters3.xml.XMLDialect;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

/**
 * Filter for XHTML files.
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class XHTMLFilter extends XMLFilter
{
    
    /**
     * Creates a new instance of XHTMLFilter
     */
    public XHTMLFilter()
    {
        super(new XHTMLDialect());
        do_not_send_to_core = false;
    }
    
    /**
     * Human-readable name of the File Format this filter supports.
     *
     * @return File format name
     */
    public String getFileFormatName()
    {
        return OStrings.getString("XHTML_FILTER_NAME");
    }
    
    /**
     * The default list of filter instances that this filter class has.
     * One filter class may have different filter instances, different
     * by source file mask, encoding of the source file etc.
     * <p>
     * Note that the user may change the instances freely.
     *
     * @return Default filter instances
     */
    public Instance[] getDefaultInstances()
    {
        return new Instance[]
        {
            new Instance("*.html", "UTF-8", "UTF-8"),                           // NOI18N
            new Instance("*.xhtml", "UTF-8", "UTF-8"),                          // NOI18N
        };
    }
    
    /**
     * Yes, XHTML may be read in a variety of encodings.
     * @return <code>true</code>
     */
    public boolean isSourceEncodingVariable()
    {
        return true;
    }
    
    /**
     * Yes, XHTML may be written out in a variety of encodings.
     * @return <code>true</code>
     */
    public boolean isTargetEncodingVariable()
    {
        return true;
    }
    
    /**
     * Whether we're now processing the XHTML file the first time,
     * and thus we don't need to send translatable content to OmegaT core.
     */
    private boolean do_not_send_to_core;
    
    /** Checking whether it is a valid XHTML file. */
    public boolean isFileSupported(File inFile, String inEncoding)
    {
        boolean result = super.isFileSupported(inFile, inEncoding);
        if (result)
        {
            try
            {
                do_not_send_to_core = true;
                // Defining the actual dialect, because at this step 
                // we have the options
                XHTMLDialect dialect = (XHTMLDialect) this.getDialect();
                dialect.defineDialect((XHTMLOptions) this.getOptions());
                super.processFile(inFile, inEncoding, null, null);
            }
            catch (Exception e)
            {
                Log.log("XHTML file "+inFile.getName()+" is not valid.");
                result = false;
            }
            finally
            {
                do_not_send_to_core = false;
            }
        }
        return result;
    }
    
    /**
     * Overrides superimplementation not to send translatable content on XHTML
     * validity check. */
    public String translate(String entry)
    {
        if (do_not_send_to_core)
            return entry;
        else
            return super.translate(entry);
    }
    
    /**
     * Returns true to indicate that the XHTML filter has options.
     * @return True, because the XHTML filter has options.
     */
    public boolean hasOptions()
    {
        return true;
    }
    
    /**
     * XHTML Filter shows a <b>modal</b> dialog to edit its own options.
     * 
     * @param currentOptions Current options to edit.
     * @return Updated filter options if user confirmed the changes, 
     * and current options otherwise.
     */
    public Serializable changeOptions(Dialog parent, Serializable currentOptions)
    {
        try
        {
            XHTMLOptions options = (XHTMLOptions) currentOptions;
            EditXOptionsDialog dialog = new EditXOptionsDialog(parent, options);
            dialog.setVisible(true);
            if( EditXOptionsDialog.RET_OK==dialog.getReturnStatus() )
                return dialog.getOptions();
            else
                return currentOptions;
        }
        catch( Exception e )
        {
            Log.logErrorRB("HTML_EXC_EDIT_OPTIONS");
            Log.log(e);
            return currentOptions;
        }
    }
}
