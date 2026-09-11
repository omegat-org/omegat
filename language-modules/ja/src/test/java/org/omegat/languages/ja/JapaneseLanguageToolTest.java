/*******************************************************************************
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
 ******************************************************************************/
package org.omegat.languages.ja;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Japanese;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tokenizers.ja.JapaneseWordTokenizer;

/**
 * Smoke test for LanguageTool Japanese chain (kuromoji tokenizer, tagger,
 * rule check) against single application-provided icu4j; module bundles no
 * own copy anymore.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class JapaneseLanguageToolTest {

    @Test
    public void testTokenizerSplitsJapaneseText() {
        // Tokenizer emits "surface partOfSpeech lemma" triples
        List<String> tokens = new JapaneseWordTokenizer().tokenize("私は日本語を勉強します。");
        assertThat(tokens).contains("日本語 名詞 日本語", "勉強 名詞 勉強", "し 動詞 する");
    }

    @Test
    public void testLanguageToolCheckRuns() throws Exception {
        JLanguageTool lt = new JLanguageTool(new Japanese());
        List<RuleMatch> matches = lt.check("これはテストです。");
        assertThat(matches).isEmpty();
    }
}
