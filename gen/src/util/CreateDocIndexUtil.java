package util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public static void main(String[] args) throws Exception {
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
        text.append("<table>\n");
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
            text.append("</font>)</td></tr>\n");
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
        String country = localeTag.length() >= 5 ? localeTag.substring(3, 5)
                : "";
        Locale locale = new Locale(language, country);
        // The following test is necessary to fix
        // [1748552] sh language is not expanded in the manual
        // since Java does not display correctly the "sh" langage name
        if (language.equalsIgnoreCase("sh"))
            return "srpskohrvatski";
        else
            return locale.getDisplayName(locale);
    }

    private static String getDocVersion(String locale) throws IOException {
        Properties prop = new Properties();
        prop
                .load(new FileInputStream("docs/" + locale
                        + "/version.properties"));
        return prop.getProperty("version");
    }
}
