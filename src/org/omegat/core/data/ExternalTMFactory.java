/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
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

package org.omegat.core.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXReader2;

/**
 * Common utility class for external TMs.
 * 
 * @author Aaron Madlon-Kay
 *
 */
public class ExternalTMFactory {

    public static boolean isSupported(File file) {
        return TMXLoader.isSupported(file);
    }

    public static ExternalTMX load(File file) throws Exception {
        ProjectProperties props = Core.getProject().getProjectProperties();
        if (TMXLoader.isSupported(file)) {
            return new TMXLoader(file)
                    .setExtTmxLevel2(Preferences.isPreference(Preferences.EXT_TMX_SHOW_LEVEL2))
                    .setUseSlash(Preferences.isPreference(Preferences.EXT_TMX_USE_SLASH))
                    .setDoSegmenting(props.isSentenceSegmentingEnabled())
                    .load(props.getSourceLanguage(), props.getTargetLanguage());
        } else {
            throw new IllegalArgumentException("Unsupported external TM type: " + file.getName());
        }
    }

    public static class TMXLoader {
        public static boolean isSupported(File file) {
            String name = file.getName().toLowerCase();
            return name.endsWith(OConsts.TMX_EXTENSION) || name.endsWith(OConsts.TMX_GZ_EXTENSION);
        }

        private final File file;
        private boolean extTmxLevel2;
        private boolean useSlash;
        private boolean doSegmenting;
    
        public TMXLoader(File file) {
            this.file = file;
        }
    
        public TMXLoader setExtTmxLevel2(boolean extTmxLevel2) {
            this.extTmxLevel2 = extTmxLevel2;
            return this;
        }
    
        public TMXLoader setUseSlash(boolean useSlash) {
            this.useSlash = useSlash;
            return this;
        }

        public TMXLoader setDoSegmenting(boolean doSegmenting) {
            this.doSegmenting = doSegmenting;
            return this;
        }
    
        public ExternalTMX load(Language sourceLang, Language targetLang) throws Exception {
            return new ExternalTMX(file.getName(), loadImpl(file, doSegmenting, sourceLang, targetLang));
        }
    
        private List<PrepareTMXEntry> loadImpl(File file, boolean doSegmenting, Language sourceLang,
                Language targetLang) throws Exception {
            List<PrepareTMXEntry> entries = new ArrayList<>();
    
            TMXReader2.LoadCallback loader = new TMXReader2.LoadCallback() {
                public boolean onEntry(TMXReader2.ParsedTu tu, TMXReader2.ParsedTuv tuvSource,
                        TMXReader2.ParsedTuv tuvTarget, boolean isParagraphSegtype) {
                    if (tuvSource == null) {
                        return false;
                    }
    
                    if (tuvTarget != null) {
                        // add only target Tuv
                        addTuv(tu, tuvSource, tuvTarget, isParagraphSegtype);
                    } else {
                        // add all non-source Tuv
                        for (int i = 0; i < tu.tuvs.size(); i++) {
                            if (tu.tuvs.get(i) != tuvSource) {
                                addTuv(tu, tuvSource, tu.tuvs.get(i), isParagraphSegtype);
                            }
                        }
                    }
                    return true;
                }
    
                private void addTuv(TMXReader2.ParsedTu tu, TMXReader2.ParsedTuv tuvSource,
                        TMXReader2.ParsedTuv tuvTarget, boolean isParagraphSegtype) {
                    String changer = StringUtil.nvl(tuvTarget.changeid, tuvTarget.creationid, tu.changeid,
                            tu.creationid);
                    String creator = StringUtil.nvl(tuvTarget.creationid, tu.creationid);
                    long changed = StringUtil.nvlLong(tuvTarget.changedate, tuvTarget.creationdate,
                            tu.changedate, tu.creationdate);
                    long created = StringUtil.nvlLong(tuvTarget.creationdate, tu.creationdate);
    
                    List<String> sources = new ArrayList<String>();
                    List<String> targets = new ArrayList<String>();
                    Core.getSegmenter().segmentEntries(doSegmenting && isParagraphSegtype, sourceLang,
                            tuvSource.text, targetLang, tuvTarget.text, sources, targets);
    
                    for (int i = 0; i < sources.size(); i++) {
                        PrepareTMXEntry te = new PrepareTMXEntry();
                        te.source = sources.get(i);
                        te.translation = targets.get(i);
                        te.changer = changer;
                        te.changeDate = changed;
                        te.creator = creator;
                        te.creationDate = created;
                        te.note = tu.note;
                        te.otherProperties = tu.props;
                        entries.add(te);
                    }
                }
            };
    
            TMXReader2 reader = new TMXReader2();
            reader.readTMX(file, sourceLang, targetLang, doSegmenting, false, extTmxLevel2, useSlash, loader);
    
            return entries;
        }
    }
}
