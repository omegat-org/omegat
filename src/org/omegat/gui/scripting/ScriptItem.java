/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Briac Pilpre, Didier Briel
               2015 Yu Tang, Aaron Madlon-Kay
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.gui.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
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

    protected static final String EDITOR_SCRIPT = "<editor script>";
    private static final String PROPERTIES = "properties/";
    private static final String BOM = "\uFEFF";

    private boolean startsWithBOM = false;
    private String lineBreak = System.lineSeparator();

    /** File containing the sources of the script. */
    private final @Nullable File sourceFile;

    /** If no file is present, the source is provided in this field (it is assumed to be in the
     * language ScriptRunner.DEFAULT_SCRIPT (Groovy).
     */
    private final @Nullable String sourceCode;

    private @Nullable String scriptName = null;
    private @Nullable String descriptionText = null;
    private @Nullable ResourceBundle bundle = null;

    /**
     * Creates a new ScriptItem with the specified script source.
     *
     * @param scriptSource the source code of the script. This must not be null.
     */
    public ScriptItem(@NotNull String scriptSource) {
        sourceCode = scriptSource;
        sourceFile = null;
    }

    /**
     * Constructs a new ScriptItem based on the provided script file.
     * This constructor initializes the ScriptItem with a reference to the script file
     * and prepares any associated resources, such as a ResourceBundle for localization.
     *
     * @param scriptFile the script file to be associated with this ScriptItem.
     *                    This file must not be null and should represent the source of the script.
     */
    public ScriptItem(@NotNull File scriptFile) {
        sourceFile = scriptFile;
        sourceCode = null;
        initResourceBundle(scriptFile);
    }

    /**
     * Initializes the resource bundle for a given script file. Attempts to locate
     * and load the appropriate resource bundle for the script, which can be used
     * for localized resources. If no resource bundle is found, it processes the
     * script file for additional description.
     *
     * @param scriptFile the script file for which the resource bundle is
     *                   being initialized. This file must not be null and must
     *                   exist on the file system.
     */
    @VisibleForTesting
    void initResourceBundle(File scriptFile) {
        try (URLClassLoader loader = new URLClassLoader(new URL[]{scriptFile.getParentFile().toURI().toURL()})) {
            String shortName = FilenameUtils.removeExtension(scriptFile.getName());
            try { // Try first at the root of the script dir, for compatibility
                bundle = ResourceBundle.getBundle(shortName, Locale.getDefault(), loader);
            } catch (MissingResourceException e) {
                try { // Then inside the /properties dir
                    bundle = ResourceBundle.getBundle(PROPERTIES + shortName, Locale.getDefault(), loader);
                } catch (MissingResourceException ex) {
                    scanFileForDescription(scriptFile);
                }
            }
        } catch (IOException e) {
            /* ignore */
        }
    }

    @VisibleForTesting
    static final String SCAN_PATTERN = ":name\\s*=\\s*(.*)\\s+:description\\s*=\\s*(.*)";

    @VisibleForTesting
    void scanFileForDescription(File file) {
        try (Scanner scan = getScanner(file)) {
            scan.findInLine(SCAN_PATTERN);
            MatchResult results = scan.match();
            scriptName = results.group(1).trim();
            descriptionText = results.group(2).trim();
        } catch (IllegalStateException e) {
            /* bad luck */
        } catch (IOException ignored) {
            /* ignore - it should not happen here */
        }
    }

    @VisibleForTesting
    Scanner getScanner(File file) throws IOException {
        return new Scanner(file, StandardCharsets.UTF_8);
    }

    /**
     * When the properties file is provided, it returns a resource bundle for
     * the script item. Otherwise, it returns an empty resource bundle,
     * which raises MissingResourceException when the resource is requested.
     *
     * @return the resource bundle for this script item
     */
    public @NotNull ResourceBundle getResourceBundle() {
        if (bundle != null) {
            return bundle;
        }

        // Create empty resource for confirmation
        return new ResourceBundle() {
            static final String MISSING_BUNDLE_MESSAGE = "ResourceBundle (.properties file for localization) is missing.";

            @Override
            protected Object handleGetObject(@NotNull String key) {
                throw new MissingResourceException(MISSING_BUNDLE_MESSAGE, null, key);
            }

            @Override
            public @NotNull Enumeration<String> getKeys() {
                throw new MissingResourceException(MISSING_BUNDLE_MESSAGE, null, null);
            }
        };
    }

    /**
     * The name of the script when it is created from a file.
     * If the script provides a resource bundle for localization,
     * it returns the name from the bundle.
     * Otherwise, it returns the name of the file.
     *
     * @return the name of the script
     */
    public String getScriptName() {
        if (scriptName == null && sourceFile != null) {
            String name = sourceFile.getName();
            if (bundle != null) {
                try {
                    name = bundle.getString("name");
                } catch (MissingResourceException ignore) {
                }
            }
            scriptName = name;
        }
        return scriptName;
    }

    /**
     * The name of the file containing the script
     * when the item was created from the file.
     * Otherwise, it is the name of the editor script.
     *
     * @return the name of the file containing the script
     */
    public @NotNull String getFileName() {
        if (sourceFile != null) {
            return sourceFile.getName();
        }

        return EDITOR_SCRIPT;
    }

    public @Nullable File getFile() {
        return sourceFile;
    }

    public String getDescription() {
        if (descriptionText != null) {
            return descriptionText;
        }

        try {
            descriptionText = bundle == null ? "" : bundle.getString("description");
        } catch (MissingResourceException e) {
            descriptionText = "";
        }

        return descriptionText;
    }

    /**
     * Generates a tooltip text for the script item by combining its script name
     * and description. If the description is empty, only the script name is returned.
     *
     * @return the tooltip text representing the script item, combining its name
     *         and description, or just the name if the description is empty
     */
    public String getToolTip() {
        String name = getScriptName();
        String description = getDescription();
        return "".equals(description) ? name : name + " - " + description;
    }

    public String getText() throws IOException {
        if (sourceFile == null && sourceCode == null) {
            throw new IOException("Cannot run a script item without source.");
        }

        if (sourceCode != null) {
            return sourceCode;
        }

        StringBuilder sb = new StringBuilder();
        try (LinebreakPreservingReader lpin = getUTF8LinebreakPreservingReader(sourceFile)) {
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

    @VisibleForTesting
    LinebreakPreservingReader getUTF8LinebreakPreservingReader(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }
        InputStream is = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(isr);
        return new LinebreakPreservingReader(in);
    }

    /**
     * Updates the text content of the script associated with this ScriptItem.
     * If the source file exists, the provided text is processed for line breaks
     * and optionally includes a BOM before being written to the file.
     * If the source file is null, an exception is thrown.
     *
     * @param text the new text content to be written to the script's source file.
     *             This parameter must not be null.
     * @throws IOException if the source file does not exist or an error occurs
     *                     while writing to the file.
     */
    public void setText(@NotNull String text) throws IOException {
        if (sourceFile != null) {
            FileUtils.writeStringToFile(sourceFile, applyLineBreaksToText(text), StandardCharsets.UTF_8);
        } else {
            throw new IOException("Cannot save inline script source.");
        }
    }

    private @NotNull String applyLineBreaksToText(String text) {
        text = text.replaceAll("\n", lineBreak);
        if (startsWithBOM) {
            text = BOM + text;
        }
        return text;
    }

    @Override
    public String toString() {
        return getScriptName();
    }

    @Override
    public int compareTo(ScriptItem o) {
        return getScriptName().compareTo(o.getScriptName());
    }

}
