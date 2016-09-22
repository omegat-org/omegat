/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.languagetools;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.languagetool.rules.RuleMatch;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.gui.issues.IIssue;
import org.omegat.gui.issues.IIssueProvider;
import org.omegat.gui.issues.IssueDetailSplitPanel;
import org.omegat.gui.issues.SimpleColorIcon;
import org.omegat.languagetools.LanguageToolWrapper.NotLoadedException;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.gui.Styles.EditorColor;

/**
 * Class for providing LanguageTool reports to the issues GUI.
 * 
 * @author Aaron Madlon-Kay
 *
 */
public class LanguageToolIssueProvider implements IIssueProvider {

    private final LanguageToolWrapper wrapper;

    public LanguageToolIssueProvider(LanguageToolWrapper languageToolWrapper) {
        this.wrapper = languageToolWrapper;
    }

    @Override
    public List<IIssue> getIssues(SourceTextEntry sourceEntry, TMXEntry tmxEntry) {
        try {
            return wrapper.getRuleMatches(sourceEntry.getSrcText(), tmxEntry.translation).stream()
                    .map(match -> new LanguageToolIssue(sourceEntry, tmxEntry.translation, match))
                    .collect(Collectors.toList());
        } catch (NotLoadedException e) {
        } catch (Exception e) {
            Log.log(e);
        }
        return Collections.emptyList();
    }

    @Override
    public String getId() {
        return getClass().getCanonicalName();
    }

    @Override
    public String getName() {
        return OStrings.getString("ISSUES_LANGUAGETOOL_PROVIDER_NAME");
    }

    static class LanguageToolIssue implements IIssue {

        static final Icon ICON = new SimpleColorIcon(EditorColor.COLOR_LANGUAGE_TOOLS.getColor());
        static final AttributeSet ERROR_STYLE;
        static {
            SimpleAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setForeground(attr, EditorColor.COLOR_LANGUAGE_TOOLS.getColor());
            StyleConstants.setBold(attr, true);
            ERROR_STYLE = attr;
        }

        private final SourceTextEntry ste;
        private final String targetText;
        private final RuleMatch match;

        public LanguageToolIssue(SourceTextEntry ste, String targetText, RuleMatch match) {
            this.ste = ste;
            this.targetText = targetText;
            this.match = match;
        }

        @Override
        public Icon getIcon() {
            return ICON;
        }

        @Override
        public String getTypeName() {
            return OStrings.getString("ISSUES_LT_TYPE");
        }

        @Override
        public int getSegmentNumber() {
            return ste.entryNum();
        }

        @Override
        public String getDescription() {
            return "<html>" + match.getMessage().replace("<suggestion>", "<i>").replace("</suggestion>", "</i>")
                    + "<html>";
        }

        @Override
        public Component getDetailComponent() {
            IssueDetailSplitPanel panel = new IssueDetailSplitPanel();
            panel.firstTextPane.setText(ste.getSrcText());
            panel.lastTextPane.setText(targetText);
            StyledDocument doc = panel.lastTextPane.getStyledDocument();
            doc.setCharacterAttributes(match.getFromPos(), match.getToPos() - match.getFromPos(), ERROR_STYLE, false);
            panel.setMinimumSize(new Dimension(0, panel.firstTextPane.getFont().getSize() * 6));
            return panel;
        }
    }
}
