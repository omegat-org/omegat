/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2018 Thomas Cordonnier
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

package org.omegat.filters4.xml.openxml;

import java.awt.Window;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters3.xml.openxml.EditOpenXMLOptionsDialog;
import org.omegat.filters3.xml.openxml.OpenXMLOptions;
import org.omegat.filters4.AbstractZipFilter;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

/**
 * Filter for Microsoft Open XML.
 *
 * @author Thomas Cordonnier
 */
public class MsOfficeFileFilter extends AbstractZipFilter {
    private String DOCUMENTS;
    private Pattern TRANSLATABLE;
    private static final Pattern DIGITS = Pattern.compile("(\\d+)\\.xml");

    /**
     * Defines the documents to read according to options
     */
    private void defineDOCUMENTSOptions(Map<String, String> config) {
        /*
         * Complete string when all options are enabled.
         * Word: "(document\\.xml)|(comments\\.xml)|(footnotes\\.xml)|(endnotes\\.xml)
         * |(header\\d+\\.xml)| (footer\\d+\\.xml)"
         * Excel: "|(sharedStrings\\.xml)|(comments\\d+\\.xml)"
         * PowerPoint: "|(slide\\d+\\.xml)|(slideMaster\\d+\\.xml)| (slideLayout\\d+\\.xml)|
         * (notesSlide\\d+\\.xml)"
         * Global: "|(data\\d+\\.xml)|(chart\\d+\\.xml)|(drawing\\d+\\.xml)"
         * Excel: "|(workbook\\.xml)" Visio: "|(page\\d+\\.xml)
         */

        DOCUMENTS = "(document\\d?\\.xml)";

        OpenXMLOptions options = new OpenXMLOptions(config);

        if (options.getTranslateComments()) {
            DOCUMENTS += "|(comments\\.xml)";
        }
        if (options.getTranslateFootnotes()) {
            DOCUMENTS += "|(footnotes\\.xml)";
        }
        if (options.getTranslateEndnotes()) {
            DOCUMENTS += "|(endnotes\\.xml)";
        }
        if (options.getTranslateHeaders()) {
            DOCUMENTS += "|(header\\d+\\.xml)";
        }
        if (options.getTranslateFooters()) {
            DOCUMENTS += "|(footer\\d+\\.xml)";
        }
        DOCUMENTS += "|(sharedStrings\\.xml)";
        if (options.getTranslateExcelComments()) {
            DOCUMENTS += "|(comments\\d+\\.xml)";
        }
        DOCUMENTS += "|(slide\\d+\\.xml)";
        if (options.getTranslateSlideMasters()) {
            DOCUMENTS += "|(slideMaster\\d+\\.xml)";
        }
        if (options.getTranslateSlideLayouts()) {
            DOCUMENTS += "|(slideLayout\\d+\\.xml)";
        }
        if (options.getTranslateSlideComments()) {
            DOCUMENTS += "|(notesSlide\\d+\\.xml)";
        }
        if (options.getTranslateDiagrams()) {
            DOCUMENTS += "|(data\\d+\\.xml)";
        }
        if (options.getTranslateCharts()) {
            DOCUMENTS += "|(chart\\d+\\.xml)";
        }
        if (options.getTranslateDrawings()) {
            DOCUMENTS += "|(drawing\\d+\\.xml)";
        }
        if (options.getTranslateSheetNames()) {
            DOCUMENTS += "|(workbook\\.xml)";
        }
        // if (options.getTranslateSlideLinks()) DOCUMENTS +=
        // "|(slide\\d+\\.xml\\.rels)";

        DOCUMENTS += "|(page\\d+\\.xml)";

        TRANSLATABLE = Pattern.compile(DOCUMENTS);
    }

    public MsOfficeFileFilter() {
        super();
        defineDOCUMENTSOptions(new HashMap<String, String>()); // Define the
                                                               // documents to
                                                               // read
    }

    @Override
    public boolean isFileSupported(java.io.File inFile, Map<String, String> config, FilterContext context) {
        defineDOCUMENTSOptions(config); // Define the documents to read
        return super.isFileSupported(inFile, config, context);
    }

    @Override
    public String getFileFormatName() {
        return OStrings.getString("MSOFFICE4_FILTER_NAME");
    }

    @Override
    protected boolean requirePrevNextFields() {
        return true;
    }

    @Override
    protected boolean acceptInternalFile(ZipEntry entry, FilterContext fc) {
        return entry.getName().endsWith("document.xml") // Word
                || entry.getName().endsWith("document2.xml") // Word 365
                || entry.getName().endsWith("sharedStrings.xml") // Excel
                || entry.getName().endsWith("slide1.xml") // Powerpoint
                || entry.getName().endsWith("page1.xml"); // Visio
    }

    @Override
    protected boolean mustTranslateInternalFile(ZipEntry entry, boolean writeMode, FilterContext fc) {
        if (writeMode && entry.getName().contains("word") && entry.getName().contains("styles")) {
            return true;
        }
        return TRANSLATABLE.matcher(removePath(entry.getName())).matches();
    }

    /**
     * If comments are not selected, their references are removed in document so
     * better remove the file
     **/
    protected boolean mustDeleteInternalFile(ZipEntry entry, boolean writeMode, FilterContext context) {
        if (entry.getName().endsWith("comments.xml")) {
            return !DOCUMENTS.contains("comments");
        }
        return false;
    }

    private String removePath(String fileName) {
        if (fileName.lastIndexOf('/') >= 0) {
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        }
        if (fileName.lastIndexOf('\\') >= 0) {
            // Some weird files may use a backslash
            fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
        }
        return fileName;
    }

    @Override
    protected java.util.Comparator<ZipEntry> getEntryComparator() {
        /** Same order as in Filter3 **/
        return (ZipEntry z1, ZipEntry z2) -> {
            String s1 = z1.getName(), s2 = z2.getName();
            String[] words1 = s1.split("\\d+\\."), words2 = s2.split("\\d+\\.");
            // Digits at the end and same text
            if ((words1.length > 1 && words2.length > 1) && // Digits
            (words1[0].equals(words2[0]))) { // Same text
                int number1 = 0, number2 = 0;
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
                String shortname1 = removePath(words1[0]), shortname2 = removePath(words2[0]);

                // Specific case for Excel
                // because "comments" is present twice in DOCUMENTS
                if (shortname1.indexOf("sharedStrings") >= 0 || shortname2.indexOf("sharedStrings") >= 0) {
                    if (shortname2.indexOf("sharedStrings") >= 0)
                        return 1; // sharedStrings must be first
                    else {
                        return -1;
                    }
                }

                if (shortname1.endsWith(".xml")) {
                    shortname1 = shortname1.substring(0, shortname1.lastIndexOf('.'));
                }
                if (shortname2.endsWith(".xml")) {
                    shortname2 = shortname2.substring(0, shortname2.lastIndexOf('.'));
                }
                int index1 = DOCUMENTS.indexOf(shortname1), index2 = DOCUMENTS.indexOf(shortname2);
                if (index1 > index2) {
                    return 1;
                } else if (index1 < index2) {
                    return -1;
                } else {
                    return s1.compareTo(s2); // Documents were not in DOCUMENTS,
                                             // we keep the normal order
                }
            }
        };
    }

    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.doc?"), new Instance("*.dotx"), new Instance("*.xls?"),
                new Instance("*.ppt?"), new Instance("*.vsdx") };
    }

    public boolean hasOptions() {
        return true;
    }

    /**
     * OpenXML Filter shows a <b>modal</b> dialog to edit its own options.
     *
     * @param currentOptions
     *            Current options to edit.
     * @return Updated filter options if user confirmed the changes, and current
     *         options otherwise.
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
    protected OpenXmlFilter getFilter(ZipEntry ze) {
        OpenXmlFilter filter = new OpenXmlFilter(DOCUMENTS.contains("comments"));
        filter.setCallbacks(entryParseCallback, entryTranslateCallback);
        return filter;
    }
}
