/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
import java.text.MessageFormat;

import org.omegat.core.Core;
import org.omegat.util.OStrings;

/**
 * Show conflict dialog.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ConflictDialogController {
    private volatile boolean result;

    public boolean show(String baseText, String remoteText, String localText) {
        final ConflictDialog dialog = new ConflictDialog(Core.getMainWindow().getApplicationFrame(), true);

        if (baseText != null && baseText.codePointCount(0, baseText.length()) > 25) {
            baseText = baseText.substring(0, baseText.offsetByCodePoints(0, 25)) + "...";
        }
        if (remoteText != null && remoteText.codePointCount(0, remoteText.length()) > 25) {
            remoteText = remoteText.substring(0, remoteText.offsetByCodePoints(0, 25)) + "...";
        }
        if (localText != null && localText.codePointCount(0, localText.length()) > 25) {
            localText = localText.substring(0, localText.offsetByCodePoints(0, 25)) + "...";
        }
        dialog.text.setText(MessageFormat.format(OStrings.getString("CONFLICT_DIALOG_TEXT"), baseText,
                remoteText, localText));

        dialog.btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = true;
                dialog.dispose();
            }
        });
        dialog.btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        dialog.setLocationRelativeTo(Core.getMainWindow().getApplicationFrame());
        dialog.setVisible(true);

        return result;
    }
}
