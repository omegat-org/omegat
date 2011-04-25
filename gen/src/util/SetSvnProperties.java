/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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
import java.util.ArrayList;
import java.util.List;

/**
 * Set mime properties for all files.
 * 
 * Good reference for mime-types could be found on
 * http://mindprod.com/jgloss/mime.html
 * 
 * If you are using TortoiseSVN, then kill TSVNCache in memory, since it can
 * conflict with command-line svn.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class SetSvnProperties {
    protected static String filename;

    public static void main(String[] args) {
        List<String> files = new ArrayList<String>();
        list(new File("."), files);
        for (String f : files) {
            filename = f.replace('\\', '/');
            process();
        }
    }

    protected static void list(File dir, List<String> result) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.isDirectory()) {
                    result.add(f.getPath());
                } else {
                    list(f, result);
                }
            }
        }
    }

    protected static void process() {
        if (filename.contains("/.svn/") || filename.startsWith("./classes/") || filename.startsWith("./build/")
                || filename.startsWith("./lib-mnemonics/build/") || filename.startsWith("./.settings/")
                || filename.startsWith("./dist")) {
            return;
        }
        if (filename.startsWith("./test/data/")) {
            if (filename.endsWith(".xml") || filename.endsWith(".tbx")) {
                set("text/xml");
            } else if (filename.endsWith(".hhc") || filename.endsWith(".hhk")) {
                set("text/html");
            } else if (filename.endsWith(".html")) {
                set("text/html");
            } else if (filename.endsWith(".ini")) {
                set("text/plain");
            } else if (filename.endsWith(".odt")) {
                set("application/octet-stream");
            } else if (filename.endsWith(".docx")) {
                set("application/octet-stream");
            } else if (filename.endsWith(".po")) {
                set("text/plain");
            } else if (filename.endsWith(".properties")) {
                set("text/plain");
            } else if (filename.endsWith(".txt") || filename.endsWith(".tab") || filename.endsWith(".csv")) {
                set("text/plain");
            } else if (filename.endsWith(".srt")) {
                set("text/plain");
            } else if (filename.endsWith(".xlf")) {
                set("text/xml");
            } else if (filename.endsWith(".tmx")) {
                set("text/xml", "native");
            } else if (filename.endsWith(".wxl")) {
                set("text/xml", "native");
            } else if (filename.endsWith(".svg")) {
                set("text/xml", "native");
            } else if (filename.endsWith(".resx")) {
                set("text/xml", "native");
            } else if (filename.endsWith(".rc")) {
                set("text/plain", "native");
            } else if (filename.endsWith(".dtd")) {
                set("text/plain", "native");
            } else if (filename.endsWith(".tex")) {
                set("text/plain", "native");
            } else if (filename.endsWith(".dz")) {
                set("application/gzip");
            } else if (filename.endsWith(".idx")) {
                set("application/octet-stream");
            } else if (filename.endsWith(".ifo")) {
                set("text/plain", "native");
            } else {
                System.out.println("unknown: " + filename);
            }
        } else if (filename.startsWith("./native/")) {
            set("application/octet-stream");
        } else if (filename.equals("./release/OmegaT")) {
            set("text/plain", "LF");
        } else if (filename.startsWith("./.")) {
            set("text/xml", "native");
        } else if (filename.endsWith(".java")) {
            set("text/x-java", "native");
            if (filename.contains("./src/")) {
                setKeywords("Author Date Id Revision");
            }
        } else if (filename.equals("./release/l10n-project/omegat.project")) {
            set("text/xml", "native");
        } else if (filename.endsWith(".properties")) {
            set("text/plain", "native");
        } else if (filename.endsWith(".xml")) {
            set("text/xml", "native");
        } else if (filename.endsWith(".jnlp")) {
            set("text/xml", "native");
        } else if (filename.endsWith(".xsd")) {
            set("text/xml", "native");
        } else if (filename.endsWith(".dtd")) {
            set("text/xml", "native");
        } else if (filename.endsWith(".mod")) {
            set("text/xml", "native");
        } else if (filename.endsWith(".exe")) {
            set("application/octet-stream");
        } else if (filename.endsWith(".jar")) {
            set("application/java-archive");
        } else if (filename.endsWith(".zip")) {
            set("application/zip");
        } else if (filename.endsWith(".gz")) {
            set("application/gzip");
        } else if (filename.endsWith(".txt")) {
            set("text/plain", "native");
        } else if (filename.endsWith(".form")) {
            set("text/xml", "native");
        } else if (filename.endsWith(".ini")) {
            set("text/plain", "native");
        } else if (filename.endsWith(".bat")) {
            set("text/plain", "CRLF");
        } else if (filename.endsWith(".cmd")) {
            set("text/plain", "CRLF");
        } else if (filename.endsWith(".gif")) {
            set("image/gif");
        } else if (filename.endsWith(".png")) {
            set("image/png");
        } else if (filename.endsWith(".jpg")) {
            set("image/jpeg");
        } else if (filename.endsWith(".ico")) {
            set("image/x-icon");
        } else if (filename.endsWith(".html")) {
            set("text/html", "native");
        } else if (filename.endsWith(".css")) {
            set("text/plain", "native");
        } else if (filename.endsWith(".mf")) {
            set("text/plain", "native");
        } else if (filename.endsWith("/.ignoreme")) {
            set("text/plain", "native");
        } else if (filename.endsWith(".iss")) {
            set("text/plain", "native");
        } else if (filename.endsWith(".j2e")) {
            set("text/plain", "native");
        } else if (filename.endsWith(".odt")) {
            set("application/octet-stream");
        } else if (filename.endsWith(".vpp")) {
            set("application/zip");
        } else if (filename.startsWith("./release/OmegaT")) {
            set("text/plain");
        } else {
            System.out.println("unknown: " + filename);
        }
    }

    protected static void set(String mimetype) {
        System.out.println("svn propset svn:mime-type " + mimetype + " \"" + filename + "\"");
    }

    protected static void set(String mimetype, String eol) {
        System.out.println("svn propset svn:mime-type " + mimetype + " \"" + filename + "\"");
        System.out.println("svn propset svn:eol-style " + eol + " \"" + filename + "\"");
    }

    protected static void setKeywords(String keywords) {
        System.out.println("svn propset svn:keywords '" + keywords + "' \"" + filename + "\"");
    }
}
