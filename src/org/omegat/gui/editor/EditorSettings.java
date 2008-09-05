/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.editor;

import java.awt.event.KeyEvent;

import javax.swing.text.AttributeSet;

import org.omegat.core.Core;
import org.omegat.util.Preferences;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Editor behavior control settings.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class EditorSettings {
    private final EditorController parent;

    private boolean useTabForAdvance;
    private boolean markTranslated;
    private boolean markUntranslated;
    private boolean displaySegmentSources;
    private boolean autoSpellChecking;

    protected EditorSettings(final EditorController parent) {
        this.parent = parent;

        useTabForAdvance = Preferences
                .isPreference(Preferences.USE_TAB_TO_ADVANCE);
        markTranslated = Preferences
                .isPreference(Preferences.MARK_TRANSLATED_SEGMENTS);
        markUntranslated = Preferences
                .isPreference(Preferences.MARK_UNTRANSLATED_SEGMENTS);
        displaySegmentSources = Preferences
                .isPreference(Preferences.DISPLAY_SEGMENT_SOURCES);
        autoSpellChecking=Preferences.isPreference(Preferences.ALLOW_AUTO_SPELLCHECKING);
    }

    public char getAdvancerChar() {
        if (useTabForAdvance) {
            return KeyEvent.VK_TAB;
        } else {
            return KeyEvent.VK_ENTER;
        }
    }

    /** the attribute set used for translated segments */
    public AttributeSet getTranslatedAttributeSet() {
        return markTranslated ? Styles.TRANSLATED : Styles.PLAIN;
    }

    /** the attribute set used for untranslated segments */
    public AttributeSet getUntranslatedAttributeSet() {
        return markUntranslated ? Styles.UNTRANSLATED : Styles.PLAIN;
    }

    public boolean isUseTabForAdvance() {
        return useTabForAdvance;
    }

    public void setUseTabForAdvance(boolean useTabForAdvance) {
        this.useTabForAdvance = useTabForAdvance;
        Preferences.setPreference(Preferences.USE_TAB_TO_ADVANCE,
                useTabForAdvance);
    }

    public boolean isMarkTranslated() {
        return markTranslated;
    }

    public void setMarkTranslated(boolean markTranslated) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.markTranslated = markTranslated;
        Preferences.setPreference(Preferences.MARK_TRANSLATED_SEGMENTS,
                markTranslated);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    public boolean isMarkUntranslated() {
        return markUntranslated;
    }

    public void setMarkUntranslated(boolean markUntranslated) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.markUntranslated = markUntranslated;
        Preferences.setPreference(Preferences.MARK_UNTRANSLATED_SEGMENTS,
                markUntranslated);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }

    /** display the segmetn sources or not */
    public boolean isDisplaySegmentSources() {
        return displaySegmentSources;
    }

    public void setDisplaySegmentSources(boolean displaySegmentSources) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.displaySegmentSources = displaySegmentSources;
        Preferences.setPreference(Preferences.DISPLAY_SEGMENT_SOURCES,
                displaySegmentSources);

        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }
    
    /** need to check spell or not */
    public boolean isAutoSpellChecking() {
        return autoSpellChecking;
    }
    
    public void setAutoSpellChecking(boolean autoSpellChecking) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();
        
        this.autoSpellChecking = autoSpellChecking;
        
        if (Core.getProject().isProjectLoaded()) {
            parent.loadDocument();
            parent.activateEntry();
        }
    }
}
