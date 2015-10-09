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

 OmegaT is distributed in the hope that it will be useful,
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
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.core.tagvalidation.ErrorReport;
import org.omegat.core.tagvalidation.ErrorReport.TagError;
import org.omegat.gui.HListener;
import org.omegat.gui.common.OmegaTIcons;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.TagUtil.Tag;
import org.omegat.util.gui.StaticUIUtils;
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

        OmegaTIcons.setIconImages(this);

        // set window size & position
        initWindowLayout();
        
        Action escapeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        };
        StaticUIUtils.setEscapeAction(this, escapeAction);

        // Configure close button
        JButton closeButton = new JButton();
        Mnemonics.setLocalizedText(closeButton, OStrings.getString("BUTTON_CLOSE"));
        closeButton.addActionListener(escapeAction);

        // Fix All button
        m_fixAllButton = new JButton();
        Mnemonics.setLocalizedText(m_fixAllButton, OStrings.getString("BUTTON_FIX_ALL"));
        m_fixAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(Core.getMainWindow().getApplicationFrame(),
                        StringUtil.format(OStrings.getString("TAG_FIX_ALL_WARNING"), m_numFixableErrors),
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
            @Override
            public void onFontChanged(Font newFont) {
                TagValidationFrame.this.setFont(newFont);
            }
        });
        setFont(Core.getMainWindow().getApplicationFont());
    }

    /** Call this to set OmegaT-wide font for the Tag Validation window. */
    @Override
    public final void setFont(Font f) {
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

    @Override
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
    private String colorTags(String str, Map<Tag, TagError> errors) {
        
        StringBuilder html = new StringBuilder(str);
        List<Tag> tags = new ArrayList<Tag>(errors.keySet());
        // Sort in reverse order so that tag offsets remain correct as we replace things.
        Collections.sort(tags, new Comparator<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return o1.pos < o2.pos ? 1
                    : o1.pos > o2.pos ? -1
                    : 0;
            }
        });
        
        int lastIndex = html.length();
        for (Tag tag : tags) {
            // HTML-escape everything after this tag (up to end, or previous tag)
            int end = tag.pos + tag.tag.length();
            String tail = html.substring(end, lastIndex);
            html.replace(end, lastIndex, htmlize(tail));
            html.replace(tag.pos, end, colorize(htmlize(tag.tag), errors.get(tag)));
            lastIndex = tag.pos;
        }
        // Don't forget to escape everything before the first tag.
        String head = html.substring(0, lastIndex);
        html.replace(0, lastIndex, htmlize(head));
        
        // show linefeed as symbol
        Matcher lfMatch = PatternConsts.HTML_BR.matcher(html);
        // /simulate unicode symbol for linefeed "\u240A", which is not
        // displayed correctly.
        return lfMatch.replaceAll("<font color=\"blue\"><sup>L</sup>F<br></font>");
    }

    public void displayErrorList(List<ErrorReport> errorList) {
        this.m_errorList = errorList;
        update();
    }

    private void update() {
        m_numFixableErrors = 0;

        StringBuilder output = new StringBuilder();

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
            output.append(colorTags(report.source, report.srcErrors));
            output.append("</td>");
            output.append("<td>");
            output.append(colorTags(report.translation, report.transErrors));
            output.append("</td>");
            output.append("<td width=\"10%\">");
            // Although NetBeans mentions that the HashSet can be replaced with java.util.EnumSet
            // Set<TagError> allErrors = EnumSet.copyOf(report.srcErrors.values());
            // creates a runtime exception in some cases, while the HashSet does not
            Set<TagError> allErrors = new HashSet<TagError>(report.srcErrors.values());
            allErrors.addAll(report.transErrors.values());
            for (TagError err : allErrors) {
                output.append(colorize(ErrorReport.localizedTagError(err), err));
                output.append("<br/>");
            }
            if (!allErrors.contains(TagError.UNSPECIFIED)) {
                output.append("<p align=\"right\">&rArr;&nbsp;<a href=\"fix:");
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
                color = "#FF8C00"; // Orange. Pre-1.7 Java doesn't recognize the name "orange".
                break;
            case UNSPECIFIED:
                color = "blue";
            }
        }
        
        return "<font color=\"" + color + "\"><b>" + text + "</b></font>";
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
        
        if (m_errorList != null && !m_errorList.isEmpty()) {
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
            PrepareTMXEntry tr = new PrepareTMXEntry();
            tr.source = report.ste.getSrcText();
            tr.translation = fixed;
            tr.note = prevTrans.note;
            Core.getProject().setTranslation(report.ste, tr, prevTrans.defaultTranslation, null);
        }
        
        return true;
    }
    
    /** The URL prefix given to "Fix" links in the Tag Validation window */
    public static final String FIX_URL_PREFIX = "fix:";
    private String message;
    private final JEditorPane m_editorPane;
    private List<ErrorReport> m_errorList;
    private JButton m_fixAllButton;
    private int m_numFixableErrors;
}
