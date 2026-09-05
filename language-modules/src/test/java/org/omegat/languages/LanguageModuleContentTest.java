/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.languages;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Audits the build scripts of the language modules.
 * <p>
 * Every language module bundles its LanguageTool language jar into the module
 * jar. When OmegaT is built from the source distribution, that jar has to be
 * taken from the provided module libraries via the providedModuleLib helper,
 * which fails on an empty match; a plain fileTree over the provided core
 * libraries matches nothing there and silently produces a module without its
 * content (no grammar rules, no bundled spelling dictionaries). The same
 * applies to the additional dictionary jars some modules pull in at runtime,
 * so every provided jar has to go through the helper. This test also catches
 * copy-paste mistakes where a module references the language jar or version
 * catalog alias of another language.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class LanguageModuleContentTest {

    private static final Pattern LANGUAGE_ARTIFACT = Pattern.compile("'language-([a-z]+)'");
    private static final Pattern PROVIDED_MODULE_LIB = Pattern.compile(
            "providedModuleLib\\s*\\(([^)]*)\\)", Pattern.DOTALL);
    private static final Pattern PROVIDED_LIBS_FILE_TREE = Pattern.compile(
            "fileTree\\s*\\(\\s*dir\\s*:\\s*provided(?:Core|Module)LibsDir");
    private static final Pattern CATALOG_ALIAS = Pattern.compile("libs\\.languagetool\\.([a-z]+)");
    private static final List<String> NON_LANGUAGE_ALIASES = Arrays.asList("core", "server");

    @Test
    public void testLanguageModulesBundleTheirOwnLanguage() throws IOException {
        File[] moduleDirs = new File("language-modules").listFiles(File::isDirectory);
        assertNotNull("language-modules directory not found; the test must run in the project root",
                moduleDirs);
        Arrays.sort(moduleDirs);
        List<String> violations = new ArrayList<>();
        int checked = 0;
        for (File moduleDir : moduleDirs) {
            File buildFile = new File(moduleDir, "build.gradle");
            if (!buildFile.isFile()) {
                continue;
            }
            checked++;
            String script = Files.readString(buildFile.toPath(), StandardCharsets.UTF_8);
            String language = moduleDir.getName();
            checkLanguageArtifacts(violations, language, script);
            checkCatalogAlias(violations, language, CATALOG_ALIAS.matcher(script));
            if (script.contains("implementation omegatModule.providedCoreLib(")) {
                violations.add(language + ": bundles its content from the provided core libraries, "
                        + "which do not contain the language jars");
            }
            if (PROVIDED_LIBS_FILE_TREE.matcher(script).find()) {
                violations.add(language + ": resolves provided libraries with a plain fileTree; "
                        + "use omegatModule.providedCoreLib / providedModuleLib instead, which fail "
                        + "on an empty match");
            }
        }
        assertTrue("Expected to audit at least 25 language modules, found " + checked, checked >= 25);
        assertTrue(String.join("\n", violations), violations.isEmpty());
    }

    /**
     * Every LanguageTool language jar requested from the provided module libraries has
     * to be the module's own, and the module has to request it. Additional artifacts in
     * the same call, such as the part-of-speech dictionaries, are left alone.
     */
    private static void checkLanguageArtifacts(List<String> violations, String language, String script) {
        boolean bundlesOwnLanguage = false;
        Matcher calls = PROVIDED_MODULE_LIB.matcher(script);
        while (calls.find()) {
            Matcher artifacts = LANGUAGE_ARTIFACT.matcher(calls.group(1));
            while (artifacts.find()) {
                if (artifacts.group(1).equals(language)) {
                    bundlesOwnLanguage = true;
                } else {
                    violations.add(language + ": references the language jar of '"
                            + artifacts.group(1) + "'");
                }
            }
        }
        if (!bundlesOwnLanguage) {
            violations.add(language + ": language jar is not resolved through "
                    + "omegatModule.providedModuleLib('language-" + language + "'); "
                    + "an incomplete source distribution would not be detected");
        }
    }

    private static void checkCatalogAlias(List<String> violations, String language, Matcher matcher) {
        while (matcher.find()) {
            String alias = matcher.group(1);
            if (!NON_LANGUAGE_ALIASES.contains(alias) && !alias.equals(language)) {
                violations.add(language + ": depends on the version catalog alias of '" + alias + "'");
            }
        }
    }
}
