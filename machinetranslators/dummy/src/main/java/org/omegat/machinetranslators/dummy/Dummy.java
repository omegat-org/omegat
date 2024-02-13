/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2024 Hiroshi Miura
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.machinetranslators.dummy;

import org.omegat.core.Core;
import org.omegat.core.machinetranslators.BaseCachedTranslate;
import org.omegat.util.Language;

public class Dummy extends BaseCachedTranslate {

    private static final String ENGINE_NAME = "dummy";
    private static final String ALLOW_TRANSLATE = "allow_dummy_translate";
    private static final String TRANSLATION = "Translated result from dummy engine.";

    /**
     * Register plugins into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerMachineTranslationClass(Dummy.class);
    }

    public static void unloadPlugins() {
    }

    public Dummy() {
        super();
    }

    @Override
    public String getName() {
        return ENGINE_NAME;
    }

    @Override
    public String getPreferenceName() {
        return ALLOW_TRANSLATE;
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) {
        return TRANSLATION;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }
}
