/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2008 Didier Briel, Martin Fleurke
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

package org.omegat.filters3.xml.xhtml;

import java.awt.Dialog;
import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

/**
 * Filter for XHTML files.
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Martin Fleurke
 */
public class XHTMLFilter extends XMLFilter {

    /**
     * Creates a new instance of XHTMLFilter
     */
    public XHTMLFilter() {
        super(new XHTMLDialect());
        do_not_send_to_core = false;
    }

    /**
     * Human-readable name of the File Format this filter supports.
     * 
     * @return File format name
     */
    public String getFileFormatName() {
        return OStrings.getString("XHTML_FILTER_NAME");
    }

    /**
     * The default list of filter instances that this filter class has. One filter class may have different
     * filter instances, different by source file mask, encoding of the source file etc.
     * <p>
     * Note that the user may change the instances freely.
     * 
     * @return Default filter instances
     */
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.html", null, null), new Instance("*.xhtml", null, null),
                new Instance("*.xht", null, null), };
    }

    /**
     * Either the encoding can be read, or it is UTF-8.
     * 
     * @return <code>false</code>
     */
    public boolean isSourceEncodingVariable() {
        return false;
    }

    /**
     * Yes, XHTML may be written out in a variety of encodings.
     * 
     * @return <code>true</code>
     */
    public boolean isTargetEncodingVariable() {
        return true;
    }

    /**
     * Whether we're now processing the XHTML file the first time, and thus we don't need to send translatable
     * content to OmegaT core.
     */
    private boolean do_not_send_to_core;

    /** Checking whether it is a valid XHTML file. */
    public boolean isFileSupported(File inFile, Map<String, String> config, FilterContext context) {
        boolean result = super.isFileSupported(inFile, config, context);
        if (result) {
            try {
                do_not_send_to_core = true;
                // Defining the actual dialect, because at this step
                // we have the options
                XHTMLDialect dialect = (XHTMLDialect) this.getDialect();
                dialect.defineDialect(new XHTMLOptions(config));
                super.processFile(inFile, null, context);
            } catch (Exception e) {
                Log.log("XHTML file " + inFile.getName() + " is not valid.");
                result = false;
            } finally {
                do_not_send_to_core = false;
            }
        }
        return result;
    }

    /**
     * Overrides superimplementation not to send translatable content on XHTML validity check, and don't
     * translate items that match regular expression.
     */
    public String translate(String entry) {
        if (do_not_send_to_core)
            return entry;
        else {
            Pattern skipRegExpPattern = ((XHTMLDialect) this.getDialect()).getSkipRegExpPattern();
            if (skipRegExpPattern != null) {
                if (skipRegExpPattern.matcher(entry).matches()) {
                    return entry;
                }
            }
            return super.translate(entry);
        }
    }

    /**
     * Returns true to indicate that the XHTML filter has options.
     * 
     * @return True, because the XHTML filter has options.
     */
    public boolean hasOptions() {
        return true;
    }

    /**
     * XHTML Filter shows a <b>modal</b> dialog to edit its own options.
     * 
     * @param currentOptions
     *            Current options to edit.
     * @return Updated filter options if user confirmed the changes, and current options otherwise.
     */
    public Map<String, String> changeOptions(Dialog parent, Map<String, String> currentOptions) {
        try {
            EditXOptionsDialog dialog = new EditXOptionsDialog(parent, currentOptions);
            dialog.setVisible(true);
            if (EditXOptionsDialog.RET_OK == dialog.getReturnStatus())
                return dialog.getOptions().getOptionsMap();
            else
                return null;
        } catch (Exception e) {
            Log.logErrorRB("HTML_EXC_EDIT_OPTIONS");
            Log.log(e);
            return null;
        }
    }
}
