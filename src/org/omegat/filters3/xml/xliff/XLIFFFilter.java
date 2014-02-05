/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2011 Didier Briel
               2013 Didier Briel, Aaron Madlon-Kay, Piotr Kulik
               2014 Piotr Kulik
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.omegat.core.Core;
import org.omegat.core.data.ProtectedPart;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.xml.sax.Attributes;

/**
 * Filter for XLIFF files.
 * 
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 * @author Piotr Kulik
 */
public class XLIFFFilter extends XMLFilter {

    private String resname;
    private boolean ignored;
    private ArrayList<String> groupResname = new ArrayList<String>();
    private int groupLevel;
    private ArrayList<String> notes = new ArrayList<String>();
    private String text;
    private ArrayList<String> entryText = new ArrayList<String>();
    private ArrayList<List<ProtectedPart>> protectedParts = new ArrayList<List<ProtectedPart>>();

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
     * We're not actually checking whether it is a valid XLIFF file; we just need a place to call defineDialect. 
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

    /** 
     * Support of group and trans-unit resname attribute and trans-unit <note> as comment, based on ResXFilter code
     */
    @Override
    public void tagStart(String path, Attributes atts) {
        if (atts != null && path.endsWith("trans-unit")) {
            resname = atts.getValue("resname");
        }
        // not all <group> tags have resname attribute
        if (path.endsWith("/group")) {
            // <group> only, it can be nested
            groupLevel++;
            groupResname.add(atts.getValue("resname"));
        }
        if ("/xliff/file/header".equals(path)) {
            ignored = true;
        }
    }

    @Override
    public void tagEnd(String path) {
        if (path.endsWith("trans-unit/note")) {
            // <trans-unit> <note>'s only 
            notes.add(text);
        } else if (path.endsWith("trans-unit")) {
            if (entryParseCallback != null) {
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < groupLevel; i++) {
                    String temp = groupResname.get(i);
                    if (temp != null) {
                        buf.append(temp);
                        // group1/group2/..:resname
                        buf.append(i == (groupLevel - 1) ? " : " : " / ");
                    }
                }

                if (resname != null) {
                    buf.append(resname);
                    buf.append('\n');
                }

                for (String note : notes) {
                    buf.append(note);
                    buf.append('\n');
                }
                
                String comment = buf.length() == 0 ? null : buf.substring(0, buf.length() - 1);
                
                for (int i = 0; i < entryText.size(); i++) {
                    entryParseCallback.addEntry(null, entryText.get(i), null, false, comment, null, this, protectedParts.get(i));
                }
            }

            resname = null;
            notes.clear();
            entryText.clear();
            protectedParts.clear();
        } else if (path.endsWith("/group")) {
            groupResname.remove(--groupLevel);
        }
        if ("/xliff/file/header".equals(path)) {
            ignored = false;
        }
    }

    @Override
    public boolean isInIgnored() {
        return ignored;
    }

    @Override
    public void text(String text) {
        this.text = text;
    }

    @Override
    public String translate(String entry, List<ProtectedPart> protectedParts) {
        if (entryParseCallback != null) {
            entryText.add(entry);
            this.protectedParts.add(protectedParts);
            return entry;
        } else if (entryTranslateCallback != null) {
            String translation = entryTranslateCallback.getTranslation(null, entry, null);
            return translation != null ? translation : entry;
        } else {
            return entry;
        }
    }
}
