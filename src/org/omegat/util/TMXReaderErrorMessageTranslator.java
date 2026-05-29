/*
 * OmegaT - Computer Assisted Translation (CAT) tool
 *          with fuzzy matching, translation memory, keyword search,
 *          glossaries, and translation leveraging into updated projects.
 *
 * Copyright (C) 2026 Hiroshi Miura
 *          Home page: https://www.omegat.org/
 *          Support center: https://omegat.org/support
 *
 * This file is part of OmegaT.
 *
 * OmegaT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OmegaT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.omegat.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Translates cryptic TMXReader validation error messages into user-friendly descriptions.
 */
public class TMXReaderErrorMessageTranslator {

    // Extracts the stable error code before the colon, e.g. "cvc-complex-type.4"
    // The rest of the message is localized and must NOT be parsed.
    private static final Pattern ERROR_CODE = Pattern.compile(
            "^(cvc-[\\w.]+(?:\\.[\\w.]+)*):"
    );

    // For the namespace-qualified attribute case we need the attribute name and element.
    // These are proper nouns (attribute/element names from the XML document itself)
    // and are never translated — safe to parse from any locale.
    private static final Pattern QUOTED_NAMES = Pattern.compile("'([^']+)'");

    private static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";

    public static String translate(int line, int column, String rawMessage) {
        if (rawMessage == null) {
            return OStrings.getString("TMX_ERROR_MESSAGE_TRANSLATOR_UNKNOWN", line, column);
        }

        Matcher codeMatcher = ERROR_CODE.matcher(rawMessage);
        if (!codeMatcher.find()) {
            return rawMessage; // not a cvc error, pass through as-is
        }
        String code = codeMatcher.group(1);

        // Extract all single-quoted tokens — these are XML names from the document,
        // not translated text, so they are safe regardless of locale.
        List<String> names = extractQuotedNames(rawMessage);

        switch (code) {
        case "cvc-complex-type.4":
            return translateMissingAttribute(line, column, names, rawMessage);
        case "cvc-complex-type.3.2.2":
            if (names.size() >= 2) {
                return describeUnexpectedAttribute(line, column, names.get(0), names.get(1));
            }
            return fallback(line, column, rawMessage, code);
        case "cvc-complex-type.2.4.a":
        case "cvc-complex-type.2.4.b":
            if (names.isEmpty()) {
                return fallback(line, column, rawMessage, code);
            }
            return OStrings.getString("TMX_ERROR_MESSAGE_TRANSLATOR_CVC_COMPLEX_TYPE_2_4", line, column, names.get(0));
        case "cvc-datatype-valid.1.2.1":
            return OStrings.getString("TMX_ERROR_MESSAGE_TRANSLATOR_CVC_DATATYPE_VALID", line, column, names.get(0),
                    names.get(1));
        case "cvc-enumeration-valid":
            if (names.isEmpty()) {
                return fallback(line, column, rawMessage, code);
            }
            return describeEnumerationError(line, column, names.get(0), stripCode(rawMessage, code));
        default:
            return fallback(line, column, rawMessage, code);
        }
    }

    private static String translateMissingAttribute(int line, int column, List<String> names, String raw) {
        // The quoted tokens are the attribute local-name and element name.
        // If the namespace URI appears in the message it is also quoted.
        // We detect the xml: namespace by its well-known URI.
        String attrLocal = !names.isEmpty() ? names.get(0) : "?";
        String element   = names.size() > 1 ? names.get(names.size() - 1) : "?";

        // Check for the XML namespace URI anywhere in the raw message (never translated)
        boolean isXmlNs = raw.contains(XML_NAMESPACE);
        String attrDisplay = isXmlNs ? "xml:" + attrLocal : attrLocal;
        return OStrings.getString("TMX_ERROR_MESSAGE_TRANSLATOR_CVC_COMPLEX_TYPE_4", line, column,
                attrDisplay, element, suggestForMissingAttribute(attrDisplay, element));
    }

    private static String describeEnumerationError(int line, int column, String value, String raw) {
        // The allowed values list uses curly braces {a, b, c} — also not translated
        Matcher m = Pattern.compile("\\{([^}]+)}").matcher(raw);
        String allowed = m.find() ? m.group(1) : "(see schema)";
        return OStrings.getString("TMX_ERROR_MESSAGE_TRANSLATOR_CVC_ENUMERATION_VALID", line, column, value, allowed);
    }

    private static String describeUnexpectedAttribute(int line, int column, String attr, String element) {
        return OStrings.getString("TMX_ERROR_MESSAGE_TRANSLATOR_CVC_COMPLEX_TYPE_3_2_2", line, column,
                attr, element, suggestForUnexpectedAttribute(attr, element));
    }

    /** Domain-specific hints for TMX attributes */
    private static String suggestForMissingAttribute(String attr, String element) {
        if ("xml:lang".equals(attr) && "tuv".equals(element)) {
            return " " + OStrings.getString("TMX_ERROR_MESSAGE_TRANSLATOR_SUGGEST_LANG");
        }
        return "";
    }

    private static String suggestForUnexpectedAttribute(String attr, String element) {
        if ("lang".equals(attr) && "tuv".equals(element)) {
            return " " + OStrings.getString("TMX_ERROR_MESSAGE_TRANSLATOR_SUGGEST_LANG_2");
        }
        return "";
    }

    private static List<String> extractQuotedNames(String message) {
        List<String> result = new ArrayList<>();
        Matcher m = QUOTED_NAMES.matcher(message);
        while (m.find()) result.add(m.group(1));
        return result;
    }

    private static String stripCode(String raw, String code) {
        return raw.replaceFirst(Pattern.quote(code) + ":\\s*", "");
    }

    private static String fallback(int line, int column, String raw, String code) {
        // Fallback: strip the error code prefix and return the raw text,
        // which is still better than the full cvc-... jargon for unrecognised codes.
        return OStrings.getString("TMX_ERROR_MESSAGE_TRANSLATOR_FALLBACK", line, column, stripCode(raw, code), code);
    }
}
