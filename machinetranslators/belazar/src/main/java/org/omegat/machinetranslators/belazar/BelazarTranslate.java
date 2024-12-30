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

package org.omegat.machinetranslators.belazar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import tokyo.northside.logging.ILogger;
import tokyo.northside.logging.LoggerFactory;

import org.omegat.core.Core;
import org.omegat.core.machinetranslators.BaseCachedTranslate;
import org.omegat.util.Language;

/**
 * Support of Belazar(http://belsoft.tut.by/belazar/) machine translation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class BelazarTranslate extends BaseCachedTranslate {
    public static final String ALLOW_BELAZAR_TRANSLATE = "allow_belazar_translate";
    protected static final String CHARSET = "Cp1251";
    private static final String BUNDLE_BASENAME = "org.omegat.machinetranslators.belazar.Bundle";
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_BASENAME);
    private static final ILogger LOGGER = LoggerFactory.getLogger(BelazarTranslate.class, BUNDLE);

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
        return ALLOW_BELAZAR_TRANSLATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return BUNDLE.getString("MT_ENGINE_BELAZAR");
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

        try {
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
        } catch (IOException e) {
            LOGGER.atError().setCause(e).setMessageRB("BELARZAR_SERVER_NOTFOUND").log();
            throw new Exception(BUNDLE.getString("BELAZAR_SERVER_NOTFOUND"));
        }
    }
}
