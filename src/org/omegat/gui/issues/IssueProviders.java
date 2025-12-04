/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               2025 Hiroshi Miura
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omegat.core.data.CoreState;
import org.omegat.util.Preferences;

/**
 * A class for aggregating issue providers. Extensions and scripts can add their
 * providers here with {@link #addIssueProvider(IIssueProvider)}.
 *
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public final class IssueProviders {

    static final String ISSUE_IDS_DELIMITER = ",";

    private IssueProviders() {
    }

    public static List<IIssueProvider> getIssueProviders() {
        ensureDefaultsInstalled();
        return Collections.unmodifiableList(CoreState.getInstance().getIssueProvidersRegistry());
    }

    public static void addIssueProvider(IIssueProvider provider) {
        ensureDefaultsInstalled();
        CoreState.getInstance().getIssueProvidersRegistry().add(provider);
    }

    /**
     * Retrieves the set of IDs corresponding to issue providers that are
     * marked as disabled.
     *
     * @return A set of strings representing the IDs of the disabled issue
     *      providers.
     */
    static Set<String> getDisabledProviderIds() {
        String disabled = Preferences.getPreference(Preferences.ISSUE_PROVIDERS_DISABLED);
        return getSetOfTerms(disabled);
    }

    /**
     * Returns a set of terms from a comma-separated string.
     * @param terms comma-separated string of terms
     * @return Set of terms
     */
    static Set<String> getSetOfTerms(String terms) {
        if (terms == null || terms.isEmpty()) {
            return Collections.emptySet();
        }
        return Stream.of(terms.split(ISSUE_IDS_DELIMITER)).collect(Collectors.toSet());
    }

    static List<IIssueProvider> getEnabledProviders() {
        ensureDefaultsInstalled();
        Set<String> disabled = getDisabledProviderIds();
        return CoreState.getInstance().getIssueProvidersRegistry().stream()
                .filter(p -> !disabled.contains(p.getId()))
                .collect(Collectors.toList());
    }

    private static void ensureDefaultsInstalled() {
        List<IIssueProvider> reg = CoreState.getInstance().getIssueProvidersRegistry();

        // Remove existing occurrences to avoid duplicates and to ensure ordering
        reg.removeIf(p -> SpellingIssueProvider.class.getCanonicalName().equals(p.getId())
                || TerminologyIssueProvider.class.getCanonicalName().equals(p.getId()));

        // Add defaults at the front in a stable order: Spelling, then Terminology
        reg.add(0, new TerminologyIssueProvider());
        reg.add(0, new SpellingIssueProvider());
    }

    public static void setProviderEnabled(String id, boolean enabled) {
        if (enabled) {
            setProviders(Collections.singleton(id), Collections.emptySet());
        } else {
            setProviders(Collections.emptySet(), Collections.singleton(id));
        }
    }

    public static void setProviders(Collection<String> enabled, Collection<String> disabled) {
        Set<String> toDisable = new HashSet<>(getDisabledProviderIds());
        toDisable.removeAll(enabled);
        toDisable.addAll(disabled);
        Preferences.setPreference(Preferences.ISSUE_PROVIDERS_DISABLED,
                String.join(ISSUE_IDS_DELIMITER, toDisable));
    }
}
