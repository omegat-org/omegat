/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.machinetranslators;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.omegat.util.LFileCopy;
import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * Support of Belazar(http://belsoft.tut.by/belazar/) machine translation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class BelazarTranslate extends BaseTranslate {
    protected static final String CHARSET = "Cp1251";

    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_BELAZAR_TRANSLATE;
    }

    public String getName() {
        return OStrings.getString("MT_ENGINE_BELAZAR");
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        String mode;
        if ("be".equalsIgnoreCase(sLang.getLanguageCode()) && "ru".equalsIgnoreCase(tLang.getLanguageCode())) {
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
        OutputStream out = conn.getOutputStream();
        try {
            out.write(db);
            out.flush();
        } finally {
            out.close();
        }
        StringWriter result = new StringWriter();
        InputStream in = conn.getInputStream();
        try {
            InputStreamReader rd = new InputStreamReader(in, CHARSET);
            LFileCopy.copy(rd, result);
        } finally {
            in.close();
        }

        return result.toString();
    }
}
