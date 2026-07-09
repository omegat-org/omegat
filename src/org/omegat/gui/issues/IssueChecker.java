/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
         with fuzzy matching, translation memory, keyword search,
         glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 OmegaT contributors
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

package org.omegat.gui.issues;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omegat.core.Core;
import org.omegat.core.data.DataUtils;
import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.StreamUtil;

/**
 * Centralized Issues checker that aggregates Tag issues and provider issues.
 * All issue checks in the application should go through this class, so UI
 * controllers only display results.
 */
public final class IssueChecker {

    private IssueChecker() {
    }

    /**
     * Collect all issues (tag validation + all enabled providers) filtered by file pattern.
     *
     * @param filePattern       Java regex pattern for file paths (as used in project entries)
     * @param filterDuplicates  when true, duplicate entries (same source) are skipped similar to
     *                          the Issues panel behavior when showing all files
     * @return list of issues
     */
    public static List<IIssue> collectIssues(String filePattern, boolean filterDuplicates) {
        Stream<IIssue> tagErrors = Core.getTagValidation().listInvalidTags(filePattern).stream()
                .map(TagIssue::new);

        List<IIssueProvider> providers = IssueProviders.getEnabledProviders();
        Stream<IIssue> providerIssues = getProviderIssues(providers, filePattern, filterDuplicates);

        return Stream.concat(tagErrors, providerIssues).collect(Collectors.toList());
    }

    private static Stream<IIssue> getProviderIssues(List<IIssueProvider> providers, String pattern,
            boolean filterDuplicates) {
        Stream<SourceTextEntry> allEntries = Core.getProject().getAllEntries().parallelStream();
        Stream<SourceTextEntry> filteredByPattern = allEntries
                .filter(StreamUtil.patternFilter(pattern, ste -> ste.getKey().file));
        Stream<Map.Entry<SourceTextEntry, TMXEntry>> entriesStream = filteredByPattern
                .flatMap(ste -> makeEntryPair(ste, filterDuplicates).stream());
        return entriesStream.flatMap(entry -> getIssuesForEntry(entry, providers));
    }

    private static Stream<IIssue> getIssuesForEntry(Map.Entry<SourceTextEntry, TMXEntry> entry,
            List<IIssueProvider> providers) {
        return providers.stream().flatMap(provider -> provider.getIssues(entry.getKey(), entry.getValue()).stream());
    }

    private static Optional<Map.Entry<SourceTextEntry, TMXEntry>> makeEntryPair(SourceTextEntry ste,
            boolean filterDuplicates) {
        IProject project = Core.getProject();
        if (!project.isProjectLoaded()) {
            return Optional.empty();
        }
        TMXEntry tmxEntry = project.getTranslationInfo(ste);
        if (!tmxEntry.isTranslated()) {
            return Optional.empty();
        }
        if (filterDuplicates && DataUtils.isDuplicate(ste, tmxEntry)) {
            return Optional.empty();
        }
        return Optional.of(new AbstractMap.SimpleImmutableEntry<>(ste, tmxEntry));
    }
}
