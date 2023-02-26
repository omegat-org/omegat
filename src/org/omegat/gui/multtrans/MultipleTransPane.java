/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
               2012 Jean-Christophe Helary
               2014 Aaron Madlon-Kay
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
package org.omegat.gui.multtrans;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

import org.openide.awt.Mnemonics;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.common.EntryInfoThreadPane;
import org.omegat.gui.editor.EditorPopups;
import org.omegat.gui.editor.IPopupMenuConstructor;
import org.omegat.gui.editor.SegmentBuilder;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.IMainWindow;
import org.omegat.gui.shortcuts.PropertiesShortcuts;
import org.omegat.util.Java8Compat;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.IPaneMenu;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Pane for display information about multiple translations.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Jean-Christophe Helary
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class MultipleTransPane extends EntryInfoThreadPane<List<MultipleTransFoundEntry>> implements IPaneMenu {

    private static final String EXPLANATION = OStrings.getString("GUI_MULTIPLETRANSLATIONSWINDOW_explanation");

    private List<DisplayedEntry> entries = new ArrayList<DisplayedEntry>();

    private final DockableScrollPane scrollPane;

    public MultipleTransPane(IMainWindow mw) {
        super(true);

        String title = OStrings.getString("MULT_TITLE");
        scrollPane = new DockableScrollPane("MULTIPLE_TRANS", title, this, true);
        mw.addDockable(scrollPane);

        setEditable(false);
        StaticUIUtils.makeCaretAlwaysVisible(this);
        setText(EXPLANATION);
        setMinimumSize(new Dimension(100, 50));

        Core.getEditor().registerPopupMenuConstructors(600, new IPopupMenuConstructor() {
            public void addItems(JPopupMenu menu, JTextComponent comp, int mousepos, boolean isInActiveEntry,
                    boolean isInActiveTranslation, final SegmentBuilder sb) {
                if (isInActiveEntry
                        && Core.getProject().getProjectProperties().isSupportDefaultTranslations()) {
                    JMenuItem miDefault = menu.add(Mnemonics.removeMnemonics(
                        OStrings.getString("MULT_MENU_DEFAULT")));
                    JMenuItem miMultiple = menu.add(Mnemonics.removeMnemonics(
                        OStrings.getString("MULT_MENU_MULTIPLE")));
                    miDefault.setEnabled(!sb.isDefaultTranslation());
                    miMultiple.setEnabled(sb.isDefaultTranslation());

                    miDefault.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            Core.getEditor().setAlternateTranslationForCurrentEntry(false);
                            Core.getEditor().commitAndLeave();
                        }
                    });
                    miMultiple.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            Core.getEditor().setAlternateTranslationForCurrentEntry(true);
                        }
                    });
                }
            }
        });

        addMouseListener(mouseListener);
    }

    @Override
    public void onEntryActivated(SourceTextEntry newEntry) {
        scrollPane.stopNotifying();
        super.onEntryActivated(newEntry);
    }

    @Override
    protected void setFoundResult(SourceTextEntry processedEntry, List<MultipleTransFoundEntry> data) {
        UIThreadsUtil.mustBeSwingThread();

        clear();

        // Check case if current segment has default translation and there are no alternative translations.
        if (data.size() == 1 && data.get(0).key == null) {
            return;
        }

        if (!data.isEmpty() && Preferences.isPreference(Preferences.NOTIFY_MULTIPLE_TRANSLATIONS)) {
            scrollPane.notify(true);
        }

        StringBuilder o = new StringBuilder();
        for (MultipleTransFoundEntry e : data) {
            DisplayedEntry de = new DisplayedEntry();
            de.entry = e;
            de.start = o.length();
            if (e.entry.translation == null) {
                continue;
            }
            if (e.key != null) {
                o.append(e.entry.translation).append('\n');
                o.append('<').append(e.key.file);
                if (e.key.id != null) {
                    o.append('/').append(e.key.id);
                }
                o.append(">\n");
                if (e.key.prev != null && e.key.next != null) {
                    o.append('(').append(StringUtil.truncate(e.key.prev, 10));
                    o.append(" <...> ").append(StringUtil.truncate(e.key.next, 10)).append(")\n");
                }
            } else {
                o.append(e.entry.translation).append('\n');
            }
            de.end = o.length();
            entries.add(de);
            o.append('\n');
        }

        setText(o.toString());
    }

    @Override
    public void clear() {
        super.clear();
        entries.clear();
    }

    @Override
    protected void onProjectOpen() {
        UIThreadsUtil.mustBeSwingThread();

        clear();
    }

    @Override
    protected void onProjectClose() {
        UIThreadsUtil.mustBeSwingThread();

        clear();
        setText(EXPLANATION);
    }

    @Override
    protected void startSearchThread(SourceTextEntry newEntry) {
        new MultipleTransFindThread(this, Core.getProject(), newEntry).start();
    }

    private DisplayedEntry getEntryAtPosition(int pos) {
        for (DisplayedEntry de : entries) {
            if (de.start <= pos && de.end >= pos) {
                return de;
            }
        }
        return null;
    }

    protected final transient MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPopup(e.getPoint());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPopup(e.getPoint());
            }
        }

        private void doPopup(Point p) {
            if (entries.isEmpty()) {
                return;
            }
            JPopupMenu popup = new JPopupMenu();
            populateContextMenu(popup, Java8Compat.viewToModel(MultipleTransPane.this, p));
            popup.show(MultipleTransPane.this, p.x, p.y);
        }
    };

    private void populateContextMenu(JPopupMenu popup, int pos) {
        final DisplayedEntry de = getEntryAtPosition(pos);

        JMenuItem item;
        // default translation
        item = popup.add(OStrings.getString("MULT_POPUP_DEFAULT"));
        item.setEnabled(de != null && de.entry.key != null);
        if (de != null && de.entry.key != null) {
            item.addActionListener(e -> {
                Core.getEditor().replaceEditText(de.entry.entry.translation);
                Core.getEditor().setAlternateTranslationForCurrentEntry(false);
                Core.getEditor().commitAndLeave();
            });
        }
        // non-default translation
        item = popup.add(OStrings.getString("MULT_POPUP_REPLACE"));
        item.setEnabled(de != null);
        if (de != null) {
            item.addActionListener(e -> Core.getEditor().replaceEditText(de.entry.entry.translation));
        }

        item = popup.add(OStrings.getString("MULT_POPUP_GOTO"));
        item.setEnabled(de != null);
        if (de != null) {
            item.addActionListener(e -> Core.getEditor().gotoEntry(de.entry.sourceText, de.entry.key));
        }
    }

    protected static class DisplayedEntry {
        int start, end;
        MultipleTransFoundEntry entry;
    }

    @Override
    public void populatePaneMenu(JPopupMenu menu) {
        //populateContextMenu(menu, getCaretPosition());
        //menu.addSeparator();
        final JMenuItem notify = new JCheckBoxMenuItem(OStrings.getString("MULT_SETTINGS_NOTIFY"));
        notify.setSelected(Preferences.isPreference(Preferences.NOTIFY_MULTIPLE_TRANSLATIONS));
        notify.addActionListener(e -> Preferences.setPreference(Preferences.NOTIFY_MULTIPLE_TRANSLATIONS,
                notify.isSelected()));
        menu.add(notify);
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        KeyStroke s = KeyStroke.getKeyStrokeForEvent(e);
        if (s.equals(PropertiesShortcuts.getEditorShortcuts().getKeyStroke("editorContextMenu"))) {
            JPopupMenu popup = new JPopupMenu();
            Caret caret = getCaret();
            Point p = caret == null ? getMousePosition() : caret.getMagicCaretPosition();
            populateContextMenu(popup, Java8Compat.viewToModel(MultipleTransPane.this, p));
            popup.show(this, (int) p.getX(), (int) p.getY());
            e.consume();
        }
        super.processKeyEvent(e);
    }
}
