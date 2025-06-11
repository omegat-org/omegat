/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.properties;

import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.IProject;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.SourceTextEntry.DUPLICATE;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.BiDiUtils;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.IPaneMenu;
import org.omegat.util.gui.Styles;

public class SegmentPropertiesArea implements IPaneMenu {

    private static final Pattern SPLIT_COMMAS = Pattern.compile("\\s*,\\s*");

    private final DateFormat dateFormat = DateFormat.getDateInstance();
    private final DateFormat timeFormat = DateFormat.getTimeInstance();

    private static final String KEY_ISDUP = "isDup";
    private static final String KEY_FILE = "file";
    private static final String KEY_ID = "id";
    private static final String KEY_TRANSLATION = "translation";
    private static final String KEY_TRANSLATIONISFUZZY = "translationIsFuzzy";
    private static final String KEY_PATH = "path";
    private static final String KEY_HASNOTE = "hasNote";
    private static final String KEY_HASCOMMENT = "hasComment";
    private static final String KEY_CHANGED = "changed";
    private static final String KEY_CHANGER = "changer";
    private static final String KEY_CREATED = "created";
    private static final String KEY_CREATOR = "creator";
    private static final String KEY_ISALT = "isAlt";
    private static final String KEY_LINKED = "linked";
    private static final String KEY_ORIGIN = "origin";
    private static final String PROP_ORIGIN = ProjectTMX.PROP_ORIGIN;

    private final List<String> properties = new ArrayList<>();
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private final DockableScrollPane scrollPane;

    private ISegmentPropertiesView viewImpl;
    private boolean isTargetRtl;

    void setTargetRtl(boolean targetRtl) {
        isTargetRtl = targetRtl;
    }

    public SegmentPropertiesArea(IMainWindow mw) {
        scrollPane = new DockableScrollPane("SEGMENTPROPERTIES", OStrings.getString("SEGPROP_PANE_TITLE"),
                null, true);
        mw.addDockable(scrollPane);

        scrollPane.setMenuProvider(this);

        CoreEvents.registerEntryEventListener(new SegmentPropertiesEntryEventListener(this));
        CoreEvents.registerProjectChangeListener(eventType -> {
            if (eventType == IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE) {
                setProperties(null);
            }
        });
        CoreEvents.registerFontChangedEventListener(newFont -> viewImpl.getViewComponent().setFont(newFont));

        scrollPane.setForeground(Styles.EditorColor.COLOR_FOREGROUND.getColor());
        scrollPane.setBackground(Styles.EditorColor.COLOR_BACKGROUND.getColor());
        scrollPane.getViewport().setBackground(Styles.EditorColor.COLOR_BACKGROUND.getColor());

        Class<?> initModeClass = SegmentPropertiesTableView.class;
        String initModeClassName = Preferences.getPreferenceDefault(Preferences.SEGPROPS_INITIAL_MODE, null);
        if (initModeClassName != null) {
            try {
                initModeClass = getClass().getClassLoader().loadClass(initModeClassName);
            } catch (ClassNotFoundException e1) {
                Log.log(e1);
            }
        }
        installView(initModeClass);
    }

    public void addPropertyChangeListener(String key, PropertyChangeListener l) {
        changes.addPropertyChangeListener(key, l);
    }

    private void installView(Class<?> viewClass) {
        if (viewImpl != null && viewImpl.getClass().equals(viewClass)) {
            return;
        }
        ISegmentPropertiesView newImpl;
        try {
            Constructor<?> constructor = viewClass.getConstructor();
            newImpl = (ISegmentPropertiesView) constructor.newInstance();
        } catch (Exception e) {
            Log.log(e);
            return;
        }
        viewImpl = newImpl;
        viewImpl.install(this);
    }

    private void toggleMode(Class<?> newMode) {
        installView(newMode);
        Preferences.setPreference(Preferences.SEGPROPS_INITIAL_MODE, newMode.getName());
        viewImpl.update();
    }

    void showContextMenu(Point p) {
        JPopupMenu menu = new JPopupMenu();
        populateLocalContextMenuOptions(menu, p);
        try {
            menu.show(scrollPane, p.x, p.y);
        } catch (IllegalComponentStateException e) {
            Log.log(e);
        }
    }

    List<String> getProperties() {
        return properties;
    }

    DockableScrollPane getScrollPane() {
        return scrollPane;
    }

    private void populateLocalContextMenuOptions(JPopupMenu contextMenu, Point p) {
        final String key = viewImpl.getKeyAtPoint(p);
        if (key == null) {
            return;
        }
        String displayKey = key;
        if (!Preferences.isPreference(Preferences.SEGPROPS_SHOW_RAW_KEYS)) {
            try {
                displayKey = OStrings.getString(
                        ISegmentPropertiesView.PROPERTY_TRANSLATION_KEY + key.toUpperCase(Locale.ENGLISH));
            } catch (MissingResourceException ignore) {
                // If this is not a known key then we can't translate it,
                // so use the "raw" key instead.
            }
        }
        String label = StringUtil.format(OStrings.getString("SEGPROP_CONTEXTMENU_NOTIFY_ON_PROP"),
                displayKey);
        final JMenuItem notifyOnItem = new JCheckBoxMenuItem(label);
        notifyOnItem.setSelected(getKeysToNotify().contains(key));
        notifyOnItem.addActionListener(e -> setKeyToNotify(key, notifyOnItem.isSelected()));
        contextMenu.add(notifyOnItem);
    }

    @Override
    public void populatePaneMenu(JPopupMenu contextMenu) {
        JMenuItem tableModeItem = new JCheckBoxMenuItem(OStrings.getString("SEGPROP_CONTEXTMENU_TABLE_MODE"));
        tableModeItem.setSelected(viewImpl.getClass().equals(SegmentPropertiesTableView.class));
        tableModeItem.addActionListener(e -> toggleMode(SegmentPropertiesTableView.class));
        JMenuItem listModeItem = new JCheckBoxMenuItem(OStrings.getString("SEGPROP_CONTEXTMENU_LIST_MODE"));
        listModeItem.setSelected(viewImpl.getClass().equals(SegmentPropertiesListView.class));
        listModeItem.addActionListener(e -> toggleMode(SegmentPropertiesListView.class));
        ButtonGroup group = new ButtonGroup();
        group.add(tableModeItem);
        group.add(listModeItem);
        contextMenu.add(tableModeItem);
        contextMenu.add(listModeItem);
        contextMenu.addSeparator();
        final JMenuItem toggleKeyTranslationItem = new JCheckBoxMenuItem(
                OStrings.getString("SEGPROP_CONTEXTMENU_RAW_KEYS"));
        toggleKeyTranslationItem.setSelected(Preferences.isPreference(Preferences.SEGPROPS_SHOW_RAW_KEYS));
        toggleKeyTranslationItem.addActionListener(e -> {
            Preferences.setPreference(Preferences.SEGPROPS_SHOW_RAW_KEYS,
                    toggleKeyTranslationItem.isSelected());
            viewImpl.update();
        });
        contextMenu.add(toggleKeyTranslationItem);
    }

    private List<String> getKeysToNotify() {
        if (!Preferences.existsPreference(Preferences.SEGPROPS_NOTIFY_PROPS)) {
            Preferences.setPreference(Preferences.SEGPROPS_NOTIFY_PROPS,
                    Preferences.SEGPROPS_NOTIFY_DEFAULT_PROPS);
        }
        String rawProps = Preferences.getPreference(Preferences.SEGPROPS_NOTIFY_PROPS);
        return Arrays.asList(SPLIT_COMMAS.split(rawProps));
    }

    private void setKeyToNotify(String key, boolean enabled) {
        List<String> currentKeys = new ArrayList<>(getKeysToNotify());
        if (enabled && !currentKeys.contains(key)) {
            currentKeys.add(key);
        }
        if (!enabled) {
            currentKeys.remove(key);
        }
        Preferences.setPreference(Preferences.SEGPROPS_NOTIFY_PROPS, StringUtils.join(currentKeys, ", "));
    }

    private void doNotify(List<String> keys) {
        final List<Integer> notify = new ArrayList<>();
        for (int i = 0; i < properties.size(); i += 2) {
            String prop = properties.get(i);
            if (keys.contains(prop)) {
                notify.add(i);
            }
        }
        if (notify.isEmpty()) {
            return;
        }
        Collections.sort(notify);
        scrollPane.notify(true);
        SwingUtilities.invokeLater(() -> viewImpl.notifyUser(notify));
    }

    private void setProperty(String key, String value) {
        if (value != null) {
            properties.add(key);
            properties.add(value);
        }
    }

    private void setProperty(String key, Object value) {
        if (value != null) {
            if (value instanceof Boolean) {
                setProperty(key, getBooleanValueVerb(key, (boolean) value));
            } else {
                setProperty(key, value.toString());
            }
        }
    }

    private String getBooleanValueVerb(String key, boolean value) {
        try {
            if (value) {
                return OStrings
                        .getString(ISegmentPropertiesView.PROPERTY_TRANSLATION_VERB + key.toUpperCase());
            } else {
                return OStrings
                        .getString(ISegmentPropertiesView.PROPERTY_TRANSLATION_NVERB + key.toUpperCase());
            }
        } catch (MissingResourceException ex) {
            // fallback to default expression
            if (value) {
                return OStrings.getString(ISegmentPropertiesView.PROPERTY_TRANSLATION_VERB + "YES");
            } else {
                return OStrings.getString(ISegmentPropertiesView.PROPERTY_TRANSLATION_VERB + "NO");
            }
        }
    }

    private void setProperties(SourceTextEntry ste) {
        var oldProperties = new ArrayList<>(properties);
        properties.clear();
        if (ste == null) {
            viewImpl.update();
            return;
        }

        if (ste.getComment() != null) {
            setProperty(KEY_HASCOMMENT, true);
        }
        if (ste.getDuplicate() != DUPLICATE.NONE) {
            setProperty(KEY_ISDUP, ste.getDuplicate());
        }
        if (ste.getSourceTranslation() != null) {
            if (isTargetRtl) {
                setProperty(KEY_TRANSLATION, BiDiUtils.addRtlBidiAround(ste.getSourceTranslation()));
            } else {
                setProperty(KEY_TRANSLATION, ste.getSourceTranslation());
            }
            if (ste.isSourceTranslationFuzzy()) {
                setProperty(KEY_TRANSLATIONISFUZZY, true);
            }
        }
        setKeyProperties(ste.getKey());
        IProject project = Core.getProject();
        if (project.isProjectLoaded()) {
            TMXEntry trg = project.getTranslationInfo(ste);
            setTranslationProperties(trg);
        }
        viewImpl.update();
        changes.firePropertyChange("properties", oldProperties, properties);
    }

    private void setKeyProperties(EntryKey key) {
        setProperty(KEY_FILE, key.file);
        setProperty(KEY_ID, key.id);
        setProperty(KEY_PATH, key.path);
    }

    private void setTranslationProperties(TMXEntry entry) {
        if (entry == null) {
            return;
        }
        if (entry.hasNote()) {
            setProperty(KEY_HASNOTE, true);
        }
        if (!entry.isTranslated()) {
            return;
        }
        if (entry.changeDate != 0) {
            setProperty(KEY_CHANGED, dateFormat.format(new Date(entry.changeDate)) + " "
                    + timeFormat.format(new Date(entry.changeDate)));
        }
        setProperty(KEY_CHANGER, entry.changer);
        if (entry.creationDate != 0) {
            setProperty(KEY_CREATED, dateFormat.format(new Date(entry.creationDate)) + " "
                    + timeFormat.format(new Date(entry.creationDate)));
        }
        setProperty(KEY_CREATOR, entry.creator);
        if (!entry.defaultTranslation) {
            setProperty(KEY_ISALT, true);
        }
        setProperty(KEY_LINKED, entry.linked);
        if (entry.getPropValue(PROP_ORIGIN) != null) {
            setProperty(KEY_ORIGIN, entry.getPropValue(PROP_ORIGIN));
        } else {
            setProperty(KEY_ORIGIN, OStrings.getString("SEGPROP_ORIGIN_UNKNOWN"));
        }
    }

    private class SegmentPropertiesEntryEventListener implements IEntryEventListener {

        private final SegmentPropertiesArea segmentPropertiesArea;

        SegmentPropertiesEntryEventListener(SegmentPropertiesArea segmentPropertiesArea) {
            this.segmentPropertiesArea = segmentPropertiesArea;
        }

        @Override
        public void onNewFile(String activeFileName) {
            // nothing to do
        }

        @Override
        public void onEntryActivated(SourceTextEntry newEntry) {
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            segmentPropertiesArea.scrollPane.stopNotifying();
            segmentPropertiesArea.setTargetRtl(BiDiUtils.isTargetLangRtl());
            segmentPropertiesArea.setProperties(newEntry);
            segmentPropertiesArea.doNotify(getKeysToNotify());
        }

    }
}
