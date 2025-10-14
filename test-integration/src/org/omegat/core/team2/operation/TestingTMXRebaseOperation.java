/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
               2025 Hiroshi Miura
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
package org.omegat.core.team2.operation;

import org.madlonkay.supertmxmerge.StmProperties;
import org.madlonkay.supertmxmerge.SuperTmxMerge;
import org.madlonkay.supertmxmerge.data.ITuv;
import org.madlonkay.supertmxmerge.data.Key;
import org.madlonkay.supertmxmerge.data.ResolutionStrategy;
import org.omegat.core.KnownException;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SyncTMX;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

import static org.omegat.core.data.TestTeamIntegrationChild.CONCURRENT_NAME;

public class TestingTMXRebaseOperation extends TMXRebaseOperation implements IRebaseOperation {
    private final ProjectTMX projectTMX;
    private final ProjectProperties config;

    public TestingTMXRebaseOperation(ProjectTMX projectTMX, ProjectProperties config) {
        super(projectTMX, config);
        this.projectTMX = projectTMX;
        this.config = config;
    }

    long v(TMXEntry e) {
        if (e == null) {
            return 0;
        } else {
            return Long.parseLong(e.translation);
        }
    }

    String src(TMXEntry e) {
        if (e == null) {
            return "null";
        } else {
            return e.source;
        }
    }

    String tr(TMXEntry e) {
        if (e == null) {
            return "null";
        } else {
            return e.translation;
        }
    }

    @Override
    protected ProjectTMX mergeTMX(ProjectTMX baseTMX, ProjectTMX headTMX, StringBuilder commitDetails) {
        Log.log("Base:   " + baseTMX);
        Log.log("Mine:   " + projectTMX);
        Log.log("Theirs: " + headTMX);
        if (isInvalidMergeInput(baseTMX, projectTMX)) {
            Log.log("'Mine' TM is not a valid derivative of 'Base' TM");
            throw new KnownException("TMXMerge: 'Mine' TM is not a valid derivative of 'Base' TM");
        }
        if (isInvalidMergeInput(baseTMX, headTMX)) {
            Log.log("'Theirs' TM is not a valid derivative of 'Base' TM");
            throw new KnownException("TMXMerge: 'Theirs' TM is not a valid derivative of 'Base' TM");
        }
        StmProperties props = new StmProperties().setLanguageResource(OStrings.getResourceBundle())
                .setResolutionStrategy(new ResolutionStrategy() {
                    @Override
                    public ITuv resolveConflict(Key key, ITuv baseTuv, ITuv projectTuv, ITuv headTuv) {
                        TMXEntry enBase = baseTuv != null
                                ? (TMXEntry) baseTuv.getUnderlyingRepresentation()
                                : null;
                        TMXEntry enProject = projectTuv != null
                                ? (TMXEntry) projectTuv.getUnderlyingRepresentation()
                                : null;
                        TMXEntry enHead = headTuv != null
                                ? (TMXEntry) headTuv.getUnderlyingRepresentation()
                                : null;
                        String s = "Rebase " + src(enProject) + " base=" + tr(enBase) + " head="
                                + tr(enHead) + " project=" + tr(enProject);
                        if (enProject != null && CONCURRENT_NAME.equals(enProject.source)) {
                            if (v(enHead) < v(enBase)) {
                                throw new RuntimeException("Rebase HEAD: wrong concurrent: " + s);
                            }
                            if (v(enProject) < v(enBase)) {
                                throw new RuntimeException("Rebase project: wrong concurrent: " + s);
                            }
                            if (v(enHead) > v(enProject)) {
                                System.err.println(s + ": result=head");
                                return headTuv;
                            } else {
                                System.err.println(s + ": result=project");
                                return projectTuv;
                            }
                        } else {
                            throw new RuntimeException("Rebase error: non-concurrent entry: " + s);
                        }
                    }
                });
        String srcLang = config.getSourceLanguage().getLanguage();
        String trgLang = config.getTargetLanguage().getLanguage();
        ProjectTMX mergedTMX = SuperTmxMerge.merge(
                new SyncTMX(baseTMX, OStrings.getString("TMX_MERGE_BASE"), srcLang, trgLang),
                new SyncTMX(projectTMX, OStrings.getString("TMX_MERGE_MINE"), srcLang, trgLang),
                new SyncTMX(headTMX, OStrings.getString("TMX_MERGE_THEIRS"), srcLang, trgLang), props);
        Log.log("Merged: " + mergedTMX);
        if (isInvalidMergeInput(baseTMX, mergedTMX)) {
            Log.log("'Merged' TM is not a valid derivative of 'Base' TM");
            throw new KnownException("TMXMerge: 'Merged' TM is not a valid derivative of 'Base' TM");
        }
        commitDetails.append('\n');
        commitDetails.append(props.getReport().toString());
        return mergedTMX;
    }

    /**
     * Check a TM against the base TM to ensure it's a valid modification of
     * the base. This integration test never deletes entries, only adds or
     * modifies them, so modified versions must be supersets of their base
     * versions.
     *
     * @param base
     *            Base TM from which the other TM is derived
     * @param other
     *            Other TM
     * @return true when invalid. false when valid
     */
    private boolean isInvalidMergeInput(ProjectTMX base, ProjectTMX other) {
        return !other.getDefaultKeys().containsAll(base.getDefaultKeys())
                || !other.getAlternativeKeys().containsAll(base.getAlternativeKeys());
    }

}
