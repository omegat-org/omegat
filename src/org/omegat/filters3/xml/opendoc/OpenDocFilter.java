/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.filters3.xml.opendoc;

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.LFileCopy;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

/**
 * Filter for Open Document file format.
 * 
 * @author Maxym Mykhalchuk
 */
public class OpenDocFilter extends AbstractFilter {
    private static final Set<String> TRANSLATABLE = new HashSet<String>(Arrays.asList(new String[] {
            "content.xml", "styles.xml", "meta.xml", }));

    /** Creates a new instance of OpenDocFilter */
    public OpenDocFilter() {
    }

    /** Returns true if it's OpenDocument file. */
    public boolean isFileSupported(File inFile, Map<String, String> config, FilterContext fc) {
        try {
            ZipFile file = new ZipFile(inFile);
            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (TRANSLATABLE.contains(entry.getName()))
                    return true;
            }
        } catch (IOException e) {
        }
        return false;
    }

    OpenDocXMLFilter xmlfilter = null;

    private OpenDocXMLFilter createXMLFilter(Map<String, String> options) {
        xmlfilter = new OpenDocXMLFilter();
        xmlfilter.setCallbacks(entryParseCallback, entryTranslateCallback);
        // Defining the actual dialect, because at this step
        // we have the options
        OpenDocDialect dialect = (OpenDocDialect) xmlfilter.getDialect();
        dialect.defineDialect(new OpenDocOptions(options));

        return xmlfilter;
    }

    /**
     * Returns a temporary file for OpenOffice XML. A nasty hack, to say polite way.
     */
    private File tmp() throws IOException {
        return File.createTempFile("ot-oo-", ".xml");
    }

    /**
     * Processes a single OpenDocument file, which is actually a ZIP file consisting of many XML files, some
     * of which should be translated.
     */
    public void processFile(File inFile, File outFile, FilterContext fc) throws IOException,
            TranslationException {
        ZipFile zipfile = new ZipFile(inFile);
        ZipOutputStream zipout = null;
        if (outFile != null)
            zipout = new ZipOutputStream(new FileOutputStream(outFile));
        Enumeration<? extends ZipEntry> zipcontents = zipfile.entries();
        while (zipcontents.hasMoreElements()) {
            ZipEntry zipentry = zipcontents.nextElement();
            String shortname = zipentry.getName();
            if (shortname.lastIndexOf('/') >= 0)
                shortname = shortname.substring(shortname.lastIndexOf('/') + 1);
            if (TRANSLATABLE.contains(shortname)) {
                File tmpin = tmp();
                LFileCopy.copy(zipfile.getInputStream(zipentry), tmpin);
                File tmpout = null;
                if (zipout != null)
                    tmpout = tmp();

                try {
                    createXMLFilter(processOptions).processFile(tmpin, tmpout, fc);
                } catch (Exception e) {
                    throw new TranslationException(e.getLocalizedMessage() + "\n"
                            + OStrings.getString("OpenDoc_ERROR_IN_FILE") + inFile);
                }

                if (zipout != null) {
                    ZipEntry outentry = new ZipEntry(zipentry.getName());
                    outentry.setMethod(ZipEntry.DEFLATED);
                    zipout.putNextEntry(outentry);
                    LFileCopy.copy(tmpout, zipout);
                    zipout.closeEntry();
                }
                if (!tmpin.delete())
                    tmpin.deleteOnExit();
                if (tmpout != null) {
                    if (!tmpout.delete())
                        tmpout.deleteOnExit();
                }
            } else {
                if (zipout != null) {
                    ZipEntry outentry = new ZipEntry(zipentry.getName());
                    zipout.putNextEntry(outentry);
                    LFileCopy.copy(zipfile.getInputStream(zipentry), zipout);
                    zipout.closeEntry();
                }
            }
        }
        if (zipout != null)
            zipout.close();
    }

    /** Human-readable OpenDocument filter name. */
    public String getFileFormatName() {
        return OStrings.getString("OpenDoc_FILTER_NAME");
    }

    /** Extensions... */
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.sx?"), new Instance("*.st?"), new Instance("*.od?"),
                new Instance("*.ot?"), };
    }

    /** Source encoding can not be varied by the user. */
    public boolean isSourceEncodingVariable() {
        return false;
    }

    /** Target encoding can not be varied by the user. */
    public boolean isTargetEncodingVariable() {
        return false;
    }

    /** Not implemented. */
    protected void processFile(BufferedReader inFile, BufferedWriter outFile) throws IOException,
            TranslationException {
        throw new IOException("Not Implemented!");
    }

    /**
     * Returns true to indicate that the OpenDoc filter has options.
     * 
     * @return True, because the OpenDoc filter has options.
     */
    public boolean hasOptions() {
        return true;
    }

    /**
     * OpenDoc Filter shows a <b>modal</b> dialog to edit its own options.
     * 
     * @param currentOptions
     *            Current options to edit.
     * @return Updated filter options if user confirmed the changes, and current options otherwise.
     */
    public Map<String, String> changeOptions(Dialog parent, Map<String, String> currentOptions) {
        try {
            EditOpenDocOptionsDialog dialog = new EditOpenDocOptionsDialog(parent, currentOptions);
            dialog.setVisible(true);
            if (EditOpenDocOptionsDialog.RET_OK == dialog.getReturnStatus())
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
