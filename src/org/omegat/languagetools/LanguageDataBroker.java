/*******************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
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
 ******************************************************************************/

package org.omegat.languagetools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.languagetool.Language;
import org.languagetool.broker.ResourceDataBroker;

public class LanguageDataBroker implements ResourceDataBroker {

    private final String resourceDir;
    private final String rulesDir;

    public LanguageDataBroker(String resourceDir, String rulesDir) {
        this.resourceDir = resourceDir == null ? "" : resourceDir;
        this.rulesDir = rulesDir == null ? "" : rulesDir;
    }

    public LanguageDataBroker() {
        this(ResourceDataBroker.RESOURCE_DIR, ResourceDataBroker.RULES_DIR);
    }

    @Override
    public @NotNull URL getFromResourceDirAsUrl(String path) {
        String completePath = getCompleteResourceUrl(path);
        URL resource = getAsURL(completePath);
        return Objects.requireNonNull(resource);
    }

    @Override
    public @NotNull List<URL> getFromResourceDirAsUrls(String path) {
        String completePath = getCompleteResourceUrl(path);
        List<URL> resources = getAsURLs(completePath.substring(1));
        return Objects.requireNonNull(resources);
    }

    private String getCompleteRulesUrl(String path) {
        return this.appendPath(this.rulesDir, path);
    }

    private String getCompleteResourceUrl(String path) {
        return this.appendPath(this.resourceDir, path);
    }

    @Override
    public boolean resourceExists(String path) {
        String completePath = this.getCompleteResourceUrl(path);
        return this.getAsURL(completePath) != null;
    }

    private String appendPath(String baseDir, String path) {
        StringBuilder completePath = new StringBuilder(baseDir);
        if (!rulesDir.endsWith("/") && !path.startsWith("/")) {
            completePath.append('/');
        }

        if (rulesDir.endsWith("/") && path.startsWith("/") && path.length() > 1) {
            completePath.append(path.substring(1));
        } else {
            completePath.append(path);
        }

        return completePath.toString();
    }

    @Override
    public boolean ruleFileExists(String path) {
        String completePath = this.getCompleteRulesUrl(path);
        return this.getAsURL(completePath) != null;
    }

    @Override
    public @NotNull InputStream getFromResourceDirAsStream(String path) {
        String completePath = getCompleteResourceUrl(path);
        return Objects.requireNonNull(getAsStream(completePath));
    }

    @Override
    public @NotNull List<String> getFromResourceDirAsLines(String path) {
        try (InputStream inputStram = getFromResourceDirAsStream(path);
             InputStreamReader inputStreamReader = new InputStreamReader(inputStram, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            return bufferedReader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Language getLanguage() {
        org.omegat.util.Language omLang = LanguageManager.getProjectTargetLanguage();
        if (omLang == null) {
            return null;
        }
        Language lang = LanguageManager.getLTLanguage(omLang.getLanguageCode(), omLang.getCountryCode());
        if (lang == null) {
            lang = LanguageManager.getLTLanguage("en", "US");
        }
        return lang;
    }

    @Override
    public InputStream getAsStream(String path) {
        InputStream inputStream = ResourceDataBroker.class.getResourceAsStream(path);
        if (inputStream == null) {
            Language lang = getLanguage();
            if (lang != null) {
                inputStream = lang.getClass().getResourceAsStream(path);
            }
        }
        return inputStream;
    }

    @Override
    public URL getAsURL(String path) {
        URL url = ResourceDataBroker.class.getResource(path);
        if (url == null) {
            Language lang = getLanguage();
            if (lang != null) {
                url = lang.getClass().getResource(path);
            }
        }
        return url;
    }

    @Override
    public @NotNull List<URL> getAsURLs(String path) {
        Enumeration<URL> enumeration = null;
        try {
            enumeration = ResourceDataBroker.class.getClassLoader().getResources(path);
        } catch (IOException ignored) {
        }
        if (enumeration != null) {
            List<URL> urls = Collections.list(enumeration);
            if (!urls.isEmpty()) {
                return urls;
            }
        }
        Language lang = getLanguage();
        if (lang == null) {
            return Collections.emptyList();
        }
        try {
            enumeration = lang.getClass().getClassLoader().getResources(path);
        } catch (IOException ignored) {
            return Collections.emptyList();
        }
        return Collections.list(enumeration);
    }

    @Override
    public @NotNull URL getFromRulesDirAsUrl(String path) {
        String completePath = getCompleteRulesUrl(path);
        return Objects.requireNonNull(getAsURL(completePath));
    }

    @Override
    public @NotNull InputStream getFromRulesDirAsStream(String path) {
        String completePath = getCompleteRulesUrl(path);
        InputStream inputStream = getAsStream(completePath);
        return Objects.requireNonNull(inputStream);
    }

    @Override
    public String getResourceDir() {
        return resourceDir;
    }

    @Override
    public String getRulesDir() {
        return rulesDir;
    }

    @Override
    public ResourceBundle getResourceBundle(String baseName, Locale locale) {
        return ResourceBundle.getBundle(baseName, locale);
    }
}
