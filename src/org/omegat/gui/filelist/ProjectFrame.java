/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Kim Bruning
           (C) 2007 Zoltan Bartko
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

package org.omegat.gui.filelist;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;

import org.omegat.core.Core;
import org.omegat.core.data.CommandThread;
import org.omegat.gui.HListener;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.openide.awt.Mnemonics;

/**
 * A frame for project,
 * showing all the files of the project.
 *
 * @author Keith Godfrey
 * @author Kim Bruning
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Zoltan Bartko
 */
public class ProjectFrame extends JFrame
{
    public ProjectFrame(MainWindow parent)
    {
        m_parent = parent;
        
        m_nameList = new ArrayList<String>(256);
        m_offsetList = new ArrayList<Integer>(256);

        // set the position and size
        initWindowLayout();

        Container cp = getContentPane();
        m_editorPane = new JEditorPane();
        m_editorPane.setEditable(false);
        m_editorPane.setContentType("text/html");                     // NOI18N
        JScrollPane scroller = new JScrollPane(m_editorPane);
        cp.add(scroller, "Center");                                   // NOI18N
        
        m_addNewFileButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(m_addNewFileButton,
                                                   OStrings.getString("TF_MENU_FILE_IMPORT"));
        m_addNewFileButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doImportSourceFiles();
            }
        });
	m_wikiImportButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(m_wikiImportButton,
                                                   OStrings.getString("TF_MENU_WIKI_IMPORT"));
        m_wikiImportButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doWikiImport();
            }
        });

        uiUpdateImportButtonStatus();

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

        
        Box bbut = Box.createHorizontalBox();
        bbut.add(Box.createHorizontalGlue());
        bbut.add(m_addNewFileButton);
        bbut.add(m_wikiImportButton);
        bbut.add(m_closeButton);
        bbut.add(Box.createHorizontalGlue());
        cp.add(bbut, "South");                                                  // NOI18N
        
        m_editorPane.addHyperlinkListener(new HListener(m_parent, true));

        Mnemonics.setLocalizedText(m_closeButton, OStrings.getString("BUTTON_CLOSE"));
        setTitle(OStrings.getString("PF_WINDOW_TITLE"));
        uiUpdateImportButtonStatus();
        
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        setBounds((screenSize.width-600)/2, (screenSize.height-500)/2, 600, 400);
    }

    /**
      * Loads/sets the position and size of the search window.
      */
    private void initWindowLayout()
    {
        // main window
        try
        {
            String dx = Preferences.getPreference(Preferences.PROJECT_FILES_WINDOW_X);
            String dy = Preferences.getPreference(Preferences.PROJECT_FILES_WINDOW_Y);
            int x = Integer.parseInt(dx);
            int y = Integer.parseInt(dy);
            setLocation(x, y);
            String dw = Preferences.getPreference(Preferences.PROJECT_FILES_WINDOW_WIDTH);
            String dh = Preferences.getPreference(Preferences.PROJECT_FILES_WINDOW_HEIGHT);
            int w = Integer.parseInt(dw);
            int h = Integer.parseInt(dh);
            setSize(w, h);
        }
        catch (NumberFormatException nfe)
        {
            // set default size and position
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setBounds((screenSize.width - 600) / 2, (screenSize.height - 400) / 2, 600, 400);
        }
    }

    /**
      * Saves the size and position of the search window
      */
    private void saveWindowLayout()
    {
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_WIDTH, getWidth());
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_HEIGHT, getHeight());
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_X, getX());
        Preferences.setPreference(Preferences.PROJECT_FILES_WINDOW_Y, getY());
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
        //dispose();
        setVisible(false);
    }

    public void reset()
    {
        m_nameList.clear();
        m_offsetList.clear();
        m_editorPane.setText("");                                               // NOI18N
        uiUpdateImportButtonStatus();
    }
    
    public void addFile(String name, int entryNum)
    {
        m_nameList.add(name);
        m_offsetList.add(entryNum);
    }
    
    /**
     * Builds the table which lists all the project files.
     */
    public void buildDisplay()
    {
        if( m_nameList==null || m_offsetList==null || m_nameList.isEmpty() )
            return;
        
        StringBuffer output = new StringBuffer();
        
        output.append("<html>\n");                                              // NOI18N
        output.append("<head>\n");                                              // NOI18N
        output.append("<style>\n");                                             // NOI18N
        output.append("<style type=\"text/css\">\n");                           // NOI18N
        output.append("    <!--\n");                                            // NOI18N
        output.append("    body {\n");                                          // NOI18N
        output.append("            font-family: "+getFont().getName()+";\n");   // NOI18N
        output.append("            font-size: "+getFont().getSize()+"pt;\n");   // NOI18N
        output.append("    }\n");                                               // NOI18N
        output.append("    -->\n");                                             // NOI18N
        output.append("</style>\n");                                            // NOI18N
        output.append("</head>\n");                                             // NOI18N
        output.append("<body>\n");                                              // NOI18N
        
        output.append("<table align=center width=95% border=0>\n");             // NOI18N
        output.append("<tr>\n");                                                // NOI18N
        output.append("<th width=80% align=center>");                           // NOI18N
        output.append(OStrings.getString("PF_FILENAME"));                       // NOI18N
        output.append("</th>\n");                                               // NOI18N
        output.append("<th width=20% align=center>");                           // NOI18N
        output.append(OStrings.getString("PF_NUM_SEGMENTS"));                   // NOI18N
        output.append("</th>\n");                                               // NOI18N
        output.append("</tr>\n");                                               // NOI18N
        int firstEntry = 1;
        int entriesUpToNow = 0;
        String currentFile = Core.getEditor().getCurrentFile();
        for (int i=0; i<m_nameList.size(); i++)
        {
            String name = m_nameList.get(i);
            entriesUpToNow = m_offsetList.get(i);
            int size = 1+entriesUpToNow-firstEntry;
            
            String tableRowTag;
            if (name.equals(currentFile))
                tableRowTag = "<tr bgcolor=\"#C8DDF2\">\n";                     // NOI18N
            else
                tableRowTag = "<tr>\n";                                         // NOI18N
            
            output.append(tableRowTag);                                         // NOI18N
            output.append("<td width=80%>");                                    // NOI18N
            output.append("<a href=\""+firstEntry+"\">"+name+"</a>");       // NOI18N
            output.append("</td>\n");                                           // NOI18N
            output.append("<td width=20% align=center>");                       // NOI18N
            output.append(size);                                                // NOI18N
            output.append("</td>\n");                                           // NOI18N
            output.append("</tr>\n");                                           // NOI18N
            
            firstEntry = entriesUpToNow+1;
        }
        
        if (m_nameList.size()>1)
        {
            output.append("<tr>\n");                                            // NOI18N
            output.append("<td width=80%><b>");                                 // NOI18N
            output.append(OStrings.getString("GUI_PROJECT_TOTAL_SEGMENTS"));
            output.append("</b></td>\n");                                       // NOI18N
            output.append("<td width=20% align=center><b>");                    // NOI18N
            output.append(CommandThread.core.getNumberOfSegmentsTotal());
            output.append("</b></td>\n");                                       // NOI18N
            output.append("</tr>\n");                                           // NOI18N
        }
        output.append("<tr>\n");                                                // NOI18N
        output.append("<td width=80%><b>");                                     // NOI18N
        output.append(OStrings.getString("GUI_PROJECT_UNIQUE_SEGMENTS"));
        output.append("</b></td>\n");                                           // NOI18N
        output.append("<td width=20% align=center><b>");                        // NOI18N
        output.append(CommandThread.core.getNumberOfUniqueSegments());
        output.append("</b></td>\n");                                           // NOI18N
        output.append("</tr>\n");                                               // NOI18N
        output.append("<tr>\n");                                                // NOI18N
        output.append("<td width=80%><b>");                                     // NOI18N
        output.append(OStrings.getString("GUI_PROJECT_TRANSLATED"));
        output.append("</b></td>\n");                                           // NOI18N
        output.append("<td width=20% align=center id=\"nts\"><b>");             // NOI18N
        output.append(CommandThread.core.getNumberofTranslatedSegments());
        output.append("</b></td>\n");                                           // NOI18N
        output.append("</tr>\n");                                               // NOI18N
        
        output.append("</table>\n");                                            // NOI18N
        output.append("</body>\n");                                             // NOI18N
        output.append("</html>\n");                                             // NOI18N
        
        m_editorPane.setText(output.toString());
        uiUpdateImportButtonStatus();
    }
    
    /** 
     * Updates the number of translated segments only, 
     * does not rebuild the whole display.
     */
    public void updateNumberOfTranslatedSegments()
    {
        try
        {
            HTMLDocument doc = (HTMLDocument) m_editorPane.getDocument();
            Element elem = doc.getElement("nts");                               // NOI18N
            int nts = CommandThread.core.getNumberofTranslatedSegments();
            doc.setInnerHTML(elem, "<b>"+nts+"</b>");                           // NOI18N
        }
        catch( Exception e ) { }
    }
    
    /**
     * Imports the file/files/folder into project's source files.
     * @author Kim Bruning
     * @author Maxym Mykhalchuk
     */
    private void doImportSourceFiles()
    {
        m_parent.doImportSourceFiles();
    }
    
    private void doWikiImport()
    {
        m_parent.doWikiImport();
    }
    /** Updates the Import Files button status. */
    public void uiUpdateImportButtonStatus()
    {
        m_addNewFileButton.setEnabled(m_parent.isProjectLoaded());
        m_wikiImportButton.setEnabled(m_parent.isProjectLoaded());
    }

    /** Call this to set OmegaT-wide font for this window. */
    public void setFont(Font f)
    {
        super.setFont(f);
        buildDisplay();
    }
    
    private JEditorPane m_editorPane;
    private JButton     m_addNewFileButton;
    private JButton     m_wikiImportButton;
    private JButton     m_closeButton;
    private List<String>   m_nameList;
    private List<Integer>   m_offsetList;
    
    private MainWindow  m_parent;
}

