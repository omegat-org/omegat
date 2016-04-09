/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.core.spellchecker;

import java.io.UnsupportedEncodingException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.languagetool.rules.spelling.hunspell.Hunspell;
import org.omegat.util.Log;

/**
 * A thin wrapper around the LanguageTool Hunspell implementation (which itself
 * wraps native libs)
 * 
 * @author Aaron Madlon-Kay
 */
public class SpellCheckerLangToolHunspell implements ISpellCheckerProvider {

    private final String dictBasename;
    private final Hunspell.Dictionary dict;

    public SpellCheckerLangToolHunspell(String dictBasename) throws Throwable {
        this.dictBasename = dictBasename;

        // Here we are going to obtain the dictionary by ultimately calling
        // Hunspell_create(String, String).
        //
        // When we do so, the Java strings for the dictionary file paths will be
        // encoded according to JNA's Native.getDefaultEncoding(), which is
        // either the property jna.encoding (if set) or UTF-8. To handle
        // non-ASCII paths on Windows we need this encoding to be the system's
        // native encoding (Cp1252, etc.) and the only way to set this appears
        // to be globally via jna.encoding.
        //
        // We use other libraries that use JNA under the hood, and it's not
        // clear that setting jna.encoding won't have some sort of adverse
        // effect on them. Hunspell only takes Java strings for this call, so
        // we set the encoding, get our dictionary, then set it back and hope
        // that nothing bad happens.
        //
        // Discussion here:
        // https://groups.google.com/d/msg/jna-users/FdcRn_F6Qts/aG-o8AZkBgAJ
        String origEncoding = System.getProperty("jna.encoding");
        System.setProperty("jna.encoding", Charset.defaultCharset().name());
        this.dict = Hunspell.getInstance().getDictionary(dictBasename);
        System.setProperty("jna.encoding", origEncoding);
    }

    @Override
    public boolean isCorrect(String word) {
        return !dict.misspelled(word);
    }

    @Override
    public List<String> suggest(String word) {
        try {
            return dict.suggest(word);
        } catch (CharacterCodingException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void learnWord(String word) {
        try {
            dict.addWord(word);
        } catch (UnsupportedEncodingException e) {
            Log.log(e);
        }
    }

    @Override
    public void destroy() {
        dict.destroy();
        Hunspell.getInstance().destroyDictionary(dictBasename);
    }
}
