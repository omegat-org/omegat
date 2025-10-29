package org.omegat.gui.preferences.view;

import org.omegat.gui.preferences.BasePreferencesController;

import javax.swing.JComponent;
import java.awt.Component;
import java.util.ArrayList;

public class CmsPreferencesController extends BasePreferencesController {

    private CmsPreferencesPanel panel;

    @Override
    protected void initFromPrefs() {
        if (panel != null) {
            panel.loadFromPrefs();
        }
    }

    @Override
    public String toString() {
        return "CMS";
    }

    @Override
    public Component getGui() {
        if (panel == null) {
            panel = new CmsPreferencesPanel();
            panel.loadFromPrefs();
        }
        return panel;
    }

    @Override
    public void persist() {
        if (panel != null) {
            panel.saveToPrefs();
        }
        setReloadRequired(false);
        setRestartRequired(false);
    }

    @Override
    public void restoreDefaults() {
        if (panel != null) {
            panel.setTargets(new ArrayList<>());
            panel.saveToPrefs();
        }
    }
}
