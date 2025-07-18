/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
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

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Locale rule to force specific runtime locale.
 * <p>
 * <code>
 *      {@literal @}Rule
 *      public final LocaleRule localeRule = new LocaleRule(new Locale("en"));
 * </code>
 */
public class LocaleRule implements TestRule {
    private final Locale testLocale;
    private final Locale originalLocale;

    public LocaleRule(@NotNull Locale locale) {
        originalLocale = Locale.getDefault();
        this.testLocale = locale;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Locale.setDefault(testLocale);
                OStrings.loadBundle(testLocale);
                try {
                    base.evaluate();
                } finally {
                    Locale.setDefault(originalLocale);
                    OStrings.loadBundle(originalLocale);
                }
            }
        };
    }
}
