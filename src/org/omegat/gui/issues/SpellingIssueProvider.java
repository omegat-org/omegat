/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.issues;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;
import org.omegat.util.gui.Styles.EditorColor;

/**
 * A provider for spelling issues.
 *
 * @author Aaron Madlon-Kay
 *
 */
class SpellingIssueProvider implements IIssueProvider {

    @Override
    public String getName() {
        return OStrings.getString("ISSUES_SPELLING_PROVIDER_NAME");
    }

    @Override
    public String getId() {
        return getClass().getCanonicalName();
    }

    @Override
    public List<IIssue> getIssues(SourceTextEntry sourceEntry, TMXEntry tmxEntry) {
        List<Token> misspelled = Core.getSpellChecker().getMisspelledTokens(tmxEntry.translation);
        return misspelled.isEmpty() ? Collections.emptyList()
                : Arrays.asList(new SpellingIssue(sourceEntry, tmxEntry, misspelled));
    }

    /**
     * A class representing misspellings in a translation. One instance holds
     * all misspelled tokens for the given source/target text pair.
     *
     * @author Aaron Madlon-Kay
     *
     */
    static class SpellingIssue implements IIssue {
        private static final Icon ICON = new SimpleColorIcon(EditorColor.COLOR_SPELLCHECK.getColor());
        private static final AttributeSet ERROR_STYLE;
        static {
            SimpleAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setForeground(attr, EditorColor.COLOR_SPELLCHECK.getColor());
            StyleConstants.setBold(attr, true);
            ERROR_STYLE = attr;
        }

        private final SourceTextEntry ste;
        private final TMXEntry tmxEntry;
        private final List<Token> misspelledTokens;

        SpellingIssue(SourceTextEntry ste, TMXEntry tmxEntry, List<Token> misspelledTokens) {
            this.ste = ste;
            this.tmxEntry = tmxEntry;
            this.misspelledTokens = misspelledTokens;
        }

        @Override
        public Icon getIcon() {
            return ICON;
        }

        @Override
        public String getTypeName() {
            return OStrings.getString("ISSUES_SPELLING_TYPE");
        }

        @Override
        public int getSegmentNumber() {
            return ste.entryNum();
        }

        @Override
        public String getDescription() {
            return misspelledTokens.stream().map(tok -> tok.getTextFromString(tmxEntry.translation)).distinct()
                    .collect(Collectors.joining(OStrings.getString("ISSUES_SPELLING_WORD_DELIMITER")));
        }

        @Override
        public Component getDetailComponent() {
            IssueDetailSplitPanel panel = new IssueDetailSplitPanel();
            panel.firstTextPane.setText(ste.getSrcText());
            panel.lastTextPane.setText(tmxEntry.translation);
            StyledDocument doc = panel.lastTextPane.getStyledDocument();
            for (Token tok : misspelledTokens) {
                doc.setCharacterAttributes(tok.getOffset(), tok.getLength(), ERROR_STYLE, false);
            }
            panel.setMinimumSize(new Dimension(0, panel.firstTextPane.getFont().getSize() * 6));
            return panel;
        }

        @Override
        public boolean hasMenuComponents() {
            return true;
        }

        @Override
        public List<? extends JMenuItem> getMenuComponents() {
            List<JMenuItem> result = new ArrayList<>();
            ISpellChecker checker = Core.getSpellChecker();
            misspelledTokens.stream().map(tok -> tok.getTextFromString(tmxEntry.translation)).distinct()
                    .forEach(word -> {
                        boolean enabled = !checker.isIgnoredWord(word) && !checker.isLearnedWord(word);
                        JMenuItem item = new JMenuItem(
                                StringUtil.format(OStrings.getString("ISSUES_SPELLING_LEARN_ITEM"), word));
                        item.addActionListener(e -> checker.learnWord(word));
                        item.setEnabled(enabled);
                        result.add(item);
                        item = new JMenuItem(
                                StringUtil.format(OStrings.getString("ISSUES_SPELLING_IGNORE_ITEM"), word));
                        item.addActionListener(e -> checker.ignoreWord(word));
                        item.setEnabled(enabled);
                        result.add(item);
                    });
            return result;
        }
    }
}
