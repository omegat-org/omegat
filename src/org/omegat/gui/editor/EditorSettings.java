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

import org.omegat.core.Core;
import org.omegat.core.spellchecker.SpellCheckerMarker;
import org.omegat.util.Preferences;
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
    private String displayModificationInfo;
    private boolean autoSpellChecking;

    public static String DISPLAY_MODIFICATION_INFO_NONE = "none";
    public static String DISPLAY_MODIFICATION_INFO_SELECTED = "selected";
    public static String DISPLAY_MODIFICATION_INFO_ALL = "all";

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
        displayModificationInfo = Preferences
                .getPreferenceDefault(Preferences.DISPLAY_MODIFICATION_INFO, DISPLAY_MODIFICATION_INFO_NONE);
        autoSpellChecking = Preferences
                .isPreference(Preferences.ALLOW_AUTO_SPELLCHECKING);
    }

    public char getAdvancerChar() {
        if (useTabForAdvance) {
            return KeyEvent.VK_TAB;
        } else {
            return KeyEvent.VK_ENTER;
        }
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

    /** display the segment sources or not */
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

    /** returns the setting for display the modification information or not
     * Either DISPLAY_MODIFICATION_INFO_NONE, DISPLAY_MODIFICATION_INFO_SELECTED, DISPLAY_MODIFICATION_INFO_ALL */
    public String getDisplayModificationInfo() {
        return displayModificationInfo;
    }

    /** Sets the setting for display the modification information or not
     * @param displayModificationInfo Either DISPLAY_MODIFICATION_INFO_NONE
     * , DISPLAY_MODIFICATION_INFO_SELECTED
     * , DISPLAY_MODIFICATION_INFO_ALL */
    public void setDisplayModificationInfo(String displayModificationInfo) {
        UIThreadsUtil.mustBeSwingThread();

        parent.commitAndDeactivate();

        this.displayModificationInfo = displayModificationInfo;
        Preferences.setPreference(Preferences.DISPLAY_MODIFICATION_INFO,
                displayModificationInfo);

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
        if (Core.getProject().isProjectLoaded()) {
            parent.commitAndDeactivate();
        }

        this.autoSpellChecking = autoSpellChecking;

        if (Core.getProject().isProjectLoaded()) {
         //   parent.loadDocument();
            parent.activateEntry();
            parent.remarkOneMarker(SpellCheckerMarker.class.getName());
        }
    }
}
