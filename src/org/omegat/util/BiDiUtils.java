/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2012 Didier Briel
               2015 Aaron Madlon-Kay
               2023 Damien Rembert
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
package org.omegat.util;

import java.awt.ComponentOrientation;

import org.omegat.core.Core;

/**
 * @author Damien Rembert
 */
public class BiDiUtils {

    public enum ORIENTATION {
        /** All text is left-to-right oriented. */
        ALL_LTR,
        /** All text is right-to-left oriented. */
        ALL_RTL,
        /**
         * different texts/segments have different orientation, depending on
         * language/locale.
         */
        DIFFER
    };

    public static final String BIDI_LRE = "\u202a";
    public static final String BIDI_RLE = "\u202b";
    public static final String BIDI_PDF = "\u202c";
    public static final String BIDI_LRM = "\u200e";
    public static final String BIDI_RLM = "\u200f";
    public static final char BIDI_LRM_CHAR = '\u200e';
    public static final char BIDI_RLM_CHAR = '\u200f';

    public static ORIENTATION getOrientationType() {
        ORIENTATION orientation;

        if (Core.getProject().isProjectLoaded()) {
            orientation = getOrientationFromProject();
        } else if (isLocaleRtl()) {
            // project not loaded, use locale
            orientation = ORIENTATION.ALL_RTL;
        } else {
            // project not loaded, default to LTR
            orientation = ORIENTATION.ALL_LTR;
        }
        return orientation;
    }

    /**
     * Decide what document orientation should be default for source/target
     * languages.
     */
    private static ORIENTATION getOrientationFromProject() {
        ORIENTATION orientation;

        boolean sourceLangIsRTL = isSourceLangRtl();
        boolean targetLangIsRTL = isTargetLangRtl();

        if (sourceLangIsRTL) {
            orientation = ORIENTATION.ALL_RTL;
        } else {
            orientation = ORIENTATION.ALL_LTR;
        }
        if (sourceLangIsRTL != targetLangIsRTL || sourceLangIsRTL != isLocaleRtl()) {
            orientation = ORIENTATION.DIFFER;
        }
        return orientation;
    }

    /**
     * Decide what document orientation should be default for source/target
     * languages.
     */
    public static ComponentOrientation getInitialOrientation() {
        return getOrientation(null);
    }

    /**
     * Decide what document orientation should be default for source/target
     * languages.
     */
    public static ComponentOrientation getOrientation(ORIENTATION orientationType) {

        ComponentOrientation targetOrientation = null;

        if (orientationType == null) {
            orientationType = getOrientationType();
        }

        switch (orientationType) {
        case ALL_LTR:
            targetOrientation = ComponentOrientation.LEFT_TO_RIGHT;
            break;
        case ALL_RTL:
            targetOrientation = ComponentOrientation.RIGHT_TO_LEFT;
            break;
        case DIFFER:
            if (isTargetLangRtl()) {
                // using target lang direction gives better result when user
                // starts editing.
                targetOrientation = ComponentOrientation.RIGHT_TO_LEFT;
            } else {
                targetOrientation = ComponentOrientation.LEFT_TO_RIGHT;
            }
        }
        return targetOrientation;
    }

    public static String addRtlBidiAround(String string) {
        return BIDI_RLE + string + BIDI_PDF;
    }

    public static String addLtrBidiAround(String string) {
        return BIDI_LRE + string + BIDI_PDF;
    }

    public static boolean isSourceLangRtl() {
        return isRtl(getSourceLanguage());
    }

    public static boolean isTargetLangRtl() {
        return isRtl(getTargetLanguage());
    }

    private static String getSourceLanguage() {
        return Core.getProject().getProjectProperties().getSourceLanguage().getLanguageCode();
    }

    private static String getTargetLanguage() {
        return Core.getProject().getProjectProperties().getTargetLanguage().getLanguageCode();
    }

    /**
     * Check if locale is Right-To-Left oriented.
     * 
     * @return true if locale is Right-To-Left oriented.
     */
    public static boolean isLocaleRtl() {
        String language = Language.getLowerCaseLanguageFromLocale();
        return isRtl(language);
    }

    /**
     * Check if language is Right-To-Left oriented.
     *
     * @param language
     *            ISO-639-2 language code
     * @return true if language is RTL
     */
    public static boolean isRtl(final String language) {
        return "ar".equalsIgnoreCase(language) || "iw".equalsIgnoreCase(language)
                || "he".equalsIgnoreCase(language) || "fa".equalsIgnoreCase(language)
                || "ur".equalsIgnoreCase(language) || "ug".equalsIgnoreCase(language)
                || "ji".equalsIgnoreCase(language) || "yi".equalsIgnoreCase(language);
    }

    public static boolean isMixedOrientationProject() {
        return (BiDiUtils.getOrientationType() == ORIENTATION.DIFFER);
    }
}
