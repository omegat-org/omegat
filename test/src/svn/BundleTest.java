/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package svn;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.omegat.Main;
import org.omegat.util.EncodingDetector;
import org.omegat.util.Language;

import junit.framework.TestCase;

/**
 *
 * @author Aaron Madlon-Kay
 */
public class BundleTest extends TestCase {
    
    /**
     * Ensure that all UI string bundles have either US-ASCII encoding
     * or ISO-8859-1 encoding. The spec requires the latter, but ISO-8859-1
     * is a superset of ASCII so ASCII is also acceptable (and is widely used
     * in practice).
     * 
     * @see PropertyResourceBundle https://docs.oracle.com/javase/8/docs/api/java/util/PropertyResourceBundle.html
     */
    public void testBundleEncodings() throws Exception {
        // Test English bundle separately as its name corresponds to the
        // empty locale, and will not be resolved otherwise.
        assertEncoding("Bundle.properties");
        for (Language lang : Language.LANGUAGES) {
            String bundle = "Bundle_" + lang.getLocaleCode() + ".properties";
            assertEncoding(bundle);
        }
    }
    
    private void assertEncoding(String bundle) throws IOException {
        InputStream stream = Main.class.getResourceAsStream(bundle);
        if (stream == null) {
            return;
        }
        String encoding = EncodingDetector.detectEncoding(stream);
        System.out.println(bundle + ": " + encoding);
        // The detector will give null for ASCII and Windows-1252 for ISO-8859-1;
        // yes, this is not technically correct, but it's close enough. See:
        // http://www.i18nqa.com/debug/table-iso8859-1-vs-windows-1252.html
        assertTrue(encoding == null || "WINDOWS-1252".equals(encoding));
    }
    
    public void testBundleLoading() {
        // We must set the default locale to English first because we provide our
        // English bundle as the empty-locale default. If we don't do so, the
        // English bundle will never be tested in the case that the "default default"
        // is a language we provide a bundle for.
        Locale.setDefault(Locale.ENGLISH);
        
        for (Language lang : Language.LANGUAGES) {
            ResourceBundle bundle = ResourceBundle.getBundle("org/omegat/Bundle", lang.getLocale());
            assertTrue(bundle.getKeys().hasMoreElements());
        }
    }

    public void testVersionPropsLoading() {
        ResourceBundle bundle = ResourceBundle.getBundle("org/omegat/Version");
        bundle.getString("version");
        bundle.getString("update");
        bundle.getString("revision");
    }

    public void testLoggerPropsLoading() {
        ResourceBundle bundle = ResourceBundle.getBundle("org/omegat/logger");
        assertTrue(bundle.getKeys().hasMoreElements());
    }

    public void testShortcutPropsLoading() throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("org/omegat/gui/main/MainMenuShortcuts");
        assertTrue(bundle.getKeys().hasMoreElements());

        // ResourceBundle.getBundle won't resolve the Mac-specific file's name
        // so we have to load it manually.
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/org/omegat/gui/main/MainMenuShortcuts.mac.properties"));
        assertFalse(props.isEmpty());
    }
}
