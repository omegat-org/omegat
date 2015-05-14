/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Briac Pilpre, Didier Briel
               2015 Yu Tang
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
package org.omegat.gui.scripting;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.MatchResult;

import org.omegat.util.LFileCopy;
import org.omegat.util.LinebreakPreservingReader;
import org.omegat.util.OConsts;

/**
 * A script file in the script list is represented as ScriptListFile to allow for localization, description and
 * reordering.
 * 
 * @author Briac Pilpre
 * @author Didier Briel
 */
public class ScriptItem extends File {

    private static final long serialVersionUID = -257191026120285430L;
    
    private static final String PROPERTIES = "properties/";

    public ScriptItem(File scriptFile) {
        super(scriptFile.getParentFile(), scriptFile.getName());

        try {
            ClassLoader loader = new URLClassLoader(new URL[]{scriptFile.getParentFile().toURI().toURL()});
            String shortName = ScriptingWindow.getBareFileName(scriptFile.getName());
            try { // Try first at the root of the script dir, for compatibility
                m_res = ResourceBundle.getBundle(shortName, Locale.getDefault(), loader); 
            } catch (MissingResourceException e) {
                try { // Then inside the /properties dir
                    m_res = ResourceBundle.getBundle(PROPERTIES  + shortName, Locale.getDefault(), loader);
                } catch (MissingResourceException ex) {
                    scanFileForDescription(scriptFile);
                }
            }
        } catch (MalformedURLException e) {
            /* ignore */
        }
    }

    private void scanFileForDescription(File file) {
        Scanner scan = null;
        try {
            scan = new Scanner(file);
            scan.findInLine(":name\\s*=\\s*(.*)\\s+:description\\s*=\\s*(.*)");
            MatchResult results = scan.match();
            m_scriptName = results.group(1).trim();
            m_description = results.group(2).trim();
        } catch (IllegalStateException e) {
            /* bad luck */
        } catch (FileNotFoundException e) {
            /* ignore - it should not happen here */
        }
        finally {
            if (scan != null)
            {
                scan.close();
            }
        }
    }

    public ResourceBundle getResourceBundle() {
        if (m_res != null) {
            return m_res;
        }

        // Create empty resource for confirmation
        return new ResourceBundle() {
            final String MISSING_BUNDLE_MESSAGE = "ResourceBundle (.properties file for localization) is missing.";

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
        if (m_scriptName != null) {
            return m_scriptName;
        }

        try {
            m_scriptName = m_res == null ? getName() : m_res.getString("name"); //OStrings.getString("SCRIPT." + fileName + ".name");
        } catch (MissingResourceException e) {
            m_scriptName = getName();
        }

        return m_scriptName;
    }

    public String getDescription() {
        if (m_description != null) {
            return m_description;
        }

        try {
            m_description = m_res == null ? "" : m_res.getString("description"); //OStrings.getString("SCRIPT." + fileName + ".description");
        } catch (MissingResourceException e) {
            m_description = "";
        }

        return m_description;
    }

    public String getToolTip() {
        String name = getScriptName();
        String description = getDescription();
        return "".equals(description) ? name : name + " - " + description;
    }

    public String getText() throws FileNotFoundException, IOException {
        String ret = "";
        LinebreakPreservingReader lpin = null;
        try {
            lpin = getUTF8LinebreakPreservingReader(this);
            StringBuilder sb = new StringBuilder();
            String s = lpin.readLine();
            startsWithBOM = s.startsWith(BOM);
            if (startsWithBOM) {
                s = s.substring(1);  // eat BOM
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
            ret = sb.toString();
        } finally {
            if (lpin != null) {
                try {
                    lpin.close();
                } catch (IOException ex) {
                    // Eat exception silently
                }
            }
        }
        return ret;
    }

    private LinebreakPreservingReader getUTF8LinebreakPreservingReader(File file) throws FileNotFoundException, UnsupportedEncodingException {
        InputStream is = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(is, OConsts.UTF8);
        BufferedReader in = new BufferedReader(isr);
        return new LinebreakPreservingReader(in);
    }

    public void setText(String text) throws UnsupportedEncodingException, IOException {
        text = text.replaceAll("\n", lineBreak);
        if (startsWithBOM) {
            text = BOM + text;
        }

        InputStream is = new ByteArrayInputStream(text.getBytes(OConsts.UTF8));
        LFileCopy.copy(is, this);
    }

    @Override
    public String toString() {
        return getScriptName();
    }

    public static class ScriptItemComparator implements Comparator<ScriptItem> {

        @Override
        public int compare(ScriptItem o1, ScriptItem o2) {
            return o1.getScriptName().compareTo(o2.getScriptName());
        }
    }

    private final String BOM = "\uFEFF";
    private boolean startsWithBOM = false;
    private String lineBreak = System.getProperty("line.separator");

    private String m_scriptName = null;
    private String m_description = null;
    private ResourceBundle m_res = null;
}
