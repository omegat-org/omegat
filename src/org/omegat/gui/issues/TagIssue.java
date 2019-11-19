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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.omegat.core.Core;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.tagvalidation.ErrorReport;
import org.omegat.core.tagvalidation.ErrorReport.TagError;
import org.omegat.core.tagvalidation.TagValidationTool;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;
import org.omegat.util.TagUtil.Tag;
import org.omegat.util.gui.CharacterWrapEditorKit;
import org.omegat.util.gui.Styles.EditorColor;
import org.openide.awt.Mnemonics;

/**
 * A class representing problems with tags in a translation. One instance holds
 * all problems for the given source/target text pair.
 *
 * @author Aaron Madlon-Kay
 *
 */
public class TagIssue implements IIssue {
    private static final Icon ICON = new SimpleColorIcon(EditorColor.COLOR_PLACEHOLDER.getColor());

    private final ErrorReport report;

    public TagIssue(ErrorReport report) {
        this.report = report;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getTypeName() {
        return OStrings.getString("ISSUES_TAGS_TYPE");
    }

    @Override
    public int getSegmentNumber() {
        return report.entryNum;
    }

    @Override
    public String getDescription() {
        Map<TagError, Long> freq = Stream.of(report.srcErrors, report.transErrors).flatMap(m -> m.values().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return "<html>" + freq.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(e -> {
            TagError error = e.getKey();
            String message = ErrorReport.localizedTagError(e.getKey());
            String html = colorizeByHTML(message, error);
            return StringUtil.format(OStrings.getString("ISSUES_TAGS_ERROR_TEMPLATE"), html, e.getValue());
        }).collect(Collectors.joining(OStrings.getString("ISSUES_TAGS_ERROR_DELIMITER"))) + "</html>";
    }

    @Override
    public Component getDetailComponent() {
        TripleSplitButtonPanel panel = new TripleSplitButtonPanel();
        panel.firstTextPane.setEditorKit(new CharacterWrapEditorKit());
        panel.middleTextPane.setEditorKit(new CharacterWrapEditorKit());
        panel.lastTextPane.setEditorKit(new CharacterWrapEditorKit());
        panel.firstButton.setBorderPainted(false);
        panel.middleButton.setBorderPainted(false);
        try {
            insertText(panel.firstTextPane.getStyledDocument(), report.source, report.srcErrors);
            insertText(panel.middleTextPane.getStyledDocument(), report.translation, report.transErrors);
        } catch (BadLocationException ex) {
            // Ignore
        }
        int minHeight = panel.firstTextPane.getFont().getSize() * 6;
        String fixText = TagValidationTool.fixErrors(report);
        if (fixText == null) {
            panel.lastPanel.setVisible(false);
            panel.firstButton.setVisible(false);
            panel.middleButton.setVisible(false);
        } else {
            panel.lastTextPane.setText(fixText);
            Mnemonics.setLocalizedText(panel.lastButton, OStrings.getString("ISSUES_TAGS_BUTTON_APPLY_FIX"));
            panel.lastButton.addActionListener(getFixActionListener(fixText));
            minHeight += panel.lastButton.getPreferredSize().height;
        }
        panel.setMinimumSize(new Dimension(0, minHeight));
        return panel;
    }

    private void insertText(StyledDocument doc, String text, Map<Tag, TagError> errors)
            throws BadLocationException {
        doc.insertString(0, text, null);
        for (Map.Entry<Tag, TagError> e : errors.entrySet()) {
            Tag tag = e.getKey();
            doc.setCharacterAttributes(tag.pos, tag.tag.length(), styleForError(e.getValue()), false);
        }
    }

    @Override
    public boolean hasMenuComponents() {
        return true;
    }

    @Override
    public List<? extends JMenuItem> getMenuComponents() {
        String fixText = TagValidationTool.fixErrors(report);
        if (fixText == null) {
            return Collections.emptyList();
        } else {
            JMenuItem doFix = new JMenuItem();
            org.openide.awt.Mnemonics.setLocalizedText(doFix, OStrings.getString("ISSUES_TAGS_BUTTON_APPLY_FIX"));
            doFix.addActionListener(getFixActionListener(fixText));
            return Arrays.asList(doFix);
        }
    }

    private ActionListener getFixActionListener(String fixText) {
        return e -> {
            if (!doFix(report, fixText)) {
                JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(),
                        OStrings.getString("TAG_FIX_ERROR_MESSAGE"), OStrings.getString("TAG_FIX_ERROR_TITLE"),
                        JOptionPane.ERROR_MESSAGE);
            }
        };
    }

    @SuppressWarnings("fallthrough")
    public static AttributeSet styleForError(TagError error) {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setBold(attr, true);
        switch (error) {
        case EXTRANEOUS:
            StyleConstants.setStrikeThrough(attr, true);
        case MISSING:
        case MALFORMED:
        case WHITESPACE:
            StyleConstants.setForeground(attr, Color.RED);
            break;
        case DUPLICATE:
            StyleConstants.setForeground(attr, Color.decode("#800080")); // purple
            break;
        case ORPHANED:
            StyleConstants.setUnderline(attr, true);
        case ORDER:
            StyleConstants.setForeground(attr, Color.decode("#FF8C00")); // orange
            break;
        case UNSPECIFIED:
            StyleConstants.setForeground(attr, Color.BLUE);
            break;
        }
        return attr;
    }

    /**
     * Fix all errors in a given report, and commit the changed translation to
     * the project. Checks to make sure the translation has not been changed in
     * the meantime.
     *
     * @param report
     *            The report to fix
     * @return Whether or not the fix succeeded
     */
    private static boolean doFix(ErrorReport report, String fixed) {
        // Make sure the translation hasn't changed in the editor.
        TMXEntry prevTrans = Core.getProject().getTranslationInfo(report.ste);
        if (!report.translation.equals(prevTrans.translation)) {
            return false;
        }

        // Put modified translation back into project.
        Core.getEditor().gotoEntry(report.entryNum);
        Core.getEditor().replaceEditTextAndMark(fixed);

        return true;
    }

    @SuppressWarnings("fallthrough")
    public static String colorizeByHTML(String text, TagError error) {
        String color = "black";
        switch (error) {
        case EXTRANEOUS:
            text = "<strike>" + text + "</strike>";
        case MISSING:
        case MALFORMED:
        case WHITESPACE:
            color = "red";
            break;
        case DUPLICATE:
            color = "purple";
            break;
        case ORPHANED:
            text = "<u>" + text + "</u>";
        case ORDER:
            color = "#FF8C00"; // Orange. Pre-1.7 Java doesn't recognize the
                               // name "orange".
            break;
        case UNSPECIFIED:
            color = "blue";
            break;
        }
        return "<font color=\"" + color + "\"><b>" + text + "</b></font>";
    }
}
