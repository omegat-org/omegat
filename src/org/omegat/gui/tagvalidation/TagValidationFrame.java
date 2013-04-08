/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers
               2007 Didier Briel
               2008-2009 Martin Fleurke
               2013 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.tagvalidation;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.gui.HListener;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.tagvalidation.ErrorReport.TagError;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.openide.awt.Mnemonics;

/**
 * A frame to display the tags with errors during tag validation.
 * 
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Didier Briel
 * @author Martin Fleurke
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class TagValidationFrame extends JFrame {
    public TagValidationFrame(MainWindow parent) {
        setTitle(OStrings.getString("TF_NOTICE_BAD_TAGS"));

        // set window size & position
        initWindowLayout();

        // Handle escape key to close the window
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);

        // Configure close button
        JButton closeButton = new JButton();
        Mnemonics.setLocalizedText(closeButton, OStrings.getString("BUTTON_CLOSE"));
        closeButton.addActionListener(escapeAction);

        // Fix All button
        m_fixAllButton = new JButton();
        Mnemonics.setLocalizedText(m_fixAllButton, OStrings.getString("BUTTON_FIX_ALL"));
        m_fixAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(Core.getMainWindow().getApplicationFrame(),
                        StaticUtils.format(OStrings.getString("TAG_FIX_ALL_WARNING"), m_numFixableErrors),
                        OStrings.getString("CONFIRM_DIALOG_TITLE"), JOptionPane.YES_NO_OPTION)) {
                    return;
                }
                List<Integer> fixed = fixAllEntries();
                Core.getEditor().refreshViewAfterFix(fixed);
            }
        });

        m_editorPane = new JEditorPane();
        m_editorPane.setEditable(false);
        m_editorPane.addHyperlinkListener(new HListener(parent, this, true)); // fix for bug 1542937
        JScrollPane scroller = new JScrollPane(m_editorPane);

        Box bbut = Box.createHorizontalBox();
        bbut.add(Box.createHorizontalGlue());
        bbut.add(m_fixAllButton);
        bbut.add(Box.createHorizontalStrut(10));
        bbut.add(closeButton);
        bbut.add(Box.createHorizontalGlue());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scroller, BorderLayout.CENTER);
        getContentPane().add(bbut, BorderLayout.SOUTH);

        CoreEvents.registerFontChangedEventListener(new IFontChangedEventListener() {
            public void onFontChanged(Font newFont) {
                TagValidationFrame.this.setFont(newFont);
            }
        });
        setFont(Core.getMainWindow().getApplicationFont());
    }

    /** Call this to set OmegaT-wide font for the Tag Validation window. */
    public void setFont(Font f) {
        super.setFont(f);
        if (isVisible())
            update();
    }

    /**
     * Loads/sets the position and size of the tag validation window.
     */
    private void initWindowLayout() {
        // main window
        try {
            String dx = Preferences.getPreference(Preferences.TAGVWINDOW_X);
            String dy = Preferences.getPreference(Preferences.TAGVWINDOW_Y);
            int x = Integer.parseInt(dx);
            int y = Integer.parseInt(dy);
            setLocation(x, y);
            String dw = Preferences.getPreference(Preferences.TAGVWINDOW_WIDTH);
            String dh = Preferences.getPreference(Preferences.TAGVWINDOW_HEIGHT);
            int w = Integer.parseInt(dw);
            int h = Integer.parseInt(dh);
            setSize(w, h);
        } catch (NumberFormatException nfe) {
            // set default size and position
            setSize(650, 700);
        }
    }

    /**
     * Saves the size and position of the tag validation window
     */
    private void saveWindowLayout() {
        Preferences.setPreference(Preferences.TAGVWINDOW_WIDTH, getWidth());
        Preferences.setPreference(Preferences.TAGVWINDOW_HEIGHT, getHeight());
        Preferences.setPreference(Preferences.TAGVWINDOW_X, getX());
        Preferences.setPreference(Preferences.TAGVWINDOW_Y, getY());
    }

    public void processWindowEvent(WindowEvent w) {
        int evt = w.getID();
        if (evt == WindowEvent.WINDOW_CLOSING || evt == WindowEvent.WINDOW_CLOSED) {
            // save window size and position
            saveWindowLayout();
        }
        super.processWindowEvent(w);
    }

    private void doCancel() {
        dispose();
    }

    /** replaces all &lt; and &gt; with &amp;lt; and &amp;gt; */
    private String htmlize(String str) {
        String htmld = str;
        htmld = htmld.replaceAll("\\<", "&lt;");
        htmld = htmld.replaceAll("\\>", "&gt;");
        htmld = htmld.replaceAll("\n", "<br>");
        return htmld;
    }

    /**
     * Replace tags with &lt;font
     * color="color"&gt;&lt;b&gt;&lt;tag&gt;&lt;/b&gt;&lt;/font&gt;
     */
    private String colorTags(String str, String color, Pattern placeholderPattern, Pattern removePattern,
            SourceTextEntry.ProtectedParts protectedParts, Map<String, TagError> errors) {
        // show OmegaT tags in bold and color, and to-remove text also
        String htmlResult = formatRemoveTagsAndPlaceholders(str, color, placeholderPattern, removePattern,
                protectedParts, errors);

        // show linefeed as symbol
        Matcher lfMatch = PatternConsts.HTML_BR.matcher(htmlResult);
        // /simulate unicode symbol for linefeed "\u240A", which is not
        // displayed correctly.
        htmlResult = lfMatch.replaceAll("<font color=\"" + color + "\"><sup>L</sup>F<br></font>");
        return htmlResult;
    }
    
    /**
     * Formats plain text as html with placeholders in color 
     * @param str the text to format
     * @param color the color to use
     * @param placeholderPattern the pattern to decide what is a placeholder
     * @return html text
     */
    private String formatPlaceholders(String str, String color, Pattern placeholderPattern,
            SourceTextEntry.ProtectedParts protectedParts, Map<String, TagError> errors) {
        List<TextPart> text = new ArrayList<TextPart>();
        text.add(new TextPart(str, false));
        while (true) {
            boolean updated = false;
            if (protectedParts != null) {
                for (String p : protectedParts.getParts()) {
                    for (int i = 0; i < text.size(); i++) {
                        TextPart tp = text.get(i);
                        if (tp.highlighted) {
                            continue;
                        }
                        int pos = tp.text.indexOf(p);
                        if (pos >= 0) {
                            split(text, i, pos, pos + p.length());
                            updated = true;
                        }
                    }
                }
            }
            for (int i = 0; i < text.size(); i++) {
                TextPart tp = text.get(i);
                if (tp.highlighted) {
                    continue;
                }
                Matcher placeholderMatcher = placeholderPattern.matcher(tp.text);
                if (placeholderMatcher.find()) {
                    split(text, i, placeholderMatcher.start(), placeholderMatcher.end());
                    updated = true;
                }
            }
            if (!updated) {
                break;
            }
        }
        StringBuilder htmlResult = new StringBuilder();
        for (TextPart tp : text) {
            if (tp.highlighted) {
                htmlResult.append(colorize(htmlize(tp.text), errors.get(tp.text)));
            } else {
                htmlResult.append(htmlize(tp.text));
            }
        }
        return htmlResult.toString();
    }

    private void split(List<TextPart> text, int index, int beg, int end) {
        int i = index;
        String tpText = text.remove(i).text;
        if (beg > 0) {
            text.add(i, new TextPart(tpText.substring(0, beg), false));
            i++;
        }
        text.add(i, new TextPart(tpText.substring(beg, end), true));
        i++;
        if (end < tpText.length()) {
            text.add(i, new TextPart(tpText.substring(end), false));
        }
    }

    protected static class TextPart {
        String text;
        boolean highlighted;

        public TextPart(String text, boolean highlighted) {
            this.text = text;
            this.highlighted = highlighted;
        }
    }

    /**
     * Formats plain text as html with placeholders and to-remove text in color 
     * @param str the text to format
     * @param color the color to use for placeholders
     * @param placeholderPattern the pattern to decide what is a placeholder
     * @param removePattern the pattern to decide what text had to be removed.
     * @return html text
     */
    private String formatRemoveTagsAndPlaceholders(String str, String color, Pattern placeholderPattern,
            Pattern removePattern, SourceTextEntry.ProtectedParts protectedParts, Map<String, TagError> errors) {
        if (removePattern != null) {
            Matcher removeMatcher = removePattern.matcher(str);
            String htmlResult="";
            int pos=0;
            while (removeMatcher.find()) {
                htmlResult += formatPlaceholders(str.substring(pos, removeMatcher.start()), color, placeholderPattern,
                        protectedParts, errors);
                htmlResult += colorize(htmlize(removeMatcher.group(0)), TagError.EXTRANEOUS);
                pos = removeMatcher.end();
            }
            htmlResult += formatPlaceholders(str.substring(pos), color, placeholderPattern, protectedParts, errors);
            return htmlResult;
        } else {
            return formatPlaceholders(str, color, placeholderPattern, protectedParts, errors);
        }
    }

    public void displayErrorList(List<ErrorReport> errorList) {
        this.m_errorList = errorList;
        update();
    }

    private void update() {
        Pattern placeholderPattern = PatternConsts.getPlaceholderPattern();
        Pattern removePattern = PatternConsts.getRemovePattern();

        m_numFixableErrors = 0;

        StringBuffer output = new StringBuffer();

        output.append("<html>\n");
        output.append("<head>\n");
        output.append("<style>\n");
        output.append("<style type=\"text/css\">\n");
        output.append("    <!--\n");
        output.append("    body {\n");
        output.append("            font-family: " + getFont().getName() + ";\n");
        output.append("            font-size: " + getFont().getSize() + "pt;\n");
        output.append("    }\n");
        output.append("    td {\n");
        output.append("            border: 1px solid gray;\n");
        output.append("    }\n");
        output.append("    -->\n");
        output.append("</style>\n");
        output.append("</head>\n");
        output.append("<body>\n");
        if (message != null) {
            output.append("<b>" + message + "</b>");
        }

        output.append("<table border=\"1\" cellspacing=\"1\" cellpadding=\"2\" width=\"100%\">\n");
        for (ErrorReport report : m_errorList) {
            output.append("<tr>");
            output.append("<td>");
            output.append("<a href=\"");
            output.append(report.entryNum);
            output.append("\"");
            output.append(">");
            output.append(report.entryNum);
            output.append("</a>");
            output.append("</td>");
            output.append("<td>");
            output.append(colorTags(report.source, "blue", placeholderPattern, null, report.ste.getProtectedParts(),
                    report.srcErrors));
            output.append("</td>");
            output.append("<td>");
            output.append(colorTags(report.translation, "blue", placeholderPattern, removePattern,
                    report.ste.getProtectedParts(), report.transErrors));
            output.append("</td>");
            output.append("<td>");
            Set<TagError> allErrors = new HashSet<TagError>(report.srcErrors.values());
            allErrors.addAll(report.transErrors.values());
            for (TagError err : allErrors) {
                output.append(colorize(ErrorReport.localizedTagError(err), err));
                output.append("<br/>");
            }
            if (!allErrors.contains(TagError.UNSPECIFIED)) {
                output.append("<p align=\"right\">&rArr; <a href=\"fix:");
                output.append(report.entryNum);
                output.append("\">");
                output.append(OStrings.getString("TAG_FIX_COMMAND"));
                output.append("</a></p>");
                m_numFixableErrors++;
            }
            output.append("</td>");
            output.append("</tr>\n");
        }
        output.append("</table>\n");
        output.append("</body>\n");
        output.append("</html>\n");

        m_fixAllButton.setEnabled(m_numFixableErrors > 0);
        m_editorPane.setContentType("text/html");
        m_editorPane.setText(output.toString());
        m_editorPane.setCaretPosition(0);
    }

    public void setMessage(String message) {
        this.message = message;
    }


    private String colorize(String text, TagError error) {
        String color = "black";
        if (error != null) {
            switch (error) {
            case EXTRANEOUS:
                text = String.format("<strike>%s</strike>", text);
            case MISSING:
            case MALFORMED:
            case WHITESPACE:
                color = "red";
                break;
            case DUPLICATE:
                color = "purple";
                break;
            case ORPHANED:
                text = String.format("<u>%s</u>", text);
            case ORDER:
                color = "#FF8C00"; // Orange. Pre-1.7 Java doesn't recognize the name "orange".
                break;
            case UNSPECIFIED:
                color = "blue";
            }
        }
        
        return String.format("<font color=\"%s\"><b>%s</b></font>", color, text);
    }
    
    /**
     * Automatically fix the tag errors in a particular entry.
     * @param entryNum The entry to fix
     * @return The source text of the fixed entry
     */
    public String fixEntry(int entryNum) {
        
        ErrorReport report = null;
        
        for (int i = 0; i < m_errorList.size(); i++) {
            
            report = m_errorList.get(i);
            
            if (report.entryNum != entryNum) {
                continue;
            }
            
            if (!doFix(report)) {
                // There was a problem, so show an error dialog.
               JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(),
                       OStrings.getString("TAG_FIX_ERROR_MESSAGE"), OStrings.getString("TAG_FIX_ERROR_TITLE"),
                       JOptionPane.ERROR_MESSAGE);
               this.dispose();
               return null;
            }
            
            if (report.ste.getDuplicate() == SourceTextEntry.DUPLICATE.NONE) {
                m_errorList.remove(i);
            } else {
                m_errorList = Core.getTagValidation().listInvalidTags();
            }
            break;
        }
        
        if (m_errorList.size() > 0) {
            update();
        } else {
            this.dispose();
        }
        
        return report != null ? report.source : null;
    }
    
    /**
     * Automatically fix tag errors in all available entries.
     * @return A list of fixed entries
     */
    private List<Integer> fixAllEntries() {
        List<Integer> fixed = new ArrayList<Integer>();
        for (ErrorReport report : m_errorList) {
            if (!doFix(report) && report.ste.getDuplicate() != SourceTextEntry.DUPLICATE.NEXT) {
                // Fixes will fail on duplicates of previously fixed segments. Ignore this.
                // Otherwise the user must have changed the translation, so show an error dialog.
                JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(),
                        OStrings.getString("TAG_FIX_ERROR_MESSAGE"), OStrings.getString("TAG_FIX_ERROR_TITLE"),
                        JOptionPane.ERROR_MESSAGE);
                break;
            }
            fixed.add(report.entryNum);
        }
        this.dispose();
        
        return fixed;
    }
    
    /**
     * Fix all errors in a given report, and commit the changed translation to the project.
     * Checks to make sure the translation has not been changed in the meantime.
     * 
     * @param report The report to fix
     * @return Whether or not the fix succeeded
     */
    private boolean doFix(ErrorReport report) {
        // Make sure the translation hasn't changed in the editor.
        TMXEntry prevTrans = Core.getProject().getTranslationInfo(report.ste);
        if (!report.translation.equals(prevTrans.translation)) {
            return false;
        }
        
        String fixed = TagValidationTool.fixErrors(report);
        
        // Put modified translation back into project.
        if (fixed != null) {
            Core.getProject().setTranslation(report.ste, fixed, prevTrans.note, prevTrans.defaultTranslation);
        }
        
        return true;
    }
    
    /** The URL prefix given to "Fix" links in the Tag Validation window */
    public static final String FIX_URL_PREFIX = "fix:";
    private String message;
    private JEditorPane m_editorPane;
    private List<ErrorReport> m_errorList;
    private JButton m_fixAllButton;
    private int m_numFixableErrors;
}
