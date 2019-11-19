/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Briac Pilpre, Didier Briel
               2015 Yu Tang, Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: https://omegat.org/support

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
package org.omegat.gui.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.MatchResult;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.omegat.util.LinebreakPreservingReader;

/**
 * A script file in the script list is represented as ScriptListFile to allow
 * for localization, description and reordering.
 *
 * @author Briac Pilpre
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 */
public class ScriptItem implements Comparable<ScriptItem> {

    private static final String PROPERTIES = "properties/";

    public ScriptItem(File scriptFile) {
        mFile = scriptFile;

        if (mFile == null) {
            return;
        }

        try {
            ClassLoader loader = new URLClassLoader(new URL[]{scriptFile.getParentFile().toURI().toURL()});
            String shortName = FilenameUtils.removeExtension(scriptFile.getName());
            try { // Try first at the root of the script dir, for compatibility
                mRes = ResourceBundle.getBundle(shortName, Locale.getDefault(), loader);
            } catch (MissingResourceException e) {
                try { // Then inside the /properties dir
                    mRes = ResourceBundle.getBundle(PROPERTIES  + shortName, Locale.getDefault(), loader);
                } catch (MissingResourceException ex) {
                    scanFileForDescription(scriptFile);
                }
            }
        } catch (MalformedURLException e) {
            /* ignore */
        }
    }

    private void scanFileForDescription(File file) {
        try (Scanner scan = new Scanner(file, StandardCharsets.UTF_8.name())) {
            scan.findInLine(":name\\s*=\\s*(.*)\\s+:description\\s*=\\s*(.*)");
            MatchResult results = scan.match();
            mScriptName = results.group(1).trim();
            mDescription = results.group(2).trim();
        } catch (IllegalStateException e) {
            /* bad luck */
        } catch (FileNotFoundException e) {
            /* ignore - it should not happen here */
        }
    }

    public ResourceBundle getResourceBundle() {
        if (mRes != null) {
            return mRes;
        }

        // Create empty resource for confirmation
        return new ResourceBundle() {
            static final String MISSING_BUNDLE_MESSAGE = "ResourceBundle (.properties file for localization) is missing.";

            @Override
            protected Object handleGetObject(String key) {
                throw new MissingResourceException(MISSING_BUNDLE_MESSAGE, null, key);
            }

            @Override
            public Enumeration<String> getKeys() {
                throw new MissingResourceException(MISSING_BUNDLE_MESSAGE, null, null);
            }
        };
    }

    public String getScriptName() {
        if (mScriptName == null) {
            String name = mFile.getName();
            if (mRes != null) {
                try {
                    name = mRes.getString("name");
                } catch (MissingResourceException ignore) {
                }
            }
            mScriptName = name;
        }
        return mScriptName;
    }

    public File getFile() {
        return mFile;
    }

    public String getDescription() {
        if (mDescription != null) {
            return mDescription;
        }

        try {
            mDescription = mRes == null ? "" : mRes.getString("description");
        } catch (MissingResourceException e) {
            mDescription = "";
        }

        return mDescription;
    }

    public String getToolTip() {
        String name = getScriptName();
        String description = getDescription();
        return "".equals(description) ? name : name + " - " + description;
    }

    public String getText() throws FileNotFoundException, IOException {
        StringBuilder sb = new StringBuilder();
        try (LinebreakPreservingReader lpin = getUTF8LinebreakPreservingReader(mFile)) {
            String s = lpin.readLine();
            if (s != null) {
                startsWithBOM = s.startsWith(BOM);
                if (startsWithBOM) {
                    s = s.substring(1); // eat BOM
                }
            }
            while (s != null) {
                sb.append(s);
                String br = lpin.getLinebreak();
                if (!br.isEmpty()) {
                    lineBreak = br;
                    sb.append('\n');
                }
                s = lpin.readLine();
            }
        }
        return sb.toString();
    }

    private LinebreakPreservingReader getUTF8LinebreakPreservingReader(File file)
            throws FileNotFoundException, UnsupportedEncodingException {
        InputStream is = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(isr);
        return new LinebreakPreservingReader(in);
    }

    public void setText(String text) throws UnsupportedEncodingException, IOException {
        text = text.replaceAll("\n", lineBreak);
        if (startsWithBOM) {
            text = BOM + text;
        }

        FileUtils.writeStringToFile(mFile, text, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return getScriptName();
    }

    @Override
    public int compareTo(ScriptItem o) {
        return getScriptName().compareTo(o.getScriptName());
    }

    private static final String BOM = "\uFEFF";
    private boolean startsWithBOM = false;
    private String lineBreak = System.lineSeparator();

    private File mFile = null;
    private String mScriptName = null;
    private String mDescription = null;
    private ResourceBundle mRes = null;
}
