/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2010 Alex Buloichik
 *                2023 Hiroshi Miura
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

package org.omegat.core.machinetranslators;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;

import org.omegat.core.Core;
import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * Support of Belazar(http://belsoft.tut.by/belazar/) machine translation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class BelazarTranslate extends BaseCachedTranslate {
    protected static final String CHARSET = "Cp1251";

    /**
     * Register plugins into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerMachineTranslationClass(BelazarTranslate.class);
    }

    public static void unloadPlugins() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_BELAZAR_TRANSLATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return OStrings.getString("MT_ENGINE_BELAZAR");
    }

    /**
     * Query Belazar translation API and return translation.
     * 
     * @param sLang
     *            source language.
     * @param tLang
     *            target language.
     * @param text
     *            source text.
     * @return translation.
     * @throws Exception
     *             when communication error.
     */
    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        String mode;
        if ("be".equalsIgnoreCase(sLang.getLanguageCode())
                && "ru".equalsIgnoreCase(tLang.getLanguageCode())) {
            mode = "br";
        } else if ("ru".equalsIgnoreCase(sLang.getLanguageCode())
                && "be".equalsIgnoreCase(tLang.getLanguageCode())) {
            mode = "rb";
        } else {
            return null;
        }

        String data = "td=" + mode + "&addtags=0&txt=" + URLEncoder.encode(text, CHARSET);

        byte[] db = data.getBytes(CHARSET);

        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:48762").openConnection();
        conn.setRequestMethod("POST");

        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(db.length));
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        try (OutputStream out = conn.getOutputStream(); InputStream in = conn.getInputStream()) {
            out.write(db);
            out.flush();
            return IOUtils.toString(in, CHARSET);
        }
    }
}
