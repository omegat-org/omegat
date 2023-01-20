/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2011 Didier Briel
               2013 Didier Briel, Aaron Madlon-Kay, Piotr Kulik, Alex Buloichik
               2014 Piotr Kulik, Didier Briel, Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters3.xml.xliff;

import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

import org.omegat.core.Core;
import org.omegat.core.data.ProtectedPart;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.XMLFilter;
import org.omegat.filters3.xml.xliff.XLIFFOptions.ID_TYPE;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Filter for XLIFF files.
 *
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 * @author Piotr Kulik
 * @author Alex Buloichik
 */
public class XLIFFFilter extends XMLFilter {

    private String resname;
    private boolean ignored;
    private ArrayList<String> groupResname = new ArrayList<String>();
    private int groupLevel;
    private ArrayList<String> props = new ArrayList<String>();
    private StringBuilder text = new StringBuilder();
    private ArrayList<String> entryText = new ArrayList<String>();
    private ArrayList<List<ProtectedPart>> protectedParts = new ArrayList<List<ProtectedPart>>();
    private HashSet<String> altIDCache = new HashSet<String>();

    private String id;

    /**
     * Sets whether alternative translations are identified by previous and next
     * paragraphs or by &lt;trans-unit&gt; ID
     */
    private ID_TYPE altTransIDType = ID_TYPE.CONTEXT;

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
        return new Instance[] { new Instance("*.xlf", null, null), new Instance("*.xliff", null, null),
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
        return altTransIDType == ID_TYPE.CONTEXT;
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
     * @return Updated filter options if user confirmed the changes, and current
     *         options otherwise.
     */
    @Override
    public Map<String, String> changeOptions(Window parent, Map<String, String> currentOptions) {
        try {
            EditXLIFFOptionsDialog dialog = new EditXLIFFOptionsDialog(parent, currentOptions);
            dialog.setVisible(true);
            if (EditXLIFFOptionsDialog.RET_OK == dialog.getReturnStatus()) {
                return dialog.getOptions().getOptionsMap();
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.logErrorRB("HTML_EXC_EDIT_OPTIONS");
            Log.log(e);
            return null;
        }
    }

    /**
     * We're not actually checking whether it is a valid XLIFF file; we just
     * need a place to call defineDialect.
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
            this.altTransIDType = dialect.altTransIDType;
        }
        return result;
    }

    /**
     * Support of group and trans-unit resname attribute and trans-unit <note>
     * as comment, based on ResXFilter code
     */
    @Override
    public void tagStart(String path, Attributes atts) {
        if (atts != null && path.endsWith("trans-unit")) {
            // resname may or may not be present.
            resname = atts.getValue("resname");
            id = atts.getValue("id");
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
        text.setLength(0);
    }

    @Override
    public void tagEnd(String path) {
        if (path.endsWith("trans-unit/note")) {
            // <trans-unit> <note>'s only
            addProperty("note", text.toString());
        } else if (path.endsWith("trans-unit")) {
            if (entryParseCallback != null) {
                StringBuilder buf = new StringBuilder();
                for (int i = 0; i < groupLevel; i++) {
                    String temp = groupResname.get(i);
                    if (temp != null) {
                        buf.append(temp);
                        // group1/group2/..
                        buf.append(i == (groupLevel - 1) ? "" : "/");
                    }
                }
                if (buf.length() > 0) {
                    addProperty("group", buf.toString());
                }

                if (resname != null) {
                    addProperty("resname", resname);
                }

                for (int i = 0; i < entryText.size(); i++) {
                    entryParseCallback.addEntryWithProperties(getSegID(), entryText.get(i), null, false,
                            finalizeProperties(), null, this, protectedParts.get(i));
                }
            }

            id = null;
            resname = null;
            props.clear();
            entryText.clear();
            protectedParts.clear();
        } else if (path.endsWith("/group")) {
            groupResname.remove(--groupLevel);
        } else if (path.endsWith("/file")) {
            altIDCache.clear();
        }
        if ("/xliff/file/header".equals(path)) {
            ignored = false;
        }
    }

    private void addProperty(String key, String value) {
        props.add(key);
        props.add(value);
    }

    private String[] finalizeProperties() {
        if (props.isEmpty()) {
            return null;
        }
        return props.toArray(new String[props.size()]);
    }

    private String getSegID() {
        String segID = null;
        switch (altTransIDType) {
        case ELEMENT_ID:
            segID = id;
            break;
        case RESNAME_ATTR:
            segID = resname == null ? id : resname;
            break;
        default:
            // Leave key null
        }
        if (segID != null) {
            segID = ensureUniqueID(segID);
        }
        return segID;
    }

    String ensureUniqueID(String id) {
        int i = 0;
        String tryID;
        while (true) {
            tryID = id + (i == 0 ? "" : "_" + i);
            if (!altIDCache.contains(tryID)) {
                altIDCache.add(tryID);
                return tryID;
            }
            i++;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInIgnored() {
        return ignored;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void text(String text) {
        this.text.append(text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String translate(String entry, List<ProtectedPart> protectedParts) {
        if (entryParseCallback != null) {
            if (!StringUtil.isEmpty(entry)) {
                entryText.add(entry);
                this.protectedParts.add(protectedParts);
            }
            return entry;
        } else if (entryTranslateCallback != null) {
            String translation = StringUtil.isEmpty(entry) ? entry
                    : entryTranslateCallback.getTranslation(getSegID(), entry, null);
            return translation != null ? translation : entry;
        } else {
            return entry;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabledInDefault() {
        return false;
    }
}
