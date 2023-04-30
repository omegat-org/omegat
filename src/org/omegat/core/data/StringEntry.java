/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.core.data;

import org.omegat.util.StringUtil;

/*
 * String entry represents a unique translatable string
 * (a single string may occur many times in data files, but only
 *  one StringEntry is created for it).
 * Multiple translations can still exist for the single string, however.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 */
public class StringEntry {
    /** Creates a new string entry for a unique translatable string. */
    public StringEntry(String srcText) {
        mSrcText = srcText;
        mTranslation = "";
    }

    /** Returns the source string */
    public String getSrcText() {
        return mSrcText;
    }

    // these methods aren't sychronized - thought about doing so, but
    // as the translation is set by user action, any race condition
    // would be the same as user pressing 'enter' key a few milliseconds
    // before or after they actually did, making the condition trivial
    // if more processing happens here later, readdress synchronization
    // issues

    /**
     * Returns the translation of the StringEntry.
     */
    public String getTranslation() {
        return mTranslation;
    }

    /**
     * Sets the translation of the StringEntry. If translation given is null or
     * equal to the source, than the empty string is set as a translation to
     * indicate that there's no translation.
     */
    public void setTranslation(String trans) {
        if (trans == null) {
            trans = "";
        }
        mTranslation = trans;
    }

    /**
     * Returns whether the given string entry is already translated.
     */
    public boolean isTranslated() {
        return !StringUtil.isEmpty(mTranslation);
    }

    private String mSrcText;
    private String mTranslation;
}
