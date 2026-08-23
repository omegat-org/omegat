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

package org.omegat.gui.align;

/**
 * Preference keys of the aligner module. The values are stored through the
 * core preferences system, but the keys are owned by this module and are
 * therefore defined here instead of in {@code org.omegat.util.Preferences}.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
final class AlignerPrefs {

    static final String ALIGNER_WINDOW_GEOMETRY_PREFIX = "aligner_window";
    static final String ALIGNER_LAST_SAVE_DIR = "aligner_last_save_dir";
    static final String ALIGNER_ALGORITHM_CLASS = "aligner_algorithm_class";
    static final String ALIGNER_CALCULATOR_TYPE = "aligner_calculator_type";
    static final String ALIGNER_COUNTER_TYPE = "aligner_counter_type";
    static final String ALIGNER_SEGMENT = "aligner_segment";
    static final String ALIGNER_REMOVE_TAGS = "aligner_remove_tags";
    static final String ALIGNER_SOURCE_LANGUAGE = "aligner_source_language";
    static final String ALIGNER_TARGET_LANGUAGE = "aligner_target_language";
    static final String ALIGNER_LAST_SOURCE_DIR = "aligner_last_source_dir";
    static final String ALIGNER_LAST_TARGET_DIR = "aligner_last_target_dir";

    private AlignerPrefs() {
    }
}
