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

package org.omegat.gui.editor;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;

/**
 * Formats the segment metadata shown in the editor gutter. Kept free of GUI
 * dependencies so the values can be tested headlessly.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
final class SegmentMetadataFormatter {

    static final String STATUS_TRANSLATED = "T";
    static final String STATUS_UNTRANSLATED = "U";
    static final String STATUS_UNIQUE_SUFFIX = "U";
    static final String STATUS_NON_UNIQUE_FIRST_SUFFIX = "F";
    static final String STATUS_NON_UNIQUE_NEXT_SUFFIX = "R";
    static final String ALTERNATIVE_MARK = "alt";
    static final String STATUS_LINKED_ICE = "I";
    static final String STATUS_LINKED_100PC = "C";
    static final String STATUS_LINKED_AUTO = "A";
    static final String STATUS_LINKED_ENFORCED = "E";
    static final String STATUS_HAS_ORIGIN = "*";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private SegmentMetadataFormatter() {
    }

    static String number(int value, Locale locale) {
        return NumberFormat.getIntegerInstance(locale).format(value);
    }

    /**
     * Compact translation state: T for translated, U for untranslated,
     * always followed by the uniqueness letter: U for a unique segment,
     * F for the first of several identical segments, R for a repeated
     * occurrence. Externally provided translations carry
     * their provenance: I for an in-context exact match, C for a 100 percent
     * context match, A for an auto-populated match from the tm folder, E for
     * an enforced translation, and an asterisk when the entry records an
     * origin such as a machine translation engine.
     */
    static String status(boolean translated, SourceTextEntry.DUPLICATE duplicate,
            TMXEntry.@Nullable ExternalLinked linked, @Nullable String origin) {
        StringBuilder status = new StringBuilder(
                translated ? STATUS_TRANSLATED : STATUS_UNTRANSLATED);
        switch (duplicate) {
        case FIRST:
            status.append(STATUS_NON_UNIQUE_FIRST_SUFFIX);
            break;
        case NEXT:
            status.append(STATUS_NON_UNIQUE_NEXT_SUFFIX);
            break;
        default:
            status.append(STATUS_UNIQUE_SUFFIX);
            break;
        }
        if (linked != null) {
            switch (linked) {
            case xICE:
                status.append(STATUS_LINKED_ICE);
                break;
            case x100PC:
                status.append(STATUS_LINKED_100PC);
                break;
            case xAUTO:
                status.append(STATUS_LINKED_AUTO);
                break;
            case xENFORCED:
                status.append(STATUS_LINKED_ENFORCED);
                break;
            default:
                break;
            }
        }
        if (origin != null && !origin.isEmpty()) {
            status.append(STATUS_HAS_ORIGIN);
        }
        return status.toString();
    }

    /** The identifier of the segment in the source file, if any. */
    static String id(@Nullable String id) {
        return id == null ? "" : id;
    }

    /**
     * The match of the pattern within the value: the last group when the
     * pattern has groups, the whole match otherwise; empty without a match,
     * the plain value without a pattern.
     */
    static String regexMatch(String value, @Nullable Pattern pattern) {
        if (pattern == null || value.isEmpty()) {
            return value;
        }
        Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) {
            return "";
        }
        String group = matcher.groupCount() > 0 ? matcher.group(matcher.groupCount())
                : matcher.group();
        return group == null ? "" : group;
    }

    /**
     * The day formatted with the given formatter, the ISO date without one;
     * empty without a timestamp.
     */
    static String date(long stamp, ZoneId zone, @Nullable DateTimeFormatter format) {
        if (stamp <= 0) {
            return "";
        }
        return (format == null ? DATE_FORMAT : format).format(Instant.ofEpochMilli(stamp).atZone(zone));
    }

    /** The character count of the text, empty for missing text. */
    static String length(@Nullable String text, Locale locale) {
        return length(text, locale, false, false);
    }

    /**
     * The character count of the text, empty for missing text; optionally of
     * the trimmed text, optionally counting only non-whitespace characters.
     */
    static String length(@Nullable String text, Locale locale, boolean trim,
            boolean onlyNonSpace) {
        if (text == null) {
            return "";
        }
        String value = trim ? text.strip() : text;
        int count = onlyNonSpace
                ? (int) value.codePoints().filter(cp -> !Character.isWhitespace(cp)).count()
                : value.length();
        return number(count, locale);
    }

    /** A mark for segments translated with an alternative translation. */
    static String alternative(boolean defaultTranslation) {
        return defaultTranslation ? "" : ALTERNATIVE_MARK;
    }

    /** The author of the last change, falling back to the creator. */
    static String author(@Nullable String changer, @Nullable String creator) {
        if (changer != null && !changer.isEmpty()) {
            return changer;
        }
        return creator == null ? "" : creator;
    }

    /**
     * The day of the last change as an ISO date, falling back to the creation
     * day; empty when the entry carries no dates.
     */
    static String date(long changeDate, long creationDate, ZoneId zone) {
        long stamp = changeDate > 0 ? changeDate : creationDate;
        if (stamp <= 0) {
            return "";
        }
        return DATE_FORMAT.format(Instant.ofEpochMilli(stamp).atZone(zone));
    }
}
