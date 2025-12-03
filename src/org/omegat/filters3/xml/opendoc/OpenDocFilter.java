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
import java.nio.file.Files;
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

import org.jspecify.annotations.Nullable;
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
    private static final Set<String> TRANSLATABLE = new HashSet<>(
            Arrays.asList("content.xml", "styles.xml", "meta.xml"));

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(OpenDocFilter.class);
    }

    public static void unloadPlugins() {
        // there is no way to unload filter.
    }

    @Override
    protected boolean requirePrevNextFields() {
        return true;
    }

    /** Returns true if it's OpenDocument file. */
    @Override
    public boolean isFileSupported(File inFile, Map<String, String> config, FilterContext fc) {
        try (ZipFile file = new ZipFile(inFile)) {
            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (TRANSLATABLE.contains(entry.getName())) {
                    return true;
                }
            }
        } catch (IOException ignored) {
            // ignore exception and return false
            // to indicate not supported
        }
        return false;
    }

    private OpenDocXMLFilter createXMLFilter(Map<String, String> options) {
        OpenDocXMLFilter xmlfilter = new OpenDocXMLFilter();
        xmlfilter.setCallbacks(entryParseCallback, entryTranslateCallback);
        // Defining the actual dialect, because at this step
        // we have the options
        OpenDocDialect dialect = (OpenDocDialect) xmlfilter.getDialect();
        dialect.defineDialect(new OpenDocOptions(options));

        return xmlfilter;
    }

    /**
     * Returns a temporary file for OpenOffice XML. A nasty hack, to say polite
     * way.
     */
    private File tmp() throws IOException {
        return File.createTempFile("ot-oo-", ".xml");
    }

    /**
     * Processes a single OpenDocument file, which is actually a ZIP file
     * consisting of many XML files, some of which should be translated.
     */
    @Override
    public void processFile(File inFile, @Nullable File outFile, FilterContext fc)
            throws IOException, TranslationException {
        try (ZipFile zipFile = new ZipFile(inFile);
                ZipOutputStream zipOut = outFile != null ? new ZipOutputStream(new FileOutputStream(outFile))
                        : null) {

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                processZipEntry(zipFile, entry, zipOut, fc, inFile);
            }
        }
    }

    private void processZipEntry(ZipFile zipFile, ZipEntry entry, @Nullable ZipOutputStream zipOut, FilterContext fc,
            File inFile) throws IOException, TranslationException {
        String shortName = extractShortName(entry.getName());

        if (TRANSLATABLE.contains(shortName)) {
            handleTranslatableEntry(zipFile, entry, zipOut, fc, inFile);
        } else if (zipOut != null) {
            copyUntranslatableEntry(zipFile, entry, zipOut);
        }
    }

    private String extractShortName(String name) {
        int lastIndex = name.lastIndexOf('/');
        return lastIndex >= 0 ? name.substring(lastIndex + 1) : name;
    }

    private void handleTranslatableEntry(ZipFile zipFile, ZipEntry entry, @Nullable ZipOutputStream zipOut,
            FilterContext fc, File inFile) throws IOException, TranslationException {
        File tmpIn = tmp();
        File tmpOut = zipOut != null ? tmp() : null;

        try {
            FileUtils.copyInputStreamToFile(zipFile.getInputStream(entry), tmpIn);
            createXMLFilter(processOptions).processFile(tmpIn, tmpOut, fc);

            if (zipOut != null) {
                writeTranslatableToZip(zipOut, entry, tmpOut);
            }
        } catch (Exception e) {
            throw new TranslationException(
                    e.getLocalizedMessage() + "\n" + OStrings.getString("OpenDoc_ERROR_IN_FILE") + inFile);
        } finally {
            cleanUpTempFile(tmpIn);
            cleanUpTempFile(tmpOut);
        }
    }

    private void writeTranslatableToZip(ZipOutputStream zipOut, ZipEntry entry, File tmpOut)
            throws IOException {
        ZipEntry outEntry = new ZipEntry(entry.getName());
        outEntry.setMethod(ZipEntry.DEFLATED);
        zipOut.putNextEntry(outEntry);
        FileUtils.copyFile(tmpOut, zipOut);
        zipOut.closeEntry();
    }

    private void copyUntranslatableEntry(ZipFile zipFile, ZipEntry entry, ZipOutputStream zipOut)
            throws IOException {
        zipOut.putNextEntry(new ZipEntry(entry.getName()));
        IOUtils.copy(zipFile.getInputStream(entry), zipOut);
        zipOut.closeEntry();
    }

    private void cleanUpTempFile(File file) {
        if (file == null) {
            return;
        }
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            file.deleteOnExit();
        }
    }

    /** Human-readable OpenDocument filter name. */
    @Override
    public String getFileFormatName() {
        return OStrings.getString("OpenDoc_FILTER_NAME");
    }

    /** Extensions... */
    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.sx?"), new Instance("*.st?"), new Instance("*.od?"),
                new Instance("*.ot?"), };
    }

    /** Source encoding can not be varied by the user. */
    @Override
    public boolean isSourceEncodingVariable() {
        return false;
    }

    /** Target encoding can not be varied by the user. */
    @Override
    public boolean isTargetEncodingVariable() {
        return false;
    }

    /** Not implemented. */

    protected void processFile(BufferedReader inFile, BufferedWriter outFile, FilterContext fc)
            throws IOException, TranslationException {
        throw new IOException("Not Implemented!");
    }

    /**
     * Returns true to indicate that the OpenDoc filter has options.
     *
     * @return True, because the OpenDoc filter has options.
     */
    @Override
    public boolean hasOptions() {
        return true;
    }

    /**
     * OpenDoc Filter shows a <b>modal</b> dialog to edit its own options.
     *
     * @param currentOptions
     *            Current options to edit.
     * @return Updated filter options if user confirmed the changes, and current
     *         options otherwise.
     */
    @Override
    public Map<String, String> changeOptions(Window parent, Map<String, String> currentOptions) {
        try {
            EditOpenDocOptionsDialog dialog = new EditOpenDocOptionsDialog(parent, currentOptions);
            dialog.setVisible(true);
            if (EditOpenDocOptionsDialog.RET_OK == dialog.getReturnStatus()) {
                return dialog.getOptions().getOptionsMap();
            }
        } catch (Exception e) {
            Log.logErrorRB(e, "HTML_EXC_EDIT_OPTIONS");
        }
        return null;
    }

    @Override
    public String getInEncodingLastParsedFile() {
        // Encoding is 'binary', it is zipped. Inside there may be many files.
        // It makes no sense to display
        // the encoding of some xml file inside.
        return "OpenDoc";
    }
}
