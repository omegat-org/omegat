/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Properties;

import org.omegat.util.LFileCopy;
import org.omegat.util.OStrings;

public class CreateDocIndexUtil {

    protected static final String MARK_BEG = "<table>";
    protected static final String MARK_END = "</table>";

    protected static Properties langExceptionsNames = new Properties();

    public static void main(String[] args) throws Exception {
        InputStream in = CreateDocIndexUtil.class
                .getResourceAsStream("lang_exceptions.properties");
        try {
            langExceptionsNames.load(in);
        } finally {
            in.close();
        }

        File[] ls = new File("docs").listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() && new File(f, "index.html").exists();
            }
        });
        Arrays.sort(ls, new Comparator<File>() {
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        String template = readTemplate();
        int mb = template.indexOf(MARK_BEG);
        int me = template.indexOf(MARK_END) + MARK_END.length();
        if (mb < 0 || me < 0) {
            throw new Exception(
                    "Error creating docs/index.html: there are no marks");
        }
        StringBuilder text = new StringBuilder();
        text.append("<table>" + System.getProperty("line.separator"));
        int count = 0;
        for (File f : ls) {
            String locale = f.getName();

            String localeName = getLocaleName(locale);
            String transVersion = getDocVersion(locale);

            // Skip incomplete translations
            if (transVersion == null)
                continue;

            // Add some HTML for the translation
            text.append("<tr><td><a href=\"").append(locale).append(
                    "/index.html#__sethome");
            text.append("\">");
            text.append(localeName);
            text.append("</a></td><td>(");
            if (transVersion.equals(OStrings.VERSION))
                text.append("<font color=\"green\"><strong>");
            else
                text.append("<font color=\"red\">");
            text.append(transVersion);
            if (transVersion.equals(OStrings.VERSION))
                text.append("</strong>");
            text.append("</font>)</td></tr>"
                    + System.getProperty("line.separator"));
            count++;
        }
        text.append("</table>");
        System.out.println("Add " + count + " locales to docs/index.html");
        writeOut(template.substring(0, mb) + text + template.substring(me));
    }

    protected static String readTemplate() throws IOException {
        Reader rd = new InputStreamReader(
                new FileInputStream("docs/index.html"), "UTF-8");
        StringWriter wr = new StringWriter();
        LFileCopy.copy(rd, wr);
        return wr.toString();
    }

    protected static void writeOut(String text) throws IOException {
        Writer wr = new OutputStreamWriter(new FileOutputStream(
                "docs/index.html"), "UTF-8");
        wr.write(text);
        wr.flush();
        wr.close();
    }

    /**
     * Returns the full locale name for a locale tag.
     * 
     * @see Locale.getDisplayName(Locale inLocale)
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     * @author Didier Briel
     */
    private static String getLocaleName(String localeTag) {
        String language = localeTag.substring(0, 2);
        String name = langExceptionsNames.getProperty(language);
        if (name == null) {
            String country = localeTag.length() >= 5 ? localeTag
                    .substring(3, 5) : "";
            Locale locale = new Locale(language, country);

            name = locale.getDisplayName(locale);
        }

        return name;
    }

    private static String getDocVersion(String locale) throws IOException {
        Properties prop = new Properties();
        prop
                .load(new FileInputStream("docs/" + locale
                        + "/version.properties"));
        return prop.getProperty("version");
    }
}
