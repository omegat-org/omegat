package org.omegat.gui.preferences.view;

import javax.swing.Icon;

public class AppearancePreferencesThemeLabel {

    String text;
    Icon icon;
    String key;

    AppearancePreferencesThemeLabel(String key, String text, Icon icon) {
        this.key = key;
        this.text = text;
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public Icon getIcon() {
        return icon;
    }

    public String getKey() {
        return key;
    }
}
