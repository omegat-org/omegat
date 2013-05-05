/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2011 Didier Briel
               2013 Didier Briel
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters3.xml.xliff;

import java.awt.Dialog;
import java.io.File;
import java.util.Map;

import org.omegat.core.Core;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

/**
 * Filter for XLIFF files.
 * 
 * @author Didier Briel
 */
public class XLIFFFilter extends XMLFilter {

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(XLIFFFilter.class);
    }

    public static void unloadPlugins() {
    }

    /**
     * Creates a new instance of XLIFFFilter
     */
    public XLIFFFilter() {
        super(new XLIFFDialect());
    }

    /**
     * Human-readable name of the File Format this filter supports.
     * 
     * @return File format name
     */
    @Override
    public String getFileFormatName() {
        return OStrings.getString("XLIFF_FILTER_NAME");
    }

    /**
     * The default list of filter instances that this filter class has. One
     * filter class may have different filter instances, different by source
     * file mask, encoding of the source file etc.
     * <p>
     * Note that the user may change the instances freely.
     * 
     * @return Default filter instances
     */
    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.xlf", null, null), 
                                new Instance("*.xliff", null, null),
                                new Instance("*.sdlxliff", null, null), };
    }

    /**
     * Either the encoding can be read, or it is UTF-8.
     * 
     * @return <code>false</code>
     */
    @Override
    public boolean isSourceEncodingVariable() {
        return false;
    }

    /**
     * Yes, XLIFF may be written out in a variety of encodings.
     * 
     * @return <code>true</code>
     */
    @Override
    public boolean isTargetEncodingVariable() {
        return true;
    }
    
    @Override
    protected boolean requirePrevNextFields() {
        return true;
    }
    
    /**
     * Returns true to indicate that the XLIFF filter has options.
     * 
     * @return True, because the XLIFF filter has options.
     */
    @Override
    public boolean hasOptions() {
        return true;
    }

    /**
     * XLIFF Filter shows a <b>modal</b> dialog to edit its own options.
     * 
     * @param currentOptions
     *            Current options to edit.
     * @return Updated filter options if user confirmed the changes, and current options otherwise.
     */
    @Override
    public Map<String, String> changeOptions(Dialog parent, Map<String, String> currentOptions) {
        try {
            EditXLIFFOptionsDialog dialog = new EditXLIFFOptionsDialog(parent, currentOptions);
            dialog.setVisible(true);
            if (EditXLIFFOptionsDialog.RET_OK == dialog.getReturnStatus())
                return dialog.getOptions().getOptionsMap();
            else {
                return null;
            }
        } catch (Exception e) {
            Log.logErrorRB("HTML_EXC_EDIT_OPTIONS");
            Log.log(e);
            return null;
        }
    }
    
    /** 
     * We're not actually checking whether it is a valid XLIFF file; we just need a place to call define.dialect. 
     */
    @Override
    public boolean isFileSupported(File inFile, Map<String, String> config, FilterContext context) {
        boolean result = super.isFileSupported(inFile, config, context);
        if (result) {
                // Defining the actual dialect, because at this step
                // we have the options
                XLIFFDialect dialect = (XLIFFDialect) this.getDialect();
                dialect.defineDialect(new XLIFFOptions(config));
                try {
                    super.processFile(inFile, null, context);
                } catch (Exception e) {
                    Log.log(e);
                }
        }
        return result;
    }


}
