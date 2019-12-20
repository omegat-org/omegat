/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Aaron Madlon-Kay, Alex Buloichik
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

package org.omegat.gui.glossary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import org.omegat.util.StringUtil;

/**
 * An entry in the glossary.
 *
 * @author Keith Godfrey
 * @author Aaron Madlon-Kay
 * @author Alex Buloichik
 */
public class GlossaryEntry {
    public GlossaryEntry(String src, String[] loc, String[] com, boolean[] fromPriorityGlossary, String[] origins) {
        mSource = StringUtil.normalizeUnicode(src);
        mTargets = loc;
        normalize(mTargets);
        mComments = com;
        normalize(com);
        mPriorities = fromPriorityGlossary;
        mOrigins = origins;
    }

    public GlossaryEntry(String src, String loc, String com, boolean fromPriorityGlossary, String origin) {
        this(src, new String[] { loc }, new String[] { com }, new boolean[] { fromPriorityGlossary },
                new String[] { origin });
    }

    public String getSrcText() {
        return mSource;
    }

    /**
     * Return the first target-language term string.
     *
     * Glossary entries can have multiple target strings
     * if they have been combined for display purposes.
     * Access all target strings with {@link GlossaryEntry#getLocTerms(boolean)}.
     *
     * @return The first target-language term string
     */
    public String getLocText() {
        return mTargets.length > 0 ? mTargets[0] : "";
    }

    /**
     * Return each individual target-language term that
     * corresponds to the source term.
     *
     * @param uniqueOnly Whether or not to filter duplicates from the list
     * @return All target-language terms
     */
    public String[] getLocTerms(boolean uniqueOnly) {
        if (!uniqueOnly || mTargets.length == 1) {
            return mTargets;
        }
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < mTargets.length; i++) {
            if (i > 0 && mTargets[i].equals(mTargets[i - 1])) {
                continue;
            }
            list.add(mTargets[i]);
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Return the first comment string.
     *
     * Glossary entries can have multiple comment strings
     * if they have been combined for display purposes.
     * Access all comment strings with {@link GlossaryEntry#getComments()}.
     *
     * @return The first comment string
     */
    public String getCommentText() {
        return mComments.length > 0 ? mComments[0] : "";
    }

    public String[] getComments() {
        return mComments;
    }

    public boolean getPriority() {
        return mPriorities.length > 0 ? mPriorities[0] : false;
    }

    public boolean[] getPriorities() {
        return mPriorities;
    }

    public String[] getOrigins(boolean uniqueOnly) {
        if (!uniqueOnly || mOrigins.length == 1) {
            return mOrigins;
        }
        return Stream.of(mOrigins).distinct().toArray(String[]::new);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        GlossaryEntry otherGlossaryEntry = (GlossaryEntry) o;

        return Objects.equals(this.mSource, otherGlossaryEntry.mSource)
                && Arrays.equals(this.mTargets, otherGlossaryEntry.mTargets)
                && Arrays.equals(this.mComments, otherGlossaryEntry.mComments);
    }

    @Override
    public int hashCode() {
        int hash = 98;
        hash = hash * 17 + (mSource == null ? 0 : mSource.hashCode());
        hash = hash * 31 + (mTargets == null ? 0 : Arrays.hashCode(mTargets));
        hash = hash * 13 + (mComments == null ? 0 : Arrays.hashCode(mComments));
        return hash;
    }

    private void normalize(String[] strs) {
        for (int i = 0; i < strs.length; i++) {
            strs[i] = StringUtil.normalizeUnicode(strs[i]);
        }
    }

    private String mSource;
    private String[] mTargets;
    private String[] mComments;
    private boolean[] mPriorities;
    private String[] mOrigins;
}
