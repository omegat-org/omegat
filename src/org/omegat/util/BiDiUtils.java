package org.omegat.util;

import org.omegat.core.Core;

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
	// TODO: delete the dagger
	public static final String UNICODE_DAGGER = "\u2020";

	public static ORIENTATION getOrientationType() {
		ORIENTATION currentOrientation;

		if (Core.getProject().isProjectLoaded()) {
			currentOrientation = getOrientationFromProject();
		} else if (isLocaleRtl()) {
			// project not loaded, use locale
			currentOrientation = ORIENTATION.ALL_RTL;
		} else {
			// project not loaded, default to LTR
			currentOrientation = ORIENTATION.ALL_LTR;
		}
		return currentOrientation;
	}

	/**
	 * Decide what document orientation should be default for source/target
	 * languages.
	 */
	public static ORIENTATION getOrientationFromProject() {
		ORIENTATION currentOrientation;

		boolean sourceLangIsRTL = isSourceLangRtl();
		boolean targetLangIsRTL = isTargetLangRtl();

		if (sourceLangIsRTL) {
			currentOrientation = ORIENTATION.ALL_RTL;
		} else {
			currentOrientation = ORIENTATION.ALL_LTR;
		}
		if (sourceLangIsRTL != targetLangIsRTL || sourceLangIsRTL != isLocaleRtl()) {
			currentOrientation = ORIENTATION.DIFFER;
		}
		return currentOrientation;
	}

	public static String addRtlBidiAround(String string) {
		return BiDiUtils.BIDI_RLE + string + BiDiUtils.BIDI_PDF;
	}

	public static String addLtrBidiAround(String string) {
		return BiDiUtils.BIDI_LRE + string + BiDiUtils.BIDI_PDF;
	}

	public static boolean isSourceLangRtl() {
		return Language.isRTL(getSourceLanguage());
	}

	public static boolean isTargetLangRtl() {
		return Language.isRTL(getTargetLanguage());
	}

	public static String getSourceLanguage() {
		return Core.getProject().getProjectProperties().getSourceLanguage().getLanguageCode();
	}

	public static String getTargetLanguage() {
		return Core.getProject().getProjectProperties().getTargetLanguage().getLanguageCode();
	}

	public static boolean isLocaleRtl() {
		return Language.localeIsRTL();
	}
}
