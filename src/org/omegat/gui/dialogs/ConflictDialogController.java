/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Alex Buloichik
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.omegat.core.Core;

/**
 * Show conflict dialog.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ConflictDialogController {
    private static final int MAX_CODEPOINTS = 100;

    private volatile String result;

    public String show(String baseText, String remoteText, String localText) {
        final ConflictDialog dialog = new ConflictDialog(Core.getMainWindow().getApplicationFrame(), true);

        dialog.textLeft.setText(prepareText(baseText));
        dialog.textCenter.setText(prepareText(localText));
        dialog.textRight.setText(prepareText(remoteText));

        dialog.btnMine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = localText;
                dialog.dispose();
            }
        });
        dialog.btnTheirs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = remoteText;
                dialog.dispose();
            }
        });

        dialog.setLocationRelativeTo(Core.getMainWindow().getApplicationFrame());
        dialog.pack();
        dialog.setVisible(true);

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
