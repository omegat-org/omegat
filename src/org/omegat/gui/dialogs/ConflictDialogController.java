/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Alex Buloichik
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

package org.omegat.gui.dialogs;

import javax.swing.JFrame;

/**
 * Show conflict dialog.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ConflictDialogController {
    private static final int MAX_CODEPOINTS = 100;
    private final JFrame parent;
    private volatile String result;

    public ConflictDialogController(JFrame parent) {
        this.parent = parent;
    }

    public boolean show(String baseText, String remoteText, String localText) {
        final ConflictDialog dialog = new ConflictDialog(parent, true);
        dialog.btnMine.setName("btnMine");
        dialog.btnTheirs.setName("btnTheirs");

        dialog.textLeft.setText(prepareText(baseText));
        dialog.textCenter.setText(prepareText(localText));
        dialog.textRight.setText(prepareText(remoteText));
        dialog.textLeft.setName("textLeft");
        dialog.textCenter.setName("textCenter");
        dialog.textRight.setName("textRight");

        dialog.btnMine.addActionListener(e -> {
            synchronized (this) {
                result = localText;
            }
            dialog.dispose();
        });
        dialog.btnTheirs.addActionListener(e -> {
            synchronized (this) {
                result = remoteText;
            }
            dialog.dispose();
        });

        dialog.setLocationRelativeTo(parent);
        dialog.pack();
        dialog.setVisible(true);

        return true;
    }

    public String getResult() {
        return result;
    }

    private static String prepareText(String text) {
        if (text == null) {
            return String.valueOf(text);
        } else if (text.codePointCount(0, text.length()) > MAX_CODEPOINTS) {
            return text.substring(0, text.offsetByCodePoints(0, MAX_CODEPOINTS)) + "...";
        } else {
            return text;
        }
    }
}
