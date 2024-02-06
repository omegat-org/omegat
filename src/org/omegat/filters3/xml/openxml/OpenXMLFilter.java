/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2013 Didier Briel
               2015-2016 Didier Briel
               2019 Didier Briel
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

package org.omegat.filters3.xml.openxml;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 * Filter for Open XML file format.
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class OpenXMLFilter extends AbstractFilter {
    private static final Logger LOGGER = Logger.getLogger(OpenXMLFilter.class.getName());
    private String documents;
    private Pattern translatable;
    private static final Pattern DIGITS = Pattern.compile("(\\d+)\\.xml");

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(OpenXMLFilter.class);
    }

    public static void unloadPlugins() {
    }

    /**
     * Defines the documents to read according to options
     */
    private void defineDOCUMENTSOptions(Map<String, String> config) {
        /*
         Complete string when all options are enabled
         Word
         "(document\\d?\\.xml)|(comments\\.xml)|(footnotes\\.xml)|(endnotes\\.xml)|(header\\d+\\.xml)
         |(footer\\d+\\.xml)|(core\\.xml)"
         Excel
         "|(sharedStrings\\.xml)|(comments\\d+\\.xml)"
         PowerPoint
         "|(slide\\d+\\.xml)|(slideMaster\\d+\\.xml)| (slideLayout\\d+\\.xml)|(notesSlide\\d+\\.xml)"
         Global
         "|(data\\d+\\.xml)|(chart\\d+\\.xml)|(drawing\\d+\\.xml)"
         Excel
         "|(workbook\\.xml)"
         Visio
         "|(page\\d+\\.xml)
        */

        StringBuilder sb = new StringBuilder("(document\\d?\\.xml)");

        OpenXMLOptions options = new OpenXMLOptions(config);

        if (options.getTranslateComments()) {
            sb.append("|(comments\\.xml)");
        }
        if (options.getTranslateFootnotes()) {
            sb.append("|(footnotes\\.xml)");
        }
        if (options.getTranslateEndnotes()) {
            sb.append("|(endnotes\\.xml)");
        }
        if (options.getTranslateHeaders()) {
            sb.append("|(header\\d+\\.xml)");
        }
        if (options.getTranslateFooters()) {
            sb.append("|(footer\\d+\\.xml)");
        }
        if (options.getTranslateDocumentProperties()) {
            sb.append("|(core\\.xml)");
        }
        sb.append("|(sharedStrings\\.xml)");
        if (options.getTranslateExcelComments()) {
            sb.append("|(comments\\d+\\.xml)");
        }
        sb.append("|(slide\\d+\\.xml)");
        if (options.getTranslateSlideMasters()) {
            sb.append("|(slideMaster\\d+\\.xml)");
        }
        if (options.getTranslateSlideLayouts()) {
            sb.append("|(slideLayout\\d+\\.xml)");
        }
        if (options.getTranslateSlideComments()) {
            sb.append("|(notesSlide\\d+\\.xml)");
        }
        if (options.getTranslateDiagrams()) {
            sb.append("|(data\\d+\\.xml)");
        }
        if (options.getTranslateCharts()) {
            sb.append("|(chart\\d+\\.xml)");
        }
        if (options.getTranslateDrawings()) {
            sb.append("|(drawing\\d+\\.xml)");
        }
        if (options.getTranslateSheetNames()) {
            sb.append("|(workbook\\.xml)");
        }
        if (options.getTranslateLinks()) {
            sb.append("|(\\w+\\d*\\.xml\\.rels)");
        }
        sb.append("|(page\\d+\\.xml)");
        documents = sb.toString();

        translatable = Pattern.compile(documents);
    }

    @Override
    protected boolean requirePrevNextFields() {
        return true;
    }

    /** Returns true if it's an Open XML file. */
    @Override
    public boolean isFileSupported(File inFile, Map<String, String> config, FilterContext fc) {
        defineDOCUMENTSOptions(config); // Define the documents to read

        try (ZipFile file = new ZipFile(inFile)) {
            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String shortname = entry.getName();
                shortname = removePath(shortname);
                Matcher filematch = translatable.matcher(shortname);
                if (filematch.matches()) {
                    return true;
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Error checking if file is supported by OpenXMLFilter: " + inFile, e);
        }
        return false;
    }

    private OpenXMLXMLFilter createXMLFilter() {
        OpenXMLXMLFilter xmlfilter = new OpenXMLXMLFilter();
        xmlfilter.setCallbacks(entryParseCallback, entryTranslateCallback);
        // Defining the actual dialect, because at this step
        // we have the options
        OpenXMLDialect dialect = (OpenXMLDialect) xmlfilter.getDialect();
        dialect.defineDialect(new OpenXMLOptions(processOptions));

        return xmlfilter;
    }

    /** Returns a temporary file for Open XML. A nasty hack, to say polite way. */
    private static File tmp() throws IOException {
        return File.createTempFile("o-xml-temp", ".xml");
    }

    /**
     * @param fileName A filename with a path
     * @return A string without the path
     */
    private static String removePath(String fileName) {
        if (fileName.lastIndexOf('/') >= 0) {
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        }  else if (fileName.lastIndexOf('\\') >= 0) { // Some weird files may use a backslash
            fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
        }
        return fileName;
    }

    /**
     * @param fileName A filename
     * @return The filename without an .xml extension if found in it
     */
    private static String removeXML(String fileName) {
        if (fileName.endsWith(".xml")) {
            fileName = fileName.substring(0, fileName.lastIndexOf(".xml"));
        }
        return fileName;
    }

    /**
     * Processes a single OpenXML file, which is actually a ZIP file consisting of many XML files, some of
     * which should be translated.
     */
    @Override
    public void processFile(File inFile, File outFile, FilterContext fc) throws IOException,
            TranslationException {
        defineDOCUMENTSOptions(processOptions); // Define the documents to read

        ZipOutputStream zipout = null;
        try (ZipFile zipfile = new ZipFile(inFile)) {
            if (outFile != null) {
                zipout = new ZipOutputStream(new FileOutputStream(outFile));
            }
            Enumeration<? extends ZipEntry> unsortedZipcontents = zipfile.entries();
            List<? extends ZipEntry> filelist = Collections.list(unsortedZipcontents);
            // Sort filenames, because zipfile.entries give a random order
            // We use a simplified natural sort, to have slide1, slide2 ...
            // slide10
            // instead of slide1, slide10, slide 2
            // We also order files arbitrarily, to have, for instance
            // documents.xml before comments.xml
            Collections.sort(filelist, this::compareZipEntries);

            for (ZipEntry zipentry : filelist) {
                String shortname = removePath(zipentry.getName());
                if (translatable.matcher(shortname).matches()) {
                    File tmpin = tmp();
                    FileUtils.copyInputStreamToFile(zipfile.getInputStream(zipentry), tmpin);
                    File tmpout = null;
                    if (zipout != null) {
                        tmpout = tmp();
                    }
                    try {
                        createXMLFilter().processFile(tmpin, tmpout, fc);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
                        throw new TranslationException(e.getLocalizedMessage() + "\n"
                                + OStrings.getString("OpenXML_ERROR_IN_FILE") + inFile, e);
                    }

                    if (zipout != null) {
                        ZipEntry outEntry = new ZipEntry(zipentry.getName());
                        zipout.putNextEntry(outEntry);
                        FileUtils.copyFile(tmpout, zipout);
                        zipout.closeEntry();
                    }
                    if (!tmpin.delete()) {
                        tmpin.deleteOnExit();
                    }
                    if (tmpout != null && !tmpout.delete()) {
                        tmpout.deleteOnExit();
                    }
                } else {
                    if (zipout != null) {
                        ZipEntry outEntry = new ZipEntry(zipentry.getName());
                        zipout.putNextEntry(outEntry);
                        try (InputStream is = zipfile.getInputStream(zipentry)) {
                            IOUtils.copy(is, zipout);
                        }
                        zipout.closeEntry();
                    }
                }
            }
        } finally {
            if (zipout != null) {
                zipout.close();
            }
        }
    }

    public int compareZipEntries(ZipEntry z1, ZipEntry z2) {
        String s1 = z1.getName();
        String s2 = z2.getName();
        String[] words1 = s1.split("\\d+\\.");
        String[] words2 = s2.split("\\d+\\.");
        // Digits at the end and same text
        if ((words1.length > 1 && words2.length > 1) && // Digits
                (words1[0].equals(words2[0]))) { // Same text
            int number1 = 0;
            int number2 = 0;
            Matcher getDigits = DIGITS.matcher(s1);
            if (getDigits.find()) {
                number1 = Integer.parseInt(getDigits.group(1));
            }
            getDigits = DIGITS.matcher(s2);
            if (getDigits.find()) {
                number2 = Integer.parseInt(getDigits.group(1));
            }
            if (number1 > number2) {
                return 1;
            } else if (number1 < number2) {
                return -1;
            } else {
                return 0;
            }
        } else {
            String shortname1 = removePath(words1[0]);
            shortname1 = removeXML(shortname1);
            String shortname2 = removePath(words2[0]);
            shortname2 = removeXML(shortname2);

            // Specific case for Excel
            // because "comments" is present twice in DOCUMENTS
            if (shortname1.indexOf("sharedStrings") >= 0 || shortname2.indexOf("sharedStrings") >= 0) {
                if (shortname2.indexOf("sharedStrings") >= 0) {
                    return 1; // sharedStrings must be first
                } else {
                    return -1;
                }
            }

            int index1 = documents.indexOf(shortname1);
            int index2 = documents.indexOf(shortname2);

            if (index1 > index2) {
                return 1;
            } else if (index1 < index2) {
                return -1;
            } else { // Documents were not in DOCUMENTS, we keep the normal order
                return s1.compareTo(s2);
            }
        }
    }

    /** Human-readable Open XML filter name. */
    @Override
    public String getFileFormatName() {
        return OStrings.getString("OpenXML_FILTER_NAME");
    }

    /** Extensions... */
    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] {
            new Instance("*.doc?"),
            new Instance("*.dotx"),
            new Instance("*.xls?"),
            new Instance("*.ppt?"),
            new Instance("*.vsdx")
        };
    }

    /** Source encoding cannot be varied by the user. */
    @Override
    public boolean isSourceEncodingVariable() {
        return false;
    }

    /** Target encoding cannot be varied by the user. */
    @Override
    public boolean isTargetEncodingVariable() {
        return false;
    }

    /** Not implemented. */
    @Override
    protected void processFile(BufferedReader inFile, BufferedWriter outFile, FilterContext fc) throws IOException,
            TranslationException {
        throw new IOException("Not Implemented!");
    }

    /**
     * Returns true to indicate that the OpenXML filter has options.
     *
     * @return True, because the OpenXML filter has options.
     */
    @Override
    public boolean hasOptions() {
        return true;
    }

    /**
     * OpenXML Filter shows a <b>modal</b> dialog to edit its own options.
     *
     * @param currentOptions
     *            Current options to edit.
     * @return Updated filter options if user confirmed the changes, and current options otherwise.
     */
    @Override
    public Map<String, String> changeOptions(Window parent, Map<String, String> currentOptions) {
        try {
            EditOpenXMLOptionsDialog dialog = new EditOpenXMLOptionsDialog(parent, currentOptions);
            dialog.setVisible(true);
            if (EditOpenXMLOptionsDialog.RET_OK == dialog.getReturnStatus()) {
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
        // Encoding is 'binary', it is zipped. Inside there may be many files.
        // It makes no sense to display the encoding of some xml file inside.
        return "OpenXML";
    }
}
