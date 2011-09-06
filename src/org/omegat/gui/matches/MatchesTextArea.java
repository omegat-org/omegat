/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Zoltan Bartko
               2011 John Moran
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

package org.omegat.gui.matches;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.StringData;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.matching.ITokenizer;
import org.omegat.core.matching.NearString;
import org.omegat.gui.common.EntryInfoPane;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * This is a Match pane, that displays fuzzy matches.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Zoltan Bartko
 * @author John Moran
 */
public class MatchesTextArea extends EntryInfoPane<List<NearString>> implements IMatcher {

    private final List<NearString> matches = new ArrayList<NearString>();

    private final List<Integer> delimiters = new ArrayList<Integer>();
    private int activeMatch;

    private final MainWindow mw;

    /** Creates new form MatchGlossaryPane */
    public MatchesTextArea(MainWindow mw) {
        super(true);
        this.mw = mw;

        String title = OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_Fuzzy_Matches");
        Core.getMainWindow().addDockable(new DockableScrollPane("MATCHES", title, this, true));

        setEditable(false);
        setMinimumSize(new Dimension(100, 50));

        addMouseListener(mouseListener);
    }

    @Override
    protected void startSearchThread(SourceTextEntry newEntry) {
        new FindMatchesThread(MatchesTextArea.this, Core.getProject(), newEntry).start();
    }

    /**
     * Sets the list of fuzzy matches to show in the pane. Each element of the
     * list should be an instance of {@link NearString}.
     */
    @Override
    protected void setFoundResult(final SourceTextEntry se, List<NearString> newMatches) {
        UIThreadsUtil.mustBeSwingThread();

        activeMatch = -1;
        matches.clear();
        delimiters.clear();

        if (newMatches == null) {
            setText("");
            return;
        }

        matches.addAll(newMatches);
        delimiters.add(0);
        StringBuffer displayBuffer = new StringBuffer();

        for (int i = 0; i < newMatches.size(); i++) {
            NearString match = newMatches.get(i);
            displayBuffer.append(MessageFormat.format("{0}) {1}\n{2}\n<{3}/{4}/{5}% {6} >", i + 1,
                    match.source, match.translation, match.score, match.scoreNoStem, match.adjustedScore,
                    match.proj));

            if (i < (newMatches.size() - 1))
                displayBuffer.append("\n\n");
            delimiters.add(displayBuffer.length());
        }

        setText(displayBuffer.toString());
        setActiveMatch(0);

        checkForReplaceTranslation();
    }

    @Override
    protected void onProjectOpen() {
        clear();
    }

    @Override
    protected void onProjectClose() {
        clear();
    }

    /**
     * {@inheritDoc}
     */
    public NearString getActiveMatch() {
        UIThreadsUtil.mustBeSwingThread();

        if (activeMatch < 0 || activeMatch >= matches.size()) {
            return null;
        } else {
            return matches.get(activeMatch);
        }
    }

    /**
     * if WORKFLOW_OPTION "Insert best fuzzy match into target field" is set
     * 
     * RFE "Option: Insert best match (80%+) into target field"
     * 
     * http://sourceforge.net/support/tracker.php?aid=1075976
     */
    private void checkForReplaceTranslation() {
        if (matches.isEmpty()) {
            return;
        }
        if (Preferences.isPreference(Preferences.BEST_MATCH_INSERT)) {
            String percentage_s = Preferences.getPreferenceDefault(Preferences.BEST_MATCH_MINIMAL_SIMILARITY,
                    Preferences.BEST_MATCH_MINIMAL_SIMILARITY_DEFAULT);
            // <HP-experiment>
            int percentage = 0;
            try {
                // int
                percentage = Integer.parseInt(percentage_s);
            } catch (Exception exception) {
                Log.log("ERROR: exception while parsing percentage:");
                Log.log("Please report to the OmegaT developers (omegat-development@lists.sourceforge.net)");
                Log.log(exception);
                return; // deliberately breaking, to simulate previous behaviour
                // FIX: unknown, but expect number parsing errors
            }
            // </HP-experiment>
            NearString thebest = matches.get(0);
            if (thebest.score >= percentage) {
            	String translation = null;

		if (Preferences.getPreferenceDefaultAllowEmptyString(
		        Preferences.BEST_MATCH_EXPLANATORY_TEXT).equals("")) {
			translation = thebest.translation;
                } else {
                    translation = Preferences.getPreferenceDefault(
			    Preferences.BEST_MATCH_EXPLANATORY_TEXT,
			    OStrings.getString("WF_DEFAULT_PREFIX"))
			    + thebest.translation;
		}
                SourceTextEntry currentEntry = Core.getEditor().getCurrentEntry();
                TMXEntry te = Core.getProject().getTranslation(currentEntry);
                if (te == null) {
                    Core.getEditor().replaceEditText(translation);
                }
            }
        }
    }

    /**
     * Sets the index of an active match. It basically highlights the fuzzy
     * match string selected. (numbers start from 0)
     */
    public void setActiveMatch(int activeMatch) {
        UIThreadsUtil.mustBeSwingThread();

        if (activeMatch < 0 || activeMatch >= matches.size() || this.activeMatch == activeMatch) {
            return;
        }

        this.activeMatch = activeMatch;

        selectAll();
        setCharacterAttributes(Styles.createAttributeSet(null, null, null, null), true);

        int start = delimiters.get(activeMatch);
        int end = delimiters.get(activeMatch + 1);

        NearString match = matches.get(activeMatch);
        // List tokens = match.str.getSrcTokenList();
        ITokenizer tokenizer = Core.getProject().getSourceTokenizer();
        if (tokenizer == null) {
            return;
        }
        Token[] tokens = tokenizer.tokenizeAllExactly(match.source);
        // fix for bug 1586397
        byte[] attributes = match.attr;
        for (int i = 0; i < tokens.length; i++) {
            Token token = tokens[i];
            int tokstart = start + 3 + token.getOffset();
            int tokend = start + 3 + token.getOffset() + token.getLength();
            select(tokstart, tokend);
            if ((attributes[i] & StringData.UNIQ) != 0) {
                setCharacterAttributes(Styles.createAttributeSet(Color.blue, null, null, null), false);
            } else if ((attributes[i] & StringData.PAIR) != 0) {
                setCharacterAttributes(Styles.createAttributeSet(Color.green, null, null, null), false);
            }
        }

        select(start, end);
        setCharacterAttributes(Styles.createAttributeSet(null, null, true, null), false);
        setCaretPosition(end - 2); // two newlines
        final int fstart = start;

        setCaretPosition(fstart);
    }

    /** Clears up the pane. */
    public void clear() {
        UIThreadsUtil.mustBeSwingThread();

        setFoundResult(null, new ArrayList<NearString>());
    }

    protected MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            // is there anything?
            if (matches == null || matches.isEmpty())
                return;

            // find out the clicked item
            int clickedItem = -1;

            // where did we click?
            int mousepos = MatchesTextArea.this.viewToModel(e.getPoint());

            int i;
            for (i = 0; i < delimiters.size() - 1; i++) {
                int start = delimiters.get(i);
                int end = delimiters.get(i + 1);

                if (mousepos >= start && mousepos < end) {
                    clickedItem = i;
                    break;
                }
            }

            if (clickedItem == -1)
                clickedItem = delimiters.size() - 1;

            if (clickedItem >= matches.size())
                return;

            // set up the menu
            if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                mouseRightClick(clickedItem, e.getPoint());
            }
        }
    };

    private void mouseOneClick(final int clickedItem, final Point clickedPoint) {
        // show colored source segment
    }

    private void mouseRightClick(final int clickedItem, final Point clickedPoint) {
        // create the menu
        JPopupMenu popup = new JPopupMenu();

        JMenuItem item = popup.add(OStrings.getString("MATCHES_INSERT"));
        item.addActionListener(new ActionListener() {
            // the action: insert this match
            public void actionPerformed(ActionEvent e) {
                setActiveMatch(clickedItem);
                mw.doInsertTrans();
            }
        });

        item = popup.add(OStrings.getString("MATCHES_REPLACE"));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setActiveMatch(clickedItem);
                mw.doRecycleTrans();
            }
        });

        popup.addSeparator();

        final NearString ns = matches.get(clickedItem);
        String proj = ns.proj;

        item = popup.add(OStrings.getString("MATCHES_GO_TO_SEGMENT_SOURCE"));

        if (StringUtil.isEmpty(proj)) {
            final IProject project = Core.getProject();
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    /*
                     * Goto segment with contains matched source. Since it enough rarely executed code, it
                     * will be better to find this segment each time, instead use additional memory storage.
                     */
                    List<SourceTextEntry> entries = Core.getProject().getAllEntries();
                    for (int i = 0; i < entries.size(); i++) {
                        SourceTextEntry ste = entries.get(i);
                        if (!ste.getSrcText().equals(ns.source)) {
                            // source text not equals - there is no sence to checking this entry
                            continue;
                        }
                        if (ns.key != null) {
                            // multiple translation
                            if (!ste.getKey().equals(ns.key)) {
                                continue;
                            }
                        } else {
                            // default translation - multiple shoun't exist for this entry
                            TMXEntry multTrans = project.getMultipleTranslation(entries.get(i));
                            if (multTrans != null) {
                                continue;
                            }
                        }
                        Core.getEditor().gotoEntry(i + 1);
                        break;
                    }
                }
            });
        } else {
            item.setEnabled(false);
        }

        popup.show(this, clickedPoint.x, clickedPoint.y);
    }
}
