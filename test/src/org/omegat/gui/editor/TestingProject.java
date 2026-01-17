/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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
package org.omegat.gui.editor;

import org.omegat.core.data.ExternalTMFactory;
import org.omegat.core.data.ExternalTMX;
import org.omegat.core.data.IProject;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.po.PoFilter;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.util.Language;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TestingProject extends NotLoadedProject implements IProject {
    private final ProjectProperties prop;

    private final ProjectTMX projectTMX;
    private Map<String, ExternalTMX> transMemories;

    TestingProject(ProjectProperties prop) {
        super();
        this.prop = prop;
        projectTMX = new ProjectTMX(new Language("en"), new Language("ca"), true,
                Paths.get("test/data/tmx/en-ca.tmx").toFile(), null);
    }

    @Override
    public ProjectProperties getProjectProperties() {
        return prop;
    }

    @Override
    public TMXEntry getTranslationInfo(SourceTextEntry ste) {
        if (projectTMX == null) {
            return EMPTY_TRANSLATION;
        }
        TMXEntry r = projectTMX.getMultipleTranslation(ste.getKey());
        if (r == null) {
            r = projectTMX.getDefaultTranslation(ste.getSrcText());
        }
        if (r == null) {
            r = EMPTY_TRANSLATION;
        }
        return r;
    }

    @Override
    public List<SourceTextEntry> getAllEntries() {
        List<SourceTextEntry> ste = new ArrayList<>();
        IFilter filter = new PoFilter();
        Path testSource = Paths.get("test/data/filters/po/file-POFilter-match-stat-en-ca.po");
        FilterContext context = new FilterContext(new Language("en"), new Language("ca"), true);
        try {
            filter.parseFile(testSource.toFile(), Collections.emptyMap(), context, null);
        } catch (Exception ignored) {
        }
        return ste;
    }

    @Override
    public ITokenizer getSourceTokenizer() {
        return new LuceneEnglishTokenizer();
    }

    @Override
    public ITokenizer getTargetTokenizer() {
        return new DefaultTokenizer();
    }

    @Override
    public Map<Language, ProjectTMX> getOtherTargetLanguageTMs() {
        return Collections.emptyMap();
    }

    @Override
    public AllTranslations getAllTranslations(SourceTextEntry ste) {
        return null;
    }

    @Override
    public Map<String, ExternalTMX> getTransMemories() {
        synchronized (projectTMX) {
            if (transMemories == null) {
                transMemories = new TreeMap<>();
                try {
                    ExternalTMX newTMX;
                    Path testTmx = Paths.get("test/data/tmx/test-match-stat-en-ca.tmx");
                    newTMX = ExternalTMFactory.load(testTmx.toFile());
                    transMemories.put(testTmx.toString(), newTMX);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return Collections.unmodifiableMap(transMemories);
    }
}
