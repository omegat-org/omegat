/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.core.machinetranslators;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import org.omegat.core.TestCore;
import org.omegat.util.Language;
import org.omegat.util.WikiGet;

public class ApertiumCertTest extends TestCore {
    private static final String APERTIUM_URL = "https://www.apertium.org/apy/translate?q=Hello+World&langpair=en|ca";

    private int javaVersion = 0;
    private int javaRevision = 0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String version = System.getProperty("java.version");

        Pattern p = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)_(\\d+)$");

        Matcher m = p.matcher(version);

        // Can't connect to Let's Encrypt Certs with java 6 (key size is too big)
        if (m.matches()) {
            javaVersion = Integer.parseInt(m.group(2));
            javaRevision = Integer.parseInt(m.group(4));
        }
    }

    public void testNoCert() throws IOException {
        if (javaVersion == 6) {
            return;
        }

        String response;
        response = WikiGet.getURLNoCert(APERTIUM_URL);
        assertNotNull(response);
        assertTrue(response.contains("\"responseStatus\": 200"));
    }

    public void testSSLFailure() throws IOException {
        if (javaVersion == 6) {
            return;
        }
        
        try {
            WikiGet.getURL(APERTIUM_URL);
            if ((javaVersion == 7 && javaRevision < 111) || (javaVersion == 8 && javaRevision < 101)) {
                fail("Connection with Apertium should fail with a bad Cert.");
            }
        }
        catch (SSLHandshakeException e) {
            assertTrue(e.getMessage().contains("PKIX path building failed"));
        }
    }
    

    public void testApertiumResponse() throws Exception {
        if (javaVersion == 6) {
            return;
        }

        ApertiumTranslate at = new ApertiumTranslate();
        Language sLang = new Language("en");
        Language tLang = new Language("ca");
        assertEquals("Món d'hola", at.translate(sLang, tLang, "Hello World"));
        assertEquals("GenericName=Monopoli®-com Jocs de taula", at.translate(sLang, tLang, "GenericName=Monopoly®-like Board Games"));
    }

}
