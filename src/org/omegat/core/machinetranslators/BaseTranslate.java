/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.core.machinetranslators;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;

import org.omegat.core.Core;
import org.omegat.gui.exttrans.IMachineTranslation;
import org.omegat.util.Language;
import org.omegat.util.Preferences;
import org.openide.awt.Mnemonics;

/**
 * Base class for machine translation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public abstract class BaseTranslate implements IMachineTranslation, ActionListener {
    protected final JCheckBoxMenuItem menuItem;

    protected boolean enabled;

    public BaseTranslate() {
        menuItem = new JCheckBoxMenuItem();
        Mnemonics.setLocalizedText(menuItem, getName());
        menuItem.addActionListener(this);
        enabled = Preferences.isPreference(getPreferenceName());
        menuItem.setState(enabled);
        Core.getMainWindow().getMainMenu().getMachineTranslationMenu().add(menuItem);
    }

    public void actionPerformed(ActionEvent e) {
        enabled = menuItem.isSelected();
        Preferences.setPreference(getPreferenceName(), enabled);
    }

    public String getTranslation(Language sLang, Language tLang, String text) throws Exception {
        if (enabled) {
            return translate(sLang, tLang, text);
        } else {
            return null;
        }
    }

    abstract protected String getPreferenceName();

    abstract protected String translate(Language sLang, Language tLang, String text) throws Exception;
}
