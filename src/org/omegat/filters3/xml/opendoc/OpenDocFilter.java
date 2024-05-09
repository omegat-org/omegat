/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters3.xml.opendoc;

import java.awt.Window;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.omegat.core.Core;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

/**
 * Filter for Open Document file format.
 *
 * @author Maxym Mykhalchuk
 */
public class OpenDocFilter extends AbstractFilter {
    private static final Set<String> TRANSLATABLE = new HashSet<String>(Arrays.asList("content.xml",
            "styles.xml", "meta.xml"));

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(OpenDocFilter.class);
    }

    public static void unloadPlugins() {
    }

    protected boolean requirePrevNextFields() {
        return true;
    }

    /** Returns true if it's OpenDocument file. */
    public boolean isFileSupported(File inFile, Map<String, String> config, FilterContext fc) {
        try {
            ZipFile file = new ZipFile(inFile);
            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (TRANSLATABLE.contains(entry.getName())) {
                    file.close();
                    return true;
                }
            }
            file.close();
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
    @Override
    public void processFile(File inFile, File outFile, FilterContext fc) throws IOException,
            TranslationException {
        ZipFile zipfile = new ZipFile(inFile);
        ZipOutputStream zipout = null;
        if (outFile != null) {
            zipout = new ZipOutputStream(new FileOutputStream(outFile));
        }
        Enumeration<? extends ZipEntry> zipcontents = zipfile.entries();
        while (zipcontents.hasMoreElements()) {
            ZipEntry zipentry = zipcontents.nextElement();
            String shortname = zipentry.getName();
            if (shortname.lastIndexOf('/') >= 0) {
                shortname = shortname.substring(shortname.lastIndexOf('/') + 1);
            }
            if (TRANSLATABLE.contains(shortname)) {
                File tmpin = tmp();
                FileUtils.copyInputStreamToFile(zipfile.getInputStream(zipentry), tmpin);
                File tmpout = null;
                if (zipout != null) {
                    tmpout = tmp();
                }
                try {
                    createXMLFilter(processOptions).processFile(tmpin, tmpout, fc);
                } catch (Exception e) {
                    zipfile.close();
                    throw new TranslationException(e.getLocalizedMessage() + "\n"
                            + OStrings.getString("OpenDoc_ERROR_IN_FILE") + inFile);
                }

                if (zipout != null) {
                    ZipEntry outentry = new ZipEntry(zipentry.getName());
                    outentry.setMethod(ZipEntry.DEFLATED);
                    zipout.putNextEntry(outentry);
                    FileUtils.copyFile(tmpout, zipout);
                    zipout.closeEntry();
                }
                if (!tmpin.delete()) {
                    tmpin.deleteOnExit();
                }
                if (tmpout != null) {
                    if (!tmpout.delete()) {
                        tmpout.deleteOnExit();
                    }
                }
            } else {
                if (zipout != null) {
                    ZipEntry outentry = new ZipEntry(zipentry.getName());
                    zipout.putNextEntry(outentry);
                    IOUtils.copy(zipfile.getInputStream(zipentry), zipout);
                    zipout.closeEntry();
                }
            }
        }
        if (zipout != null) {
            zipout.close();
        }
        zipfile.close();
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
    protected void processFile(BufferedReader inFile, BufferedWriter outFile, FilterContext fc) throws IOException,
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
    public Map<String, String> changeOptions(Window parent, Map<String, String> currentOptions) {
        try {
            EditOpenDocOptionsDialog dialog = new EditOpenDocOptionsDialog(parent, currentOptions);
            dialog.setVisible(true);
            if (EditOpenDocOptionsDialog.RET_OK == dialog.getReturnStatus()) {
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

    @Override
    public String getInEncodingLastParsedFile() {
        // Encoding is 'binary', it is zipped. Inside there may be many files. It makes no sense to display
        // the encoding of some xml file inside.
        return "OpenDoc";
    }
}
