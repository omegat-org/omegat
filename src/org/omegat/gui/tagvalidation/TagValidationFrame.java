/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers
               2007 Didier Briel
               2008-2009 Martin Fleurke
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

package org.omegat.gui.tagvalidation;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TransEntry;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.gui.HListener;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;
import org.openide.awt.Mnemonics;

/**
 * A frame to display the tags with errors during tag validation.
 * 
 * @author Keith Godfrey
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Didier Briel
 * @author Martin Fleurke
 */
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

        m_editorPane = new JEditorPane();
        m_editorPane.setEditable(false);
        m_editorPane.addHyperlinkListener(new HListener(parent, true)); // fix
                                                                        // for
                                                                        // bug
                                                                        // 1542937
        JScrollPane scroller = new JScrollPane(m_editorPane);

        Box bbut = Box.createHorizontalBox();
        bbut.add(Box.createHorizontalGlue());
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
    private String colorTags(String str, String color, Pattern printfPattern) {
        // show OmegaT tags in bold and color
        Matcher tagMatch = PatternConsts.OMEGAT_HTML_TAG.matcher(str);
        str = tagMatch.replaceAll("<font color=\"" + color + "\"><b>$1</b></font>");
        // show linefeed as symbol
        Matcher lfMatch = PatternConsts.HTML_BR.matcher(str);
        // /simulate unicode symbol for linefeed "\u240A", which is not
        // displayed correctly.
        str = lfMatch.replaceAll("<font color=\"" + color + "\"><sup>L</sup>F<br></font>");
        // show printf variables in bold and color (e.g. %s and %n\$s)
        if (printfPattern != null) {
            Matcher varMatch = PatternConsts.PRINTF_VARS.matcher(str);
            str = varMatch.replaceAll("<font color=\"" + color + "\"><b>$0</b></font>");
        }
        return str;
    }

    public void displayStringList(List<SourceTextEntry> stringList) {
        this.stringList = stringList;
        update();
    }

    private void update() {
        Pattern printfPattern = null;
        if ("true".equalsIgnoreCase(Preferences.getPreference(Preferences.CHECK_ALL_PRINTF_TAGS))) {
            printfPattern = PatternConsts.PRINTF_VARS;
        } else if ("true".equalsIgnoreCase(Preferences.getPreference(Preferences.CHECK_SIMPLE_PRINTF_TAGS))) {
            printfPattern = PatternConsts.SIMPLE_PRINTF_VARS;
        }
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
        output.append("    -->\n");
        output.append("</style>\n");
        output.append("</head>\n");
        output.append("<body>\n");

        output.append("<table BORDER COLS=3 WIDTH=\"100%\" NOSAVE>\n");
        for (SourceTextEntry ste : stringList) {
            String src = ste.getSrcText();
            TransEntry trans = Core.getProject().getTranslation(ste);
            if (src.length() > 0 && trans != null) {
                output.append("<tr>");
                output.append("<td>");
                output.append("<a href=");
                output.append("\"");
                output.append(ste.entryNum());
                output.append("\"");
                output.append(">");
                output.append(ste.entryNum());
                output.append("</a>");
                output.append("</td>");
                output.append("<td>");
                output.append(colorTags(htmlize(src), "blue", printfPattern));
                output.append("</td>");
                output.append("<td>");
                output.append(colorTags(htmlize(trans.translation), "blue", printfPattern));
                output.append("</td>");
                output.append("</tr>\n");
            }
        }
        output.append("</table>\n");
        output.append("</body>\n");
        output.append("</html>\n");

        m_editorPane.setContentType("text/html");
        m_editorPane.setText(output.toString());
    }

    private JEditorPane m_editorPane;
    private List<SourceTextEntry> stringList;
}