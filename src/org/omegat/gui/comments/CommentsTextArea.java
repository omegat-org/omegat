/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Zoltan Bartko
               2011 Martin Fleurke, Alex Buloichik
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

package org.omegat.gui.comments;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.gui.common.EntryInfoPane;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.IPaneMenu;
import org.omegat.util.gui.JTextPaneLinkifier;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * This is a pane that displays comments on source texts.
 *
 * @author Martin Fleurke
 * @author Alex Buloichik
 */
@SuppressWarnings("serial")
public class CommentsTextArea extends EntryInfoPane<SourceTextEntry> implements IEntryEventListener,
        IComments, IPaneMenu {

    private static final String EXPLANATION = OStrings.getString("GUI_COMMENTSWINDOW_explanation");

    private final List<ProviderStorage> providers = new ArrayList<ProviderStorage>();

    private final DockableScrollPane scrollPane;

    /** Creates new Comments Text Area Pane */
    public CommentsTextArea(IMainWindow mw) {
        super(true);

        String title = OStrings.getString("GUI_COMMENTSWINDOW_SUBWINDOWTITLE_Comments");
        scrollPane = new DockableScrollPane("COMMENTS", title, this, true);
        mw.addDockable(scrollPane);

        setEditable(false);
        StaticUIUtils.makeCaretAlwaysVisible(this);
        setText(EXPLANATION);
        setMinimumSize(new Dimension(100, 50));

        addCommentProvider(ENTRY_COMMENT_PROVIDER, 0);

        CoreEvents.registerEntryEventListener(this);

        JTextPaneLinkifier.linkify(this);
    }

    public void onEntryActivated(SourceTextEntry newEntry) {
        UIThreadsUtil.mustBeSwingThread();

        scrollPane.stopNotifying();

        List<ProviderStorage> list;
        synchronized (providers) {
            list = new ArrayList<ProviderStorage>(providers);
        }
        StringBuilder text = new StringBuilder(1024);
        for (ProviderStorage ps : list) {
            String c = ps.provider.getComment(newEntry);
            if (c != null) {
                text.append(c);
            }
        }

        setText(text.toString());
        setCaretPosition(0);
        if (text.length() > 0 && Preferences.isPreference(Preferences.NOTIFY_COMMENTS)) {
            scrollPane.notify(true);
        }
    }

    static final ICommentProvider ENTRY_COMMENT_PROVIDER = new ICommentProvider() {
        public String getComment(SourceTextEntry newEntry) {
            StringBuilder text = new StringBuilder(1024);
            if (newEntry.getKey().id != null) {
                text.append(OStrings.getString("GUI_COMMENTSWINDOW_FIELD_ID"));
                text.append(' ');
                text.append(newEntry.getKey().id);
                text.append('\n');
            }
            if (newEntry.getKey().path != null) {
                text.append(OStrings.getString("GUI_COMMENTSWINDOW_FIELD_Path"));
                text.append(' ');
                text.append(newEntry.getKey().path);
                text.append('\n');
            }
            if (newEntry.getSourceTranslation() != null) {
                text.append(OStrings.getString("GUI_COMMENTSWINDOW_FIELD_Translation"));
                text.append(' ');
                text.append(newEntry.getSourceTranslation());
                text.append('\n');
            }
            if (newEntry.getComment() != null) {
                text.append(OStrings.getString("GUI_COMMENTSWINDOW_FIELD_Comment"));
                text.append('\n');
                text.append(newEntry.getComment());
                text.append('\n');
            }
            return text.toString();
        }
    };

    public void onNewFile(String activeFileName) {
    }

    @Override
    protected void onProjectOpen() {
        clear();
    }

    @Override
    protected void onProjectClose() {
        clear();
        setText(EXPLANATION);
    }

    @Override
    public void addCommentProvider(ICommentProvider provider, int priority) {
        ProviderStorage s = new ProviderStorage();
        s.provider = provider;
        s.priority = priority;
        synchronized (providers) {
            providers.add(s);
            Collections.sort(providers, new Comparator<ProviderStorage>() {
                @Override
                public int compare(ProviderStorage o1, ProviderStorage o2) {
                    return o1.priority < o2.priority ? -1 : o1.priority > o2.priority ? 1 : 0;
                }
            });
        }
    }

    @Override
    public void removeCommentProvider(ICommentProvider provider) {
        synchronized (providers) {
            for (int i = 0; i < providers.size(); i++) {
                if (providers.get(i).provider == provider) {
                    providers.remove(i);
                    break;
                }
            }
        }
    }

    static class ProviderStorage {
        ICommentProvider provider;
        int priority;
    }

    @Override
    public void populatePaneMenu(JPopupMenu menu) {
        final JMenuItem notify = new JCheckBoxMenuItem(OStrings.getString("GUI_COMMENTSWINDOW_SETTINGS_NOTIFICATIONS"));
        notify.setSelected(Preferences.isPreference(Preferences.NOTIFY_COMMENTS));
        notify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Preferences.setPreference(Preferences.NOTIFY_COMMENTS, notify.isSelected());
            }
        });
        menu.add(notify);
    }
}
