/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.util;

import java.util.List;

import org.omegat.util.PreferencesImpl.IPrefsPersistence;

/**
 * An isolated preferences store for tests.
 * <p>
 * It is a {@link PreferencesImpl} backed by an in-memory persistence that
 * starts empty and is never written to disk, so each test can install a fresh,
 * independent instance through {@link Preferences#setPreferences(Preferences.IPreferences)}
 * without preference state leaking between tests. This mirrors the
 * {@code TestCoreState} and {@code TestRuntimePreferenceStore} fixtures.
 */
public class TestPreferences extends PreferencesImpl {

    public TestPreferences() {
        super(new InMemoryPersistence());
    }

    private static final class InMemoryPersistence implements IPrefsPersistence {
        @Override
        public void load(List<String> keys, List<String> values) {
            // Tests start from an empty store: nothing to load.
        }

        @Override
        public void save(List<String> keys, List<String> values) {
            // Tests never persist preferences to disk.
        }

        @Override
        public boolean isFirstRun() {
            return true;
        }

        @Override
        public String getCreatedAt() {
            return null;
        }

        @Override
        public String getUpdatedAt() {
            return null;
        }
    }
}
