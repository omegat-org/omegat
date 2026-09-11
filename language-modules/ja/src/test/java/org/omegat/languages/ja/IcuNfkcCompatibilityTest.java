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

import java.text.Normalizer;
import java.util.function.Consumer;

import org.junit.Test;

import com.ibm.icu.text.Normalizer2;

/**
 * Proves icu4j version shipped in application lib/ covers NFKC surface
 * historically served by icu4j copy bundled with LanguageTool Japanese
 * (gosen tokenizer era; omt builds tokenize through kuromoji and reference
 * no com.ibm.icu class, so module now relies on application copy alone).
 *
 * Expectations cross-checked against independent JDK implementation and
 * pinned by golden sentences; both verified identical on icu4j 72.1, 78.1
 * and 78.3 before bundled copy was dropped. Unicode normalization stability
 * policy keeps assigned code point results stable across future upgrades.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class IcuNfkcCompatibilityTest {

    /** Same legacy entry point gosen's GosenNormalizerCharFilter used. */
    private static final Normalizer2 NFKC = Normalizer2.getInstance(null, "nfkc",
            Normalizer2.Mode.COMPOSE);
    private static final Normalizer2 NFKD = Normalizer2.getInstance(null, "nfkc",
            Normalizer2.Mode.DECOMPOSE);

    /** Combining and halfwidth (han)dakuten marks: U+3099 U+309A U+FF9E U+FF9F. */
    private static final char[] VOICING_MARKS = {'\u3099', '\u309A', '\uFF9E', '\uFF9F'};

    /** Stable Japanese-relevant blocks: enclosed alphanumerics, punctuation
     * and kana, enclosed and compat CJK, compat forms, half/fullwidth forms. */
    private static final int[][] STABLE_BLOCKS = {
        {0x2460, 0x24FF}, {0x3000, 0x30FF}, {0x3200, 0x33FF}, {0xFE30, 0xFE4F}, {0xFF00, 0xFFEF},
    };

    @Test
    public void testGoldenSentences() {
        assertThat(NFKC.normalize("ﾃﾞｼﾞﾀﾙｶﾒﾗで撮った写真をパソコンに保存した。"))
                .isEqualTo("デジタルカメラで撮った写真をパソコンに保存した。");
        assertThat(NFKC.normalize("全角の１２３４５と半角の12345を比較する。"))
                .isEqualTo("全角の12345と半角の12345を比較する。");
        assertThat(NFKC.normalize("㌧㌔などの組文字は正規化で展開される。"))
                .isEqualTo("トンキロなどの組文字は正規化で展開される。");
        assertThat(NFKC.normalize("彼はｶﾞｷﾞｸﾞｹﾞｺﾞと書いた。"))
                .isEqualTo("彼はガギグゲゴと書いた。");
        assertThat(NFKC.normalize("①②③の順番で進めてください。"))
                .isEqualTo("123の順番で進めてください。");
        assertThat(NFKC.normalize("がぎぐは合成済みのがぎぐになる。"))
                .isEqualTo("がぎぐは合成済みのがぎぐになる。");
    }

    @Test
    public void testKanaVoicingSequencesAgreeWithJdk() {
        forEachKanaVoicingSequence(this::assertAgreesWithJdk);
    }

    @Test
    public void testStableBlockCodePointsAgreeWithJdk() {
        forEachStableBlockCodePoint(this::assertAgreesWithJdk);
    }

    @Test
    public void testNormalizationInvariants() {
        forEachKanaVoicingSequence(this::assertInvariants);
        forEachStableBlockCodePoint(this::assertInvariants);
    }

    private void forEachKanaVoicingSequence(Consumer<String> check) {
        for (int cp = 0x3041; cp <= 0x30FF; cp++) {
            checkWithMarks(cp, check);
        }
        for (int cp = 0xFF61; cp <= 0xFFEE; cp++) {
            checkWithMarks(cp, check);
        }
    }

    private void checkWithMarks(int cp, Consumer<String> check) {
        // Skip code points JDK's older Unicode data does not know yet
        if (!Character.isDefined(cp)) {
            return;
        }
        for (char mark : VOICING_MARKS) {
            check.accept(new String(Character.toChars(cp)) + mark);
        }
    }

    private void forEachStableBlockCodePoint(Consumer<String> check) {
        for (int[] block : STABLE_BLOCKS) {
            for (int cp = block[0]; cp <= block[1]; cp++) {
                // Skip code points JDK's older Unicode data does not know yet
                if (!Character.isDefined(cp)) {
                    continue;
                }
                check.accept(new String(Character.toChars(cp)));
            }
        }
    }

    private void assertAgreesWithJdk(String input) {
        assertThat(NFKC.normalize(input))
                .as("NFKC of %s", hex(input))
                .isEqualTo(Normalizer.normalize(input, Normalizer.Form.NFKC));
        assertThat(NFKD.normalize(input))
                .as("NFKD of %s", hex(input))
                .isEqualTo(Normalizer.normalize(input, Normalizer.Form.NFKD));
    }

    private void assertInvariants(String input) {
        String composed = NFKC.normalize(input);
        assertThat(NFKC.normalize(composed))
                .as("NFKC idempotence for %s", hex(input))
                .isEqualTo(composed);
        assertThat(NFKC.normalize(NFKD.normalize(input)))
                .as("NFKC after NFKD for %s", hex(input))
                .isEqualTo(composed);
    }

    private static String hex(String s) {
        StringBuilder sb = new StringBuilder();
        s.codePoints().forEach(cp -> sb.append(String.format("U+%04X ", cp)));
        return sb.toString().trim();
    }
}
