/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.gui.dialogs;

import javax.swing.JFrame;

public class WebDownloadDialogController {

    public static final int RET_CANCEL = 0;
    public static final int RET_OK = 1;

    String webDownloadUrl;

    public WebDownloadDialogController() {
        webDownloadUrl = null;
    }

    public String getWebDownloadUrl() {
        return webDownloadUrl;
    }

    public int show(JFrame parent) {
        WebDownloadDialog dialog = new WebDownloadDialog(parent);
        dialog.setVisible(true);

        if (dialog.returnStatus == RET_CANCEL) {
            return RET_CANCEL;
        }
        webDownloadUrl = dialog.urlInputTextField.getText();
        return RET_OK;
    }
}
