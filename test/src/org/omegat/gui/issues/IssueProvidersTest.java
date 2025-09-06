/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

import org.junit.BeforeClass;
import org.junit.Test;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IssueProvidersTest {

    private static String testingProviderId;

    @BeforeClass
    public static void setUpClass() throws IOException {
        TestPreferencesInitializer.init();
        // Add a new provider dynamically
        IIssueProvider testingIssueProvider = new TestingIssueProvider();
        testingProviderId = TestingIssueProvider.class.getCanonicalName();
        IssueProviders.addIssueProvider(testingIssueProvider);
    }

    @Test
    public void testGetIssueProviders() {
        // Retrieve the list of issue providers
        List<IIssueProvider> issueProviders = IssueProviders.getIssueProviders();
        Set<String> disabledProviderIds = IssueProviders.getDisabledProviderIds();

        // Assert that the list is not null
        assertNotNull(issueProviders);
        assertNotNull(disabledProviderIds);

        // Assert that the list contains the expected number of providers
        assertEquals(3, issueProviders.size());
        assertEquals(0, disabledProviderIds.size());

        // Assert that the correct providers are present
        assertEquals(SpellingIssueProvider.class.getCanonicalName(), issueProviders.get(0).getId());
        assertEquals(TerminologyIssueProvider.class.getCanonicalName(), issueProviders.get(1).getId());
        assertEquals(OStrings.getString("ISSUES_SPELLING_PROVIDER_NAME"), issueProviders.get(0).getName());
        assertEquals(OStrings.getString("ISSUES_TERMINOLOGY_PROVIDER_NAME"), issueProviders.get(1).getName());
    }

    @Test
    public void testGetDisabledProviderIds() {
        try {
            // Set up mock preferences for disabled provider IDs
            Preferences.setPreference(Preferences.ISSUE_PROVIDERS_DISABLED, "spelling,terminology");

            // Retrieve the set of disabled provider IDs
            Set<String> disabledProviderIds = IssueProviders.getDisabledProviderIds();

            // Assert the correct IDs are retrieved
            assertNotNull(disabledProviderIds);
            assertEquals(2, disabledProviderIds.size());
            assertTrue(disabledProviderIds.contains("spelling"));
            assertTrue(disabledProviderIds.contains("terminology"));
        } finally {
            Preferences.setPreference(Preferences.ISSUE_PROVIDERS_DISABLED, "");
        }
    }
    @Test
    public void testGetSetOfTerms() {
        // Test for null
        Set<String> result = IssueProviders.getSetOfTerms(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Test for empty terms
        result = IssueProviders.getSetOfTerms("");
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Test for a single term
        result = IssueProviders.getSetOfTerms("spelling");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains("spelling"));

        // Test for multiple terms
        result = IssueProviders.getSetOfTerms("spelling,terminology");
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("spelling"));
        assertTrue(result.contains("terminology"));
    }

    @Test
    public void testGetEnabledProviders() {
        try {
            // No disabled providers
            Preferences.setPreference(Preferences.ISSUE_PROVIDERS_DISABLED, "");
            List<IIssueProvider> enabledProviders = IssueProviders.getEnabledProviders();
            assertNotNull(enabledProviders);
            assertEquals(3, enabledProviders.size());
            assertEquals(SpellingIssueProvider.class.getCanonicalName(), enabledProviders.get(0).getId());
            assertEquals(TerminologyIssueProvider.class.getCanonicalName(), enabledProviders.get(1).getId());

            // Disable one provider
            Preferences.setPreference(Preferences.ISSUE_PROVIDERS_DISABLED,
                    SpellingIssueProvider.class.getCanonicalName());
            enabledProviders = IssueProviders.getEnabledProviders();
            assertNotNull(enabledProviders);
            assertEquals(2, enabledProviders.size());
            assertEquals(TerminologyIssueProvider.class.getCanonicalName(), enabledProviders.get(0).getId());

            // Disable both providers
            Preferences.setPreference(Preferences.ISSUE_PROVIDERS_DISABLED,
                    SpellingIssueProvider.class.getCanonicalName() + ","
                            + TerminologyIssueProvider.class.getCanonicalName());
            enabledProviders = IssueProviders.getEnabledProviders();
            assertNotNull(enabledProviders);
            assertEquals(1, enabledProviders.size());
        } finally {
            Preferences.setPreference(Preferences.ISSUE_PROVIDERS_DISABLED, "");
        }
    }

    @Test
    public void testDynamicProviderEnablingDisabling() {
        try {
            // Verify default providers are enabled
            Preferences.setPreference(Preferences.ISSUE_PROVIDERS_DISABLED, "");
            List<IIssueProvider> enabledProviders = IssueProviders.getEnabledProviders();
            assertNotNull(enabledProviders);
            assertEquals(3, enabledProviders.size());
            assertEquals(testingProviderId, enabledProviders.get(2).getId());

            // Disable the dynamic provider
            Preferences.setPreference(Preferences.ISSUE_PROVIDERS_DISABLED, testingProviderId);
            enabledProviders = IssueProviders.getEnabledProviders();
            assertEquals(2, enabledProviders.size());
            assertTrue(enabledProviders.stream().noneMatch(p -> p.getId().equals(testingProviderId)));

            // Re-enable the dynamic provider
            Preferences.setPreference(Preferences.ISSUE_PROVIDERS_DISABLED, "");
            enabledProviders = IssueProviders.getEnabledProviders();
            assertEquals(3, enabledProviders.size());
        } finally {
            Preferences.setPreference(Preferences.ISSUE_PROVIDERS_DISABLED, "");
        }
    }

    @Test
    public void testSetProviders() {
        try {
            // Verify the initial state
            Set<String> disabledProviderIds = IssueProviders.getDisabledProviderIds();
            assertTrue(disabledProviderIds.isEmpty());

            // Disable specific providers using setProviders and verify
            IssueProviders.setProviders(Collections.emptySet(),
                    Set.of(SpellingIssueProvider.class.getCanonicalName()));
            disabledProviderIds = IssueProviders.getDisabledProviderIds();
            assertEquals(1, disabledProviderIds.size());
            assertTrue(disabledProviderIds.contains(SpellingIssueProvider.class.getCanonicalName()));

            // Enable a disabled provider back using setProviders
            IssueProviders.setProviders(Set.of(SpellingIssueProvider.class.getCanonicalName()),
                    Collections.emptySet());
            disabledProviderIds = IssueProviders.getDisabledProviderIds();
            assertTrue(disabledProviderIds.isEmpty());

            // Disable multiple providers and verify
            IssueProviders.setProviders(Collections.emptySet(), Set.of(
                    SpellingIssueProvider.class.getCanonicalName(),
                    TerminologyIssueProvider.class.getCanonicalName()));
            disabledProviderIds = IssueProviders.getDisabledProviderIds();
            assertEquals(2, disabledProviderIds.size());
            assertTrue(disabledProviderIds.contains(SpellingIssueProvider.class.getCanonicalName()));
            assertTrue(disabledProviderIds.contains(TerminologyIssueProvider.class.getCanonicalName()));
        } finally {
            // reset to default finally
            IssueProviders.setProviders(Set.of(SpellingIssueProvider.class.getCanonicalName(),
                    TerminologyIssueProvider.class.getCanonicalName()), Collections.emptySet());
        }
    }

}
