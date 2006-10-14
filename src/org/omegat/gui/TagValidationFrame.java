/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
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

package org.omegat.gui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.omegat.core.StringEntry;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.openide.awt.Mnemonics;
import org.omegat.gui.main.MainWindow;

/**
 * A frame to display the tags with errors during tag validation.
 *
 * @author Keith Godfrey
 */
public class TagValidationFrame extends JFrame
{
    public TagValidationFrame(MainWindow parent)
    {
        m_parent = parent;

        // set window size & position
        initWindowLayout();

        Container cp = getContentPane();
        m_editorPane = new JEditorPane();
        m_editorPane.setEditable(false);
        JScrollPane scroller = new JScrollPane(m_editorPane);
        cp.add(scroller, "Center");    // NOI18N

        Box bbut = Box.createHorizontalBox();
        bbut.add(Box.createHorizontalGlue());
        bbut.add(m_closeButton);
        bbut.add(Box.createHorizontalGlue());
        cp.add(bbut, "South");    // NOI18N

        m_editorPane.addHyperlinkListener(new HListener(m_parent, false));

        // Configure close button
        m_closeButton = new JButton();
        m_closeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doCancel();
            }
        });

        //  Handle escape key to close the window
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                doCancel();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
        put(escape, "ESCAPE");                                                  // NOI18N
        getRootPane().getActionMap().put("ESCAPE", escapeAction);               // NOI18N

        updateUIText();
    }
    
    /**
      * Loads/sets the position and size of the tag validation window.
      */
    private void initWindowLayout()
    {
        // main window
        try
        {
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
        }
        catch (NumberFormatException nfe)
        {
            // set default size and position
            setSize(650, 700);
        }
    }
    
    /**
      * Saves the size and position of the tag validation window
      */
    private void saveWindowLayout()
    {
        Preferences.setPreference(Preferences.TAGVWINDOW_WIDTH, getWidth());
        Preferences.setPreference(Preferences.TAGVWINDOW_HEIGHT, getHeight());
        Preferences.setPreference(Preferences.TAGVWINDOW_X, getX());
        Preferences.setPreference(Preferences.TAGVWINDOW_Y, getY());
    }
    
    public void processWindowEvent(WindowEvent w)
    {
        int evt = w.getID();
        if (evt == WindowEvent.WINDOW_CLOSING || evt == WindowEvent.WINDOW_CLOSED) {
            // save window size and position
            saveWindowLayout();
        }
        super.processWindowEvent(w);
    }

    private void doCancel()
    {
        dispose();
    }

    private void updateUIText()
    {
        Mnemonics.setLocalizedText(m_closeButton, OStrings.getString("BUTTON_CLOSE"));
    }

    /** replaces all &lt; and &gt; with &amp;lt; and &amp;gt; */
    private String htmlize(String str)
    {
        String htmld = str;
        htmld = htmld.replaceAll("\\<", "&lt;");                                // NOI18N
        htmld = htmld.replaceAll("\\>", "&gt;");                                // NOI18N
        return htmld;
    }
    
    public void displayStringList(ArrayList stringList)
    {
        setTitle(OStrings.TF_NOTICE_BAD_TAGS);
        String out;
        String src;
        String trans;
        SourceTextEntry ste;
        StringEntry se;

        out = "<table BORDER COLS=3 WIDTH=\"100%\" NOSAVE>";                    // NOI18N
        for (int i=0; i<stringList.size(); i++)
        {
            ste = (SourceTextEntry) stringList.get(i);
            se = ste.getStrEntry();
            src = se.getSrcText();
            trans = se.getTranslation();
            if (!src.equals("") && !trans.equals(""))        // NOI18N
            {
                out += "<tr>";                                                    // NOI18N
                out += "<td><a href=\"" + (ste.entryNum()+ 1) + "\">";            // NOI18N
                out += (ste.entryNum() + 1) + "</a></td>";                        // NOI18N
                out += "<td>" + htmlize(src) + "</td>";                            // NOI18N
                out += "<td>" + htmlize(trans) + "</td>";                        // NOI18N
                out += "</tr>";                                                    // NOI18N
            }
        }
        out += "</table>";                                                        // NOI18N
        m_editorPane.setContentType("text/html");                                // NOI18N
        m_editorPane.setText(out);
    }

    private JEditorPane m_editorPane;
    private JButton        m_closeButton;

    private MainWindow m_parent;

}