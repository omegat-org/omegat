/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2012 Didier Briel
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.filters3.xml.openxml;

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 * Filter for Open XML file format.
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class OpenXMLFilter extends AbstractFilter {
    private String DOCUMENTS;
    private Pattern TRANSLATABLE;
    private static final Pattern DIGITS = Pattern.compile("(\\d+)\\.xml");

    /**
     * Defines the documents to read according to options
     */
    private void defineDOCUMENTSOptions(Map<String, String> config) {
        /*
         Complete string when all options are enabled 
         Word
         "(document\\.xml)|(comments\\.xml)|(footnotes\\.xml)|(endnotes\\.xml)|(header\\d+\\.xml)
         |(footer\\d+\\.xml)"
         Excel 
         "|(sharedStrings\\.xml)|(comments\\d+\\.xml)" 
         PowerPoint 
         "|(slide\\d+\\.xml)|(slideMaster\\d+\\.xml)| (slideLayout\\d+\\.xml)|(notesSlide\\d+\\.xml)"
         Global
         "|(data\\d+\\.xml)|(chart\\d+\\.xml)|(drawing\\d+\\.xml)"
         Excel 
         "|(workbook\\.xml)"
         */

        DOCUMENTS = "(document\\.xml)";

        OpenXMLOptions options = new OpenXMLOptions(config);

        if (options.getTranslateComments())
            DOCUMENTS += "|(comments\\.xml)";
        if (options.getTranslateFootnotes())
            DOCUMENTS += "|(footnotes\\.xml)";
        if (options.getTranslateEndnotes())
            DOCUMENTS += "|(endnotes\\.xml)";
        if (options.getTranslateHeaders())
            DOCUMENTS += "|(header\\d+\\.xml)";
        if (options.getTranslateFooters())
            DOCUMENTS += "|(footer\\d+\\.xml)";
        DOCUMENTS += "|(sharedStrings\\.xml)";
        if (options.getTranslateExcelComments())
            DOCUMENTS += "|(comments\\d+\\.xml)";
        DOCUMENTS += "|(slide\\d+\\.xml)";
        if (options.getTranslateSlideMasters())
            DOCUMENTS += "|(slideMaster\\d+\\.xml)";
        if (options.getTranslateSlideLayouts())
            DOCUMENTS += "|(slideLayout\\d+\\.xml)";
        if (options.getTranslateSlideComments())
            DOCUMENTS += "|(notesSlide\\d+\\.xml)";
        if (options.getTranslateDiagrams())
            DOCUMENTS += "|(data\\d+\\.xml)";
        if (options.getTranslateCharts())
            DOCUMENTS += "|(chart\\d+\\.xml)";
        if (options.getTranslateDrawings())
            DOCUMENTS += "|(drawing\\d+\\.xml)";
        if (options.getTranslateSheetNames())
            DOCUMENTS += "|(workbook\\.xml)";

        TRANSLATABLE = Pattern.compile(DOCUMENTS);
    }

    protected boolean requirePrevNextFields() {
        return true;
    }

    /** Returns true if it's an Open XML file. */
    @Override
    public boolean isFileSupported(File inFile, Map<String, String> config, FilterContext fc) {
        try {
            defineDOCUMENTSOptions(config); // Define the documents to read

            ZipFile file = new ZipFile(inFile);
            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String shortname = entry.getName();
                shortname = removePath(shortname);
                Matcher filematch = TRANSLATABLE.matcher(shortname);
                if (filematch.matches())
                    return true;
            }
        } catch (IOException e) {
        }
        return false;
    }

    OpenXMLXMLFilter xmlfilter = null;

    private OpenXMLXMLFilter createXMLFilter() {
        xmlfilter = new OpenXMLXMLFilter();
        xmlfilter.setCallbacks(entryParseCallback, entryTranslateCallback);
        // Defining the actual dialect, because at this step
        // we have the options
        OpenXMLDialect dialect = (OpenXMLDialect) xmlfilter.getDialect();
        dialect.defineDialect(new OpenXMLOptions(processOptions));

        return xmlfilter;
    }

    /** Returns a temporary file for Open XML. A nasty hack, to say polite way. */
    private File tmp() throws IOException {
        return File.createTempFile("o-xml-temp", ".xml");
    }

    /**
     * @param fileName A filename with a path
     * @return A string without the path
     */
    private String removePath(String fileName) {
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
    private String removeXML(String fileName) {
        if (fileName.endsWith(".xml"))
            fileName = fileName.substring(0, fileName.lastIndexOf(".xml"));
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

        ZipFile zipfile = new ZipFile(inFile);
        ZipOutputStream zipout = null;
        if (outFile != null)
            zipout = new ZipOutputStream(new FileOutputStream(outFile));
        Enumeration<? extends ZipEntry> unsortedZipcontents = zipfile.entries();
        List<? extends ZipEntry> filelist = Collections.list(unsortedZipcontents);
        Collections.sort(filelist, new Comparator<ZipEntry>() {
            // Sort filenames, because zipfile.entries give a random order
            // We use a simplified natural sort, to have slide1, slide2 ...
            // slide10
            // instead of slide1, slide10, slide 2
            // We also order files arbitrarily, to have, for instance
            // documents.xml before comments.xml
            public int compare(ZipEntry z1, ZipEntry z2) {
                String s1 = z1.getName();
                String s2 = z2.getName();
                String[] words1 = s1.split("\\d+\\.");
                String[] words2 = s2.split("\\d+\\.");
                // Digits at the end and same text
                if ((words1.length > 1 && words2.length > 1) && // Digits
                        (words1[0].equals(words2[0]))) // Same text
                {
                    int number1 = 0;
                    int number2 = 0;
                    Matcher getDigits = DIGITS.matcher(s1);
                    if (getDigits.find())
                        number1 = Integer.parseInt(getDigits.group(1));
                    getDigits = DIGITS.matcher(s2);
                    if (getDigits.find())
                        number2 = Integer.parseInt(getDigits.group(1));
                    if (number1 > number2)
                        return 1;
                    else if (number1 < number2)
                        return -1;
                    else
                        return 0;
                } else {
                    String shortname1 = removePath(words1[0]);
                    shortname1 = removeXML(shortname1);
                    String shortname2 = removePath(words2[0]);
                    shortname2 = removeXML(shortname2);

                    // Specific case for Excel
                    // because "comments" is present twice in DOCUMENTS
                    if (shortname1.indexOf("sharedStrings") >= 0 || shortname2.indexOf("sharedStrings") >= 0) {
                        if (shortname2.indexOf("sharedStrings") >= 0)
                            return 1; // sharedStrings must be first
                        else
                            return -1;
                    }

                    int index1 = DOCUMENTS.indexOf(shortname1);
                    int index2 = DOCUMENTS.indexOf(shortname2);

                    if (index1 > index2)
                        return 1;
                    else if (index1 < index2)
                        return -1;
                    else { // Documents were not in DOCUMENTS, we keep the normal order
                        return s1.compareTo(s2);
                    }
                }
            }
        });
        Enumeration<? extends ZipEntry> zipcontents = Collections.enumeration(filelist);

        while (zipcontents.hasMoreElements()) {
            ZipEntry zipentry = zipcontents.nextElement();
            String shortname = zipentry.getName();
            shortname = removePath(shortname);
            Matcher filematch = TRANSLATABLE.matcher(shortname);
            if (filematch.matches()) {
                File tmpin = tmp();
                LFileCopy.copy(zipfile.getInputStream(zipentry), tmpin);
                File tmpout = null;
                if (zipout != null)
                    tmpout = tmp();

                try {
                    createXMLFilter().processFile(tmpin, tmpout, fc);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new TranslationException(e.getLocalizedMessage() + "\n"
                            + OStrings.getString("OpenXML_ERROR_IN_FILE") + inFile, e);
                }

                if (zipout != null) {
                    ZipEntry outEntry = new ZipEntry(zipentry.getName());
                    zipout.putNextEntry(outEntry);
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
                    ZipEntry outEntry = new ZipEntry(zipentry.getName());
                    zipout.putNextEntry(outEntry);

                    LFileCopy.copy(zipfile.getInputStream(zipentry), zipout);
                    zipout.closeEntry();
                }
            }
        }
        if (zipout != null)
            zipout.close();
    }

    /** Human-readable Open XML filter name. */
    public String getFileFormatName() {
        return OStrings.getString("OpenXML_FILTER_NAME");
    }

    /** Extensions... */
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.doc?"), new Instance("*.xls?"), new Instance("*.ppt?"), };
    }

    /** Source encoding cannot be varied by the user. */
    public boolean isSourceEncodingVariable() {
        return false;
    }

    /** Target encoding cannot be varied by the user. */
    public boolean isTargetEncodingVariable() {
        return false;
    }

    /** Not implemented. */
    protected void processFile(BufferedReader inFile, BufferedWriter outFile) throws IOException,
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
    public Map<String, String> changeOptions(Dialog parent, Map<String, String> currentOptions) {
        try {
            EditOpenXMLOptionsDialog dialog = new EditOpenXMLOptionsDialog(parent, currentOptions);
            dialog.setVisible(true);
            if (EditOpenXMLOptionsDialog.RET_OK == dialog.getReturnStatus())
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
