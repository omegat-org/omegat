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

import java.io.InputStream;
import java.util.PropertyResourceBundle;

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
        for (Language lang : Language.LANGUAGES) {
            String bundle = "Bundle_" + lang.getLocaleCode() + ".properties";
            InputStream stream = Main.class.getResourceAsStream(bundle);
            if (stream == null) {
                continue;
            }
            String encoding = EncodingDetector.detectEncoding(stream);
            System.out.println(bundle + ": " + encoding);
            // The detector will give null for ASCII and Windows-1252 for ISO-8859-1;
            // yes, this is not technically correct, but it's close enough. See:
            // http://www.i18nqa.com/debug/table-iso8859-1-vs-windows-1252.html
            assertTrue(encoding == null || "WINDOWS-1252".equals(encoding));
        }
    }
}
