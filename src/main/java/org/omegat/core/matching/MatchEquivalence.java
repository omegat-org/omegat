/**************************************************************************
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
 **************************************************************************/

package org.omegat.core.matching;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.UnicodeSet;

import org.jspecify.annotations.Nullable;

import org.omegat.core.data.TeamSetting;
import org.omegat.core.data.TeamSettingsRegistry;
import org.omegat.util.OStrings;

/**
 * Named equivalence classes of character variants that fuzzy matching can
 * treat as equal (feature request #1681). Each class folds its members to one
 * representative character, so segments differing only in such variants
 * compare as equal. All classes are active by default; a project can disable
 * individual classes.
 * <p>
 * Quotation marks fold in two separate groups, double-quote variants and
 * single-quote variants, following the proposal in #1681: a straight double
 * quote matches curly double quotes but not single quotes. Prime marks and
 * other lookalikes that are not quotation marks stay untouched. The
 * invisible-format class folds to the empty string, removing the characters
 * from comparison entirely.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public enum MatchEquivalence {

    /**
     * Opening and closing quotation marks of all scripts plus apostrophe
     * variants: apostrophes and single quotes share code points, so folding
     * cannot separate the two roles and they form one class. Double variants
     * fold to the straight double quote, single and apostrophe variants
     * (including the modifier letter apostrophe) to the right single
     * quotation mark. U+2019 is the representative because polished TM
     * content already uses the typographic form: folding toward it keeps the
     * historic verbatim tokenization of such content unchanged, and
     * straight-typed text adopts that segmentation.
     */
    QUOTES("EQUIVALENCE_CLASS_QUOTES",
            new Group(new UnicodeSet("[\"«»“”„‟⹂❝❞❠〝〞〟＂「」『』｢｣🙶🙷🙸]"), "\""),
            new Group(new UnicodeSet("['‘’‚‛‹›❛❜❟＇ʼ]"), "’")),

    /** Hyphen, dash and minus variants. */
    DASHES("EQUIVALENCE_CLASS_DASHES",
            new Group(new UnicodeSet("[\\-‐‑‒–—―−﹘﹣－]"), "-")),

    /** Space variants: every Unicode space separator folds to plain space. */
    SPACES("EQUIVALENCE_CLASS_SPACES",
            new Group(new UnicodeSet("[[:Zs:]]"), " ")),

    /**
     * Invisible formatting characters removed from comparison: soft hyphen,
     * bidi controls, zero-width space, word joiner, BOM. ZWNJ and ZWJ stay
     * untouched: they are meaning-bearing in Persian and Indic scripts.
     */
    INVISIBLES("EQUIVALENCE_CLASS_INVISIBLES",
            new Group(new UnicodeSet(
                    "[\\u00AD\\u061C\\u200B\\u200E\\u200F\\u202A-\\u202E\\u2060\\u2066-\\u2069\\uFEFF]"),
                    ""));

    /** One folding group: a set of characters and their shared replacement. */
    private static final class Group {
        private final UnicodeSet chars;
        private final String replacement;

        Group(UnicodeSet chars, String replacement) {
            this.chars = chars.freeze();
            this.replacement = replacement;
        }
    }

    private static final Normalizer2 NFC = Normalizer2.getNFCInstance();
    private static final Normalizer2 NFD = Normalizer2.getNFDInstance();

    private final String labelKey;
    private final Group[] groups;

    MatchEquivalence(String labelKey, Group... groups) {
        this.labelKey = labelKey;
        this.groups = groups;
    }

    /** Key of the team-negotiated setting in the project settings file. */
    public static final String TEAM_SETTING_KEY = "match_equivalence_disabled";

    /** Identifier used in the project settings file, stable across locales. */
    public String getId() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Localized display name of the class. */
    public String getLocalizedName() {
        return OStrings.getString(labelKey);
    }

    /**
     * Code points of the class in display order, mapped to their replacement
     * (empty string means removed). Insertion-ordered for the GUI listing.
     */
    public Map<Integer, String> getMembers() {
        Map<Integer, String> members = new LinkedHashMap<>();
        for (Group group : groups) {
            for (UnicodeSet.EntryRange range : group.chars.ranges()) {
                for (int cp = range.codepoint; cp <= range.codepointEnd; cp++) {
                    members.put(cp, group.replacement);
                }
            }
        }
        return members;
    }

    /**
     * Builds a code point replacement map for the given active classes.
     */
    public static Map<Integer, String> buildFoldMap(Set<MatchEquivalence> active) {
        Map<Integer, String> map = new HashMap<>();
        for (MatchEquivalence eq : active) {
            for (Group group : eq.groups) {
                for (UnicodeSet.EntryRange range : group.chars.ranges()) {
                    for (int cp = range.codepoint; cp <= range.codepointEnd; cp++) {
                        // Identity mappings (a representative onto itself)
                        // would force a copy of nearly every string in fold.
                        if (group.replacement.codePointCount(0, group.replacement.length()) == 1
                                && group.replacement.codePointAt(0) == cp) {
                            continue;
                        }
                        map.putIfAbsent(cp, group.replacement);
                    }
                }
            }
        }
        return map;
    }

    /**
     * Folds text for comparison: canonical normalization (NFC) always, then
     * replacement of every code point claimed by the fold map. Identity for
     * text without variant characters.
     */
    public static String fold(String text, Map<Integer, String> foldMap) {
        String normalized = NFC.normalize(text);
        if (foldMap.isEmpty()) {
            return normalized;
        }
        StringBuilder sb = null;
        for (int i = 0; i < normalized.length();) {
            int cp = normalized.codePointAt(i);
            int len = Character.charCount(cp);
            String replacement = foldMap.get(cp);
            if (replacement != null && sb == null) {
                sb = new StringBuilder(normalized.length());
                sb.append(normalized, 0, i);
            }
            if (sb != null) {
                if (replacement != null) {
                    sb.append(replacement);
                } else {
                    sb.appendCodePoint(cp);
                }
            }
            i += len;
        }
        return sb == null ? normalized : sb.toString();
    }

    /**
     * Builds the subset of {@link #buildFoldMap(Set)} whose replacements keep
     * UTF-16 length: single BMP character replaced by single BMP character.
     * Safe to apply to a whole string before tokenization, so variant
     * characters cannot shift word boundaries while token offsets stay valid
     * for the original string. Removal (invisible formatting characters),
     * canonical normalization and supplementary-plane members must fold in
     * token text afterwards.
     */
    public static Map<Integer, String> buildSameLengthFoldMap(Set<MatchEquivalence> active) {
        Map<Integer, String> map = new HashMap<>();
        buildFoldMap(active).forEach((cp, replacement) -> {
            if (cp <= Character.MAX_VALUE && replacement.length() == 1) {
                map.put(cp, replacement);
            }
        });
        return map;
    }

    /** Character-by-character fold with a same-length map; no normalization. */
    public static String foldSameLength(String text, Map<Integer, String> sameLengthFoldMap) {
        if (sameLengthFoldMap.isEmpty()) {
            return text;
        }
        StringBuilder sb = null;
        for (int i = 0; i < text.length(); i++) {
            String replacement = sameLengthFoldMap.get((int) text.charAt(i));
            if (replacement != null && sb == null) {
                sb = new StringBuilder(text);
            }
            if (replacement != null) {
                sb.setCharAt(i, replacement.charAt(0));
            }
        }
        return sb == null ? text : sb.toString();
    }

    /**
     * Builds a regular expression from a plain search text (with the search
     * wildcards "*" and "?") that also matches every character variant
     * equivalent under the active classes. Equivalence works on the pattern
     * side: each variant character becomes a character class of its group,
     * and with the invisibles class active, every gap between atoms tolerates
     * invisible formatting characters in the searched text. The needle itself
     * is normalized (NFC) and cleared of invisible formatting characters.
     * Searched text and match offsets stay untouched.
     */
    public static String globToRegex(String text, Set<MatchEquivalence> active) {
        Map<Integer, String> memberClasses = buildRegexClassMap(active);
        UnicodeSet invisibles = active.contains(INVISIBLES) ? INVISIBLES.groups[0].chars : null;
        String gap = invisibles == null ? ""
                : characterClass(invisibles) + "*";
        String nonSpaceRun = active.contains(SPACES) ? "[^\\s\\p{Zs}]" : "\\S";
        String normalized = NFC.normalize(text);
        StringBuilder sb = new StringBuilder(normalized.length() * 8);
        boolean first = true;
        for (int i = 0; i < normalized.length();) {
            int cp = normalized.codePointAt(i);
            i += Character.charCount(cp);
            if (invisibles != null && invisibles.contains(cp)) {
                continue;
            }
            String atom;
            if (cp == '*') {
                atom = nonSpaceRun + "*";
            } else if (cp == '?') {
                atom = nonSpaceRun;
            } else {
                atom = memberClasses.get(cp);
                if (atom == null) {
                    atom = literalAtom(cp);
                }
            }
            if (!first) {
                sb.append(gap);
            }
            sb.append(atom);
            first = false;
        }
        if (sb.length() == 0 && !normalized.isEmpty()) {
            // A needle consisting only of invisible formatting characters
            // must not collapse to an empty pattern (which finds everything);
            // search for it literally instead.
            normalized.codePoints().forEach(cp -> sb.append(escapeLiteral(cp)));
        }
        return sb.toString();
    }

    /**
     * Literal atom for one code point; when its canonical decomposition
     * differs, both forms are alternated so decomposed text (frequent on
     * macOS) is found as well.
     */
    private static String literalAtom(int cp) {
        String composed = new String(Character.toChars(cp));
        String decomposed = NFD.normalize(composed);
        if (decomposed.equals(composed)) {
            return escapeLiteral(cp);
        }
        StringBuilder alt = new StringBuilder("(?:").append(escapeLiteral(cp)).append('|');
        decomposed.codePoints().forEach(dcp -> alt.append(escapeLiteral(dcp)));
        return alt.append(')').toString();
    }

    /** Member code point to character class of its group, for active classes. */
    private static Map<Integer, String> buildRegexClassMap(Set<MatchEquivalence> active) {
        Map<Integer, String> map = new HashMap<>();
        for (MatchEquivalence eq : active) {
            if (eq == INVISIBLES) {
                continue;
            }
            for (Group group : eq.groups) {
                String characterClass = characterClass(group.chars);
                for (UnicodeSet.EntryRange range : group.chars.ranges()) {
                    for (int cp = range.codepoint; cp <= range.codepointEnd; cp++) {
                        map.putIfAbsent(cp, characterClass);
                    }
                }
            }
        }
        return map;
    }

    private static String characterClass(UnicodeSet chars) {
        StringBuilder sb = new StringBuilder("[");
        for (UnicodeSet.EntryRange range : chars.ranges()) {
            sb.append(String.format(Locale.ROOT, "\\x{%X}", range.codepoint));
            if (range.codepointEnd != range.codepoint) {
                sb.append('-').append(String.format(Locale.ROOT, "\\x{%X}", range.codepointEnd));
            }
        }
        return sb.append(']').toString();
    }

    /** Single code point as a regex literal; alphanumerics stay readable. */
    private static String escapeLiteral(int cp) {
        if (Character.isLetterOrDigit(cp) && cp < 0x80) {
            return new String(Character.toChars(cp));
        }
        return String.format(Locale.ROOT, "\\x{%X}", cp);
    }

    /** All classes; the default state is every class active. */
    public static Set<MatchEquivalence> all() {
        return EnumSet.allOf(MatchEquivalence.class);
    }

    /**
     * Comma-separated id list for storing in the settings file,
     * alphabetically sorted so the canonical form survives classes inserted
     * into the enum later.
     */
    public static String toIdList(Set<MatchEquivalence> classes) {
        return classes.stream().map(MatchEquivalence::getId).sorted().collect(Collectors.joining(","));
    }

    /**
     * Parses a comma-separated id list. Ids compare case-insensitively
     * because the settings file is hand-editable; unknown ids are ignored so
     * files written by newer versions with more classes still load.
     */
    public static Set<MatchEquivalence> fromIdList(String idList) {
        Set<MatchEquivalence> result = EnumSet.noneOf(MatchEquivalence.class);
        for (String id : idList.split(",")) {
            for (MatchEquivalence eq : values()) {
                if (eq.getId().equals(id.trim().toLowerCase(Locale.ROOT))) {
                    result.add(eq);
                }
            }
        }
        return result;
    }

    /**
     * Team-negotiated project setting: opt-out id list of disabled classes,
     * absent key means every class active.
     */
    static TeamSetting teamSetting() {
        return TeamSetting.of(TEAM_SETTING_KEY, "EQUIVALENCE_TEAM_SETTING_NAME", config -> {
            Set<MatchEquivalence> disabled = config.getDisabledMatchEquivalences();
            return disabled.isEmpty() ? null : toIdList(disabled);
        }, (config, raw) -> config.setDisabledMatchEquivalences(
                raw == null ? EnumSet.noneOf(MatchEquivalence.class) : fromIdList(raw)),
                MatchEquivalence::describeDisabled, MatchEquivalence::normalizeIdList);
    }

    /** Registers the team setting; safe to call from every bootstrap. */
    public static void registerTeamSetting() {
        if (TeamSettingsRegistry.byKey(TEAM_SETTING_KEY) == null) {
            TeamSettingsRegistry.register(teamSetting());
        }
    }

    private static String describeDisabled(@Nullable String raw) {
        Set<MatchEquivalence> disabled = raw == null ? EnumSet.noneOf(MatchEquivalence.class)
                : fromIdList(raw);
        if (disabled.isEmpty()) {
            return OStrings.getString("EQUIVALENCE_TEAM_SETTING_ALL_ACTIVE");
        }
        return MessageFormat.format(OStrings.getString("EQUIVALENCE_TEAM_SETTING_DISABLED"),
                disabled.stream().sorted().map(MatchEquivalence::getLocalizedName)
                        .collect(Collectors.joining(", ")));
    }

    private static @Nullable String normalizeIdList(@Nullable String raw) {
        if (raw == null) {
            return null;
        }
        Set<MatchEquivalence> disabled = fromIdList(raw);
        return disabled.isEmpty() ? null : toIdList(disabled);
    }
}
