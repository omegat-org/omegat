/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2013 Didier Briel
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

package org.omegat.core.machinetranslators;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;

import javax.swing.JCheckBoxMenuItem;

import org.omegat.core.Core;
import org.omegat.gui.exttrans.IMachineTranslation;
import org.omegat.util.Language;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.openide.awt.Mnemonics;

/**
 * Base class for machine translation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
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
    
    /**
     * Attempt to clean spaces added around tags by machine translators. Do it by comparing spaces between the source
     * text and the machine translated text.
     * @param machineText The text returned by the machine translator
     * @param sourceText The original source segment
     * @return 
     */
    protected String cleanSpacesAroundTags(String machineText, String sourceText) {
        
        // Spaces after
        Matcher tag = PatternConsts.OMEGAT_TAG_SPACE.matcher(machineText);
        while (tag.find()) {
            String searchTag = tag.group();
            if (sourceText.indexOf(searchTag) == -1) { // The tag didn't appear with a trailing space in the source text
                String replacement = searchTag.substring(0, searchTag.length() - 1);
                machineText = machineText.replace(searchTag, replacement);
            }
        }

        // Spaces before
        tag = PatternConsts.SPACE_OMEGAT_TAG.matcher(machineText);
        while (tag.find()) {
            String searchTag = tag.group();
            if (sourceText.indexOf(searchTag) == -1) { // The tag didn't appear with a leading space in the source text
                String replacement = searchTag.substring(1, searchTag.length());
                machineText = machineText.replace(searchTag, replacement);
            }
        }
        return machineText;
    }
}
