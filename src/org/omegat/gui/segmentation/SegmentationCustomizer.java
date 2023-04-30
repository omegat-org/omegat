/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.segmentation;

import java.awt.Window;

import org.omegat.core.segmentation.SRX;
import org.omegat.gui.dialogs.PreferencesDialog;

/**
 * A convenience class implementing a similar API to the old SegmentationCustomizer implementation, now based
 * on {@link SegmentationCustomizerController} and {@link PreferencesDialog}.
 *
 * @author Aaron Madlon-Kay
 */
public class SegmentationCustomizer {

    private final SegmentationCustomizerController view;
    private final PreferencesDialog dialog;

    public SegmentationCustomizer(boolean projectSpecific, SRX defaultSRX, SRX userSRX, SRX projectSRX) {
        this.view = new SegmentationCustomizerController(projectSpecific, defaultSRX, userSRX, projectSRX);
        this.dialog = new PreferencesDialog(view);
    }

    public boolean show(Window parent) {
        return dialog.show(parent);
    }

    public SRX getResult() {
        return view.getResult();
    }
}
