/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik, Martin Fleurke
               2009 Martin Fleurke
               2013 Aaron Madlon-Kay, Alex Buloichik
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

package org.omegat.core.tagvalidation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omegat.core.Core;
import org.omegat.core.data.DataUtils;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.tagvalidation.ErrorReport.TagError;
import org.omegat.filters2.po.PoFilter;
import org.omegat.util.Preferences;
import org.omegat.util.StreamUtil;
import org.omegat.util.TagUtil.Tag;

/**
 * Class for show tag validation results.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Aaron Madlon-Kay
 */
public class TagValidationTool implements ITagValidation {

    static final String ALL_FILES_PATTERN = ".*";

    @Override
    public synchronized void logTagValidationErrors(List<ErrorReport> suspects) {
        if (suspects != null && !suspects.isEmpty()) {
            for (ErrorReport report : suspects) {
                System.out.println(report.entryNum);
                System.out.println(report.source);
                System.out.println(report.translation);
                for (Map.Entry<TagError, List<Tag>> e : report.inverseReport().entrySet()) {
                    System.out.print("  ");
                    System.out.print(ErrorReport.localizedTagError(e.getKey()));
                    System.out.print(": ");
                    for (Tag tag : e.getValue()) {
                        System.out.print(tag);
                        System.out.print(" ");
                    }
                    System.out.println();
                }
            }
        }
    }

    /**
     * Scans project and builds the list of entries which are suspected of
     * having changed (possibly invalid) tag structures.
     * <p>
     * Duplicate entries that are not "alternative" translations are filtered
     * from the results.
     */
    @Override
    public List<ErrorReport> listInvalidTags() {
        return listInvalidTags(ALL_FILES_PATTERN);
    }

    /**
     * Scans project and builds the list of entries which are suspected of
     * having changed (possibly invalid) tag structures from specified files
     * corresponding to sourcePattern.
     * <p>
     * Duplicate entries that are not "alternative" translations are filtered
     * from the results.
     */
    @Override
    public List<ErrorReport> listInvalidTags(String sourcePattern) {
        return Core.getProject().getProjectFiles().stream()
                .filter(StreamUtil.patternFilter(sourcePattern, fi -> fi.filePath))
                .flatMap(fi -> fi.entries.stream().map(ste -> {
                    TMXEntry te = Core.getProject().getTranslationInfo(ste);
                    if (sourcePattern.equals(ALL_FILES_PATTERN) && DataUtils.isDuplicate(ste, te)) {
                        return null;
                    } else {
                        return checkEntry(fi, ste, te);
                    }
                })).filter(report -> report != null && !report.isEmpty()).collect(Collectors.toList());
    }

    @Override
    public boolean checkInvalidTags(SourceTextEntry ste) {
        Optional<Boolean> result = Core.getProject().getProjectFiles().stream().filter(fi -> fi.entries.contains(ste))
                .findFirst().map(fi -> checkEntry(fi, ste, Core.getProject().getTranslationInfo(ste)).isEmpty());
        if (result.isPresent()) {
            return result.get();
        } else {
            throw new RuntimeException("Invalid SourceTextEntry storage for tag validation");
        }
    }

    /**
     * Checks entry for valid tags.
     *
     * @return An {@link ErrorReport} summarizing the results (will be empty if
     *         no issues found)
     */
    private ErrorReport checkEntry(FileInfo fi, SourceTextEntry ste, TMXEntry te) {

        ErrorReport report = new ErrorReport(ste, te);

        // if there's no translation, skip the string bugfix for:
        // https://sourceforge.net/p/omegat/bugs/64/
        if (!te.isTranslated() || ste.getSrcText().isEmpty()) {
            return report;
        }

        // Check printf variables
        if (Preferences.isPreference(Preferences.CHECK_ALL_PRINTF_TAGS)) {
            TagValidation.inspectPrintfVariables(false, report);
        } else if (Preferences.isPreference(Preferences.CHECK_SIMPLE_PRINTF_TAGS)) {
            TagValidation.inspectPrintfVariables(true, report);
        }

        // Extra checks for PO files:
        if (fi.filterClass.equals(PoFilter.class)) {
            TagValidation.inspectPOWhitespace(report);
        }

        TagValidation.inspectOmegaTTags(ste, report);

        if (Preferences.isPreference(Preferences.CHECK_JAVA_PATTERN_TAGS)) {
            TagValidation.inspectJavaMessageFormat(report);
        }

        TagValidation.inspectRemovePattern(report);

        return report;
    }

    /**
     * Fix all errors indicated in a given ErrorReport.
     *
     * @param report
     *            The report indicating the segment and errors to fix
     * @return The fixed translation string, or null if one of the errors is of
     *         type UNSPECIFIED.
     */
    public static String fixErrors(ErrorReport report) {
        // Don't try to fix unspecified errors.
        if (report.srcErrors.containsValue(TagError.UNSPECIFIED)
                || report.transErrors.containsValue(TagError.UNSPECIFIED)) {
            return null;
        }

        StringBuilder sb = new StringBuilder(report.translation);

        Stream.of(report.srcErrors, report.transErrors).flatMap(m -> m.entrySet().stream())
                .sorted(Comparator.comparing(e -> e.getKey().pos)).forEach(e -> {
                    TagRepair.fixTag(report.ste, e.getKey(), e.getValue(), sb, report.source);
                });

        return sb.toString();
    }
}
