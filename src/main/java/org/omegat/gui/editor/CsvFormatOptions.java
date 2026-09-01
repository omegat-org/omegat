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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.omegat.util.Preferences;

/**
 * File format of a segment CSV: character set, field separator, quoting and
 * escaping. Shared between the CSV export and the planned CSV import,
 * including the preferences the values persist in.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
final class CsvFormatOptions {

    /** Offered character sets. BOM variants ease opening in spreadsheet applications. */
    enum CsvCharset {
        UTF_8("UTF-8", StandardCharsets.UTF_8, false),
        UTF_8_BOM("UTF-8 (BOM)", StandardCharsets.UTF_8, true),
        UTF_16LE_BOM("UTF-16LE (BOM)", StandardCharsets.UTF_16LE, true),
        ISO_8859_1("ISO-8859-1", StandardCharsets.ISO_8859_1, false),
        US_ASCII("US-ASCII", StandardCharsets.US_ASCII, false);

        private final String displayName;
        private final Charset charset;
        private final boolean bom;

        CsvCharset(String displayName, Charset charset, boolean bom) {
            this.displayName = displayName;
            this.charset = charset;
            this.bom = bom;
        }

        Charset getCharset() {
            return charset;
        }

        boolean hasBom() {
            return bom;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /** Offered field separators; OTHER uses the user-supplied character. */
    enum SeparatorChoice {
        COMMA(','), SEMICOLON(';'), TAB('\t'), OTHER(',');

        private final char separator;

        SeparatorChoice(char separator) {
            this.separator = separator;
        }
    }

    /** How a quote inside a quoted field is escaped. */
    enum QuoteEscape { DOUBLED, BACKSLASH }

    static final char DEFAULT_CUSTOM_SEPARATOR = ',';

    private final CsvCharset charset;
    private final SeparatorChoice separatorChoice;
    private final char customSeparator;
    private final boolean quoteAll;
    private final boolean escapeNewlines;
    private final QuoteEscape quoteEscape;

    CsvFormatOptions(CsvCharset charset, SeparatorChoice separatorChoice, char customSeparator,
            boolean quoteAll, boolean escapeNewlines, QuoteEscape quoteEscape) {
        this.charset = charset;
        this.separatorChoice = separatorChoice;
        this.customSeparator = customSeparator;
        this.quoteAll = quoteAll;
        this.escapeNewlines = escapeNewlines;
        this.quoteEscape = quoteEscape;
    }

    CsvCharset getCharset() {
        return charset;
    }

    SeparatorChoice getSeparatorChoice() {
        return separatorChoice;
    }

    char getCustomSeparator() {
        return customSeparator;
    }

    /** The effective field separator character. */
    char getSeparator() {
        return separatorChoice == SeparatorChoice.OTHER ? customSeparator : separatorChoice.separator;
    }

    /** Whether every field is quoted, not only fields that need it. */
    boolean isQuoteAll() {
        return quoteAll;
    }

    /** Whether line breaks in field text turn into literal {@code \n}. */
    boolean isEscapeNewlines() {
        return escapeNewlines;
    }

    QuoteEscape getQuoteEscape() {
        return quoteEscape;
    }

    static CsvFormatOptions loadFromPreferences() {
        CsvCharset charset = parseEnum(CsvCharset.class,
                Preferences.getPreferenceDefault(Preferences.EDITOR_CSV_CHARSET, CsvCharset.UTF_8.name()),
                CsvCharset.UTF_8);
        SeparatorChoice separator = parseEnum(SeparatorChoice.class,
                Preferences.getPreferenceDefault(Preferences.EDITOR_CSV_SEPARATOR, SeparatorChoice.COMMA.name()),
                SeparatorChoice.COMMA);
        String custom = Preferences.getPreferenceDefault(Preferences.EDITOR_CSV_SEPARATOR_CUSTOM,
                String.valueOf(DEFAULT_CUSTOM_SEPARATOR));
        char customSeparator = custom.isEmpty() ? DEFAULT_CUSTOM_SEPARATOR : custom.charAt(0);
        boolean quoteAll = Preferences.isPreferenceDefault(Preferences.EDITOR_CSV_QUOTE_ALL, false);
        boolean escapeNewlines = Preferences.isPreferenceDefault(Preferences.EDITOR_CSV_ESCAPE_NEWLINES,
                false);
        QuoteEscape quoteEscape = parseEnum(QuoteEscape.class,
                Preferences.getPreferenceDefault(Preferences.EDITOR_CSV_QUOTE_ESCAPE,
                        QuoteEscape.DOUBLED.name()),
                QuoteEscape.DOUBLED);
        return new CsvFormatOptions(charset, separator, customSeparator, quoteAll, escapeNewlines,
                quoteEscape);
    }

    void saveToPreferences() {
        Preferences.setPreference(Preferences.EDITOR_CSV_CHARSET, charset.name());
        Preferences.setPreference(Preferences.EDITOR_CSV_SEPARATOR, separatorChoice.name());
        Preferences.setPreference(Preferences.EDITOR_CSV_SEPARATOR_CUSTOM, String.valueOf(customSeparator));
        Preferences.setPreference(Preferences.EDITOR_CSV_QUOTE_ALL, quoteAll);
        Preferences.setPreference(Preferences.EDITOR_CSV_ESCAPE_NEWLINES, escapeNewlines);
        Preferences.setPreference(Preferences.EDITOR_CSV_QUOTE_ESCAPE, quoteEscape.name());
    }

    static <E extends Enum<E>> E parseEnum(Class<E> type, String name, E fallback) {
        try {
            return Enum.valueOf(type, name);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
