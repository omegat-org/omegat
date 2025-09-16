/*******************************************************************************
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023-2025 Hiroshi Miura
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.omegat.core.statistics;

import org.omegat.core.data.ExternalTMFactory;
import org.omegat.core.data.ExternalTMX;
import org.omegat.core.data.IProject;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.IParseCallback;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.po.PoFilter;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.tokenizer.LuceneEnglishTokenizer;
import org.omegat.util.Language;
import org.omegat.util.Log;

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
    private final Segmenter segmenter;
    private final FilterMaster filterMaster;

    public TestingProject(Path tmpDir) {
        super();
        prop = new TestingProjectProperties(tmpDir);
        filterMaster = new FilterMaster(FilterMaster.createDefaultFiltersConfig());
        segmenter = new Segmenter(SRX.getDefault());
        projectTMX = new ProjectTMX(new Language("en"), new Language("ca"), true,
                Paths.get("test/data/tmx/empty.tmx").toFile(), null, segmenter);
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
        IParseCallback testCallback = new TestingParseCallback(ste);
        FilterContext context = new FilterContext(new Language("en"), new Language("ca"), true);
        try {
            filter.parseFile(testSource.toFile(), Collections.emptyMap(), context, testCallback);
        } catch (Exception e) {
            Log.log(e);
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
        TestingAllTranslations r = new TestingAllTranslations();
        synchronized (projectTMX) {
            r.setDefaultTranslation(projectTMX.getDefaultTranslation(ste.getSrcText()));
            r.setAlternativeTranslation(projectTMX.getMultipleTranslation(ste.getKey()));
            if (r.getAlternativeTranslation() != null) {
                r.setCurrentTranslation(r.getAlternativeTranslation());
            } else if (r.getDefaultTranslation() != null) {
                r.setCurrentTranslation(r.getDefaultTranslation());
            } else {
                r.setCurrentTranslation(EMPTY_TRANSLATION);
            }
            if (r.getDefaultTranslation() == null) {
                r.setDefaultTranslation(EMPTY_TRANSLATION);
            }
            if (r.getAlternativeTranslation() == null) {
                r.setAlternativeTranslation(EMPTY_TRANSLATION);
            }
        }
        return r;
    }

    @Override
    public Map<String, ExternalTMX> getTransMemories() {
        synchronized (projectTMX) {
            if (transMemories == null) {
                transMemories = new TreeMap<>();
                try {
                    ExternalTMX newTMX;
                    Path testTmx = Paths.get("test/data/tmx/test-match-stat-en-ca.tmx");
                    newTMX = ExternalTMFactory.load(testTmx.toFile(), prop, segmenter, filterMaster);
                    transMemories.put(testTmx.toString(), newTMX);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return Collections.unmodifiableMap(transMemories);
    }

    protected static class TestingProjectProperties extends ProjectProperties {
        TestingProjectProperties(Path tmpDir) {
            super();
            setSourceLanguage(new Language("en"));
            setSourceTokenizer(LuceneEnglishTokenizer.class);
            setSentenceSegmentingEnabled(false);
            setTargetLanguage(new Language("ca"));
            setTargetTokenizer(DefaultTokenizer.class);
            setProjectRoot(tmpDir.toString());
        }
    }

    public static class TestingAllTranslations extends IProject.AllTranslations {
        public void setAlternativeTranslation(TMXEntry entry) {
            alternativeTranslation = entry;
        }

        public void setDefaultTranslation(TMXEntry entry) {
            defaultTranslation = entry;
        }

        public void setCurrentTranslation(TMXEntry entry) {
            currentTranslation = entry;
        }
    }

}
